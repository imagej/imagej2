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
package imagej.ui.swing.tools.roi;

import imagej.data.roi.LineOverlay;
import imagej.data.roi.Overlay;
import imagej.display.DisplayView;
import imagej.tool.Tool;
import imagej.ui.swing.tools.FreehandTool;

import java.awt.geom.Point2D;

import net.imglib2.RealPoint;

import org.jhotdraw.draw.AttributeKeys;
import org.jhotdraw.draw.Figure;
import org.jhotdraw.draw.LineFigure;
import org.jhotdraw.geom.BezierPath.Node;

/**
 * @author Lee Kamentsky
 *
 */
@Tool(name = "Line", iconPath = "/icons/tools/line.png",
		priority = LineAdapter.PRIORITY, enabled = true)
@JHotDrawOverlayAdapter(priority = LineAdapter.PRIORITY)
public class LineAdapter extends AbstractJHotDrawOverlayAdapter<LineOverlay> {
	public static final int PRIORITY = FreehandTool.PRIORITY + 1;

	/* (non-Javadoc)
	 * @see imagej.ui.swing.tools.roi.IJHotDrawOverlayAdapter#supports(imagej.data.roi.Overlay, org.jhotdraw.draw.Figure)
	 */
	@Override
	public boolean supports(Overlay overlay, Figure figure) {
		if (! (overlay instanceof LineOverlay)) return false;
		return (figure == null) || (figure instanceof LineFigure);
	}

	/* (non-Javadoc)
	 * @see imagej.ui.swing.tools.roi.IJHotDrawOverlayAdapter#createNewOverlay()
	 */
	@Override
	public LineOverlay createNewOverlay() {
		return new LineOverlay();
	}

	/* (non-Javadoc)
	 * @see imagej.ui.swing.tools.roi.IJHotDrawOverlayAdapter#createDefaultFigure()
	 */
	@Override
	public Figure createDefaultFigure() {
		LineFigure figure = new LineFigure();
		figure.set(AttributeKeys.STROKE_COLOR, defaultStrokeColor);
		return figure;
	}

	/* (non-Javadoc)
	 * @see imagej.ui.swing.tools.roi.AbstractJHotDrawOverlayAdapter#updateFigure(imagej.data.roi.Overlay, org.jhotdraw.draw.Figure, imagej.display.DisplayView)
	 */
	@Override
	public void updateFigure(Overlay overlay, Figure figure, DisplayView view) {
		super.updateFigure(overlay, figure, view);
		assert figure instanceof LineFigure;
		LineFigure line = (LineFigure)figure;
		assert overlay instanceof LineOverlay;
		LineOverlay loverlay = (LineOverlay)overlay;
		line.setStartPoint(new Point2D.Double(
				loverlay.getLineStart().getDoublePosition(0),
				loverlay.getLineStart().getDoublePosition(1))); 
		line.setEndPoint(new Point2D.Double(
				loverlay.getLineEnd().getDoublePosition(0),
				loverlay.getLineEnd().getDoublePosition(1))); 
	}

	/* (non-Javadoc)
	 * @see imagej.ui.swing.tools.roi.AbstractJHotDrawOverlayAdapter#updateOverlay(org.jhotdraw.draw.Figure, imagej.data.roi.Overlay)
	 */
	@Override
	public void updateOverlay(Figure figure, Overlay overlay) {
		// TODO Auto-generated method stub
		super.updateOverlay(figure, overlay);
		assert figure instanceof LineFigure;
		LineFigure line = (LineFigure)figure;
		assert overlay instanceof LineOverlay;
		LineOverlay loverlay = (LineOverlay)overlay;
		final Node startNode = line.getNode(0);
		loverlay.setLineStart(new RealPoint( new double [] {
				startNode.getControlPoint(0).x,
				startNode.getControlPoint(0).y }) );
		final Node endNode = line.getNode(1);
		loverlay.setLineEnd(new RealPoint( new double [] {
				endNode.getControlPoint(0).x,
				endNode.getControlPoint(0).y }) );
	}

}
