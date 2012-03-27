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

import imagej.event.EventService;
import imagej.ext.module.Module;
import imagej.ext.module.ModuleInfo;
import imagej.ext.plugin.PluginService;
import imagej.ext.plugin.RunnablePlugin;
import imagej.service.IService;

/**
 * Interface for service that tracks the application's menu structure.
 * 
 * @author Curtis Rueden
 */
public interface MenuService extends IService {

	EventService getEventService();

	PluginService getPluginService();

	/** Gets the root node of the application menu structure. */
	ShadowMenu getMenu();

	/**
	 * Gets the root node of a menu structure.
	 * 
	 * @param menuRoot the root of the desired menu structure (see
	 *          {@link ModuleInfo#getMenuRoot()}).
	 */
	ShadowMenu getMenu(final String menuRoot);

	/**
	 * Populates a UI-specific application menu structure.
	 * 
	 * @param creator the {@link MenuCreator} to use to populate the menus.
	 * @param menu the destination menu structure to populate.
	 */
	<T> T createMenus(final MenuCreator<T> creator, final T menu);

	/**
	 * Populates a UI-specific menu structure.
	 * 
	 * @param menuRoot the root of the menu structure to generate (see
	 *          {@link ModuleInfo#getMenuRoot()}).
	 * @param creator the {@link MenuCreator} to use to populate the menus.
	 * @param menu the destination menu structure to populate.
	 */
	<T> T createMenus(final String menuRoot, final MenuCreator<T> creator,
		final T menu);

	/** Selects or deselects the given module in the menu structure. */
	void setSelected(final Module module, final boolean selected);

	/** Selects or deselects the given plugin in the menu structure. */
	void setSelected(final RunnablePlugin plugin, final boolean selected);

	/**
	 * Selects or deselects the plugin of the given class in the menu structure.
	 */
	<R extends RunnablePlugin> void setSelected(final Class<R> pluginClass,
		final boolean selected);

	/**
	 * Selects or deselects the plugin of the given class in the menu structure.
	 */
	<R extends RunnablePlugin> void setSelected(final String pluginClassName,
		final boolean selected);

	/** Selects or deselects the given module in the menu structure. */
	void setSelected(final ModuleInfo info, final boolean selected);

}
