//
// JHotDrawImageCanvas.java
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
import imagej.data.roi.Overlay;
import imagej.display.CanvasHelper;
import imagej.display.DisplayView;
import imagej.display.DisplayWindow;
import imagej.display.EventDispatcher;
import imagej.display.ImageCanvas;
import imagej.display.MouseCursor;
import imagej.display.event.DisplayViewDeselectedEvent;
import imagej.display.event.DisplayViewSelectedEvent;
import imagej.event.EventSubscriber;
import imagej.event.Events;
import imagej.tool.ITool;
import imagej.tool.ToolManager;
import imagej.tool.event.ToolActivatedEvent;
import imagej.ui.common.awt.AWTCursors;
import imagej.ui.common.awt.AWTEventDispatcher;
import imagej.ui.swing.tools.SelectionTool;
import imagej.ui.swing.tools.roi.IJCreationTool;
import imagej.ui.swing.tools.roi.IJCreationTool.FigureCreatedEvent;
import imagej.ui.swing.tools.roi.IJHotDrawOverlayAdapter;
import imagej.util.IntCoords;
import imagej.util.RealCoords;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.util.Set;

import javax.swing.JPanel;
import javax.swing.JScrollPane;

import net.imglib2.img.Axes;
import net.imglib2.img.Axis;

import org.jhotdraw.draw.DefaultDrawing;
import org.jhotdraw.draw.DefaultDrawingEditor;
import org.jhotdraw.draw.DefaultDrawingView;
import org.jhotdraw.draw.Drawing;
import org.jhotdraw.draw.DrawingEditor;
import org.jhotdraw.draw.Figure;
import org.jhotdraw.draw.event.FigureSelectionEvent;
import org.jhotdraw.draw.event.FigureSelectionListener;
import org.jhotdraw.draw.event.ToolAdapter;
import org.jhotdraw.draw.event.ToolEvent;
import org.jhotdraw.draw.tool.AbstractTool;
import org.jhotdraw.draw.tool.DelegationSelectionTool;

/**
 * A Swing implementation of {@link ImageCanvas}, which uses JHotDraw's
 * {@link DefaultDrawingView} component to do most of the work.
 * 
 * @author Curtis Rueden
 * @author Lee Kamentsky
 */
public class JHotDrawImageCanvas extends JPanel implements ImageCanvas,
	AdjustmentListener
{

	private final CanvasHelper canvasHelper;

	private final Drawing drawing;
	private final DefaultDrawingView drawingView;
	private final DrawingEditor drawingEditor;

	private final SwingImageDisplay display;
	private final JScrollPane scrollPane;

	private final EventSubscriber<ToolActivatedEvent> toolActivatedSubscriber =
		new EventSubscriber<ToolActivatedEvent>()
	{
		@Override
		public void onEvent(ToolActivatedEvent event) {
			onToolActivatedEvent(event);
		}
	};
	
	private final EventSubscriber<DisplayViewSelectedEvent> displayViewSelectedEvent = 
		new EventSubscriber<DisplayViewSelectedEvent> () {

			@Override
			public void onEvent(DisplayViewSelectedEvent event) {
				onViewSelected(event);
			}
		
	};

	private final EventSubscriber<DisplayViewDeselectedEvent> displayViewDeselectedEvent = 
		new EventSubscriber<DisplayViewDeselectedEvent> () {

			@Override
			public void onEvent(DisplayViewDeselectedEvent event) {
				onViewDeselected(event);
			}
		
	};
	
	public JHotDrawImageCanvas(final SwingImageDisplay display) {
		this.display = display; 
		canvasHelper = new CanvasHelper(this);

		drawing = new DefaultDrawing(); // or QuadTreeDrawing?

		drawingView = new DefaultDrawingView();
		drawingView.setDrawing(drawing);

		drawingEditor = new DefaultDrawingEditor();
		drawingEditor.add(drawingView);
		drawingEditor.setTool(new DelegationSelectionTool());

		scrollPane = new JScrollPane(drawingView);
		setLayout(new BorderLayout());
		add(scrollPane, BorderLayout.CENTER);

		scrollPane.getHorizontalScrollBar().addAdjustmentListener(this);
		scrollPane.getVerticalScrollBar().addAdjustmentListener(this);

		Events.subscribe(ToolActivatedEvent.class, toolActivatedSubscriber);
		Events.subscribe(DisplayViewSelectedEvent.class, displayViewSelectedEvent);
		Events.subscribe(DisplayViewDeselectedEvent.class, displayViewDeselectedEvent);
		
		drawingView.addFigureSelectionListener(new FigureSelectionListener() {
			
			@Override
			public void selectionChanged(FigureSelectionEvent event) {
				onFigureSelectionChanged(event);
			}
		});
	}
	
	/**
	 * Respond to the JHotDraw figure selection event by selecting and
	 * deselecting views whose state has changed
	 * 
	 * @param event - event indicating that the figure selections have changed
	 */
	protected void onFigureSelectionChanged(FigureSelectionEvent event) {
		Set<Figure> newSelection = event.getNewSelection();
		Set<Figure> oldSelection = event.getOldSelection();
		for (DisplayView view:display.getViews()) {
			if (view instanceof FigureView) {
				Figure figure = ((FigureView)view).getFigure();
				if (newSelection.contains(figure)) {
					if (! oldSelection.contains(figure)) {
						view.setSelected(true);
					}
				} else if (oldSelection.contains(figure)) {
					view.setSelected(false);
				}
			}
		}
	}
	
	protected void onViewSelected(DisplayViewSelectedEvent event) {
		DisplayView view = event.getDisplayView();
		if ((view.getDisplay() == display) && (view instanceof FigureView)) {
			Figure figure = ((FigureView)view).getFigure();
			if (! drawingView.getSelectedFigures().contains(figure)) {
				drawingView.addToSelection(figure);
			}
		}
	}
	
	protected void onViewDeselected(DisplayViewDeselectedEvent event) {
		DisplayView view = event.getDisplayView();
		if ((view.getDisplay() == display) && (view instanceof FigureView)) {
			Figure figure = ((FigureView)view).getFigure();
			if (drawingView.getSelectedFigures().contains(figure)) {
				drawingView.removeFromSelection(figure);
			}
		}
	}
	
	protected void onToolActivatedEvent(ToolActivatedEvent event) {
		final ITool iTool = event.getTool();
		if (iTool instanceof IJHotDrawOverlayAdapter) {
			final IJHotDrawOverlayAdapter adapter = (IJHotDrawOverlayAdapter)iTool;
			final IJCreationTool creationTool = new IJCreationTool(adapter);

			// Listen for toolDone from the creation tool. This means that
			// we finished using the JHotDraw tool and we deactivate it.
			creationTool.addToolListener(new ToolAdapter() {
				@Override
				public void toolDone(final ToolEvent e) {
					final ToolManager toolManager = ImageJ.get(ToolManager.class);
					toolManager.setActiveTool(new SelectionTool());
				}
			});

			// When the tool creates an overlay, add the
			// overlay/figure combo to a SwingOverlayView.
			creationTool.addOverlayCreatedListener(
				new IJCreationTool.OverlayCreatedListener()
			{
				@SuppressWarnings("synthetic-access")
				@Override
				public void overlayCreated(final FigureCreatedEvent e) {
					final Overlay overlay = e.getOverlay();
					final SwingOverlayView v = new SwingOverlayView(display,
						overlay, e.getFigure());
					final SwingDisplayWindow window = display.getDisplayWindow();
					overlay.setAxis(Axes.X, Axes.X.ordinal());
					overlay.setAxis(Axes.Y, Axes.Y.ordinal());
					for (int i=2; i<display.numDimensions(); i++) {
						Axis axis = display.axis(i);
						if (overlay.getAxisIndex(axis) < 0) {
							overlay.setPosition(axis, window.getAxisPosition(axis));
						}
					}
					display.addView(v);
					if (drawingView.getSelectedFigures().contains(e.getFigure())) {
						v.setSelected(true);
					}
				}
			});
			drawingEditor.setTool(creationTool);
		}
		else if (iTool instanceof SelectionTool) {
			drawingEditor.setTool(new DelegationSelectionTool());
		}
		else {
			// use a dummy tool so that JHotDraw ignores input events
			drawingEditor.setTool(new AbstractTool() {
				@Override
				public void mouseDragged(MouseEvent e) {
					// do nothing
				}
			});
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

	// -- ImageCanvas methods --

	@Override
	public int getCanvasWidth() {
		// NB: Return *unscaled* canvas width.
		return (int) (drawingView.getPreferredSize().width / getZoomFactor());
	}

	@Override
	public int getCanvasHeight() {
		// NB: Return *unscaled* canvas height.
		return (int) (drawingView.getPreferredSize().height / getZoomFactor());
	}

	@Override
	public int getViewportWidth() {
		return drawingView.getWidth();
	}

	@Override
	public int getViewportHeight() {
		return drawingView.getHeight();
	}

	@Override
	public void addEventDispatcher(final EventDispatcher dispatcher) {
		final AWTEventDispatcher awtDispatcher = (AWTEventDispatcher) dispatcher;
		drawingView.addKeyListener(awtDispatcher);
		drawingView.addMouseListener(awtDispatcher);
		drawingView.addMouseMotionListener(awtDispatcher);
		drawingView.addMouseWheelListener(awtDispatcher);
	}

	@Override
	public boolean isInImage(final IntCoords point) {
		return canvasHelper.isInImage(point);
	}

	@Override
	public RealCoords panelToImageCoords(final IntCoords panelCoords) {
		final Point viewCoords = new Point(panelCoords.x, panelCoords.y);
		final Point2D.Double drawCoords = drawingView.viewToDrawing(viewCoords);
		return new RealCoords(drawCoords.x, drawCoords.y);
	}

	@Override
	public IntCoords imageToPanelCoords(final RealCoords imageCoords) {
		final Point2D.Double drawCoords =
			new Point2D.Double(imageCoords.x, imageCoords.y);
		final Point viewCoords = drawingView.drawingToView(drawCoords);
		return new IntCoords(viewCoords.x, viewCoords.y);
	}

	@Override
	public void setCursor(final MouseCursor cursor) {
		drawingView.setCursor(AWTCursors.getCursor(cursor));
	}

	// -- Pannable methods --

	@Override
	public void pan(final IntCoords delta) {
		canvasHelper.pan(delta);
		syncPan();
	}

	@Override
	public void setPan(final IntCoords origin) {
		canvasHelper.setPan(origin);
		syncPan();
	}

	@Override
	public void panReset() {
		canvasHelper.panReset();
	}

	@Override
	public IntCoords getPanOrigin() {
		return canvasHelper.getPanOrigin();
	}

	// -- Zoomable methods --

	@Override
	public void setZoom(final double factor) {
		canvasHelper.setZoom(factor);
	}

	@Override
	public void setZoom(final double factor, final IntCoords center) {
		canvasHelper.setZoom(factor, center);
		syncZoom();
		syncPan();
	}

	@Override
	public void zoomIn() {
		canvasHelper.zoomIn();
	}

	@Override
	public void zoomIn(final IntCoords center) {
		canvasHelper.zoomIn(center);
	}

	@Override
	public void zoomOut() {
		canvasHelper.zoomOut();
	}

	@Override
	public void zoomOut(final IntCoords center) {
		canvasHelper.zoomOut(center);
	}

	@Override
	public void zoomToFit(final IntCoords topLeft, final IntCoords bottomRight) {
		canvasHelper.zoomToFit(topLeft, bottomRight);
	}

	@Override
	public double getZoomFactor() {
		return canvasHelper.getZoomFactor();
	}

	@Override
	public void setZoomStep(final double zoomStep) {
		canvasHelper.setZoomStep(zoomStep);
	}

	@Override
	public double getZoomStep() {
		return canvasHelper.getZoomStep();
	}

	// -- AdjustmentListener methods --

	@Override
	public void adjustmentValueChanged(final AdjustmentEvent e) {
		final Point viewPos = scrollPane.getViewport().getViewPosition();
		canvasHelper.setPan(new IntCoords(viewPos.x, viewPos.y));
	}

	// -- Helper methods --

	private void syncPan() {
		final Point viewPos = scrollPane.getViewport().getViewPosition();
		final IntCoords origin = canvasHelper.getPanOrigin();
		if (viewPos.x == origin.x && viewPos.y == origin.y) return; // no change
		constrainOrigin(origin);
		scrollPane.getViewport().setViewPosition(new Point(origin.x, origin.y));
	}

	private void syncZoom() {
		drawingView.setScaleFactor(canvasHelper.getZoomFactor());
	}

	private void constrainOrigin(final IntCoords origin) {
		if (origin.x < 0) origin.x = 0;
		if (origin.y < 0) origin.y = 0;
		final Dimension viewportSize = scrollPane.getViewport().getSize();
		final Dimension canvasSize = drawingView.getSize();
		final int xMax = canvasSize.width - viewportSize.width;
		final int yMax = canvasSize.height - viewportSize.height;
		if (origin.x > xMax) origin.x = xMax;
		if (origin.y > yMax) origin.y = yMax;
	}

}
