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

import imagej.service.Service;

import java.util.Collection;
import java.util.List;

/**
 * Interface for the event handling service.
 * 
 * @author Curtis Rueden
 * @author Grant Harris
 */
public interface EventService extends Service {

	/**
	 * Publishes the given event immediately, reporting it to all subscribers.
	 * Does not return until all subscribers have handled the event.
	 * <p>
	 * Note that with {@link #publish}, in the case of multiple events published
	 * in a chain to multiple subscribers, the delivery order will resemble that
	 * of a stack. For example:
	 * </p>
	 * <ol>
	 * <li>{@link imagej.module.event.ModulesUpdatedEvent} is published with
	 * {@link #publish}.</li>
	 * <li>{@link imagej.menu.DefaultMenuService} receives the event and handles
	 * it, publishing {@link imagej.menu.event.MenusUpdatedEvent} in response.</li>
	 * <li>A third party that subscribes to both
	 * {@link imagej.module.event.ModulesUpdatedEvent} and
	 * {@link imagej.menu.event.MenusUpdatedEvent} will receive the latter before
	 * the former.</li>
	 * </ol>
	 * That said, the behavior of {@link #publish} depends on the thread from
	 * which it is called: if called from a thread identified as a dispatch thread
	 * by {@link imagej.thread.ThreadService#isDispatchThread()}, it will publish
	 * immediately; otherwise, it will be queued for publication on a dispatch
	 * thread, and block the calling thread until publication is complete. This
	 * means that a chain of events published with a mixture of {@link #publish}
	 * and {@link #publishLater} may result in event delivery in an unintuitive
	 * order.
	 */
	<E extends ImageJEvent> void publish(E e);

	/**
	 * Queues the given event for publication, typically on a separate thread
	 * (called the "event dispatch thread"). This method returns immediately,
	 * before subscribers have fully received the event.
	 * <p>
	 * Note that with {@link #publishLater}, in the case of multiple events
	 * published in a chain to multiple subscribers, the delivery order will
	 * resemble that of a queue. For example:
	 * </p>
	 * <ol>
	 * <li>{@link imagej.module.event.ModulesUpdatedEvent} is published with
	 * {@link #publishLater}.</li>
	 * <li>{@link imagej.menu.DefaultMenuService} receives the event and handles
	 * it, publishing {@link imagej.menu.event.MenusUpdatedEvent} in response.</li>
	 * <li>A third party that subscribes to both
	 * {@link imagej.module.event.ModulesUpdatedEvent} and
	 * {@link imagej.menu.event.MenusUpdatedEvent} will receive the former first,
	 * since it was already queued by the time the latter was published.</li>
	 * </ol>
	 */
	<E extends ImageJEvent> void publishLater(E e);

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
	List<EventSubscriber<?>> subscribe(Object o);

	/**
	 * Removes all the given subscribers; they will no longer be notified when
	 * events are published.
	 */
	void unsubscribe(Collection<EventSubscriber<?>> subscribers);

	/**
	 * Gets a list of all subscribers to the given event class (and subclasses
	 * thereof).
	 */
	<E extends ImageJEvent> List<EventSubscriber<E>> getSubscribers(Class<E> c);

}
