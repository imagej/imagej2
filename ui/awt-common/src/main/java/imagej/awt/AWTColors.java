//
// AWTColors.java
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

package imagej.awt;

import imagej.util.ColorRGB;
import imagej.util.ColorRGBA;

import java.awt.Color;

/**
 * Translates ImageJ {@link ColorRGB}s into AWT {@link Color}s.
 * 
 * @author Curtis Rueden
 */
public final class AWTColors {

	private AWTColors() {
		// prevent instantiation of utility class
	}

	/**
	 * Gets the AWT {@link Color} corresponding to the given ImageJ
	 * {@link ColorRGB}.
	 */
	public static Color getColor(final ColorRGB color) {
		return new Color(color.getRed(), color.getGreen(), color.getBlue());
	}
	
	/**
	 * Get a java.awt.Color including the alpha component
	 * @param color - RGBA color
	 * @return the AWT color
	 */
	public static Color getColorRGBA(final ColorRGBA color) {
		return new Color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha());
	}
	
	/**
	 * Get a java.awt.Color given an RGB color and an explicit alpha component (0-255)
	 * @param color - RGB color
	 * @param alpha - alpha value (0-255)
	 * @return AWT color
	 */
	public static Color getColorRGBA(final ColorRGB color, int alpha) {
		assert (alpha >=0) && (alpha <=255): String.format("Alpha value of %d is out of range (0-255)", alpha);
		return new Color(color.getRed(), color.getGreen(), color.getBlue(), alpha);
	}
	
	/**
	 * Gets the ImageJ {@link ColorRGB} corresponding to the given AWT
	 * {@link Color}.
	 */
	public static ColorRGB getColorRGB(final Color color) {
		return new ColorRGB(color.getRed(), color.getGreen(), color.getBlue());
	}
	
	/**
	 * Get the color with alpha component
	 * @param color - java.awt.Color to be translated
	 * @return the imageJ RGB color
	 */
	public static ColorRGBA getColorRGBA(final Color color) {
		return new ColorRGBA(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha());
	}

}
