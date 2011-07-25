//
// Events.java
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

import imagej.IService;
import imagej.ImageJ;
import imagej.Service;

import java.util.List;

import org.bushe.swing.event.ThreadSafeEventService;

/**
 * Service for publishing and subscribing to ImageJ events.
 * 
 * @author Curtis Rueden
 * @author Grant Harris
 */
@Service
public final class EventService implements IService {

	private ImageJ context;

	private ThreadSafeEventService eventBus;

	public EventService(final ImageJ context) {
		this.context = context;
	}

	public <E extends ImageJEvent> void publish(final E e) {
		e.setContext(context);
		eventBus.publish(e);
	}

	public <E extends ImageJEvent> void subscribe(final Class<E> c,
		final EventSubscriber<E> subscriber)
	{
		eventBus.subscribe(c, subscriber);
	}

	public <E extends ImageJEvent> void unsubscribe(final Class<E> c,
		final EventSubscriber<E> subscriber)
	{
		eventBus.unsubscribe(c, subscriber);
	}

	public <E extends ImageJEvent> List<E> getSubscribers(final Class<E> c) {
		return eventBus.getSubscribers(c);
	}

	// -- IService methods --

	@Override
	public void initialize() {
		eventBus = new ThreadSafeEventService();
	}

}
