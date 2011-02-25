package imagej.plugin.finder;

import imagej.Log;
import imagej.plugin.ImageJPlugin;
import imagej.plugin.api.PluginEntry;
import imagej.plugin.api.PluginIndex;

import java.util.ArrayList;
import java.util.List;

/**
 * TODO
 *
 * @author Curtis Rueden
 */
@PluginFinder
public class ImageJPluginFinder implements IPluginFinder {

	@Override
	public void findPlugins(List<PluginEntry<?>> plugins) {
		Log.debug("Searching for plugins...");
		final ArrayList<PluginEntry<ImageJPlugin>> pluginList =
			PluginIndex.getIndex().getPlugins(ImageJPlugin.class);
		plugins.addAll(pluginList);
	}

}
