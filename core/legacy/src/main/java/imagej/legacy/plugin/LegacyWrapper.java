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

package imagej.legacy.plugin;

import ij.IJ;
import ij.ImagePlus;
import ij.WindowManager;
import imagej.command.ModuleCommand;
import imagej.data.display.ImageDisplay;
import imagej.data.display.ImageDisplayService;
import imagej.legacy.LegacyOutputTracker;
import imagej.legacy.LegacyService;
import imagej.module.ModuleItem;

import org.scijava.plugin.Parameter;
import org.scijava.util.ClassUtils;

/**
 * TODO
 * 
 * @author Curtis Rueden
 */
public abstract class LegacyWrapper extends ModuleCommand {

	// run("Histogram", "bins=256 use x_min=15 x_max=255 y_max=Auto");

	@Parameter
	private LegacyService legacyService;

	@Parameter
	private ImageDisplayService imageDisplayService;

	// -- LegacyWrapper methods --

	protected abstract String getCommand();

	// -- Runnable methods --

	@Override
	public void run() {
		convertInputs();
		IJ.run(getCommand(), getOptions());
		convertOutputs();
	}

	// -- Helper methods --

	/** Builds an ImageJ 1.x options string from this command's inputs. */
	private String getOptions() {
		final StringBuilder options = new StringBuilder();
		for (final ModuleItem<?> input : getInfo().inputs()) {
			options.append(" " + getOption(input));
		}
		return options.toString().substring(1);
	}

	/** Builds an ImageJ 1.x option token from the given input. */
	private Object getOption(final ModuleItem<?> input) {
		final String name = input.getName();
		final Object value = input.getValue(this);
		if (ClassUtils.isBoolean(input.getType())) {
			final Boolean b = (Boolean) value;
			if (b == null || !b) return "";
			return " " + name;
		}
		return name + "=" + quote(value);
	}

	private String quote(final Object value) {
		if (value == null) return "";
		final String s = value.toString();
		return s.indexOf(' ') < 0 ? s : "[" + s + "]";
	}

	private void convertInputs() {
		for (final ModuleItem<?> input : getInfo().inputs()) {
			convertInput(input);
		}
	}

	private void convertOutputs() {
		final ImagePlus[] imps = LegacyOutputTracker.getOutputs();
		int index = 0;
		for (final ModuleItem<?> output : getInfo().outputs()) {
			if (index >= imps.length) break;
			if (convertOutput(output, imps[index])) index++;
		}
	}

	private void convertInput(final ModuleItem<?> input) {
		if (!ImageDisplay.class.isAssignableFrom(input.getType())) return;
		final ImageDisplay display = (ImageDisplay) input.getValue(this);
		final ImagePlus imp = toLegacy(display);
		if (display == imageDisplayService.getActiveImageDisplay()) {
			WindowManager.setTempCurrentImage(imp);
		}
	}

	private boolean
		convertOutput(final ModuleItem<?> output, final ImagePlus imp)
	{
		if (!ImageDisplay.class.isAssignableFrom(output.getType())) return false;
		setOutput(output.getName(), fromLegacy(imp));
		return true;
	}

	private ImagePlus toLegacy(ImageDisplay display) {
		return legacyService.getImageMap().registerDisplay(display);
	}

	private ImageDisplay fromLegacy(ImagePlus imp) {
		return legacyService.getImageMap().registerLegacyImage(imp);
	}

}
