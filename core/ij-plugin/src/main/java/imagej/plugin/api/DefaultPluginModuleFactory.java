package imagej.plugin.api;

import imagej.plugin.BasePlugin;
import imagej.plugin.PluginModule;

public class DefaultPluginModuleFactory<T extends BasePlugin>
	implements PluginModuleFactory<T>
{

	@Override
	public PluginModule<T> createModule(final PluginEntry<T> entry)
		throws PluginException
	{
		return new PluginModule<T>(entry);
	}

}
