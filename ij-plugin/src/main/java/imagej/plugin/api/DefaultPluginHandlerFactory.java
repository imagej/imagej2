package imagej.plugin.api;

import imagej.plugin.IPlugin;
import imagej.plugin.PluginHandler;
import imagej.plugin.spi.PluginHandlerFactory;

public class DefaultPluginHandlerFactory implements PluginHandlerFactory {

	@Override
	public PluginHandler createPluginHandler(IPlugin plugin)
		throws PluginException
	{
		return new PluginHandler(plugin);
	}

}
