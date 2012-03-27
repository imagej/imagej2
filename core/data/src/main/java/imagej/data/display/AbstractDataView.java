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

package imagej.data.display;

import imagej.ImageJ;
import imagej.data.Data;
import imagej.data.Extents;
import imagej.data.Position;
import imagej.data.display.event.DataViewDeselectedEvent;
import imagej.data.display.event.DataViewSelectedEvent;
import imagej.data.display.event.DataViewSelectionEvent;
import imagej.event.EventService;
import imagej.event.ImageJEvent;

import java.util.HashMap;
import java.util.Map;

import net.imglib2.Localizable;
import net.imglib2.meta.AxisType;

/**
 * Abstract superclass for {@link DataView}s.
 * 
 * @author Curtis Rueden
 */
public abstract class AbstractDataView implements DataView {

	private final Data data;

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

	/** Indicates the view is no longer in use. */
	private boolean disposed;

	/** True if view is selected, false if not. */
	private boolean selected;

	public AbstractDataView(final Data data) {
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
		final long[] planeDims = new long[data.numDimensions() - 2];
		for (int d = 0; d < planeDims.length; d++) {
			planeDims[d] = data.dimension(d + 2);
		}
		final Extents planeExtents = new Extents(planeDims);
		final Position planePos = planeExtents.createPosition();
		for (int d = 0; d < planePos.numDimensions(); d++) {
			final AxisType axis = data.axis(d + 2);
			planePos.setPosition(getLongPosition(axis), d);
		}
		return planePos;
	}

	@Override
	public void setSelected(final boolean isSelected) {
		if (selected != isSelected) {
			selected = isSelected;
			final DataViewSelectionEvent event =
				isSelected ? new DataViewSelectedEvent(this)
					: new DataViewDeselectedEvent(this);
			publish(event);
		}
	}

	@Override
	public boolean isSelected() {
		return selected;
	}

	@Override
	public void dispose() {
		if (disposed) return;
		disposed = true;
		data.decrementReferences();
	}

	// -- PositionableByAxis methods --

	@Override
	public int getIntPosition(final AxisType axis) {
		return (int) getLongPosition(axis);
	}

	@Override
	public long getLongPosition(final AxisType axis) {
		final Long value = pos.get(axis);
		return value == null ? 0 : value;
	}

	@Override
	public void setPosition(final long position, final AxisType axis) {
		pos.put(axis, position);
	}

	// -- Localizable methods --

	@Override
	public void localize(final int[] position) {
		for (int i = 0; i < position.length; i++)
			position[i] = getIntPosition(i);
	}

	@Override
	public void localize(final long[] position) {
		for (int i = 0; i < position.length; i++)
			position[i] = getLongPosition(i);
	}

	@Override
	public int getIntPosition(final int d) {
		return getIntPosition(getData().axis(d));
	}

	@Override
	public long getLongPosition(final int d) {
		return getLongPosition(getData().axis(d));
	}

	// -- RealLocalizable methods --

	@Override
	public void localize(final float[] position) {
		for (int i = 0; i < position.length; i++)
			position[i] = getFloatPosition(i);
	}

	@Override
	public void localize(final double[] position) {
		for (int i = 0; i < position.length; i++)
			position[i] = getDoublePosition(i);
	}

	@Override
	public float getFloatPosition(final int d) {
		return getLongPosition(d);
	}

	@Override
	public double getDoublePosition(final int d) {
		return getLongPosition(d);
	}

	// -- EuclideanSpace methods --

	@Override
	public int numDimensions() {
		return data.numDimensions();
	}

	// -- Positionable methods --

	@Override
	public void fwd(final int d) {
		setPosition(getLongPosition(d) + 1, d);
	}

	@Override
	public void bck(final int d) {
		setPosition(getLongPosition(d) - 1, d);
	}

	@Override
	public void move(final int distance, final int d) {
		setPosition(getLongPosition(d) + distance, d);
	}

	@Override
	public void move(final long distance, final int d) {
		setPosition(getLongPosition(d) + distance, d);
	}

	@Override
	public void move(final Localizable localizable) {
		for (int i = 0; i < localizable.numDimensions(); i++)
			move(localizable.getLongPosition(i), i);
	}

	@Override
	public void move(final int[] distance) {
		for (int i = 0; i < distance.length; i++)
			move(distance[i], i);
	}

	@Override
	public void move(final long[] distance) {
		for (int i = 0; i < distance.length; i++)
			move(distance[i], i);
	}

	@Override
	public void setPosition(final Localizable localizable) {
		for (int i = 0; i < localizable.numDimensions(); i++)
			setPosition(localizable.getLongPosition(i), i);
	}

	@Override
	public void setPosition(final int[] position) {
		for (int i = 0; i < position.length; i++)
			setPosition(position[i], i);
	}

	@Override
	public void setPosition(final long[] position) {
		for (int i = 0; i < position.length; i++)
			setPosition(position[i], i);
	}

	@Override
	public void setPosition(final int position, final int d) {
		setPosition(position, getData().axis(d));
	}

	@Override
	public void setPosition(final long position, final int d) {
		setPosition(position, getData().axis(d));
	}

	// -- Helper methods --

	protected EventService getEventService() {
		final ImageJ context = data.getContext();
		if (context == null) return null;
		return context.getService(EventService.class);
	}

	protected void publish(final ImageJEvent event) {
		final EventService eventService = getEventService();
		if (eventService == null) return;
		eventService.publish(event);
	}

}
