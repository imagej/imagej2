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
import imagej.awt.AWTCursors;
import imagej.awt.AWTEventDispatcher;
import imagej.awt.AWTImageCanvas;
import imagej.display.CanvasHelper;
import imagej.display.EventDispatcher;
import imagej.display.ImageCanvas;
import imagej.display.MouseCursor;
import imagej.event.EventSubscriber;
import imagej.event.Events;
import imagej.tool.ITool;
import imagej.tool.ToolManager;
import imagej.tool.event.ToolActivatedEvent;
import imagej.ui.swing.tools.SelectionTool;
import imagej.ui.swing.tools.roi.IJCreationTool;
import imagej.ui.swing.tools.roi.IJCreationTool.OverlayCreatedEvent;
import imagej.ui.swing.tools.roi.IJHotDrawOverlayAdapter;
import imagej.util.IntCoords;
import imagej.util.Log;
import imagej.util.RealCoords;

import java.awt.BorderLayout;
import java.awt.Point;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;

import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.jhotdraw.draw.DefaultDrawing;
import org.jhotdraw.draw.DefaultDrawingEditor;
import org.jhotdraw.draw.DefaultDrawingView;
import org.jhotdraw.draw.Drawing;
import org.jhotdraw.draw.DrawingEditor;
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
public class JHotDrawImageCanvas extends JPanel implements AWTImageCanvas,
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
	}

	protected void onToolActivatedEvent(final ToolActivatedEvent event) {
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
				public void overlayCreated(final OverlayCreatedEvent e) {
					final SwingOverlayView v = new SwingOverlayView(display,
						e.getOverlay(), e.getFigure());
					display.addView(v);
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

	// -- AWTImageCanvas methods --

	@Override
	public void setImage(final BufferedImage newImage) {
		// TODO Auto-generated method stub
	}

	@Override
	public BufferedImage getImage() {
		// TODO Auto-generated method stub
		return null;
	}

	// -- ImageCanvas methods --

	@Override
	public int getImageWidth() {
		return drawingView.getPreferredSize().width;
	}

	@Override
	public int getImageHeight() {
		return drawingView.getPreferredSize().height;
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
		Log.debug("===> syncPan: origin => " + origin);
		scrollPane.getViewport().setViewPosition(new Point(origin.x, origin.y));
	}

	private void syncZoom() {
		drawingView.setScaleFactor(canvasHelper.getZoomFactor());
	}

}
