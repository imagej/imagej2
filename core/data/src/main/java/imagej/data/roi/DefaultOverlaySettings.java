//
// DefaultOverlaySettings.java
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

import imagej.data.roi.Overlay.ArrowStyle;
import imagej.data.roi.Overlay.LineStyle;
import imagej.util.ColorRGB;


/**
 * Utility class that gives access to default ovelay settings such as fill,
 * transparency, arrow styles, etc. There is one DefaultOverlaySettings class
 * per OverlayService instance.
 * 
 * @author Barry DeZonia
 *
 */
public class DefaultOverlaySettings {
	
	// -- instance variables --
	
	private double defaultLineWidth = 1.0;
	private LineStyle defaultLineStyle = LineStyle.SOLID;
	private ColorRGB defaultLineColor = new ColorRGB(255, 255, 0);
	private ColorRGB defaultFillColor = new ColorRGB(0, 255, 0);
	private int defaultAlpha = 0;
	private ArrowStyle defaultStartArrowStyle = ArrowStyle.NONE;
	private ArrowStyle defaultEndArrowStyle = ArrowStyle.NONE;

	// -- accessors --
	
	public double getDefaultLineWidth() { return defaultLineWidth; }
	public void setDefaultLineWidth(double width) { defaultLineWidth = width; }

	public LineStyle getDefaultLineStyle() { return defaultLineStyle; }
	public void setDefaultLineStyle(LineStyle style) { defaultLineStyle = style; }

	public ColorRGB getDefaultLineColor() { return defaultLineColor; }
	public void setDefaultLineColor(ColorRGB c) { defaultLineColor = c; }
	
	public ColorRGB getDefaultFillColor() { return defaultFillColor; }
	public void setDefaultFillColor(ColorRGB c) { defaultFillColor = c; }
	
	public int getDefaultAlpha() { return defaultAlpha; }
	public void setDefaultAlpha(int alpha) { defaultAlpha = alpha; }
	
	public ArrowStyle getDefaultStartArrowStyle() { return defaultStartArrowStyle; }
	public void setDefaultStartArrowStyle(ArrowStyle style) { defaultStartArrowStyle = style; }
	
	public ArrowStyle getDefaultEndArrowStyle() { return defaultEndArrowStyle; }
	public void setDefaultEndArrowStyle(ArrowStyle style) { defaultEndArrowStyle = style; }
}
