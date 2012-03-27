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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;

import org.junit.Test;

/**
 * Tests {@link ColorRGB}.
 * 
 * @author Lee Kamentsky
 */
public class ColorRGBTest {

	@Test
	public void testHashCode() {
		assertNotSame(new ColorRGB(1, 2, 3).hashCode(), new ColorRGB(3, 2, 1));
	}

	@Test
	public void testColorRGBIntIntInt() {
		assertEquals(1, new ColorRGB(1, 2, 3).getRed());
		assertEquals(2, new ColorRGB(1, 2, 3).getGreen());
		assertEquals(3, new ColorRGB(1, 2, 3).getBlue());
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
		assertEquals(255, new ColorRGB(1, 2, 3).getAlpha());
	}

	@Test
	public void testGetARGB() {
		assertEquals(255 * 256 * 256 * 256 + 256 * 256 + 2 * 256 + 3,
			new ColorRGB(1, 2, 3).getARGB());
	}

	@Test
	public void testToHTMLColor() {
		assertEquals("#010203", new ColorRGB(1, 2, 3).toHTMLColor());
		assertEquals("red", new ColorRGB(255, 0, 0).toHTMLColor());
		assertEquals("red", Colors.RED.toHTMLColor());
	}

	@Test
	public void testToString() {
		assertEquals("1,2,3", new ColorRGB("1,2,3").toString());
	}

	@Test
	public void testEqualsObject() {
		assertEquals(new ColorRGB(1, 2, 3), new ColorRGB(1, 2, 3));
		assertFalse(new ColorRGB(1, 2, 3).equals(new ColorRGB(3, 2, 1)));
	}

	@Test
	public void testFromHTMLColor() {
		assertEquals(new ColorRGB(1, 2, 3), ColorRGB.fromHTMLColor("#010203"));
		assertEquals(new ColorRGB(255, 0, 0), ColorRGB.fromHTMLColor("red"));
		assertSame(Colors.RED, ColorRGB.fromHTMLColor("red"));
		assertNotSame(Colors.RED, ColorRGB.fromHTMLColor("green"));
	}

}
