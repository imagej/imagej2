
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
