package ij.measure;

import static org.junit.Assert.*;
import ij.Assert;
import ij.IJInfo;

import org.junit.Test;

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

		// 2.0, 2 frame, 2 vals, frame x inc, frame y inc, x inc, y inc
		sf = new SplineFitter(new int[]{1,2},new int[]{7,12},2);
		x = new int[] {1,2};
		y = new int[] {4,5};
		assertEquals(5.0, sf.evalSpline(x, y, 2, 2.0), Assert.DOUBLE_TOL);

		// 9.0, 2 frame, 2 vals, frame x inc, frame y inc, x dec, y dec
		sf = new SplineFitter(new int[]{3,7},new int[]{6,11},2);
		x = new int[] {6,5};
		y = new int[] {8,4};
		assertEquals(20, sf.evalSpline(x, y, 2, 9.0), Assert.DOUBLE_TOL);

		// 3.0, 3 frame, 2 vals, frame x inc, frame y inc, x inc, y inc
		sf = new SplineFitter(new int[]{1,2,3},new int[]{4,5,6},3);
		x = new int[] {1,2};
		y = new int[] {4,5};
		assertEquals(6.0, sf.evalSpline(x, y, 2, 3.0), Assert.DOUBLE_TOL);

		// 3.0, 3 frame, 2 vals, frame x inc, frame y dec, x inc, y inc
		sf = new SplineFitter(new int[]{-3,2,7},new int[]{88,77,66},3);
		x = new int[] {1,2};
		y = new int[] {4,7};
		assertEquals(10.0, sf.evalSpline(x, y, 2, 3.0), Assert.DOUBLE_TOL);
		
		// 4.0, 3 frame, 2 vals, frame x dec, frame y inc, x dec, y dec
		sf = new SplineFitter(new int[]{-3,-6,-10},new int[]{22,104,505},3);
		x = new int[] {9,3};
		y = new int[] {7,5};
		assertEquals(-42.40972, sf.evalSpline(x, y, 2, 4.0), Assert.DOUBLE_TOL);

		// 5.0, 4 frame, 4 vals, frame x bouncing, frame y bouncing, x inc, y inc
		sf = new SplineFitter(new int[]{21,14,8,33},new int[]{19,99,17,44},4);
		x = new int[] {1,5,9,14};
		y = new int[] {2,4,6,8};
		assertEquals(4.0, sf.evalSpline(x, y, x.length, 5.0), Assert.DOUBLE_TOL);

		// 7.0, 4 frame, 4 vals, frame x bouncing, frame y inc, x dec, y inc
		sf = new SplineFitter(new int[]{108,-13,0,19},new int[]{8,22,44,63},4);
		x = new int[] {7,5,3,1};
		y = new int[] {1,2,4,8};
		assertEquals(-4.86467, sf.evalSpline(x, y, x.length, 7.0), Assert.DOUBLE_TOL);

		// 6.0, 4 frame, 4 vals, frame x dec, frame y inc, x dec, y dec
		sf = new SplineFitter(new int[]{93,77,74,44},new int[]{8,22,44,63},4);
		x = new int[] {7,5,3,1};
		y = new int[] {8,4,2,1};
		assertEquals(-2.25578, sf.evalSpline(x, y, x.length, 6.0), Assert.DOUBLE_TOL);

		// 2.5, 4 frame, 4 vals, frame x dec, frame y flat, x dec, y inc
		sf = new SplineFitter(new int[]{108,84,62,23},new int[]{4,4,4,4},4);
		x = new int[] {7,5,3,1};
		y = new int[] {2,4,7,11};
		assertEquals(6.5, sf.evalSpline(x, y, x.length, 2.5), Assert.DOUBLE_TOL);

		// 52.0, 4 frame, 4 vals, frame x dec, frame y dec, x dec, y dec
		sf = new SplineFitter(new int[]{63,57,51,46},new int[]{8,4,2,1},4);
		x = new int[] {7,5,3,1};
		y = new int[] {11,10,9,8};
		assertEquals(195.02073, sf.evalSpline(x, y, x.length, 52.0), Assert.DOUBLE_TOL);

		// 21.0, 5 frame, 3 vals, frame x inc, frame y inc, x dec, y dec
		sf = new SplineFitter(new int[]{18,22,31,45,59},new int[]{3,6,19,31,50},5);
		x = new int[] {99,88,77};
		y = new int[] {66,44,22};
		assertEquals(1393.40162, sf.evalSpline(x, y, x.length, 21.0), Assert.DOUBLE_TOL);
	}

}
