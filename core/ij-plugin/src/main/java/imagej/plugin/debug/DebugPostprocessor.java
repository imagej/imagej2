package imagej.plugin.debug;

import imagej.Log;
import imagej.plugin.PluginHandler;
import imagej.plugin.process.PluginPostprocessor;
import imagej.plugin.process.Postprocessor;

import java.util.Map;

/** A plugin postprocessor that dumps parameter values to the log. */
@Postprocessor
public class DebugPostprocessor implements PluginPostprocessor {

	@Override
	public void process(PluginHandler pluginHandler) {
		// dump input values to log
		Log.debug("INPUTS:");
		final Map<String, Object> inputs = pluginHandler.getInputMap();
		for (String key : inputs.keySet()) {
			Log.debug("\t" + key + " = " + inputs.get(key));
		}

		// dump output values to log
		Log.debug("OUTPUTS:");
		final Map<String, Object> outputs = pluginHandler.getOutputMap();
		for (String key : outputs.keySet()) {
			Log.debug("\t" + key + " = " + outputs.get(key));
		}
	}

}
