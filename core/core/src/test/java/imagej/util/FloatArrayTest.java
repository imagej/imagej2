/*
 * #%L
 * ImageJ software for multidimensional image processing and analysis.
 * %%
 * Copyright (C) 2009 - 2013 Board of Regents of the University of
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
 * Tests {@link FloatArray}.
 * 
 * @author Curtis Rueden
 * @author Johannes Schindelin
 */
public class FloatArrayTest extends PrimitiveArrayTest {

	/** Tests {@link FloatArray#FloatArray()}. */
	@Test
	public void testConstructorNoArgs() {
		final FloatArray array = new FloatArray();
		assertEquals(0, array.size());
		assertEquals(0, array.copyArray().length);
	}

	/** Tests {@link FloatArray#FloatArray(int)}. */
	@Test
	public void testConstructorSize() {
		final int size = 24;
		final FloatArray array = new FloatArray(size);
		assertEquals(size, array.size());
		assertEquals(size, array.copyArray().length);
	}

	/** Tests {@link FloatArray#FloatArray(float[])}. */
	@Test
	public void testConstructorArray() {
		final float[] raw = { 3, 5, 8, 13, 21 };
		final FloatArray array = new FloatArray(raw);
		assertSame(raw, array.getArray());
		assertEquals(raw.length, array.size());
		for (int i = 0; i < raw.length; i++) {
			assertEquals("@" + i, raw[i], array.getValue(i), 0);
		}
		assertArrayEquals(raw, array.copyArray(), 0);
	}

	/** Tests {@link FloatArray#addValue(float)}. */
	@Test
	public void testAddValue() {
		final float[] raw = { 3, 5, 8, 13, 21 };
		final FloatArray array = new FloatArray(raw.clone());
		final float e6 = 1.1f, e7 = 2.2f;
		array.addValue(e6);
		array.addValue(e7);
		assertEquals(raw.length + 2, array.size());
		for (int i = 0; i < raw.length; i++) {
			assertEquals("@" + i, raw[i], array.getValue(i), 0);
		}
		assertEquals(e6, array.getValue(5), 0);
		assertEquals(e7, array.getValue(6), 0);
	}

	/** Tests {@link FloatArray#removeValue(float)}. */
	public void testRemoveValue() {
		final float[] raw = { 3, 5, 8, 13, 21 };
		final FloatArray array = new FloatArray(raw.clone());
		assertEquals(raw.length, array.size());
		array.removeValue(raw[0]);
		assertEquals(raw.length - 1, array.size());
		array.removeValue(raw[2]);
		assertEquals(raw.length - 2, array.size());
		array.removeValue(raw[4]);
		assertEquals(raw.length - 3, array.size());
		assertEquals(raw[1], array.getValue(0), 0);
		assertEquals(raw[3], array.getValue(1), 0);
	}

	/** Tests {@link FloatArray#getValue(int)}. */
	public void testGetValue() {
		final float[] raw = { 3, 5, 8, 13, 21 };
		final FloatArray array = new FloatArray(raw.clone());
		for (int i = 0; i < raw.length; i++) {
			assertEquals("@" + i, raw[i], array.getValue(i), 0);
		}
	}

	/** Tests {@link FloatArray#setValue(int, float)}. */
	@Test
	public void testSetValue() {
		final float[] raw = { 3, 5, 8, 13, 21 };
		final FloatArray array = new FloatArray(raw.clone());
		final float e0 = 7.7f, e2 = 1.1f, e4 = 2.2f;
		array.setValue(0, e0);
		array.setValue(2, e2);
		array.setValue(4, e4);
		assertEquals(raw.length, array.size());
		assertEquals(e0, array.getValue(0), 0);
		assertEquals(raw[1], array.getValue(1), 0);
		assertEquals(e2, array.getValue(2), 0);
		assertEquals(raw[3], array.getValue(3), 0);
		assertEquals(e4, array.getValue(4), 0);
	}

	/** Tests {@link FloatArray#addValue(int, float)}. */
	@Test
	public void testAddValueIndex() {
		final float[] raw = { 3, 5, 8, 13, 21 };
		final FloatArray array = new FloatArray(raw.clone());
		final float e0 = 7.7f, e4 = 1.1f, e7 = 2.2f;
		array.addValue(0, e0);
		array.addValue(4, e4);
		array.addValue(7, e7);
		assertEquals(raw.length + 3, array.size());
		assertEquals(e0, array.getValue(0), 0);
		assertEquals(raw[0], array.getValue(1), 0);
		assertEquals(raw[1], array.getValue(2), 0);
		assertEquals(raw[2], array.getValue(3), 0);
		assertEquals(e4, array.getValue(4), 0);
		assertEquals(raw[3], array.getValue(5), 0);
		assertEquals(raw[4], array.getValue(6), 0);
		assertEquals(e7, array.getValue(7), 0);
	}

	/** Tests {@link FloatArray#remove(int)}. */
	public void testRemoveIndex() {
		final float[] raw = { 3, 5, 8, 13, 21 };
		final FloatArray array = new FloatArray(raw.clone());
		assertEquals(raw.length, array.size());
		array.remove(0);
		assertEquals(raw.length - 1, array.size());
		array.remove(2);
		assertEquals(raw.length - 2, array.size());
		array.remove(4);
		assertEquals(raw.length - 3, array.size());
		assertEquals(raw[1], array.getValue(0), 0);
		assertEquals(raw[3], array.getValue(1), 0);
	}

	/** Tests {@link FloatArray#indexOf(float)}. */
	@Test
	public void testIndexOf() {
		final float[] raw = { 3, 5, 8, 13, 21 };
		final FloatArray array = new FloatArray(raw.clone());
		for (int i = 0; i < raw.length; i++) {
			assertEquals("@" + i, i, array.indexOf(raw[i]));
		}
		assertEquals(-1, array.indexOf(-1));
		assertEquals(-1, array.indexOf(0));
		assertEquals(-1, array.indexOf(1));
		assertEquals(-1, array.indexOf(Float.NaN));
		assertEquals(-1, array.indexOf(Float.POSITIVE_INFINITY));
		assertEquals(-1, array.indexOf(Float.NEGATIVE_INFINITY));
	}

	/** Tests {@link FloatArray#lastIndexOf(float)}. */
	@Test
	public void testLastIndexOf() {
		final float[] raw = { 3, 5, 8, 13, 21 };
		final FloatArray array = new FloatArray(raw.clone());
		for (int i = 0; i < raw.length; i++) {
			assertEquals("@" + i, i, array.lastIndexOf(raw[i]));
		}
		assertEquals(-1, array.lastIndexOf(-1));
		assertEquals(-1, array.lastIndexOf(0));
		assertEquals(-1, array.lastIndexOf(1));
		assertEquals(-1, array.lastIndexOf(Float.NaN));
		assertEquals(-1, array.lastIndexOf(Float.POSITIVE_INFINITY));
		assertEquals(-1, array.lastIndexOf(Float.NEGATIVE_INFINITY));
	}

	/** Tests {@link FloatArray#contains(float)}. */
	@Test
	public void testContains() {
		final float[] raw = { 3, 5, 8, 13, 21 };
		final FloatArray array = new FloatArray(raw.clone());
		for (int i = 0; i < raw.length; i++) {
			assertTrue("@" + i, array.contains(raw[i]));
		}
		assertFalse(array.contains(-1));
		assertFalse(array.contains(0));
		assertFalse(array.contains(1));
		assertFalse(array.contains(Float.NaN));
		assertFalse(array.contains(Float.POSITIVE_INFINITY));
		assertFalse(array.contains(Float.NEGATIVE_INFINITY));
	}

	/**
	 * Tests {@link FloatArray#getArray()} and
	 * {@link FloatArray#setArray(float[])}.
	 */
	@Test
	public void testSetArray() {
		final FloatArray array = new FloatArray();
		final float[] raw = { 1, 2, 3, 5, 8, 13, 21 };
		array.setArray(raw);
		assertSame(raw, array.getArray());
	}

	/** Tests {@link FloatArray#insert(int, int)}. */
	@Test
	public void testInsert() {
		final float[] raw = { 3, 5, 8, 13, 21 };
		testInsert(new FloatArray(raw));
	}

	/** Tests {@link FloatArray#delete(int, int)}. */
	@Test
	public void testDelete() {
		final float[] raw = { 3, 5, 8, 13, 21 };
		testDelete(new FloatArray(raw));
	}

	/** Tests {@link FloatArray#get(int)}. */
	@Test
	public void testGet() {
		final float[] raw = { 3, 5, 8, 13, 21 };
		final FloatArray array = new FloatArray(raw.clone());
		for (int i = 0; i < raw.length; i++) {
			assertEquals("@" + i, raw[i], array.get(i).floatValue(), 0);
		}
	}

	/** Tests {@link FloatArray#set(int, Float)}. */
	@Test
	public void testSet() {
		final float[] raw = { 3, 5, 8, 13, 21 };
		final FloatArray array = new FloatArray(raw.clone());
		final Float e0 = 7.7f, e2 = 1.1f, e4 = 2.2f;
		array.set(0, e0);
		array.set(2, e2);
		array.set(4, e4);
		assertEquals(raw.length, array.size());
		assertEquals(e0, array.get(0), 0);
		assertEquals(raw[1], array.getValue(1), 0);
		assertEquals(e2, array.get(2), 0);
		assertEquals(raw[3], array.getValue(3), 0);
		assertEquals(e4, array.get(4), 0);
	}

	/** Tests {@link FloatArray#add(int, Float)}. */
	@Test
	public void testAdd() {
		final float[] raw = { 3, 5, 8, 13, 21 };
		final FloatArray array = new FloatArray(raw.clone());
		final Float e6 = 1.1f, e7 = 2.2f;
		array.add(e6);
		array.add(e7);
		assertEquals(raw.length + 2, array.size());
		for (int i = 0; i < raw.length; i++) {
			assertEquals("@" + i, raw[i], array.getValue(i), 0);
		}
		assertEquals(e6, array.get(5), 0);
		assertEquals(e7, array.get(6), 0);
	}

	/** Tests {@link FloatArray#indexOf(Object)}. */
	@Test
	public void testIndexOfBoxed() {
		final float[] raw = { 3, 5, 8, 13, 21 };
		final FloatArray array = new FloatArray(raw.clone());
		for (int i = 0; i < raw.length; i++) {
			assertEquals("@" + i, i, array.indexOf(new Float(raw[i])));
		}
		assertEquals(-1, array.indexOf(new Float(-1)));
		assertEquals(-1, array.indexOf(new Float(0)));
		assertEquals(-1, array.indexOf(new Float(1)));
		assertEquals(-1, array.indexOf(new Float(Float.NaN)));
		assertEquals(-1, array.indexOf(new Float(Float.POSITIVE_INFINITY)));
		assertEquals(-1, array.indexOf(new Float(Float.NEGATIVE_INFINITY)));
		assertEquals(-1, array.indexOf(null));
		assertEquals(-1, array.indexOf("Not a float"));
	}

	/** Tests {@link FloatArray#lastIndexOf(Object)}. */
	@Test
	public void testLastIndexOfBoxed() {
		final float[] raw = { 3, 5, 8, 13, 21 };
		final FloatArray array = new FloatArray(raw.clone());
		for (int i = 0; i < raw.length; i++) {
			assertEquals("@" + i, i, array.lastIndexOf(new Float(raw[i])));
		}
		assertEquals(-1, array.lastIndexOf(new Float(-1)));
		assertEquals(-1, array.lastIndexOf(new Float(0)));
		assertEquals(-1, array.lastIndexOf(new Float(1)));
		assertEquals(-1, array.lastIndexOf(new Float(Float.NaN)));
		assertEquals(-1, array.lastIndexOf(new Float(Float.POSITIVE_INFINITY)));
		assertEquals(-1, array.lastIndexOf(new Float(Float.NEGATIVE_INFINITY)));
		assertEquals(-1, array.lastIndexOf(null));
		assertEquals(-1, array.lastIndexOf("Not a float"));
	}

	/** Tests {@link FloatArray#contains(Object)}. */
	@Test
	public void testContainsBoxed() {
		final float[] raw = { 3, 5, 8, 13, 21 };
		final FloatArray array = new FloatArray(raw.clone());
		for (int i = 0; i < raw.length; i++) {
			assertTrue("@" + i, array.contains(new Float(raw[i])));
		}
		assertFalse(array.contains(new Float(-1)));
		assertFalse(array.contains(new Float(0)));
		assertFalse(array.contains(new Float(1)));
		assertFalse(array.contains(new Float(Float.NaN)));
		assertFalse(array.contains(new Float(Float.POSITIVE_INFINITY)));
		assertFalse(array.contains(new Float(Float.NEGATIVE_INFINITY)));
		assertFalse(array.contains(null));
		assertFalse(array.contains("Not a float"));
	}

	/** Tests {@link FloatArray#remove(Object)}. */
	@Test
	public void testRemove() {
		final float[] raw = { 3, 5, 8, 13, 21 };
		final FloatArray array = new FloatArray(raw.clone());
		assertEquals(raw.length, array.size());
		array.remove(new Float(raw[0]));
		assertEquals(raw.length - 1, array.size());
		array.remove(new Float(raw[2]));
		assertEquals(raw.length - 2, array.size());
		array.remove(new Float(raw[4]));
		assertEquals(raw.length - 3, array.size());
		assertEquals(raw[1], array.getValue(0), 0);
		assertEquals(raw[3], array.getValue(1), 0);
	}

	/** Tests {@link FloatArray#containsAll}. */
	@Test
	public void testContainsAll() {
		final float[] raw = { 3, 5, 8, 13, 21 };
		final FloatArray array = new FloatArray(raw.clone());

		final ArrayList<Float> list = new ArrayList<Float>();
		assertTrue(array.containsAll(list));
		list.add(13f);
		assertTrue(array.containsAll(list));
		list.add(1f);
		assertFalse(array.containsAll(list));

		final FloatArray yes = new FloatArray(new float[] { 3, 8, 21 });
		assertTrue(array.containsAll(yes));

		final FloatArray no = new FloatArray(new float[] { 5, 13, 1 });
		assertFalse(array.containsAll(no));
	}

	/** Tests {@link FloatArray#addAll(int, java.util.Collection)}. */
	@Test
	public void testAddAll() {
		final float[] raw = { 3, 5, 8, 13, 21 };
		final FloatArray array = new FloatArray(raw.clone());
		final float[] add = { 1.1f, 7.7f };
		final FloatArray toAdd = new FloatArray(add.clone());
		final int index = 3;
		array.addAll(index, toAdd);
		for (int i = 0; i < index; i++) {
			assertEquals(raw[i], array.getValue(i), 0);
		}
		for (int i = index; i < index + add.length; i++) {
			assertEquals(add[i - index], array.getValue(i), 0);
		}
		for (int i = index + add.length; i < raw.length + add.length; i++) {
			assertEquals(raw[i - add.length], array.getValue(i), 0);
		}
	}

	/** Tests {@link FloatArray#removeAll}. */
	@Test
	public void testRemoveAll() {
		final float[] raw = { 3, 5, 8, 13, 21 };
		final FloatArray array = new FloatArray(raw.clone());
		final FloatArray toRemove = new FloatArray(new float[] { 3, 8, 21 });
		assertEquals(raw.length, array.size());
		array.removeAll(toRemove);
		assertEquals(raw.length - 3, array.size());
		assertEquals(raw[1], array.getValue(0), 0);
		assertEquals(raw[3], array.getValue(1), 0);
	}

}
