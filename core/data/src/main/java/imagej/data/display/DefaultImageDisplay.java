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
import imagej.data.event.DataRestructuredEvent;
import imagej.data.event.DataUpdatedEvent;
import imagej.data.undo.UndoService;
import imagej.display.AbstractDisplay;
import imagej.display.DisplayService;
import imagej.display.DisplayState;
import imagej.display.SupportsUndo;
import imagej.display.event.DisplayDeletedEvent;
import imagej.event.EventHandler;
import imagej.event.EventService;
import imagej.event.EventSubscriber;
import imagej.plugin.Plugin;
import imagej.util.RealRect;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import net.imglib2.Localizable;
import net.imglib2.Positionable;
import net.imglib2.RealPositionable;
import net.imglib2.img.Img;
import net.imglib2.meta.Axes;
import net.imglib2.meta.AxisType;
import net.imglib2.ops.pointset.HyperVolumePointSet;
import net.imglib2.ops.pointset.PointSet;
import net.imglib2.type.numeric.real.DoubleType;

/**
 * Default implementation of {@link ImageDisplay}.
 * 
 * @author Lee Kamentsky
 * @author Curtis Rueden
 */
@Plugin(type = ImageDisplay.class)
public class DefaultImageDisplay extends AbstractDisplay<DataView>
	implements ImageDisplay, SupportsUndo
{
	private List<EventSubscriber<?>> subscribers;

	/** Data structure that aggregates dimensional axes from constituent views. */
	private final CombinedInterval combinedInterval = new CombinedInterval();

	private AxisType activeAxis = null;

	final private ImageCanvas canvas;

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
		canvas = new DefaultImageCanvas(this);
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
		if (!combinedInterval.isDiscrete()) {
			throw new IllegalStateException("Invalid combination of views");
		}

		// rebuild views
		for (final DataView view : this) {
			view.rebuild();
		}

		// remove obsolete axes
		for (final AxisType axis : pos.keySet()) {
			if (getAxisIndex(axis) < 0) {
				pos.remove(axis);
			}
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
		
		if (getActiveAxis() == null) initActiveAxis();
	}

	// TODO - Eventually this should capture all of an ImageDisplay's state. As a
	// proof of concept only Dataset state is captured now. The UndoService falls
	// back to recording complete display state by calling this when the running
	// command does not provide any undo information. This can be inefficient. The
	// key to avoiding speed and size issues with undo is for each command to
	// implement the InvertibleCommand interface that when run restores the
	// original data correctly.
	/**
	 * Designed to fully capture a display's state for later restore purposes.
	 */
	@Override
	public DisplayState captureState() {
		ImageDisplayService idSrv = getContext().getService(ImageDisplayService.class);
		UndoService undoSrv = getContext().getService(UndoService.class);
		Dataset ds = idSrv.getActiveDataset(this);
		PointSet points = new HyperVolumePointSet(ds.getDims());
		Img<DoubleType> data = undoSrv.captureData(ds,points);
		return new ImageDisplayState(ds, points, data);
	}

	// TODO - Eventually this should restore all of an ImageDisplay's state. As a
	// proof of concept only Dataset state is restored now. This is inadequate.
	/**
	 * Designed to fully restore a display's state to an earlier state.
	 */
	@Override
	public void restoreState(DisplayState dispState) {
		if (!(dispState instanceof ImageDisplayState))
			throw new IllegalArgumentException("given wrong kind of DisplayState");
		ImageDisplayState state = (ImageDisplayState) dispState;
		UndoService undoSrv = getContext().getService(UndoService.class);
		undoSrv.restoreData(state.ds, state.points, state.data);
	}
	
	private class ImageDisplayState implements DisplayState {
		
		private final Dataset ds;
		private final PointSet points;
		private final Img<DoubleType> data;
		
		public ImageDisplayState(Dataset ds, PointSet points, Img<DoubleType> data) {
			this.ds = ds;
			this.points = points;
			this.data = data;
		}
		
		@Override
		public long getMemoryUsage() {
			return 8 * data.dimension(0);
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

	@Override
	public ImageCanvas getCanvas() {
		return canvas;
	}

	@Override
	public RealRect getPlaneExtents() {
		final Extents extents = getExtents();
		final int xAxis = getAxisIndex(Axes.X);
		final int yAxis = getAxisIndex(Axes.Y);
		final double xMin = extents.realMin(xAxis);
		final double yMin = extents.realMin(yAxis);
		final double width = extents.realMax(xAxis) - extents.realMin(xAxis);
		final double height = extents.realMax(yAxis) - extents.realMin(yAxis);
		return new RealRect(xMin, yMin, width, height);
	}

	// -- Display methods --

	@Override
	public boolean canDisplay(final Class<?> c) {
		return Data.class.isAssignableFrom(c) || super.canDisplay(c);
	}

	@Override
	public void display(final Object o) {
		if (o instanceof DataView) {
			// object is a data view, natively compatible with this display
			final DataView dataView = (DataView) o;
			super.display(dataView);
			updateName(dataView);
			rebuild();
		}
		else if (o instanceof Data) {
			// object is a data object, which we can wrap in a data view
			final Data data = (Data) o;
			final ImageDisplayService imageDisplayService =
					getContext().getService(ImageDisplayService.class);
			if (imageDisplayService == null) {
				throw new IllegalStateException(
						"An ImageDisplayService is required to display Data objects");
			}
			final DataView dataView = imageDisplayService.createDataView(data);
			add(dataView);
			updateName(dataView);
			rebuild();
		}
		else {
			throw new IllegalArgumentException("Incompatible object: " + o + " [" +
				o.getClass().getName() + "]");
		}
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
	public void calibration(float[] cal) {
		combinedInterval.calibration(cal);
	}

	@Override
	public void setCalibration(final double cal, final int d) {
		combinedInterval.setCalibration(cal, d);
	}

	@Override
	public void setCalibration(double[] cal) {
		combinedInterval.setCalibration(cal);
	}

	@Override
	public void setCalibration(float[] cal) {
		combinedInterval.setCalibration(cal);
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
		final EventService eventService =
			getContext().getService(EventService.class);
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

	// -- Contextual methods --

	@Override
	public void setContext(final ImageJ context) {
		super.setContext(context);
		assert subscribers == null;
		final EventService eventService =
			getContext().getService(EventService.class);
		if (eventService != null) {
			subscribers = eventService.subscribe(this);
		}
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
	 * 
	 * @param proposedName
	 * @return the name with stuff added to make it unique
	 */
	private String createName(final String proposedName) {
		final DisplayService displayService =
			getContext().getService(DisplayService.class);
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
			final AxisType[] axes = getAxes();
			for (final AxisType axis : axes) {
				if (axis == Axes.X) continue;
				if (axis == Axes.Y) continue;
				setActiveAxis(axis);
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
		// NB - this is necessary to make sure resources get returned via GC.
		// Else there is a memory leak.
		final EventService eventService =
			getContext().getService(EventService.class);
		if (eventService != null) eventService.unsubscribe(subscribers);
	}

}
