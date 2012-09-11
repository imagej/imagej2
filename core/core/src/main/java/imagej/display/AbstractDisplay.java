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

package imagej.display;

import imagej.display.event.DisplayDeletedEvent;
import imagej.display.event.DisplayUpdatedEvent;
import imagej.display.event.DisplayUpdatedEvent.DisplayUpdateLevel;
import imagej.event.EventService;
import imagej.plugin.SortablePlugin;

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
public abstract class AbstractDisplay<T> extends SortablePlugin implements
	Display<T>
{

	/** The type of object the display can visualize. */
	private final Class<T> type;

	/** List of objects being displayed. */
	private final ArrayList<T> objects;

	/** Flag set when display needs to be fully rebuilt. */
	private boolean structureChanged;

	/** The name of the display. */
	private String name;

	protected boolean isClosed = false;

	public AbstractDisplay(final Class<T> type) {
		this.type = type;
		objects = new ArrayList<T>();
	}

	// -- AbstractDisplay methods --

	protected void rebuild() {
		structureChanged = true;
	}

	// -- Object methods --

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder(super.toString());
		sb.append(": type=" + type);
		sb.append(", name=" + name);
		sb.append(", objects={");
		boolean first = true;
		for (final T object : objects) {
			if (first) first = false;
			else sb.append(", ");
			sb.append(object);
		}
		sb.append("}");
		return sb.toString();
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
		final T typedObj = (T) o;
		add(typedObj);
	}

	@Override
	public boolean isDisplaying(final Object o) {
		return contains(o);
	}

	@Override
	public void update() {
		final EventService eventService =
			getContext().getService(EventService.class);
		if (eventService != null && !isClosed) {
			eventService.publish(new DisplayUpdatedEvent(this, structureChanged
				? DisplayUpdateLevel.REBUILD : DisplayUpdateLevel.UPDATE));
		}
		structureChanged = false;
	}

	@Override
	public void close() {
		if (isClosed) return;
		final DisplayService displayService =
			getContext().getService(DisplayService.class);
		if (displayService != null && displayService.getActiveDisplay() == this) {
			displayService.setActiveDisplay(null);
		}

		final EventService eventService =
			getContext().getService(EventService.class);
		if (eventService != null) {
			eventService.publish(new DisplayDeletedEvent(this));
		}
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
	public void add(final int index, final T element) {
		objects.add(index, element);
		announceStructureChange();
	}

	@Override
	public boolean addAll(final int index, final Collection<? extends T> c) {
		final boolean changed = objects.addAll(index, c);
		if (changed) announceStructureChange();
		return changed;
	}

	@Override
	public T get(final int index) {
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
	public ListIterator<T> listIterator() {
		return objects.listIterator();
	}

	@Override
	public ListIterator<T> listIterator(final int index) {
		return objects.listIterator(index);
	}

	@Override
	public T remove(final int index) {
		final T result = objects.remove(index);
		if (result != null) announceStructureChange();
		return result;
	}

	@Override
	public T set(final int index, final T element) {
		final T result = objects.set(index, element);
		if (result != null) announceStructureChange();
		return result;
	}

	@Override
	public List<T> subList(final int fromIndex, final int toIndex) {
		return objects.subList(fromIndex, toIndex);
	}

	// -- Collection methods --

	@Override
	public boolean add(final T o) {
		checkObject(o);
		final boolean changed = objects.add(o);
		if (changed) announceStructureChange();
		return changed;
	}

	@Override
	public boolean addAll(final Collection<? extends T> c) {
		for (final T o : c) {
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
	public Iterator<T> iterator() {
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
	public <U> U[] toArray(final U[] a) {
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
