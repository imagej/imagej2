//
// EventService.java
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

package imagej.event;

import imagej.AbstractService;
import imagej.ImageJ;
import imagej.Service;
import imagej.util.Log;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.bushe.swing.event.SwingEventService;

/**
 * Service for publishing and subscribing to ImageJ events.
 * 
 * @author Curtis Rueden
 * @author Grant Harris
 */
@Service
public final class EventService extends AbstractService {

	private org.bushe.swing.event.EventService eventBus;

	// -- Constructors --

	public EventService() {
		// NB: Required by SezPoz.
		super(null);
		throw new UnsupportedOperationException();
	}

	public EventService(final ImageJ context) {
		super(context);
	}

	// -- EventService methods --

	public <E extends ImageJEvent> void publish(final E e) {
		e.setContext(getContext());
		eventBus.publish(e);
	}

	public <E extends ImageJEvent> void subscribe(
		final EventSubscriber<E> subscriber)
	{
		final Class<E> c = getEventClass(subscriber);
		subscribe(c, subscriber);
	}

	public <E extends ImageJEvent> void subscribeStrongly(
		final EventSubscriber<E> subscriber)
	{
		final Class<E> c = getEventClass(subscriber);
		subscribeStrongly(c, subscriber);
	}

	public <E extends ImageJEvent> void unsubscribe(
		final EventSubscriber<E> subscriber)
	{
		final Class<E> c = getEventClass(subscriber);
		unsubscribe(c, subscriber);
	}

	public <E extends ImageJEvent> void subscribe(final Class<E> c,
		final EventSubscriber<E> subscriber)
	{
		eventBus.subscribe(c, subscriber);
	}

	public <E extends ImageJEvent> void subscribeStrongly(final Class<E> c,
		final EventSubscriber<E> subscriber)
	{
		eventBus.subscribeStrongly(c, subscriber);
	}

	public <E extends ImageJEvent> void unsubscribe(final Class<E> c,
		final EventSubscriber<E> subscriber)
	{
		eventBus.unsubscribe(c, subscriber);
	}

	public void subscribe(final List<EventSubscriber<?>> subscribers) {
		for (final EventSubscriber<?> subscriber : subscribers) {
			subscribe(subscriber);
		}
	}

	public void subscribeStrongly(final List<EventSubscriber<?>> subscribers) {
		for (final EventSubscriber<?> subscriber : subscribers) {
			subscribeStrongly(subscriber);
		}
	}

	public void unsubscribe(final List<EventSubscriber<?>> subscribers) {
		for (final EventSubscriber<?> subscriber : subscribers) {
			unsubscribe(subscriber);
		}
	}

	/**
	 * Subscribes all of the given object's @{@link EventHandler} annotated
	 * methods. This allows a single class to subscribe to multiple types of
	 * events by implementing multiple event handling methods and annotating each
	 * one with @{@link EventHandler}.
	 * 
	 * @return The list of newly created {@link EventSubscriber}s, weakly
	 *         subscribed to the event service. These objects must not be allowed
	 *         to fall out of scope or they will be garbage collected (in which
	 *         case events will not be delivered to them!).
	 */
	public List<EventSubscriber<?>> subscribe(final Object o) {
		final List<EventSubscriber<?>> subscribers =
			new ArrayList<EventSubscriber<?>>();

		for (final Method m : o.getClass().getDeclaredMethods()) {
			final EventHandler ann = m.getAnnotation(EventHandler.class);
			if (ann == null) continue; // not an event handler method

			final Class<? extends ImageJEvent> eventClass = getEventClass(m);
			if (eventClass == null) {
				Log.warn("Invalid EventHandler method: " + m);
				continue;
			}

			subscribers.add(subscribe(eventClass, o, m));
		}

		return subscribers;
	}

	public <E extends ImageJEvent> List<EventSubscriber<E>> getSubscribers(
		final Class<E> c)
	{
		// HACK - It appears that EventBus API is incorrect, in that
		// EventBus#getSubscribers(Class<T>) returns a List<T> when it should
		// actually be a List<EventSubscriber<T>>. This method works around the
		// problem with casts.
		@SuppressWarnings("rawtypes")
		final List list = eventBus.getSubscribers(c);
		@SuppressWarnings("unchecked")
		final List<EventSubscriber<E>> typedList = list;
		return typedList;
	}

	// -- IService methods --

	@Override
	public void initialize() {
		// TODO - Use ThreadSafeEventService instead of SwingEventService.
		// Unfortunately, without further care elsewhere in the code (subject to
		// further investigation), using it results in a race condition where
		// JHotDraw partially repaints images before they are done being processed.
		// See ticket #719: http://dev.imagejdev.org/trac/imagej/ticket/719
		eventBus = new SwingEventService();
	}

	// -- Helper methods --

	private <E extends ImageJEvent> EventSubscriber<E> subscribe(
		final Class<E> eventClass, final Object o, final Method m)
	{
		final EventMethodSubscriber<E> subscriber =
			new EventMethodSubscriber<E>(o, m);
		subscribe(eventClass, subscriber);
		return subscriber;
	}

	/**
	 * Gets the event class for the given {@link EventSubscriber}.
	 * <p>
	 * This method works by scanning for an appropriate <code>onEvent</code>
	 * method via reflection, and extracting the {@link Class} of the argument.
	 * </p>
	 */
	private <E extends ImageJEvent> Class<E> getEventClass(
		final EventSubscriber<E> subscriber)
	{
		for (final Method m : subscriber.getClass().getDeclaredMethods()) {
			if (!m.getName().equals("onEvent")) continue;
			final Class<? extends ImageJEvent> eventClass = getEventClass(m);
			@SuppressWarnings("unchecked")
			final Class<E> typedClass = (Class<E>) eventClass;
			return typedClass;
		}
		return null;
	}

	/** Gets the event class parameter of the given method. */
	private Class<? extends ImageJEvent> getEventClass(final Method m) {
		final Class<?>[] c = m.getParameterTypes();
		if (c == null || c.length != 1) return null; // wrong number of args
		if (!ImageJEvent.class.isAssignableFrom(c[0])) return null; // wrong class

		@SuppressWarnings("unchecked")
		final Class<? extends ImageJEvent> eventClass =
			(Class<? extends ImageJEvent>) c[0];
		return eventClass;
	}

	// -- Helper classes --

	/** Helper class used by {@link #subscribe(Object)}. */
	private class EventMethodSubscriber<E extends ImageJEvent> implements
		EventSubscriber<E>
	{

		private final Object o;
		private final Method m;

		public EventMethodSubscriber(final Object o, final Method m) {
			this.o = o;
			this.m = m;
			// allow calling of non-public methods
			m.setAccessible(true);
		}

		@Override
		public void onEvent(final E event) {
			try {
				m.invoke(o, event);
			}
			catch (final IllegalArgumentException e) {
				Log.error("Event handler threw exception", e);
			}
			catch (final IllegalAccessException e) {
				Log.error("Event handler threw exception", e);
			}
			catch (final InvocationTargetException e) {
				Log.error("Event handler threw exception", e);
			}
		}
	}

}
