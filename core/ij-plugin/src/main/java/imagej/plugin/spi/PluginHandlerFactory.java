package imagej.plugin.spi;

import imagej.plugin.PluginHandler;
import imagej.plugin.api.PluginEntry;
import imagej.plugin.api.PluginException;

public interface PluginHandlerFactory {

	/** Wraps the given plugin instance in a plugin handler. */
	PluginHandler createPluginHandler(PluginEntry entry) throws PluginException;

}
