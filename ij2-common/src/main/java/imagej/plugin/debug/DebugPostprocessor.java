package imagej.plugin.debug;

import imagej.Log;
import imagej.plugin.IPlugin;
import imagej.plugin.ParameterHandler;
import imagej.plugin.api.PluginException;
import imagej.plugin.spi.PluginPostprocessor;

import java.util.Map;

import org.openide.util.lookup.ServiceProvider;

/** A plugin postprocessor that dumps parameter values to the log. */
@ServiceProvider(service=PluginPostprocessor.class)
public class DebugPostprocessor implements PluginPostprocessor {

	@Override
	public void process(IPlugin plugin) {
		// dump input values to log
		Log.debug("INPUTS:");
		try {
			final Map<String, Object> inputs = ParameterHandler.getInputMap(plugin);
			for (String key : inputs.keySet()) {
				Log.debug("\t" + key + " = " + inputs.get(key));
			}
		}
		catch (PluginException e) {
			Log.printStackTrace(e);
		}
		// dump output values to log
		Log.debug("OUTPUTS:");
		try {
			final Map<String, Object> outputs = ParameterHandler.getOutputMap(plugin);
			for (String key : outputs.keySet()) {
				Log.debug("\t" + key + " = " + outputs.get(key));
			}
		}
		catch (PluginException e) {
			Log.printStackTrace(e);
		}
	}

}
