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

import ij.ImagePlus;
import ij.ImageStack;
import ij.WindowManager;
import ij.process.ImageProcessor;
import imagej.legacy.LegacyService;
import imagej.module.Module;
import imagej.module.ModuleItem;
import imagej.module.ModuleService;
import imagej.plugin.AbstractPreprocessorPlugin;
import imagej.plugin.PreprocessorPlugin;

import org.scijava.Priority;
import org.scijava.plugin.Plugin;

/**
 * Assigns the active {@link ImagePlus} when there is one single unresolved
 * {@link ImagePlus} parameter. Hence, rather than a dialog prompting the user
 * to choose an {@link ImagePlus}, the active {@link ImagePlus} is used
 * automatically.
 * <p>
 * In the case of more than one {@link ImagePlus} parameter, the active
 * {@link ImagePlus} is not used and instead the user must select. This behavior
 * is consistent with ImageJ v1.x.
 * </p>
 * <p>
 * The same process is applied for {@link ImageStack} and {@link ImageProcessor}
 * parameters, using the active {@link ImagePlus}'s associated
 * {@link ImageStack} and active {@link ImageProcessor} slice, respectively.
 * </p>
 * 
 * @author Curtis Rueden
 */
@Plugin(type = PreprocessorPlugin.class, priority = Priority.VERY_HIGH_PRIORITY)
public class ActiveImagePlusPreprocessor extends AbstractPreprocessorPlugin {

	// TODO: Reconcile with ActiveDisplayPreprocessor and ActiveImageProcessor.
	// Share common superclass?

	// -- ModuleProcessor methods --

	@Override
	public void process(final Module module) {
		final LegacyService legacyService =
			getContext().getService(LegacyService.class);
		if (legacyService == null) return;

		// TODO: Change this to a LegacyService API call?
		final ImagePlus imp = WindowManager.getCurrentImage();
		if (imp == null) return;

		// assign active legacy image to single ImagePlus input
		final String impInput = getSingleInput(module, ImagePlus.class);
		if (impInput != null) {
			module.setInput(impInput, imp);
			module.setResolved(impInput, true);
		}

		// assign active ImageStack to single ImageStack input
		final String stackInput = getSingleInput(module, ImageStack.class);
		if (stackInput != null) {
			final ImageStack imageStack = imp.getStack();
			if (imageStack != null) {
				module.setInput(stackInput, imageStack);
				module.setResolved(stackInput, true);
			}
		}

		// assign active ImageProcessor to single ImageProcessor input
		final String ipInput = getSingleInput(module, ImageProcessor.class);
		final ImageProcessor ip = imp.getProcessor();
		if (ipInput != null) {
			if (ip != null) {
				module.setInput(ipInput, ip);
				module.setResolved(ipInput, true);
			}
		}
	}

	// -- Helper methods --

	private String getSingleInput(final Module module, final Class<?> type) {
		final ModuleService moduleService =
			getContext().getService(ModuleService.class);
		if (moduleService == null) return null;
		final ModuleItem<?> item = moduleService.getSingleInput(module, type);
		if (item == null || !item.isAutoFill()) return null;
		return item.getName();
	}

}
