package imagej.plugin.api;

import imagej.Log;
import imagej.plugin.IPlugin;
import imagej.plugin.PluginHandler;
import imagej.plugin.process.PluginPostprocessor;
import imagej.plugin.process.PluginPreprocessor;
import imagej.plugin.process.Postprocessor;
import imagej.plugin.process.Preprocessor;
import net.java.sezpoz.Index;
import net.java.sezpoz.IndexItem;

/** Executes an ImageJ plugin. */
public class PluginRunner {

	private PluginEntry entry;

	public PluginRunner(final PluginEntry entry) {
		this.entry = entry;
	}

	public IPlugin run() {
		final PluginHandler handler;
		try {
			handler = entry.createPluginHandler();
		}
		catch (PluginException e) {
			Log.error(e);
			return null;
		}
		final IPlugin plugin = handler.getPlugin();

		// execute plugin
		preProcess(handler);
		plugin.run();
		postProcess(handler);

		return plugin;
	}

	public void preProcess(final PluginHandler pluginHandler) {
		// use SezPoz to discover all plugin preprocessors
		for (final IndexItem<Preprocessor, PluginPreprocessor> item :
			Index.load(Preprocessor.class, PluginPreprocessor.class))
		{
			try {
				final PluginPreprocessor processor = item.instance();
				processor.process(pluginHandler);
			}
			catch (InstantiationException e) {
				Log.error(e);
			}
		}
	}

	public void postProcess(final PluginHandler pluginHandler) {
		// use SezPoz to discover all plugin postprocessors
		for (final IndexItem<Postprocessor, PluginPostprocessor> item :
			Index.load(Postprocessor.class, PluginPostprocessor.class))
		{
			try {
				final PluginPostprocessor processor = item.instance();
				processor.process(pluginHandler);
			}
			catch (InstantiationException e) {
				Log.error(e);
			}
		}
	}

}
