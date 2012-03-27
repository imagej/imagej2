/*
 * #%L
 * ImageJ software for multidimensional image processing and analysis.
 * %%
 * Copyright (C) 2009 - 2012 Board of Regents of the University of
 * Wisconsin-Madison, Broad Institute of MIT and Harvard, and Max Planck
 * Institute of Molecular Cell Biology and Genetics.
 * %%
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * 
 * The views and conclusions contained in the software and documentation are
 * those of the authors and should not be interpreted as representing official
 * policies, either expressed or implied, of any organization.
 * #L%
 */

package imagej.ui.swing.tools.overlay;

import imagej.data.display.ImageDisplay;
import imagej.data.display.OverlayView;
import imagej.data.overlay.Overlay;
import imagej.ui.swing.overlay.IJCreationTool;
import imagej.ui.swing.overlay.JHotDrawOverlayAdapter;
import imagej.ui.swing.overlay.JHotDrawTool;
import imagej.ui.swing.overlay.OverlayCreatedListener;
import imagej.util.ColorRGB;

import java.awt.Color;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.IndexColorModel;
import java.awt.image.Raster;
import java.awt.image.SampleModel;
import java.awt.image.SinglePixelPackedSampleModel;

import net.imglib2.RealRandomAccess;
import net.imglib2.roi.RegionOfInterest;
import net.imglib2.type.logic.BitType;

import org.jhotdraw.draw.AttributeKeys;
import org.jhotdraw.draw.Figure;
import org.jhotdraw.draw.ImageFigure;

/**
 * The default adapter handles any kind of overlay. It uses the fill color and
 * alpha of the overlay to draw the mask and leaves the rest of the figure
 * transparent.
 * 
 * @author Lee Kamentsky
 */
@JHotDrawOverlayAdapter(priority = DefaultAdapter.PRIORITY)
public class DefaultAdapter extends AbstractJHotDrawOverlayAdapter<Overlay> {

	static public final int PRIORITY = 0;

	@Override
	public boolean supports(final Overlay overlay, final Figure figure) {
		return ((figure == null) || (figure instanceof ImageFigure));
	}

	@Override
	public Overlay createNewOverlay() {
		return null;
	}

	@Override
	public Figure createDefaultFigure() {
		final ImageFigure figure = new ImageFigure();
		figure.setTransformable(false);
		figure.set(AttributeKeys.FILL_COLOR, new Color(0, 0, 0, 0));
		// Avoid IllegalArgumentException: miter limit < 1 on the EDT
		figure.set(AttributeKeys.IS_STROKE_MITER_LIMIT_FACTOR, false);
		return figure;
	}

	@Override
	public void updateFigure(final OverlayView overlay, final Figure figure) {
		super.updateFigure(overlay, figure);

		// Override the base: set the fill color to transparent.
		figure.set(AttributeKeys.FILL_COLOR, new Color(0, 0, 0, 0));
		assert figure instanceof ImageFigure;
		final ImageFigure imgf = (ImageFigure) figure;
		final RegionOfInterest roi = overlay.getData().getRegionOfInterest();
		if (roi != null) {
			final long minX = (long) Math.floor(roi.realMin(0));
			final long maxX = (long) Math.ceil(roi.realMax(0)) + 1;
			final long minY = (long) Math.floor(roi.realMin(1));
			final long maxY = (long) Math.ceil(roi.realMax(1)) + 1;
			final ColorRGB color = overlay.getData().getFillColor();
			final IndexColorModel cm =
				new IndexColorModel(1, 2, new byte[] { 0, (byte) color.getRed() },
					new byte[] { 0, (byte) color.getGreen() }, new byte[] { 0,
						(byte) color.getBlue() },
					new byte[] { 0, (byte) overlay.getData().getAlpha() });
			final int w = (int) (maxX - minX);
			final int h = (int) (maxY - minY);
			final BufferedImage img =
				new BufferedImage(w, h, BufferedImage.TYPE_BYTE_INDEXED, cm);
			final SampleModel sm =
				new SinglePixelPackedSampleModel(DataBuffer.TYPE_BYTE, w, h,
					new int[] { 1 });
			final DataBuffer dbuncast = sm.createDataBuffer();
			assert dbuncast instanceof DataBufferByte;
			final DataBufferByte db = (DataBufferByte) dbuncast;
			final byte[] bankData = db.getData();
			final RealRandomAccess<BitType> ra = roi.realRandomAccess();
			for (int i = 2; i < ra.numDimensions(); i++) {
				final long position = overlay.getPlanePosition().getLongPosition(i - 2);
				ra.setPosition(position, i);
			}
			int index = 0;
			for (int j = 0; j < h; j++) {
				ra.setPosition(minY + j, 1);
				for (int i = 0; i < w; i++) {
					ra.setPosition(minX + i, 0);
					if (ra.get().get()) bankData[index] = -1;
					index++;
				}
			}
			final Raster raster =
				Raster.createRaster(sm, db, new java.awt.Point(0, 0));
			img.setData(raster);
			imgf.setBounds(new Rectangle2D.Double(minX, minY, w, h));
			imgf.setBufferedImage(img);
		}
	}

	@Override
	public JHotDrawTool getCreationTool(final ImageDisplay display,
		final OverlayCreatedListener listener)
	{
		return new IJCreationTool(display, this, listener);
	}

}
