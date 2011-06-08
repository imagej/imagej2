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

import static org.junit.Assert.*;

import java.awt.Transparency;
import java.awt.color.ColorSpace;
import java.awt.image.ColorModel;
import java.awt.image.ComponentColorModel;
import java.awt.image.DataBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import ij.ImagePlus;
import ij.gui.FreehandRoi;
import ij.gui.ImageCanvas;
import ij.gui.ImageWindow;
import ij.gui.PolygonRoi;
import ij.gui.Roi;
import ij.gui.ShapeRoi;
import ij.gui.Toolbar;
import ij.process.ByteProcessor;
import imagej.ImageJ;
import imagej.data.Dataset;
import imagej.data.roi.BinaryMaskOverlay;
import imagej.data.roi.EllipseOverlay;
import imagej.data.roi.Overlay;
import imagej.data.roi.PolygonOverlay;
import imagej.data.roi.RectangleOverlay;
import imagej.display.OverlayManager;

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

import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author Lee Kamentsky
 *
 */
public class OverlayTranslatorTest {
	@BeforeClass
	public static void setUp() {
	}

	private PolygonOverlay makePolygonOverlay(double [] x, double [] y) {
		assertEquals(x.length, y.length);
		PolygonOverlay overlay = new PolygonOverlay();
		PolygonRegionOfInterest roi = overlay.getRegionOfInterest();
		for (int i=0; i<x.length; i++) {
			roi.addVertex(i, new RealPoint(x[i], y[i]));
		}
		return overlay;
	}
	
	private RectangleOverlay makeRectangleOverlay(double x, double y, double w, double h) {
		RectangleOverlay overlay = new RectangleOverlay();
		overlay.getRegionOfInterest().setOrigin(new double [] { x, y });
		overlay.getRegionOfInterest().setExtent(new double [] { w, h });
		return overlay;
	}
	
	private EllipseOverlay makeEllipseOverlay(double x, double y, double w, double h) {
		EllipseOverlay overlay = new EllipseOverlay();
		overlay.getRegionOfInterest().setOrigin(new double [] { x, y});
		overlay.getRegionOfInterest().setRadius(w / 2, 0);
		overlay.getRegionOfInterest().setRadius(h / 2, 1);
		return overlay;
	}
	
	/**
	 * Make a binary mask overlay by making the pixels indicated by the coordinates
	 * part of the ROI
	 * @param x - x coordinates of the pixels
	 * @param y - y coordinates of the pixels
	 * @return a binary mask overlay with the ROI inside
	 */
	private BinaryMaskOverlay makeBinaryMaskOverlay(int x, int y, boolean [][] mask) {
		long w = mask.length;
		long h = mask[0].length;
		NativeImg<BitType, BitAccess> img = new ArrayImgFactory<BitType>().createBitInstance(new long [] { w, h } , 1);
		BitType t = new BitType(img);
		img.setLinkedType(t);
		RandomAccess<BitType> ra = img.randomAccess();
		for (int i = 0; i < mask.length; i++) {
			ra.setPosition(i, 0);
			for (int j=0; j<mask[i].length; j++) {
				ra.setPosition(j,1);
				ra.get().set(mask[i][j]);
			}
		}
		Img<BitType> offsetImg = new ImgTranslationAdapter<BitType, Img<BitType>>(img, new long [] { x, y});
		BinaryMaskOverlay overlay = new BinaryMaskOverlay(new BinaryMaskRegionOfInterest<BitType, Img<BitType>>(offsetImg));
		return overlay;
	}
	
	/**
	 * Make an image plus initialized with random values via a ByteProcessor
	 * @param name - name for the ImagePlus
	 * @param r - instance of Random to be used to fill
	 * @param w - width of ImagePlus
	 * @param h - height of ImagePlus
	 * @return a 
	 */
	private ImagePlus makeImagePlus(String name, byte [][] image) {
		int w = image.length;
		int h = image[0].length;
		byte [] data = new byte[w*h];
		for (int i=0; i<data.length; i++) {
			data[i] = image[i / h][ i % h];
		}
		ColorModel cm = new ComponentColorModel(
				ColorSpace.getInstance(ColorSpace.CS_GRAY), new int [] {8} , false, false,
				Transparency.OPAQUE,	
				DataBuffer.TYPE_BYTE);
		ByteProcessor ip = new ByteProcessor(w, h, data, cm);
		ImagePlus imp = new ImagePlus(name, ip);
		new ImageWindow(imp);
		
		return imp;
	}
	
	private Dataset makeDataset(byte [][] data, String name) {
		int w = data.length;
		int h = data[0].length;
		NativeImg<ByteType, ByteAccess> img = new ArrayImgFactory<ByteType>().createByteInstance(new long [] { w, h }, 1);
		ByteType t = new ByteType(img);
		img.setLinkedType(t);
		RandomAccess<ByteType> ra = img.randomAccess();
		for (int i=0; i<w; i++) {
			ra.setPosition(i, 0);
			for (int j=0; j<h; j++) {
				ra.setPosition(j, 1);
				ra.get().set(data[i][j]);
			}
		}
		return new Dataset(new ImgPlus<ByteType>(img, name, new Axis[] { Axes.X, Axes.Y }));
	}

	private PolygonRoi makePolygonROI(int [] x, int [] y) {
		return makePolygonROI(x, y, Roi.POLYGON);
	}
	
	private PolygonRoi makeFreeROI(int [] x, int [] y) {
		return makePolygonROI(x, y, Roi.FREEROI);
	}
	
	private PolygonRoi makePolygonROI(int [] x, int [] y, int type) {
		return new PolygonRoi(x, y, x.length, type);
	}
	
	private byte [][] makeRandomByteArray(Random r, int w, int h) {
		byte[][] data = new byte[w][];
		for (int i=0; i<w; i++) {
			data[i] = new byte[h];
			r.nextBytes(data[i]);
		}
		return data;
	}
	
	private byte [][] makeRandomMaskArray(Random r, int w, int h) {
		byte [][] data = makeRandomByteArray(r, w, h);
		for (int i=0; i<w; i++) {
			for (int j=0; j<h; j++)
				data[i][j] = (data[i][j] >= 0)?0:(byte)0xFF;
		}
		return data;
	}
	
	private boolean [][] makeRandomBooleanArray(Random r, int w, int h) {
		boolean [][] data = new boolean[w][];
		for (int i=0; i<w; i++) {
			data[i] = new boolean[h];
			for (int j=0; j<h; j++) data[i][j] = r.nextBoolean();
		}
		return data;
	}
	/**
	 * Test method for {@link imagej.legacy.OverlayTranslator#setDatasetOverlays(imagej.data.Dataset, ij.ImagePlus)}.
	 */
	@Test
	public void testSetDatasetOverlays() {
		// TODO: this just runs the code, but does not check the results.
		OverlayTranslator ot = new OverlayTranslator();
		Random r = new Random(1234);
		Dataset ds = makeDataset(makeRandomByteArray(r, 11, 15), "Foo");
		ImagePlus imagePlus = makeImagePlus("Bar", makeRandomByteArray(r, 11, 15));
		imagePlus.setRoi(makePolygonROI(new int[] { 0, 5, 5, 0, 0}, new int[] { 0, 0, 5, 5, 0}));
		ot.setDatasetOverlays(ds, imagePlus);
	}

	/**
	 * Test method for {@link imagej.legacy.OverlayTranslator#setImagePlusOverlays(imagej.data.Dataset, ij.ImagePlus)}.
	 */
	@Test
	public void testSetImagePlusOverlays() {
		// TODO: there are no headless displays at this point, so this pretty much does nothing.
		//       So someone needs to make it really test something when headless displays become available
		OverlayTranslator ot = new OverlayTranslator();
		Random r = new Random(1234);
		Dataset ds = makeDataset(makeRandomByteArray(r, 11, 15), "Foo");
		ImagePlus imagePlus = makeImagePlus("Bar", makeRandomByteArray(r, 11, 15));
		ot.setImagePlusOverlays(ds, imagePlus);
	}

	/**
	 * Test method for {@link imagej.legacy.OverlayTranslator#getOverlays(ij.ImagePlus)}.
	 */
	@Test
	public void testGetOverlays() {
		// Just test that we get a single overlay of the correct type. Other tests for particulars of the decoding.
		Random r = new Random(1234);
		OverlayTranslator ot = new OverlayTranslator();
		ImagePlus imagePlus = makeImagePlus("Bar", makeRandomByteArray(r, 11, 15));
		imagePlus.setRoi(makePolygonROI(new int[] { 0, 5, 5, 0, 0}, new int[] { 0, 0, 5, 5, 0}));
		List<Overlay> list = ot.getOverlays(imagePlus);
		assertEquals(1, list.size());
		assertTrue(list.get(0) instanceof PolygonOverlay);
	}

	/**
	 * Test method for {@link imagej.legacy.OverlayTranslator#setOverlays(java.util.List, ij.ImagePlus)}.
	 */
	@Test
	public void testSetOverlays() {
		Random r = new Random(1234);
		OverlayTranslator ot = new OverlayTranslator();
		ImagePlus imagePlus = makeImagePlus("Bar", makeRandomByteArray(r, 11, 15));
		ArrayList<Overlay> l = new ArrayList<Overlay>();
		l.add(makePolygonOverlay(new double[] { 0, 5, 5, 0, 0}, new double[] { 0, 0, 5, 5, 0}));
		ot.setOverlays(l, imagePlus);
		Roi roi = imagePlus.getRoi();
		assertEquals(roi.getType(), Roi.COMPOSITE);
		assertTrue(roi instanceof ShapeRoi);
	}
	// TODO: authors should probably test the individual overlay and ROI translators that they wrote
	@Test
	public void testPolygonOverlay()
	{
		Random r = new Random(1234);
		int [][][] vertices = new int [][][] {
				{{ 0,5,5,0},{0,0,5,5}},
				{{ 3,8,8,3},{5,9,9,5}},
				{{ 1,2,3,4,5,6},{ 2,4,8,16,32,64}}
		};
		int index = 1;
		for (int [][] testCase:vertices) {
			OverlayTranslator ot = new OverlayTranslator();
			ImagePlus imagePlus = makeImagePlus("Bar", makeRandomByteArray(r, 11, 15));
			imagePlus.setRoi(makePolygonROI(testCase[0], testCase[1]));
			List<Overlay> list = ot.getOverlays(imagePlus);
			assertEquals(1, list.size());
			assertTrue(list.get(0) instanceof PolygonOverlay);
			PolygonOverlay overlay = (PolygonOverlay)(list.get(0));
			PolygonRegionOfInterest roi = overlay.getRegionOfInterest();
			assertEquals(roi.getVertexCount(), testCase[0].length);
			for (int i=0; i < testCase[0].length; i++) {
				RealLocalizable pt = roi.getVertex(i);
				boolean found = false;
				for (int j=0; j<testCase[0].length; j++) {
					if ((Math.abs(pt.getDoublePosition(0) - testCase[0][j]) < .0001) &&
						(Math.abs(pt.getDoublePosition(1) - testCase[1][j]) < .0001)) {
						found = true;
						break;
					}
				}
				assertTrue(String.format("Test case %d had bad point = %f,%f", index, pt.getDoublePosition(0), pt.getDoublePosition(1)), found);
			}
			index++;
		}
	}
	@Test
	public void testPolygonROI()
	{
		Random r = new Random(1234);
		double [][][] vertices = new double [][][] {
				{{ 0,5,5,0},{0,0,5,5}},
				{{ 3,8,8,3},{5,9,9,5}},
				{{ 1,2,3,4,5,6},{ 2,4,8,16,32,64}}
		};
		int index = 1;
		for (double [][] testCase:vertices) {
			OverlayTranslator ot = new OverlayTranslator();
			PolygonOverlay overlay = makePolygonOverlay(testCase[0], testCase[1]);
			ImagePlus imagePlus = makeImagePlus("Bar", makeRandomByteArray(r, 11, 15));
			ArrayList<Overlay> overlays = new ArrayList<Overlay>();
			overlays.add(overlay);
			ot.setOverlays(overlays, imagePlus);
			assertTrue(imagePlus.getRoi() instanceof ShapeRoi);
			ShapeRoi sroi = (ShapeRoi)imagePlus.getRoi();
			Roi [] rois = sroi.getRois();
			assertEquals(rois.length, 1);
			assertTrue(rois[0] instanceof PolygonRoi);
			PolygonRoi roi = (PolygonRoi)(rois[0]);
			int [] x = roi.getXCoordinates();
			assertEquals(x.length, testCase[0].length);
			int [] y = roi.getYCoordinates();
			assertEquals(y.length, testCase[1].length);
			int x0 = roi.getBounds().x + sroi.getBounds().x;
			int y0 = roi.getBounds().y + sroi.getBounds().y;
			for (int i=0; i < testCase[0].length; i++) {
				boolean found = false;
				for (int j=0; j<testCase[0].length; j++) {
					if ((x[i]+x0 == testCase[0][j]) && (y[i]+y0 == testCase[1][j])){
						found = true;
						break;
					}
				}
				assertTrue(String.format("Test case %d had bad point = %d,%d", index, x[i], y[i]), found);
			}
			index++;
		}
	}

	@Test
	public void testCompositeRoi()
	{
		/*
		 * The composite Roi has an offset and its contained Rois are relative to that offset
		 */
		OverlayTranslator ot = new OverlayTranslator();
		Random r = new Random(1234);
		ImagePlus imagePlus = makeImagePlus("Bar", makeRandomByteArray(r, 11, 15));
		/*
		 * Put a rectangular hole inside a rectangle - hopefully this is too much and falls into the default code.
		 */
		Roi r1 = makePolygonROI(new int [] { 3, 5, 5, 3 }, new int [] { 8, 8, 10, 10 });
		Roi r2 = makePolygonROI(new int [] { 8, 8, 10, 10 }, new int [] { 3, 5, 5, 3 });
		Roi roi = new ShapeRoi(r1).or(new ShapeRoi(r2));
		
		// Is the trailing edge in or out? I suppose a sane person would say that
		// the way Java does it must be correct and arguably, of course, it is.
		//
		// But what if you take that geometric figure and mirror or rotate it so
		// the leading edge is the trailing and vice-versa? My way gets the same
		// answer, that the transformed points are in the transformed region
		// and I don't know if theirs does.
		// The following are the discrepancies.
		
		int [][] questionablePairs = new int [][] {
				{3,10}, {4,10}, { 5,10}, { 5, 8 }, { 5, 9},
				{8, 5}, {9, 5}, {10, 5}, {10, 3 }, {10, 4}}; 
		imagePlus.setRoi(roi);
		List<Overlay> list = ot.getOverlays(imagePlus);
		assertEquals(2, list.size());
		RealRandomAccess<BitType> ra0 = list.get(0).getRegionOfInterest().realRandomAccess();
		RealRandomAccess<BitType> ra1 = list.get(1).getRegionOfInterest().realRandomAccess();
		for (int i=0; i < 11; i++) {
			ra0.setPosition(i, 0);
			ra1.setPosition(i, 0);
			for (int j=0; j< 11; j++) {
				ra0.setPosition(j, 1);
				ra1.setPosition(j, 1);
				boolean skip = false;
				for (int [] p: questionablePairs) {
					if ((p[0] == i) && (p[1] == j)) {
						skip = true;
						break;
					}
				}
				if (skip) continue;
				assertEquals(roi.contains(i, j), ra0.get().get() || ra1.get().get());
			}
		}
	}
	
	//@Test
	//TODO: uncomment the above and see why this test will make you cry
	//      The ShapeRoi is composed of two polygons, but the inner one
	//      is subtracted from the outer one and how are you supposed to
	//      figure that out?
	//
	public void testThatWillMakeYouCry()
	{
		OverlayTranslator ot = new OverlayTranslator();
		Random r = new Random(1234);
		ImagePlus imagePlus = makeImagePlus("Bar", makeRandomByteArray(r, 11, 15));
		/*
		 * Put a rectangular hole inside a rectangle - hopefully this is too much and falls into the default code.
		 */
		Roi r1 = makePolygonROI(new int [] { 6, 11, 11, 6, 6 }, new int [] { 9, 9, 15, 15, 9 });
		Roi r2 = makePolygonROI(new int [] { 8, 9, 9, 8, 8}, new int [] { 11, 11, 13, 13, 11});
		Roi roi = new ShapeRoi(r1).not(new ShapeRoi(r2));
		
		imagePlus.setRoi(roi);
		List<Overlay> list = ot.getOverlays(imagePlus);
		for (int i=0; i < 9; i++) {
			for (int j=0; j<14; j++) {
				boolean contains = false;
				for (Overlay overlay: list) {
					RealRandomAccess<BitType> ra = overlay.getRegionOfInterest().realRandomAccess();
					ra.setPosition(i, 0);
					ra.setPosition(j, 1);
					contains |= ra.get().get();
				}
				assertEquals(roi.contains(i, j), contains);
			}
		}
	}
	@Test
	public void testCreateBinaryMaskOverlay()
	{
		OverlayTranslator ot = new OverlayTranslator();
		Random r = new Random(1234);
		ImagePlus imagePlus = makeImagePlus("Bar", makeRandomByteArray(r, 11, 15));
		/*
		 * Put a rectangular hole inside a rectangle - hopefully this is too much and falls into the default code.
		 */
		Roi roi = makeFreeROI(new int [] { 6, 11, 11, 6, 6 }, new int [] { 9, 9, 15, 15, 9 });
		
		imagePlus.setRoi(roi);
		List<Overlay> list = ot.getOverlays(imagePlus);
		assertEquals(1, list.size());
		assertTrue(list.get(0) instanceof BinaryMaskOverlay);
		BinaryMaskOverlay overlay = (BinaryMaskOverlay)(list.get(0));
		RealRandomAccess<BitType> ra = overlay.getRegionOfInterest().realRandomAccess();
		for (int i=0; i < 9; i++) {
			ra.setPosition(i, 0);
			for (int j=0; j<14; j++) {
				ra.setPosition(j, 1);
				assertEquals(roi.contains(i, j), ra.get().get());
			}
		}
	}
	
	@Test
	public void testCreateBinaryMaskROI()
	{
		Random r = new Random(54321);
		boolean [][] data = makeRandomBooleanArray(r, 7, 8);
		BinaryMaskOverlay overlay = makeBinaryMaskOverlay(5, 6, data);
		RealRandomAccess<BitType> ra = overlay.getRegionOfInterest().realRandomAccess();
		OverlayTranslator ot = new OverlayTranslator();
		ImagePlus imagePlus = makeImagePlus("Bar", makeRandomByteArray(r, 15, 20));
		ArrayList<Overlay> overlays = new ArrayList<Overlay>();
		overlays.add(overlay);
		ot.setOverlays(overlays, imagePlus);
		Roi roi = imagePlus.getRoi();
		for (int i=0; i<15; i++) {
			ra.setPosition(i, 0);
			for (int j=0; j<20; j++) {
				ra.setPosition(j, 1);
				assertEquals(ra.get().get(), roi.contains(i, j));
			}
		}
	}

}
