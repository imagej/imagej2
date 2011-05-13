//
// AbstractDisplayView.java
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

package imagej.display;

import imagej.data.DataObject;
import imagej.data.event.DataObjectRestructuredEvent;
import imagej.data.event.DataObjectUpdatedEvent;
import imagej.event.EventSubscriber;
import imagej.event.Events;
import imagej.util.Index;

import java.util.ArrayList;
import java.util.List;

/**
 * Abstract supeclass for {@link DisplayView}s.
 * 
 * @author Curtis Rueden
 */
public abstract class AbstractDisplayView implements DisplayView {

	private final Display display;
	private final DataObject dataObject;

	/** List of event subscribers, to avoid garbage collection. */
	private final List<EventSubscriber<?>> subscribers =
		new ArrayList<EventSubscriber<?>>();

	protected long[] dims, planeDims;
	protected long[] position, planePos;

	/** Indicates the view is no longer in use. */
	private boolean disposed;

	public AbstractDisplayView(final Display display, final DataObject dataObject) {
		this.display = display;
		this.dataObject = dataObject;
		dataObject.incrementReferences();
		subscribeToEvents();
	}

	// -- DisplayView methods --

	@Override
	public Display getDisplay() {
		return display;
	}

	@Override
	public DataObject getDataObject() {
		return dataObject;
	}

	@Override
	public long[] getPlanePosition() {
		return planePos;
	}

	@Override
	public long getPlaneIndex() {
		return Index.indexNDto1D(planeDims, planePos);
	}

	@Override
	public void setPosition(final int value, final int dim) {
		position[dim] = value;
		for (int i = 0; i < planePos.length; i++)
			planePos[i] = position[i + 2];
	}

	@Override
	public void dispose() {
		if (disposed) return;
		disposed = true;
		dataObject.decrementReferences();
	}

	// -- Helper methods --

	/** Updates the display when the linked object changes. */
	private void subscribeToEvents() {
		final EventSubscriber<DataObjectUpdatedEvent> updateSubscriber =
			new EventSubscriber<DataObjectUpdatedEvent>()
		{
			@SuppressWarnings("synthetic-access")
			@Override
			public void onEvent(final DataObjectUpdatedEvent event) {
				if (event.getObject() != dataObject) return;
				update();
				display.update();
			}
		};
		Events.subscribe(DataObjectUpdatedEvent.class, updateSubscriber);
		subscribers.add(updateSubscriber);

		// TODO - perhaps it would be better for the display to listen for
		// ObjectRestructuredEvents, compare the data object to all of its views,
		// and call rebuild() on itself (only once). This would avoid a potential
		// issue where multiple views linked to the same data object will currently
		// result in multiple rebuilds.
		final EventSubscriber<DataObjectRestructuredEvent> restructureSubscriber =
			new EventSubscriber<DataObjectRestructuredEvent>()
		{
			@SuppressWarnings("synthetic-access")
			@Override
			public void onEvent(final DataObjectRestructuredEvent event) {
				if (event.getObject() != dataObject) return;
				rebuild();
				display.update();
			}
		};
		Events.subscribe(DataObjectRestructuredEvent.class, restructureSubscriber);
		subscribers.add(restructureSubscriber);
	}

}
