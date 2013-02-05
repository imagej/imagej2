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

import imagej.Previewable;
import imagej.command.DynamicCommand;
import imagej.data.Dataset;
import imagej.data.display.DatasetView;
import imagej.data.display.ImageDisplayService;
import imagej.display.Display;
import imagej.display.DisplayService;
import imagej.display.event.DisplayActivatedEvent;
import imagej.event.EventHandler;
import imagej.event.EventService;
import imagej.log.LogService;
import imagej.module.ModuleItem;
import imagej.plugin.Parameter;

/**
 * A command intended to be used interactively.
 * <p>
 * It is {@link Previewable}, with the previews used for interactive
 * exploration. Further, this class provides added convenience for keeping
 * certain input parameters up to date with active {@link Display}s,
 * {@link DatasetView}s and {@link Dataset}s. It listens for
 * {@link DisplayActivatedEvent}s, updating the inputs specified in the
 * constructor when such events occur. Individual interactive commands can then
 * add callback methods to affected inputs, for reacting to a change in the
 * active display.
 * </p>
 * <p>
 * See the {@code BrightnessContrast} command in {@code ij-commands-display} for
 * an example.
 * </p>
 * 
 * @author Curtis Rueden
 */
public abstract class InteractiveCommand extends DynamicCommand implements
	Previewable
{

	@Parameter
	protected ImageDisplayService imageDisplayService;

	@Parameter
	protected DisplayService displayService;

	@Parameter
	protected EventService eventService;

	@Parameter
	protected LogService log;

	/** List of inputs to keep in sync when the active display changes. */
	private final String[] inputs;

	/**
	 * Creates a new interactive command.
	 * 
	 * @param inputs The list of inputs to keep in sync when the active display
	 *          changes. Each input must be a {@link Display}, {@link DatasetView}
	 *          or {@link Dataset}.
	 */
	public InteractiveCommand(final String... inputs) {
		this.inputs = inputs;
	}

	// -- Previewable methods --

	@Override
	public void preview() {
		// NB: Interactive commands call run upon any parameter change.
		run();
	}

	@Override
	public void cancel() {
		// NB: Interactive commands cannot be canceled.
		// That is, closing the non-modal dialog does nothing.
	}

	// -- Event handlers --

	/** Updates the displayed min/max range to match min and max values. */
	@EventHandler
	protected void updateInputs(
		@SuppressWarnings("unused") final DisplayActivatedEvent evt)
	{
		for (final String input : inputs) {
			final ModuleItem<?> item = getInfo().getInput(input);

			final ModuleItem<Display<?>> displayItem = asDisplay(item);
			final ModuleItem<DatasetView> viewItem = asView(item);
			final ModuleItem<Dataset> datasetItem = asDataset(item);

			if (displayItem != null) updateDisplay(displayItem);
			else if (viewItem != null) updateView(viewItem);
			else if (datasetItem != null) updateDataset(datasetItem);
			else {
				log.warn("Input '" + input + "' (" + item.getClass().getName() +
					") is not supported");
			}
		}
	}

	// -- Helper methods --

	private ModuleItem<Display<?>> asDisplay(final ModuleItem<?> item) {
		@SuppressWarnings({ "rawtypes", "unchecked" })
		final Class<Display<?>> displayClass = (Class) Display.class;
		return asType(item, displayClass);
	}

	private ModuleItem<DatasetView> asView(final ModuleItem<?> item) {
		return asType(item, DatasetView.class);
	}

	private ModuleItem<Dataset> asDataset(final ModuleItem<?> item) {
		return asType(item, Dataset.class);
	}

	private <T> ModuleItem<T>
		asType(final ModuleItem<?> item, final Class<T> type)
	{
		if (!type.isAssignableFrom(item.getType())) {
			return null;
		}
		@SuppressWarnings("unchecked")
		final ModuleItem<T> typedItem = (ModuleItem<T>) item;
		return typedItem;
	}

	private <D extends Display<?>> void updateDisplay(final ModuleItem<D> item) {
		update(item, displayService.getActiveDisplay(item.getType()));
	}

	private void updateView(final ModuleItem<DatasetView> item) {
		update(item, imageDisplayService.getActiveDatasetView());
	}

	private void updateDataset(final ModuleItem<Dataset> item) {
		update(item, imageDisplayService.getActiveDataset());
	}

	private <T> void update(final ModuleItem<T> item, final T newValue) {
		final T oldValue = item.getValue(this);
		if (oldValue != newValue) {
			item.setValue(this, newValue);
			item.callback(this);
		}
	}

}
