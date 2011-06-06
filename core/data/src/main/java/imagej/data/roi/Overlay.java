//
// Overlay.java
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

package imagej.data.roi;

import imagej.data.DataObject;
import imagej.data.Dataset;
import imagej.util.ColorRGB;
import net.imglib2.roi.RegionOfInterest;

/**
 * An overlay is a 2D vector object, typically displayed with {@link Dataset}s.
 * Many (but not all) overlays have an associated ImgLib
 * {@link RegionOfInterest} allowing iteration over pixel values deemed included
 * in the overlay.
 * <p>
 * This data model is also completely planar in nature, and does not provide
 * represention of ROIs in 3D and beyond.
 * </p>
 * 
 * @author Lee Kamentsky
 * @author Curtis Rueden
 */
public interface Overlay extends DataObject {

	/** The style used to render the bounding line of the overlay. */
	public enum LineStyle {
		SOLID, DASH, DOT, DOT_DASH, NONE
	}

	/**
	 * Retrieves the ImgLib region of interest, if any, that knows how to
	 * determine whether a pixel is included in the overlay.
	 * 
	 * @return the region of interest or null if none supported.
	 */
	RegionOfInterest getRegionOfInterest();

	/**
	 * Set the color of the outline line of the overlay, if appropriate
	 * 
	 * @param lineColor
	 */
	void setLineColor(ColorRGB lineColor);

	/**
	 * Set the width of the outline line of the overlay, if appropriate
	 * 
	 * @param lineWidth the width in pixels
	 */
	void setLineWidth(double lineWidth);

	/**
	 * Get the style of the outline line
	 * 
	 * @return the outline line style.
	 */
	LineStyle getLineStyle();

	/**
	 * Set the style of the outline line of the overlay, if appropriate
	 * 
	 * @param style - style of line
	 */
	void setLineStyle(LineStyle style);

	/**
	 * @return the color of the outline line of the overlay
	 */
	ColorRGB getLineColor();

	/**
	 * @return the width of the outline line in pixels
	 */
	double getLineWidth();

	/**
	 * @return the fill color for overlays that have an interior
	 */
	ColorRGB getFillColor();

	/**
	 * @param fillColor the fill color for overlays that have an interior
	 */
	void setFillColor(ColorRGB fillColor);

	/**
	 * @return the opacity of the interior of the overlay, from 0-255
	 */
	int getAlpha();

	/**
	 * @param alpha the opacity of the interior of the overlay, from 0-255
	 */
	void setAlpha(int alpha);

}
