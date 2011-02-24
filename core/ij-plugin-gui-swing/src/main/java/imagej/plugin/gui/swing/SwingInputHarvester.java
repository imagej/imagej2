package imagej.plugin.gui.swing;

import java.awt.Dimension;
import java.awt.Toolkit;

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
	public boolean showDialog(final InputPanel inputPanel,
		final PluginModule<?> module)
	{
		final JOptionPane optionPane = new JOptionPane(null);
		optionPane.setOptionType(JOptionPane.OK_CANCEL_OPTION);
		final JDialog dialog = optionPane.createDialog(module.getInfo().getLabel());
		final JPanel mainPane = (JPanel) optionPane.getComponent(0);
		final JPanel widgetPane = (JPanel) mainPane.getComponent(0);
		widgetPane.add((SwingInputPanel) inputPanel);
		dialog.setModal(true);
		dialog.pack();
		ensureDialogSizeReasonable(dialog);
		dialog.setVisible(true);
		final Integer rval = (Integer) optionPane.getValue();
		return rval != null && rval == JOptionPane.OK_OPTION;
	}

	private void ensureDialogSizeReasonable(JDialog dialog)
	{
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		
		Dimension dialogSize = dialog.getSize();

		int newWidth = dialogSize.width;
		int newHeight = dialogSize.height;
		
		if (dialogSize.width > (0.75*screenSize.width))
			newWidth = (int)(0.75 * screenSize.width);
		
		if (dialogSize.height > (0.75*screenSize.height))
			newHeight = (int)(0.75 * screenSize.height);

		dialog.setSize(newWidth, newHeight);
	}
}
