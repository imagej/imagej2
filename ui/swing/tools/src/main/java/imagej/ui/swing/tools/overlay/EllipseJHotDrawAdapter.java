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
import imagej.data.overlay.EllipseOverlay;
import imagej.data.overlay.Overlay;
import imagej.plugins.uis.swing.overlay.AbstractJHotDrawAdapter;
import imagej.plugins.uis.swing.overlay.IJCreationTool;
import imagej.plugins.uis.swing.overlay.JHotDrawAdapter;
import imagej.plugins.uis.swing.overlay.JHotDrawTool;
import imagej.tool.Tool;
import imagej.tool.ToolService;
import imagej.ui.swing.tools.SwingEllipseTool;

import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import org.jhotdraw.draw.EllipseFigure;
import org.jhotdraw.draw.Figure;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.util.RealCoords;

/**
 * JHotDraw adapter for ellipse overlays.
 * 
 * @author Lee Kamentsky
 * @author Barry DeZonia
 * @see SwingEllipseTool
 */
@Plugin(type = JHotDrawAdapter.class, priority = SwingEllipseTool.PRIORITY)
public class EllipseJHotDrawAdapter extends
	AbstractJHotDrawAdapter<EllipseOverlay, EllipseFigure>
{

	protected static EllipseOverlay downcastOverlay(final Overlay roi) {
		assert roi instanceof EllipseOverlay;
		return (EllipseOverlay) roi;
	}

	protected static EllipseFigure downcastFigure(final Figure figure) {
		assert figure instanceof EllipseFigure;
		return (EllipseFigure) figure;
	}

	@Parameter
	private ToolService toolService;

	// -- JHotDrawAdapter methods --

	@Override
	public boolean supports(final Tool tool) {
		return tool instanceof SwingEllipseTool;
	}

	@Override
	public boolean supports(final Overlay overlay, final Figure figure) {
		if (!(overlay instanceof EllipseOverlay)) return false;
		return figure == null || figure instanceof EllipseFigure;
	}

	@Override
	public Overlay createNewOverlay() {
		return new EllipseOverlay(getContext());
	}

	@Override
	public Figure createDefaultFigure() {
		final EllipseFigure figure = new EllipseFigure();
		initDefaultSettings(figure);
		return figure;
	}

	@Override
	public void updateFigure(final OverlayView view, final EllipseFigure figure)
	{
		super.updateFigure(view, figure);
		final EllipseOverlay overlay = downcastOverlay(view.getData());
		final double centerX = overlay.getOrigin(0);
		final double centerY = overlay.getOrigin(1);
		final double radiusX = overlay.getRadius(0);
		final double radiusY = overlay.getRadius(1);

		figure.setBounds(new Point2D.Double(centerX - radiusX, centerY - radiusY),
			new Point2D.Double(centerX + radiusX, centerY + radiusY));
	}

	@Override
	public void
		updateOverlay(final EllipseFigure figure, final OverlayView view)
	{
		super.updateOverlay(figure, view);
		final EllipseOverlay overlay = downcastOverlay(view.getData());
		final Rectangle2D.Double bounds = figure.getBounds();
		final double x = bounds.getMinX();
		final double y = bounds.getMinY();
		final double w = bounds.getWidth();
		final double h = bounds.getHeight();
		overlay.setOrigin(x + w / 2, 0);
		overlay.setOrigin(y + h / 2, 1);
		overlay.setRadius(w / 2, 0);
		overlay.setRadius(h / 2, 1);
		overlay.update();
		toolService.reportRectangle(x, y, w, h);
	}

	@Override
	public JHotDrawTool getCreationTool(final ImageDisplay display) {
		return new IJCreationTool<EllipseFigure>(display, this);
	}

	@Override
	public void report(final RealCoords p1, final RealCoords p2) {
		toolService.reportRectangle(p1, p2);
	}

	@Override
	public Shape toShape(final EllipseFigure figure) {
		final Rectangle2D.Double bounds = figure.getBounds();
		return new Ellipse2D.Double(bounds.x, bounds.y, bounds.width,
			bounds.height);
	}

}
