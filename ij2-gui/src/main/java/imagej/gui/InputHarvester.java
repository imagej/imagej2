package imagej.gui;

import imagej.plugin.PluginHandler;

public interface InputHarvester {

	InputPanel createInputPanel();

	void buildPanel(InputPanel inputPanel, PluginHandler pluginHandler);

	boolean showDialog(InputPanel inputPanel, PluginHandler pluginHandler);

	void harvestResults(InputPanel inputPanel, PluginHandler pluginHandler);

}
