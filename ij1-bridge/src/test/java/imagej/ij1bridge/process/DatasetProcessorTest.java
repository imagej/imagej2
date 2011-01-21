package imagej.ij1bridge.process;

import static org.junit.Assert.*;

import java.awt.Color;
import java.awt.Image;

import ij.process.Blitter;
import ij.process.ByteProcessor;
import ij.process.FloatProcessor;
import ij.process.ImageProcessor;
import ij.process.ImageStatistics;
import ij.process.ShortProcessor;
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
		short[] shorts =
			new short[]{17,45,106,
						111,9,92,
						255,187,13};
		
		ImageProcessor ij1Proc;
		
		ij1Proc = new ShortProcessor(3,3,shorts,null);
		setupData(new int[]{3,3}, true, shorts);

		for (int x = 0; x < 3; x++)
		{
			double xD = 0.75*x;
			for (int y = 0; y < 3; y++)
			{
				double yD = 1 + 0.4*y;
				
				double ij1 = ij1Proc.getInterpolatedPixel(xD, yD);
				double ij2 = proc.getInterpolatedPixel(xD, yD);
				assertEquals(ij1, ij2, 0.001);
			}
		}
	}

	@Test
	public void testGetPixelInterpolated() {
		short[] shorts =
			new short[]{17,45,106,
						111,9,92,
						255,187,13};
		
		ImageProcessor ij1Proc;
		
		ij1Proc = new ShortProcessor(3,3,shorts,null);
		setupData(new int[]{3,3}, true, shorts);

		for (int x = 0; x < 3; x++)
		{
			double xD = 0.75*x;
			for (int y = 0; y < 3; y++)
			{
				double yD = 1 + 0.4*y;
				
				double ij1 = ij1Proc.getPixelInterpolated(xD, yD);
				double ij2 = proc.getPixelInterpolated(xD, yD);
				assertEquals(ij1, ij2, 0.001);
			}
		}
	}

	@Test
	public void testGetBicubicInterpolatedPixel() {
		short[] shorts =
			new short[]{17,45,106,
						111,9,92,
						255,187,13};
		
		ImageProcessor ij1Proc;
		
		ij1Proc = new ShortProcessor(3,3,shorts,null);
		setupData(new int[]{3,3}, true, shorts);

		for (int x = 0; x < 3; x++)
		{
			double xD = 0.75*x;
			for (int y = 0; y < 3; y++)
			{
				double yD = 1 + 0.4*y;
				
				double ij1 = ij1Proc.getBicubicInterpolatedPixel(xD, yD, ij1Proc);
				double ij2 = proc.getBicubicInterpolatedPixel(xD, yD, proc);
				assertEquals(ij1, ij2, 0.001);
			}
		}
	}

	@Test
	public void testPutPixelIntIntInt() {
		setupData(new int[]{2,2}, false, new double[]{0.4,1.5,2.6,3.7});
		proc.putPixelValue(0, 0, 93.6);
		proc.putPixelValue(1, 0, -101);
		proc.putPixelValue(0, 1, 0);
		proc.putPixelValue(1, 1, Double.MAX_VALUE);
		assertEquals(93.6, proc.getd(0,0), 0);
		assertEquals(-101, proc.getd(1,0), 0);
		assertEquals(0, proc.getd(0,1), 0);
		assertEquals(Double.MAX_VALUE, proc.getd(1,1), 0);

		setupData(new int[]{2,2}, false, new int[]{Integer.MIN_VALUE,-1,0,5000});
		proc.putPixelValue(0, 0, 93.6);
		proc.putPixelValue(1, 0, -101);
		proc.putPixelValue(0, 1, 0);
		proc.putPixelValue(1, 1, Integer.MAX_VALUE);
		assertEquals(94, proc.getd(0,0), 0);
		assertEquals(-101, proc.getd(1,0), 0);
		assertEquals(0, proc.getd(0,1), 0);
		assertEquals(Integer.MAX_VALUE, proc.getd(1,1), 0);
	}

	@Test
	public void testGetPixelValue()
	{
		setupData(new int[]{2,2}, false, new double[]{0.4,1.5,2.6,3.7});
		assertEquals(0.4, proc.getPixelValue(0,0), 0.00001);
		assertEquals(1.5, proc.getPixelValue(1,0), 0.00001);
		assertEquals(2.6, proc.getPixelValue(0,1), 0.00001);
		assertEquals(3.7, proc.getPixelValue(1,1), 0.00001);
	}

	@Test
	public void testPutPixelValue() {
		setupData(new int[]{2,2}, false, new double[]{0.4,1.5,2.6,3.7});
		proc.putPixelValue(0, 0, 19.19);
		proc.putPixelValue(1, 0, 18.18);
		proc.putPixelValue(0, 1, 17.17);
		proc.putPixelValue(1, 1, 16.16);
		assertEquals(19.19, proc.getPixelValue(0,0), 0.00001);
		assertEquals(18.18, proc.getPixelValue(1,0), 0.00001);
		assertEquals(17.17, proc.getPixelValue(0,1), 0.00001);
		assertEquals(16.16, proc.getPixelValue(1,1), 0.00001);
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

	private ImageProcessor initNewProc()
	{
		ImageProcessor newProc = proc.duplicate();
		
		newProc.setl(0, 54);
		newProc.setl(1, 51);
		newProc.setl(2, 48);
		newProc.setl(3, 45);
		newProc.setl(4, 42);
		newProc.setl(5, 39);
		newProc.setl(6, 36);
		newProc.setl(7, 33);
		newProc.setl(8, 30);
		newProc.setl(9, 27);
		newProc.setl(10, 24);
		newProc.setl(11, 21);
		newProc.setl(12, 18);
		newProc.setl(13, 15);
		newProc.setl(14, 12);
		newProc.setl(15, 9);
		
		return newProc;
	}
	
	@Test
	public void testCopyBits(){
		setupData(new int[]{4,4},
				false,
				new long[]{0, 1, 2, 3,
							4, 5, 6, 7,
							8, 9, 10, 11,
							12, 13, 14, 15});
		
		ImageProcessor newProc;
		
		newProc = initNewProc();
		newProc.copyBits(proc, 0, 0, Blitter.ADD);
		assertEquals(54+0, newProc.getl(0));
		assertEquals(9+15, newProc.getl(15));
		
		newProc = initNewProc();
		newProc.copyBits(proc, 0, 0, Blitter.AND);
		assertEquals(54&0, newProc.getl(0));
		assertEquals(33&7, newProc.getl(7));
		assertEquals(21&11, newProc.getl(11));
		
		newProc = initNewProc();
		newProc.copyBits(proc, 0, 0, Blitter.AVERAGE);
		assertEquals((54+0)/2, newProc.getl(0));
		assertEquals((33+7)/2, newProc.getl(7));
		assertEquals((21+11)/2, newProc.getl(11));
		
		newProc = initNewProc();
		newProc.copyBits(proc, 0, 0, Blitter.COPY);
		assertEquals(0, newProc.getl(0));
		assertEquals(7, newProc.getl(7));
		assertEquals(11, newProc.getl(11));
		
		newProc = initNewProc();
		newProc.copyBits(proc, 0, 0, Blitter.COPY_INVERTED);
		assertEquals(0, newProc.getl(0));
		assertEquals(-7, newProc.getl(7));
		assertEquals(-11, newProc.getl(11));
		
		newProc = initNewProc();
		newProc.copyBits(proc, 0, 0, Blitter.COPY_TRANSPARENT);
		assertEquals(0, newProc.getl(0));
		assertEquals(7, newProc.getl(7));
		assertEquals(11, newProc.getl(11));
		
		newProc = initNewProc();
		newProc.copyBits(proc, 0, 0, Blitter.COPY_ZERO_TRANSPARENT);
		assertEquals(54, newProc.getl(0));
		assertEquals(7, newProc.getl(7));
		assertEquals(11, newProc.getl(11));
		
		newProc = initNewProc();
		newProc.copyBits(proc, 0, 0, Blitter.DIFFERENCE);
		assertEquals(Math.abs(54-0), newProc.getl(0));
		assertEquals(Math.abs(42-4), newProc.getl(4));
		assertEquals(Math.abs(27-9), newProc.getl(9));
		
		newProc = initNewProc();
		newProc.copyBits(proc, 0, 0, Blitter.DIVIDE);
		assertEquals(Long.MAX_VALUE, newProc.getl(0));
		assertEquals(42/4, newProc.getl(4));
		assertEquals(27/9, newProc.getl(9));
		
		newProc = initNewProc();
		newProc.copyBits(proc, 0, 0, Blitter.MAX);
		assertEquals(54, newProc.getl(0));
		assertEquals(15, newProc.getl(13));
		assertEquals(14, newProc.getl(14));
		
		newProc = initNewProc();
		newProc.copyBits(proc, 0, 0, Blitter.MIN);
		assertEquals(0, newProc.getl(0));
		assertEquals(13, newProc.getl(13));
		assertEquals(12, newProc.getl(14));
		
		newProc = initNewProc();
		newProc.copyBits(proc, 0, 0, Blitter.MULTIPLY);
		assertEquals(54*0, newProc.getl(0));
		assertEquals(42*4, newProc.getl(4));
		assertEquals(27*9, newProc.getl(9));
		
		newProc = initNewProc();
		newProc.copyBits(proc, 0, 0, Blitter.OR);
		assertEquals(54|0, newProc.getl(0));
		assertEquals(33|7, newProc.getl(7));
		assertEquals(21|11, newProc.getl(11));
		
		newProc = initNewProc();
		newProc.copyBits(proc, 0, 0, Blitter.SUBTRACT);
		assertEquals(54-0, newProc.getl(0));
		assertEquals(42-4, newProc.getl(4));
		assertEquals(27-9, newProc.getl(9));
		
		newProc = initNewProc();
		newProc.copyBits(proc, 0, 0, Blitter.XOR);
		assertEquals(54^0, newProc.getl(0));
		assertEquals(33^7, newProc.getl(7));
		assertEquals(21^11, newProc.getl(11));
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
		setupData(new int[]{2,2}, false, new double[]{0,14.3,452.7,1013.8});
		proc.gamma(0.75);
		assertEquals(0.0, proc.getd(0), 0.00001);
		assertEquals(7.35363, proc.getd(1), 0.00001);
		assertEquals(98.14267, proc.getd(2), 0.00001);
		assertEquals(179.66530, proc.getd(3), 0.00001);
	}

	@Test
	public void testLog() {
		setupData(new int[]{2,2}, false, new double[]{0,14.3,452.7,1013.8});
		proc.log();
		assertEquals(0.0, proc.getd(0), 0.00001);
		assertEquals(2.66026, proc.getd(1), 0.00001);
		assertEquals(6.11523, proc.getd(2), 0.00001);
		assertEquals(6.92146, proc.getd(3), 0.00001);
	}

	@Test
	public void testExp() {
		setupData(new int[]{2,2}, false, new double[]{0,1,-4,11.3});
		proc.exp();
		assertEquals(1, proc.getd(0), 0.00001);
		assertEquals(Math.E, proc.getd(1), 0.00001);
		assertEquals(0.01832, proc.getd(2), 0.00001);
		assertEquals(80821.63754, proc.getd(3), 0.00001);
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
		setupData(new int[]{2,2}, false, new long[]{1,2,3,4});
		Image image = proc.createImage();
		assertNotNull(image);
		// TODO - what else do I test?
	}

	@Test
	public void testCreateProcessor() {
		setupData(new int[]{2,2}, false, new long[]{1,2,3,4});
		ImageProcessor newProc = proc.createProcessor(7, 5);
		assertEquals(7,newProc.getWidth());
		assertEquals(5,newProc.getHeight());
		assertEquals(proc.getMax(), newProc.getMax(), 0);
		assertEquals(proc.getMin(), newProc.getMin(), 0);
		assertEquals(proc.getInterpolationMethod(), newProc.getInterpolationMethod());
		assertEquals(proc.getColorModel(), newProc.getColorModel());
	}

	@Test
	public void testSnapshotAndReset() {
		setupData(new int[]{2,2}, false, new long[]{1,3,5,7});
		proc.snapshot();
		proc.setl(0, 99);
		proc.setl(1, 98);
		proc.setl(2, 97);
		proc.setl(3, 96);
		assertEquals(99, proc.getl(0));
		assertEquals(98, proc.getl(1));
		assertEquals(97, proc.getl(2));
		assertEquals(96, proc.getl(3));
		proc.reset();
		assertEquals(1, proc.getl(0));
		assertEquals(3, proc.getl(1));
		assertEquals(5, proc.getl(2));
		assertEquals(7, proc.getl(3));
	}

	@Test
	public void testResetImageProcessor() {
		setupData(new int[]{2,2}, false, new long[]{1,3,5,7});
		proc.snapshot();
		proc.setl(0, 99);
		proc.setl(1, 98);
		proc.setl(2, 97);
		proc.setl(3, 96);
		assertEquals(99, proc.getl(0));
		assertEquals(98, proc.getl(1));
		assertEquals(97, proc.getl(2));
		assertEquals(96, proc.getl(3));
		ImageProcessor mask = new ByteProcessor(2,2,new byte[]{0,1,0,1}, null);
		proc.reset(mask);
		assertEquals(1, proc.getl(0));
		assertEquals(98, proc.getl(1));
		assertEquals(5, proc.getl(2));
		assertEquals(96, proc.getl(3));
	}

	@Test
	public void testSetAndGetSnapshotPixels() {
		setupData(new int[]{2,2}, false, new long[]{1,3,5,7});
		proc.snapshot();
		assertEquals(1, ((long[])proc.getSnapshotPixels())[0]);
		assertEquals(3, ((long[])proc.getSnapshotPixels())[1]);
		assertEquals(5, ((long[])proc.getSnapshotPixels())[2]);
		assertEquals(7, ((long[])proc.getSnapshotPixels())[3]);
		long[] newPixelData = new long[]{2,4,6,8};
		proc.setSnapshotPixels(newPixelData);
		assertEquals(2, ((long[])proc.getSnapshotPixels())[0]);
		assertEquals(4, ((long[])proc.getSnapshotPixels())[1]);
		assertEquals(6, ((long[])proc.getSnapshotPixels())[2]);
		assertEquals(8, ((long[])proc.getSnapshotPixels())[3]);
	}

	@Test
	public void testConvolve3x3() {
		short[] shorts =
			new short[]{17,45,106,
						111,9,92,
						255,187,13};
		
		int[] kernel = {-1, 1,-1,
						 1, 3, 1,
						-1, 1,-1};
		
		ImageProcessor ij1Proc;
		
		ij1Proc = new ShortProcessor(3,3,shorts,null);
		setupData(new int[]{3,3}, true, shorts);
		assertEquals(ij1Proc.get(4), proc.get(4));
		
		ij1Proc.convolve3x3(kernel);
		proc.convolve3x3(kernel);
		
		for (int i = 0; i < 9; i++)
			assertEquals(ij1Proc.getl(i), proc.getl(i));
	}

	@Test
	public void testFilter() {
		// all filters tested elsewhere indirectly except BLUR_MORE & FIND_EDGES
		short[] shorts =
			new short[]{17,45,106,
						111,9,92,
						255,187,13};
		
		ImageProcessor ij1Proc;
		
		setupData(new int[]{3,3}, true, shorts);
		ij1Proc = new ShortProcessor(3,3,shorts.clone(),null);
		
		ij1Proc.filter(ImageProcessor.BLUR_MORE);
		proc.filter(ImageProcessor.BLUR_MORE);
		
		for (int i = 0; i < 9; i++)
			assertEquals(ij1Proc.getl(i), proc.getl(i));
		
		setupData(new int[]{3,3}, true, shorts);
		ij1Proc = new ShortProcessor(3,3,shorts.clone(),null);
		
		ij1Proc.filter(ImageProcessor.FIND_EDGES);
		proc.filter(ImageProcessor.FIND_EDGES);

		for (int i = 0; i < 9; i++)
			assertEquals(ij1Proc.getl(i), proc.getl(i));
	}

	@Test
	public void testMedianFilter() {
		setupData(new int[]{3,3}, true,
				new byte[]{ 1, 6, 3,
							2, 8, 7,
							9, 4, 5});
		proc.medianFilter();
		assertEquals(2, proc.get(0,0));
		assertEquals(3, proc.get(1,0));
		assertEquals(6, proc.get(2,0));
		assertEquals(4, proc.get(0,1));
		assertEquals(5, proc.get(1,1));
		assertEquals(5, proc.get(2,1));
		assertEquals(8, proc.get(0,2));
		assertEquals(5, proc.get(1,2));
		assertEquals(5, proc.get(2,2));
	}

	@Test
	public void testNoise() {
		setupData(new int[]{4,4}, true,
				new short[]{ 0, 10, 20, 30,
							40, 50, 60, 70,
							80, 90, 100, 110,
							120, 130, 140, 150});
		proc.noise(12);
		
		// TODO - not sure what to test ...
	}

	@Test
	public void testCrop() {
		setupData(new int[]{4,4}, false,
				new short[]{1,2,3,4,
							5,6,7,8,
							9,10,11,12,
							13,14,15,16});
		proc.setRoi(1, 1, 2, 2);
		ImageProcessor newProc = proc.crop();
		assertEquals(2, newProc.getWidth());
		assertEquals(2, newProc.getHeight());
		assertEquals(6, newProc.getl(0,0));
		assertEquals(7, newProc.getl(1,0));
		assertEquals(10, newProc.getl(0,1));
		assertEquals(11, newProc.getl(1,1));
	}

	@Test
	public void testThreshold() {
		setupData(new int[]{4,4}, false,
				new short[]{1,2,3,4,
							5,6,7,8,
							9,10,11,12,
							13,14,15,16});
		proc.threshold(0);
		for (int i = 0; i < 16; i++)
			assertEquals(255, proc.getl(i));

		setupData(new int[]{4,4}, false,
				new short[]{1,2,3,4,
							5,6,7,8,
							9,10,11,12,
							13,14,15,16});
		proc.threshold(16);
		for (int i = 0; i < 16; i++)
			assertEquals(0, proc.getl(i));

		setupData(new int[]{4,4}, false,
				new short[]{1,2,3,4,
							5,6,7,8,
							9,10,11,12,
							13,14,15,16});
		proc.threshold(8);
		for (int i = 0; i < 8; i++)
			assertEquals(0, proc.getl(i));
		for (int i = 8; i < 16; i++)
			assertEquals(255, proc.getl(i));
	}

	@Test
	public void testDuplicate() {
		setupData(new int[]{2,2}, true, new short[]{1,2,3,4});
		ImageProcessor newProc = proc.duplicate();
		assertEquals(proc.getWidth(), newProc.getWidth());
		assertEquals(proc.getHeight(), newProc.getHeight());
		assertEquals(proc.getColorModel(), newProc.getColorModel());
		assertEquals(proc.getMax(), newProc.getMax(), 0);
		assertEquals(proc.getMin(), newProc.getMin(), 0);
		for (int i = 0; i < 4; i++)
			assertEquals(proc.getl(i), newProc.getl(i));
	}

	@Test
	public void testScale() {
		short[] shorts =
			new short[]{17,45,106,
						111,9,92,
						255,187,13};
		
		ImageProcessor ij1Proc;
		
		ij1Proc = new ShortProcessor(3,3,shorts,null);
		setupData(new int[]{3,3}, true, shorts);

		ij1Proc.scale(0.5,0.7);
		proc.scale(0.5,0.7);
		
		for (int j = 0; j < 9; j++)
			assertEquals(ij1Proc.getl(j), proc.getl(j));

		ij1Proc.scale(2.3,1.7);
		proc.scale(2.3,1.7);
		
		for (int j = 0; j < 9; j++)
			assertEquals(ij1Proc.getl(j), proc.getl(j));
	}

	@Test
	public void testResizeIntInt() {
		short[] shorts =
			new short[]{17,45,106,
						111,9,92,
						255,187,13};
		
		ImageProcessor ij1Proc;
		
		ij1Proc = new ShortProcessor(3,3,shorts,null);
		setupData(new int[]{3,3}, true, shorts);

		ij1Proc.resize(5,7);
		proc.resize(5,7);

		int width = ij1Proc.getWidth();
		int height = ij1Proc.getHeight();
		
		assertEquals(width, proc.getWidth());
		assertEquals(height, proc.getHeight());
		
		for (int x = 0; x < width; x++)
			for (int y = 0; y < height; y++)
				assertEquals(ij1Proc.getl(x,y), proc.getl(x,y));
	}

	@Test
	public void testRotate() {
		short[] shorts =
			new short[]{17,45,106,
						111,9,92,
						255,187,13};
		
		ImageProcessor ij1Proc;
		
		ij1Proc = new ShortProcessor(3,3,shorts,null);
		setupData(new int[]{3,3}, true, shorts);

		for (int i = 0; i < 6; i++)
		{
			ij1Proc.rotate(90);
			proc.rotate(90);
			
			for (int j = 0; j < 9; j++)
				assertEquals(ij1Proc.getl(j), proc.getl(j));
		}

		for (int i = 0; i < 6; i++)
		{
			ij1Proc.rotate(-30);
			proc.rotate(-30);
			
			for (int j = 0; j < 9; j++)
				assertEquals(ij1Proc.getl(j), proc.getl(j));
		}

		for (int i = 0; i < 6; i++)
		{
			ij1Proc.rotate(49.4);
			proc.rotate(49.4);
			
			for (int j = 0; j < 9; j++)
				assertEquals(ij1Proc.getl(j), proc.getl(j));
		}
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
		byte[] bytes =
			new byte[]{17,45,106,
						111,9,92,
						(byte)255,(byte)187,13};
		setupData(new int[]{3,3}, true, bytes);
		ImageProcessor ij1Proc = new ByteProcessor(3,3,bytes,null);
		
		ij1Proc.erode();
		proc.erode();
		
		for (int i = 0; i < 9; i++)
			assertEquals(ij1Proc.getl(i), proc.getl(i));
		
		ij1Proc.erode();
		proc.erode();
		
		for (int i = 0; i < 9; i++)
			assertEquals(ij1Proc.getl(i), proc.getl(i));
	}

	@Test
	public void testDilate() {
		byte[] bytes =
			new byte[]{17,45,106,
						111,9,92,
						(byte)255,(byte)187,13};
		setupData(new int[]{3,3}, true, bytes);
		ImageProcessor ij1Proc = new ByteProcessor(3,3,bytes,null);
		
		ij1Proc.dilate();
		proc.dilate();
		
		for (int i = 0; i < 9; i++)
			assertEquals(ij1Proc.getl(i), proc.getl(i));
		
		ij1Proc.dilate();
		proc.dilate();
		
		for (int i = 0; i < 9; i++)
			assertEquals(ij1Proc.getl(i), proc.getl(i));
	}

	@Test
	public void testConvolve() {
		short[] shorts =
			new short[]{17,45,106,
						111,9,92,
						255,187,13};
		
		float[] kernel = {-1, 1,-1,
						 1, 3, 1,
						-1, 1,-1};
		
		ImageProcessor ij1Proc;
		
		ij1Proc = new ShortProcessor(3,3,shorts,null);
		setupData(new int[]{3,3}, true, shorts);
		
		ij1Proc.convolve(kernel,3,3);
		proc.convolve(kernel,3,3);
		
		for (int i = 0; i < 9; i++)
			assertEquals(ij1Proc.getl(i), proc.getl(i));
	}

	@Test
	public void testAutoThreshold() {
		byte[] bytes =
			new byte[]{17,45,106,
						111,9,92,
						(byte)255,(byte)187,13};
		setupData(new int[]{3,3}, true, bytes);
		ImageProcessor ij1Proc = new ByteProcessor(3,3,bytes,null);
		
		ij1Proc.autoThreshold();
		proc.autoThreshold();
		
		for (int i = 0; i < 9; i++)
			assertEquals(ij1Proc.getd(i), proc.getd(i), 0.001);
		
		float[] floats =
			new float[]{1.1f, 2.2f, 3.3f,
						4.4f, 5.5f, 6.6f,
						7.7f, 8.8f, 9.9f};
		
		setupData(new int[]{3,3}, false, floats);
		ij1Proc = new FloatProcessor(3,3,floats,null);

		ij1Proc.autoThreshold();
		proc.autoThreshold();
		
		for (int i = 0; i < 9; i++)
			assertEquals(ij1Proc.getd(i), proc.getd(i), 0.001);
		
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
		setupData(new int[]{2,2}, true, new int[]{0,1,2,3});
		FloatProcessor floatProc = new FloatProcessor(2,2,new float[]{19.6f, 18.5f, 17.4f, 16.3f}, null);
		proc.setPixels(0, floatProc);
		assertEquals(20, proc.getl(0));
		assertEquals(19, proc.getl(1));
		assertEquals(17, proc.getl(2));
		assertEquals(16, proc.getl(3));
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
