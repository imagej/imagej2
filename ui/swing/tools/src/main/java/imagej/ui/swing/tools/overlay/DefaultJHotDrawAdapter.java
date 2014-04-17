/*
 * #%L
 * ImageJ software for multidimensional image processing and analysis.
 * %%
 * Copyright (C) 2009 - 2014 Board of Regents of the University of
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
 * #L%
 */

package imagej.ui.swing.tools.overlay;

import imagej.data.Position;
import imagej.data.display.ImageDisplay;
import imagej.data.display.OverlayView;
import imagej.data.overlay.Overlay;
import imagej.plugins.uis.swing.overlay.AbstractJHotDrawAdapter;
import imagej.plugins.uis.swing.overlay.IJCreationTool;
import imagej.plugins.uis.swing.overlay.JHotDrawAdapter;
import imagej.plugins.uis.swing.overlay.JHotDrawTool;
import imagej.tool.Tool;

import java.awt.Color;
import java.awt.Shape;
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
import org.scijava.Priority;
import org.scijava.plugin.Plugin;
import org.scijava.util.ColorRGB;

/**
 * The default adapter handles any kind of overlay. It uses the fill color and
 * alpha of the overlay to draw the mask and leaves the rest of the figure
 * transparent.
 * 
 * @author Lee Kamentsky
 */
@Plugin(type = JHotDrawAdapter.class,
	priority = DefaultJHotDrawAdapter.PRIORITY)
public class DefaultJHotDrawAdapter extends
	AbstractJHotDrawAdapter<Overlay, ImageFigure>
{

	public static final double PRIORITY = Priority.VERY_LOW_PRIORITY;

	@Override
	public boolean supports(final Tool tool) {
		return false;
	}

	@Override
	public boolean supports(final Overlay overlay, final Figure figure) {
		return figure == null || figure instanceof ImageFigure;
	}

	@Override
	public Overlay createNewOverlay() {
		return null;
	}

	@Override
	public Figure createDefaultFigure() {
		final ImageFigure figure = new ImageFigure();
		figure.setTransformable(false);
		initDefaultSettings(figure);
		return figure;
	}

	@Override
	public void
		updateFigure(final OverlayView overlay, final ImageFigure figure)
	{
		super.updateFigure(overlay, figure);

		// Override the base: set the fill color to transparent.
		figure.set(AttributeKeys.FILL_COLOR, new Color(0, 0, 0, 0));
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
						(byte) color.getBlue() }, new byte[] { 0,
						(byte) overlay.getData().getAlpha() });
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
			final Position planePos = overlay.getPlanePosition();
			for (int i = 0; i < planePos.numDimensions(); i++) {
				final long position = planePos.getLongPosition(i);
				ra.setPosition(position, i + 2);
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
			figure.setBounds(new Rectangle2D.Double(minX, minY, w, h));
			figure.setBufferedImage(img);
		}
	}

	@Override
	public JHotDrawTool getCreationTool(final ImageDisplay display) {
		return new IJCreationTool<ImageFigure>(display, this);
	}

	@Override
	public Shape toShape(final ImageFigure figure) {
		throw new UnsupportedOperationException();
	}
}
