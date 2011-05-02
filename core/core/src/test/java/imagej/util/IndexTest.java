//
// IndexTest.java
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

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Test;

/**
 * Unit tests for {@link Index}.
 * 
 * @author Barry DeZonia
 */
public class IndexTest {

	@Test
	public void testCreateInt() {
		long[] vals;

		try {
			vals = Span.create(-1);
			fail();
		}
		catch (final NegativeArraySizeException e) {
			assertTrue(true);
		}

		vals = Span.create(0);
		assertArrayEquals(new long[] {}, vals);

		vals = Span.create(1);
		assertArrayEquals(new long[] { 0 }, vals);

		vals = Span.create(2);
		assertArrayEquals(new long[] { 0, 0 }, vals);

		vals = Span.create(3);
		assertArrayEquals(new long[] { 0, 0, 0 }, vals);
	}

	@Test
	public void testCreateIntArray() {
		long[] vals;

		vals = Span.create(new long[] {});
		assertArrayEquals(new long[] {}, vals);

		vals = Span.create(new long[] { 4 });
		assertArrayEquals(new long[] { 4 }, vals);

		vals = Span.create(new long[] { 1, 7 });
		assertArrayEquals(new long[] { 1, 7 }, vals);

		vals = Span.create(new long[] { 1, 2, 3, 4, 5, 6, 7, 8, 9 });
		assertArrayEquals(new long[] { 1, 2, 3, 4, 5, 6, 7, 8, 9 }, vals);
	}

	@Test
	public void testCreateIntIntIntArray() {
		long[] vals;

		// failure cases first

		// x < 0

		try {
			vals = Index.create(-1, 1, new long[] { 1, 1, 1 });
			fail();
		}
		catch (final IllegalArgumentException e) {
			assertTrue(true);
		}

		// y < 0

		try {
			vals = Index.create(1, -1, new long[] { 1, 1, 1 });
			fail();
		}
		catch (final IllegalArgumentException e) {
			assertTrue(true);
		}

		// otherwise good input

		vals = Index.create(0, 0, new long[] {});
		assertArrayEquals(new long[] { 0, 0 }, vals);

		vals = Index.create(0, 0, new long[] { 1 });
		assertArrayEquals(new long[] { 0, 0, 1 }, vals);

		vals = Index.create(0, 0, new long[] { 1, 2 });
		assertArrayEquals(new long[] { 0, 0, 1, 2 }, vals);

		vals = Index.create(0, 0, new long[] { 1, 2, 3 });
		assertArrayEquals(new long[] { 0, 0, 1, 2, 3 }, vals);

		vals = Index.create(5, 6, new long[] { 9, 8, 7, 3, 2, 1 });
		assertArrayEquals(new long[] { 5, 6, 9, 8, 7, 3, 2, 1 }, vals);
	}

	@Test
	public void testIsValid() {
		assertFalse(Index.isValid(new long[] { -1 }, new long[] { 0 },
			new long[] { 1 }));
		assertTrue(Index.isValid(new long[] { 0 }, new long[] { 0 },
			new long[] { 1 }));
		assertFalse(Index.isValid(new long[] { 1 }, new long[] { 0 },
			new long[] { 1 }));

		assertFalse(Index.isValid(new long[] { -1 }, new long[] { 0 },
			new long[] { 2 }));
		assertTrue(Index.isValid(new long[] { 0 }, new long[] { 0 },
			new long[] { 2 }));
		assertTrue(Index.isValid(new long[] { 1 }, new long[] { 0 },
			new long[] { 2 }));
		assertFalse(Index.isValid(new long[] { 2 }, new long[] { 0 },
			new long[] { 2 }));

		assertFalse(Index.isValid(new long[] { -1, 0 }, new long[] { 0, 0 },
			new long[] { 2, 2 }));
		assertFalse(Index.isValid(new long[] { 0, -1 }, new long[] { 0, 0 },
			new long[] { 2, 2 }));
		assertTrue(Index.isValid(new long[] { 0, 0 }, new long[] { 0, 0 },
			new long[] { 2, 2 }));
		assertTrue(Index.isValid(new long[] { 0, 1 }, new long[] { 0, 0 },
			new long[] { 2, 2 }));
		assertTrue(Index.isValid(new long[] { 1, 0 }, new long[] { 0, 0 },
			new long[] { 2, 2 }));
		assertTrue(Index.isValid(new long[] { 1, 1 }, new long[] { 0, 0 },
			new long[] { 2, 2 }));
		assertFalse(Index.isValid(new long[] { 2, 1 }, new long[] { 0, 0 },
			new long[] { 2, 2 }));
		assertFalse(Index.isValid(new long[] { 1, 2 }, new long[] { 0, 0 },
			new long[] { 2, 2 }));

		assertFalse(Index.isValid(new long[] { 1, 0 }, new long[] { 1, 1 },
			new long[] { 3, 3 }));
		assertFalse(Index.isValid(new long[] { 0, 1 }, new long[] { 1, 1 },
			new long[] { 3, 3 }));
		assertTrue(Index.isValid(new long[] { 1, 1 }, new long[] { 1, 1 },
			new long[] { 3, 3 }));
		assertTrue(Index.isValid(new long[] { 1, 2 }, new long[] { 1, 1 },
			new long[] { 3, 3 }));
		assertTrue(Index.isValid(new long[] { 1, 3 }, new long[] { 1, 1 },
			new long[] { 3, 3 }));
		assertTrue(Index.isValid(new long[] { 2, 1 }, new long[] { 1, 1 },
			new long[] { 3, 3 }));
		assertTrue(Index.isValid(new long[] { 2, 2 }, new long[] { 1, 1 },
			new long[] { 3, 3 }));
		assertTrue(Index.isValid(new long[] { 2, 3 }, new long[] { 1, 1 },
			new long[] { 3, 3 }));
		assertTrue(Index.isValid(new long[] { 3, 1 }, new long[] { 1, 1 },
			new long[] { 3, 3 }));
		assertTrue(Index.isValid(new long[] { 3, 2 }, new long[] { 1, 1 },
			new long[] { 3, 3 }));
		assertTrue(Index.isValid(new long[] { 3, 3 }, new long[] { 1, 1 },
			new long[] { 3, 3 }));
		assertFalse(Index.isValid(new long[] { 3, 4 }, new long[] { 1, 1 },
			new long[] { 3, 3 }));
		assertFalse(Index.isValid(new long[] { 4, 3 }, new long[] { 1, 1 },
			new long[] { 3, 3 }));
	}

	private void shouldFailIncrement(final long[] position, final long[] origin,
		final long[] span)
	{
		Index.increment(position, origin, span);

		assertFalse(Index.isValid(position, origin, span));
	}

	@Test
	public void testIncrement() {
		long[] position, origin, span;

		// one element 1D array

		origin = new long[] { 0 };
		span = new long[] { 1 };
		position = origin.clone();
		assertTrue(Index.isValid(position, origin, span));
		shouldFailIncrement(position, origin, span);

		// two element 1D array

		origin = new long[] { 0 };
		span = new long[] { 2 };
		position = origin.clone();

		assertTrue(Index.isValid(position, origin, span));
		Index.increment(position, origin, span);
		assertArrayEquals(new long[] { 1 }, position);

		assertTrue(Index.isValid(position, origin, span));
		shouldFailIncrement(position, origin, span);

		// three element 1D array

		origin = new long[] { 0 };
		span = new long[] { 3 };
		position = origin.clone();

		assertTrue(Index.isValid(position, origin, span));
		Index.increment(position, origin, span);
		assertArrayEquals(new long[] { 1 }, position);

		assertTrue(Index.isValid(position, origin, span));
		Index.increment(position, origin, span);
		assertArrayEquals(new long[] { 2 }, position);

		assertTrue(Index.isValid(position, origin, span));
		shouldFailIncrement(position, origin, span);

		// four element 2D array
		origin = new long[] { 0, 0 };
		span = new long[] { 2, 2 };
		position = origin.clone();

		assertTrue(Index.isValid(position, origin, span));
		Index.increment(position, origin, span);
		assertArrayEquals(new long[] { 1, 0 }, position);

		assertTrue(Index.isValid(position, origin, span));
		Index.increment(position, origin, span);
		assertArrayEquals(new long[] { 0, 1 }, position);

		assertTrue(Index.isValid(position, origin, span));
		Index.increment(position, origin, span);
		assertArrayEquals(new long[] { 1, 1 }, position);

		assertTrue(Index.isValid(position, origin, span));
		shouldFailIncrement(position, origin, span);

		// eight element 3D array
		origin = new long[] { 1, 1, 1 };
		span = new long[] { 2, 2, 2 };
		position = origin.clone();

		assertTrue(Index.isValid(position, origin, span));
		Index.increment(position, origin, span);
		assertArrayEquals(new long[] { 2, 1, 1 }, position);

		assertTrue(Index.isValid(position, origin, span));
		Index.increment(position, origin, span);
		assertArrayEquals(new long[] { 1, 2, 1 }, position);

		assertTrue(Index.isValid(position, origin, span));
		Index.increment(position, origin, span);
		assertArrayEquals(new long[] { 2, 2, 1 }, position);

		assertTrue(Index.isValid(position, origin, span));
		Index.increment(position, origin, span);
		assertArrayEquals(new long[] { 1, 1, 2 }, position);

		assertTrue(Index.isValid(position, origin, span));
		Index.increment(position, origin, span);
		assertArrayEquals(new long[] { 2, 1, 2 }, position);

		assertTrue(Index.isValid(position, origin, span));
		Index.increment(position, origin, span);
		assertArrayEquals(new long[] { 1, 2, 2 }, position);

		assertTrue(Index.isValid(position, origin, span));
		Index.increment(position, origin, span);
		assertArrayEquals(new long[] { 2, 2, 2 }, position);

		assertTrue(Index.isValid(position, origin, span));
		shouldFailIncrement(position, origin, span);

		// 2x3x1 array
		origin = new long[] { 0, 0, 0 };
		span = new long[] { 2, 3, 1 };
		position = origin.clone();

		assertTrue(Index.isValid(position, origin, span));
		Index.increment(position, origin, span);
		assertArrayEquals(new long[] { 1, 0, 0 }, position);

		assertTrue(Index.isValid(position, origin, span));
		Index.increment(position, origin, span);
		assertArrayEquals(new long[] { 0, 1, 0 }, position);

		assertTrue(Index.isValid(position, origin, span));
		Index.increment(position, origin, span);
		assertArrayEquals(new long[] { 1, 1, 0 }, position);

		assertTrue(Index.isValid(position, origin, span));
		Index.increment(position, origin, span);
		assertArrayEquals(new long[] { 0, 2, 0 }, position);

		assertTrue(Index.isValid(position, origin, span));
		Index.increment(position, origin, span);
		assertArrayEquals(new long[] { 1, 2, 0 }, position);

		assertTrue(Index.isValid(position, origin, span));
		shouldFailIncrement(position, origin, span);
	}

	private void getPlanePositionShouldFail(final long[] dimensions,
		final int index)
	{
		try {
			Index.getPlanePosition(dimensions, index);
			fail();
		}
		catch (final IllegalArgumentException e) {
			assertTrue(true);
		}
	}

	@Test
	public void testGetPlanePosition() {

		long[] dimensions;

		dimensions = new long[] {};
		getPlanePositionShouldFail(dimensions, -1);
		getPlanePositionShouldFail(dimensions, 0);
		getPlanePositionShouldFail(dimensions, 1);

		dimensions = new long[] { 50 };
		getPlanePositionShouldFail(dimensions, -1);
		getPlanePositionShouldFail(dimensions, 0);
		getPlanePositionShouldFail(dimensions, 1);

		// TODO - the middle case is unintuitive. Its up to the user to specify a
		// MxNx1 image for a single plane
		dimensions = new long[] { 50, 60 };
		getPlanePositionShouldFail(dimensions, -1);
		assertArrayEquals(new long[] {}, Index.getPlanePosition(dimensions, 0));
		getPlanePositionShouldFail(dimensions, 1);

		dimensions = new long[] { 50, 60, 3 };
		getPlanePositionShouldFail(dimensions, -1);
		assertArrayEquals(new long[] { 0 }, Index.getPlanePosition(dimensions, 0));
		assertArrayEquals(new long[] { 1 }, Index.getPlanePosition(dimensions, 1));
		assertArrayEquals(new long[] { 2 }, Index.getPlanePosition(dimensions, 2));
		getPlanePositionShouldFail(dimensions, 3);

		dimensions = new long[] { 50, 60, 2, 2 };
		getPlanePositionShouldFail(dimensions, -1);
		assertArrayEquals(new long[] { 0, 0 },
			Index.getPlanePosition(dimensions, 0));
		assertArrayEquals(new long[] { 1, 0 },
			Index.getPlanePosition(dimensions, 1));
		assertArrayEquals(new long[] { 0, 1 },
			Index.getPlanePosition(dimensions, 2));
		assertArrayEquals(new long[] { 1, 1 },
			Index.getPlanePosition(dimensions, 3));
		getPlanePositionShouldFail(dimensions, 4);

		dimensions = new long[] { 50, 60, 2, 2, 2 };
		getPlanePositionShouldFail(dimensions, -1);
		assertArrayEquals(new long[] { 0, 0, 0 },
			Index.getPlanePosition(dimensions, 0));
		assertArrayEquals(new long[] { 1, 0, 0 },
			Index.getPlanePosition(dimensions, 1));
		assertArrayEquals(new long[] { 0, 1, 0 }, 
			Index.getPlanePosition(dimensions, 2));
		assertArrayEquals(new long[] { 1, 1, 0 }, 
			Index.getPlanePosition(dimensions, 3));
		assertArrayEquals(new long[] { 0, 0, 1 }, 
			Index.getPlanePosition(dimensions, 4));
		assertArrayEquals(new long[] { 1, 0, 1 }, 
			Index.getPlanePosition(dimensions, 5));
		assertArrayEquals(new long[] { 0, 1, 1 }, 
			Index.getPlanePosition(dimensions, 6));
		assertArrayEquals(new long[] { 1, 1, 1 }, 
			Index.getPlanePosition(dimensions, 7));
		getPlanePositionShouldFail(dimensions, 8);
	}

	@Test
	public void testGetTotalLength() {
		assertEquals(0, Index.getTotalLength(new long[] { 0 }));
		assertEquals(1, Index.getTotalLength(new long[] {}));
		assertEquals(1, Index.getTotalLength(new long[] { 1 }));
		assertEquals(1, Index.getTotalLength(new long[] { 1, 1 }));
		assertEquals(7, Index.getTotalLength(new long[] { 7 }));
		assertEquals(0, Index.getTotalLength(new long[] { 1, 0 }));
		assertEquals(6, Index.getTotalLength(new long[] { 2, 3 }));
		assertEquals(8, Index.getTotalLength(new long[] { 8 }));
		assertEquals(10, Index.getTotalLength(new long[] { 2, 5 }));
		assertEquals(24, Index.getTotalLength(new long[] { 2, 3, 4 }));
		assertEquals(24, Index.getTotalLength(new long[] { 4, 3, 2 }));
		assertEquals(105, Index.getTotalLength(new long[] { 1, 3, 5, 7 }));
		assertEquals(720, Index.getTotalLength(new long[] { 1, 2, 3, 4, 5, 6 }));
		assertEquals(4294836225L,
			Index.getTotalLength(new long[] { 65535, 65535 }));
	}

}
