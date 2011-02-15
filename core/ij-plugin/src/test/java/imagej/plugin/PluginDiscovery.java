package imagej.plugin;

import imagej.plugin.api.PluginEntry;
import imagej.plugin.api.PluginUtils;

import java.util.List;

public class PluginDiscovery {

	/**
	 * Tests the plugin discovery mechanism,
	 * printing a list of all discovered plugins.
	 */
	public static void main(String[] args) {
		System.out.println("Scanning for plugins:");
		final List<PluginEntry<?>> plugins = PluginUtils.findPlugins();
		System.out.println("Discovered plugins:");
		for (final PluginEntry<?> plugin : plugins) {
			System.out.println("\t" + plugin);
		}
	}

}
