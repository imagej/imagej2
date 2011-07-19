//
// SwingToolBar.java
//

/*
ImageJ software for multidimensional image processing and analysis.

Copyright (c) 2010, ImageJDev.org.
All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met:
    * Redistributions of source code must retain the above copyright
      notice, this list of conditions and the following disclaimer.
    * Redistributions in binary form must reproduce the above copyright
      notice, this list of conditions and the following disclaimer in the
      documentation and/or other materials provided with the distribution.
    * Neither the names of the ImageJDev.org developers nor the
      names of its contributors may be used to endorse or promote products
      derived from this software without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
POSSIBILITY OF SUCH DAMAGE.
*/

package imagej.ui.swing;

import imagej.ImageJ;
import imagej.event.EventSubscriber;
import imagej.event.Events;
import imagej.event.StatusEvent;
import imagej.ext.InstantiableException;
import imagej.tool.ITool;
import imagej.tool.ToolInfo;
import imagej.tool.ToolService;
import imagej.tool.event.ToolActivatedEvent;
import imagej.tool.event.ToolDeactivatedEvent;
import imagej.ui.ToolBar;
import imagej.util.Log;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import javax.swing.AbstractButton;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.border.BevelBorder;
import javax.swing.border.Border;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 * Button bar with selectable tools, similar to ImageJ 1.x.
 * 
 * @author Curtis Rueden
 */
public class SwingToolBar extends JToolBar implements ToolBar {

	protected static final Border ACTIVE_BORDER = new BevelBorder(
		BevelBorder.LOWERED);

	protected static final Border INACTIVE_BORDER = new BevelBorder(
		BevelBorder.RAISED);

	private final ToolService toolService;

	private final Map<String, AbstractButton> toolButtons;

	private final ButtonGroup buttonGroup = new ButtonGroup();

	final private EventSubscriber<ToolActivatedEvent> toolActivatedEventSubscriber =
		new EventSubscriber<ToolActivatedEvent>() {

			@Override
			public void onEvent(final ToolActivatedEvent event) {
				onToolActivatedEvent(event);
			}
		};

	final private EventSubscriber<ToolDeactivatedEvent> toolDeactivatedEventSubscriber =
		new EventSubscriber<ToolDeactivatedEvent>() {

			@Override
			public void onEvent(final ToolDeactivatedEvent event) {
				onToolDeactivatedEvent(event);

			}
		};

	public SwingToolBar() {
		toolService = ImageJ.get(ToolService.class);
		toolButtons = new HashMap<String, AbstractButton>();
		populateToolBar();
		Events.subscribe(ToolActivatedEvent.class, toolActivatedEventSubscriber);
		Events.subscribe(ToolDeactivatedEvent.class,
			toolDeactivatedEventSubscriber);
	}

	// -- ToolBar methods --

	@Override
	public ToolService getToolService() {
		return toolService;
	}

	// -- Helper methods --

	private void populateToolBar() {
		int lastPriority = Integer.MAX_VALUE;
		for (final ToolInfo entry : toolService.getToolEntries()) {
			try {
				final AbstractButton button = createButton(entry);
				toolButtons.put(entry.getName(), button);

				// add a separator between tools with clustered priorities
				final int priority = entry.getPriority();
				if (priority - lastPriority > 10) addSeparator();
				lastPriority = priority;

				add(button);
			}
			catch (final InstantiableException e) {
				Log.warn("Invalid tool: " + entry, e);
			}
		}
	}

	private AbstractButton createButton(final ToolInfo entry)
		throws InstantiableException
	{
		final ITool tool = entry.createInstance();
		// TODO - consider alternatives to assigning the entry manually
		tool.setInfo(entry);
		final String name = entry.getName();
		final String label = entry.getLabel();
		final String description = entry.getDescription();
		final URL iconURL = entry.getIconURL();
		final boolean enabled = entry.isEnabled();

		final JToggleButton button = new JToggleButton();

		// set icon
		if (iconURL == null) {
			button.setText(name);
			Log.warn("Invalid icon for tool: " + tool);
		}
		else {
			Log.debug("Loading icon from " + iconURL.toString());
			button.setIcon(new ImageIcon(iconURL, label));
		}

		// set tool tip
		if (label != null && !label.isEmpty()) {
			button.setToolTipText(label);
		}
		else button.setToolTipText(name);
		buttonGroup.add(button);

		// display description on mouseover
		button.addMouseListener(new MouseAdapter() {

			@Override
			public void mouseEntered(final MouseEvent evt) {
				Events.publish(new StatusEvent(description));
			}
		});

		// activate tool when button pressed
		button.addChangeListener(new ChangeListener() {

			boolean active = false;

			@Override
			public void stateChanged(final ChangeEvent e) {
				final boolean selected = button.isSelected();
				button.setBorder(selected ? ACTIVE_BORDER : INACTIVE_BORDER);
				if (selected == active) return;
				active = selected;
				if (active) {
					getToolService().setActiveTool(tool);
				}
			}
		});

		button.setBorder(INACTIVE_BORDER);
		button.setEnabled(enabled);

		return button;
	}

	public void onToolActivatedEvent(final ToolActivatedEvent event) {
		final ToolInfo info = event.getTool().getInfo();
		if (info == null) return; // no info, no button
		final String name = info.getName();
		if (name == null) return; // no name, no button?
		final AbstractButton button = toolButtons.get(name);
		if (button == null) return; // not on toolbar
		button.setSelected(true);
		button.setBorder(ACTIVE_BORDER);
		Log.debug("Selected " + name + " button.");
	}

	public void onToolDeactivatedEvent(final ToolDeactivatedEvent event) {
		final ToolInfo info = event.getTool().getInfo();
		if (info == null) return; // no info, no button
		final String name = info.getName();
		if (name == null) return; // no name, no button?
		final AbstractButton button = toolButtons.get(name);
		if (button == null) return; // not on toolbar
		button.setBorder(INACTIVE_BORDER);
		Log.debug("Deactivated " + name + " button.");
	}

}
