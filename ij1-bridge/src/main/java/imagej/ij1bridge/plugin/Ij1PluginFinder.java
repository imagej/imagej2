package imagej.ij1bridge.plugin;

import ij.IJ;
import ij.ImageJ;
import ij.Menus;
import imagej.plugin.PluginEntry;
import imagej.plugin.PluginFinder;

import java.util.Hashtable;
import java.util.List;

import org.openide.util.lookup.ServiceProvider;

@ServiceProvider(service=PluginFinder.class)
public class Ij1PluginFinder implements PluginFinder {

	@Override
	public void findPlugins(List<PluginEntry> plugins) {
		if (IJ.getInstance() == null) {
			new ImageJ(ImageJ.NO_SHOW);
		}
		final Hashtable<?, ?> commands = Menus.getCommands();
		for (final Object key : commands.keySet()) {
			final String pluginClass = commands.get(key).toString();
			final String label = key.toString();
			final PluginEntry pluginEntry = new PluginEntry(  pluginClass, label );

			//try to find the menu hierarchy
			PluginAdapterUtils plugInAdapterUtils = new PluginAdapterUtils(IJ.getInstance());
			plugInAdapterUtils.setIJPluginParentMenu(pluginEntry);
			plugins.add(pluginEntry);
		}
	}


}
