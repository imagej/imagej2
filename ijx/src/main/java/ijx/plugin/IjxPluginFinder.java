package ijx.plugin;

import java.util.ArrayList;
import java.util.List;

import org.openide.util.lookup.ServiceProvider;

import imagej2.plugin.PluginEntry;
import imagej2.plugin.PluginFinder;

@ServiceProvider(service=PluginFinder.class)
public class IjxPluginFinder implements PluginFinder {

	@Override
	public void findPlugins(List<PluginEntry> plugins) {
		// TODO add real implementation here.
		String pluginClass = "ijx.plugin.FooBar";
		ArrayList<String> parentMenu = new ArrayList<String>();
		parentMenu.add("Plugins");
		parentMenu.add("Foo");
		String label = "Bar";
		PluginEntry entry = new PluginEntry(pluginClass, parentMenu, label);
		plugins.add(entry);
	}

}
