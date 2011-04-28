//
// Zoomable.java
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

import imagej.util.IntCoords;

/**
 * Defines methods needed to adjust the zoom of a {@link Display} or
 * {@link ImageCanvas}.
 * 
 * @author Barry DeZonia
 * @author Grant Harris
 * @author Curtis Rueden
 */
public interface Zoomable {

	/**
	 * Sets the zoom level used to display the image.
	 * <p>
	 * This method is used in programmatic zooming. The zooming center is the
	 * point of the image closest to the center of the panel. After a new zoom
	 * level is set the image is repainted.
	 * </p>
	 * 
	 * @param factor the zoom level used to display this panel's image.
	 */
	void setZoom(double factor);

	/**
	 * Sets the zoom level used to display the image, and the zooming center,
	 * around which zooming is done.
	 * <p>
	 * This method is used in programmatic zooming. After a new zoom level is set
	 * the image is repainted.
	 * </p>
	 * 
	 * @param factor the zoom level used to display this panel's image.
	 * @param center the zoom center, in panel coordinates (pixels).
	 */
	void setZoom(double factor, IntCoords center);

	/** Zooms in by the default amount, centered around the panel's middle. */
	void zoomIn();

	/**
	 * Zooms in by the default amount, centered around the given coordinates.
	 * 
	 * @param center The zoom center, in panel coordinates (pixels).
	 */
	void zoomIn(IntCoords center);

	/** Zooms out by the default amount, centered around the panel's middle. */
	void zoomOut();

	/**
	 * Zooms out by the default amount, centered around the given coordinates.
	 * 
	 * @param center The zoom center, in panel coordinates (pixels).
	 */
	void zoomOut(IntCoords center);

	void zoomToFit(IntCoords topLeft, IntCoords bottomRight);

	/** Gets the current zoom level. */
	double getZoomFactor();

	/** Sets the amount zoomed in/out per operation. */
	void setZoomStep(double zoomStep);

	/** Gets the amount zoomed in/out per operation. */
	double getZoomStep();

}
