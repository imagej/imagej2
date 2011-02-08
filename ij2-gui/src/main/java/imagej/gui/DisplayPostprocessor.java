package imagej.gui;

import imagej.dataset.Dataset;
import imagej.plugin.DisplayPlugin;
import imagej.plugin.PluginHandler;
import imagej.plugin.spi.PluginPostprocessor;

import java.util.Collection;
import java.util.Map;

import org.openide.util.Lookup;
import org.openide.util.lookup.ServiceProvider;

@ServiceProvider(service=PluginPostprocessor.class)
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

			// display dataset using the first compatible DisplayPlugin
			// TODO: prompt user with dialog box if multiple matches
			// TODO: use SezPoz instead of Lookup for DisplayPlugins?
			final Collection<? extends DisplayPlugin> displayPlugins =
				Lookup.getDefault().lookupAll(DisplayPlugin.class);
			for (final DisplayPlugin displayPlugin : displayPlugins) {
				if (displayPlugin.canDisplay(dataset)) {
					displayPlugin.display(dataset);
					break;
				}
			}
		}
		else {
			// ignore non-Dataset output
		}
	}

}
