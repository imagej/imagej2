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

import imagej.ImageJ;
import imagej.data.Data;
import imagej.data.Dataset;
import imagej.data.display.CanvasHelper;
import imagej.data.display.DataView;
import imagej.data.display.DatasetView;
import imagej.data.display.ImageCanvas;
import imagej.data.display.ImageDisplay;
import imagej.data.display.ImageDisplayService;
import imagej.data.roi.Overlay;
import imagej.ext.display.DisplayPanel;
import imagej.ext.display.DisplayWindow;
import imagej.ui.common.awt.AWTKeyEventDispatcher;
import imagej.ui.swing.StaticSwingUtils;
import imagej.util.ColorRGB;
import imagej.util.awt.AWTColors;

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
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

import net.imglib2.display.ColorTable8;
import net.imglib2.display.RealLUTConverter;
import net.imglib2.img.Axes;
import net.imglib2.img.Axis;
import net.imglib2.roi.RegionOfInterest;
import net.imglib2.type.numeric.RealType;
import net.miginfocom.swing.MigLayout;

/**
 * Swing implementation of image display panel. Contains a label, a graphics
 * pane containing an {@link ImageCanvas}, and panel containing dimensional
 * controllers. This panel is added to a top-level {@link DisplayWindow} display
 * container.
 * 
 * @author Curtis Rueden
 * @author Grant Harris
 * @author Barry DeZonia
 */
public class SwingDisplayPanel extends JPanel implements DisplayPanel {

	private final ImageDisplay display;
	private final JLabel imageLabel;
	private final JPanel imagePane;
	private final JPanel sliderPanel;
	private final DisplayWindow window;
	private boolean initialScaleCalculated = false;

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

	public SwingDisplayPanel(final AbstractSwingImageDisplay display,
		final DisplayWindow window)
	{
		this.display = display;
		this.window = window;

		imageLabel = new JLabel(" ");
		final int prefHeight = imageLabel.getPreferredSize().height;
		imageLabel.setPreferredSize(new Dimension(0, prefHeight));

		imagePane = new JPanel();
		imagePane.setLayout(new MigLayout("ins 0", "fill,grow", "fill,grow"));
		imagePane.add(display.getCanvas());

		sliderPanel = new JPanel();
		sliderPanel.setLayout(new MigLayout("fillx,wrap 2", "[right|fill,grow]"));

		setLayout(new BorderLayout());
		setBorder(new EmptyBorder(3, 3, 3, 3));
		setBorderColor(null);

		add(imageLabel, BorderLayout.NORTH);
		add(imagePane, BorderLayout.CENTER);
		add(sliderPanel, BorderLayout.SOUTH);

		window.setContent(this);
	}

	// -- SwingDisplayPanel methods --

	public void addEventDispatcher(final AWTKeyEventDispatcher dispatcher) {
		addKeyListener(dispatcher);
	}

	// -- DisplayPanel methods --

	@Override
	public ImageDisplay getDisplay() {
		return display;
	}

	@Override
	public DisplayWindow getWindow() {
		return window;
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
				updateBorder(0);
				sliderPanel.setVisible(sliderPanel.getComponentCount() > 0);
				sizeAppropriately();
				window.setTitle(getDisplay().getName());
				window.showDisplay(true);
			}
		});
	}

	void sizeAppropriately() {
		final JHotDrawImageCanvas canvas =
			(JHotDrawImageCanvas) display.getCanvas();
		final Dimension canvasSize = canvas.getPreferredSize();
		final Rectangle deskBounds = StaticSwingUtils.getWorkSpaceBounds();
		// width determined by scaled image canvas width
		final int labelPlusSliderHeight =
			imageLabel.getPreferredSize().height +
				sliderPanel.getPreferredSize().height;
		final int extraSpace = 32;
		final int maxViewHeight =
			deskBounds.height - labelPlusSliderHeight - extraSpace;
		final int maxViewWidth = deskBounds.width - extraSpace;
		double scale = 1.0;
		if ((canvasSize.width > maxViewWidth) ||
			(canvasSize.height > maxViewHeight))
		{
			final double canvasAspect = 1.0 * canvasSize.width / canvasSize.height;
			final double viewAspect = 1.0 * maxViewWidth / maxViewHeight;
			if (canvasAspect < viewAspect) {
				// image height the issue
				scale = 1.0 * maxViewHeight / canvasSize.height;
			}
			else {
				// image width the issue
				scale = 1.0 * maxViewWidth / canvasSize.width;
			}
		}
		final double zoomLevel = CanvasHelper.getBestZoomLevel(scale);
		canvas.setZoom(zoomLevel);
		// canvasSize = canvas.getPreferredSize();
		if (!initialScaleCalculated) {
			canvas.setInitialScale(canvas.getZoomFactor());
			initialScaleCalculated = true;
		}
	}

	@Override
	public void setLabel(final String s) {
		imageLabel.setText(s);
	}

	@Override
	public void setBorderColor(final ColorRGB color) {
		final int width = 1;
		final Border border;
		if (color == null) {
			border = new EmptyBorder(width, width, width, width);
		}
		else {
			border = new LineBorder(AWTColors.getColor(color), width);
		}
		imagePane.setBorder(border);
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
				sliderPanel.remove(axisSliders.get(axis));
				sliderPanel.remove(axisLabels.get(axis));
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
					if (axis == Axes.CHANNEL) updateBorder((int) position);
					getDisplay().update();
				}
			});
			sliderPanel.add(label);
			sliderPanel.add(slider);
		}
	}

	protected void updateBorder(final int c) {
		final ImageDisplayService imageDisplayService =
			ImageJ.get(ImageDisplayService.class);
		final DatasetView view = imageDisplayService.getActiveDatasetView(display);
		if (view == null) return; // no active dataset
		final RealLUTConverter<? extends RealType<?>> converter =
			view.getConverters().get(c);
		final ColorTable8 lut = converter.getLUT();
		final int last = lut.getLength() - 1;
		final int r = lut.get(0, last);
		final int g = lut.get(1, last);
		final int b = lut.get(2, last);
		final ColorRGB color = new ColorRGB(r, g, b);
		setBorderColor(color);
	}

	// CTR TODO - migrate axis position tracking to AbstractImageDisplay

	protected long getAxisPosition(final Axis axis) {
		if (axisPositions.containsKey(axis)) {
			return axisPositions.get(axis);
		}
		return 0;
	}

	protected void setAxisPosition(final Axis axis, final long position) {
		axisPositions.put(axis, position);
		final JScrollBar scrollBar = axisSliders.get(axis);
		if (scrollBar != null) scrollBar.setValue((int) position);
	}

}
