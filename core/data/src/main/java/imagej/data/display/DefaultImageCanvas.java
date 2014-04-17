/*
 * #%L
 * ImageJ software for multidimensional image processing and analysis.
 * %%
 * Copyright (C) 2009 - 2014 Board of Regents of the University of
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
 * #L%
 */

package imagej.data.display;

import imagej.data.display.event.MouseCursorEvent;
import imagej.data.display.event.PanZoomEvent;
import imagej.data.display.event.ViewportResizeEvent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.scijava.event.EventService;
import org.scijava.input.MouseCursor;
import org.scijava.log.LogService;
import org.scijava.plugin.Parameter;
import org.scijava.util.IntCoords;
import org.scijava.util.IntRect;
import org.scijava.util.RealCoords;
import org.scijava.util.RealRect;

/**
 * The DefaultImageCanvas maintains a viewport, a zoom scale and a center
 * coordinate that it uses to map viewport pixels to display coordinates. It
 * also maintains an abstract mouse cursor.
 * <p>
 * The canvas sends a {@link PanZoomEvent} whenever it is panned or zoomed. It
 * sends a {@link MouseCursorEvent} whenever the mouse cursor changes.
 * </p>
 * 
 * @author Lee Kamentsky
 * @author Curtis Rueden
 * @author Barry DeZonia
 */
public class DefaultImageCanvas implements ImageCanvas {

	private static final RealCoords DATA_ZERO = new RealCoords(0, 0);

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

		final int EXTRA_ZOOMS = 30;

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

		maxZoom = hiZooms.get(hiZooms.size() - 1);
	}

	/** The canvas's display. */
	private final ImageDisplay display;

	/** The size of the viewport. */
	private final IntCoords viewportSize;

	/** The standard zoom levels for the canvas. */
	private final double[] zoomLevels;

	@Parameter(required = false)
	private LogService log;

	@Parameter(required = false)
	private EventService eventService;

	/** Initial scale factor, for resetting zoom. */
	private double initialScale = 1;

	/** The current scale factor. */
	private double scale = 1.0;

	private MouseCursor mouseCursor;
	private RealCoords panCenter;

	public DefaultImageCanvas(final ImageDisplay display) {
		display.getContext().inject(this);
		this.display = display;

		mouseCursor = MouseCursor.DEFAULT;
		viewportSize = new IntCoords(100, 100);
		zoomLevels = validatedZoomLevels(defaultZooms);
	}

	// -- ImageCanvas methods --

	@Override
	public ImageDisplay getDisplay() {
		return display;
	}

	@Override
	public int getViewportWidth() {
		return viewportSize.x;
	}

	@Override
	public int getViewportHeight() {
		return viewportSize.y;
	}

	@Override
	public void setViewportSize(final int width, final int height) {
		viewportSize.x = width;
		viewportSize.y = height;
		if (eventService != null) {
			eventService.publish(new ViewportResizeEvent(this));
		}
	}

	@Override
	public boolean isInImage(final IntCoords point) {
		final RealCoords dataCoords = panelToDataCoords(point);
		return getDisplay().getPlaneExtents().contains(dataCoords);
	}

	@Override
	public RealCoords panelToDataCoords(final IntCoords panelCoords) {
		final double dataX = panelCoords.x / getZoomFactor() + getLeftImageX();
		final double dataY = panelCoords.y / getZoomFactor() + getTopImageY();
		return new RealCoords(dataX, dataY);
	}

	@Override
	public IntCoords dataToPanelCoords(final RealCoords dataCoords) {
		final int panelX =
			(int) Math.round(getZoomFactor() * (dataCoords.x - getLeftImageX()));
		final int panelY =
			(int) Math.round(getZoomFactor() * (dataCoords.y - getTopImageY()));
		return new IntCoords(panelX, panelY);
	}

	@Override
	public MouseCursor getCursor() {
		return mouseCursor;
	}

	@Override
	public void setCursor(final MouseCursor cursor) {
		mouseCursor = cursor;
		if (eventService != null) eventService.publish(new MouseCursorEvent(this));
	}

	// -- Pannable methods --

	@Override
	public RealCoords getPanCenter() {
		if (panCenter == null) {
			panReset();
		}
		if (panCenter == null) throw new IllegalStateException();
		return new RealCoords(panCenter.x, panCenter.y);
	}

	@Override
	public IntCoords getPanOffset() {
		final IntCoords offset = dataToPanelCoords(DATA_ZERO);
		offset.x = -offset.x;
		offset.y = -offset.y;
		return offset;
	}

	@Override
	public void setPanCenter(final RealCoords center) {
		if (panCenter == null) {
			panCenter = new RealCoords(center.x, center.y);
		}
		else {
			// NB: Reuse existing object to avoid allocating a new one.
			panCenter.x = center.x;
			panCenter.y = center.y;
		}
		publishPanZoomEvent();
	}

	@Override
	public void setPanCenter(final IntCoords center) {
		setPanCenter(panelToDataCoords(center));
	}

	@Override
	public void pan(final RealCoords delta) {
		final double centerX = getPanCenter().x + delta.x;
		final double centerY = getPanCenter().y + delta.y;
		setPanCenter(new RealCoords(centerX, centerY));
	}

	@Override
	public void pan(final IntCoords delta) {
		final double centerX = getPanCenter().x + delta.x / getZoomFactor();
		final double centerY = getPanCenter().y + delta.y / getZoomFactor();
		setPanCenter(new RealCoords(centerX, centerY));
	}

	@Override
	public void panReset() {
		final RealRect extents = getDisplay().getPlaneExtents();
		final double centerX = extents.x + extents.width / 2d;
		final double centerY = extents.y + extents.height / 2d;
		setPanCenter(new RealCoords(centerX, centerY));
	}

	// -- Zoomable methods --

	@Override
	public void setZoom(final double factor) {
		setZoomAndCenter(factor, getPanCenter());
	}

	@Override
	public void setZoomAtPoint(final double factor, final RealCoords pos) {
		final double newScale = factor == 0 ? initialScale : factor;

		// NB: This was derived from the data <-> panel conversion equations.
		final RealCoords center = getPanCenter();
		center.x = pos.x - (pos.x - center.x) * scale / newScale;
		center.y = pos.y - (pos.y - center.y) * scale / newScale;

		setZoomAndCenter(newScale, center);
	}

	@Override
	public void setZoomAtPoint(final double factor, final IntCoords pos) {
		setZoomAtPoint(factor, panelToDataCoords(pos));
	}

	@Override
	public void setZoomAndCenter(final double factor) {
		final double x = getViewportWidth() / getZoomFactor() / 2d;
		final double y = getViewportHeight() / getZoomFactor() / 2d;
		setZoomAndCenter(factor, new RealCoords(x, y));
	}

	@Override
	public void setZoomAndCenter(final double factor, final RealCoords center) {
		final double newScale = factor == 0 ? initialScale : factor;
		if (scaleOutOfBounds(newScale)) return;

		scale = newScale;

		setPanCenter(center);
	}

	@Override
	public void zoomIn() {
		final double newScale = nextLargerZoom(zoomLevels, getZoomFactor());
		setZoom(newScale);
	}

	@Override
	public void zoomIn(final RealCoords pos) {
		final double desiredScale = nextLargerZoom(zoomLevels, getZoomFactor());
		setZoomAtPoint(desiredScale, pos);
	}

	@Override
	public void zoomIn(final IntCoords pos) {
		final double desiredScale = nextLargerZoom(zoomLevels, getZoomFactor());
		setZoomAtPoint(desiredScale, pos);
	}

	@Override
	public void zoomOut() {
		final double desiredScale = nextSmallerZoom(zoomLevels, getZoomFactor());
		setZoom(desiredScale);
	}

	@Override
	public void zoomOut(final RealCoords pos) {
		final double newScale = nextSmallerZoom(zoomLevels, getZoomFactor());
		setZoomAtPoint(newScale, pos);
	}

	@Override
	public void zoomOut(final IntCoords pos) {
		final double newScale = nextSmallerZoom(zoomLevels, getZoomFactor());
		setZoomAtPoint(newScale, pos);
	}

	@Override
	public void zoomToFit(final IntRect viewportBox) {
		final IntCoords topLeft = viewportBox.getTopLeft();
		final IntCoords bottomRight = viewportBox.getBottomRight();
		final RealCoords dataTopLeft = panelToDataCoords(topLeft);
		final RealCoords dataBottomRight = panelToDataCoords(bottomRight);
		final double newCenterX = Math.abs(dataBottomRight.x + dataTopLeft.x) / 2d;
		final double newCenterY = Math.abs(dataBottomRight.y + dataTopLeft.y) / 2d;
		final double dataSizeX = Math.abs(dataBottomRight.x - dataTopLeft.x);
		final double dataSizeY = Math.abs(dataBottomRight.y - dataTopLeft.y);
		final double xZoom = getViewportWidth() / dataSizeX;
		final double yZoom = getViewportHeight() / dataSizeY;
		final double factor = Math.min(xZoom, yZoom);
		setZoomAndCenter(factor, new RealCoords(newCenterX, newCenterY));
	}

	@Override
	public void zoomToFit(final RealRect viewportBox) {
		final double newCenterX = (viewportBox.x + viewportBox.width / 2d);
		final double newCenterY = (viewportBox.y + viewportBox.height / 2d);
		final double minScale =
			Math.min(getViewportWidth() / viewportBox.width, getViewportHeight() /
				viewportBox.height);
		setZoomAndCenter(minScale, new RealCoords(newCenterX, newCenterY));
	}

	@Override
	public double getZoomFactor() {
		return this.scale;
	}

	@Override
	public double getInitialScale() {
		return initialScale;
	}

	@Override
	public void setInitialScale(final double zoomFactor) {
		if (zoomFactor <= 0) {
			throw new IllegalArgumentException("Initial scale must be > 0");
		}
		this.initialScale = zoomFactor;
	}

	@Override
	public double getBestZoomLevel(final double fractionalScale) {
		final double[] levels = defaultZooms;

		final int zoomIndex = lookupZoomIndex(levels, fractionalScale);

		if (zoomIndex != -1) return levels[zoomIndex];

		return nextSmallerZoom(levels, fractionalScale);
	}

	// -- Helper methods --

	private void publishPanZoomEvent() {
		if (eventService != null) eventService.publish(new PanZoomEvent(this));
	}

	// -- Helper methods --

	/**
	 * Gets the coordinate of the left edge of the viewport in <em>data</em>
	 * space.
	 */
	private double getLeftImageX() {
		final double viewportImageWidth = getViewportWidth() / getZoomFactor();
		return getPanCenter().x - viewportImageWidth / 2d;
	}

	/**
	 * Gets the coordinate of the top edge of the viewport in <em>data</em> space.
	 */
	private double getTopImageY() {
		final double viewportImageHeight = getViewportHeight() / getZoomFactor();
		return getPanCenter().y - viewportImageHeight / 2d;
	}

	/** Checks whether the given scale is out of bounds. */
	private boolean scaleOutOfBounds(final double desiredScale) {
		if (desiredScale <= 0) {
			if (log != null) log.warn("**** BAD SCALE: " + desiredScale + " ****");
			return true;
		}

		if (desiredScale > maxZoom) return true;

		// check if trying to zoom out too far
		if (desiredScale < getZoomFactor()) {
			// get boundaries of the plane in panel coordinates
			final RealRect planeExtents = getDisplay().getPlaneExtents();
			final IntCoords nearCornerPanel =
				dataToPanelCoords(new RealCoords(planeExtents.x, planeExtents.y));
			final IntCoords farCornerPanel =
				dataToPanelCoords(new RealCoords(planeExtents.x + planeExtents.width,
					planeExtents.y + planeExtents.height));

			// if boundaries take up less than min allowed pixels in either dimension
			final int panelX = farCornerPanel.x - nearCornerPanel.x;
			final int panelY = farCornerPanel.y - nearCornerPanel.y;
			if (panelX < MIN_ALLOWED_VIEW_SIZE && panelY < MIN_ALLOWED_VIEW_SIZE) {
				return true;
			}
		}

		return false;
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

	/**
	 * Unfortunately, we can't rely on Java's binary search since we're using
	 * doubles and rounding errors could cause problems. So we write our own that
	 * searches zooms avoiding rounding problems.
	 */
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
