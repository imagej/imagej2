package imagej.data;

import static org.junit.Assert.*;
import imagej.data.Extents;
import imagej.data.Position;

import org.junit.Test;


public class PositionTest {

	private Extents extents;
	private Position pos;
	
	@Test
	public void testPosition() {
		extents = new Extents(new long[]{2,3,4});
		pos = new Position(extents);
		assertTrue(true);
	}

	@Test
	public void testFirst() {
		pos = new Extents(new long[]{2,3,4}).createPosition();
		pos.first();
		for (int i = 0; i < pos.numDimensions(); i++)
			assertEquals(0, pos.get(i));
	}

	@Test
	public void testLast() {
		pos = new Extents(new long[]{2,3,4}).createPosition();
		pos.last();
		for (int i = 0; i < pos.numDimensions(); i++)
			assertEquals(pos.dimension(i)-1, pos.get(i));
	}

	@Test
	public void testFwd() {
		pos = new Extents(new long[]{2,3,4,5,6}).createPosition();
		pos.first();
		assertEquals(0, pos.getIndex());
		for (int i = 1; i < pos.getExtents().numElements(); i++) {
			pos.fwd();
			assertEquals(i, pos.getIndex());
		}
		try {
			pos.fwd();
			fail();
		} catch (IllegalArgumentException e) {
			assertTrue(true);
		}
	}

	@Test
	public void testBack() {
		pos = new Extents(new long[]{2,3,4,5,6}).createPosition();
		pos.last();
		assertEquals(pos.getExtents().numElements()-1, pos.getIndex());
		for (long i = pos.getExtents().numElements()-1; i > 0; i--) {
			pos.back();
			assertEquals(i-1, pos.getIndex());
		}
		try {
			pos.back();
			fail();
		} catch (IllegalArgumentException e) {
			assertTrue(true);
		}
	}

	@Test
	public void testMoveLongInt() {
		pos = new Extents(new long[]{2,3,4}).createPosition();
		pos.set(new long[]{1,2,2});
		pos.move(-1,0);
		assertEquals(0, pos.get(0));
		pos.move(-2,1);
		assertEquals(0, pos.get(1));
		pos.move(1,2);
		assertEquals(3, pos.get(2));
	}

	@Test
	public void testMoveLongArray() {
		pos = new Extents(new long[]{2,3,4}).createPosition();
		pos.set(new long[]{1,2,2});
		pos.move(new long[]{-1,0,1});
		assertEquals(0, pos.get(0));
		assertEquals(2, pos.get(1));
		assertEquals(3, pos.get(2));
	}

	@Test
	public void testGetInt() {
		pos = new Extents(new long[]{2,3,4}).createPosition();
		pos.set(new long[]{1,2,2});
		assertEquals(1, pos.get(0));
		assertEquals(2, pos.get(1));
		assertEquals(2, pos.get(2));
	}

	@Test
	public void testGetLongArray() {
		pos = new Extents(new long[]{2,3,4}).createPosition();
		pos.set(new long[]{0,1,3});
		long[] position = new long[3];
		pos.get(position);
		assertEquals(0,position[0]);
		assertEquals(1,position[1]);
		assertEquals(3,position[2]);
	}

	@Test
	public void testSetLongInt() {
		pos = new Extents(new long[]{2,3,4}).createPosition();
		pos.set(1,0);
		pos.set(2,1);
		pos.set(2,2);
		assertEquals(1, pos.get(0));
		assertEquals(2, pos.get(1));
		assertEquals(2, pos.get(2));
	}

	@Test
	public void testSetLongArray() {
		pos = new Extents(new long[]{2,3,4}).createPosition();
		pos.set(new long[]{1,2,2});
		assertEquals(1, pos.get(0));
		assertEquals(2, pos.get(1));
		assertEquals(2, pos.get(2));
	}

	@Test
	public void testNumDimensions() {
		pos = new Extents(new long[]{}).createPosition();
		assertEquals(0, pos.numDimensions());
		pos = new Extents(new long[]{1}).createPosition();
		assertEquals(1, pos.numDimensions());
		pos = new Extents(new long[]{1,1}).createPosition();
		assertEquals(2, pos.numDimensions());
		pos = new Extents(new long[]{1,1,1}).createPosition();
		assertEquals(3, pos.numDimensions());
		pos = new Extents(new long[]{1,1,1,1}).createPosition();
		assertEquals(4, pos.numDimensions());
	}

	@Test
	public void testDimension() {
		pos = new Extents(new long[]{7,5,3,1}).createPosition();
		assertEquals(7, pos.dimension(0));
		assertEquals(5, pos.dimension(1));
		assertEquals(3, pos.dimension(2));
		assertEquals(1, pos.dimension(3));
	}

	@Test
	public void testSetIndex() {
		long[] dims = new long[]{8,6,4,2};
		pos = new Extents(dims).createPosition();
		long index = 0;
		for (int l = 0; l < dims[3]; l++) {
			for (int k = 0; k < dims[2]; k++) {
				for (int j = 0; j < dims[1]; j++) {
					for (int i = 0; i < dims[0]; i++) {
						pos.setIndex(index++);
						assertEquals(i,pos.get(0));
						assertEquals(j,pos.get(1));
						assertEquals(k,pos.get(2));
						assertEquals(l,pos.get(3));
					}
				}
			}
		}
		try {
			pos.setIndex(-1);
			fail();
		} catch (IllegalArgumentException e) {
			assertTrue(true);
		}
		try {
			pos.setIndex(dims[0]*dims[1]*dims[2]*dims[3]);
			fail();
		} catch (IllegalArgumentException e) {
			assertTrue(true);
		}
	}

	@Test
	public void testGetIndex() {
		long[] dims = new long[]{8,6,4,2};
		pos = new Extents(dims).createPosition();
		long index = 0;
		for (int l = 0; l < dims[3]; l++) {
			for (int k = 0; k < dims[2]; k++) {
				for (int j = 0; j < dims[1]; j++) {
					for (int i = 0; i < dims[0]; i++) {
						pos.set(i, 0);
						pos.set(j, 1);
						pos.set(k, 2);
						pos.set(l, 3);
						assertEquals(index++,pos.getIndex());
					}
				}
			}
		}
	}

}
