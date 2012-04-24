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

package imagej.ext.display;

import imagej.ImageJ;
import imagej.event.EventService;
import imagej.ext.display.event.DisplayActivatedEvent;
import imagej.ext.display.event.DisplayDeletedEvent;
import imagej.ext.display.event.DisplayUpdatedEvent;
import imagej.ext.display.event.DisplayUpdatedEvent.DisplayUpdateLevel;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

/**
 * Abstract superclass of {@link Display} implementations.
 * 
 * @author Curtis Rueden
 */
public abstract class AbstractDisplay<E> implements Display<E> {

	/** The type of object the display can visualize. */
	private final Class<E> type;

	/** List of objects being displayed. */
	private final ArrayList<E> objects;

	/** Flag set when display needs to be fully rebuilt. */
	private boolean structureChanged;

	/** The name of the display. */
	private String name;

	protected EventService eventService;

	protected ImageJ context;
	
	protected boolean isClosed = false;

	public AbstractDisplay(final Class<E> type) {
		this.type = type;
		objects = new ArrayList<E>();
	}
	
	@Override
	public void setContext(final ImageJ context) {
		assert this.context == null;
		this.context = context;
		eventService = context.getService(EventService.class);
	}

	@Override
	public ImageJ getContext() {
		return context;
	}

	// -- AbstractDisplay methods --

	protected void rebuild() {
		structureChanged = true;
	}

	// -- Display methods --

	@Override
	public boolean canDisplay(final Class<?> c) {
		return type.isAssignableFrom(c);
	}

	@Override
	public boolean canDisplay(final Object o) {
		if (o == null) return false;
		return canDisplay(o.getClass());
	}

	@Override
	public void display(final Object o) {
		checkObject(o);
		@SuppressWarnings("unchecked")
		final E typedObj = (E) o;
		add(typedObj);
	}

	@Override
	public void update() {
		if (eventService != null && ! isClosed) {
			eventService.publish(new DisplayUpdatedEvent(this,
					structureChanged? DisplayUpdateLevel.REBUILD : DisplayUpdateLevel.UPDATE));
		}
		structureChanged = false;
	}

	@Override
	public void activate() {
		if (eventService != null && ! isClosed)
			eventService.publish(new DisplayActivatedEvent(this));
	}

	@Override
	public void close() {
		if (eventService != null && ! isClosed)
			eventService.publish(new DisplayDeletedEvent(this));
		isClosed = true;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public void setName(final String name) {
		this.name = name;
	}

	// -- List methods --

	@Override
	public void add(final int index, final E element) {
		objects.add(index, element);
		announceStructureChange();
	}

	@Override
	public boolean addAll(final int index, final Collection<? extends E> c) {
		final boolean changed = objects.addAll(index, c);
		if (changed) announceStructureChange();
		return changed;
	}

	@Override
	public E get(final int index) {
		return objects.get(index);
	}

	@Override
	public int indexOf(final Object o) {
		return objects.indexOf(o);
	}

	@Override
	public int lastIndexOf(final Object o) {
		return objects.lastIndexOf(o);
	}

	@Override
	public ListIterator<E> listIterator() {
		return objects.listIterator();
	}

	@Override
	public ListIterator<E> listIterator(final int index) {
		return objects.listIterator(index);
	}

	@Override
	public E remove(final int index) {
		final E result = objects.remove(index);
		if (result != null) announceStructureChange();
		return result;
	}

	@Override
	public E set(final int index, final E element) {
		final E result = objects.set(index, element);
		if (result != null) announceStructureChange();
		return result;
	}

	@Override
	public List<E> subList(final int fromIndex, final int toIndex) {
		return objects.subList(fromIndex, toIndex);
	}

	// -- Collection methods --

	@Override
	public boolean add(final E o) {
		checkObject(o);
		final boolean changed = objects.add(o);
		if (changed) announceStructureChange();
		return changed;
	}

	@Override
	public boolean addAll(final Collection<? extends E> c) {
		for (final E o : c) {
			checkObject(o);
		}
		final boolean changed = objects.addAll(c);
		if (changed) announceStructureChange();
		return changed;
	}

	@Override
	public void clear() {
		final boolean changed = objects.size() > 0;
		objects.clear();
		if (changed) announceStructureChange();
	}

	@Override
	public boolean contains(final Object o) {
		return objects.contains(o);
	}

	@Override
	public boolean containsAll(final Collection<?> c) {
		return objects.containsAll(c);
	}

	@Override
	public boolean isEmpty() {
		return objects.isEmpty();
	}

	@Override
	public Iterator<E> iterator() {
		return objects.iterator();
	}

	@Override
	public boolean remove(final Object o) {
		final boolean changed = objects.remove(o);
		if (changed) announceStructureChange();
		return changed;
	}

	@Override
	public boolean removeAll(final Collection<?> c) {
		final boolean changed = objects.removeAll(c);
		if (changed) announceStructureChange();
		return changed;
	}

	@Override
	public boolean retainAll(final Collection<?> c) {
		final boolean changed = objects.retainAll(c);
		if (changed) announceStructureChange();
		return changed;
	}

	@Override
	public int size() {
		return objects.size();
	}

	@Override
	public Object[] toArray() {
		return objects.toArray();
	}

	@Override
	public <T> T[] toArray(final T[] a) {
		return objects.toArray(a);
	}

	// -- Internal methods --

	protected void checkObject(final Object o) {
		if (!canDisplay(o.getClass())) {
			final String typeName = o.getClass().getName();
			throw new IllegalArgumentException("Unsupported type: " + typeName);
		}
		if (!canDisplay(o)) {
			throw new IllegalArgumentException("Unsupported object: " + o);
		}
	}

	protected void announceStructureChange() {
		structureChanged = true;
	}

}
