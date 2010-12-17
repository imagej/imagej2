package imagej.plugin;

public interface PluginRunner {

	/**
	 * Executes the given plugin.
	 *
	 * @throws PluginException if the plugin cannot be executed. 
	 */
	void runPlugin(PluginEntry plugin) throws PluginException;

}
