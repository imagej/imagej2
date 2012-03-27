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

package imagej.data.display;

import imagej.ImageJ;
import imagej.event.EventHandler;
import imagej.event.EventService;
import imagej.ext.MenuEntry;
import imagej.ext.MenuPath;
import imagej.ext.display.Display;
import imagej.ext.display.event.DisplayActivatedEvent;
import imagej.ext.display.event.DisplayCreatedEvent;
import imagej.ext.display.event.DisplayDeletedEvent;
import imagej.ext.menu.MenuConstants;
import imagej.ext.menu.MenuService;
import imagej.ext.module.ModuleInfo;
import imagej.ext.module.ModuleService;
import imagej.ext.plugin.ImageJPlugin;
import imagej.ext.plugin.PluginModuleInfo;
import imagej.service.AbstractService;
import imagej.service.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Service for keeping track of open windows, including management of the
 * Windows menu.
 * 
 * @author Grant Harris
 */
@Service
public final class WindowMenuService extends AbstractService {

	public static final int MAX_FILES_SHOWN = 10;

	/** Maximum title length shown. */
	private static final int MAX_DISPLAY_LENGTH = 40;

	private final MenuService menuService;

	private final ModuleService moduleService;

	private final EventService eventService;

	private List<String> openWindows;

	private Map<String, ModuleInfo> windowModules;

	/*
	 * order in menu, 'weight'
	 */

	private int order = 1000;

	public WindowMenuService() {
		// NB: Required by SezPoz.
		super(null);
		throw new UnsupportedOperationException();
	}

	public WindowMenuService(final ImageJ context,
		final MenuService menuService, final ModuleService moduleService,
		final EventService eventService)
	{
		super(context);
		this.eventService = eventService;
		this.menuService = menuService;
		this.moduleService = moduleService;

		openWindows = new ArrayList<String>();
		windowModules = new HashMap<String, ModuleInfo>();

		subscribeToEvents(eventService);
	}

	// -- WindowService methods --

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
		}
		else {
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
	public List<String> getOpenWindows() {
		return Collections.unmodifiableList(openWindows);
	}

	// -- Event handlers --

	@EventHandler
	protected void onEvent(final DisplayCreatedEvent event) {
		final Display<?> display = event.getObject();
		add(display.getName());
	}

	@EventHandler
	protected void onEvent(final DisplayActivatedEvent event) {
		final Display<?> display = event.getDisplay();
		// @TODO - needs checkbox menu functionality
		// setActiveWindow(display);
	}

	@EventHandler
	protected void onEvent(final DisplayDeletedEvent event) {
		final Display<?> display = event.getObject();
		remove(display.getName());
	}

	// -- Helper methods --

	/** Creates a {@link ModuleInfo} to reopen data at the given path. */
	private ModuleInfo createInfo(final String displayName) {
		final PluginModuleInfo<ImageJPlugin> info =
			new PluginModuleInfo<ImageJPlugin>(SelectWindow.class.getName(),
				ImageJPlugin.class);

		// hard code path to open as a preset
		final HashMap<String, Object> presets = new HashMap<String, Object>();
		presets.put("displayToSelect", displayName);
		info.setPresets(presets);

		// set menu path
		final MenuPath menuPath = new MenuPath();
		menuPath.add(new MenuEntry(MenuConstants.WINDOW_LABEL));
		final MenuEntry leaf = new MenuEntry(shortPath(displayName));
		menuPath.add(leaf);
		info.setMenuPath(menuPath);

		// set menu position
		leaf.setWeight(order++); // TODO - do this properly

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
		info.update(eventService);
	}

	/** Shortens the given path to ensure it conforms to a maximum length. */
	private String shortPath(final String path) {
		// TODO - shorten path name as needed
		return path;
	}

}
