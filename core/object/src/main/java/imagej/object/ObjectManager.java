//
// ObjectManager.java
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

import imagej.Manager;
import imagej.ManagerComponent;
import imagej.event.EventSubscriber;
import imagej.event.Events;
import imagej.object.event.ObjectCreatedEvent;
import imagej.object.event.ObjectDeletedEvent;
import imagej.object.event.ObjectsUpdatedEvent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manager component for keeping track of registered objects.
 * Automatically registers new objects from {@link ObjectCreatedEvent}s,
 * and removes objects from {@link ObjectDeletedEvent}s.
 * <p>
 * This is useful to retrieve available objects of a particular type,
 * such as the list of datasets upon which a user can choose to operate.
 * </p>
 *
 * @author Curtis Rueden
 */
@Manager(priority = Manager.FIRST_PRIORITY)
public final class ObjectManager implements ManagerComponent {

	/**
	 * "Sleeping on a dragon's hoard with greedy, dragonish
	 * thoughts in his heart, he had become a dragon himself."
	 * &mdash;C.S. Lewis
	 */
	private Map<Class<?>, List<?>> hoard;

	/** Maintains the list of event subscribers, to avoid garbage collection. */
	private List<EventSubscriber<?>> subscribers;

	// -- ObjectManager methods --

	/** Gets a list of all registered objects compatible with the given type. */
	public <T> List<T> getObjects(final Class<T> type) {
		final List<T> list = getList(type);
		return Collections.unmodifiableList(list);
	}

	/** Registers an object with the object manager. */
	public void addObject(final Object obj) {
		addObject(obj, obj.getClass());
		Events.publish(new ObjectsUpdatedEvent(obj));
	}

	/** Deregisters an object with the object manager. */
	public void removeObject(final Object obj) {
		removeObject(obj, obj.getClass());
		Events.publish(new ObjectsUpdatedEvent(obj));
	}

	// -- ManagerComponent methods --

	@Override
	public void initialize() {
		hoard = new ConcurrentHashMap<Class<?>, List<?>>();
		subscribeToEvents();
	}

	// -- Helper methods --

	private <T> void addObject(final Object obj, final Class<T> c) {
		if (c == null) return;

		final List<T> list = getList(c);
		@SuppressWarnings("unchecked")
		final T typedObj = (T) obj;
		if (!list.contains(obj)) list.add(typedObj);

		// recursively add to supertypes
		addObject(obj, c.getSuperclass());
		for (final Class<?> iface : c.getInterfaces()) addObject(obj, iface);
	}

	private <T> void removeObject(final Object obj, final Class<T> c) {
		if (c == null) return;

		getList(c).remove(obj);

		// recursively remove from supertypes
		removeObject(obj, c.getSuperclass());
		for (final Class<?> iface : c.getInterfaces()) removeObject(obj, iface);
	}

	private <T> List<T> getList(final Class<T> type) {
		@SuppressWarnings("unchecked")
		List<T> list = (List<T>) hoard.get(type);
		if (list == null) {
			list = new ArrayList<T>();
			hoard.put(type, list);
		}
		return list;
	}

	private void subscribeToEvents() {
		subscribers = new ArrayList<EventSubscriber<?>>();

		final EventSubscriber<ObjectCreatedEvent> objectCreatedSubscriber =
			new EventSubscriber<ObjectCreatedEvent>()
		{
			@Override
			public void onEvent(final ObjectCreatedEvent event) {
				addObject(event.getObject());
			}
		};
		subscribers.add(objectCreatedSubscriber);
		Events.subscribe(ObjectCreatedEvent.class, objectCreatedSubscriber);

		final EventSubscriber<ObjectDeletedEvent> objectDeletedSubscriber =
			new EventSubscriber<ObjectDeletedEvent>()
		{
			@Override
			public void onEvent(final ObjectDeletedEvent event) {
				removeObject(event.getObject());
			}
		};
		subscribers.add(objectDeletedSubscriber);
		Events.subscribe(ObjectDeletedEvent.class, objectDeletedSubscriber);
	}

}
