/*
 * #%L
 * ImageJ software for multidimensional image processing and analysis.
 * %%
 * Copyright (C) 2009 - 2012 Board of Regents of the University of
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

import imagej.data.Dataset;
import imagej.data.display.DatasetView;
import imagej.menu.MenuConstants;
import imagej.module.ItemIO;
import imagej.plugin.Menu;
import imagej.plugin.Parameter;
import imagej.plugin.Plugin;
import imagej.widget.NumberWidget;
import net.imglib2.algorithm.stats.ComputeMinMax;
import net.imglib2.img.Img;
import net.imglib2.type.numeric.RealType;

/**
 * Plugin that sets the minimum and maximum for scaling of display values. Sets
 * the same min/max for each channel.
 * 
 * @author Curtis Rueden
 * @author Grant Harris
 */
@Plugin(menu = {
	@Menu(label = MenuConstants.IMAGE_LABEL, weight = MenuConstants.IMAGE_WEIGHT,
		mnemonic = MenuConstants.IMAGE_MNEMONIC),
	@Menu(label = "Adjust"),
	@Menu(label = "Brightness/Contrast...", accelerator = "control shift C",
		weight = 0) }, iconPath = "/icons/plugins/contrast.png", headless = true,
	initializer = "initValues")
public class BrightnessContrast extends InteractiveCommand {

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

	@Parameter(type = ItemIO.BOTH, callback = "initValues")
	private DatasetView view;

	@Parameter(label = "Minimum", persist = false, callback = "minMaxChanged")
	private double min = Double.NaN;

	@Parameter(label = "Maximum", persist = false, callback = "minMaxChanged")
	private double max = Double.NaN;

	@Parameter(callback = "brightnessContrastChanged", persist = false,
		style = NumberWidget.SCROLL_BAR_STYLE, min = S_MIN, max = S_MAX)
	private int brightness;

	@Parameter(callback = "brightnessContrastChanged", persist = false,
		style = NumberWidget.SCROLL_BAR_STYLE, min = S_MIN, max = S_MAX)
	private int contrast;

	/** The minimum and maximum values of the data itself. */
	private double dataMin, dataMax;

	/** The initial minimum and maximum values of the data view. */
	private double initialMin, initialMax;

	public BrightnessContrast() {
		super("view");
	}

	// -- Runnable methods --

	@Override
	public void run() {
		updateDisplay();
	}

	// -- Previewable methods --

	@Override
	public void preview() {
		run();
	}

	@Override
	public void cancel() {
		min = initialMin;
		max = initialMax;
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

	@SuppressWarnings({ "rawtypes", "unchecked" })
	protected void initValues() {
		final Dataset dataset = view.getData();
		final Img img = dataset.getImgPlus();
		computeDataMinMax(img);
		computeInitialMinMax();
		if (Double.isNaN(min)) min = initialMin;
		if (Double.isNaN(max)) max = initialMax;
		computeBrightnessContrast();
	}

	// -- Callback methods --

	/** Called when min or max changes. Updates brightness and contrast. */
	protected void minMaxChanged() {
		computeBrightnessContrast();
	}

	/** Called when brightness or contrast changes. Updates min and max. */
	protected void brightnessContrastChanged() {
		computeMinMax();
	}

	// -- Helper methods --

	private <T extends RealType<T>> void computeDataMinMax(final Img<T> img) {
		final ComputeMinMax<T> computeMinMax = new ComputeMinMax<T>(img);
		computeMinMax.process();
		dataMin = computeMinMax.getMin().getRealDouble();
		dataMax = computeMinMax.getMax().getRealDouble();
		log.debug("computeDataMinMax: dataMin=" + dataMin + ", dataMax=" + dataMax);
	}

	private void computeInitialMinMax() {
		// use only first channel, for now
		initialMin = view.getChannelMin(0);
		initialMax = view.getChannelMax(0);
		log.debug("computeInitialMinMax: initialMin=" + initialMin +
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

		log.debug("computeMinMax: bUnit=" + bUnit + ", cUnit=" + cUnit + ", b=" + b +
			", m=" + m + ", minUnit=" + minUnit + ", maxUnit=" + maxUnit + ", min=" +
			min + ", max=" + max);
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

		log.debug("computeBrightnessContrast: minUnit=" + minUnit + ", maxUnit=" +
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
