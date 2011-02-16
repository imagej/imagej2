package imagej.plugin.gui;

import imagej.Log;
import imagej.dataset.Dataset;
import imagej.plugin.Parameter;
import imagej.plugin.PluginModule;
import imagej.plugin.process.PluginPreprocessor;
import imagej.util.ClassUtils;

import java.io.File;
import java.lang.reflect.Field;

/**
 * InputHarvester is a plugin preprocessor that obtains the input parameters.
 *
 * It first assigns values from the passed-in input map. Any remaining
 * parameters are collected using an {@link InputPanel} dialog box.
 */
public abstract class AbstractInputHarvester
	implements PluginPreprocessor, InputHarvester
{

	private boolean canceled;

	// -- PluginPreprocessor methods --

	@Override
	public void process(final PluginModule<?> module) {
		final Iterable<Field> inputs = module.getInfo().getInputFields();
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
	public abstract InputPanel createInputPanel();

	@Override
	public void buildPanel(InputPanel inputPanel, PluginModule<?> module) {
		final Iterable<Field> inputs = module.getInfo().getInputFields();

		for (final Field field : inputs) {
			final String name = field.getName();
			final Class<?> type = field.getType();
			final Parameter param = module.getInfo().getParameter(field);

			final String label = makeLabel(name, param.label());
			final boolean required = param.required();
			final String persist = param.persist();

			Object value = "";
			if (!persist.isEmpty()) {
				// TODO - retrieve initial value from persistent storage
			}
			else if (!required) {
				value = module.getValue(field);
			}

			if (ClassUtils.isNumber(type)) {
				Number min = ClassUtils.toNumber(param.min(), type);
				if (min == null) min = ClassUtils.getMinimumNumber(type);
				Number max = ClassUtils.toNumber(param.max(), type);
				if (max == null) max = ClassUtils.getMaximumNumber(type);
				Number stepSize = ClassUtils.toNumber(param.stepSize(), type);
				if (stepSize == null) stepSize = ClassUtils.toNumber("1", type);
				inputPanel.addNumber(name, label, (Number) value, min, max, stepSize);
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
			else if (Dataset.class.isAssignableFrom(type)) {
				inputPanel.addDataset(name, label, (Dataset) value);
			}
			else {
				// NB: unsupported field type
				Log.warn("Unsupported field type: " + type.getName());
			}
		}
	}

	@Override
	public abstract boolean showDialog(InputPanel inputPanel,
		PluginModule<?> module);

	@Override
	public void harvestResults(InputPanel inputPanel, PluginModule<?> module) {
		// TODO: harvest inputPanel values and assign to plugin input parameters
		final Iterable<Field> inputs = module.getInfo().getInputFields();

		for (final Field field : inputs) {
			final String name = field.getName();
			final Class<?> type = field.getType();
			final Parameter param = module.getInfo().getParameter(field);

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
			else if (Dataset.class.isAssignableFrom(type)) {
				value = inputPanel.getDataset(name);
			}
			else value = null;
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
