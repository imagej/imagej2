package imagej.plugin.ij2;

import imagej.Log;
import imagej.plugin.PluginEntry;
import imagej.plugin.PluginException;
import imagej.plugin.PluginRunner;

import java.util.Map;

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

		// FIXME - do something with output parameters:
		// invoke an AutoDisplayPlugin that matches each output

		Log.debug("INPUTS:");
		final Map<String, Object> inputs = ParameterHandler.getInputMap(plugin);
		for (String key : inputs.keySet()) {
			Log.debug("\t" + key + " = " + inputs.get(key));
		}
		Log.debug("OUTPUTS:");
		final Map<String, Object> outputs = ParameterHandler.getOutputMap(plugin);
		for (String key : outputs.keySet()) {
			Log.debug("\t" + key + " = " + outputs.get(key));
		}
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
