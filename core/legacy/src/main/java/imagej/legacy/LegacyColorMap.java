//
// LegacyColorMap.java
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

package imagej.legacy;

import java.awt.Color;

import imagej.util.ColorRGB;
import imagej.util.JavaColorMap;

/**
 * This class maintains a mapping between IJ1 & IJ2 colors
 * 
 * @author bdezonia
 *
 */
public class LegacyColorMap {
	
	private LegacyColorMap() {
		// do not instantiate utility class
	}
	
	static public ColorRGB getIJ2Color(Color ij1Color) {
		if (ij1Color.equals(Color.black)) return JavaColorMap.BASIC_BLACK;
		if (ij1Color.equals(Color.blue)) return JavaColorMap.BASIC_BLUE;
		if (ij1Color.equals(Color.cyan)) return JavaColorMap.BASIC_CYAN;
		if (ij1Color.equals(Color.gray)) return JavaColorMap.BASIC_GRAY;
		if (ij1Color.equals(Color.green)) return JavaColorMap.BASIC_GREEN;
		if (ij1Color.equals(Color.magenta)) return JavaColorMap.BASIC_MAGENTA;
		if (ij1Color.equals(Color.orange)) return JavaColorMap.BASIC_ORANGE;
		if (ij1Color.equals(Color.pink)) return JavaColorMap.BASIC_PINK;
		if (ij1Color.equals(Color.red)) return JavaColorMap.BASIC_RED;
		if (ij1Color.equals(Color.white)) return JavaColorMap.BASIC_WHITE;
		if (ij1Color.equals(Color.yellow)) return JavaColorMap.BASIC_YELLOW;
		return new ColorRGB(ij1Color.getRed(), ij1Color.getGreen(), ij1Color.getBlue());
	}
	
	static public Color getIJ1Color(ColorRGB ij2Color) {
		if (ij2Color.equals(JavaColorMap.BASIC_BLACK)) return Color.black;
		if (ij2Color.equals(JavaColorMap.BASIC_BLUE)) return Color.blue;
		if (ij2Color.equals(JavaColorMap.BASIC_CYAN)) return Color.cyan;
		if (ij2Color.equals(JavaColorMap.BASIC_GRAY)) return Color.gray;
		if (ij2Color.equals(JavaColorMap.BASIC_GREEN)) return Color.green;
		if (ij2Color.equals(JavaColorMap.BASIC_MAGENTA)) return Color.magenta;
		if (ij2Color.equals(JavaColorMap.BASIC_ORANGE)) return Color.orange;
		if (ij2Color.equals(JavaColorMap.BASIC_PINK)) return Color.pink;
		if (ij2Color.equals(JavaColorMap.BASIC_RED)) return Color.red;
		if (ij2Color.equals(JavaColorMap.BASIC_WHITE)) return Color.white;
		if (ij2Color.equals(JavaColorMap.BASIC_YELLOW)) return Color.yellow;
		return new Color(ij2Color.getRed(), ij2Color.getGreen(), ij2Color.getBlue());
	}
}
