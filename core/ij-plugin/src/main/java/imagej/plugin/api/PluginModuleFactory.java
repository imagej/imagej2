package imagej.plugin.api;

import imagej.plugin.BasePlugin;
import imagej.plugin.PluginModule;

public interface PluginModuleFactory<T extends BasePlugin> {

	/** Constructs a module to work with the given plugin entry. */
	PluginModule<T> createModule(PluginEntry<T> entry)
		throws PluginException;

}
