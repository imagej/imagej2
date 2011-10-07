//
// CalibrationTools.java
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


package ij.measure;

import static org.junit.Assert.assertEquals;

import java.lang.reflect.Field;

/**
 * Utility methods for testing {@link Calibration} objects.
 * 
 * @author Barry DeZonia
 * @author Curtis Rueden
 */
public final class CalibrationTools {

	private CalibrationTools() {
		// prevent instantiation of utility class
	}

	/**
	 * Compares two Calibration objects for deep equality. Tests more internals of
	 * a Calibration than equals() does.
	 */
	public static void assertCalibrationsEqual(final Calibration c1,
		final Calibration c2)
	{
		if (c1 == c2) return;
		assertEquals(c1.pixelWidth, c2.pixelWidth, 0);
		assertEquals(c1.pixelHeight, c2.pixelHeight, 0);
		assertEquals(c1.pixelDepth, c2.pixelDepth, 0);
		assertEquals(c1.frameInterval, c2.frameInterval, 0);
		assertEquals(c1.fps, c2.fps, 0);
		assertEquals(c1.loop, c2.loop);
		assertEquals(c1.xOrigin, c2.xOrigin, 0);
		assertEquals(c1.yOrigin, c2.yOrigin, 0);
		assertEquals(c1.zOrigin, c2.zOrigin, 0);
		assertEquals(c1.info, c2.info);
		assertFieldsEqual(c1, c2, "coefficients");
		assertFieldsEqual(c1, c2, "unit");
		assertFieldsEqual(c1, c2, "yunit");
		assertFieldsEqual(c1, c2, "zunit");
		assertFieldsEqual(c1, c2, "units");
		assertFieldsEqual(c1, c2, "valueUnit");
		assertFieldsEqual(c1, c2, "timeUnit");
		assertFieldsEqual(c1, c2, "function");
		assertFieldsEqual(c1, c2, "cTable");
		assertFieldsEqual(c1, c2, "invertedLut");
		assertFieldsEqual(c1, c2, "bitDepth");
		assertFieldsEqual(c1, c2, "zeroClip");
		assertFieldsEqual(c1, c2, "invertY");
	}

	private static void assertFieldsEqual(final Calibration c1,
		final Calibration c2, final String field)
	{
		assertEquals(get(c1, field), get(c2, field));
	}

	private static Object get(final Calibration c, final String field) {
		try {
			final Field f = c.getClass().getDeclaredField(field);
			f.setAccessible(true);
			return f.get(c);
		}
		catch (final Exception e) {
			throw new IllegalArgumentException("Invalid field: " + field, e);
		}
	}

}
