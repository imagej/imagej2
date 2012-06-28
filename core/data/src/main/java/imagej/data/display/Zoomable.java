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

import imagej.util.IntCoords;
import imagej.util.IntRect;
import imagej.util.RealCoords;
import imagej.util.RealRect;

/**
 * Defines methods needed to adjust the zoom of an {@link ImageCanvas}.
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
	 * Set the zoom and center the viewport within the image.
	 * 
	 * @param factor
	 */
	void setZoomAndCenter(double factor);

	/**
	 * Recenters the image around the given <em>image</em> coordinates, then sets
	 * the zoom level.
	 * 
	 * @param factor The new zoom level.
	 * @param center The center in <em>image</em> coordinates.
	 */
	void setZoom(double factor, RealCoords center);

	/**
	 * Recenters the image around the given <em>panel</em> coordinates, then sets
	 * the zoom level.
	 * 
	 * @param factor The new zoom level.
	 * @param center The center in <em>panel</em> coordinates.
	 */
	void setZoom(double factor, IntCoords center);

	/** Zooms in by the default amount, centered around the panel's center. */
	void zoomIn();

	/**
	 * Zooms in by the default amount, centered around the given <em>image</em>
	 * coordinates.
	 * 
	 * @param center The zoom center, in <em>image</em> coordinates.
	 */
	void zoomIn(RealCoords center);

	/**
	 * Zooms in by the default amount, centered around the given <em>panel</em>
	 * coordinates.
	 * 
	 * @param center The zoom center, in <em>panel</em> coordinates.
	 */
	void zoomIn(IntCoords center);

	/** Zooms out by the default amount, centered around the panel's center. */
	void zoomOut();

	/**
	 * Zooms out by the default amount, centered around the given <em>image</em>
	 * coordinates.
	 * 
	 * @param center The zoom center, in <em>image</em> coordinates.
	 */
	void zoomOut(RealCoords center);

	/**
	 * Zooms out by the default amount, centered around the given <em>panel</em>
	 * coordinates.
	 * 
	 * @param center The zoom center, in <em>panel</em> coordinates.
	 */
	void zoomOut(IntCoords center);

	/**
	 * Zoom the viewport to fit the given bounding box in <em>image</em>
	 * coordinates.
	 * 
	 * @param viewportBox The viewport bounding box, in <em>image</em>
	 *          coordinates.
	 */
	void zoomToFit(RealRect viewportBox);

	/**
	 * Zooms the viewport to fit the given bounding box in <em>panel</em>
	 * coordinates.
	 * 
	 * @param viewportBox The viewport bounding box, in <em>panel</em>
	 *          coordinates.
	 */
	void zoomToFit(IntRect viewportBox);

	/** Gets the current zoom level. */
	double getZoomFactor();

	/** Gets the scale to use when reverting after zooming. */
	double getInitialScale();

	/** Sets the scale to use when reverting after zooming. */
	void setInitialScale(double zoomFactor);

	/** Gets the closest step-wise zoom factor below the given scale. */
	double getBestZoomLevel(final double fractionalScale);

}
