package imagej.plugin.ij2;

import imagej.Log;

import org.openide.util.lookup.ServiceProvider;

/** A plugin preprocessor that dumps information to the log. */
@ServiceProvider(service=PluginPreprocessor.class)
public class DebugPreprocessor implements PluginPreprocessor {

	@Override
	public void process(IPlugin plugin) {
		Log.debug("Executing plugin: " + plugin);
	}

}
