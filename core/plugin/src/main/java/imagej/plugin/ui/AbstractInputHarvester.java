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

package imagej.plugin.ui;

import imagej.module.ModuleItem;
import imagej.plugin.ParamVisibility;
import imagej.plugin.Parameter;
import imagej.plugin.PluginEntry;
import imagej.plugin.PluginException;
import imagej.plugin.PluginModule;
import imagej.plugin.PluginModuleItem;
import imagej.plugin.process.PluginPreprocessor;
import imagej.util.ClassUtils;
import imagej.util.ColorRGB;
import imagej.util.Log;
import imagej.util.Prefs;

import java.io.File;
import java.util.ArrayList;

/**
 * Abstract superclass for input harvesters.
 * <p>
 * An input harvester is a plugin preprocessor that obtains the input
 * parameters. Parameters are collected using an {@link InputPanel} dialog box.
 * </p>
 * 
 * @author Curtis Rueden
 */
public abstract class AbstractInputHarvester implements PluginPreprocessor,
	InputHarvester
{

	private boolean canceled;

	// -- PluginPreprocessor methods --

	@Override
	public boolean canceled() {
		return canceled;
	}

	// -- PluginProcessor methods --

	@Override
	public void process(final PluginModule<?> module) {
		final InputPanel inputPanel = createInputPanel();
		buildPanel(inputPanel, module);
		if (!inputPanel.hasWidgets()) return; // no inputs left to harvest
		final boolean ok = harvestInputs(inputPanel, module);
		if (ok) processResults(inputPanel, module);
		else canceled = true;
	}

	// -- InputHarvester methods --

	@Override
	public void buildPanel(final InputPanel inputPanel,
		final PluginModule<?> module)
	{
		final Iterable<ModuleItem> inputs = module.getInfo().inputs();

		final ArrayList<ParamModel> models = new ArrayList<ParamModel>();

		for (final ModuleItem item : inputs) {
			final String name = item.getName();
			final boolean resolved = module.isResolved(name);
			if (resolved) continue; // skip resolved inputs

			final PluginModuleItem pmi = (PluginModuleItem) item;
			final Parameter param = pmi.getParameter();

			final Class<?> type = item.getType();
			final ParamModel model =
				new ParamModel(inputPanel, module, name, type, param);
			models.add(model);

			final boolean message = param.visibility() == ParamVisibility.MESSAGE;
			if (message) {
				addMessage(inputPanel, model);
				continue;
			}

			final boolean required = param.required();
			final boolean persist = param.persist();
			final String persistKey = param.persistKey();

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
				addObject(inputPanel, model, initialValue);
			}
		}

		// mark all models as initialized
		for (final ParamModel model : models)
			model.setInitialized(true);

		// compute initial preview
		module.preview();
	}

	@Override
	public void processResults(final InputPanel inputPanel,
		final PluginModule<?> module)
	{
		final Iterable<ModuleItem> inputs = module.getInfo().inputs();

		for (final ModuleItem item : inputs) {
			final String name = item.getName();
			module.setResolved(name, true);

			final PluginModuleItem pmi = (PluginModuleItem) item;
			final Parameter param = pmi.getParameter();

			final Object value = module.getInput(name);
			if (value == null) {
				if (param.required()) canceled = true;
				continue;
			}

			final boolean persist = param.persist();
			if (!persist) continue;

			final String persistKey = param.persistKey();
			setPrefValue(module, item, persistKey, value);
		}
	}

	// -- Helper methods - input panel population --

	private void addMessage(final InputPanel inputPanel, final ParamModel model)
	{
		inputPanel.addMessage(model.getValue().toString());
	}

	private void addNumber(final InputPanel inputPanel, final ParamModel model,
		final Number initialValue)
	{
		final Class<?> type = model.getType();
		final Parameter param = model.getParameter();
		Number min = ClassUtils.toNumber(param.min(), type);
		if (min == null) min = ClassUtils.getMinimumNumber(type);
		Number max = ClassUtils.toNumber(param.max(), type);
		if (max == null) max = ClassUtils.getMaximumNumber(type);
		Number stepSize = ClassUtils.toNumber(param.stepSize(), type);
		if (stepSize == null) stepSize = ClassUtils.toNumber("1", type);
		final Number iValue = clampToRange(initialValue, min, max);
		model.setValue(iValue);
		inputPanel.addNumber(model, min, max, stepSize);
	}

	private void addTextField(final InputPanel inputPanel,
		final ParamModel model, final String initialValue)
	{
		final String[] choices = model.getParameter().choices();
		if (choices.length > 0) {
			final String iValue = initialValue == null ? choices[0] : initialValue;
			model.setValue(iValue);
			inputPanel.addChoice(model, choices);
		}
		else {
			final String iValue = initialValue == null ? "" : initialValue;
			final int columns = model.getParameter().columns();
			model.setValue(iValue);
			inputPanel.addTextField(model, columns);
		}
	}

	private void addToggle(final InputPanel inputPanel, final ParamModel model,
		final Boolean initialValue)
	{
		model.setValue(initialValue);
		inputPanel.addToggle(model);
	}

	private void addFile(final InputPanel inputPanel, final ParamModel model,
		final File initialValue)
	{
		model.setValue(initialValue);
		inputPanel.addFile(model);
	}

	private void addColor(final InputPanel inputPanel, final ParamModel model,
		final ColorRGB initialValue)
	{
		model.setValue(initialValue);
		inputPanel.addColor(model);
	}

	private void addObject(final InputPanel inputPanel, final ParamModel model,
		final Object initialValue)
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

	private Object getInitialObjectValue(final String prefValue,
		final Object defaultValue)
	{
		if (prefValue != null) return prefValue;
		return defaultValue;
	}

	// -- Helper methods - persistence --

	private String getPrefValue(final PluginModule<?> module,
		final ModuleItem item, final String persistKey)
	{
		final String prefValue;
		if (persistKey.isEmpty()) {
			final String prefKey = item.getName();
			try {
				final PluginEntry<?> pluginEntry = module.getInfo().getPluginEntry();
				final Class<?> prefClass = pluginEntry.loadClass();
				prefValue = Prefs.get(prefClass, prefKey);
			}
			catch (final PluginException e) {
				Log.error("Error retrieving preference: " + prefKey, e);
				return null;
			}
		}
		else prefValue = Prefs.get(persistKey);
		return prefValue;
	}

	private void setPrefValue(final PluginModule<?> module,
		final ModuleItem item, final String persistKey, final Object value)
	{
		if (persistKey.isEmpty()) {
			final String prefKey = item.getName();
			try {
				final PluginEntry<?> pluginEntry = module.getInfo().getPluginEntry();
				final Class<?> prefClass = pluginEntry.loadClass();
				Prefs.put(prefClass, prefKey, value.toString());
			}
			catch (final PluginException e) {
				Log.error("Error storing preference: " + prefKey, e);
			}
		}
		else Prefs.put(persistKey, value.toString());
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
