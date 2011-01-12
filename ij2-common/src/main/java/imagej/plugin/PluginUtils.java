package imagej.plugin;

import imagej.dataset.Dataset;
import imagej.plugin.ij2.DisplayPlugin;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.openide.util.Lookup;

/** Utility class for discovering and launching plugins. */
public final class PluginUtils {

	private PluginUtils() {
		// prohibit instantiation of utility class
	}

	public static List<PluginEntry> findPlugins() {
		final Collection<? extends PluginFinder> finders =
			Lookup.getDefault().lookupAll(PluginFinder.class);
		List<PluginEntry> plugins = new ArrayList<PluginEntry>();
		for (PluginFinder finder : finders) {
			finder.findPlugins(plugins);
		}
		return plugins;
	}

	public static Object runPlugin(PluginEntry entry) {
		final Collection<? extends PluginRunner> runners =
			Lookup.getDefault().lookupAll(PluginRunner.class);
		for (PluginRunner runner : runners) {
			try {
				return runner.runPlugin(entry);
			}
			catch (PluginException e) {
				// execution failed; try the next runner
			}
		}
		throw new IllegalArgumentException(
			"No compatible PluginRunners for PluginEntry: " + entry);
	}

}
