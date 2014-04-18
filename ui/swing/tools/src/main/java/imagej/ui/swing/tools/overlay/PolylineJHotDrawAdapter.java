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

import imagej.plugins.uis.swing.overlay.AbstractJHotDrawAdapter;
import imagej.plugins.uis.swing.overlay.IJBezierTool;
import imagej.plugins.uis.swing.overlay.JHotDrawAdapter;
import imagej.plugins.uis.swing.overlay.JHotDrawTool;
import imagej.ui.swing.tools.SwingPolylineTool;

import java.awt.Shape;
import java.awt.geom.PathIterator;

import net.imagej.display.ImageDisplay;
import net.imagej.display.OverlayView;
import net.imagej.overlay.GeneralPathOverlay;
import net.imagej.overlay.Overlay;
import net.imglib2.roi.GeneralPathRegionOfInterest;

import org.jhotdraw.draw.BezierFigure;
import org.jhotdraw.draw.Figure;
import org.jhotdraw.geom.BezierPath.Node;
import org.scijava.log.LogService;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.tool.Tool;

/**
 * JHotDraw adapter for multi-segmented lines
 * 
 * @author Benjamin Nanes
 */
@Plugin(type = JHotDrawAdapter.class, priority = SwingPolylineTool.PRIORITY)
public class PolylineJHotDrawAdapter extends
	AbstractJHotDrawAdapter<GeneralPathOverlay, BezierFigure>
{

	@Parameter(required = false)
	private LogService log;

	// -- JHotDrawAdapter methods --

	@Override
	public boolean supports(final Tool tool) {
		return tool instanceof SwingPolylineTool;
	}

	@Override
	public boolean supports(final Overlay overlay, final Figure figure) {
		if (!(overlay instanceof GeneralPathOverlay)) return false;
		return figure == null || figure instanceof PolylineFigure;
	}

	@Override
	public Overlay createNewOverlay() {
		return new GeneralPathOverlay(getContext());
	}

	@Override
	public Figure createDefaultFigure() {
		final BezierFigure figure = new PolylineFigure();
		initDefaultSettings(figure);
		return figure;
	}

	@Override
	public void updateOverlay(final BezierFigure figure, final OverlayView view) {
		super.updateOverlay(figure, view);
		assert view.getData() instanceof GeneralPathOverlay;
		final GeneralPathOverlay gpo = (GeneralPathOverlay) view.getData();
		final GeneralPathRegionOfInterest gpr = gpo.getRegionOfInterest();
		gpr.reset();
		for (int i = 0; i < figure.getNodeCount(); i++) {
			final Node n = figure.getNode(i);
			if (i == 0) gpr.moveTo(n.x[0], n.y[0]);
			else gpr.lineTo(n.x[0], n.y[0]);
		}
		gpo.update();
	}

	@Override
	public void updateFigure(final OverlayView view, final BezierFigure figure) {
		super.updateFigure(view, figure);
		assert view.getData() instanceof GeneralPathOverlay;
		final GeneralPathOverlay gpo = (GeneralPathOverlay) view.getData();
		final GeneralPathRegionOfInterest gpr = gpo.getRegionOfInterest();
		final PathIterator pi = gpr.getGeneralPath().getPathIterator(null);
		final int nCount = figure.getNodeCount();
		int i = 0;
		final double[] pos = new double[6];
		while (!pi.isDone()) {
			pi.currentSegment(pos);
			final Node n = new Node(pos[0], pos[1]);
			if (i < nCount) figure.getNode(i).setTo(n);
			else figure.addNode(n);
			pi.next();
			i++;
		}
		while (i < figure.getNodeCount())
			figure.removeNode(i);
	}

	@Override
	public JHotDrawTool getCreationTool(final ImageDisplay display) {
		return new IJBezierTool(display, this);
	}

	@Override
	public Shape toShape(final BezierFigure figure) {
		return figure.getBezierPath().toGeneralPath();
	}

}
