//
// SwingDatasetView.java
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

package imagej.ui.swing.display;

import imagej.data.Dataset;
import imagej.display.AbstractDatasetView;
import imagej.util.awt.AWTImageTools;

import java.awt.Image;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;

import net.imglib2.img.Axis;

import org.jhotdraw.draw.Drawing;
import org.jhotdraw.draw.Figure;
import org.jhotdraw.draw.ImageFigure;

/**
 * TODO
 * 
 * @author Curtis Rueden
 */
public class SwingDatasetView extends AbstractDatasetView implements
	FigureView
{

	private final ImageFigure figure;

	public SwingDatasetView(final SwingImageDisplay display,
		final Dataset dataset)
	{
		super(display, dataset);
		final JHotDrawImageCanvas canvas = display.getImageCanvas();
		final Drawing drawing = canvas.getDrawing();
		figure = new ImageFigure();
		figure.setSelectable(false);
		figure.setTransformable(false);
		drawing.add(figure);
		rebuild();
	}

	// -- DisplayView methods --

	@Override
	public int getPreferredWidth() {
		return getScreenImage().image().getWidth(null);
	}

	@Override
	public int getPreferredHeight() {
		return getScreenImage().image().getHeight(null);
	}

	private SwingDisplayWindow getDisplayWindow() {
		assert getDisplay().getDisplayWindow() instanceof SwingDisplayWindow;
		return (SwingDisplayWindow) getDisplay().getDisplayWindow();
	}
	@Override
	public void update() {
		final Image image = getScreenImage().image();
		final BufferedImage bufImage = AWTImageTools.makeBuffered(image);
		figure.setBounds(new Rectangle2D.Double(0, 0, bufImage.getWidth(),
			bufImage.getHeight()));
		figure.setBufferedImage(bufImage);
	}
	
	// -- FigureView methods --

	@Override
	public Figure getFigure() {
		return figure;
	}

}
