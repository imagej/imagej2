//
// AbstractImageDisplay.java
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
import imagej.data.Dataset;
import imagej.data.Extents;
import imagej.data.display.event.AxisPositionEvent;
import imagej.data.display.event.ZoomEvent;
import imagej.data.event.DataRestructuredEvent;
import imagej.data.event.DataUpdatedEvent;
import imagej.data.event.DatasetRestructuredEvent;
import imagej.data.event.DatasetUpdatedEvent;
import imagej.data.roi.Overlay;
import imagej.event.EventHandler;
import imagej.event.EventSubscriber;
import imagej.ext.display.AbstractDisplay;
import imagej.ext.display.event.DisplayDeletedEvent;
import imagej.ext.display.event.window.WinActivatedEvent;
import imagej.ext.tool.ToolService;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import net.imglib2.img.Axes;
import net.imglib2.img.Axis;

/**
 * TODO - better Javadoc. The abstract display handles axes resolution,
 * maintaining the dimensionality of the EuclideanSpace represented by the
 * display.
 * 
 * @author Lee Kamentsky
 * @author Curtis Rueden
 */
public abstract class AbstractImageDisplay extends AbstractDisplay<DataView>
	implements ImageDisplay
{

	private ImageCanvas canvas;

	private List<EventSubscriber<?>> subscribers;

	private Axis activeAxis = null;

	// NB: If axisPositions is a HashMap rather than a ConcurrentHashMap,
	// the Delete Axis plugin throws a ConcurrentModificationException.

	private final Map<Axis, Long> axisPositions =
		new ConcurrentHashMap<Axis, Long>();

	public AbstractImageDisplay() {
		super(DataView.class);
		subscribers = eventService.subscribe(this);
	}

	// -- AbstractImageDisplay methods --

	protected void setCanvas(final ImageCanvas canvas) {
		this.canvas = canvas;
	}

	protected void initActiveAxis() {
		if (activeAxis == null) {
			final Axis[] axes = getAxes();
			for (final Axis axis : axes) {
				if (axis == Axes.X) continue;
				if (axis == Axes.Y) continue;
				setActiveAxis(axis);
				return;
			}
		}
	}

	// -- AbstractDisplay methods --

	@Override
	protected void rebuild() {
		final long[] min = new long[numDimensions()];
		Arrays.fill(min, Long.MAX_VALUE);
		final long[] max = new long[numDimensions()];
		Arrays.fill(max, Long.MIN_VALUE);

		final Axis[] axes = getAxes();
		final Extents extents = getExtents();

		// remove obsolete axes
		for (final Axis axis : axisPositions.keySet()) {
			if (getAxisIndex(axis) >= 0) continue; // axis still active
			axisPositions.remove(axis);
		}

		// add new axes
		for (int i = 0; i < axes.length; i++) {
			final Axis axis = axes[i];
			if (axisPositions.containsKey(axis)) continue; // axis already exists
			if (Axes.isXY(axis)) continue; // do not track position of planar axes
			setAxisPosition(axis, extents.min(i)); // start at minimum value
		}

		// rebuild panel
		getPanel().redoLayout();
	}

	// -- ImageDisplay methods --

	@Override
	public DataView getActiveView() {
		return size() > 0 ? get(0) : null;
	}

	@Override
	public Axis getActiveAxis() {
		return activeAxis;
	}

	@Override
	public void setActiveAxis(final Axis axis) {
		if (!axisPositions.containsKey(axis)) {
			throw new IllegalArgumentException("Unknown axis: " + axis);
		}
		activeAxis = axis;
	}

	@Override
	public long getAxisPosition(final Axis axis) {
		if (axisPositions.containsKey(axis)) {
			return axisPositions.get(axis);
		}
		return 0; // untracked axes are all at position 0 by default
	}

	@Override
	public void setAxisPosition(final Axis axis, final long position) {
		final int axisIndex = getAxisIndex(axis);
		if (axisIndex < 0) {
			throw new IllegalArgumentException("Invalid axis: " + axis);
		}

		// clamp new position value to [min, max]
		final Extents extents = getExtents();
		final long min = extents.min(axisIndex);
		final long max = extents.max(axisIndex);
		long pos = position;
		if (pos < min) pos = min;
		if (pos > max) pos = max;

		// update position and notify interested parties of the change
		axisPositions.put(axis, pos);
		eventService.publish(new AxisPositionEvent(this, axis));
	}

	@Override
	public ImageCanvas getCanvas() {
		return canvas;
	}

	@Override
	public boolean containsData(final Data data) {
		for (final DataView view : this) {
			if (data == view.getData()) return true;
		}
		return false;
	}

	// -- Display methods --

	@Override
	public boolean canDisplay(final Class<?> c) {
		return Data.class.isAssignableFrom(c) || super.canDisplay(c);
	}

	@Override
	public void display(final Object o) {
		// CTR FIXME
		if (o instanceof Dataset) display((Dataset) o);
		else if (o instanceof Overlay) display((Overlay) o);
		else super.display(o);
	}

	@Override
	public void update() {
		super.update();
		for (final DataView view : this) {
			for (final Axis axis : getAxes()) {
				final int index = getAxisIndex(axis);
				if (index < 0) continue;
				if (Axes.isXY(axis)) continue;
				view.setPosition(getAxisPosition(axis), index);
			}
			view.update();
		}
		getPanel().setLabel(makeLabel());
	}

	// -- LabeledSpace methods --

	@Override
	public long[] getDims() {
		// This logic scans the axes of all constituent data objects, and merges
		// them into a single aggregate coordinate space. The current implementation
		// is not performance optimized.

		// CTR TODO - reconcile multiple copies of same axis with different lengths.

		final ArrayList<Long> dimsList = new ArrayList<Long>();
		final HashSet<Axis> axes = new HashSet<Axis>();
		for (final DataView view : this) {
			final Data data = view.getData();
			final long[] dataDims = data.getDims();
			for (int i = 0; i < dataDims.length; i++) {
				final Axis axis = data.axis(i);
				if (!axes.contains(axis)) {
					axes.add(axis);
					dimsList.add(dataDims[i]);
				}
			}
		}
		final long[] dims = new long[dimsList.size()];
		for (int i = 0; i < dims.length; i++) {
			dims[i] = dimsList.get(i);
		}
		return dims;
	}

	@Override
	public Axis[] getAxes() {
		// This logic scans the axes of all constituent data objects, and merges
		// them into a single aggregate coordinate space. The current implementation
		// is not performance optimized.

		// CTR TODO - reconcile multiple copies of same axis with different lengths.

		final ArrayList<Axis> axes = new ArrayList<Axis>();
		for (final DataView view : this) {
			final Data data = view.getData();
			final int nAxes = data.numDimensions();
			for (int i = 0; i < nAxes; i++) {
				final Axis axis = data.axis(i);
				if (!axes.contains(axis)) {
					axes.add(axis);
				}
			}
		}
		return axes.toArray(new Axis[0]);
	}

	@Override
	public Extents getExtents() {
		return new Extents(getDims());
	}

	// -- EuclideanSpace methods --

	@Override
	public int numDimensions() {
		return getAxes().length;
	}

	// -- LabeledAxes methods --

	@Override
	public int getAxisIndex(final Axis axis) {
		final Axis[] axes = getAxes();
		for (int i = 0; i < axes.length; i++) {
			if (axes[i] == axis) return i;
		}
		return -1;
	}

	@Override
	public Axis axis(final int d) {
		// TODO - avoid array allocation
		return getAxes()[d];
	}

	@Override
	public void axes(final Axis[] axes) {
		System.arraycopy(getAxes(), 0, axes, 0, axes.length);
	}

	@Override
	public void setAxis(final Axis axis, final int d) {
		throw new UnsupportedOperationException(
			"You can't change the axes of a display");
	}

	@Override
	public double calibration(final int d) {
		// The display is calibrated in the base unit
		return 1.0;
	}

	@Override
	public void calibration(final double[] cal) {
		Arrays.fill(cal, 1.0);
	}

	@Override
	public void setCalibration(final double cal, final int d) {
		throw new UnsupportedOperationException(
			"You can't change the calibration of a display yet");
	}

	// -- Event handlers --

	@EventHandler
	public void onEvent(final DataRestructuredEvent event) {
		for (final DataView view : this) {
			if (event.getObject() == view.getData()) {
				view.rebuild();
				update();
				return;
			}
		}
	}

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
	protected void onEvent(final DatasetRestructuredEvent event) {
		// NOTE - this code used to just note that a rebuild was necessary
		// and had the rebuild done in update(). But due to timing of
		// events it is possible to get the update() before this call.
		// So make this do a rebuild. In some cases update() will be
		// called twice. Not sure if avoiding this was the reason we used
		// to just record and do work in update. Or if that code was to
		// avoid some other bug. Changing on 8-18-11. Fixed bug #627
		// and bug #605. BDZ
		final Dataset dataset = event.getObject();
		for (final DataView view : this) {
			if (dataset == view.getData()) {
				// BDZ - calls to imgCanvas.setZoom(0) followed by
				// imgCanvas.panReset() removed from here to fix bug #797.
				rebuild();
				update();
				return;
			}
		}
	}

	@EventHandler
	protected void onEvent(final DatasetUpdatedEvent event) {
		final DataView view = getActiveView();
		if (view == null) return;
		final Dataset ds = getDataset(view);
		if (event.getObject() != ds) return;
		getPanel().setLabel(makeLabel());
	}

	@EventHandler
	protected void onEvent(final DisplayDeletedEvent event) {
		if (event.getObject() == this) {
			closeHelper();
			// NB - we've avoided dispose() since its been called elsewhere.
			// If call close() here instead get duplicated WindowClosingEvents.
		}
	}

	@EventHandler
	protected void onEvent(final WinActivatedEvent event) {
		if (event.getDisplay() != this) return;
		// final UserInterface ui = ImageJ.get(UIService.class).getUI();
		// final ToolService toolMgr = ui.getToolBar().getToolService();
		final ToolService toolService = ImageJ.get(ToolService.class);
		getCanvas().setCursor(toolService.getActiveTool().getCursor());
	}

	@EventHandler
	protected void onEvent(final ZoomEvent event) {
		if (event.getCanvas() != getCanvas()) return;
		getPanel().setLabel(makeLabel());
	}

	// -- Helper methods --

	// NB - this method necessary to make sure resources get returned via GC.
	// Else there is a memory leak.
	private void unsubscribeFromEvents() {
		eventService.unsubscribe(subscribers);
	}

	protected void closeHelper() {
		unsubscribeFromEvents();
	}

	@Override
	public void close() {
		closeHelper();
		getPanel().getWindow().close();
	}

	protected Dataset getDataset(final DataView view) {
		final Data data = view.getData();
		return data instanceof Dataset ? (Dataset) data : null;
	}

}
