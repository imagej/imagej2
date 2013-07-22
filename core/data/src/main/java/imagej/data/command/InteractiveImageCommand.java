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

package imagej.data.command;

import imagej.command.InteractiveCommand;
import imagej.data.Dataset;
import imagej.data.display.DatasetView;
import imagej.data.display.ImageDisplayService;
import imagej.display.Display;
import imagej.module.ModuleItem;

import org.scijava.plugin.Parameter;

/**
 * An extension of {@link InteractiveCommand} which supports syncing
 * {@link DatasetView} and {@link Dataset} inputs in addition to only
 * {@link Display}s.
 * <p>
 * See the {@code BrightnessContrast} and {@code Threshold} commands in
 * {@code ij-commands} for examples.
 * </p>
 * 
 * @author Curtis Rueden
 */
public abstract class InteractiveImageCommand extends InteractiveCommand {

	@Parameter
	protected ImageDisplayService imageDisplayService;

	/**
	 * Creates a new interactive image command.
	 * 
	 * @param listenerNames The list of names of inputs to keep in sync when the
	 *          active display changes. Each input must be a {@link Display},
	 *          {@link DatasetView} or {@link Dataset}.
	 */
	public InteractiveImageCommand(final String... listenerNames) {
		super(listenerNames);
	}

	// -- Internal methods --

	@Override
	protected void updateInput(final ModuleItem<?> item) {
		final ModuleItem<DatasetView> viewItem = asView(item);
		final ModuleItem<Dataset> datasetItem = asDataset(item);
		if (viewItem != null) updateView(viewItem);
		else if (datasetItem != null) updateDataset(datasetItem);
		else super.updateInput(item);
	}

	// -- Helper methods --

	private ModuleItem<DatasetView> asView(final ModuleItem<?> item) {
		return asType(item, DatasetView.class);
	}

	private ModuleItem<Dataset> asDataset(final ModuleItem<?> item) {
		return asType(item, Dataset.class);
	}

	private void updateView(final ModuleItem<DatasetView> item) {
		update(item, imageDisplayService.getActiveDatasetView());
	}

	private void updateDataset(final ModuleItem<Dataset> item) {
		update(item, imageDisplayService.getActiveDataset());
	}

}
