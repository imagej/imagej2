//
// SplineFitterTest.java
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
import static org.junit.Assert.assertNotNull;
import ij.Assert;
import ij.IJInfo;

import org.junit.Test;

/**
 * Unit tests for {@link SplineFitter}.
 *
 * @author Barry DeZonia
 */
public class SplineFitterTest {

	SplineFitter sf;
	
	@Test
	public void testSplineFitter() {
		// methods that fail catastrophically as of 1.43o
		if (IJInfo.RUN_ENHANCED_TESTS)
		{
			// just test that these don't crash
			sf = new SplineFitter(null,null,-1);                 // pathological data
			sf = new SplineFitter(new int[]{},new int[]{},0);    // 0 items
			sf = new SplineFitter(new int[]{1},new int[]{2},1);  // 1 item
		}
		
		// try various valid data cases: num items >= 2
		
		sf = new SplineFitter(new int[]{1,2},new int[]{2,5},2);
		assertNotNull(sf);

		sf = new SplineFitter(new int[]{-3,2},new int[]{88,5},2);
		assertNotNull(sf);

		sf = new SplineFitter(new int[]{0,0,0,0},new int[]{88,14,33,12},4);
		assertNotNull(sf);

		sf = new SplineFitter(new int[]{1,1,1,1},new int[]{88,14,33,12},4);
		assertNotNull(sf);

		sf = new SplineFitter(new int[]{18,19,20,21},new int[]{0,0,0,0},4);
		assertNotNull(sf);
	}

	@Test
	public void testEvalSpline() {
		int[] x,y;
		
		if (IJInfo.RUN_ENHANCED_TESTS)
		{
			// try pathological data
			sf = new SplineFitter(new int[]{-3,2,7,14},new int[]{88,77,66,55},4);
			x = null;
			y = null;
			assertEquals(6.0, sf.evalSpline(x, y, -1, 0.0), Assert.DOUBLE_TOL);
		}
		
		// try valid data sets : pure regression tests generated from existing runtime

		// 2.0, 2 frame, 2 vals, frame x inc, frame y inc
		x = new int[] {1,2};
		y = new int[] {4,5};
		sf = new SplineFitter(x,y,x.length);
		assertEquals(5.0, sf.evalSpline(x, y, x.length, 2.0), Assert.DOUBLE_TOL);

		// 9.0, 2 frame, 2 vals, frame x inc, frame y inc
		x = new int[] {6,5};
		y = new int[] {8,4};
		sf = new SplineFitter(x,y,x.length);
		assertEquals(20.0, sf.evalSpline(x, y, x.length, 9.0), Assert.DOUBLE_TOL);

		// 3.0, 3 frame, 2 vals, frame x inc, frame y inc
		x = new int[] {1,2,3};
		y = new int[] {4,5,6};
		sf = new SplineFitter(x,y,x.length);
		assertEquals(6.0, sf.evalSpline(x, y, x.length, 3.0), Assert.DOUBLE_TOL);

		// 3.0, 3 frame, 2 vals, frame x inc, frame y dec
		x = new int[] {-3,2,7};
		y = new int[] {88,77,66};
		sf = new SplineFitter(x,y,x.length);
		assertEquals(74.8, sf.evalSpline(x, y, x.length, 3.0), Assert.DOUBLE_TOL);
		
		// 4.0, 3 frame, 2 vals, frame x dec, frame y inc
		x = new int[]{-3,-6,-10};
		y = new int[]{22,104,505};
		sf = new SplineFitter(x,y,x.length);
		assertEquals(-655.44444, sf.evalSpline(x, y, 2, 4.0), Assert.DOUBLE_TOL);

		// 5.0, 4 frame, 4 vals, frame x bouncing, frame y bouncing
		x = new int[]{21,14,8,33};
		y = new int[]{19,99,17,44};
		sf = new SplineFitter(x,y,x.length);
		assertEquals(-203.87166, sf.evalSpline(x, y, x.length, 5.0), Assert.DOUBLE_TOL);

		// 7.0, 4 frame, 4 vals, frame x bouncing, frame y inc
		x = new int[]{108,-13,0,19};
		y = new int[]{8,22,44,63};
		sf = new SplineFitter(x,y,x.length);
		assertEquals(52.23443, sf.evalSpline(x, y, x.length, 7.0), Assert.DOUBLE_TOL);

		// 6.0, 4 frame, 4 vals, frame x dec, frame y inc
		x = new int[]{93,77,74,44};
		y = new int[]{8,22,44,63};
		sf = new SplineFitter(x,y,x.length);
		assertEquals(7186.50862, sf.evalSpline(x, y, x.length, 6.0), Assert.DOUBLE_TOL);

		// 2.5, 4 frame, 4 vals, frame x dec, frame y flat
		x = new int[]{108,84,62,23};
		y = new int[]{4,4,4,4};
		sf = new SplineFitter(x,y,x.length);
		assertEquals(4.00000, sf.evalSpline(x, y, x.length, 2.5), Assert.DOUBLE_TOL);

		// 52.0, 4 frame, 4 vals, frame x dec, frame y dec
		x = new int[]{63,57,51,46};
		y = new int[]{8,4,2,1};
		sf = new SplineFitter(x,y,x.length);
		assertEquals(2.73600, sf.evalSpline(x, y, x.length, 52.0), Assert.DOUBLE_TOL);

		// 21.0, 5 frame, 3 vals, frame x inc, frame y inc
		x = new int[]{18,22,31,45,59};
		y = new int[]{3,6,19,31,50};
		sf = new SplineFitter(x,y,x.length);
		assertEquals(5.06582, sf.evalSpline(x, y, x.length, 21.0), Assert.DOUBLE_TOL);
	}

}
