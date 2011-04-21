//
// OMERectangleAdapter.java
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
import imagej.roi.ome.OMERectangleROI;
import imagej.util.Log;

import java.awt.geom.Point2D.Double;
import java.awt.geom.Rectangle2D;

import ome.xml.model.Rectangle;

import org.jhotdraw.draw.Figure;
import org.jhotdraw.draw.RectangleFigure;

/**
 * JHotDraw adapter for an OME-XML Rectangle.
 * 
 * @author Lee Kamentsky
 */
@JHotDrawROIAdapter
public class OMERectangleAdapter extends AbstractOMEShapeAdapter {

	private static final String TYPE_NAME = "OME Rectangle";

	@Override
	public boolean supports(final ImageJROI roi) {
		return roi instanceof OMERectangleROI;
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
	protected RectangleFigure getFigure(final ImageJROI roi) {
		final Rectangle r = getOMERectangle(roi);
		final int x = r.getX().intValue();
		final int y = r.getY().intValue();
		final int width = r.getWidth().intValue();
		final int height = r.getHeight().intValue();
		final RectangleFigure figure = new RectangleFigure(x, y, width, height);
		return figure;
	}

	@Override
	protected void updateROIModel(final Figure figure, final ImageJROI roi) {
		super.updateROIModel(figure, roi);
		final Rectangle r = getOMERectangle(roi);
		final RectangleFigure rf = getRectangleFigure(figure);
		final Double startPoint = rf.getStartPoint();
		final double x = startPoint.x, y = startPoint.y;
		final Rectangle2D.Double drawingArea = rf.getDrawingArea();
		final double w = drawingArea.width, h = drawingArea.height;
		Log.debug(String.format("Rectangle: x=%f, y=%f, w=%f, h=%f", x, y, w, h));
		r.setX(x);
		r.setY(y);
		r.setWidth(w);
		r.setHeight(h);
	}

	@Override
	public ImageJROI createNewROI(final String name) {
		if (name.equals(TYPE_NAME)) {
			final OMERectangleROI roi = new OMERectangleROI();
			final Rectangle r = roi.getOMEShape();
			r.setX(0.);
			r.setY(0.);
			r.setWidth(20.);
			r.setHeight(20.);
			return roi;
		}
		throw new UnsupportedOperationException("Don't know how to make " + name);
	}

	@Override
	public Figure createDefaultFigure() {
		return new RectangleFigure();
	}

	@Override
	public String getIconName() {
		return "/org/jhotdraw/images/RECT";
	}

	// -- Helper methods --

	private Rectangle getOMERectangle(final ImageJROI roi) {
		if (!(roi instanceof OMERectangleROI)) {
			throw new UnsupportedOperationException("Can't adapt to " +
				roi.getClass().getName());
		}
		return ((OMERectangleROI) roi).getOMEShape();
	}

	private RectangleFigure getRectangleFigure(final Figure f) {
		if (!(f instanceof RectangleFigure)) {
			throw new UnsupportedOperationException("Can't adapt from " +
				f.getClass().getName());
		}
		return (RectangleFigure) f;
	}

}
