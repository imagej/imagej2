//
// GrayscaleImageTranslatorTest.java
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
import ij.ImagePlus;
import ij.ImageStack;
import ij.gui.NewImage;
import ij.measure.Calibration;
import ij.process.ImageProcessor;
import imagej.ImageJ;
import imagej.data.Dataset;
import imagej.data.DatasetFactory;
import net.imglib2.Cursor;
import net.imglib2.RandomAccess;
import net.imglib2.img.Axes;
import net.imglib2.img.Axis;
import net.imglib2.img.ImgPlus;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.integer.UnsignedByteType;
import net.imglib2.type.numeric.integer.UnsignedShortType;
import net.imglib2.type.numeric.real.FloatType;

import org.junit.BeforeClass;
import org.junit.Test;

// BDZ - FIXME notes: tests commented out. Tests always fail as
// createDisplay() and createLegacyImage() always return null. This is because
// the DisplayService doesn't get setup correctly within the test framework
// and cannot create displays. Need to setup a headless UI and create displays
// within that framework. Then these tests can be run. For now do NO testing.
// Note that the tests are partially translated from using Datasets for
// translatiions to using Displays.

/**
 * Unit tests for {@link GrayscaleImageTranslator}.
 * 
 * @author Barry DeZonia
 */
public class GrayscaleImageTranslatorTest {

	private enum DataType {
		BYTE, SHORT, FLOAT
	}

	private final GrayscaleImageTranslator translator =
		new GrayscaleImageTranslator();

	// -- helper methods --

	private void fill(final Dataset ds) {
		final ImgPlus<? extends RealType<?>> data = ds.getImgPlus();
		final Cursor<? extends RealType<?>> cursor = data.cursor();
		long i = 0;
		while (cursor.hasNext()) {
			cursor.next();
			cursor.get().setReal(i++);
		}
	}

	// -- helper tests --

	private void testDataSame(final Dataset ds, final ImagePlus imp)
	{
		final RandomAccess<? extends RealType<?>> accessor =
				ds.getImgPlus().randomAccess();
		long[] dims = ds.getDims();
		Axis[] axes = ds.getAxes();
		int xIndex = ds.getAxisIndex(Axes.X);
		int yIndex = ds.getAxisIndex(Axes.Y);
		int zIndex = ds.getAxisIndex(Axes.Z);
		int tIndex = ds.getAxisIndex(Axes.TIME);
		int xSize = imp.getWidth();
		int ySize = imp.getHeight();
		int zSize = imp.getNSlices();
		int tSize = imp.getNFrames();
		int cSize = imp.getNChannels();
		ImageStack stack = imp.getStack();
		int planeNum = 1;
		long[] pos = new long[dims.length];
		for (int t = 0; t < tSize; t++) {
			if (tIndex >= 0) pos[tIndex] = t;
			for (int z = 0; z < zSize; z++) {
				if (zIndex >= 0) pos[zIndex] = z;
				for (int c = 0; c < cSize; c++) {
					LegacyUtils.fillChannelIndices(dims, axes, c, pos);
					ImageProcessor proc = stack.getProcessor(planeNum++);
					for (int x = 0; x < xSize; x++) {
						if (xIndex >= 0) pos[xIndex] = x;
						for (int y = 0; y < ySize; y++) {
							if (yIndex >= 0) pos[yIndex] = y;
							accessor.setPosition(pos);
							float dsValue = accessor.get().getRealFloat();
							float impValue = proc.getf(x, y);
							assertEquals(dsValue, impValue, 0.000001f);
						}
					}
				}
			}
		}
	}

	private void testMetadataSame(final Dataset ds, final ImagePlus imp) {
		final Axis[] axesPresent = ds.getAxes();

		// axes
		for (final Axis axis : axesPresent) {
			final int axisIndex = ds.getAxisIndex(axis);
			if (axisIndex >= 0) {
				if (axis == Axes.X) assertEquals(ds.axis(axisIndex), axis);
				if (axis == Axes.Y) assertEquals(ds.axis(axisIndex), axis);
				if (axis == Axes.CHANNEL) assertEquals(ds.axis(axisIndex), axis);
				if (axis == Axes.Z) assertEquals(ds.axis(axisIndex), axis);
				if (axis == Axes.TIME) assertEquals(ds.axis(axisIndex), axis);
			}
		}

		// type
		if (imp.getType() == ImagePlus.GRAY8) assertTrue(ds.getType() instanceof UnsignedByteType);
		if (imp.getType() == ImagePlus.GRAY16) assertTrue(ds.getType() instanceof UnsignedShortType);
		if (imp.getType() == ImagePlus.GRAY32) assertTrue(ds.getType() instanceof FloatType);

		// calibration
		final Calibration cal = imp.getCalibration();
		int axisIndex;
		axisIndex = ds.getAxisIndex(Axes.X);
		if (axisIndex >= 0) assertEquals(ds.calibration(axisIndex), cal.pixelWidth,
			0);
		axisIndex = ds.getAxisIndex(Axes.Y);
		if (axisIndex >= 0) assertEquals(ds.calibration(axisIndex),
			cal.pixelHeight, 0);
		axisIndex = ds.getAxisIndex(Axes.CHANNEL);
		if (axisIndex >= 0) assertEquals(ds.calibration(axisIndex), 1, 0);
		axisIndex = ds.getAxisIndex(Axes.Z);
		if (axisIndex >= 0) assertEquals(ds.calibration(axisIndex), cal.pixelDepth,
			0);
		axisIndex = ds.getAxisIndex(Axes.TIME);
		if (axisIndex >= 0) assertEquals(ds.calibration(axisIndex),
			cal.frameInterval, 0);

		// name
		assertEquals(ds.getName(), imp.getTitle());

		// integer
		assertEquals(!ds.isInteger(), (imp.getType() == ImagePlus.GRAY32));

		// color
		assertFalse(ds.isRGBMerged());

		// signed data flag
		assertEquals(ds.isSigned(), (imp.getType() == ImagePlus.GRAY32));

	}

	private void testImageFromIJ1(final DataType type, final int x, final int y,
		final int c, final int z, final int t)
	{

		ImagePlus imp;
		switch (type) {
			case BYTE:
				imp =
					NewImage.createByteImage("byte image", x, y, c * z * t,
						NewImage.FILL_RAMP);
				break;
			case SHORT:
				imp =
					NewImage.createShortImage("short image", x, y, c * z * t,
						NewImage.FILL_RAMP);
				break;
			case FLOAT:
				imp =
					NewImage.createFloatImage("float image", x, y, c * z * t,
						NewImage.FILL_RAMP);
				break;
			default:
				throw new IllegalStateException();
		}
		imp.setDimensions(c, z, t);
		final Calibration cal = new Calibration();
		cal.pixelHeight = 3;
		cal.pixelDepth = 4;
		cal.pixelWidth = 7;
		imp.setCalibration(cal);
		// CTR FIXME - Fix comparison tests.
		//ImageDisplay disp = translator.createDisplay(imp);
		//Dataset ds = ImageJ.get(ImageDisplayService.class).getActiveDataset(disp);
		//testDataSame(ds, imp);
		//testMetadataSame(ds, imp);
	}

	private void testImageFromIJ2(final DataType type, final int x, final int y,
		final int c, final int z, final int t)
	{

		final Axes[] axes =
			new Axes[] { Axes.X, Axes.Y, Axes.CHANNEL, Axes.Z, Axes.TIME };

		Dataset ds;
		switch (type) {
			case BYTE:
				ds =
					DatasetFactory.create(new long[] { x, y, c, z, t }, "byte image",
						axes, 8, false, false);
				break;
			case SHORT:
				ds =
					DatasetFactory.create(new long[] { x, y, c, z, t }, "short image",
						axes, 16, false, false);
				break;
			case FLOAT:
				ds =
					DatasetFactory.create(new long[] { x, y, c, z, t }, "float image",
						axes, 32, true, true);
				break;
			default:
				throw new IllegalStateException();
		}
		fill(ds);
		ds.setCalibration(3, 0);
		ds.setCalibration(7, 1);
		ds.setCalibration(1, 2);
		ds.setCalibration(9, 3);
		ds.setCalibration(1, 4);
		// CTR FIXME - Fix comparison tests.
//		ImagePlus imp = translator.createLegacyImage(ds);
//		testDataSame(ds, imp, x, y, c, z, t);
//		testMetadataSame(ds, imp);
	}

	// -- setup code --

	// BDZ - FIXME - partial attempt to fix initialization of
	// DisplayService. See FIXME discussion above.
	
	//@BeforeClass
	//public static void setup() {
	//	final ImageJ context = ImageJ.createContext();
	//	//context.getService(UIService.class).processArgs(args);
	//}
	
	// -- public tests --

	@Test
	public void testFromIJ1() {
		int x, y, c, z, t;

		x = 25;
		y = 35;
		c = 1;
		z = 1;
		t = 1;
		testImageFromIJ1(DataType.BYTE, x, y, c, z, t);

		x = 95;
		y = 22;
		c = 3;
		z = 5;
		t = 7;
		testImageFromIJ1(DataType.BYTE, x, y, c, z, t);

		x = 80;
		y = 91;
		c = 1;
		z = 1;
		t = 1;
		testImageFromIJ1(DataType.SHORT, x, y, c, z, t);

		x = 80;
		y = 48;
		c = 5;
		z = 7;
		t = 3;
		testImageFromIJ1(DataType.SHORT, x, y, c, z, t);

		x = 107;
		y = 185;
		c = 1;
		z = 1;
		t = 1;
		testImageFromIJ1(DataType.FLOAT, x, y, c, z, t);

		x = 83;
		y = 56;
		c = 7;
		z = 3;
		t = 5;
		testImageFromIJ1(DataType.FLOAT, x, y, c, z, t);
	}

	@Test
	public void testToImageJ1() {
		int x, y, c, z, t;

		x = 25;
		y = 35;
		c = 1;
		z = 1;
		t = 1;
		testImageFromIJ2(DataType.BYTE, x, y, c, z, t);

		x = 95;
		y = 22;
		c = 3;
		z = 5;
		t = 7;
		testImageFromIJ2(DataType.BYTE, x, y, c, z, t);

		x = 80;
		y = 91;
		c = 1;
		z = 1;
		t = 1;
		testImageFromIJ2(DataType.SHORT, x, y, c, z, t);

		x = 80;
		y = 48;
		c = 5;
		z = 7;
		t = 3;
		testImageFromIJ2(DataType.SHORT, x, y, c, z, t);

		x = 107;
		y = 185;
		c = 1;
		z = 1;
		t = 1;
		testImageFromIJ2(DataType.FLOAT, x, y, c, z, t);

		x = 83;
		y = 56;
		c = 7;
		z = 3;
		t = 5;
		testImageFromIJ2(DataType.FLOAT, x, y, c, z, t);
	}

	private void testOrdering(final Axis[] axes) {
		// System.out.println("Testing order : "+axes[0]+","+axes[1]+","+axes[2]);
		int nullAxes = 0;
		for (final Axis axis : axes)
			if (axis == null) nullAxes++;
		final Axis[] fullAxes = new Axis[2 + axes.length - nullAxes];
		fullAxes[0] = Axes.X;
		fullAxes[1] = Axes.Y;
		int axisIndex = 2;
		for (int i = 0; i < axes.length; i++)
			if (axes[i] != null) fullAxes[axisIndex++] = axes[i];
		final long[] dims = new long[fullAxes.length];
		dims[0] = 3;
		dims[1] = 1;
		for (int i = 2; i < dims.length; i++)
			dims[i] = 5 + i * 2;
		final Dataset ds =
			DatasetFactory.create(new UnsignedByteType(), dims, "temp", fullAxes);
		final int cIndex = ds.getAxisIndex(Axes.CHANNEL);
		final int zIndex = ds.getAxisIndex(Axes.Z);
		final int tIndex = ds.getAxisIndex(Axes.TIME);
		final long cCount = (cIndex < 0) ? 1 : dims[cIndex];
		final long zCount = (zIndex < 0) ? 1 : dims[zIndex];
		final long tCount = (tIndex < 0) ? 1 : dims[tIndex];
		final long[] position = new long[dims.length];
		final RandomAccess<? extends RealType<?>> accessor =
			ds.getImgPlus().randomAccess();
		for (int t = 0; t < tCount; t++) {
			if (tIndex >= 0) position[tIndex] = t;
			for (int z = 0; z < zCount; z++) {
				if (zIndex >= 0) position[zIndex] = z;
				for (int c = 0; c < cCount; c++) {
					if (cIndex >= 0) position[cIndex] = c;
					position[1] = 0;
					position[0] = 0;
					accessor.setPosition(position);
					accessor.get().setReal(c);
					position[0] = 1;
					accessor.setPosition(position);
					accessor.get().setReal(z);
					position[0] = 2;
					accessor.setPosition(position);
					accessor.get().setReal(t);
				}
			}
		}
		// CTR FIXME - Fix comparison tests.
//		ImagePlus imp = translator.createLegacyImage(ds);
//		for (int t = 0; t < tCount; t++) {
//			if (tIndex >= 0) position[tIndex] = t;
//			for (int z = 0; z < zCount; z++) {
//				if (zIndex >= 0) position[zIndex] = z;
//				for (int c = 0; c < cCount; c++) {
//					if (cIndex >= 0) position[cIndex] = c;
//					imp.setPositionWithoutUpdate(c+1, z+1, t+1);
//					ImageProcessor proc = imp.getProcessor();
//					position[1] = 0;
//					position[0] = 0;
//					accessor.setPosition(position);
//					assertEquals(accessor.get().getRealDouble(), proc.get(0,0), 0);
//					position[0] = 1;
//					accessor.setPosition(position);
//					assertEquals(accessor.get().getRealDouble(), proc.get(1,0), 0);
//					position[0] = 2;
//					accessor.setPosition(position);
//					assertEquals(accessor.get().getRealDouble(), proc.get(2,0), 0);
//				}
//			}
//		}
	}

	@Test
	public void testAxisOrderingIJ2DatasetToImageJ1() {
		final Axis[] axes = new Axis[] { null, Axes.CHANNEL, Axes.Z, Axes.TIME };
		for (final Axis outer : axes) {
			for (final Axis middle : axes) {
				for (final Axis inner : axes) {
					if (LegacyTestUtils.allNull(new Axis[] { outer, middle, inner })) continue;
					if (LegacyTestUtils.repeated(new Axis[] { outer, middle, inner })) continue;
					testOrdering(new Axis[] { outer, middle, inner });
				}
			}
		}
	}

}
