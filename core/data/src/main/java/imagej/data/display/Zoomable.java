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

import org.scijava.util.IntCoords;
import org.scijava.util.IntRect;
import org.scijava.util.RealCoords;
import org.scijava.util.RealRect;

/**
 * Defines methods needed to adjust the zoom of an {@link ImageCanvas}.
 * 
 * @author Barry DeZonia
 * @author Grant Harris
 * @author Curtis Rueden
 */
public interface Zoomable {

	/**
	 * Zooms to the given scale factor, without changing the viewport's center.
	 * 
	 * @param factor The new scale factor.
	 */
	void setZoom(double factor);

	/**
	 * Zooms to the given scale factor, recentering the viewport at the center of
	 * the <em>data</em> space.
	 * 
	 * @param factor The new scale factor.
	 */
	void setZoomAndCenter(double factor);

	/**
	 * Zooms to the given scale factor, recentering the viewport at the specified
	 * <em>data</em> coordinates.
	 * 
	 * @param factor The new scale factor.
	 * @param center Absolute coordinates, in <em>data</em> coordinate space.
	 */
	void setZoomAndCenter(double factor, RealCoords center);

	/**
	 * Zooms to the given scale factor, such that the specified position in
	 * <em>data</em> coordinates remains at the same place in the viewport.
	 * <p>
	 * This is useful for repeatedly zooming at a chosen point in the data.
	 * </p>
	 * 
	 * @param factor The new scale factor.
	 * @param pos The <em>data</em> coordinates to keep in the same place within
	 *          the viewport.
	 */
	void setZoomAtPoint(double factor, RealCoords pos);

	/**
	 * Zooms to the given scale factor, such that the specified position in
	 * <em>panel</em> coordinates remains at the same place in the viewport.
	 * <p>
	 * This is useful for repeatedly zooming at a clicked point.
	 * </p>
	 * 
	 * @param factor The new scale factor.
	 * @param pos The <em>panel</em> coordinates to keep in the same place within
	 *          the viewport.
	 */
	void setZoomAtPoint(double factor, IntCoords pos);

	/** Zooms in by the default amount, without changing the viewport's center. */
	void zoomIn();

	/**
	 * Zooms in by the default amount, such that the specified position in
	 * <em>data</em> coordinates remains at the same place in the viewport.
	 * 
	 * @param pos The <em>data</em> coordinates to keep in the same place within
	 *          the viewport.
	 */
	void zoomIn(RealCoords pos);

	/**
	 * Zooms in by the default amount, such that the specified position in
	 * <em>panel</em> coordinates remains at the same place in the viewport.
	 * 
	 * @param pos The <em>panel</em> coordinates to keep in the same place within
	 *          the viewport.
	 */
	void zoomIn(IntCoords pos);

	/**
	 * Zooms out by the default amount, without changing the viewport's center.
	 */
	void zoomOut();

	/**
	 * Zooms out by the default amount, such that the specified position in
	 * <em>data</em> coordinates remains at the same place in the viewport.
	 * 
	 * @param pos The <em>data</em> coordinates to keep in the same place within
	 *          the viewport.
	 */
	void zoomOut(RealCoords pos);

	/**
	 * Zooms out by the default amount, such that the specified position in
	 * <em>panel</em> coordinates remains at the same place in the viewport.
	 * 
	 * @param pos The <em>panel</em> coordinates to keep in the same place within
	 *          the viewport.
	 */
	void zoomOut(IntCoords pos);

	/**
	 * Zoom the viewport to fit the given bounding box in <em>data</em>
	 * coordinates.
	 * 
	 * @param viewportBox The viewport bounding box, in <em>data</em> coordinates.
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
