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

package imagej.ui.swing.display;

import imagej.ImageJ;
import imagej.data.Extents;
import imagej.data.display.CanvasHelper;
import imagej.data.display.DatasetView;
import imagej.data.display.ImageCanvas;
import imagej.data.display.ImageDisplayService;
import imagej.data.display.event.AxisPositionEvent;
import imagej.event.EventHandler;
import imagej.event.EventService;
import imagej.event.EventSubscriber;
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
import java.util.HashMap;
import java.util.List;
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
import net.imglib2.meta.Axes;
import net.imglib2.meta.AxisType;
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

	private final AbstractSwingImageDisplay display;
	private final JLabel imageLabel;
	private final JPanel imagePane;
	private final JPanel sliderPanel;
	private final DisplayWindow window;
	private boolean initialScaleCalculated = false;

	private final Map<AxisType, JScrollBar> axisSliders =
		new ConcurrentHashMap<AxisType, JScrollBar>();

	private final Map<AxisType, JLabel> axisLabels = new HashMap<AxisType, JLabel>();

	@SuppressWarnings("unused")
	private List<EventSubscriber<?>> subscribers;

	// -- constructors --
	
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

		subscribers = ImageJ.get(EventService.class).subscribe(this);
	}

	// -- SwingDisplayPanel methods --

	public void addEventDispatcher(final AWTKeyEventDispatcher dispatcher) {
		addKeyListener(dispatcher);
	}

	// -- DisplayPanel methods --

	@Override
	public AbstractSwingImageDisplay getDisplay() {
		return display;
	}

	@Override
	public DisplayWindow getWindow() {
		return window;
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
				doInitialSizing();
				window.setTitle(getDisplay().getName());
				window.showDisplay(true);
			}
		});
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

	@Override
	public void redraw() {
		final ImageDisplayService imageDisplayService =
				ImageJ.get(ImageDisplayService.class);
			final DatasetView view = imageDisplayService.getActiveDatasetView(display);
			if (view == null) return; // no active dataset
			view.getProjector().map();
	}
	
	// -- Event handlers --

	@EventHandler
	protected void onEvent(AxisPositionEvent event) {
		if (event.getDisplay() != getDisplay()) return;
		final AxisType axis = event.getAxis();
		updateAxis(axis);
	}

	// -- Helper methods --

	private void createSliders() {
		final AxisType[] axes = display.getAxes();
		final Extents extents = display.getExtents();

		// remove obsolete sliders
		for (final AxisType axis : axisSliders.keySet()) {
			if (display.getAxisIndex(axis) >= 0) continue; // axis still active
			sliderPanel.remove(axisSliders.get(axis));
			sliderPanel.remove(axisLabels.get(axis));
			axisSliders.remove(axis);
			axisLabels.remove(axis);
		}
		
		// configure sliders to match axes and extents
		for (int i = 0; i < axes.length; i++) {
			final AxisType axis = axes[i];
			if (Axes.isXY(axis)) continue; // skip spatial axes
			final int min = (int) extents.min(i);
			final int max = (int) extents.max(i) + 1;
			final int value = (int) getDisplay().getLongPosition(axis);

			final JScrollBar axisSlider = axisSliders.get(axis);
			if (axisSlider == null) {
				// create new slider

				final JLabel label = new JLabel(axis.getLabel());
				label.setHorizontalAlignment(SwingConstants.RIGHT);
				axisLabels.put(axis, label);

				final JScrollBar slider = new JScrollBar(Adjustable.HORIZONTAL, value,
					1, min, max);
				slider.addAdjustmentListener(new AdjustmentListener() {
	
					@Override
					public void adjustmentValueChanged(final AdjustmentEvent e) {
						getDisplay().setPosition(slider.getValue(), axis);
					}
				});
				axisSliders.put(axis, slider);

				// add slider to panel
				// TODO - ensure sliders are always ordered the same (alphabetical?)
				sliderPanel.add(label);
				sliderPanel.add(slider);
			}
			else {
				// update slider extents and value
				if (axisSlider.getMinimum() != min || axisSlider.getMaximum() != max) {
					axisSlider.setValues(value, 1, min, max);
				}
				else if (axisSlider.getValue() != value) axisSlider.setValue(value);
			}
		}
	}

	private void updateBorder(final int c) {
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

	private void doInitialSizing() {
		final JHotDrawImageCanvas canvas = getDisplay().getCanvas();
		final double scale = findFullyVisibleScale(canvas);
		final double zoomLevel = CanvasHelper.getBestZoomLevel(scale);
		canvas.setZoom(zoomLevel);
		if (!initialScaleCalculated) {
			canvas.setInitialScale(canvas.getZoomFactor());
			initialScaleCalculated = true;
		}
	}

	private void updateAxis(final AxisType axis) {
		final int value = (int) getDisplay().getLongPosition(axis);
		final JScrollBar scrollBar = axisSliders.get(axis);
		if (scrollBar != null) scrollBar.setValue(value);
		if (axis == Axes.CHANNEL) updateBorder(value);
		getDisplay().update();
	}

	private double findFullyVisibleScale(JHotDrawImageCanvas canvas) {
		final Dimension canvasSize = canvas.getPreferredSize();
		final Rectangle deskBounds = StaticSwingUtils.getWorkSpaceBounds();
		
		// calc height variables
		final int labelHeight = imageLabel.getPreferredSize().height;
		final int sliderHeight = sliderPanel.getPreferredSize().height;
		final int extraSpace = 32;
		
		// determine largest viewable panel sizes
		final int maxViewHeight =
			deskBounds.height - labelHeight - sliderHeight - extraSpace;
		final int maxViewWidth = deskBounds.width - extraSpace;
		
		// is canvas bigger than largest viewable panel?
		if ((canvasSize.width > maxViewWidth) ||
			(canvasSize.height > maxViewHeight))
		{
			// yes it is
			// so calc best scale that brings whole image into max viewable panel size
			
			final double canvasAspect = 1.0 * canvasSize.width / canvasSize.height;
			final double viewAspect = 1.0 * maxViewWidth / maxViewHeight;
			if (canvasAspect < viewAspect) {
				// image height the issue
				return 1.0 * maxViewHeight / canvasSize.height;
			}
			// else image width the issue
			return 1.0 * maxViewWidth / canvasSize.width;
		}
		
		// else canvas fits on screen as is
		return 1;
	}
}
