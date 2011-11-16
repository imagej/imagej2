//
// JavaColorMap.java
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

package imagej.util;

/**
 * Collection of mappings from RGB colors to a basic color set that matches
 * Java's basic color naming scheme.
 * 
 * @author Barry DeZonia
 *
 */
public final class JavaColorMap {

	private JavaColorMap() {
		// do not instantiate utility class
	}
	
	// These following constants are used elsewhere to map IJ2 colors
	// to IJ1 Java colors. Also used in OptionsOverlay dialog.
	
	public static final ColorRGB BASIC_BLACK = Colors.BLACK;
	public static final ColorRGB BASIC_BLUE = Colors.BLUE;
	public static final ColorRGB BASIC_CYAN = Colors.CYAN;
	public static final ColorRGB BASIC_GRAY = Colors.GRAY;
	public static final ColorRGB BASIC_GREEN = Colors.LIME;  // this one is nonstandard
	public static final ColorRGB BASIC_MAGENTA = Colors.MAGENTA;
	public static final ColorRGB BASIC_ORANGE = Colors.ORANGE;
	public static final ColorRGB BASIC_PINK = Colors.PINK;
	public static final ColorRGB BASIC_RED = Colors.RED;
	public static final ColorRGB BASIC_WHITE = Colors.WHITE;
	public static final ColorRGB BASIC_YELLOW = Colors.YELLOW;

}
