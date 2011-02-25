package imagej.tool.finder;

import imagej.plugin.api.PluginEntry;

import java.util.List;

/**
 * TODO
 *
 * @author Rick Lentz
 */
public interface IToolFinder {

	/** Discovers plugins, appending them to the given list. */
	void findPlugins(List<PluginEntry<?>> plugins);

}
