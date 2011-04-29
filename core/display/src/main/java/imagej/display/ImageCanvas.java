//
// ImageCanvas.java
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
import imagej.util.RealCoords;

/**
 * A canvas upon which a {@link Display} paints output image planes.
 * 
 * @author Grant Harris
 * @author Curtis Rueden
 */
public interface ImageCanvas extends Pannable, Zoomable {

	/** Gets the width of the current image. */
	int getImageWidth();

	/** Gets the height of the current image. */
	int getImageHeight();

	/**
	 * Adds the given {@link EventDispatcher} as an event handler; e.g., for key
	 * and mouse events.
	 */
	void addEventDispatcher(EventDispatcher dispatcher);

	/**
	 * Indicates whether the high quality rendering feature is enabled.
	 * 
	 * @return true if high quality rendering is enabled, false otherwise.
	 */
	boolean isHighQualityRenderingEnabled();

	/**
	 * Enables/disables high quality rendering.
	 * 
	 * @param enabled enables/disables high quality rendering
	 */
	void setHighQualityRenderingEnabled(boolean enabled);

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

	/** Handles setting of the cursor depending on the activated tool. */
	void setCursor(MouseCursor cursor);

}
