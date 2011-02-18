package imagej.legacy;

import imagej.model.Dataset;
import imagej.plugin.Plugin;
import imagej.plugin.PluginModule;
import imagej.plugin.process.PluginPostprocessor;

import java.util.Map;

/** A plugin postprocessor that keeps datasets synced with legacy images. */
@Plugin(type = PluginPostprocessor.class)
public class LegacyPostprocessor implements PluginPostprocessor {

	@Override
	public void process(final PluginModule<?> module) {
		final Map<String, Object> outputs = module.getOutputs();
		for (final String name : outputs.keySet()) {
			// register output datasets with the legacy image map
			final Object value = outputs.get(name);
			if (value instanceof Dataset) {
				final Dataset dataset = (Dataset) value;
				LegacyManager.getImageMap().registerDataset(dataset);
			}
		}
	}

}
