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

package imagej.util;

import java.io.Serializable;

/**
 * A color with red, green and blue color components.
 * 
 * @author Curtis Rueden
 * @author Lee Kamentsky
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
	 * Convert this ColorRGB to a string in the format specified by <a
	 * href="http://www.w3.org/TR/css3-color/">CSS Color Module Level 3 - W3C
	 * Recommendation 07 June 2011</a>.
	 * <p>
	 * We preferentially encode using one of the colors in the table and fall back
	 * to the hex encoding.
	 * </p>
	 * 
	 * @return HTML-encoded string
	 */
	public String toHTMLColor() {
		// return name of matching preset, if possible
		final String preset = Colors.getName(this);
		if (preset != null) return preset;

		// return hex-encoded string
		final int r = getRed();
		final int g = getGreen();
		final int b = getBlue();
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
	 * Convert a string in the format specified by <a
	 * href="http://www.w3.org/TR/css3-color/">CSS Color Module Level 3 - W3C
	 * Recommendation 07 June 2011</a> to a {@link ColorRGB} object.
	 * 
	 * @param color The color string to convert.
	 * @return The resultant color object.
	 */
	public static ColorRGB fromHTMLColor(final String color) {
		// 4.2.1
		// The format of an RGB value in hexadecimal notation is a "#"
		// immediately followed by either three or six hexadecimal characters.
		// The three-digit RGB notation (#rgb) is converted into six-digit form
		// (#rrggbb) by replicating digits, not by adding zeros. For example,
		// #fb0 expands to #ffbb00. This ensures that white (#ffffff) can be
		// specified with the short notation (#fff) and removes any dependencies
		// on the color depth of the display.
		if (color.startsWith("#")) {
			final String hexColor;
			if (color.length() == 4) {
				hexColor =
					new String(new char[] { color.charAt(0), color.charAt(1),
						color.charAt(1), color.charAt(2), color.charAt(2),
						color.charAt(3), color.charAt(3) });
			}
			else hexColor = color;
			final int red = Integer.parseInt(hexColor.substring(1, 3), 16);
			final int green = Integer.parseInt(hexColor.substring(3, 5), 16);
			final int blue = Integer.parseInt(hexColor.substring(5, 7), 16);
			return new ColorRGB(red, green, blue);
		}
		// assume color is a preset
		return Colors.getColor(color);
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

}
