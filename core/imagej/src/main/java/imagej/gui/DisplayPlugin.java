package imagej.gui;

import imagej.dataset.Dataset;
import imagej.plugin.BasePlugin;

public interface DisplayPlugin extends BasePlugin {

	// DisplayPlugin extends BasePlugin, so that the name of the interface
	// unambiguously identifies a display plugin.

	/** Tests whether the display plugin can show the given dataset. */
	boolean canDisplay(Dataset dataset);

	/** Displays the given dataset, if possible. */
	void display(Dataset dataset);

}
