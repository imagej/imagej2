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

import imagej.module.ModuleItem;
import imagej.plugin.Parameter;
import imagej.plugin.PluginModule;
import imagej.plugin.PluginModuleItem;
import imagej.plugin.process.PluginPreprocessor;
import imagej.util.ClassUtils;

import java.io.File;

/**
 * InputHarvester is a plugin preprocessor that obtains the input parameters.
 *
 * It first assigns values from the passed-in input map. Any remaining
 * parameters are collected using an {@link InputPanel} dialog box.
 *
 * @author Curtis Rueden
 */
public abstract class AbstractInputHarvester
	implements PluginPreprocessor, InputHarvester
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
	public boolean canceled() { return canceled; }

	// -- InputHarvester methods --

	@Override
	public void buildPanel(InputPanel inputPanel, PluginModule<?> module) {
		final Iterable<ModuleItem> inputs = module.getInfo().inputs();

		for (final ModuleItem item : inputs) {
			final String name = item.getName();
			final Class<?> type = item.getType();
			final Parameter param = ((PluginModuleItem) item).getParameter();

			final String label = makeLabel(name, param.label());
			final boolean required = param.required();
			final String persist = param.persist();
			final WidgetStyle style = param.widgetStyle();

			Object value = "";
			if (!persist.isEmpty()) {
				// TODO - retrieve initial value from persistent storage
			}
			else if (!required) {
				value = module.getInput(name);
			}

			if (ClassUtils.isNumber(type)) {
				Number min = ClassUtils.toNumber(param.min(), type);
				if (min == null) min = ClassUtils.getMinimumNumber(type);
				Number max = ClassUtils.toNumber(param.max(), type);
				if (max == null) max = ClassUtils.getMaximumNumber(type);
				Number stepSize = ClassUtils.toNumber(param.stepSize(), type);
				if (stepSize == null) stepSize = ClassUtils.toNumber("1", type);
				// BDZ begin - range clamp initial value to avoid runtime error
				Number val = (Number) value;
				if (val.doubleValue() < min.doubleValue())
					value = min;
				if (val.doubleValue() > max.doubleValue())
					value = max;
				// BDZ end
				inputPanel.addNumber(name, label, (Number) value,
					min, max, stepSize, style);
			}
			else if (ClassUtils.isText(type)) {
				final String[] choices = param.choices();
				if (choices.length > 0) {
					final String initialValue =
						value == null ? choices[0] : value.toString();
					inputPanel.addChoice(name, label, initialValue, choices);
				}
				else {
					final String initialValue =
						value == null ? "" : value.toString();
					final int columns = param.columns();
					inputPanel.addTextField(name, label, initialValue, columns);
				}
			}
			else if (ClassUtils.isBoolean(type)) {
				inputPanel.addToggle(name, label, (Boolean) value);
			}
			else if (File.class.isAssignableFrom(type)) {
				inputPanel.addFile(name, label, (File) value);
			}
			else {
				inputPanel.addObject(name, label, value);
			}
		}
	}

	@Override
	public void harvestResults(InputPanel inputPanel, PluginModule<?> module) {
		final Iterable<ModuleItem> inputs = module.getInfo().inputs();

		for (final ModuleItem item : inputs) {
			final String name = item.getName();
			final Class<?> type = item.getType();
			final Parameter param = ((PluginModuleItem) item).getParameter();

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
			if (value != null) module.setInput(name, value);
		}
	}

	private String makeLabel(String name, String label) {
		if (label == null || label.isEmpty()) {
			return name.substring(0, 1).toUpperCase() + name.substring(1);
		}
		return label;
	}

}
