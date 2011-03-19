package imagej.gui.swing.display;

import imagej.display.Coords;
import imagej.display.EventDispatcher;
import imagej.display.NavigableImageCanvas;
import imagej.event.ImageJEvent;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.image.BufferedImage;
import javax.swing.JPanel;

/**
 *
 * @author GBH
 */
public class ImageCanvasSwing extends JPanel implements NavigableImageCanvas {

	private BufferedImage image;

	@Override
	public void setImage(BufferedImage image) {
		BufferedImage oldImage = this.image;
		this.image = image;
		//Reset scale so that initializeParameters() is called in paintComponent()
		//for the new image.
		firePropertyChange(IMAGE_CHANGED_PROPERTY, oldImage, image);
		repaint();
	}

	@Override
	public BufferedImage getImage() {
		return image;
	}

	@Override
	public void updateImage() {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g); // Paints the background
		if (image == null) {
			return;
		}
		g.drawImage(image, 0, 0, image.getWidth(), image.getHeight(), null);
	}

	@Override
	public void addEventDispatcher(EventDispatcher dispatcher) {
		addKeyListener((AWTEventDispatcher) dispatcher);
		addMouseListener((AWTEventDispatcher) dispatcher);
		addMouseMotionListener((AWTEventDispatcher) dispatcher);
		addMouseWheelListener((AWTEventDispatcher) dispatcher);
	}

	@Override
	public Point getImageOrigin() {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public double getZoom() {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public boolean isHighQualityRenderingEnabled() {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public boolean isNavigationImageEnabled() {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public void pan(int xDelta, int yDelta) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public void setHighQualityRenderingEnabled(boolean enabled) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public void setImageOrigin(int x, int y) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public void setImageOrigin(Point newOrigin) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public void setNavigationImageEnabled(boolean enabled) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public void setZoom(double newZoom) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public void setZoom(double newZoom, Point zoomingCenter) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public void setZoomIncrement(double newZoomIncrement) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public double getZoomIncrement() {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	//=====================================
	// Event Handline

	@Override
	public void subscribeToToolEvents() {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public void setCursor(int cursor) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public Coords panelToImageCoords(Point p) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public Coords imageToPanelCoords(Coords p) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public boolean isInImage(Point p) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

}
