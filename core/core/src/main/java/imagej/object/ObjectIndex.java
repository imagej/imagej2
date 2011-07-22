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
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Data structure for managing lists of registered objects.
 * 
 * @author Curtis Rueden
 */
public class ObjectIndex<E> implements Collection<E> {

	/**
	 * "Sleeping on a dragon's hoard with greedy, dragonish thoughts in his heart,
	 * he had become a dragon himself." &mdash;C.S. Lewis
	 */
	protected final Map<Class<?>, List<E>> hoard =
		new ConcurrentHashMap<Class<?>, List<E>>();

	private final Class<E> baseClass;

	public ObjectIndex(final Class<E> baseClass) {
		this.baseClass = baseClass;
	}

	// -- ObjectIndex methods --

	/** Gets the base class of the items being managed. */
	public Class<E> getBaseClass() {
		return baseClass;
	}

	/**
	 * Gets a list of all registered objects.
	 * 
	 * @return Read-only list of all registered objects, or an empty list if none
	 *         (this method never returns null).
	 */
	public List<E> getAll() {
		return get(Object.class);
	}

	/**
	 * Gets a list of registered objects compatible with the given type.
	 * 
	 * @return Read-only list of registered objects of the given type, or an empty
	 *         list if no such objects exist (this method never returns null).
	 */
	public List<E> get(final Class<?> type) {
		final List<E> list = getList(type);
		return Collections.unmodifiableList(list);
	}

	// -- Collection methods --

	@Override
	public int size() {
		return getAll().size();
	}

	@Override
	public boolean isEmpty() {
		return getAll().isEmpty();
	}

	@Override
	public boolean contains(final Object o) {
		return getAll().contains(o);
	}

	@Override
	public Iterator<E> iterator() {
		return getAll().iterator();
	}

	@Override
	public Object[] toArray() {
		return getAll().toArray();
	}

	@Override
	public <T> T[] toArray(final T[] a) {
		return getAll().toArray(a);
	}

	@Override
	public boolean add(final E o) {
		return add(o, false);
	}

	@Override
	public boolean remove(final Object o) {
		return remove(o, false);
	}

	@Override
	public boolean containsAll(final Collection<?> c) {
		return getAll().containsAll(c);
	}

	@Override
	public boolean addAll(final Collection<? extends E> c) {
		boolean changed = false;
		for (final E o : c) {
			final boolean result = add(o, true);
			if (result) changed = true;
		}
		return changed;
	}

	@Override
	public boolean removeAll(final Collection<?> c) {
		boolean changed = false;
		for (final Object o : c) {
			final boolean result = remove(o, true);
			if (result) changed = true;
		}
		return changed;
	}

	@Override
	public boolean retainAll(final Collection<?> c) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void clear() {
		hoard.clear();
	}

	// -- Internal methods --

	protected boolean add(final E o, final boolean batch) {
		return add(o, o.getClass(), batch);
	}

	protected boolean remove(final Object o, final boolean batch) {
		return remove(o, o.getClass(), batch);
	}

	/** Adds an object to type lists beneath the given type hierarchy. */
	protected boolean add(final E o, final Class<?> type, final boolean batch) {
		final boolean result = register(o, type, batch);
		if (extendsObject(o.getClass())) register(o, Object.class, batch);
		return result;
	}

	/** Removes an object from type lists beneath the given type hierarchy. */
	protected boolean remove(final Object o, final Class<?> type,
		final boolean batch)
	{
		final boolean result = deregister(o, type, batch);
		if (!extendsObject(o.getClass())) deregister(o, Object.class, batch);
		return result;
	}

	protected boolean addToList(final E obj, final List<E> list,
		@SuppressWarnings("unused") final boolean batch)
	{
		if (list.contains(obj)) return false; // object already on the list
		list.add(obj);
		return true;
	}

	protected boolean removeFromList(final Object obj, final List<E> list,
		@SuppressWarnings("unused") final boolean batch)
	{
		return list.remove(obj);
	}

	// -- Helper methods --

	/** Recursively adds the given object to type lists. */
	private boolean
		register(final E o, final Class<?> type, final boolean batch)
	{
		if (type == null) return false; // invalid class

		final boolean result = addToList(o, getList(type), batch);

		// recursively add to supertypes
		register(o, type.getSuperclass(), batch);
		for (final Class<?> iface : type.getInterfaces()) {
			register(o, iface, batch);
		}

		return result;
	}

	/** Recursively removes the given object from type lists. */
	private boolean deregister(final Object obj, final Class<?> type,
		final boolean batch)
	{
		if (type == null) return false;

		final boolean result = removeFromList(obj, getList(type), batch);

		// recursively remove from supertypes
		deregister(obj, type.getSuperclass(), batch);
		for (final Class<?> iface : type.getInterfaces()) {
			deregister(obj, iface, batch);
		}

		return result;
	}

	private List<E> getList(final Class<?> type) {
		List<E> list = hoard.get(type);
		if (list == null) {
			list = new ArrayList<E>();
			hoard.put(type, list);
		}
		return list;
	}

	private boolean extendsObject(final Class<?> c) {
		if (c == null) return false;
		if (c == Object.class) return true;
		return extendsObject(c.getSuperclass());
	}

}
