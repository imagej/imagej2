package imagej.gui;

import imagej.dataset.Dataset;

public interface DisplayPlugin {

	boolean canDisplay(Dataset dataset);

	void display(Dataset dataset);

}
