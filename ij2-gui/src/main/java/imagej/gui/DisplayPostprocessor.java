package imagej.gui;

import imagej.Log;
import imagej.dataset.Dataset;
import imagej.plugin.PluginException;
import imagej.plugin.ij2.DisplayPlugin;
import imagej.plugin.ij2.IPlugin;
import imagej.plugin.ij2.ParameterHandler;
import imagej.plugin.ij2.PluginPostprocessor;

import java.util.Collection;
import java.util.Map;

import org.openide.util.Lookup;
import org.openide.util.lookup.ServiceProvider;

@ServiceProvider(service=PluginPostprocessor.class)
public class DisplayPostprocessor implements PluginPostprocessor {

	@Override
	public void process(IPlugin plugin) {
		try {
			final Map<String, Object> outputs = ParameterHandler.getOutputMap(plugin);

			for (String key : outputs.keySet()) {
				// display output datasets
				final Object value = outputs.get(key);
				if (value instanceof Dataset) {
					final Dataset dataset = (Dataset) value;
	
					// display dataset using the first compatible DisplayPlugin
					// TODO: prompt user with dialog box if multiple matches
					// TODO: use SezPoz instead of Lookup for DisplayPlugins?
					final Collection<? extends DisplayPlugin> displayPlugins =
						Lookup.getDefault().lookupAll(DisplayPlugin.class);
					for (final DisplayPlugin displayPlugin : displayPlugins) {
						if (displayPlugin.canDisplay(dataset)) {
							displayPlugin.display(dataset);
							return;
						}
					}
				}
			}
		}
		catch (PluginException e) {
			Log.printStackTrace(e);
		}
	}

}
