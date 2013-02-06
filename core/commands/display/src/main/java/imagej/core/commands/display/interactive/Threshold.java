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

import imagej.command.ContextCommand;
import imagej.data.display.ImageDisplay;
import imagej.data.overlay.ThresholdOverlay;
import imagej.data.overlay.ThresholdService;
import imagej.menu.MenuConstants;
import imagej.module.ItemIO;
import imagej.options.OptionsService;
import imagej.plugin.Menu;
import imagej.plugin.Parameter;
import imagej.plugin.Plugin;
import imagej.util.ColorRGB;

/**
 * @author Barry DeZonia
 */
@Plugin(menu = {
	@Menu(label = MenuConstants.IMAGE_LABEL, weight = MenuConstants.IMAGE_WEIGHT,
		mnemonic = MenuConstants.IMAGE_MNEMONIC), @Menu(label = "Adjust"),
	@Menu(label = "Threshold...", accelerator = "control shift T") },
	initializer = "initValues")
public class Threshold extends ContextCommand {

	// -- Parameters --

	@Parameter(type = ItemIO.BOTH)
	private ImageDisplay display;

	@Parameter(label = "Minimum", persist = false, callback = "rangeChanged")
	private double min;

	@Parameter(label = "Maximum", persist = false, callback = "rangeChanged")
	private double max;

	@Parameter(label = "Color", persist = false, callback = "colorChanged")
	private ColorRGB color;

	@Parameter
	private ThresholdService threshSrv;

	@Parameter
	private OptionsService optionsSrv;

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
		// TODO - we used to cancel() if min > max. What now? Callbacks keep them
		// legal?
		threshSrv.getThreshold(display).setRange(min, max);
	}


	// -- callbacks --

	protected void initValues() {

		// TODO : cannot call service.getThreshold() all the time. Otherwise one can
		// create a thresh that doesn't get thrown away if you cancel this dialog

		if (threshSrv.hasThreshold(display)) {
			ThresholdOverlay overlay = threshSrv.getThreshold(display);
			min = overlay.getRangeMin();
			max = overlay.getRangeMax();
			color = overlay.getFillColor();
		}
		else { // no thresh exists: get defaults
			min = threshSrv.getDefaultRangeMin();
			max = threshSrv.getDefaultRangeMax();
			color = threshSrv.getDefaultColor();
		}
	}

	protected void rangeChanged() {
		ThresholdOverlay overlay = threshSrv.getThreshold(display);
		overlay.setRange(min, max);
		overlay.update();
	}

	protected void colorChanged() {
		ThresholdOverlay overlay = threshSrv.getThreshold(display);
		overlay.setFillColor(color);
		overlay.update();
	}

}
