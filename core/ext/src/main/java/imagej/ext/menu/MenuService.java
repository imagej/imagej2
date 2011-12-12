//
// MenuService.java
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

package imagej.ext.menu;

import imagej.AbstractService;
import imagej.ImageJ;
import imagej.Service;
import imagej.event.EventHandler;
import imagej.event.EventService;
import imagej.ext.module.Module;
import imagej.ext.module.ModuleInfo;
import imagej.ext.module.event.ModulesAddedEvent;
import imagej.ext.module.event.ModulesRemovedEvent;
import imagej.ext.module.event.ModulesUpdatedEvent;
import imagej.ext.plugin.PluginService;
import imagej.ext.plugin.RunnablePlugin;

/**
 * Service for keeping track of the application's menu structure.
 * 
 * @author Curtis Rueden
 * @see ShadowMenu
 */
@Service
public class MenuService extends AbstractService {

	private final EventService eventService;
	private final PluginService pluginService;

	/** Menu tree structure. */
	private ShadowMenu rootMenu;

	// -- Constructors --

	public MenuService() {
		// NB: Required by SezPoz.
		super(null);
		throw new UnsupportedOperationException();
	}

	public MenuService(final ImageJ context, final EventService eventService,
		final PluginService pluginService)
	{
		super(context);
		this.eventService = eventService;
		this.pluginService = pluginService;
	}

	// -- MenuService methods --

	public EventService getEventService() {
		return eventService;
	}

	public PluginService getPluginService() {
		return pluginService;
	}

	/** Gets the root node of the menu structure. */
	public ShadowMenu getMenu() {
		return rootMenu;
	}

	/**
	 * Populates a UI-specific menu structure.
	 * 
	 * @param creator the {@link MenuCreator} to use to populate the menus.
	 * @param menu the destination menu structure to populate.
	 */
	public <T> T createMenus(final MenuCreator<T> creator, final T menu) {
		creator.createMenus(rootMenu, menu);
		return menu;
	}

	/** Selects or deselects the given module in the menu structure. */
	public void setSelected(final Module module, final boolean selected) {
		setSelected(module.getInfo(), selected);
	}

	/** Selects or deselects the given plugin in the menu structure. */
	public void setSelected(final RunnablePlugin plugin, final boolean selected)
	{
		setSelected(plugin.getClass(), selected);
	}

	/**
	 * Selects or deselects the plugin of the given class in the menu structure.
	 */
	public <R extends RunnablePlugin> void setSelected(
		final Class<R> pluginClass, final boolean selected)
	{
		setSelected(pluginService.getRunnablePlugin(pluginClass), selected);
	}

	/**
	 * Selects or deselects the plugin of the given class in the menu structure.
	 */
	public <R extends RunnablePlugin> void setSelected(
		final String pluginClassName, final boolean selected)
	{
		setSelected(pluginService.getRunnablePlugin(pluginClassName), selected);
	}

	/** Selects or deselects the given module in the menu structure. */
	public void setSelected(final ModuleInfo info, final boolean selected) {
		info.setSelected(selected);
		info.update(eventService);
	}

	// -- IService methods --

	@Override
	public void initialize() {
		rootMenu = new ShadowMenu(this);
		super.initialize();
	}

	// -- Event handlers --

	@EventHandler
	protected void onEvent(final ModulesAddedEvent event) {
		getMenu().addAll(event.getItems());
	}

	@EventHandler
	protected void onEvent(final ModulesRemovedEvent event) {
		getMenu().removeAll(event.getItems());
	}

	@EventHandler
	protected void onEvent(final ModulesUpdatedEvent event) {
		getMenu().updateAll(event.getItems());
	}

}
