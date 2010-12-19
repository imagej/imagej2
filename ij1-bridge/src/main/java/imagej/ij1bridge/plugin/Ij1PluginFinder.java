package imagej.ij1bridge.plugin;

import ij.IJ;
import ij.ImageJ;
import ij.Menus;
import imagej.plugin.PluginEntry;
import imagej.plugin.PluginFinder;

import java.awt.Menu;
import java.awt.MenuBar;
import java.awt.MenuItem;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import org.openide.util.lookup.ServiceProvider;

@ServiceProvider(service=PluginFinder.class)
public class Ij1PluginFinder implements PluginFinder {

	@Override
	public void findPlugins(List<PluginEntry> plugins) {
		if (IJ.getInstance() == null) {
			new ImageJ(ImageJ.NO_SHOW);
		}
		final ImageJ ij = IJ.getInstance();
		if (ij == null) return;

		final Map<String, List<String>> menuTable = parseMenus(ij);
		final Hashtable<?, ?> commands = Menus.getCommands();
		for (final Object key : commands.keySet()) {
			final String label = key.toString();
			final String ij1PluginString = commands.get(key).toString();
			final String pluginClass = parsePluginClass(ij1PluginString);
			final List<String> menuPath = menuTable.get(label);
			final String arg = parseArg(ij1PluginString);
			final PluginEntry pluginEntry =
				new PluginEntry(pluginClass, menuPath, label, arg);
			plugins.add(pluginEntry);
		}
	}

	/** Creates a table mapping IJ1 command labels to menu paths. */
	private Map<String, List<String>> parseMenus(ImageJ ij) {
		final Map<String, List<String>> menuTable =
			new HashMap<String, List<String>>();
		final MenuBar menubar = ij.getMenuBar();
		final int menuCount = menubar.getMenuCount();
		for (int i = 0; i < menuCount; i++) {
			final Menu menu = menubar.getMenu(i);
			parseMenu(menu, new ArrayList<String>(), menuTable);
		}
		return menuTable;
	}

	private void parseMenu(final Menu menu,
		final ArrayList<String> path, final Map<String, List<String>> menuTable)
	{
		final String label = menu.getLabel();
		path.add(label);
		final int itemCount = menu.getItemCount();
		for (int i = 0; i < itemCount; i++) {
			final MenuItem item = menu.getItem(i);
			if (item instanceof Menu) {
				final Menu subMenu = (Menu) item;
				parseMenu(subMenu, copyList(path), menuTable);
			}
			else {
				// add leaf item to table
				menuTable.put(item.getLabel(), path);
			}
		}
	}

	@SuppressWarnings("unchecked")
	private ArrayList<String> copyList(final ArrayList<String> list) {
		return (ArrayList<String>) list.clone();
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
