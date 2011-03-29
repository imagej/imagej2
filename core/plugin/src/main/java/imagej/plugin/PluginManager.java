//
// PluginIndex.java
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

package imagej.plugin;

import imagej.manager.Manager;
import imagej.manager.ManagerComponent;
import imagej.manager.Managers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import net.java.sezpoz.Index;
import net.java.sezpoz.IndexItem;

/**
 * Manager component for keeping track of available plugins.
 *
 * @author Curtis Rueden
 */
@Manager(priority = Managers.HIGH_PRIORITY)
public class PluginManager implements ManagerComponent {

	/** Class loader to use when querying SezPoz. */
	private static ClassLoader classLoader;

	public static void setPluginClassLoader(final ClassLoader cl) {
		classLoader = cl;
	}

	/** SezPoz index of available {@link BasePlugin}s. */
	private Index<Plugin, BasePlugin> pluginIndex;

	/** Table of plugin lists, organized by plugin type. */
	private HashMap<Class<?>, ArrayList<PluginEntry<?>>> pluginLists =
		new HashMap<Class<?>, ArrayList<PluginEntry<?>>>();

	public void reloadPlugins() {
		if (classLoader == null) {
			pluginIndex = Index.load(Plugin.class, BasePlugin.class);
		}
		else {
			pluginIndex = Index.load(Plugin.class, BasePlugin.class, classLoader);
		}

		// classify plugins into types
		pluginLists.clear();
		for (final IndexItem<Plugin, BasePlugin> item : pluginIndex) {
			final PluginEntry<?> entry = createEntry(item);
			final Class<?> type = item.annotation().type();
			registerType(entry, type);
		}

		// sort plugin lists by priority
		for (final ArrayList<PluginEntry<?>> pluginList : pluginLists.values()) {
			Collections.sort(pluginList);
		}
	}

	/** Gets a copy of the list of plugins labeled with the given type. */
	public <T extends BasePlugin> ArrayList<PluginEntry<T>>
		getPlugins(final Class<T> type)
	{
		// TODO - find a way to avoid making a copy of the list here?
		final ArrayList<PluginEntry<T>> outputList =
			new ArrayList<PluginEntry<T>>();
		final ArrayList<PluginEntry<?>> cachedList = pluginLists.get(type);
		if (cachedList != null) {
			for (PluginEntry<?> entry : cachedList) {
				@SuppressWarnings("unchecked")
				final PluginEntry<T> typedEntry = (PluginEntry<T>) entry;
				outputList.add(typedEntry);
			}
		}
		return outputList;
	}

	// -- ManagerComponent methods --

	@Override
	public void initialize() {
		reloadPlugins();
	}

	// -- Helper methods --

	private <T extends BasePlugin> PluginEntry<T> createEntry(
		final IndexItem<Plugin, BasePlugin> item)
	{
		final String className = item.className();
		final Plugin plugin = item.annotation();

		@SuppressWarnings("unchecked")
		final Class<T> pluginType = (Class<T>) plugin.type();

		final PluginEntry<T> entry = new PluginEntry<T>(className, pluginType);
		entry.setName(plugin.name());
		entry.setLabel(plugin.label());
		entry.setDescription(plugin.description());
		entry.setIconPath(plugin.iconPath());
		entry.setPriority(plugin.priority());

		final List<MenuEntry> menuPath = new ArrayList<MenuEntry>();
		final Menu[] menu = plugin.menu();
		if (menu.length > 0) {
			parseMenuPath(menuPath, menu);
		}
		else {
			// parse menuPath attribute
			final String path = plugin.menuPath();
			if (!path.isEmpty()) parseMenuPath(menuPath, path);
		}
		entry.setMenuPath(menuPath);

		return entry;
	}

	private void registerType(PluginEntry<?> entry, Class<?> type) {
		ArrayList<PluginEntry<?>> pluginList = pluginLists.get(type);
		if (pluginList == null) {
			pluginList = new ArrayList<PluginEntry<?>>();
			pluginLists.put(type, pluginList);
		}
		pluginList.add(entry);
	}

	private void parseMenuPath(final List<MenuEntry> menuPath,
		final Menu[] menu)
	{
		for (int i = 0; i < menu.length; i++) {
			final String name = menu[i].label();
			final double weight = menu[i].weight();
			final char mnemonic = menu[i].mnemonic();
			final String accelerator = menu[i].accelerator();
			final String icon = menu[i].icon();				
			menuPath.add(new MenuEntry(name, weight, mnemonic, accelerator, icon));
		}
	}

	private void parseMenuPath(final List<MenuEntry> menuPath,
		final String path)
	{
		final String[] menuPathTokens = path.split(">");
		for (String token : menuPathTokens) menuPath.add(new MenuEntry(token));
	}

}
