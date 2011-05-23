//
// RectangleAdapter.java
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

import net.imglib2.roi.RectangleRegionOfInterest;

import org.jhotdraw.draw.AttributeKeys;
import org.jhotdraw.draw.Figure;
import org.jhotdraw.draw.RectangleFigure;

import imagej.data.roi.Overlay;
import imagej.data.roi.RectangleOverlay;
import imagej.tool.Tool;

/**
 * @author leek
 *
 */
@JHotDrawOverlayAdapter(priority = RectangleAdapter.PRIORITY)
@Tool(name = "Rectangle", iconPath = "/tools/rectangle.png",
		priority = RectangleAdapter.PRIORITY, enabled = true)
public class RectangleAdapter extends AbstractJHotDrawOverlayAdapter<RectangleOverlay> {
	public static final int PRIORITY = 60;
	
	static protected RectangleOverlay downcastOverlay(Overlay roi) {
		assert(roi instanceof RectangleOverlay);
		return (RectangleOverlay)roi;
	}
	
	@Override
	public boolean supports(Overlay overlay, Figure figure) {
		if ((figure != null) && (!(figure instanceof RectangleFigure)))
			return false;
		if (overlay instanceof RectangleOverlay) return true;
		return false;
	}

	@Override
	public Overlay createNewOverlay() {
		return new RectangleOverlay();
	}

	@Override
	public Figure createDefaultFigure() {
		RectangleFigure figure = new RectangleFigure();
		figure.set(AttributeKeys.FILL_COLOR, new Color(255,255,255,0));
		return figure;
	}

	@Override
	public void updateFigure(Overlay overlay, Figure f) {
		super.updateFigure(overlay, f);
		RectangleOverlay rectangleOverlay = downcastOverlay(overlay);
		RectangleRegionOfInterest roi = rectangleOverlay.getRegionOfInterest();
		double x0 = roi.getOrigin(0);
		double w = roi.getExtent(0);
		double y0 = roi.getOrigin(1);
		double h = roi.getExtent(1);
		Point2D.Double anchor = new Point2D.Double(x0, y0);
		Point2D.Double lead = new Point2D.Double(x0 + w, y0 + h);
		f.setBounds(anchor, lead);
	}

	@Override
	public void updateOverlay(Figure figure, Overlay overlay) {
		super.updateOverlay(figure, overlay);
		RectangleOverlay rOverlay = downcastOverlay(overlay);
		RectangleRegionOfInterest roi = rOverlay.getRegionOfInterest();
		Rectangle2D.Double bounds = figure.getBounds();
		roi.setOrigin(bounds.getMinX(), 0);
		roi.setOrigin(bounds.getMinY(), 1);
		roi.setExtent(bounds.getWidth(), 0);
		roi.setExtent(bounds.getHeight(), 1);
	}

}
