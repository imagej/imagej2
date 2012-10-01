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

package imagej.object;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.junit.Test;

/**
 * Tests {@link ObjectIndex}.
 * 
 * @author Curtis Rueden
 */
public class ObjectIndexTest {

	@Test
	public void testGetAll() {
		final ObjectIndex<Object> objectIndex =
			new ObjectIndex<Object>(Object.class);
		final Object o1 = new Integer(5);
		final Object o2 = new Float(2.5f);
		final Object o3 = new Integer(3);
		objectIndex.add(o1);
		objectIndex.add(o2);
		objectIndex.add(o3);
		final List<Object> all = objectIndex.getAll();
		assertEquals(3, all.size());
		assertSame(o1, all.get(0));
		assertSame(o2, all.get(1));
		assertSame(o3, all.get(2));
	}

	@Test
	public void testGet() {
		final ObjectIndex<Object> objectIndex =
			new ObjectIndex<Object>(Object.class);
		final Object o1 = new Integer(5);
		final Object o2 = new Float(2.5f);
		final Object o3 = new Integer(3);
		objectIndex.add(o1);
		objectIndex.add(o2);
		objectIndex.add(o3);
		final List<Object> integers = objectIndex.get(Integer.class);
		assertEquals(2, integers.size());
		assertSame(o1, integers.get(0));
		assertSame(o3, integers.get(1));
		final List<Object> numbers = objectIndex.get(Number.class);
		assertEquals(numbers.size(), 3);
		assertSame(o1, numbers.get(0));
		assertSame(o2, numbers.get(1));
		assertSame(o3, numbers.get(2));
	}

	@Test
	public void testIsEmpty() {
		final ObjectIndex<Object> objectIndex =
			new ObjectIndex<Object>(Object.class);
		assertTrue(objectIndex.isEmpty());
		final Object o1 = new Integer(5);
		objectIndex.add(o1);
		assertFalse(objectIndex.isEmpty());
		objectIndex.remove(o1);
		assertTrue(objectIndex.isEmpty());
	}

	@Test
	public void testContains() {
		final ObjectIndex<Object> objectIndex =
			new ObjectIndex<Object>(Object.class);
		final Object o1 = new Integer(5);
		assertFalse(objectIndex.contains(o1));
		objectIndex.add(o1);
		assertTrue(objectIndex.contains(o1));
		objectIndex.remove(o1);
		assertFalse(objectIndex.contains(o1));
	}

	@Test
	public void testIterator() {
		final ObjectIndex<Object> objectIndex =
			new ObjectIndex<Object>(Object.class);
		final Object[] objects =
			{ new Integer(5), new Float(2.5f), new Integer(3) };
		for (final Object o : objects)
			objectIndex.add(o);
		final Iterator<Object> iter = objectIndex.iterator();
		int i = 0;
		while (iter.hasNext()) {
			final Object o = iter.next();
			assertSame(objects[i], o);
			i++;
		}
	}

	@Test
	public void testToArray() {
		final ObjectIndex<Object> objectIndex =
			new ObjectIndex<Object>(Object.class);
		final Object[] objects =
			{ new Integer(5), new Float(2.5f), new Integer(3) };
		for (final Object o : objects)
			objectIndex.add(o);
		final Object[] result = objectIndex.toArray();
		assertArrayEquals(objects, result);
	}

	@Test
	public void testContainsAll() {
		final ObjectIndex<Object> objectIndex =
			new ObjectIndex<Object>(Object.class);
		assertTrue(objectIndex.containsAll(new ArrayList<Object>()));
		final Object o1 = new Integer(5);
		final Object o2 = new Float(2.5f);
		final Object o3 = new Integer(3);
		final ArrayList<Object> objects = new ArrayList<Object>();
		objects.add(o1);
		objects.add(o2);
		objects.add(o3);
		objectIndex.addAll(objects);
		objects.remove(o3);
		assertTrue(objectIndex.containsAll(objects));
		objectIndex.remove(o1);
		assertFalse(objectIndex.containsAll(objects));
	}

	@Test
	public void testAddAll() {
		final ObjectIndex<Object> objectIndex =
			new ObjectIndex<Object>(Object.class);
		final ArrayList<Object> objects = new ArrayList<Object>();
		objects.add(new Integer(5));
		objects.add(new Float(2.5f));
		objects.add(new Integer(3));
		objectIndex.addAll(objects);
		final List<Object> result = objectIndex.getAll();
		assertEquals(objects, result);
	}

	@Test
	public void testRemoveAll() {
		final ObjectIndex<Object> objectIndex =
			new ObjectIndex<Object>(Object.class);
		final Object o1 = new Integer(5);
		final Object o2 = new Float(2.5f);
		final Object o3 = new Integer(3);
		final ArrayList<Object> objects = new ArrayList<Object>();
		objects.add(o1);
		objects.add(o2);
		objects.add(o3);
		objectIndex.addAll(objects);
		assertEquals(3, objectIndex.size());
		objects.remove(o2);
		objectIndex.removeAll(objects);
		assertEquals(1, objectIndex.size());
		assertSame(o2, objectIndex.getAll().get(0));
	}

	@Test
	public void testClear() {
		final ObjectIndex<Object> objectIndex =
			new ObjectIndex<Object>(Object.class);
		objectIndex.clear();
		assertTrue(objectIndex.isEmpty());
		objectIndex.add(new Integer(5));
		assertFalse(objectIndex.isEmpty());
		objectIndex.clear();
		assertTrue(objectIndex.isEmpty());
	}

	@Test
	public void testToString() {
		final ObjectIndex<Object> objectIndex =
			new ObjectIndex<Object>(Object.class);
		objectIndex.add(new Integer(5));
		objectIndex.add(new Float(2.5f));
		objectIndex.add(new Integer(3));
		final String[] expected =
			{ "imagej.object.ObjectIndex$All: {5, 2.5, 3}",
				"java.io.Serializable: {5, 2.5, 3}",
				"java.lang.Comparable: {5, 2.5, 3}", "java.lang.Float: {2.5}",
				"java.lang.Integer: {5, 3}", "java.lang.Number: {5, 2.5, 3}",
				"java.lang.Object: {5, 2.5, 3}" };
		final String[] actual =
			objectIndex.toString().split(System.getProperty("line.separator"));
		assertArrayEquals(expected, actual);
	}

}
