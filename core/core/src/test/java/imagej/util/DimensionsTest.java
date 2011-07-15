//
// DimensionsTest.java
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
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Test;

/**
 * Unit tests for {@link Dimensions}.
 * 
 * @author Barry DeZonia
 */
public class DimensionsTest {

	// -- Test methods --

	@Test
	public void testgetDims3AndGreater() {
		getDims3AndGreaterShouldFail(new long[]{});
		getDims3AndGreaterShouldFail(new long[]{1});
		assertArrayEquals(new long[]{}, Dimensions.getDims3AndGreater(new long[]{1,2}));
		assertArrayEquals(new long[]{3}, Dimensions.getDims3AndGreater(new long[]{1,2,3}));
		assertArrayEquals(new long[]{3,4}, Dimensions.getDims3AndGreater(new long[]{1,2,3,4}));
		assertArrayEquals(new long[]{3,4,5}, Dimensions.getDims3AndGreater(new long[]{1,2,3,4,5}));
	}

	@Test
	public void testGetDims3AndGreater()
	{
		try {
			Dimensions.getDims3AndGreater(new long[]{});
			fail();
		} catch (IllegalArgumentException e) {
			assertTrue(true);
		}
		try {
			Dimensions.getDims3AndGreater(new long[]{1});
			fail();
		} catch (IllegalArgumentException e) {
			assertTrue(true);
		}
		assertArrayEquals(new long[]{},
			Dimensions.getDims3AndGreater(new long[]{1,2}));
		assertArrayEquals(new long[]{3},
			Dimensions.getDims3AndGreater(new long[]{1,2,3}));
		assertArrayEquals(new long[]{3,4},
			Dimensions.getDims3AndGreater(new long[]{1,2,3,4}));
		assertArrayEquals(new long[]{3,4,5},
			Dimensions.getDims3AndGreater(new long[]{1,2,3,4,5}));
		assertArrayEquals(new long[]{3,4,5,6},
			Dimensions.getDims3AndGreater(new long[]{1,2,3,4,5,6}));
	}

	@Test
	public void testGetTotalPlanes() {
		assertEquals(0, Dimensions.getTotalPlanes(new long[]{}));
		assertEquals(0, Dimensions.getTotalPlanes(new long[]{0}));
		assertEquals(0, Dimensions.getTotalPlanes(new long[]{1}));
		assertEquals(0, Dimensions.getTotalPlanes(new long[]{8}));
		assertEquals(1, Dimensions.getTotalPlanes(new long[]{1,0}));  // NOTE - nonintuitive but I thinks mathematically its correct
		assertEquals(1, Dimensions.getTotalPlanes(new long[]{1,1}));
		assertEquals(1, Dimensions.getTotalPlanes(new long[]{2,3}));
		assertEquals(1, Dimensions.getTotalPlanes(new long[]{2,5}));
		assertEquals(4, Dimensions.getTotalPlanes(new long[]{2,3,4}));
		assertEquals(2, Dimensions.getTotalPlanes(new long[]{4,3,2}));
		assertEquals(35, Dimensions.getTotalPlanes(new long[]{1,3,5,7}));
		assertEquals(360, Dimensions.getTotalPlanes(new long[]{1,2,3,4,5,6}));
		assertEquals(65535, Dimensions.getTotalPlanes(new long[]{65535, 65535, 65535}));
	}

	@Test
	public void testVerifyDimensions()
	{
		// fail if span dims don't match origin dims
		shouldFail(new long[]{1,2,3}, new long[]{0,0,0}, new long[]{1,1});

		// fail if origin/span dimensions don't match image dimensions
		shouldFail(new long[]{1,2,3}, new long[]{0,0}, new long[]{1,1});

		// fail if origin outside the bounds of source image
		shouldFail(new long[]{1,2,3}, new long[]{1,0,0}, new long[]{1,1,1});

		// fail if any span dim < 1
		shouldFail(new long[]{1,2,3}, new long[]{0,0,0}, new long[]{0,1,1});

		// fail if origin + span outside the bounds of the input image
		shouldFail(new long[]{1,2,3}, new long[]{0,0,0}, new long[]{2,0,0});

		// succeed for any other case
		
		// full size span
		shouldSucceed(new long[]{2,3,4}, new long[]{0,0,0}, new long[]{2,3,4});
		
		// partial size span
		shouldSucceed(new long[]{2,3,4}, new long[]{0,0,0}, new long[]{1,1,1});
		
		// complete subset of original
		shouldSucceed(new long[]{2,3,4}, new long[]{1,1,1}, new long[]{1,1,2});
	}

	@Test
	public void testVerifyDimensions2() {
		final boolean FAIL = true;

		// origin len != span len
		verifyDims(FAIL, new long[]{1}, new long[]{0}, new long[]{1,1});

		// origin len != dim len
		verifyDims(FAIL, new long[]{1}, new long[]{0,0}, new long[]{1,1});

		// dim len != span len
		verifyDims(FAIL, new long[]{1,2}, new long[]{0,0}, new long[]{1});

		// origin outside image in some dim
		verifyDims(FAIL, new long[]{1,2,3}, new long[]{0,0,-1}, new long[]{1,1,1});
		verifyDims(FAIL, new long[]{1,2,3}, new long[]{0,0,3}, new long[]{1,1,1});
		verifyDims(FAIL, new long[]{1,2,3}, new long[]{0,-1,0}, new long[]{1,1,1});
		verifyDims(FAIL, new long[]{1,2,3}, new long[]{0,2,0}, new long[]{1,1,1});
		verifyDims(FAIL, new long[]{1,2,3}, new long[]{-1,0,0}, new long[]{1,1,1});
		verifyDims(FAIL, new long[]{1,2,3}, new long[]{1,0,0}, new long[]{1,1,1});

		// span <= 0 in some dim
		verifyDims(FAIL, new long[]{1,2,3}, new long[]{0,0,-1}, new long[]{0,1,1});
		verifyDims(FAIL, new long[]{1,2,3}, new long[]{0,0,3}, new long[]{1,0,1});
		verifyDims(FAIL, new long[]{1,2,3}, new long[]{0,-1,0}, new long[]{1,1,0});

		// origin + span outside image in some dim
		verifyDims(FAIL, new long[]{1}, new long[]{0}, new long[]{2});
		verifyDims(FAIL, new long[]{2,2}, new long[]{0,1}, new long[]{2,2});
		verifyDims(FAIL, new long[]{2,2}, new long[]{1,0}, new long[]{2,2});

		// all other cases should succeed

		final boolean SUCCEED = false;

		verifyDims(SUCCEED, new long[]{2,2}, new long[]{0,0}, new long[]{2,2});
		verifyDims(SUCCEED, new long[]{2,2}, new long[]{0,0}, new long[]{1,1});
		verifyDims(SUCCEED, new long[]{2,2}, new long[]{0,0}, new long[]{1,2});
		verifyDims(SUCCEED, new long[]{2,2}, new long[]{0,0}, new long[]{2,1});
		verifyDims(SUCCEED, new long[]{2,2}, new long[]{1,0}, new long[]{1,1});
		verifyDims(SUCCEED, new long[]{2,2}, new long[]{1,0}, new long[]{1,2});
		verifyDims(SUCCEED, new long[]{2,2}, new long[]{0,1}, new long[]{1,1});
		verifyDims(SUCCEED, new long[]{2,2}, new long[]{0,1}, new long[]{2,1});
		verifyDims(SUCCEED, new long[]{2,2}, new long[]{1,1}, new long[]{1,1});
	}

	// -- Helper methods --

	private void getDims3AndGreaterShouldFail(long[] dims) {
		try {
			Dimensions.getDims3AndGreater(dims);
			fail();
		} catch (IllegalArgumentException e) {
			assertTrue(true);
		}
	}

	private void verifyDims(boolean shouldFail, long[] dims, long[] origin, long[] span) {
		try {
			Dimensions.verifyDimensions(dims, origin, span);
			if (shouldFail)
				fail();
			else
				assertTrue(true);
		} catch (IllegalArgumentException e) {
			if (shouldFail)
				assertTrue(true);
			else
				fail();
		}
	}

	private void shouldFail(long[] imageDims, long[] origin, long[] span) {
		try {
			Dimensions.verifyDimensions(imageDims, origin, span);
			fail();
		} catch (IllegalArgumentException e) {
			assertTrue(true);
		}
	}
	
	private void shouldSucceed(long[] imageDims, long[] origin, long[] span) {
		try {
			Dimensions.verifyDimensions(imageDims, origin, span);
			assertTrue(true);
		} catch (IllegalArgumentException e) {
			fail();
		}
	}
	
}
