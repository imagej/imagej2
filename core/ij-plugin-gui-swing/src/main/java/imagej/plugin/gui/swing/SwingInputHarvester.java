package imagej.plugin.gui.swing;

import imagej.plugin.Plugin;
import imagej.plugin.PluginModule;
import imagej.plugin.gui.AbstractInputHarvester;
import imagej.plugin.gui.InputPanel;
import imagej.plugin.process.PluginPreprocessor;

import java.awt.Frame;

import javax.swing.JDialog;

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
		final SwingInputPanel swingInputPanel = (SwingInputPanel) inputPanel;

		final Frame owner = null;
		final String title = module.getPlugin().getClass().getName(); //TEMP
		final JDialog dialog = new JDialog(owner, title, true);//TEMP
		dialog.setContentPane(swingInputPanel);
		dialog.pack();
		dialog.setVisible(true);
		// TODO: add OK and cancel buttons
		return true; //TEMP
	}

}
