//
// UIService.java
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

package imagej.ui;

import imagej.AbstractService;
import imagej.ImageJ;
import imagej.Service;
import imagej.event.EventService;
import imagej.event.EventSubscriber;
import imagej.ext.menu.MenuService;
import imagej.ext.menu.event.MenuEvent;
import imagej.ext.plugin.PluginService;
import imagej.platform.PlatformService;
import imagej.tool.ToolService;
import imagej.util.Log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.java.sezpoz.Index;
import net.java.sezpoz.IndexItem;

/**
 * Service for the ImageJ user interface.
 * 
 * @author Curtis Rueden
 */
@Service
public final class UIService extends AbstractService {

	private final EventService eventService;
	private final PlatformService platformService;
	private final PluginService pluginService;
	private final MenuService menuService;
	private final ToolService toolService;

	/** The active user interface. */
	private UserInterface userInterface;

	/** Available user interfaces. */
	private List<UserInterface> availableUIs;

	/** Maintain list of subscribers, to avoid garbage collection. */
	private List<EventSubscriber<?>> subscribers;

	// -- Constructors --

	public UIService() {
		// NB: Required by SezPoz.
		super(null);
		throw new UnsupportedOperationException();
	}

	public UIService(final ImageJ context, final EventService eventService,
		final PlatformService platformService, final PluginService pluginService,
		final MenuService menuService, final ToolService toolService)
	{
		super(context);
		this.eventService = eventService;
		this.platformService = platformService;
		this.pluginService = pluginService;
		this.menuService = menuService;
		this.toolService = toolService;
	}

	// -- UIService methods --

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
		for (final String arg : args)
			Log.info("\t" + arg);
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
		return userInterface.newOutputWindow(title);
	}

	// -- IService methods --

	@Override
	public void initialize() {
		launchUI();
		subscribeToEvents();
	}

	// -- Helper methods --

	/** Discovers and launches the user interface. */
	private void launchUI() {
		final List<UserInterface> uis = discoverUIs();
		availableUIs = Collections.unmodifiableList(uis);
		if (uis.size() > 0) {
			final UserInterface ui = uis.get(0);
			Log.info("Launching user interface: " + ui.getClass().getName());
			ui.initialize();
			userInterface = ui;
		}
		else {
			Log.warn("No user interfaces found.");
			userInterface = null;
		}
	}

	/** Discovers user interfaces using SezPoz. */
	private List<UserInterface> discoverUIs() {
		final List<UserInterface> uis = new ArrayList<UserInterface>();
		for (final IndexItem<UI, UserInterface> item : Index.load(UI.class,
			UserInterface.class))
		{
			try {
				final UserInterface ui = item.instance();
				Log.info("Discovered user interface: " + ui.getClass().getName());
				uis.add(ui);
			}
			catch (final InstantiationException e) {
				Log.warn("Invalid user interface: " + item, e);
			}
		}
		return uis;
	}

	private void subscribeToEvents() {
		subscribers = new ArrayList<EventSubscriber<?>>();

		final EventSubscriber<MenuEvent> menusChangedSubscriber =
			new EventSubscriber<MenuEvent>() {

				@Override
				public void onEvent(final MenuEvent event) {
					// TODO - This rebuilds the entire menu structure whenever the
					// menus change at all. Better would be to listen to MenusAddedEvent,
					// MenusRemovedEvent and MenusUpdatedEvent separately and surgically
					// adjust the menus accordingly. But this would require updates to
					// the MenuCreator API to be more powerful.
					getUI().createMenus();
				}
			};
		subscribers.add(menusChangedSubscriber);
		eventService.subscribe(MenuEvent.class, menusChangedSubscriber);
	}

}
