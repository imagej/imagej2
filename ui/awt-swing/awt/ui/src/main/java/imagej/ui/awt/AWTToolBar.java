
package imagej.ui.awt;

import imagej.ImageJ;
import imagej.event.EventService;
import imagej.event.StatusEvent;
import imagej.ext.InstantiableException;
import imagej.ext.plugin.PluginInfo;
import imagej.ext.tool.Tool;
import imagej.ext.tool.ToolService;
import imagej.ui.ToolBar;
import imagej.util.Log;

import java.awt.Button;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Label;
import java.awt.Panel;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 * Button bar with selectable tools, similar to ImageJ 1.x.
 * 
 * @author Curtis Rueden
 */
public class AWTToolBar extends Panel implements ToolBar {

	private final ToolService toolService;

	private final Map<String, Button> toolButtons;

	final protected EventService eventService;

	public AWTToolBar() {
		eventService = ImageJ.get(EventService.class);
		toolService = ImageJ.get(ToolService.class);
		toolButtons = new HashMap<String, Button>();
		setLayout(new FlowLayout());
		populateToolBar();
	}

	// -- ToolBar methods --

	@Override
	public ToolService getToolService() {
		return toolService;
	}

	// -- Helper methods --

	private void populateToolBar() {
		Tool lastTool = null;
		for (final Tool tool : toolService.getTools()) {
			try {
				final Button button = createButton(tool);
				toolButtons.put(tool.getInfo().getName(), button);
				add(button);

				// add a separator between tools where applicable
				if (toolService.isSeparatorNeeded(tool, lastTool)) add(new Label(" "));
				lastTool = tool;
			}
			catch (final InstantiableException e) {
				Log.warn("Invalid tool: " + tool.getInfo(), e);
			}
		}
	}

	private Button createButton(final Tool tool)
		throws InstantiableException
	{
		final PluginInfo<Tool> info = tool.getInfo();
		final String name = info.getName();
		final String label = info.getLabel();
		final URL iconURL = info.getIconURL();
		final Image iconImage = loadImage(iconURL);
		final boolean enabled = info.isEnabled();

		final Button button = new Button() {

			@Override
			public void paint(final Graphics g) {
				super.paint(g);
				if (iconImage == null) return;
				final int buttonWidth = getWidth();
				final int buttonHeight = getHeight();
				final int iconWidth = iconImage.getWidth(this);
				final int iconHeight = iconImage.getHeight(this);
				g.drawImage(iconImage, (buttonWidth - iconWidth) / 2,
					(buttonHeight - iconHeight) / 2, this);
			}
		};
		if (iconURL == null) {
			if (label != null && !label.isEmpty()) button.setLabel(label);
			else button.setLabel(name);
			Log.warn("Invalid icon for tool: " + tool);
		}

		// display description on mouseover
		button.addMouseListener(new MouseAdapter() {

			@Override
			public void mouseEntered(final MouseEvent evt) {
				eventService.publish(new StatusEvent(tool.getDescription()));
			}
			
			@Override
			public void mousePressed(final MouseEvent evt) {
				if (evt.getButton() == MouseEvent.NOBUTTON) return;
				if (evt.getButton() == MouseEvent.BUTTON1) return;
				tool.configure();
			}
		});

		// activate tool when button pressed
		button.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(final ActionEvent e) {
				getToolService().setActiveTool(tool);
			}
		});

		button.setEnabled(enabled);

		return button;
	}

	private Image loadImage(final URL iconURL) {
		return Toolkit.getDefaultToolkit().createImage(iconURL);
	}

}
