package imagej.gui;

import imagej.Log;
import imagej.event.EventSubscriber;
import imagej.plugin.api.PluginException;
import imagej.tool.ITool;
import imagej.tool.ToolEntry;
import imagej.tool.ToolManager;
import imagej.tool.event.ToolActivatedEvent;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;

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
public class ToolBar extends JToolBar
	implements EventSubscriber<ToolActivatedEvent>
{

	private ToolManager toolManager;

	private Map<String, AbstractButton> toolButtons;

	public ToolBar() {
		toolManager = new ToolManager();
		toolButtons = new HashMap<String, AbstractButton>();
		populateToolBar();
	}

	public ToolManager getToolManager() {
		return toolManager;
	}

	private void populateToolBar() {
		final ButtonGroup buttonGroup = new ButtonGroup();
		for (final ToolEntry entry : toolManager.getToolEntries()) {
			try {
				final AbstractButton button = createButton(entry, buttonGroup);
				toolButtons.put(entry.getName(), button);
				add(button);
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
		// TODO - consider alternatives to assigning the entry manually
		tool.setToolEntry(entry);
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
				getToolManager().setActiveTool(tool);
				active = selected;
			}
		});

		return button;
	}

	@Override
	public void onEvent(final ToolActivatedEvent event) {
		final String name = event.getTool().getName();
		if (name == null) return; // no name, no button?
		final AbstractButton button = toolButtons.get(name);
		if (button == null) return; // not on toolbar
		button.setSelected(true);
	}

}
