package ijx.plugin;

import imagej.plugin.api.PluginEntry;
import imagej.plugin.finder.IPluginFinder;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.openide.util.Lookup;

public class PluginDiscovery {

	/**
	 * Tests the plugin discovery mechanism,
	 * printing a list of all discovered plugins.
	 */
	public static void main(String[] args) {
		System.out.println("Scanning for plugin finders...");
		Collection<? extends IPluginFinder> finders =
			Lookup.getDefault().lookupAll(IPluginFinder.class);

		List<PluginEntry> plugins = new ArrayList<PluginEntry>();

		for (IPluginFinder finder : finders) {
			System.out.println("Querying " + finder + "...");
			finder.findPlugins(plugins);
		}
		System.out.println("Discovered plugins:");
		for (PluginEntry plugin : plugins) {
			System.out.println("\t" + plugin);
		}
	}

}
