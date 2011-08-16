//
// WindowMenuService.java
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
package imagej.display;

import imagej.AbstractService;
import imagej.ImageJ;
import imagej.Service;
import imagej.display.event.DisplayActivatedEvent;
import imagej.display.event.DisplayCreatedEvent;
import imagej.display.event.DisplayDeletedEvent;
import imagej.display.event.window.WinActivatedEvent;
import imagej.display.event.window.WinClosedEvent;
import imagej.display.event.window.WinOpenedEvent;
import imagej.event.EventService;
import imagej.event.EventSubscriber;
import imagej.ext.MenuEntry;
import imagej.ext.MenuPath;
import imagej.ext.menu.MenuService;
import imagej.ext.module.ModuleInfo;
import imagej.ext.module.ModuleService;
import imagej.ext.plugin.ImageJPlugin;
import imagej.ext.plugin.PluginModuleInfo;
import imagej.ext.plugin.PluginService;
import imagej.ext.plugin.RunnablePlugin;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Service for managing the Open Windows menu.
 * <p>
 * <ul>
 * <li>add(String path)</li>
 * <li>remove(String path)</li>
 * </ul>
 * 
 * @author Grant Harris
 */
@Service
public final class WindowMenuService extends AbstractService {

	//===========================================
	public static final int MAX_FILES_SHOWN = 10;
	/** Maximum title  length shown. */
	private static final int MAX_DISPLAY_LENGTH = 40;
	private static final String WINDOW_MENU_NAME = "Window";
	private MenuService menuService;
	private ModuleService moduleService;
	protected final EventService eventService;
	private List<String> openWindows;
	private Map<String, ModuleInfo> windowModules;
	/** Maintain list of subscribers, to avoid garbage collection. */
	private List<EventSubscriber<?>> subscribers;

	public WindowMenuService() {
		// NB: Required by SezPoz.
		super(null);
		throw new UnsupportedOperationException();
	}

	public WindowMenuService(final ImageJ context,
			final MenuService menuService, final ModuleService moduleService, final EventService eventService) {
		super(context);
		this.eventService = eventService;
		this.menuService = menuService;
		this.moduleService = moduleService;
	}

	// -- openWindowService methods --
	public MenuService getMenuService() {
		return menuService;
	}

	public ModuleService getModuleService() {
		return moduleService;
	}

	public EventService getEventService() {
		return eventService;
	}

	/** Adds a path to the list of window files. */
	public void add(final String displayName) {
		final boolean present = windowModules.containsKey(displayName);
		if (present) {
			remove(displayName);
			updateInfo(displayName);
		} else {
			windowModules.put(displayName, createInfo(displayName));
		}
		openWindows.add(displayName);
	}

	/** Removes a path from the list of window files. */
	public boolean remove(final String displayName) {
		final ModuleInfo info = windowModules.remove(displayName);
		if (info != null) {
			moduleService.removeModule(info);
		}
		return openWindows.remove(displayName);
	}

	/** Clears the list of window files. */
	public void clear() {
		openWindows.clear();
		moduleService.removeModules(windowModules.values());
		windowModules.clear();
	}

	/** Gets the list of window files. */
	public List<String> getopenWindows() {
		return Collections.unmodifiableList(openWindows);
	}

	// -- IService methods --
	@Override
	public void initialize() {
		openWindows = new ArrayList<String>();
		windowModules = new HashMap<String, ModuleInfo>();
		subscribeToEvents();
	}

	/** Creates a {@link ModuleInfo} to reopen data at the given path. */
	private ModuleInfo createInfo(final String displayName) {
		final PluginModuleInfo<ImageJPlugin> info =
				new PluginModuleInfo<ImageJPlugin>("imagej.display.SelectWindow",
				ImageJPlugin.class);

		// hard code path to open as a preset
		final HashMap<String, Object> presets = new HashMap<String, Object>();
		presets.put("displayToSelect", displayName);
		info.setPresets(presets);

		// set menu path
		final MenuPath menuPath = new MenuPath();
		menuPath.add(new MenuEntry(WINDOW_MENU_NAME));
		final MenuEntry leaf = new MenuEntry(shortPath(displayName));
		menuPath.add(leaf);
		info.setMenuPath(menuPath);

		// set menu position
		leaf.setWeight(0); // TODO - do this properly

		// use the same icon as File > Open
//		final PluginService pluginService = ImageJ.get(PluginService.class);
//		final PluginModuleInfo<RunnablePlugin> fileOpen =
//				pluginService.getRunnablePlugin("imagej.io.plugins.OpenImage");
//		final String iconPath = fileOpen.getIconPath();
//		info.setIconPath(iconPath);
//		leaf.setIconPath(iconPath);

		// register the module with the module service
		moduleService.addModule(info);

		return info;
	}

	private void updateInfo(final String path) {
		final ModuleInfo info = windowModules.get(path);

		// TODO - update module weights

		// notify interested parties
		info.update();
	}

	/** Shortens the given path to ensure it conforms to a maximum length. */
	private String shortPath(final String path) {
		// TODO - shorten path name as needed
		return path;
	}

	private void subscribeToEvents() {
		subscribers = new ArrayList<EventSubscriber<?>>();

		// Created
		final EventSubscriber<DisplayCreatedEvent> displayCreatedSubscriber =
				new EventSubscriber<DisplayCreatedEvent>() {

					@Override
					public void onEvent(final DisplayCreatedEvent event) {
						final Display display  = event.getObject();
						add(display.getName());
					}

				};
		subscribers.add(displayCreatedSubscriber);
		eventService.subscribe(DisplayCreatedEvent.class, displayCreatedSubscriber);
		
		// Activated
		final EventSubscriber<DisplayActivatedEvent> displayActivatedSubscriber =
				new EventSubscriber<DisplayActivatedEvent>() {

					@Override
					public void onEvent(final DisplayActivatedEvent event) {
						final Object obj = event.getDisplay();
						// @TODO - needs checkbox menu functionality
						//setActiveWindow(display);
					}

				};
		subscribers.add(displayActivatedSubscriber);
		eventService.subscribe(DisplayActivatedEvent.class, displayActivatedSubscriber);
		
		// Deleted
		final EventSubscriber<DisplayDeletedEvent> displayDeletedSubscriber =
				new EventSubscriber<DisplayDeletedEvent>() {

					@Override
					public void onEvent(final DisplayDeletedEvent event) {
						final Display  obj = event.getObject();
						remove(obj.getName());
					}

				};
		subscribers.add(displayDeletedSubscriber);
		eventService.subscribe(DisplayDeletedEvent.class, displayDeletedSubscriber);
	}
	

}