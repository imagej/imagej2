package imagej.gui.display;

import imagej.dataset.Dataset;
import imagej.gui.DisplayPlugin;
import imagej.gui.IDisplayPlugin;

@DisplayPlugin
public class NavigableImageDisplay implements IDisplayPlugin {

	private Dataset dataset;

	@Override
	public boolean canDisplay(Dataset d) {
		dataset = d;
		return true;
	}

	@Override
	public void run() {
		final NavigableImageFrame imageFrame = new NavigableImageFrame();
		imageFrame.setDataset(dataset);
		imageFrame.setVisible(true);
	}

}
