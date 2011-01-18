package imagej.gui;

import imagej.Log;
import imagej.dataset.Dataset;
import imagej.plugin.ij2.IPlugin;
import imagej.plugin.ij2.Parameter;
import imagej.plugin.ij2.ParameterHandler;
import imagej.plugin.ij2.PluginPreprocessor;
import imagej.util.ClassUtils;

import java.io.File;
import java.lang.reflect.Field;

/**
 * InputHarvester is a plugin preprocessor that collects input parameter
 * values from the user using an {@link InputPanel} dialog box.
 */
public abstract class AbstractInputHarvester
	implements PluginPreprocessor, InputHarvester
{

	@Override
	public void process(IPlugin plugin) {
		final InputPanel inputPanel = createInputPanel();
		buildPanel(inputPanel, plugin);
		final boolean ok = showDialog(inputPanel, plugin);
		if (!ok) return;
		harvestResults(inputPanel, plugin);
	}

	@Override
	public abstract InputPanel createInputPanel();

	@Override
	public void buildPanel(InputPanel inputPanel, IPlugin plugin) {
		final Iterable<Field> inputs = ParameterHandler.getInputParameters(plugin);

		for (final Field field : inputs) {
			final String name = field.getName();
			final Class<?> type = field.getType();
			final Parameter param = ParameterHandler.getParameter(field);

			final String label = makeLabel(name, param.label());
			final boolean required = param.required();
			final String persist = param.persist();

			Object value = null;
			if (!persist.isEmpty()) {
				// TODO - retrieve initial value from persistent storage
			}
			else if (!required) {
				try {
					value = field.get(plugin);
				}
				catch (IllegalArgumentException e) {
					Log.printStackTrace(e);
				}
				catch (IllegalAccessException e) {
					Log.printStackTrace(e);
				}
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
					inputPanel.addChoice(name, label, value.toString(), choices);
				}
				else {
					final int columns = param.columns();
					inputPanel.addTextField(name, label, value.toString(), columns);
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
	public abstract boolean showDialog(InputPanel inputPanel, IPlugin plugin);

	@Override
	public void harvestResults(InputPanel inputPanel, IPlugin plugin) {
		// TODO: harvest inputPanel values and assign to plugin input parameters
		final Iterable<Field> inputs = ParameterHandler.getInputParameters(plugin);

		for (final Field field : inputs) {
			final String name = field.getName();
			final Class<?> type = field.getType();
			final Parameter param = ParameterHandler.getParameter(field);

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
			if (value != null) ParameterHandler.setParameter(plugin, name, value);
		}
	}

	private String makeLabel(String name, String label) {
		if (label == null || label.isEmpty()) {
			label = name.substring(0, 1).toUpperCase() + name.substring(1);
		}
		return label;
	}

}
