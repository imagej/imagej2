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

package imagej.event;

import imagej.ImageJ;
import imagej.service.AbstractService;
import imagej.service.Service;
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
 * Default service for publishing and subscribing to ImageJ events.
 * 
 * @author Curtis Rueden
 * @author Grant Harris
 */
@Service
public class DefaultEventService extends AbstractService
	implements EventService
{

	protected org.bushe.swing.event.EventService eventBus;

	// -- Constructors --

	public DefaultEventService() {
		// NB: Required by SezPoz.
		super(null);
		throw new UnsupportedOperationException();
	}

	public DefaultEventService(final ImageJ context) {
		super(context);

		// TODO - Use ThreadSafeEventService instead of SwingEventService.
		// Unfortunately, without further care elsewhere in the code (subject to
		// further investigation), using it results in a race condition where
		// JHotDraw partially repaints images before they are done being processed.
		// See ticket #719: http://trac.imagej.net/ticket/719
		eventBus = new SwingEventService();
	}

	// -- EventService methods --

	@Override
	public <E extends ImageJEvent> void publish(final E e) {
		e.setContext(getContext());
		eventBus.publish(e);
	}

	@Override
	public List<EventSubscriber<?>> subscribe(final Object o) {
		final List<EventSubscriber<?>> subscribers =
			new ArrayList<EventSubscriber<?>>();
		subscribeRecursively(subscribers, o.getClass(), o);
		return subscribers;
	}

	@Override
	public void unsubscribe(final Collection<EventSubscriber<?>> subscribers) {
		for (final EventSubscriber<?> subscriber : subscribers) {
			unsubscribe(subscriber);
		}
	}

	@Override
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

	// -- Helper methods --

	/**
	 * Recursively scans for @{@link EventHandler} annotated methods, and
	 * subscribes them to the event service.
	 */
	private void subscribeRecursively(
		final List<EventSubscriber<?>> subscribers, final Class<?> type,
		final Object o)
	{
		if (type == null || type == Object.class) return;
		for (final Method m : type.getDeclaredMethods()) {
			final EventHandler ann = m.getAnnotation(EventHandler.class);
			if (ann == null) continue; // not an event handler method

			final Class<? extends ImageJEvent> eventClass = getEventClass(m);
			if (eventClass == null) {
				Log.warn("Invalid EventHandler method: " + m);
				continue;
			}

			subscribers.add(subscribe(eventClass, o, m));
		}
		subscribeRecursively(subscribers, type.getSuperclass(), o);
	}

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
	 * Helper class used by {@link #subscribe(Object)}.
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
				if (obj == null) return; // has been garbage collected
				getSubscriptionMethod().invoke(obj, event);
			}
			catch (final IllegalAccessException exc) {
				Log.error("Exception during event handling:\n\t[Event] " +
					event.getClass().getName() + ":" + event + "\n\t[Subscriber] " +
					getProxiedSubscriber() + "\n\t[Method] " + getSubscriptionMethod(),
					exc);
			}
			catch (final InvocationTargetException exc) {
				Log.error("Exception during event handling:\n\t[Event] " +
					event.getClass().getName() + event + "\n\t[Subscriber] " +
					getProxiedSubscriber() + "\n\t[Method] " + getSubscriptionMethod(),
					exc.getCause());
			}
		}

		@Override
		public Class<E> getEventClass() {
			return c;
		}

	}

}
