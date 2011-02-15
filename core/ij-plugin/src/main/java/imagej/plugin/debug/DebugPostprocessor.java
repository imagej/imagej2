package imagej.plugin.debug;

import imagej.Log;
import imagej.plugin.Plugin;
import imagej.plugin.PluginModule;
import imagej.plugin.process.PluginPostprocessor;

import java.util.Map;

/** A plugin postprocessor that dumps parameter values to the log. */
@Plugin(type = PluginPostprocessor.class)
public class DebugPostprocessor implements PluginPostprocessor {

	@Override
	public void process(final PluginModule<?> module) {
		// dump input values to log
		Log.debug("INPUTS:");
		final Map<String, Object> inputs = module.getInputs();
		for (String key : inputs.keySet()) {
			Log.debug("\t" + key + " = " + inputs.get(key));
		}

		// dump output values to log
		Log.debug("OUTPUTS:");
		final Map<String, Object> outputs = module.getOutputs();
		for (String key : outputs.keySet()) {
			Log.debug("\t" + key + " = " + outputs.get(key));
		}
	}

}
