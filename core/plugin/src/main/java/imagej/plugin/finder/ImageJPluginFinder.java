//
// ImageJPluginFinder.java
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

package imagej.plugin.finder;

import imagej.plugin.BasePlugin;
import imagej.plugin.Menu;
import imagej.plugin.MenuEntry;
import imagej.plugin.Plugin;
import imagej.plugin.PluginEntry;
import imagej.util.Log;

import java.util.ArrayList;
import java.util.List;

import net.java.sezpoz.Index;
import net.java.sezpoz.IndexItem;

/**
 * Discovers ImageJ plugins.
 * <p>
 * To accomplish this, SezPoz scans the classpath for {@link Plugin}
 * annotations.
 * </p>
 * 
 * @author Curtis Rueden
 */
@PluginFinder
public class ImageJPluginFinder implements IPluginFinder {

	/** Class loader to use when querying SezPoz. */
	private static ClassLoader classLoader;

	/** Sets the class loader to use when querying SezPoz. */
	public static void setPluginClassLoader(final ClassLoader cl) {
		classLoader = cl;
	}

	// -- IPluginFinder methods --

	@Override
	public void findPlugins(final List<PluginEntry<?>> plugins) {
		final Index<Plugin, BasePlugin> pluginIndex;
		if (classLoader == null) {
			pluginIndex = Index.load(Plugin.class, BasePlugin.class);
		}
		else {
			pluginIndex = Index.load(Plugin.class, BasePlugin.class, classLoader);
		}

		final int oldSize = plugins.size();
		for (final IndexItem<Plugin, BasePlugin> item : pluginIndex) {
			final PluginEntry<?> entry = createEntry(item);
			plugins.add(entry);
		}
		final int newSize = plugins.size();

		Log.info("Found " + (newSize - oldSize) + " plugins.");
		if (Log.isDebug()) {
			for (int i = oldSize; i < newSize; i++) {
				Log.debug("- " + plugins.get(i));
			}
		}
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
		final String iconPath = plugin.iconPath();
		entry.setIconPath(iconPath);
		entry.setPriority(plugin.priority());

		entry.setToggleParameter(plugin.toggleParameter());
		entry.setToggleGroup(plugin.toggleGroup());

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

		// add default icon if none attached to leaf
		if (menuPath.size() > 0 && !iconPath.isEmpty()) {
			final MenuEntry menuEntry = menuPath.get(menuPath.size() - 1);
			final String menuIconPath = menuEntry.getIconPath();
			if (menuIconPath == null || menuIconPath.isEmpty()) {
				menuEntry.setIconPath(iconPath);
			}
		}

		entry.setMenuPath(menuPath);

		return entry;
	}

	private void
		parseMenuPath(final List<MenuEntry> menuPath, final Menu[] menu)
	{
		for (int i = 0; i < menu.length; i++) {
			final String name = menu[i].label();
			final double weight = menu[i].weight();
			final char mnemonic = menu[i].mnemonic();
			final String accel = menu[i].accelerator();
			final String iconPath = menu[i].iconPath();
			menuPath.add(new MenuEntry(name, weight, mnemonic, accel, iconPath));
		}
	}

	private void
		parseMenuPath(final List<MenuEntry> menuPath, final String path)
	{
		final String[] menuPathTokens = path.split(">");
		for (final String token : menuPathTokens) {
			menuPath.add(new MenuEntry(token.trim()));
		}
	}

}
