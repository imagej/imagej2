package imagej.gui;

import imagej.plugin.IPlugin;

public interface InputHarvester {

	InputPanel createInputPanel();

	void buildPanel(InputPanel inputPanel, IPlugin plugin);

	boolean showDialog(InputPanel inputPanel, IPlugin plugin);

	void harvestResults(InputPanel inputPanel, IPlugin plugin);

}
