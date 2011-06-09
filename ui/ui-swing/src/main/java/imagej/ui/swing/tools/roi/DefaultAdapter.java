//
// DefaultAdapter.java
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
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.IndexColorModel;
import java.awt.image.Raster;
import java.awt.image.SampleModel;
import java.awt.image.SinglePixelPackedSampleModel;


import imagej.data.roi.Overlay;
import imagej.display.DisplayView;
import imagej.util.ColorRGB;

import org.jhotdraw.draw.AttributeKeys;
import org.jhotdraw.draw.Figure;
import org.jhotdraw.draw.ImageFigure;

import net.imglib2.RealRandomAccess;
import net.imglib2.roi.RegionOfInterest;
import net.imglib2.type.logic.BitType;

/**
 * @author Lee Kamentsky
 *
 *The default adapter handles any kind of overlay. It uses the fill color
 *and alpha of the overlay to draw the mask and leaves the rest of the figure transparent. 
 */
@JHotDrawOverlayAdapter(priority = DefaultAdapter.PRIORITY)
public class DefaultAdapter extends AbstractJHotDrawOverlayAdapter<Overlay> {
	static public final int PRIORITY = Integer.MAX_VALUE;
	@Override
	public boolean supports(Overlay overlay, Figure figure) {
		return ((figure == null) || (figure instanceof ImageFigure));
	}

	@Override
	public Overlay createNewOverlay() {
		return null;
	}

	@Override
	public Figure createDefaultFigure() {
		ImageFigure figure = new ImageFigure();
		figure.setTransformable(false);
		figure.set(AttributeKeys.FILL_COLOR, new Color(0,0,0,0));
		return figure;
	}

	/* (non-Javadoc)
	 * @see imagej.ui.swing.tools.roi.AbstractJHotDrawOverlayAdapter#updateFigure(imagej.data.roi.Overlay, org.jhotdraw.draw.Figure)
	 */
	@Override
	public void updateFigure(Overlay overlay, Figure figure, DisplayView view) {
		super.updateFigure(overlay, figure, view);
		/*
		 * Override the base: set the fill color to transparent.
		 */
		figure.set(AttributeKeys.FILL_COLOR, new Color(0,0,0,0));
		assert figure instanceof ImageFigure;
		ImageFigure imgf = (ImageFigure)figure;
		RegionOfInterest roi = overlay.getRegionOfInterest();
		if (roi != null) {
			long minX = (long)Math.floor(roi.realMin(0));
			long maxX = (long)Math.ceil(roi.realMax(0)) + 1;
			long minY = (long)Math.floor(roi.realMin(1));
			long maxY = (long)Math.ceil(roi.realMax(1)) + 1;
			ColorRGB color = overlay.getFillColor();
			IndexColorModel cm = new IndexColorModel(1, 2, 
					new byte[] { 0, (byte)color.getRed()},
					new byte[] { 0, (byte)color.getGreen()},
					new byte[] { 0, (byte)color.getBlue() },
					new byte[] { 0, (byte)overlay.getAlpha() });
			int w = (int)(maxX - minX);
			int h = (int)(maxY - minY);
			BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_BYTE_INDEXED, cm);
			SampleModel sm = new SinglePixelPackedSampleModel(DataBuffer.TYPE_BYTE, w, h, new int [] {1});
			DataBuffer dbuncast = sm.createDataBuffer();
			assert dbuncast instanceof DataBufferByte;
			DataBufferByte db = (DataBufferByte)dbuncast;
			byte [] bankData = db.getData();
			RealRandomAccess<BitType> ra = roi.realRandomAccess();
			for (int i=2; i < ra.numDimensions(); i++) {
				long position = view.getPlanePosition()[i-2];
				ra.setPosition(position, i);
			}
			int index = 0;
			for (int j=0; j<h; j++) {
				ra.setPosition(minY + j, 1);
				for (int i=0; i<w; i++) {
					ra.setPosition(minX + i, 0);
					if (ra.get().get()) 
						bankData[index] = -1;
					index++;
				}
			}
			Raster raster = Raster.createRaster(sm, db, new java.awt.Point(0,0));
			img.setData(raster);
			imgf.setBounds(new Rectangle2D.Double(minX,minY,w, h));
			imgf.setBufferedImage(img);
		}
	}

}
