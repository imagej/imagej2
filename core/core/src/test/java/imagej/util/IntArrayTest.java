/*
 * #%L
 * ImageJ software for multidimensional image processing and analysis.
 * %%
 * Copyright (C) 2009 - 2012 Board of Regents of the University of
 * Wisconsin-Madison, Broad Institute of MIT and Harvard, and Max Planck
 * Institute of Molecular Cell Biology and Genetics.
 * %%
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * 
 * The views and conclusions contained in the software and documentation are
 * those of the authors and should not be interpreted as representing official
 * policies, either expressed or implied, of any organization.
 * #L%
 */

package imagej.util;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;

import org.junit.Test;

/**
 * Tests {@link IntArray}.
 * 
 * @author Curtis Rueden
 * @author Johannes Schindelin
 */
public class IntArrayTest extends PrimitiveArrayTest {

	/** Tests {@link IntArray#IntArray()}. */
	@Test
	public void testConstructorNoArgs() {
		final IntArray array = new IntArray();
		assertEquals(0, array.size());
		assertEquals(0, array.copyArray().length);
	}

	/** Tests {@link IntArray#IntArray(int)}. */
	@Test
	public void testConstructorSize() {
		final int size = 24;
		final IntArray array = new IntArray(size);
		assertEquals(size, array.size());
		assertEquals(size, array.copyArray().length);
	}

	/** Tests {@link IntArray#IntArray(int[])}. */
	@Test
	public void testConstructorArray() {
		final int[] raw = { 3, 5, 8, 13, 21 };
		final IntArray array = new IntArray(raw);
		assertSame(raw, array.getArray());
		assertEquals(raw.length, array.size());
		for (int i = 0; i < raw.length; i++) {
			assertEquals("@" + i, raw[i], array.getValue(i));
		}
		assertArrayEquals(raw, array.copyArray());
	}

	/** Tests {@link IntArray#addValue(int)}. */
	@Test
	public void testAddValue() {
		final int[] raw = { 3, 5, 8, 13, 21 };
		final IntArray array = new IntArray(raw.clone());
		final int e6 = 1, e7 = 2;
		array.addValue(e6);
		array.addValue(e7);
		assertEquals(raw.length + 2, array.size());
		for (int i = 0; i < raw.length; i++) {
			assertEquals("@" + i, raw[i], array.getValue(i));
		}
		assertEquals(e6, array.getValue(5));
		assertEquals(e7, array.getValue(6));
	}

	/** Tests {@link IntArray#removeValue(int)}. */
	public void testRemoveValue() {
		final int[] raw = { 3, 5, 8, 13, 21 };
		final IntArray array = new IntArray(raw.clone());
		assertEquals(raw.length, array.size());
		array.removeValue(raw[0]);
		assertEquals(raw.length - 1, array.size());
		array.removeValue(raw[2]);
		assertEquals(raw.length - 2, array.size());
		array.removeValue(raw[4]);
		assertEquals(raw.length - 3, array.size());
		assertEquals(raw[1], array.getValue(0));
		assertEquals(raw[3], array.getValue(1));
	}

	/** Tests {@link IntArray#getValue(int)}. */
	public void testGetValue() {
		final int[] raw = { 3, 5, 8, 13, 21 };
		final IntArray array = new IntArray(raw.clone());
		for (int i = 0; i < raw.length; i++) {
			assertEquals("@" + i, raw[i], array.getValue(i));
		}
	}

	/** Tests {@link IntArray#setValue(int, int)}. */
	@Test
	public void testSetValue() {
		final int[] raw = { 3, 5, 8, 13, 21 };
		final IntArray array = new IntArray(raw.clone());
		final int e0 = 7, e2 = 1, e4 = 2;
		array.setValue(0, e0);
		array.setValue(2, e2);
		array.setValue(4, e4);
		assertEquals(raw.length, array.size());
		assertEquals(e0, array.getValue(0));
		assertEquals(raw[1], array.getValue(1));
		assertEquals(e2, array.getValue(2));
		assertEquals(raw[3], array.getValue(3));
		assertEquals(e4, array.getValue(4));
	}

	/** Tests {@link IntArray#addValue(int, int)}. */
	@Test
	public void testAddValueIndex() {
		final int[] raw = { 3, 5, 8, 13, 21 };
		final IntArray array = new IntArray(raw.clone());
		final int e0 = 7, e4 = 1, e7 = 2;
		array.addValue(0, e0);
		array.addValue(4, e4);
		array.addValue(7, e7);
		assertEquals(raw.length + 3, array.size());
		assertEquals(e0, array.getValue(0));
		assertEquals(raw[0], array.getValue(1));
		assertEquals(raw[1], array.getValue(2));
		assertEquals(raw[2], array.getValue(3));
		assertEquals(e4, array.getValue(4));
		assertEquals(raw[3], array.getValue(5));
		assertEquals(raw[4], array.getValue(6));
		assertEquals(e7, array.getValue(7));
	}

	/** Tests {@link IntArray#remove(int)}. */
	public void testRemoveIndex() {
		final int[] raw = { 3, 5, 8, 13, 21 };
		final IntArray array = new IntArray(raw.clone());
		assertEquals(raw.length, array.size());
		array.remove(0);
		assertEquals(raw.length - 1, array.size());
		array.remove(2);
		assertEquals(raw.length - 2, array.size());
		array.remove(4);
		assertEquals(raw.length - 3, array.size());
		assertEquals(raw[1], array.getValue(0));
		assertEquals(raw[3], array.getValue(1));
	}

	/** Tests {@link IntArray#indexOf(int)}. */
	@Test
	public void testIndexOf() {
		final int[] raw = { 3, 5, 8, 13, 21 };
		final IntArray array = new IntArray(raw.clone());
		for (int i = 0; i < raw.length; i++) {
			assertEquals("@" + i, i, array.indexOf(raw[i]));
		}
		assertEquals(-1, array.indexOf(-1));
		assertEquals(-1, array.indexOf(0));
		assertEquals(-1, array.indexOf(1));
		assertEquals(-1, array.indexOf(Integer.MAX_VALUE));
		assertEquals(-1, array.indexOf(Integer.MIN_VALUE));
	}

	/** Tests {@link IntArray#lastIndexOf(int)}. */
	@Test
	public void testLastIndexOf() {
		final int[] raw = { 3, 5, 8, 13, 21 };
		final IntArray array = new IntArray(raw.clone());
		for (int i = 0; i < raw.length; i++) {
			assertEquals("@" + i, i, array.lastIndexOf(raw[i]));
		}
		assertEquals(-1, array.lastIndexOf(-1));
		assertEquals(-1, array.lastIndexOf(0));
		assertEquals(-1, array.lastIndexOf(1));
		assertEquals(-1, array.lastIndexOf(Integer.MAX_VALUE));
		assertEquals(-1, array.lastIndexOf(Integer.MIN_VALUE));
	}

	/** Tests {@link IntArray#contains(int)}. */
	@Test
	public void testContains() {
		final int[] raw = { 3, 5, 8, 13, 21 };
		final IntArray array = new IntArray(raw.clone());
		for (int i = 0; i < raw.length; i++) {
			assertTrue("@" + i, array.contains(raw[i]));
		}
		assertFalse(array.contains(-1));
		assertFalse(array.contains(0));
		assertFalse(array.contains(1));
		assertFalse(array.contains(Integer.MAX_VALUE));
		assertFalse(array.contains(Integer.MIN_VALUE));
	}

	/**
	 * Tests {@link IntArray#getArray()} and
	 * {@link IntArray#setArray(int[])}.
	 */
	@Test
	public void testSetArray() {
		final IntArray array = new IntArray();
		final int[] raw = { 1, 2, 3, 5, 8, 13, 21 };
		array.setArray(raw);
		assertSame(raw, array.getArray());
	}

	/** Tests {@link IntArray#insert(int, int)}. */
	@Test
	public void testInsert() {
		final int[] raw = { 3, 5, 8, 13, 21 };
		testInsert(new IntArray(raw));
	}

	/** Tests {@link IntArray#delete(int, int)}. */
	@Test
	public void testDelete() {
		final int[] raw = { 3, 5, 8, 13, 21 };
		testDelete(new IntArray(raw));
	}

	/** Tests {@link IntArray#get(int)}. */
	@Test
	public void testGet() {
		final int[] raw = { 3, 5, 8, 13, 21 };
		final IntArray array = new IntArray(raw.clone());
		for (int i = 0; i < raw.length; i++) {
			assertEquals("@" + i, raw[i], array.get(i).intValue());
		}
	}

	/** Tests {@link IntArray#set(int, Integer)}. */
	@Test
	public void testSet() {
		final int[] raw = { 3, 5, 8, 13, 21 };
		final IntArray array = new IntArray(raw.clone());
		final Integer e0 = 7, e2 = 1, e4 = 2;
		array.set(0, e0);
		array.set(2, e2);
		array.set(4, e4);
		assertEquals(raw.length, array.size());
		assertEquals(e0, array.get(0));
		assertEquals(raw[1], array.getValue(1));
		assertEquals(e2, array.get(2));
		assertEquals(raw[3], array.getValue(3));
		assertEquals(e4, array.get(4));
	}

	/** Tests {@link IntArray#add(int, Integer)}. */
	@Test
	public void testAdd() {
		final int[] raw = { 3, 5, 8, 13, 21 };
		final IntArray array = new IntArray(raw.clone());
		final Integer e6 = 1, e7 = 2;
		array.add(e6);
		array.add(e7);
		assertEquals(raw.length + 2, array.size());
		for (int i = 0; i < raw.length; i++) {
			assertEquals("@" + i, raw[i], array.getValue(i));
		}
		assertEquals(e6, array.get(5));
		assertEquals(e7, array.get(6));
	}

	/** Tests {@link IntArray#indexOf(Object)}. */
	@Test
	public void testIndexOfBoxed() {
		final int[] raw = { 3, 5, 8, 13, 21 };
		final IntArray array = new IntArray(raw.clone());
		for (int i = 0; i < raw.length; i++) {
			assertEquals("@" + i, i, array.indexOf(new Integer(raw[i])));
		}
		assertEquals(-1, array.indexOf(new Integer(-1)));
		assertEquals(-1, array.indexOf(new Integer(0)));
		assertEquals(-1, array.indexOf(new Integer(1)));
		assertEquals(-1, array.indexOf(new Integer(Integer.MAX_VALUE)));
		assertEquals(-1, array.indexOf(new Integer(Integer.MIN_VALUE)));
		assertEquals(-1, array.indexOf(null));
		assertEquals(-1, array.indexOf("Not an int"));
	}

	/** Tests {@link IntArray#lastIndexOf(Object)}. */
	@Test
	public void testLastIndexOfBoxed() {
		final int[] raw = { 3, 5, 8, 13, 21 };
		final IntArray array = new IntArray(raw.clone());
		for (int i = 0; i < raw.length; i++) {
			assertEquals("@" + i, i, array.lastIndexOf(new Integer(raw[i])));
		}
		assertEquals(-1, array.lastIndexOf(new Integer(-1)));
		assertEquals(-1, array.lastIndexOf(new Integer(0)));
		assertEquals(-1, array.lastIndexOf(new Integer(1)));
		assertEquals(-1, array.lastIndexOf(new Integer(Integer.MAX_VALUE)));
		assertEquals(-1, array.lastIndexOf(new Integer(Integer.MIN_VALUE)));
		assertEquals(-1, array.lastIndexOf(null));
		assertEquals(-1, array.lastIndexOf("Not an int"));
	}

	/** Tests {@link IntArray#contains(Object)}. */
	@Test
	public void testContainsBoxed() {
		final int[] raw = { 3, 5, 8, 13, 21 };
		final IntArray array = new IntArray(raw.clone());
		for (int i = 0; i < raw.length; i++) {
			assertTrue("@" + i, array.contains(new Integer(raw[i])));
		}
		assertFalse(array.contains(new Integer(-1)));
		assertFalse(array.contains(new Integer(0)));
		assertFalse(array.contains(new Integer(1)));
		assertFalse(array.contains(new Integer(Integer.MAX_VALUE)));
		assertFalse(array.contains(new Integer(Integer.MIN_VALUE)));
		assertFalse(array.contains(null));
		assertFalse(array.contains("Not an int"));
	}

	/** Tests {@link IntArray#remove(Object)}. */
	@Test
	public void testRemove() {
		final int[] raw = { 3, 5, 8, 13, 21 };
		final IntArray array = new IntArray(raw.clone());
		assertEquals(raw.length, array.size());
		array.remove(new Integer(raw[0]));
		assertEquals(raw.length - 1, array.size());
		array.remove(new Integer(raw[2]));
		assertEquals(raw.length - 2, array.size());
		array.remove(new Integer(raw[4]));
		assertEquals(raw.length - 3, array.size());
		assertEquals(raw[1], array.getValue(0));
		assertEquals(raw[3], array.getValue(1));
	}

	/** Tests {@link IntArray#containsAll}. */
	@Test
	public void testContainsAll() {
		final int[] raw = { 3, 5, 8, 13, 21 };
		final IntArray array = new IntArray(raw.clone());

		final ArrayList<Integer> list = new ArrayList<Integer>();
		assertTrue(array.containsAll(list));
		list.add(13);
		assertTrue(array.containsAll(list));
		list.add(1);
		assertFalse(array.containsAll(list));

		final IntArray yes = new IntArray(new int[] { 3, 8, 21 });
		assertTrue(array.containsAll(yes));

		final IntArray no = new IntArray(new int[] { 5, 13, 1 });
		assertFalse(array.containsAll(no));
	}

	/** Tests {@link IntArray#addAll(int, java.util.Collection)}. */
	@Test
	public void testAddAll() {
		final int[] raw = { 3, 5, 8, 13, 21 };
		final IntArray array = new IntArray(raw.clone());
		final int[] add = { 1, 7 };
		final IntArray toAdd = new IntArray(add.clone());
		final int index = 3;
		array.addAll(index, toAdd);
		for (int i = 0; i < index; i++) {
			assertEquals(raw[i], array.getValue(i));
		}
		for (int i = index; i < index + add.length; i++) {
			assertEquals(add[i - index], array.getValue(i));
		}
		for (int i = index + add.length; i < raw.length + add.length; i++) {
			assertEquals(raw[i - add.length], array.getValue(i));
		}
	}

	/** Tests {@link IntArray#removeAll}. */
	@Test
	public void testRemoveAll() {
		final int[] raw = { 3, 5, 8, 13, 21 };
		final IntArray array = new IntArray(raw.clone());
		final IntArray toRemove = new IntArray(new int[] { 3, 8, 21 });
		assertEquals(raw.length, array.size());
		array.removeAll(toRemove);
		assertEquals(raw.length - 3, array.size());
		assertEquals(raw[1], array.getValue(0));
		assertEquals(raw[3], array.getValue(1));
	}

}
