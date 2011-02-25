package imagej.plugin.display;

import imagej.model.Dataset;

/**
 *
 * @author GBH
 */
public interface Display {

	boolean canDisplay(Dataset dataset);

	void display(Dataset dataset);

	void pan(float x, float y);

	void zoom(float factor);
}
