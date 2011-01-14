package imagej.gui;

import imagej.Log;
import imagej.dataset.Dataset;
import imagej.plugin.ij2.IPlugin;
import imagej.plugin.ij2.Parameter;
import imagej.plugin.ij2.ParameterHandler;
import imagej.plugin.ij2.PluginPreprocessor;

import java.awt.Frame;
import java.io.File;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.math.BigInteger;

import javax.swing.JDialog;

import org.openide.util.lookup.ServiceProvider;

/**
 * InputHarvester is a plugin preprocessor that collects input parameter
 * values from the user using an {@link InputPanel} dialog box.
 */
@ServiceProvider(service=PluginPreprocessor.class)
public class InputHarvester implements PluginPreprocessor {

	@Override
	public void process(IPlugin plugin) {
		final SwingInputPanel inputPanel = new SwingInputPanel();
		buildPanel(inputPanel, plugin);
		showDialog(inputPanel, plugin);
		harvestResults(inputPanel, plugin);
	}

	private void buildPanel(InputPanel inputPanel, IPlugin plugin) {
		final Iterable<Field> inputs = ParameterHandler.getInputParameters(plugin);

		for (final Field field : inputs) {
			final Class<?> type = field.getType();

			final Parameter param = ParameterHandler.getParameter(field);
			final String label = param.label();
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

			if (Number.class.isAssignableFrom(type)) {
				final Number min = toNumber(param.min(), type);
				final Number max = toNumber(param.max(), type);
				final Number stepSize = toNumber(param.stepSize(), type);
				inputPanel.addNumber(label, (Number) value, min, max, stepSize);
			}
			else if (String.class.isAssignableFrom(type)) {
				final String[] choices = param.choices();
				if (choices.length > 0) {
					inputPanel.addChoice(label, choices, (String) value);
				}
				else {
					final int columns = param.columns();
					inputPanel.addTextField(label, (String) value, columns);
				}
			}
			else if (Boolean.class.isAssignableFrom(type)) {
				inputPanel.addToggle(label, (Boolean) value);
			}
			else if (File.class.isAssignableFrom(type)) {
				inputPanel.addFile(label, (File) value);
			}
			else if (Dataset.class.isAssignableFrom(type)) {
				inputPanel.addDataset(label, (Dataset) value);
			}
			else {
				// NB: unsupported field type
				Log.warn("Unsupported field type: " + type.getName());
			}
		}
	}

	private void showDialog(SwingInputPanel inputPanel, IPlugin plugin) {
		final Frame owner = null;
		final String title = plugin.getClass().getName(); //TEMP
		final JDialog dialog = new JDialog(owner, title, true);//TEMP
		dialog.setContentPane(inputPanel);
		dialog.setVisible(true);
		// TODO: add OK and cancel buttons
	}

	private void harvestResults(InputPanel inputPanel, IPlugin plugin) {
		// TODO: harvest inputPanel values and assign to plugin input parameters
	}

	/** Converts the given string to a {@link Number}. */
	private static Number toNumber(String num, Class<?> type) {
		if (Integer.class.isAssignableFrom(type)) {
			return new Integer(num);
		}
		if (Long.class.isAssignableFrom(type)) {
			return new Long(num);
		}
		if (Float.class.isAssignableFrom(type)) {
			return new Float(num);
		}
		if (Double.class.isAssignableFrom(type)) {
			return new Double(num);
		}
		if (BigInteger.class.isAssignableFrom(type)) {
			return new BigInteger(num);
		}
		if (BigDecimal.class.isAssignableFrom(type)) {
			return new BigDecimal(num);
		}
		return null;
	}

}
