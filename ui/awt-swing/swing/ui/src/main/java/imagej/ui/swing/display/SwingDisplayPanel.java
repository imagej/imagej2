//
// SwingDisplayPanel.java
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

package imagej.ui.swing.display;

import imagej.data.DataObject;
import imagej.data.Dataset;
import imagej.data.Position;
import imagej.data.event.DatasetUpdatedEvent;
import imagej.data.roi.Overlay;
import imagej.display.DisplayView;
import imagej.display.DisplayWindow;
import imagej.display.EventDispatcher;
import imagej.display.ImageDisplay;
import imagej.display.event.AxisPositionEvent;
import imagej.display.event.DisplayDeletedEvent;
import imagej.display.event.ZoomEvent;
import imagej.event.EventSubscriber;
import imagej.event.Events;
import imagej.ui.common.awt.AWTKeyEventDispatcher;
import imagej.ui.common.awt.AWTMouseEventDispatcher;
import imagej.ui.swing.display.sdi.SwingDisplayWindow;

import java.awt.Adjustable;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

import net.imglib2.img.Axes;
import net.imglib2.img.Axis;
import net.imglib2.roi.RegionOfInterest;
import net.miginfocom.swing.MigLayout;

/**
 * Swing implementation of display window.
 * 
 * @author Curtis Rueden
 * @author Grant Harris
 * @author Barry DeZonia
 */
public class SwingDisplayPanel extends AbstractSwingDisplayPanel {

	private final ImageDisplay display;
	private final JLabel imageLabel;
	private final JPanel sliders;
	private final DisplayWindow window;

	protected final Map<Axis, Integer> axisPositions =
		new HashMap<Axis, Integer>();

	private final Map<Axis, JScrollBar> axisSliders =
		new HashMap<Axis, JScrollBar>();
	private final Map<Axis, JLabel> axisLabels = new HashMap<Axis, JLabel>();

	private EventSubscriber<ZoomEvent> zoomSubscriber;
	// private EventSubscriber<DatasetRestructuredEvent> restructureSubscriber;
	private EventSubscriber<DatasetUpdatedEvent> updateSubscriber;
	private EventSubscriber<AxisPositionEvent> axisMoveSubscriber;
	private EventSubscriber<DisplayDeletedEvent> displayDeletedSubscriber;

	public SwingDisplayPanel(final ImageDisplay display,
		final DisplayWindow window)
	{
		this.display = display;
		this.window = window;

		imageLabel = new JLabel(" ");
		final int prefHeight = imageLabel.getPreferredSize().height;
		imageLabel.setPreferredSize(new Dimension(0, prefHeight));

		final JPanel graphicPane = new JPanel();
		graphicPane.setLayout(new MigLayout("ins 0", "fill,grow", "fill,grow"));
		graphicPane.add((JHotDrawImageCanvas) display.getImageCanvas());

		sliders = new JPanel();
		sliders.setLayout(new MigLayout("fillx,wrap 2", "[right|fill,grow]"));

		setLayout(new BorderLayout());
		setBorder(new EmptyBorder(3, 3, 3, 3));

		add(imageLabel, BorderLayout.NORTH);
		add(graphicPane, BorderLayout.CENTER);
		add(sliders, BorderLayout.SOUTH);

		subscribeToEvents();
		window.setContent(this);
	}

	/**
	 * Get the position of some axis other than X and Y
	 * 
	 * @param axis - the axis
	 * @return the position of that axis on the sliders
	 */
	@Override
	public long getAxisPosition(final Axis axis) {
		if (axisPositions.containsKey(axis)) {
			return axisPositions.get(axis);
		}
		return 0;
	}

	// TODO - position might be better as a long
	@Override
	public void setAxisPosition(final Axis axis, final int position) {
		axisPositions.put(axis, position);
	}

	// -- DisplayWindow methods --
	@Override
	public ImageDisplay getDisplay() {
		return display;
	}

	@Override
	public void makeActive() {
		window.requestFocus();
	}

	@Override
	public void update() {
		final List<Axis> dispAxes = display.getAxes();
		for (final DisplayView view : display.getViews()) {
			for (final Axis axis : axisPositions.keySet()) {
				final int index = dispAxes.indexOf(axis);
				if (index >= 0) {
					view.setPosition(axisPositions.get(axis), index);
				}
			}
			view.update();
		}
		setLabel(makeLabel());
	}

	@Override
	public void redoLayout() {
		EventQueue.invokeLater(new Runnable() {

			@SuppressWarnings("synthetic-access")
			@Override
			public void run() {
				createSliders();
				sliders.setVisible(sliders.getComponentCount() > 0);
				window.setTitle(getDisplay().getName());
				window.pack();
				window.showDisplay(true);
			}
		});
	}

	@Override
	public void setLabel(final String s) {
		imageLabel.setText(s);
	}

	@Override
	public void addEventDispatcher(final EventDispatcher dispatcher) {
		if (dispatcher instanceof AWTKeyEventDispatcher) {
			addKeyListener((AWTKeyEventDispatcher) dispatcher);
		}
		if (dispatcher instanceof AWTMouseEventDispatcher) {
			addMouseListener((AWTMouseEventDispatcher) dispatcher);
			addMouseMotionListener((AWTMouseEventDispatcher) dispatcher);
			addMouseWheelListener((AWTMouseEventDispatcher) dispatcher);
		}
	}

	// -- Helper methods --

	@SuppressWarnings("synthetic-access")
	private void subscribeToEvents() {

		zoomSubscriber = new EventSubscriber<ZoomEvent>() {

			@Override
			public void onEvent(final ZoomEvent event) {
				if (event.getCanvas() != getDisplay().getImageCanvas()) return;
				setLabel(makeLabel());
			}
		};
		Events.subscribe(ZoomEvent.class, zoomSubscriber);

		/* NB - BDZ - 7-29-11
		 * This code may no longer be necessary. There were race conditions where
		 * createSliders() was getting called twice from two different places in
		 * this class at the same time resulting in exceptions. Simply deleting
		 * the channel axis from Organ Of Corti would initiate it. redoLayout()
		 * must be getting called more appropriately now and this event might be
		 * able to be ignored. If it cannot be ignored then making createSliders()
		 * synchronized will fix the bug too. Leave code here for now for easy
		 * restoration.
		restructureSubscriber =
			new EventSubscriber<DatasetRestructuredEvent>() {

				@Override
				public void onEvent(DatasetRestructuredEvent event) {
					for (DisplayView view : getDisplay().getViews()) {
						if (event.getObject() == view.getDataObject()) {
							createSliders();
							return;
						}
					}
				}
			};
		Events.subscribe(DatasetRestructuredEvent.class, restructureSubscriber);
		*/

		updateSubscriber = new EventSubscriber<DatasetUpdatedEvent>() {

			@Override
			public void onEvent(final DatasetUpdatedEvent event) {
				final DisplayView view = getDisplay().getActiveView();
				final Dataset ds = getDataset(view);
				if (event.getObject() != ds) return;
				setLabel(makeLabel());
			}
		};
		Events.subscribe(DatasetUpdatedEvent.class, updateSubscriber);

		axisMoveSubscriber = new EventSubscriber<AxisPositionEvent>() {

			@Override
			public void onEvent(final AxisPositionEvent event) {
				if (event.getDisplay() == display) {
					final Axis axis = event.getAxis();
					final long value = event.getValue();
					long newPos = value;
					if (event.isRelative()) {
						final long currPos = getAxisPosition(axis);
						newPos = currPos + value;
					}
					final long max = event.getMax();
					if ((newPos >= 0) && (newPos < max)) {
						setAxisPosition(axis, (int) newPos); // TODO eliminate cast
						final long pos = getAxisPosition(axis);
						final JScrollBar scrollBar = axisSliders.get(axis);
						scrollBar.setValue((int) pos);
						update();
					}
				}
			}
		};
		Events.subscribe(AxisPositionEvent.class, axisMoveSubscriber);

		displayDeletedSubscriber = new EventSubscriber<DisplayDeletedEvent>() {

			@Override
			public void onEvent(final DisplayDeletedEvent event) {
				if (event.getObject() == display) {
					closeHelper();
					// NB - we've avoided dispose() since its been called elsewhere.
					// If call close() here instead get duplicated WindowClosingEvents.
				}
			}
		};
		Events.subscribe(DisplayDeletedEvent.class, displayDeletedSubscriber);
	}

	// NB - this method necessary to make sure resources get returned via GC.
	// Else there is a memory leak.
	private void unsubscribeFromEvents() {
		Events.unsubscribe(ZoomEvent.class, zoomSubscriber);
		// Events.unsubscribe(DatasetRestructuredEvent.class,
		// restructureSubscriber);
		Events.unsubscribe(DatasetUpdatedEvent.class, updateSubscriber);
		Events.unsubscribe(AxisPositionEvent.class, axisMoveSubscriber);
		Events.unsubscribe(DisplayDeletedEvent.class, displayDeletedSubscriber);
	}

	private/*synchronized*/void createSliders() {
		final long[] min = new long[display.numDimensions()];
		Arrays.fill(min, Long.MAX_VALUE);
		final long[] max = new long[display.numDimensions()];
		Arrays.fill(max, Long.MIN_VALUE);
		// final Axis[] axes = new Axis[display.numDimensions()];
		// display.axes(axes);
		final List<Axis> dispAxes = display.getAxes();
		/*
		 * Run through all of the views and determine the extents of each.
		 * 
		 * NB: Images can have minimum spatial extents less than zero,
		 *     for instance, some sort of bounded function that somehow
		 *     became an image in a dataset. So the dataset should have
		 *     something more than dimensions.
		 *     
		 *     For something like time or Z, this could be kind of cool:
		 *     my thing's time dimension goes from last Tuesday to Friday.
		 */
		for (final DisplayView v : display.getViews()) {
			final DataObject o = v.getDataObject();
			if (o instanceof Dataset) {
				final Dataset ds = (Dataset) o;
				final long[] dims = ds.getDims();
				for (int i = 0; i < dispAxes.size(); i++) {
					final int index = ds.getAxisIndex(dispAxes.get(i));
					if (index >= 0) {
						min[i] = Math.min(min[i], 0);
						max[i] = Math.max(max[i], dims[index]);
					}
				}
			}
			else if (o instanceof Overlay) {
				final Overlay overlay = (Overlay) o;
				final RegionOfInterest roi = overlay.getRegionOfInterest();
				if (roi != null) {
					for (int i = 0; i < dispAxes.size(); i++) {
						final int index = overlay.getAxisIndex(dispAxes.get(i));
						if ((index >= 0) && (index < roi.numDimensions())) {
							min[i] = Math.min(min[i], (long) Math.ceil(roi.realMin(index)));
							max[i] = Math.max(max[i], (long) Math.floor(roi.realMax(index)));
						}
					}
				}
			}
		}

		for (final Axis axis : axisSliders.keySet()) {
			if (dispAxes.indexOf(axis) < 0) {
				sliders.remove(axisSliders.get(axis));
				sliders.remove(axisLabels.get(axis));
				axisSliders.remove(axis);
				axisLabels.remove(axis);
				axisPositions.remove(axis);
			}

			// if a Dataset had planes deleted this will eventually get called.
			// if thats the case the slider might exist but its allowable range
			// has changed. check that we have correct range.
			final JScrollBar slider = axisSliders.get(axis);
			if (slider != null) {
				for (int i = 0; i < dispAxes.size(); i++) {
					if (axis == dispAxes.get(i)) {
						if ((slider.getMinimum() != min[i]) ||
							(slider.getMaximum() != max[i]))
						{
							if (slider.getValue() > max[i]) slider.setValue((int) max[i]);
							slider.setMinimum((int) min[i]);
							slider.setMaximum((int) max[i]);
						}
					}
				}
			}
		}

		for (int i = 0; i < dispAxes.size(); i++) {
			final Axis axis = dispAxes.get(i);
			if (axisSliders.containsKey(axis)) continue;
			if (Axes.isXY(axis)) continue;
			if (min[i] >= max[i] - 1) continue;

			setAxisPosition(axis, (int) min[i]);
			final JLabel label = new JLabel(axis.getLabel());
			axisLabels.put(axis, label);
			label.setHorizontalAlignment(SwingConstants.RIGHT);
			final JScrollBar slider =
				new JScrollBar(Adjustable.HORIZONTAL, (int) min[i], 1, (int) min[i],
					(int) max[i]);
			axisSliders.put(axis, slider);
			slider.addAdjustmentListener(new AdjustmentListener() {

				@Override
				public void adjustmentValueChanged(final AdjustmentEvent e) {
					final int position = slider.getValue();
					axisPositions.put(axis, position);
					update();
				}
			});
			sliders.add(label);
			sliders.add(slider);
		}
	}

	private String makeLabel() {
		// CTR TODO - Fix window label to show beyond just the active view.
		final DisplayView view = display.getActiveView();
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
		final double zoomPercent =
			getDisplay().getImageCanvas().getZoomFactor() * 100.0;
		if (Math.abs(100.0 - zoomPercent) >= 0.01) sb.append(String.format(
			" [%.2f%%]", zoomPercent));
		return sb.toString();
	}

	private Dataset getDataset(final DisplayView view) {
		final DataObject dataObject = view.getDataObject();
		return dataObject instanceof Dataset ? (Dataset) dataObject : null;
	}

	private void closeHelper() {
		this.getParent().setVisible(false);
		unsubscribeFromEvents();
	}

	@Override
	public void close() {
		closeHelper();
		((SwingDisplayWindow) this.getParent()).dispose();
	}

}
