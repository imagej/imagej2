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
