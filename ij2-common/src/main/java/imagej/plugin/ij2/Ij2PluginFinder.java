package imagej.plugin.ij2;

import imagej.Log;
import imagej.plugin.PluginEntry;
import imagej.plugin.PluginFinder;

import java.util.ArrayList;
import java.util.List;

import net.java.sezpoz.Index;
import net.java.sezpoz.IndexItem;

import org.openide.util.lookup.ServiceProvider;

@ServiceProvider(service=PluginFinder.class)
public class Ij2PluginFinder implements PluginFinder {

	@Override
	public void findPlugins(List<PluginEntry> plugins) {
		for (final IndexItem<Plugin, IPlugin> item :
			Index.load(Plugin.class, IPlugin.class))
		{
			final String pluginClass = item.className();
			final List<String> menuPath = new ArrayList<String>();

			// parse menu path from annotations
			final Menu[] menu = item.annotation().menu();
			if (menu.length > 0) {
				for (Menu m : menu) menuPath.add(m.label());
			}
			else {
				// parse menuPath attribute
				final String[] menuPathTokens = item.annotation().menuPath().split(">");
				for (String token : menuPathTokens) menuPath.add(token);
			}

			// TEMP - use last menu element for label
			final int lastIndex = menuPath.size() - 1;
			final String label;
			if (lastIndex < 0) {
				label = "";
			}
			else {
				label = menuPath.get(lastIndex);
				menuPath.remove(lastIndex);
			}

			final String arg = "";
			final PluginEntry pluginEntry =
				new PluginEntry(pluginClass, menuPath, label, arg);
			plugins.add(pluginEntry);

			Log.debug("Loaded " + pluginEntry);
		}
	}

}
