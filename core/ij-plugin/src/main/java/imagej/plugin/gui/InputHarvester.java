package imagej.plugin.gui;

import imagej.plugin.PluginModule;

// TODO - Use Module instead of PluginModule<?>, and move to imagej.module.gui.

public interface InputHarvester {

	InputPanel createInputPanel();

	void buildPanel(InputPanel inputPanel, PluginModule<?> module);

	boolean showDialog(InputPanel inputPanel, PluginModule<?> module);

	void harvestResults(InputPanel inputPanel, PluginModule<?> module);

}
