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
 * Tests {@link DoubleArray}.
 * 
 * @author Curtis Rueden
 * @author Johannes Schindelin
 */
public class DoubleArrayTest extends PrimitiveArrayTest {

	/** Tests {@link DoubleArray#DoubleArray()}. */
	@Test
	public void testConstructorNoArgs() {
		final DoubleArray array = new DoubleArray();
		assertEquals(0, array.size());
		assertEquals(0, array.copyArray().length);
	}

	/** Tests {@link DoubleArray#DoubleArray(int)}. */
	@Test
	public void testConstructorSize() {
		final int size = 24;
		final DoubleArray array = new DoubleArray(size);
		assertEquals(size, array.size());
		assertEquals(size, array.copyArray().length);
	}

	/** Tests {@link DoubleArray#DoubleArray(double[])}. */
	@Test
	public void testConstructorArray() {
		final double[] raw = { 3, 5, 8, 13, 21 };
		final DoubleArray array = new DoubleArray(raw);
		assertSame(raw, array.getArray());
		assertEquals(raw.length, array.size());
		for (int i = 0; i < raw.length; i++) {
			assertEquals("@" + i, raw[i], array.getValue(i), 0);
		}
		assertArrayEquals(raw, array.copyArray(), 0);
	}

	/** Tests {@link DoubleArray#addValue(double)}. */
	@Test
	public void testAddValue() {
		final double[] raw = { 3, 5, 8, 13, 21 };
		final DoubleArray array = new DoubleArray(raw.clone());
		final double e6 = 1.1f, e7 = 2.2f;
		array.addValue(e6);
		array.addValue(e7);
		assertEquals(raw.length + 2, array.size());
		for (int i = 0; i < raw.length; i++) {
			assertEquals("@" + i, raw[i], array.getValue(i), 0);
		}
		assertEquals(e6, array.getValue(5), 0);
		assertEquals(e7, array.getValue(6), 0);
	}

	/** Tests {@link DoubleArray#removeValue(double)}. */
	public void testRemoveValue() {
		final double[] raw = { 3, 5, 8, 13, 21 };
		final DoubleArray array = new DoubleArray(raw.clone());
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

	/** Tests {@link DoubleArray#getValue(int)}. */
	public void testGetValue() {
		final double[] raw = { 3, 5, 8, 13, 21 };
		final DoubleArray array = new DoubleArray(raw.clone());
		for (int i = 0; i < raw.length; i++) {
			assertEquals("@" + i, raw[i], array.getValue(i), 0);
		}
	}

	/** Tests {@link DoubleArray#setValue(int, double)}. */
	@Test
	public void testSetValue() {
		final double[] raw = { 3, 5, 8, 13, 21 };
		final DoubleArray array = new DoubleArray(raw.clone());
		final double e0 = 7.7f, e2 = 1.1f, e4 = 2.2f;
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

	/** Tests {@link DoubleArray#addValue(int, double)}. */
	@Test
	public void testAddValueIndex() {
		final double[] raw = { 3, 5, 8, 13, 21 };
		final DoubleArray array = new DoubleArray(raw.clone());
		final double e0 = 7.7f, e4 = 1.1f, e7 = 2.2f;
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

	/** Tests {@link DoubleArray#remove(int)}. */
	public void testRemoveIndex() {
		final double[] raw = { 3, 5, 8, 13, 21 };
		final DoubleArray array = new DoubleArray(raw.clone());
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

	/** Tests {@link DoubleArray#indexOf(double)}. */
	@Test
	public void testIndexOf() {
		final double[] raw = { 3, 5, 8, 13, 21 };
		final DoubleArray array = new DoubleArray(raw.clone());
		for (int i = 0; i < raw.length; i++) {
			assertEquals("@" + i, i, array.indexOf(raw[i]));
		}
		assertEquals(-1, array.indexOf(-1));
		assertEquals(-1, array.indexOf(0));
		assertEquals(-1, array.indexOf(1));
		assertEquals(-1, array.indexOf(Double.NaN));
		assertEquals(-1, array.indexOf(Double.POSITIVE_INFINITY));
		assertEquals(-1, array.indexOf(Double.NEGATIVE_INFINITY));
	}

	/** Tests {@link DoubleArray#lastIndexOf(double)}. */
	@Test
	public void testLastIndexOf() {
		final double[] raw = { 3, 5, 8, 13, 21 };
		final DoubleArray array = new DoubleArray(raw.clone());
		for (int i = 0; i < raw.length; i++) {
			assertEquals("@" + i, i, array.lastIndexOf(raw[i]));
		}
		assertEquals(-1, array.lastIndexOf(-1));
		assertEquals(-1, array.lastIndexOf(0));
		assertEquals(-1, array.lastIndexOf(1));
		assertEquals(-1, array.lastIndexOf(Double.NaN));
		assertEquals(-1, array.lastIndexOf(Double.POSITIVE_INFINITY));
		assertEquals(-1, array.lastIndexOf(Double.NEGATIVE_INFINITY));
	}

	/** Tests {@link DoubleArray#contains(double)}. */
	@Test
	public void testContains() {
		final double[] raw = { 3, 5, 8, 13, 21 };
		final DoubleArray array = new DoubleArray(raw.clone());
		for (int i = 0; i < raw.length; i++) {
			assertTrue("@" + i, array.contains(raw[i]));
		}
		assertFalse(array.contains(-1));
		assertFalse(array.contains(0));
		assertFalse(array.contains(1));
		assertFalse(array.contains(Double.NaN));
		assertFalse(array.contains(Double.POSITIVE_INFINITY));
		assertFalse(array.contains(Double.NEGATIVE_INFINITY));
	}

	/**
	 * Tests {@link DoubleArray#getArray()} and
	 * {@link DoubleArray#setArray(double[])}.
	 */
	@Test
	public void testSetArray() {
		final DoubleArray array = new DoubleArray();
		final double[] raw = { 1, 2, 3, 5, 8, 13, 21 };
		array.setArray(raw);
		assertSame(raw, array.getArray());
	}

	/** Tests {@link DoubleArray#insert(int, int)}. */
	@Test
	public void testInsert() {
		final double[] raw = { 3, 5, 8, 13, 21 };
		testInsert(new DoubleArray(raw));
	}

	/** Tests {@link DoubleArray#delete(int, int)}. */
	@Test
	public void testDelete() {
		final double[] raw = { 3, 5, 8, 13, 21 };
		testDelete(new DoubleArray(raw));
	}

	/** Tests {@link DoubleArray#get(int)}. */
	@Test
	public void testGet() {
		final double[] raw = { 3, 5, 8, 13, 21 };
		final DoubleArray array = new DoubleArray(raw.clone());
		for (int i = 0; i < raw.length; i++) {
			assertEquals("@" + i, raw[i], array.get(i).doubleValue(), 0);
		}
	}

	/** Tests {@link DoubleArray#set(int, Double)}. */
	@Test
	public void testSet() {
		final double[] raw = { 3, 5, 8, 13, 21 };
		final DoubleArray array = new DoubleArray(raw.clone());
		final Double e0 = 7.7, e2 = 1.1, e4 = 2.2;
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

	/** Tests {@link DoubleArray#add(int, Double)}. */
	@Test
	public void testAdd() {
		final double[] raw = { 3, 5, 8, 13, 21 };
		final DoubleArray array = new DoubleArray(raw.clone());
		final Double e6 = 1.1, e7 = 2.2;
		array.add(e6);
		array.add(e7);
		assertEquals(raw.length + 2, array.size());
		for (int i = 0; i < raw.length; i++) {
			assertEquals("@" + i, raw[i], array.getValue(i), 0);
		}
		assertEquals(e6, array.get(5), 0);
		assertEquals(e7, array.get(6), 0);
	}

	/** Tests {@link DoubleArray#indexOf(Object)}. */
	@Test
	public void testIndexOfBoxed() {
		final double[] raw = { 3, 5, 8, 13, 21 };
		final DoubleArray array = new DoubleArray(raw.clone());
		for (int i = 0; i < raw.length; i++) {
			assertEquals("@" + i, i, array.indexOf(new Double(raw[i])));
		}
		assertEquals(-1, array.indexOf(new Double(-1)));
		assertEquals(-1, array.indexOf(new Double(0)));
		assertEquals(-1, array.indexOf(new Double(1)));
		assertEquals(-1, array.indexOf(new Double(Double.NaN)));
		assertEquals(-1, array.indexOf(new Double(Double.POSITIVE_INFINITY)));
		assertEquals(-1, array.indexOf(new Double(Double.NEGATIVE_INFINITY)));
		assertEquals(-1, array.indexOf(null));
		assertEquals(-1, array.indexOf("Not a double"));
	}

	/** Tests {@link DoubleArray#lastIndexOf(Object)}. */
	@Test
	public void testLastIndexOfBoxed() {
		final double[] raw = { 3, 5, 8, 13, 21 };
		final DoubleArray array = new DoubleArray(raw.clone());
		for (int i = 0; i < raw.length; i++) {
			assertEquals("@" + i, i, array.lastIndexOf(new Double(raw[i])));
		}
		assertEquals(-1, array.lastIndexOf(new Double(-1)));
		assertEquals(-1, array.lastIndexOf(new Double(0)));
		assertEquals(-1, array.lastIndexOf(new Double(1)));
		assertEquals(-1, array.lastIndexOf(new Double(Double.NaN)));
		assertEquals(-1, array.lastIndexOf(new Double(Double.POSITIVE_INFINITY)));
		assertEquals(-1, array.lastIndexOf(new Double(Double.NEGATIVE_INFINITY)));
		assertEquals(-1, array.lastIndexOf(null));
		assertEquals(-1, array.lastIndexOf("Not a double"));
	}

	/** Tests {@link DoubleArray#contains(Object)}. */
	@Test
	public void testContainsBoxed() {
		final double[] raw = { 3, 5, 8, 13, 21 };
		final DoubleArray array = new DoubleArray(raw.clone());
		for (int i = 0; i < raw.length; i++) {
			assertTrue("@" + i, array.contains(new Double(raw[i])));
		}
		assertFalse(array.contains(new Double(-1)));
		assertFalse(array.contains(new Double(0)));
		assertFalse(array.contains(new Double(1)));
		assertFalse(array.contains(new Double(Double.NaN)));
		assertFalse(array.contains(new Double(Double.POSITIVE_INFINITY)));
		assertFalse(array.contains(new Double(Double.NEGATIVE_INFINITY)));
		assertFalse(array.contains(null));
		assertFalse(array.contains("Not a double"));
	}

	/** Tests {@link DoubleArray#remove(Object)}. */
	@Test
	public void testRemove() {
		final double[] raw = { 3, 5, 8, 13, 21 };
		final DoubleArray array = new DoubleArray(raw.clone());
		assertEquals(raw.length, array.size());
		array.remove(new Double(raw[0]));
		assertEquals(raw.length - 1, array.size());
		array.remove(new Double(raw[2]));
		assertEquals(raw.length - 2, array.size());
		array.remove(new Double(raw[4]));
		assertEquals(raw.length - 3, array.size());
		assertEquals(raw[1], array.getValue(0), 0);
		assertEquals(raw[3], array.getValue(1), 0);
	}

	/** Tests {@link DoubleArray#containsAll}. */
	@Test
	public void testContainsAll() {
		final double[] raw = { 3, 5, 8, 13, 21 };
		final DoubleArray array = new DoubleArray(raw.clone());

		final ArrayList<Double> list = new ArrayList<Double>();
		assertTrue(array.containsAll(list));
		list.add(13d);
		assertTrue(array.containsAll(list));
		list.add(1d);
		assertFalse(array.containsAll(list));

		final DoubleArray yes = new DoubleArray(new double[] { 3, 8, 21 });
		assertTrue(array.containsAll(yes));

		final DoubleArray no = new DoubleArray(new double[] { 5, 13, 1 });
		assertFalse(array.containsAll(no));
	}

	/** Tests {@link DoubleArray#addAll(int, java.util.Collection)}. */
	@Test
	public void testAddAll() {
		final double[] raw = { 3, 5, 8, 13, 21 };
		final DoubleArray array = new DoubleArray(raw.clone());
		final double[] add = { 1.1f, 7.7f };
		final DoubleArray toAdd = new DoubleArray(add.clone());
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

	/** Tests {@link DoubleArray#removeAll}. */
	@Test
	public void testRemoveAll() {
		final double[] raw = { 3, 5, 8, 13, 21 };
		final DoubleArray array = new DoubleArray(raw.clone());
		final DoubleArray toRemove = new DoubleArray(new double[] { 3, 8, 21 });
		assertEquals(raw.length, array.size());
		array.removeAll(toRemove);
		assertEquals(raw.length - 3, array.size());
		assertEquals(raw[1], array.getValue(0), 0);
		assertEquals(raw[3], array.getValue(1), 0);
	}

}
