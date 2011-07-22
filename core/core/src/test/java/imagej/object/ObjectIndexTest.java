//
// ObjectIndexTest.java
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

package imagej.object;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
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
		assertTrue(all.size() == 3);
		assertTrue(all.get(0) == o1);
		assertTrue(all.get(1) == o2);
		assertTrue(all.get(2) == o3);
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
		final List<Object> all = objectIndex.get(Integer.class);
		assertTrue(all.size() == 2);
		assertTrue(all.get(0) == o1);
		assertTrue(all.get(1) == o3);
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
		final Object[] objects = {
			new Integer(5),
			new Float(2.5f),
			new Integer(3)
		};
		for (final Object o : objects)
			objectIndex.add(o);
		final Iterator<Object> iter = objectIndex.iterator();
		int i = 0;
		while (iter.hasNext()) {
			final Object o = iter.next();
			assertTrue(o == objects[i]);
			i++;
		}
	}

	@Test
	public void testToArray() {
		final ObjectIndex<Object> objectIndex =
			new ObjectIndex<Object>(Object.class);
		final Object[] objects = {
			new Integer(5),
			new Float(2.5f),
			new Integer(3)
		};
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
		assertEquals(result, objects);
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
		assertTrue(objectIndex.size() == 3);
		objects.remove(o2);
		objectIndex.removeAll(objects);
		assertTrue(objectIndex.size() == 1);
		assertTrue(objectIndex.getAll().get(0) == o2);
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

}
