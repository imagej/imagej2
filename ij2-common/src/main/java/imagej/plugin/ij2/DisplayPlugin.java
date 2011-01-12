package imagej.plugin.ij2;

import imagej.dataset.Dataset;

public interface DisplayPlugin {

	boolean canDisplay(Dataset dataset);

	void display(Dataset dataset);

}
