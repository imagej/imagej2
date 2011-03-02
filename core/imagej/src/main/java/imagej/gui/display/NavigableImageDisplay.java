package imagej.gui.display;

import imagej.Log;
import imagej.display.Display;
import imagej.display.DisplayView;
import imagej.display.LayeredDisplay;
import imagej.model.Dataset;
import imagej.plugin.Plugin;

/**
 * TODO
 *
 * @author Curtis Rueden
 * @author Grant Harris
 */
@Plugin(type = Display.class)
public class NavigableImageDisplay extends AbstractAWTDisplay
	implements LayeredDisplay
{

	private NavigableImageFrame imageFrame;

	@Override
	public boolean canDisplay(Dataset dataset) {
		return true;
	}

	@Override
	public void display(Dataset dataset) {
		imageFrame = new NavigableImageFrame();

		// listen for user input
		imageFrame.getPanel().addKeyListener(this);
		imageFrame.getPanel().addMouseListener(this);
		imageFrame.getPanel().addMouseMotionListener(this);
		imageFrame.getPanel().addMouseWheelListener(this);
		imageFrame.addWindowListener(this);

		// TODO - use DisplayView instead of Dataset directly
		imageFrame.setDataset(dataset);
		imageFrame.setVisible(true);
	}

	@Override
	public void pan(float x, float y) {
		imageFrame.getPanel().pan((int) x, (int) y);
	}

	@Override
	public void zoom(float factor) {
		// TODO
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
	public DisplayView[] getViews() {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public DisplayView getView(int n) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public DisplayView getActiveView() {
		return getView(0);
	}

}
