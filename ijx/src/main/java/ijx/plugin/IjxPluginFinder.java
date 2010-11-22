package ijx.plugin;

import java.util.ArrayList;
import java.util.List;

import org.openide.util.lookup.ServiceProvider;

import imagej2.plugin.PluginEntry;
import imagej2.plugin.PluginFinder;

@ServiceProvider(service=PluginFinder.class)
public class IjxPluginFinder implements PluginFinder {

	@Override
	public void findPlugins(List<PluginEntry> plugins) {
		// TODO -- add real implementation here
		String pluginClass = "ijx.plugin.FooBar";
		ArrayList<String> parentMenu = new ArrayList<String>();
		parentMenu.add("Plugins");
		parentMenu.add("Foo");
		String label = "Bar";
		PluginEntry entry = new PluginEntry(pluginClass, parentMenu, label);
		plugins.add(entry);
	}

	/**
	 * Tests the IJX plugin discovery mechanism,
	 * printing a list of all discovered plugins.
	 */
	public static void main(String[] args) {
		System.out.println("Finding plugins...");
		List<PluginEntry> plugins = new ArrayList<PluginEntry>();
		new IjxPluginFinder().findPlugins(plugins);
		System.out.println("Discovered plugins:");
		for (PluginEntry plugin : plugins) {
			System.out.println("\t" + plugin);
		}
		System.exit(0);
	}

}
