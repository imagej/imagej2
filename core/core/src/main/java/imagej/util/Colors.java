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

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Predefined {@link ColorRGB} objects and related utility methods. These colors
 * match <a href="http://www.w3schools.com/cssref/css_colornames.asp">those
 * defined in CSS</a>.
 * 
 * @author Lee Kamentsky
 * @author Curtis Rueden
 */
public final class Colors {

	public static final ColorRGB ALICEBLUE = new ColorRGB(240, 248, 255);
	public static final ColorRGB ANTIQUEWHITE = new ColorRGB(250, 235, 215);
	public static final ColorRGB AQUA = new ColorRGB(0, 255, 255);
	public static final ColorRGB AQUAMARINE = new ColorRGB(127, 255, 212);
	public static final ColorRGB AZURE = new ColorRGB(240, 255, 255);
	public static final ColorRGB BEIGE = new ColorRGB(245, 245, 220);
	public static final ColorRGB BISQUE = new ColorRGB(255, 228, 196);
	public static final ColorRGB BLACK = new ColorRGB(0, 0, 0);
	public static final ColorRGB BLANCHEDALMOND = new ColorRGB(255, 235, 205);
	public static final ColorRGB BLUE = new ColorRGB(0, 0, 255);
	public static final ColorRGB BLUEVIOLET = new ColorRGB(138, 43, 226);
	public static final ColorRGB BROWN = new ColorRGB(165, 42, 42);
	public static final ColorRGB BURLYWOOD = new ColorRGB(222, 184, 135);
	public static final ColorRGB CADETBLUE = new ColorRGB(95, 158, 160);
	public static final ColorRGB CHARTREUSE = new ColorRGB(127, 255, 0);
	public static final ColorRGB CHOCOLATE = new ColorRGB(210, 105, 30);
	public static final ColorRGB CORAL = new ColorRGB(255, 127, 80);
	public static final ColorRGB CORNFLOWERBLUE = new ColorRGB(100, 149, 237);
	public static final ColorRGB CORNSILK = new ColorRGB(255, 248, 220);
	public static final ColorRGB CRIMSON = new ColorRGB(220, 20, 60);
	public static final ColorRGB CYAN = new ColorRGB(0, 255, 255);
	public static final ColorRGB DARKBLUE = new ColorRGB(0, 0, 139);
	public static final ColorRGB DARKCYAN = new ColorRGB(0, 139, 139);
	public static final ColorRGB DARKGOLDENROD = new ColorRGB(184, 134, 11);
	public static final ColorRGB DARKGRAY = new ColorRGB(169, 169, 169);
	public static final ColorRGB DARKGREEN = new ColorRGB(0, 100, 0);
	public static final ColorRGB DARKGREY = DARKGRAY;
	public static final ColorRGB DARKKHAKI = new ColorRGB(189, 183, 107);
	public static final ColorRGB DARKMAGENTA = new ColorRGB(139, 0, 139);
	public static final ColorRGB DARKOLIVEGREEN = new ColorRGB(85, 107, 47);
	public static final ColorRGB DARKORANGE = new ColorRGB(255, 140, 0);
	public static final ColorRGB DARKORCHID = new ColorRGB(153, 50, 204);
	public static final ColorRGB DARKRED = new ColorRGB(139, 0, 0);
	public static final ColorRGB DARKSALMON = new ColorRGB(233, 150, 122);
	public static final ColorRGB DARKSEAGREEN = new ColorRGB(143, 188, 143);
	public static final ColorRGB DARKSLATEBLUE = new ColorRGB(72, 61, 139);
	public static final ColorRGB DARKSLATEGRAY = new ColorRGB(47, 79, 79);
	public static final ColorRGB DARKSLATEGREY = DARKSLATEGRAY;
	public static final ColorRGB DARKTURQUOISE = new ColorRGB(0, 206, 209);
	public static final ColorRGB DARKVIOLET = new ColorRGB(148, 0, 211);
	public static final ColorRGB DEEPPINK = new ColorRGB(255, 20, 147);
	public static final ColorRGB DEEPSKYBLUE = new ColorRGB(0, 191, 255);
	public static final ColorRGB DIMGRAY = new ColorRGB(105, 105, 105);
	public static final ColorRGB DIMGREY = DIMGRAY;
	public static final ColorRGB DODGERBLUE = new ColorRGB(30, 144, 255);
	public static final ColorRGB FIREBRICK = new ColorRGB(178, 34, 34);
	public static final ColorRGB FLORALWHITE = new ColorRGB(255, 250, 240);
	public static final ColorRGB FORESTGREEN = new ColorRGB(34, 139, 34);
	public static final ColorRGB FUCHSIA = new ColorRGB(255, 0, 255);
	public static final ColorRGB GAINSBORO = new ColorRGB(220, 220, 220);
	public static final ColorRGB GHOSTWHITE = new ColorRGB(248, 248, 255);
	public static final ColorRGB GOLD = new ColorRGB(255, 215, 0);
	public static final ColorRGB GOLDENROD = new ColorRGB(218, 165, 32);
	public static final ColorRGB GRAY = new ColorRGB(128, 128, 128);
	public static final ColorRGB GREEN = new ColorRGB(0, 128, 0);
	public static final ColorRGB GREENYELLOW = new ColorRGB(173, 255, 47);
	public static final ColorRGB GREY = GRAY;
	public static final ColorRGB HONEYDEW = new ColorRGB(240, 255, 240);
	public static final ColorRGB HOTPINK = new ColorRGB(255, 105, 180);
	public static final ColorRGB INDIANRED = new ColorRGB(205, 92, 92);
	public static final ColorRGB INDIGO = new ColorRGB(75, 0, 130);
	public static final ColorRGB IVORY = new ColorRGB(255, 255, 240);
	public static final ColorRGB KHAKI = new ColorRGB(240, 230, 140);
	public static final ColorRGB LAVENDER = new ColorRGB(230, 230, 250);
	public static final ColorRGB LAVENDERBLUSH = new ColorRGB(255, 240, 245);
	public static final ColorRGB LAWNGREEN = new ColorRGB(124, 252, 0);
	public static final ColorRGB LEMONCHIFFON = new ColorRGB(255, 250, 205);
	public static final ColorRGB LIGHTBLUE = new ColorRGB(173, 216, 230);
	public static final ColorRGB LIGHTCORAL = new ColorRGB(240, 128, 128);
	public static final ColorRGB LIGHTCYAN = new ColorRGB(224, 255, 255);
	public static final ColorRGB LIGHTGOLDENRODYELLOW = new ColorRGB(250, 250,
		210);
	public static final ColorRGB LIGHTGRAY = new ColorRGB(211, 211, 211);
	public static final ColorRGB LIGHTGREEN = new ColorRGB(144, 238, 144);
	public static final ColorRGB LIGHTGREY = LIGHTGRAY;
	public static final ColorRGB LIGHTPINK = new ColorRGB(255, 182, 193);
	public static final ColorRGB LIGHTSALMON = new ColorRGB(255, 160, 122);
	public static final ColorRGB LIGHTSEAGREEN = new ColorRGB(32, 178, 170);
	public static final ColorRGB LIGHTSKYBLUE = new ColorRGB(135, 206, 250);
	public static final ColorRGB LIGHTSLATEGRAY = new ColorRGB(119, 136, 153);
	public static final ColorRGB LIGHTSLATEGREY = LIGHTSLATEGRAY;
	public static final ColorRGB LIGHTSTEELBLUE = new ColorRGB(176, 196, 222);
	public static final ColorRGB LIGHTYELLOW = new ColorRGB(255, 255, 224);
	public static final ColorRGB LIME = new ColorRGB(0, 255, 0);
	public static final ColorRGB LIMEGREEN = new ColorRGB(50, 205, 50);
	public static final ColorRGB LINEN = new ColorRGB(250, 240, 230);
	public static final ColorRGB MAGENTA = new ColorRGB(255, 0, 255);
	public static final ColorRGB MAROON = new ColorRGB(128, 0, 0);
	public static final ColorRGB MEDIUMAQUAMARINE = new ColorRGB(102, 205, 170);
	public static final ColorRGB MEDIUMBLUE = new ColorRGB(0, 0, 205);
	public static final ColorRGB MEDIUMORCHID = new ColorRGB(186, 85, 211);
	public static final ColorRGB MEDIUMPURPLE = new ColorRGB(147, 112, 219);
	public static final ColorRGB MEDIUMSEAGREEN = new ColorRGB(60, 179, 113);
	public static final ColorRGB MEDIUMSLATEBLUE = new ColorRGB(123, 104, 238);
	public static final ColorRGB MEDIUMSPRINGGREEN = new ColorRGB(0, 250, 154);
	public static final ColorRGB MEDIUMTURQUOISE = new ColorRGB(72, 209, 204);
	public static final ColorRGB MEDIUMVIOLETRED = new ColorRGB(199, 21, 133);
	public static final ColorRGB MIDNIGHTBLUE = new ColorRGB(25, 25, 112);
	public static final ColorRGB MINTCREAM = new ColorRGB(245, 255, 250);
	public static final ColorRGB MISTYROSE = new ColorRGB(255, 228, 225);
	public static final ColorRGB MOCCASIN = new ColorRGB(255, 228, 181);
	public static final ColorRGB NAVAJOWHITE = new ColorRGB(255, 222, 173);
	public static final ColorRGB NAVY = new ColorRGB(0, 0, 128);
	public static final ColorRGB OLDLACE = new ColorRGB(253, 245, 230);
	public static final ColorRGB OLIVE = new ColorRGB(128, 128, 0);
	public static final ColorRGB OLIVEDRAB = new ColorRGB(107, 142, 35);
	public static final ColorRGB ORANGE = new ColorRGB(255, 165, 0);
	public static final ColorRGB ORANGERED = new ColorRGB(255, 69, 0);
	public static final ColorRGB ORCHID = new ColorRGB(218, 112, 214);
	public static final ColorRGB PALEGOLDENROD = new ColorRGB(238, 232, 170);
	public static final ColorRGB PALEGREEN = new ColorRGB(152, 251, 152);
	public static final ColorRGB PALETURQUOISE = new ColorRGB(175, 238, 238);
	public static final ColorRGB PALEVIOLETRED = new ColorRGB(219, 112, 147);
	public static final ColorRGB PAPAYAWHIP = new ColorRGB(255, 239, 213);
	public static final ColorRGB PEACHPUFF = new ColorRGB(255, 218, 185);
	public static final ColorRGB PERU = new ColorRGB(205, 133, 63);
	public static final ColorRGB PINK = new ColorRGB(255, 192, 203);
	public static final ColorRGB PLUM = new ColorRGB(221, 160, 221);
	public static final ColorRGB POWDERBLUE = new ColorRGB(176, 224, 230);
	public static final ColorRGB PURPLE = new ColorRGB(128, 0, 128);
	public static final ColorRGB RED = new ColorRGB(255, 0, 0);
	public static final ColorRGB ROSYBROWN = new ColorRGB(188, 143, 143);
	public static final ColorRGB ROYALBLUE = new ColorRGB(65, 105, 225);
	public static final ColorRGB SADDLEBROWN = new ColorRGB(139, 69, 19);
	public static final ColorRGB SALMON = new ColorRGB(250, 128, 114);
	public static final ColorRGB SANDYBROWN = new ColorRGB(244, 164, 96);
	public static final ColorRGB SEAGREEN = new ColorRGB(46, 139, 87);
	public static final ColorRGB SEASHELL = new ColorRGB(255, 245, 238);
	public static final ColorRGB SIENNA = new ColorRGB(160, 82, 45);
	public static final ColorRGB SILVER = new ColorRGB(192, 192, 192);
	public static final ColorRGB SKYBLUE = new ColorRGB(135, 206, 235);
	public static final ColorRGB SLATEBLUE = new ColorRGB(106, 90, 205);
	public static final ColorRGB SLATEGRAY = new ColorRGB(112, 128, 144);
	public static final ColorRGB SLATEGREY = SLATEGRAY;
	public static final ColorRGB SNOW = new ColorRGB(255, 250, 250);
	public static final ColorRGB SPRINGGREEN = new ColorRGB(0, 255, 127);
	public static final ColorRGB STEELBLUE = new ColorRGB(70, 130, 180);
	public static final ColorRGB TAN = new ColorRGB(210, 180, 140);
	public static final ColorRGB TEAL = new ColorRGB(0, 128, 128);
	public static final ColorRGB THISTLE = new ColorRGB(216, 191, 216);
	public static final ColorRGB TOMATO = new ColorRGB(255, 99, 71);
	public static final ColorRGB TURQUOISE = new ColorRGB(64, 224, 208);
	public static final ColorRGB VIOLET = new ColorRGB(238, 130, 238);
	public static final ColorRGB WHEAT = new ColorRGB(245, 222, 179);
	public static final ColorRGB WHITE = new ColorRGB(255, 255, 255);
	public static final ColorRGB WHITESMOKE = new ColorRGB(245, 245, 245);
	public static final ColorRGB YELLOW = new ColorRGB(255, 255, 0);
	public static final ColorRGB YELLOWGREEN = new ColorRGB(154, 205, 50);

	private static final Map<String, ColorRGB> COLORS =
		new HashMap<String, ColorRGB>();

	static {
		for (final Field f : Colors.class.getDeclaredFields()) {
			final Object value;
			try {
				value = f.get(null);
			}
			catch (final IllegalArgumentException e) {
				continue;
			}
			catch (final IllegalAccessException e) {
				continue;
			}
			if (!(value instanceof ColorRGB)) continue; // not a color
			final ColorRGB color = (ColorRGB) value;
			COLORS.put(f.getName().toLowerCase(), color);
		}
	}

	private Colors() {
		// prevent instantiation of utility class
	}

	/**
	 * Gets the preset color with the given name. For example,
	 * <code>Colors.get("red")</code> will return {@link Colors#RED}.
	 */
	public static ColorRGB getColor(final String name) {
		return COLORS.get(name);
	}

	/** Gets the name of the preset matching the given color. */
	public static String getName(final ColorRGB color) {
		if (color == null) return null;
		for (final String name : COLORS.keySet()) {
			final ColorRGB value = COLORS.get(name);
			if (color.equals(value)) return name;
		}
		return null;
	}

	/** Gets the table of all preset colors. */
	public static Map<String, ColorRGB> map() {
		return Collections.unmodifiableMap(COLORS);
	}

	/** Gets the list of all preset colors. */
	public static Collection<ColorRGB> values() {
		return COLORS.values();
	}

	/**
	 * Returns the preset color closest to a given color. The definition of
	 * closest is the nearest color in RGB space.
	 */
	public static ColorRGB getClosestPresetColor(final ColorRGB color) {

		final int r = color.getRed();
		final int g = color.getGreen();
		final int b = color.getBlue();

		ColorRGB bestSoFar = null;
		double distance = Double.POSITIVE_INFINITY;
		boolean firstPass = true;
		for (final ColorRGB presetColor : COLORS.values()) {
			if (firstPass) {
				bestSoFar = presetColor;
				firstPass = false;
			}
			else { // not first pass
				final double dr = presetColor.getRed() - r;
				final double dg = presetColor.getGreen() - g;
				final double db = presetColor.getBlue() - b;
				final double thisDist = dr * dr + dg * dg + db * db;
				if (thisDist < distance) {
					bestSoFar = presetColor;
					distance = thisDist;
				}
			}
		}
		return bestSoFar;
	}
}
