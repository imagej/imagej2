//
// LegacyPluginFinder.java
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

package imagej.legacy.plugin;

import ij.ImageJ;
import ij.Menus;
import imagej.Log;
import imagej.legacy.LegacyManager;
import imagej.plugin.ImageJPlugin;
import imagej.plugin.api.MenuEntry;
import imagej.plugin.api.PluginEntry;
import imagej.plugin.finder.IPluginFinder;
import imagej.plugin.finder.PluginFinder;
import imagej.util.ListUtils;

import java.awt.Menu;
import java.awt.MenuBar;
import java.awt.MenuItem;
import java.awt.MenuShortcut;
import java.awt.Toolkit;
import java.awt.event.InputEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.KeyStroke;

/**
 * Discovers legacy ImageJ 1.x plugins.
 * 
 * @author Curtis Rueden
 */
@PluginFinder
public class LegacyPluginFinder implements IPluginFinder {

	private static final String LEGACY_PLUGIN_CLASS =
		LegacyPlugin.class.getName();

	/** A list of plugins to exclude from legacy plugin discovery. */
	private final Set<String> blacklist;

	public LegacyPluginFinder() {
		blacklist = new HashSet<String>();
		blacklist.add("ij.plugin.Commands(quit)");
	}

	@Override
	public void findPlugins(List<PluginEntry<?>> plugins) {
		final long startTime = System.currentTimeMillis();
		final ImageJ ij = LegacyManager.initialize();
		assert ij != null;
		final Map<String, List<MenuEntry>> menuTable = parseMenus(ij);
		final Hashtable<?, ?> commands = Menus.getCommands();
		final long endTime = System.currentTimeMillis();
		final long time = endTime - startTime;
		Log.debug("Found " + commands.size() +
			" legacy plugins in " + time + " ms:");
		for (final Object key : commands.keySet()) {
			final PluginEntry<ImageJPlugin> pe =
				createEntry(key, commands, menuTable);
			if (pe != null) plugins.add(pe);
		}
	}

	private PluginEntry<ImageJPlugin> createEntry(final Object key,
		final Hashtable<?, ?> commands,
		final Map<String, List<MenuEntry>> menuTable)
	{
		final String ij1PluginString = commands.get(key).toString();
		if (blacklist.contains(ij1PluginString)) return null;

		final String className = parsePluginClass(ij1PluginString);
		final String arg = parseArg(ij1PluginString);
		final List<MenuEntry> menuPath = menuTable.get(key);
		final Map<String, Object> presets = new HashMap<String, Object>();
		presets.put("className", className);
		presets.put("arg", arg);
		final PluginEntry<ImageJPlugin> pe =
			new PluginEntry<ImageJPlugin>(LEGACY_PLUGIN_CLASS, ImageJPlugin.class);
		pe.setMenuPath(menuPath);
		pe.setPresets(presets);
		Log.debug("- " + className + "(" + arg + ")");
		return pe;
	}

	/** Creates a table mapping IJ1 command labels to menu paths. */
	private Map<String, List<MenuEntry>> parseMenus(ImageJ ij) {
		final Map<String, List<MenuEntry>> menuTable =
			new HashMap<String, List<MenuEntry>>();
		final MenuBar menubar = ij.getMenuBar();
		final int menuCount = menubar.getMenuCount();
		for (int i = 0; i < menuCount; i++) {
			final Menu menu = menubar.getMenu(i);
			parseMenu(menu, i, new ArrayList<MenuEntry>(), menuTable);
		}
		return menuTable;
	}

	private void parseMenu(final MenuItem menuItem,
		final double weight, final ArrayList<MenuEntry> path,
		final Map<String, List<MenuEntry>> menuTable)
	{
		// build menu entry
		final String name = menuItem.getLabel();
		final MenuEntry entry = new MenuEntry(name, weight);
		final MenuShortcut shortcut = menuItem.getShortcut();
		if (shortcut != null) {
			final int keyCode = shortcut.getKey();
			final int shortcutMask =
				Toolkit.getDefaultToolkit().getMenuShortcutKeyMask();
			final int shiftMask =
				shortcut.usesShiftModifier() ? InputEvent.SHIFT_MASK : 0;
			final int modifiers = shortcutMask | shiftMask;
			final KeyStroke keyStroke = KeyStroke.getKeyStroke(keyCode, modifiers);
			final String accelerator = keyStroke.toString();
			entry.setAccelerator(accelerator);
		}
		path.add(entry);

		if (menuItem instanceof Menu) { // non-leaf
			// recursively process child menu items
			final Menu menu = (Menu) menuItem;
			final int itemCount = menu.getItemCount();
			double w = -1;
			for (int i = 0; i < itemCount; i++) {
				final MenuItem item = menu.getItem(i);
				final boolean isSeparator = item.getLabel().equals("-");
				if (isSeparator) w += 10;
				else w += 1;
				parseMenu(item, w, ListUtils.copyList(path), menuTable);
			}
		}
		else { // leaf item
			// flag legacy plugin with special icon
			entry.setIcon("/icons/legacy.png");

			// add menu item to table
			menuTable.put(menuItem.getLabel(), path);
		}
	}

	private String parsePluginClass(final String ij1PluginString) {
		final int quote = ij1PluginString.indexOf("(");
		if (quote < 0) return ij1PluginString;
		return ij1PluginString.substring(0, quote);
	}

	private String parseArg(final String ij1PluginString) {
		final int quote = ij1PluginString.indexOf("\"");
		if (quote < 0) return "";
		final int quote2 = ij1PluginString.indexOf("\"", quote + 1);
		if (quote2 < 0) return ij1PluginString.substring(quote + 1);
		return ij1PluginString.substring(quote + 1, quote2);
	}

}
