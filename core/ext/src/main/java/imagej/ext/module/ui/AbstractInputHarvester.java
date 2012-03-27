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

package imagej.ext.module.ui;

import imagej.ext.module.ItemVisibility;
import imagej.ext.module.Module;
import imagej.ext.module.ModuleCanceledException;
import imagej.ext.module.ModuleException;
import imagej.ext.module.ModuleItem;
import imagej.util.ClassUtils;
import imagej.util.ColorRGB;
import imagej.util.Log;
import imagej.util.NumberUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Abstract superclass for {@link InputHarvester}s.
 * <p>
 * An input harvester obtains a module's unresolved input parameter values from
 * the user. Parameters are collected using an {@link InputPanel} dialog box.
 * </p>
 * 
 * @author Curtis Rueden
 */
public abstract class AbstractInputHarvester implements InputHarvester {

	// -- InputHarvester methods --

	@Override
	public void harvest(final Module module) throws ModuleException {
		final InputPanel inputPanel = createInputPanel();
		buildPanel(inputPanel, module);
		if (!inputPanel.hasWidgets()) return; // no inputs left to harvest

		final boolean ok = harvestInputs(inputPanel, module);
		if (!ok) throw new ModuleCanceledException();

		processResults(inputPanel, module);
	}

	@Override
	public void buildPanel(final InputPanel inputPanel, final Module module)
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
	public void processResults(final InputPanel inputPanel, final Module module)
		throws ModuleException
	{
		final Iterable<ModuleItem<?>> inputs = module.getInfo().inputs();

		for (final ModuleItem<?> item : inputs) {
			final String name = item.getName();
			module.setResolved(name, true);
			saveValue(module, item);
		}
	}

	// -- Helper methods - input panel population --

	private <T> WidgetModel addInput(final InputPanel inputPanel,
		final Module module, final ModuleItem<T> item) throws ModuleException
	{
		final String name = item.getName();
		final boolean resolved = module.isResolved(name);
		if (resolved) return null; // skip resolved inputs

		final Class<T> type = item.getType();
		final WidgetModel model = new WidgetModel(inputPanel, module, item);

		final boolean message = item.getVisibility() == ItemVisibility.MESSAGE;
		if (message) {
			addMessage(inputPanel, model, item.getLabel());
			return model;
		}

		final T defaultValue = item.getValue(module);
		final T prefValue = item.loadValue();
		final T initialValue = getInitialValue(prefValue, defaultValue, type);

		if (ClassUtils.isNumber(type)) {
			addNumber(inputPanel, model, (Number) initialValue);
		}
		else if (ClassUtils.isText(type)) {
			final String sInitialValue =
				ClassUtils.convert(initialValue, String.class);
			addTextField(inputPanel, model, sInitialValue);
		}
		else if (ClassUtils.isBoolean(type)) {
			addToggle(inputPanel, model, (Boolean) initialValue);
		}
		else if (File.class.isAssignableFrom(type)) {
			addFile(inputPanel, model, (File) initialValue);
		}
		else if (ColorRGB.class.isAssignableFrom(type)) {
			addColor(inputPanel, model, (ColorRGB) initialValue);
		}
		else {
			try {
				addObject(inputPanel, model, initialValue);
			}
			catch (final ModuleException e) {
				if (item.isRequired()) {
					throw new ModuleException("A " + type.getSimpleName() +
						" is required but none exist.", e);
				}
				Log.debug(e);
			}
		}

		return model;
	}

	private void
		addMessage(final InputPanel inputPanel, final WidgetModel model, String label)
	{
		String message = model.getValue().toString();
		if ((label != null) && (label.length() > 0))
			message = label + ": " + message;
		inputPanel.addMessage(message);
	}

	private void addNumber(final InputPanel inputPanel, final WidgetModel model,
		final Number initialValue)
	{
		final ModuleItem<?> item = model.getItem();
		final Class<?> type = item.getType();
		final Class<?> saneType = ClassUtils.getNonprimitiveType(type);

		final Object itemMin = item.getMinimumValue();
		Number min = NumberUtils.toNumber(itemMin, saneType);
		if (min == null) min = NumberUtils.getMinimumNumber(type);

		final Object itemMax = item.getMaximumValue();
		Number max = NumberUtils.toNumber(itemMax, saneType);
		if (max == null) max = NumberUtils.getMaximumNumber(type);

		final Object itemStep = item.getStepSize();
		Number stepSize = NumberUtils.toNumber(itemStep, saneType);
		if (stepSize == null) stepSize = NumberUtils.toNumber("1", type);

		final Number iValue =
			NumberUtils.clampToRange(type, initialValue, min, max);
		model.setValue(iValue);
		inputPanel.addNumber(model, min, max, stepSize);
	}

	private void addTextField(final InputPanel inputPanel,
		final WidgetModel model, final String initialValue)
	{
		final ModuleItem<?> item = model.getItem();
		final List<?> itemChoices = item.getChoices();
		final String[] choices = new String[itemChoices.size()];
		for (int i = 0; i < choices.length; i++) {
			choices[i] = itemChoices.get(i).toString();
		}
		if (choices.length > 0) {
			final String iValue = initialValue == null ? choices[0] : initialValue;
			model.setValue(iValue);
			inputPanel.addChoice(model, choices);
		}
		else {
			final String iValue = initialValue == null ? "" : initialValue;
			final int columns = item.getColumnCount();
			model.setValue(iValue);
			inputPanel.addTextField(model, columns);
		}
	}

	private void addToggle(final InputPanel inputPanel, final WidgetModel model,
		final Boolean initialValue)
	{
		model.setValue(initialValue == null ? Boolean.FALSE : initialValue);
		inputPanel.addToggle(model);
	}

	private void addFile(final InputPanel inputPanel, final WidgetModel model,
		final File initialValue)
	{
		model.setValue(initialValue);
		inputPanel.addFile(model);
	}

	private void addColor(final InputPanel inputPanel, final WidgetModel model,
		final ColorRGB initialValue)
	{
		model.setValue(initialValue);
		inputPanel.addColor(model);
	}

	private void addObject(final InputPanel inputPanel, final WidgetModel model,
		final Object initialValue) throws ModuleException
	{
		model.setValue(initialValue);
		inputPanel.addObject(model);
	}

	// -- Helper methods - initial value computation --

	private <T> T getInitialValue(final Object prefValue,
		final Object defaultValue, final Class<T> type)
	{
		if (prefValue != null) return ClassUtils.convert(prefValue, type);
		if (defaultValue != null) return ClassUtils.convert(defaultValue, type);
		return ClassUtils.getNullValue(type);
	}

	// -- Helper methods - other --

	/** Saves the value of the given module item to persistent storage. */
	private <T> void saveValue(final Module module, final ModuleItem<T> item)
		throws ModuleException
	{
		final T value = item.getValue(module);
		if (value == null) {
			if (item.isRequired()) throw new ModuleCanceledException();
		}
		item.saveValue(value);
	}

}
