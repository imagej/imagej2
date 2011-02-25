package imagej.plugin.process;

import imagej.plugin.BasePlugin;
import imagej.plugin.PluginModule;

/**
 * TODO
 *
 * @author Curtis Rueden
 */
public interface PluginProcessor extends BasePlugin {

	void process(PluginModule<?> module);

}
