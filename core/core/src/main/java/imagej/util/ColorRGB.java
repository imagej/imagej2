//
// ColorRGB.java
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

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * A color with red, green and blue color components.
 * 
 * @author Curtis Rueden
 */
public class ColorRGB implements Serializable {

	private static final long serialVersionUID = 1L;
	private final int red;
	private final int green;
	private final int blue;

	public ColorRGB(final int red, final int green, final int blue) {
		this.red = red;
		this.green = green;
		this.blue = blue;
	}

	/** Parses a color from a string of the form "r,g,b". */
	public ColorRGB(final String value) {
		final String[] tokens = value.split(",");
		red = parse(tokens, 0);
		green = parse(tokens, 1);
		blue = parse(tokens, 2);
	}

	public int getRed() {
		return red;
	}

	public int getGreen() {
		return green;
	}

	public int getBlue() {
		return blue;
	}

	public int getAlpha() {
		return 0xff;
	}

	/**
	 * Gets the color as a packed integer, 8 bits per color component. HSB is
	 * alpha, next is red, then green, and finally blue is LSB.
	 */
	public int getARGB() {
		final int a = getAlpha();
		final int r = getRed();
		final int g = getGreen();
		final int b = getBlue();
		return (a << 24) | (r << 16) | (g << 8) | b;
	}
	
	/**
	 * Convert this ColorRGB to a string in the format specified by 
	 * CSS Color Module Level 3 - W3C Recommendation 07 June 2011
	 * {@link http://www.w3.org/TR/css3-color/}
	 * 
	 * We preferentially encode using one of the colors in the table
	 * and fall back to the hex encoding
	 * 
	 * @return HTML-encoded string
	 */
	public String toHTMLColor() {
		final int r = getRed();
		final int g = getGreen();
		final int b = getBlue();
		for (Map.Entry<String, int[]>p:colorNames.entrySet()) {
			int [] value = p.getValue();
			if ((value[0] == r) && (value[1] == g) && (value[2] == b)) {
				return p.getKey();
			}
		}
		return String.format("#%02x%02x%02x", r, g, b);
	}

	// -- Object methods --

	@Override
	public String toString() {
		return red + "," + green + "," + blue;
	}

	@Override
	public boolean equals(final Object obj) {
		if (!(obj instanceof ColorRGB)) return super.equals(obj);
		final ColorRGB other = (ColorRGB) obj;
		return getRed() == other.getRed() && getGreen() == other.getGreen() &&
			getBlue() == other.getBlue() && getAlpha() == other.getAlpha();
	}

	@Override
	public int hashCode() {
		return getARGB();
	}
	
	// -- Static methods --
	
	/**
	 * Convert a string in the format specified by 
	 * CSS Color Module Level 3 - W3C Recommendation 07 June 2011
	 * {@link http://www.w3.org/TR/css3-color/}
	 * to a ColorRGB instance
	 * 
	 * @param color
	 * @return
	 */
	static public ColorRGB fromHTMLColor(String color) {
		// 4.2.1
		// The format of an RGB value in hexadecimal notation is a "#" 
		// immediately followed by either three or six hexadecimal characters. 
		// The three-digit RGB notation (#rgb) is converted into six-digit form 
		// (#rrggbb) by replicating digits, not by adding zeros. For example, 
		// #fb0 expands to #ffbb00. This ensures that white (#ffffff) can be 
		// specified with the short notation (#fff) and removes any dependencies 
		// on the color depth of the display.
		if (color.startsWith("#")) {
			if (color.length() == 4) {
				color = new String(
						new char[] {  color.charAt(0),
								color.charAt(1), color.charAt(1),
								color.charAt(2), color.charAt(2),
								color.charAt(3), color.charAt(3)});
			}
			int red = Integer.parseInt(color.substring(1, 3), 16);
			int green = Integer.parseInt(color.substring(3, 5), 16);
			int blue = Integer.parseInt(color.substring(5, 7), 16);
			return new ColorRGB(red, green, blue);
		} else {
			int [] colorValues = colorNames.get(color);
			return new ColorRGB(colorValues[0], colorValues[1], colorValues[2]);
		}
	}

	// -- Helper methods --

	private int parse(final String[] s, final int index) {
		if (s == null || index >= s.length) return 0;
		try {
			return Integer.parseInt(s[index]);
		}
		catch (final NumberFormatException exc) {
			return 0;
		}
	}
	// TODO - convert this to (extensible?) imagej.util.Colors enumeration
	private static final Map<String, int[]> colorNames = new HashMap<String, int[]> ();
	static {
		colorNames.put("aliceblue", new int [] { 240, 248, 255 });
		colorNames.put("antiquewhite", new int [] { 250, 235, 215 });
		colorNames.put("aqua", new int [] { 0, 255, 255 });
		colorNames.put("aquamarine", new int [] { 127, 255, 212 });
		colorNames.put("azure", new int [] { 240, 255, 255 });
		colorNames.put("beige", new int [] { 245, 245, 220 });
		colorNames.put("bisque", new int [] { 255, 228, 196 });
		colorNames.put("black", new int [] { 0, 0, 0 });
		colorNames.put("blanchedalmond", new int [] { 255, 235, 205 });
		colorNames.put("blue", new int [] { 0, 0, 255 });
		colorNames.put("blueviolet", new int [] { 138, 43, 226 });
		colorNames.put("brown", new int [] { 165, 42, 42 });
		colorNames.put("burlywood", new int [] { 222, 184, 135 });
		colorNames.put("cadetblue", new int [] { 95, 158, 160 });
		colorNames.put("chartreuse", new int [] { 127, 255, 0 });
		colorNames.put("chocolate", new int [] { 210, 105, 30 });
		colorNames.put("coral", new int [] { 255, 127, 80 });
		colorNames.put("cornflowerblue", new int [] { 100, 149, 237 });
		colorNames.put("cornsilk", new int [] { 255, 248, 220 });
		colorNames.put("crimson", new int [] { 220, 20, 60 });
		colorNames.put("cyan", new int [] { 0, 255, 255 });
		colorNames.put("darkblue", new int [] { 0, 0, 139 });
		colorNames.put("darkcyan", new int [] { 0, 139, 139 });
		colorNames.put("darkgoldenrod", new int [] { 184, 134, 11 });
		colorNames.put("darkgray", new int [] { 169, 169, 169 });
		colorNames.put("darkgreen", new int [] { 0, 100, 0 });
		colorNames.put("darkgrey", new int [] { 169, 169, 169 });
		colorNames.put("darkkhaki", new int [] { 189, 183, 107 });
		colorNames.put("darkmagenta", new int [] { 139, 0, 139 });
		colorNames.put("darkolivegreen", new int [] { 85, 107, 47 });
		colorNames.put("darkorange", new int [] { 255, 140, 0 });
		colorNames.put("darkorchid", new int [] { 153, 50, 204 });
		colorNames.put("darkred", new int [] { 139, 0, 0 });
		colorNames.put("darksalmon", new int [] { 233, 150, 122 });
		colorNames.put("darkseagreen", new int [] { 143, 188, 143 });
		colorNames.put("darkslateblue", new int [] { 72, 61, 139 });
		colorNames.put("darkslategray", new int [] { 47, 79, 79 });
		colorNames.put("darkslategrey", new int [] { 47, 79, 79 });
		colorNames.put("darkturquoise", new int [] { 0, 206, 209 });
		colorNames.put("darkviolet", new int [] { 148, 0, 211 });
		colorNames.put("deeppink", new int [] { 255, 20, 147 });
		colorNames.put("deepskyblue", new int [] { 0, 191, 255 });
		colorNames.put("dimgray", new int [] { 105, 105, 105 });
		colorNames.put("dimgrey", new int [] { 105, 105, 105 });
		colorNames.put("dodgerblue", new int [] { 30, 144, 255 });
		colorNames.put("firebrick", new int [] { 178, 34, 34 });
		colorNames.put("floralwhite", new int [] { 255, 250, 240 });
		colorNames.put("forestgreen", new int [] { 34, 139, 34 });
		colorNames.put("fuchsia", new int [] { 255, 0, 255 });
		colorNames.put("gainsboro", new int [] { 220, 220, 220 });
		colorNames.put("ghostwhite", new int [] { 248, 248, 255 });
		colorNames.put("gold", new int [] { 255, 215, 0 });
		colorNames.put("goldenrod", new int [] { 218, 165, 32 });
		colorNames.put("gray", new int [] { 128, 128, 128 });
		colorNames.put("green", new int [] { 0, 128, 0 });
		colorNames.put("greenyellow", new int [] { 173, 255, 47 });
		colorNames.put("grey", new int [] { 128, 128, 128 });
		colorNames.put("honeydew", new int [] { 240, 255, 240 });
		colorNames.put("hotpink", new int [] { 255, 105, 180 });
		colorNames.put("indianred", new int [] { 205, 92, 92 });
		colorNames.put("indigo", new int [] { 75, 0, 130 });
		colorNames.put("ivory", new int [] { 255, 255, 240 });
		colorNames.put("khaki", new int [] { 240, 230, 140 });
		colorNames.put("lavender", new int [] { 230, 230, 250 });
		colorNames.put("lavenderblush", new int [] { 255, 240, 245 });
		colorNames.put("lawngreen", new int [] { 124, 252, 0 });
		colorNames.put("lemonchiffon", new int [] { 255, 250, 205 });
		colorNames.put("lightblue", new int [] { 173, 216, 230 });
		colorNames.put("lightcoral", new int [] { 240, 128, 128 });
		colorNames.put("lightcyan", new int [] { 224, 255, 255 });
		colorNames.put("lightgoldenrodyellow", new int [] { 250, 250, 210 });
		colorNames.put("lightgray", new int [] { 211, 211, 211 });
		colorNames.put("lightgreen", new int [] { 144, 238, 144 });
		colorNames.put("lightgrey", new int [] { 211, 211, 211 });
		colorNames.put("lightpink", new int [] { 255, 182, 193 });
		colorNames.put("lightsalmon", new int [] { 255, 160, 122 });
		colorNames.put("lightseagreen", new int [] { 32, 178, 170 });
		colorNames.put("lightskyblue", new int [] { 135, 206, 250 });
		colorNames.put("lightslategray", new int [] { 119, 136, 153 });
		colorNames.put("lightslategrey", new int [] { 119, 136, 153 });
		colorNames.put("lightsteelblue", new int [] { 176, 196, 222 });
		colorNames.put("lightyellow", new int [] { 255, 255, 224 });
		colorNames.put("lime", new int [] { 0, 255, 0 });
		colorNames.put("limegreen", new int [] { 50, 205, 50 });
		colorNames.put("linen", new int [] { 250, 240, 230 });
		colorNames.put("magenta", new int [] { 255, 0, 255 });
		colorNames.put("maroon", new int [] { 128, 0, 0 });
		colorNames.put("mediumaquamarine", new int [] { 102, 205, 170 });
		colorNames.put("mediumblue", new int [] { 0, 0, 205 });
		colorNames.put("mediumorchid", new int [] { 186, 85, 211 });
		colorNames.put("mediumpurple", new int [] { 147, 112, 219 });
		colorNames.put("mediumseagreen", new int [] { 60, 179, 113 });
		colorNames.put("mediumslateblue", new int [] { 123, 104, 238 });
		colorNames.put("mediumspringgreen", new int [] { 0, 250, 154 });
		colorNames.put("mediumturquoise", new int [] { 72, 209, 204 });
		colorNames.put("mediumvioletred", new int [] { 199, 21, 133 });
		colorNames.put("midnightblue", new int [] { 25, 25, 112 });
		colorNames.put("mintcream", new int [] { 245, 255, 250 });
		colorNames.put("mistyrose", new int [] { 255, 228, 225 });
		colorNames.put("moccasin", new int [] { 255, 228, 181 });
		colorNames.put("navajowhite", new int [] { 255, 222, 173 });
		colorNames.put("navy", new int [] { 0, 0, 128 });
		colorNames.put("oldlace", new int [] { 253, 245, 230 });
		colorNames.put("olive", new int [] { 128, 128, 0 });
		colorNames.put("olivedrab", new int [] { 107, 142, 35 });
		colorNames.put("orange", new int [] { 255, 165, 0 });
		colorNames.put("orangered", new int [] { 255, 69, 0 });
		colorNames.put("orchid", new int [] { 218, 112, 214 });
		colorNames.put("palegoldenrod", new int [] { 238, 232, 170 });
		colorNames.put("palegreen", new int [] { 152, 251, 152 });
		colorNames.put("paleturquoise", new int [] { 175, 238, 238 });
		colorNames.put("palevioletred", new int [] { 219, 112, 147 });
		colorNames.put("papayawhip", new int [] { 255, 239, 213 });
		colorNames.put("peachpuff", new int [] { 255, 218, 185 });
		colorNames.put("peru", new int [] { 205, 133, 63 });
		colorNames.put("pink", new int [] { 255, 192, 203 });
		colorNames.put("plum", new int [] { 221, 160, 221 });
		colorNames.put("powderblue", new int [] { 176, 224, 230 });
		colorNames.put("purple", new int [] { 128, 0, 128 });
		colorNames.put("red", new int [] { 255, 0, 0 });
		colorNames.put("rosybrown", new int [] { 188, 143, 143 });
		colorNames.put("royalblue", new int [] { 65, 105, 225 });
		colorNames.put("saddlebrown", new int [] { 139, 69, 19 });
		colorNames.put("salmon", new int [] { 250, 128, 114 });
		colorNames.put("sandybrown", new int [] { 244, 164, 96 });
		colorNames.put("seagreen", new int [] { 46, 139, 87 });
		colorNames.put("seashell", new int [] { 255, 245, 238 });
		colorNames.put("sienna", new int [] { 160, 82, 45 });
		colorNames.put("silver", new int [] { 192, 192, 192 });
		colorNames.put("skyblue", new int [] { 135, 206, 235 });
		colorNames.put("slateblue", new int [] { 106, 90, 205 });
		colorNames.put("slategray", new int [] { 112, 128, 144 });
		colorNames.put("slategrey", new int [] { 112, 128, 144 });
		colorNames.put("snow", new int [] { 255, 250, 250 });
		colorNames.put("springgreen", new int [] { 0, 255, 127 });
		colorNames.put("steelblue", new int [] { 70, 130, 180 });
		colorNames.put("tan", new int [] { 210, 180, 140 });
		colorNames.put("teal", new int [] { 0, 128, 128 });
		colorNames.put("thistle", new int [] { 216, 191, 216 });
		colorNames.put("tomato", new int [] { 255, 99, 71 });
		colorNames.put("turquoise", new int [] { 64, 224, 208 });
		colorNames.put("violet", new int [] { 238, 130, 238 });
		colorNames.put("wheat", new int [] { 245, 222, 179 });
		colorNames.put("white", new int [] { 255, 255, 255 });
		colorNames.put("whitesmoke", new int [] { 245, 245, 245 });
		colorNames.put("yellow", new int [] { 255, 255, 0 });
		colorNames.put("yellowgreen", new int [] { 154, 205, 50 });
	}
}
