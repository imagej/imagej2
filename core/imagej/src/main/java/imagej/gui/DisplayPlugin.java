package imagej.gui;

import imagej.plugin.display.DisplayView;
import imagej.model.Dataset;
import imagej.plugin.BasePlugin;

/**
 * TODO
 *
 * @author Curtis Rueden
 * @author Grant Harris
 */
public interface DisplayPlugin extends BasePlugin {

	// DisplayPlugin extends BasePlugin, so that the name of the interface
	// unambiguously identifies a display plugin.

	/** Tests whether the display plugin can show the given dataset. */
	boolean canDisplay(Dataset dataset);

	/** Displays the given dataset, if possible. */
	void display(Dataset dataset);

	// ++ Added by GBH >>>
	
	void addView(DisplayView view);

	void removeView(DisplayView view);

	void removeAllViews();

	void getViews();

	void getView(int n);

	void getActiveView(); 	// returning getView(0) for now

	void pan(float x, float y);

	// <<<
}
