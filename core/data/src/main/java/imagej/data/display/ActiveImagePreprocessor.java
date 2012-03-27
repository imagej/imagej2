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

package imagej.data.display;

import imagej.ImageJ;
import imagej.data.Dataset;
import imagej.ext.Priority;
import imagej.ext.module.Module;
import imagej.ext.module.ModuleItem;
import imagej.ext.module.ModuleService;
import imagej.ext.plugin.Plugin;
import imagej.ext.plugin.process.PreprocessorPlugin;

/**
 * Assigns the active {@link ImageDisplay} when there is one single unresolved
 * {@link ImageDisplay} parameter. Hence, rather than a dialog prompting the
 * user to choose a {@link ImageDisplay}, the active {@link ImageDisplay} is
 * used automatically.
 * <p>
 * In the case of more than one {@link ImageDisplay} parameter, the active
 * {@link ImageDisplay} is not used and instead the user must select. This
 * behavior is consistent with ImageJ v1.x.
 * </p>
 * <p>
 * The same process is applied for {@link DataView} and {@link Dataset}
 * parameters, using the active {@link ImageDisplay}'s active {@link DataView}
 * and {@link Dataset}, respectively.
 * </p>
 * 
 * @author Curtis Rueden
 */
@Plugin(type = PreprocessorPlugin.class,
	priority = Priority.VERY_HIGH_PRIORITY)
public class ActiveImagePreprocessor implements PreprocessorPlugin {

	// -- ModulePreprocessor methods --

	@Override
	public boolean canceled() {
		return false;
	}

	@Override
	public String getMessage() {
		return null;
	}

	// -- ModuleProcessor methods --

	@Override
	public void process(final Module module) {
		final ImageDisplayService displayService =
			ImageJ.get(ImageDisplayService.class);
		final ImageDisplay activeDisplay = displayService.getActiveImageDisplay();
		if (activeDisplay == null) return;

		// assign active display to single ImageDisplay input
		final String displayInput = getSingleInput(module, ImageDisplay.class);
		if (displayInput != null) {
			module.setInput(displayInput, activeDisplay);
			module.setResolved(displayInput, true);
		}

		// assign active dataset view to single DatasetView input
		final String datasetViewInput = getSingleInput(module, DatasetView.class);
		final DatasetView activeDatasetView =
			displayService.getActiveDatasetView();
		if (datasetViewInput != null && activeDatasetView != null) {
			module.setInput(datasetViewInput, activeDatasetView);
			module.setResolved(datasetViewInput, true);
		}

		// assign active display view to single DataView input
		final String dataViewInput = getSingleInput(module, DataView.class);
		final DataView activeDataView = activeDisplay.getActiveView();
		if (dataViewInput != null && activeDataView != null) {
			module.setInput(dataViewInput, activeDataView);
			module.setResolved(dataViewInput, true);
		}

		// assign active dataset to single Dataset input
		final String datasetInput = getSingleInput(module, Dataset.class);
		final Dataset activeDataset = displayService.getActiveDataset();
		if (datasetInput != null && activeDataset != null) {
			module.setInput(datasetInput, activeDataset);
			module.setResolved(datasetInput, true);
		}
	}

	// -- Helper methods --

	private String getSingleInput(final Module module, final Class<?> type) {
		final ModuleService moduleService = ImageJ.get(ModuleService.class);
		final ModuleItem<?> item = moduleService.getSingleInput(module, type);
		if (item == null || !item.isAutoFill()) return null;
		return item.getName();
	}

}
