package imagej.gui;

import imagej.Log;
import imagej.dataset.Dataset;
import imagej.plugin.PluginHandler;
import imagej.plugin.process.PluginPostprocessor;
import imagej.plugin.process.Postprocessor;

import java.util.Collection;
import java.util.Map;

import net.java.sezpoz.Index;
import net.java.sezpoz.IndexItem;

@Postprocessor
public class DisplayPostprocessor implements PluginPostprocessor {

	@Override
	public void process(PluginHandler pluginHandler) {
		final Map<String, Object> outputs = pluginHandler.getOutputMap();
		handleOutput(outputs.values());
	}

	/** Displays output datasets. */
	public void handleOutput(Object value) {
		if (value instanceof Collection) {
			final Collection<?> collection = (Collection<?>) value;
			for (final Object item : collection) handleOutput(item);
		}
		else if (value instanceof Dataset) {
			final Dataset dataset = (Dataset) value;

			// use SezPoz to discover all display plugins
			for (final IndexItem<DisplayPlugin, IDisplayPlugin> item :
				Index.load(DisplayPlugin.class, IDisplayPlugin.class))
			{
				try {
					final IDisplayPlugin displayPlugin = item.instance();
					// display dataset using the first compatible DisplayPlugin
					// TODO: prompt user with dialog box if multiple matches
					if (displayPlugin.canDisplay(dataset)) {
						displayPlugin.run();
						break;
					}
				}
				catch (InstantiationException e) {
					Log.error(e);
				}
			}
		}
		else {
			// ignore non-Dataset output
		}
	}

}
