package ij.io;

import static org.junit.Assert.*;

import org.junit.Test;

import java.awt.image.ColorModel;
import java.io.IOException;
import java.io.ByteArrayOutputStream;

import ij.VirtualStack;
import ij.Assert;

/*
	NOTES
		fi.nImages == 1 implies no imageStack : its not possible to have a stack of 1 items.
		Therefore must make sure you don't pass a "stack" of 1 thing to the tryX() routines as ImageWriter will
		assume the data is not stacked and will fail to cast the pixels correctly. This requirement enforced
		by making sure Images.ImageSets contain sets with more than one image in them.
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

	// the test routine everything else calls
	
	private byte[] writeData(FileInfo fi, boolean expectIOException)
	{
		try {
			
			ByteArrayOutputStream stream = new ByteArrayOutputStream();
			ImageWriter writer = new ImageWriter(fi);
			writer.write(stream);
			return stream.toByteArray();
			
		} catch (IOException e){
			if (expectIOException)
				assertTrue(true);
			else
				fail(e.getMessage());
		} catch(Exception e){
			fail(e.getMessage());
		}
		return null;
	}

	// the concats() are used below for creating expected data sets for stacks
	
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

	private void tryNoPixAndNoVirtStack()
	{
		FileInfo fi = new FileInfo();
		fi.pixels = null;
		fi.virtualStack = null;
		
		writeData(fi, true);	// expecting IOException
	}

	private void tryBadPix()
	{
		FileInfo fi = new FileInfo();
		fi.nImages = 2;
		fi.virtualStack = null;
		fi.pixels = "Any Object not instanceof Object[]";  // bad pix
		writeData(fi, true);  // expecting IOException
	}
	
	private void tryColor8BitVirtualStack() {

		PixelFormat format = new Gray8Format();
		
		// try regular
		for (ByteOrder.Value order : ByteOrders)
		{
			VirtualStack vStack = new VirtualStack(3, 2, (ColorModel) null, "data/");
			
			vStack.addSlice("gray8-2x3-sub1.tif");
			vStack.addSlice("gray8-2x3-sub2.tif");
			
			assertNotNull(vStack.getProcessor(1));
	
			FileInfo fi = new FileInfo();
			setFileInfo(fi, FileInfo.COLOR8, 2, 3, (order == ByteOrder.Value.INTEL), 2, vStack, null);
	
			byte[] bytes = writeData(fi, false);
			
			byte[] actual = (byte[])format.pixelsFromBytes(bytes, order);
			
			byte[] expected = new byte[] {0,40,80,(byte)120,(byte)160,(byte)200,
											0,40,80,(byte)120,(byte)160,(byte)200};
			
			assertArrayEquals(expected,actual);
		}
		
		// try with filename hack that flips pixels
		for (ByteOrder.Value order : ByteOrders)
		{
			VirtualStack vStack = new VirtualStack(3, 2, (ColorModel) null, "data/");
			
			vStack.addSlice("gray8-2x3-sub1.tif");
			vStack.addSlice("gray8-2x3-sub2.tif");
			
			assertNotNull(vStack.getProcessor(1));
	
			FileInfo fi = new FileInfo();
			setFileInfo(fi, FileInfo.COLOR8, 2, 3, (order == ByteOrder.Value.INTEL), 2, vStack, null);
			fi.fileName = "FlipTheseImages";
			
			byte[] bytes = writeData(fi, false);
			
			byte[] actual = (byte[])format.pixelsFromBytes(bytes, order);
			
			byte[] expected = new byte[] {120,(byte)160,(byte)200,0,40,80,120,(byte)160,(byte)200,0,40,80};
			
			assertArrayEquals(expected,actual);
		}
	}
	
	private void tryColor8BitStack() {
		PixelFormat format = new Color8Format();
		for (long[][][] imageSet : Images.ImageSets)  // for each test image set we have
		{
			for (ByteOrder.Value order : ByteOrders)  // for all possible byte orders
			{
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
		PixelFormat format = new Color8Format();
		for (long[][] image : Images.Images) // for all test images
		{
			for (ByteOrder.Value order : ByteOrders)  // for all possible byte orders
			{
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
	
	private void tryGray8BitVirtualStack() {

		PixelFormat format = new Gray8Format();
		
		// try regular
		for (ByteOrder.Value order : ByteOrders)  // for all possible byte orders
		{
			VirtualStack vStack = new VirtualStack(3, 2, (ColorModel) null, "data/");
			
			vStack.addSlice("gray8-2x3-sub1.tif");
			vStack.addSlice("gray8-2x3-sub2.tif");
			
			assertNotNull(vStack.getProcessor(1));
	
			FileInfo fi = new FileInfo();
			setFileInfo(fi, FileInfo.GRAY8, 2, 3, (order == ByteOrder.Value.INTEL), 2, vStack, null);
	
			byte[] bytes = writeData(fi, false);
			
			byte[] actual = (byte[])format.pixelsFromBytes(bytes, order);
			
			byte[] expected = new byte[] {0,40,80,(byte)120,(byte)160,(byte)200,
											0,40,80,(byte)120,(byte)160,(byte)200};
			
			assertArrayEquals(expected,actual);
		}
		
		// try with filename hack that flips pixels
		for (ByteOrder.Value order : ByteOrders)  // for all possible byte orders
		{
			VirtualStack vStack = new VirtualStack(3, 2, (ColorModel) null, "data/");
			
			vStack.addSlice("gray8-2x3-sub1.tif");
			vStack.addSlice("gray8-2x3-sub2.tif");
			
			assertNotNull(vStack.getProcessor(1));
	
			FileInfo fi = new FileInfo();
			setFileInfo(fi, FileInfo.GRAY8, 2, 3, (order == ByteOrder.Value.INTEL), 2, vStack, null);
			fi.fileName = "FlipTheseImages";
			
			byte[] bytes = writeData(fi, false);
			
			byte[] actual = (byte[])format.pixelsFromBytes(bytes, order);
			
			byte[] expected = new byte[] {120,(byte)160,(byte)200,0,40,80,120,(byte)160,(byte)200,0,40,80};
			
			assertArrayEquals(expected,actual);
		}
	}
	
	private void tryGray8BitStack() {
		PixelFormat format = new Gray8Format();
		for (long[][][] imageSet : Images.ImageSets)  // for each test image set we have
		{
			for (ByteOrder.Value order : ByteOrders)  // for all possible byte orders
			{
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
		PixelFormat format = new Gray8Format();
		for (long[][] image : Images.Images) // for all test images
		{
			for (ByteOrder.Value order : ByteOrders)  // for all possible byte orders
			{
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
	
	private void tryGray16SignedVirtualStack() {

		PixelFormat format = new Gray16SignedFormat();
		
		// try regular
		for (ByteOrder.Value order : ByteOrders)  // for all possible byte orders
		{
			VirtualStack vStack = new VirtualStack(3, 2, (ColorModel) null, "data/");
			
			vStack.addSlice("gray16-2x3-sub1.tif");
			vStack.addSlice("gray16-2x3-sub2.tif");
			
			assertNotNull(vStack.getProcessor(1));
			
			FileInfo fi = new FileInfo();
			setFileInfo(fi, FileInfo.GRAY16_SIGNED, 2, 3, (order == ByteOrder.Value.INTEL), 2, vStack, null);
	
			byte[] bytes = writeData(fi, false);
			
			short[] actual = (short[])format.pixelsFromBytes(bytes,order);
			
			short[] expected = new short[] {0,10280,20560,30840,-24416,-14136,0,10280,20560,30840,-24416,-14136};
			
			assertArrayEquals(expected,actual);
		}
		
		// try with filename hack that flips pixels
		for (ByteOrder.Value order : ByteOrders)
		{
			VirtualStack vStack = new VirtualStack(3, 2, (ColorModel) null, "data/");
			
			vStack.addSlice("gray16-2x3-sub1.tif");
			vStack.addSlice("gray16-2x3-sub2.tif");
			
			assertNotNull(vStack.getProcessor(1));
			
			FileInfo fi = new FileInfo();
			setFileInfo(fi, FileInfo.GRAY16_SIGNED, 2, 3, (order == ByteOrder.Value.INTEL), 2, vStack, null);
			fi.fileName = "FlipTheseImages";
			
			byte[] bytes = writeData(fi, false);
			
			short[] actual = (short[])format.pixelsFromBytes(bytes,order);
			
			short[] expected = new short[] {30840,-24416,-14136,0,10280,20560,30840,-24416,-14136,0,10280,20560};
			
			assertArrayEquals(expected,actual);
		}
	}
	
	private void tryGray16SignedStack() {
		PixelFormat format = new Gray16SignedFormat();
		for (long[][][] imageSet : Images.ImageSets)  // for each test image set we have
		{
			for (ByteOrder.Value order : ByteOrders)  // for all possible byte orders
			{
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
		PixelFormat format = new Gray16SignedFormat();
		for (long[][] image : Images.Images) // for all test images
		{
			for (ByteOrder.Value order : ByteOrders)  // for all possible byte orders
			{
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
	
	private void tryGray16UnsignedVirtualStack() {

		PixelFormat format = new Gray16UnsignedFormat();
		
		// try regular
		for (ByteOrder.Value order : ByteOrders)  // for all possible byte orders
		{
			VirtualStack vStack = new VirtualStack(3, 2, (ColorModel) null, "data/");
			
			vStack.addSlice("gray16-2x3-sub1.tif");
			vStack.addSlice("gray16-2x3-sub2.tif");
			
			assertNotNull(vStack.getProcessor(1));
			
			FileInfo fi = new FileInfo();
			setFileInfo(fi, FileInfo.GRAY16_UNSIGNED, 2, 3, (order == ByteOrder.Value.INTEL), 2, vStack, null);
	
			byte[] bytes = writeData(fi, false);
			
			short[] actual = (short[])format.pixelsFromBytes(bytes, order);
			
			short[] expected = new short[] {0,10280,20560,30840,-24416,-14136,0,10280,20560,30840,-24416,-14136};
			
			assertArrayEquals(expected,actual);
		}

		// try with filename hack that flips pixels
		for (ByteOrder.Value order : ByteOrders)  // for all possible byte orders
		{
			VirtualStack vStack = new VirtualStack(3, 2, (ColorModel) null, "data/");
			
			vStack.addSlice("gray16-2x3-sub1.tif");
			vStack.addSlice("gray16-2x3-sub2.tif");
			
			assertNotNull(vStack.getProcessor(1));
			
			FileInfo fi = new FileInfo();
			setFileInfo(fi, FileInfo.GRAY16_UNSIGNED, 2, 3, (order == ByteOrder.Value.INTEL), 2, vStack, null);
			fi.fileName = "FlipTheseImages";
	
			byte[] bytes = writeData(fi, false);
			
			short[] actual = (short[])format.pixelsFromBytes(bytes, order);
			
			short[] expected = new short[] {30840,-24416,-14136,0,10280,20560,30840,-24416,-14136,0,10280,20560};
			
			assertArrayEquals(expected,actual);
		}
	}
	
	private void tryGray16UnsignedStack() {
		PixelFormat format = new Gray16UnsignedFormat();
		for (long[][][] imageSet : Images.ImageSets)  // for each test image set we have
		{
			for (ByteOrder.Value order : ByteOrders)  // for all possible byte orders
			{
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
		PixelFormat format = new Gray16UnsignedFormat();
		for (long[][] image : Images.Images) // for all test images
		{
			for (ByteOrder.Value order : ByteOrders)  // for all possible byte orders
			{
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
	
	private void tryRgbVirtualStack() {

		PixelFormat format = new RgbFormat();
		
		// try regular
		for (ByteOrder.Value order : ByteOrders)  // for all possible byte orders
		{
			VirtualStack vStack = new VirtualStack(3, 2, (ColorModel) null, "data/");
			
			vStack.addSlice("gray24-2x3-sub1.tif");
			vStack.addSlice("gray24-2x3-sub2.tif");
			
			assertNotNull(vStack.getProcessor(1));
			
			FileInfo fi = new FileInfo();
			setFileInfo(fi, FileInfo.RGB, 2, 3, (order == ByteOrder.Value.INTEL), 2, vStack, null);
	
			byte[] bytes = writeData(fi, false);
			
			int[] actual = (int[])format.pixelsFromBytes(bytes, order);
			
			int[] expected = new int[] {-16777216,-14145496,-11513776,-8882056,-6250336,-3618616,
										-16777216,-14145496,-11513776,-8882056,-6250336,-3618616};
			
			assertArrayEquals(expected,actual);
		}

		// try with filename hack that flips pixels
		for (ByteOrder.Value order : ByteOrders)  // for all possible byte orders
		{
			VirtualStack vStack = new VirtualStack(3, 2, (ColorModel) null, "data/");
			
			vStack.addSlice("gray24-2x3-sub1.tif");
			vStack.addSlice("gray24-2x3-sub2.tif");
			
			assertNotNull(vStack.getProcessor(1));
			
			FileInfo fi = new FileInfo();
			setFileInfo(fi, FileInfo.RGB, 2, 3, (order == ByteOrder.Value.INTEL), 2, vStack, null);
			fi.fileName = "FlipTheseImages";
	
			byte[] bytes = writeData(fi, false);
			
			int[] actual = (int[])format.pixelsFromBytes(bytes, order);
			
			int[] expected = new int[] {-8882056,-6250336,-3618616,-16777216,-14145496,-11513776,
										-8882056,-6250336,-3618616,-16777216,-14145496,-11513776};
			
			assertArrayEquals(expected,actual);
		}
	}
	
	private void tryRgbStack() {
		PixelFormat format = new RgbFormat();
		for (long[][][] imageSet : Images.ImageSets)  // for each test image set we have
		{
			for (ByteOrder.Value order : ByteOrders)  // for all possible byte orders
			{
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
		PixelFormat format = new RgbFormat();
		for (long[][] image : Images.Images) // for all test images
		{
			for (ByteOrder.Value order : ByteOrders)  // for all possible byte orders
			{
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
	
	private void tryGray32FloatVirtualStack() {
		PixelFormat format = new Gray32FloatFormat();
		
		// try regular
		for (ByteOrder.Value order : ByteOrders)  // for all possible byte orders
		{
			VirtualStack vStack = new VirtualStack(3, 2, (ColorModel) null, "data/");
			
			vStack.addSlice("gray32float-2x3-sub1.tif");
			vStack.addSlice("gray32float-2x3-sub2.tif");
			
			assertNotNull(vStack.getProcessor(1));
			
			FileInfo fi = new FileInfo();
			setFileInfo(fi, FileInfo.GRAY32_FLOAT, 2, 3, (order == ByteOrder.Value.INTEL), 2, vStack, null);
	
			byte[] bytes = writeData(fi, false);
			
			float[] actual = (float[])format.pixelsFromBytes(bytes, order);
			
			float[] expected = new float[] {0,0.15686275f,0.3137255f,0.47058824f,0.627451f,0.78431374f,
											0,0.15686275f,0.3137255f,0.47058824f,0.627451f,0.78431374f};
			
			Assert.assertFloatArraysEqual(expected,actual,Assert.FLOAT_TOL);
		}

		// try with filename hack that flips pixels
		for (ByteOrder.Value order : ByteOrders)  // for all possible byte orders
		{
			VirtualStack vStack = new VirtualStack(3, 2, (ColorModel) null, "data/");
			
			vStack.addSlice("gray32float-2x3-sub1.tif");
			vStack.addSlice("gray32float-2x3-sub2.tif");
			
			assertNotNull(vStack.getProcessor(1));
			
			FileInfo fi = new FileInfo();
			setFileInfo(fi, FileInfo.GRAY32_FLOAT, 2, 3, (order == ByteOrder.Value.INTEL), 2, vStack, null);
			fi.fileName = "FlipTheseImages";
	
			byte[] bytes = writeData(fi, false);
			
			float[] actual = (float[])format.pixelsFromBytes(bytes, order);
			
			float[] expected = new float[] {0.47058824f,0.627451f,0.78431374f,0,0.15686275f,0.3137255f,
											0.47058824f,0.627451f,0.78431374f,0,0.15686275f,0.3137255f};
			
			Assert.assertFloatArraysEqual(expected,actual,Assert.FLOAT_TOL);
		}
	}
	
	private void tryGray32FloatStack() {
		PixelFormat format = new Gray32FloatFormat();
		for (long[][][] imageSet : Images.ImageSets)  // for each test image set we have
		{
			for (ByteOrder.Value order : ByteOrders)  // for all possible byte orders
			{
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
				
				Assert.assertFloatArraysEqual(expectedAfterWrite,actual,Assert.FLOAT_TOL);
			}
		}
	}
	
	private void tryGray32FloatImage() {
		PixelFormat format = new Gray32FloatFormat();
		for (long[][] image : Images.Images) // for all test images
		{
			for (ByteOrder.Value order : ByteOrders)  // for all possible byte orders
			{
				float[] expected = (float[])(format.expectedResults(image)); 
				
				FileInfo fi = new FileInfo();
				setFileInfo(fi, FileInfo.GRAY32_FLOAT, image.length, image[0].length, (order == ByteOrder.Value.INTEL),
							1, null, expected);
	
				byte[] bytes = writeData(fi, false);
				
				float[] actual = (float[])format.pixelsFromBytes(bytes, order);
				
				Assert.assertFloatArraysEqual(expected,actual,Assert.FLOAT_TOL);
			}
		}
	}
	
	private void tryGray32Float() {
		tryGray32FloatVirtualStack();
		tryGray32FloatStack();
		tryGray32FloatImage();
	}
	
	private void tryRgb48Image() {
		PixelFormat format = new Rgb48Format();
		for (long[][] image : Images.Images) // for all test images
		{
			for (ByteOrder.Value order : ByteOrders)  // for all possible byte orders
			{
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
