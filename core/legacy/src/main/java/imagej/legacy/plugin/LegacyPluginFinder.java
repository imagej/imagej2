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

package imagej.legacy.plugin;

import ij.IJ;
import ij.Menus;
import imagej.ext.Accelerator;
import imagej.ext.InputModifiers;
import imagej.ext.KeyCode;
import imagej.ext.MenuEntry;
import imagej.ext.MenuPath;
import imagej.ext.plugin.ImageJPlugin;
import imagej.ext.plugin.PluginInfo;
import imagej.ext.plugin.PluginModuleInfo;
import imagej.util.Log;

import java.awt.Menu;
import java.awt.MenuBar;
import java.awt.MenuItem;
import java.awt.MenuShortcut;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Discovers legacy ImageJ 1.x plugins.
 * <p>
 * To accomplish this, we must crawl the (invisible) ImageJ 1.x AWT menu,
 * because IJ1 does not store the list of commands in any other data structure.
 * </p>
 * 
 * @author Curtis Rueden
 * @author Barry DeZonia
 */
public class LegacyPluginFinder {

	private static final String LEGACY_PLUGIN_CLASS = LegacyPlugin.class
		.getName();

	private static final String PLUGIN_BLACKLIST = "plugin-blacklist.txt";
	private static final String LEGACY_PLUGIN_ICON = "/icons/legacy.png";

	/** A list of plugins to exclude from legacy plugin discovery. */
	private final Set<String> blacklist;

	public LegacyPluginFinder() {
		blacklist = new HashSet<String>();

		// load blacklist items from data file
		try {
			readBlacklistFile(blacklist, PLUGIN_BLACKLIST);
		}
		catch (final IOException e) {
			Log.error("Error reading blacklist", e);
		}
	}

	// -- LegacyPluginFinder methods --

	public void findPlugins(final List<PluginInfo<?>> plugins) {
		final ij.ImageJ ij = IJ.getInstance();
		if (ij == null) return; // no IJ1, so no IJ1 plugins
		final Map<String, MenuPath> menuTable = parseMenus(ij);
		final Hashtable<?, ?> commands = Menus.getCommands();
		Log.info("Found " + commands.size() + " legacy plugins.");
		for (final Object key : commands.keySet()) {
			final PluginInfo<ImageJPlugin> pe =
				createEntry(key, commands, menuTable);
			if (pe != null) plugins.add(pe);
		}
	}

	// -- Helper methods --

	private void readBlacklistFile(final Set<String> list, final String file)
		throws IOException
	{
		final InputStream in = getClass().getResourceAsStream(file);
		if (in == null) throw new FileNotFoundException("Blacklist not found");
		final BufferedReader r = new BufferedReader(new InputStreamReader(in));
		while (true) {
			final String line = r.readLine();
			if (line == null) break; // eof
			final String item = line.replaceAll("#.*", "").trim(); // ignore comments
			if (item.isEmpty()) continue; // skip blank lines
			list.add(item);
		}
		r.close();
	}

	private PluginInfo<ImageJPlugin> createEntry(final Object key,
		final Hashtable<?, ?> commands, final Map<String, MenuPath> menuTable)
	{
		final String ij1PluginString = commands.get(key).toString();
		final boolean blacklisted = blacklist.contains(ij1PluginString);
		final MenuPath menuPath = menuTable.get(key);

		final String debugString;
		if (Log.isDebug()) {
			debugString =
				"- " + (blacklisted ? "[BLACKLISTED] " : "") + ij1PluginString +
					" [menu = " + menuPath.getMenuString() + ", weight = " +
					menuPath.getLeaf().getWeight() + "]";
		}
		else debugString = null;
		Log.debug(debugString);

		if (blacklisted) return null;

		final String className = parsePluginClass(ij1PluginString);
		final String arg = parseArg(ij1PluginString);

		final Map<String, Object> presets = new HashMap<String, Object>();
		presets.put("className", className);
		presets.put("arg", arg);
		final PluginModuleInfo<ImageJPlugin> pe =
			new PluginModuleInfo<ImageJPlugin>(LEGACY_PLUGIN_CLASS,
				ImageJPlugin.class);
		pe.setMenuPath(menuPath);
		pe.setPresets(presets);

		// flag legacy plugin with special icon
		menuPath.getLeaf().setIconPath(LEGACY_PLUGIN_ICON);

		return pe;
	}

	/** Creates a table mapping IJ1 command labels to menu paths. */
	private Map<String, MenuPath> parseMenus(final ij.ImageJ ij) {
		final Map<String, MenuPath> menuTable = new HashMap<String, MenuPath>();
		final MenuBar menubar = ij.getMenuBar();
		final int menuCount = menubar.getMenuCount();
		for (int i = 0; i < menuCount; i++) {
			final Menu menu = menubar.getMenu(i);
			parseMenu(menu, i, new MenuPath(), menuTable);
		}
		return menuTable;
	}

	private void parseMenu(final MenuItem menuItem, final double weight,
		final MenuPath path, final Map<String, MenuPath> menuTable)
	{
		// build menu entry
		final String name = menuItem.getLabel();
		final MenuEntry entry = new MenuEntry(name, weight);
		final MenuShortcut shortcut = menuItem.getShortcut();
		if (shortcut != null) {
			// convert AWT MenuShortcut to ImageJ Accelerator
			final int code = shortcut.getKey();
			final boolean meta = Accelerator.isCtrlReplacedWithMeta();
			final boolean ctrl = !meta;
			final boolean shift = shortcut.usesShiftModifier();
			final KeyCode keyCode = KeyCode.get(code);
			final InputModifiers modifiers =
				new InputModifiers(false, false, ctrl, meta, shift, false, false,
					false);
			final Accelerator acc = new Accelerator(keyCode, modifiers);
			entry.setAccelerator(acc);
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
				parseMenu(item, w, new MenuPath(path), menuTable);
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
