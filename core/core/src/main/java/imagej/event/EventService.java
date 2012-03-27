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

import imagej.service.IService;

import java.util.Collection;
import java.util.List;

/**
 * Interface for the event handling service.
 * 
 * @author Curtis Rueden
 * @author Grant Harris
 */
public interface EventService extends IService {

	/** TODO */
	<E extends ImageJEvent> void publish(final E e);

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
	List<EventSubscriber<?>> subscribe(final Object o);

	/** TODO */
	void unsubscribe(final Collection<EventSubscriber<?>> subscribers);

	/** TODO */
	<E extends ImageJEvent> List<EventSubscriber<E>> getSubscribers(
		final Class<E> c);

}
