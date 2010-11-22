package imagej2.ij1bridge.plugin;

import ij.IJ;
import ij.ImageJ;
import ij.Menus;
import imagej2.plugin.PluginEntry;
import imagej2.plugin.PluginFinder;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import org.openide.util.lookup.ServiceProvider;

@ServiceProvider(service=PluginFinder.class)
public class Ij1PluginFinder implements PluginFinder {

	@Override
	public void findPlugins(List<PluginEntry> plugins) {
		if (IJ.getInstance() == null) {
			// TODO -- use NO_SHOW mode (requires ImageJ 1.44)
			new ImageJ();
			IJ.getInstance().setVisible(false);
		}
		final Hashtable<?, ?> commands = Menus.getCommands();
		for (final Object key : commands.keySet()) {
			final String pluginClass = commands.get(key).toString();
			final List<String> parentMenu = new ArrayList<String>();
			// TODO -- populate with actual menu location
			parentMenu.add("Plugins");
			final String label = key.toString();
			final PluginEntry pluginEntry = new PluginEntry(pluginClass, parentMenu, label);
			plugins.add(pluginEntry);
		}
	}

	/**
	 * Tests the IJ1 plugin discovery mechanism,
	 * printing a list of all discovered plugins.
	 */
	public static void main(String[] args) {
		System.setProperty("plugins.dir", "/Applications/Science/ImageJ/plugins");//TEMP
		System.out.println("Finding plugins...");
		List<PluginEntry> plugins = new ArrayList<PluginEntry>();
		new Ij1PluginFinder().findPlugins(plugins);
		System.out.println("Discovered plugins:");
		for (PluginEntry plugin : plugins) {
			System.out.println("\t" + plugin);
		}
		System.exit(0);
	}

}
