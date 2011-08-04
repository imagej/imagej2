//
// RecentFileService.java
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

package imagej.io;

import imagej.AbstractService;
import imagej.ImageJ;
import imagej.Service;
import imagej.event.EventSubscriber;
import imagej.event.Events;
import imagej.event.FileOpenedEvent;
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
 * Service for managing the Recently Used Files menu.
 * <p>
 * Behavior: There is a limited number of files presented (maxFilesShown),
 * regardless of the list length. When a file is opened, its path is added to
 * the top of the list. If an image has been saved as a new file, its path is
 * added to the top of the list upon
 * </p>
 * <ul>
 * <li>add(String path)</li>
 * <li>remove(String path)</li>
 * </ul>
 * 
 * @author Grant Harris
 * @author Curtis Rueden
 */
@Service
public final class RecentFileService extends AbstractService {

	private static final int MAX_FILES_SHOWN = 10;

	/** Maximum pathname length shown. */
	private static final int MAX_DISPLAY_LENGTH = 40;

	private static final String RECENT_MENU_NAME = "Open Recent";

	private MenuService menuService;
	private ModuleService moduleService;

	private List<String> recentFiles;
	private Map<String, ModuleInfo> recentModules;

	/** Maintain list of subscribers, to avoid garbage collection. */
	private List<EventSubscriber<?>> subscribers;

	public RecentFileService() {
		// NB: Required by SezPoz.
		super(null);
		throw new UnsupportedOperationException();
	}

	public RecentFileService(final ImageJ context,
		final MenuService menuService, final ModuleService moduleService)
	{
		super(context);
		this.menuService = menuService;
		this.moduleService = moduleService;
	}

	// -- RecentFileService methods --

	public MenuService getMenuService() {
		return menuService;
	}

	public ModuleService getModuleService() {
		return moduleService;
	}

	/** Adds a path to the list of recent files. */
	public void add(final String path) {
		final boolean present = recentModules.containsKey(path);
		if (present) {
			remove(path);
			updateInfo(path);
		}
		else {
			recentModules.put(path, createInfo(path));
		}
		recentFiles.add(path);
	}

	/** Removes a path from the list of recent files. */
	public boolean remove(final String path) {
		return recentFiles.remove(path);
	}

	/** Gets the list of recent files. */
	public List<String> getRecentFiles() {
		return Collections.unmodifiableList(recentFiles);
	}

	// -- IService methods --

	@Override
	public void initialize() {
		recentFiles = new ArrayList<String>();
		subscribeToEvents();
	}

	// -- Helper methods --

	private void subscribeToEvents() {
		subscribers = new ArrayList<EventSubscriber<?>>();

		final EventSubscriber<FileOpenedEvent> fileOpenedSubscriber =
			new EventSubscriber<FileOpenedEvent>() {

				@Override
				public void onEvent(final FileOpenedEvent event) {
					add(event.getPath());
				}

			};
		subscribers.add(fileOpenedSubscriber);
		Events.subscribe(FileOpenedEvent.class, fileOpenedSubscriber);

		// TODO
		// FileSavedEvent
		// ?? FileClosedEvent
		// DisplayCreatedEvent
		// DisplayDeletedEvent
	}

	/** Creates a {@link ModuleInfo} to reopen data at the given path. */
	private ModuleInfo createInfo(final String path) {
		final PluginModuleInfo<ImageJPlugin> info =
			new PluginModuleInfo<ImageJPlugin>("imagej.io.plugins.OpenImage",
				ImageJPlugin.class);

		final HashMap<String, Object> presets = new HashMap<String, Object>();
		presets.put("inputFile", path);
		info.setPresets(presets);

		// use the same icon as File > Open
		final PluginService pluginService = ImageJ.get(PluginService.class);
		final PluginModuleInfo<RunnablePlugin> fileOpen =
			pluginService.getRunnablePlugin("imagej.io.plugins.OpenImage");
		final String iconPath = fileOpen.getIconPath();

		final MenuPath menuPath = new MenuPath();
		menuPath.add(new MenuEntry("File"));
		menuPath.add(new MenuEntry(RECENT_MENU_NAME));
		final MenuEntry leaf = new MenuEntry(shortPath(path));
		leaf.setIconPath(iconPath);
		menuPath.add(leaf);
		info.setMenuPath(menuPath);

		// register the module with the module service
		moduleService.addModule(info);

		return info;
	}

	private void updateInfo(final String path) {
		final ModuleInfo info = recentModules.get(path);

		// TODO - update module weights

		// notify interested parties
		info.update();
	}

	/** Shortens the given path to ensure it conforms to a maximum length. */
	private String shortPath(final String path) {
		// TODO - shorten path name as needed
		return path;
	}

}
