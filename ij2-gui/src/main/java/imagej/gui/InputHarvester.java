package imagej.gui;

import imagej.plugin.ij2.IPlugin;

public interface InputHarvester {

	InputPanel createInputPanel();

	void buildPanel(InputPanel inputPanel, IPlugin plugin);

	boolean showDialog(InputPanel inputPanel, IPlugin plugin);

	void harvestResults(InputPanel inputPanel, IPlugin plugin);

}
