//
// LineAdapter.java
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

package imagej.ui.swing.tools.overlay;

import imagej.data.display.OverlayView;
import imagej.data.overlay.LineOverlay;
import imagej.data.overlay.Overlay;
import imagej.ext.plugin.Plugin;
import imagej.ext.tool.Tool;
import imagej.ui.swing.overlay.JHotDrawOverlayAdapter;
import imagej.ui.swing.tools.FreehandTool;

import java.awt.geom.Point2D;

import net.imglib2.RealPoint;

import org.jhotdraw.draw.AttributeKeys;
import org.jhotdraw.draw.Figure;
import org.jhotdraw.draw.LineFigure;
import org.jhotdraw.geom.BezierPath.Node;

/**
 * TODO
 * 
 * @author Lee Kamentsky
 */
@Plugin(type = Tool.class, name = "Line",
	description = "Straight line overlays", iconPath = "/icons/tools/line.png",
	priority = LineAdapter.PRIORITY, enabled = true)
@JHotDrawOverlayAdapter(priority = LineAdapter.PRIORITY)
public class LineAdapter extends AbstractJHotDrawOverlayAdapter<LineOverlay> {

	public static final int PRIORITY = FreehandTool.PRIORITY - 1;

	@Override
	public boolean supports(final Overlay overlay, final Figure figure) {
		if (!(overlay instanceof LineOverlay)) return false;
		return (figure == null) || (figure instanceof LineFigure);
	}

	@Override
	public LineOverlay createNewOverlay() {
		return new LineOverlay();
	}

	@Override
	public Figure createDefaultFigure() {
		final LineFigure figure = new LineFigure();
		figure.set(AttributeKeys.STROKE_COLOR, getDefaultStrokeColor());
		return figure;
	}

	@Override
	public void updateFigure(final OverlayView overlay, final Figure figure) {
		super.updateFigure(overlay, figure);
		assert figure instanceof LineFigure;
		final LineFigure line = (LineFigure) figure;
		assert overlay instanceof LineOverlay;
		final LineOverlay loverlay = (LineOverlay) overlay;
		line.setStartPoint(new Point2D.Double(loverlay.getLineStart()
			.getDoublePosition(0), loverlay.getLineStart().getDoublePosition(1)));
		line.setEndPoint(new Point2D.Double(loverlay.getLineEnd()
			.getDoublePosition(0), loverlay.getLineEnd().getDoublePosition(1)));
	}

	@Override
	public void updateOverlay(final Figure figure, final OverlayView overlay) {
		super.updateOverlay(figure, overlay);
		assert figure instanceof LineFigure;
		final LineFigure line = (LineFigure) figure;
		assert overlay instanceof LineOverlay;
		final LineOverlay loverlay = (LineOverlay) overlay;
		final Node startNode = line.getNode(0);
		loverlay.setLineStart(new RealPoint(new double[] {
			startNode.getControlPoint(0).x, startNode.getControlPoint(0).y }));
		final Node endNode = line.getNode(1);
		loverlay.setLineEnd(new RealPoint(new double[] {
			endNode.getControlPoint(0).x, endNode.getControlPoint(0).y }));
	}

}
