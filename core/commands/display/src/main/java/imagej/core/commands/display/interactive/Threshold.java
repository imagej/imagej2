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

import imagej.data.display.ImageDisplay;
import imagej.data.display.ImageDisplayService;
import imagej.data.overlay.ThresholdOverlay;
import imagej.data.overlay.ThresholdService;
import imagej.menu.MenuConstants;
import imagej.module.DefaultModuleItem;
import imagej.module.ItemIO;
import imagej.options.OptionsService;
import imagej.plugin.Menu;
import imagej.plugin.Parameter;
import imagej.plugin.Plugin;
import imagej.util.Colors;
import imagej.widget.Button;
import net.imglib2.algorithm.stats.ComputeMinMax;
import net.imglib2.img.Img;
import net.imglib2.type.numeric.RealType;

/**
 * @author Barry DeZonia
 */
@Plugin(menu = {
	@Menu(label = MenuConstants.IMAGE_LABEL, weight = MenuConstants.IMAGE_WEIGHT,
		mnemonic = MenuConstants.IMAGE_MNEMONIC), @Menu(label = "Adjust"),
	@Menu(label = "Threshold...", accelerator = "control shift T") },
	initializer = "initValues")
public class Threshold extends InteractiveCommand {

	// -- Parameters --

	@Parameter
	private ThresholdService threshSrv;

	@Parameter
	private OptionsService optionsSrv;

	@Parameter
	private ImageDisplayService imgDispSrv;

	@Parameter(type = ItemIO.BOTH)
	private ImageDisplay display;

	@Parameter(label = "Method",
		choices = { "Method 1", "Method 2", "Method 3" }, persist = false)
	private String method;

	@Parameter(label = "Display type", choices = { "Red", "B&W", "Over/Under" },
		callback = "displayTypeChanged", persist = false)
	private String displayType;

	@Parameter(label = "Auto", callback = "autoThreshold")
	private Button auto;

	@Parameter(label = "Apply", callback = "changePixels")
	private Button apply;

	@Parameter(label = "Reset", callback = "deleteThreshold")
	private Button reset;

	@Parameter(label = "Dark Background", callback = "backgroundChange",
		persist = false)
	private boolean darkBackground;

	@Parameter(label = "Stack Histogram", callback = "stackHistogram",
		persist = false)
	private boolean stackHistogram;

	// -- instance variables --

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

	// -- callbacks --

	@SuppressWarnings("unchecked")
	protected void initValues() {

		computeDataMinMax(getImg());

		ThresholdOverlay overlay = threshSrv.getThreshold(display);

		// color = overlay.getFillColor();

		DefaultModuleItem<Double> minItem =
			new DefaultModuleItem<Double>(getInfo(), "Minimum", Double.class);
		minItem.setCallback("rangeChanged");
		minItem.setMinimumValue(dataMin);
		minItem.setMaximumValue(dataMax);
		minItem.setValue(this, overlay.getRangeMin());
		minItem.setPersisted(false);
		getInfo().addInput(minItem);

		DefaultModuleItem<Double> maxItem =
			new DefaultModuleItem<Double>(getInfo(), "Maximum", Double.class);
		maxItem.setCallback("rangeChanged");
		maxItem.setMinimumValue(dataMin);
		maxItem.setMaximumValue(dataMax);
		maxItem.setValue(this, overlay.getRangeMax());
		maxItem.setPersisted(false);
		getInfo().addInput(maxItem);
	}

	protected void rangeChanged() {
		double min = (Double) getInput("Minimum");
		double max = (Double) getInput("Maximum");
		ThresholdOverlay overlay = threshSrv.getThreshold(display);
		overlay.setRange(min, max);
		overlay.update();
	}

	protected void displayTypeChanged() {
		ThresholdOverlay overlay = threshSrv.getThreshold(display);
		overlay.setFillColor(Colors.RED); // TODO - temp hack
		overlay.update();
	}

	protected void autoThreshold() {
		// TODO
		System.out.println("UNIMPLEMENTED");
	}

	protected void deleteThreshold() {
		threshSrv.removeThreshold(display);
	}

	protected void changePixels() {
		// TODO
		System.out.println("UNIMPLEMENTED");
	}

	protected void backgroundChange() {
		ThresholdOverlay overlay = threshSrv.getThreshold(display);
		double min, max;
		// if (darkBackground) {
		min = dataMax - overlay.getRangeMax() + dataMin;
		max = dataMax - overlay.getRangeMin() + dataMin;
		// }
		// else { // light background
		// min = ;
		// max = ;
		// }
		overlay.setRange(min, max);
		overlay.update();
	}

	protected void stackHistogram() {
		// TODO
		System.out.println("UNIMPLEMENTED");
	}

	// -- helpers --

	private <T extends RealType<T>> void computeDataMinMax(final Img<T> img) {
		final ComputeMinMax<T> computeMinMax = new ComputeMinMax<T>(img);
		computeMinMax.process();
		dataMin = computeMinMax.getMin().getRealDouble();
		dataMax = computeMinMax.getMax().getRealDouble();
		log.debug("computeDataMinMax: dataMin=" + dataMin + ", dataMax=" + dataMax);
	}

	@SuppressWarnings("rawtypes")
	private Img getImg() {
		return imgDispSrv.getActiveDataset(display).getImgPlus();
	}
}
