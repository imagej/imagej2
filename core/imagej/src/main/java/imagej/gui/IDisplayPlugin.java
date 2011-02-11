package imagej.gui;

import imagej.dataset.Dataset;
import imagej.plugin.IPlugin;

public interface IDisplayPlugin extends IPlugin {

	/**
	 * Tests whether the display plugin can show the given dataset.
	 * If true, a subsequent call to {@link #run()} is expected to do so.
	 */
	boolean canDisplay(Dataset dataset);

}
