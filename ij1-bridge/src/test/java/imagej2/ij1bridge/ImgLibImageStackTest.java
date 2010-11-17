package imagej2.ij1bridge;

import static org.junit.Assert.*;

import org.junit.Test;

import java.awt.*;
import java.awt.image.*;
import ij.process.*;

public class ImgLibImageStackTest {

	private ImgLibImageStack is;
	
	/*  trying to eliminate default constructor
	@Test
	public void testImageStack()
	{
		ImageStack is = new ImageStack();
		assertNotNull(is);
		assertEquals(0,is.getWidth());
		assertEquals(0,is.getHeight());
	}
	*/
	
	private void tryConsWH(int width, int height)
	{
		is = new ImgLibImageStack(width,height);
		assertNotNull(is);
		assertEquals(width,is.getWidth());
		assertEquals(height,is.getHeight());
	}
	
	@Test
	public void testImgLibImageStackWidthHeight()
	{
		tryConsWH(-1,-1);
		tryConsWH(0,0);
		tryConsWH(1,1);
		tryConsWH(1,10);
		tryConsWH(10,1);
		tryConsWH(257,257);
		tryConsWH(65537,65537);
	}

	private void tryConsWHI(int width, int height, int initialSize)
	{
		is = new ImgLibImageStack(width,height,initialSize);
		assertNotNull(is);
		assertEquals(width,is.getWidth());
		assertEquals(height,is.getHeight());
		//assertEquals(initialSize,is.getSize());  // ignoring now as we're phasing out sizing in ImgLibImageStack
	}
	
	@Test
	public void testImgLibImageStackWidthHeightSize()
	{
		// shouldn't need to test bizarre behavior.
		//try {
		//	tryConsWHI(-1,-1,-1);
		//	fail();
		//} catch (NegativeArraySizeException e)
		//{
		//	assertTrue(true);
		//}

		tryConsWHI(0,0,0);
		tryConsWHI(1,1,1);
		tryConsWHI(1,10,5);
		tryConsWHI(10,1,5);
		tryConsWHI(257,257,257);
		tryConsWHI(65537,65537,65537);
	}

	private void tryConsWHC(int width, int height, ColorModel cm)
	{
		is = new ImgLibImageStack(width,height,cm);
		assertNotNull(is);
		assertEquals(0,is.getSize());
		assertEquals(cm,is.getColorModel());
		assertEquals(width,is.getWidth());
		assertEquals(height,is.getHeight());
	}
	
	@Test
	public void testImgLibImageStackWidthHeightColormodel()
	{
		ColorModel cm = new DirectColorModel(32,0x00ff0000,0x0000ff00,0x000000ff,0xff000000);
		
		tryConsWHC(-1,-1,null);
		tryConsWHC(-1,-1,cm);
		tryConsWHC(0,0,null);
		tryConsWHC(0,0,cm);
		tryConsWHC(1,1,null);
		tryConsWHC(1,1,cm);
		tryConsWHC(1,10,null);
		tryConsWHC(1,10,cm);
		tryConsWHC(10,1,null);
		tryConsWHC(10,1,cm);
		tryConsWHC(257,257,null);
		tryConsWHC(257,257,cm);
	}

	@Test
	public void testAddSliceLabelPixels()
	{
		// null pixels passed in should throw exception
		
		is = new ImgLibImageStack(0,0);
		
		try {
			is.addSlice("Fred", (Object)null);  // null pixels
			fail();
		} catch (IllegalArgumentException e) {
			assertTrue(true);
		}

		// passing in data other than an array of numbers should throw an exception
		
		is = new ImgLibImageStack(0,0);

		try {
			is.addSlice("Martha", "Jones");  // "Jones" != an array of numbers
			fail();
		} catch (IllegalArgumentException e) {
			assertTrue(true);
		}
		
		// do one that does not cause a stack expansion
		
		is = new ImgLibImageStack(1,5);
		is.addSlice("Fake pixels", new byte[] {1,2,3,4,5});
		assertEquals(1,is.getSize());
		
		// now do one that should cause a stack expansion
		is = new ImgLibImageStack(1,5);
		for (int i = 0; i < 256; i++)
			is.addSlice(("Fake pixels "+i), new byte[] {1,2,3,4,5});

		assertEquals(256,is.getSize());
	}

	@Test
	public void testAddSliceLabelProcessor()
	{
		ByteProcessor ip;
		
		// stack width != processor width should throw an exception
		is = new ImgLibImageStack(10,10);
		ip = new ByteProcessor(10,1);
		try {
			is.addSlice("Failure 1", ip);
			fail();
		} catch (IllegalArgumentException e) {
			assertTrue(true);
		}

		// stack height != processor height should throw an exception
		is = new ImgLibImageStack(10,10);
		ip = new ByteProcessor(1,10);
		try {
			is.addSlice("Failure 2", ip);
			fail();
		} catch (IllegalArgumentException e) {
			assertTrue(true);
		}

		// otherwise it should work
		is = new ImgLibImageStack(10,10);
		ip = new ByteProcessor(10,10);
		is.addSlice("Uno",ip);
		
		assertEquals(1,is.getSize());
	}

	@Test
	public void testAddSliceLabelProcessorNumber()
	{
		ByteProcessor ip;
		
		// if n < 0 should throw an exception

		is = new ImgLibImageStack(3,2);
		ip = new ByteProcessor(3,2);
		
		try {
			is.addSlice("Kerpow!",ip,-1);
			fail();
		} catch (IllegalArgumentException e)
		{
			assertTrue(true);
		}

		// if n > nSlices should throw an exception

		is = new ImgLibImageStack(3,2);
		ip = new ByteProcessor(3,2);
		
		try {
			is.addSlice("Kablooey!",ip,1);
			fail();
		} catch (IllegalArgumentException e)
		{
			assertTrue(true);
		}
		
		// otherwise it should succeed

		is = new ImgLibImageStack(3,2);
		ip = new ByteProcessor(3,2);
		
		is.addSlice("Yeehah!",ip,0);
		assertEquals(1,is.getSize());
	}

	@Test
	public void testDeleteSlice()
	{
		// deleting slice < 1 should throw exception
		is = new ImgLibImageStack(0,0);
		try {
			is.deleteSlice(0);
			fail();
		} catch (IllegalArgumentException e) {
			assertTrue(true);
		}
		
		// deleting slice > nslices should throw exception
		is = new ImgLibImageStack(2,2);
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

		is = new ImgLibImageStack(2,2);
		is.addSlice("Succeed please", (Object)a);
		is.deleteSlice(1);
		assertEquals(0,is.getSize());
		
		// delete the first slice
		is = new ImgLibImageStack(2,2);
		is.addSlice("By golly", (Object)a);
		is.addSlice("Geewilikers", (Object)b);
		is.addSlice("Gosh darn", (Object)c);
		is.deleteSlice(1);
		assertEquals(2,is.getSize());
		assertEquals(b,is.getPixels(1));
		assertEquals(c,is.getPixels(2));
		assertArrayEquals(b,(byte[])is.getPixels(1));
		assertArrayEquals(c,(byte[])is.getPixels(2));
		
		// delete the last slice
		is = new ImgLibImageStack(2,2);
		is.addSlice("By golly", (Object)a);
		is.addSlice("Geewilikers", (Object)b);
		is.addSlice("Gosh darn", (Object)c);
		is.deleteSlice(3);
		assertEquals(2,is.getSize());
		assertEquals(a,is.getPixels(1));
		assertEquals(b,is.getPixels(2));
		assertArrayEquals(a,(byte[])is.getPixels(1));
		assertArrayEquals(b,(byte[])is.getPixels(2));
		
		// delete a middle slice
		is = new ImgLibImageStack(2,2);
		is.addSlice("By golly", (Object)a);
		is.addSlice("Geewilikers", (Object)b);
		is.addSlice("Gosh darn", (Object)c);
		is.deleteSlice(2);
		assertEquals(2,is.getSize());
		assertEquals(a,is.getPixels(1));
		assertEquals(c,is.getPixels(2));
		assertArrayEquals(a,(byte[])is.getPixels(1));
		assertArrayEquals(c,(byte[])is.getPixels(2));
	}

	@Test
	public void testDeleteLastSlice()
	{
		int Total = 512;
		
		is = new ImgLibImageStack(1,7);
		
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
		is = new ImgLibImageStack(0,0);
		assertEquals(0,is.getWidth());
		
		is = new ImgLibImageStack(-1,-2);
		assertEquals(-1,is.getWidth());

		is = new ImgLibImageStack(14,44);
		assertEquals(14,is.getWidth());
	}

	@Test
    public void testGetHeight()
	{
		is = new ImgLibImageStack(0,0);
		assertEquals(0,is.getHeight());
		
		is = new ImgLibImageStack(-1,-2);
		assertEquals(-2,is.getHeight());

		is = new ImgLibImageStack(14,44);
		assertEquals(44,is.getHeight());
	}

	@Test
	public void testSetAndGetRoi()
	{
		is = new ImgLibImageStack(0,0);
		assertEquals(new Rectangle(0,0,0,0),is.getRoi());

		is = new ImgLibImageStack(30,63);
		is.setRoi(null);
		assertEquals(new Rectangle(0,0,30,63),is.getRoi());

		is = new ImgLibImageStack(99,8);
		is.setRoi(new Rectangle(0,0,400,172));
		assertEquals(new Rectangle(0,0,400,172),is.getRoi());
	}

	@Test
	public void testUpdate()
	{
		// can't test cTable as its private
			// except via the getProcessor() test below
		
		// can't really test min and max as they're private 
			// except via the getProcessor() test below
		
		// can test cm via is.getColorModel();
		
		is = new ImgLibImageStack(0,0);
		assertNull(is.getColorModel());
		is.update(null);
		assertNull(is.getColorModel());

		ColorModel cm = new DirectColorModel(8,224,28,3);
		is = new ImgLibImageStack(40,50,cm);
		is.update(null);
		assertEquals(cm,is.getColorModel());
		
		ImageProcessor ip = new ByteProcessor(2,2,new byte[] {0,10,20,30},cm);
		is = new ImgLibImageStack(2,2);
		assertNull(is.getColorModel());
		is.update(ip);
		assertEquals(cm,is.getColorModel());
	}

	@Test
	public void testGetPixels()
	{
		// get the pixels associated with one entry in the stack
		
		is = new ImgLibImageStack(2,2);
		byte[] a = new byte[] {1,2,3,4};
		byte[] b = new byte[] {4,3,2,1};
		byte[] c = new byte[] {5,6,7,8};
		byte[] d = new byte[] {8,7,6,5};

		is.addSlice("a",a);
		is.addSlice("b",b);
		is.addSlice("c",c);
		is.addSlice("d",d);

		// if entry number < 1 should throw an exception
		try {
			is.getPixels(0);
			fail();
		} catch (IllegalArgumentException e)
		{
			assertTrue(true);
		}
		
		// if entry number > last should throw an exception
		try {
			is.getPixels(5);
			fail();
		} catch (IllegalArgumentException e)
		{
			assertTrue(true);
		}
		
		// otherwise we should get the nth stack entry
		assertEquals(a,is.getPixels(1));
		assertEquals(b,is.getPixels(2));
		assertEquals(c,is.getPixels(3));
		assertEquals(d,is.getPixels(4));
		// for now just test content equality
		assertArrayEquals(a,(byte[])is.getPixels(1));
		assertArrayEquals(b,(byte[])is.getPixels(2));
		assertArrayEquals(c,(byte[])is.getPixels(3));
		assertArrayEquals(d,(byte[])is.getPixels(4));
	}

	@Test
	public void testSetPixels()
	{
		// set the pixels associated with one entry in the stack
		
		is = new ImgLibImageStack(2,2);
		byte[] a = new byte[] {1,2,3,4};
		byte[] b = new byte[] {4,3,2,1};
		byte[] c = new byte[] {5,6,7,8};
		byte[] d = new byte[] {8,7,6,5};

		is.addSlice("a",a);
		is.addSlice("b",b);
		is.addSlice("c",c);
		is.addSlice("d",d);

		// if entry number < 1 should throw an exception
		try {
			is.setPixels(new byte[] {9,9,9,9},0);
			fail();
		} catch (IllegalArgumentException e)
		{
			assertTrue(true);
		}
		
		// if entry number > last should throw an exception
		try {
			is.setPixels(new byte[] {9,9,9,9},5);
			fail();
		} catch (IllegalArgumentException e)
		{
			assertTrue(true);
		}
		
		// otherwise we should set the nth stack entry
		byte[] tmp = new byte[] {1,1,1,1};
		is.setPixels(tmp,1);
		assertEquals(tmp,is.getPixels(1));
		assertArrayEquals(tmp,(byte[])is.getPixels(1));
		
		tmp = new byte[] {2,2,2,2};
		is.setPixels(tmp,2);
		assertEquals(tmp,is.getPixels(2));
		assertArrayEquals(tmp,(byte[])is.getPixels(2));
		
		tmp = new byte[] {3,3,3,3};
		is.setPixels(tmp,3);
		assertEquals(tmp,is.getPixels(3));
		assertArrayEquals(tmp,(byte[])is.getPixels(3));
		
		tmp = new byte[] {4,4,4,4};
		is.setPixels(tmp,4);
		assertEquals(tmp,is.getPixels(4));
		assertArrayEquals(tmp,(byte[])is.getPixels(4));
	}

	@Test
	public void testGetImageArray()
	{
		// this method is just a getter
		//   no real test but exercise so that a compile time check exists for the method

		is = new ImgLibImageStack(0,0);
		assertNull(is.getImageArray());
	}

	@Test
	public void testGetSize()
	{
		// tested in many other methods
	}

	@Test
	public void testGetSliceLabels()
	{
		// if 0 slices it should return null
		is = new ImgLibImageStack(0,0);
		assertNull(is.getSliceLabels());
		
		// otherwise it should return label list
		is = new ImgLibImageStack(4,4,null);
		is.addSlice("SuperFred", new byte[] {1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16});
		assertNotNull(is.getSliceLabels());
	}

	@Test
	public void testGetSliceLabel()
	{
		is = new ImgLibImageStack(2,2);
		byte[] a = new byte[] {1,2,3,4};
		byte[] b = new byte[] {4,3,2,1};
		byte[] c = new byte[] {5,6,7,8};
		byte[] d = new byte[] {8,7,6,5};

		is.addSlice("a",a);
		is.addSlice("b",b);
		is.addSlice("c",c);
		is.addSlice("d",d);

		// if entry number < 1 should throw an exception
		try {
			is.getSliceLabel(0);
			fail();
		} catch (IllegalArgumentException e)
		{
			assertTrue(true);
		}
		
		// if entry number > last should throw an exception
		try {
			is.getSliceLabel(5);
			fail();
		} catch (IllegalArgumentException e)
		{
			assertTrue(true);
		}

		// else it should return correct labels
		assertEquals("a",is.getSliceLabel(1));
		assertEquals("b",is.getSliceLabel(2));
		assertEquals("c",is.getSliceLabel(3));
		assertEquals("d",is.getSliceLabel(4));
	}

	@Test
	public void testGetShortSliceLabel()
	{
		is = new ImgLibImageStack(2,2);

		is.addSlice(null,new byte[] {1,2,3,4});
		assertNull(is.getShortSliceLabel(1));
		
		is.addSlice("a",new byte[] {1,2,3,4});
		assertEquals("a",is.getShortSliceLabel(2));

		is.addSlice("\nZippyMan",new byte[] {1,2,3,4});
		assertNull(is.getShortSliceLabel(3));

		is.addSlice("a\nb",new byte[] {1,2,3,4});
		assertEquals("a",is.getShortSliceLabel(4));

		is.addSlice("a.tif",new byte[] {1,2,3,4});
		assertEquals("a",is.getShortSliceLabel(5));

		String sixtyChars = "123456789012345678901234567890123456789012345678901234567890";
		String sixtyOneChars = sixtyChars + "1";
		is.addSlice(sixtyOneChars,new byte[] {1,2,3,4});
		assertEquals(sixtyChars,is.getShortSliceLabel(6));
	}

	@Test
	public void testSetSliceLabel()
	{
		is = new ImgLibImageStack(2,2);
		byte[] a = new byte[] {1,2,3,4};

		is.addSlice("a",a);

		// if entry number < 1 should throw an exception
		try {
			is.setSliceLabel("no chance",0);
			fail();
		} catch (IllegalArgumentException e)
		{
			assertTrue(true);
		}
		
		// if entry number > last should throw an exception
		try {
			is.setSliceLabel("no chance",2);
			fail();
		} catch (IllegalArgumentException e)
		{
			assertTrue(true);
		}

		// else it should return correct labels
		is.setSliceLabel("zamblooey",1);
		
		assertEquals("zamblooey",is.getSliceLabel(1));
	}

	@Test
	public void testGetProcessor()
	{
		is = new ImgLibImageStack(2,2);
        is.addSlice("a", new byte[] {1,2,3,4});
        
		// if entry number < 1 should throw an exception
		try {
			is.getProcessor(0);
			fail();
		} catch (IllegalArgumentException e)
		{
			assertTrue(true);
		}
		
		// if entry number > last should throw an exception
		try {
			is.getProcessor(2);
			fail();
		} catch (IllegalArgumentException e)
		{
			assertTrue(true);
		}
		
		// otherwise returns a new ImageProcessor of the correct type

		// not happy with null pixels
		is = new ImgLibImageStack(2,2);
		is.addSlice("a", new byte[] {1,2,3,4});
		try {
			is.setPixels(null, 1);
			fail();
		} catch (IllegalArgumentException e)
		{
			assertTrue(true);
		}
		
		Object pixels;
        ImageProcessor ip;

		// byte[]
		is = new ImgLibImageStack(2,2);
		pixels = new byte[] {1,2,3,4};
		is.addSlice("a", pixels);
		ip = is.getProcessor(1); 
		assertEquals(pixels,ip.getPixels());
		assertArrayEquals((byte[])pixels,(byte[])ip.getPixels());
		
		// short[]
		is = new ImgLibImageStack(2,2);
		pixels = new short[] {1,2,3,4};
		is.addSlice("a",pixels);
		ip = is.getProcessor(1); 
		assertEquals(pixels,ip.getPixels());
		assertArrayEquals((short[])pixels,(short[])ip.getPixels());

		// int[]
		is = new ImgLibImageStack(2,2);
		pixels = new int[] {1,2,3,4};
		is.addSlice("a",pixels);
		ip = is.getProcessor(1); 
		assertEquals(pixels,ip.getPixels());
		assertArrayEquals((int[])pixels,(int[])ip.getPixels());

		// float[]
		is = new ImgLibImageStack(2,2);
		pixels = new float[] {1,2,3,4};
		is.addSlice("a",pixels);
		ip = is.getProcessor(1); 
		assertEquals(pixels,ip.getPixels());
		float[] myPixels = (float[]) pixels;
		float[] ipsPixels = (float[]) ip.getPixels();
		assertEquals(myPixels.length, ipsPixels.length);
		for (int i = 0; i < myPixels.length; i++)
			assertEquals(myPixels[i], ipsPixels[i], 0);
		
		// There are a couple side effects of getProcessor() on the processor returned that are not tested.
		//   ip.setMinAndMax() sometimes called
		//   ip.setCalibration() sometimes called
	}

	@Test
	public void testSetAndGetColorModel()
	{
		ColorModel cm = new DirectColorModel(24,0xff0000,0x00ff00,0x0000ff);
		
		is = new ImgLibImageStack(2,2,null);
		assertNull(is.getColorModel());
		
		is.setColorModel(cm);
		assertEquals(cm,is.getColorModel());
	}

	@Test
	public void testIsRGB()
	{
		is = new ImgLibImageStack(2,2);
		assertFalse(is.isRGB());

		is = new ImgLibImageStack(2,2);
		is.addSlice(null, new short[] {1,2,3,4});
		is.addSlice(null, new short[] {1,2,3,4});
		is.addSlice(null, new short[] {1,2,3,4});
		assertFalse(is.isRGB());

		is = new ImgLibImageStack(2,2);
		is.addSlice("a", new byte[] {1,2,3,4});
		is.addSlice("b", new byte[] {1,2,3,4});
		is.addSlice("c", new byte[] {1,2,3,4});
		assertFalse(is.isRGB());

		is = new ImgLibImageStack(2,2);
		is.addSlice("Red", new byte[] {1,2,3,4});
		is.addSlice("Anything", new byte[] {1,2,3,4});
		is.addSlice("Something", new byte[] {1,2,3,4});
		assertTrue(is.isRGB());
	}

	@Test
	public void testIsHSB()
	{
		is = new ImgLibImageStack(2,2);
		assertFalse(is.isHSB());

		is = new ImgLibImageStack(2,2);
		is.addSlice(null, new short[] {1,2,3,4});
		is.addSlice(null, new short[] {1,2,3,4});
		is.addSlice(null, new short[] {1,2,3,4});
		assertFalse(is.isHSB());

		is = new ImgLibImageStack(2,2);
		is.addSlice("a", new byte[] {1,2,3,4});
		is.addSlice("b", new byte[] {1,2,3,4});
		is.addSlice("c", new byte[] {1,2,3,4});
		assertFalse(is.isHSB());

		is = new ImgLibImageStack(2,2);
		is.addSlice("Hue", new byte[] {1,2,3,4});
		is.addSlice("Anything", new byte[] {1,2,3,4});
		is.addSlice("Something", new byte[] {1,2,3,4});
		assertTrue(is.isHSB());
	}

	@Test
	public void testIsVirtual()
	{
		is = new ImgLibImageStack(2,2);
		assertFalse(is.isVirtual());
	}

	@Test
	public void testReset()
	{
		is = new ImgLibImageStack(2,2);
		is.addSlice("scorch", new byte[]{1,2,3,4});
		is.addSlice("horch", new byte[]{1,2,3,4});
		is.addSlice("torch", new byte[]{1,2,3,4});
		is.addSlice("lorch", new byte[]{1,2,3,4});
		assertEquals(4,is.getSize());
		
		is.reset();
		
		assertEquals(0,is.getSize());
		assertNull(is.getSliceLabels());
	}

	
	// NOTE: I don't know if I need to enforce the same trim algo or not ...
	// For now assume I do ...

	private void tryTest(int howMany)
	{
		int numToBeDeleted;
		
		is = new ImgLibImageStack(2,2);
		for (int i = 0; i < howMany; i++)
			is.addSlice(""+(i+1), new byte[] {1,2,3,4});
		// same trim algo as IJ - this test may need to be removed
		numToBeDeleted = (int)Math.round(Math.log(is.getSize())+1.0);
		is.trim();
		assertEquals(howMany-numToBeDeleted,is.getSize());
		assertEquals(""+(howMany-numToBeDeleted),is.getSliceLabel(is.getSize()));
	}
	
	@Test
	public void testTrim()
	{
		tryTest(10);
		tryTest(100);
		tryTest(1000);
		tryTest(10000);
	}

	@Test
	public void testToString()
	{
		is = new ImgLibImageStack(0,0);
		assertEquals("stack[0x0x0]",is.toString());
		
		is = new ImgLibImageStack(1,4);
		is.addSlice("a",new byte[] {1,2,3,4});
		is.addSlice("b",new byte[] {1,2,3,4});
		is.addSlice("c",new byte[] {1,2,3,4});
		
		assertEquals("stack[1x4x3]",is.toString());
	}
}
