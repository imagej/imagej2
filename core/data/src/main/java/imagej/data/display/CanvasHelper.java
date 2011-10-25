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

package imagej.data.display;

import imagej.ImageJ;
import imagej.data.display.event.ZoomEvent;
import imagej.event.EventService;
import imagej.util.IntCoords;
import imagej.util.Log;
import imagej.util.RealCoords;

import java.util.Arrays;

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

	/** The standard zoom levels for the canvas */
	private final double[] zoomLevels;

	/** Scale factor, for zooming. */
	private double scale = 1;

	/** Initial scale factor, for resetting zoom. */
	private double initialScale = 1;

	/** Offset from top left, in panel coordinates (pixels). */
	private final IntCoords offset = new IntCoords(0, 0);

	private final EventService eventService;

	public CanvasHelper(final ImageCanvas canvas) {
		this(canvas, defaultZoomLevels());
	}

	public CanvasHelper(final ImageCanvas canvas, final double[] zoomLevels) {
		eventService = ImageJ.get(EventService.class);
		this.canvas = canvas;
		this.zoomLevels = validatedZoomLevels(zoomLevels);
	}

	// -- CanvasHelper methods --

	public boolean isInImage(final IntCoords point) {
		final RealCoords imageCoords = panelToImageCoords(point);
		final int x = imageCoords.getIntX();
		final int y = imageCoords.getIntY();
		return x >= 0 && x < canvas.getCanvasWidth() && y >= 0 &&
			y < canvas.getCanvasHeight();
	}

	public RealCoords panelToImageCoords(final IntCoords panelCoords) {
		final double imageX = (panelCoords.x + offset.x) / scale;
		final double imageY = (panelCoords.y + offset.y) / scale;
		return new RealCoords(imageX, imageY);
	}

	public IntCoords imageToPanelCoords(final RealCoords imageCoords) {
		final int panelX = (int) Math.round(scale * imageCoords.x - offset.x);
		final int panelY = (int) Math.round(scale * imageCoords.y - offset.y);
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
		offset.x = origin.x;
		offset.y = origin.y;
	}

	@Override
	public void panReset() {
		canvas.setPan(new IntCoords(0, 0));
	}

	@Override
	public IntCoords getPanOrigin() {
		return new IntCoords(offset.x, offset.y);
	}

	// -- Zoomable methods --

	@Override
	public void setZoom(final double factor) {
		canvas.setZoom(factor, getDefaultZoomCenter());
	}

	@Override
	public void setZoom(final double factor, final IntCoords center) {
		double desiredScale = factor;
		if (factor == 0) desiredScale = initialScale;
		if (scaleOutOfBounds(desiredScale)) return;

		// We know:
		// imageCenter.x = (center.x + offset.x) / scale
		// (and only offset and scale change)
		// Hence:
		// (center.x + newOffset.x) / desiredScale = (center.x + offset.x) / scale
		// newOffset.x = -center.x + (center.x + offset.x) * desiredScale / scale

		offset.x = (int) (-center.x + (center.x + offset.x) * desiredScale / scale);
		offset.y = (int) (-center.y + (center.y + offset.y) * desiredScale / scale);
		scale = desiredScale;

		eventService.publish(new ZoomEvent(canvas, getZoomFactor(), center.x,
			center.y));
	}

	@Override
	public void zoomIn() {
		canvas.zoomIn(getDefaultZoomCenter());
	}

	@Override
	public void zoomIn(final IntCoords center) {
		final double newScale = nextLargerZoom(scale);
		if (newScale != scale) canvas.setZoom(newScale, center);
	}

	@Override
	public void zoomOut() {
		canvas.zoomOut(getDefaultZoomCenter());
	}

	@Override
	public void zoomOut(final IntCoords center) {
		final double newScale = nextSmallerZoom(scale);
		if (newScale != scale) canvas.setZoom(newScale, center);
	}

	@Override
	public void zoomToFit(final IntCoords topLeft, final IntCoords bottomRight) {
		final int width = bottomRight.x - topLeft.x;
		final int height = bottomRight.y - topLeft.y;

		final double imageSizeX = width / scale;
		final double imageSizeY = height / scale;
		final double xZoom = canvas.getViewportWidth() / imageSizeX;
		final double yZoom = canvas.getViewportHeight() / imageSizeY;
		final double factor = Math.min(xZoom, yZoom);

		final int centerX = topLeft.x + width / 2;
		final int centerY = topLeft.y + height / 2;

		canvas.setZoom(factor, new IntCoords(centerX, centerY));
	}

	@Override
	public double getZoomFactor() {
		return scale;
	}

	public void setInitialScale(final double value) {
		if (value <= 0) {
			throw new IllegalArgumentException("Initial scale must be > 0");
		}
		this.initialScale = value;
	}

	public double getInitialScale() {
		return initialScale;
	}

	public static double getNextLowerZoomLevel(double fractionalScale) {
		double[] levels = defaultZoomLevels() ;
		 for (int i = 0; i < levels.length; i++) {
			 if(levels[i] >= fractionalScale ) return levels[i-1];
		 }
		return 1.0;
	}
	
	// Could do this algorithmically but would require some speci al cases.
	// So make it very clear what the zooms are by hand specifying them.

	public static double[] defaultZoomLevels() {
		final double[] levels = new double[49];

		levels[0] = 1 / 256d;
		levels[1] = 1 / 240d;
		levels[2] = 1 / 224d;
		levels[3] = 1 / 208d;
		levels[4] = 1 / 192d;
		levels[5] = 1 / 176d;
		levels[6] = 1 / 160d;
		levels[7] = 1 / 144d;
		levels[8] = 1 / 128d;
		levels[9] = 1 / 112d;
		levels[10] = 1 / 96d;
		levels[11] = 1 / 80d;
		levels[12] = 1 / 64d;
		levels[13] = 1 / 48d;
		levels[14] = 1 / 32d;
		levels[15] = 1 / 24d;
		levels[16] = 1 / 16d;
		levels[17] = 1 / 12d;
		levels[18] = 1 / 8d;
		levels[19] = 1 / 6d;
		levels[20] = 1 / 4d;
		levels[21] = 1 / 3d;
		levels[22] = 1 / 2d;
		levels[23] = 3 / 4d;
		levels[24] = 1;
		levels[25] = 1.5;
		levels[26] = 2;
		levels[27] = 3;
		levels[28] = 4;
		levels[29] = 6;
		levels[30] = 8;
		levels[31] = 12;
		levels[32] = 16;
		levels[33] = 24;
		levels[34] = 32;
		levels[35] = 48;
		levels[36] = 64;
		levels[37] = 80;
		levels[38] = 96;
		levels[39] = 112;
		levels[40] = 128;
		levels[41] = 144;
		levels[42] = 160;
		levels[43] = 176;
		levels[44] = 192;
		levels[45] = 208;
		levels[46] = 224;
		levels[47] = 240;
		levels[48] = 256;

		return levels;
	}

	// -- Helper methods --

	/** Gets the zoom center to use when none is specified. */
	private IntCoords getDefaultZoomCenter() {
		final int w = canvas.getViewportWidth();
		final int h = canvas.getViewportHeight();
		return new IntCoords(w / 2, h / 2);
	}

	private boolean scaleOutOfBounds(final double desiredScale) {
		if (desiredScale <= 0) {
			Log.warn("*********** BAD SCALE in CanvasHelper *******************");
			return true;
		}

		// check if trying to zoom in too close
		if (desiredScale > scale) {
			final int maxDimension =
				Math.max(canvas.getCanvasWidth(), canvas.getCanvasHeight());

			// if zooming the image would show less than one pixel of image data
			if (maxDimension / desiredScale < 1) return true;
		}

		// check if trying to zoom out too far
		if (desiredScale < scale) {
			// get boundaries of image in panel coords
			final RealCoords nearCornerImage = new RealCoords(0, 0);
			final RealCoords farCornerImage =
				new RealCoords(canvas.getCanvasWidth(), canvas.getCanvasHeight());
			final IntCoords nearCornerPanel = imageToPanelCoords(nearCornerImage);
			final IntCoords farCornerPanel = imageToPanelCoords(farCornerImage);

			// if boundaries take up less than min allowed pixels in either dimension
			if (farCornerPanel.x - nearCornerPanel.x < MIN_ALLOWED_VIEW_SIZE ||
				farCornerPanel.y - nearCornerPanel.y < MIN_ALLOWED_VIEW_SIZE)
			{
				return true;
			}
		}

		return false;
	}

	private double nextSmallerZoom(final double currScale) {
		final int index = Arrays.binarySearch(zoomLevels, currScale);

		int nextIndex;
		if (index >= 0) nextIndex = index - 1;
		else nextIndex = -(index + 1) - 1;

		if (nextIndex < 0) return zoomLevels[0];

		if (nextIndex > zoomLevels.length - 1) {
			return zoomLevels[zoomLevels.length - 1];
		}

		return zoomLevels[nextIndex];
	}

	private double nextLargerZoom(final double currScale) {
		final int index = Arrays.binarySearch(zoomLevels, currScale);

		int nextIndex;
		if (index >= 0) nextIndex = index + 1;
		else nextIndex = -(index + 1);

		if (nextIndex < 0) return zoomLevels[0];

		if (nextIndex > zoomLevels.length - 1) {
			return zoomLevels[zoomLevels.length - 1];
		}

		return zoomLevels[nextIndex];
	}

	private static double[] validatedZoomLevels(final double[] levels) {
		final double[] validatedLevels = levels.clone();

		Arrays.sort(validatedLevels);

		if (validatedLevels.length == 0) {
			throw new IllegalArgumentException("given zoom level array is empty");
		}

		double prevEntry = validatedLevels[0];
		if (prevEntry <= 0) {
			throw new IllegalArgumentException(
				"zoom level array contains nonpositive entries");
		}

		for (int i = 1; i < validatedLevels.length; i++) {
			final double currEntry = validatedLevels[i];
			if (currEntry == prevEntry) {
				throw new IllegalArgumentException(
					"zoom level array contains duplicate entries");
			}
			prevEntry = currEntry;
		}

		return validatedLevels;
	}

}
