//
// ImageCanvasSwing.java
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

import imagej.Coords;
import imagej.display.EventDispatcher;
import imagej.display.MouseCursor;
import imagej.display.NavigableImageCanvas;

import java.awt.Graphics;
import java.awt.Point;
import java.awt.image.BufferedImage;

import javax.swing.JPanel;

/**
 * TODO
 *
 * @author Grant Harris
 */
public class ImageCanvasSwing extends JPanel implements NavigableImageCanvas {

	private BufferedImage image;

	@Override
	public void setImage(final BufferedImage image) {
		final BufferedImage oldImage = this.image;
		this.image = image;
		// Reset scale so that initializeParameters() is called in paintComponent()
		// for the new image.
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
	protected void paintComponent(final Graphics g) {
		super.paintComponent(g); // Paints the background
		if (image == null) {
			return;
		}
		g.drawImage(image, 0, 0, image.getWidth(), image.getHeight(), null);
	}

	@Override
	public void addEventDispatcher(final EventDispatcher dispatcher) {
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
	public void pan(final int xDelta, final int yDelta) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public void setHighQualityRenderingEnabled(final boolean enabled) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public void setImageOrigin(final int x, final int y) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public void setImageOrigin(final Point newOrigin) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public void setNavigationImageEnabled(final boolean enabled) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public void setZoom(final double newZoom) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public void setZoom(final double newZoom, final Point zoomingCenter) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public void setZoomIncrement(final double newZoomIncrement) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public double getZoomIncrement() {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	// =====================================
	// Event Handline

	@Override
	public void subscribeToToolEvents() {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public void setCursor(final MouseCursor cursor) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public Coords panelToImageCoords(final Point p) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public Coords imageToPanelCoords(final Coords p) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public boolean isInImage(final Point p) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

}
