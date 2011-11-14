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
import java.util.Collection;
import java.util.List;

import org.bushe.swing.event.SwingEventService;
import org.bushe.swing.event.annotation.AbstractProxySubscriber;
import org.bushe.swing.event.annotation.BaseProxySubscriber;
import org.bushe.swing.event.annotation.ReferenceStrength;

/**
 * Service for publishing and subscribing to ImageJ events.
 * 
 * @author Curtis Rueden
 * @author Grant Harris
 */
@Service
public final class EventService extends AbstractService {

	protected org.bushe.swing.event.EventService eventBus;

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
		subscribe(subscriber.getEventClass(), subscriber);
	}

	public void unsubscribe(final Collection<EventSubscriber<?>> subscribers) {
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
	public List<EventSubscriber<?>> subscribeAll(final Object o) {
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

	private <E extends ImageJEvent> void subscribe(final Class<E> c,
		final EventSubscriber<E> subscriber)
	{
		eventBus.subscribe(c, subscriber);
	}

	private <E extends ImageJEvent> void unsubscribe(
		final EventSubscriber<E> subscriber)
	{
		unsubscribe(subscriber.getEventClass(), subscriber);
	}

	private <E extends ImageJEvent> void unsubscribe(final Class<E> c,
		final EventSubscriber<E> subscriber)
	{
		eventBus.unsubscribe(c, subscriber);
	}

	private <E extends ImageJEvent> EventSubscriber<E> subscribe(
		final Class<E> c, final Object o, final Method m)
	{
		final ProxySubscriber<E> subscriber = new ProxySubscriber<E>(c, o, m);
		subscribe(c, subscriber);
		return subscriber;
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

	/**
	 * Helper class used by {@link #subscribeAll(Object)}.
	 * <p>
	 * Recapitulates some logic from {@link BaseProxySubscriber}, because that
	 * class implements {@link org.bushe.swing.event.EventSubscriber} as a raw
	 * type, which is incompatible with this class implementing ImageJ's
	 * {@link EventSubscriber} as a typed interface; it becomes impossible to
	 * implement both <code>onEvent(Object)</code> and <code>onEvent(E)</code>.
	 * </p>
	 */
	private class ProxySubscriber<E extends ImageJEvent> extends
		AbstractProxySubscriber implements EventSubscriber<E>
	{

		private final Class<E> c;

		public ProxySubscriber(final Class<E> c, final Object o, final Method m) {
			super(o, m, ReferenceStrength.WEAK, eventBus, false);
			this.c = c;

			// allow calling of non-public methods
			m.setAccessible(true);
		}

		/**
		 * Handles the event publication by pushing it to the real subscriber's
		 * subscription method.
		 * 
		 * @param event The event to publish.
		 */
		@Override
		public void onEvent(final E event) {
			try {
				final Object obj = getProxiedSubscriber();
				if (obj == null) {
					// has been garbage collected
					return;
				}
				final Method subscriptionMethod = getSubscriptionMethod();
				subscriptionMethod.invoke(obj, event);
			}
			catch (final IllegalAccessException e) {
				Log.error("Exception when invoking annotated method from " +
					"EventService publication.  Event class:" + event.getClass() +
					", Event:" + event + ", subscriber:" + getProxiedSubscriber() +
					", subscription Method=" + getSubscriptionMethod(), e);
			}
			catch (final InvocationTargetException e) {
				Log.error("InvocationTargetException when invoking " +
					"annotated method from EventService publication.  Event class:" +
					event.getClass() + ", Event:" + event + ", subscriber:" +
					getProxiedSubscriber() + ", subscription Method=" +
					getSubscriptionMethod(), e);
			}
		}

		@Override
		public Class<E> getEventClass() {
			return c;
		}

	}

}
