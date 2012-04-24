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
import imagej.data.CombinedInterval;
import imagej.data.Data;
import imagej.data.Dataset;
import imagej.data.Extents;
import imagej.data.display.event.AxisPositionEvent;
import imagej.data.display.event.ZoomEvent;
import imagej.data.event.DataRestructuredEvent;
import imagej.data.event.DataUpdatedEvent;
import imagej.data.event.DatasetUpdatedEvent;
import imagej.data.overlay.Overlay;
import imagej.event.EventHandler;
import imagej.event.EventSubscriber;
import imagej.ext.display.AbstractDisplay;
import imagej.ext.display.DisplayService;
import imagej.ext.display.event.DisplayDeletedEvent;
import imagej.ext.display.event.DisplayUpdatedEvent;
import imagej.ext.display.event.window.WinActivatedEvent;
import imagej.ext.tool.ToolService;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import net.imglib2.EuclideanSpace;
import net.imglib2.Localizable;
import net.imglib2.Positionable;
import net.imglib2.RealPositionable;
import net.imglib2.meta.Axes;
import net.imglib2.meta.AxisType;

/**
 * TODO - better Javadoc. The abstract display handles axes resolution,
 * maintaining the dimensionality of the {@link EuclideanSpace} represented by
 * the display.
 * 
 * @author Lee Kamentsky
 * @author Curtis Rueden
 */
public class DefaultImageDisplay extends AbstractDisplay<DataView>
	implements ImageDisplay
{
	private List<EventSubscriber<?>> subscribers;

	/** Data structure that aggregates dimensional axes from constituent views. */
	private final CombinedInterval combinedInterval = new CombinedInterval();

	private AxisType activeAxis = null;

	// NB - older comment - see 12-7-11 note
	// If pos is a HashMap rather than a ConcurrentHashMap,
	// the Delete Axis plugin throws a ConcurrentModificationException.
	private final ConcurrentHashMap<AxisType, Long> pos =
		new ConcurrentHashMap<AxisType, Long>();

	// NB - after a rewrite around 12-7-11 by CTR a ConcurrentHashMap might not
	// be needed. Initial testing seemed okay but will try and relax this
	// constraint later. Comment out for now.
	// private final HashMap<AxisType, Long> pos =
	// new HashMap<AxisType, Long>();

	public DefaultImageDisplay() {
		super(DataView.class);
	}

	protected void initActiveAxis() {
		if (activeAxis == null) {
			final AxisType[] axes = getAxes();
			for (final AxisType axis : axes) {
				if (axis == Axes.X) continue;
				if (axis == Axes.Y) continue;
				setActiveAxis(axis);
				return;
			}
		}
	}

	// -- Display method overrides --
	@Override
	public void setContext(ImageJ context) {
		super.setContext(context);
		assert subscribers == null;
		subscribers = eventService.subscribe(this);
	}
	// -- AbstractDisplay methods --

	@Override
	protected void rebuild() {
		// combine constituent views into a single aggregate spatial interval
		combinedInterval.clear();
		for (final DataView view : this) {
			combinedInterval.add(view.getData());
		}
		combinedInterval.update();
		if (!combinedInterval.isDiscrete()) {
			throw new IllegalStateException("Invalid combination of views");
		}

		// rebuild views
		for (final DataView view : this) {
			view.rebuild();
		}

		// remove obsolete axes
		for (final AxisType axis : pos.keySet()) {
			if (getAxisIndex(axis) < 0) pos.remove(axis);
		}

		// initialize position of new axes
		for (int i = 0; i < numDimensions(); i++) {
			final AxisType axis = axis(i);
			if (Axes.isXY(axis)) continue; // do not track position of planar axes
			if (!pos.containsKey(axis)) {
				// start at minimum value
				pos.put(axis, min(i));
			}
		}

	}

	// -- ImageDisplay methods --

	@Override
	public DataView getActiveView() {
		return size() > 0 ? get(0) : null;
	}

	@Override
	public AxisType getActiveAxis() {
		return activeAxis;
	}

	@Override
	public void setActiveAxis(final AxisType axis) {
		if (getAxisIndex(axis) < 0) {
			throw new IllegalArgumentException("Unknown axis: " + axis);
		}
		activeAxis = axis;
	}

	@Override
	public boolean containsData(final Data data) {
		for (final DataView view : this) {
			if (data == view.getData()) return true;
		}
		return false;
	}

	@Override
	public boolean isVisible(final DataView view) {
		for (int i = 0; i < numDimensions(); i++) {
			final AxisType axis = axis(i);
			if (axis.isXY()) continue;
			final long value = getLongPosition(axis);
			final int index = view.getData().getAxisIndex(axis);
			if (index < 0) {
				// verify that the display's position matches the view's value
				if (value != view.getLongPosition(axis)) return false;
			}
			else {
				// verify that the display's position matches the data's range
				final double min = index < 0 ? 0 : view.getData().realMin(index);
				final double max = index < 0 ? 0 : view.getData().realMax(index);
				if (value < min || value > max) {
					// dimensional position is outside the data's range
					return false;
				}
			}
		}
		return true;
	}

	// -- Display methods --

	@Override
	public boolean canDisplay(final Class<?> c) {
		return Data.class.isAssignableFrom(c) || super.canDisplay(c);
	}

	@Override
	public void display(final Object o) {
		if (o instanceof Dataset) {
			Dataset dataset = (Dataset) o;
			setName(createName(dataset.getName()));
			add(new DefaultDatasetView(dataset));
		}
		else if (o instanceof Overlay) {
			Overlay overlay = (Overlay) o;
			add(new DefaultOverlayView(overlay));
		}
		else super.display(o);
	}

	@Override
	public void update() {
		for (final DataView view : this) {
			for (final AxisType axis : getAxes()) {
				if (Axes.isXY(axis)) continue;
				if (view.getData().getAxisIndex(axis) < 0) continue;
				view.setPosition(getLongPosition(axis), axis);
			}
			view.update();
		}
		super.update();
	}

	// -- CalibratedInterval methods --

	@Override
	public AxisType[] getAxes() {
		return combinedInterval.getAxes();
	}

	@Override
	public boolean isDiscrete() {
		return combinedInterval.isDiscrete();
	}

	@Override
	public Extents getExtents() {
		return combinedInterval.getExtents();
	}

	@Override
	public long[] getDims() {
		return combinedInterval.getDims();
	}

	// -- Interval methods --

	@Override
	public long min(final int d) {
		return combinedInterval.min(d);
	}

	@Override
	public void min(final long[] min) {
		combinedInterval.min(min);
	}

	@Override
	public void min(final Positionable min) {
		combinedInterval.min(min);
	}

	@Override
	public long max(final int d) {
		return combinedInterval.max(d);
	}

	@Override
	public void max(final long[] max) {
		combinedInterval.max(max);
	}

	@Override
	public void max(final Positionable max) {
		combinedInterval.max(max);
	}

	@Override
	public void dimensions(final long[] dimensions) {
		combinedInterval.dimensions(dimensions);
	}

	@Override
	public long dimension(final int d) {
		return combinedInterval.dimension(d);
	}

	// -- RealInterval methods --

	@Override
	public double realMin(final int d) {
		return combinedInterval.realMin(d);
	}

	@Override
	public void realMin(final double[] min) {
		combinedInterval.realMin(min);
	}

	@Override
	public void realMin(final RealPositionable min) {
		combinedInterval.realMin(min);
	}

	@Override
	public double realMax(final int d) {
		return combinedInterval.realMax(d);
	}

	@Override
	public void realMax(final double[] max) {
		combinedInterval.realMax(max);
	}

	@Override
	public void realMax(final RealPositionable max) {
		combinedInterval.realMax(max);
	}

	// -- EuclideanSpace methods --

	@Override
	public int numDimensions() {
		return combinedInterval.numDimensions();
	}

	// -- CalibratedSpace methods --

	@Override
	public int getAxisIndex(final AxisType axis) {
		return combinedInterval.getAxisIndex(axis);
	}

	@Override
	public AxisType axis(final int d) {
		return combinedInterval.axis(d);
	}

	@Override
	public void axes(final AxisType[] axes) {
		combinedInterval.axes(axes);
	}

	@Override
	public void setAxis(final AxisType axis, final int d) {
		combinedInterval.setAxis(axis, d);
	}

	@Override
	public double calibration(final int d) {
		return combinedInterval.calibration(d);
	}

	@Override
	public void calibration(final double[] cal) {
		combinedInterval.calibration(cal);
	}

	@Override
	public void setCalibration(final double cal, final int d) {
		combinedInterval.setCalibration(cal, d);
	}

	// -- PositionableByAxis methods --

	@Override
	public int getIntPosition(final AxisType axis) {
		return (int) getLongPosition(axis);
	}

	@Override
	public long getLongPosition(final AxisType axis) {
		if (getAxisIndex(axis) < 0) {
			// untracked axes are all at position 0 by default
			return 0;
		}
		final Long value = pos.get(axis);
		return value == null ? 0 : value;
	}

	@Override
	public void setPosition(final long position, final AxisType axis) {
		final int axisIndex = getAxisIndex(axis);
		if (axisIndex < 0) {
			throw new IllegalArgumentException("Invalid axis: " + axis);
		}

		// clamp new position value to [min, max]
		final long min = min(axisIndex);
		final long max = max(axisIndex);
		long value = position;
		if (value < min) value = min;
		if (value > max) value = max;

		// update position and notify interested parties of the change
		pos.put(axis, value);
		// NB: DataView.setPosition is called only in update method.
		eventService.publish(new AxisPositionEvent(this, axis));
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
		return getIntPosition(axis(d));
	}

	@Override
	public long getLongPosition(final int d) {
		return getLongPosition(axis(d));
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
		setPosition(position, axis(d));
	}

	@Override
	public void setPosition(final long position, final int d) {
		setPosition(position, axis(d));
	}

	// -- Event handlers --

	// TODO - displays should not listen for Data events. Views should listen for
	// data events, adjust themseleves, and generate view events. The display
	// classes should listen for view events and refresh themselves as necessary.

	@EventHandler
	protected void onEvent(final DataRestructuredEvent event) {
		for (final DataView view : this) {
			if (event.getObject() == view.getData()) {
				rebuild();
				update();
				return;
			}
		}
	}

	// TODO - displays should not listen for Data events. Views should listen for
	// data events, adjust themseleves, and generate view events. The display
	// classes should listen for view events and refresh themselves as necessary.

	@EventHandler
	protected void onEvent(final DataUpdatedEvent event) {
		for (final DataView view : this) {
			if (event.getObject() == view.getData()) {
				view.update();
				update();
				return;
			}
		}
	}

	@EventHandler
	protected void onEvent(final DisplayDeletedEvent event) {
		if (event.getObject() != this) return;
		
		cleanup();
	}

	// -- Helper methods --
	
	/**
	 * Create a name for the display based on the given name
	 * accounting for collisions with other image displays
	 * 
	 * @param proposedName
	 * @return the name with stuff added to make it unique
	 */
	private String createName(String proposedName) {
		final DisplayService displayService = getContext().getService(DisplayService.class);
		String theName = proposedName;
		int n = 0;
		while (!displayService.isUniqueName(theName)) {
			n++;
			theName = proposedName + "-" + n;
		}
		return theName;		
	}

	/** Frees resources associated with the display. */
	private void cleanup() {
		// NB: Fixes bug #893.
		for (final DataView view : this) {
			view.dispose();
		}
		clear();
		// NB - this is necessary to make sure resources get returned via GC.
		// Else there is a memory leak.
		eventService.unsubscribe(subscribers);
	}

	protected Dataset getDataset(final DataView view) {
		final Data data = view.getData();
		return data instanceof Dataset ? (Dataset) data : null;
	}

	@Override
	public ImageJ getContext() {
		// TODO Auto-generated method stub
		return null;
	}

}
