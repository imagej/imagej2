//
// EllipseAdapter.java
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

import imagej.data.display.DataView;
import imagej.data.overlay.EllipseOverlay;
import imagej.data.overlay.Overlay;
import imagej.ext.tool.Tool;
import imagej.ui.swing.overlay.JHotDrawOverlayAdapter;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import net.imglib2.RealPoint;
import net.imglib2.roi.EllipseRegionOfInterest;

import org.jhotdraw.draw.AttributeKeys;
import org.jhotdraw.draw.EllipseFigure;
import org.jhotdraw.draw.Figure;

/**
 * TODO
 * 
 * @author Lee Kamentsky
 */
@Tool(name = "Oval", description = "Oval selections",
	iconPath = "/icons/tools/oval.png", priority = EllipseAdapter.PRIORITY,
	enabled = true)
@JHotDrawOverlayAdapter(priority = EllipseAdapter.PRIORITY)
public class EllipseAdapter extends
	AbstractJHotDrawOverlayAdapter<EllipseOverlay>
{

	public static final int PRIORITY = RectangleAdapter.PRIORITY - 1;

	static protected EllipseOverlay downcastOverlay(final Overlay roi) {
		assert (roi instanceof EllipseOverlay);
		return (EllipseOverlay) roi;
	}

	static protected EllipseFigure downcastFigure(final Figure figure) {
		assert (figure instanceof EllipseFigure);
		return (EllipseFigure) figure;
	}

	@Override
	public boolean supports(final Overlay overlay, final Figure figure) {
		if ((figure != null) && (!(figure instanceof EllipseFigure))) {
			return false;
		}
		return overlay instanceof EllipseOverlay;
	}

	@Override
	public Overlay createNewOverlay() {
		return new EllipseOverlay();
	}

	@Override
	public Figure createDefaultFigure() {
		final EllipseFigure figure = new EllipseFigure();
		figure.set(AttributeKeys.FILL_COLOR, getDefaultFillColor());
		figure.set(AttributeKeys.STROKE_COLOR, getDefaultStrokeColor());
		return figure;
	}

	@Override
	public void updateFigure(final Overlay o, final Figure f,
		final DataView view)
	{
		super.updateFigure(o, f, view);
		final EllipseOverlay overlay = downcastOverlay(o);
		final EllipseFigure figure = downcastFigure(f);
		final EllipseRegionOfInterest eRoi = overlay.getRegionOfInterest();
		final double centerX = eRoi.getOrigin(0);
		final double centerY = eRoi.getOrigin(1);
		final double radiusX = eRoi.getRadius(0);
		final double radiusY = eRoi.getRadius(1);

		figure.setBounds(new Point2D.Double(centerX - radiusX, centerY - radiusY),
			new Point2D.Double(centerX + radiusX, centerY + radiusY));
	}

	@Override
	public void updateOverlay(final Figure figure, final Overlay roi) {
		super.updateOverlay(figure, roi);
		final EllipseOverlay overlay = downcastOverlay(roi);
		final EllipseFigure eFigure = downcastFigure(figure);
		final Rectangle2D.Double r = eFigure.getBounds();
		final RealPoint ptCenter =
			new RealPoint(new double[] { r.x + r.width / 2, r.y + r.height / 2 });
		final EllipseRegionOfInterest eRoi = overlay.getRegionOfInterest();
		eRoi.setOrigin(ptCenter);
		eRoi.setRadius(r.width / 2, 0);
		eRoi.setRadius(r.height / 2, 1);
	}

}
