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

package imagej.io;

import imagej.MenuEntry;
import imagej.MenuPath;
import imagej.command.CommandInfo;
import imagej.command.CommandService;
import imagej.event.EventHandler;
import imagej.event.EventService;
import imagej.io.event.FileOpenedEvent;
import imagej.io.event.FileSavedEvent;
import imagej.io.plugins.OpenImage;
import imagej.menu.MenuConstants;
import imagej.module.ModuleInfo;
import imagej.module.ModuleService;
import imagej.plugin.Parameter;
import imagej.plugin.Plugin;
import imagej.service.AbstractService;
import imagej.service.Service;
import imagej.util.FileUtils;
import imagej.util.Prefs;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Default service for managing the Recently Used Files menu.
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
@Plugin(type = Service.class)
public final class DefaultRecentFileService extends AbstractService implements
	RecentFileService
{

	// -- Constants --

	/** Maximum pathname length shown. */
	private static final int MAX_DISPLAY_LENGTH = 40;

	private static final String RECENT_MENU_NAME = "Open Recent";

	private static final String RECENT_FILES_KEY = "recentfiles";

	// -- Fields --

	@Parameter
	private EventService eventService;

	@Parameter
	private ModuleService moduleService;

	@Parameter
	private CommandService commandService;

	private List<String> recentFiles;
	private Map<String, CommandInfo> recentModules;

	// -- RecentFileService methods --

	@Override
	public void add(final String path) {
		final boolean present = recentModules.containsKey(path);

		// add path to recent files list
		if (present) recentFiles.remove(path);
		recentFiles.add(path);

		// persist the updated list
		Prefs.putList(recentFiles, RECENT_FILES_KEY);

		if (present) {
			// path already present; update linked module info
			final CommandInfo info = recentModules.get(path);
			// TODO - update module weights
			info.update(eventService);
		}
		else {
			// new path; create linked module info
			final CommandInfo info = createInfo(path);
			recentModules.put(path, info);

			// register the module with the module service
			moduleService.addModule(info);
		}
	}

	@Override
	public boolean remove(final String path) {
		// remove path from recent files list
		final boolean success = recentFiles.remove(path);

		// persist the updated list
		Prefs.putList(recentFiles, RECENT_FILES_KEY);

		// remove linked module info
		final CommandInfo info = recentModules.remove(path);
		if (info != null) moduleService.removeModule(info);

		return success;
	}

	@Override
	public void clear() {
		recentFiles.clear();
		Prefs.clear(RECENT_FILES_KEY);

		// unregister the modules with the module service
		moduleService.removeModules(recentModules.values());

		recentModules.clear();
	}

	@Override
	public List<String> getRecentFiles() {
		return Collections.unmodifiableList(recentFiles);
	}

	// -- Service methods --

	@Override
	public void initialize() {
		recentFiles = Prefs.getList(RECENT_FILES_KEY);
		recentModules = new HashMap<String, CommandInfo>();
		for (final String path : recentFiles) {
			recentModules.put(path, createInfo(path));
		}

		// register the modules with the module service
		moduleService.addModules(recentModules.values());

		subscribeToEvents(eventService);
	}

	// -- Event handlers --

	@EventHandler
	protected void onEvent(final FileOpenedEvent event) {
		add(event.getPath());
	}

	@EventHandler
	protected void onEvent(final FileSavedEvent event) {
		add(event.getPath());
	}

	// -- Helper methods --

	/** Creates a {@link CommandInfo} to reopen data at the given path. */
	private CommandInfo createInfo(final String path) {
		final CommandInfo info = new CommandInfo("imagej.io.plugins.OpenImage");

		// hard code path to open as a preset
		final HashMap<String, Object> presets = new HashMap<String, Object>();
		presets.put("inputFile", path);
		info.setPresets(presets);

		// set menu path
		final MenuPath menuPath = new MenuPath();
		menuPath.add(new MenuEntry(MenuConstants.FILE_LABEL));
		menuPath.add(new MenuEntry(RECENT_MENU_NAME));
		final MenuEntry leaf = new MenuEntry(shortPath(path));
		menuPath.add(leaf);
		info.setMenuPath(menuPath);

		// set menu position
		leaf.setWeight(0); // TODO - do this properly

		// use the same icon as File > Open
		final CommandInfo fileOpen = commandService.getCommand(OpenImage.class);
		final String iconPath = fileOpen.getIconPath();
		info.setIconPath(iconPath);

		return info;
	}

	/** Shortens the given path to ensure it conforms to a maximum length. */
	private String shortPath(final String path) {
		// TODO - shorten path name as needed
		return FileUtils.limitPath(path, MAX_DISPLAY_LENGTH);
	}

}
