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

package imagej.event;

import imagej.log.LogService;
import imagej.service.Service;
import imagej.thread.ThreadService;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.List;

import org.bushe.swing.event.CleanupEvent;
import org.bushe.swing.event.ThreadSafeEventService;

/**
 * An {@link org.bushe.swing.event.EventService} implementation for ImageJ.
 * <p>
 * It is called "DefaultEventBus" rather than "DefaultEventService" to avoid a
 * name clash with {@link DefaultEventService}, which is not an
 * {@link org.bushe.swing.event.EventService} but rather an ImageJ
 * {@link Service} implementation.
 * </p>
 * 
 * @author Curtis Rueden
 */
public class DefaultEventBus extends ThreadSafeEventService {

	private final ThreadService threadService;
	private final LogService log;

	// TODO - Think more about how publishing events should work.
	// Unfortunately, without further care elsewhere in the code (subject to
	// further investigation), event publication can result in a race condition
	// where JHotDraw partially repaints images before they are done being
	// processed.
	// See ticket #719: http://trac.imagej.net/ticket/719

	public DefaultEventBus(final ThreadService threadService,
		final LogService log)
	{
		super(200L, false, null, null, null);
		this.threadService = threadService;
		this.log = log;
	}

	// -- DefaultEventBus methods --

	public void publishNow(final Object event) {
		if (event == null) {
			throw new IllegalArgumentException("Cannot publish null event.");
		}
		publishNow(event, null, null, getSubscribers(event.getClass()),
			getVetoSubscribers(event.getClass()), null);
	}

	public void publishNow(final Type genericType, final Object event) {
		if (genericType == null) {
			throw new IllegalArgumentException("genericType must not be null.");
		}
		if (event == null) {
			throw new IllegalArgumentException("Cannot publish null event.");
		}
		publishNow(event, null, null, getSubscribers(genericType), null, null);
	}

	public void publishNow(final String topicName, final Object eventObj) {
		publishNow(null, topicName, eventObj, getSubscribers(topicName),
			getVetoEventListeners(topicName), null);
	}

	public void publishLater(final Object event) {
		if (event == null) {
			throw new IllegalArgumentException("Cannot publish null event.");
		}
		publishLater(event, null, null, getSubscribers(event.getClass()),
			getVetoSubscribers(event.getClass()), null);
	}

	public void publishLater(final Type genericType, final Object event) {
		if (genericType == null) {
			throw new IllegalArgumentException("genericType must not be null.");
		}
		if (event == null) {
			throw new IllegalArgumentException("Cannot publish null event.");
		}
		publishLater(event, null, null, getSubscribers(genericType), null, null);
	}

	public void publishLater(final String topicName, final Object eventObj) {
		publishLater(null, topicName, eventObj, getSubscribers(topicName),
			getVetoEventListeners(topicName), null);
	}

	// -- org.bushe.swing.event.EventService methods --

	@Override
	public void publish(final Object event) {
		// HACK: Work around a deadlock problem caused by ThreadSafeEventService:

		// 1) The ThreadSafeEventService superclass has a special cleanup thread
		// that takes care of cleaning up stale references. Every time it runs, it
		// publishes some CleanupEvents using publish(Object) to announce that this
		// is occurring. Normally, such publication delegates to
		// publishNow, which calls ThreadService#invoke, which calls
		// EventQueue.invokeAndWait, which queues the publication for execution on
		// the EDT and then blocks until publication is complete.

		// 2) When the ThreadSafeEventService publishes the CleanupEvents, it does
		// so inside a synchronized block that locks on a "listenerLock" object.

		// 3) Unfortunately, since the CleanupEvent publication is merely *queued*,
		// any other pending operations on the EDT happen first. If one such
		// operation meanwhile calls e.g.
		// ThreadSafeEventService#getSubscribers(Class<T>), it will deadlock because
		// those getter methods are also synchronized on the listenerLock object.

		// Hence, our hack workaround is to instead use publishLater for the
		// CleanupEvents, since no one really cares about them anyway. ;-)

		if (event instanceof CleanupEvent) {
			publishLater(event);
			return;
		}

		publishNow(event);
	}

	@Override
	public void publish(final Type genericType, final Object event) {
		publishNow(genericType, event);
	}

	@Override
	public void publish(final String topicName, final Object eventObj) {
		publishNow(topicName, eventObj);
	}

	// -- Internal methods --

	@Override
	protected void publish(final Object event, final String topic,
		final Object eventObj,
		@SuppressWarnings("rawtypes") final List subscribers,
		@SuppressWarnings("rawtypes") final List vetoSubscribers,
		final StackTraceElement[] callingStack)
	{
		publishNow(event, topic, eventObj, subscribers, vetoSubscribers,
			callingStack);
	}

	// -- Helper methods --

	private void publishNow(final Object event, final String topic,
		final Object eventObj,
		@SuppressWarnings("rawtypes") final List subscribers,
		@SuppressWarnings("rawtypes") final List vetoSubscribers,
		final StackTraceElement[] callingStack)
	{
		try {
			threadService.invoke(new Runnable() {

				@Override
				public void run() {
					log.debug("publish(" + event + "," + topic + "," + eventObj +
						"), called from non-EDT Thread:" + Arrays.toString(callingStack));
					DefaultEventBus.super.publish(event, topic, eventObj, subscribers,
						vetoSubscribers, callingStack);
				}
			});
		}
		catch (final InterruptedException exc) {
			log.error(exc);
		}
		catch (final InvocationTargetException exc) {
			log.error(exc);
		}
	}

	private void publishLater(final Object event, final String topic,
		final Object eventObj,
		@SuppressWarnings("rawtypes") final List subscribers,
		@SuppressWarnings("rawtypes") final List vetoSubscribers,
		final StackTraceElement[] callingStack)
	{
		threadService.queue(new Runnable() {

			@Override
			public void run() {
				log.debug("publish(" + event + "," + topic + "," + eventObj +
					"), called from non-EDT Thread:" + Arrays.toString(callingStack));
				DefaultEventBus.super.publish(event, topic, eventObj, subscribers,
					vetoSubscribers, callingStack);
			}
		});
	}

}
