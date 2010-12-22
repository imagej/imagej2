package imagej.plugin;

import imagej.plugin.PluginEntry;
import imagej.plugin.PluginException;
import imagej.plugin.PluginRunner;

import org.openide.util.lookup.ServiceProvider;

/** Executes an IJ2 plugin. */
@ServiceProvider(service=PluginRunner.class)
public class Ij2PluginRunner implements PluginRunner {

	@Override
	public void runPlugin(PluginEntry entry) throws PluginException {
		final IPlugin plugin = createInstance(entry);

		// FIXME - populate plugin parameters before execution

		// execute plugin
		plugin.run();
	}

	public IPlugin createInstance(PluginEntry entry) throws PluginException {
		// get Class object for plugin entry
		final Class<?> pluginClass;
		try {
			pluginClass = Class.forName(entry.getPluginClass());
		}
		catch (ClassNotFoundException e) {
			throw new PluginException(e);
		}
		if (!IPlugin.class.isAssignableFrom(pluginClass)) {
			throw new PluginException("Not an IJ2 plugin");
		}

		// instantiate plugin
		final Object pluginInstance;
		try {
			pluginInstance = pluginClass.newInstance();
		}
		catch (InstantiationException e) {
			throw new PluginException(e);
		}
		catch (IllegalAccessException e) {
			throw new PluginException(e);
		}
		if (!(pluginInstance instanceof IPlugin)) {
			throw new PluginException("Not a java.lang.IPlugin");
		}
		IPlugin plugin = (IPlugin) pluginInstance;

		return plugin;
	}

}
