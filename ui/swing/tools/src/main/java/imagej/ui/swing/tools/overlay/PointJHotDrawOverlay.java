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
import imagej.data.overlay.PointOverlay;
import imagej.plugins.uis.swing.overlay.AbstractJHotDrawAdapter;
import imagej.plugins.uis.swing.overlay.IJCreationTool;
import imagej.plugins.uis.swing.overlay.JHotDrawAdapter;
import imagej.plugins.uis.swing.overlay.JHotDrawTool;
import imagej.tool.Tool;
import imagej.ui.swing.tools.SwingPointTool;

import java.awt.Shape;

import org.jhotdraw.draw.Figure;
import org.scijava.plugin.Plugin;
import org.scijava.util.ColorRGB;

/**
 * JHotDraw adapter for point tool.
 * 
 * @author Barry DeZonia
 * @see SwingPointTool
 */
@Plugin(type = JHotDrawAdapter.class, priority = SwingPointTool.PRIORITY)
public class PointJHotDrawOverlay extends
	AbstractJHotDrawAdapter<PointOverlay, PointFigure>
{

	// -- JHotDrawAdapter methods --

	@Override
	public boolean supports(final Tool tool) {
		return tool instanceof SwingPointTool;
	}

	@Override
	public boolean supports(final Overlay overlay, final Figure figure) {
		if (!(overlay instanceof PointOverlay)) return false;
		return figure == null || figure instanceof PointFigure;
	}

	@Override
	public PointOverlay createNewOverlay() {
		return new PointOverlay(getContext());
	}

	@Override
	public Figure createDefaultFigure() {
		final PointFigure figure = new PointFigure();
		initDefaultSettings(figure);
		return figure;
	}

	@Override
	public void updateFigure(final OverlayView view, final PointFigure figure) {
		super.updateFigure(view, figure);
		final PointFigure pointFigure = figure;
		final Overlay overlay = view.getData();
		assert overlay instanceof PointOverlay;
		final PointOverlay pointOverlay = (PointOverlay) overlay;
		pointFigure.setFillColor(pointOverlay.getFillColor());
		pointFigure.setLineColor(pointOverlay.getLineColor());
		pointFigure.setPoints(pointOverlay.getPoints());
	}

	@Override
	public void updateOverlay(final PointFigure figure, final OverlayView view) {
		final Overlay overlay = view.getData();
		assert overlay instanceof PointOverlay;
		final PointOverlay pointOverlay = (PointOverlay) overlay;
		// do not let call to super.updateOverlay() mess with drawing attributes
		// so save colors
		final ColorRGB fillColor = overlay.getFillColor();
		final ColorRGB lineColor = overlay.getLineColor();
		// call super in case it initializes anything of importance
		super.updateOverlay(figure, view);
		// and restore colors to what we really want
		pointOverlay.setFillColor(fillColor);
		pointOverlay.setLineColor(lineColor);
		// set points
		pointOverlay.setPoints(figure.getPoints());
		pointOverlay.update();
	}

	@Override
	public JHotDrawTool getCreationTool(final ImageDisplay display) {
		return new IJCreationTool<PointFigure>(display, this);
	}

	@Override
	public Shape toShape(final PointFigure figure) {
		throw new UnsupportedOperationException();
	}

}
