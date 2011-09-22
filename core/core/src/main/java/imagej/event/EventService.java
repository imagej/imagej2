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

}
