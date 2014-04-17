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

package imagej.plugins.uis.swing.viewer.image;

import imagej.data.Dataset;
import imagej.data.DatasetService;
import imagej.data.display.DataView;
import imagej.data.display.DatasetView;
import imagej.data.display.ImageCanvas;
import imagej.data.display.ImageDisplay;
import imagej.data.display.ImageDisplayService;
import imagej.data.display.OverlayView;
import imagej.data.display.event.DataViewDeselectedEvent;
import imagej.data.display.event.DataViewSelectedEvent;
import imagej.data.display.event.MouseCursorEvent;
import imagej.data.display.event.PanZoomEvent;
import imagej.display.event.DisplayDeletedEvent;
import imagej.plugins.uis.swing.StaticSwingUtils;
import imagej.plugins.uis.swing.overlay.FigureCreatedEvent;
import imagej.plugins.uis.swing.overlay.JHotDrawAdapter;
import imagej.plugins.uis.swing.overlay.JHotDrawService;
import imagej.plugins.uis.swing.overlay.JHotDrawTool;
import imagej.plugins.uis.swing.overlay.ToolDelegator;
import imagej.tool.Tool;
import imagej.tool.ToolService;
import imagej.tool.event.ToolActivatedEvent;
import imagej.ui.common.awt.AWTCursors;
import imagej.ui.common.awt.AWTDropTargetEventDispatcher;
import imagej.ui.common.awt.AWTInputEventDispatcher;

import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JViewport;

import net.imglib2.RandomAccess;
import net.imglib2.display.screenimage.awt.ARGBScreenImage;
import net.imglib2.meta.Axes;
import net.imglib2.meta.AxisType;
import net.imglib2.type.numeric.RealType;

import org.jhotdraw.draw.DefaultDrawing;
import org.jhotdraw.draw.DefaultDrawingEditor;
import org.jhotdraw.draw.DefaultDrawingView;
import org.jhotdraw.draw.Drawing;
import org.jhotdraw.draw.DrawingEditor;
import org.jhotdraw.draw.Figure;
import org.jhotdraw.draw.event.FigureSelectionEvent;
import org.jhotdraw.draw.event.FigureSelectionListener;
import org.scijava.Disposable;
import org.scijava.event.EventHandler;
import org.scijava.event.EventService;
import org.scijava.event.EventSubscriber;
import org.scijava.input.MouseCursor;
import org.scijava.log.LogService;
import org.scijava.plugin.Parameter;
import org.scijava.thread.ThreadService;
import org.scijava.util.IntCoords;
import org.scijava.util.RealCoords;
import org.scijava.util.RealRect;

/**
 * A renderer of an {@link ImageCanvas}, which uses JHotDraw's
 * {@link DefaultDrawingView} component to do most of the work.
 * 
 * @author Curtis Rueden
 * @author Lee Kamentsky
 */
public class JHotDrawImageCanvas extends JPanel implements AdjustmentListener,
	ComponentListener, FigureSelectionListener, Disposable
{

	private final SwingImageDisplayViewer displayViewer;

	private final Drawing drawing;
	private final DefaultDrawingView drawingView;
	private final DrawingEditor drawingEditor;
	private final ToolDelegator toolDelegator;

	private final JScrollPane scrollPane;

	private final List<FigureView> figureViews = new ArrayList<FigureView>();

	private final List<EventSubscriber<?>> subscribers;

	@Parameter
	private ToolService toolService;

	@Parameter
	private EventService eventService;

	@Parameter
	private ImageDisplayService imageDisplayService;

	@Parameter
	private DatasetService datasetService;

	@Parameter
	private ThreadService threadService;

	@Parameter
	private JHotDrawService jHotDrawService;

	@Parameter
	private LogService log;

	public JHotDrawImageCanvas(final SwingImageDisplayViewer displayViewer) {
		displayViewer.getDisplay().getContext().inject(this);
		this.displayViewer = displayViewer;

		drawing = new DefaultDrawing(); // or QuadTreeDrawing?

		drawingView = new DefaultDrawingView() {

			@Override
			public Dimension getPreferredSize() {
				final Dimension drawViewSize = super.getPreferredSize();
				if (drawViewSize.width > 0 && drawViewSize.height > 0) {
					// NB: Current preferred size is OK.
					return drawViewSize;
				}

				// NB: One or more figures in the drawing view are not initialized yet.
				// So we calculate the projected size manually.
				final RealRect imageBounds = getDisplay().getPlaneExtents();
				final double zoomFactor = getDisplay().getCanvas().getZoomFactor();
				final int x = (int) (imageBounds.width * zoomFactor);
				final int y = (int) (imageBounds.height * zoomFactor);
				// HACK: Add extra space to avoid unnecessary scroll bars.
				final int extra = 2;
				return new Dimension(x + extra, y + extra);
			}
		};
		drawingView.setDrawing(drawing);

		drawingEditor = new DefaultDrawingEditor();
		drawingEditor.add(drawingView);
		toolDelegator = new ToolDelegator();
		toolDelegator.setSelection(false);
		drawingEditor.setTool(toolDelegator);

		scrollPane = new JScrollPane(drawingView);
		setLayout(new BorderLayout());
		add(scrollPane, BorderLayout.CENTER);

		scrollPane.getHorizontalScrollBar().addAdjustmentListener(this);
		scrollPane.getVerticalScrollBar().addAdjustmentListener(this);

		final Tool activeTool = toolService.getActiveTool();
		activateTool(activeTool);
		subscribers = eventService.subscribe(this);

		drawingView.addFigureSelectionListener(this);
		drawingView.addComponentListener(this);
	}

	// -- JHotDrawImageCanvas methods --

	public Drawing getDrawing() {
		return drawing;
	}

	public DefaultDrawingView getDrawingView() {
		return drawingView;
	}

	public DrawingEditor getDrawingEditor() {
		return drawingEditor;
	}

	public void addEventDispatcher(final AWTInputEventDispatcher dispatcher) {
		dispatcher.register(drawingView, true, true);
	}

	public void addEventDispatcher(final AWTDropTargetEventDispatcher dispatcher)
	{
		dispatcher.register(drawingView);
	}

	/**
	 * Captures the current view of data displayed in the canvas, including all
	 * JHotDraw embellishments.
	 */
	public Dataset capture() {
		final ImageDisplay display = getDisplay();
		if (display == null) return null;
		final DatasetView datasetView =
			imageDisplayService.getActiveDatasetView(display);
		if (datasetView == null) return null;

		final ARGBScreenImage screenImage = datasetView.getScreenImage();
		final Image pixels = screenImage.image();

		final int w = pixels.getWidth(null);
		final int h = pixels.getHeight(null);

		// draw the backdrop image info
		final BufferedImage outputImage =
			new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
		final Graphics2D outputGraphics = outputImage.createGraphics();
		outputGraphics.drawImage(pixels, 0, 0, null);

		// draw the overlay info
		for (final FigureView view : figureViews) {
			view.getFigure().draw(outputGraphics);
		}

		// create a dataset that has view data with overlay info on top
		final Dataset dataset =
			datasetService.create(new long[] { w, h, 3 }, "Captured view",
				new AxisType[] { Axes.X, Axes.Y, Axes.CHANNEL }, 8, false, false);
		dataset.setRGBMerged(true);
		final RandomAccess<? extends RealType<?>> accessor =
			dataset.getImgPlus().randomAccess();
		for (int x = 0; x < w; x++) {
			accessor.setPosition(x, 0);
			for (int y = 0; y < h; y++) {
				accessor.setPosition(y, 1);
				final int rgb = outputImage.getRGB(x, y);
				final int r = (rgb >> 16) & 0xff;
				final int g = (rgb >> 8) & 0xff;
				final int b = (rgb >> 0) & 0xff;
				accessor.setPosition(0, 2);
				accessor.get().setReal(r);
				accessor.setPosition(1, 2);
				accessor.get().setReal(g);
				accessor.setPosition(2, 2);
				accessor.get().setReal(b);
			}
		}
		return dataset;
	}

	// -- AdjustmentListener methods --

	@Override
	public void adjustmentValueChanged(final AdjustmentEvent e) {
		syncCanvas();
	}

	// -- FigureSelectionListener methods --

	/**
	 * Responds to the JHotDraw figure selection event by selecting and
	 * deselecting views whose state has changed.
	 * 
	 * @param event Event indicating that the figure selections have changed.
	 */
	@Override
	public void selectionChanged(FigureSelectionEvent event) {
		final Set<Figure> newSelection = event.getNewSelection();
		final Set<Figure> oldSelection = event.getOldSelection();
		for (final DataView view : getDisplay()) {
			final FigureView figureView = getFigureView(view);
			if (figureView != null) {
				final Figure figure = figureView.getFigure();
				if (newSelection.contains(figure)) {
					view.setSelected(true);
				}
				else if (oldSelection.contains(figure)) {
					view.setSelected(false);
				}
			}
		}
	}

	// -- ComponentListener methods --

	@Override
	public void componentResized(ComponentEvent e) {
		syncCanvas();
	}

	@Override
	public void componentMoved(ComponentEvent e) {
		// NB: No action needed.
	}

	@Override
	public void componentShown(ComponentEvent e) {
		// NB: No action needed.
	}

	@Override
	public void componentHidden(ComponentEvent e) {
		// NB: No action needed.
	}

	// -- Event handlers --

	@EventHandler
	protected void onEvent(final DataViewSelectedEvent event) {
		final DataView view = event.getView();
		final FigureView figureView = getFigureView(view);
		if (figureView == null) return; // not one of this canvas's views

		final Figure figure = figureView.getFigure();
		if (!drawingView.getSelectedFigures().contains(figure)) {
			drawingView.addToSelection(figure);
		}
	}

	@EventHandler
	protected void onEvent(final DataViewDeselectedEvent event) {
		final DataView view = event.getView();
		final FigureView figureView = getFigureView(view);
		if (figureView == null) return; // not one of this canvas's views

		final Figure figure = figureView.getFigure();
		if (drawingView.getSelectedFigures().contains(figure)) {
			drawingView.removeFromSelection(figure);
		}
	}

	@EventHandler
	protected void onEvent(final ToolActivatedEvent event) {
		final Tool iTool = event.getTool();
		activateTool(iTool);
	}

	@EventHandler
	protected void onEvent(final DisplayDeletedEvent event) {
		if (event.getObject() != getDisplay()) return; // not this canvas's display

		eventService.unsubscribe(subscribers);
	}

	@EventHandler
	protected void onEvent(final PanZoomEvent event) {
		final ImageCanvas canvas = event.getCanvas();
		if (canvas != getDisplay().getCanvas()) return; // not this canvas

		syncUI();
	}

	@EventHandler
	protected void onEvent(final MouseCursorEvent event) {
		final ImageCanvas canvas = event.getCanvas();
		if (canvas != getDisplay().getCanvas()) return; // not this canvas

		final MouseCursor cursor = canvas.getCursor();
		final Cursor awtCursor = AWTCursors.getCursor(cursor);
		drawingView.setCursor(awtCursor);
	}

	/**
	 * When a tool creates an overlay, add the overlay/figure combo to an
	 * {@link OverlayFigureView}.
	 */
	@EventHandler
	protected void onEvent(final FigureCreatedEvent event) {
		final ImageDisplay display = event.getDisplay();
		if (display != getDisplay()) return; // not this canvas's display

		final OverlayView overlay = event.getView();
		for (int i = 0; i < display.numDimensions(); i++) {
			final AxisType axisType = display.axis(i).type();
			if (axisType.isXY()) continue;
			if (overlay.getData().dimensionIndex(axisType) < 0) {
				overlay.setPosition(display.getLongPosition(axisType), axisType);
			}
		}
		if (drawingView.getSelectedFigures().contains(event.getFigure())) {
			overlay.setSelected(true);
		}
		final OverlayFigureView figureView =
			new OverlayFigureView(displayViewer, overlay, event.getFigure());
		figureViews.add(figureView);
		display.add(overlay);
		display.update();
	}

	// -- Internal methods --

	void rebuild() {
		for (final DataView dataView : getDisplay()) {
			FigureView figureView = getFigureView(dataView);
			if (figureView == null) {
				if (dataView instanceof DatasetView) {
					figureView =
						new DatasetFigureView(this.displayViewer, (DatasetView) dataView);
				}
				else if (dataView instanceof OverlayView) {
					figureView =
						new OverlayFigureView(this.displayViewer, (OverlayView) dataView);
				}
				else {
					log.error("Don't know how to make a figure view for " +
						dataView.getClass().getName());
					continue;
				}
				figureViews.add(figureView);
			}
		}
		int idx = 0;
		while (idx < figureViews.size()) {
			final FigureView figureView = figureViews.get(idx);
			if (!getDisplay().contains(figureView.getDataView())) {
				figureViews.remove(idx);
				figureView.dispose();
			}
			else {
				idx++;
			}
		}
	}

	void update() {
		for (final FigureView figureView : figureViews) {
			figureView.update();
		}
	}

	// -- Helper methods --

	private ImageDisplay getDisplay() {
		return displayViewer.getDisplay();
	}

	private FigureView getFigureView(final DataView dataView) {
		for (final FigureView figureView : figureViews) {
			if (figureView.getDataView() == dataView) return figureView;
		}
		return null;
	}

	/** Updates the {@link ImageCanvas} to match the UI. */
	private void syncCanvas() {
		sync(true);
	}

	/** Updates the UI to match the {@link ImageCanvas}. */
	private void syncUI() {
		sync(false);
	}

	private void sync(final boolean updateCanvas) {
		final ImageCanvas canvas = getDisplay().getCanvas();

		// threading sanity check
		if (!threadService.isDispatchThread()) {
			throw new IllegalStateException("Cannot sync viewport from thread: " +
				Thread.currentThread().getName());
		}

		// get UI settings
		final Dimension uiSize = scrollPane.getViewport().getExtentSize();
		final double uiZoom = drawingView.getScaleFactor();
		final Point uiOffset = scrollPane.getViewport().getViewPosition();

		// get canvas settings
		final int canvasWidth = canvas.getViewportWidth();
		final int canvasHeight = canvas.getViewportHeight();
		final double canvasZoom = canvas.getZoomFactor();
		final IntCoords canvasOffset = canvas.getPanOffset();

		final boolean sizeChanged =
			uiSize.width != canvasWidth || uiSize.height != canvasHeight;
		final boolean offsetChanged =
			uiOffset.x != canvasOffset.x || uiOffset.y != canvasOffset.y;
		final boolean zoomChanged = uiZoom != canvasZoom;

		if (!sizeChanged && !offsetChanged && !zoomChanged) return;

		if (log.isDebug()) {
			log.debug(getClass().getSimpleName() + " " +
				(updateCanvas ? "syncCanvas: " : "syncUI: ") + "\n\tUI size = " +
				uiSize.width + " x " + uiSize.height + "\n\tUI offset = " + uiOffset.x +
				", " + uiOffset.y + "\n\tUI zoom = " + uiZoom + "\n\tCanvas size = " +
				canvasWidth + " x " + canvasHeight + "\n\tCanvas offset = " +
				canvasOffset.x + ", " + canvasOffset.y + "\n\tCanvas zoom = " +
				canvasZoom + "\n\t" + (sizeChanged ? "sizeChanged " : "") +
				(offsetChanged ? "offsetChanged " : "") +
				(zoomChanged ? "zoomChanged " : ""));
		}

		if (updateCanvas) {
			// sync canvas viewport size
			if (sizeChanged) canvas.setViewportSize(uiSize.width, uiSize.height);

			// sync canvas pan & zoom position
			if (offsetChanged || zoomChanged) {
				// back-compute the center from the origin
				final double panCenterX = (uiOffset.x + uiSize.width / 2d) / uiZoom;
				final double panCenterY = (uiOffset.y + uiSize.height / 2d) / uiZoom;
				canvas.setZoomAndCenter(uiZoom, new RealCoords(panCenterX, panCenterY));
			}
		}
		else { // update UI
			// sync UI viewport size
			if (sizeChanged) {
				final Dimension newViewSize = new Dimension(canvasWidth, canvasHeight);
				scrollPane.getViewport().setViewSize(newViewSize);
			}

			// sync UI zoom factor
			if (zoomChanged) drawingView.setScaleFactor(canvasZoom);

			// sync UI pan position
			if (offsetChanged) {
				final Point newViewPos = new Point(canvasOffset.x, canvasOffset.y);
				scrollPane.getViewport().setViewPosition(newViewPos);
			}

			if (zoomChanged) maybeResizeWindow();
		}
	}

	private void maybeResizeWindow() {
		final Rectangle bounds = StaticSwingUtils.getWorkSpaceBounds();
		final RealRect imageBounds = getDisplay().getPlaneExtents();
		final ImageCanvas canvas = getDisplay().getCanvas();
		final IntCoords topLeft =
			canvas.dataToPanelCoords(new RealCoords(imageBounds.x, imageBounds.y));
		final IntCoords bottomRight =
			canvas.dataToPanelCoords(new RealCoords(
				imageBounds.x + imageBounds.width, imageBounds.y + imageBounds.height));
		if (bottomRight.x - topLeft.x > bounds.width) return;
		if (bottomRight.y - topLeft.y > bounds.height) return;

		displayViewer.getWindow().pack();
	}

	private void activateTool(final Tool tool) {
		final JHotDrawAdapter<?> adapter = jHotDrawService.getAdapter(tool);
		if (adapter != null) {
			final JHotDrawTool creationTool = adapter.getCreationTool(getDisplay());
			toolDelegator.setCreationTool(creationTool);
			toolDelegator.setSelection(true);
		}
		else {
			toolDelegator.setCreationTool(null);
			toolDelegator.setSelection(false);
		}
	}

	// -- Disposable API --

	/**
	 * Clears any resources associated with this canvas. This is necessary to
	 * avoid deadlocks where finalization-dependent cleanup operations (e.g.
	 * PhantomReference queueing) are unable to proceed because this object
	 * is being held by a hard reference by the Finalizer (which the downstream
	 * resources are waiting on) implicit in upstream {@link JViewport} use.
	 */
	@Override
	public void dispose() {
		figureViews.clear();
	}

}
