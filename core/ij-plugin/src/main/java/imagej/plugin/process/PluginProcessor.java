package imagej.plugin.process;

import imagej.plugin.BasePlugin;
import imagej.plugin.PluginModule;

public interface PluginProcessor extends BasePlugin {

	void process(PluginModule<?> module);

}
