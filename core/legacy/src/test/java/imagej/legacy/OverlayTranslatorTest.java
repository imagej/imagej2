//
// OverlayTranslatorTest.java
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

package imagej.legacy;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import ij.ImagePlus;
import ij.gui.PolygonRoi;
import ij.gui.Roi;
import ij.gui.ShapeRoi;
import ij.process.ByteProcessor;
import imagej.data.Dataset;
import imagej.data.display.ImageDisplay;
import imagej.data.roi.BinaryMaskOverlay;
import imagej.data.roi.EllipseOverlay;
import imagej.data.roi.Overlay;
import imagej.data.roi.PolygonOverlay;
import imagej.data.roi.RectangleOverlay;

import java.awt.Transparency;
import java.awt.color.ColorSpace;
import java.awt.image.ColorModel;
import java.awt.image.ComponentColorModel;
import java.awt.image.DataBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import net.imglib2.RandomAccess;
import net.imglib2.RealLocalizable;
import net.imglib2.RealPoint;
import net.imglib2.RealRandomAccess;
import net.imglib2.img.Axes;
import net.imglib2.img.Axis;
import net.imglib2.img.Img;
import net.imglib2.img.ImgPlus;
import net.imglib2.img.NativeImg;
import net.imglib2.img.array.ArrayImgFactory;
import net.imglib2.img.basictypeaccess.BitAccess;
import net.imglib2.img.basictypeaccess.ByteAccess;
import net.imglib2.img.transform.ImgTranslationAdapter;
import net.imglib2.roi.BinaryMaskRegionOfInterest;
import net.imglib2.roi.PolygonRegionOfInterest;
import net.imglib2.type.logic.BitType;
import net.imglib2.type.numeric.integer.ByteType;

import org.junit.Test;

/**
 * Unit tests for {@link OverlayTranslator}.
 * 
 * @author Lee Kamentsky
 */
public class OverlayTranslatorTest {

	private PolygonOverlay
		makePolygonOverlay(final double[] x, final double[] y)
	{
		assertEquals(x.length, y.length);
		final PolygonOverlay overlay = new PolygonOverlay();
		final PolygonRegionOfInterest roi = overlay.getRegionOfInterest();
		for (int i = 0; i < x.length; i++) {
			roi.addVertex(i, new RealPoint(x[i], y[i]));
		}
		return overlay;
	}

	private RectangleOverlay makeRectangleOverlay(final double x,
		final double y, final double w, final double h)
	{
		final RectangleOverlay overlay = new RectangleOverlay();
		overlay.getRegionOfInterest().setOrigin(new double[] { x, y });
		overlay.getRegionOfInterest().setExtent(new double[] { w, h });
		return overlay;
	}

	private EllipseOverlay makeEllipseOverlay(final double x, final double y,
		final double w, final double h)
	{
		final EllipseOverlay overlay = new EllipseOverlay();
		overlay.getRegionOfInterest().setOrigin(new double[] { x, y });
		overlay.getRegionOfInterest().setRadius(w / 2, 0);
		overlay.getRegionOfInterest().setRadius(h / 2, 1);
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
	private BinaryMaskOverlay makeBinaryMaskOverlay(final int x, final int y,
		final boolean[][] mask)
	{
		final long w = mask.length;
		final long h = mask[0].length;
		final NativeImg<BitType, BitAccess> img =
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
			new ImgTranslationAdapter<BitType, Img<BitType>>(img,
				new long[] { x, y });
		final BinaryMaskOverlay overlay =
			new BinaryMaskOverlay(
				new BinaryMaskRegionOfInterest<BitType, Img<BitType>>(offsetImg));
		return overlay;
	}

	/**
	 * Make an image plus initialized with random values via a ByteProcessor
	 * 
	 * @param name - name for the ImagePlus
	 * @param r - instance of Random to be used to fill
	 * @param w - width of ImagePlus
	 * @param h - height of ImagePlus
	 * @return a
	 */
	private ImagePlus makeImagePlus(final String name, final byte[][] image) {
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

	private Dataset makeDataset(final byte[][] data, final String name) {
		final int w = data.length;
		final int h = data[0].length;
		final NativeImg<ByteType, ByteAccess> img =
			new ArrayImgFactory<ByteType>().createByteInstance(new long[] { w, h },
				1);
		final ByteType t = new ByteType(img);
		img.setLinkedType(t);
		final RandomAccess<ByteType> ra = img.randomAccess();
		for (int i = 0; i < w; i++) {
			ra.setPosition(i, 0);
			for (int j = 0; j < h; j++) {
				ra.setPosition(j, 1);
				ra.get().set(data[i][j]);
			}
		}
		return new Dataset(new ImgPlus<ByteType>(img, name, new Axis[] { Axes.X,
			Axes.Y }));
	}

	private PolygonRoi makePolygonROI(final int[] x, final int[] y) {
		return makePolygonROI(x, y, Roi.POLYGON);
	}

	private PolygonRoi makeFreeROI(final int[] x, final int[] y) {
		return makePolygonROI(x, y, Roi.FREEROI);
	}

	private PolygonRoi makePolygonROI(final int[] x, final int[] y,
		final int type)
	{
		return new PolygonRoi(x, y, x.length, type);
	}

	private byte[][]
		makeRandomByteArray(final Random r, final int w, final int h)
	{
		final byte[][] data = new byte[w][];
		for (int i = 0; i < w; i++) {
			data[i] = new byte[h];
			r.nextBytes(data[i]);
		}
		return data;
	}

	private byte[][]
		makeRandomMaskArray(final Random r, final int w, final int h)
	{
		final byte[][] data = makeRandomByteArray(r, w, h);
		for (int i = 0; i < w; i++) {
			for (int j = 0; j < h; j++)
				data[i][j] = (data[i][j] >= 0) ? 0 : (byte) 0xFF;
		}
		return data;
	}

	private boolean[][] makeRandomBooleanArray(final Random r, final int w,
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

	/**
	 * Test method for
	 * {@link imagej.legacy.OverlayTranslator#setDisplayOverlays(ImageDisplay, ImagePlus)}
	 * .
	 */
	@Test
	public void testSetDatasetOverlays() {
		// TODO: this just runs the code, but does not check the results.
//		OverlayTranslator ot = new OverlayTranslator();
//		Random r = new Random(1234);
//		Dataset ds = makeDataset(makeRandomByteArray(r, 11, 15), "Foo");
//		ImagePlus imagePlus = makeImagePlus("Bar", makeRandomByteArray(r, 11, 15));
//		imagePlus.setRoi(makePolygonROI(new int[] { 0, 5, 5, 0, 0}, new int[] { 0, 0, 5, 5, 0}));
//		ot.setDatasetOverlays(ds, imagePlus);
	}

	/**
	 * Test method for
	 * {@link imagej.legacy.OverlayTranslator#setImagePlusOverlays(imagej.data.Dataset, ij.ImagePlus)}
	 * .
	 */
	@Test
	public void testSetImagePlusOverlays() {
		// TODO: there are no headless displays at this point, so this pretty much
		// does nothing.
		// So someone needs to make it really test something when headless displays
		// become available
//		OverlayTranslator ot = new OverlayTranslator();
//		Random r = new Random(1234);
//		Dataset ds = makeDataset(makeRandomByteArray(r, 11, 15), "Foo");
//		ImagePlus imagePlus = makeImagePlus("Bar", makeRandomByteArray(r, 11, 15));
//		ot.setImagePlusOverlays(ds, imagePlus);
	}

	/**
	 * Test method for
	 * {@link imagej.legacy.OverlayTranslator#getOverlays(ij.ImagePlus)}.
	 */
	@Test
	public void testGetOverlays() {
		// Just test that we get a single overlay of the correct type. Other tests
		// for particulars of the decoding.
		final Random r = new Random(1234);
		final OverlayTranslator ot = new OverlayTranslator();
		final ImagePlus imagePlus =
			makeImagePlus("Bar", makeRandomByteArray(r, 11, 15));
		imagePlus.setRoi(makePolygonROI(new int[] { 0, 5, 5, 0, 0 }, new int[] {
			0, 0, 5, 5, 0 }));
		final List<Overlay> list = ot.getOverlays(imagePlus);
		assertEquals(1, list.size());
		assertTrue(list.get(0) instanceof PolygonOverlay);
	}

	/**
	 * Test method for
	 * {@link imagej.legacy.OverlayTranslator#setOverlays(java.util.List, ij.ImagePlus)}
	 * .
	 */
	@Test
	public void testSetOverlays() {
		final Random r = new Random(1234);
		final OverlayTranslator ot = new OverlayTranslator();
		final ImagePlus imagePlus =
			makeImagePlus("Bar", makeRandomByteArray(r, 11, 15));
		final ArrayList<Overlay> l = new ArrayList<Overlay>();
		l.add(makePolygonOverlay(new double[] { 0, 5, 5, 0, 0 }, new double[] { 0,
			0, 5, 5, 0 }));
		ot.setOverlays(l, imagePlus);
		final Roi roi = imagePlus.getRoi();
		assertEquals(roi.getType(), Roi.POLYGON);
		assertTrue(roi instanceof PolygonRoi);
	}

	// TODO: authors should probably test the individual overlay and ROI
	// translators that they wrote
	@Test
	public void testPolygonOverlay() {
		final Random r = new Random(1234);
		final int[][][] vertices =
			new int[][][] { { { 0, 5, 5, 0 }, { 0, 0, 5, 5 } },
				{ { 3, 8, 8, 3 }, { 5, 9, 9, 5 } },
				{ { 1, 2, 3, 4, 5, 6 }, { 2, 4, 8, 16, 32, 64 } } };
		int index = 1;
		for (final int[][] testCase : vertices) {
			final OverlayTranslator ot = new OverlayTranslator();
			final ImagePlus imagePlus =
				makeImagePlus("Bar", makeRandomByteArray(r, 11, 15));
			imagePlus.setRoi(makePolygonROI(testCase[0], testCase[1]));
			final List<Overlay> list = ot.getOverlays(imagePlus);
			assertEquals(1, list.size());
			assertTrue(list.get(0) instanceof PolygonOverlay);
			final PolygonOverlay overlay = (PolygonOverlay) (list.get(0));
			final PolygonRegionOfInterest roi = overlay.getRegionOfInterest();
			assertEquals(roi.getVertexCount(), testCase[0].length);
			for (int i = 0; i < testCase[0].length; i++) {
				final RealLocalizable pt = roi.getVertex(i);
				boolean found = false;
				for (int j = 0; j < testCase[0].length; j++) {
					if ((Math.abs(pt.getDoublePosition(0) - testCase[0][j]) < .0001) &&
						(Math.abs(pt.getDoublePosition(1) - testCase[1][j]) < .0001))
					{
						found = true;
						break;
					}
				}
				assertTrue(String.format("Test case %d had bad point = %f,%f", index,
					pt.getDoublePosition(0), pt.getDoublePosition(1)), found);
			}
			index++;
		}
	}

	@Test
	public void testPolygonROI() {
		final Random r = new Random(1234);
		final double[][][] vertices =
			new double[][][] { { { 0, 5, 5, 0 }, { 0, 0, 5, 5 } },
				{ { 3, 8, 8, 3 }, { 5, 9, 9, 5 } },
				{ { 1, 2, 3, 4, 5, 6 }, { 2, 4, 8, 16, 32, 64 } } };
		int index = 1;
		for (final double[][] testCase : vertices) {
			final OverlayTranslator ot = new OverlayTranslator();
			final PolygonOverlay overlay =
				makePolygonOverlay(testCase[0], testCase[1]);
			final ImagePlus imagePlus =
				makeImagePlus("Bar", makeRandomByteArray(r, 11, 15));
			final ArrayList<Overlay> overlays = new ArrayList<Overlay>();
			overlays.add(overlay);
			ot.setOverlays(overlays, imagePlus);
			assertTrue(imagePlus.getRoi() instanceof PolygonRoi);
			final PolygonRoi roi = (PolygonRoi) (imagePlus.getRoi());
			final int[] x = roi.getXCoordinates();
			assertEquals(x.length, testCase[0].length);
			final int[] y = roi.getYCoordinates();
			assertEquals(y.length, testCase[1].length);
			final int x0 = roi.getBounds().x;
			final int y0 = roi.getBounds().y;
			for (int i = 0; i < testCase[0].length; i++) {
				boolean found = false;
				for (int j = 0; j < testCase[0].length; j++) {
					if ((x[i] + x0 == testCase[0][j]) && (y[i] + y0 == testCase[1][j])) {
						found = true;
						break;
					}
				}
				assertTrue(String.format("Test case %d had bad point = %d,%d", index,
					x[i], y[i]), found);
			}
			index++;
		}
	}

	@Test
	public void testCompositeRoi() {
		/*
		 * The composite Roi has an offset and its contained Rois are relative to that offset
		 */
		final OverlayTranslator ot = new OverlayTranslator();
		final Random r = new Random(1234);
		final ImagePlus imagePlus =
			makeImagePlus("Bar", makeRandomByteArray(r, 11, 15));
		/*
		 * Put a rectangular hole inside a rectangle - hopefully this is too much and falls into the default code.
		 */
		final Roi r1 =
			makePolygonROI(new int[] { 3, 5, 5, 3 }, new int[] { 8, 8, 10, 10 });
		final Roi r2 =
			makePolygonROI(new int[] { 8, 8, 10, 10 }, new int[] { 3, 5, 5, 3 });
		final Roi roi = new ShapeRoi(r1).xor(new ShapeRoi(r2));

		// Is the trailing edge in or out? I suppose a sane person would say that
		// the way Java does it must be correct and arguably, of course, it is.
		//
		// But what if you take that geometric figure and mirror or rotate it so
		// the leading edge is the trailing and vice-versa? My way gets the same
		// answer, that the transformed points are in the transformed region
		// and I don't know if theirs does.
		// The following are the discrepancies.

		final int[][] questionablePairs =
			new int[][] { { 3, 10 }, { 4, 10 }, { 5, 10 }, { 5, 8 }, { 5, 9 },
				{ 8, 5 }, { 9, 5 }, { 10, 5 }, { 10, 3 }, { 10, 4 } };
		imagePlus.setRoi(roi);
		final List<Overlay> list = ot.getOverlays(imagePlus);
		assertEquals(1, list.size());
		final RealRandomAccess<BitType> ra =
			list.get(0).getRegionOfInterest().realRandomAccess();
		for (int i = 0; i < 11; i++) {
			ra.setPosition(i, 0);
			for (int j = 0; j < 11; j++) {
				ra.setPosition(j, 1);
				boolean skip = false;
				for (int k = 0; k < questionablePairs.length; k++) {
					if ((i == questionablePairs[k][0]) && (j == questionablePairs[k][1]))
					{
						skip = true;
						break;
					}
				}
				if (!skip) assertEquals(roi.contains(i, j), ra.get().get());
			}
		}
	}

	@Test
	public void testDonut() {
		final OverlayTranslator ot = new OverlayTranslator();
		final Random r = new Random(1234);
		final ImagePlus imagePlus =
			makeImagePlus("Bar", makeRandomByteArray(r, 11, 15));
		/*
		 * Put a rectangular hole inside a rectangle. This should translate to
		 * a composite ROI that can deal with it.
		 */
		final int[] r1x = new int[] { 6, 11, 11, 6, 6 };
		final int[] r1y = new int[] { 9, 9, 15, 15, 9 };
		final int[] r2x = new int[] { 8, 9, 9, 8, 8 };
		final int[] r2y = new int[] { 11, 11, 13, 13, 11 };
		final int[][] all_x = new int[][] { r1x, r2x };
		final int[][] all_y = new int[][] { r1y, r2y };
		final Roi r1 = makePolygonROI(r1x, r1y);
		final Roi r2 = makePolygonROI(r2x, r2y);
		final Roi roi = new ShapeRoi(r1).not(new ShapeRoi(r2));

		imagePlus.setRoi(roi);
		final List<Overlay> list = ot.getOverlays(imagePlus);
		for (int i = 0; i < 12; i++) {
			boolean ignore = false;
			for (final int[] aa : all_x) {
				for (final int c : aa) {
					if (i == c) {
						ignore = true;
						break;
					}
				}
			}
			if (ignore) continue;
			for (int j = 0; j < 14; j++) {
				ignore = false;
				for (final int[] aa : all_y) {
					for (final int c : aa) {
						if (j == c) {
							ignore = true;
							break;
						}
					}
				}
				if (ignore) continue;
				boolean contains = false;
				for (final Overlay overlay : list) {
					final RealRandomAccess<BitType> ra =
						overlay.getRegionOfInterest().realRandomAccess();
					ra.setPosition(i, 0);
					ra.setPosition(j, 1);
					contains |= ra.get().get();
				}
				assertEquals(roi.contains(i, j), contains);
			}
		}
	}

	@Test
	public void testCreateBinaryMaskOverlay() {
		final OverlayTranslator ot = new OverlayTranslator();
		final Random r = new Random(1234);
		final ImagePlus imagePlus =
			makeImagePlus("Bar", makeRandomByteArray(r, 11, 15));
		/*
		 * Put a rectangular hole inside a rectangle - hopefully this is too much and falls into the default code.
		 */
		final Roi roi =
			makeFreeROI(new int[] { 6, 11, 11, 6, 6 }, new int[] { 9, 9, 15, 15, 9 });

		imagePlus.setRoi(roi);
		final List<Overlay> list = ot.getOverlays(imagePlus);
		assertEquals(1, list.size());
		assertTrue(list.get(0) instanceof BinaryMaskOverlay);
		final BinaryMaskOverlay overlay = (BinaryMaskOverlay) (list.get(0));
		final RealRandomAccess<BitType> ra =
			overlay.getRegionOfInterest().realRandomAccess();
		for (int i = 0; i < 9; i++) {
			ra.setPosition(i, 0);
			for (int j = 0; j < 14; j++) {
				ra.setPosition(j, 1);
				assertEquals(roi.contains(i, j), ra.get().get());
			}
		}
	}

	@Test
	public void testCreateBinaryMaskROI() {
		final Random r = new Random(54321);
		final boolean[][] data = makeRandomBooleanArray(r, 7, 8);
		final BinaryMaskOverlay overlay = makeBinaryMaskOverlay(5, 6, data);
		final RealRandomAccess<BitType> ra =
			overlay.getRegionOfInterest().realRandomAccess();
		final OverlayTranslator ot = new OverlayTranslator();
		final ImagePlus imagePlus =
			makeImagePlus("Bar", makeRandomByteArray(r, 15, 20));
		final ArrayList<Overlay> overlays = new ArrayList<Overlay>();
		overlays.add(overlay);
		ot.setOverlays(overlays, imagePlus);
		final Roi roi = imagePlus.getRoi();
		for (int i = 0; i < 15; i++) {
			ra.setPosition(i, 0);
			for (int j = 0; j < 20; j++) {
				ra.setPosition(j, 1);
				assertEquals(ra.get().get(), roi.contains(i, j));
			}
		}
	}

}
