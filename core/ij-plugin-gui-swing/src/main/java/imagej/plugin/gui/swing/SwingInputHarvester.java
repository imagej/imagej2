package imagej.plugin.gui.swing;

import imagej.plugin.Plugin;
import imagej.plugin.PluginModule;
import imagej.plugin.gui.AbstractInputHarvester;
import imagej.plugin.gui.InputPanel;
import imagej.plugin.process.PluginPreprocessor;

import java.awt.Dimension;
import java.awt.Toolkit;

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
		// TODO - use JScrollPane in case there are many widgets
		widgetPane.add((SwingInputPanel) inputPanel);
		dialog.setModal(true);
		dialog.pack();
		ensureDialogSizeReasonable(dialog);
		dialog.setVisible(true);
		final Integer rval = (Integer) optionPane.getValue();
		return rval != null && rval == JOptionPane.OK_OPTION;
	}

	private void ensureDialogSizeReasonable(final JDialog dialog) {
		final Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();		
		final Dimension dialogSize = dialog.getSize();

		int newWidth = dialogSize.width;
		int newHeight = dialogSize.height;

		final int maxWidth = 3 * screenSize.width / 4;
		final int maxHeight = 3 * screenSize.height / 4;

		if (newWidth > maxWidth) newWidth = maxWidth;
		if (newHeight > maxHeight) newHeight = maxHeight;

		dialog.setSize(newWidth, newHeight);
	}
}
