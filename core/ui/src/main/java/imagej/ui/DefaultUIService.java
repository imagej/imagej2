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
import imagej.event.StatusService;
import imagej.ext.InstantiableException;
import imagej.ext.display.Display;
import imagej.ext.menu.MenuService;
import imagej.ext.menu.event.MenuEvent;
import imagej.ext.plugin.PluginInfo;
import imagej.ext.plugin.PluginService;
import imagej.ext.tool.ToolService;
import imagej.log.LogService;
import imagej.options.OptionsService;
import imagej.platform.AppService;
import imagej.platform.PlatformService;
import imagej.platform.event.AppQuitEvent;
import imagej.service.AbstractService;
import imagej.service.Service;
import imagej.thread.ThreadService;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Default service for handling ImageJ user interfaces.
 * 
 * @author Curtis Rueden
 */
@Service
public final class DefaultUIService extends AbstractService implements
	UIService
{

	private final LogService log;
	private final EventService eventService;
	private final StatusService statusService;
	private final ThreadService threadService;
	private final PlatformService platformService;
	private final PluginService pluginService;
	private final MenuService menuService;
	private final ToolService toolService;
	private final OptionsService optionsService;
	private final AppService appService;

	/** The active user interface. */
	private UserInterface userInterface;

	/** Available user interfaces. */
	private List<UserInterface> availableUIs;

	// -- Constructors --

	public DefaultUIService() {
		// NB: Required by SezPoz.
		super(null);
		throw new UnsupportedOperationException();
	}

	public DefaultUIService(final ImageJ context, final LogService log,
		final EventService eventService, final StatusService statusService,
		final ThreadService threadService, final PlatformService platformService,
		final PluginService pluginService, final MenuService menuService,
		final ToolService toolService, final OptionsService optionsService,
		final AppService appService)
	{
		super(context);
		this.log = log;
		this.eventService = eventService;
		this.statusService = statusService;
		this.threadService = threadService;
		this.platformService = platformService;
		this.pluginService = pluginService;
		this.menuService = menuService;
		this.toolService = toolService;
		this.optionsService = optionsService;
		this.appService = appService;

		launchUI();

		subscribeToEvents(eventService);
	}

	// -- UIService methods --

	@Override
	public EventService getEventService() {
		return eventService;
	}

	@Override
	public StatusService getStatusService() {
		return statusService;
	}

	@Override
	public ThreadService getThreadService() {
		return threadService;
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
	public void createUI() {
		if (userInterface == null) return;
		userInterface.create();
	}

	@Override
	public void processArgs(final String[] args) {
		log.info("Received command line arguments:");
		for (final String arg : args) {
			log.info("\t" + arg);
		}
		if (userInterface == null) return;
		userInterface.processArgs(args);
	}

	@Override
	public UserInterface getUI() {
		return userInterface;
	}

	@Override
	public List<UserInterface> getAvailableUIs() {
		return availableUIs;
	}

	@Override
	public OutputWindow createOutputWindow(final String title) {
		if (userInterface == null) return null;
		return userInterface.newOutputWindow(title);
	}

	@Override
	public DialogPrompt.Result showDialog(final String message) {
		return showDialog(message, "ImageJ");
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
		if (userInterface == null) return null;
		final DialogPrompt dialogPrompt =
			userInterface.dialogPrompt(message, title, messageType, optionType);
		return dialogPrompt.prompt();
	}

	@Override
	public void showContextMenu(final String menuRoot, final Display<?> display,
		final int x, final int y)
	{
		if (userInterface == null) return;
		userInterface.showContextMenu(menuRoot, display, x, y);
	}

	// -- Event handlers --

	@EventHandler
	protected void onEvent(@SuppressWarnings("unused") final MenuEvent event) {
		/* NOTE BDZ removed 6-11-12. Menus get created once. We will need to update
		 * this event to tweak existing menu accordingly. 
		 * 
		// TODO - This rebuilds the entire menu structure whenever the
		// menus change at all. Better would be to listen to MenusAddedEvent,
		// MenusRemovedEvent and MenusUpdatedEvent separately and surgically
		// adjust the menus accordingly. But this would require updates to
		// the MenuCreator API to be more powerful.
		if (userInterface == null) return;
		userInterface.createMenus();
		
		*/
	}

	@Override
	@EventHandler
	public void onEvent(final AppQuitEvent event) {
		userInterface.saveLocation();
	}

	// -- Helper methods --

	/** Discovers and launches the user interface. */
	private void launchUI() {
		final List<UserInterface> uis = discoverUIs();
		availableUIs = Collections.unmodifiableList(uis);
		if (uis.size() > 0) {
			final UserInterface ui = uis.get(0);
			log.info("Launching user interface: " + ui.getClass().getName());
			ui.initialize(this);
			userInterface = ui;
		}
		else {
			log.warn("No user interfaces found.");
			userInterface = null;
		}
	}

	/** Discovers available user interfaces. */
	private List<UserInterface> discoverUIs() {
		final List<UserInterface> uis = new ArrayList<UserInterface>();
		for (final PluginInfo<? extends UserInterface> info : pluginService
			.getPluginsOfType(UserInterface.class))
		{
			try {
				final UserInterface ui = info.createInstance();
				log.info("Discovered user interface: " + ui.getClass().getName());
				uis.add(ui);
			}
			catch (final InstantiableException e) {
				log.warn("Invalid user interface: " + info.getClassName(), e);
			}
		}
		return uis;
	}

}
