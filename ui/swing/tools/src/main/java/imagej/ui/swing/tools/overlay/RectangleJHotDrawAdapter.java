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
import imagej.data.overlay.Overlay;
import imagej.data.overlay.RectangleOverlay;
import imagej.plugins.uis.swing.overlay.AbstractJHotDrawAdapter;
import imagej.plugins.uis.swing.overlay.IJCreationTool;
import imagej.plugins.uis.swing.overlay.JHotDrawAdapter;
import imagej.plugins.uis.swing.overlay.JHotDrawTool;
import imagej.tool.Tool;
import imagej.tool.ToolService;
import imagej.ui.swing.tools.SwingRectangleTool;

import java.awt.Shape;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import org.jhotdraw.draw.Figure;
import org.jhotdraw.draw.RectangleFigure;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.util.RealCoords;

/**
 * JHotDraw adapter for rectangle overlays.
 * 
 * @author Lee Kamentsky
 * @author Grant Harris
 * @author Barry DeZonia
 * @see SwingRectangleTool
 */
@Plugin(type = JHotDrawAdapter.class, priority = SwingRectangleTool.PRIORITY)
public class RectangleJHotDrawAdapter extends
	AbstractJHotDrawAdapter<RectangleOverlay, RectangleFigure>
{

	protected static RectangleOverlay downcastOverlay(final Overlay roi) {
		assert roi instanceof RectangleOverlay;
		return (RectangleOverlay) roi;
	}

	@Parameter
	private ToolService toolService;

	// -- JHotDrawAdapter methods --

	@Override
	public boolean supports(final Tool tool) {
		return tool instanceof SwingRectangleTool;
	}

	@Override
	public boolean supports(final Overlay overlay, final Figure figure) {
		if (!(overlay instanceof RectangleOverlay)) return false;
		return figure == null || figure instanceof RectangleFigure;
	}

	@Override
	public Overlay createNewOverlay() {
		return new RectangleOverlay(getContext());
	}

	@Override
	public Figure createDefaultFigure() {
		final RectangleFigure figure = new RectangleFigure();
		initDefaultSettings(figure);
		return figure;
	}

	@Override
	public void
		updateFigure(final OverlayView view, final RectangleFigure figure)
	{
		super.updateFigure(view, figure);
		final RectangleOverlay overlay = downcastOverlay(view.getData());
		final double x0 = overlay.getOrigin(0);
		final double y0 = overlay.getOrigin(1);
		final double w = overlay.getExtent(0);
		final double h = overlay.getExtent(1);
		final Point2D.Double anch = new Point2D.Double(x0, y0);
		final Point2D.Double lead = new Point2D.Double(x0 + w, y0 + h);
		figure.setBounds(anch, lead);
	}

	@Override
	public void updateOverlay(final RectangleFigure figure,
		final OverlayView view)
	{
		super.updateOverlay(figure, view);
		final RectangleOverlay overlay = downcastOverlay(view.getData());
		final Rectangle2D.Double bounds = figure.getBounds();
		final double x = bounds.getMinX();
		final double y = bounds.getMinY();
		final double w = bounds.getWidth();
		final double h = bounds.getHeight();
		overlay.setOrigin(x, 0);
		overlay.setOrigin(y, 1);
		overlay.setExtent(w, 0);
		overlay.setExtent(h, 1);
		overlay.update();
		toolService.reportRectangle(x, y, w, h);
	}

	@Override
	public JHotDrawTool getCreationTool(final ImageDisplay display) {
		return new IJCreationTool<RectangleFigure>(display, this);
	}

	@Override
	public void report(final RealCoords p1, final RealCoords p2) {
		toolService.reportRectangle(p1, p2);
	}

	@Override
	public Shape toShape(final RectangleFigure figure) {
		return figure.getBounds();
	}

}
