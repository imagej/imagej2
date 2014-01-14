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
import static org.junit.Assert.assertTrue;
import ij.ImagePlus;
import ij.gui.PolygonRoi;
import ij.gui.Roi;
import ij.gui.ShapeRoi;
import imagej.data.display.ImageDisplay;
import imagej.data.overlay.BinaryMaskOverlay;
import imagej.data.overlay.Overlay;
import imagej.data.overlay.PolygonOverlay;
import imagej.legacy.translate.OverlayHarmonizer;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import net.imglib2.RealLocalizable;
import net.imglib2.RealRandomAccess;
import net.imglib2.img.Img;
import net.imglib2.roi.PolygonRegionOfInterest;
import net.imglib2.roi.RegionOfInterest;
import net.imglib2.type.logic.BitType;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.scijava.Context;

/**
 * Unit tests for {@link OverlayHarmonizer}.
 * 
 * @author Lee Kamentsky
 */
public class OverlayHarmonizerTest {

	static {
		/*
		 * We absolutely require that the LegacyInjector did its job before we
		 * use the ImageJ 1.x classes here, in case the LegacyService tests did
		 * not run yet, so that the classes are properly patched before use.
		 * 
		 * Just loading the class is not enough; it will not get initialized. So
		 * we call the preinit() method just to force class initialization (and
		 * thereby the LegacyInjector to patch ImageJ 1.x).
		 */
		DefaultLegacyService.preinit();
	}

	private Context context;

	@Before
	public void beforeMethod() {
		synchronized (DefaultLegacyService.class) {
			context = new Context(LegacyService.class);
		}
	}

	@After
	public void afterMethod() {
		synchronized (DefaultLegacyService.class) {
			if (context != null) {
				context.dispose();
				context = null;
			}
		}
	}

	/**
	 * Test method for
	 * {@link OverlayHarmonizer#updateDisplay(ImageDisplay, ImagePlus)}.
	 */
	@Test
	public void testUpdateDisplay() {
		// TODO: this just runs the code, but does not check the results.
//		OverlayTranslator ot = new OverlayTranslator();
//		Random r = new Random(1234);
//		Dataset ds = Helper.makeDataset(Helper.makeRandomByteArray(r, 11, 15), "Foo");
//		ImagePlus imagePlus = Helper.makeImagePlus("Bar", Helper.makeRandomByteArray(r, 11, 15));
//		imagePlus.setRoi(Helper.makePolygonROI(new int[] { 0, 5, 5, 0, 0}, new int[] { 0, 0, 5, 5, 0}));
//		ot.setDatasetOverlays(ds, imagePlus);
		synchronized (DefaultLegacyService.class) {
			// fill me some day
		}
	}

	/**
	 * Test method for
	 * {@link OverlayHarmonizer#updateLegacyImage(ImageDisplay, ImagePlus)}.
	 */
	@Test
	public void testUpdateImagePlus() {
		// TODO: there are no headless displays at this point, so this pretty much
		// does nothing.
		// So someone needs to make it really test something when headless displays
		// become available
//		OverlayTranslator ot = new OverlayTranslator();
//		Random r = new Random(1234);
//		Dataset ds = Helper.makeDataset(Helper.makeRandomByteArray(r, 11, 15), "Foo");
//		ImagePlus imagePlus = Helper.makeImagePlus("Bar", Helper.makeRandomByteArray(r, 11, 15));
//		ot.setImagePlusOverlays(ds, imagePlus);
		synchronized (DefaultLegacyService.class) {
			// fill me some day
		}
	}

	/**
	 * Test method for {@link OverlayHarmonizer#getOverlays(ImagePlus)}.
	 */
	@Test
	public void testGetOverlays() {
		synchronized (DefaultLegacyService.class) {
			// Just test that we get a single overlay of the correct type. Other tests
			// for particulars of the decoding.
			final Random r = new Random(1234);
			final OverlayHarmonizer ot =
				new OverlayHarmonizer(context.getService(LegacyService.class));
			final ImagePlus imagePlus =
				Helper.makeImagePlus("Bar", Helper.makeRandomByteArray(r, 11, 15));
			imagePlus.setRoi(Helper.makePolygonROI(new int[] { 0, 5, 5, 0, 0 },
				new int[] { 0, 0, 5, 5, 0 }));
			final List<Overlay> list = ot.getOverlays(imagePlus);
			assertEquals(1, list.size());
			assertTrue(list.get(0) instanceof PolygonOverlay);
		}
	}

	/**
	 * Test method for {@link OverlayHarmonizer#setOverlays(List, ImagePlus)}.
	 */
	@Test
	public void testSetOverlays() {
		synchronized (DefaultLegacyService.class) {
			final Random r = new Random(1234);
			final OverlayHarmonizer ot =
				new OverlayHarmonizer(context.getService(LegacyService.class));
			final ImagePlus imagePlus =
				Helper.makeImagePlus("Bar", Helper.makeRandomByteArray(r, 11, 15));
			final ArrayList<Overlay> l = new ArrayList<Overlay>();
			l.add(Helper.makePolygonOverlay(context, new double[] { 0, 5, 5, 0, 0 },
				new double[] { 0, 0, 5, 5, 0 }));
			ot.setOverlays(l, l.get(0), imagePlus);
			final Roi roi = imagePlus.getRoi();
			assertEquals(roi.getType(), Roi.POLYGON);
			assertTrue(roi instanceof PolygonRoi);
		}
	}

	// TODO: authors should probably test the individual overlay and ROI
	// translators that they wrote
	@Test
	public void testPolygonOverlay() {
		synchronized (DefaultLegacyService.class) {
			final Random r = new Random(1234);
			final int[][][] vertices =
				new int[][][] { { { 0, 5, 5, 0 }, { 0, 0, 5, 5 } },
					{ { 3, 8, 8, 3 }, { 5, 9, 9, 5 } },
					{ { 1, 2, 3, 4, 5, 6 }, { 2, 4, 8, 16, 32, 64 } } };
			int index = 1;
			for (final int[][] testCase : vertices) {
				final OverlayHarmonizer ot =
					new OverlayHarmonizer(context.getService(LegacyService.class));
				final ImagePlus imagePlus =
					Helper.makeImagePlus("Bar", Helper.makeRandomByteArray(r, 11, 15));
				imagePlus.setRoi(Helper.makePolygonROI(testCase[0], testCase[1]));
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
	}

	@Test
	public void testPolygonROI() {
		synchronized (DefaultLegacyService.class) {
			final Random r = new Random(1234);
			final double[][][] vertices =
				new double[][][] { { { 0, 5, 5, 0 }, { 0, 0, 5, 5 } },
					{ { 3, 8, 8, 3 }, { 5, 9, 9, 5 } },
					{ { 1, 2, 3, 4, 5, 6 }, { 2, 4, 8, 16, 32, 64 } } };
			int index = 1;
			for (final double[][] testCase : vertices) {
				final OverlayHarmonizer ot =
					new OverlayHarmonizer(context.getService(LegacyService.class));
				final PolygonOverlay overlay =
					Helper.makePolygonOverlay(context, testCase[0], testCase[1]);
				final ImagePlus imagePlus =
					Helper.makeImagePlus("Bar", Helper.makeRandomByteArray(r, 11, 15));
				final ArrayList<Overlay> overlays = new ArrayList<Overlay>();
				overlays.add(overlay);
				ot.setOverlays(overlays, overlay, imagePlus);
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
						if ((x[i] + x0 == testCase[0][j]) && (y[i] + y0 == testCase[1][j]))
						{
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
	}

	@Test
	public void testCompositeRoi() {
		synchronized (DefaultLegacyService.class) {
			/*
			 * The composite Roi has an offset and its contained Rois are relative to that offset
			 */
			final OverlayHarmonizer ot =
				new OverlayHarmonizer(context.getService(LegacyService.class));
			final Random r = new Random(1234);
			final ImagePlus imagePlus =
				Helper.makeImagePlus("Bar", Helper.makeRandomByteArray(r, 11, 15));
			/*
			 * Put a rectangular hole inside a rectangle - hopefully this is too much and falls into the default code.
			 */
			final Roi r1 =
				Helper.makePolygonROI(new int[] { 3, 5, 5, 3 }, new int[] { 8, 8, 10,
					10 });
			final Roi r2 =
				Helper.makePolygonROI(new int[] { 8, 8, 10, 10 }, new int[] { 3, 5, 5,
					3 });
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
						if ((i == questionablePairs[k][0]) &&
							(j == questionablePairs[k][1]))
						{
							skip = true;
							break;
						}
					}
					if (!skip) assertEquals(roi.contains(i, j), ra.get().get());
				}
			}
		}
	}

	@Test
	public void testDonut() {
		synchronized (DefaultLegacyService.class) {
			final OverlayHarmonizer ot =
				new OverlayHarmonizer(context.getService(LegacyService.class));
			final Random r = new Random(1234);
			final ImagePlus imagePlus =
				Helper.makeImagePlus("Bar", Helper.makeRandomByteArray(r, 11, 15));
			/*
			 * Put a rectangular hole inside a rectangle. This should translate to
			 * a composite ROI that can deal with it.
			 */
			final int[] r1x = new int[] { 6, 11, 11, 6, 6 };
			final int[] r1y = new int[] { 9, 9, 15, 15, 9 };
			final int[] r2x = new int[] { 8, 9, 9, 8, 8 };
			final int[] r2y = new int[] { 11, 11, 13, 13, 11 };
			final Roi r1 = Helper.makePolygonROI(r1x, r1y);
			final Roi r2 = Helper.makePolygonROI(r2x, r2y);
			final Roi roi = new ShapeRoi(r1).not(new ShapeRoi(r2));

			imagePlus.setRoi(roi);
			final List<Overlay> list = ot.getOverlays(imagePlus);
			assertEquals(1, list.size());
			RegionOfInterest roi2 = list.get(0).getRegionOfInterest();
			for (int i = 0; i < 12; i++) {
				for (int j = 0; j < 16; j++) {
					assertEquals("@(" + i + ", " + j + ")", roi.contains(i, j), roi2
						.contains(new double[] { i, j }));
				}
			}
		}
	}

	@Test
	public void testCreateBinaryMaskOverlay() {
		synchronized (DefaultLegacyService.class) {
			final OverlayHarmonizer ot =
				new OverlayHarmonizer(context.getService(LegacyService.class));
			final Random r = new Random(1234);
			final ImagePlus imagePlus =
				Helper.makeImagePlus("Bar", Helper.makeRandomByteArray(r, 11, 15));
			/*
			 * Put a rectangular hole inside a rectangle - hopefully this is too much and falls into the default code.
			 */
			final Roi roi =
				Helper.makeFreeROI(new int[] { 6, 11, 11, 6, 6 }, new int[] { 9, 9, 15,
					15, 9 });

			imagePlus.setRoi(roi);
			final List<Overlay> list = ot.getOverlays(imagePlus);
			assertEquals(1, list.size());
			assertTrue(list.get(0) instanceof PolygonOverlay);
			final PolygonOverlay overlay = (PolygonOverlay) list.get(0);
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
	}

	@Test
	public void testCreateBinaryMaskROI() {
		synchronized (DefaultLegacyService.class) {
			final Random r = new Random(54321);
			final boolean[][] data = Helper.makeRandomBooleanArray(r, 7, 8);
			final BinaryMaskOverlay<BitType, Img<BitType>> overlay =
				Helper.makeBinaryMaskOverlay(context, 5, 6, data);
			final RealRandomAccess<BitType> ra =
				overlay.getRegionOfInterest().realRandomAccess();
			final OverlayHarmonizer ot =
				new OverlayHarmonizer(context.getService(LegacyService.class));
			final ImagePlus imagePlus =
				Helper.makeImagePlus("Bar", Helper.makeRandomByteArray(r, 15, 20));
			final ArrayList<Overlay> overlays = new ArrayList<Overlay>();
			overlays.add(overlay);
			ot.setOverlays(overlays, overlay, imagePlus);
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
}
