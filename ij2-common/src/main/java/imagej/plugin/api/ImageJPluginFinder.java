package imagej.plugin.api;

import imagej.Log;
import imagej.plugin.IPlugin;
import imagej.plugin.Menu;
import imagej.plugin.Plugin;
import imagej.plugin.spi.PluginFinder;

import java.util.ArrayList;
import java.util.List;

import net.java.sezpoz.Index;
import net.java.sezpoz.IndexItem;

import org.openide.util.lookup.ServiceProvider;

@ServiceProvider(service=PluginFinder.class)
public class ImageJPluginFinder implements PluginFinder {

	@Override
	public void findPlugins(List<PluginEntry> plugins) {
		Log.debug("Searching for plugins...");
		// use SezPoz to discover available IJ2 plugins
		for (final IndexItem<Plugin, IPlugin> item :
			Index.load(Plugin.class, IPlugin.class))
		{
			final String pluginClass = item.className();
			final List<MenuEntry> menuPath = new ArrayList<MenuEntry>();

			// parse menu path from annotations
			final Menu[] menu = item.annotation().menu();
			if (menu.length > 0) {
				parseMenuPath(menuPath, menu);
			}
			else {
				// parse menuPath attribute
				final String path = item.annotation().menuPath();
				parseMenuPath(menuPath, path);
			}

			final String arg = "";
			final PluginEntry pluginEntry =
				new PluginEntry(pluginClass, menuPath, arg);
			plugins.add(pluginEntry);
			Log.debug("Found plugin: " + pluginEntry);
		}
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
