package imagej.ij1bridge;

import static org.junit.Assert.*;

import java.awt.Rectangle;
import java.awt.image.ColorModel;
import java.awt.image.IndexColorModel;

import ij.process.ByteProcessor;
import ij.process.ImageProcessor;
import imagej.imglib.dataset.LegacyImgLibDataset;
import imagej.imglib.process.ImageUtils;

import mpicbg.imglib.container.planar.PlanarContainerFactory;
import mpicbg.imglib.image.Image;
import mpicbg.imglib.type.numeric.integer.UnsignedByteType;

import org.junit.Test;

public class BridgeStackTest
{
	private Image<?> image;
	private LegacyImgLibDataset dataset;
	private BridgeStack stack;

	private void setupVariables(int[] dimensions)
	{
		this.image = ImageUtils.createImage(new UnsignedByteType(), new PlanarContainerFactory(), dimensions);
		this.dataset = new LegacyImgLibDataset(this.image);
		this.stack = new BridgeStack(this.dataset, new ImgLibProcessorFactory(this.image));
	}
	
	@Test
	public void testBridgeStack()
	{
		setupVariables(new int[]{5,4});
		assertNotNull(this.stack);
		
		setupVariables(new int[]{5,4,3});
		assertNotNull(this.stack);

		setupVariables(new int[]{5,4,3,2});
		assertNotNull(this.stack);
	}

	@Test
	public void testDeleteSlice()
	{
		Object plane1, plane3;

		setupVariables(new int[]{5,4,1});
		assertNotNull(this.stack);
		
		this.stack.deleteSlice(1);
		assertEquals(0, this.stack.getSize());

		setupVariables(new int[]{5,4,2});
		assertNotNull(this.stack);
		
		plane1 = this.stack.getPixels(2);
		this.stack.deleteSlice(1);
		assertEquals(1, this.stack.getSize());
		assertSame(plane1, this.stack.getPixels(1));

		setupVariables(new int[]{5,4,2});
		assertNotNull(this.stack);
		
		plane1 = this.stack.getPixels(1);
		this.stack.deleteSlice(2);
		assertEquals(1, this.stack.getSize());
		assertSame(plane1, this.stack.getPixels(1));

		setupVariables(new int[]{5,4,3});
		assertNotNull(this.stack);
		
		plane1 = this.stack.getPixels(1);
		plane3 = this.stack.getPixels(3);
		this.stack.deleteSlice(2);
		assertEquals(2, this.stack.getSize());
		assertSame(plane1, this.stack.getPixels(1));
		assertSame(plane3, this.stack.getPixels(2));

	}

	@Test
	public void testDeleteLastSlice()
	{
		setupVariables(new int[]{5,4,7});
		assertNotNull(this.stack);
		
		Object[] planeRefs = this.stack.getImageArray().clone();
		
		int numSlices = this.stack.getSize();
		
		for (int i = numSlices; i > 0; i--)
		{
			this.stack.deleteLastSlice();
			assertEquals(i-1, this.stack.getSize());
			for (int j = 0; j < i-1; j++)
				assertSame(planeRefs[j], this.stack.getPixels(j+1));
		}
	}

	@Test
	public void testGetWidth()
	{
		setupVariables(new int[]{22,44,7});
		assertNotNull(this.stack);
		assertEquals(22,this.stack.getWidth());
	}

	@Test
	public void testGetHeight()
	{
		setupVariables(new int[]{22,44,7});
		assertNotNull(this.stack);
		assertEquals(44,this.stack.getHeight());
	}

	@Test
	public void testGetSize()
	{
		setupVariables(new int[]{22,44});
		assertNotNull(this.stack);
		assertEquals(1,this.stack.getSize());

		setupVariables(new int[]{22,44,2});
		assertNotNull(this.stack);
		assertEquals(2,this.stack.getSize());

		setupVariables(new int[]{22,44,2,3});
		assertNotNull(this.stack);
		assertEquals(6,this.stack.getSize());
	}

	@Test
	public void testIsRGB()
	{
		setupVariables(new int[]{22,44,2,3});
		assertNotNull(this.stack);
		assertFalse(this.stack.isRGB());

		setupVariables(new int[]{22,44,3});
		assertNotNull(this.stack);
		this.stack.setSliceLabel("Red",1);
		assertTrue(this.stack.isRGB());
	}

	@Test
	public void testIsHSB()
	{
		setupVariables(new int[]{22,44,2,3});
		assertNotNull(this.stack);
		assertFalse(this.stack.isHSB());

		setupVariables(new int[]{22,44,3});
		assertNotNull(this.stack);
		this.stack.setSliceLabel("Hue",1);
		assertTrue(this.stack.isHSB());
	}

	@Test
	public void testIsVirtual()
	{
		setupVariables(new int[]{22,44,2,3});
		assertNotNull(this.stack);
		assertFalse(this.stack.isVirtual());
	}

	@Test
	public void testTrim()
	{
		setupVariables(new int[]{200,100,17});
		assertNotNull(this.stack);
		
		int size = this.stack.getSize();

		this.stack.trim();
		
		assertTrue(this.stack.getSize() < size);
	}

	@Test
	public void testAddSliceStringObject()
	{
		setupVariables(new int[]{6,4,2});
		assertNotNull(this.stack);

		this.stack.addSlice("wookieland", new byte[24]);
		assertEquals(3, this.stack.getSize());
		assertEquals("wookieland", this.stack.getSliceLabel(3));
	}

	@Test
	public void testAddSliceStringImageProcessor()
	{
		setupVariables(new int[]{6,4,2});
		assertNotNull(this.stack);

		ByteProcessor byteProc = new ByteProcessor(6, 4, new byte[24], null);
		this.stack.addSlice("wookieland", byteProc);
		assertEquals(3, this.stack.getSize());
		assertSame(byteProc.getPixels(), this.stack.getPixels(3));
		assertEquals("wookieland", this.stack.getSliceLabel(3));
	}

	@Test
	public void testAddSliceStringImageProcessorInt()
	{
		setupVariables(new int[]{6,4,2});
		assertNotNull(this.stack);

		ByteProcessor byteProc = new ByteProcessor(6, 4, new byte[24], null);
		this.stack.addSlice("wookieland", byteProc, 2);
		assertEquals(3, this.stack.getSize());
		assertSame(byteProc.getPixels(), this.stack.getPixels(2));
		assertEquals("wookieland", this.stack.getSliceLabel(2));
	}

	@Test
	public void testSetAndGetRoiRectangle()
	{
		setupVariables(new int[]{2,3,4,5,6,7});
		assertNotNull(this.stack);

		Rectangle baseline = new Rectangle(1,2,3,4);
		this.stack.setRoi(baseline);
		Rectangle rect = this.stack.getRoi();
		assertEquals(baseline, rect);
	}

	@Test
	public void testUpdateImageProcessor()
	{
		setupVariables(new int[]{2,3,4});
		assertNotNull(this.stack);
		
		ImageProcessor proc = this.stack.getProcessor(1);
		
		double origMin = proc.getMin();
		double origMax = proc.getMin();
		float[] origCTable = proc.getCalibrationTable();
		ColorModel origCm = this.stack.getColorModel();

		proc = this.stack.getProcessor(2);
		
		float[] newCTable = new float[25];
		int bits = 4;
		int size = 1 << bits;
		byte[] r = new byte[size], g = new byte[size], b = new byte[size];
		for (int i = 0; i < size; i++)
		{
			r[i] = (byte) i;
			g[i] = (byte) i;
			b[i] = (byte) i;
		}
		ColorModel newCm = new IndexColorModel(bits, size, r, g, b);
		double newMin = 1;
		double newMax = 17;
		
		proc.setMinAndMax(newMin, newMax);
		proc.setCalibrationTable(newCTable);
		proc.setColorModel(newCm);
		
		this.stack.update(proc);
		
		proc = this.stack.getProcessor(1);
		
		assertTrue(origMin != newMin);
		assertTrue(origMax != newMax);
		assertEquals(newMin, proc.getMin(), 0);
		assertEquals(newMax, proc.getMax(), 0);
		assertNotSame(origCTable, proc.getCalibrationTable());
		assertSame(newCTable, proc.getCalibrationTable());
		assertNotSame(origCm, this.stack.getColorModel());
		assertSame(newCm, this.stack.getColorModel());
	}

	@Test
	public void testGetPixelsInt()
	{
		setupVariables(new int[]{1,1,6});
		assertNotNull(this.stack);
		
		for (int i = 0; i < this.stack.getSize(); i++)
		{
			this.dataset.setDouble(new int[]{0,0,i}, i+17);
		}
		
		for (int i = 0; i < this.stack.getSize(); i++)
		{
			byte[] data = (byte[]) this.stack.getPixels(i+1);
			
			assertEquals(i+17, data[0]);
		}
	}

	@Test
	public void testSetPixelsObjectInt()
	{
		setupVariables(new int[]{1,1,6});
		assertNotNull(this.stack);
		
		for (int i = 0; i < this.stack.getSize(); i++)
		{
			byte[] plane = new byte[1];
			plane[0] = (byte) (i - 63);
			this.stack.setPixels(plane, i+1);
		}
		
		for (int i = 0; i < this.stack.getSize(); i++)
		{
			byte[] plane = (byte[]) this.stack.getPixels(i+1);
			
			assertEquals((i-63), plane[0]);
		}
	}

	@Test
	public void testGetImageArray()
	{
		setupVariables(new int[]{1,1,6});
		assertNotNull(this.stack);
		
		Object imageArray = this.stack.getImageArray();
		assertNotNull(imageArray);
		assertTrue(imageArray instanceof Object[]);
		
		Object[] objects = (Object[]) imageArray;
		
		assertEquals(this.stack.getSize(), objects.length);
		
		for (int i = 0; i < objects.length; i++)
		{
			assertSame(this.stack.getPixels(i+1), objects[i]);
		}
	}

	@Test
	public void testGetSliceLabels()
	{
		setupVariables(new int[]{1,1,1});
		assertNotNull(this.stack);
		
		this.stack.deleteLastSlice();
		
		assertNull(this.stack.getSliceLabels());
		
		for (int i = 0; i < 4; i++)
			this.stack.addSlice("porp "+i, new byte[1]);
		
		String[] labels = this.stack.getSliceLabels();
		
		assertEquals(4, labels.length);
		
		for (int i = 0; i < labels.length; i++)
			assertEquals("porp "+i, labels[i]);
	}

	@Test
	public void testSetAndGetSliceLabel()
	{
		setupVariables(new int[]{1,1,4});
		assertNotNull(this.stack);
		
		for (int i = 0; i < 4; i++)
			this.stack.setSliceLabel("wagala "+i, i+1);
		
		for (int i = 0; i < 4; i++)
			assertEquals("wagala "+i, this.stack.getSliceLabel(i+1));
	}

	@Test
	public void testGetShortSliceLabelInt()
	{
		setupVariables(new int[]{2,2,1});
		assertNotNull(this.stack);
		
		this.stack.deleteLastSlice();

		this.stack.addSlice(null,new byte[] {1,2,3,4});
		assertNull(this.stack.getShortSliceLabel(1));
		
		this.stack.addSlice("a",new byte[] {1,2,3,4});
		assertEquals("a",this.stack.getShortSliceLabel(2));

		this.stack.addSlice("\nZippyMan",new byte[] {1,2,3,4});
		assertNull(this.stack.getShortSliceLabel(3));

		this.stack.addSlice("a\nb",new byte[] {1,2,3,4});
		assertEquals("a",this.stack.getShortSliceLabel(4));

		this.stack.addSlice("a.tif",new byte[] {1,2,3,4});
		assertEquals("a",this.stack.getShortSliceLabel(5));

		String sixtyChars = "123456789012345678901234567890123456789012345678901234567890";
		String sixtyOneChars = sixtyChars + "1";
		this.stack.addSlice(sixtyOneChars,new byte[] {1,2,3,4});
		assertEquals(sixtyChars,this.stack.getShortSliceLabel(6));
	}

	@Test
	public void testGetProcessorInt()
	{
		setupVariables(new int[]{1,5,3});
		assertNotNull(this.stack);
		
		ImageProcessor proc;
		
		try {
			proc = this.stack.getProcessor(0);
			fail();
		} catch (IllegalArgumentException e) {
			assertTrue(true);
		}

		for (int i = 1; i <= 3; i++)
		{
			proc = this.stack.getProcessor(i);
			assertNotNull(proc);
			assertNull(proc.getCalibrationTable());
			assertEquals(0, proc.getMin(), 0);
			assertEquals(255, proc.getMax(), 0);
		}
		
		try {
			proc = this.stack.getProcessor(4);
			fail();
		} catch (IllegalArgumentException e) {
			assertTrue(true);
		}
		
		proc = this.stack.getProcessor(1);
		proc.setMinAndMax(93, 178);
		float[] newCTable = new float[25];
		proc.setCalibrationTable(newCTable);
		this.stack.update(proc);

		for (int i = 1; i <= 3; i++)
		{
			proc = this.stack.getProcessor(i);
			assertNotNull(proc);
			assertSame(newCTable, proc.getCalibrationTable());
			assertEquals(93, proc.getMin(), 0);
			assertEquals(178, proc.getMax(), 0);
		}
		
	}

	@Test
	public void testSetAndGetColorModel()
	{
		setupVariables(new int[]{1,5,1});
		assertNotNull(this.stack);
		
		assertNull(this.stack.getColorModel());

		int bits = 4;
		int size = 1 << bits;
		byte[] r = new byte[size], g = new byte[size], b = new byte[size];
		for (int i = 0; i < size; i++)
		{
			r[i] = (byte) i;
			g[i] = (byte) i;
			b[i] = (byte) i;
		}
		ColorModel newCm = new IndexColorModel(bits, size, r, g, b);
		
		this.stack.setColorModel(newCm);
		
		assertSame(newCm, this.stack.getColorModel());
	}

	@Test
	public void testToString()
	{
		setupVariables(new int[]{2,3,4});
		assertNotNull(this.stack);
		
		assertEquals("stack[2x3x4]", this.stack.toString());
	}

}
