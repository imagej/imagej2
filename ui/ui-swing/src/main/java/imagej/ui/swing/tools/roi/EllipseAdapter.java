//
// OvalAdapter.java
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

import java.awt.Color;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import imagej.data.roi.EllipseOverlay;
import imagej.data.roi.Overlay;
import imagej.tool.Tool;
import net.imglib2.RealPoint;
import net.imglib2.roi.EllipseRegionOfInterest;

import org.jhotdraw.draw.AttributeKeys;
import org.jhotdraw.draw.EllipseFigure;
import org.jhotdraw.draw.Figure;

/**
 * @author leek
 *
 */
@Tool(name = "Oval", iconPath = "/tools/oval.png",
		priority = EllipseAdapter.PRIORITY, enabled = true)
@JHotDrawOverlayAdapter(priority = EllipseAdapter.PRIORITY)
public class EllipseAdapter extends AbstractJHotDrawOverlayAdapter<EllipseOverlay> {
	final static public int PRIORITY=70;
	
	static protected EllipseOverlay downcastOverlay(Overlay roi) {
		assert(roi instanceof EllipseOverlay);
		return (EllipseOverlay)roi;
	}
	
	static protected EllipseFigure downcastFigure(Figure figure) {
		assert(figure instanceof EllipseFigure);
		return (EllipseFigure)figure;
	}
	
	
	@Override
	public boolean supports(Overlay overlay, Figure figure) {
		if ((figure != null) && (!(figure instanceof EllipseFigure))){
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
		EllipseFigure figure = new EllipseFigure();
		figure.set(AttributeKeys.FILL_COLOR, new Color(255,255,255,0));
		return figure;
	}

	@Override
	public void updateFigure(Overlay o, Figure f) {
		super.updateFigure(o, f);
		EllipseOverlay overlay = downcastOverlay(o);
		EllipseFigure figure = downcastFigure(f);
		EllipseRegionOfInterest eRoi = overlay.getRegionOfInterest();
		double centerX = eRoi.getOrigin(0);
		double centerY = eRoi.getOrigin(1);
		double radiusX = eRoi.getRadius(0);
		double radiusY = eRoi.getRadius(1);
		
		figure.setBounds(
				new Point2D.Double(centerX - radiusX, centerY - radiusY),
				new Point2D.Double(centerX + radiusX, centerY + radiusY));
	}

	@Override
	public void updateOverlay(Figure figure, Overlay roi) {
		super.updateOverlay(figure, roi);
		EllipseOverlay overlay = downcastOverlay(roi);
		EllipseFigure eFigure = downcastFigure(figure);
		Rectangle2D.Double r = eFigure.getBounds();
		RealPoint ptCenter = new RealPoint(
				new double[] { r.x + r.width/2, r.y + r.height/2 });
		EllipseRegionOfInterest eRoi = overlay.getRegionOfInterest();
		eRoi.setOrigin(ptCenter);
		eRoi.setRadius(r.width / 2, 0);
		eRoi.setRadius(r.height / 2, 1);
	}

}
