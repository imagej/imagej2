package ij;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.awt.*;
import java.awt.image.*;
import ij.process.*;

public class ImageStackTest {

	private ImageStack is;
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testImageStack()
	{
		ImageStack is = new ImageStack();
		assertNotNull(is);
	}

	@Test
	public void testImageStackWidthHeight()
	{
		is = new ImageStack(-1,-1);
		assertNotNull(is);

		is = new ImageStack(0,0);
		assertNotNull(is);
		
		is = new ImageStack(1,1);
		assertNotNull(is);
		
		is = new ImageStack(1,10);
		assertNotNull(is);
		
		is = new ImageStack(10,1);
		assertNotNull(is);
		
		is = new ImageStack(257,257);
		assertNotNull(is);
	}

	@Test
	public void testImageStackWidthHeightSize()
	{
		try {
			is = new ImageStack(-1,-1,-1);
			fail();
		} catch (NegativeArraySizeException e)
		{
			assertTrue(true);
		}

		is = new ImageStack(0,0,0);
		assertNotNull(is);
		
		is = new ImageStack(1,1,1);
		assertNotNull(is);
		
		is = new ImageStack(1,10,5);
		assertNotNull(is);
		
		is = new ImageStack(10,1,5);
		assertNotNull(is);
		
		is = new ImageStack(257,257,257);
		assertNotNull(is);
	}

	@Test
	public void testImageStackWidthHeightColormodel()
	{
		ColorModel cm = new DirectColorModel(32,0x00ff0000,0x0000ff00,0x000000ff,0xff000000);
		
		is = new ImageStack(-1,-1,null);
		assertNotNull(is);

		is = new ImageStack(-1,-1,cm);
		assertNotNull(is);

		is = new ImageStack(0,0,null);
		assertNotNull(is);
		
		is = new ImageStack(0,0,cm);
		assertNotNull(is);

		is = new ImageStack(1,1,null);
		assertNotNull(is);
		
		is = new ImageStack(1,1,cm);
		assertNotNull(is);

		is = new ImageStack(1,10,null);
		assertNotNull(is);
		
		is = new ImageStack(1,10,cm);
		assertNotNull(is);

		is = new ImageStack(10,1,null);
		assertNotNull(is);
		
		is = new ImageStack(10,1,cm);
		assertNotNull(is);

		is = new ImageStack(257,257,null);
		assertNotNull(is);

		is = new ImageStack(257,257,cm);
		assertNotNull(is);
	}

	@Test
	public void testAddSliceLabelPixels()
	{
		// null pixels passed in should throw exception
		
		is = new ImageStack();
		
		try {
			is.addSlice("Fred", (Object)null);  // null pixels
			fail();
		} catch (IllegalArgumentException e) {
			assertTrue(true);
		}

		// passing in data other than an array of numbers should throw an exception
		
		is = new ImageStack();

		try {
			is.addSlice("Martha", "Jones");  // "Jones" != an array of numbers
			fail();
		} catch (IllegalArgumentException e) {
			assertTrue(true);
		}
		
		// do one that does not cause a stack expansion
		
		is = new ImageStack(1,5);
		is.addSlice("Fake pixels", new byte[] {1,2,3,4,5});
		assertEquals(1,is.getSize());
		
		// now do one that should cause a stack expansion
		is = new ImageStack(1,5);
		for (int i = 0; i < 256; i++)
			is.addSlice(("Fake pixels "+i), new byte[] {1,2,3,4,5});

		assertEquals(256,is.getSize());
	}

	// This test should behave exactly the same as the previous test. I've cut and pasted the tests but have
	// only modified the parameters to increase coverage.
	
	@Test
	public void testAddUnsignedShortSliceLabelPixels()
	{
		// null pixels passed in should throw exception
		
		is = new ImageStack();
		
		try {
			is.addUnsignedShortSlice("Zooky", (Object)null);  // null pixels
			fail();
		} catch (IllegalArgumentException e) {
			assertTrue(true);
		}

		// passing in data other than an array of numbers should throw an exception
		
		is = new ImageStack();

		try {
			is.addUnsignedShortSlice("42", new DirectColorModel(24,0xff0000,0x00ff00,0x0000ff,0));
			fail();
		} catch (IllegalArgumentException e) {
			assertTrue(true);
		}
		
		// do one that does not cause a stack expansion
		
		is = new ImageStack(3,2);
		is.addUnsignedShortSlice("Worse pixels", new byte[] {6,5,4,3,2,1});
		assertEquals(1,is.getSize());
		
		// now do one that should cause a stack expansion
		is = new ImageStack(3,2);
		for (int i = 0; i < 1200; i++)
			is.addUnsignedShortSlice(("Even worser pixels "+i), new short[] {42,42,42,42,42,42});

		assertEquals(1200,is.getSize());
	}

	@Test
	public void testAddSliceLabelProcessor()
	{
		ByteProcessor ip;
		
		// stack width != processor width should throw an exception
		is = new ImageStack(10,10);
		ip = new ByteProcessor(10,1);
		try {
			is.addSlice("Failure 1", ip);
			fail();
		} catch (IllegalArgumentException e) {
			assertTrue(true);
		}

		// stack height != processor height should throw an exception
		is = new ImageStack(10,10);
		ip = new ByteProcessor(1,10);
		try {
			is.addSlice("Failure 2", ip);
			fail();
		} catch (IllegalArgumentException e) {
			assertTrue(true);
		}

		// otherwise it should work
		is = new ImageStack(10,10);
		ip = new ByteProcessor(10,10);
		is.addSlice("Uno",ip);
		
		assertEquals(1,is.getSize());
	}

	@Test
	public void testAddSliceLabelProcessorNumber()
	{
		ByteProcessor ip;
		
		// if n < 0 should throw an exception

		is = new ImageStack(3,2);
		ip = new ByteProcessor(3,2);
		
		try {
			is.addSlice("Kerpow!",ip,-1);
			fail();
		} catch (IllegalArgumentException e)
		{
			assertTrue(true);
		}

		// if n > nSlices should throw an exception

		is = new ImageStack(3,2);
		ip = new ByteProcessor(3,2);
		
		try {
			is.addSlice("Kablooey!",ip,1);
			fail();
		} catch (IllegalArgumentException e)
		{
			assertTrue(true);
		}
		
		// otherwise it should succeed

		is = new ImageStack(3,2);
		ip = new ByteProcessor(3,2);
		
		is.addSlice("Yeehah!",ip,0);
		assertEquals(1,is.getSize());
	}

	@Test
	public void testDeleteSlice()
	{
		// deleting slice < 1 should throw exception
		is = new ImageStack();
		try {
			is.deleteSlice(0);
			fail();
		} catch (IllegalArgumentException e) {
			assertTrue(true);
		}
		
		// deleting slice > nslices should throw exception
		is = new ImageStack(2,2);
		is.addSlice("Temp", (Object)new byte[]{0,1,2,3});
		try {
			is.deleteSlice(2);
			fail();
		} catch (IllegalArgumentException e) {
			assertTrue(true);
		}

		// otherwise things should work
		
		// delete the only slice present

		byte[] a = new byte[] {0,1,2,3};
		byte[] b = new byte[] {4,5,6,7};
		byte[] c = new byte[] {8,9,10,11};

		is = new ImageStack(2,2);
		is.addSlice("Succeed please", (Object)a);
		is.deleteSlice(1);
		assertEquals(0,is.getSize());
		
		// delete the first slice
		is = new ImageStack(2,2);
		is.addSlice("By golly", (Object)a);
		is.addSlice("Geewilikers", (Object)b);
		is.addSlice("Gosh darn", (Object)c);
		is.deleteSlice(1);
		assertEquals(2,is.getSize());
		assertEquals(b,is.getPixels(1));
		assertEquals(c,is.getPixels(2));
		
		// delete the last slice
		is = new ImageStack(2,2);
		is.addSlice("By golly", (Object)a);
		is.addSlice("Geewilikers", (Object)b);
		is.addSlice("Gosh darn", (Object)c);
		is.deleteSlice(3);
		assertEquals(2,is.getSize());
		assertEquals(a,is.getPixels(1));
		assertEquals(b,is.getPixels(2));
		
		// delete a middle slice
		is = new ImageStack(2,2);
		is.addSlice("By golly", (Object)a);
		is.addSlice("Geewilikers", (Object)b);
		is.addSlice("Gosh darn", (Object)c);
		is.deleteSlice(2);
		assertEquals(2,is.getSize());
		assertEquals(a,is.getPixels(1));
		assertEquals(c,is.getPixels(2));
	}

	@Test
	public void testDeleteLastSlice()
	{
		int Total = 512;
		
		is = new ImageStack(1,7);
		
		// deleting from an empty list should work
		is.deleteLastSlice();
		assertEquals(0,is.getSize());
		
		// now add a bunch of slices
		for (int i = 1; i <= Total; i++)
			is.addSlice(""+i, new short[] {1,2,3,4,5,6,7});
		
		// then delete them
		for (int i = Total; i > 0; i--)
		{
			// make sure slice is gone
			is.deleteLastSlice();
			assertEquals(i-1,is.getSize());
			
			// make sure the order is still correct
			for (int j=1; j < i-1; j++)
				assertEquals(""+j,is.getSliceLabel(j));
		}
		
		// and delete an extra to make sure its all working
		is.deleteLastSlice();
		assertEquals(0,is.getSize());
	}

	@Test
    public void testGetWidth()
	{
		is = new ImageStack();
		assertEquals(0,is.getWidth());
		
		is = new ImageStack(-1,-2);
		assertEquals(-1,is.getWidth());

		is = new ImageStack(14,44);
		assertEquals(14,is.getWidth());
	}

	@Test
    public void testGetHeight()
	{
		is = new ImageStack();
		assertEquals(0,is.getHeight());
		
		is = new ImageStack(-1,-2);
		assertEquals(-2,is.getHeight());

		is = new ImageStack(14,44);
		assertEquals(44,is.getHeight());
	}

	// TODO : left off here
	@Test
	public void testSetRoi()
	{
	}

	@Test
	public void testGetRoi()
	{
	}

	@Test
	public void testUpdate()
	{
	}

	@Test
	public void testGetPixels()
	{
	}

	@Test
	public void testSetPixels()
	{
	}

	@Test
	public void testGetImageArray()
	{
	}

	@Test
	public void testGetSize()
	{
	}

	@Test
	public void testGetSliceLabels()
	{
	}

	@Test
	public void testGetSliceLabel()
	{
	}

	@Test
	public void testGetShortSliceLabel()
	{
	}

	@Test
	public void testSetSliceLabel()
	{
	}

	@Test
	public void testGetProcessor()
	{
	}

	@Test
	public void testSetColorModel()
	{
	}

	@Test
	public void testGetColorModel()
	{
	}

	@Test
	public void testIsRGB()
	{
	}

	@Test
	public void testIsHSB()
	{
	}

	@Test
	public void testIsVirtual()
	{
	}

	@Test
	public void testTrim()
	{
	}

	@Test
	public void testToString()
	{
	}
}
