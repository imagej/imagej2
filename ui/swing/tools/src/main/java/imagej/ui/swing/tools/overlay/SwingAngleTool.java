/*
 * #%L
 * ImageJ software for multidimensional image processing and analysis.
 * %%
 * Copyright (C) 2009 - 2013 Board of Regents of the University of
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

package imagej.ui.swing.tools.overlay;

import imagej.data.display.ImageDisplay;
import imagej.data.display.OverlayView;
import imagej.data.overlay.AngleOverlay;
import imagej.data.overlay.Overlay;
import imagej.plugin.Plugin;
import imagej.ui.swing.overlay.AbstractJHotDrawAdapter;
import imagej.ui.swing.overlay.IJCreationTool;
import imagej.ui.swing.overlay.JHotDrawAdapter;
import imagej.ui.swing.overlay.JHotDrawTool;

import java.awt.Shape;
import java.awt.geom.Point2D;

import org.jhotdraw.draw.BezierFigure;
import org.jhotdraw.draw.Figure;
import org.jhotdraw.geom.BezierPath;

/**
 * TODO
 * 
 * @author Lee Kamentsky
 * @author Barry DeZonia
 */
@Plugin(type = JHotDrawAdapter.class, name = "Angle",
	description = "Angle overlays", iconPath = "/icons/tools/angle.png",
	priority = SwingAngleTool.PRIORITY)
public class SwingAngleTool extends AbstractJHotDrawAdapter<AngleOverlay, SwingAngleTool.AngleFigure> {

	public static final double PRIORITY = SwingLineTool.PRIORITY - 1;

	@Override
	public boolean supports(final Overlay overlay, final Figure figure) {
		if (!(overlay instanceof AngleOverlay)) return false;
		return figure == null || figure instanceof AngleFigure;
	}

	@Override
	public AngleOverlay createNewOverlay() {
		AngleOverlay overlay = new AngleOverlay(getContext());
		/* no effect
		overlay.setEndPoint1(new RealPoint(new double[]{6,1}));
		overlay.setCenterPoint(new RealPoint(new double[]{1,1}));
		overlay.setEndPoint2(new RealPoint(new double[]{1,6}));
		*/
		return overlay;
	}

	@Override
	public Figure createDefaultFigure() {
		final AngleFigure figure = new AngleFigure();
		/* no effect
		figure.setEndPoint1(6,1);
		figure.setCenterPoint(1,1);
		figure.setEndPoint2(1,6);
		*/
		initDefaultSettings(figure);
		return figure;
	}

	@Override
	public void updateFigure(final OverlayView overlayView, final AngleFigure figure) {
		super.updateFigure(overlayView, figure);
		assert figure instanceof AngleFigure;
		final AngleFigure angleFig = (AngleFigure) figure;
		final Overlay overlay = overlayView.getData();
		assert overlay instanceof AngleOverlay;
		final AngleOverlay angleOverlay = (AngleOverlay) overlay;
		double ptX, ptY;
		ptX = angleOverlay.getPoint1(0);
		ptY = angleOverlay.getPoint1(1);
		angleFig.setEndPoint1(ptX, ptY);
		ptX = angleOverlay.getCenter(0);
		ptY = angleOverlay.getCenter(1);
		angleFig.setCenterPoint(ptX, ptY);
		ptX = angleOverlay.getPoint2(0);
		ptY = angleOverlay.getPoint2(1);
		angleFig.setEndPoint2(ptX, ptY);
	}

	@Override
	public void updateOverlay(final AngleFigure figure, final OverlayView overlayView)
	{
		super.updateOverlay(figure, overlayView);
		assert figure instanceof AngleFigure;
		final AngleFigure angleFig = (AngleFigure) figure;
		final Overlay overlay = overlayView.getData();
		assert overlay instanceof AngleOverlay;
		final AngleOverlay angleOverlay = (AngleOverlay) overlay;
		Point2D.Double figPt;
		double[] ovPt = new double[angleOverlay.numDimensions()];
		figPt = angleFig.getEndPoint1();
		ovPt[0] = figPt.x; ovPt[1] = figPt.y;
		for (int i = 0; i < ovPt.length; i++)
			angleOverlay.setPoint1(ovPt[i], i);
		figPt = angleFig.getCenterPoint();
		ovPt[0] = figPt.x; ovPt[1] = figPt.y;
		for (int i = 0; i < ovPt.length; i++)
			angleOverlay.setCenter(ovPt[i], i);
		figPt = angleFig.getEndPoint2();
		ovPt[0] = figPt.x; ovPt[1] = figPt.y;
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
			addNode(new BezierPath.Node(point(6,1)));
			addNode(new BezierPath.Node(point(1,1)));
			addNode(new BezierPath.Node(point(1,6)));
			/* no effect
			getNode(0).setControlPoint(0, point(6,1));
			getNode(1).setControlPoint(0, point(1,1));
			getNode(2).setControlPoint(0, point(1,6));
			*/
			setConnectable(false);
		}
		
		public void setEndPoint1(double x, double y) {
			setPoint(0, x, y);
		}
		
		public void setCenterPoint(double x, double y) {
			setPoint(1, x, y);
		}

		public void setEndPoint2(double x, double y) {
			setPoint(2, x, y);
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
		
		private void setPoint(int nodeNum, double x, double y) {
			getNode(nodeNum).setTo(new BezierPath.Node(point(x, y)));
		}
		
		private Point2D.Double point(double x, double y) {
			return new Point2D.Double(x, y);
		}

	}

	@Override
	public Shape toShape(final AngleFigure figure) {
		throw new UnsupportedOperationException();
	}

}
