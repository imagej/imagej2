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

import imagej.ImageJ;
import imagej.event.EventHandler;
import imagej.event.EventService;
import imagej.ext.InstantiableException;
import imagej.ext.display.Display;
import imagej.ext.menu.MenuService;
import imagej.ext.menu.event.MenuEvent;
import imagej.ext.plugin.PluginInfo;
import imagej.ext.plugin.PluginService;
import imagej.ext.tool.ToolService;
import imagej.platform.PlatformService;
import imagej.service.AbstractService;
import imagej.service.Service;
import imagej.thread.ThreadService;
import imagej.util.Log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Service for the ImageJ user interface.
 * 
 * @author Curtis Rueden
 */
@Service
public final class UIService extends AbstractService {

	private final EventService eventService;
	private final ThreadService threadService;
	private final PlatformService platformService;
	private final PluginService pluginService;
	private final MenuService menuService;
	private final ToolService toolService;

	/** The active user interface. */
	private UserInterface userInterface;

	/** Available user interfaces. */
	private List<UserInterface> availableUIs;

	// -- Constructors --

	public UIService() {
		// NB: Required by SezPoz.
		super(null);
		throw new UnsupportedOperationException();
	}

	public UIService(final ImageJ context, final EventService eventService,
		final ThreadService threadService, final PlatformService platformService,
		final PluginService pluginService, final MenuService menuService,
		final ToolService toolService)
	{
		super(context);
		this.eventService = eventService;
		this.threadService = threadService;
		this.platformService = platformService;
		this.pluginService = pluginService;
		this.menuService = menuService;
		this.toolService = toolService;

		launchUI();

		subscribeToEvents(eventService);
	}

	// -- UIService methods --

	public EventService getEventService() {
		return eventService;
	}

	public ThreadService getThreadService() {
		return threadService;
	}

	public PlatformService getPlatformService() {
		return platformService;
	}

	public PluginService getPluginService() {
		return pluginService;
	}

	public MenuService getMenuService() {
		return menuService;
	}

	public ToolService getToolService() {
		return toolService;
	}

	/** Processes the given command line arguments. */
	public void processArgs(final String[] args) {
		Log.info("Received command line arguments:");
		for (final String arg : args) {
			Log.info("\t" + arg);
		}
		if (userInterface == null) return;
		userInterface.processArgs(args);
	}

	/** Gets the active user interface. */
	public UserInterface getUI() {
		return userInterface;
	}

	/** Gets the user interfaces available on the classpath. */
	public List<UserInterface> getAvailableUIs() {
		return availableUIs;
	}

	/** Creates a new output window. */
	public OutputWindow createOutputWindow(final String title) {
		if (userInterface == null) return null;
		return userInterface.newOutputWindow(title);
	}

	/**
	 * Displays a dialog prompt.
	 * 
	 * @param message The message in the dialog itself.
	 * @return The choice selected by the user when dismissing the dialog.
	 */
	public DialogPrompt.Result showDialog(final String message) {
		return showDialog(message, "ImageJ");
	}

	/**
	 * Displays a dialog prompt.
	 * 
	 * @param message The message in the dialog itself.
	 * @param title The title of the dialog.
	 * @return The choice selected by the user when dismissing the dialog.
	 */
	public DialogPrompt.Result
		showDialog(final String message, final String title)
	{
		return showDialog(message, title,
			DialogPrompt.MessageType.INFORMATION_MESSAGE);
	}

	/**
	 * Displays a dialog prompt.
	 * 
	 * @param message The message in the dialog itself.
	 * @param title The title of the dialog.
	 * @param messageType The type of message. This typically is rendered as an
	 *          icon next to the message. For example,
	 *          {@link DialogPrompt.MessageType#WARNING_MESSAGE} typically appears
	 *          as an exclamation point.
	 * @return The choice selected by the user when dismissing the dialog.
	 */
	public DialogPrompt.Result showDialog(final String message,
		final String title, final DialogPrompt.MessageType messageType)
	{
		return showDialog(message, title, messageType,
			DialogPrompt.OptionType.DEFAULT_OPTION);
	}

	/**
	 * Displays a dialog prompt.
	 * 
	 * @param message The message in the dialog itself.
	 * @param title The title of the dialog.
	 * @param messageType The type of message. This typically is rendered as an
	 *          icon next to the message. For example,
	 *          {@link DialogPrompt.MessageType#WARNING_MESSAGE} typically appears
	 *          as an exclamation point.
	 * @param optionType The choices available when dismissing the dialog. These
	 *          choices are typically rendered as buttons for the user to click.
	 * @return The choice selected by the user when dismissing the dialog.
	 */
	public DialogPrompt.Result showDialog(final String message,
		final String title, final DialogPrompt.MessageType messageType,
		final DialogPrompt.OptionType optionType)
	{
		if (userInterface == null) return null;
		final DialogPrompt dialogPrompt =
			userInterface.dialogPrompt(message, title, messageType, optionType);
		return dialogPrompt.prompt();
	}

	/**
	 * Displays a popup context menu for the given display at the specified
	 * position.
	 */
	public void showContextMenu(final String menuRoot, final Display<?> display,
		final int x, final int y)
	{
		if (userInterface == null) return;
		userInterface.showContextMenu(menuRoot, display, x, y);
	}

	// -- Event handlers --

	@EventHandler
	protected void onEvent(@SuppressWarnings("unused")
	final MenuEvent event)
	{
		// TODO - This rebuilds the entire menu structure whenever the
		// menus change at all. Better would be to listen to MenusAddedEvent,
		// MenusRemovedEvent and MenusUpdatedEvent separately and surgically
		// adjust the menus accordingly. But this would require updates to
		// the MenuCreator API to be more powerful.
		if (userInterface == null) return;
		userInterface.createMenus();
	}

	// -- Helper methods --

	/** Discovers and launches the user interface. */
	private void launchUI() {
		final List<UserInterface> uis = discoverUIs();
		availableUIs = Collections.unmodifiableList(uis);
		if (uis.size() > 0) {
			final UserInterface ui = uis.get(0);
			Log.info("Launching user interface: " + ui.getClass().getName());
			ui.initialize(this);
			userInterface = ui;
		}
		else {
			Log.warn("No user interfaces found.");
			userInterface = null;
		}
	}

	/** Discovers available user interfaces. */
	private List<UserInterface> discoverUIs() {
		final List<UserInterface> uis = new ArrayList<UserInterface>();
		for (final PluginInfo<UserInterface> info :
			pluginService.getPluginsOfType(UserInterface.class))
		{
			try {
				final UserInterface ui = info.createInstance();
				Log.info("Discovered user interface: " + ui.getClass().getName());
				uis.add(ui);
			}
			catch (final InstantiableException e) {
				Log.warn("Invalid user interface: " + info.getClassName(), e);
			}
		}
		return uis;
	}

}
