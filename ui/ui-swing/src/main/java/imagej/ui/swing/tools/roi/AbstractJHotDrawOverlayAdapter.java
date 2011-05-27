//
// AbstractJHotDrawROIAdapter.java
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

import imagej.data.roi.Overlay;
import imagej.tool.BaseTool;
import imagej.util.ColorRGB;
import imagej.util.ColorRGBA;
import imagej.util.awt.AWTColors;

import java.awt.Color;

import org.jhotdraw.draw.AttributeKeys;
import org.jhotdraw.draw.Figure;


/**
 * An abstract class that gives default behavior for the IJHotDrawROIAdapter
 * interface
 * 
 * @author Lee Kamentsky
 */
public abstract class AbstractJHotDrawOverlayAdapter<O extends Overlay> extends BaseTool implements IJHotDrawOverlayAdapter
{
	private int priority;
	/* (non-Javadoc)
	 * @see imagej.ui.swing.tools.roi.IJHotDrawOverlayAdapter#getPriority()
	 */
	@Override
	public int getPriority() {
		return priority;
	}

	/* (non-Javadoc)
	 * @see imagej.ui.swing.tools.roi.IJHotDrawOverlayAdapter#setPriority(int)
	 */
	@Override
	public void setPriority(int priority) {
		this.priority = priority;
	}

	@Override
	public void updateFigure(final Overlay overlay, final Figure figure) {
		final ColorRGB lineColor = overlay.getLineColor();
		figure.set(AttributeKeys.STROKE_COLOR, AWTColors.getColor(lineColor));
		figure.set(AttributeKeys.STROKE_WIDTH, overlay.getLineWidth());
		final ColorRGB fillColor = overlay.getFillColor();
		figure.set(AttributeKeys.FILL_COLOR, AWTColors.getColor(fillColor, overlay.getAlpha()));
	}

	@Override
	public void updateOverlay(final Figure figure, final Overlay overlay) {
		final Color strokeColor = figure.get(AttributeKeys.STROKE_COLOR);
		overlay.setLineColor(AWTColors.getColorRGB(strokeColor));
		overlay.setLineWidth(figure.get(AttributeKeys.STROKE_WIDTH));
		final Color fillColor = figure.get(AttributeKeys.FILL_COLOR);
		final ColorRGBA imageJColor = AWTColors.getColorRGBA(fillColor); 
		overlay.setFillColor(imageJColor);
		overlay.setAlpha(imageJColor.getAlpha());
	}
	
}
