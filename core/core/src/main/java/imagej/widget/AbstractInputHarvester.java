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

package imagej.widget;

import imagej.module.Module;
import imagej.module.ModuleCanceledException;
import imagej.module.ModuleException;
import imagej.module.ModuleItem;

import java.util.ArrayList;
import java.util.List;

import org.scijava.AbstractContextual;
import org.scijava.object.ObjectService;

/**
 * Abstract superclass for {@link InputHarvester}s.
 * <p>
 * An input harvester obtains a module's unresolved input parameter values from
 * the user. Parameters are collected using an {@link InputPanel} dialog box.
 * </p>
 * 
 * @author Curtis Rueden
 * @param <P> The type of UI component housing the input panel itself.
 * @param <W> The type of UI component housing each input widget.
 */
public abstract class AbstractInputHarvester<P, W> extends AbstractContextual
	implements InputHarvester<P, W>
{

	// -- InputHarvester methods --

	@Override
	public void harvest(final Module module) throws ModuleException {
		final InputPanel<P, W> inputPanel = createInputPanel();
		buildPanel(inputPanel, module);
		if (!inputPanel.hasWidgets()) return; // no inputs left to harvest

		final boolean ok = harvestInputs(inputPanel, module);
		if (!ok) throw new ModuleCanceledException();

		processResults(inputPanel, module);
	}

	@Override
	public void
		buildPanel(final InputPanel<P, W> inputPanel, final Module module)
			throws ModuleException
	{
		final Iterable<ModuleItem<?>> inputs = module.getInfo().inputs();

		final ArrayList<WidgetModel> models = new ArrayList<WidgetModel>();

		for (final ModuleItem<?> item : inputs) {
			final WidgetModel model = addInput(inputPanel, module, item);
			if (model != null) models.add(model);
		}

		// mark all models as initialized
		for (final WidgetModel model : models)
			model.setInitialized(true);

		// compute initial preview
		module.preview();
	}

	@Override
	public void processResults(final InputPanel<P, W> inputPanel,
		final Module module) throws ModuleException
	{
		final Iterable<ModuleItem<?>> inputs = module.getInfo().inputs();

		for (final ModuleItem<?> item : inputs) {
			final String name = item.getName();
			module.setResolved(name, true);
		}
	}

	// -- Helper methods --

	private <T> WidgetModel addInput(final InputPanel<P, W> inputPanel,
		final Module module, final ModuleItem<T> item) throws ModuleException
	{
		final String name = item.getName();
		final boolean resolved = module.isResolved(name);
		if (resolved) return null; // skip resolved inputs

		final Class<T> type = item.getType();
		final WidgetModel model =
			new WidgetModel(inputPanel, module, item, getObjects(type));

		final WidgetService widgetService =
			getContext().getService(WidgetService.class);
		final InputWidget<?, ?> widget = widgetService.createWidget(model);
		if (widget != null) {
			// FIXME: This cast is NOT safe! Multiple UIs in the
			// classpath will have clashing widget implementations.
			@SuppressWarnings("unchecked")
			final InputWidget<?, W> typedWidget = (InputWidget<?, W>) widget;
			inputPanel.addWidget(typedWidget);
			return model;
		}

		if (item.isRequired()) {
			throw new ModuleException("A " + type.getSimpleName() +
				" is required but none exist.");
		}

		// item is not required; we can skip it
		return null;
	}

	/** Asks the object service for valid choices */
	private <T> List<T> getObjects(final Class<T> type) {
		final ObjectService objectService =
			getContext().getService(ObjectService.class);
		return objectService.getObjects(type);
	}

}
