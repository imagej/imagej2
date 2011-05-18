//
// AbstractShapeROIAdapter.java
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

import net.imglib2.roi.RegionOfInterest;

import org.jhotdraw.draw.AbstractAttributedFigure;
import org.jhotdraw.draw.AttributeKeys;

import imagej.data.roi.AbstractShapeOverlay;


/**
 * @author leek
 *
 * The AbstractShapeROIAdapter adds mechanisms for populating the
 * line and fill attributes of the AbstractShapeROI from an AbstractAttributedFigure
 * and vice-versa
 * 
 * @param T - the derived RegionOfInterest type exposed by the AbstractShapeROI adapted by this adapter.
 */
public abstract class AbstractShapeOverlayAdapter <F extends AbstractAttributedFigure, T extends RegionOfInterest> extends AbstractLineOverlayAdapter<F> {
	protected void setFigureShapeProperties(AbstractShapeOverlay<T> roi, F figure) {
		figure.set(AttributeKeys.FILL_COLOR, roi.getFillColor());
		figure.set(AttributeKeys.CANVAS_FILL_OPACITY, roi.getOpacity());
		super.setFigureLineProperties(roi, figure);
	}
	
	protected void getShapeROIProperties(F figure, AbstractShapeOverlay<T> roi) {
		roi.setFillColor(figure.get(AttributeKeys.FILL_COLOR));
		roi.setOpacity(figure.get(AttributeKeys.CANVAS_FILL_OPACITY));
		super.setROILineProperties(figure, roi);
	}
}
