//
// AbstractInputHarvester.java
//

/*
ImageJ software for multidimensional image processing and analysis.

Copyright (c) 2010, ImageJDev.org.
All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met:
    * Redistributions of source code must retain the above copyright
      notice, this list of conditions and the following disclaimer.
    * Redistributions in binary form must reproduce the above copyright
      notice, this list of conditions and the following disclaimer in the
      documentation and/or other materials provided with the distribution.
    * Neither the names of the ImageJDev.org developers nor the
      names of its contributors may be used to endorse or promote products
      derived from this software without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
POSSIBILITY OF SUCH DAMAGE.
*/

package imagej.module.ui;

import imagej.module.ItemVisibility;
import imagej.module.Module;
import imagej.module.ModuleException;
import imagej.module.ModuleInfo;
import imagej.module.ModuleItem;
import imagej.util.ClassUtils;
import imagej.util.ColorRGB;
import imagej.util.Log;
import imagej.util.Prefs;

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
		if (!ok) throw new ModuleException(); // canceled

		processResults(inputPanel, module);
	}

	@Override
	public void buildPanel(final InputPanel inputPanel, final Module module)
		throws ModuleException
	{
		final Iterable<ModuleItem<?>> inputs = module.getInfo().inputs();

		final ArrayList<WidgetModel> models = new ArrayList<WidgetModel>();

		for (final ModuleItem<?> item : inputs) {
			final String name = item.getName();
			final boolean resolved = module.isResolved(name);
			if (resolved) continue; // skip resolved inputs

			final Class<?> type = item.getType();
			final WidgetModel model = new WidgetModel(inputPanel, module, item);
			models.add(model);

			final boolean message = item.getVisibility() == ItemVisibility.MESSAGE;
			if (message) {
				addMessage(inputPanel, model);
				continue;
			}

			final boolean required = item.isRequired();
			final boolean persist = item.isPersisted();
			final String persistKey = item.getPersistKey();

			final Object defaultValue = required ? null : module.getInput(name);
			final String prefValue =
				persist ? getPrefValue(module, item, persistKey) : null;

			if (ClassUtils.isNumber(type)) {
				final Number initialValue =
					getInitialNumberValue(prefValue, defaultValue, type);
				addNumber(inputPanel, model, initialValue);
			}
			else if (ClassUtils.isText(type)) {
				final String initialValue =
					getInitialStringValue(prefValue, defaultValue);
				addTextField(inputPanel, model, initialValue);
			}
			else if (ClassUtils.isBoolean(type)) {
				final boolean initialValue =
					getInitialBooleanValue(prefValue, defaultValue);
				addToggle(inputPanel, model, initialValue);
			}
			else if (File.class.isAssignableFrom(type)) {
				final File initialValue = getInitialFileValue(prefValue, defaultValue);
				addFile(inputPanel, model, initialValue);
			}
			else if (ColorRGB.class.isAssignableFrom(type)) {
				final ColorRGB initialValue =
					getInitialColorValue(prefValue, defaultValue);
				addColor(inputPanel, model, initialValue);
			}
			else {
				final Object initialValue =
					getInitialObjectValue(prefValue, defaultValue);
				try {
					addObject(inputPanel, model, initialValue);
				}
				catch (final ModuleException e) {
					throw new ModuleException("A " + type.getName() +
						" is required but none exist.", e);
				}
			}
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

			final Object value = module.getInput(name);
			if (value == null) {
				if (item.isRequired()) throw new ModuleException(); // canceled
			}

			final boolean persist = item.isPersisted();
			if (!persist) continue;

			final String persistKey = item.getPersistKey();
			setPrefValue(module, item, persistKey, value);
		}
	}

	// -- Helper methods - input panel population --

	private void
		addMessage(final InputPanel inputPanel, final WidgetModel model)
	{
		inputPanel.addMessage(model.getValue().toString());
	}

	private void addNumber(final InputPanel inputPanel, final WidgetModel model,
		final Number initialValue)
	{
		final ModuleItem<?> item = model.getItem();
		final Class<?> type = item.getType();

		final Object itemMin = item.getMinimumValue();
		Number min = ClassUtils.toNumber(itemMin, type);
		if (min == null) min = ClassUtils.getMinimumNumber(type);

		final Object itemMax = item.getMaximumValue();
		Number max = ClassUtils.toNumber(itemMax, type);
		if (max == null) max = ClassUtils.getMaximumNumber(type);

		final Object itemStep = item.getStepSize();
		Number stepSize = ClassUtils.toNumber(itemStep, type);
		if (stepSize == null) stepSize = ClassUtils.toNumber("1", type);

		final Number iValue = clampToRange(initialValue, min, max);
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
		model.setValue(initialValue);
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

	private Number getInitialNumberValue(final String prefValue,
		final Object defaultValue, final Class<?> type)
	{
		if (prefValue != null) return ClassUtils.toNumber(prefValue, type);
		if (defaultValue != null) {
			return ClassUtils.toNumber(defaultValue.toString(), type);
		}
		return null;
	}

	private String getInitialStringValue(final String prefValue,
		final Object defaultValue)
	{
		if (prefValue != null) return prefValue;
		if (defaultValue != null) return defaultValue.toString();
		return null;
	}

	private Boolean getInitialBooleanValue(final String prefValue,
		final Object defaultValue)
	{
		if (prefValue != null) return new Boolean(prefValue);
		if (defaultValue != null) return new Boolean(defaultValue.toString());
		return Boolean.FALSE;
	}

	private File getInitialFileValue(final String prefValue,
		final Object defaultValue)
	{
		if (prefValue != null) return new File(prefValue);
		if (defaultValue != null) return new File(defaultValue.toString());
		return null;
	}

	private ColorRGB getInitialColorValue(final String prefValue,
		final Object defaultValue)
	{
		if (prefValue != null) return new ColorRGB(prefValue);
		if (defaultValue != null) return new ColorRGB(defaultValue.toString());
		return null;
	}

	/**
	 * @param prefValue Ignored for arbitrary objects, since we have no general
	 *          way to translate a string back to a particular object type.
	 */
	private Object getInitialObjectValue(final String prefValue,
		final Object defaultValue)
	{
		return defaultValue;
	}

	// -- Helper methods - persistence --

	private String getPrefValue(final Module module, final ModuleItem<?> item,
		final String persistKey)
	{
		String prefValue = null;
		if (persistKey.isEmpty()) {
			final String prefKey = item.getName();
			final Class<?> prefClass = getPrefClass(module, prefKey);
			if (prefClass != null) prefValue = Prefs.get(prefClass, prefKey);
		}
		else prefValue = Prefs.get(persistKey);
		return prefValue;
	}

	private void setPrefValue(final Module module, final ModuleItem<?> item,
		final String persistKey, final Object value)
	{
		if (persistKey.isEmpty()) {
			final String prefKey = item.getName();
			final Class<?> prefClass = getPrefClass(module, prefKey);
			if (prefClass != null) Prefs.put(prefClass, prefKey, value.toString());
		}
		else Prefs.put(persistKey, value.toString());
	}

	private Class<?> getPrefClass(final Module module, final String prefKey) {
		final ModuleInfo info = module.getInfo();
		final String className = info.getDelegateClassName();
		final Class<?> prefClass = ClassUtils.loadClass(className);
		if (prefClass == null) {
			Log.error("Error locating preference '" + prefKey + "' from class " +
				className);
		}
		return prefClass;
	}

	// -- Helper methods - other --

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private Number clampToRange(final Number value, final Number min,
		final Number max)
	{
		if (value == null) return min;
		final Class<?> type = value.getClass();
		if (Comparable.class.isAssignableFrom(type)) {
			final Comparable cValue = (Comparable) value;
			if (cValue.compareTo(min) < 0) return min;
			if (cValue.compareTo(max) > 0) return max;
		}
		return value;
	}

}
