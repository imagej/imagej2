//
// OMEPolygonAdapter.java
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
import imagej.roi.ome.OMEPolygonROI;
import ome.xml.model.Polyline;

import org.jhotdraw.draw.BezierFigure;
import org.jhotdraw.draw.Figure;

/**
 * JHotDraw adapter for an OME-XML Polygon.
 * 
 * @author Adam Fraser
 */
@JHotDrawROIAdapter
public class OMEPolygonAdapter extends AbstractOMEShapeAdapter {

	private static final String TYPE_NAME = "OME Polygon";

	@Override
	public boolean supports(final ImageJROI roi) {
		return roi instanceof OMEPolygonROI;
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
	protected BezierFigure getFigure(final ImageJROI roi) {
		final Polyline e = getOMEPolygon(roi);
		// TODO:
//		e.getPoints();
		return null; // new PolygonFigure();
	}

	@Override
	protected void updateROIModel(final Figure figure, final ImageJROI roi) {
		// TODO
	}

	@Override
	public String getIconName() {
		return "/org/jhotdraw/images/POLYGON";
	}

	@Override
	public ImageJROI createNewROI(final String name) {
		if (name.equals(TYPE_NAME)) {
			final OMEPolygonROI roi = new OMEPolygonROI();
			final Polyline r = roi.getOMEShape();
			// TODO:
			// r.setPoints("");
			return roi;
		}
		throw new UnsupportedOperationException("Don't know how to make " + name);
	}

	@Override
	public Figure createDefaultFigure() {
		return new BezierFigure();
	}

	// -- Helper methods --

	private Polyline getOMEPolygon(final ImageJROI roi) {
		if (!(roi instanceof OMEPolygonROI)) {
			throw new UnsupportedOperationException("Can't adapt to " +
				roi.getClass().getName());
		}
		return ((OMEPolygonROI) roi).getOMEShape();
	}

}
