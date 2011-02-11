package imagej.plugin.api;

import imagej.plugin.PluginHandler;


public interface PluginHandlerFactory {

	/** Wraps the given plugin instance in a plugin handler. */
	PluginHandler createPluginHandler(PluginEntry entry) throws PluginException;

}
