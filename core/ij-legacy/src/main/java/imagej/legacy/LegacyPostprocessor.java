package imagej.legacy;

import imagej.dataset.Dataset;
import imagej.plugin.PluginHandler;
import imagej.plugin.process.PluginPostprocessor;
import imagej.plugin.process.Postprocessor;

import java.util.Map;

/** A plugin postprocessor that keeps datasets synced with legacy images. */
@Postprocessor
public class LegacyPostprocessor implements PluginPostprocessor {

	@Override
	public void process(PluginHandler pluginHandler) {
		final Map<String, Object> outputs = pluginHandler.getOutputMap();
		for (String key : outputs.keySet()) {
			// register output datasets with the legacy image map
			final Object value = outputs.get(key);
			if (value instanceof Dataset) {
				final Dataset dataset = (Dataset) value;
				LegacyManager.getImageMap().registerDataset(dataset);
			}
		}
	}

}
