//
// NavigableImageCanvas.java
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

import imagej.util.RealCoords;
import imagej.util.Rect;

/**
 * TODO
 * 
 * @author Grant Harris
 */
public interface NavigableImageCanvas extends ImageCanvas {

	/***
	 * This is an interface extracted from NavigableImagePanel GBH, March 17, 2011
	 * Modified later by BDZ
	 */

	// set EventDispatcher as event listener on this - esp. for key, mouse events
	void addEventDispatcher(EventDispatcher dispatcher);

	/** get the x coordinate of the image space point currently displayed in
	 * the upper left corner of the window. Unlike the setters these results are
	 * in image coordinate space.
	 */
	double getPanX();
	
	/** get the y coordinate of the image space point currently displayed in
	 * the upper left corner of the window. Unlike the setters these results are
	 * in image coordinate space.
	 */
	double getPanY();
	
	/** get the x coordinate of the image space point currently displayed in
	 * the center of the window. Unlike the setters these results are
	 * in image coordinate space.
	 */
	double getZoomCtrX();
	
	/** get the y coordinate of the image space point currently displayed in
	 * the center of the window. Unlike the setters these results are
	 * in image coordinate space.
	 */
	double getZoomCtrY();

	/**
	 * Gets the current zoom level.
	 * 
	 * @return the current zoom level
	 */
	double getZoom();

	/**
	 * Gets the current zoom device.
	 */
	// ZoomDevice getZoomDevice();

	/**
	 * Indicates whether the high quality rendering feature is enabled.
	 * 
	 * @return true if high quality rendering is enabled, false otherwise.
	 */
	boolean isHighQualityRenderingEnabled();

	/**
	 * Indicates whether navigation image is enabled.
	 * 
	 * @return true when navigation image is enabled, false otherwise.
	 */
	boolean isNavigationImageEnabled();

	/**
	 * Pans the image by the given (X, Y) amount. Note that X & Y are in
	 * panel coordinate space (pixels).
	 */
	void pan(double xDelta, double yDelta);

	/**
	 * Pans the image to the given (X, Y). Note that X & Y are in
	 * panel coordinate space (pixels).
	 */
	void setPan(double x, double y);

	/**
	 * Enables/disables high quality rendering.
	 * 
	 * @param enabled enables/disables high quality rendering
	 */
	void setHighQualityRenderingEnabled(boolean enabled);

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
	void setNavigationImageEnabled(boolean enabled);

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
	void setZoom(double newZoom);

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
	void setZoom(double newZoom, double centerX, double centerY);

	void zoomToFit(Rect rectangle);
	
	/**
	 * Sets a new zoom device.
	 * 
	 * @param newZoomDevice specifies the type of a new zoom device.
	 */
	// void setZoomDevice(ZoomDevice newZoomDevice);

	/**
	 * Sets a new zooming scale factor value.
	 * 
	 * @param newZoomFactor new zoom factor value
	 */
	void setZoomMultiplier(double newZoomMultiplier);

	double getZoomMultiplier();
	
	// Is this point in the image as displayed in the panel
	boolean isInImage(RealCoords p);

	// Converts this panel's coordinates into the original image coordinates
	RealCoords panelToImageCoords(RealCoords p);

	// Converts the original image coordinates into this panel's coordinates
	RealCoords imageToPanelCoords(RealCoords p);

	void setCursor(MouseCursor cursor);

	void subscribeToToolEvents();

}
