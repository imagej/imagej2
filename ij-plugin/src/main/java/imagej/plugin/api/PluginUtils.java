package imagej.plugin.api;

import imagej.plugin.IPlugin;
import imagej.plugin.spi.PluginFinder;

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

	public static IPlugin runPlugin(PluginEntry entry) {
		return new PluginRunner(entry).run();
	}

}
