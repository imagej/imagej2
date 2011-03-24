//
// NavigableImagePanel.java
//

/*
ImageJ software for multidimensional image processing and analysis.

Copyright (c) 2010, ImageJDev.org.
All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met:
    * Redistributions of source code must retain the above copyright
      notice, this list of conditions and the following disclaimer.
    * Redistributions in binary form must reproduce the above copyright
      notice, this list of conditions and the following disclaimer in the
      documentation and/or other materials provided with the distribution.
    * Neither the names of the ImageJDev.org developers nor the
      names of its contributors may be used to endorse or promote products
      derived from this software without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
POSSIBILITY OF SUCH DAMAGE.
*/

package imagej.gui.swing.display;

import imagej.IntCoords;
import imagej.RealCoords;
import imagej.display.EventDispatcher;
import imagej.display.MouseCursor;
import imagej.display.NavigableImageCanvas;
import imagej.event.EventSubscriber;
import imagej.event.Events;
import imagej.tool.event.ToolActivatedEvent;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Transparency;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

/**
 * <p>
 * <code>NavigableImagePanel</code> is a lightweight container displaying an
 * image that can be zoomed in and out and panned with ease and simplicity,
 * using an adaptive rendering for high quality display and satisfactory
 * performance.
 * </p>
 * <p>
 * <h3>Image</h3>
 * <p>
 * An image is loaded either via a constructor:
 * </p>
 * 
 * <pre>
 * 
 * 
 * 
 * 
 * 
 * 
 * NavigableImagePanel panel = new NavigableImagePanel(image);
 * </pre>
 * 
 * or using a setter:
 * 
 * <pre>
 * NavigableImagePanel panel = new NavigableImagePanel();
 * panel.setImage(image);
 * </pre>
 * 
 * When an image is set, it is initially painted centered in the component, at
 * the largest possible size, fully visible, with its aspect ratio is preserved.
 * This is defined as 100% of the image size and its corresponding zoom level is
 * 1.0. </p> <h3>Zooming</h3>
 * <p>
 * Zooming can be controlled interactively, using either the mouse scroll wheel
 * (default) or the mouse two buttons, or programmatically, allowing the
 * programmer to implement other custom zooming methods. If the mouse does not
 * have a scroll wheel, set the zooming device to mouse buttons:
 * 
 * <pre>
 * panel.setZoomDevice(ZoomDevice.MOUSE_BUTTON);
 * </pre>
 * 
 * The left mouse button works as a toggle switch between zooming in and zooming
 * out modes, and the right button zooms an image by one increment (default is
 * 20%). You can change the zoom increment value by:
 * 
 * <pre>
 * panel.setZoomIncrement(newZoomIncrement);
 * </pre>
 * 
 * If you intend to provide programmatic zoom control, set the zoom device to
 * none to disable both the mouse wheel and buttons for zooming purposes:
 * 
 * <pre>
 * panel.setZoomDevice(ZoomDevice.NONE);
 * </pre>
 * 
 * and use <code>setZoom()</code> to change the zoom level.
 * </p>
 * <p>
 * Zooming is always around the point the mouse pointer is currently at, so that
 * this point (called a zooming center) remains stationary ensuring that the
 * area of an image we are zooming into does not disappear off the screen. The
 * zooming center stays at the same location on the screen and all other points
 * move radially away from it (when zooming in), or towards it (when zooming
 * out). For programmatically controlled zooming the zooming center is either
 * specified when <code>setZoom()</code> is called:
 * 
 * <pre>
 * panel.setZoom(newZoomLevel, newZoomingCenter);
 * </pre>
 * 
 * or assumed to be the point of an image which is the closest to the center of
 * the panel, if no zooming center is specified:
 * 
 * <pre>
 * panel.setZoom(newZoomLevel);
 * </pre>
 * 
 * </p>
 * <p>
 * There are no lower or upper zoom level limits.
 * </p>
 * <h3>Navigation</h3>
 * <p>
 * <code>NavigableImagePanel</code> does not use scroll bars for navigation, but
 * relies on a navigation image located in the upper left corner of the panel.
 * The navigation image is a small replica of the image displayed in the panel.
 * When you click on any point of the navigation image that part of the image is
 * displayed in the panel, centered. The navigation image can also be zoomed in
 * the same way as the main image.
 * </p>
 * <p>
 * In order to adjust the position of an image in the panel, it can be dragged
 * with the mouse, using the left button.
 * </p>
 * <p>
 * For programmatic image navigation, disable the navigation image:
 * 
 * <pre>
 * panel.setNavigationImageEnabled(false)
 * </pre>
 * 
 * and use <code>getImageOrigin()</code> and <code>setImageOrigin()</code> to
 * move the image around the panel.
 * </p>
 * <h3>Rendering</h3>
 * <p>
 * <code>NavigableImagePanel</code> uses the Nearest Neighbor interpolation for
 * image rendering (default in Java). When the scaled image becomes larger than
 * the original image, the Bilinear interpolation is applied, but only to the
 * part of the image which is displayed in the panel. This interpolation change
 * threshold can be controlled by adjusting the value of
 * <code>HIGH_QUALITY_RENDERING_SCALE_THRESHOLD</code>.
 * </p>
 * 
 * @see <a
 *      href="http://today.java.net/article/2007/03/23/navigable-image-panel">A
 *      Navigable Image Panel</a>
 * @author Slav Boleslawski
 * @author Romain Guy
 */

/*
 * Feb/Mar 2011
 * Curtis & Grant added Pan controlled by Pan Tool
 * GBH: 
 * TODO: Break out the NavigationImage from the panel
 * TODO: Change firePropertyChanges to emit Events on EventBus.
 * Rearranged code into sections: ZoomDevice, NavigationImage, ...
 *
 * Extracted an interface....
 *
 * Also see: Romain Guy's ZoomableImagePanel was my starting point to write NavigableImagePanel,
 * http://weblogs.java.net/blog/gfx/archive/2005/09/zoom_pictures_w.html
 */
public class NavigableImagePanel extends JPanel implements
	NavigableImageCanvas
{

	// Rendering...

	private static final double HIGH_QUALITY_RENDERING_SCALE_THRESHOLD = 1.0;
	private static final Object INTERPOLATION_TYPE =
		RenderingHints.VALUE_INTERPOLATION_BILINEAR;
	private boolean highQualityRenderingEnabled = true;
	private double zoomFactor = 1.0 + getZoomIncrement();
	private BufferedImage image;
	private double initialScale = 0.0;
	private double scale = 0.0;
	private int originX = 0;
	private int originY = 0;
	private Point mousePosition;
	private Dimension previousPanelSize;

	/**
	 * <p>
	 * Creates a new navigable image panel with no default image and the mouse
	 * scroll wheel as the zooming device.
	 * </p>
	 */
	public NavigableImagePanel() {
		setOpaque(false);
		addResizeListener();
		addMouseListeners();
		setZoomDevice(ZoomDevice.MOUSE_WHEEL);
		// setZoomDevice(ZoomDevice.MOUSE_BUTTON);
	}

	/**
	 * <p>
	 * Creates a new navigable image panel with the specified image and the mouse
	 * scroll wheel as the zooming device.
	 * </p>
	 */
	public NavigableImagePanel(final BufferedImage image) {
		this();
		setImage(image);
	}

	private void addMouseListeners() {
		addMouseListener(new MouseAdapter() {

			@SuppressWarnings("synthetic-access")
			@Override
			public void mousePressed(final MouseEvent e) {
				if (SwingUtilities.isLeftMouseButton(e)) {
					if (isInNavigationImage(e.getPoint())) {
						final Point p = e.getPoint();
						displayImageAt(ptToCoords(p));
					}
				}
			}

		});

		addMouseMotionListener(new MouseMotionListener() {

			@SuppressWarnings("synthetic-access")
			@Override
			public void mouseDragged(final MouseEvent e) {
				if (SwingUtilities.isLeftMouseButton(e) &&
					!isInNavigationImage(e.getPoint()))
				{
					// TODO - clean up this section, to fully remove pan support
					// in favor of the pan tool way of doing things
//	  				Point p = e.getPoint();
//	  				moveImage(p);
				}
			}

			@SuppressWarnings("synthetic-access")
			@Override
			public void mouseMoved(final MouseEvent e) {
				// we need the mouse position so that after zooming
				// that position of the image is maintained
				mousePosition = e.getPoint();
//				Coords coord = panelToImageCoords(mousePosition);
//
//				// Display current pixel value in StatusBar
//				//sampleImage.getRaster().getDataBuffer(). .getPixel(coord.getIntX(), coord.getIntY(), dArray)
//				if (isInImage(mousePosition)) {
//					int value = image.getRGB(coord.getIntX(), coord.getIntY());
//					Events.publish(new StatusEvent("Pixel: (" + coord.getIntX() + "," + coord.getIntY() + ") "
//							+ pixelARGBtoString(value)));
//				}
			}

		});
	}

	public String pixelARGBtoString(final int pixel) {
		final int alpha = (pixel >> 24) & 0xff;
		final int red = (pixel >> 16) & 0xff;
		final int green = (pixel >> 8) & 0xff;
		final int blue = (pixel) & 0xff;
		return "" + alpha + ", " + red + ", " + green + ", " + blue;
	}

	// Called from paintComponent() when a new image is set.
	private void initializeParams() {
		final double xScale = (double) getWidth() / image.getWidth();
		final double yScale = (double) getHeight() / image.getHeight();
		initialScale = Math.min(xScale, yScale);
		scale = initialScale;

		// An image is initially centered
		centerImage();

		if (isNavigationImageEnabled()) {
			createNavigationImage();
		}
		// setSize(image.getWidth(), image.getHeight());
	}

	@Override
	public int getImageWidth() {
		return image.getWidth();
	}

	@Override
	public int getImageHeight() {
		return image.getHeight();
	}

	/**
	 * <p>
	 * Sets an image for display in the panel.
	 * </p>
	 * 
	 * @param image an image to be set in the panel
	 */
	public void setImage(final BufferedImage newImage) {
		final BufferedImage oldImage = this.image;
		this.image = toCompatibleImage(newImage);
		setPreferredSize(new Dimension(this.image.getWidth(), this.image.getHeight()));
		NavigableImageFrame frame = (NavigableImageFrame) getTopLevelAncestor(); 
		frame.pack();
		// Reset scale so that initializeParameters() is called in paintComponent()
		// for the new image.
		scale = 0.0;
		firePropertyChange(IMAGE_CHANGED_PROPERTY, oldImage, this.image);
		// old way that may have been wrong
		//firePropertyChange(IMAGE_CHANGED_PROPERTY, oldImage, newImage);
		repaint();
	}

	public BufferedImage getImage() {
		return image;
	}

	/**
	 * <p>
	 * Tests whether an image uses the standard RGB color space.
	 * </p>
	 */
	public static boolean isStandardRGBImage(final BufferedImage bImage) {
		return bImage.getColorModel().getColorSpace().isCS_sRGB();
	}

	private static BufferedImage toCompatibleImage(final BufferedImage image) {
		if (image.getColorModel().equals(CONFIGURATION.getColorModel())) {
			return image;
		}
		final BufferedImage compatibleImage =
			CONFIGURATION.createCompatibleImage(image.getWidth(), image.getHeight(),
				image.getTransparency());
		final Graphics g = compatibleImage.getGraphics();
		g.drawImage(image, 0, 0, null);
		g.dispose();
		return compatibleImage;
	}

	private static final GraphicsConfiguration CONFIGURATION =
		GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice()
			.getDefaultConfiguration();

	private int getScreenImageWidth() {
		return (int) (scale * image.getWidth());
	}

	private int getScreenImageHeight() {
		return (int) (scale * image.getHeight());
	}

// <editor-fold defaultstate="collapsed" desc=" <<< Position / Panning >>> ">

	/** Centers the current image in the panel. */
	private void centerImage() {
		originX = (getWidth() - getScreenImageWidth()) / 2;
		originY = (getHeight() - getScreenImageHeight()) / 2;
	}

	/**
	 * <p>
	 * Gets the image origin.
	 * </p>
	 * <p>
	 * Image origin is defined as the upper, left corner of the image in the
	 * panel's coordinate system.
	 * </p>
	 * 
	 * @return the point of the upper, left corner of the image in the panel's
	 *         coordinates system.
	 */
	@Override
	public IntCoords getImageOrigin() {
		return new IntCoords(originX, originY);
	}

	/**
	 * <p>
	 * Sets the image origin.
	 * </p>
	 * <p>
	 * Image origin is defined as the upper, left corner of the image in the
	 * panel's coordinate system. After a new origin is set, the image is
	 * repainted. This method is used for programmatic image navigation.
	 * </p>
	 * 
	 * @param x the x coordinate of the new image origin
	 * @param y the y coordinate of the new image origin
	 */
	@Override
	public void setImageOrigin(final int x, final int y) {
		setImageOrigin(new IntCoords(x, y));
	}

	/**
	 * <p>
	 * Sets the image origin.
	 * </p>
	 * <p>
	 * Image origin is defined as the upper, left corner of the image in the
	 * panel's coordinate system. After a new origin is set, the image is
	 * repainted. This method is used for programmatic image navigation.
	 * </p>
	 * 
	 * @param newOrigin the value of a new image origin
	 */
	@Override
	public void setImageOrigin(final IntCoords newOrigin) {
		originX = newOrigin.x;
		originY = newOrigin.y;
		repaint();
	}

	/** Pans the image by the given (X, Y) amount. */
	@Override
	public void pan(final int xDelta, final int yDelta) {
		originX += xDelta;
		originY += yDelta;
		repaint();
	}

// </editor-fold>

	@Override
	public void updateImage() {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public void addEventDispatcher(final EventDispatcher dispatcher) {
		addKeyListener((AWTEventDispatcher) dispatcher);
		addMouseListener((AWTEventDispatcher) dispatcher);
		addMouseMotionListener((AWTEventDispatcher) dispatcher);
		addMouseWheelListener((AWTEventDispatcher) dispatcher);
	}

	/*
	 * Handles setting of the cursor depending on the activated tool
	 */
	@Override
	public void subscribeToToolEvents() {
		final EventSubscriber<ToolActivatedEvent> toolSubscriber =
			new EventSubscriber<ToolActivatedEvent>() {

				@Override
				public void onEvent(final ToolActivatedEvent event) {
					setCursor(event.getTool().getCursor());
				}
			};
		Events.subscribe(ToolActivatedEvent.class, toolSubscriber);
	}

	@Override
	public void setCursor(final MouseCursor cursor) {
		final int cursorCode = AWTCursorManager.getAWTCursorCode(cursor);
		setCursor(Cursor.getPredefinedCursor(cursorCode));
	}

// <editor-fold defaultstate="collapsed" desc=" <<< Coordinate XForms, Origin >>> ">

	// Converts this panel's coordinates into the original image coordinates
	@Override
	public RealCoords panelToImageCoords(final IntCoords p) {
		return new RealCoords((p.x - originX) / scale, (p.y - originY) / scale);
	}

	// Converts the original image coordinates into this panel's coordinates
	@Override
	public RealCoords imageToPanelCoords(final RealCoords p) {
		return new RealCoords((p.x * scale) + originX, (p.y * scale) + originY);
	}

	// Tests whether a given point in the panel falls within the image boundaries.
	@Override
	public boolean isInImage(final IntCoords p) {
		final RealCoords coords = panelToImageCoords(p);
		final int x = coords.getIntX();
		final int y = coords.getIntY();
		return (x >= 0 && x < image.getWidth() && y >= 0 && y < image.getHeight());
	}

	// Tests whether the image is displayed in its entirety in the panel.
	private boolean isFullImageInPanel() {
		return (originX >= 0 && (originX + getScreenImageWidth()) < getWidth() &&
			originY >= 0 && (originY + getScreenImageHeight()) < getHeight());
	}

// </editor-fold>

// <editor-fold defaultstate="collapsed" desc="<<< Rendering Quality >>>">

	/**
	 * <p>
	 * Indicates whether the high quality rendering feature is enabled.
	 * </p>
	 * 
	 * @return true if high quality rendering is enabled, false otherwise.
	 */
	@Override
	public boolean isHighQualityRenderingEnabled() {
		return highQualityRenderingEnabled;
	}

	/**
	 * <p>
	 * Enables/disables high quality rendering.
	 * </p>
	 * 
	 * @param enabled enables/disables high quality rendering
	 */
	@Override
	public void setHighQualityRenderingEnabled(final boolean enabled) {
		highQualityRenderingEnabled = enabled;
	}

	// High quality rendering kicks in when when a scaled image is larger
	// than the original image. In other words,
	// when image decimation stops and interpolation starts.
	private boolean isHighQualityRendering() {
		return (highQualityRenderingEnabled && scale > HIGH_QUALITY_RENDERING_SCALE_THRESHOLD);
	}

// </editor-fold>

// <editor-fold defaultstate="collapsed" desc=" <<< Paint >>> ">

	/**
	 * Gets the bounds of the image area currently displayed in the panel (in
	 * image coordinates).
	 */
	private Rectangle getImageClipBounds() {
		final RealCoords startCoords = panelToImageCoords(new IntCoords(0, 0));
		final RealCoords endCoords =
			panelToImageCoords(new IntCoords(getWidth() - 1, getHeight() - 1));
		final int panelX1 = startCoords.getIntX();
		final int panelY1 = startCoords.getIntY();
		final int panelX2 = endCoords.getIntX();
		final int panelY2 = endCoords.getIntY();
		// No intersection?
		if (panelX1 >= image.getWidth() || panelX2 < 0 ||
			panelY1 >= image.getHeight() || panelY2 < 0)
		{
			return null;
		}

		final int x1 = (panelX1 < 0) ? 0 : panelX1;
		final int y1 = (panelY1 < 0) ? 0 : panelY1;
		final int x2 =
			(panelX2 >= image.getWidth()) ? image.getWidth() - 1 : panelX2;
		final int y2 =
			(panelY2 >= image.getHeight()) ? image.getHeight() - 1 : panelY2;
		return new Rectangle(x1, y1, x2 - x1 + 1, y2 - y1 + 1);
	}

	/**
	 * Paints the panel and its image at the current zoom level, location, and
	 * interpolation method dependent on the image scale.</p>
	 * 
	 * @param g the <code>Graphics</code> context for painting
	 */
	@Override
	protected void paintComponent(final Graphics g) {
		super.paintComponent(g); // Paints the background
		if (image == null) {
			return;
		}
		if (scale == 0.0) {
			initializeParams();
		}
		if (isHighQualityRendering()) {
			final Rectangle rect = getImageClipBounds();
			if (rect == null || rect.width == 0 || rect.height == 0) { // no part of
																																	// image is
																																	// displayed
																																	// in the
																																	// panel
				return;
			}
			final BufferedImage subimage =
				image.getSubimage(rect.x, rect.y, rect.width, rect.height);
			final Graphics2D g2 = (Graphics2D) g;
			g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, INTERPOLATION_TYPE);
			g2.drawImage(subimage, Math.max(0, originX), Math.max(0, originY), Math
				.min((int) (subimage.getWidth() * scale), getWidth()), Math.min(
				(int) (subimage.getHeight() * scale), getHeight()), null);
		}
		else {
			g.drawImage(image, originX, originY, getScreenImageWidth(),
				getScreenImageHeight(), null);
		}
		drawNavigationImage(g);
	}

// </editor-fold>

// <editor-fold defaultstate="collapsed" desc=" <<< Resizing >>> ">

	private void addResizeListener() {
		// Handle component resizing
		addComponentListener(new ComponentAdapter() {

			@SuppressWarnings("synthetic-access")
			@Override
			public void componentResized(final ComponentEvent e) {
				if (scale > 0.0) {
					if (isFullImageInPanel()) {
						centerImage();
					}
					else if (isImageEdgeInPanel()) {
						scaleOrigin();
					}
					if (isNavigationImageEnabled()) {
						createNavigationImage();
					}
					repaint();
				}
				previousPanelSize = getSize();
			}

		});
	}

	// Used when the image is resized.
	private boolean isImageEdgeInPanel() {
		if (previousPanelSize == null) {
			return false;
		}
		return (originX > 0 && originX < previousPanelSize.width || originY > 0 &&
			originY < previousPanelSize.height);
	}

	// Used when the panel is resized
	private void scaleOrigin() {
		originX = originX * getWidth() / previousPanelSize.width;
		originY = originY * getHeight() / previousPanelSize.height;
		repaint();
	}

// </editor-fold>

// <editor-fold defaultstate="collapsed" desc=" <<< Zooming >>> ">

	/** Converts the specified zoom level to scale. */
	private double zoomToScale(final double zoom) {
		return initialScale * zoom;
	}

	/**
	 * <p>
	 * Gets the current zoom level.
	 * </p>
	 * 
	 * @return the current zoom level
	 */
	@Override
	public double getZoom() {
		return scale / initialScale;
	}

	/**
	 * <p>
	 * Sets the zoom level used to display the image.
	 * </p>
	 * <p>
	 * This method is used in programmatic zooming. The zooming center is the
	 * point of the image closest to the center of the panel. After a new zoom
	 * level is set the image is repainted.
	 * </p>
	 * 
	 * @param newZoom the zoom level used to display this panel's image.
	 */
	@Override
	public void setZoom(final double newZoom) {
		final IntCoords zoomingCenter =
			new IntCoords(getWidth() / 2, getHeight() / 2);
		setZoom(newZoom, zoomingCenter);
	}

	/**
	 * <p>
	 * Sets the zoom level used to display the image, and the zooming center,
	 * around which zooming is done.
	 * </p>
	 * <p>
	 * This method is used in programmatic zooming. After a new zoom level is set
	 * the image is repainted.
	 * </p>
	 * 
	 * @param newZoom the zoom level used to display this panel's image.
	 */
	@Override
	public void setZoom(final double newZoom, final IntCoords zoomingCenter) {
		final RealCoords imageP = panelToImageCoords(zoomingCenter);
		if (imageP.x < 0.0) {
			imageP.x = 0.0;
		}
		if (imageP.y < 0.0) {
			imageP.y = 0.0;
		}
		if (imageP.x >= image.getWidth()) {
			imageP.x = image.getWidth() - 1.0;
		}
		if (imageP.y >= image.getHeight()) {
			imageP.y = image.getHeight() - 1.0;
		}
		final RealCoords correctedP = imageToPanelCoords(imageP);
		final double oldZoom = getZoom();
		scale = zoomToScale(newZoom);
		final RealCoords panelP = imageToPanelCoords(imageP);
		originX += (correctedP.getIntX() - (int) panelP.x);
		originY += (correctedP.getIntY() - (int) panelP.y);
		firePropertyChange(ZOOM_LEVEL_CHANGED_PROPERTY, new Double(oldZoom),
			new Double(getZoom()));
		repaint();
	}

	// TODO - allow the increment to be a function

	private double zoomIncrement = 0.2;

	/**
	 * <p>
	 * Gets the current zoom increment.
	 * </p>
	 * 
	 * @return the current zoom increment
	 */
	@Override
	public double getZoomIncrement() {
		return calculateZoomIncrement();
	}

	private double calculateZoomIncrement() {
		return this.zoomIncrement;
	}

	/**
	 * <p>
	 * Sets a new zoom increment value.
	 * </p>
	 * 
	 * @param newZoomIncrement new zoom increment value
	 */
	@Override
	public void setZoomIncrement(final double newZoomIncrement) {
		final double oldZoomIncrement = zoomIncrement;
		zoomIncrement = newZoomIncrement;
		firePropertyChange(ZOOM_INCREMENT_CHANGED_PROPERTY, new Double(
			oldZoomIncrement), new Double(zoomIncrement));
	}

	// Zooms an image in the panel by repainting it at the new zoom level.
	// The current mouse position is the zooming center.
	private void zoomImage() {
		final RealCoords imageP = panelToImageCoords(ptToCoords(mousePosition));
		
		// check if zoomed in too close
		if ((zoomFactor > 1) && (scale > initialScale))
		{
			int maxDimension = Math.max(image.getWidth(), image.getHeight());

			// if zooming the image would show less than one pixel of image data
			if ((maxDimension / getZoom()) < 1)
				return;  // DO NOT ZOOM ANY FARTHER
		}
		
		// check if zoomed out too far
		if ((zoomFactor < 1) && (scale < initialScale))
		{
			// get boundaries of image in panel coords
			final RealCoords nearCorner = imageToPanelCoords(new RealCoords(0,0));
			final RealCoords farCorner = imageToPanelCoords(new RealCoords(image.getWidth(),image.getHeight()));

			// if boundaries take up less than 25 pixels in either dimension 
			if (((farCorner.x - nearCorner.x) < 25) || ((farCorner.y - nearCorner.y < 25)))
				return;  // DO NOT ZOOM ANY FARTHER
		}
		
		final double oldZoom = getZoom();
		scale *= zoomFactor;
		final RealCoords panelP = imageToPanelCoords(imageP);
		originX += (mousePosition.x - (int) panelP.x);
		originY += (mousePosition.y - (int) panelP.y);
		firePropertyChange(ZOOM_LEVEL_CHANGED_PROPERTY, new Double(oldZoom),
			new Double(getZoom()));
		repaint();
	}

// </editor-fold>

// <editor-fold defaultstate="collapsed" desc=" <<< NavigationImage >>> ">

	private static final double SCREEN_NAV_IMAGE_FACTOR = 0.15; // 15% of panel's
																															// width
	private static final double NAV_IMAGE_FACTOR = 0.3; // 30% of panel's width
	private double navZoomFactor = 1.0 + zoomIncrement;
	private double navScale = 0.0;
	private boolean navigationImageEnabled = false;  // TODO - enable with a hotkey?????
	private BufferedImage navigationImage;
	private int navImageWidth;
	private int navImageHeight;

	// Creates and renders the navigation image in the upper let corner of the
	// panel.
	private void createNavigationImage() {
		// We keep the original navigation image larger than initially
		// displayed to allow for zooming into it without pixellation effect.
		navImageWidth = (int) (getWidth() * NAV_IMAGE_FACTOR);
		navImageHeight = navImageWidth * image.getHeight() / image.getWidth();
		final int scrNavImageWidth = (int) (getWidth() * SCREEN_NAV_IMAGE_FACTOR);
		// int scrNavImageHeight = scrNavImageWidth * image.getHeight() /
		// image.getWidth();
		navScale = (double) scrNavImageWidth / navImageWidth;

		final GraphicsEnvironment ge =
			GraphicsEnvironment.getLocalGraphicsEnvironment();
		final GraphicsDevice gs = ge.getDefaultScreenDevice();
		final GraphicsConfiguration gc = gs.getDefaultConfiguration();
		navigationImage =
			gc.createCompatibleImage(navImageWidth, navImageHeight,
				Transparency.OPAQUE);

		final Graphics g = navigationImage.getGraphics();
		g.drawImage(image, 0, 0, navImageWidth, navImageHeight, null);
	}

	private int getScreenNavImageWidth() {
		return (int) (navScale * navImageWidth);
	}

	private int getScreenNavImageHeight() {
		return (int) (navScale * navImageHeight);
	}

	// Converts the navigation image coordinates into the zoomed image coordinates
	private IntCoords navToZoomedImageCoords(final IntCoords p) {
		final int x = p.x * getScreenImageWidth() / getScreenNavImageWidth();
		final int y = p.y * getScreenImageHeight() / getScreenNavImageHeight();
		return new IntCoords(x, y);
	}

	// The user clicked within the navigation image and this part of the image
	// is displayed in the panel. The clicked point of the image is centered in
	// the panel.
	private void displayImageAt(final IntCoords p) {
		final IntCoords scrImagePoint = navToZoomedImageCoords(p);
		originX = -(scrImagePoint.x - getWidth() / 2);
		originY = -(scrImagePoint.y - getHeight() / 2);
		repaint();
	}

	private IntCoords ptToCoords(final Point p) {
		return new IntCoords(p.x, p.y);
	}

	/**
	 * <p>
	 * Indicates whether navigation image is enabled.
	 * <p>
	 * 
	 * @return true when navigation image is enabled, false otherwise.
	 */
	@Override
	public boolean isNavigationImageEnabled() {
		return navigationImageEnabled;
	}

	/**
	 * <p>
	 * Enables/disables navigation with the navigation image.
	 * </p>
	 * <p>
	 * Navigation image should be disabled when custom, programmatic navigation is
	 * implemented.
	 * </p>
	 * 
	 * @param enabled true when navigation image is enabled, false otherwise.
	 */
	@Override
	public void setNavigationImageEnabled(final boolean enabled) {
		navigationImageEnabled = enabled;
		repaint();
	}

	// Tests whether a given point in the panel falls within the navigation image
	// boundaries.
	private boolean isInNavigationImage(final Point p) {
		return (isNavigationImageEnabled() && p.x < getScreenNavImageWidth() && p.y < getScreenNavImageHeight());
	}

	// Zooms the navigation image

	private void zoomNavigationImage() {
		navScale *= navZoomFactor;
		repaint();
	}

	private void drawNavigationImage(final Graphics g) {
		// Draw navigation image
		if (isNavigationImageEnabled()) {
			g.drawImage(navigationImage, 0, 0, getScreenNavImageWidth(),
				getScreenNavImageHeight(), null);
			g.setColor(Color.blue);
			g.drawRect(0, 0, getScreenNavImageWidth(), getScreenNavImageHeight());
			drawZoomAreaOutline(g);
		}
	}

	// Paints a white outline over the navigation image indicating
	// the area of the image currently displayed in the panel.
	private void drawZoomAreaOutline(final Graphics g) {
		if (isFullImageInPanel()) {
			return;
		}
		final int x = -originX * getScreenNavImageWidth() / getScreenImageWidth();
		final int y =
			-originY * getScreenNavImageHeight() / getScreenImageHeight();
		final int width =
			getWidth() * getScreenNavImageWidth() / getScreenImageWidth();
		final int height =
			getHeight() * getScreenNavImageHeight() / getScreenImageHeight();
		g.setColor(Color.white);
		g.drawRect(x, y, width, height);
	}

// </editor-fold>

// <editor-fold defaultstate="collapsed" desc=" <<< Zoom Device >>> ">

	// -- Zooming --

	private WheelZoomDevice wheelZoomDevice = null;
	private ButtonZoomDevice buttonZoomDevice = null;

	@SuppressWarnings("synthetic-access")
	private void addWheelZoomDevice() {
		if (wheelZoomDevice == null) {
			wheelZoomDevice = new WheelZoomDevice();
			addMouseWheelListener(wheelZoomDevice);
		}
	}

	@SuppressWarnings("synthetic-access")
	private void addButtonZoomDevice() {
		if (buttonZoomDevice == null) {
			buttonZoomDevice = new ButtonZoomDevice();
			addMouseListener(buttonZoomDevice);
		}
	}

	private void removeWheelZoomDevice() {
		if (wheelZoomDevice != null) {
			removeMouseWheelListener(wheelZoomDevice);
			wheelZoomDevice = null;
		}
	}

	private void removeButtonZoomDevice() {
		if (buttonZoomDevice != null) {
			removeMouseListener(buttonZoomDevice);
			buttonZoomDevice = null;
		}
	}

	/**
	 * <p>
	 * Sets a new zoom device.
	 * </p>
	 * 
	 * @param newZoomDevice specifies the type of a new zoom device.
	 */
	public void setZoomDevice(final ZoomDevice newZoomDevice) {
		if (newZoomDevice == ZoomDevice.NONE) {
			removeWheelZoomDevice();
			removeButtonZoomDevice();
		}
		else if (newZoomDevice == ZoomDevice.MOUSE_BUTTON) {
			removeWheelZoomDevice();
			addButtonZoomDevice();
		}
		else if (newZoomDevice == ZoomDevice.MOUSE_WHEEL) {
			removeButtonZoomDevice();
			addWheelZoomDevice();
		}
	}

	/**
	 * <p>
	 * Gets the current zoom device.
	 * </p>
	 */
	public ZoomDevice getZoomDevice() {
		if (buttonZoomDevice != null) {
			return ZoomDevice.MOUSE_BUTTON;
		}
		else if (wheelZoomDevice != null) {
			return ZoomDevice.MOUSE_WHEEL;
		}
		else {
			return ZoomDevice.NONE;
		}
	}

	/**
	 * <p>
	 * Defines zoom devices.
	 * </p>
	 */
	public static class ZoomDevice {

		/**
		 * <p>
		 * Identifies that the panel does not implement zooming, but the component
		 * using the panel does (programmatic zooming method).
		 * </p>
		 */
		public static final ZoomDevice NONE = new ZoomDevice("none");
		/**
		 * <p>
		 * Identifies the left and right mouse buttons as the zooming device.
		 * </p>
		 */
		public static final ZoomDevice MOUSE_BUTTON =
			new ZoomDevice("mouseButton");
		/**
		 * <p>
		 * Identifies the mouse scroll wheel as the zooming device.
		 * </p>
		 */
		public static final ZoomDevice MOUSE_WHEEL = new ZoomDevice("mouseWheel");
		private final String zoomDevice;

		private ZoomDevice(final String zoomDevice) {
			this.zoomDevice = zoomDevice;
		}

		@Override
		public String toString() {
			return zoomDevice;
		}

	}

	private class WheelZoomDevice implements MouseWheelListener {

		@SuppressWarnings("synthetic-access")
		@Override
		public void mouseWheelMoved(final MouseWheelEvent e) {
			final Point p = e.getPoint();
			final boolean zoomIn = (e.getWheelRotation() < 0);
			if (isInNavigationImage(p)) {
				if (zoomIn) {
					navZoomFactor = 1.0 + zoomIncrement;
				}
				else {
					navZoomFactor = 1.0 - zoomIncrement;
				}
				zoomNavigationImage();
			}
			else if (isInImage(ptToCoords(p))) {
				if (zoomIn) {
					zoomFactor = 1.0 + zoomIncrement;
				}
				else {
					zoomFactor = 1.0 - zoomIncrement;
				}
				zoomImage();
			}
		}

	}

	private class ButtonZoomDevice extends MouseAdapter {

		@SuppressWarnings("synthetic-access")
		@Override
		public void mouseClicked(final MouseEvent e) {
			final Point p = e.getPoint();
			if (SwingUtilities.isRightMouseButton(e)) {
				if (isInNavigationImage(p)) {
					navZoomFactor = 1.0 - zoomIncrement;
					zoomNavigationImage();
				}
				else if (isInImage(ptToCoords(p))) {
					zoomFactor = 1.0 - zoomIncrement;
					zoomImage();
				}
			}
			else {
				if (isInNavigationImage(p)) {
					navZoomFactor = 1.0 + zoomIncrement;
					zoomNavigationImage();
				}
				else if (isInImage(ptToCoords(p))) {
					zoomFactor = 1.0 + zoomIncrement;
					zoomImage();
				}
			}
		}

	}

// </editor-fold>

	public static void main(final String[] args) {
		// if (args.length == 0) {
		// System.out.println("Usage: java NavigableImagePanel imageFilename");
		// System.exit(1);
		// }

		final String filename = // args[0];
			"diatoms.jpg";

		SwingUtilities.invokeLater(new Runnable() {

			@Override
			public void run() {
				final JFrame frame = new JFrame("Navigable Image Panel");
				frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
				final NavigableImagePanel panel = new NavigableImagePanel();
				try {
					final BufferedImage image = ImageIO.read(new File(filename));
					panel.setImage(image);
				}
				catch (final IOException e) {
					JOptionPane.showMessageDialog(null, e.getMessage(), "",
						JOptionPane.ERROR_MESSAGE);
					System.exit(1);
				}
				panel.setNavigationImageEnabled(true);
				frame.getContentPane().add(panel, BorderLayout.CENTER);
				final GraphicsEnvironment ge =
					GraphicsEnvironment.getLocalGraphicsEnvironment();
				final Rectangle bounds = ge.getMaximumWindowBounds();
				frame.setSize(new Dimension(bounds.width, bounds.height));
				frame.setVisible(true);
			}

		});
	}

}
