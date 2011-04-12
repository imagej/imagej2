//
// OMEEllipseAdapter.java
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

package imagej.roi.ui.jhotdraw.ome;

import imagej.roi.ImageJROI;
import imagej.roi.ui.jhotdraw.JHotDrawROIAdapter;
import imagej.roi.ome.OMEEllipseROI;
import imagej.util.Log;

import java.awt.geom.Point2D.Double;
import java.awt.geom.Rectangle2D;

import ome.xml.model.Ellipse;

import org.jhotdraw.draw.EllipseFigure;
import org.jhotdraw.draw.Figure;

/**
 * JHotDraw adapter for an OME-XML Ellipse.
 * 
 * @author Adam Fraser
 */
@JHotDrawROIAdapter
public class OMEEllipseAdapter extends AbstractOMEShapeAdapter {

	private static final String TYPE_NAME = "OME Ellipse";

	@Override
	public boolean supports(final ImageJROI roi) {
		return roi instanceof OMEEllipseROI;
	}

	@Override
	public String[] getROITypeNames() {
		return new String[] { TYPE_NAME };
	}

	@Override
	public String displayName() {
		return TYPE_NAME;
	}

	@Override
	protected EllipseFigure getFigure(final ImageJROI roi) {
		final Ellipse e = getOmeEllipse(roi);
		final int x = e.getX().intValue();
		final int y = e.getY().intValue();
		final int rx = e.getRadiusX().intValue();
		final int rb = e.getRadiusY().intValue();
		return new EllipseFigure(x, y, x - rx, y - rb);
	}

	@Override
	protected void updateROIModel(final Figure figure, final ImageJROI roi) {
		super.updateROIModel(figure, roi);
		final Ellipse e = getOmeEllipse(roi);
		final EllipseFigure ef = getEllipseFigure(figure);
		final Double startPoint = ef.getStartPoint();
		final double x = startPoint.x, y = startPoint.y;
		final Rectangle2D.Double drawingArea = ef.getDrawingArea();
		final double w = drawingArea.width, h = drawingArea.height;
		Log.debug(String.format("Ellipse: x=%f, y=%f, w=%f, h=%f", x, y, w, h));
		e.setX(x);
		e.setY(y);
		e.setRadiusX(w / 2.);
		e.setRadiusY(h / 2.);
	}

	@Override
	public String getIconName() {
		// TODO ellipse icon name?
		return "/org/jhotdraw/images/ELLIPSE";
	}

	@Override
	public ImageJROI createNewROI(final String name) {
		if (name.equals(TYPE_NAME)) {
			final OMEEllipseROI roi = new OMEEllipseROI();
			final Ellipse r = roi.getOMEShape();
			r.setX(0.);
			r.setY(0.);
			r.setRadiusX(10.);
			r.setRadiusY(10.);
			return roi;
		}
		throw new UnsupportedOperationException("Don't know how to make " + name);
	}

	@Override
	public Figure createDefaultFigure() {
		return new EllipseFigure();
	}

	// -- Helper methods --

	private Ellipse getOmeEllipse(final ImageJROI roi) {
		if (!(roi instanceof OMEEllipseROI)) {
			throw new UnsupportedOperationException("Can't adapt to " +
				roi.getClass().getName());
		}
		return ((OMEEllipseROI) roi).getOMEShape();
	}

	private EllipseFigure getEllipseFigure(final Figure f) {
		if (!(f instanceof EllipseFigure)) {
			throw new UnsupportedOperationException("Can't adapt from " +
				f.getClass().getName());
		}
		return (EllipseFigure) f;
	}

}
