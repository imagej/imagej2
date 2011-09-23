//
// ShowSubscribers.java
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

package imagej.core.plugins.debug;

import imagej.event.EventService;
import imagej.event.EventSubscriber;
import imagej.event.ImageJEvent;
import imagej.ext.display.event.DisplayActivatedEvent;
import imagej.ext.display.event.DisplayUpdatedEvent;
import imagej.ext.module.ItemIO;
import imagej.ext.plugin.ImageJPlugin;
import imagej.ext.plugin.Parameter;
import imagej.ext.plugin.Plugin;
import imagej.object.event.ObjectCreatedEvent;
import imagej.object.event.ObjectDeletedEvent;
import imagej.object.event.ObjectsListEvent;

import java.util.List;

/**
 * For EventBus diagnostics: shows what is subscribed to various event types.
 * 
 * @author Grant Harris
 * @author Curtis Rueden
 */
@Plugin(menuPath = "Plugins>Debug>Subscribers")
public class ShowSubscribers implements ImageJPlugin {

	@Parameter(required = true, persist = false)
	private EventService eventService;

	@Parameter(label = "Subscriber Log", type = ItemIO.OUTPUT)
	private String subscriberLog;

	// -- ShowSubscribers methods --

	public String getSubscriberLog() {
		return subscriberLog;
	}

	// -- Runnable methods --

	@Override
	public void run() {
		final StringBuilder sb = new StringBuilder();
		listSubs(sb, ObjectsListEvent.class);
		listSubs(sb, ObjectCreatedEvent.class);
		listSubs(sb, ObjectDeletedEvent.class);
		listSubs(sb, DisplayActivatedEvent.class);
		listSubs(sb, DisplayUpdatedEvent.class);
		subscriberLog = sb.toString();
	}

	// -- Helper methods --

	private <E extends ImageJEvent> void listSubs(final StringBuilder sb,
		final Class<E> c)
	{
		final List<EventSubscriber<E>> subscribers =
			eventService.getSubscribers(c);
		sb.append(c.getSimpleName() + ":\n");
		for (final EventSubscriber<E> subscriber : subscribers) {
			sb.append("    " + subscriber.toString() + "\n");
		}
	}

}
