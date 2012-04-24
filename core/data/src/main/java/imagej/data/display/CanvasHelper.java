/*
 * #%L
 * ImageJ software for multidimensional image processing and analysis.
 * %%
 * Copyright (C) 2009 - 2012 Board of Regents of the University of
 * Wisconsin-Madison, Broad Institute of MIT and Harvard, and Max Planck
 * Institute of Molecular Cell Biology and Genetics.
 * %%
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * 
 * The views and conclusions contained in the software and documentation are
 * those of the authors and should not be interpreted as representing official
 * policies, either expressed or implied, of any organization.
 * #L%
 */

package imagej.data.display;

import imagej.ImageJ;
import imagej.data.Extents;
import imagej.data.display.event.ZoomEvent;
import imagej.event.EventService;
import imagej.util.IntCoords;
import imagej.util.Log;
import imagej.util.RealCoords;
import imagej.util.RealRect;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import net.imglib2.meta.Axes;
import net.imglib2.meta.AxisType;

/**
 * A collection of helper methods for {@link ImageCanvas} objects, particularly
 * panning and zooming. The helper controls its canvas: the canvas has
 * the center, viewport size and zoom factor and the CanvasHelper
 * implements the pannable and zoomable behavior by manipulating these
 * canvas variables.
 * 
 * @author Curtis Rueden
 * @author Barry DeZonia
 */
public class CanvasHelper implements Pannable, Zoomable {

	private static final int MIN_ALLOWED_VIEW_SIZE = 25;

	private static double maxZoom;
	
	private static double[] defaultZooms;

	static {
		final List<Double> midLevelZooms = new ArrayList<Double>();

		midLevelZooms.add(1 / 32d);
		midLevelZooms.add(1 / 24d);
		midLevelZooms.add(1 / 16d);
		midLevelZooms.add(1 / 12d);
		midLevelZooms.add(1 / 8d);
		midLevelZooms.add(1 / 6d);
		midLevelZooms.add(1 / 4d);
		midLevelZooms.add(1 / 3d);
		midLevelZooms.add(1 / 2d);
		midLevelZooms.add(3 / 4d);
		midLevelZooms.add(1d);
		midLevelZooms.add(1.5d);
		midLevelZooms.add(2d);
		midLevelZooms.add(3d);
		midLevelZooms.add(4d);
		midLevelZooms.add(6d);
		midLevelZooms.add(8d);
		midLevelZooms.add(12d);
		midLevelZooms.add(16d);
		midLevelZooms.add(24d);
		midLevelZooms.add(32d);

		final int EXTRA_ZOOMS = 25;

		final List<Double> loZooms = new ArrayList<Double>();
		double prevDenom = 1 / midLevelZooms.get(0);
		for (int i = 0; i < EXTRA_ZOOMS; i++) {
			final double newDenom = prevDenom + 16;
			loZooms.add(1 / newDenom);
			prevDenom = newDenom;
		}
		Collections.reverse(loZooms);

		final List<Double> hiZooms = new ArrayList<Double>();
		double prevNumer = midLevelZooms.get(midLevelZooms.size() - 1);
		for (int i = 0; i < EXTRA_ZOOMS; i++) {
			final double newNumer = prevNumer + 16;
			hiZooms.add(newNumer / 1);
			prevNumer = newNumer;
		}

		final List<Double> combinedZoomLevels = new ArrayList<Double>();
		combinedZoomLevels.addAll(loZooms);
		combinedZoomLevels.addAll(midLevelZooms);
		combinedZoomLevels.addAll(hiZooms);

		defaultZooms = new double[combinedZoomLevels.size()];
		for (int i = 0; i < defaultZooms.length; i++)
			defaultZooms[i] = combinedZoomLevels.get(i);
		
		maxZoom = hiZooms.get(hiZooms.size()-1);
	}

	/** The {@link ImageCanvas} on which this helper operates. */
	private final DefaultImageCanvas canvas;

	/** The standard zoom levels for the canvas */
	private final double[] zoomLevels;

	/** Initial scale factor, for resetting zoom. */
	private double initialScale = 1;

	private final EventService eventService;

	// -- constructors --

	public CanvasHelper(final DefaultImageCanvas canvas) {
		this(canvas, defaultZoomLevels());
	}

	public CanvasHelper(final DefaultImageCanvas canvas, final double[] zoomLevels) {
		eventService = canvas.getDisplay().getContext().getService(EventService.class);
		this.canvas = canvas;
		this.zoomLevels = validatedZoomLevels(zoomLevels);
		panReset();
	}

	// -- CanvasHelper methods --

	public static double[] defaultZoomLevels() {
		return defaultZooms;
	}

	public boolean isInImage(final IntCoords point) {
		final RealCoords imageCoords = panelToImageCoords(point);
		return getImageExtents().contains(imageCoords);
	}

	public RealCoords panelToImageCoords(final IntCoords panelCoords) {
		final double imageX = panelCoords.x / getZoomFactor() + getLeftImageX();
		final double imageY = panelCoords.y / getZoomFactor() + getTopImageY();
		return new RealCoords(imageX, imageY);
	}

	public IntCoords imageToPanelCoords(final RealCoords imageCoords) {
		final int panelX = (int) Math.round(getZoomFactor() * (imageCoords.x - getLeftImageX()));
		final int panelY = (int) Math.round(getZoomFactor() * (imageCoords.y - getTopImageY()));
		return new IntCoords(panelX, panelY);
	}

	// -- Pannable methods --

	@Override
	public void pan(final IntCoords delta) {
		double centerX = getPanCenter().x + delta.x / getZoomFactor();
		double centerY = getPanCenter().y + delta.y / getZoomFactor();
		canvas.doSetCenter(centerX, centerY);
	}

	@Override
	public void setPan(final RealCoords origin) {
		canvas.doSetCenter(origin.x, origin.y);
	}

	@Override
	public void panReset() {
		canvas.doSetCenter(canvas.getViewportWidth() / getZoomFactor() / 2.0,
						   canvas.getViewportHeight() / getZoomFactor() / 2.0);
	}

	@Override
	public RealCoords getPanCenter() {
		return canvas.getPanCenter();
	}

	// -- Zoomable methods --

	@Override
	public void setZoom(final double factor) {
		double desiredScale = (factor == 0)? initialScale: factor;
		if (scaleOutOfBounds(desiredScale) || (desiredScale == getZoomFactor())) return;
		canvas.doSetZoom(factor);
	}

	@Override
	public void setZoom(final double factor, final IntCoords center) {
		double desiredScale = factor;
		if (factor == 0) desiredScale = initialScale;
		RealCoords newCenter = panelToImageCoords(center);
		if (scaleOutOfBounds(desiredScale) ||
			((desiredScale == getZoomFactor()) &&
			 (getPanCenter().x == newCenter.x) &&
			 (getPanCenter().y == newCenter.y))) return;
		
		canvas.doSetZoomAndCenter(desiredScale, newCenter.x, newCenter.y);
	}

	@Override
	public void zoomIn() {
		final double newScale = nextLargerZoom(zoomLevels, getZoomFactor());
		setZoom(newScale);
	}

	@Override
	public void zoomIn(final IntCoords center) {
		final double desiredScale = nextLargerZoom(zoomLevels, getZoomFactor());
		setZoom(desiredScale, center);
	}

	@Override
	public void zoomOut() {
		final double desiredScale = nextSmallerZoom(zoomLevels, getZoomFactor());
		setZoom(desiredScale);
	}

	@Override
	public void zoomOut(final IntCoords center) {
		final double newScale = nextSmallerZoom(zoomLevels, getZoomFactor());
		setZoom(newScale, center);
	}

	@Override
	public void zoomToFit(final IntCoords topLeft, final IntCoords bottomRight) {
		RealCoords imageTopLeft = panelToImageCoords(topLeft);
		RealCoords imageBottomRight = panelToImageCoords(bottomRight);
		double newCenterX = Math.abs(imageBottomRight.x - imageTopLeft.x) / 2;
		double newCenterY = Math.abs(imageBottomRight.y - imageTopLeft.y) / 2;
		final double imageSizeX = Math.abs(imageBottomRight.x - imageTopLeft.x);
		final double imageSizeY = Math.abs(imageBottomRight.y - imageTopLeft.y);
		final double xZoom = canvas.getViewportWidth() / imageSizeX;
		final double yZoom = canvas.getViewportHeight() / imageSizeY;
		final double factor = Math.min(xZoom, yZoom);
		if (scaleOutOfBounds(factor)) return;

		canvas.doSetZoomAndCenter(factor, newCenterX, newCenterY);
	}

	@Override
	public double getZoomFactor() {
		return canvas.getZoomFactor();
	}

	@Override
	public void setZoomAndCenter(double factor) {
		final double desiredScale = (factor == 0)? initialScale: factor;
		if (scaleOutOfBounds(desiredScale)) return;
		canvas.doSetZoomAndCenter(desiredScale,
				canvas.getViewportWidth() / getZoomFactor() / 2.0,
				canvas.getViewportHeight() / getZoomFactor() / 2.0);
	}

	@Override
	public void setZoom(double factor, RealCoords center) {
		final double desiredScale = (factor == 0)? initialScale: factor;
		if (scaleOutOfBounds(desiredScale)) return;
		canvas.doSetZoomAndCenter(desiredScale, center.x, center.y);
	}

	@Override
	public RealRect getViewportImageRect() {
		RealCoords topLeft = panelToImageCoords(new IntCoords(0,0));
		RealCoords bottomRight = panelToImageCoords(
				new IntCoords(canvas.getViewportWidth(), canvas.getViewportHeight()));
		return new RealRect(topLeft.x, topLeft.y, 
				bottomRight.x - topLeft.x, bottomRight.y - topLeft.y);
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

	public static double getBestZoomLevel(final double fractionalScale) {
		final double[] levels = defaultZoomLevels();

		final int zoomIndex = lookupZoomIndex(levels, fractionalScale);

		if (zoomIndex != -1) return levels[zoomIndex];

		return nextSmallerZoom(levels, fractionalScale);
	}
	
	// -- Helper methods --

	/**
	 * @return the coordinate of the left edge of the viewport in image space.
	 */
	private double getLeftImageX() {
		return (double)(canvas.getViewportWidth()) / canvas.getZoomFactor() / 2;
	}
	/**
	 * @return the coordinate of the top edge of the viewport in image space.
	 */
	private double getTopImageY() {
		return (double)(canvas.getViewportHeight()) / canvas.getZoomFactor() / 2;
	}

	private boolean scaleOutOfBounds(final double desiredScale) {
		if (desiredScale <= 0) {
			Log.warn("*********** BAD SCALE in CanvasHelper *******************");
			return true;
		}

		// BDZ removed 3-1-12 and replaced with test versus maxZoom. This should
		// be less confusing to end users
		/*
		// check if trying to zoom in too close
		if (desiredScale > scale) {
			final int maxDimension =
				Math.max(canvas.getCanvasWidth(), canvas.getCanvasHeight());

			// if zooming the image would show less than one pixel of image data
			if (maxDimension / desiredScale < 1) return true;
		}
		*/
		if (desiredScale > maxZoom) return true;
	
		// check if trying to zoom out too far
		if (desiredScale < getZoomFactor()) {
			// get boundaries of image in panel coords
			RealRect displayExtents = getImageExtents();
			final IntCoords nearCornerPanel = imageToPanelCoords(
					new RealCoords(displayExtents.x, displayExtents.y));
			final IntCoords farCornerPanel = imageToPanelCoords(
					new RealCoords(displayExtents.x + displayExtents.width, 
							       displayExtents.y + displayExtents.height) );

			// if boundaries take up less than min allowed pixels in either dimension
			final int panelX = farCornerPanel.x - nearCornerPanel.x;
			final int panelY = farCornerPanel.y - nearCornerPanel.y;
			if (panelX < MIN_ALLOWED_VIEW_SIZE && panelY < MIN_ALLOWED_VIEW_SIZE) {
				return true;
			}
		}

		return false;
	}
	
	/**
	 * @return the extents of the display in image coordinates.
	 */
	private RealRect getImageExtents() {
		return canvas.getDisplay().getImageExtents();
	}
	
	private static double nextSmallerZoom(final double[] zoomLevels,
		final double currScale)
	{
		final int index = Arrays.binarySearch(zoomLevels, currScale);

		int nextIndex;
		if (index >= 0) nextIndex = index - 1;
		else nextIndex = -(index + 1) - 1;

		if (nextIndex < 0) nextIndex = 0;
		if (nextIndex > zoomLevels.length - 1) nextIndex = zoomLevels.length - 1;

		return zoomLevels[nextIndex];
	}

	private static double nextLargerZoom(final double[] zoomLevels,
		final double currScale)
	{
		final int index = Arrays.binarySearch(zoomLevels, currScale);

		int nextIndex;
		if (index >= 0) nextIndex = index + 1;
		else nextIndex = -(index + 1);

		if (nextIndex < 0) nextIndex = 0;
		if (nextIndex > zoomLevels.length - 1) nextIndex = zoomLevels.length - 1;

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

	// unfortunately can't rely on Java's binary search since we're using
	// doubles and rounding errors could cause problems. write our own that
	// searches zooms avoiding rounding problems.
	private static int lookupZoomIndex(final double[] levels,
		final double requestedZoom)
	{
		int lo = 0;
		int hi = levels.length - 1;
		do {
			final int mid = (lo + hi) / 2;
			final double possibleZoom = levels[mid];
			if (Math.abs(requestedZoom - possibleZoom) < 0.00001) return mid;
			if (requestedZoom < possibleZoom) hi = mid - 1;
			else lo = mid + 1;
		}
		while (hi >= lo);
		return -1;
	}

}
