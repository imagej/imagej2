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
		final long startTime = System.currentTimeMillis();
		final ArrayList<PluginEntry<ImageJPlugin>> pluginList =
			PluginIndex.getIndex().getPlugins(ImageJPlugin.class);
		final long endTime = System.currentTimeMillis();
		plugins.addAll(pluginList);
		if (Log.isDebug()) {
			final long time = endTime - startTime;
			Log.debug("Found " + pluginList.size() +
				" plugins in " + time + " ms:");
			for (PluginEntry<ImageJPlugin> pe : pluginList) {
				Log.debug("- " + pe);
			}
		}
	}

}
