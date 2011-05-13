//
// CanvasHelper.java
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

package imagej.display;

import imagej.display.event.ZoomEvent;
import imagej.event.Events;
import imagej.util.IntCoords;
import imagej.util.Log;
import imagej.util.RealCoords;

/**
 * A collection of helper methods for {@link ImageCanvas} objects, particularly
 * panning and zooming.
 * 
 * @author Curtis Rueden
 * @author Barry DeZonia
 */
public class CanvasHelper implements Pannable, Zoomable {

	private static final int MIN_ALLOWED_VIEW_SIZE = 25;

	/** The {@link ImageCanvas} on which this helper operates. */
	private final ImageCanvas canvas;

	/** Scale factor, for zooming. */
	private double scale = 0;

	/** Offset from top left, in panel coordinates (pixels). */
	private IntCoords offset = new IntCoords(0, 0);

	/** The amount the image should be zoomed in or out per operation. */
	private double zoomStep = 1.2;

	public CanvasHelper(final ImageCanvas canvas) {
		this.canvas = canvas;
	}

	// -- CanvasHelper methods --

	public boolean isInImage(final IntCoords point) {
		final RealCoords imageCoords = panelToImageCoords(point);
		final int x = imageCoords.getIntX();
		final int y = imageCoords.getIntY();
		return x >= 0 && x < canvas.getImageWidth() && y >= 0 &&
			y < canvas.getImageHeight();
	}

	public RealCoords panelToImageCoords(final IntCoords panelCoords) {
		final double imageX = (panelCoords.x - offset.x) / scale;
		final double imageY = (panelCoords.y - offset.y) / scale;
		return new RealCoords(imageX, imageY);
	}

	public IntCoords imageToPanelCoords(final RealCoords imageCoords) {
		final int panelX = (int) Math.round(scale * imageCoords.x + offset.x);
		final int panelY = (int) Math.round(scale * imageCoords.y + offset.y);
		return new IntCoords(panelX, panelY);
	}

	// -- Pannable methods --

	@Override
	public void pan(final IntCoords delta) {
		offset.x += delta.x;
		offset.y += delta.y;
	}

	@Override
	public void setPan(final IntCoords origin) {
		offset = origin;
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
		canvas.setZoom(factor, getDefaultZoomCenter());
	}

	@Override
	public void setZoom(final double factor, final IntCoords center) {
		if (scaleOutOfBounds(factor)) return;
		final RealCoords imageCenter = panelToImageCoords(center);
		clipToImageBoundaries(imageCenter);
		scale = factor;

		// We know:
		// panel = scale * image + offset
		// Hence:
		// offset = panel - scale * image
		// Desired panel coordinates should remain unchanged

		final int panelX = center.x;
		final int panelY = center.y;
		offset.x = (int) (panelX - scale * imageCenter.x);
		offset.y = (int) (panelY - scale * imageCenter.y);

		Events.publish(new ZoomEvent(canvas, getZoomFactor(), center.x, center.y));
	}

	@Override
	public void zoomIn() {
		canvas.zoomIn(getDefaultZoomCenter());
	}

	@Override
	public void zoomIn(final IntCoords center) {
		canvas.setZoom(scale * zoomStep, center);
	}

	@Override
	public void zoomOut() {
		canvas.zoomOut(getDefaultZoomCenter());
	}

	@Override
	public void zoomOut(final IntCoords center) {
		canvas.setZoom(scale / zoomStep, center);
	}

	@Override
	public void zoomToFit(final IntCoords topLeft, final IntCoords bottomRight) {
		final int width = bottomRight.x - topLeft.x;
		final int height = bottomRight.y - topLeft.y;

		final double imageSizeX = width / scale;
		final double imageSizeY = height / scale;
		final double xZoom = canvas.getWidth() / imageSizeX;
		final double yZoom = canvas.getHeight() / imageSizeY;
		final double factor = Math.min(xZoom, yZoom);

		final int centerX = topLeft.x + width / 2;
		final int centerY = topLeft.y + height / 2;

		canvas.setZoom(factor, new IntCoords(centerX, centerY));
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

	// -- Helper methods --

	/** Gets the zoom center to use when none is specified. */
	private IntCoords getDefaultZoomCenter() {
		return new IntCoords(canvas.getWidth() / 2, canvas.getHeight() / 2);
	}

	private void clipToImageBoundaries(final RealCoords coords) {
		if (coords.x < 0) coords.x = 0;
		if (coords.y < 0) coords.y = 0;
		if (coords.x >= canvas.getImageWidth()) {
			coords.x = canvas.getImageWidth() - 1;
		}
		if (coords.y >= canvas.getImageHeight()) {
			coords.y = canvas.getImageHeight() - 1;
		}
	}

	private boolean scaleOutOfBounds(final double desiredScale) {
		if (desiredScale <= 0) {
			Log
				.debug("*********** BAD SCALE !!!!!! ********************************");
			return true;
		}

		// check if trying to zoom in too close
		if (desiredScale > scale) {
			final int maxDimension =
				Math.max(canvas.getImageWidth(), canvas.getImageHeight());

			// if zooming the image would show less than one pixel of image data
			if ((maxDimension / getZoomFactor()) < 1) return true;
		}

		// check if trying to zoom out too far
		if (desiredScale < scale) {
			// get boundaries of image in panel coords
			final IntCoords nearCorner = imageToPanelCoords(new RealCoords(0, 0));
			final IntCoords farCorner =
				imageToPanelCoords(new RealCoords(canvas.getImageWidth(), canvas
					.getImageHeight()));

			// if boundaries take up less than min allowed pixels in either dimension
			if (((farCorner.x - nearCorner.x) < MIN_ALLOWED_VIEW_SIZE) ||
				((farCorner.y - nearCorner.y) < MIN_ALLOWED_VIEW_SIZE)) return true;
		}

		return false;
	}

}
