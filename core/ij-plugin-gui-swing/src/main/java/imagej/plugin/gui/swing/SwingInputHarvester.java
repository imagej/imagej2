package imagej.plugin.gui.swing;

import imagej.plugin.Plugin;
import imagej.plugin.PluginModule;
import imagej.plugin.gui.AbstractInputHarvester;
import imagej.plugin.gui.InputPanel;
import imagej.plugin.process.PluginPreprocessor;

import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

/**
 * SwingInputHarvester is a plugin preprocessor that collects input parameter
 * values from the user using a {@link SwingInputPanel} dialog box.
 */
@Plugin(type = PluginPreprocessor.class)
public class SwingInputHarvester extends AbstractInputHarvester {

	@Override
	public InputPanel createInputPanel() {
		return new SwingInputPanel();
	}

	@Override
	public boolean showDialog(InputPanel inputPanel, PluginModule<?> module) {
		final JOptionPane optionPane = new JOptionPane(null);
		optionPane.setOptionType(JOptionPane.OK_CANCEL_OPTION);
		final JDialog dialog = optionPane.createDialog(module.getInfo().getLabel());
		final JPanel mainPane = (JPanel) optionPane.getComponent(0);
		final JPanel widgetPane = (JPanel) mainPane.getComponent(0);
		widgetPane.add((SwingInputPanel) inputPanel);
		dialog.setModal(true);
		dialog.pack();
		dialog.setVisible(true);
		final Integer rval = (Integer) optionPane.getValue();
		return rval != null && rval == JOptionPane.OK_OPTION;
	}

}
