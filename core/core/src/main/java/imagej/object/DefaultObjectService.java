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

package imagej.object;

import imagej.ImageJ;
import imagej.event.EventHandler;
import imagej.event.EventService;
import imagej.object.event.ObjectCreatedEvent;
import imagej.object.event.ObjectDeletedEvent;
import imagej.object.event.ObjectsAddedEvent;
import imagej.object.event.ObjectsRemovedEvent;
import imagej.service.AbstractService;
import imagej.service.Service;

import java.util.List;

/**
 * Default service for keeping track of registered objects. Automatically
 * registers new objects from {@link ObjectCreatedEvent}s, and removes objects
 * from {@link ObjectDeletedEvent}s.
 * <p>
 * This is useful to retrieve available objects of a particular type, such as
 * the list of <code>imagej.data.Dataset</code>s upon which a user can choose to
 * operate.
 * </p>
 * 
 * @author Curtis Rueden
 */
@Service
public final class DefaultObjectService extends AbstractService implements
	ObjectService
{

	private final EventService eventService;

	/** Index of registered objects. */
	private final ObjectIndex<Object> objectIndex = new ObjectIndex<Object>(
		Object.class);

	// -- Constructors --

	public DefaultObjectService() {
		// NB: Required by SezPoz.
		super(null);
		throw new UnsupportedOperationException();
	}

	public DefaultObjectService(final ImageJ context,
		final EventService eventService)
	{
		super(context);
		this.eventService = eventService;

		subscribeToEvents(eventService);
	}

	// -- ObjectService methods --

	@Override
	public EventService getEventService() {
		return eventService;
	}

	@Override
	public ObjectIndex<Object> getIndex() {
		return objectIndex;
	}

	@Override
	public <T> List<T> getObjects(final Class<T> type) {
		final List<Object> list = objectIndex.get(type);
		@SuppressWarnings("unchecked")
		final List<T> result = (List<T>) list;
		return result;
	}

	@Override
	public void addObject(final Object obj) {
		objectIndex.add(obj);
		eventService.publish(new ObjectsAddedEvent(obj));
	}

	@Override
	public void removeObject(final Object obj) {
		objectIndex.remove(obj);
		eventService.publish(new ObjectsRemovedEvent(obj));
	}

	// -- Event handlers --

	@EventHandler
	protected void onEvent(final ObjectCreatedEvent event) {
		addObject(event.getObject());
	}

	@EventHandler
	protected void onEvent(final ObjectDeletedEvent event) {
		removeObject(event.getObject());
	}

}
