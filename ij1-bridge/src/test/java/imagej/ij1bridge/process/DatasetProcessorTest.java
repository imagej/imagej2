package imagej.ij1bridge.process;

import static org.junit.Assert.*;

import java.awt.Color;

import ij.process.ByteProcessor;
import ij.process.ImageProcessor;
import ij.process.ImageStatistics;
import imagej.dataset.Dataset;
import imagej.dataset.DatasetFactory;
import imagej.dataset.PlanarDatasetFactory;
import imagej.dataset.PrimitiveDatasetCreator;

import org.junit.Test;

public class DatasetProcessorTest
{
	private Dataset ds;
	private DatasetProcessor proc;
	
	private void setupData(int[] dimensions, boolean unsigned, Object arrayOfData)
	{
		DatasetFactory factory = new PlanarDatasetFactory();
		PrimitiveDatasetCreator creator = new PrimitiveDatasetCreator(factory);
		ds = creator.createDataset(dimensions, unsigned, arrayOfData);
		proc = new DatasetProcessor(ds);
	}
	@Test
	public void testSetColorColor() {
		setupData(new int[]{1,5}, false, new int[]{-3,14,77,-18,12});
		proc.setColor(Color.GRAY);
		proc.drawDot(0, 2);
		assertEquals(30, proc.getl(0, 2));
	}

	@Test
	public void testSetValue() {
		setupData(new int[]{1,5}, false, new short[]{88,1024,-14000,105,-88});
		proc.setValue(42);
		proc.fill();
		assertEquals(42, proc.get(0,0));
	}

	@Test
	public void testSetBackgroundValue() {
		setupData(new int[]{1,5}, false, new short[]{88,1024,-14000,105,-88});
		proc.setBackgroundValue(42);
		// nothing to test - DatasetProcessor does not change any state via this call
	}

	@Test
	public void testGetBackgroundValue() {
		setupData(new int[]{1,5}, false, new short[]{88,1024,-14000,105,-88});
		assertEquals(0, proc.getBackgroundValue(), 0);
		proc.setBackgroundValue(42);
		// make sure its unchanged
		assertEquals(0, proc.getBackgroundValue(), 0);
	}

	@Test
	public void testGetMin() {
		setupData(new int[]{1,5}, false, new int[]{-3,14,77,-18,12});
		assertEquals(-18, proc.getMin(), 0);
	}

	@Test
	public void testGetMax() {
		setupData(new int[]{1,5}, false, new int[]{-3,14,77,-18,12});
		assertEquals(77, proc.getMax(), 0);
	}

	@Test
	public void testSetMinAndMax() {
		setupData(new int[]{1,5}, false, new int[]{-3,14,77,-18,12});
		proc.setMinAndMax(-14, 68);
		assertEquals(-14, proc.getMin(), 0);
		assertEquals(68, proc.getMax(), 0);
	}

	@Test
	public void testResetMinAndMax() {
		setupData(new int[]{1,5}, false, new int[]{-3,14,77,-18,12});
		proc.setMinAndMax(-14, 68);
		assertEquals(-14, proc.getMin(), 0);
		assertEquals(68, proc.getMax(), 0);
		proc.resetMinAndMax();
		assertEquals(-18, proc.getMin(), 0);
		assertEquals(77, proc.getMax(), 0);
	}

	@Test
	public void testFlipVertical() {
		// even row count
		setupData(new int[]{2,2}, true, new byte[]{0,1,2,3});
		assertEquals(0, ds.getLong(new int[]{0,0}));
		assertEquals(1, ds.getLong(new int[]{1,0}));
		assertEquals(2, ds.getLong(new int[]{0,1}));
		assertEquals(3, ds.getLong(new int[]{1,1}));
		proc.flipVertical();
		assertEquals(2, ds.getLong(new int[]{0,0}));
		assertEquals(3, ds.getLong(new int[]{1,0}));
		assertEquals(0, ds.getLong(new int[]{0,1}));
		assertEquals(1, ds.getLong(new int[]{1,1}));

		// odd row count
		setupData(new int[]{2,3}, true, new byte[]{0,1,2,3,4,5});
		assertEquals(0, ds.getLong(new int[]{0,0}));
		assertEquals(1, ds.getLong(new int[]{1,0}));
		assertEquals(2, ds.getLong(new int[]{0,1}));
		assertEquals(3, ds.getLong(new int[]{1,1}));
		assertEquals(4, ds.getLong(new int[]{0,2}));
		assertEquals(5, ds.getLong(new int[]{1,2}));
		proc.flipVertical();
		assertEquals(4, ds.getLong(new int[]{0,0}));
		assertEquals(5, ds.getLong(new int[]{1,0}));
		assertEquals(2, ds.getLong(new int[]{0,1}));
		assertEquals(3, ds.getLong(new int[]{1,1}));
		assertEquals(0, ds.getLong(new int[]{0,2}));
		assertEquals(1, ds.getLong(new int[]{1,2}));
	}

	@Test
	public void testFill() {
		setupData(new int[]{2,2}, false, new int[]{0,1,2,3});
		proc.setColor(906);
		proc.fill();
		assertEquals(906, ds.getLong(new int[]{0,0}));
		assertEquals(906, ds.getLong(new int[]{1,0}));
		assertEquals(906, ds.getLong(new int[]{0,1}));
		assertEquals(906, ds.getLong(new int[]{1,1}));
	}

	@Test
	public void testFillImageProcessor() {
		ByteProcessor mask = new ByteProcessor(2,2,new byte[]{0,1,0,1},null);
		setupData(new int[]{2,2}, false, new int[]{0,1,2,3});
		proc.setColor(906);
		proc.fill(mask);
		assertEquals(0, ds.getLong(new int[]{0,0}));
		assertEquals(906, ds.getLong(new int[]{1,0}));
		assertEquals(2, ds.getLong(new int[]{0,1}));
		assertEquals(906, ds.getLong(new int[]{1,1}));
	}

	@Test
	public void testGetPixels() {
		setupData(new int[]{2,2}, false, new int[]{0,1,2,3});
		Object pixels = proc.getPixels();
		assertTrue(pixels instanceof int[]);
		int[] values = (int[]) pixels;
		assertEquals(0, values[0]);
		assertEquals(1, values[1]);
		assertEquals(2, values[2]);
		assertEquals(3, values[3]);
	}

	@Test
	public void testGetPixelsCopy() {
		setupData(new int[]{2,2}, false, new int[]{0,1,2,3});
		Object pixels = proc.getPixelsCopy();
		assertTrue(pixels instanceof int[]);
		assertNotSame(pixels, proc.getPixels());
		int[] values = (int[]) pixels;
		assertEquals(0, values[0]);
		assertEquals(1, values[1]);
		assertEquals(2, values[2]);
		assertEquals(3, values[3]);
	}

	@Test
	public void testGetPixelIntInt() {
		setupData(new int[]{2,2}, false, new byte[]{0,1,2,3});
		assertEquals(0, proc.getPixel(0, 0));
		assertEquals(1, proc.getPixel(1, 0));
		assertEquals(2, proc.getPixel(0, 1));
		assertEquals(3, proc.getPixel(1, 1));
	}

	@Test
	public void testGetIntInt() {
		setupData(new int[]{2,2}, false, new byte[]{0,1,2,3});
		assertEquals(0, proc.get(0, 0));
		assertEquals(1, proc.get(1, 0));
		assertEquals(2, proc.get(0, 1));
		assertEquals(3, proc.get(1, 1));
	}

	@Test
	public void testGetInt() {
		setupData(new int[]{2,2}, false, new byte[]{0,1,2,3});
		assertEquals(0, proc.get(0));
		assertEquals(1, proc.get(1));
		assertEquals(2, proc.get(2));
		assertEquals(3, proc.get(3));
	}

	@Test
	public void testSetIntIntInt() {
		setupData(new int[]{2,2}, false, new byte[]{0,1,2,3});
		assertEquals(0, proc.get(0, 0));
		assertEquals(1, proc.get(1, 0));
		assertEquals(2, proc.get(0, 1));
		assertEquals(3, proc.get(1, 1));
		proc.set(0,0,9);
		proc.set(1,0,8);
		proc.set(0,1,7);
		proc.set(1,1,6);
		assertEquals(9, proc.get(0, 0));
		assertEquals(8, proc.get(1, 0));
		assertEquals(7, proc.get(0, 1));
		assertEquals(6, proc.get(1, 1));
	}

	@Test
	public void testSetIntInt() {
		setupData(new int[]{2,2}, false, new byte[]{0,1,2,3});
		assertEquals(0, proc.get(0, 0));
		assertEquals(1, proc.get(1, 0));
		assertEquals(2, proc.get(0, 1));
		assertEquals(3, proc.get(1, 1));
		proc.set(0,9);
		proc.set(1,8);
		proc.set(2,7);
		proc.set(3,6);
		assertEquals(9, proc.get(0));
		assertEquals(8, proc.get(1));
		assertEquals(7, proc.get(2));
		assertEquals(6, proc.get(3));
	}

	@Test
	public void testGetfIntInt() {
		setupData(new int[]{2,2}, false, new float[]{0,1.1f,2.2f,3.3f});
		assertEquals(0f, proc.getf(0, 0), 0);
		assertEquals(1.1f, proc.getf(1, 0), 0);
		assertEquals(2.2f, proc.getf(0, 1), 0);
		assertEquals(3.3f, proc.getf(1, 1), 0);
	}

	@Test
	public void testGetfInt() {
		setupData(new int[]{2,2}, false, new float[]{0,1.1f,2.2f,3.3f});
		assertEquals(0f, proc.getf(0), 0);
		assertEquals(1.1f, proc.getf(1), 0);
		assertEquals(2.2f, proc.getf(2), 0);
		assertEquals(3.3f, proc.getf(3), 0);
	}

	@Test
	public void testSetfIntIntFloat() {
		setupData(new int[]{2,2}, false, new float[]{0,1.1f,2.2f,3.3f});
		assertEquals(0f, proc.getf(0, 0), 0);
		assertEquals(1.1f, proc.getf(1, 0), 0);
		assertEquals(2.2f, proc.getf(0, 1), 0);
		assertEquals(3.3f, proc.getf(1, 1), 0);
		proc.setf(0,0,9.9f);
		proc.setf(1,0,8.8f);
		proc.setf(0,1,7.7f);
		proc.setf(1,1,6.6f);
		assertEquals(9.9f, proc.getf(0, 0), 0);
		assertEquals(8.8f, proc.getf(1, 0), 0);
		assertEquals(7.7f, proc.getf(0, 1), 0);
		assertEquals(6.6f, proc.getf(1, 1), 0);
	}

	@Test
	public void testSetfIntFloat() {
		setupData(new int[]{2,2}, false, new float[]{0,1.1f,2.2f,3.3f});
		assertEquals(0f, proc.getf(0), 0);
		assertEquals(1.1f, proc.getf(1), 0);
		assertEquals(2.2f, proc.getf(2), 0);
		assertEquals(3.3f, proc.getf(3), 0);
		proc.setf(0,9.9f);
		proc.setf(1,8.8f);
		proc.setf(2,7.7f);
		proc.setf(3,6.6f);
		assertEquals(9.9f, proc.getf(0), 0);
		assertEquals(8.8f, proc.getf(1), 0);
		assertEquals(7.7f, proc.getf(2), 0);
		assertEquals(6.6f, proc.getf(3), 0);
	}

	@Test
	public void testGetInterpolatedPixel() {
		setupData(new int[]{2,2}, false, new int[]{0,1,2,3});
		//assertEquals(1.5, proc.getInterpolatedPixel(0.4, 0.6), 0);
		// fail("not finished");
	}

	@Test
	public void testGetPixelInterpolated() {
		setupData(new int[]{2,2}, false, new int[]{0,1,2,3});
		//assertEquals(1.5, proc.getPixelInterpolated(0.4, 0.6), 0);
		// fail("not finished");
	}

	@Test
	public void testGetBicubicInterpolatedPixel() {
		setupData(new int[]{2,2}, false, new int[]{0,1,2,3});
		proc.getBicubicInterpolatedPixel(0.5, 0.5, proc);
		//assertFalse(true);
		// fail("not finished");
	}

	@Test
	public void testPutPixelIntIntInt() {
		// fail("Not yet implemented");
	}

	@Test
	public void testGetPixelValue() {
		// fail("Not yet implemented");
	}

	@Test
	public void testPutPixelValue() {
		// fail("Not yet implemented");
	}

	@Test
	public void testDrawPixel() {
		setupData(new int[]{2,2}, false, new long[]{0,1,2,3});
		assertEquals(0, proc.getl(0, 0));
		proc.setColor(Color.GRAY);
		proc.drawPixel(0, 0);
		assertEquals(2, proc.getl(0, 0));
	}

	@Test
	public void testSetPixelsObject() {
		setupData(new int[]{2,2}, false, new long[]{0,1,2,3});
		long[] newPixels = new long[]{9,8,7,6};
		proc.setPixels(newPixels);
		assertSame(newPixels, proc.getPixels());
	}

	@Test
	public void testCopyBits() {
		// fail("Not yet implemented");
	}

	@Test
	public void testApplyTable() {
		setupData(new int[]{2,2}, true, new byte[]{0,1,2,3});
		int[] table = new int[256];
		table[0] = 9;
		table[1] = 8;
		table[2] = 7;
		table[3] = 6;
		proc.applyTable(table);
		assertEquals(9, proc.get(0));
		assertEquals(8, proc.get(1));
		assertEquals(7, proc.get(2));
		assertEquals(6, proc.get(3));
	}

	@Test
	public void testInvert() {
		
		// NOTE - this inverts around min/max values - not around min/max allowed values.
		//   This is different from IJ1 for unsigned byte data
		
		setupData(new int[]{2,2}, true, new byte[]{0,10,20,30});
		proc.invert();
		assertEquals(30, proc.get(0));
		assertEquals(20, proc.get(1));
		assertEquals(10, proc.get(2));
		assertEquals(0, proc.get(3));
	}

	@Test
	public void testAddInt() {
		setupData(new int[]{2,2}, true, new byte[]{0,10,20,30});
		proc.add(2);
		assertEquals(2, proc.get(0));
		assertEquals(12, proc.get(1));
		assertEquals(22, proc.get(2));
		assertEquals(32, proc.get(3));
	}

	@Test
	public void testAddDouble() {
		setupData(new int[]{2,2}, false, new double[]{0,10,20,30});
		proc.add(-Math.PI);
		assertEquals(-Math.PI, proc.getd(0), 0);
		assertEquals(10-Math.PI, proc.getd(1), 0);
		assertEquals(20-Math.PI, proc.getd(2), 0);
		assertEquals(30-Math.PI, proc.getd(3), 0);
	}

	@Test
	public void testMultiply() {
		setupData(new int[]{2,2}, false, new double[]{0,10,20,30});
		proc.multiply(0.5);
		assertEquals(0, proc.getd(0), 0);
		assertEquals(5, proc.getd(1), 0);
		assertEquals(10, proc.getd(2), 0);
		assertEquals(15, proc.getd(3), 0);
	}

	@Test
	public void testAnd() {
		setupData(new int[]{2,2}, false, new long[]{1,2,3,4});
		proc.and(2);
		assertEquals(1&2, proc.getl(0));
		assertEquals(2&2, proc.getl(1));
		assertEquals(3&2, proc.getl(2));
		assertEquals(4&2, proc.getl(3));
	}

	@Test
	public void testOr() {
		setupData(new int[]{2,2}, false, new long[]{1,2,3,4});
		proc.or(7);
		assertEquals(1|7, proc.getl(0));
		assertEquals(2|7, proc.getl(1));
		assertEquals(3|7, proc.getl(2));
		assertEquals(4|7, proc.getl(3));
	}

	@Test
	public void testXor() {
		setupData(new int[]{2,2}, false, new long[]{1,2,3,4});
		proc.xor(7);
		assertEquals(1^7, proc.getl(0));
		assertEquals(2^7, proc.getl(1));
		assertEquals(3^7, proc.getl(2));
		assertEquals(4^7, proc.getl(3));
	}

	@Test
	public void testGamma() {
		// fail("Not yet implemented");
	}

	@Test
	public void testLog() {
		// fail("Not yet implemented");
	}

	@Test
	public void testExp() {
		// fail("Not yet implemented");
	}

	@Test
	public void testSqr() {
		setupData(new int[]{2,2}, false, new long[]{1,2,3,4});
		proc.sqr();
		assertEquals(1*1, proc.getl(0));
		assertEquals(2*2, proc.getl(1));
		assertEquals(3*3, proc.getl(2));
		assertEquals(4*4, proc.getl(3));
	}

	@Test
	public void testSqrt() {
		setupData(new int[]{2,2}, false, new double[]{1,2,3,4});
		proc.sqrt();
		assertEquals(Math.sqrt(1), proc.getd(0), 0);
		assertEquals(Math.sqrt(2), proc.getd(1), 0);
		assertEquals(Math.sqrt(3), proc.getd(2), 0);
		assertEquals(Math.sqrt(4), proc.getd(3), 0);
	}

	@Test
	public void testAbs() {
		setupData(new int[]{2,2}, false, new long[]{1,-2,3,-4});
		proc.abs();
		assertEquals(1, proc.getl(0));
		assertEquals(2, proc.getl(1));
		assertEquals(3, proc.getl(2));
		assertEquals(4, proc.getl(3));
	}

	@Test
	public void testMin() {
		setupData(new int[]{2,2}, false, new long[]{1,3,5,7});
		proc.min(4);
		assertEquals(4, proc.getl(0));
		assertEquals(4, proc.getl(1));
		assertEquals(5, proc.getl(2));
		assertEquals(7, proc.getl(3));
	}

	@Test
	public void testMax() {
		setupData(new int[]{2,2}, false, new long[]{1,3,5,7});
		proc.max(4);
		assertEquals(1, proc.getl(0));
		assertEquals(3, proc.getl(1));
		assertEquals(4, proc.getl(2));
		assertEquals(4, proc.getl(3));
	}

	@Test
	public void testCreateImage() {
		// fail("Not yet implemented");
	}

	@Test
	public void testCreateProcessor() {
		// fail("Not yet implemented");
	}

	@Test
	public void testSnapshot() {
		// fail("Not yet implemented");
	}

	@Test
	public void testReset() {
		// fail("Not yet implemented");
	}

	@Test
	public void testResetImageProcessor() {
		// fail("Not yet implemented");
	}

	@Test
	public void testSetSnapshotPixels() {
		// fail("Not yet implemented");
	}

	@Test
	public void testGetSnapshotPixels() {
		// fail("Not yet implemented");
	}

	@Test
	public void testConvolve3x3() {
		// fail("Not yet implemented");
	}

	@Test
	public void testFilter() {
		// fail("Not yet implemented");
	}

	@Test
	public void testMedianFilter() {
		setupData(new int[]{3,3}, true,
				new byte[]{ 1, 6, 3,
							2, 8, 7,
							9, 4, 5});
		proc.medianFilter();
		//assertEquals(0, proc.get(0));
		//assertEquals(2, proc.get(1));
		//assertEquals(0, proc.get(2));
		//assertEquals(2, proc.get(3));
		//assertEquals(5, proc.get(4));
		//assertEquals(4, proc.get(5));
		//assertEquals(0, proc.get(6));
		//assertEquals(4, proc.get(7));
		//assertEquals(0, proc.get(8));
		// fail("not finished");
	}

	@Test
	public void testNoise() {
		// fail("Not yet implemented");
	}

	@Test
	public void testCrop() {
		// fail("Not yet implemented");
	}

	@Test
	public void testThreshold() {
		// fail("Not yet implemented");
	}

	@Test
	public void testDuplicate() {
		// fail("Not yet implemented");
	}

	@Test
	public void testScale() {
		// fail("Not yet implemented");
	}

	@Test
	public void testResizeIntInt() {
		// fail("Not yet implemented");
	}

	@Test
	public void testRotate() {
		// fail("Not yet implemented");
	}

	@Test
	public void testGetHistogram() {
		setupData(new int[]{2,2}, true, new byte[]{0,1,2,3});
		int[] hist = proc.getHistogram();
		for (int i = 0; i < 4; i++)
			assertEquals(1, hist[i]);
		for (int i = 4; i < 256; i++)
			assertEquals(0, hist[i]);
	}

	@Test
	public void testErode() {
		// fail("Not yet implemented");
	}

	@Test
	public void testDilate() {
		// fail("Not yet implemented");
	}

	@Test
	public void testConvolve() {
		// fail("Not yet implemented");
	}

	@Test
	public void testAutoThreshold() {
		// fail("Not yet implemented");
	}

	@Test
	public void testToFloat() {
		setupData(new int[]{2,2}, false, new byte[]{-1,0,1,100});
		ImageProcessor floatProc = proc.toFloat(0, null);
		assertEquals(-1, floatProc.getf(0), 0);
		assertEquals(0, floatProc.getf(1), 0);
		assertEquals(1, floatProc.getf(2), 0);
		assertEquals(100, floatProc.getf(3), 0);
	}

	@Test
	public void testSetPixelsIntFloatProcessor() {
		// fail("Not yet implemented");
	}

	@Test
	public void testCreate8BitImage() {
		// fail("Not yet implemented");
	}

	@Test
	public void testGetBitDepth() {
		setupData(new int[]{2,2}, false, new int[]{0,1,2,3});
		assertEquals(32, proc.getBitDepth());

		setupData(new int[]{2,2}, true, new short[]{0,1,2,3});
		assertEquals(16, proc.getBitDepth());
	}

	@Test
	public void testGetBytesPerPixel() {
		setupData(new int[]{2,2}, false, new int[]{0,1,2,3});
		assertEquals(4, proc.getBytesPerPixel(), 0);

		setupData(new int[]{2,2}, true, new short[]{0,1,2,3});
		assertEquals(2, proc.getBytesPerPixel(), 0);
	}

	@Test
	public void testGetStatisticsIntCalibration() {
		setupData(new int[]{2,2}, false, new int[]{0,1,2,3});
		ImageStatistics stats = proc.getStatistics(0, null);
		assertTrue(stats instanceof GenericStatistics);
	}

	@Test
	public void testIsFloatingType() {
		setupData(new int[]{2,2}, false, new int[]{0,1,2,3});
		assertFalse(proc.isFloatingType());

		setupData(new int[]{2,2}, false, new float[]{0,1,2,3});
		assertTrue(proc.isFloatingType());
	}

	@Test
	public void testIsUnsignedType() {
		setupData(new int[]{2,2}, false, new int[]{0,1,2,3});
		assertFalse(proc.isUnsignedType());

		setupData(new int[]{2,2}, true, new int[]{0,1,2,3});
		assertTrue(proc.isUnsignedType());
	}

	@Test
	public void testGetMinimumAllowedValue() {
		setupData(new int[]{2,2}, false, new int[]{0,1,2,3});
		assertEquals(Integer.MIN_VALUE, proc.getMinimumAllowedValue(), 0);
	}

	@Test
	public void testGetMaximumAllowedValue() {
		setupData(new int[]{2,2}, false, new int[]{0,1,2,3});
		assertEquals(Integer.MAX_VALUE, proc.getMaximumAllowedValue(), 0);
	}

	@Test
	public void testGetTypeName() {
		setupData(new int[]{2,2}, false, new double[]{0,1.1,2.2,3.3});
		assertEquals("64-bit float", proc.getTypeName());
	}

	@Test
	public void testGetdIntInt() {
		setupData(new int[]{2,2}, false, new double[]{0,1.1,2.2,3.3});
		assertEquals(0, proc.getd(0, 0), 0);
		assertEquals(1.1, proc.getd(1, 0), 0);
		assertEquals(2.2, proc.getd(0, 1), 0);
		assertEquals(3.3, proc.getd(1, 1), 0);
	}

	@Test
	public void testGetdInt() {
		setupData(new int[]{2,2}, false, new double[]{0,1.1,2.2,3.3});
		assertEquals(0, proc.getd(0), 0);
		assertEquals(1.1, proc.getd(1), 0);
		assertEquals(2.2, proc.getd(2), 0);
		assertEquals(3.3, proc.getd(3), 0);
	}

	@Test
	public void testSetdIntIntDouble() {
		setupData(new int[]{2,2}, false, new double[]{0,1.1,2.2,3.3});
		assertEquals(0, proc.getd(0, 0), 0);
		assertEquals(1.1, proc.getd(1, 0), 0);
		assertEquals(2.2, proc.getd(0, 1), 0);
		assertEquals(3.3, proc.getd(1, 1), 0);
		proc.setd(0,0,9.9);
		proc.setd(1,0,8.8);
		proc.setd(0,1,7.7);
		proc.setd(1,1,6.6);
		assertEquals(9.9, proc.getd(0, 0), 0);
		assertEquals(8.8, proc.getd(1, 0), 0);
		assertEquals(7.7, proc.getd(0, 1), 0);
		assertEquals(6.6, proc.getd(1, 1), 0);
	}

	@Test
	public void testSetdIntDouble() {
		setupData(new int[]{2,2}, false, new double[]{0,1.1,2.2,3.3});
		assertEquals(0, proc.getd(0), 0);
		assertEquals(1.1, proc.getd(1), 0);
		assertEquals(2.2, proc.getd(2), 0);
		assertEquals(3.3, proc.getd(3), 0);
		proc.setd(0,9.9);
		proc.setd(1,8.8);
		proc.setd(2,7.7);
		proc.setd(3,6.6);
		assertEquals(9.9, proc.getd(0), 0);
		assertEquals(8.8, proc.getd(1), 0);
		assertEquals(7.7, proc.getd(2), 0);
		assertEquals(6.6, proc.getd(3), 0);
	}

	@Test
	public void testGetlIntInt() {
		setupData(new int[]{2,2}, false, new long[]{0,1,2,3});
		assertEquals(0, proc.getl(0, 0));
		assertEquals(1, proc.getl(1, 0));
		assertEquals(2, proc.getl(0, 1));
		assertEquals(3, proc.getl(1, 1));
	}

	@Test
	public void testGetlInt() {
		setupData(new int[]{2,2}, false, new long[]{0,1,2,3});
		assertEquals(0, proc.getl(0));
		assertEquals(1, proc.getl(1));
		assertEquals(2, proc.getl(2));
		assertEquals(3, proc.getl(3));
	}

	@Test
	public void testSetlIntIntLong() {
		setupData(new int[]{2,2}, false, new long[]{0,1,2,3});
		assertEquals(0, proc.getl(0, 0));
		assertEquals(1, proc.getl(1, 0));
		assertEquals(2, proc.getl(0, 1));
		assertEquals(3, proc.getl(1, 1));
		proc.setl(0,0,9);
		proc.setl(1,0,8);
		proc.setl(0,1,7);
		proc.setl(1,1,6);
		assertEquals(9, proc.getl(0, 0));
		assertEquals(8, proc.getl(1, 0));
		assertEquals(7, proc.getl(0, 1));
		assertEquals(6, proc.getl(1, 1));
	}

	@Test
	public void testSetlIntLong() {
		setupData(new int[]{2,2}, false, new long[]{0,1,2,3});
		assertEquals(0, proc.getl(0));
		assertEquals(1, proc.getl(1));
		assertEquals(2, proc.getl(2));
		assertEquals(3, proc.getl(3));
		proc.setl(0,9);
		proc.setl(1,8);
		proc.setl(2,7);
		proc.setl(3,6);
		assertEquals(9, proc.getl(0));
		assertEquals(8, proc.getl(1));
		assertEquals(7, proc.getl(2));
		assertEquals(6, proc.getl(3));
	}

	@Test
	public void testEncodePixelInfo() {
		int[] ints = new int[4];
		long workLong;
		double workDouble;

		// integral, 32 bits or less
		setupData(new int[]{2,2}, false, new byte[]{-1,0,1,98});
		
		proc.encodePixelInfo(ints, 0, 0);
		assertEquals(-1, ints[0]);
		
		proc.encodePixelInfo(ints, 1, 0);
		assertEquals(0, ints[0]);
		
		proc.encodePixelInfo(ints, 0, 1);
		assertEquals(1, ints[0]);
		
		proc.encodePixelInfo(ints, 1, 1);
		assertEquals(98, ints[0]);

		// integral, > 32 bits
		setupData(new int[]{2,2}, false, new long[]{-1,0,1,98});
		
		proc.encodePixelInfo(ints, 0, 0);
		workLong = (((long)ints[1]) << 32) | ((long)ints[0]);
		assertEquals(-1, workLong);
		
		proc.encodePixelInfo(ints, 1, 0);
		workLong = (((long)ints[1]) << 32) | ((long)ints[0]);
		assertEquals(0, workLong);
		
		proc.encodePixelInfo(ints, 0, 1);
		workLong = (((long)ints[1]) << 32) | ((long)ints[0]);
		assertEquals(1, workLong);
		
		proc.encodePixelInfo(ints, 1, 1);
		workLong = (((long)ints[1]) << 32) | ((long)ints[0]);
		assertEquals(98, workLong);

		// float, 32 bits
		setupData(new int[]{2,2}, false, new float[]{-1,0,1,98});
		
		proc.encodePixelInfo(ints, 0, 0);
		workDouble = Float.intBitsToFloat(ints[0]);
		assertEquals(-1, workDouble, 0);
		
		proc.encodePixelInfo(ints, 1, 0);
		workDouble = Float.intBitsToFloat(ints[0]);
		assertEquals(0, workDouble, 0);
		
		proc.encodePixelInfo(ints, 0, 1);
		workDouble = Float.intBitsToFloat(ints[0]);
		assertEquals(1, workDouble, 0);
		
		proc.encodePixelInfo(ints, 1, 1);
		workDouble = Float.intBitsToFloat(ints[0]);
		assertEquals(98, workDouble, 0);

		// float, 64 bits
		setupData(new int[]{2,2}, false, new double[]{-1,0,1,98});
		
		proc.encodePixelInfo(ints, 0, 0);
		workDouble = Double.longBitsToDouble( (((long)ints[1]) << 32) | ((long)ints[0]));
		assertEquals(-1, workDouble, 0);
		
		proc.encodePixelInfo(ints, 1, 0);
		workDouble = Double.longBitsToDouble( (((long)ints[1]) << 32) | ((long)ints[0]));
		assertEquals(0, workDouble, 0);
		
		proc.encodePixelInfo(ints, 0, 1);
		workDouble = Double.longBitsToDouble( (((long)ints[1]) << 32) | ((long)ints[0]));
		assertEquals(1, workDouble, 0);
		
		proc.encodePixelInfo(ints, 1, 1);
		workDouble = Double.longBitsToDouble( (((long)ints[1]) << 32) | ((long)ints[0]));
		assertEquals(98, workDouble, 0);
	}

	@Test
	public void testDatasetProcessor() {
		setupData(new int[]{2,2}, false, new long[]{1,3,5,7});
		assertNotNull(proc);
	}

	@Test
	public void testGetDataset() {
		// this method call wraps input Dataset in a protective wrapper so can't test same/equality
		setupData(new int[]{2,2}, false, new long[]{1,3,5,7});
		Dataset returnedDs = proc.getDataset();
		assertNotSame(ds, returnedDs);
		assertArrayEquals(ds.getDimensions(), returnedDs.getDimensions());
		int[] position = new int[2];
		for (int x = 0; x < 2; x++)
		{
			for (int y = 0; y < 2; y++)
			{
				position[0] = x;
				position[1] = y;
				assertEquals(ds.getLong(position), returnedDs.getLong(position));
			}
		}
	}

}
