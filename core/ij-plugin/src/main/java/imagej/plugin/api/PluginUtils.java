package imagej.plugin.api;

import imagej.Log;
import imagej.plugin.RunnablePlugin;
import imagej.plugin.finder.IPluginFinder;
import imagej.plugin.finder.PluginFinder;

import java.util.ArrayList;
import java.util.List;

import net.java.sezpoz.Index;
import net.java.sezpoz.IndexItem;

/**
 * Utility class for discovering and launching plugins.
 *
 * @author Curtis Rueden
 */
public final class PluginUtils {

	private PluginUtils() {
		// prohibit instantiation of utility class
	}

	public static List<PluginEntry<?>> findPlugins() {
		// use SezPoz to discover all plugin finders
		final List<PluginEntry<?>> plugins = new ArrayList<PluginEntry<?>>();
		for (final IndexItem<PluginFinder, IPluginFinder> item :
			Index.load(PluginFinder.class, IPluginFinder.class))
		{
			try {
				final IPluginFinder finder = item.instance();
				finder.findPlugins(plugins);
			}
			catch (InstantiationException e) {
				Log.warn("Invalid plugin finder: " + item, e);
			}
		}
		return plugins;
	}

	public static <T extends RunnablePlugin> T runPlugin(PluginEntry<T> entry) {
		return new PluginRunner<T>(entry).run();
	}

}
