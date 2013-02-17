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

package imagej.core.commands.debug;

import imagej.command.Command;
import imagej.display.event.DisplayActivatedEvent;
import imagej.display.event.DisplayUpdatedEvent;

import java.util.List;

import org.scijava.ItemIO;
import org.scijava.event.EventService;
import org.scijava.event.EventSubscriber;
import org.scijava.event.SciJavaEvent;
import org.scijava.object.event.ObjectCreatedEvent;
import org.scijava.object.event.ObjectDeletedEvent;
import org.scijava.object.event.ObjectsListEvent;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

/**
 * For EventBus diagnostics: shows what is subscribed to various event types.
 * 
 * @author Grant Harris
 * @author Curtis Rueden
 */
@Plugin(type = Command.class, menuPath = "Plugins>Debug>Subscribers", headless = true)
public class ShowSubscribers implements Command {

	@Parameter
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

	private <E extends SciJavaEvent> void listSubs(final StringBuilder sb,
		final Class<E> c)
	{
		final List<EventSubscriber<E>> subscribers = eventService.getSubscribers(c);
		sb.append(c.getSimpleName() + ":\n");
		for (final EventSubscriber<E> subscriber : subscribers) {
			sb.append("    " + subscriber.toString() + "\n");
		}
	}

}
