/*
 * #%L
 * ImageJ software for multidimensional image processing and analysis.
 * %%
 * Copyright (C) 2009 - 2014 Board of Regents of the University of
 * Wisconsin-Madison, Broad Institute of MIT and Harvard, and Max Planck
 * Institute of Molecular Cell Biology and Genetics.
 * %%
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * #L%
 */

package imagej.plugins.commands.display.interactive;

import imagej.command.Command;
import imagej.data.Dataset;
import imagej.data.autoscale.AutoscaleService;
import imagej.data.autoscale.DataRange;
import imagej.data.command.InteractiveImageCommand;
import imagej.data.display.ImageDisplay;
import imagej.data.display.ImageDisplayService;
import imagej.data.display.event.AxisPositionEvent;
import imagej.data.overlay.ThresholdOverlay;
import imagej.data.threshold.ThresholdMethod;
import imagej.data.threshold.ThresholdService;
import imagej.data.widget.HistogramBundle;
import imagej.menu.MenuConstants;
import imagej.module.MutableModuleItem;
import imagej.ui.DialogPrompt;
import imagej.ui.UIService;
import imagej.widget.Button;
import imagej.widget.NumberWidget;
import net.imglib2.Cursor;
import net.imglib2.IterableInterval;
import net.imglib2.histogram.Histogram1d;
import net.imglib2.histogram.Real1dBinMapper;
import net.imglib2.img.Img;
import net.imglib2.meta.AxisType;
import net.imglib2.meta.ImgPlus;
import net.imglib2.type.numeric.RealType;
import net.imglib2.view.IntervalView;
import net.imglib2.view.Views;

import org.scijava.ItemIO;
import org.scijava.event.EventHandler;
import org.scijava.plugin.Menu;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.util.Colors;

// TODO All the problems with thresh overlay code at the moment:
//
//  - when thresh drawn at 3/2 scale on boats it looks gridded. JHot prob?
//  - we will have to display a histogram and thresh lines like IJ1 does
//  - overlay not selectable in view but only via ovr mgr
//  - do thresh overlays kill graphics of other overlays? It seems it may.
//     Might need to draw in a certain order
//  - update commands that need a threshold to use them now
//  - this plugin written to work with one display. So if you leave it up and
//     switch images this plugin won't immediately work with it. see what IJ1
//     does.
//  - the min and max are not rounded to integers. And dark/light bounce reuses
//     the cutoff instead of cutoff + 1. Think how best to calc and show range.
//     There is a related TODO below.
//  - should we make binary images rather than 0/255? Or just call convert to
//     mask? or is it fine?
//  - we delete threshold when someone Apply's the current thresh method to
//     change the existing pixels. if we don't then the threshold is overdrawn
//     but still exists. It is not immediately visible and making it so will
//     maybe display poorly. Do we want to kill thresh or not? TODO below.
//  - there is a disconnect with thresh overlays and other overlays. thresh
//     overlays are concerned with a single dataset. overlays in general apply
//     to displays. We now have plugins that take as an input a display and
//     finds the active dataset. These plugins should work on datasets directly.
//     And we might have a display with 3 datasets in it. The thresh service
//     only registers a single thresh overlay per display. Need to discuss this
//     further. I'm sure CTR had ideas about overlays across datasets in a
//     display.
//  - fix code that determines histogram table size. The existing code is just
//     a simple approach
//  - make min and max fields into sliders. There is a related TODO below.
//  - note that as designed the stacked histogram is always generated at start.
//     We could try different approach to speed dialog appearence. But if we do
//     we need to calc at some point. BTW the design as is will be invalid if
//     someone opens thresh command, then uses other plugin to change data
//     values because the stacked (and other) histograms will not be updated.
//     Maybe handle dataset updated events to recalc hist as needed.

/**
 * Threshold is the {@link Command} that allows one to interactively create and
 * manipulate {@link ThresholdOverlay}s for a {@link ImageDisplay}. It uses
 * autodiscovered {@link ThresholdMethod}s.
 * 
 * @author Barry DeZonia
 */
@Plugin(type = Command.class, menu = {
	@Menu(label = MenuConstants.IMAGE_LABEL, weight = MenuConstants.IMAGE_WEIGHT,
		mnemonic = MenuConstants.IMAGE_MNEMONIC), @Menu(label = "Adjust"),
	@Menu(label = "Threshold...", accelerator = "shift ^T") },
	initializer = "initValues")
public class Threshold<T extends RealType<T>> extends InteractiveImageCommand {

	// -- constants --
	
	private static final String RED = "Red";
	private static final String BLACK_WHITE = "Black/White";
	private static final String OVER_UNDER = "Over/Under";
	
	// -- Parameters --

	@Parameter(label = "Histogram")
	private HistogramBundle histBundle;

	@Parameter(label = "Display Type",
		choices = { RED, BLACK_WHITE, OVER_UNDER },
		callback = "displayTypeChanged", persist = false)
	private String displayType = RED;

	@Parameter(label = "Method", callback = "autoThreshold", persist = false)
	private ThresholdMethod method;

	@Parameter(label = "Auto", callback = "autoThreshold")
	private Button auto;

	@Parameter(label = "Apply", callback = "changePixels")
	private Button apply;

	@Parameter(label = "Delete", callback = "deleteThreshold")
	private Button delete;

	@Parameter(label = "Dark Background", callback = "backgroundChange",
		persist = false)
	private boolean darkBackground;

	@Parameter(label = "Stack Histogram", callback = "stackHistogram",
		persist = false)
	private boolean stackHistogram = true;

	@Parameter(label = "Nan Background", persist = false)
	private boolean nanBackground;

	@Parameter(label = "Minimum", callback = "rangeChanged", persist = false,
		style = NumberWidget.SCROLL_BAR_STYLE)
	private double minimum;

	@Parameter(label = "Maximum", callback = "rangeChanged", persist = false,
		style = NumberWidget.SCROLL_BAR_STYLE)
	private double maximum;

	@Parameter(type = ItemIO.BOTH)
	private ImageDisplay display;

	@Parameter
	private ThresholdService threshSrv;

	@Parameter
	private ImageDisplayService imgDispSrv;

	@Parameter
	private AutoscaleService autoscaleService;

	@Parameter
	private UIService uiSrv;

	// -- instance variables --

	private Histogram1d<T> fullHistogram;

	private Histogram1d<T> planeHistogram;

	private boolean invalidPlaneHist = true;

	private DataRange minMax;

	// -- accessors --

	public void setImageDisplay(final ImageDisplay disp) {
		display = disp;
	}

	public ImageDisplay getImageDisplay() {
		return display;
	}

	// -- Command methods --

	@Override
	public void run() {
		// nothing to do
	}

	// -- initializers --

	protected void initValues() {
		if (display == null) return;

		boolean alreadyHadOne = threshSrv.hasThreshold(display);
		ThresholdOverlay overlay = threshSrv.getThreshold(display);

		minMax = calcDataRange();

		fullHistogram = buildHistogram(true, null);
		planeHistogram = null;
		invalidPlaneHist = true;

		if (!alreadyHadOne) {
			// default the thresh to something sensible: 85/170 of 255 is IJ1 default
			double mn = 1 * minMax.getExtent() / 3;
			double mx = 2 * minMax.getExtent() / 3;
			overlay.setRange(mn, mx);
		}

		// TEMP HACK: finding bin numbers of curr min/max values for drawing lines
		// on chart later. Kludgy since really we should use hist.map(T value)
		// but we don't have a T here. Argh.
		long binCount = fullHistogram.getBinCount();
		long minBin = calcBin(binCount, overlay.getRangeMin());
		long maxBin = calcBin(binCount, overlay.getRangeMax());

		histBundle = new HistogramBundle(fullHistogram);
		histBundle.setMinBin(minBin);
		histBundle.setMaxBin(maxBin);

		// TODO note
		// The threshold ranges would be best as a slider with range ends noted.
		// However the current number widget code cannot handle sliders/scrolls on
		// doubles but just ints. Make that widget allow doubles and then change the
		// the widget style for min and max here below.

		// set min range widget
		final MutableModuleItem<Double> minItem =
			getInfo().getMutableInput("minimum", Double.class);
		minItem.setMinimumValue(minMax.getMin());
		minItem.setMaximumValue(minMax.getMax());
		minItem.setValue(this, overlay.getRangeMin());

		// set max range widget
		final MutableModuleItem<Double> maxItem =
			getInfo().getMutableInput("maximum", Double.class);
		maxItem.setMinimumValue(minMax.getMin());
		maxItem.setMaximumValue(minMax.getMax());
		maxItem.setValue(this, overlay.getRangeMax());

		// initialize the colors of the overlay
		colorize(overlay);
	}

	// -- callbacks --

	protected void autoThreshold() {
		Histogram1d<T> hist = histogram();
		long cutoff = method.getThreshold(hist);
		if (cutoff < 0) {
			DialogPrompt dialog =
				uiSrv.getDefaultUI().dialogPrompt(method.getMessage(),
				"Thresholding failure", DialogPrompt.MessageType.INFORMATION_MESSAGE,
				DialogPrompt.OptionType.DEFAULT_OPTION);
			dialog.prompt();
			return;
		}
		else if (method.getMessage() != null) {
			log().warn(method.getMessage());
		}
		double maxRange = hist.max();
		// TODO : what is best increment? To avoid roundoff errs use a teeny inc
		// (like 0.0001 instead of 1). But then result does not match IJ1. With
		// teeny inc dark bckgrnd bounces from (0,cutoff) to (cutoff,255) rather
		// than (0,cutoff) to (cutoff+1,255). Think how to best display range values
		// so that they don't have gaps when displayed in dialog bouncing between
		// light and dark background.
		double bot = (darkBackground) ? cutoff + 1 : 0;
		double top = (darkBackground) ? maxRange : cutoff;
		minimum = minMax.getMin() + (bot / maxRange) * (minMax.getExtent());
		maximum = minMax.getMin() + (top / maxRange) * (minMax.getExtent());
		rangeChanged();
	}

	protected void backgroundChange() {
		autoThreshold();
	}

	protected void changePixels() {
		ThresholdOverlay thresh = getThreshold();
		Dataset ds = imgDispSrv.getActiveDataset(display);
		ImgPlus<? extends RealType<?>> imgPlus = ds.getImgPlus();
		Cursor<? extends RealType<?>> cursor = imgPlus.cursor();
		double typeMax = cursor.get().getMaxValue();
		boolean setOffOnly = nanBackground && !ds.isInteger();
		double OFF = (setOffOnly) ? Double.NaN : 0;
		double ON = (typeMax < 255) ? typeMax : 255;
		double min = thresh.getRangeMin();
		double max = thresh.getRangeMax();
		while (cursor.hasNext()) {
			cursor.fwd();
			double value = cursor.get().getRealDouble();
			boolean set;
			if (value < min || value > max || Double.isNaN(value)) {
				value = OFF;
				set = true;
			}
			else {
				value = ON;
				set = !setOffOnly;
			}
			if (set) cursor.get().setReal(value);
		}
		deleteThreshold(); // TODO - maybe not.
		ds.update();
	}

	protected void deleteThreshold() {
		threshSrv.removeThreshold(display);
	}

	protected void displayTypeChanged() {
		ThresholdOverlay overlay = getThreshold();
		overlay.update();
	}

	protected void rangeChanged() {
		double min = (Double) getInput("minimum");
		double max = (Double) getInput("maximum");
		ThresholdOverlay overlay = getThreshold();
		overlay.setRange(min, max);
		display.update();
		updateBundle(min, max);
	}

	protected void stackHistogram() {
		autoThreshold();
	}

	// -- EventHandlers --

	@EventHandler
	protected void onEvent(AxisPositionEvent evt) {
		if (evt.getDisplay() != display) return;
		invalidPlaneHist = true;
		/*
		ThresholdOverlay overlay = getThreshold();
		updateBundle(overlay.getRangeMin(), overlay.getRangeMax());
		// TODO - axis pos events should refresh the hist widget somehow
		 */
	}

	// -- helpers --

	private ThresholdOverlay getThreshold() {
		ThresholdOverlay overlay = threshSrv.getThreshold(display);
		colorize(overlay);
		return overlay;
	}

	private Histogram1d<T> histogram() {
		if (stackHistogram) return fullHistogram;
		if (invalidPlaneHist) {
			// null is on purpose. we want new histograms to certainly update the
			// HistogramBundle so plane changes always reflected in panel.
			planeHistogram = buildHistogram(false, null);
			invalidPlaneHist = false;
		}
		return planeHistogram;
	}

	private void colorize(ThresholdOverlay overlay) {
		if (displayType.equals(BLACK_WHITE)) {
			overlay.setColorWithin(Colors.WHITE);
			overlay.setColorLess(Colors.BLACK);
			overlay.setColorGreater(Colors.BLACK);
		}
		else if (displayType.equals(OVER_UNDER)) {
			overlay.setColorWithin(null);
			overlay.setColorLess(Colors.BLUE);
			overlay.setColorGreater(Colors.GREEN);
		}
		else { // equals RED
			overlay.setColorWithin(Colors.RED);
			overlay.setColorLess(null);
			overlay.setColorGreater(null);
		}
	}
	
	// calcs the data range of the whole dataset

	private DataRange calcDataRange() {
		Dataset ds = imgDispSrv.getActiveDataset(display);
		return autoscaleService.getDefaultIntervalRange(ds.getImgPlus());
	}

	// builds the histogram from either the whole data range or the currently
	// viewed plane

	private Histogram1d<T> buildHistogram(boolean allData,
		Histogram1d<T> existingHist)
	{
		// return buildHistogramFromPointSets(allData, existingHist);
		return buildHistogramFromViews(allData, existingHist);
	}

	private Histogram1d<T> buildHistogramFromViews(boolean allData,
		Histogram1d<T> existingHist)
	{
		Dataset ds = imgDispSrv.getActiveDataset(display);
		long[] min = new long[ds.numDimensions()];
		long[] max = min.clone();
		max[0] = ds.dimension(0) - 1;
		max[1] = ds.dimension(1) - 1;
		for (int d = 2; d < ds.numDimensions(); d++) {
			if (allData) {
				min[d] = 0;
				max[d] = ds.dimension(d) - 1;
			}
			else { // viewed data only
				AxisType axisType = ds.axis(d).type();
				long pos = display.getLongPosition(axisType);
				min[d] = pos;
				max[d] = pos;
			}
		}
		@SuppressWarnings("unchecked")
		Img<T> img = (Img<T>) ds.getImgPlus();
		IntervalView<T> view = Views.interval(img, min, max);
		IterableInterval<T> data = Views.iterable(view);
		final Histogram1d<T> histogram;
		if (existingHist == null) {
			// +1 needed for int but maybe not float
			histogram = allocateHistogram(ds.isInteger(), minMax);
		}
		else {
			existingHist.resetCounters();
			histogram = existingHist;
		}
		histogram.countData(data);
		return histogram;
	}

	private Histogram1d<T> allocateHistogram(boolean dataIsIntegral,
		DataRange dataRange)
	{
		double range = dataRange.getExtent();
		if (dataIsIntegral) range++;
		Real1dBinMapper<T> binMapper = null;
		// TODO - size of histogram affects speed of all autothresh methods
		// What is the best way to determine size?
		// Do we want some power of two as size? For now yes.
		final int MaxBinCount = 16384;
		for (int binCount = 256; binCount <= MaxBinCount; binCount *= 2) {
			if (range <= binCount) {
				binMapper =
					new Real1dBinMapper<T>(dataRange.getMin(), dataRange.getMax(),
						binCount,
						false);
				break;
			}
		}
		if (binMapper == null) {
			binMapper =
				new Real1dBinMapper<T>(dataRange.getMin(), dataRange.getMax(),
					MaxBinCount,
					false);
		}
		return new Histogram1d<T>(binMapper);
	}

	private long calcBin(long binCount, double val) {
		long value =
			(long) (binCount * (val - minMax.getMin()) / minMax.getExtent());
		if (value < 0) value = 0;
		if (value >= binCount) value = binCount - 1;
		return value;
	}

	private void updateBundle(double min, double max) {
		long binCount = histogram().getBinCount();
		histBundle.setHistogram(0, histogram());
		histBundle.setMinBin(calcBin(binCount, min));
		histBundle.setMaxBin(calcBin(binCount, max));
	}
}
