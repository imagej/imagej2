package imagej.plugin.api;

import imagej.Log;
import imagej.plugin.IPlugin;
import imagej.plugin.PluginHandler;
import imagej.plugin.spi.PluginPostprocessor;
import imagej.plugin.spi.PluginPreprocessor;

import java.util.Collection;

import org.openide.util.Lookup;

/** Executes an ImageJ plugin. */
public class PluginRunner {

	private PluginEntry entry;

	public PluginRunner(final PluginEntry entry) {
		this.entry = entry;
	}

	public IPlugin run() {
		final PluginHandler pluginHandler;
		try {
			pluginHandler = new PluginHandler(entry);
		}
		catch (PluginException e) {
			Log.debug(e);
			return null;
		}
		final IPlugin plugin = pluginHandler.getPlugin();

		// execute plugin
		preProcess(pluginHandler);
		plugin.run();
		postProcess(pluginHandler);

		return plugin;
	}

	public void preProcess(final PluginHandler pluginHandler) {
		final Collection<? extends PluginPreprocessor> processors =
			Lookup.getDefault().lookupAll(PluginPreprocessor.class);
		for (final PluginPreprocessor processor : processors) {
			processor.process(pluginHandler);
		}
	}

	public void postProcess(final PluginHandler pluginHandler) {
		final Collection<? extends PluginPostprocessor> processors =
			Lookup.getDefault().lookupAll(PluginPostprocessor.class);
		for (final PluginPostprocessor processor : processors) {
			processor.process(pluginHandler);
		}
	}

}
