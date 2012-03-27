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

import imagej.ext.MouseCursor;
import imagej.util.IntCoords;
import imagej.util.RealCoords;

/**
 * A canvas upon which an {@link ImageDisplay} draws its output.
 * 
 * @author Grant Harris
 * @author Curtis Rueden
 */
public interface ImageCanvas extends Pannable, Zoomable {

	/** Gets the display to which this canvas belongs. */
	ImageDisplay getDisplay();

	/** Gets the unscaled width of the canvas. */
	int getCanvasWidth();

	/** Gets the unscaled height of the canvas. */
	int getCanvasHeight();

	/** Gets the current width of the canvas viewport in pixels. */
	int getViewportWidth();

	/** Gets the current height of the canvas viewport in pixels. */
	int getViewportHeight();

	/**
	 * Tests whether a given point in the panel falls within the image boundaries.
	 * 
	 * @param point The point to check, in panel coordinates (pixels).
	 */
	boolean isInImage(IntCoords point);

	/** Converts the given panel coordinates into original image coordinates. */
	RealCoords panelToImageCoords(IntCoords panelCoords);

	/** Converts the given original image coordinates into panel coordinates. */
	IntCoords imageToPanelCoords(RealCoords imageCoords);

	/** Sets the mouse to the given {@link MouseCursor} type. */
	void setCursor(MouseCursor cursor);

}
