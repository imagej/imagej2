package imagej.plugin.debug;

import imagej.Log;
import imagej.plugin.IPlugin;
import imagej.plugin.spi.PluginPreprocessor;

import org.openide.util.lookup.ServiceProvider;

/** A plugin preprocessor that dumps information to the log. */
@ServiceProvider(service=PluginPreprocessor.class)
public class DebugPreprocessor implements PluginPreprocessor {

	@Override
	public void process(IPlugin plugin) {
		Log.debug("Executing plugin: " + plugin);
	}

}
