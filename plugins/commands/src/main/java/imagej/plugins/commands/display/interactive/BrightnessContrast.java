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

import imagej.data.autoscale.AutoscaleService;
import imagej.data.autoscale.DataRange;
import imagej.data.command.InteractiveImageCommand;
import imagej.data.display.DatasetView;
import imagej.data.widget.HistogramBundle;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.histogram.BinMapper1d;
import net.imglib2.histogram.Histogram1d;
import net.imglib2.histogram.Real1dBinMapper;
import net.imglib2.type.numeric.RealType;
import net.imglib2.view.Views;

import org.scijava.ItemIO;
import org.scijava.command.Command;
import org.scijava.menu.MenuConstants;
import org.scijava.module.MutableModuleItem;
import org.scijava.plugin.Menu;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.widget.Button;
import org.scijava.widget.ChoiceWidget;
import org.scijava.widget.NumberWidget;

/**
 * Plugin that sets the minimum and maximum for scaling of display values. Sets
 * the same min/max for each channel.
 * 
 * @author Curtis Rueden
 * @author Grant Harris
 */
@Plugin(type = Command.class,
	menu = {
		@Menu(label = MenuConstants.IMAGE_LABEL,
			weight = MenuConstants.IMAGE_WEIGHT,
			mnemonic = MenuConstants.IMAGE_MNEMONIC),
		@Menu(label = "Adjust"),
		@Menu(label = "Brightness/Contrast...", accelerator = "shift ^C",
			weight = 0) }, iconPath = "/icons/commands/contrast.png",
	headless = true, initializer = "initValues")
public class BrightnessContrast<T extends RealType<T>> extends
	InteractiveImageCommand
{

	// -- constants --

	private static final int SLIDER_MIN = 0;
	private static final int SLIDER_MAX = 100;

	private static final String S_MIN = "" + SLIDER_MIN;
	private static final String S_MAX = "" + SLIDER_MAX;

	/**
	 * The exponential power used for computing contrast. The greater this number,
	 * the steeper the slope will be at maximum contrast, and flatter it will be
	 * at minimum contrast.
	 */
	private static final int MAX_POWER = 4;

	private static final String PLANE = "Plane";
	private static final String GLOBAL = "Global";

	// -- Parameter fields --

	@Parameter
	private AutoscaleService autoscaleService;

	@Parameter(type = ItemIO.BOTH, callback = "viewChanged")
	private DatasetView view;

	@Parameter(label = "Histogram")
	private HistogramBundle bundle;

	@Parameter(label = "Minimum", persist = false, callback = "minMaxChanged",
		style = NumberWidget.SCROLL_BAR_STYLE)
	private double min = Double.NaN;

	@Parameter(label = "Maximum", persist = false, callback = "minMaxChanged",
		style = NumberWidget.SCROLL_BAR_STYLE)
	private double max = Double.NaN;

	@Parameter(callback = "brightnessContrastChanged", persist = false,
		style = NumberWidget.SCROLL_BAR_STYLE, min = S_MIN, max = S_MAX)
	private int brightness;

	@Parameter(callback = "brightnessContrastChanged", persist = false,
		style = NumberWidget.SCROLL_BAR_STYLE, min = S_MIN, max = S_MAX)
	private int contrast;

	@Parameter(label = "Default", callback = "setDefault")
	private Button defaultButton;
	
	@Parameter(label = "Range:",
		style = ChoiceWidget.RADIO_BUTTON_HORIZONTAL_STYLE, choices = { PLANE,
			GLOBAL }, callback = "viewChanged")
	String rangeChoice = PLANE;

	// -- other fields --

	/** The minimum and maximum values of the data itself. */
	private double dataMin, dataMax;

	/** The initial minimum and maximum values of the data view. */
	private double initialMin, initialMax;

	// -- constructors --

	public BrightnessContrast() {
		super("view");
	}

	// -- Runnable methods --

	@Override
	public void run() {
		updateDisplay();
	}

	// -- BrightnessContrast methods --

	public DatasetView getView() {
		return view;
	}

	public void setView(final DatasetView view) {
		this.view = view;
	}

	public double getMinimum() {
		return min;
	}

	public void setMinimum(final double min) {
		this.min = min;
	}

	public double getMaximum() {
		return max;
	}

	public void setMaximum(final double max) {
		this.max = max;
	}

	public int getBrightness() {
		return brightness;
	}

	public void setBrightness(final int brightness) {
		this.brightness = brightness;
	}

	public int getContrast() {
		return contrast;
	}

	public void setContrast(final int contrast) {
		this.contrast = contrast;
	}

	// -- Initializers --

	protected void initValues() {
		viewChanged();
	}

	// -- Callback methods --

	/** Called when view changes. Updates everything to match. */
	protected void viewChanged() {
		RandomAccessibleInterval<? extends RealType<?>> interval;
		
		if (rangeChoice.equals(PLANE)) interval = view.xyPlane();
		else interval = view.getData().getImgPlus();
		
		computeDataMinMax(interval);
		computeInitialMinMax();
		if (Double.isNaN(min)) min = initialMin;
		if (Double.isNaN(max)) max = initialMax;
		computeBrightnessContrast();
		// TEMP : try this to clear up refresh problem
		// NOPE
		// updateDisplay();
	}

	/** Called when min or max changes. Updates brightness and contrast. */
	protected void minMaxChanged() {
		computeBrightnessContrast();
	}

	/** Called when brightness or contrast changes. Updates min and max. */
	protected void brightnessContrastChanged() {
		computeMinMax();
	}

	protected void setDefault() {
		brightness = (SLIDER_MIN + SLIDER_MAX) / 2;
		contrast = (SLIDER_MIN + SLIDER_MAX) / 2;
		brightnessContrastChanged();
		updateDisplay();
	}

	// -- Helper methods --

	// TODO we have a couple refresh problems
	// 1) right now if you bounce between two displays with this dialog open the
	// dialog values (like min, max, and hist don't update)
	// 2) even if you can fix 1) the fact that we don't change to a new
	// HistogramBundle but rather tweak the existing one might cause refresh()
	// probs also. Because the update() code checks if the T's are the same.
	// This means there is a implicit requirement for object reference equality
	// rather than using something like equals(). Or a isChanged() interface
	// (since in this case equals() would not work either).

	private void computeDataMinMax(
		final RandomAccessibleInterval<? extends RealType<?>> img)
	{
		// FIXME: Reconcile this with DefaultDatasetView.autoscale(int). There is
		// no reason to hardcode the usage of ComputeMinMax twice. Rather, there
		// should be a single entry point for obtain the channel min/maxes from
		// the metadata, and if they aren't there, then compute them. Probably
		// Dataset (not DatasetView) is a good place for it, because it is metadata
		// independent of the visualization settings.

		DataRange range = autoscaleService.getDefaultRandomAccessRange(img);
		dataMin = range.getMin();
		dataMax = range.getMax();

		final MutableModuleItem<Double> minItem =
			getInfo().getMutableInput("min", Double.class);
		minItem.setSoftMinimum(dataMin);
		minItem.setSoftMaximum(dataMax);
		final MutableModuleItem<Double> maxItem =
			getInfo().getMutableInput("max", Double.class);
		maxItem.setSoftMinimum(dataMin);
		maxItem.setSoftMaximum(dataMax);

		// System.out.println("IN HERE!!!!!!");
		// System.out.println(" dataMin = " + dataMin);
		// System.out.println(" dataMax = " + dataMax);
		@SuppressWarnings({ "unchecked", "rawtypes" })
		Iterable<T> iterable =
			Views
				.iterable((RandomAccessibleInterval<T>) (RandomAccessibleInterval) img);
		BinMapper1d<T> mapper =
			new Real1dBinMapper<T>(dataMin, dataMax, 256, false);
		Histogram1d<T> histogram = new Histogram1d<T>(iterable, mapper);
		if (bundle == null) {
			bundle = new HistogramBundle(histogram);
		}
		else {
			bundle.setHistogram(0, histogram);
		}
		bundle.setDataMinMax(dataMin, dataMax);
		// bundle.setLineSlopeIntercept(1, 0);
		log().debug(
			"computeDataMinMax: dataMin=" + dataMin + ", dataMax=" + dataMax);
		// force a widget refresh to see new Hist (and also fill min and max fields)
		// NOPE. HistBundle is unchanged. Only internals are. So no
		// refresh called. Note that I changed InteractiveCommand::update() to
		// always setValue() and still this did not work. !!!! Huh?
		// update(getInfo().getMutableInput("bundle", HistogramBundle.class),
		// bundle);
		// NOPE
		// getInfo().getInput("bundle", HistogramBundle.class).setValue(this,
		// bundle);
		// NOPE
		// getInfo().setVisible(false);
		// getInfo().setVisible(true);
		// NOPE
		// getInfo().getMutableInput("bundle",HistogramBundle.class).initialize(this);
		// NOPE
		// getInfo().getMutableInput("bundle",HistogramBundle.class).callback(this);
	}

	private void computeInitialMinMax() {
		// use only first channel, for now
		initialMin = view.getChannelMin(0);
		initialMax = view.getChannelMax(0);
		log().debug("computeInitialMinMax: initialMin=" + initialMin +
			", initialMax=" + initialMax);
	}

	/** Computes min and max from brightness and contrast. */
	private void computeMinMax() {
		// normalize brightness and contrast to [0, 1]
		final double bUnit =
			(double) (brightness - SLIDER_MIN) / (SLIDER_MAX - SLIDER_MIN);
		final double cUnit =
			(double) (contrast - SLIDER_MIN) / (SLIDER_MAX - SLIDER_MIN);

		// convert brightness to offset [-1, 1]
		final double b = 2 * bUnit - 1;

		// convert contrast to slope [e^-n, e^n]
		final double m = Math.exp(2 * MAX_POWER * cUnit - MAX_POWER);

		// y = m*x + b
		// minUnit is x at y=0
		// maxUnit is x at y=1
		final double minUnit = -b / m;
		final double maxUnit = (1 - b) / m;

		// convert unit min/max to actual min/max
		min = (dataMax - dataMin) * minUnit + dataMin;
		max = (dataMax - dataMin) * maxUnit + dataMin;

		bundle.setTheoreticalMinMax(min, max);
		// bundle.setLineSlopeIntercept(m, b);

		log().debug("computeMinMax: bUnit=" + bUnit + ", cUnit=" + cUnit + ", b=" +
			b + ", m=" + m + ", minUnit=" + minUnit + ", maxUnit=" + maxUnit +
			", min=" + min + ", max=" + max);
	}

	/** Computes brightness and contrast from min and max. */
	private void computeBrightnessContrast() {
		// normalize min and max to [0, 1]
		final double minUnit = (min - dataMin) / (dataMax - dataMin);
		final double maxUnit = (max - dataMin) / (dataMax - dataMin);

		// y = m*x + b
		// minUnit is x at y=0
		// maxUnit is x at y=1
		// b = y - m*x = -m * minUnit = 1 - m * maxUnit
		// m * maxUnit - m * minUnit = 1
		// m = 1 / (maxUnit - minUnit)
		final double m = 1 / (maxUnit - minUnit);
		final double b = -m * minUnit;

		// convert offset to normalized brightness
		final double bUnit = (b + 1) / 2;

		// convert slope to normalized contrast
		final double cUnit = (Math.log(m) + MAX_POWER) / (2 * MAX_POWER);

		// convert unit brightness/contrast to actual brightness/contrast
		brightness = (int) ((SLIDER_MAX - SLIDER_MIN) * bUnit + SLIDER_MIN + 0.5);
		contrast = (int) ((SLIDER_MAX - SLIDER_MIN) * cUnit + SLIDER_MIN + 0.5);

		bundle.setTheoreticalMinMax(min, max);
		// bundle.setLineSlopeIntercept(m, b);

		log().debug("computeBrightnessContrast: minUnit=" + minUnit + ", maxUnit=" +
			maxUnit + ", m=" + m + ", b=" + b + ", bUnit=" + bUnit + ", cUnit=" +
			cUnit + ", brightness=" + brightness + ", contrast=" + contrast);
	}

	/** Updates the displayed min/max range to match min and max values. */
	private void updateDisplay() {
		view.setChannelRanges(min, max);
		view.getProjector().map();
		view.update();
	}

}
