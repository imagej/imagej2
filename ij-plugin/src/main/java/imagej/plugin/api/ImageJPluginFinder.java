package imagej.plugin.api;

import imagej.Log;
import imagej.plugin.ImageJPlugin;
import imagej.plugin.spi.PluginFinder;

import java.util.List;

import org.openide.util.lookup.ServiceProvider;

@ServiceProvider(service=PluginFinder.class)
public class ImageJPluginFinder extends SezpozPluginFinder<ImageJPlugin>
	implements PluginFinder
{

	@Override
	public void findPlugins(List<PluginEntry> plugins) {
		Log.debug("Searching for plugins...");
		findPlugins(plugins, ImageJPlugin.class);
	}

}