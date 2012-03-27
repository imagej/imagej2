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

package imagej.data.overlay;

import imagej.data.Data;
import imagej.data.Dataset;
import imagej.util.ColorRGB;
import net.imglib2.roi.RegionOfInterest;

/**
 * An overlay is a vector object, typically displayed with {@link Dataset}s.
 * Many (but not all) overlays have an associated ImgLib
 * {@link RegionOfInterest} allowing iteration over pixel values deemed included
 * in the overlay.
 * 
 * @author Lee Kamentsky
 * @author Curtis Rueden
 */
public interface Overlay extends Data {

	/** The style used to render the bounding line of the overlay. */
	enum LineStyle {
		SOLID, DASH, DOT, DOT_DASH, NONE
	}

	/** Arrow decorations for the start and end. */
	enum ArrowStyle {
		NONE, ARROW
	}

	/**
	 * Retrieves the ImgLib region of interest, if any, that knows how to
	 * determine whether a pixel is included in the overlay.
	 * 
	 * @return the region of interest or null if none supported.
	 */
	RegionOfInterest getRegionOfInterest();

	/**
	 * @return the opacity of the interior of the overlay, from 0-255
	 */
	int getAlpha();

	/**
	 * @param alpha the opacity of the interior of the overlay, from 0-255
	 */
	void setAlpha(int alpha);

	/**
	 * @return the fill color for overlays that have an interior
	 */
	ColorRGB getFillColor();

	/**
	 * @param fillColor the fill color for overlays that have an interior
	 */
	void setFillColor(ColorRGB fillColor);

	/**
	 * @return the color of the outline line of the overlay
	 */
	ColorRGB getLineColor();

	/**
	 * Set the color of the outline line of the overlay, if appropriate
	 * 
	 * @param lineColor
	 */
	void setLineColor(ColorRGB lineColor);

	/**
	 * @return the width of the outline line in pixels
	 */
	double getLineWidth();

	/**
	 * Set the width of the outline line of the overlay, if appropriate
	 * 
	 * @param lineWidth the width to be used when painting lines and shape
	 *          borders, in pixels.
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
	 * @return the arrow style at the start of a line or path (if appropriate)
	 */
	ArrowStyle getLineStartArrowStyle();

	/**
	 * @param style the arrow style to be shown at the start of a line or path
	 */
	void setLineStartArrowStyle(ArrowStyle style);

	/**
	 * @return the arrow style at the end of a line or path (if appropriate)
	 */
	ArrowStyle getLineEndArrowStyle();

	/**
	 * @param style the arrow style to be shown at the end of a line or path
	 */
	void setLineEndArrowStyle(ArrowStyle style);

}
