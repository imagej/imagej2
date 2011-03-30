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

import static org.junit.Assert.*;
import imagej.util.Dimensions;

import org.junit.Test;

public class DimensionsTest {

	@Test
	public void testGetDims3AndGreater()
	{
		try {
			Dimensions.getDims3AndGreater(new int[]{});
			fail();
		} catch (IllegalArgumentException e) {
			assertTrue(true);
		}
		try {
			Dimensions.getDims3AndGreater(new int[]{1});
			fail();
		} catch (IllegalArgumentException e) {
			assertTrue(true);
		}
		assertArrayEquals(new int[]{}, Dimensions.getDims3AndGreater(new int[]{1,2}));
		assertArrayEquals(new int[]{3}, Dimensions.getDims3AndGreater(new int[]{1,2,3}));
		assertArrayEquals(new int[]{3,4}, Dimensions.getDims3AndGreater(new int[]{1,2,3,4}));
		assertArrayEquals(new int[]{3,4,5}, Dimensions.getDims3AndGreater(new int[]{1,2,3,4,5}));
		assertArrayEquals(new int[]{3,4,5,6}, Dimensions.getDims3AndGreater(new int[]{1,2,3,4,5,6}));
	}

	@Test
	public void testGetTotalSamples()
	{
		assertEquals(0, Dimensions.getTotalSamples(new int[]{}));
		assertEquals(1, Dimensions.getTotalSamples(new int[]{1}));
		assertEquals(7, Dimensions.getTotalSamples(new int[]{7}));
		assertEquals(0, Dimensions.getTotalSamples(new int[]{1,0}));
		assertEquals(6, Dimensions.getTotalSamples(new int[]{2,3}));
		assertEquals(24, Dimensions.getTotalSamples(new int[]{4,3,2}));
		assertEquals(105, Dimensions.getTotalSamples(new int[]{1,3,5,7}));
		assertEquals(4294836225L, Dimensions.getTotalSamples(new int[]{0xffff,0xffff}));
	}

	@Test
	public void testGetTotalPlanes()
	{
		assertEquals(0, Dimensions.getTotalPlanes(new int[]{}));
		assertEquals(0, Dimensions.getTotalPlanes(new int[]{1}));
		assertEquals(1, Dimensions.getTotalPlanes(new int[]{1,0}));  // NOTE - nonintuitive but I thinks mathematically its correct
		assertEquals(1, Dimensions.getTotalPlanes(new int[]{2,3}));
		assertEquals(2, Dimensions.getTotalPlanes(new int[]{4,3,2}));
		assertEquals(35, Dimensions.getTotalPlanes(new int[]{1,3,5,7}));
		assertEquals(0xffff, Dimensions.getTotalPlanes(new int[]{0xffff,0xffff, 0xffff}));
	}

	private void shouldFail(int[] imageDims, int[] origin, int[] span)
	{
		try {
			Dimensions.verifyDimensions(imageDims, origin, span);
			fail();
		} catch (IllegalArgumentException e) {
			assertTrue(true);
		}
	}
	
	private void shouldSucceed(int[] imageDims, int[] origin, int[] span)
	{
		try {
			Dimensions.verifyDimensions(imageDims, origin, span);
			assertTrue(true);
		} catch (IllegalArgumentException e) {
			fail();
		}
	}
	
	@Test
	public void testVerifyDimensions()
	{
		// fail if span dims don't match origin dims
		shouldFail(new int[]{1,2,3}, new int[]{0,0,0}, new int[]{1,1});

		// fail if origin/span dimensions don't match image dimensions
		shouldFail(new int[]{1,2,3}, new int[]{0,0}, new int[]{1,1});

		// fail if origin outside the bounds of source image
		shouldFail(new int[]{1,2,3}, new int[]{1,0,0}, new int[]{1,1,1});

		// fail if any span dim < 1
		shouldFail(new int[]{1,2,3}, new int[]{0,0,0}, new int[]{0,1,1});

		// fail if origin + span outside the bounds of the input image
		shouldFail(new int[]{1,2,3}, new int[]{0,0,0}, new int[]{2,0,0});

		// succeed for any other case
		
		// full size span
		shouldSucceed(new int[]{2,3,4}, new int[]{0,0,0}, new int[]{2,3,4});
		
		// partial size span
		shouldSucceed(new int[]{2,3,4}, new int[]{0,0,0}, new int[]{1,1,1});
		
		// complete subset of original
		shouldSucceed(new int[]{2,3,4}, new int[]{1,1,1}, new int[]{1,1,2});
	}

}
