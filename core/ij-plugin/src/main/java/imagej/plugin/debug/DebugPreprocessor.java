package imagej.plugin.debug;

import imagej.Log;
import imagej.plugin.PluginHandler;
import imagej.plugin.process.PluginPreprocessor;
import imagej.plugin.process.Preprocessor;

/** A plugin preprocessor that dumps information to the log. */
@Preprocessor
public class DebugPreprocessor implements PluginPreprocessor {

	@Override
	public void process(PluginHandler pluginHandler) {
		Log.debug("Executing plugin: " + pluginHandler.getPlugin());
	}

}
