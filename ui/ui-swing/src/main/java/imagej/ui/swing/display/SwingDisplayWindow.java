//
// SwingDisplayWindow.java
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

import imagej.awt.AWTDisplayWindow;
import imagej.awt.AWTEventDispatcher;
import imagej.data.DataObject;
import imagej.data.Dataset;
import imagej.data.event.DatasetRestructuredEvent;
import imagej.display.DisplayView;
import imagej.display.EventDispatcher;
import imagej.display.event.ZoomEvent;
import imagej.event.EventSubscriber;
import imagej.event.Events;

import java.awt.Adjustable;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.util.ArrayList;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.SwingConstants;
import javax.swing.WindowConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

import net.imglib2.img.Axes;
import net.imglib2.img.Axis;
import net.miginfocom.swing.MigLayout;

/**
 * Swing implementation of display window.
 * 
 * @author Curtis Rueden
 * @author Grant Harris
 * @author Barry DeZonia
 */
public class SwingDisplayWindow extends JFrame implements AWTDisplayWindow {

	// TODO - Rework internal logic to use a JPanel, not a JFrame.
	// In this way, the GUI components could be used in other contexts.

	private final SwingImageDisplay display;
	private final JLabel imageLabel;
	private final JPanel sliders;
	private ArrayList<EventSubscriber<?>> subscribers;

	public SwingDisplayWindow(final SwingImageDisplay display) {
		this.display = display;

		imageLabel = new JLabel(" ");
		final int prefHeight = imageLabel.getPreferredSize().height;
		imageLabel.setPreferredSize(new Dimension(0, prefHeight));

		final JPanel graphicPane = new JPanel();
		graphicPane.setLayout(new MigLayout("ins 0", "fill,grow", "fill,grow"));
		graphicPane.setBorder(new LineBorder(Color.black));
		graphicPane.add(display.getImageCanvas());

		sliders = new JPanel();
		sliders.setLayout(new MigLayout("fillx,wrap 2", "[right|fill,grow]"));

		final JPanel pane = new JPanel();
		pane.setLayout(new BorderLayout());
		pane.setBorder(new EmptyBorder(3, 3, 3, 3));
		setContentPane(pane);

		pane.add(imageLabel, BorderLayout.NORTH);
		pane.add(graphicPane, BorderLayout.CENTER);
		pane.add(sliders, BorderLayout.SOUTH);

		subscribeToEvents();
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
	}

	// -- DisplayWindow methods --

	@Override
	public SwingImageDisplay getDisplay() {
		return display;
	}

	@Override
	public void update() {
		setLabel(makeLabel());
		for (final DisplayView view : display.getViews())
			view.update();
	}

	@Override
	public void redoLayout() {
		for (final DisplayView view : display.getViews()) {
			final Dataset dataset = getDataset(view);
			if (dataset == null) continue;

			createSliders(view);
			sliders.setVisible(sliders.getComponentCount() > 0);

			setTitle(makeTitle(dataset, 1.0));
			setLabel(makeLabel());

			// CTR TODO - for 2.0-alpha2 we are limiting displays to a single view.
			// But most of the infrastructure is in place to support multiple views.
			// We just need to do something more intelligent here regarding the
			// slider panel (i.e., multiple groups of sliders), then expose the
			// multi-view capabilities in the UI.
			break;
		}
		pack();
		setVisible(true);
	}

	@Override
	public void setLabel(final String s) {
		imageLabel.setText(s);
	}

	@Override
	public void addEventDispatcher(final EventDispatcher dispatcher) {
		final AWTEventDispatcher awtDispatcher = (AWTEventDispatcher) dispatcher;
		addKeyListener(awtDispatcher);
		addWindowListener(awtDispatcher);
	}

	// -- Helper methods --

	private void subscribeToEvents() {
		subscribers = new ArrayList<EventSubscriber<?>>();

		final EventSubscriber<ZoomEvent> zoomSubscriber =
			new EventSubscriber<ZoomEvent>()
		{
			@SuppressWarnings("synthetic-access")
			@Override
			public void onEvent(final ZoomEvent event) {
				if (event.getCanvas() != getDisplay().getImageCanvas()) return;
				// CTR TODO - Fix zoom label to show beyond just the active view.
				final DisplayView activeView = getDisplay().getActiveView();
				final Dataset dataset = getDataset(activeView);
				setTitle(makeTitle(dataset, event.getScale()));
			}
		};
		subscribers.add(zoomSubscriber);
		Events.subscribe(ZoomEvent.class, zoomSubscriber);

		final EventSubscriber<DatasetRestructuredEvent> dsRestructuredSubscriber =
			new EventSubscriber<DatasetRestructuredEvent>()
		{
			@SuppressWarnings("synthetic-access")
			@Override
			public void onEvent(DatasetRestructuredEvent event) {
				DisplayView view = getDisplay().getActiveView();
				final Dataset ds = getDataset(view); 
				if (event.getObject() != ds) return;
				createSliders(view);
			}
		};
		subscribers.add(dsRestructuredSubscriber);
		Events.subscribe(DatasetRestructuredEvent.class, dsRestructuredSubscriber);
	}

	private void createSliders(final DisplayView view) {
		final Dataset dataset = getDataset(view);
		final long[] dims = dataset.getDims();
		final Axis[] axes = dataset.getAxes();

		sliders.removeAll();
		for (int i = 0; i < dims.length; i++) {
			if (Axes.isXY(axes[i])) continue;
			if (dims[i] == 1) continue;

			final JLabel label = new JLabel(axes[i].getLabel());
			label.setHorizontalAlignment(SwingConstants.RIGHT);
			final long max = dims[i] + 1;
			if (max < 1 || max > Integer.MAX_VALUE) {
				throw new IllegalArgumentException("Dimension #" + i +
					" out of range: " + max);
			}
			final JScrollBar slider =
				new JScrollBar(Adjustable.HORIZONTAL, 1, 1, 1, (int) max);
			final int axisNumber = i;
			slider.addAdjustmentListener(new AdjustmentListener() {
				@Override
				public void adjustmentValueChanged(final AdjustmentEvent e) {
					final int position = slider.getValue() - 1;
					view.setPosition(position, axisNumber);
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
		final long[] pos = view.getPlanePosition();

		final StringBuilder sb = new StringBuilder();
		for (int i = 0, p = -1; i < dims.length; i++) {
			if (Axes.isXY(axes[i])) continue;
			p++;
			if (dims[i] == 1) continue;
			sb.append(axes[i] + ": " + (pos[p] + 1) + "/" + dims[i] + "; ");
		}
		sb.append(dims[xIndex] + "x" + dims[yIndex] + "; ");
		sb.append(dataset.getTypeLabel());
		return sb.toString();
	}

	private String makeTitle(final Dataset dataset, final double scale) {
		final String datasetName = dataset.getName();

		if (scale == 1.0) return datasetName; // exactly 100% zoom

		final String infoString =
			String.format("%s (%.2f%%)", datasetName, scale * 100);

		return infoString;
	}

	private Dataset getDataset(final DisplayView view) {
		final DataObject dataObject = view.getDataObject();
		return dataObject instanceof Dataset ? (Dataset) dataObject : null;
	}
}
