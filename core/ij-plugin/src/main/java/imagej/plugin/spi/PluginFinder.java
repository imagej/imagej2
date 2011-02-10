package imagej.plugin.spi;

import imagej.plugin.api.PluginEntry;

import java.util.List;

public interface PluginFinder {

	/** Discovers plugins, appending them to the given list. */
	void findPlugins(List<PluginEntry> plugins);

}
