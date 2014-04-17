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
import org.scijava.util.RealCoords;

/**
 * Defines methods needed to adjust the pan position of a viewport, such as an
 * {@link ImageCanvas}.
 * <p>
 * The pan position is the center of the viewport. The assumption is that the
 * user cares most about what's in the center of the viewport, especially for
 * operations like a pure zoom.
 * </p>
 * 
 * @author Grant Harris
 * @author Curtis Rueden
 * @author Barry DeZonia
 * @author Lee Kamentsky
 * @see ImageCanvas
 */
public interface Pannable {

	/**
	 * Gets the coordinates of the <em>data</em> space point currently displayed
	 * in the center of the viewport.
	 * 
	 * @return Pan center coordinates, in <em>data</em> coordinate space.
	 */
	RealCoords getPanCenter();

	/**
	 * Gets the absolute offset of the viewport in pixels from the top left.
	 * 
	 * @return Pan offset in pixels; note that this value is not in <em>panel</em>
	 *         coordinate space, since the pan offset in <em>panel</em> coordinate
	 *         space is always (0, 0).
	 */
	IntCoords getPanOffset();

	/**
	 * Pans to the given absolute (X, Y) position in <em>data</em> coordinate
	 * space.
	 * 
	 * @param center Absolute coordinates, in <em>data</em> coordinate space.
	 */
	void setPanCenter(RealCoords center);

	/**
	 * Pans to the given absolute (X, Y) position in <em>panel</em> coordinate
	 * space.
	 * 
	 * @param center Absolute coordinates, in <em>panel</em> coordinate space.
	 */
	void setPanCenter(IntCoords center);

	/**
	 * Adjusts the pan by the given (X, Y) amount.
	 * 
	 * @param delta Pan modifier, in <em>data</em> coordinate space.
	 */
	void pan(RealCoords delta);

	/**
	 * Adjusts the pan by the given (X, Y) amount in <em>panel</em> space.
	 * 
	 * @param delta Pan modifier, in <em>panel</em> coordinate space.
	 */
	void pan(IntCoords delta);

	/** Resets the pan origin to the center of the display. */
	void panReset();

}
