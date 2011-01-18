package imagej.gui;

import imagej.Log;
import imagej.dataset.Dataset;
import imagej.plugin.ij2.IPlugin;
import imagej.plugin.ij2.Parameter;
import imagej.plugin.ij2.ParameterHandler;
import imagej.plugin.ij2.PluginPreprocessor;

import java.io.File;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.math.BigInteger;

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

			if (isNumber(type)) {
				Number min = toNumber(param.min(), type);
				if (min == null) min = getMinimumNumber(type);
				Number max = toNumber(param.max(), type);
				if (max == null) max = getMaximumNumber(type);
				Number stepSize = toNumber(param.stepSize(), type);
				if (stepSize == null) stepSize = toNumber("1", type);
				inputPanel.addNumber(name, label, (Number) value, min, max, stepSize);
			}
			else if (String.class.isAssignableFrom(type) || isCharacter(type)) {
				final String[] choices = param.choices();
				if (choices.length > 0) {
					inputPanel.addChoice(name, label, value.toString(), choices);
				}
				else {
					final int columns = param.columns();
					inputPanel.addTextField(name, label, value.toString(), columns);
				}
			}
			else if (isBoolean(type)) {
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
			if (isNumber(type)) {
				value = inputPanel.getNumber(name);
			}
			else if (isText(type)) {
				final String[] choices = param.choices();
				if (choices.length > 0) value = inputPanel.getChoice(name);
				else value = inputPanel.getTextField(name);
			}
			else if (isBoolean(type)) {
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

	/** Converts the given string to a {@link Number}. */
	private static Number toNumber(String num, Class<?> type) {
		if (num.isEmpty()) return null;
		if (isByte(type) || isShort(type) || isInteger(type)) {
			return new Integer(num);
		}
		if (isLong(type)) return new Long(num);
		if (isFloat(type)) return new Float(num);
		if (isDouble(type)) return new Double(num);
		if (BigInteger.class.isAssignableFrom(type)) return new BigInteger(num);
		if (BigDecimal.class.isAssignableFrom(type)) return new BigDecimal(num);
		return null;
	}
	
	private static Number getMinimumNumber(Class<?> type) {
		if (isByte(type)) return Byte.MIN_VALUE;
		if (isShort(type)) return Short.MIN_VALUE;
		if (isInteger(type)) return Integer.MIN_VALUE;
		if (isLong(type)) return Long.MIN_VALUE;
		if (isFloat(type)) return Float.MIN_VALUE;
		if (isDouble(type)) return Double.MIN_VALUE;
		return Double.NEGATIVE_INFINITY;
	}

	private static Number getMaximumNumber(Class<?> type) {
		if (isByte(type)) return Byte.MAX_VALUE;
		if (isShort(type)) return Short.MAX_VALUE;
		if (isInteger(type)) return Integer.MAX_VALUE;
		if (isLong(type)) return Long.MAX_VALUE;
		if (isFloat(type)) return Float.MAX_VALUE;
		if (isDouble(type)) return Double.MAX_VALUE;
		return Double.POSITIVE_INFINITY;
	}

	private static boolean isBoolean(Class<?> type) {
		return Boolean.class.isAssignableFrom(type) ||
			boolean.class.isAssignableFrom(type);
	}

	private static boolean isByte(Class<?> type) {
		return Byte.class.isAssignableFrom(type) ||
			byte.class.isAssignableFrom(type);
	}

	private static boolean isCharacter(Class<?> type) {
		return Character.class.isAssignableFrom(type) ||
			char.class.isAssignableFrom(type);
	}

	private static boolean isDouble(Class<?> type) {
		return Double.class.isAssignableFrom(type) ||
			double.class.isAssignableFrom(type);
	}

	private static boolean isFloat(Class<?> type) {
		return Float.class.isAssignableFrom(type) ||
			float.class.isAssignableFrom(type);
	}

	private static boolean isInteger(Class<?> type) {
		return Integer.class.isAssignableFrom(type) ||
			int.class.isAssignableFrom(type);
	}

	private static boolean isLong(Class<?> type) {
		return Long.class.isAssignableFrom(type) ||
			long.class.isAssignableFrom(type);
	}

	private static boolean isShort(Class<?> type) {
		return Short.class.isAssignableFrom(type) ||
			short.class.isAssignableFrom(type);
	}

	private static boolean isNumber(Class<?> type) {
		return Number.class.isAssignableFrom(type) ||
			byte.class.isAssignableFrom(type) ||
			double.class.isAssignableFrom(type) ||
			float.class.isAssignableFrom(type) ||
			int.class.isAssignableFrom(type) ||
			long.class.isAssignableFrom(type) ||
			short.class.isAssignableFrom(type);
	}
	
	private static boolean isText(Class<?> type) {
		return String.class.isAssignableFrom(type) || isCharacter(type);
	}

}
