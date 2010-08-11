package ij.process;

import static org.junit.Assert.*;

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

}
