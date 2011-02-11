package imagej.plugin.finder;

import imagej.Log;
import imagej.plugin.ImageJPlugin;
import imagej.plugin.api.PluginEntry;

import java.util.List;

@PluginFinder
public class ImageJPluginFinder extends SezpozPluginFinder<ImageJPlugin>
	implements IPluginFinder
{

	@Override
	public void findPlugins(List<PluginEntry> plugins) {
		Log.debug("Searching for plugins...");
		findPlugins(plugins, ImageJPlugin.class);
	}

}
