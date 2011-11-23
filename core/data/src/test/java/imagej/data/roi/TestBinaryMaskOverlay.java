//
// TestBinaryMaskOverlay.java
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

package imagej.data.roi;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import imagej.ImageJ;
import imagej.data.display.OverlayService;
import imagej.util.ColorRGB;

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

import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Unit tests for {@link BinaryMaskOverlay}.
 * 
 * @author Lee Kamentsky
 */
public class TestBinaryMaskOverlay {

	// TODO - Evaluate whether it is really the best option to force creation
	// of an ImageJ context in the test harness so that overlay tests can pass.

	@BeforeClass
	@SuppressWarnings("unchecked")
	public static void setup() {
		ImageJ.createContext(OverlayService.class);
	}
	
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

	private BinaryMaskOverlay makeOverlay(final boolean[][] imgArray) {
		return new BinaryMaskOverlay(makeRoi(imgArray));
	}

	@Test
	public void testWriteExternal() {
		final BinaryMaskOverlay overlay =
			makeOverlay(new boolean[][] { { true } });
		final ByteArrayOutputStream os = new ByteArrayOutputStream();
		try {
			final ObjectOutputStream out = new ObjectOutputStream(os);
			out.writeObject(overlay);
		}
		catch (final IOException e) {
			e.printStackTrace();
			throw new AssertionError(e.getMessage());
		}

	}

	@Test
	public void testReadExternal() {
		final Random r = new Random(54321);
		for (int iter = 0; iter < 100; iter++) {
			final boolean[][] imgArray = new boolean[5][5];
			for (int i = 0; i < 5; i++) {
				for (int j = 0; j < 5; j++) {
					imgArray[i][j] = r.nextBoolean();
				}
			}
			final BinaryMaskOverlay overlay = makeOverlay(imgArray);
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
				e.printStackTrace();
				throw new AssertionError(e.getMessage());
			}
			catch (final ClassNotFoundException e) {
				e.printStackTrace();
				throw new AssertionError(e.getMessage());
			}
		}
	}

	@Test
	public void testBinaryMaskOverlay() {
		makeOverlay(new boolean[][] { { true } });
	}

}
