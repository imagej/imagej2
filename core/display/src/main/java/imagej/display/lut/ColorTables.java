//
// ColorTables.java
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

package imagej.display.lut;

import java.awt.Color;

import net.imglib2.display.ColorTable8;

/**
 * Built-in lookup tables.
 * 
 * @author Wayne Rasband
 * @author Curtis Rueden
 * @author Grant Harris
 */
public final class ColorTables {

	public static final ColorTable8 FIRE = fire();
	public static final ColorTable8 ICE = ice();
	public static final ColorTable8 SPECTRUM = spectrum();
	public static final ColorTable8 RGB332 = rgb332();
	public static final ColorTable8 RED = primary(4);     // 100
	public static final ColorTable8 GREEN = primary(2);   // 010
	public static final ColorTable8 BLUE = primary(1);    // 001
	public static final ColorTable8 CYAN = primary(3);    // 011
	public static final ColorTable8 MAGENTA = primary(5); // 101
	public static final ColorTable8 YELLOW = primary (6); // 110
	public static final ColorTable8 GRAYS = primary(7);   // 111
	public static final ColorTable8 REDGREEN = redGreen();

	private ColorTables() {
		// prevent instantiation of utility class
	}

	// -- Helper methods --

	private static ColorTable8 fire() {
		final int[][] v = {
			{ 0, 0, 1, 25, 49, 73, 98, 122, 146, 162, 173, 184, 195, 207, 217,
				229, 240, 252, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255,
				255, 255, 255, 255 },
			{ 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 14, 35, 57, 79, 101, 117,
				133, 147, 161, 175, 190, 205, 219, 234, 248, 255, 255, 255, 255 },
			{ 0, 61, 96, 130, 165, 192, 220, 227, 210, 181, 151, 122, 93, 64, 35,
				5, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 35, 98, 160, 223, 255 }
		};
		return custom(v);
	}
	
	private static ColorTable8 ice() {
		final int[][] v = {
			{ 0, 0, 0, 0, 0, 0, 19, 29, 50, 48, 79, 112, 134, 158, 186, 201, 217,
				229, 242, 250, 250, 250, 250, 251, 250, 250, 250, 250, 251, 251, 243,
				230 },
			{ 156, 165, 176, 184, 190, 196, 193, 184, 171, 162, 146, 125, 107, 93,
				81, 87, 92, 97, 95, 93, 93, 90, 85, 69, 64, 54, 47, 35, 19, 0, 4, 0 },
			{ 140, 147, 158, 166, 170, 176, 209, 220, 234, 225, 236, 246, 250, 251,
				250, 250, 245, 230, 230, 222, 202, 180, 163, 142, 123, 114, 106, 94,
				84, 64, 26, 27 }
		};
		return custom(v);
	}

	private static ColorTable8 spectrum() {
		final byte[] r = new byte[256], g = new byte[256], b = new byte[256];
		for (int i = 0; i < 256; i++) {
			final Color c = Color.getHSBColor(i / 255f, 1f, 1f);
			r[i] = (byte) c.getRed();
			g[i] = (byte) c.getGreen();
			b[i] = (byte) c.getBlue();
		}
		return new ColorTable8(r, g, b);
	}

	private static ColorTable8 rgb332() {
		final byte[] r = new byte[256], g = new byte[256], b = new byte[256];
		for (int i = 0; i < 256; i++) {
			r[i] = (byte) (i & 0xe0);
			g[i] = (byte) ((i << 3) & 0xe0);
			b[i] = (byte) ((i << 6) & 0xc0);
		}
		return new ColorTable8(r, g, b);
	}

	private static ColorTable8 redGreen() {
		byte[][] values = new byte[3][256];
		for (int i = 0; i < 128; i++) {
			values[0][i] = (byte) (i * 2);
			values[1][i] = (byte) 0;
			values[2][i] = (byte) 0;
		}
		for (int i = 128; i < 256; i++) {
			values[0][i] = (byte) 0;
			values[1][i] = (byte) (i * 2);
			values[2][i] = (byte) 0;
		}
		return new ColorTable8(values);
	}

	private static ColorTable8 primary(final int color) {
		byte[] r = new byte[256], g = new byte[256], b = new byte[256];
		for (int i = 0; i < 256; i++) {
			if ((color & 4) != 0) r[i] = (byte) i;
			if ((color & 2) != 0) g[i] = (byte) i;
			if ((color & 1) != 0) b[i] = (byte) i;
		}
		return new ColorTable8(r, g, b);
	}

	private static ColorTable8 custom(final int[][] v) {
		final byte[][] values = new byte[v.length][];
		for (int j = 0; j < v.length; j++) {
			values[j] = new byte[v[j].length];
			for (int i = 0; i < v[j].length; i++) {
				values[j][i] = (byte) v[j][i];
			}
		}
		final ColorTable8 lut = new ColorTable8(values);
		return interpolate(lut, 256);
	}

	private static ColorTable8 interpolate(final ColorTable8 inTable,
		final int outLength)
	{
		final byte[][] inValues = inTable.getValues();
		final int componentCount = inValues.length;
		final int inLength = inValues[0].length;

		final byte[][] outValues = new byte[componentCount][outLength];
		for (int i = 0; i < outLength; i++) {
			final double q = (double) inLength * i / outLength;
			final int i1 = (int) q;
			final double fraction = q - i1;
			int i2 = i1 + 1;
			if (i2 == inLength) i2 = inLength - 1;
			for (int j = 0; j < componentCount; j++) {
				outValues[j][i] = (byte)
					((1.0 - fraction) * (inValues[j][i1] & 0xff) +
					fraction * (inValues[j][i2] & 0xff));
			}
		}
		return new ColorTable8(outValues);
	}

}
