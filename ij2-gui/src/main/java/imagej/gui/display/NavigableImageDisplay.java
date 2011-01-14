package imagej.gui.display;

import imagej.dataset.Dataset;
import imagej.plugin.ij2.DisplayPlugin;

import org.openide.util.lookup.ServiceProvider;

@ServiceProvider(service=DisplayPlugin.class)
public class NavigableImageDisplay implements DisplayPlugin {

	@Override
	public boolean canDisplay(Dataset dataset) {
		return true;
	}

	@Override
	public void display(Dataset dataset) {
		final NavigableImageFrame imageFrame = new NavigableImageFrame();
		imageFrame.setDataset(dataset);
		imageFrame.setVisible(true);
	}

}
