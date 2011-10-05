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

import imagej.data.Data;
import imagej.data.Dataset;
import imagej.data.Extents;
import imagej.data.Position;
import imagej.data.display.event.AxisPositionEvent;
import imagej.data.display.event.ZoomEvent;
import imagej.data.event.DataRestructuredEvent;
import imagej.data.event.DataUpdatedEvent;
import imagej.data.event.DatasetUpdatedEvent;
import imagej.data.roi.Overlay;
import imagej.event.EventSubscriber;
import imagej.ext.display.AbstractDisplay;
import imagej.ext.display.event.DisplayDeletedEvent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

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

	/** List of event subscribers, to avoid garbage collection. */
	private final List<EventSubscriber<?>> subscribers =
		new ArrayList<EventSubscriber<?>>();

	private EventSubscriber<ZoomEvent> zoomSubscriber;
	private EventSubscriber<DatasetUpdatedEvent> updateSubscriber;
	private EventSubscriber<AxisPositionEvent> axisMoveSubscriber;
	private EventSubscriber<DisplayDeletedEvent> displayDeletedSubscriber;
	
	private Axis activeAxis = Axes.Z;

	public AbstractImageDisplay() {
		super(DataView.class);
		subscribeToEvents();
	}

	// -- ImageDisplay methods --

	@Override
	public DataView getActiveView() {
		return size() > 0 ? get(0) : null;
	}

	@Override
	public void removeView(final DataView view) {
		remove(view);
		view.dispose();
		redoWindowLayout();
		update();
	}

	@Override
	public Axis getActiveAxis() {
		return activeAxis;
	}

	@Override
	public void setActiveAxis(final Axis axis) {
		activeAxis = axis;
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
		for (final DataView view : this) {
			for (final Axis axis : getAxes()) {
				final int index = getAxisIndex(axis);
				if (index >= 0) {
					view.setPosition(getPanel().getAxisPosition(axis), index);
				}
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

	// -- Helper methods --

	/** Updates the display when the linked object changes. */
	private void subscribeToEvents() {
		final EventSubscriber<DataUpdatedEvent> dataUpdatedSubscriber =
			new EventSubscriber<DataUpdatedEvent>() {

				@Override
				public void onEvent(final DataUpdatedEvent event) {
					for (final DataView view : AbstractImageDisplay.this) {
						if (event.getObject() == view.getData()) {
							view.update();
							update();
							return;
						}
					}
				}
			};
		eventService.subscribe(DataUpdatedEvent.class, dataUpdatedSubscriber);
		subscribers.add(dataUpdatedSubscriber);

		final EventSubscriber<DataRestructuredEvent> dataRestructedSubscriber =
			new EventSubscriber<DataRestructuredEvent>() {

				@Override
				public void onEvent(final DataRestructuredEvent event) {
					for (final DataView view : AbstractImageDisplay.this) {
						if (event.getObject() == view.getData()) {
							view.rebuild();
							update();
							return;
						}
					}
				}
			};
		eventService.subscribe(DataRestructuredEvent.class, dataRestructedSubscriber);
		subscribers.add(dataRestructedSubscriber);
		
		zoomSubscriber = new EventSubscriber<ZoomEvent>() {

			@Override
			public void onEvent(final ZoomEvent event) {
				if (event.getCanvas() != getCanvas()) return;
				getPanel().setLabel(makeLabel());
			}
		};
		eventService.subscribe(ZoomEvent.class, zoomSubscriber);

		updateSubscriber = new EventSubscriber<DatasetUpdatedEvent>() {

			@Override
			public void onEvent(final DatasetUpdatedEvent event) {
				final DataView view = getActiveView();
				if (view == null) return;
				final Dataset ds = getDataset(view);
				if (event.getObject() != ds) return;
				getPanel().setLabel(makeLabel());
			}
		};
		eventService.subscribe(DatasetUpdatedEvent.class, updateSubscriber);

		axisMoveSubscriber = new EventSubscriber<AxisPositionEvent>() {

			@Override
			public void onEvent(final AxisPositionEvent event) {
				if (event.getDisplay() == AbstractImageDisplay.this) {
					final Axis axis = event.getAxis();
					final long value = event.getValue();
					long newPos = value;
					if (event.isRelative()) {
						final long currPos = getPanel().getAxisPosition(axis);
						newPos = currPos + value;
					}
					final long max = event.getMax();
					if (newPos >= 0 && newPos < max) {
						getPanel().setAxisPosition(axis, newPos);
						update();
					}
				}
			}
		};
		eventService.subscribe(AxisPositionEvent.class, axisMoveSubscriber);

		displayDeletedSubscriber = new EventSubscriber<DisplayDeletedEvent>() {

			@Override
			public void onEvent(final DisplayDeletedEvent event) {
				if (event.getObject() == AbstractImageDisplay.this) {
					closeHelper();
					// NB - we've avoided dispose() since its been called elsewhere.
					// If call close() here instead get duplicated WindowClosingEvents.
				}
			}
		};
		eventService.subscribe(DisplayDeletedEvent.class, displayDeletedSubscriber);
	}

	// NB - this method necessary to make sure resources get returned via GC.
	// Else there is a memory leak.
	private void unsubscribeFromEvents() {
		eventService.unsubscribe(ZoomEvent.class, zoomSubscriber);
		// eventService.unsubscribe(DatasetRestructuredEvent.class,
		// dataRestructedSubscriber);
		eventService.unsubscribe(DatasetUpdatedEvent.class, updateSubscriber);
		eventService.unsubscribe(AxisPositionEvent.class, axisMoveSubscriber);
		eventService.unsubscribe(DisplayDeletedEvent.class, displayDeletedSubscriber);
	}

	protected void closeHelper() {
		unsubscribeFromEvents();
	}

	@Override
	public void close() {
		closeHelper();
		getPanel().getWindow().close();
	}

	protected String makeLabel() {
		// CTR TODO - Fix window label to show beyond just the active view.
		final DataView view = getActiveView();
		final Dataset dataset = getDataset(view);

		final int xIndex = dataset.getAxisIndex(Axes.X);
		final int yIndex = dataset.getAxisIndex(Axes.Y);
		final long[] dims = dataset.getDims();
		final Axis[] axes = dataset.getAxes();
		final Position pos = view.getPlanePosition();

		final StringBuilder sb = new StringBuilder();
		for (int i = 0, p = -1; i < dims.length; i++) {
			if (Axes.isXY(axes[i])) continue;
			p++;
			if (dims[i] == 1) continue;
			sb.append(axes[i] + ": " + (pos.getLongPosition(p) + 1) + "/" + dims[i] +
				"; ");
		}
		
		sb.append(dims[xIndex] + "x" + dims[yIndex] + "; ");
		
		sb.append(dataset.getTypeLabelLong());
		
		final double zoomPercent = getCanvas().getZoomFactor() * 100;
		if (zoomPercent == 100) {
			// do nothing
		}
		else { // some kind of magnification being done
			if (zoomPercent > 100 && zoomPercent - Math.floor(zoomPercent) < 0.00001) {
				sb.append(String.format(" [%d%%]", (int)zoomPercent));
			}
			else {
				sb.append(String.format(" [%.2f%%]", zoomPercent));
			}
		}
		
		return sb.toString();
	}

	protected Dataset getDataset(final DataView view) {
		final Data dataObject = view.getData();
		return dataObject instanceof Dataset ? (Dataset) dataObject : null;
	}

}
