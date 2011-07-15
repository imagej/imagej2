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

import java.awt.EventQueue;
import java.awt.Image;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;

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
	private boolean needsUpdate;

	public SwingDatasetView(final SwingImageDisplay display,
		final Dataset dataset)
	{
		super(display, dataset);
		needsUpdate = false;
		final JHotDrawImageCanvas canvas = display.getImageCanvas();
		final Drawing drawing = canvas.getDrawing();
		figure = new ImageFigure();
		figure.setSelectable(false);
		figure.setTransformable(false);
		figure.setBounds(
				new Point2D.Double(dataset.getImgPlus().realMin(0), dataset.getImgPlus().realMin(1)),
				new Point2D.Double(dataset.getImgPlus().realMax(0), dataset.getImgPlus().realMax(1)));
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
	public synchronized void update() {
		if (! needsUpdate) {
			needsUpdate = true;
			EventQueue.invokeLater(new Runnable(){

				@Override
				public void run() {
					SwingDatasetView.this.doUpdate();
				}}
			);
		}
	}
	
	private synchronized void doUpdate() {
		try {
			final Image image = getScreenImage().image();
			final BufferedImage bufImage = AWTImageTools.makeBuffered(image);
			figure.setBounds(new Rectangle2D.Double(0, 0, bufImage.getWidth(),
				bufImage.getHeight()));
			figure.setBufferedImage(bufImage);
		} finally {
			needsUpdate = false;
		}
	}
	

	// -- FigureView methods --

	@Override
	public Figure getFigure() {
		return figure;
	}

}
