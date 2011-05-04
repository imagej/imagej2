//
// SwingImageCanvas.java
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

package imagej.ui.swing.display;

import imagej.awt.AWTCursors;
import imagej.awt.AWTEventDispatcher;
import imagej.awt.AWTImageCanvas;
import imagej.display.EventDispatcher;
import imagej.display.ImageCanvas;
import imagej.display.MouseCursor;
import imagej.display.event.ZoomEvent;
import imagej.event.EventSubscriber;
import imagej.event.Events;
import imagej.tool.event.ToolActivatedEvent;
import imagej.util.IntCoords;
import imagej.util.Log;
import imagej.util.RealCoords;
import imagej.util.Rect;

import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.image.BufferedImage;

import javax.swing.JPanel;

/**
 * A Swing implementation of {@link ImageCanvas}.
 * <p>
 * This code is based on <a
 * href="http://today.java.net/article/2007/03/23/navigable-image-panel">Slav
 * Boleslawski's NavigableImagePanel</a>.
 * </p>
 * 
 * @author Grant Harris
 * @author Curtis Rueden
 * @author Barry DeZonia
 */
public class SwingImageCanvas extends JPanel implements AWTImageCanvas,
	EventSubscriber<ToolActivatedEvent>
{
	private static final int MIN_ALLOWED_VIEW_SIZE = 25;
	private static final double MAX_SCREEN_PROPORTION = 0.85;
	private static final double HIGH_QUALITY_RENDERING_SCALE_THRESHOLD = 1.0;
	private static final Object INTERPOLATION_TYPE =
	// TODO - put this back?? //RenderingHints.VALUE_INTERPOLATION_BILINEAR;
		RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR; // this is like IJ1

	private boolean highQualityRenderingEnabled = true;

	private BufferedImage image;

	/** Initially computed scale factor. */
	private double initialScale = 0;

	/** Scale factor, for zooming. */
	private double scale = 0;

	/** Offset from top left, in panel coordinates (pixels. */
	private IntCoords offset = new IntCoords(0, 0);

	/** The amount the image should be zoomed in or out per operation. */
	private double zoomStep = 1.2;

	private Dimension previousPanelSize;

	/** Creates an image canvas with no default image. */
	public SwingImageCanvas() {
		setOpaque(false);
		addResizeListener();
		Events.subscribe(ToolActivatedEvent.class, this);
	}

	/** Creates an image canvas with the specified image. */
	public SwingImageCanvas(final BufferedImage image) {
		this();
		setImage(image);
	}

	// -- JComponent methods --

	@Override
	protected void paintComponent(final Graphics g) {
		super.paintComponent(g); // paint the background

		if (image == null) return;

		if (scale == 0.0) initializeParams();

		if (isHighQualityRendering()) {
			final Rect rect = getImageClipBounds();

			// if no part of image is displayed in the panel
			if (rect == null || rect.width == 0 || rect.height == 0) return;

			final BufferedImage subImage =
				image.getSubimage(rect.x, rect.y, rect.width, rect.height);

			final Graphics2D g2 = (Graphics2D) g;

			g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, INTERPOLATION_TYPE);

			final int x = Math.max(0, offset.x);
			final int y = Math.max(0, offset.y);
			final int width = Math.min((int)
				(scale * subImage.getWidth()), getWidth());
			final int height = Math.min((int)
				(scale * subImage.getHeight()), getHeight());
			g2.drawImage(subImage, x, y, width, height, this);

			Log.debug("HIGH QUALITY CASE: origin=(" + x + ", " + y +
				"), size=(" + width + ", " + height + ")");
		}
		else {
			final int x = offset.x;
			final int y = offset.y;
			final int width = getScreenImageWidth();
			final int height = getScreenImageHeight();
			g.drawImage(image, x, y, width, height, null);

			Log.debug("LOW QUALITY CASE: origin=(" + x + ", " + y +
				"), size=(" + width + ", " + height + ")");
		}
	}

	// -- AWTImageCanvas methods --

	@Override
	public BufferedImage getImage() {
		return image;
	}

	@Override
	public void setImage(final BufferedImage newImage) {
		image = newImage;
		Dimension maxDims = calcMaxAllowableDimensions();
		Dimension dimensions =
			calcReasonableDimensions(maxDims, image.getWidth(), image.getHeight());
		setPreferredSize(dimensions);
		repaint();
	}

	// -- ImageCanvas methods --

	@Override
	public int getImageWidth() {
		return image.getWidth();
	}

	@Override
	public int getImageHeight() {
		return image.getHeight();
	}

	@Override
	public void addEventDispatcher(final EventDispatcher dispatcher) {
		final AWTEventDispatcher awtDispatcher = (AWTEventDispatcher) dispatcher;
		addKeyListener(awtDispatcher);
		addMouseListener(awtDispatcher);
		addMouseMotionListener(awtDispatcher);
		addMouseWheelListener(awtDispatcher);
	}

	@Override
	public boolean isHighQualityRenderingEnabled() {
		return highQualityRenderingEnabled;
	}

	@Override
	public void setHighQualityRenderingEnabled(final boolean enabled) {
		highQualityRenderingEnabled = enabled;
	}

	@Override
	public boolean isInImage(final IntCoords panelCoords) {
		final RealCoords imageCoords = panelToImageCoords(panelCoords);
		final int x = imageCoords.getIntX();
		final int y = imageCoords.getIntY();
		return (x >= 0 && x < image.getWidth() && y >= 0 && y < image.getHeight());
	}

	@Override
	public RealCoords panelToImageCoords(final IntCoords panelCoords) {
		final double imageX = (panelCoords.x - offset.x) / scale;
		final double imageY = (panelCoords.y - offset.y) / scale;
		return new RealCoords(imageX, imageY);
	}

	@Override
	public IntCoords imageToPanelCoords(final RealCoords imageCoords) {
		final int panelX = (int) Math.round(scale * imageCoords.x + offset.x);
		final int panelY = (int) Math.round(scale * imageCoords.y + offset.y);
		return new IntCoords(panelX, panelY);
	}

	@Override
	public void setCursor(final MouseCursor cursor) {
		final int cursorCode = AWTCursors.getCursorCode(cursor);
		setCursor(Cursor.getPredefinedCursor(cursorCode));
	}

	// -- Pannable methods --

	@Override
	public void pan(final IntCoords delta) {
		offset.x += delta.x;
		offset.y += delta.y;
		repaint();
	}

	@Override
	public void setPan(final IntCoords origin) {
		offset = origin;
		repaint();
	}

	@Override
	public void panReset() {
		setPan(new IntCoords(0, 0));
	}

	@Override
	public IntCoords getPanOrigin() {
		return offset;
	}

	// -- Zoomable methods --

	@Override
	public void setZoom(final double factor) {
		setZoom(factor, getDefaultZoomCenter());
	}

	@Override
	public void setZoom(final double factor, final IntCoords center) {
		double desiredScale = factor;
		if (desiredScale == 0)
			desiredScale = initialScale;
		if (scaleOutOfBounds(desiredScale)) return;
		final RealCoords imageCenter = panelToImageCoords(center);
		clipToImageBoundaries(imageCenter);
		scale = desiredScale;

		// We know:
		//   panel = scale * image + offset
		// Hence:
		//   offset = panel - scale * image
		// Desired panel coordinates should remain unchanged

		final int panelX = center.x;
		final int panelY = center.y;
		offset.x = (int) (panelX - scale * imageCenter.x);
		offset.y = (int) (panelY - scale * imageCenter.y);

		Events.publish(new ZoomEvent(this, getZoomFactor(), center.x, center.y));
		repaint();
	}

	@Override
	public void zoomIn() {
		zoomIn(getDefaultZoomCenter());
	}

	@Override
	public void zoomIn(final IntCoords center) {
		setZoom(scale * zoomStep, center);
	}

	@Override
	public void zoomOut() {
		zoomOut(getDefaultZoomCenter());
	}

	@Override
	public void zoomOut(final IntCoords center) {
		setZoom(scale / zoomStep, center);
	}

	@Override
	public void zoomToFit(final IntCoords topLeft, final IntCoords bottomRight) {
		final int width = bottomRight.x - topLeft.x;
		final int height = bottomRight.y - topLeft.y;

		final double imageSizeX = width / scale;
		final double imageSizeY = height / scale;
		final double xZoom = getWidth() / imageSizeX;
		final double yZoom = getHeight() / imageSizeY;
		final double factor = Math.min(xZoom, yZoom);

		final int centerX = topLeft.x + width / 2;
		final int centerY = topLeft.y + height / 2;

		setZoom(factor, new IntCoords(centerX, centerY));
	}

	@Override
	public double getZoomFactor() {
		return scale;
	}

	@Override
	public void setZoomStep(final double zoomStep) {
		if (zoomStep <= 1) {
			throw new IllegalArgumentException("zoom step must be > 1");
		}
		this.zoomStep = zoomStep;
	}

	@Override
	public double getZoomStep() {
		return zoomStep;
	}

	// -- EventSubscriber methods --

	@Override
	public void onEvent(final ToolActivatedEvent event) {
		setCursor(event.getTool().getCursor());
	}

	// -- Helper methods --

	/** Gets the zoom center to use when none is specified. */
	private IntCoords getDefaultZoomCenter() {
		return new IntCoords(getWidth() / 2, getHeight() / 2);
	}

	private void addResizeListener() {
		addComponentListener(new ComponentAdapter() {
			@SuppressWarnings("synthetic-access")
			@Override
			public void componentResized(final ComponentEvent e) {
				if (scale > 0) {
					if (isFullImageInPanel()) {
						centerImage();
					}
					else if (isImageEdgeInPanel()) {
						scaleOrigin();
					}
				}
				previousPanelSize = getSize();
			}
		});
	}

	/** Tests whether the image is displayed in its entirety in the panel. */
	private boolean isFullImageInPanel() {
		return offset.x >= 0 &&
			offset.x + getScreenImageWidth() < getWidth() &&
			offset.y >= 0 &&
			offset.y + getScreenImageHeight() < getHeight();
	}

	/** Used when the image is resized. */
	private boolean isImageEdgeInPanel() {
		if (previousPanelSize == null) {
			return false;
		}
		return offset.x > 0 && offset.x < previousPanelSize.width ||
			offset.y > 0 && offset.y < previousPanelSize.height;
	}

	/** Centers the current image in the panel. */
	private void centerImage() {
		offset.x = (getWidth() - getScreenImageWidth()) / 2;
		offset.y = (getHeight() - getScreenImageHeight()) / 2;
		repaint();
	}

	/** Used when the panel is resized. */
	private void scaleOrigin() {
		offset.x = offset.x * getWidth() / previousPanelSize.width;
		offset.y = offset.y * getHeight() / previousPanelSize.height;
		repaint();
	}

	private void clipToImageBoundaries(final RealCoords coords) {
		if (coords.x < 0) coords.x = 0;
		if (coords.y < 0) coords.y = 0;
		if (coords.x >= image.getWidth()) coords.x = image.getWidth() - 1;
		if (coords.y >= image.getHeight()) coords.y = image.getHeight() - 1;
	}

	/**
	 * Gets the bounds of the image area currently displayed in the panel (in
	 * image coordinates).
	 */
	private Rect getImageClipBounds() {
		final RealCoords startCoords = panelToImageCoords(new IntCoords(0, 0));
		final RealCoords endCoords =
			panelToImageCoords(new IntCoords(getWidth() - 1, getHeight() - 1));
		final int panelX1 = startCoords.getIntX();
		final int panelY1 = startCoords.getIntY();
		final int panelX2 = endCoords.getIntX();
		final int panelY2 = endCoords.getIntY();

		final int imageWidth = image.getWidth();
		final int imageHeight = image.getHeight();

		if (panelX1 >= imageWidth || panelX2 < 0 ||
			panelY1 >= imageHeight || panelY2 < 0)
		{
			// no intersection
			return null;
		}

		final int x1 = panelX1 < 0 ? 0 : panelX1;
		final int y1 = panelY1 < 0 ? 0 : panelY1;
		final int x2 = panelX2 >= imageWidth ? imageWidth - 1 : panelX2;
		final int y2 = panelY2 >= imageHeight ? imageHeight - 1 : panelY2;
		return new Rect(x1, y1, x2 - x1 + 1, y2 - y1 + 1);
	}

	/** Gets the width of the image, scaled by the zoom factor. */
	private int getScreenImageWidth() {
		return (int) (scale * image.getWidth());
	}

	/** Gets the height of the image, scaled by the zoom factor. */
	private int getScreenImageHeight() {
		return (int) (scale * image.getHeight());
	}

	/** Called from {@link #paintComponent} when a new image is set. */
	private void initializeParams() {
		initialScale = calcInitialScale();
		scale = initialScale;
		offset.x = offset.y = 0;
		// must publish an initial zoom event
		Events.publish(new ZoomEvent(this, scale, getWidth()/2, getHeight()/2));
	}

	/**
	 * High quality rendering kicks in when when a scaled image is larger than the
	 * original image. In other words, when image decimation stops and
	 * interpolation starts.
	 */
	private boolean isHighQualityRendering() {
		return highQualityRenderingEnabled &&
			scale > HIGH_QUALITY_RENDERING_SCALE_THRESHOLD;
	}

	private boolean scaleOutOfBounds(final double desiredScale) {
		if (desiredScale <= 0) {
			Log.debug("*********** BAD SCALE !!!!!! ********************************");
			return true;
		}

		// check if trying to zoom in too close
		if (desiredScale > scale) {
			final int maxDimension = Math.max(image.getWidth(), image.getHeight());

			// if zooming the image would show less than one pixel of image data
			if ((maxDimension / getZoomFactor()) < 1) return true;
		}

		// check if trying to zoom out too far
		if (desiredScale < scale) {
			// get boundaries of image in panel coords
			final IntCoords nearCorner = imageToPanelCoords(new RealCoords(0, 0));
			final IntCoords farCorner =
				imageToPanelCoords(new RealCoords(image.getWidth(), image.getHeight()));

			// if boundaries take up less than min allowed pixels in either dimension
			if (((farCorner.x - nearCorner.x) < MIN_ALLOWED_VIEW_SIZE) ||
				((farCorner.y - nearCorner.y) < MIN_ALLOWED_VIEW_SIZE)) return true;
		}

		return false;
	}

	private Dimension calcMaxAllowableDimensions() {
		final Dimension screenDims = Toolkit.getDefaultToolkit().getScreenSize();
		final double maxAllowedWidth = MAX_SCREEN_PROPORTION * screenDims.width;
		final double maxAllowedHeight = MAX_SCREEN_PROPORTION * screenDims.height;
		return new Dimension((int)maxAllowedWidth, (int)maxAllowedHeight);
	}
	
	private enum ImageShape {TOO_WIDE, TOO_TALL, FITS_FINE}
	
	private ImageShape checkImageShape(Dimension maxDims, int width, int height) {
		// is image too big to comfortably fit on screen??
		if ((width > maxDims.width) || (height > maxDims.height)) {
			final double windowAspect = ((double)maxDims.width) / maxDims.height;
			final double imageAspect = ((double)width) / height;
			if (imageAspect > windowAspect) {
				// width is the problem dimension
				return ImageShape.TOO_WIDE;
			}
			// else imageAspect <= windowAspect
			// height is the problem dimension
			return ImageShape.TOO_TALL;
		}

		return ImageShape.FITS_FINE;
	}

	private Dimension calcReasonableDimensions(Dimension maxDims, int imageWidth, int imageHeight) {
		int reasonableWidth, reasonableHeight;
		double aspectRatio;
		switch (checkImageShape(maxDims, imageWidth, imageHeight)) {
			case TOO_WIDE:
				aspectRatio = ((double)imageHeight / imageWidth);
				reasonableWidth = maxDims.width;
				reasonableHeight = (int) (aspectRatio * maxDims.width);
				break;
			case TOO_TALL:
				aspectRatio = ((double)imageWidth / imageHeight);
				reasonableHeight = maxDims.height;
				reasonableWidth = (int) (aspectRatio * maxDims.height);
				break;
			default:  // fits fine
				reasonableWidth = imageWidth;
				reasonableHeight = imageHeight;
				break;
		}
		return new Dimension(reasonableWidth, reasonableHeight);
	}

	private double calcInitialScale() {
		int imageWidth = image.getWidth();
		int imageHeight = image.getHeight();
		Dimension maxDims = calcMaxAllowableDimensions();
		switch (checkImageShape(maxDims, imageWidth, imageHeight)) {
			case TOO_WIDE:
				return ((double)maxDims.width) / imageWidth;
			case TOO_TALL:
				return ((double)maxDims.height) / imageHeight;
			default: // fits fine
				return 1.0;
		}
	}
	
}
