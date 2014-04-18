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
import imagej.plugins.uis.swing.overlay.IJCreationTool;
import imagej.plugins.uis.swing.overlay.JHotDrawAdapter;
import imagej.plugins.uis.swing.overlay.JHotDrawTool;
import imagej.ui.swing.tools.SwingAngleTool;

import java.awt.Shape;
import java.awt.geom.Point2D;

import net.imagej.display.ImageDisplay;
import net.imagej.display.OverlayView;
import net.imagej.overlay.AngleOverlay;
import net.imagej.overlay.Overlay;

import org.jhotdraw.draw.BezierFigure;
import org.jhotdraw.draw.Figure;
import org.jhotdraw.geom.BezierPath;
import org.scijava.plugin.Plugin;
import org.scijava.tool.Tool;

/**
 * JHotDraw adapter for angle overlays.
 * 
 * @author Lee Kamentsky
 * @author Barry DeZonia
 * @see SwingAngleTool
 */
@Plugin(type = JHotDrawAdapter.class, priority = SwingAngleTool.PRIORITY)
public class AngleJHotDrawAdapter extends
	AbstractJHotDrawAdapter<AngleOverlay, AngleJHotDrawAdapter.AngleFigure>
{

	// -- JHotDrawAdapter methods --

	@Override
	public boolean supports(final Tool tool) {
		return tool instanceof SwingAngleTool;
	}

	@Override
	public boolean supports(final Overlay overlay, final Figure figure) {
		if (!(overlay instanceof AngleOverlay)) return false;
		return figure == null || figure instanceof AngleFigure;
	}

	@Override
	public AngleOverlay createNewOverlay() {
		final AngleOverlay overlay = new AngleOverlay(getContext());
		return overlay;
	}

	@Override
	public Figure createDefaultFigure() {
		final AngleFigure figure = new AngleFigure();
		initDefaultSettings(figure);
		return figure;
	}

	@Override
	public void updateFigure(final OverlayView overlayView,
		final AngleFigure figure)
	{
		super.updateFigure(overlayView, figure);
		final Overlay overlay = overlayView.getData();
		assert overlay instanceof AngleOverlay;
		final AngleOverlay angleOverlay = (AngleOverlay) overlay;
		double ptX, ptY;
		ptX = angleOverlay.getPoint1(0);
		ptY = angleOverlay.getPoint1(1);
		figure.setEndPoint1(ptX, ptY);
		ptX = angleOverlay.getCenter(0);
		ptY = angleOverlay.getCenter(1);
		figure.setCenterPoint(ptX, ptY);
		ptX = angleOverlay.getPoint2(0);
		ptY = angleOverlay.getPoint2(1);
		figure.setEndPoint2(ptX, ptY);
	}

	@Override
	public void updateOverlay(final AngleFigure figure,
		final OverlayView overlayView)
	{
		super.updateOverlay(figure, overlayView);
		final Overlay overlay = overlayView.getData();
		assert overlay instanceof AngleOverlay;
		final AngleOverlay angleOverlay = (AngleOverlay) overlay;
		Point2D.Double figPt;
		final double[] ovPt = new double[angleOverlay.numDimensions()];
		figPt = figure.getEndPoint1();
		ovPt[0] = figPt.x;
		ovPt[1] = figPt.y;
		for (int i = 0; i < ovPt.length; i++)
			angleOverlay.setPoint1(ovPt[i], i);
		figPt = figure.getCenterPoint();
		ovPt[0] = figPt.x;
		ovPt[1] = figPt.y;
		for (int i = 0; i < ovPt.length; i++)
			angleOverlay.setCenter(ovPt[i], i);
		figPt = figure.getEndPoint2();
		ovPt[0] = figPt.x;
		ovPt[1] = figPt.y;
		for (int i = 0; i < ovPt.length; i++)
			angleOverlay.setPoint2(ovPt[i], i);
		angleOverlay.update();
	}

	@Override
	public JHotDrawTool getCreationTool(final ImageDisplay display) {
		return new IJCreationTool<AngleFigure>(display, this);
	}

	protected class AngleFigure extends BezierFigure {

		public AngleFigure() {
			// coords have no effect on initial placement in window
			addNode(new BezierPath.Node(6, 1));
			addNode(new BezierPath.Node(1, 1));
			addNode(new BezierPath.Node(1, 6));
			setConnectable(false);
		}

		public void setEndPoint1(final double x, final double y) {
			setPoint(0, point(x, y));
		}

		public void setCenterPoint(final double x, final double y) {
			setPoint(1, point(x, y));
		}

		public void setEndPoint2(final double x, final double y) {
			setPoint(2, point(x, y));
		}

		public Point2D.Double getEndPoint1() {
			return getPoint(0);
		}

		public Point2D.Double getCenterPoint() {
			return getPoint(1);
		}

		public Point2D.Double getEndPoint2() {
			return getPoint(2);
		}

		// -- helpers --

		private Point2D.Double point(double x, double y) {
			return new Point2D.Double(x, y);
		}

	}

	@Override
	public Shape toShape(final AngleFigure figure) {
		throw new UnsupportedOperationException();
	}

}
