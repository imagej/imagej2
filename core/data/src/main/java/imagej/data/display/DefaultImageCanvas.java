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

import imagej.IContext;
import imagej.data.display.event.MouseCursorEvent;
import imagej.data.display.event.ZoomEvent;
import imagej.event.EventService;
import imagej.ext.MouseCursor;
import imagej.util.IntCoords;
import imagej.util.IntRect;
import imagej.util.RealCoords;
import imagej.util.RealRect;

/**
 * @author Lee Kamentsky
 * 
 * The DefaultImageCanvas maintains a viewport, a zoom scale
 * and a center coordinate that it uses to map viewport pixels
 * to display coordinates. It also maintains an abstract mouse
 * cursor.
 * 
 * The canvas sends a zoom event whenever it is panned or
 * zoomed. It sends a mouse event whenever the mouse changes. 
 *
 */
public class DefaultImageCanvas implements ImageCanvas {
	private final ImageDisplay display;
	private final EventService eventService;
	private MouseCursor mouseCursor;
	private CanvasHelper canvasHelper;
	private final RealCoords center;
	private final IntCoords viewportSize;
	private double scale = 1.0;

	public DefaultImageCanvas(ImageDisplay display) {
		this.display = display;
		eventService = display.getContext().getService(EventService.class);
		mouseCursor = MouseCursor.DEFAULT;
		canvasHelper = new CanvasHelper(this);
		// The center is set by panReset. The viewport size
		// will be initialized once a panel is attached to the UI.
		center = new RealCoords(0, 0);
		viewportSize = new IntCoords(100, 100);
		panReset();
	}
	//-- Pannable methods --//
	@Override
	public void pan(IntCoords delta) {
		canvasHelper.pan(delta);
	}
	@Override
	public void setPan(RealCoords center) {
		canvasHelper.setPan(center);
	}
	@Override
	public void panReset() {
		canvasHelper.panReset();
	}
	@Override
	public RealCoords getPanCenter() {
		return new RealCoords(center.x, center.y);
	}
	//-- Zoomable methods --//
	@Override
	public void setZoom(double factor) {
		canvasHelper.setZoom(factor);
	}
	@Override
	public void setZoom(double factor, IntCoords center) {
		canvasHelper.setZoom(factor, center);
	}

	@Override
	public void setZoom(double factor, RealCoords center) {
		canvasHelper.setZoom(factor, center);
	}
	@Override
	public void setZoomAndCenter(double factor) {
		canvasHelper.setZoomAndCenter(factor);
	}
	@Override
	public void zoomIn() {
		canvasHelper.zoomIn();
	}
	@Override
	public void zoomIn(IntCoords center) {
		canvasHelper.zoomIn(center);
	}
	@Override
	public void zoomOut() {
		canvasHelper.zoomOut();
	}
	@Override
	public void zoomOut(IntCoords center) {
		canvasHelper.zoomOut(center);
	}
	@Override
	public void zoomToFit(IntCoords topLeft, IntCoords bottomRight) {
		canvasHelper.zoomToFit(topLeft, bottomRight);
	}
	@Override
	public double getZoomFactor() {
		return this.scale;
	}
	@Override
	public RealRect getViewportImageRect() {
		return canvasHelper.getViewportImageRect();
	}
	//-- ImageCanvas methods --//
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
	public boolean isInImage(IntCoords point) {
		return canvasHelper.isInImage(point);
	}
	@Override
	public RealCoords panelToImageCoords(IntCoords panelCoords) {
		return canvasHelper.panelToImageCoords(panelCoords);
	}
	@Override
	public IntCoords imageToPanelCoords(RealCoords imageCoords) {
		return canvasHelper.imageToPanelCoords(imageCoords);
	}
	@Override
	public MouseCursor getCursor() {
		return mouseCursor;
	}
	@Override
	public void setCursor(MouseCursor cursor) {
		mouseCursor = cursor;
		eventService.publish(new MouseCursorEvent(this));
	}
	
	/**
	 * Set the canvas's center X and Y and
	 * publish an event that tells the world that
	 * the viewport mapping changed.
	 * 
	 * @param x
	 * @param y
	 */
	void doSetCenter(double x, double y) {
		center.x = x;
		center.y = y;
		publishZoomEvent();
	}
	
	/**
	 * Set the canvas's zoom scale and publish
	 * an event that tells the world that the
	 * viewport mapping changed.
	 *  
	 * @param scale
	 */
	void doSetZoom(double scale) {
		this.scale = scale;
		publishZoomEvent();
	}
	
	/**
	 * Set the canvas's X, Y and scale simultaneously
	 * and publish an event that tells the world that
	 * the viewport mapping changed.
	 * 
	 * @param scale
	 * @param x
	 * @param y
	 */
	void doSetZoomAndCenter(double scale, double x, double y) {
		center.x = x;
		center.y = y;
		this.scale = scale;
		publishZoomEvent();
	}
	
	//-- helper methods --//
	
	private void publishZoomEvent() {
		if (eventService != null)
			eventService.publish(new ZoomEvent(this));
	}
}
