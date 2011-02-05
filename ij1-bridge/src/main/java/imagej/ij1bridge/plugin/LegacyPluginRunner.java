package imagej.ij1bridge.plugin;

import ij.IJ;
import ij.plugin.PlugIn;
import ij.plugin.filter.PlugInFilter;
import imagej.plugin.api.PluginEntry;
import imagej.plugin.api.PluginException;
import imagej.plugin.spi.PluginRunner;

import org.openide.util.lookup.ServiceProvider;

/** Executes an IJ1 plugin. */
@ServiceProvider(service=PluginRunner.class)
public class LegacyPluginRunner implements PluginRunner {

	@Override
	public Object runPlugin(PluginEntry entry) throws PluginException {
		final Object pluginInstance =
			IJ.runPlugIn(entry.getPluginClass(), entry.getArg());

		if (!(pluginInstance instanceof PlugIn) &&
			!(pluginInstance instanceof PlugInFilter))
		{
			throw new PluginException("Not a legacy plugin");
		}
		return pluginInstance;
	}

}
