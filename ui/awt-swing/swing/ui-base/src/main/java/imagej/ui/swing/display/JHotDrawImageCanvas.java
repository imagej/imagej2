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
import imagej.data.display.CanvasHelper;
import imagej.data.display.DataView;
import imagej.data.display.DatasetView;
import imagej.data.display.ImageCanvas;
import imagej.data.display.ImageDisplay;
import imagej.data.display.ImageDisplayViewer;
import imagej.data.display.OverlayView;
import imagej.data.display.event.DataViewDeselectedEvent;
import imagej.data.display.event.DataViewSelectedEvent;
import imagej.data.display.event.MouseCursorEvent;
import imagej.data.display.event.ZoomEvent;
import imagej.event.EventHandler;
import imagej.event.EventService;
import imagej.event.EventSubscriber;
import imagej.ext.MouseCursor;
import imagej.ext.display.event.DisplayUpdatedEvent;
import imagej.ext.display.event.DisplayUpdatedEvent.DisplayUpdateLevel;
import imagej.ext.tool.Tool;
import imagej.ext.tool.ToolService;
import imagej.ext.tool.event.ToolActivatedEvent;
import imagej.ui.common.awt.AWTCursors;
import imagej.ui.common.awt.AWTKeyEventDispatcher;
import imagej.ui.common.awt.AWTMouseEventDispatcher;
import imagej.ui.swing.StaticSwingUtils;
import imagej.ui.swing.overlay.FigureCreatedEvent;
import imagej.ui.swing.overlay.IJHotDrawOverlayAdapter;
import imagej.ui.swing.overlay.JHotDrawTool;
import imagej.ui.swing.overlay.OverlayCreatedListener;
import imagej.ui.swing.overlay.ToolDelegator;
import imagej.util.IntCoords;
import imagej.util.Log;
import imagej.util.RealCoords;
import imagej.util.RealRect;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import net.imglib2.meta.Axes;
import net.imglib2.meta.AxisType;

import org.jhotdraw.draw.DefaultDrawing;
import org.jhotdraw.draw.DefaultDrawingEditor;
import org.jhotdraw.draw.DefaultDrawingView;
import org.jhotdraw.draw.Drawing;
import org.jhotdraw.draw.DrawingEditor;
import org.jhotdraw.draw.Figure;
import org.jhotdraw.draw.event.FigureSelectionEvent;
import org.jhotdraw.draw.event.FigureSelectionListener;

/**
 * A renderer of an {@link ImageCanvas}, which uses JHotDraw's
 * {@link DefaultDrawingView} component to do most of the work.
 * 
 * @author Curtis Rueden
 * @author Lee Kamentsky
 */
public class JHotDrawImageCanvas extends JPanel implements AdjustmentListener
{

	private static final long serialVersionUID = 1L;

	private final AbstractSwingImageDisplayViewer displayViewer;

	private final Drawing drawing;
	private final DefaultDrawingView drawingView;
	private final DrawingEditor drawingEditor;
	private final ToolDelegator toolDelegator;

	private final JScrollPane scrollPane;
	private boolean insideSynch = false;
	
	private final List<FigureView> figureViews = new ArrayList<FigureView>();

	@SuppressWarnings("unused")
	private final List<EventSubscriber<?>> subscribers;

	public JHotDrawImageCanvas(final AbstractSwingImageDisplayViewer displayViewer) {
		this.displayViewer = displayViewer;

		drawing = new DefaultDrawing(); // or QuadTreeDrawing?

		drawingView = new DefaultDrawingView();
		drawingView.setDrawing(drawing);

		drawingEditor = new DefaultDrawingEditor();
		drawingEditor.add(drawingView);
		toolDelegator = new ToolDelegator();
		drawingEditor.setTool(toolDelegator);

		scrollPane = new JScrollPane(drawingView);
		setLayout(new BorderLayout());
		add(scrollPane, BorderLayout.CENTER);

		scrollPane.getHorizontalScrollBar().addAdjustmentListener(this);
		scrollPane.getVerticalScrollBar().addAdjustmentListener(this);

		final ImageJ context = displayViewer.getImageDisplay().getContext();
		final ToolService toolService = context.getService(ToolService.class);
		final Tool activeTool = toolService.getActiveTool();
		activateTool(activeTool);
		final EventService eventService = context.getService(EventService.class);
		subscribers = eventService.subscribe(this);

		drawingView.addFigureSelectionListener(new FigureSelectionListener() {

			@Override
			public void selectionChanged(final FigureSelectionEvent event) {
				onFigureSelectionChanged(event);
			}
		});
		drawingView.addComponentListener(new ComponentAdapter() {

			@Override
			public void componentResized(ComponentEvent e) {
				onSizeChanged(e);
			}
			
		});
	}

	protected FigureView getFigureView(DataView dataView) {
		for(FigureView figureView:figureViews) {
			if (figureView.getDataView() == dataView) return figureView;
		}
		return null;
	}
	/**
	 * Respond to the JHotDraw figure selection event by selecting and deselecting
	 * views whose state has changed
	 * 
	 * @param event - event indicating that the figure selections have changed
	 */
	protected void onFigureSelectionChanged(final FigureSelectionEvent event) {
		final Set<Figure> newSelection = event.getNewSelection();
		final Set<Figure> oldSelection = event.getOldSelection();
		for (final DataView view : displayViewer.getDisplay()) {
			FigureView figureView = getFigureView(view);
			if (figureView != null) {
				final Figure figure = figureView.getFigure();
				if (newSelection.contains(figure)) {
					// BDZ removed next line 10-12-11
					// Fixes drawing of multiple overlays (#817). Lee had this code
					// here in anticipation of avoiding infinite event loops.
					// Inspection seems to bear out that this possibility doesn't
					// happen.
					// if (!oldSelection.contains(figure))
					view.setSelected(true);
				}
				else if (oldSelection.contains(figure)) {
					view.setSelected(false);
				}
			}
		}
	}
	
	/**
	 * Tell the display's canvas that its viewport's size changed
	 * @param e
	 */
	protected void onSizeChanged(final ComponentEvent e) {
		final ImageCanvas canvas = getDisplay().getCanvas();
		Rectangle viewRect = scrollPane.getViewport().getViewRect();
		canvas.setViewportSize(viewRect.width, viewRect.height);
	}

	@EventHandler
	protected void onViewSelected(final DataViewSelectedEvent event) {
		final DataView view = event.getView();
		FigureView figureView = getFigureView(view); 
		if (figureView != null) {
			final Figure figure = figureView.getFigure();
			if (!drawingView.getSelectedFigures().contains(figure)) {
				drawingView.addToSelection(figure);
			}
		}
	}

	@EventHandler
	protected void onViewDeselected(final DataViewDeselectedEvent event) {
		final DataView view = event.getView();
		FigureView figureView = getFigureView(view); 
		if (figureView != null) {
			final Figure figure = figureView.getFigure();
			if (drawingView.getSelectedFigures().contains(figure)) {
				drawingView.removeFromSelection(figure);
			}
		}
	}

	@EventHandler
	protected void onToolActivatedEvent(final ToolActivatedEvent event) {
		final Tool iTool = event.getTool();
		activateTool(iTool);
	}
	
	@EventHandler
	protected void onEvent(DisplayUpdatedEvent event) {
		if (event.getDisplay() != getDisplay()) return;
		
		rebuild();
		update();
	}
	
	@EventHandler
	protected void onEvent(ZoomEvent event) {
		if (event.getCanvas() != getDisplay().getCanvas()) return;
		syncPanAndZoom();
	}
	
	@EventHandler
	protected void onEvent(MouseCursorEvent event) {
		drawingView.setCursor(AWTCursors.getCursor(getDisplay().getCanvas().getCursor()));
	}
	
	
	protected void rebuild() {
		for (DataView dataView:getDisplay()) {
			FigureView figureView = getFigureView(dataView);
			if (figureView == null) {
				if (dataView instanceof DatasetView) {
					figureView = new SwingDatasetView(this.displayViewer, (DatasetView)dataView);
				} else if (dataView instanceof OverlayView) {
					figureView = new SwingOverlayView(this.displayViewer, (OverlayView)dataView);
				} else {
					Log.error("Don't know how to make a figure view for " + dataView.getClass().getName());
					continue;
				}
				figureViews.add(figureView);
			}
		}
		int idx = 0;
		while (idx < figureViews.size()) {
			FigureView figureView = figureViews.get(idx);
			if (! getDisplay().contains(figureView.getDataView())) {
				figureViews.remove(idx);
				figureView.dispose();
			} else {
				idx++;
			}
		}
	}
	
	protected void update() {
		for (FigureView figureView:figureViews) {
			figureView.update();
		}
	}

	protected void activateTool(final Tool iTool) {
		if (iTool instanceof IJHotDrawOverlayAdapter) {
			final IJHotDrawOverlayAdapter adapter = (IJHotDrawOverlayAdapter) iTool;

			// When the tool creates an overlay, add the
			// overlay/figure combo to a SwingOverlayView.
			final OverlayCreatedListener listener = new OverlayCreatedListener() {

				@SuppressWarnings("synthetic-access")
				@Override
				public void overlayCreated(final FigureCreatedEvent e) {
					final OverlayView overlay = e.getOverlay();
					ImageDisplay display = displayViewer.getImageDisplay();
					for (int i = 0; i < display.numDimensions(); i++) {
						final AxisType axis = display.axis(i);
						if (Axes.isXY(axis)) continue;
						if (overlay.getData().getAxisIndex(axis) < 0) {
							overlay.setPosition(display.getLongPosition(axis), axis);
						}
					}
					if (drawingView.getSelectedFigures().contains(e.getFigure())) {
						overlay.setSelected(true);
					}
					SwingOverlayView figureView = new SwingOverlayView(displayViewer, overlay, e.getFigure());
					figureViews.add(figureView);
					display.add(overlay);
					display.update();
				}
			};

			final JHotDrawTool creationTool = adapter.getCreationTool(getDisplay(), listener);

			toolDelegator.setCreationTool(creationTool);
		}
		else {
			toolDelegator.setCreationTool(null);
		}
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

	public void addEventDispatcher(final AWTKeyEventDispatcher dispatcher) {
		drawingView.addKeyListener(dispatcher);
	}

	public void addEventDispatcher(final AWTMouseEventDispatcher dispatcher) {
		drawingView.addMouseListener(dispatcher);
		drawingView.addMouseMotionListener(dispatcher);
		drawingView.addMouseWheelListener(dispatcher);
	}

	// -- JComponent methods --
	@Override
	public Dimension getPreferredSize() {
		// NB Johannes
		// HACK: Size the canvas one pixel larger. This is a workaround to an
		// apparent bug in JHotDraw, where an ImageFigure is initially drawn as a
		// large X until it is finished being rendered. Unfortunately, the X is
		// slightly smaller than the image after being rendered.
		final int w = scrollPane.getPreferredSize().width + 1;
		final int h = scrollPane.getPreferredSize().height + 1;
		
		// NB BDZ - Avoid space left around for nonexistent scroll bars
		// This code works (except rare cases). Not sure why 4 is key. 3 and lower
		// and sizing failures happen. We used to have a fudge factor of 5 in place
		// elsewhere. Can no longer find it. But it is apparent from debugging
		// statements that there is an off by 5(?). Notice we test versus 4 but add
		// 5. If both 4 then initial zoom has scrollbars when not needed. If both 5
		// then some zoom ins can leave empty scroll bar space. (I may have these
		// conditions reversed). Anyhow printing in here can show getPreferredSize()
		// starts out at w,h and before/during 1st redraw goes to w+1,h+1. Its this
		// off by one that makes initial view have scroll bars unnecessarily. Look
		// into this further.
		Dimension drawViewSize = drawingView.getPreferredSize();
		if (drawViewSize.width+4 <= scrollPane.getPreferredSize().width)
			if (drawViewSize.height+4 <= scrollPane.getPreferredSize().height)
				return new Dimension(drawViewSize.width+5, drawViewSize.height+5);
		return new Dimension(w, h);
	}

	// -- Helper methods --
	
	private ImageDisplay getDisplay() {
		return displayViewer.getImageDisplay();
	}

	// -- AdjustmentListener methods --

	@Override
	public void adjustmentValueChanged(final AdjustmentEvent e) {
		if (insideSynch) return;
		final ImageCanvas canvas = getDisplay().getCanvas();
		final Point viewPos = scrollPane.getViewport().getViewPosition();
		final Rectangle viewRect = scrollPane.getViewport().getViewRect();
		if (viewRect.width != canvas.getViewportWidth() || viewRect.height != canvas.getViewportHeight()) {
			canvas.setViewportSize(viewRect.width, viewRect.height);
		}
		final RealCoords center = new RealCoords(
				(viewPos.x + viewRect.width / 2) / drawingView.getScaleFactor(),
				(viewPos.y + viewRect.height / 2) / drawingView.getScaleFactor());
		final RealCoords oldCenter = canvas.getPanCenter(); 
		if (oldCenter.x == center.x && oldCenter.y == center.y) return;
		canvas.setPan(center);
	}

	private void syncPanAndZoom() {
		if (insideSynch) return;
		
		insideSynch = true;
		try {
			final ImageCanvas canvas = getDisplay().getCanvas();
			boolean considerResize = false;
			double zoomFactor = canvas.getZoomFactor();
			if (drawingView.getScaleFactor() != zoomFactor ) {
				drawingView.setScaleFactor(zoomFactor);
				scrollPane.validate();
				considerResize = true;
			}
			final Point viewPos = scrollPane.getViewport().getViewPosition();
			final Rectangle viewRect = scrollPane.getViewport().getViewRect();
			final RealCoords center = canvas.getPanCenter();
			final int originX = (int)Math.round(center.x * zoomFactor - viewRect.width / 2.0);
			final int originY = (int)Math.round(center.y * zoomFactor - viewRect.height / 2.0);
			final IntCoords origin = new IntCoords(originX, originY);
			if (constrainOrigin(origin)) {
				// Back-compute the center if origin was changed.
				canvas.setPan(new RealCoords(
						(origin.x + viewRect.width / 2.0) / zoomFactor,
						(origin.y + viewRect.height / 2.0) / zoomFactor));
			}
			if (viewPos.x != origin.x || viewPos.y != origin.y)
				scrollPane.getViewport().setViewPosition(new Point(origin.x, origin.y));
			if (considerResize)
				maybeResizeWindow();
		} finally {
			insideSynch = false;
		}
	}

	/**
	 * Constrain the origin to within the image.
	 * 
	 * @param origin
	 * @return true if origin was changed.
	 */
	private boolean constrainOrigin(final IntCoords origin) {
		boolean originChanged = false;
		if (origin.x < 0) { origin.x = 0; originChanged = true; }
		if (origin.y < 0) { origin.y = 0; originChanged = true; }
		final Dimension viewportSize = scrollPane.getViewport().getSize();
		final Dimension canvasSize = drawingView.getSize();
		final int xMax = canvasSize.width - viewportSize.width;
		final int yMax = canvasSize.height - viewportSize.height;
		if (origin.x > xMax) { origin.x = xMax; originChanged = true; }
		if (origin.y > yMax) { origin.y = yMax; originChanged = true; }
		return originChanged;
	}

	private void maybeResizeWindow()
	{
		final Rectangle bounds = StaticSwingUtils.getWorkSpaceBounds();
		final RealRect imageBounds = getDisplay().getImageExtents();
		final ImageCanvas canvas = getDisplay().getCanvas();
		final IntCoords topLeft = canvas.imageToPanelCoords(
				new RealCoords(imageBounds.x, imageBounds.y));
		final IntCoords bottomRight = canvas.imageToPanelCoords(
				new RealCoords(imageBounds.x + imageBounds.width, 
						       imageBounds.y + imageBounds.height));
		if (bottomRight.x - topLeft.x > bounds.width) return;
		if (bottomRight.y - topLeft.y > bounds.height) return;

		// FIXME TEMP
		// NB BDZ - there is an issue where zoom out does not always pack()
		// correctly. There seems like there is a race condition. I have tried a
		// number of approaches to a fix but nothing has fallen out yet. Approaches
		// included:
		// - generating a "INeedARepackEvent" and trying to handle elsewhere
		// - calling redoLayout() on the panel (infinite loop)
		// - call pack() after a slight delay
		// Current approach:
		//   Update on a different thread after a slight delay.
		//   Note this always resizes correctly (except for off by a fixed
		//   scrollbar size which I have handled in getPreferredSize())
		
		new Thread() {
			@Override
			@SuppressWarnings("synthetic-access")
			public void run() {
				// NB - its not enough to be in separate thread - it must sleep a little
				try { Thread.sleep(30); } catch (Exception e) {/*do nothing*/}
				displayViewer.getPanel().getWindow().pack();
			}
		}.start();
	}

}
