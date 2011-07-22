//
// SortedObjectIndex.java
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

import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Data structure for managing sorted lists of registered objects.
 * 
 * @author Curtis Rueden
 */
public class SortedObjectIndex<E extends Comparable<? super E>> extends
	ObjectIndex<E>
{

	public SortedObjectIndex(final Class<E> baseClass) {
		super(baseClass);
	}

	// -- Collection methods --

	@Override
	public boolean contains(final Object o) {
		final int index = findInList(o, getAll());
		return index < 0;
	}

	@Override
	public boolean containsAll(final Collection<?> c) {
		for (final Object o : c) {
			if (!contains(o)) return false;
		}
		return true;
	}

	@Override
	public boolean addAll(final Collection<? extends E> c) {
		if (c.size() == 1) {
			// add single item normally, to avoid resorting the lists
			return add(c.iterator().next());
		}
		final boolean changed = super.addAll(c);
		if (changed) sort();
		return changed;
	}

	// -- Internal methods --

	@Override
	protected boolean addToList(final E obj, final List<E> list,
		final boolean batch)
	{
		if (batch) {
			// adding multiple values; append to end of list, and sort afterward
			return super.addToList(obj, list, batch);
		}

		// search for the correct location to insert the object
		final int index = Collections.binarySearch(list, obj);
		if (index >= 0) return false; // object already on the list
		// insert object at the appropriate location
		list.add(-index - 1, obj);
		return true;
	}

	// -- Helper methods --

	private void sort() {
		for (final List<E> list : hoard.values()) {
			Collections.sort(list);
		}
	}

	private int findInList(final Object o, final List<E> list) {
		if (!getBaseClass().isAssignableFrom(o.getClass())) {
			// wrong type
			return list.size();
		}
		@SuppressWarnings("unchecked")
		final E typedObj = (E) o;
		return Collections.binarySearch(list, typedObj);
	}

}
