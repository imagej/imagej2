//
// RGBImageTranslatorTest.java
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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import ij.ImagePlus;
import ij.ImageStack;
import ij.gui.NewImage;
import ij.measure.Calibration;
import ij.process.ColorProcessor;
import imagej.data.Dataset;
import net.imglib2.Cursor;
import net.imglib2.RandomAccess;
import net.imglib2.img.Axes;
import net.imglib2.img.ImgPlus;
import net.imglib2.type.numeric.RealType;

import org.junit.Test;

/**
 * Unit tests for {@link RGBImageTranslator}.
 * 
 * @author Barry DeZonia
 */
public class RGBImageTranslatorTest {

	private final RGBImageTranslator translator = new RGBImageTranslator();

	// -- helper methods --

	private void fill(final Dataset ds) {
		final ImgPlus<? extends RealType<?>> data = ds.getImgPlus();
		final Cursor<? extends RealType<?>> cursor = data.cursor();
		final long i = 0;
		while (cursor.hasNext()) {
			cursor.next();
			final int r = (int) (i % 256);
			final int g = (int) ((i + 23) % 256);
			final int b = (int) ((i + 66) % 256);
			final int pixValue = 0xff000000 | (r << 16) | (g << 8) | b;
			cursor.get().setReal(pixValue);
		}
	}

	// -- helper tests --

	private void testDataSame(final Dataset ds, final ImagePlus imp,
		final int x, final int y, int z, int t)
	{
		final long[] dims = ds.getDims();

		final int xIndex = ds.getAxisIndex(Axes.X);
		final int yIndex = ds.getAxisIndex(Axes.Y);
		final int cIndex = ds.getAxisIndex(Axes.CHANNEL);
		final int zIndex = ds.getAxisIndex(Axes.Z);
		final int tIndex = ds.getAxisIndex(Axes.TIME);

		int ijCompatAxesPresent = 0;
		if (xIndex >= 0) ijCompatAxesPresent++;
		if (yIndex >= 0) ijCompatAxesPresent++;
		if (cIndex >= 0) ijCompatAxesPresent++;
		if (zIndex >= 0) ijCompatAxesPresent++;
		if (tIndex >= 0) ijCompatAxesPresent++;

		if (z <= 0) z = 1;
		if (t <= 0) t = 1;

		// test Dataset expectations

		assertEquals(ijCompatAxesPresent, dims.length);
		assertEquals(x, dims[xIndex]);
		assertEquals(y, dims[yIndex]);
		assertEquals(3, dims[cIndex]);
		if (zIndex >= 0) assertEquals(z, dims[zIndex]);
		if (tIndex >= 0) assertEquals(t, dims[tIndex]);

		// test ImagePlus expectations

		assertTrue(imp.getProcessor() instanceof ColorProcessor);
		assertEquals(imp.getWidth(), x);
		assertEquals(imp.getHeight(), y);
		assertEquals(imp.getNChannels(), 1);
		assertEquals(imp.getNSlices(), z);
		assertEquals(imp.getNFrames(), t);

		// compare internal data
		final RandomAccess<? extends RealType<?>> accessor =
			ds.getImgPlus().randomAccess();
		final ImageStack stack = imp.getStack();
		final long[] position = new long[5];
		int procNum = 1;
		for (int ti = 0; ti < t; ti++) {
			for (int zi = 0; zi < z; zi++) {
				final ColorProcessor proc =
					(ColorProcessor) stack.getProcessor(procNum++);
				for (int yi = 0; yi < y; yi++) {
					for (int xi = 0; xi < x; xi++) {
						position[xIndex] = xi;
						position[yIndex] = yi;
						if (zIndex >= 0) position[zIndex] = zi;
						if (tIndex >= 0) position[tIndex] = ti;

						position[cIndex] = 0;
						accessor.setPosition(position);
						final int rValue = (int) accessor.get().getRealDouble();

						position[cIndex] = 1;
						accessor.setPosition(position);
						final int gValue = (int) accessor.get().getRealDouble();

						position[cIndex] = 2;
						accessor.setPosition(position);
						final int bValue = (int) accessor.get().getRealDouble();

						final int pixValue =
							0xff000000 | (rValue << 16) | (gValue << 8) | bValue;

						assertEquals(proc.get(xi, yi), pixValue);
					}
				}
			}
		}
	}

	private void testMetadataSame(final Dataset ds, final ImagePlus imp) {
		// axes
		final int xIndex = ds.getAxisIndex(Axes.X);
		final int yIndex = ds.getAxisIndex(Axes.Y);
		final int cIndex = ds.getAxisIndex(Axes.CHANNEL);
		final int zIndex = ds.getAxisIndex(Axes.Z);
		final int tIndex = ds.getAxisIndex(Axes.TIME);
		assertEquals(ds.axis(0), Axes.X);
		assertEquals(ds.axis(1), Axes.Y);
		assertEquals(ds.axis(2), Axes.CHANNEL);
		if (zIndex >= 0) assertEquals(ds.axis(3), Axes.Z);
		if (tIndex >= 0) assertEquals(ds.axis(ds.getAxes().length - 1), Axes.TIME);

		// type
		assertTrue(ds.isRGBMerged());
		assertTrue(imp.getType() == ImagePlus.COLOR_RGB);

		// calibration
		final Calibration cal = imp.getCalibration();
		assertEquals(ds.calibration(xIndex), cal.pixelWidth, 0);
		assertEquals(ds.calibration(yIndex), cal.pixelHeight, 0);
		assertEquals(ds.calibration(cIndex), 1, 0);
		if (zIndex >= 0) assertEquals(ds.calibration(zIndex), cal.pixelDepth, 0);
		if (tIndex >= 0) assertEquals(ds.calibration(tIndex), cal.frameInterval, 0);

		// name
		assertEquals(ds.getName(), imp.getTitle());

		// integer
		assertTrue(ds.isInteger());

		// signed data flag
		assertFalse(ds.isSigned());
	}

	private void testImageFromIJ1(final int x, final int y, int z, int t) {
		if (z <= 0) z = 1;
		if (t <= 0) t = 1;
		final ImagePlus imp =
			NewImage.createRGBImage("rgb image", x, y, z * t, NewImage.FILL_RAMP);
		imp.setDimensions(1, z, t);
		final Calibration cal = new Calibration();
		cal.pixelWidth = 7;
		cal.pixelHeight = 3;
		cal.pixelDepth = 4;
		imp.setCalibration(cal);
		// CTR FIXME - Fix comparison tests.
//		Dataset ds = translator.createDataset(imp);
//		testDataSame(ds, imp, x, y, z, t);
//		testMetadataSame(ds, imp);
	}

	private void testImageToIJ1(final int x, final int y, final int z,
		final int t)
	{
		int totalDims = 3;
		if (z > 0) totalDims++;
		if (t > 0) totalDims++;

		final Axes[] axes = new Axes[totalDims];
		axes[0] = Axes.X;
		axes[1] = Axes.Y;
		axes[2] = Axes.CHANNEL;
		if (z > 0) axes[3] = Axes.Z;
		if (t > 0) axes[axes.length - 1] = Axes.TIME;

		final long[] dims = new long[totalDims];
		dims[0] = x;
		dims[1] = y;
		dims[2] = 3;
		if (z > 0) dims[3] = z;
		if (t > 0) dims[dims.length - 1] = t;

		final Dataset ds =
			Dataset.create(dims, "color image", axes, 8, false, false);
		ds.setRGBMerged(true);
		fill(ds);
		ds.setCalibration(3, 0);
		ds.setCalibration(7, 1);
		ds.setCalibration(1, 2);
		if (totalDims > 3) ds.setCalibration(9, 3);
		if (totalDims > 4) ds.setCalibration(1, 4);
		// CTR FIXME - Fix comparison tests.
//		ImagePlus imp = translator.createLegacyImage(ds);
//		testDataSame(ds, imp, x, y, z, t);
//		testMetadataSame(ds, imp);
	}

	// -- public tests --

	@Test
	public void testFromIJ1() {
		int x, y, z, t;

		x = 25;
		y = 35;
		z = -1;
		t = -1;
		testImageFromIJ1(x, y, z, t);

		x = 95;
		y = 22;
		z = 5;
		t = -1;
		testImageFromIJ1(x, y, z, t);

		x = 80;
		y = 91;
		z = -1;
		t = 3;
		testImageFromIJ1(x, y, z, t);

		x = 80;
		y = 48;
		z = 4;
		t = 2;
		testImageFromIJ1(x, y, z, t);
	}

	@Test
	public void testToIJ1() {
		int x, y, z, t;

		x = 25;
		y = 35;
		z = -1;
		t = -1;
		testImageToIJ1(x, y, z, t);

		x = 95;
		y = 22;
		z = 5;
		t = -1;
		testImageToIJ1(x, y, z, t);

		x = 80;
		y = 91;
		z = -1;
		t = 3;
		testImageToIJ1(x, y, z, t);

		x = 80;
		y = 48;
		z = 4;
		t = 2;
		testImageToIJ1(x, y, z, t);
	}

	@Test
	public void testProblemImagePluses() {
		if (true) return; // CTR FIXME TEMP

		ImagePlus imp;

		// channels not == 1
		imp = NewImage.createRGBImage("rgb image", 10, 10, 8, NewImage.FILL_RAMP);
		imp.setDimensions(2, 4, 1); // c == 2, z == 4
		try {
			// CTR FIXME - Fix tests.
//			translator.createDataset(imp);
			fail();
		}
		catch (final IllegalArgumentException e) {
			assertTrue(true);
		}

		// type not == RGB
		imp =
			NewImage.createByteImage("byte image", 10, 10, 3, NewImage.FILL_RAMP);
		imp.setDimensions(3, 1, 1); // c == 3
		try {
			// CTR FIXME - Fix tests.
//			translator.createDataset(imp);
			fail();
		}
		catch (final IllegalArgumentException e) {
			assertTrue(true);
		}

	}

	@Test
	public void testProblemDatasets() {
		if (true) return; // CTR FIXME TEMP

		// make sure exceptions thrown when needed

		Dataset ds;

		// to IJ1 : isRgbMerged not true
		ds =
			Dataset.create(new long[] { 1, 2, 3, 4, 5 }, "zoompa", new Axes[] {
				Axes.X, Axes.Y, Axes.CHANNEL, Axes.Z, Axes.TIME }, 8, false, false);
		try {
			// CTR FIXME - Fix tests.
//			translator.createLegacyImage(ds);
			fail();
		}
		catch (final IllegalArgumentException e) {
			assertTrue(true);
		}

		// to IJ1 : not unsigned byte
		ds =
			Dataset.create(new long[] { 1, 2, 3, 4, 5 }, "zoompa", new Axes[] {
				Axes.X, Axes.Y, Axes.CHANNEL, Axes.Z, Axes.TIME }, 16, false, false);
		ds.setRGBMerged(true);
		try {
			// CTR FIXME - Fix tests.
//			translator.createLegacyImage(ds);
			fail();
		}
		catch (final IllegalArgumentException e) {
			assertTrue(true);
		}

		// to IJ1 : X Axis not present
		ds =
			Dataset.create(new long[] { 2, 3, 4, 5 }, "zoompa", new Axes[] { Axes.Y,
				Axes.CHANNEL, Axes.Z, Axes.TIME }, 8, false, false);
		ds.setRGBMerged(true);
		try {
			// CTR FIXME - Fix tests.
//			translator.createLegacyImage(ds);
			fail();
		}
		catch (final IllegalArgumentException e) {
			assertTrue(true);
		}

		// to IJ1 : Y Axis not present
		ds =
			Dataset.create(new long[] { 2, 3, 4, 5 }, "zoompa", new Axes[] { Axes.X,
				Axes.CHANNEL, Axes.Z, Axes.TIME }, 8, false, false);
		ds.setRGBMerged(true);
		try {
			// CTR FIXME - Fix tests.
//			translator.createLegacyImage(ds);
			fail();
		}
		catch (final IllegalArgumentException e) {
			assertTrue(true);
		}

		// to IJ1 : C Axis not present
		ds =
			Dataset.create(new long[] { 1, 2, 4, 5 }, "zoompa", new Axes[] { Axes.X,
				Axes.Y, Axes.Z, Axes.TIME }, 8, false, false);
		ds.setRGBMerged(true);
		try {
			// CTR FIXME - Fix tests.
//			translator.createLegacyImage(ds);
			fail();
		}
		catch (final IllegalArgumentException e) {
			assertTrue(true);
		}

		// to IJ1 : non XYCZT index present
		ds =
			Dataset.create(new long[] { 1, 2, 3, 4, 5 }, "zoompa", new Axes[] {
				Axes.X, Axes.Y, Axes.CHANNEL, Axes.Z, Axes.FREQUENCY }, 8, false,
				false);
		ds.setRGBMerged(true);
		try {
			// CTR FIXME - Fix tests.
//			translator.createLegacyImage(ds);
			fail();
		}
		catch (final IllegalArgumentException e) {
			assertTrue(true);
		}

		// channels != 3
		ds =
			Dataset.create(new long[] { 1, 2, 97, 4, 5 }, "zoompa", new Axes[] {
				Axes.X, Axes.Y, Axes.CHANNEL, Axes.Z, Axes.TIME }, 8, false, false);
		ds.setRGBMerged(true);
		try {
			// CTR FIXME - Fix tests.
//			translator.createLegacyImage(ds);
			fail();
		}
		catch (final IllegalArgumentException e) {
			assertTrue(true);
		}

		// UNTESTED:

		// any dim > Integer.MAX_VALUE
		// takes too much memory to test - skip for now

		// combo of c*z*t > Integer.MAX_VALUE
		// takes too much memory to test - skip for now
	}

}
