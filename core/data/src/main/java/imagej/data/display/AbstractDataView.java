//
// AbstractDataView.java
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

package imagej.data.display;

import imagej.ImageJ;
import imagej.data.Data;
import imagej.data.Extents;
import imagej.data.Position;
import imagej.data.display.event.DataViewDeselectedEvent;
import imagej.data.display.event.DataViewSelectedEvent;
import imagej.data.display.event.DataViewSelectionEvent;
import imagej.event.EventService;

import java.util.HashMap;
import java.util.Map;

import net.imglib2.meta.AxisType;

/**
 * Abstract superclass for {@link DataView}s.
 * 
 * @author Curtis Rueden
 */
public abstract class AbstractDataView implements DataView {

	private final Data data;

	protected final EventService eventService;

	/**
	 * View's position along each applicable dimensional axis.
	 * <p>
	 * Note that axes keyed here may go beyond those of the linked {@link Data}
	 * object, if the view is part of a larger aggregate coordinate space.
	 * </p>
	 * <p>
	 * By default, each axis is at position 0 unless otherwise specified.
	 * </p>
	 */
	private final Map<AxisType, Long> pos = new HashMap<AxisType, Long>();

	private long[] planeDims;
	private long[] position;
	private Position planePosition;

	/** Indicates the view is no longer in use. */
	private boolean disposed;

	/**
	 * True if view is selected, false if not.
	 */
	private boolean selected;

	public AbstractDataView(final Data data) {
		eventService = ImageJ.get(EventService.class);
		this.data = data;
		data.incrementReferences();
	}

	// -- DataView methods --

	@Override
	public Data getData() {
		return data;
	}

	@Override
	public Position getPlanePosition() {
		final Position p = data.getExtents().createPosition();
		for (int d=2; d<data.numDimensions(); d++) {
			final AxisType axis = data.axis(d);
			p.setPosition(getPosition(axis), d - 2);
		}
		return p;
	}

	@Override
	public long getPosition(final AxisType axis) {
		// FIXME
		final int dim = data.getAxisIndex(axis);
		if (dim < 0) {
			throw new IllegalArgumentException("Unknown axis: " + axis);
		}
		return position[dim];
	}

	@Override
	public void setPosition(final AxisType axis, final long value) {
		// FIXME
		final int dim = data.getAxisIndex(axis);
		if (dim < 0) {
			throw new IllegalArgumentException("Unknown axis: " + axis);
		}
		position[dim] = value;
		if (dim >= 2) planePosition.setPosition(value, dim - 2);
	}

	@Override
	public void setSelected(final boolean isSelected) {
		if (selected != isSelected) {
			selected = isSelected;
			final DataViewSelectionEvent event =
				isSelected ? new DataViewSelectedEvent(this)
					: new DataViewDeselectedEvent(this);
			eventService.publish(event);
		}
	}

	@Override
	public boolean isSelected() {
		return selected;
	}

	@Override
	public boolean isVisible() {
		return true;
	}

	@Override
	public void dispose() {
		if (disposed) return;
		disposed = true;
		data.decrementReferences();
	}

	// -- Helper methods --

	protected void setDimensions(final long[] dims) {
		position = new long[dims.length];
		planeDims = new long[dims.length - 2];
		for (int i = 0; i < planeDims.length; i++)
			planeDims[i] = dims[i + 2];
		final Extents extents = new Extents(planeDims);
		planePosition = extents.createPosition();
		planePosition.first();
	}

}
