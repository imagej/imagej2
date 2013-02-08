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

// TODO All the problems with thresh overlay code at the moment:
//
// 1) delete button takes away thresh but cannot get it back without exiting
//    dialog and reentering
// 2) some methods here are unimplemented: autothresh, changePixels, stack hist
// 3) stack histogram: don't yet know what this is to do
// 4) we will have to display a histogram and thresh lines like IJ1 does
// 6) overlay manager threshold name does not update immediately
// 7) overlay not selectable in view but only via ovr mgr
// 8) do thresh overlays kill graphics of other overlays? It seems it may. Might
//    need to draw in a certain order
// 9) dark background: does not update the thresh values within the dialog
// 10) need to discover autothresh methods, populate list, and make autothresh
//     method call the one selected by user.
// 11) update commands that need a threshold to use them now: Convert To Mask?
// 12) dark background - make code exactly like IJ1
// 13) default thresh range might not be a setting. rather default to min and
//     half max. This would slow down the creation of threshold overlays in
//     general. Might not want to do this.
// 14) this plugin written to work with one display. So if you leave it up and
//     switch images this plugin won't immediately work with it. see what IJ1
//     does.

/**
 * @author Barry DeZonia
 */
@Plugin(menu = {
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

	@Parameter
	private ThresholdService threshSrv;

	@Parameter
	private OptionsService optionsSrv;

	@Parameter
	private ImageDisplayService imgDispSrv;

	@Parameter(label = "Method",
		choices = { "Method 1", "Method 2", "Method 3" }, persist = false)
	private String method;

	@Parameter(label = "Display type",
		choices = { RED, BLACK_WHITE, OVER_UNDER },
		callback = "displayTypeChanged", persist = false)
	private String displayType;

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
	private boolean stackHistogram;

	@Parameter(type = ItemIO.BOTH)
	private ImageDisplay display;

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
		boolean alreadyHadOne = threshSrv.hasThreshold(display);
		ThresholdOverlay overlay = threshSrv.getThreshold(display);

		// set default values: TODO - calc stats/histogram and do something nice.
		// For now we'll set them to half the range
		if (!alreadyHadOne) overlay.setRange(dataMin, dataMax / 2);

		// TODO note
		// The threshold ranges would be best as a slider with range ends noted.
		// However the current number widget code cannot handle sliders/scrolls on
		// doubles but just ints. Make that widget allow doubles and then change the
		// the widget style for min and max here below.

		// set min range widget
		DefaultModuleItem<Double> minItem =
			new DefaultModuleItem<Double>(getInfo(), "Minimum", Double.class);
		minItem.setMinimumValue(dataMin);
		minItem.setMaximumValue(dataMax);
		minItem.setValue(this, overlay.getRangeMin());
		minItem.setPersisted(false);
		// TODO - enable type as a SLIDER - see TODO note
		// minItem.setWidgetStyle(NumberWidget.SLIDER_STYLE);
		// minItem.setStepSize((dataMax - dataMin) / 12.0);
		minItem.setCallback("rangeChanged");
		getInfo().addInput(minItem);

		// set max range widget
		DefaultModuleItem<Double> maxItem =
			new DefaultModuleItem<Double>(getInfo(), "Maximum", Double.class);
		maxItem.setMinimumValue(dataMin);
		maxItem.setMaximumValue(dataMax);
		maxItem.setValue(this, overlay.getRangeMax());
		maxItem.setPersisted(false);
		// TODO - enable type as a SLIDER - see TODO note
		// maxItem.setWidgetStyle(NumberWidget.SLIDER_STYLE);
		// maxItem.setStepSize((dataMax - dataMin) / 12.0);
		maxItem.setCallback("rangeChanged");
		getInfo().addInput(maxItem);

		// initialize the colors of the overlay
		colorize(overlay);
	}

	// -- callbacks --

	protected void autoThreshold() {
		// TODO
		System.out.println("UNIMPLEMENTED");
	}

	protected void backgroundChange() {
		ThresholdOverlay overlay = getThreshold();
		// TODO - do these calx match IJ1? No. IJ1 calcs a threshold. Then either
		// goes from (0,thresh) or (thresh+1,255) depending on setting. Maybe we
		// do this too. Or we remove functionality altogether.
		double min = dataMin + dataMax - overlay.getRangeMax();
		double max = dataMin + dataMax - overlay.getRangeMin();
		overlay.setRange(min, max);
		overlay.update();
	}

	protected void changePixels() {
		// TODO
		System.out.println("UNIMPLEMENTED");
	}

	protected void deleteThreshold() {
		threshSrv.removeThreshold(display);
	}

	protected void displayTypeChanged() {
		ThresholdOverlay overlay = getThreshold();
		overlay.update();
	}

	protected void rangeChanged() {
		double min = (Double) getInput("Minimum");
		double max = (Double) getInput("Maximum");
		ThresholdOverlay overlay = getThreshold();
		overlay.setRange(min, max);
		overlay.update();
	}


	protected void stackHistogram() {
		// TODO
		System.out.println("UNIMPLEMENTED");
	}

	// -- helpers --

	private ThresholdOverlay getThreshold() {
		ThresholdOverlay overlay = threshSrv.getThreshold(display);
		colorize(overlay);
		return overlay;
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
		else { // ONE_COLOR
			overlay.setColorWithin(Colors.RED);
			overlay.setColorLess(null);
			overlay.setColorGreater(null);
		}
	}
	
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
