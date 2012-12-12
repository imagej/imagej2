/*
 * #%L
 * ImageJ software for multidimensional image processing and analysis.
 * %%
 * Copyright (C) 2009 - 2012 Board of Regents of the University of
 * Wisconsin-Madison, Broad Institute of MIT and Harvard, and Max Planck
 * Institute of Molecular Cell Biology and Genetics.
 * %%
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * 
 * The views and conclusions contained in the software and documentation are
 * those of the authors and should not be interpreted as representing official
 * policies, either expressed or implied, of any organization.
 * #L%
 */

package imagej.ui.swing;

import imagej.InstantiableException;
import imagej.event.EventHandler;
import imagej.event.EventSubscriber;
import imagej.plugin.PluginInfo;
import imagej.tool.Tool;
import imagej.tool.ToolService;
import imagej.tool.event.ToolActivatedEvent;
import imagej.tool.event.ToolDeactivatedEvent;
import imagej.ui.ToolBar;
import imagej.ui.UIService;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
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

	protected final UIService uiService;
	
	private final SwingIconService iconService;

	private final Map<String, AbstractButton> toolButtons;

	private final ButtonGroup buttonGroup = new ButtonGroup();

	@SuppressWarnings("unused")
	private final List<EventSubscriber<?>> subscribers;

	public SwingToolBar(final UIService uiService) {
		this.uiService = uiService;
		iconService = uiService.getContext().getService(SwingIconService.class);
		toolButtons = new HashMap<String, AbstractButton>();
		populateToolBar();
		subscribers = uiService.getEventService().subscribe(this);
	}

	// -- ToolBar methods --

	@Override
	public ToolService getToolService() {
		return uiService.getToolService();
	}

	// -- Helper methods --

	private void populateToolBar() {
		final ToolService toolService = getToolService();
		final Tool activeTool = toolService.getActiveTool();
		Tool lastTool = null;
		for (final Tool tool : toolService.getTools()) {
			try {
				final AbstractButton button = createButton(tool, tool == activeTool);
				toolButtons.put(tool.getInfo().getName(), button);
				iconService.registerButton(tool, button);
				// add a separator between tools where applicable
				if (getToolService().isSeparatorNeeded(tool, lastTool)) addSeparator();
				lastTool = tool;

				add(button);
			}
			catch (final InstantiableException e) {
				uiService.getLog().warn("Invalid tool: " + tool.getInfo(), e);
			}
		}
		
	}

	private AbstractButton createButton(final Tool tool, boolean active)
		throws InstantiableException
	{
		final PluginInfo<? extends Tool> info = tool.getInfo();
		final String name = info.getName();
		final String label = info.getLabel();
		final URL iconURL = info.getIconURL();
		final boolean enabled = info.isEnabled();
		final boolean visible = info.isVisible();

		final JToggleButton button = new JToggleButton();

		// set icon
		if (iconURL == null) {
			button.setText(name);
			uiService.getLog().warn("Invalid icon for tool: " + tool);
		}
		else {
			uiService.getLog().debug("Loading icon from " + iconURL.toString());
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
				uiService.getStatusService().showStatus(tool.getDescription());
			}

			@Override
			public void mousePressed(final MouseEvent evt) {
				if (evt.getButton() == MouseEvent.NOBUTTON) return;
				if (evt.getButton() == MouseEvent.BUTTON1) return;
				tool.configure();
			}
		});

		// activate tool when button pressed
		button.addChangeListener(new ChangeListener() {

			boolean isActive = false;

			@Override
			public void stateChanged(final ChangeEvent e) {
				final boolean selected = button.isSelected();
				button.setBorder(selected ? ACTIVE_BORDER : INACTIVE_BORDER);
				if (selected == isActive) return;
				isActive = selected;
				if (isActive) {
					getToolService().setActiveTool(tool);
				}
			}
		});

		button.setBorder(active ? ACTIVE_BORDER : INACTIVE_BORDER);
		if (active) button.setSelected(true);
		button.setEnabled(enabled);
		button.setVisible(visible);

		return button;
	}

	// -- Event handlers --

	@EventHandler
	protected void onEvent(final ToolActivatedEvent event) {
		final PluginInfo<? extends Tool> info = event.getTool().getInfo();
		if (info == null) return; // no info, no button
		final String name = info.getName();
		if (name == null) return; // no name, no button?
		final AbstractButton button = toolButtons.get(name);
		if (button == null) return; // not on toolbar
		button.setSelected(true);
		button.setBorder(ACTIVE_BORDER);
		uiService.getLog().debug("Selected " + name + " button.");
	}

	@EventHandler
	protected void onEvent(final ToolDeactivatedEvent event) {
		final PluginInfo<? extends Tool> info = event.getTool().getInfo();
		if (info == null) return; // no info, no button
		final String name = info.getName();
		if (name == null) return; // no name, no button?
		final AbstractButton button = toolButtons.get(name);
		if (button == null) return; // not on toolbar
		button.setBorder(INACTIVE_BORDER);
		uiService.getLog().debug("Deactivated " + name + " button.");
	}

}
