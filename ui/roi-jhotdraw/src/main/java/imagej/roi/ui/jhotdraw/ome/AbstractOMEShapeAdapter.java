//
// AbstractOMEShapeAdapter.java
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
import imagej.roi.ui.jhotdraw.AbstractJHotDrawROIAdapter;
import imagej.roi.ome.OMEShapeROI;
import imagej.util.Log;

import java.awt.Color;

import ome.xml.model.Shape;
import ome.xml.model.enums.FontStyle;
import ome.xml.model.primitives.NonNegativeInteger;

import org.jhotdraw.draw.AbstractAttributedFigure;
import org.jhotdraw.draw.AttributeKeys;
import org.jhotdraw.draw.Figure;

/**
 * TODO
 * 
 * @author Lee Kamentsky
 */
public abstract class AbstractOMEShapeAdapter extends AbstractJHotDrawROIAdapter {

	/**
	 * Gets the JHotDraw figure that represents the ROI.
	 * 
	 * @param roi - ROI in question
	 * @return a figure, properly sized and placed, but the generic shape
	 *         parameters need not be filled in.
	 */
	protected abstract AbstractAttributedFigure getFigure(ImageJROI roi);

	private static Shape getROIShape(final ImageJROI roi) {
		if (!(roi instanceof OMEShapeROI<?>)) {
			Log.error(roi.getClass().getName() + " is not an Omero shape");
			return null;
		}
		final OMEShapeROI<?> shapeROI = (OMEShapeROI<?>) roi;
		return shapeROI.getOMEShape();
	}

	@Override
	protected Figure createFigureForROI(final ImageJROI roi) {
		final Shape shape = getROIShape(roi);
		final AbstractAttributedFigure figure = getFigure(roi);
		final Integer fill = shape.getFill();
		final NonNegativeInteger fontSize = shape.getFontSize();
		final FontStyle fontStyle = shape.getFontStyle();
		if (fill != null) {
			figure.set(AttributeKeys.FILL_COLOR, new Color(fill.intValue()));
		}
		if (fontSize != null) {
			figure.set(AttributeKeys.FONT_SIZE, new Double(fontSize.getValue()));
		}
		if (fontStyle != null) {
			boolean bold = false, italic = false;
			if (fontStyle.name().equals(FontStyle.BOLD)) {
				bold = true;
			}
			else if (fontStyle.name().equals(FontStyle.ITALIC)) {
				italic = true;
			}
			else if (fontStyle.name().equals(FontStyle.BOLDITALIC)) {
				bold = true;
				italic = true;
			}
			figure.set(AttributeKeys.FONT_BOLD, bold);
			figure.set(AttributeKeys.FONT_ITALIC, italic);
		}
		return figure;
	}

	@Override
	protected void updateROIModel(final Figure figure, final ImageJROI roi) {
		final Shape shape = getROIShape(roi);
		final Color oFill = figure.get(AttributeKeys.FILL_COLOR);
		shape.setFill(oFill.getRGB());

		final Double oFontSize = figure.get(AttributeKeys.FONT_SIZE);
		shape.setFontSize(new NonNegativeInteger(oFontSize.intValue()));

		final boolean oFontBold = figure.get(AttributeKeys.FONT_BOLD);
		final boolean oFontItalic = figure.get(AttributeKeys.FONT_ITALIC);
		final FontStyle fontStyle;
		if (oFontBold && oFontItalic) fontStyle = FontStyle.BOLDITALIC;
		else if (oFontBold) fontStyle = FontStyle.BOLD;
		else if (oFontItalic) fontStyle = FontStyle.ITALIC;
		else fontStyle = FontStyle.REGULAR;
		shape.setFontStyle(fontStyle);
	}

}
