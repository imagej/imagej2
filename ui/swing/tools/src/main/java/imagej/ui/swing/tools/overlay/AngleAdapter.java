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

package imagej.ui.swing.tools.overlay;

import imagej.data.display.ImageDisplay;
import imagej.data.display.OverlayView;
import imagej.data.overlay.AngleOverlay;
import imagej.data.overlay.Overlay;
import imagej.ext.plugin.Plugin;
import imagej.ext.tool.Tool;
import imagej.ui.swing.overlay.IJCreationTool;
import imagej.ui.swing.overlay.JHotDrawOverlayAdapter;
import imagej.ui.swing.overlay.JHotDrawTool;
import imagej.ui.swing.overlay.OverlayCreatedListener;

import java.awt.Graphics2D;
import java.awt.geom.Point2D;

import net.imglib2.RealLocalizable;
import net.imglib2.RealPoint;

import org.jhotdraw.draw.AttributeKeys;
import org.jhotdraw.draw.BezierFigure;
import org.jhotdraw.draw.Figure;
import org.jhotdraw.geom.BezierPath;
import org.jhotdraw.geom.BezierPath.Node;

/**
 * TODO
 * 
 * @author Lee Kamentsky
 * @author Barry DeZonia
 */
@Plugin(type = Tool.class, name = "Angle",
	description = "Angle overlays", iconPath = "/icons/tools/angle.png",
	priority = AngleAdapter.PRIORITY, enabled = true)
@JHotDrawOverlayAdapter(priority = AngleAdapter.PRIORITY)
public class AngleAdapter extends AbstractJHotDrawOverlayAdapter<AngleOverlay> {

	public static final int PRIORITY = LineAdapter.PRIORITY - 1;

	@Override
	public boolean supports(final Overlay overlay, final Figure figure) {
		if (!(overlay instanceof AngleOverlay)) return false;
		return (figure == null) || (figure instanceof AngleFigure);
	}

	@Override
	public AngleOverlay createNewOverlay() {
		return new AngleOverlay(getContext());
	}

	@Override
	public Figure createDefaultFigure() {
		@SuppressWarnings("serial")
		final AngleFigure figure = new AngleFigure() {

			// Make sure that the lines are always drawn 1 pixel wide
			@Override
			public void draw(final Graphics2D g) {
				Double width = new Double(1 / g.getTransform().getScaleX());
				set(AttributeKeys.STROKE_WIDTH, width);
				super.draw(g);
			}
		};
		figure.set(AttributeKeys.STROKE_COLOR, getDefaultStrokeColor());
		// Avoid IllegalArgumentException: miter limit < 1 on the EDT
		figure.set(AttributeKeys.IS_STROKE_MITER_LIMIT_FACTOR, false);
		return figure;
	}

	@Override
	public void updateFigure(final OverlayView overlayView, final Figure figure) {
		super.updateFigure(overlayView, figure);
		assert figure instanceof AngleFigure;
		final AngleFigure angleFig = (AngleFigure) figure;
		final Overlay overlay = overlayView.getData();
		assert overlay instanceof AngleOverlay;
		final AngleOverlay angleOverlay = (AngleOverlay) overlay;
		RealLocalizable overlayPt = angleOverlay.getEndPoint1();
		angleFig.setEndPoint1(overlayPt.getDoublePosition(0), overlayPt.getDoublePosition(1));
		overlayPt = angleOverlay.getCenterPoint();
		angleFig.setCenterPoint(overlayPt.getDoublePosition(0), overlayPt.getDoublePosition(1));
		overlayPt = angleOverlay.getCenterPoint();
		angleFig.setEndPoint2(overlayPt.getDoublePosition(0), overlayPt.getDoublePosition(1));
	}

	@Override
	public void updateOverlay(final Figure figure, final OverlayView overlayView)
	{
		super.updateOverlay(figure, overlayView);
		assert figure instanceof AngleFigure;
		final AngleFigure angleFig = (AngleFigure) figure;
		final Overlay overlay = overlayView.getData();
		assert overlay instanceof AngleOverlay;
		final AngleOverlay angleOverlay = (AngleOverlay) overlay;
		Node node = angleFig.getNode(0);
		RealPoint point = new RealPoint(node.x[0], node.y[0]);
		angleOverlay.setEndPoint1(point);
		node = angleFig.getNode(1);
		point = new RealPoint(node.x[0], node.y[0]);
		angleOverlay.setCenterPoint(point);
		node = angleFig.getNode(2);
		point = new RealPoint(node.x[0], node.y[0]);
		angleOverlay.setEndPoint2(point);
		overlay.update();
	}

	@Override
	public JHotDrawTool getCreationTool(final ImageDisplay display,
		final OverlayCreatedListener listener)
	{
		return new IJCreationTool(display, this, listener);
	}

	private class AngleFigure extends BezierFigure {
		
		public AngleFigure() {
			addNode(new BezierPath.Node());
			addNode(new BezierPath.Node());
			addNode(new BezierPath.Node());
			setConnectable(false);
			setEndPoint1(6,1);
			setCenterPoint(1,1);
			setEndPoint2(1,6);
		}
		
		public void setEndPoint1(double x, double y) {
			setPoint(0,x,y);
		}
		
		public void setCenterPoint(double x, double y) {
			setPoint(1,x,y);
		}

		public void setEndPoint2(double x, double y) {
			setPoint(2,x,y);
		}
		
		private void setPoint(int nodeNum, double x, double y) {
			Point2D.Double p = new Point2D.Double(x, y);
			getNode(nodeNum).setTo(new BezierPath.Node(p));
		}
	}
}
