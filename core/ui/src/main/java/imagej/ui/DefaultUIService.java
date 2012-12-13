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

package imagej.ui;

import imagej.InstantiableException;
import imagej.command.CommandService;
import imagej.display.Display;
import imagej.display.DisplayService;
import imagej.display.event.DisplayActivatedEvent;
import imagej.display.event.DisplayCreatedEvent;
import imagej.display.event.DisplayDeletedEvent;
import imagej.display.event.DisplayUpdatedEvent;
import imagej.event.EventHandler;
import imagej.event.EventService;
import imagej.event.StatusService;
import imagej.log.LogService;
import imagej.menu.MenuService;
import imagej.options.OptionsService;
import imagej.platform.AppService;
import imagej.platform.PlatformService;
import imagej.platform.event.AppQuitEvent;
import imagej.plugin.Parameter;
import imagej.plugin.Plugin;
import imagej.plugin.PluginInfo;
import imagej.plugin.PluginService;
import imagej.service.AbstractService;
import imagej.service.Service;
import imagej.thread.ThreadService;
import imagej.tool.ToolService;
import imagej.ui.DialogPrompt.MessageType;
import imagej.ui.DialogPrompt.OptionType;
import imagej.ui.DialogPrompt.Result;
import imagej.ui.viewer.DisplayViewer;
import imagej.ui.viewer.DisplayWindow;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Default service for handling ImageJ user interfaces.
 * 
 * @author Curtis Rueden
 */
@Plugin(type = Service.class)
public final class DefaultUIService extends AbstractService implements
	UIService
{

	@Parameter
	private LogService log;

	@Parameter
	private EventService eventService;

	@Parameter
	private StatusService statusService;

	@Parameter
	private ThreadService threadService;

	@Parameter
	private PlatformService platformService;

	@Parameter
	private PluginService pluginService;

	@Parameter
	private CommandService commandService;

	@Parameter
	private DisplayService displayService;

	@Parameter
	private MenuService menuService;

	@Parameter
	private ToolService toolService;

	@Parameter
	private OptionsService optionsService;

	@Parameter
	private AppService appService;

	/**
	 * A list of extant display viewers. It's needed in order to find the viewer
	 * associated with a display.
	 */
	private List<DisplayViewer<?>> displayViewers;

	/** List of available user interfaces, ordered by priority. */
	private List<UserInterface> uiList;

	/** Map of available user interfaces, keyed off their names. */
	private Map<String, UserInterface> uiMap;

	/** The default user interface to use, if one is not explicitly specified. */
	private UserInterface defaultUI;

	private boolean activationInvocationPending = false;

	// -- UIService methods --

	@Override
	public LogService getLog() {
		return log;
	}

	@Override
	public ThreadService getThreadService() {
		return threadService;
	}

	@Override
	public EventService getEventService() {
		return eventService;
	}

	@Override
	public StatusService getStatusService() {
		return statusService;
	}

	@Override
	public PlatformService getPlatformService() {
		return platformService;
	}

	@Override
	public PluginService getPluginService() {
		return pluginService;
	}

	@Override
	public DisplayService getDisplayService() {
		return displayService;
	}

	@Override
	public CommandService getCommandService() {
		return commandService;
	}

	@Override
	public MenuService getMenuService() {
		return menuService;
	}

	@Override
	public ToolService getToolService() {
		return toolService;
	}

	@Override
	public OptionsService getOptionsService() {
		return optionsService;
	}

	@Override
	public AppService getAppService() {
		return appService;
	}

	@Override
	public void addUI(final UserInterface ui) {
		addUI(null, ui);
	}

	@Override
	public void addUI(final String name, final UserInterface ui) {
		// add to UI list
		uiList.add(ui);

		// add to UI map
		uiMap.put(ui.getClass().getName(), ui);
		if (name != null && !name.isEmpty()) uiMap.put(name, ui);
	}

	@Override
	public void createUI() {
		final UserInterface ui = getDefaultUI();
		if (ui == null) {
			throw new IllegalStateException("No UIs available.");
		}
		createUI(ui);
	}

	@Override
	public void createUI(final String name) {
		final UserInterface ui = uiMap.get(name);
		if (ui == null) {
			throw new IllegalArgumentException("No such user interface: " + name);
		}
		createUI(ui);
	}

	@Override
	public void createUI(final UserInterface ui) {
		log.info("Launching user interface: " + ui.getClass().getName());
		ui.show();
	}

	@Override
	public boolean isVisible() {
		final UserInterface ui = getDefaultUI();
		if (ui == null) {
			throw new IllegalStateException("No UIs available.");
		}
		return ui.isVisible();
	}

	@Override
	public boolean isVisible(final String name) {
		final UserInterface ui = uiMap.get(name);
		if (ui == null) {
			throw new IllegalArgumentException("No such user interface: " + name);
		}
		return ui.isVisible();
	}

	@Override
	public UserInterface getDefaultUI() {
		return defaultUI;
	}

	@Override
	public void setDefaultUI(final UserInterface ui) {
		defaultUI = ui;
	}

	@Override
	public UserInterface getUI(final String name) {
		return uiMap.get(name);
	}

	@Override
	public List<UserInterface> getAvailableUIs() {
		return Collections.unmodifiableList(uiList);
	}

	@Override
	public List<UserInterface> getVisibleUIs() {
		final ArrayList<UserInterface> uis = new ArrayList<UserInterface>();
		for (final UserInterface ui : uiList) {
			if (ui.isVisible()) uis.add(ui);
		}
		return uis;
	}

	@Override
	public DisplayViewer<?> getDisplayViewer(final Display<?> display) {
		for (final DisplayViewer<?> displayViewer : displayViewers) {
			if (displayViewer.getDisplay() == display) return displayViewer;
		}
		log.warn("No viewer found for display: '" + display.getName() + "'");
		return null;
	}

	@Override
	public OutputWindow createOutputWindow(final String title) {
		final UserInterface ui = getDefaultUI();
		if (ui == null) return null;
		return ui.newOutputWindow(title);
	}

	@Override
	public DialogPrompt.Result showDialog(final String message) {
		return showDialog(message, getContext().getTitle());
	}

	@Override
	public Result showDialog(String message, MessageType messageType) {
		return showDialog(message, getContext().getTitle(), messageType);
	}

	@Override
	public Result showDialog(String message, MessageType messageType,
		OptionType optionType)
	{
		return showDialog(message, getContext().getTitle(), messageType, optionType);
	}

	@Override
	public DialogPrompt.Result showDialog(final String message,
		final String title)
	{
		return showDialog(message, title,
			DialogPrompt.MessageType.INFORMATION_MESSAGE);
	}

	@Override
	public DialogPrompt.Result showDialog(final String message,
		final String title, final DialogPrompt.MessageType messageType)
	{
		return showDialog(message, title, messageType,
			DialogPrompt.OptionType.DEFAULT_OPTION);
	}

	@Override
	public DialogPrompt.Result showDialog(final String message,
		final String title, final DialogPrompt.MessageType messageType,
		final DialogPrompt.OptionType optionType)
	{
		final UserInterface ui = getDefaultUI();
		if (ui == null) return null;
		final DialogPrompt dialogPrompt =
			ui.dialogPrompt(message, title, messageType, optionType);
		return dialogPrompt.prompt();
	}

	@Override
	public File chooseFile(final File file, final String style) {
		final UserInterface ui = getDefaultUI();
		if (ui == null) return null;
		return ui.chooseFile(file, style);
	}

	@Override
	public void showContextMenu(final String menuRoot, final Display<?> display,
		final int x, final int y)
	{
		final UserInterface ui = getDefaultUI();
		if (ui == null) return;
		ui.showContextMenu(menuRoot, display, x, y);
	}

	// -- Service methods --

	@Override
	public void initialize() {
		displayViewers = new ArrayList<DisplayViewer<?>>();
		uiList = new ArrayList<UserInterface>();
		uiMap = new HashMap<String, UserInterface>();

		discoverUIs();

		subscribeToEvents(eventService);
	}

	// -- Event handlers --

	/**
	 * Called when a display is created. This is the magical place where the
	 * display model is connected with the real UI.
	 */
	@EventHandler
	protected void onEvent(final DisplayCreatedEvent e) {
		final Display<?> display = e.getObject();

		@SuppressWarnings({ "rawtypes", "unchecked" })
		final List<PluginInfo<DisplayViewer<?>>> viewers = (List)
			pluginService.getPluginsOfType(DisplayViewer.class);

		for (final UserInterface ui : getVisibleUIs()) {
			DisplayViewer<?> displayViewer = null;
			for (final PluginInfo<DisplayViewer<?>> info : viewers) {
				// check that viewer can actually handle the given display
				try {
					final DisplayViewer<?> viewer = info.createInstance();
					viewer.setContext(getContext());
					viewer.setPriority(info.getPriority());
					if (!viewer.canView(display)) continue;
					if (!viewer.isCompatible(ui)) continue;
					displayViewer = viewer;
					break; // found a suitable viewer; move on to the next UI
				}
				catch (final InstantiableException exc) {
					log.warn("Failed to create instance of " + info, exc);
				}
			}
			if (displayViewer == null) {
				log.warn("For UI '" + ui.getClass().getName() +
					"': no suitable viewer for display: " + display);
			}
			else {
				final DisplayWindow displayWindow =
						getDefaultUI().createDisplayWindow(display);
				displayViewer.view(displayWindow, display);
				displayWindow.setTitle(display.getName());
				displayViewers.add(displayViewer);
				displayWindow.showDisplay(true);
			}
		}
	}

	/**
	 * Called when a display is deleted. The display viewer is not removed
	 * from the list of viewers until after this returns.
	 */
	@EventHandler
	protected void onEvent(final DisplayDeletedEvent e) {
		final Display<?> display = e.getObject();
		final DisplayViewer<?> displayViewer = getDisplayViewer(display);
		if (displayViewer != null) {
			displayViewer.onDisplayDeletedEvent(e);
			displayViewers.remove(displayViewer);
		}
	}

	/** Called when a display is updated. */
	@EventHandler
	protected void onEvent(final DisplayUpdatedEvent e) {
		final Display<?> display = e.getDisplay();
		final DisplayViewer<?> displayViewer = getDisplayViewer(display);
		if (displayViewer != null) {
			displayViewer.onDisplayUpdatedEvent(e);
		}
	}

	/**
	 * Called when a display is activated.
	 * <p>
	 * The goal here is to eventually synchronize the window activation state with
	 * the display activation state if the display activation state changed
	 * programmatically. We queue a call on the UI thread to activate the display
	 * viewer of the currently active window.
	 * </p>
	 */
	@EventHandler
	protected void onEvent(final DisplayActivatedEvent e) {
		// CTR FIXME: Verify whether this threading logic is really necessary.
		if (activationInvocationPending) return;
		activationInvocationPending = true;
		threadService.queue(new Runnable() {

			@Override
			public void run() {
				final Display<?> activeDisplay = displayService.getActiveDisplay();
				if (activeDisplay != null) {
					final DisplayViewer<?> displayViewer =
						getDisplayViewer(activeDisplay);
					if (displayViewer != null) displayViewer.onDisplayActivatedEvent(e);
				}
				activationInvocationPending = false;
			}
		});
	}

	@EventHandler
	protected void onEvent(@SuppressWarnings("unused") final AppQuitEvent event) {
		for (final UserInterface ui : getVisibleUIs()) {
			ui.saveLocation();
		}
	}

	// -- Helper methods --

	/** Discovers available user interfaces. */
	private void discoverUIs() {
		final List<PluginInfo<UserInterface>> infos =
			pluginService.getPluginsOfType(UserInterface.class);
		for (final PluginInfo<UserInterface> info : infos) {
			try {
				// instantiate user interface
				final UserInterface ui = info.createInstance();
				ui.setContext(getContext());
				ui.setPriority(info.getPriority());
				log.info("Discovered user interface: " + ui.getClass().getName());

				addUI(info.getName(), ui);
			}
			catch (final InstantiableException e) {
				log.warn("Invalid user interface: " + info, e);
			}
		}

		// check system property for explicit UI preference
		final String uiProp = System.getProperty(UI_PROPERTY);
		final UserInterface ui = uiMap.get(uiProp);

		if (ui != null) {
			// set the default UI to the one provided by the system property
			setDefaultUI(ui);
		}
		else if (uiList.size() > 0) {
			// set the default UI to the one with the highest priority
			setDefaultUI(uiList.get(0));
		}
	}

	@Override
	public boolean isDefaultUI(final String name) {
		return getDefaultUI() == getUI(name);
	}

}
