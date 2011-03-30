//
// SpanTest.java
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
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import imagej.util.Span;

import org.junit.Test;

public class SpanTest {

	@Test
	public void testCreateInt() {
		int[] vals;
		
		try {
			vals = Span.create(-1);
			fail();
		} catch (NegativeArraySizeException e) {
			assertTrue(true);
		}

		vals = Span.create(0);
		assertArrayEquals(new int[]{},vals);
		
		vals = Span.create(1);
		assertArrayEquals(new int[]{0},vals);
		
		vals = Span.create(2);
		assertArrayEquals(new int[]{0,0},vals);
		
		vals = Span.create(3);
		assertArrayEquals(new int[]{0,0,0},vals);
	}

	@Test
	public void testCreateIntArray() {
		int[] vals;
		
		vals = Span.create(new int[]{});
		assertArrayEquals(new int[]{},vals);
		
		vals = Span.create(new int[]{4});
		assertArrayEquals(new int[]{4},vals);
		
		vals = Span.create(new int[]{1,7});
		assertArrayEquals(new int[]{1,7},vals);
		
		vals = Span.create(new int[]{1,2,3,4,5,6,7,8,9});
		assertArrayEquals(new int[]{1,2,3,4,5,6,7,8,9},vals);
	}

	@Test
	public void testSinglePlane() {
		int[] vals;
		
		int width, height, totalDims;
		
		// test failure cases first
		
		//   width <= 0
		try {
			width = 0; height = 106; totalDims = 7;
			vals = Span.singlePlane(width, height, totalDims);
			fail();
		} catch (IllegalArgumentException e) {
			assertTrue(true);
		}

		//   height <= 0
		
		try {
			width = 23; height = 0; totalDims = 5;
			vals = Span.singlePlane(width, height, totalDims);
			fail();
		} catch (IllegalArgumentException e) {
			assertTrue(true);
		}
		
		//   total dims < 2

		try {
			width = 14; height = 7; totalDims = 1;
			vals = Span.singlePlane(width, height, totalDims);
			fail();
		} catch (IllegalArgumentException e) {
			assertTrue(true);
		}

		// otherwise should succeed
		
		width = 19; height = 5005; totalDims = 2;
		vals = Span.singlePlane(width, height, totalDims);
		assertArrayEquals(new int[]{19,5005},vals);
		
		width = 86; height = 161; totalDims = 3;
		vals = Span.singlePlane(width, height, totalDims);
		assertArrayEquals(new int[]{86,161,1},vals);
		
		width = 658; height = 476; totalDims = 6;
		vals = Span.singlePlane(width, height, totalDims);
		assertArrayEquals(new int[]{658,476,1,1,1,1},vals);
	}
	
	@Test
	public void testSpanWholeRange()
	{
		int[] inputDims = new int[]{1,2,3,4,5,6,7,8,9};
		
		assertArrayEquals(new int[]{1,2,3,4,5,6,7,8,9}, Span.wholeRange(inputDims));
	}

}
