package imagej.gui.display;

import imagej.gui.DisplayPlugin;
import imagej.plugin.display.DisplayView;
import imagej.model.Dataset;
import imagej.plugin.Plugin;

/**
 * TODO
 *
 * @author Curtis Rueden
 * @author Grant Harris
 */
@Plugin(type = DisplayPlugin.class)
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

	@Override
	public void addView(DisplayView view) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public void removeView(DisplayView view) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public void removeAllViews() {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public void getViews() {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public void getView(int n) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public void getActiveView() {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public void pan(float x, float y) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

}
