//
// DataView.java
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

package imagej.data.display;

import imagej.data.Data;
import imagej.data.Dataset;
import imagej.data.Position;
import imagej.data.PositionableByAxis;
import imagej.data.overlay.Overlay;

/**
 * A view provides visualization settings for an associated {@link Data} object
 * (such as a {@link Dataset} or {@link Overlay}) for use with an
 * {@link ImageDisplay}. The view keeps track of the currently visualized
 * position within the N-dimensional data space, as well as color settings and
 * other view-specific metadata.
 * <p>
 * For example, a typical 2D display may have a number of sliders enabling a
 * user to select a particular plane of a {@link Dataset} for display. The view
 * keeps track of the current position and provides access to the resultant
 * plane.
 * </p>
 * 
 * @author Curtis Rueden
 */
public interface DataView extends PositionableByAxis {

	/** Gets the {@link Data} represented by this view. */
	Data getData();

	/** Gets the N-dimensional plane position of this view. */
	Position getPlanePosition();

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
	 * @return true if the data object (or some part of it) is visible, given the
	 *         current (hyper)plane position
	 */
	boolean isVisible();

	// CTR TODO - reevaluate the methods below, and potentially eliminate some.

	/** Gets the view's natural width in pixels. */
	int getPreferredWidth();

	/** Gets the view's natural height in pixels. */
	int getPreferredHeight();

	/** Updates and redraws the view onscreen. */
	void update();

	/**
	 * Recreates the view. This operation is useful in case the linked
	 * {@link Data} has changed structurally somehow.
	 */
	void rebuild();

	/** Discards the view, performing any needed cleanup. */
	void dispose();

}
