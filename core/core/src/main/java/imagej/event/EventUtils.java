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

import imagej.Context;
import imagej.command.Command;

import java.util.Collection;
import java.util.List;

/**
 * Utility methods for working with {@link Command}s.
 * 
 * @author Curtis Rueden
 */
public final class EventUtils {

	private EventUtils() {
		// prevent instantiation of utility class
	}

	/**
	 * Registers any event handling methods (defined in subclasses).
	 * <p>
	 * Does nothing if the context is null, or if the context has no associated
	 * {@link EventService}.
	 * </p>
	 * 
	 * @return The list of event subscribers that were created. It is necessary to
	 *         keep a reference to this list, to avoid them being garbage
	 *         collected (which would cause events to stop being delivered).
	 * @see EventService#subscribe(Object)
	 */
	public static List<EventSubscriber<?>> subscribe(final Context context,
		final Object o)
	{
		if (context == null) return null;
		final EventService eventService = context.getService(EventService.class);
		if (eventService == null) return null;
		return eventService.subscribe(o);
	}

	/**
	 * Unregisters the given event handling methods.
	 * <p>
	 * Does nothing if the context is null, or if the context has no associated
	 * {@link EventService}.
	 * </p>
	 * 
	 * @see EventService#unsubscribe(Collection)
	 */
	public static void unsubscribe(final Context context,
		final Collection<EventSubscriber<?>> subscribers)
	{
		if (context == null) return;
		final EventService eventService = context.getService(EventService.class);
		if (eventService == null) return;
		eventService.unsubscribe(subscribers);
	}

}
