/*
 * #%L
 * ImageJ software for multidimensional image processing and analysis.
 * %%
 * Copyright (C) 2009 - 2013 Board of Regents of the University of
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
 * 
 * The views and conclusions contained in the software and documentation are
 * those of the authors and should not be interpreted as representing official
 * policies, either expressed or implied, of any organization.
 * #L%
 */

package imagej.core.commands.display.interactive;

import imagej.command.Command;
import imagej.data.Dataset;
import imagej.data.display.ImageDisplay;
import imagej.data.display.ImageDisplayService;
import imagej.data.display.event.AxisPositionEvent;
import imagej.data.overlay.ThresholdOverlay;
import imagej.data.threshold.ThresholdMethod;
import imagej.data.threshold.ThresholdService;
import imagej.menu.MenuConstants;
import imagej.module.DefaultModuleItem;
import imagej.ui.DialogPrompt;
import imagej.ui.UIService;
import imagej.util.Colors;
import imagej.widget.Button;
import net.imglib2.Cursor;
import net.imglib2.IterableInterval;
import net.imglib2.RandomAccess;
import net.imglib2.img.Img;
import net.imglib2.img.ImgPlus;
import net.imglib2.meta.AxisType;
import net.imglib2.ops.pointset.HyperVolumePointSet;
import net.imglib2.ops.pointset.PointSet;
import net.imglib2.ops.pointset.PointSetIterator;
import net.imglib2.type.numeric.RealType;
import net.imglib2.view.IntervalView;
import net.imglib2.view.Views;

import org.scijava.ItemIO;
import org.scijava.event.EventHandler;
import org.scijava.plugin.Menu;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

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
 * @author Barry DeZonia
 */
@Plugin(type = Command.class, menu = {
	@Menu(label = MenuConstants.IMAGE_LABEL, weight = MenuConstants.IMAGE_WEIGHT,
		mnemonic = MenuConstants.IMAGE_MNEMONIC), @Menu(label = "Adjust"),
	@Menu(label = "Threshold...", accelerator = "control shift T") },
	initializer = "initValues")
public class Threshold extends InteractiveCommand {

	// -- constants --
	
	private static final String RED = "Red";
	private static final String BLACK_WHITE = "Black/White";
	private static final String OVER_UNDER = "Over/Under";
	
	// -- Parameters --

	@Parameter(label = "Display Type",
		choices = { RED, BLACK_WHITE, OVER_UNDER },
		callback = "displayTypeChanged", persist = false)
	private String displayType = RED;

	@Parameter(label = "Method", persist = false)
	private String methodName;

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

	@Parameter(label = "Minimum", callback = "rangeChanged", persist = false)
	private double minimum;

	@Parameter(label = "Maximum", callback = "rangeChanged", persist = false)
	private double maximum;

	@Parameter(type = ItemIO.BOTH)
	private ImageDisplay display;

	@Parameter
	private ThresholdService threshSrv;

	@Parameter
	private ImageDisplayService imgDispSrv;

	@Parameter
	private UIService uiSrv;

	// -- instance variables --

	private long[] fullHistogram;

	private long[] planeHistogram;

	private boolean invalidPlaneHist = true;

	private double dataMin, dataMax;

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

	@SuppressWarnings("unchecked")
	protected void initValues() {

		if (display == null) return;

		getThresholdMethodNames();

		boolean alreadyHadOne = threshSrv.hasThreshold(display);
		ThresholdOverlay overlay = threshSrv.getThreshold(display);

		calcDataRange();

		fullHistogram = buildHistogram(true, null);
		planeHistogram = null;
		invalidPlaneHist = true;

		if (!alreadyHadOne) {
			// default the thresh to something sensible: 85/170 is IJ1's default
			double min = 85 * (dataMax - dataMin) / 255;
			double max = 170 * (dataMax - dataMin) / 255;
			overlay.setRange(min, max);
		}

		// TODO note
		// The threshold ranges would be best as a slider with range ends noted.
		// However the current number widget code cannot handle sliders/scrolls on
		// doubles but just ints. Make that widget allow doubles and then change the
		// the widget style for min and max here below.

		// set min range widget
		final DefaultModuleItem<Double> minItem =
			(DefaultModuleItem<Double>) getInfo().getInput("minimum");
		minItem.setMinimumValue(dataMin);
		minItem.setMaximumValue(dataMax);
		minItem.setValue(this, overlay.getRangeMin());

		// set max range widget
		final DefaultModuleItem<Double> maxItem =
			(DefaultModuleItem<Double>) getInfo().getInput("maximum");
		maxItem.setMinimumValue(dataMin);
		maxItem.setMaximumValue(dataMax);
		maxItem.setValue(this, overlay.getRangeMax());

		// initialize the colors of the overlay
		colorize(overlay);
	}

	// -- callbacks --

	protected void autoThreshold() {
		ThresholdMethod method = threshSrv.getThresholdMethod(methodName);
		int cutoff = method.getThreshold(histogram());
		if (cutoff < 0) {
			DialogPrompt dialog =
				uiSrv.getDefaultUI().dialogPrompt(method.getMessage(),
				"Thresholding failure", DialogPrompt.MessageType.INFORMATION_MESSAGE,
				DialogPrompt.OptionType.DEFAULT_OPTION);
			dialog.prompt();
			return;
		}
		else if (method.getMessage() != null) {
			log.warn(method.getMessage());
		}
		double maxRange = histogram().length - 1;
		// TODO : what is best increment? To avoid roundoff errs use a teeny inc
		// (like 0.0001 instead of 1). But then result does not match IJ1. With
		// teeny inc dark bckgrnd bounces from (0,cutoff) to (cutoff,255) rather
		// than (0,cutoff) to (cutoff+1,255). Think how to best display range values
		// so that they don't have gaps when displayed in dialog bouncing between
		// light and dark background.
		double bot = (darkBackground) ? cutoff + 1 : 0;
		double top = (darkBackground) ? maxRange : cutoff;
		minimum = dataMin + (bot / maxRange) * (dataMax - dataMin);
		maximum = dataMin + (top / maxRange) * (dataMax - dataMin);
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
		overlay.update();
	}

	protected void stackHistogram() {
		autoThreshold();
	}

	// -- EventHandlers --

	@EventHandler
	protected void onEvent(AxisPositionEvent evt) {
		if (evt.getDisplay() != display) return;
		invalidPlaneHist = true;
	}

	// -- helpers --

	private ThresholdOverlay getThreshold() {
		ThresholdOverlay overlay = threshSrv.getThreshold(display);
		colorize(overlay);
		return overlay;
	}

	private long[] histogram() {
		if (stackHistogram) return fullHistogram;
		if (invalidPlaneHist) {
			planeHistogram = buildHistogram(false, planeHistogram);
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
	
	private void getThresholdMethodNames() {
		@SuppressWarnings("unchecked")
		final DefaultModuleItem<String> methodNameInput =
			(DefaultModuleItem<String>) getInfo().getInput("methodName");
		methodNameInput.setChoices(threshSrv.getThresholdMethodNames());
	}

	// calcs the data range of the whole dataset

	private void calcDataRange() {
		Dataset ds = imgDispSrv.getActiveDataset(display);
		dataMin = Double.POSITIVE_INFINITY;
		dataMax = Double.NEGATIVE_INFINITY;
		ImgPlus<? extends RealType<?>> imgPlus = ds.getImgPlus();
		Cursor<? extends RealType<?>> cursor = imgPlus.cursor();
		while (cursor.hasNext()) {
			cursor.fwd();
			double value = cursor.get().getRealDouble();
			if (!Double.isNaN(value)) {
				if (value < dataMin) dataMin = value;
				if (value > dataMax) dataMax = value;
			}
		}
	}

	// builds the histogram from either the whole data range or the currently
	// viewed plane

	private long[] buildHistogram(boolean allData, long[] existingHist) {
		// return buildHistogramFromPointSets(allData, existingHist);
		return buildHistogramFromViews(allData, existingHist);
	}

	private long[] buildHistogramFromViews(boolean allData, long[] existingHist) {
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
				AxisType axisType = ds.axis(d);
				long pos = display.getLongPosition(axisType);
				min[d] = pos;
				max[d] = pos;
			}
		}
		Img<? extends RealType<?>> img = ds.getImgPlus();
		IntervalView<? extends RealType<?>> view = Views.interval(img, min, max);
		IterableInterval<? extends RealType<?>> data = Views.iterable(view);
		final long[] histogram;
		if (existingHist == null) {
			// +1 needed for int but maybe not float
			histogram = allocateHistogram(dataMax - dataMin + 1);
		}
		else {
			zeroOut(existingHist);
			histogram = existingHist;
		}
		Cursor<? extends RealType<?>> cursor = data.cursor();
		while (cursor.hasNext()) {
			double value = cursor.next().getRealDouble();
			double relPos = (value - dataMin) / (dataMax - dataMin);
			int binNumber = (int) Math.round((histogram.length - 1) * relPos);
			histogram[binNumber]++;
		}
		return histogram;
	}

	@SuppressWarnings("unused")
	private long[] buildHistogramFromPointSets(boolean allData,
		long[] existingHist)
	{
		Dataset ds = imgDispSrv.getActiveDataset(display);
		final long[] histogram;
		if (existingHist == null) {
			// +1 needed for int but maybe not float
			histogram = allocateHistogram(dataMax - dataMin + 1);
		}
		else {
			zeroOut(existingHist);
			histogram = existingHist;
		}
		RandomAccess<? extends RealType<?>> accessor =
			ds.getImgPlus().randomAccess();
		PointSet points = allData ? allPlanes(ds) : viewedPlane(ds);
		PointSetIterator iter = points.iterator();
		while (iter.hasNext()) {
			long[] pos = iter.next();
			accessor.setPosition(pos);
			double value = accessor.get().getRealDouble();
			double relPos = (value - dataMin) / (dataMax - dataMin);
			int binNumber = (int) Math.round((histogram.length - 1) * relPos);
			histogram[binNumber]++;
		}
		return histogram;
	}

	private PointSet allPlanes(Dataset dataset) {
		return new HyperVolumePointSet(dataset.getDims());
	}

	private PointSet viewedPlane(Dataset dataset) {
		long[] pt1 = new long[dataset.numDimensions()];
		long[] pt2 = new long[dataset.numDimensions()];
		for (int i = 2; i < pt1.length; i++) {
			AxisType axisType = dataset.axis(i);
			pt1[i] = pt2[i] = display.getLongPosition(axisType);
		}
		pt2[0] = dataset.dimension(0) - 1;
		pt2[1] = dataset.dimension(1) - 1;
		return new HyperVolumePointSet(pt1, pt2);
	}

	private long[] allocateHistogram(double dataRange) {
		// TODO - size of histogram affects speed of all autothresh methods
		// What is the best way to determine size?
		// Do we want some power of two as size? For now yes.
		final int MAX = 4096;
		int histSize = -1;
		for (int i = 256; i <= MAX; i *= 2) {
			if (dataRange <= i) {
				histSize = i;
				break;
			}
		}
		if (histSize == -1) histSize = MAX;
		return new long[histSize];
	}

	private void zeroOut(long[] histogram) {
		for (int i = 0; i < histogram.length; i++) {
			histogram[i] = 0;
		}
	}
}
