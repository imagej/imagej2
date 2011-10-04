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
import imagej.data.event.DataRestructuredEvent;
import imagej.data.event.DataUpdatedEvent;
import imagej.data.roi.Overlay;
import imagej.event.EventSubscriber;
import imagej.ext.display.AbstractDisplay;

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
		if (o instanceof Dataset) display((Dataset) o);
		else if (o instanceof Overlay) display((Overlay) o);
		else super.display(o);
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
		final EventSubscriber<DataUpdatedEvent> updateSubscriber =
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
		eventService.subscribe(DataUpdatedEvent.class, updateSubscriber);
		subscribers.add(updateSubscriber);

		final EventSubscriber<DataRestructuredEvent> restructureSubscriber =
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
		eventService.subscribe(DataRestructuredEvent.class, restructureSubscriber);
		subscribers.add(restructureSubscriber);
	}

}
