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

import imagej.data.Data;
import imagej.data.Dataset;
import imagej.data.display.DataView;
import imagej.data.display.DisplayWindow;
import imagej.data.display.ImageDisplay;
import imagej.data.display.ImageDisplayPanel;
import imagej.data.roi.Overlay;
import imagej.ext.display.EventDispatcher;
import imagej.ui.common.awt.AWTKeyEventDispatcher;
import imagej.ui.common.awt.AWTMouseEventDispatcher;
import imagej.ui.swing.StaticSwingUtils;

import java.awt.Adjustable;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Rectangle;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

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
 * Swing implementation of image display panel. Contains a label, a graphics
 * pane containing an ImageCanvas, and panel containing dimensional controllers
 * (sliders). This panel is added to a top-level display container
 * (DisplayWindow).
 * 
 * @author Curtis Rueden
 * @author Grant Harris
 * @author Barry DeZonia
 */
public class SwingDisplayPanel extends JPanel implements ImageDisplayPanel {

	private final ImageDisplay display;
	private final JLabel imageLabel;
	private final JPanel sliders;
	private final DisplayWindow window;

	protected final Map<Axis, Long> axisPositions = new HashMap<Axis, Long>();

	// TODO - HACK - to avoid concurrent modification exceptions I need to make
	// axisSliders a ConcurrentHashMap instead of a regular HashMap. See bug
	// #672. The weird part is tracing with print statements you can show that
	// nobody but the single call to createSliders() is accessing the
	// axisSliders instance variable. I can find no evidence of a concurrent
	// access let alone modification. Making hashmap concurrent makes issue go
	// away.
	private final Map<Axis, JScrollBar> axisSliders =
		new ConcurrentHashMap<Axis, JScrollBar>();

	private final Map<Axis, JLabel> axisLabels = new HashMap<Axis, JLabel>();

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
		graphicPane.add((JHotDrawImageCanvas) display.getCanvas());

		sliders = new JPanel();
		sliders.setLayout(new MigLayout("fillx,wrap 2", "[right|fill,grow]"));

		setLayout(new BorderLayout());
		setBorder(new EmptyBorder(3, 3, 3, 3));

		add(imageLabel, BorderLayout.NORTH);
		add(graphicPane, BorderLayout.CENTER);
		add(sliders, BorderLayout.SOUTH);

		window.setContent(this);
	}

	// -- ImageDisplayPanel methods --

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

	@Override
	public void setAxisPosition(final Axis axis, final long position) {
		axisPositions.put(axis, position);
		final JScrollBar scrollBar = axisSliders.get(axis);
		if (scrollBar != null) scrollBar.setValue((int) position);
	}

	@Override
	public DisplayWindow getWindow() {
		return window;
	}

	// -- DisplayPanel methods --

	@Override
	public ImageDisplay getDisplay() {
		return display;
	}

	@Override
	public void makeActive() {
		window.requestFocus();
	}

	@Override
	public void redoLayout() {
		EventQueue.invokeLater(new Runnable() {

			@SuppressWarnings("synthetic-access")
			@Override
			public void run() {
				createSliders();
				sliders.setVisible(sliders.getComponentCount() > 0);
				sizeAppropriately();
				window.setTitle(getDisplay().getName());
				window.showDisplay(true);
			}
		});
	}

	private boolean initial = true;

	void sizeAppropriately() {
		final JHotDrawImageCanvas canvas =
			(JHotDrawImageCanvas) display.getCanvas();
		final Rectangle deskBounds = StaticSwingUtils.getWorkSpaceBounds();
		Dimension canvasSize = canvas.getPreferredSize();
		// width determined by scaled image canvas width
		final int labelPlusSliderHeight =
			imageLabel.getPreferredSize().height + sliders.getPreferredSize().height;
		// graphicPane.getPreferredSize();
		int scaling = 1;
		while (canvasSize.height + labelPlusSliderHeight > deskBounds.height - 32 ||
			canvasSize.width > deskBounds.width - 32)
		{
			canvas.setZoom(1.0 / scaling++);
			canvasSize = canvas.getPreferredSize();
		}
		if (initial) {
			canvas.setInitialScale(canvas.getZoomFactor());
			initial = false;
		}
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

	private/*synchronized*/void createSliders() {
		final long[] min = new long[display.numDimensions()];
		Arrays.fill(min, Long.MAX_VALUE);
		final long[] max = new long[display.numDimensions()];
		Arrays.fill(max, Long.MIN_VALUE);
		// final Axis[] axes = new Axis[display.numDimensions()];
		// display.axes(axes);
		final Axis[] dispAxes = display.getAxes();
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
		for (final DataView v : display) {
			final Data o = v.getData();
			if (o instanceof Dataset) {
				final Dataset ds = (Dataset) o;
				final long[] dims = ds.getDims();
				for (int i = 0; i < dispAxes.length; i++) {
					final int index = ds.getAxisIndex(dispAxes[i]);
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
					for (int i = 0; i < dispAxes.length; i++) {
						final int index = overlay.getAxisIndex(dispAxes[i]);
						if ((index >= 0) && (index < roi.numDimensions())) {
							min[i] = Math.min(min[i], (long) Math.ceil(roi.realMin(index)));
							max[i] = Math.max(max[i], (long) Math.floor(roi.realMax(index)));
						}
					}
				}
			}
		}

		for (final Axis axis : axisSliders.keySet()) {
			if (display.getAxisIndex(axis) < 0) {
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
				for (int i = 0; i < dispAxes.length; i++) {
					if (axis == dispAxes[i]) {
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

		for (int i = 0; i < dispAxes.length; i++) {
			final Axis axis = dispAxes[i];
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
					final long position = slider.getValue();
					axisPositions.put(axis, position);
					getDisplay().update();
				}
			});
			sliders.add(label);
			sliders.add(slider);
		}
	}

}
