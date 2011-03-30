//
// UtilsTest.java
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
import imagej.util.Dimensions;

import org.junit.Test;

public class UtilsTest {

	// *************  instance vars ********************************************

	int width = 224, height = 403;

	// *************  private helpers ********************************************


	private void getDims3AndGreaterShouldFail(int[] dims)
	{
		try {
			Dimensions.getDims3AndGreater(dims);
			fail();
		} catch (IllegalArgumentException e) {
			assertTrue(true);
		}
	}

	private void verifyDims(boolean shouldFail, int[] dims, int[] origin, int[] span)
	{
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

	// *************  public tests ********************************************

	@Test
	public void testgetDims3AndGreater() {
		getDims3AndGreaterShouldFail(new int[]{});
		getDims3AndGreaterShouldFail(new int[]{1});
		assertArrayEquals(new int[]{}, Dimensions.getDims3AndGreater(new int[]{1,2}));
		assertArrayEquals(new int[]{3}, Dimensions.getDims3AndGreater(new int[]{1,2,3}));
		assertArrayEquals(new int[]{3,4}, Dimensions.getDims3AndGreater(new int[]{1,2,3,4}));
		assertArrayEquals(new int[]{3,4,5}, Dimensions.getDims3AndGreater(new int[]{1,2,3,4,5}));
	}

	@Test
	public void testGetTotalSamples() {
		assertEquals(0,Dimensions.getTotalSamples(new int[]{}));
		assertEquals(0,Dimensions.getTotalSamples(new int[]{0}));
		assertEquals(1,Dimensions.getTotalSamples(new int[]{1}));
		assertEquals(8,Dimensions.getTotalSamples(new int[]{8}));
		assertEquals(1,Dimensions.getTotalSamples(new int[]{1,1}));
		assertEquals(10,Dimensions.getTotalSamples(new int[]{2,5}));
		assertEquals(24,Dimensions.getTotalSamples(new int[]{2,3,4}));
		assertEquals(720,Dimensions.getTotalSamples(new int[]{1,2,3,4,5,6}));
	}

	@Test
	public void testGetTotalPlanes() {
		assertEquals(0,Dimensions.getTotalPlanes(new int[]{}));
		assertEquals(0,Dimensions.getTotalPlanes(new int[]{0}));
		assertEquals(0,Dimensions.getTotalPlanes(new int[]{1}));
		assertEquals(0,Dimensions.getTotalPlanes(new int[]{8}));
		assertEquals(1,Dimensions.getTotalPlanes(new int[]{1,1}));
		assertEquals(1,Dimensions.getTotalPlanes(new int[]{2,5}));
		assertEquals(4,Dimensions.getTotalPlanes(new int[]{2,3,4}));
		assertEquals(360,Dimensions.getTotalPlanes(new int[]{1,2,3,4,5,6}));
	}

	@Test
	public void testVerifyDimensions()
	{
		final boolean FAIL = true;

		// origin len != span len
		verifyDims(FAIL, new int[]{1}, new int[]{0}, new int[]{1,1});

		// origin len != dim len
		verifyDims(FAIL, new int[]{1}, new int[]{0,0}, new int[]{1,1});

		// dim len != span len
		verifyDims(FAIL, new int[]{1,2}, new int[]{0,0}, new int[]{1});

		// origin outside image in some dim
		verifyDims(FAIL, new int[]{1,2,3}, new int[]{0,0,-1}, new int[]{1,1,1});
		verifyDims(FAIL, new int[]{1,2,3}, new int[]{0,0,3}, new int[]{1,1,1});
		verifyDims(FAIL, new int[]{1,2,3}, new int[]{0,-1,0}, new int[]{1,1,1});
		verifyDims(FAIL, new int[]{1,2,3}, new int[]{0,2,0}, new int[]{1,1,1});
		verifyDims(FAIL, new int[]{1,2,3}, new int[]{-1,0,0}, new int[]{1,1,1});
		verifyDims(FAIL, new int[]{1,2,3}, new int[]{1,0,0}, new int[]{1,1,1});

		// span <= 0 in some dim
		verifyDims(FAIL, new int[]{1,2,3}, new int[]{0,0,-1}, new int[]{0,1,1});
		verifyDims(FAIL, new int[]{1,2,3}, new int[]{0,0,3}, new int[]{1,0,1});
		verifyDims(FAIL, new int[]{1,2,3}, new int[]{0,-1,0}, new int[]{1,1,0});

		// origin + span outside image in some dim
		verifyDims(FAIL, new int[]{1}, new int[]{0}, new int[]{2});
		verifyDims(FAIL, new int[]{2,2}, new int[]{0,1}, new int[]{2,2});
		verifyDims(FAIL, new int[]{2,2}, new int[]{1,0}, new int[]{2,2});

		// all other cases should succeed

		final boolean SUCCEED = false;

		verifyDims(SUCCEED, new int[]{2,2}, new int[]{0,0}, new int[]{2,2});
		verifyDims(SUCCEED, new int[]{2,2}, new int[]{0,0}, new int[]{1,1});
		verifyDims(SUCCEED, new int[]{2,2}, new int[]{0,0}, new int[]{1,2});
		verifyDims(SUCCEED, new int[]{2,2}, new int[]{0,0}, new int[]{2,1});
		verifyDims(SUCCEED, new int[]{2,2}, new int[]{1,0}, new int[]{1,1});
		verifyDims(SUCCEED, new int[]{2,2}, new int[]{1,0}, new int[]{1,2});
		verifyDims(SUCCEED, new int[]{2,2}, new int[]{0,1}, new int[]{1,1});
		verifyDims(SUCCEED, new int[]{2,2}, new int[]{0,1}, new int[]{2,1});
		verifyDims(SUCCEED, new int[]{2,2}, new int[]{1,1}, new int[]{1,1});
	}


}
