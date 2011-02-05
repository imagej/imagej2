package imagej.plugin.api;

import imagej.plugin.IPlugin;
import imagej.plugin.spi.PluginPostprocessor;
import imagej.plugin.spi.PluginPreprocessor;
import imagej.plugin.spi.PluginRunner;

import java.util.Collection;

import org.openide.util.Lookup;
import org.openide.util.lookup.ServiceProvider;

/** Executes an IJ2 plugin. */
@ServiceProvider(service=PluginRunner.class)
public class ImageJPluginRunner implements PluginRunner {

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
		final Collection<? extends PluginPreprocessor> processors =
			Lookup.getDefault().lookupAll(PluginPreprocessor.class);
		for (final PluginPreprocessor processor : processors) {
			processor.process(plugin);
		}
	}

	private void postProcess(final IPlugin plugin) {
		final Collection<? extends PluginPostprocessor> processors =
			Lookup.getDefault().lookupAll(PluginPostprocessor.class);
		for (final PluginPostprocessor processor : processors) {
			processor.process(plugin);
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
