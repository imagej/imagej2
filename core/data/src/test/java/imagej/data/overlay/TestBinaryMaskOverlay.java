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

package imagej.data.overlay;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import imagej.ImageJ;
import imagej.util.ColorRGB;
import imagej.util.Log;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Random;

import net.imglib2.RandomAccess;
import net.imglib2.RealRandomAccess;
import net.imglib2.img.Img;
import net.imglib2.img.NativeImg;
import net.imglib2.img.array.ArrayImgFactory;
import net.imglib2.img.basictypeaccess.BitAccess;
import net.imglib2.roi.BinaryMaskRegionOfInterest;
import net.imglib2.type.logic.BitType;

import org.junit.Test;

/**
 * Unit tests for {@link BinaryMaskOverlay}.
 * 
 * @author Lee Kamentsky
 */
public class TestBinaryMaskOverlay {

	private Img<BitType> makeImg(final boolean[][] imgArray) {
		final NativeImg<BitType, ? extends BitAccess> img =
			new ArrayImgFactory<BitType>().createBitInstance(new long[] {
				imgArray.length, imgArray[0].length }, 1);
		final BitType t = new BitType(img);
		img.setLinkedType(t);
		final RandomAccess<BitType> ra = img.randomAccess();
		for (int i = 0; i < imgArray.length; i++) {
			ra.setPosition(i, 0);
			for (int j = 0; j < imgArray[i].length; j++) {
				ra.setPosition(j, 1);
				ra.get().set(imgArray[i][j]);
			}
		}
		return img;
	}

	private BinaryMaskRegionOfInterest<BitType, Img<BitType>> makeRoi(
		final boolean[][] imgArray)
	{
		return new BinaryMaskRegionOfInterest<BitType, Img<BitType>>(
			makeImg(imgArray));
	}

	private BinaryMaskOverlay makeOverlay(final ImageJ context, final boolean[][] imgArray) {
		return new BinaryMaskOverlay(context, makeRoi(imgArray));
	}

	@Test
	public void testWriteExternal() {
		final ImageJ context = ImageJ.createContext();
		final BinaryMaskOverlay overlay =
			makeOverlay(context, new boolean[][] { { true } });
		final ByteArrayOutputStream os = new ByteArrayOutputStream();
		try {
			final ObjectOutputStream out = new ObjectOutputStream(os);
			out.writeObject(overlay);
		}
		catch (final IOException e) {
			Log.error(e);
			throw new AssertionError(e.getMessage());
		}

	}

	@Test
	public void testReadExternal() {
		final ImageJ context = ImageJ.createContext();
		final Random r = new Random(54321);
		for (int iter = 0; iter < 100; iter++) {
			final boolean[][] imgArray = new boolean[5][5];
			for (int i = 0; i < 5; i++) {
				for (int j = 0; j < 5; j++) {
					imgArray[i][j] = r.nextBoolean();
				}
			}
			final BinaryMaskOverlay overlay = makeOverlay(context, imgArray);
			final ColorRGB colorIn =
				new ColorRGB(r.nextInt(256), r.nextInt(256), r.nextInt(256));
			overlay.setFillColor(colorIn);
			overlay.setAlpha(r.nextInt(256));
			final ByteArrayOutputStream os = new ByteArrayOutputStream();
			try {
				final ObjectOutputStream out = new ObjectOutputStream(os);
				out.writeObject(overlay);
				final byte[] buffer = os.toByteArray();
				final ObjectInputStream in =
					new ObjectInputStream(new ByteArrayInputStream(buffer));
				final Object o = in.readObject();
				assertEquals(0, in.available());
				assertTrue(o instanceof BinaryMaskOverlay);
				final BinaryMaskOverlay overlayOut = (BinaryMaskOverlay) o;
				assertEquals(overlayOut.getAlpha(), overlay.getAlpha());
				final ColorRGB fillOut = overlayOut.getFillColor();
				assertEquals(colorIn, fillOut);
				final RealRandomAccess<BitType> ra =
					overlayOut.getRegionOfInterest().realRandomAccess();
				for (int i = 0; i < 5; i++) {
					ra.setPosition(i, 0);
					for (int j = 0; j < 5; j++) {
						ra.setPosition(j, 1);
						assertEquals(imgArray[i][j], ra.get().get());
					}
				}
			}
			catch (final IOException e) {
				Log.error(e);
				throw new AssertionError(e.getMessage());
			}
			catch (final ClassNotFoundException e) {
				Log.error(e);
				throw new AssertionError(e.getMessage());
			}
		}
	}

	@Test
	public void testBinaryMaskOverlay() {
		final ImageJ context = ImageJ.createContext();
		makeOverlay(context, new boolean[][] { { true } });
	}

}
