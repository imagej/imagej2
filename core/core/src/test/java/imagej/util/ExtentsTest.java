package imagej.util;

import static org.junit.Assert.*;

import org.junit.Test;


public class ExtentsTest {

	private Extents ext;
	
	@Test
	public void testExtents() {
		ext = new Extents(new long[]{});
		ext = new Extents(new long[]{1});
		ext = new Extents(new long[]{1,2});
		ext = new Extents(new long[]{1,2,3});
		ext = new Extents(new long[]{1,2,3,4});
		assertTrue(true);
	}

	@Test
	public void testCreatePosition() {
		ext = new Extents(new long[]{1,2,3});
		Position pos = ext.createPosition();
		assertEquals(ext.numDimensions(), pos.numDimensions());
	}

	@Test
	public void testNumDimensions() {
		ext = new Extents(new long[]{});
		assertEquals(0,ext.numDimensions());
		ext = new Extents(new long[]{1});
		assertEquals(1,ext.numDimensions());
		ext = new Extents(new long[]{1,2});
		assertEquals(2,ext.numDimensions());
		ext = new Extents(new long[]{1,2,3});
		assertEquals(3,ext.numDimensions());
		ext = new Extents(new long[]{1,2,3,4});
		assertEquals(4,ext.numDimensions());
	}

	@Test
	public void testDimension() {
		ext = new Extents(new long[]{1,2,3,4});
		assertEquals(1,ext.dimension(0));
		assertEquals(2,ext.dimension(1));
		assertEquals(3,ext.dimension(2));
		assertEquals(4,ext.dimension(3));
	}

	@Test
	public void testDimensions() {
		ext = new Extents(new long[]{1,2,3,4});
		long[] dims = new long[ext.numDimensions()];
		ext.dimensions(dims);
		assertEquals(4, dims.length);
		assertEquals(1, dims[0]);
		assertEquals(2, dims[1]);
		assertEquals(3, dims[2]);
		assertEquals(4, dims[3]);
	}

	@Test
	public void testNumElements() {
		ext = new Extents(new long[]{});
		assertEquals(0,ext.numElements());
		ext = new Extents(new long[]{7});
		assertEquals(7,ext.numElements());
		ext = new Extents(new long[]{7,5});
		assertEquals(35,ext.numElements());
		ext = new Extents(new long[]{7,5,3});
		assertEquals(105,ext.numElements());
		ext = new Extents(new long[]{7,5,3,1});
		assertEquals(105,ext.numElements());
	}

}
