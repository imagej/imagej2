package ij.process;

import static org.junit.Assert.*;

import org.junit.Test;

public class IndexTest {

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
	public void testCreateIntIntIntArray() {
		int[] vals;
	
		// failure cases first

		//   x < 0
		
		try {
			vals = Index.create(-1, 1, new int[]{1,1,1});
			fail();
		} catch (IllegalArgumentException e) {
			assertTrue(true);
		}
		
		//   y < 0
		
		try {
			vals = Index.create(1, -1, new int[]{1,1,1});
			fail();
		} catch (IllegalArgumentException e) {
			assertTrue(true);
		}
		
		// otherwise good input

		vals = Index.create(0, 0, new int[]{});
		assertArrayEquals(new int[]{0,0},vals);
		
		vals = Index.create(0, 0, new int[]{1});
		assertArrayEquals(new int[]{0,0,1},vals);
		
		vals = Index.create(0, 0, new int[]{1,2});
		assertArrayEquals(new int[]{0,0,1,2},vals);
		
		vals = Index.create(0, 0, new int[]{1,2,3});
		assertArrayEquals(new int[]{0,0,1,2,3},vals);
		
		vals = Index.create(5, 6, new int[]{9,8,7,3,2,1});
		assertArrayEquals(new int[]{5,6,9,8,7,3,2,1},vals);		
	}

}
