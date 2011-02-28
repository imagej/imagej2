package imagej.gui;

import imagej.Log;
import imagej.plugin.api.PluginException;
import imagej.tool.ITool;
import imagej.tool.ToolEntry;
import imagej.tool.ToolUtils;

import java.awt.Dimension;
import java.net.URL;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JToolBar;

/**
 * Simple status bar with text area and progress bar, similar to ImageJ 1.x.
 *
 * @author Curtis Rueden
 */
public class ToolBar extends JToolBar {

	public ToolBar() {
		setPreferredSize(new Dimension(26 * 21, 26));//TEMP
		populateToolBar();
	}

	private void populateToolBar() {
		final List<ToolEntry> entries = ToolUtils.findTools();
		for (final ToolEntry entry : entries) {
			try {
				final JButton button = createButton(entry);
				add(button);
			}
			catch (PluginException e) {
				Log.warn("Invalid tool: " + entry, e);
			}
		}
	}

	private JButton createButton(final ToolEntry entry) throws PluginException {
		final ITool tool = entry.createInstance();
		final String name = entry.getName();
		final String label = entry.getLabel();
		final String description = entry.getDescription();
		final URL iconURL = entry.getIconURL();

		final JButton button = new JButton();
		if (iconURL == null) {
			button.setText(name);
			Log.warn("Invalid icon for tool: " + tool);
		}
		else button.setIcon(new ImageIcon(iconURL, label));
		if (description != null && !description.isEmpty()) {
			button.setToolTipText(description);
		}
		return button;
	}

}
