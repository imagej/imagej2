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
import imagej.data.overlay.PolygonOverlay;
import imagej.plugins.uis.swing.overlay.AbstractJHotDrawAdapter;
import imagej.plugins.uis.swing.overlay.IJBezierTool;
import imagej.plugins.uis.swing.overlay.JHotDrawAdapter;
import imagej.plugins.uis.swing.overlay.JHotDrawTool;
import imagej.tool.Tool;
import imagej.ui.swing.tools.SwingPolygonTool;

import java.awt.Shape;
import java.util.Arrays;

import net.imglib2.RealLocalizable;
import net.imglib2.RealPoint;
import net.imglib2.roi.PolygonRegionOfInterest;

import org.jhotdraw.draw.AttributeKeys;
import org.jhotdraw.draw.BezierFigure;
import org.jhotdraw.draw.Figure;
import org.jhotdraw.geom.BezierPath.Node;
import org.scijava.log.LogService;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

/**
 * JHotDraw adapter for polygon/freehand overlays.
 * 
 * @author Lee Kamentsky
 * @author Barry DeZonia
 * @see SwingPolygonTool
 */
@Plugin(type = JHotDrawAdapter.class, priority = SwingPolygonTool.PRIORITY)
public class PolygonJHotDrawAdapter extends
	AbstractJHotDrawAdapter<PolygonOverlay, BezierFigure>
{

	private static PolygonOverlay downcastOverlay(final Overlay overlay) {
		assert overlay instanceof PolygonOverlay;
		return (PolygonOverlay) overlay;
	}

	@Parameter(required = false)
	private LogService log;

	// -- JHotDrawAdapter methods --

	@Override
	public boolean supports(final Tool tool) {
		return tool instanceof SwingPolygonTool;
	}

	@Override
	public boolean supports(final Overlay overlay, final Figure figure) {
		if (!(overlay instanceof PolygonOverlay)) return false;
		return figure == null || figure instanceof BezierFigure;
	}

	@Override
	public Overlay createNewOverlay() {
		final PolygonOverlay o = new PolygonOverlay(getContext());
		return o;
	}

	@Override
	public Figure createDefaultFigure() {
		final BezierFigure figure = new PolygonFigure();
		initDefaultSettings(figure);
		figure.set(AttributeKeys.WINDING_RULE, AttributeKeys.WindingRule.EVEN_ODD);
		return figure;
	}

	@Override
	public void updateOverlay(final BezierFigure figure, final OverlayView view)
	{
		super.updateOverlay(figure, view);
		final PolygonOverlay poverlay = downcastOverlay(view.getData());
		final PolygonRegionOfInterest roi = poverlay.getRegionOfInterest();
		final int nodeCount = figure.getNodeCount();
		while (roi.getVertexCount() > nodeCount) {
			roi.removeVertex(nodeCount);
			if (log != null) log.debug("Removed node from overlay.");
		}
		for (int i = 0; i < nodeCount; i++) {
			final Node node = figure.getNode(i);
			final double[] position = new double[] { node.x[0], node.y[0] };
			if (roi.getVertexCount() == i) {
				roi.addVertex(i, new RealPoint(position));
				if (log != null) log.debug("Added node to overlay");
			}
			else {
				if ((position[0] != roi.getVertex(i).getDoublePosition(0)) ||
					(position[1] != roi.getVertex(i).getDoublePosition(1)))
				{
					if (log != null) {
						log.debug(String.format("Vertex # %d moved to %f,%f", i + 1,
							position[0], position[1]));
					}
				}
				roi.setVertexPosition(i, position);
			}
		}
		poverlay.update();
	}

	@Override
	public void updateFigure(final OverlayView view, final BezierFigure figure) {
		super.updateFigure(view, figure);
		final PolygonOverlay polygonOverlay = downcastOverlay(view.getData());
		final PolygonRegionOfInterest roi = polygonOverlay.getRegionOfInterest();
		final int vertexCount = roi.getVertexCount();
		while (figure.getNodeCount() > vertexCount) {
			figure.removeNode(vertexCount);
		}
		for (int i = 0; i < vertexCount; i++) {
			final RealLocalizable vertex = roi.getVertex(i);
			final double x = vertex.getDoublePosition(0);
			final double y = vertex.getDoublePosition(1);
			if (figure.getNodeCount() == i) {
				figure.addNode(new Node(x, y));
			}
			else {
				final Node node = figure.getNode(i);
				node.mask = 0;
				Arrays.fill(node.x, x);
				Arrays.fill(node.y, y);
			}
		}
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
