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

package imagej.core.commands.misc;

import imagej.data.overlay.ThresholdService;
import imagej.menu.MenuConstants;
import imagej.options.OptionsPlugin;
import imagej.plugin.Menu;
import imagej.plugin.Parameter;
import imagej.plugin.Plugin;

// TODO - some hackery in place. Rather than being a nice clean OptionsDialog
// that can be reused by ThresholdService we instead maintain state there and
// synchronize here. That is because ThresholdService wants to live in ij-data
// while ij-options is not accessible from there. So some ugly delegation is in
// place.

/**
 * Runs the Image::Adjust::Threshold Options dialog.
 * 
 * @author Barry DeZonia
 */
@Plugin(menu = {
	@Menu(label = MenuConstants.IMAGE_LABEL, weight = MenuConstants.IMAGE_WEIGHT,
		mnemonic = MenuConstants.IMAGE_MNEMONIC), @Menu(label = "Adjust"),
	@Menu(label = "Threshold options") }, initializer = "initValues")
public class OptionsThreshold extends OptionsPlugin {

	// -- Parameters --

	@Parameter(label = "Default minimum", persist = false)
	private double minimum;

	@Parameter(label = "Default maximum", persist = false)
	private double maximum;

	// TODO - a default color? Or just use Overlay defaults?

	@Override
	public void run() {
		super.run();
		threshSrv().setDefaultThreshold(minimum, maximum);
	}

	// -- accessors --

	public void setDefaultRange(double min, double max) {
		threshSrv().setDefaultThreshold(min, max);
		minimum = min;
		maximum = max;
	}

	public double getDefaultRangeMin() {
		return threshSrv().getDefaultRangeMin();
	}

	public double getDefaultRangeMax() {
		return threshSrv().getDefaultRangeMax();
	}

	// -- initializers --

	protected void initValues() {
		ThresholdService threshSrv = threshSrv();
		minimum = threshSrv.getDefaultRangeMin();
		maximum = threshSrv.getDefaultRangeMax();
	}

	// -- helpers --

	private ThresholdService threshSrv() {
		return getContext().getService(ThresholdService.class);
	}
}
