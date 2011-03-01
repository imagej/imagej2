package imagej.gui;

import imagej.Log;
import imagej.event.Events;
import imagej.plugin.api.PluginException;
import imagej.tool.ITool;
import imagej.tool.ToolEntry;
import imagej.tool.ToolUtils;
import imagej.tool.event.ToolActivatedEvent;
import imagej.tool.event.ToolDeactivatedEvent;

import java.net.URL;
import java.util.List;

import javax.swing.AbstractButton;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 * Button bar with selectable tools, similar to ImageJ 1.x.
 *
 * @author Curtis Rueden
 */
public class ToolBar extends JToolBar {

	public ToolBar() {
		populateToolBar();
	}

	private void populateToolBar() {
		final List<ToolEntry> entries = ToolUtils.findTools();
		final ButtonGroup buttonGroup = new ButtonGroup();
		for (final ToolEntry entry : entries) {
			try {
				add(createButton(entry, buttonGroup));
			}
			catch (PluginException e) {
				Log.warn("Invalid tool: " + entry, e);
			}
		}
	}

	private AbstractButton createButton(final ToolEntry entry,
		final ButtonGroup buttonGroup) throws PluginException
	{
		final ITool tool = entry.createInstance();
		final String name = entry.getName();
		final String label = entry.getLabel();
		final String description = entry.getDescription();
		final URL iconURL = entry.getIconURL();

		final JToggleButton button = new JToggleButton();
		if (iconURL == null) {
			button.setText(name);
			Log.warn("Invalid icon for tool: " + tool);
		}
		else button.setIcon(new ImageIcon(iconURL, label));
		if (description != null && !description.isEmpty()) {
			button.setToolTipText(description);
		}
		buttonGroup.add(button);

		button.addChangeListener(new ChangeListener() {
			boolean active = false;
			@Override
			public void stateChanged(ChangeEvent e) {
				boolean selected = button.isSelected();
				if (selected == active) return;
				if (selected) Events.publish(new ToolActivatedEvent(tool));					
				else Events.publish(new ToolDeactivatedEvent(tool));					
				active = selected;
			}
		});

		return button;
	}

}
