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

import org.scijava.input.MouseCursor;
import org.scijava.util.IntCoords;
import org.scijava.util.RealCoords;

/**
 * A canvas upon which an {@link ImageDisplay} draws its output.
 * <p>
 * An image canvas supports two different coordinate systems, and provides a
 * mechanism for conversion between them. The first is <em>panel</em>
 * coordinates, which are in pixels onscreen. The second is <em>data</em>
 * coordinates, which match the linked {@link ImageDisplay}'s original
 * coordinate system.
 * </p>
 * 
 * @author Grant Harris
 * @author Curtis Rueden
 */
public interface ImageCanvas extends Pannable, Zoomable {

	/** Gets the canvas's display. */
	ImageDisplay getDisplay();

	/**
	 * Gets the current width of the canvas viewport in <em>panel</em>
	 * coordinates.
	 */
	int getViewportWidth();

	/**
	 * Gets the current height of the canvas viewport in <em>panel</em>
	 * coordinates.
	 */
	int getViewportHeight();

	/** Sets the dimensions of the viewport in <em>panel</em> coordinates. */
	void setViewportSize(int width, int height);

	/**
	 * Tests whether a given point in the panel falls within the boundaries of the
	 * display space.
	 * 
	 * @param point The point to check, in <em>panel</em> coordinates.
	 */
	boolean isInImage(IntCoords point);

	/**
	 * Converts the given <em>panel</em> coordinates into <em>data</em>
	 * coordinates.
	 */
	RealCoords panelToDataCoords(IntCoords panelCoords);

	/**
	 * Converts the given <em>data</em> coordinates into <em>panel</em>
	 * coordinates.
	 */
	IntCoords dataToPanelCoords(RealCoords dataCoords);

	/** Gets the current mouse cursor. */
	MouseCursor getCursor();

	/** Sets the mouse to the given {@link MouseCursor} type. */
	void setCursor(MouseCursor cursor);

}
