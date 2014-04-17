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

package imagej.ui.swing.tools.overlay;

import imagej.data.display.ImageDisplay;
import imagej.data.display.OverlayView;
import imagej.data.overlay.LineOverlay;
import imagej.data.overlay.Overlay;
import imagej.plugins.uis.swing.overlay.AbstractJHotDrawAdapter;
import imagej.plugins.uis.swing.overlay.IJCreationTool;
import imagej.plugins.uis.swing.overlay.JHotDrawAdapter;
import imagej.plugins.uis.swing.overlay.JHotDrawTool;
import imagej.tool.Tool;
import imagej.tool.ToolService;
import imagej.ui.swing.tools.SwingLineTool;

import java.awt.Shape;
import java.awt.geom.Point2D;

import org.jhotdraw.draw.Figure;
import org.jhotdraw.draw.LineFigure;
import org.jhotdraw.geom.BezierPath.Node;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.util.RealCoords;

/**
 * JHotDraw adapter for line overlays.
 * 
 * @author Lee Kamentsky
 * @author Barry DeZonia
 * @see SwingLineTool
 */
@Plugin(type = JHotDrawAdapter.class, priority = SwingLineTool.PRIORITY)
public class LineJHotDrawAdapter extends
	AbstractJHotDrawAdapter<LineOverlay, LineFigure>
{

	@Parameter
	private ToolService toolService;

	// -- JHotDrawAdapter methods --

	@Override
	public boolean supports(final Tool tool) {
		return tool instanceof SwingLineTool;
	}

	@Override
	public boolean supports(final Overlay overlay, final Figure figure) {
		if (!(overlay instanceof LineOverlay)) return false;
		return figure == null || figure instanceof LineFigure;
	}

	@Override
	public LineOverlay createNewOverlay() {
		return new LineOverlay(getContext());
	}

	@Override
	public Figure createDefaultFigure() {
		final LineFigure figure = new LineFigure();
		initDefaultSettings(figure);
		return figure;
	}

	@Override
	public void updateFigure(final OverlayView view, final LineFigure figure) {
		super.updateFigure(view, figure);
		final LineFigure lineFigure = figure;
		final Overlay overlay = view.getData();
		assert overlay instanceof LineOverlay;
		final LineOverlay lineOverlay = (LineOverlay) overlay;
		final double pt1X = lineOverlay.getLineStart(0);
		final double pt1Y = lineOverlay.getLineStart(1);
		final double pt2X = lineOverlay.getLineEnd(0);
		final double pt2Y = lineOverlay.getLineEnd(1);
		lineFigure.setStartPoint(new Point2D.Double(pt1X, pt1Y));
		lineFigure.setEndPoint(new Point2D.Double(pt2X, pt2Y));
	}

	@Override
	public void updateOverlay(final LineFigure figure, final OverlayView view) {
		super.updateOverlay(figure, view);
		final LineFigure line = figure;
		final Overlay overlay = view.getData();
		assert overlay instanceof LineOverlay;
		final LineOverlay lineOverlay = (LineOverlay) overlay;
		final Node startNode = line.getNode(0);
		final double x1 = startNode.getControlPoint(0).x;
		final double y1 = startNode.getControlPoint(0).y;
		lineOverlay.setLineStart(x1, 0);
		lineOverlay.setLineStart(y1, 1);
		final Node endNode = line.getNode(1);
		final double x2 = endNode.getControlPoint(0).x;
		final double y2 = endNode.getControlPoint(0).y;
		lineOverlay.setLineEnd(x2, 0);
		lineOverlay.setLineEnd(y2, 1);
		lineOverlay.update();
		toolService.reportLine(x1, y1, x2, y2);
	}

	@Override
	public JHotDrawTool getCreationTool(final ImageDisplay display) {
		return new IJCreationTool<LineFigure>(display, this);
	}

	@Override
	public void report(final RealCoords p1, final RealCoords p2) {
		toolService.reportLine(p1, p2);
	}

	@Override
	public Shape toShape(final LineFigure figure) {
		return figure.getBezierPath().toGeneralPath();
	}

}
