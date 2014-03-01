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

package imagej.legacy;

import static org.junit.Assert.assertEquals;
import ij.ImagePlus;
import ij.gui.PolygonRoi;
import ij.gui.Roi;
import ij.process.ByteProcessor;
import imagej.data.overlay.BinaryMaskOverlay;
import imagej.data.overlay.PolygonOverlay;

import java.awt.Transparency;
import java.awt.color.ColorSpace;
import java.awt.image.ColorModel;
import java.awt.image.ComponentColorModel;
import java.awt.image.DataBuffer;
import java.util.Random;

import net.imglib2.RandomAccess;
import net.imglib2.RealPoint;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImg;
import net.imglib2.img.array.ArrayImgFactory;
import net.imglib2.img.basictypeaccess.array.BitArray;
import net.imglib2.img.transform.ImgTranslationAdapter;
import net.imglib2.roi.BinaryMaskRegionOfInterest;
import net.imglib2.roi.PolygonRegionOfInterest;
import net.imglib2.type.logic.BitType;

import org.scijava.Context;

/**
 * This class exists purely so that the ij.ImagePlus class is not defined before
 * OverlayHarmonizerTest's methods call new ImageJ() which in turn
 * will initialize the LegacyService that wants to re-define the ImageJ class.
 * 
 * @author Johannes Schindelin
 */
public class Helper {

	public static PolygonOverlay makePolygonOverlay(final Context context,
		final double[] x, final double[] y)
	{
		assertEquals(x.length, y.length);
		final PolygonOverlay overlay = new PolygonOverlay(context);
		final PolygonRegionOfInterest roi = overlay.getRegionOfInterest();
		for (int i = 0; i < x.length; i++) {
			roi.addVertex(i, new RealPoint(x[i], y[i]));
		}
		return overlay;
	}

	/**
	 * Make a binary mask overlay by making the pixels indicated by the
	 * coordinates part of the ROI
	 * 
	 * @param x - x coordinates of the pixels
	 * @param y - y coordinates of the pixels
	 * @return a binary mask overlay with the ROI inside
	 */
	public static BinaryMaskOverlay<BitType, Img<BitType>> makeBinaryMaskOverlay(final Context context,
		final int x, final int y, final boolean[][] mask)
	{
		final long w = mask.length;
		final long h = mask[0].length;
		final ArrayImg<BitType, BitArray> img =
			new ArrayImgFactory<BitType>().createBitInstance(new long[] { w, h }, 1);
		final BitType t = new BitType(img);
		img.setLinkedType(t);
		final RandomAccess<BitType> ra = img.randomAccess();
		for (int i = 0; i < mask.length; i++) {
			ra.setPosition(i, 0);
			for (int j = 0; j < mask[i].length; j++) {
				ra.setPosition(j, 1);
				ra.get().set(mask[i][j]);
			}
		}
		final Img<BitType> offsetImg =
			new ImgTranslationAdapter<BitType, Img<BitType>>(img, new long[] { x, y });
		final BinaryMaskOverlay<BitType, Img<BitType>> overlay =
			new BinaryMaskOverlay<BitType, Img<BitType>>(context,
				new BinaryMaskRegionOfInterest<BitType, Img<BitType>>(offsetImg));
		return overlay;
	}

	/**
	 * Make an ImagePlus initialized with the given values.
	 * 
	 * @param name - name for the ImagePlus
	 * @param image - matrix containing image data
	 * @return the newly created ImagePlus
	 */
	public static ImagePlus
		makeImagePlus(final String name, final byte[][] image)
	{
		final int w = image.length;
		final int h = image[0].length;
		final byte[] data = new byte[w * h];
		for (int i = 0; i < data.length; i++) {
			data[i] = image[i / h][i % h];
		}
		final ColorModel cm =
			new ComponentColorModel(ColorSpace.getInstance(ColorSpace.CS_GRAY),
				new int[] { 8 }, false, false, Transparency.OPAQUE,
				DataBuffer.TYPE_BYTE);
		final ByteProcessor ip = new ByteProcessor(w, h, data, cm);
		final ImagePlus imp = new ImagePlus(name, ip);

		return imp;
	}

	public static PolygonRoi makePolygonROI(final int[] x, final int[] y) {
		return makePolygonROI(x, y, Roi.POLYGON);
	}

	public static PolygonRoi makeFreeROI(final int[] x, final int[] y) {
		return makePolygonROI(x, y, Roi.FREEROI);
	}

	private static PolygonRoi makePolygonROI(final int[] x, final int[] y,
		final int type)
	{
		return new PolygonRoi(x, y, x.length, type);
	}

	public static byte[][] makeRandomByteArray(final Random r, final int w,
		final int h)
	{
		final byte[][] data = new byte[w][];
		for (int i = 0; i < w; i++) {
			data[i] = new byte[h];
			r.nextBytes(data[i]);
		}
		return data;
	}

	public static boolean[][] makeRandomBooleanArray(final Random r, final int w,
		final int h)
	{
		final boolean[][] data = new boolean[w][];
		for (int i = 0; i < w; i++) {
			data[i] = new boolean[h];
			for (int j = 0; j < h; j++)
				data[i][j] = r.nextBoolean();
		}
		return data;
	}

}
