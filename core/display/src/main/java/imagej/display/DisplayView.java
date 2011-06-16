//
// DisplayView.java
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

import imagej.data.DataObject;
import imagej.data.Dataset;
import imagej.data.roi.Overlay;

/**
 * A linkage between a {@link DataObject} (such as a {@link Dataset} or
 * {@link Overlay}) and a {@link Display}. The view takes care of mapping the
 * N-dimensional data into a representation suitable for showing onscreen.
 * <p>
 * For example, a typical 2D display may have a number of sliders enabling a
 * user to select a particular plane of a {@link Dataset} for display. The
 * DisplayView keeps track of the current position and provides access the
 * resultant plane.
 * </p>
 * 
 * @author Curtis Rueden
 */
public interface DisplayView {

	/** Gets the {@link Display} containing this view. */
	Display getDisplay();

	/** Gets the {@link DataObject} represented by this view. */
	DataObject getDataObject();

	/** Gets the N-dimensional plane position of this view. */
	long[] getPlanePosition();

	/** Gets the 1-dimensional plane index of this view. */
	long getPlaneIndex();

	/** gets the position of the given dimensional axis. */
	long getPosition(final int dim);

	/** Sets the position of the given dimensional axis. */
	void setPosition(final int value, final int dim);

	/** Gets the view's ideal width in pixels. */
	int getPreferredWidth();

	/** Gets the view's ideal height in pixels. */
	int getPreferredHeight();

	/** Updates and redraws the view onscreen. */
	void update();

	/**
	 * Recreates the view. This operation is useful in case the linked
	 * {@link DataObject} has changed structurally somehow.
	 */
	void rebuild();

	/** Discards the view, performing any needed cleanup. */
	void dispose();
	
	/**
	 * Set the view's selection state.
	 * 
	 * @param isSelected - true if selected, false if not.
	 */
	void setSelected(boolean isSelected);

	/**
	 * @return the view's selection state.
	 */
	boolean isSelected();
	
	/**
	 * @return true if the data object (or some part of it) is visible, given the current (hyper)plane position
	 */
	boolean isVisible();

}
