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

package imagej.plugin.gui;

import imagej.Log;
import imagej.Prefs;
import imagej.module.ModuleItem;
import imagej.plugin.Parameter;
import imagej.plugin.PluginEntry;
import imagej.plugin.PluginException;
import imagej.plugin.PluginModule;
import imagej.plugin.PluginModuleItem;
import imagej.plugin.process.PluginPreprocessor;
import imagej.util.ClassUtils;

import java.io.File;

/**
 * InputHarvester is a plugin preprocessor that obtains the input parameters.
 * Parameters are collected using an {@link InputPanel} dialog box.
 * 
 * @author Curtis Rueden
 */
public abstract class AbstractInputHarvester implements PluginPreprocessor,
	InputHarvester
{

	private boolean canceled;

	// -- PluginPreprocessor methods --

	@Override
	public void process(final PluginModule<?> module) {
		final Iterable<ModuleItem> inputs = module.getInfo().inputs();
		if (!inputs.iterator().hasNext()) return; // no inputs to harvest

		final InputPanel inputPanel = createInputPanel();
		buildPanel(inputPanel, module);
		final boolean ok = showDialog(inputPanel, module);
		if (ok) harvestResults(inputPanel, module);
		else canceled = true;
	}

	@Override
	public boolean canceled() {
		return canceled;
	}

	// -- InputHarvester methods --

	@Override
	public void buildPanel(final InputPanel inputPanel,
		final PluginModule<?> module)
	{
		final Iterable<ModuleItem> inputs = module.getInfo().inputs();

		for (final ModuleItem item : inputs) {
			final String name = item.getName();
			final Class<?> type = item.getType();
			final Parameter param = ((PluginModuleItem) item).getParameter();
			final ParamDetails details =
				new ParamDetails(inputPanel, module, name, type, param);

			final boolean required = param.required();
			final boolean persist = param.persist();
			final String persistKey = param.persistKey();

			final Object defaultValue = required ? null : module.getInput(name);
			final String prefValue =
				persist ? getPrefValue(module, item, persistKey) : null;

			if (ClassUtils.isNumber(type)) {
				final Number initialValue =
					getInitialNumberValue(prefValue, defaultValue, type);
				addNumber(inputPanel, details, initialValue);
			}
			else if (ClassUtils.isText(type)) {
				final String initialValue =
					getInitialStringValue(prefValue, defaultValue);
				addTextField(inputPanel, details, initialValue);
			}
			else if (ClassUtils.isBoolean(type)) {
				boolean initialValue =
					getInitialBooleanValue(prefValue, defaultValue);
				addToggle(inputPanel, details, initialValue);
			}
			else if (File.class.isAssignableFrom(type)) {
				final File initialValue =
					getInitialFileValue(prefValue, defaultValue);
				addFile(inputPanel, details, initialValue);
			}
			else {
				final Object initialValue =
					getInitialObjectValue(prefValue, defaultValue);
				addObject(inputPanel, details, initialValue);
			}
		}
	}

	@Override
	public void harvestResults(final InputPanel inputPanel,
		final PluginModule<?> module)
	{
		final Iterable<ModuleItem> inputs = module.getInfo().inputs();

		for (final ModuleItem item : inputs) {
			final String name = item.getName();
			final Class<?> type = item.getType();

			final Parameter param = ((PluginModuleItem) item).getParameter();
			final boolean persist = param.persist();
			final String persistKey = param.persistKey();

			final Object value;
			if (ClassUtils.isNumber(type)) {
				value = inputPanel.getNumber(name);
			}
			else if (ClassUtils.isText(type)) {
				final String[] choices = param.choices();
				if (choices.length > 0) value = inputPanel.getChoice(name);
				else value = inputPanel.getTextField(name);
			}
			else if (ClassUtils.isBoolean(type)) {
				value = inputPanel.getToggle(name);
			}
			else if (File.class.isAssignableFrom(type)) {
				value = inputPanel.getFile(name);
			}
			else {
				value = inputPanel.getObject(name);
			}
			if (value != null) {
				// TODO - eliminate this unnecessary step
				// (widgets call setInput whenever they change)
				module.setInput(name, value);

				if (persist) {
					setPrefValue(module, item, persistKey, value);
				}
			}
		}
	}

	// -- Helper methods - input panel population --

	private void addNumber(final InputPanel inputPanel,
		final ParamDetails details, final Number initialValue)
	{
		final Class<?> type = details.getType();
		final Parameter param = details.getParameter();
		Number min = ClassUtils.toNumber(param.min(), type);
		if (min == null) min = ClassUtils.getMinimumNumber(type);
		Number max = ClassUtils.toNumber(param.max(), type);
		if (max == null) max = ClassUtils.getMaximumNumber(type);
		Number stepSize = ClassUtils.toNumber(param.stepSize(), type);
		if (stepSize == null) stepSize = ClassUtils.toNumber("1", type);
		final Number iValue = clampToRange(initialValue, min, max);
		inputPanel.addNumber(details, iValue, min, max, stepSize);
	}

	private void addTextField(final InputPanel inputPanel,
		final ParamDetails details, final String initialValue)
	{
		final String[] choices = details.getParameter().choices();
		if (choices.length > 0) {
			final String iValue = initialValue == null ? choices[0] : initialValue;
			inputPanel.addChoice(details, iValue, choices);
		}
		else {
			final String iValue = initialValue == null ? "" : initialValue;
			final int columns = details.getParameter().columns();
			inputPanel.addTextField(details, iValue, columns);
		}
	}

	private void addToggle(final InputPanel inputPanel,
		final ParamDetails details, final Boolean initialValue)
	{
		inputPanel.addToggle(details, initialValue);
	}

	private void addFile(final InputPanel inputPanel,
		final ParamDetails details, final File initialValue)
	{
		inputPanel.addFile(details, initialValue);
	}

	private void addObject(final InputPanel inputPanel, 
		final ParamDetails details, final Object initialValue)
	{
		inputPanel.addObject(details, initialValue);
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
	private Number clampToRange(final Number value,
		final Number min, final Number max)
	{
		final Class<?> type = value.getClass();
		if (Comparable.class.isAssignableFrom(type)) {
			final Comparable cValue = (Comparable) value;
			if (cValue.compareTo(min) < 0) return min;
			if (cValue.compareTo(max) > 0) return max;
		}
		return value;
	}

}
