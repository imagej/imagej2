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
 * Discovers legacy ImageJ 1.x plugins by walking the invisible IJ1 AWT menu entries
 * 
 * @author Curtis Rueden
 */
@PluginFinder
public class LegacyPluginFinder implements IPluginFinder {

	private static final String LEGACY_PLUGIN_CLASS =
		LegacyPlugin.class.getName();
	
	private static final String LEGACY_PLUGIN_ICON = "/icons/legacy.png";

	/** A list of plugins to exclude from legacy plugin discovery. */
	private final Set<String> blacklist;

	public LegacyPluginFinder() {
		blacklist = new HashSet<String>();
		
		// TODO - load these from a data file
		
		blacklist.add("ij.plugin.Commands(\"quit\")");  // File>Quit
		blacklist.add("ij.plugin.filter.Filler(\"fill\")");  // Edit>Fill
		blacklist.add("ij.plugin.filter.Filters(\"invert\")");  // Edit>Invert
		blacklist.add("ij.plugin.Options(\"line\")");  // Edit>Options>Line Width
		blacklist.add("ij.plugin.Options(\"io\")");  // Edit>Options>Input/Output
		blacklist.add("ij.plugin.frame.Fonts");  // Edit>Options>Fonts
		blacklist.add("ij.plugin.filter.Profiler(\"set\")");  // Edit>Options>Profile Plot
		blacklist.add("ij.plugin.ArrowToolOptions");  // Edit>Options>Arrow Tool
		blacklist.add("ij.plugin.Colors(\"point\")");  // Edit>Options>Point Tool
		blacklist.add("ij.plugin.WandToolOptions");  // Edit>Options>Wand Tools
		blacklist.add("ij.plugin.Colors");  // Edit>Options>Colors
		blacklist.add("ij.plugin.Options(\"display\")");  // Edit>Options>Appearance
		blacklist.add("ij.plugin.Options(\"conv\")");  // Edit>Options>Conversions
		blacklist.add("ij.plugin.Memory");  // Edit>Options>Memory & Threads
		blacklist.add("ij.plugin.ProxySettings");  // Edit>Options>Proxy Settings
		blacklist.add("ij.plugin.Compiler(\"options\")");  // Edit>Options>Compiler
		blacklist.add("ij.plugin.Options(\"dicom\")");  // Edit>Options>DICOM
		blacklist.add("ij.plugin.Options(\"misc\")");  // Edit>Options>Misc.
		blacklist.add("ij.plugin.Resizer(\"crop\")");  // Image>Crop
		blacklist.add("ij.plugin.Duplicator"); // Image>Duplicate
		blacklist.add("ij.plugin.filter.Transformer(\"fliph\")");  // Image>Transform>Flip Horizontally
		blacklist.add("ij.plugin.filter.Transformer(\"flipv\")");  // Image>Transform>Flip Vertically
		blacklist.add("ij.plugin.filter.Transformer(\"left\")");  // Image>Transform>Rotate 90 Degrees Left
		blacklist.add("ij.plugin.filter.Transformer(\"right\")");  // Image>Transform>Rotate 90 Degrees Right
		blacklist.add("ij.plugin.filter.Filters(\"smooth\")");  // Process>Smooth
		blacklist.add("ij.plugin.filter.Filters(\"sharpen\")");  // Process>Sharpen
		blacklist.add("ij.plugin.filter.Filters(\"edge\")");  // Process>Find Edges
		blacklist.add("ij.plugin.filter.Filters(\"add\")");  // Process>Noise>Add Noise
		blacklist.add("ij.plugin.filter.Filters(\"noise\")"); // Process>Noise>Add Specified Noise
		blacklist.add("ij.plugin.filter.SaltAndPepper");  // Process>Noise>Salt and Pepper
		blacklist.add("ij.plugin.filter.Shadows(\"north\")");  // Process>Shadows>North 
		blacklist.add("ij.plugin.filter.Shadows(\"northeast\")");  // Process>Shadows>Northeast
		blacklist.add("ij.plugin.filter.Shadows(\"east\")");  // Process>Shadows>East
		blacklist.add("ij.plugin.filter.Shadows(\"southeast\")");  // Process>Shadows>Southeast
		blacklist.add("ij.plugin.filter.Shadows(\"south\")");  // Process>Shadows>South
		blacklist.add("ij.plugin.filter.Shadows(\"southwest\")");  // Process>Shadows>Southwest
		blacklist.add("ij.plugin.filter.Shadows(\"west\")");  // Process>Shadows>West
		blacklist.add("ij.plugin.filter.Shadows(\"northwest\")");  // Process>Shadows>Northwest
		blacklist.add("ij.plugin.filter.ImageMath(\"add\")");  // Process>Math>Add
		blacklist.add("ij.plugin.filter.ImageMath(\"sub\")");  // Process>Math>Subtract
		blacklist.add("ij.plugin.filter.ImageMath(\"mul\")");  // Process>Math>Multiply
		blacklist.add("ij.plugin.filter.ImageMath(\"div\")");  // Process>Math>Divide
		blacklist.add("ij.plugin.filter.ImageMath(\"and\")");  // Process>Math>AND
		blacklist.add("ij.plugin.filter.ImageMath(\"or\")");  // Process>Math>OR
		blacklist.add("ij.plugin.filter.ImageMath(\"xor\")");  // Process>Math>XOR
		blacklist.add("ij.plugin.filter.ImageMath(\"min\")");  // Process>Math>Min
		blacklist.add("ij.plugin.filter.ImageMath(\"max\")");  // Process>Math>Max
		blacklist.add("ij.plugin.filter.ImageMath(\"gamma\")");  // Process>Math>Gamma
		blacklist.add("ij.plugin.filter.ImageMath(\"set\")");  // Process>Math>Set
		blacklist.add("ij.plugin.filter.ImageMath(\"log\")");  // Process>Math>Log
		blacklist.add("ij.plugin.filter.ImageMath(\"exp\")");  // Process>Math>Exp
		blacklist.add("ij.plugin.filter.ImageMath(\"sqr\")");  // Process>Math>Sqr
		blacklist.add("ij.plugin.filter.ImageMath(\"sqrt\")");  // Process>Math>Sqrt
		blacklist.add("ij.plugin.filter.ImageMath(\"reciprocal\")");  // Process>Math>Reciprocal
		blacklist.add("ij.plugin.filter.ImageMath(\"nan\")");  // Process>Math>Set Background To NaN
		blacklist.add("ij.plugin.filter.ImageMath(\"abs\")");  // Process>Math>Abs
		blacklist.add("ij.plugin.ImageCalculator");  // Process>Image Calculator
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

	private PluginEntry<ImageJPlugin> createEntry(
		final Object key, final Hashtable<?, ?> commands,
		final Map<String, List<MenuEntry>> menuTable)
	{
		final String ij1PluginString = commands.get(key).toString();

		if (blacklist.contains(ij1PluginString)) {
			Log.debug("- [BLACKLISTED] " + ij1PluginString);
			return null;
		}
		Log.debug("- " + ij1PluginString);

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

		// flag legacy plugin with special icon
		menuPath.get(menuPath.size() - 1).setIcon(LEGACY_PLUGIN_ICON);

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
