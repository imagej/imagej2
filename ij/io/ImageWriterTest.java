package ij.io;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.awt.image.ColorModel;
import java.io.IOException;
import java.io.ByteArrayOutputStream;

import ij.VirtualStack;
import ij.io.Assert;

/*
	NOTES
		fi.nImages == 1 implies no imageStack : its not possible to have a stack of 1 items.
		Therefore must make sure you don't pass a "stack" of 1 thing to the tryX() routines as ImageWriter will
		assume the data is not stacked and will fail to cast the pixels correctly. This requirement enforced
		by making sure Images.ImageSets contain sets with more than one image in them.
	        
	TODO
		virtual stacks - will need to collect some data first and then implement tests
 */

public class ImageWriterTest {

	final ByteOrder.Value[] ByteOrders = new ByteOrder.Value[] {ByteOrder.Value.DEFAULT,ByteOrder.Value.INTEL};

	//  *******    Helper methods  ****************************
	
	private void setFileInfo(FileInfo fi, int fileType, int height, int width, boolean intel, int nImages,
			VirtualStack vStack, Object pixels)
	{
		fi.intelByteOrder = intel;
		fi.fileType = fileType;
		fi.nImages = nImages;
		fi.virtualStack = vStack;
		fi.height = height;
		fi.width = width;
		fi.pixels = pixels;
	}

	private byte[] writeData(FileInfo fi, boolean expectException)
	{
		try {
			
			ByteArrayOutputStream stream = new ByteArrayOutputStream();
			ImageWriter writer = new ImageWriter(fi);
			writer.write(stream);
			return stream.toByteArray();
			
		} catch (Exception e)
		{
			if (expectException)
				assertTrue(true);
			else
				fail(e.getMessage());
		}
		return null;
	}

	byte[] concat(byte[] a, byte[] b)
	{
		byte[] output = new byte[a.length + b.length];
		
		System.arraycopy(a, 0, output, 0, a.length);
		System.arraycopy(b, 0, output, a.length, b.length);
		
		return output;
	}
	
	short[] concat(short[] a, short[] b)
	{
		short[] output = new short[a.length + b.length];
		
		System.arraycopy(a, 0, output, 0, a.length);
		System.arraycopy(b, 0, output, a.length, b.length);
		
		return output;
	}
	
	int[] concat(int[] a, int[] b)
	{
		int[] output = new int[a.length + b.length];
		
		System.arraycopy(a, 0, output, 0, a.length);
		System.arraycopy(b, 0, output, a.length, b.length);
		
		return output;
	}
	
	float[] concat(float[] a, float[] b)
	{
		float[] output = new float[a.length + b.length];
		
		System.arraycopy(a, 0, output, 0, a.length);
		System.arraycopy(b, 0, output, a.length, b.length);
		
		return output;
	}

	//  *******    Tests     *************************************************

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
	public void testConstructor() {
		FileInfo fi = new FileInfo();
		assertNotNull(new ImageWriter(fi));
	}

	private void tryNoPixAndNoVirtStack()
	{
		FileInfo fi = new FileInfo();
		fi.pixels = null;
		fi.virtualStack = null;
		
		writeData(fi, true);	
	}

	private void tryBadPix()
	{
		FileInfo fi = new FileInfo();
		fi.nImages = 2;
		fi.virtualStack = null;
		fi.pixels = "Any Object not instanceof Object[]";
		writeData(fi, true);
	}
	
	// TODO - write this
	private void tryColor8BitVirtualStack() {
		// if (fi.nImages>1 && fi.virtualStack!=null)
		FileInfo fi = new FileInfo();
		fi.intelByteOrder = false;
		fi.fileType = FileInfo.COLOR8;
		fi.nImages = 2;
		fi.virtualStack = new VirtualStack();
	}
	
	private void tryColor8BitStack() {
		for (long[][][] imageSet : Images.ImageSets)
		{
			for (ByteOrder.Value order : ByteOrders)
			{
				PixelFormat format = new Color8Format();
				byte[] expectedAfterWrite = new byte[] {};
				byte[][] imageStack = new byte[imageSet.length][];
				for (int i = 0; i < imageSet.length; i++)
				{
					byte[] expected = (byte[])(format.expectedResults(imageSet[i])); 
					imageStack[i] = expected;
					expectedAfterWrite = concat(expectedAfterWrite,expected);
				}
				
				FileInfo fi = new FileInfo();
				setFileInfo(fi, FileInfo.COLOR8, imageSet[0].length, imageSet[0][0].length, (order == ByteOrder.Value.INTEL),
							imageSet.length, null, imageStack);
	
				byte[] bytes = writeData(fi, false);
				
				byte[] actual = (byte[])format.pixelsFromBytes(bytes, order);
				
				assertArrayEquals(expectedAfterWrite,actual);
			}
		}
	}
	
	private void tryColor8BitImage() {
		for (long[][] image : Images.Images)
		{
			for (ByteOrder.Value order : ByteOrders)
			{
				PixelFormat format = new Color8Format();
				byte[] expected = (byte[])(format.expectedResults(image)); 
				
				FileInfo fi = new FileInfo();
				setFileInfo(fi, FileInfo.COLOR8, image.length, image[0].length, (order == ByteOrder.Value.INTEL),
							1, null, expected);
	
				byte[] bytes = writeData(fi, false);
				
				byte[] actual = (byte[])format.pixelsFromBytes(bytes, order);
				
				assertArrayEquals(expected,actual);
			}
		}
	}
	
	private void tryColor8() {
		tryColor8BitVirtualStack();
		tryColor8BitStack();
		tryColor8BitImage();
	}
	
	// TODO - write this
	private void tryGray8BitVirtualStack() {
		// if (fi.nImages>1 && fi.virtualStack!=null)
		// sub-case:	write8BitVirtualStack(out, fi.virtualStack);
		FileInfo fi = new FileInfo();
		fi.intelByteOrder = false;
		fi.fileType = FileInfo.GRAY8;
	}
	
	private void tryGray8BitStack() {
		for (long[][][] imageSet : Images.ImageSets)
		{
			for (ByteOrder.Value order : ByteOrders)
			{
				PixelFormat format = new Gray8Format();
				byte[] expectedAfterWrite = new byte[] {};
				byte[][] imageStack = new byte[imageSet.length][];
				for (int i = 0; i < imageSet.length; i++)
				{
					byte[] expected = (byte[])(format.expectedResults(imageSet[i])); 
					imageStack[i] = expected;
					expectedAfterWrite = concat(expectedAfterWrite,expected);
				}
				
				FileInfo fi = new FileInfo();
				setFileInfo(fi, FileInfo.GRAY8, imageSet[0].length, imageSet[0][0].length, (order == ByteOrder.Value.INTEL),
						imageSet.length, null, imageStack);
	
				byte[] bytes = writeData(fi, false);
				
				byte[] actual = (byte[])format.pixelsFromBytes(bytes, order);
				
				assertArrayEquals(expectedAfterWrite,actual);
			}
		}
	}
	
	private void tryGray8BitImage() {
		for (long[][] image : Images.Images)
		{
			for (ByteOrder.Value order : ByteOrders)
			{
				PixelFormat format = new Gray8Format();
				byte[] expected = (byte[])(format.expectedResults(image)); 
				
				FileInfo fi = new FileInfo();
				setFileInfo(fi, FileInfo.COLOR8, image.length, image[0].length, (order == ByteOrder.Value.INTEL),
							1, null, expected);

				byte[] bytes = writeData(fi, false);
				
				byte[] actual = (byte[])format.pixelsFromBytes(bytes, order);
				
				assertArrayEquals(expected,actual);
			}
		}
	}
	
	private void tryGray8() {
		tryGray8BitVirtualStack();
		tryGray8BitStack();
		tryGray8BitImage();
	}
	
	// TODO - write this
	private void tryGray16SignedVirtualStack() {

		VirtualStack vStack = new VirtualStack(5, 1, (ColorModel) null, "data/");
		
		vStack.addSlice("gray16signedA.tif");
		vStack.addSlice("gray16signedB.tif");
		
		assertNotNull(vStack.getProcessor(1));

		PixelFormat format = new Gray16SignedFormat();
		
		FileInfo fi = new FileInfo();
		setFileInfo(fi, FileInfo.GRAY16_SIGNED, 1, 5, false, 2, vStack, null);

		byte[] bytes = writeData(fi, false);
		
		short[] actual = (short[])format.pixelsFromBytes(bytes, ByteOrder.Value.DEFAULT);
		
		//assertArrayEquals(new byte[] {-128,1,-128,2,-128,3,-128,4,-128,5,-128,1,-128,2,-128,3,-128,4,-128,5},bytes);
		//assertArrayEquals(new short[] {1,2,3,4,5,1,2,3,4,5},actual);  // fails
		assertArrayEquals(new short[] {-32767,-32766,-32765,-32764,-32763,-32767,-32766,-32765,-32764,-32763},actual);
	}
	
	private void tryGray16SignedStack() {
		for (long[][][] imageSet : Images.ImageSets)
		{
			for (ByteOrder.Value order : ByteOrders)
			{
				PixelFormat format = new Gray16SignedFormat();
				short[] expectedAfterWrite = new short[] {};
				short[][] imageStack = new short[imageSet.length][];
				for (int i = 0; i < imageSet.length; i++)
				{
					short[] expected = (short[])(format.expectedResults(imageSet[i])); 
					imageStack[i] = expected;
					expectedAfterWrite = concat(expectedAfterWrite,expected);
				}
				
				FileInfo fi = new FileInfo();
				setFileInfo(fi, FileInfo.GRAY16_SIGNED, imageSet[0].length, imageSet[0][0].length, (order == ByteOrder.Value.INTEL),
						imageSet.length, null, imageStack);
	
				byte[] bytes = writeData(fi, false);
				
				short[] actual = (short[])format.pixelsFromBytes(bytes, order);
				
				assertArrayEquals(expectedAfterWrite,actual);
			}
		}
	}
	
	private void tryGray16SignedImage() {
		for (long[][] image : Images.Images)
		{
			for (ByteOrder.Value order : ByteOrders)
			{
				PixelFormat format = new Gray16SignedFormat();
				short[] expected = (short[])(format.expectedResults(image)); 
				
				FileInfo fi = new FileInfo();
				setFileInfo(fi, FileInfo.GRAY16_SIGNED, image.length, image[0].length, (order == ByteOrder.Value.INTEL),
							1, null, expected);
	
				byte[] bytes = writeData(fi, false);
				
				short[] actual = (short[])format.pixelsFromBytes(bytes, order);

				assertArrayEquals(expected,actual);
			}
		}
	}
	
	private void tryGray16Signed() {
		tryGray16SignedVirtualStack();
		tryGray16SignedStack();
		tryGray16SignedImage();
	}
	
	// TODO - write this - not finished - copied from above - needs work
	private void tryGray16UnsignedVirtualStack() {

		VirtualStack vStack = new VirtualStack(5, 1, (ColorModel) null, "data/");
		
		vStack.addSlice("gray16signedA.tif");
		vStack.addSlice("gray16signedB.tif");
		
		assertNotNull(vStack.getProcessor(1));

		PixelFormat format = new Gray16UnsignedFormat();
		
		FileInfo fi = new FileInfo();
		setFileInfo(fi, FileInfo.GRAY16_UNSIGNED, 1, 5, false, 2, vStack, null);

		byte[] bytes = writeData(fi, false);
		
		short[] actual = (short[])format.pixelsFromBytes(bytes, ByteOrder.Value.DEFAULT);
		
		// assertArrayEquals(new byte[] {-128,1,-128,2,-128,3,-128,4,-128,5,-128,1,-128,2,-128,3,-128,4,-128,5},bytes);
		assertArrayEquals(new short[] {-32767,-32766,-32765,-32764,-32763,-32767,-32766,-32765,-32764,-32763},actual);
	}
	
	private void tryGray16UnsignedStack() {
		for (long[][][] imageSet : Images.ImageSets)
		{
			for (ByteOrder.Value order : ByteOrders)
			{
				PixelFormat format = new Gray16UnsignedFormat();
				short[] expectedAfterWrite = new short[] {};
				short[][] imageStack = new short[imageSet.length][];
				for (int i = 0; i < imageSet.length; i++)
				{
					short[] expected = (short[])(format.expectedResults(imageSet[i])); 
					imageStack[i] = expected;
					expectedAfterWrite = concat(expectedAfterWrite,expected);
				}
				
				FileInfo fi = new FileInfo();
				setFileInfo(fi, FileInfo.GRAY16_UNSIGNED, imageSet[0].length, imageSet[0][0].length, (order == ByteOrder.Value.INTEL),
						imageSet.length, null, imageStack);
	
				byte[] bytes = writeData(fi, false);
				
				short[] actual = (short[])format.pixelsFromBytes(bytes, order);
				
				assertArrayEquals(expectedAfterWrite,actual);
			}
		}
	}
	
	private void tryGray16UnsignedImage() {
		for (long[][] image : Images.Images)
		{
			for (ByteOrder.Value order : ByteOrders)
			{
				PixelFormat format = new Gray16UnsignedFormat();
				short[] expected = (short[])(format.expectedResults(image)); 
				
				FileInfo fi = new FileInfo();
				setFileInfo(fi, FileInfo.GRAY16_UNSIGNED, image.length, image[0].length, (order == ByteOrder.Value.INTEL),
							1, null, expected);
	
				byte[] bytes = writeData(fi, false);
				
				short[] actual = (short[])format.pixelsFromBytes(bytes, order);

				assertArrayEquals(expected,actual);
			}
		}
	}
	
	private void tryGray16Unsigned() {
		tryGray16UnsignedVirtualStack();
		tryGray16UnsignedStack();
		tryGray16UnsignedImage();
	}
	
	// TODO - write this
	private void tryRgbVirtualStack() {
		// if (fi.nImages>1 && fi.virtualStack!=null)
		// sub-case: writeRGBVirtualStack(out, fi.virtualStack);
		FileInfo fi = new FileInfo();
		fi.fileType = FileInfo.RGB;
	}
	
	private void tryRgbStack() {
		for (long[][][] imageSet : Images.ImageSets)
		{
			for (ByteOrder.Value order : ByteOrders)
			{
				PixelFormat format = new RgbFormat();
				int[] expectedAfterWrite = new int[] {};
				int[][] imageStack = new int[imageSet.length][];
				for (int i = 0; i < imageSet.length; i++)
				{
					int[] expected = (int[])(format.expectedResults(imageSet[i])); 
					imageStack[i] = expected;
					expectedAfterWrite = concat(expectedAfterWrite,expected);
				}
				
				FileInfo fi = new FileInfo();
				setFileInfo(fi, FileInfo.RGB, imageSet[0].length, imageSet[0][0].length, (order == ByteOrder.Value.INTEL),
						imageSet.length, null, imageStack);
	
				byte[] bytes = writeData(fi, false);
				
				int[] actual = (int[])format.pixelsFromBytes(bytes, order);
				
				assertArrayEquals(expectedAfterWrite,actual);
			}
		}
	}
	
	private void tryRgbImage() {
		for (long[][] image : Images.Images)
		{
			for (ByteOrder.Value order : ByteOrders)
			{
				PixelFormat format = new RgbFormat();
				int[] expected = (int[])(format.expectedResults(image)); 
				
				FileInfo fi = new FileInfo();
				setFileInfo(fi, FileInfo.RGB, image.length, image[0].length, (order == ByteOrder.Value.INTEL),
							1, null, expected);
	
				byte[] bytes = writeData(fi, false);
				
				int[] actual = (int[])format.pixelsFromBytes(bytes, order);
				
				assertArrayEquals(expected,actual);
			}
		}
	}
	
	private void tryRgb() {
		tryRgbVirtualStack();
		tryRgbStack();
		tryRgbImage();
	}
	
	// TODO - write this
	private void tryGray32FloatVirtualStack() {
		// if (fi.nImages>1 && fi.virtualStack!=null)
		// sub-case: writeFloatVirtualStack(out, fi.virtualStack);
		FileInfo fi = new FileInfo();
		fi.intelByteOrder = false;
		fi.fileType = FileInfo.GRAY32_FLOAT;
	}
	
	private void tryGray32FloatStack() {
		for (long[][][] imageSet : Images.ImageSets)
		{
			for (ByteOrder.Value order : ByteOrders)
			{
				PixelFormat format = new Gray32FloatFormat();
				float[] expectedAfterWrite = new float[] {};
				float[][] imageStack = new float[imageSet.length][];
				for (int i = 0; i < imageSet.length; i++)
				{
					float[] expected = (float[])(format.expectedResults(imageSet[i])); 
					imageStack[i] = expected;
					expectedAfterWrite = concat(expectedAfterWrite,expected);
				}
				
				FileInfo fi = new FileInfo();
				setFileInfo(fi, FileInfo.GRAY32_FLOAT, imageSet[0].length, imageSet[0][0].length, (order == ByteOrder.Value.INTEL),
						imageSet.length, null, imageStack);
	
				byte[] bytes = writeData(fi, false);
				
				float[] actual = (float[])format.pixelsFromBytes(bytes, order);
				
				Assert.assertFloatArraysEqual(expectedAfterWrite,actual);
			}
		}
	}
	
	private void tryGray32FloatImage() {
		for (long[][] image : Images.Images)
		{
			for (ByteOrder.Value order : ByteOrders)
			{
				PixelFormat format = new Gray32FloatFormat();
				float[] expected = (float[])(format.expectedResults(image)); 
				
				FileInfo fi = new FileInfo();
				setFileInfo(fi, FileInfo.GRAY32_FLOAT, image.length, image[0].length, (order == ByteOrder.Value.INTEL),
							1, null, expected);
	
				byte[] bytes = writeData(fi, false);
				
				float[] actual = (float[])format.pixelsFromBytes(bytes, order);
				
				Assert.assertFloatArraysEqual(expected,actual);
			}
		}
	}
	
	private void tryGray32Float() {
		tryGray32FloatVirtualStack();
		tryGray32FloatStack();
		tryGray32FloatImage();
	}
	
	private void tryRgb48Image() {
		for (long[][] image : Images.Images)
		{
			for (ByteOrder.Value order : ByteOrders)
			{
				PixelFormat format = new Rgb48Format();
				short[][] expected = (short[][]) format.expectedResults(image);
	
				FileInfo fi = new FileInfo();
				setFileInfo(fi, FileInfo.RGB48, image.length, image[0].length, (order == ByteOrder.Value.INTEL),
							0, null, expected);  // 0 is intentional
	
				byte[] bytes = writeData(fi, false);
				
				short[][] actual = (short[][])format.pixelsFromBytes(bytes, order);
				
				assertArrayEquals(expected,actual);
			}
		}
	}
	
	private void tryRgb48() {
		tryRgb48Image();
	}

	@Test
	public void testWritePixels() {

		tryNoPixAndNoVirtStack();
		tryBadPix();
		tryColor8();
		tryGray8();
		tryGray16Signed();
		tryGray16Unsigned();
		tryRgb();
		tryGray32Float();
		tryRgb48();
	}
}
