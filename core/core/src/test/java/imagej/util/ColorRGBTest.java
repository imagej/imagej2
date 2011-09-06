package imagej.util;

import static org.junit.Assert.*;

import org.junit.Test;

public class ColorRGBTest {

	@Test
	public void testHashCode() {
		assertNotSame(new ColorRGB(1,2,3).hashCode(), new ColorRGB(3,2,1));
	}

	@Test
	public void testColorRGBIntIntInt() {
		assertEquals(1, new ColorRGB(1,2,3).getRed());
		assertEquals(2, new ColorRGB(1,2,3).getGreen());
		assertEquals(3, new ColorRGB(1,2,3).getBlue());
	}

	@Test
	public void testColorRGBString() {
		assertEquals(1, new ColorRGB("1,2,3").getRed());
		assertEquals(2, new ColorRGB("1,2,3").getGreen());
		assertEquals(3, new ColorRGB("1,2,3").getBlue());
	}

	@Test
	public void testGetRed() {
		// Tested by constructor
	}

	@Test
	public void testGetGreen() {
		// Tested by constructor
	}

	@Test
	public void testGetBlue() {
		// Tested by constructor
	}

	@Test
	public void testGetAlpha() {
		assertEquals(255, new ColorRGB(1,2,3).getAlpha());
	}

	@Test
	public void testGetARGB() {
		assertEquals(255*256*256*256 + 256*256 + 2 * 256 + 3, new ColorRGB(1,2,3).getARGB());
	}

	@Test
	public void testToHTMLColor() {
		assertEquals("#010203", new ColorRGB(1,2,3).toHTMLColor());
		assertEquals("red", new ColorRGB(255,0,0).toHTMLColor());
	}

	@Test
	public void testToString() {
		assertEquals("1,2,3", new ColorRGB("1,2,3").toString());
	}

	@Test
	public void testEqualsObject() {
		assertTrue(new ColorRGB(1,2,3).equals(new ColorRGB(1,2,3)));
		assertFalse(new ColorRGB(1,2,3).equals(new ColorRGB(3,2,1)));
	}

	@Test
	public void testFromHTMLColor() {
		assertEquals(new ColorRGB(1,2,3), ColorRGB.fromHTMLColor("#010203"));
		assertEquals(new ColorRGB(255,0,0), ColorRGB.fromHTMLColor("red"));
	}

}
