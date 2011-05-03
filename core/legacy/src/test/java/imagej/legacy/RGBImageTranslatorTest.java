package imagej.legacy;

import static org.junit.Assert.*;
import ij.ImagePlus;
import ij.ImageStack;
import ij.gui.NewImage;
import ij.measure.Calibration;
import ij.process.ColorProcessor;
import imagej.data.Dataset;

import net.imglib2.Cursor;
import net.imglib2.img.Axes;
import net.imglib2.img.ImgPlus;
import net.imglib2.type.numeric.RealType;

import org.junit.Test;

public class RGBImageTranslatorTest {

	private RGBImageTranslator translator = new RGBImageTranslator();

	// -- helper methods --
	
	private void fill(Dataset ds) {
		ImgPlus<? extends RealType<?>> data = ds.getImgPlus();
		Cursor<? extends RealType<?>> cursor = data.cursor();
		long i = 0;
		while (cursor.hasNext()) {
			cursor.next();
			int r = (int)(i % 256);
			int g = (int)((i+23) % 256);
			int b = (int)((i+66) % 256);
			int pixValue = 0xff000000 | (r << 16) | (g << 8) | b;
			cursor.get().setReal(pixValue);
		}
	}
	
	// -- helper tests --
	
	private void testDataSame(Dataset ds, ImagePlus imp, int x, int y, int z, int t) {
		long[] dims = ds.getDims();

		int xIndex = ds.getAxisIndex(Axes.X);
		int yIndex = ds.getAxisIndex(Axes.Y);
		int cIndex = ds.getAxisIndex(Axes.CHANNEL);
		int zIndex = ds.getAxisIndex(Axes.Z);
		int tIndex = ds.getAxisIndex(Axes.TIME);

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
		if (zIndex >= 0)
			assertEquals(z, dims[zIndex]);
		if (tIndex >= 0)
			assertEquals(t, dims[tIndex]);

		// test ImagePlus expectations

		assertTrue(imp.getProcessor() instanceof ColorProcessor);
		assertEquals(imp.getWidth(), x);
		assertEquals(imp.getHeight(), y);
		assertEquals(imp.getNChannels(), 1);
		assertEquals(imp.getNSlices(), z);
		assertEquals(imp.getNFrames(), t);
		
		// compare internal data
		
		final ImageStack stack = imp.getStack();
		long[] position = new long[5];
		int procNum = 1;
		for (int ti = 0; ti < t; ti++) {
			for (int zi = 0; zi < z; zi++) {
				ColorProcessor proc = (ColorProcessor) stack.getProcessor(procNum++);
				for (int yi = 0; yi < y; yi++) {
					for (int xi = 0; xi < x; xi++) {
						position[xIndex] = xi;
						position[yIndex] = yi;
						if (zIndex >= 0) position[zIndex] = zi;
						if (tIndex >= 0) position[tIndex] = ti;
						
						position[cIndex] = 0;
						int rValue = (int) ds.getDoubleValue(position);
						
						position[cIndex] = 1;
						int gValue = (int) ds.getDoubleValue(position);
						
						position[cIndex] = 2;
						int bValue = (int) ds.getDoubleValue(position);
						
						int pixValue = 0xff000000 | (rValue<<16) | (gValue<<8) | bValue;
						
						assertEquals(proc.get(xi, yi), pixValue);
					}
				}
			}
		}
	}

	private void testMetadataSame(Dataset ds, ImagePlus imp) {
		// axes
		int xIndex = ds.getAxisIndex(Axes.X);
		int yIndex = ds.getAxisIndex(Axes.Y);
		int zIndex = ds.getAxisIndex(Axes.Z);
		int tIndex = ds.getAxisIndex(Axes.TIME);
		assertEquals(ds.axis(0), Axes.X);
		assertEquals(ds.axis(1), Axes.Y);
		assertEquals(ds.axis(2), Axes.CHANNEL);
		if (zIndex >= 0)
			assertEquals(ds.axis(3), Axes.Z);
		if (tIndex >= 0)
			assertEquals(ds.axis(ds.getAxes().length-1), Axes.TIME);
		
		// type
		assertTrue(ds.isRGBMerged());
		assertTrue(imp.getType() == ImagePlus.COLOR_RGB);

		// calibration
		Calibration cal = imp.getCalibration();
		assertEquals(ds.calibration(xIndex), cal.pixelWidth, 0);
		assertEquals(ds.calibration(yIndex), cal.pixelHeight, 0);
		if (zIndex >= 0)
			assertEquals(ds.calibration(zIndex), cal.pixelDepth, 0);
		
		// name
		assertEquals(ds.getName(), imp.getTitle());
		
		// integer
		assertTrue(ds.isInteger());
		
		// signed data flag
		assertFalse(ds.isSigned());
	}
	
	private void testImageFromIJ1(int x, int y, int z, int t) {
		if (z <= 0) z = 1;
		if (t <= 0) t = 1;
		ImagePlus imp = NewImage.createRGBImage("rgb image", x, y, z*t, NewImage.FILL_RAMP);
		imp.setDimensions(1, z, t);
		Calibration cal = new Calibration();
		cal.pixelWidth = 7;
		cal.pixelHeight = 3;
		cal.pixelDepth = 4;
		imp.setCalibration(cal);
		Dataset ds = translator.createDataset(imp);
		testDataSame(ds, imp, x, y, z, t);
		testMetadataSame(ds, imp);
	}

	private void testImageToIJ1(int x, int y, int z, int t) {
		int totalDims = 3;
		if (z > 0) totalDims++;
		if (t > 0) totalDims++;
		
		Axes[] axes = new Axes[totalDims];
		axes[0] = Axes.X;
		axes[1] = Axes.Y;
		axes[2] = Axes.CHANNEL;
		if (z > 0)
			axes[3] = Axes.Z;
		if (t > 0)
			axes[axes.length-1] = Axes.TIME;

		long[] dims = new long[totalDims];
		dims[0] = x;
		dims[1] = y;
		dims[2] = 3;
		if (z > 0)
			dims[3] = z;
		if (t > 0)
			dims[dims.length-1] = t;
		
		Dataset ds = Dataset.create(dims, "color image", axes, 8, false, false);
		ds.setRGBMerged(true);
		fill(ds);
		ds.setCalibration(3, 0);
		ds.setCalibration(7, 1);
		ds.setCalibration(1, 2);
		if (totalDims > 3)
			ds.setCalibration(9, 3);
		if (totalDims > 4)
			ds.setCalibration(1, 4);
		ImagePlus imp = translator.createLegacyImage(ds);
		testDataSame(ds, imp, x, y, z, t);
		testMetadataSame(ds, imp);
	}

	// -- public tests --
	
	@Test
	public void testFromIJ1() {
		int x,y,z,t;
		
		x = 25; y = 35; z = -1; t = -1; 
		testImageFromIJ1(x, y, z, t);

		x = 95; y = 22; z = 5; t = -1; 
		testImageFromIJ1(x, y, z, t);

		x = 80; y = 91; z = -1; t = 3; 
		testImageFromIJ1(x, y, z, t);

		x = 80; y = 48; z = 4; t = 2; 
		testImageFromIJ1(x, y, z, t);
	}

	@Test
	public void testToIJ1() {
		int x,y,z,t;
		
		x = 25; y = 35; z = -1; t = -1; 
		testImageToIJ1(x, y, z, t);

		x = 95; y = 22; z = 5; t = -1; 
		testImageToIJ1(x, y, z, t);

		x = 80; y = 91; z = -1; t = 3; 
		testImageToIJ1(x, y, z, t);

		x = 80; y = 48; z = 4; t = 2; 
		testImageToIJ1(x, y, z, t);
	}
	
	@Test
	public void testProblemImagePluses() {
		// channels not == 1
		// type not == RGB
	}
	
	@Test
	public void testProblemDatasets() {
		// make sure exceptions thrown when needed
		
		Dataset ds;

		// to IJ1 : isRgbMerged not true
		ds = Dataset.create(new long[]{1,2,3,4,5}, "zoompa", new Axes[]{Axes.X,Axes.Y,Axes.CHANNEL,Axes.Z,Axes.TIME}, 8, false, false);
		try {
			translator.createLegacyImage(ds);
			fail();
		} catch (IllegalArgumentException e) {
			assertTrue(true);
		}
		
		// to IJ1 : not unsigned byte
		ds = Dataset.create(new long[]{1,2,3,4,5}, "zoompa", new Axes[]{Axes.X,Axes.Y,Axes.CHANNEL,Axes.Z,Axes.TIME}, 16, false, false);
		ds.setRGBMerged(true);
		try {
			translator.createLegacyImage(ds);
			fail();
		} catch (IllegalArgumentException e) {
			assertTrue(true);
		}

		// to IJ1 : X Axis not present
		ds = Dataset.create(new long[]{2,3,4,5}, "zoompa", new Axes[]{Axes.Y,Axes.CHANNEL,Axes.Z,Axes.TIME}, 8, false, false);
		ds.setRGBMerged(true);
		try {
			translator.createLegacyImage(ds);
			fail();
		} catch (IllegalArgumentException e) {
			assertTrue(true);
		}

		// to IJ1 : Y Axis not present
		ds = Dataset.create(new long[]{2,3,4,5}, "zoompa", new Axes[]{Axes.X,Axes.CHANNEL,Axes.Z,Axes.TIME}, 8, false, false);
		ds.setRGBMerged(true);
		try {
			translator.createLegacyImage(ds);
			fail();
		} catch (IllegalArgumentException e) {
			assertTrue(true);
		}

		// to IJ1 : C Axis not present
		ds = Dataset.create(new long[]{1,2,4,5}, "zoompa", new Axes[]{Axes.X,Axes.Y,Axes.Z,Axes.TIME}, 8, false, false);
		ds.setRGBMerged(true);
		try {
			translator.createLegacyImage(ds);
			fail();
		} catch (IllegalArgumentException e) {
			assertTrue(true);
		}

		// to IJ1 : C Axis not present
		ds = Dataset.create(new long[]{2,3,4,5}, "zoompa", new Axes[]{Axes.X,Axes.Y,Axes.Z,Axes.TIME}, 8, false, false);
		ds.setRGBMerged(true);
		try {
			translator.createLegacyImage(ds);
			fail();
		} catch (IllegalArgumentException e) {
			assertTrue(true);
		}

		// to IJ1 : non XYCZT index present
		ds = Dataset.create(new long[]{1,2,3,4,5}, "zoompa", new Axes[]{Axes.X,Axes.Y,Axes.Z,Axes.FREQUENCY}, 8, false, false);
		ds.setRGBMerged(true);
		try {
			translator.createLegacyImage(ds);
			fail();
		} catch (IllegalArgumentException e) {
			assertTrue(true);
		}

		// channels != 3
		ds = Dataset.create(new long[]{1,2,97,4,5}, "zoompa", new Axes[]{Axes.X,Axes.Y,Axes.CHANNEL,Axes.Z,Axes.TIME}, 8, false, false);
		ds.setRGBMerged(true);
		try {
			translator.createLegacyImage(ds);
			fail();
		} catch (IllegalArgumentException e) {
			assertTrue(true);
		}

		// UNTESTED:
		
		// any dim > Integer.MAX_VALUE
		//   takes too much memory to test - skip for now
		
		// combo of c*z*t > Integer.MAX_VALUE
		//   takes too much memory to test - skip for now
	}
}
