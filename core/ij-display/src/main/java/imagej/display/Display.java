package imagej.display;

import imagej.model.Dataset;
import imagej.plugin.BasePlugin;

/**
 * TODO
 *
 * @author Grant Harris
 * @author Curtis Rueden
 */
public interface Display extends BasePlugin {

	// DisplayPlugin extends BasePlugin, so that the name of the interface
	// unambiguously identifies a display plugin.

	boolean canDisplay(Dataset dataset);

	void display(Dataset dataset);

	void pan(float x, float y);

	void zoom(float factor);

}
