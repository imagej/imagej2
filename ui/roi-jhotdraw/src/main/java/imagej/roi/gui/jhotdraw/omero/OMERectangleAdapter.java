/* OMERectangleAdapter.java
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
package imagej.roi.gui.jhotdraw.omero;

import java.awt.Point;

import ome.xml.model.Rectangle;
import imagej.Log;
import imagej.roi.ImageJROI;
import imagej.roi.gui.jhotdraw.JHotDrawROIAdapter;
import imagej.roi.omero.OmeroRectangleROI;
import org.jhotdraw.figures.AttributeFigure;
import org.jhotdraw.figures.RectangleFigure;
import org.jhotdraw.framework.Figure;

/**
 * @author leek
 *
 *JHotDraw adapter for an Omero Rectangle
 */
@JHotDrawROIAdapter
public class OMERectangleAdapter extends BaseShapeAdapter {
	private static final String TYPE_NAME = "Omero Rectangle";
	/* (non-Javadoc)
	 * @see imagej.roi.gui.jhotdraw.IJHotDrawROIAdapter#supports(imagej.roi.ImageJROI)
	 */
	@Override
	public boolean supports(ImageJROI roi) {
		return roi instanceof OmeroRectangleROI;
	}

	/* (non-Javadoc)
	 * @see imagej.roi.gui.jhotdraw.IJHotDrawROIAdapter#getROITypeNames()
	 */
	@Override
	public String[] getROITypeNames() {
		return new String[] { TYPE_NAME };
	}


	/* (non-Javadoc)
	 * @see imagej.roi.gui.jhotdraw.IJHotDrawROIAdapter#displayName()
	 */
	@Override
	public String displayName() {
		return "Omero Rectangle";
	}
	
	static private Rectangle getOmeroRectangle(ImageJROI roi) {
		if (! ( roi instanceof OmeroRectangleROI)) {
			throw new UnsupportedOperationException("Can't adapt to " + roi.getClass().getName());
		}
		return ((OmeroRectangleROI) roi).getOMEShape();
	}
	
	static private RectangleFigure getRectangleFigure(Figure f) {
		if (! (f instanceof RectangleFigure)) {
			throw new UnsupportedOperationException("Can't adapt from " + f.getClass().getName());
		}
		return (RectangleFigure)f;
	}

	/* (non-Javadoc)
	 * @see imagej.roi.gui.jhotdraw.omero.BaseShapeAdapter#getFigure(imagej.roi.ImageJROI)
	 */
	@Override
	protected AttributeFigure getFigure(ImageJROI roi) {
		Rectangle r = getOmeroRectangle(roi); 
		int x = r.getX().intValue();
		int y = r.getY().intValue();
		int width = r.getWidth().intValue();
		int height = r.getHeight().intValue();
		Point origin = new Point(x, y);
		Point corner = new Point(x+width, y+height);
		RectangleFigure figure = new RectangleFigure(origin, corner);
		return figure;
	}

	/* (non-Javadoc)
	 * @see imagej.roi.gui.jhotdraw.omero.BaseShapeAdapter#updateROIModel(CH.ifa.draw.framework.Figure, imagej.roi.ImageJROI)
	 */
	@Override
	protected void updateROIModel(Figure figure, ImageJROI roi) {
		super.updateROIModel(figure, roi);
		Rectangle r = getOmeroRectangle(roi);
		RectangleFigure rf = getRectangleFigure(figure);
		java.awt.Rectangle displayBox = rf.displayBox();
		Log.debug(String.format("Rectangle: x=%f, y=%f, w=%f, h=%f", 
				displayBox.getX(), displayBox.getY(), 
				displayBox.getWidth(), displayBox.getHeight()));
		r.setX(new Double(displayBox.getX()));
		r.setY(new Double(displayBox.getY()));
		r.setWidth(new Double(displayBox.getWidth()));
		r.setHeight(new Double(displayBox.getHeight()));
	}

	/* (non-Javadoc)
	 * @see imagej.roi.gui.jhotdraw.AbstractJHotDrawROIAdapter#createNewROI(java.lang.String)
	 */
	@Override
	public ImageJROI createNewROI(String name) {
		if (name.equals(TYPE_NAME)) {
			OmeroRectangleROI roi = new OmeroRectangleROI();
			Rectangle r = roi.getOMEShape();
			r.setX(0.);
			r.setY(0.);
			r.setWidth(20.);
			r.setHeight(20.);
			return roi;
		}
		throw new UnsupportedOperationException("Don't know how to make " + name);
	}

	@Override
	public String getIconName() {
		return "/CH/ifa/draw/images/RECT";
	}
}
