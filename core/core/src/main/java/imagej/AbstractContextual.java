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

package imagej;

import imagej.event.EventService;
import imagej.event.EventSubscriber;

import java.util.List;

/**
 * Abstract base class for {@link Contextual} objects.
 * <p>
 * This class enforces a single call to {@link #setContext}, throwing an
 * {@link IllegalStateException} if the context is already set. It also
 * registers the object's {@link imagej.event.EventHandler} methods with the
 * {@link EventService}, if any, at the time the context is assigned. This frees
 * subclasses from the burden of maintaining {@link EventSubscriber} references
 * manually.
 * </p>
 * 
 * @author Curtis Rueden
 */
public abstract class AbstractContextual implements Contextual {

	/** This application context associated with the object. */
	private ImageJ context;

	/**
	 * The list of event subscribers, maintained to avoid garbage collection.
	 * 
	 * @see EventService#subscribe(Object)
	 */
	private List<EventSubscriber<?>> subscribers;

	// -- Object methods --

	@Override
	public void finalize() {
		// unregister any event handling methods
		final EventService eventService = context.getService(EventService.class);
		if (eventService != null) eventService.unsubscribe(subscribers);
	}

	// -- Contextual methods --

	@Override
	public ImageJ getContext() {
		return context;
	}

	@Override
	public void setContext(final ImageJ context) {
		if (this.context != null) {
			throw new IllegalStateException("Context already set");
		}
		this.context = context;

		// register any event handling methods defined in subclasses
		if (context != null) {
			final EventService eventService = context.getService(EventService.class);
			if (eventService != null) {
				// NB: Subscribe to all events handled by this object.
				// This greatly simplifies event handling for subclasses.
				subscribers = eventService.subscribe(this);
			}
		}
	}

}
