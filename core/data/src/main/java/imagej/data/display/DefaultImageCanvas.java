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
import imagej.data.display.event.MouseCursorEvent;
import imagej.data.display.event.ZoomEvent;
import imagej.event.EventService;
import imagej.ext.MouseCursor;
import imagej.log.LogService;
import imagej.util.IntCoords;
import imagej.util.RealCoords;
import imagej.util.RealRect;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * The DefaultImageCanvas maintains a viewport, a zoom scale and a center
 * coordinate that it uses to map viewport pixels to display coordinates. It
 * also maintains an abstract mouse cursor.
 * <p>
 * The canvas sends a zoom event whenever it is panned or zoomed. It sends a
 * mouse event whenever the mouse changes.
 * </p>
 * 
 * @author Lee Kamentsky
 * @author Curtis Rueden
 * @author Barry DeZonia
 */
public class DefaultImageCanvas implements ImageCanvas {

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

		maxZoom = hiZooms.get(hiZooms.size() - 1);
	}

	/** The canvas's display. */
	private final ImageDisplay display;

	/** The size of the viewport. */
	private final IntCoords viewportSize;

	/** The standard zoom levels for the canvas. */
	private final double[] zoomLevels;

	/** Initial scale factor, for resetting zoom. */
	private double initialScale = 1;

	/** The current scale factor. */
	private double scale = 1.0;

	private MouseCursor mouseCursor;
	private RealCoords center;

	public DefaultImageCanvas(final ImageDisplay display) {
		this.display = display;
		mouseCursor = MouseCursor.DEFAULT;
		viewportSize = new IntCoords(100, 100);
		zoomLevels = validatedZoomLevels(defaultZooms);
	}

	// -- Pannable methods --

	@Override
	public void pan(final IntCoords delta) {
		final double centerX = getPanCenter().x + delta.x / getZoomFactor();
		final double centerY = getPanCenter().y + delta.y / getZoomFactor();
		doSetCenter(centerX, centerY);
	}

	@Override
	public void setPan(final RealCoords center) {
		doSetCenter(center.x, center.y);
	}

	@Override
	public void panReset() {
		final RealRect extents = getDisplay().getImageExtents();
		doSetCenter(extents.x + extents.height / 2, extents.y + extents.width / 2);
	}

	@Override
	public RealCoords getPanCenter() {
		if (center == null) {
			panReset();
		}
		if (center == null) throw new IllegalStateException();
		return new RealCoords(center.x, center.y);
	}

	// -- Zoomable methods --

	@Override
	public void setZoom(final double factor) {
		final double desiredScale = factor == 0 ? initialScale : factor;
		if (scaleOutOfBounds(desiredScale) || desiredScale == getZoomFactor()) {
			return;
		}
		doSetZoom(desiredScale);
	}

	@Override
	public void setZoom(final double factor, final IntCoords center) {
		double desiredScale = factor;
		if (factor == 0) desiredScale = initialScale;
		final RealCoords newCenter = panelToImageCoords(center);
		if (scaleOutOfBounds(desiredScale) || desiredScale == getZoomFactor() &&
			getPanCenter().x == newCenter.x && getPanCenter().y == newCenter.y)
		{
			return;
		}
		doSetZoomAndCenter(desiredScale, newCenter.x, newCenter.y);
	}

	@Override
	public void setZoom(final double factor, final RealCoords center) {
		final double desiredScale = factor == 0 ? initialScale : factor;
		if (scaleOutOfBounds(desiredScale)) return;
		doSetZoomAndCenter(desiredScale, center.x, center.y);
	}

	@Override
	public void setZoomAndCenter(final double factor) {
		final double desiredScale = factor == 0 ? initialScale : factor;
		if (scaleOutOfBounds(desiredScale)) return;
		doSetZoomAndCenter(desiredScale,
			getViewportWidth() / getZoomFactor() / 2.0, getViewportHeight() /
				getZoomFactor() / 2.0);
	}

	@Override
	public void zoomIn() {
		final double newScale = nextLargerZoom(zoomLevels, getZoomFactor());
		setZoom(newScale);
	}

	@Override
	public void zoomIn(final IntCoords ctr) {
		final double desiredScale = nextLargerZoom(zoomLevels, getZoomFactor());
		setZoom(desiredScale, center);
	}

	@Override
	public void zoomOut() {
		final double desiredScale = nextSmallerZoom(zoomLevels, getZoomFactor());
		setZoom(desiredScale);
	}

	@Override
	public void zoomOut(final IntCoords ctr) {
		final double newScale = nextSmallerZoom(zoomLevels, getZoomFactor());
		setZoom(newScale, center);
	}

	@Override
	public void zoomToFit(final IntCoords topLeft, final IntCoords bottomRight) {
		final RealCoords imageTopLeft = panelToImageCoords(topLeft);
		final RealCoords imageBottomRight = panelToImageCoords(bottomRight);
		final double newCenterX = Math.abs(imageBottomRight.x + imageTopLeft.x) / 2;
		final double newCenterY = Math.abs(imageBottomRight.y + imageTopLeft.y) / 2;
		final double imageSizeX = Math.abs(imageBottomRight.x - imageTopLeft.x);
		final double imageSizeY = Math.abs(imageBottomRight.y - imageTopLeft.y);
		final double xZoom = getViewportWidth() / imageSizeX;
		final double yZoom = getViewportHeight() / imageSizeY;
		final double factor = Math.min(xZoom, yZoom);
		if (scaleOutOfBounds(factor)) return;

		doSetZoomAndCenter(factor, newCenterX, newCenterY);
	}

	@Override
	public void zoomToFit(final RealRect viewportRect) {
		final double newCenterX = (viewportRect.x + viewportRect.width / 2);
		final double newCenterY = (viewportRect.y + viewportRect.height / 2);
		final double minScale =
			Math.min(getViewportWidth() / viewportRect.width, getViewportHeight() /
				viewportRect.height);
		if (scaleOutOfBounds(minScale)) return;
		doSetZoomAndCenter(minScale, newCenterX, newCenterY);
	}

	@Override
	public double getZoomFactor() {
		return this.scale;
	}

	@Override
	public RealRect getViewportImageRect() {
		final RealCoords topLeft = panelToImageCoords(new IntCoords(0, 0));
		final RealCoords bottomRight =
			panelToImageCoords(new IntCoords(getViewportWidth(), getViewportHeight()));
		return new RealRect(topLeft.x, topLeft.y, bottomRight.x - topLeft.x,
			bottomRight.y - topLeft.y);
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
	}

	@Override
	public boolean isInImage(final IntCoords point) {
		final RealCoords imageCoords = panelToImageCoords(point);
		return getImageExtents().contains(imageCoords);
	}

	@Override
	public RealCoords panelToImageCoords(final IntCoords panelCoords) {
		final double imageX = panelCoords.x / getZoomFactor() + getLeftImageX();
		final double imageY = panelCoords.y / getZoomFactor() + getTopImageY();
		return new RealCoords(imageX, imageY);
	}

	@Override
	public IntCoords imageToPanelCoords(final RealCoords imageCoords) {
		final int panelX =
			(int) Math.round(getZoomFactor() * (imageCoords.x - getLeftImageX()));
		final int panelY =
			(int) Math.round(getZoomFactor() * (imageCoords.y - getTopImageY()));
		return new IntCoords(panelX, panelY);
	}

	@Override
	public MouseCursor getCursor() {
		return mouseCursor;
	}

	@Override
	public void setCursor(final MouseCursor cursor) {
		mouseCursor = cursor;
		final EventService eventService = getEventService();
		if (eventService != null) eventService.publish(new MouseCursorEvent(this));
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

	/**
	 * Sets the canvas's center X and Y and publish an event that tells the world
	 * that the viewport mapping changed.
	 */
	private void doSetCenter(final double x, final double y) {
		if (center == null) {
			center = new RealCoords(x, y);
		}
		else {
			center.x = x;
			center.y = y;
		}
		publishZoomEvent();
	}

	/**
	 * Sets the canvas's zoom scale and publish an event that tells the world that
	 * the viewport mapping changed.
	 */
	private void doSetZoom(final double scaleFactor) {
		this.scale = scaleFactor;
		publishZoomEvent();
	}

	/**
	 * Sets the canvas's X, Y and scale simultaneously and publish an event that
	 * tells the world that the viewport mapping changed.
	 */
	private void doSetZoomAndCenter(final double scaleFactor, final double x,
		final double y)
	{
		if (center == null) {
			center = new RealCoords(x, y);
		}
		else {
			center.x = x;
			center.y = y;
		}
		this.scale = scaleFactor;
		publishZoomEvent();
	}

	private void publishZoomEvent() {
		final ImageJ context = getDisplay().getContext();
		if (context == null) return;
		final EventService eventService = getEventService();
		if (eventService != null) eventService.publish(new ZoomEvent(this));
	}

	// -- Helper methods --

	/** Gets the log to which messages should be sent. */
	private LogService getLog() {
		final ImageJ context = display.getContext();
		if (context == null) return null;
		return context.getService(LogService.class);
	}

	/** Gets the service to which events should be published. */
	private EventService getEventService() {
		final ImageJ context = display.getContext();
		if (context == null) return null;
		return context.getService(EventService.class);
	}

	/** Gets the coordinate of the left edge of the viewport in image space. */
	private double getLeftImageX() {
		return getPanCenter().x - getViewportWidth() / getZoomFactor() / 2;
	}

	/** Gets the coordinate of the top edge of the viewport in image space. */
	private double getTopImageY() {
		return getPanCenter().y - getViewportHeight() / getZoomFactor() / 2;
	}

	/** Checks whether the given scale is out of bounds. */
	private boolean scaleOutOfBounds(final double desiredScale) {
		if (desiredScale <= 0) {
			final LogService log = getLog();
			if (log != null) log.warn("**** BAD SCALE: " + desiredScale + " ****");
			return true;
		}

		if (desiredScale > maxZoom) return true;

		// check if trying to zoom out too far
		if (desiredScale < getZoomFactor()) {
			// get boundaries of image in panel coords
			final RealRect displayExtents = getImageExtents();
			final IntCoords nearCornerPanel =
				imageToPanelCoords(new RealCoords(displayExtents.x, displayExtents.y));
			final IntCoords farCornerPanel =
				imageToPanelCoords(new RealCoords(displayExtents.x +
					displayExtents.width, displayExtents.y + displayExtents.height));

			// if boundaries take up less than min allowed pixels in either dimension
			final int panelX = farCornerPanel.x - nearCornerPanel.x;
			final int panelY = farCornerPanel.y - nearCornerPanel.y;
			if (panelX < MIN_ALLOWED_VIEW_SIZE && panelY < MIN_ALLOWED_VIEW_SIZE) {
				return true;
			}
		}

		return false;
	}

	/** Gets the extents of the display in image coordinates. */
	private RealRect getImageExtents() {
		return getDisplay().getImageExtents();
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
