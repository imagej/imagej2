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

package imagej.ext.menu;

import imagej.ImageJ;
import imagej.event.EventHandler;
import imagej.event.EventService;
import imagej.ext.module.Module;
import imagej.ext.module.ModuleInfo;
import imagej.ext.module.event.ModulesAddedEvent;
import imagej.ext.module.event.ModulesRemovedEvent;
import imagej.ext.module.event.ModulesUpdatedEvent;
import imagej.ext.plugin.Plugin;
import imagej.ext.plugin.PluginService;
import imagej.ext.plugin.RunnablePlugin;
import imagej.service.AbstractService;
import imagej.service.Service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

/**
 * Default service for keeping track of the application's menu structure.
 * 
 * @author Curtis Rueden
 * @see ShadowMenu
 */
@Service
public class DefaultMenuService extends AbstractService implements MenuService {

	private final EventService eventService;
	private final PluginService pluginService;

	/** Menu tree structures. There is one structure per menu root. */
	private HashMap<String, ShadowMenu> rootMenus;

	// -- Constructors --

	public DefaultMenuService() {
		// NB: Required by SezPoz.
		super(null);
		throw new UnsupportedOperationException();
	}

	public DefaultMenuService(final ImageJ context,
		final EventService eventService, final PluginService pluginService)
	{
		super(context);
		this.eventService = eventService;
		this.pluginService = pluginService;

		rootMenus = new HashMap<String, ShadowMenu>();

		final List<ModuleInfo> allModules =
			getPluginService().getModuleService().getModules();
		addModules(allModules);

		subscribeToEvents(eventService);
	}

	// -- MenuService methods --

	@Override
	public EventService getEventService() {
		return eventService;
	}

	@Override
	public PluginService getPluginService() {
		return pluginService;
	}

	@Override
	public ShadowMenu getMenu() {
		return getMenu(Plugin.APPLICATION_MENU_ROOT);
	}

	@Override
	public ShadowMenu getMenu(final String menuRoot) {
		return rootMenus.get(menuRoot);
	}

	@Override
	public <T> T createMenus(final MenuCreator<T> creator,
		final T menu)
	{
		return createMenus(Plugin.APPLICATION_MENU_ROOT, creator, menu);
	}

	@Override
	public <T> T createMenus(final String menuRoot, final MenuCreator<T> creator,
		final T menu)
	{
		creator.createMenus(getMenu(menuRoot), menu);
		return menu;
	}

	@Override
	public void setSelected(final Module module, final boolean selected) {
		setSelected(module.getInfo(), selected);
	}

	@Override
	public void setSelected(final RunnablePlugin plugin, final boolean selected) {
		setSelected(plugin.getClass(), selected);
	}

	@Override
	public <R extends RunnablePlugin> void setSelected(
		final Class<R> pluginClass, final boolean selected)
	{
		setSelected(pluginService.getRunnablePlugin(pluginClass), selected);
	}

	@Override
	public <R extends RunnablePlugin> void setSelected(
		final String pluginClassName, final boolean selected)
	{
		setSelected(pluginService.getRunnablePlugin(pluginClassName), selected);
	}

	@Override
	public void setSelected(final ModuleInfo info, final boolean selected) {
		info.setSelected(selected);
		info.update(eventService);
	}

	// -- Event handlers --

	@EventHandler
	protected void onEvent(final ModulesAddedEvent event) {
		addModules(event.getItems());
	}

	@EventHandler
	protected void onEvent(final ModulesRemovedEvent event) {
		for (final ShadowMenu menu : rootMenus.values()) {
			menu.removeAll(event.getItems());
		}
	}

	@EventHandler
	protected void onEvent(final ModulesUpdatedEvent event) {
		for (final ShadowMenu menu : rootMenus.values()) {
			menu.updateAll(event.getItems());
		}
	}

	// -- Helper methods --

	private void addModules(final Collection<ModuleInfo> items) {
		// categorize modules by menu root
		final HashMap<String, ArrayList<ModuleInfo>> modulesByMenuRoot =
			new HashMap<String, ArrayList<ModuleInfo>>();
		for (final ModuleInfo info : items) {
			final String menuRoot = info.getMenuRoot();
			ArrayList<ModuleInfo> modules = modulesByMenuRoot.get(menuRoot);
			if (modules == null) {
				modules = new ArrayList<ModuleInfo>();
				modulesByMenuRoot.put(menuRoot, modules);
			}
			modules.add(info);
		}

		// process each menu root separately
		for (final String menuRoot : modulesByMenuRoot.keySet()) {
			final ArrayList<ModuleInfo> modules = modulesByMenuRoot.get(menuRoot);
			ShadowMenu menu = rootMenus.get(menuRoot);
			if (menu == null) {
				// new menu root: create new menu structure
				menu = new ShadowMenu(this, modules);
				rootMenus.put(menuRoot, menu);
			}
			else {
				// existing menu root: add to menu structure
				menu.addAll(modules);
			}
		}

	}

}
