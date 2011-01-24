package imagej.ij1bridge.plugin;

import ij.IJ;
import ij.ImageJ;
import ij.Menus;
import imagej.Log;
import imagej.plugin.MenuEntry;
import imagej.plugin.PluginEntry;
import imagej.plugin.PluginFinder;

import java.awt.Menu;
import java.awt.MenuBar;
import java.awt.MenuItem;
import java.awt.MenuShortcut;
import java.awt.Toolkit;
import java.awt.event.InputEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import javax.swing.KeyStroke;

import org.openide.util.lookup.ServiceProvider;

@ServiceProvider(service=PluginFinder.class)
public class Ij1PluginFinder implements PluginFinder {

	@Override
	public void findPlugins(List<PluginEntry> plugins) {
		Log.debug("Searching for legacy plugins...");
		if (IJ.getInstance() == null) {
			new ImageJ(ImageJ.NO_SHOW);
		}
		final ImageJ ij = IJ.getInstance();
		if (ij == null) return;

		final Map<String, List<MenuEntry>> menuTable = parseMenus(ij);
		final Hashtable<?, ?> commands = Menus.getCommands();
		for (final Object key : commands.keySet()) {
			final String ij1PluginString = commands.get(key).toString();
			final String pluginClass = parsePluginClass(ij1PluginString);
			final List<MenuEntry> menuPath = menuTable.get(key);
			final String arg = parseArg(ij1PluginString);
			final PluginEntry pluginEntry =
				new PluginEntry(pluginClass, menuPath, arg);
			plugins.add(pluginEntry);
			Log.debug("Found legacy plugin: " + pluginEntry);
		}
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

	private void parseMenu(final MenuItem menuItem, final int pos,
		final ArrayList<MenuEntry> path,
		final Map<String, List<MenuEntry>> menuTable)
	{
		// build menu entry
		final String name = menuItem.getLabel();
		final double weight = pos;
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

		if (menuItem instanceof Menu) {
			// non-leaf; recursively process child menu items
			final Menu menu = (Menu) menuItem;
			final int itemCount = menu.getItemCount();
			for (int i = 0; i < itemCount; i++) {
				final MenuItem item = menu.getItem(i);
				parseMenu(item, i, copyList(path), menuTable);
			}
		}
		else {
			// leaf; add menu item to table
			menuTable.put(menuItem.getLabel(), path);
		}
	}

	@SuppressWarnings("unchecked")
	private ArrayList<MenuEntry> copyList(final ArrayList<MenuEntry> list) {
		return (ArrayList<MenuEntry>) list.clone();
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
