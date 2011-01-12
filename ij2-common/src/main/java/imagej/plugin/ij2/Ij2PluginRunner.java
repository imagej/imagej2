package imagej.plugin.ij2;

import imagej.Log;
import imagej.dataset.Dataset;
import imagej.plugin.PluginEntry;
import imagej.plugin.PluginException;
import imagej.plugin.PluginRunner;
import imagej.plugin.PluginUtils;

import java.util.Map;

import org.openide.util.lookup.ServiceProvider;

/** Executes an IJ2 plugin. */
@ServiceProvider(service=PluginRunner.class)
public class Ij2PluginRunner implements PluginRunner {

	@Override
	public IPlugin runPlugin(final PluginEntry entry) throws PluginException {
		final IPlugin plugin = createInstance(entry);

		// execute plugin
		preProcess(plugin);
		plugin.run();
		postProcess(plugin);
		
		return plugin;
	}

	private void preProcess(final IPlugin plugin) {
		// FIXME - populate plugin parameters before execution
	}

	private void postProcess(final IPlugin plugin) throws PluginException {
		Log.debug("INPUTS:");
		final Map<String, Object> inputs = ParameterHandler.getInputMap(plugin);
		for (String key : inputs.keySet()) {
			Log.debug("\t" + key + " = " + inputs.get(key));
		}
		Log.debug("OUTPUTS:");
		final Map<String, Object> outputs = ParameterHandler.getOutputMap(plugin);
		for (String key : outputs.keySet()) {
			Log.debug("\t" + key + " = " + outputs.get(key));
			// display output datasets onscreen
			final Object value = outputs.get(key);
			if (value instanceof Dataset) {
				final Dataset dataset = (Dataset) value;
				PluginUtils.display(dataset);
			}
			// TODO - handle other output parameter types?
		}
	}

	public IPlugin createInstance(final PluginEntry entry)
		throws PluginException
	{
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
