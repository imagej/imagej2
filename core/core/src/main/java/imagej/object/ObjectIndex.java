//
// ObjectIndex.java
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Data structure for managing lists of registered objects.
 *
 * @author Curtis Rueden
 */
public final class ObjectIndex {

	/**
	 * "Sleeping on a dragon's hoard with greedy, dragonish
	 * thoughts in his heart, he had become a dragon himself."
	 * &mdash;C.S. Lewis
	 */
	private Map<Class<?>, List<Object>> hoard =
		new ConcurrentHashMap<Class<?>, List<Object>>();

	// -- ObjectService methods --

	/** Gets a list of all registered objects compatible with the given type. */
	public List<Object> getObjects(final Class<?> type) {
		final List<Object> list = getList(type);
		return Collections.unmodifiableList(list);
	}

	/** Registers an object, using its own class to categorize it. */
	public void addObject(final Object obj) {
		addObject(obj, obj.getClass());
	}

	/** Deregisters an object, using its own class to categorize it. */
	public void removeObject(final Object obj) {
		removeObject(obj, obj.getClass());
	}

	/** Registers an object, beneath the given type hierarchy. */
	public void addObject(final Object obj, final Class<?> c) {
		if (c == null) return;

		final List<Object> list = getList(c);
		if (!list.contains(obj)) list.add(obj);

		// recursively add to supertypes
		addObject(obj, c.getSuperclass());
		for (final Class<?> iface : c.getInterfaces()) addObject(obj, iface);
	}

	/** Deregisters an object, beneath the given type hierarchy. */
	public void removeObject(final Object obj, final Class<?> c) {
		if (c == null) return;

		getList(c).remove(obj);

		// recursively remove from supertypes
		removeObject(obj, c.getSuperclass());
		for (final Class<?> iface : c.getInterfaces()) removeObject(obj, iface);
	}

	// -- Helper methods --

	private List<Object> getList(final Class<?> type) {
		List<Object> list = hoard.get(type);
		if (list == null) {
			list = new ArrayList<Object>();
			hoard.put(type, list);
		}
		return list;
	}

}
