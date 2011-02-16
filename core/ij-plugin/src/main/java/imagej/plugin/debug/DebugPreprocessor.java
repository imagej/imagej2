package imagej.plugin.debug;

import imagej.Log;
import imagej.plugin.Plugin;
import imagej.plugin.PluginModule;
import imagej.plugin.process.PluginPreprocessor;

/** A plugin preprocessor that dumps information to the log. */
@Plugin(type = PluginPreprocessor.class)
public class DebugPreprocessor implements PluginPreprocessor {

	@Override
	public void process(final PluginModule<?> module) {
		Log.debug("Executing plugin: " + module.getPlugin());
	}

	@Override
	public boolean canceled() { return false; }

}
