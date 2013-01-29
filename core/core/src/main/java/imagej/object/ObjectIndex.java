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

package imagej.object;

import imagej.util.ClassUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Data structure for managing lists of registered objects.
 * <p>
 * The object index keeps lists of objects segregated by type. The type
 * hierarchy beneath which each object is classified can be customized through
 * subclassing (e.g., see {@link imagej.plugin.PluginIndex}), but by default,
 * each registered object is added to all type lists with which its class is
 * compatible. For example, an object of type {@link String} would be added to
 * the following type lists: {@link String}, {@link java.io.Serializable},
 * {@link Comparable}, {@link CharSequence} and {@link Object}. A subsequent
 * request for all objects of type {@link Comparable} (via a call to
 * {@link #get(Class)}) would return a list that includes the object.
 * </p>
 * <p>
 * Note that similar to {@link List}, it is possible for the same object to be
 * added to the index more than once, in which case it will appear on relevant
 * type lists multiple times.
 * </p>
 * <p>
 * Note that similar to {@link List}, it is possible for the same object to be
 * added to the index more than once, in which case it will appear on compatible
 * type lists multiple times.
 * </p>
 * 
 * @author Curtis Rueden
 */
public class ObjectIndex<E> implements Collection<E> {

	/**
	 * "Them as counts counts moren them as dont count." <br>
	 * &mdash;Russell Hoban, <em>Riddley Walker</em>
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
	 * Gets a list of <em>all</em> registered objects.
	 * 
	 * @return Read-only list of all registered objects, or an empty list if none
	 *         (this method never returns null).
	 */
	public List<E> getAll() {
		// NB: We return the special "All" list here, since in some cases,
		// *no* other list contains *all* elements of the index.

		// We cannot use Object.class, since interface type hierarchies do not
		// extend Object. In particular, PluginIndex classifies objects beneath the
		// ImageJPlugin type hierarchy, which does not extend Object.

		// And we cannot use getBaseClass() because the actual base class of the
		// objects stored in the index may differ from the type hierarchy beneath
		// which they are classified. In particular, PluginIndex classifies its
		// PluginInfo objects beneath the ImageJPlugin type hierarchy, and not that
		// of PluginInfo.
		return get(All.class);
	}

	/**
	 * Gets a list of registered objects compatible with the given type.
	 * 
	 * @return Read-only list of registered objects of the given type, or an empty
	 *         list if no such objects exist (this method never returns null).
	 */
	public List<E> get(final Class<?> type) {
		final List<E> list = retrieveList(type);
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
		return get(o.getClass()).contains(o);
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

	// -- Object methods --

	@Override
	public String toString() {
		final List<Class<?>> classes = new ArrayList<Class<?>>(hoard.keySet());
		Collections.sort(classes, new Comparator<Class<?>>() {

			@Override
			public int compare(final Class<?> c1, final Class<?> c2) {
				return ClassUtils.compare(c1, c2);
			}

		});

		final String nl = System.getProperty("line.separator");
		final StringBuilder sb = new StringBuilder();
		for (final Class<?> c : classes) {
			sb.append(c.getName() + ": {");
			final List<E> list = hoard.get(c);
			boolean first = true;
			for (final E element : list) {
				if (first) first = false;
				else sb.append(", ");
				sb.append(element);
			}
			sb.append("}" + nl);
		}

		return sb.toString();
	}

	// -- Internal methods --

	/** Adds the object to all compatible type lists. */
	protected boolean add(final E o, final boolean batch) {
		return add(o, o.getClass(), batch);
	}

	/** Removes the object from all compatible type lists. */
	protected boolean remove(final Object o, final boolean batch) {
		return remove(o, o.getClass(), batch);
	}

	/** Adds an object to type lists beneath the given type hierarchy. */
	protected boolean add(final E o, final Class<?> type, final boolean batch) {
		boolean result = false;
		final Set<Class<?>> types = getTypes(type);
		for (final Class<?> c : types) {
			if (addToList(o, retrieveList(c), batch)) result = true;
		}
		return result;
	}

	/** Removes an object from type lists beneath the given type hierarchy. */
	protected boolean remove(final Object o, final Class<?> type,
		final boolean batch)
	{
		boolean result = false;
		final Set<Class<?>> types = getTypes(type);
		for (final Class<?> c : types) {
			if (removeFromList(o, retrieveList(c), batch)) result = true;
		}
		return result;
	}

	protected boolean addToList(final E obj, final List<E> list,
		@SuppressWarnings("unused") final boolean batch)
	{
		return list.add(obj);
	}

	protected boolean removeFromList(final Object obj, final List<E> list,
		@SuppressWarnings("unused") final boolean batch)
	{
		return list.remove(obj);
	}

	// -- Helper methods --

	/** Gets a new set containing the type and all its supertypes. */
	private HashSet<Class<?>> getTypes(final Class<?> type) {
		final HashSet<Class<?>> types = new HashSet<Class<?>>();
		types.add(All.class); // NB: Always include the "All" class.
		getTypes(type, types);
		return types;
	}

	/** Recursively adds the type and all its supertypes to the given set. */
	private void getTypes(final Class<?> type, final HashSet<Class<?>> types) {
		if (type == null) return;
		types.add(type);

		// recursively add to supertypes
		getTypes(type.getSuperclass(), types);
		for (final Class<?> iface : type.getInterfaces()) {
			getTypes(iface, types);
		}
	}

	/** Retrieves the type list for the given type, creating it if necessary. */
	private List<E> retrieveList(final Class<?> type) {
		List<E> list = hoard.get(type);
		if (list == null) {
			list = new ArrayList<E>();
			hoard.put(type, list);
		}
		return list;
	}

	private static class All {
		// NB: A special class beneath which *all* elements of the index are listed.
	}

}
