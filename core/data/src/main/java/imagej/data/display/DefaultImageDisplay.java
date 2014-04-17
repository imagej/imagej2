/*
 * #%L
 * ImageJ software for multidimensional image processing and analysis.
 * %%
 * Copyright (C) 2009 - 2014 Board of Regents of the University of
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
 * #L%
 */

package imagej.data.display;

import imagej.data.Data;
import imagej.data.display.event.AxisActivatedEvent;
import imagej.data.display.event.AxisPositionEvent;
import imagej.data.event.DataRestructuredEvent;
import imagej.data.event.DataUpdatedEvent;
import imagej.data.lut.LUTService;
import imagej.display.AbstractDisplay;
import imagej.display.Display;
import imagej.display.DisplayService;
import imagej.display.event.DisplayDeletedEvent;

import java.util.concurrent.ConcurrentHashMap;

import net.imglib2.Localizable;
import net.imglib2.Positionable;
import net.imglib2.RealPositionable;
import net.imglib2.display.ColorTable;
import net.imglib2.meta.Axes;
import net.imglib2.meta.AxisType;
import net.imglib2.meta.CalibratedAxis;
import net.imglib2.meta.CalibratedRealInterval;
import net.imglib2.meta.CombinedCalibratedRealInterval;

import org.scijava.event.EventHandler;
import org.scijava.event.EventService;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.thread.ThreadService;
import org.scijava.util.RealRect;

/**
 * Default implementation of {@link ImageDisplay}.
 * 
 * @author Lee Kamentsky
 * @author Curtis Rueden
 */
@Plugin(type = Display.class)
public class DefaultImageDisplay extends AbstractDisplay<DataView> implements
	ImageDisplay
{

	/** Data structure that aggregates dimensional axes from constituent views. */
	// private final CombinedInterval combinedInterval = new CombinedInterval();
	private final CombinedCalibratedRealInterval<CalibratedAxis, CalibratedRealInterval<CalibratedAxis>> combinedInterval =
		new CombinedCalibratedRealInterval<CalibratedAxis, CalibratedRealInterval<CalibratedAxis>>();

	@Parameter
	private ThreadService threadService;

	@Parameter(required = false)
	private DisplayService displayService;

	@Parameter(required = false)
	private EventService eventService;

	@Parameter(required = false)
	private ImageDisplayService imageDisplayService;

	@Parameter(required = false)
	private LUTService lutService;

	private AxisType activeAxis = null;

	private ImageCanvas canvas;

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

	// -- AbstractDisplay methods --

	@Override
	protected void rebuild() {
		// NB: Ensure display flags its structure as changed.
		super.rebuild();

		// combine constituent views into a single aggregate spatial interval
		combinedInterval.clear();
		for (final DataView view : this) {
			combinedInterval.add(view.getData());
		}
		combinedInterval.update();

		// rebuild views
		for (final DataView view : DefaultImageDisplay.this) {
			view.rebuild();
		}

		// remove obsolete axes
		for (final AxisType axis : pos.keySet()) {
			if (dimensionIndex(axis) < 0) {
				pos.remove(axis);
			}
		}

		// initialize position of new axes
		for (int i = 0; i < numDimensions(); i++) {
			final AxisType axis = axis(i).type();
			if (axis.isXY()) continue; // do not track position of planar axes
			if (!pos.containsKey(axis)) {
				// start at minimum value
				pos.put(axis, min(i));
			}
		}

		if (getActiveAxis() == null) initActiveAxis();
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
		if (dimensionIndex(axis) < 0) {
			throw new IllegalArgumentException("Unknown axis: " + axis);
		}
		activeAxis = axis;

		// notify interested parties of the change
		if (eventService != null) {
			eventService.publish(new AxisActivatedEvent(this, activeAxis));
		}
	}

	@Override
	public boolean isVisible(final DataView view) {
		for (int i = 0; i < numDimensions(); i++) {
			final AxisType axis = axis(i).type();
			if (axis.isXY()) continue;
			final long value = getLongPosition(axis);
			final int index = view.getData().dimensionIndex(axis);
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

	@Override
	public ImageCanvas getCanvas() {
		if (canvas == null) canvas = new DefaultImageCanvas(this);
		return canvas;
	}

	@Override
	public RealRect getPlaneExtents() {
		final int xAxis = dimensionIndex(Axes.X);
		final int yAxis = dimensionIndex(Axes.Y);
		final double xMin = realMin(xAxis);
		final double yMin = realMin(yAxis);
		final double width = realMax(xAxis) - realMin(xAxis);
		final double height = realMax(yAxis) - realMin(yAxis);
		return new RealRect(xMin, yMin, width, height);
	}

	// -- Display methods --

	@Override
	public boolean canDisplay(final Class<?> c) {
		return (imageDisplayService != null && Data.class.isAssignableFrom(c)) ||
			(lutService != null && ColorTable.class.isAssignableFrom(c)) ||
			super.canDisplay(c);
	}

	@Override
	public void display(final Object o) {
		DataView dataView = null;
		Data data = null;
		if (o instanceof DataView) {
			// object is a data view, natively compatible with this display
			dataView = (DataView) o;
		}
		else if (o instanceof Data) {
			// object is a data object, which we can wrap in a data view
			data = (Data) o;
		}
		else if (o instanceof ColorTable) {
			// object is a LUT, which we can wrap in a dataset
			final ColorTable colorTable = (ColorTable) o;
			data = lutService.createDataset(null, colorTable);
		}

		if (data != null) {
			// wrap data object in a data view
			if (imageDisplayService == null) {
				throw new IllegalStateException(
					"An ImageDisplayService is required to display Data objects");
			}
			dataView = imageDisplayService.createDataView(data);
		}

		if (dataView == null) {
			throw new IllegalArgumentException("Incompatible object: " + o + " [" +
				o.getClass().getName() + "]");
		}

		// display the data view
		super.display(dataView);
		updateName(dataView);
		rebuild();
	}

	@Override
	public boolean isDisplaying(final Object o) {
		if (super.isDisplaying(o)) return true;

		// check for wrapped Data objects
		for (final DataView view : this) {
			if (o == view.getData()) return true;
		}

		return false;
	}

	@Override
	public void update() {
		// NB - this combinedinterval.update() call rebuilds the interval. We have
		// found cases where this is necessary to avoid situations where the we try
		// to access a no longer existing axis. As an example of this try running
		// legacy command Type > 8-bit Color on Clowns. Without this line, when you
		// run the command, an exception is thrown.
		// TODO - is this a performance issue?
		combinedInterval.update();
		for (final DataView view : this) {
			for (int i = 0; i < numDimensions(); i++) {
				AxisType axis = axis(i).type();
				if (axis.isXY()) continue;
				final int axisNum = view.getData().dimensionIndex(axis);
				if (axisNum < 0) continue;
				final long p = getLongPosition(axis);
				Data data = view.getData();
				double size = data.realMax(axisNum) - data.realMin(axisNum) + 1;
				if (p < size) {
					view.setPosition(p, axis);
				}
			}
			view.update();
		}
		super.update();
	}

	// -- Interval methods --

	@Override
	public long min(final int d) {
		return (long) Math.floor(combinedInterval.realMin(d));
	}

	@Override
	public void min(final long[] min) {
		for (int i = 0; i < min.length; i++) {
			min[i] = min(i);
		}
	}

	@Override
	public void min(final Positionable min) {
		for (int i = 0; i < min.numDimensions(); i++) {
			min.setPosition(min(i), i);
		}
	}

	@Override
	public long max(final int d) {
		return (long) Math.ceil(combinedInterval.realMax(d));
	}

	@Override
	public void max(final long[] max) {
		for (int i = 0; i < max.length; i++) {
			max[i] = max(i);
		}
	}

	@Override
	public void max(final Positionable max) {
		for (int i = 0; i < max.numDimensions(); i++) {
			max.setPosition(max(i), i);
		}
	}

	@Override
	public void dimensions(final long[] dimensions) {
		for (int i = 0; i < dimensions.length; i++) {
			dimensions[i] = dimension(i);
		}
	}

	@Override
	public long dimension(final int d) {
		return max(d) - min(d) + 1;
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
	public double averageScale(final int d) {
		return combinedInterval.averageScale(d);
	}

	// -- TypedSpace methods --

	@Override
	public int dimensionIndex(final AxisType axis) {
		return combinedInterval.dimensionIndex(axis);
	}

	// -- AnnotatedSpace methods --

	@Override
	public CalibratedAxis axis(final int d) {
		return combinedInterval.axis(d);
	}

	@Override
	public void axes(final CalibratedAxis[] axes) {
		combinedInterval.axes(axes);
	}

	@Override
	public void setAxis(final CalibratedAxis axis, final int d) {
		combinedInterval.setAxis(axis, d);
	}

	// -- PositionableByAxis methods --

	@Override
	public int getIntPosition(final AxisType axis) {
		return (int) getLongPosition(axis);
	}

	@Override
	public long getLongPosition(final AxisType axis) {
		final int d = dimensionIndex(axis);
		if (d < 0) {
			// untracked axes are all at position 0 by default
			return 0;
		}
		final Long value = pos.get(axis);
		if (value == null) return 0;
		final long min = min(d);
		if (value < min) return min;
		final long max = max(d);
		if (value > max) return max;
		return value;
	}

	@Override
	public void setPosition(final long position, final AxisType axis) {
		final int axisIndex = dimensionIndex(axis);
		if (axisIndex < 0) {
			throw new IllegalArgumentException("Invalid axis: " + axis);
		}

		// clamp new position value to [min, max]
		final long min = min(axisIndex);
		final long max = max(axisIndex);
		long value = position;
		if (value < min) value = min;
		if (value > max) value = max;

		// update position
		pos.put(axis, value);

		// notify interested parties of the change
		// NB: DataView.setPosition is called only in update method.
		if (eventService != null) {
			// NB: BDZ changed from publish() to publishLater(). This fixes bug #1234.
			// We may want to change order of events to allow publish() instead.
			eventService.publishLater(new AxisPositionEvent(this, axis));
		}
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
		return getIntPosition(axis(d).type());
	}

	@Override
	public long getLongPosition(final int d) {
		return getLongPosition(axis(d).type());
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
		setPosition(position, axis(d).type());
	}

	@Override
	public void setPosition(final long position, final int d) {
		setPosition(position, axis(d).type());
	}

	// -- Event handlers --

	// TODO - displays should not listen for Data events. Views should listen for
	// data events, adjust themseleves, and generate view events. The display
	// classes should listen for view events and refresh themselves as necessary.

	@EventHandler
	protected void onEvent(final DataRestructuredEvent event) {
		threadService.run(new Runnable() {

			@Override
			public void run() {
				synchronized (getContext()) {
					for (final DataView view : DefaultImageDisplay.this) {
						if (event.getObject() == view.getData()) {
							rebuild();
							update();
							return;
						}
					}
				}
			}
		});
	}

	// TODO - displays should not listen for Data events. Views should listen for
	// data events, adjust themseleves, and generate view events. The display
	// classes should listen for view events and refresh themselves as necessary.

	@EventHandler
	protected void onEvent(final DataUpdatedEvent event) {
		for (final DataView view : this) {
			if (event.getObject() == view.getData()) {
				// BDZ removed 2013-03-15: update() updates all views. Addresses #1220.
				// view.update();
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
	 * If the display is still nameless, tries to name it after the given
	 * {@link DataView}.
	 */
	private void updateName(final DataView dataView) {
		if (getName() != null) return; // display already has a name
		final String dataName = dataView.getData().getName();
		if (dataName != null && !dataName.isEmpty()) {
			setName(createName(dataName));
		}
	}

	/**
	 * Creates a name for the display based on the given name, accounting for
	 * collisions with other image displays.
	 * <p>
	 * NB: If no {@link DisplayService} is available in this display's application
	 * context, the proposed name will be used without any collision checking.
	 * </p>
	 * 
	 * @param proposedName desired name prefix
	 * @return the name with stuff added to make it unique
	 */
	private String createName(final String proposedName) {
		if (displayService == null) return proposedName; // no way to check
		String theName = proposedName;
		int n = 0;
		while (!displayService.isUniqueName(theName)) {
			n++;
			theName = proposedName + "-" + n;
		}
		return theName;
	}

	private void initActiveAxis() {
		if (activeAxis == null) {
			for (int i = 0; i < numDimensions(); i++) {
				AxisType axisType = axis(i).type();
				if (axisType.isXY()) continue;
				setActiveAxis(axisType);
				return;
			}
		}
	}

	/** Frees resources associated with the display. */
	private void cleanup() {
		// NB: Fixes bug #893.
		for (final DataView view : this) {
			view.dispose();
		}
		clear();
		combinedInterval.clear();
	}

}
