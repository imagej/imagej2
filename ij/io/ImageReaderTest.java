package ij.io;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.*;

// NOTES
//   This suite of tests also exercises the public class ByteVector which is defined in ImageReader.java
//   Due to the API definitions of ByteVector it is difficult to exercise single methods. The tests tend
//   to be interdependent so some methods are tested in multiple places.

public class ImageReaderTest {

	// constant image data used by readPixels() tests. All in form {{11,12,13},{21,22,23},{31,32,33}};
	
	static final byte[] Pix3x3byte = new byte[] {11,12,13,21,22,23,31,32,33};
	static final short[] Pix3x3short = new short[] {11,12,13,21,22,23,31,32,33};
	static final int[] Pix3x3int = new int[] {11,12,13,21,22,23,31,32,33};
	static final float[] Pix3x3float = new float[] {11,12,13,21,22,23,31,32,33};
	static final double[] Pix3x3double = new double[] {11,12,13,21,22,23,31,32,33};
	static final byte[] Pix3x3rgb = new byte[] {0,0,0x11,0,0,0x12,0,0,0x13,0,0,0x21,0,0,0x22,0,0,0x23,0,0,0x31,0,0,0x32,0,0,0x33};
	static final byte[] Pix3x3bgr = new byte[] {0x11,0,0,0x12,0,0,0x13,0,0,0x21,0,0,0x22,0,0,0x23,0,0,0x31,0,0,0x32,0,0,0x33,0,0};
	static final int[] Pix3x3argb = new int[] {0xff000011, 0xff000012, 0xff000013, 0xff000021, 0xff000022, 0xff000023, 0xff000031, 0xff000032, 0xff000033};
	static final int[] Pix3x3barg = new int[] {0x11ff0000, 0x12ff0000, 0x13ff0000, 0x21ff0000, 0x22ff0000, 0x23ff0000, 0x31ff0000, 0x32ff0000, 0x33ff0000};
	// Note that file type ABGR is stored as BGRA
	static final int[] Pix3x3abgr = new int[] {0x110000ff, 0x120000ff, 0x130000ff, 0x210000ff, 0x220000ff, 0x230000ff, 0x310000ff, 0x320000ff, 0x330000ff};
	static final byte[] Pix3x3rgbPlanar = new byte[] {00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,0x11,0x12,0x13,0x21,0x22,0x23,0x31,0x32,0x33};
	static final byte[] Pix3x3bitmap = new byte[] {};
	
	static final int[][] BaseImage1x1 = {{77}};
	static final int[][] BaseImage3x3 = {{11,12,13},{21,22,23},{31,32,33}};
	static final int[][] BaseImage1x9 = {{11,12,13,14,15,16,17,18,19}};
	static final int[][] BaseImage7x2 = {{11,12},{21,22},{31,32},{41,42},{51,52},{61,62},{71,72}};
	static final int[][] BaseImage5x4 = {{255,255,255,255},{127,127,127,127},{63,63,63,63},{31,31,31,31},{15,15,15,15}};
	static final int[][] BaseImage4x6 = {{0,255,100,200,77,153},{255,254,253,252,251,250},{1,2,3,4,5,6},{0,0,0,0,0,0},{67,67,67,67,67,67},{8,99,8,99,8,255}};

	static final float FLOAT_TOL = 0.00001f;

	private ByteVector bv;
	
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

	// *********************** ByteVector Tests  **************************************
	
	@Test
	public void testByteVectorCons(){
		
		// test default constructor
		bv = new ByteVector();
		assertNotNull(bv);
		assertEquals(0,bv.size());
	}

	@Test
	public void testByteVectorSize(){

		// this next test crashes on original IJ
		if (IJInfo.RUN_ENHANCED_TESTS){
			// test if bv can handle bad initial size
			bv = new ByteVector(-1);
			assertNotNull(bv);
			assertEquals(0,bv.size());
		}

		// test initial size of 0
		bv = new ByteVector(0);
		assertNotNull(bv);
		assertEquals(0,bv.size());

		bv = new ByteVector(1024);
		assertNotNull(bv);
		assertEquals(0,bv.size());
	}

	@Test
	public void testByteVectorAddByte(){
		
		// create an empty byte vec
		bv = new ByteVector(0);		
		assertNotNull(bv);
		assertEquals(0,bv.size());
		
		// add a single byte and test that it pulls back out
		bv.add((byte)33);		
		assertEquals(1,bv.size());
		assertEquals(33,bv.toByteArray()[0]);
		
		// add a bunch of bytes to see that it grows correctly
		for (int i = 0; i < 1024; i++)
			bv.add((byte)104);
		assertEquals(1025,bv.size());
	}

	@Test
	public void testByteVectorAddBytes(){
		
		bv = new ByteVector();
		assertNotNull(bv);

		if (IJInfo.RUN_ENHANCED_TESTS)
		{
			// test what happens if we pass in null : original IJ has a null ptr exception here
			bv.add(null);
			assertEquals(0,bv.size());
		}

		byte[] theBytes;
		
		bv = new ByteVector();
		assertNotNull(bv);
		
		// add a bunch of bytes and then try to pull back out
		theBytes = new byte[] {0,1,2,3,4,5,6,7,8,9};
		bv.add(theBytes);
		assertArrayEquals(theBytes,bv.toByteArray());
	}

	@Test
	public void testByteVectorClear(){

		// create a BV
		bv = new ByteVector();
		assertNotNull(bv);

		// add something
		bv.add((byte)5);
		assertEquals(1,bv.size());
		
		// clear and see what happens
		bv.clear();
		assertEquals(0,bv.size());
		assertArrayEquals(new byte[0],bv.toByteArray());
		
		// now try it after adding many
		bv = new ByteVector();
		for (int i = 0; i < 2048; i++)
			bv.add((byte)1);
		assertEquals(2048,bv.size());

		// clear and see what happens
		bv.clear();
		assertEquals(0,bv.size());
		assertArrayEquals(new byte[0],bv.toByteArray());
	}

	@Test
	public void testByteVectorConsInt(){

		// crash : negative array size exception - ByteVector does not do any testing of input value
		if (IJInfo.RUN_ENHANCED_TESTS)
		{
			// try passing bad size
			bv = new ByteVector(-1);
			assertNotNull(bv);
			assertEquals(0,bv.size());
		}

		// try passing 0 size
		bv = new ByteVector(0);

		// test ok
		assertNotNull(bv);
		assertEquals(0,bv.size());

		// try passing a larger size
		bv = new ByteVector(1000);
		assertNotNull(bv);
		assertEquals(0,bv.size());
	}

	@Test
	public void testByteVectorConsBytes(){

		// ByteVector(byte[]) allows you to specify the initial buffer to use for data
		
		// this next test crashes on original IJ : no checking on input data
		if (IJInfo.RUN_ENHANCED_TESTS)
		{
			// try passing null
			bv = new ByteVector(null);
			assertEquals(0,bv.size());
			assertArrayEquals(null,bv.toByteArray());
		}

		// try passing empty array
		byte[] bytes = new byte[] {};
		bv = new ByteVector(bytes);
		assertEquals(0,bv.size());

		// try passing 1 element array
		bytes = new byte[] {1};
		bv = new ByteVector(bytes);
		assertEquals(0,bv.size());

		// try passing multiple element array
		bytes = new byte[] {1,2,3,4,5,6,7,8,9,0};
		bv = new ByteVector(bytes);
		assertEquals(0,bv.size());
	}

	@Test
	public void testByteVectorToByteArray(){
		// test an empty array
		byte[] bytes = new byte[] {};		
		bv = new ByteVector(bytes);
		assertArrayEquals(bytes,bv.toByteArray());
		
		// test a populated array
		bytes = new byte[] {99,98,87,76};
		bv = new ByteVector(bytes);
		for (byte b : bytes)
			bv.add(b);
		assertArrayEquals(bytes,bv.toByteArray());
	}

	// *********************** ImageReaderTest helper methods  **************************************

	// there may be Javaish ways of doing this.
	
	private byte[] convertToBytes(Object inArray)
	{
		byte[] outputBytes;
		
		if (inArray instanceof byte[])
			return ((byte[])inArray).clone();
		
		if (inArray instanceof short[])
		{
			short[] arr = (short[]) inArray;
			outputBytes = new byte[arr.length * 2];
			
			for (int i = 0; i < arr.length; i++)
			{
				outputBytes[2*i]   = (byte)((arr[i] & 0xff00) >> 8);
				outputBytes[2*i+1] = (byte)((arr[i] & 0x00ff) >> 0);
			}
			return outputBytes;
		}
		
		if (inArray instanceof int[])
		{
			int[] arr = (int[]) inArray;
			outputBytes = new byte[arr.length * 4];
			
			for (int i = 0; i < arr.length; i++)
			{
				outputBytes[4*i]   = (byte)((arr[i] & 0xff000000L) >> 24);
				outputBytes[4*i+1] = (byte)((arr[i] & 0x00ff0000L) >> 16);
				outputBytes[4*i+2] = (byte)((arr[i] & 0x0000ff00L) >> 8);
				outputBytes[4*i+3] = (byte)((arr[i] & 0x000000ffL) >> 0);
			}
			return outputBytes;
		}
		
		if (inArray instanceof long[])
		{
			// for now not needed I think - fall through to null return below
		}
		
		if (inArray instanceof float[])
		{
			float[] arr = (float[]) inArray;
			outputBytes = new byte[arr.length * 4];
			
			for (int i = 0; i < arr.length; i++)
			{
				int bits = Float.floatToIntBits(arr[i]);
				
				outputBytes[4*i]   = (byte)((bits & 0xff000000L) >> 24);
				outputBytes[4*i+1] = (byte)((bits & 0x00ff0000L) >> 16);
				outputBytes[4*i+2] = (byte)((bits & 0x0000ff00L) >> 8);
				outputBytes[4*i+3] = (byte)((bits & 0x000000ffL) >> 0);
			}
			
			return outputBytes;
		}
		
		if (inArray instanceof double[])
		{
			double[] arr = (double[]) inArray;
			outputBytes = new byte[arr.length * 8];
			
			for (int i = 0; i < arr.length; i++)
			{
				long bits = Double.doubleToLongBits(arr[i]);
				
				outputBytes[8*i]   = (byte)((bits & 0xff00000000000000L) >> 56);
				outputBytes[8*i+1] = (byte)((bits & 0x00ff000000000000L) >> 48);
				outputBytes[8*i+2] = (byte)((bits & 0x0000ff0000000000L) >> 40);
				outputBytes[8*i+3] = (byte)((bits & 0x000000ff00000000L) >> 32);
				outputBytes[8*i+4] = (byte)((bits & 0x00000000ff000000L) >> 24);
				outputBytes[8*i+5] = (byte)((bits & 0x0000000000ff0000L) >> 16);
				outputBytes[8*i+6] = (byte)((bits & 0x000000000000ff00L) >> 8);
				outputBytes[8*i+7] = (byte)((bits & 0x00000000000000ffL) >> 0);
			}
			
			return outputBytes;
		}
		
		return null;
	}
	
	private Object readPixelHelper(int fileType, int compression, int r, int c, Object inPixels)
	{
		byte[] inBytes = convertToBytes(inPixels);
		ByteArrayInputStream stream = new ByteArrayInputStream(inBytes);
		FileInfo fi = new FileInfo();
		fi.fileType = fileType;
		fi.compression = compression;
		fi.width = c;
		fi.height = r;
		ImageReader rdr = new ImageReader(fi);
		return rdr.readPixels(stream);
	}
	
	private Object callReadPixels(FileInfo fi, byte[] pixInData)
	{
		ImageReader reader = new ImageReader(fi);
		ByteArrayInputStream stream = new ByteArrayInputStream(pixInData);
		return reader.readPixels(stream);
	}
	
	private void lociAssertArrayEquals(float[] a, float[] b)
	{
		assertEquals(a.length,b.length);
		for (int i = 0; i < a.length; i++)
			assertEquals(a[i],b[i],FLOAT_TOL);
	}
	
	private void lociAssertArrayEquals(int[] a, float[] b)
	{
		assertEquals(a.length,b.length);
		for (int i = 0; i < a.length; i++)
			assertEquals((float)a[i],b[i],FLOAT_TOL);
	}
	
	private void lociAssertArrayEquals(double[] a, float[] b)
	{
		assertEquals(a.length,b.length);
		for (int i = 0; i < a.length; i++)
			assertEquals((float)a[i],b[i],FLOAT_TOL);
	}
	
	private byte[] prependFakeHeader(int headerBytes, byte[] pixData)
	{
		byte[] header = new byte[headerBytes];
		byte[] output = new byte[header.length + pixData.length];
		System.arraycopy(header,0,output,0,header.length);
		System.arraycopy(pixData,0,output,header.length,pixData.length);
		return output;
	}

	// *********************** ImageReader Tests  **************************************

	@Test
	public void testImageReader() {
		
		FileInfo f = new FileInfo();
		ImageReader reader = new ImageReader(f);
		
		assertNotNull(reader);
	}

	// unknown file type
	private void readBogusFileType()
	{
		byte[] inBytes = new byte[] {5,3,1};
		
		Object pixels;
		
		pixels = readPixelHelper(-1,FileInfo.COMPRESSION_NONE,1,3,inBytes);
		assertNull(pixels);

		pixels = readPixelHelper(-184625640,FileInfo.COMPRESSION_NONE,1,3,inBytes);
		assertNull(pixels);

		pixels = readPixelHelper(1014,FileInfo.COMPRESSION_NONE,1,3,inBytes);
		assertNull(pixels);
	}
	
	private byte[] gray8PixelBytes(int[][] inputData, FileInfo fi, int compression, int headerBytes, boolean intelByteOrder)
	{
		// set FileInfo fields for subsequent calls
		
		fi.fileType = FileInfo.GRAY8;
		fi.compression = compression;
		fi.intelByteOrder = intelByteOrder;
		fi.height = inputData.length;
		fi.width = inputData[0].length;
		
		byte[] output = new byte[fi.height * fi.width];
		
		int i = 0;
		for (int[] row : inputData)
			for (int pix : row)
				output[i++] = (byte)(pix & 0xff);
	
		if (intelByteOrder)
			; // nothing to do for a byte oriented data stream
		
		if (compression == FileInfo.LZW)
			; // output = lzwCompress(output); // compress the output data
		else if (compression == FileInfo.LZW_WITH_DIFFERENCING)
			; // output = lzwCompress(output); // compress the output data
		else if (compression == FileInfo.PACK_BITS)
			; // output = packBitsCompress(output); // compress the output data

		// else assuming its COMPRESSION_NONE : do nothing		
		
		// create a bogus header if desired
		//   note that this may not work correctly as skip is figured from strips too investigate further
		if (headerBytes > 0)
		{
			fi.offset = headerBytes;
			fi.longOffset = headerBytes;
			output = prependFakeHeader(headerBytes,output);
		}
		
		return output;
	}

	private byte[] gray8ExpectedOutput(int[][] image, boolean intelByteOrder)
	{
		byte[] output = new byte[image.length * image[0].length];
		
		int i = 0;
		for (int[] row : image)
			for (int pix : row)
				output[i++] = (byte) pix;
		
		return output;
	}

	private void tryGray8Map(int[][] baseImage, int compression, int headerBytes, boolean intelByteOrder)
	{
		FileInfo fi = new FileInfo();
		byte[] pixInData = gray8PixelBytes(baseImage,fi,compression,headerBytes,intelByteOrder);
		Object pixels = callReadPixels(fi,pixInData);
		assertNotNull(pixels);
		assertTrue(pixels instanceof byte[]);
		assertArrayEquals(gray8ExpectedOutput(baseImage,intelByteOrder),(byte[])pixels);
	}
	
	// FileInfo.GRAY8
	//   subcases:
	//     compression
	//     skip()
	private void readGray8FileType()
	{
		// Object pixels;
		
		// GRAY8 uncompressed
		//pixels = readPixelHelper(FileInfo.GRAY8,FileInfo.COMPRESSION_NONE,3,3,Pix3x3byte);

		//assertNotNull(pixels);
		//assertTrue(pixels instanceof byte[]);
		//assertArrayEquals(Pix3x3byte,(byte[])pixels);
		

		// GRAY8 uncompressed
		tryGray8Map(BaseImage1x1,FileInfo.COMPRESSION_NONE,0,false);
		tryGray8Map(BaseImage3x3,FileInfo.COMPRESSION_NONE,0,false);
		tryGray8Map(BaseImage1x9,FileInfo.COMPRESSION_NONE,0,false);
		tryGray8Map(BaseImage7x2,FileInfo.COMPRESSION_NONE,0,false);
		tryGray8Map(BaseImage5x4,FileInfo.COMPRESSION_NONE,0,false);
		tryGray8Map(BaseImage4x6,FileInfo.COMPRESSION_NONE,0,false);
		
		tryGray8Map(BaseImage7x2,FileInfo.COMPRESSION_NONE,63,false);  // test header specifically
		
		// GRAY8 compressed LZW
		// GRAY8 compressed LZW_WITH_DIFFERENCING
		// GRAY8 compressed PACK_BITS
	}

	private byte[] color8PixelBytes(int[][] inputData, FileInfo fi, int compression, int headerBytes, boolean intelByteOrder)
	{
		// set FileInfo fields for subsequent calls
		
		fi.fileType = FileInfo.COLOR8;
		fi.compression = compression;
		fi.intelByteOrder = intelByteOrder;
		fi.height = inputData.length;
		fi.width = inputData[0].length;
		
		byte[] output = new byte[fi.height * fi.width];
		
		int i = 0;
		for (int[] row : inputData)
			for (int pix : row)
				output[i++] = (byte)(pix & 0xff);
	
		if (intelByteOrder)
			; // nothing to do for a byte oriented data stream
		
		if (compression == FileInfo.LZW)
			; // output = lzwCompress(output); // compress the output data
		else if (compression == FileInfo.LZW_WITH_DIFFERENCING)
			; // output = lzwCompress(output); // compress the output data
		else if (compression == FileInfo.PACK_BITS)
			; // output = packBitsCompress(output); // compress the output data

		// else assuming its COMPRESSION_NONE : do nothing		
		
		// create a bogus header if desired
		//   note that this may not work correctly as skip is figured from strips too investigate further
		if (headerBytes > 0)
		{
			fi.offset = headerBytes;
			fi.longOffset = headerBytes;
			output = prependFakeHeader(headerBytes,output);
		}
		
		return output;
	}

	private byte[] color8ExpectedOutput(int[][] image, boolean intelByteOrder)
	{
		byte[] output = new byte[image.length * image[0].length];
		
		int i = 0;
		for (int[] row : image)
			for (int pix : row)
				output[i++] = (byte) pix;
		
		return output;
	}

	private void tryColor8Map(int[][] baseImage, int compression, int headerBytes, boolean intelByteOrder)
	{
		FileInfo fi = new FileInfo();
		byte[] pixInData = color8PixelBytes(baseImage,fi,compression,headerBytes,intelByteOrder);
		Object pixels = callReadPixels(fi,pixInData);
		assertNotNull(pixels);
		assertTrue(pixels instanceof byte[]);
		assertArrayEquals(color8ExpectedOutput(baseImage,intelByteOrder),(byte[])pixels);
	}
	
	// FileInfo.COLOR8
	//   subcases:
	//     compression
	//     skip()
	private void readColor8FileType()
	{
		//Object pixels;

		// COLOR8 uncompressed
		//pixels = readPixelHelper(FileInfo.COLOR8,FileInfo.COMPRESSION_NONE,3,3,Pix3x3byte);
		//assertNotNull(pixels);
		//assertTrue(pixels instanceof byte[]);
		//assertArrayEquals(Pix3x3byte,(byte[])pixels);

		// COLOR8 uncompressed
		tryColor8Map(BaseImage1x1,FileInfo.COMPRESSION_NONE,0,false);
		tryColor8Map(BaseImage3x3,FileInfo.COMPRESSION_NONE,0,false);
		tryColor8Map(BaseImage1x9,FileInfo.COMPRESSION_NONE,0,false);
		tryColor8Map(BaseImage7x2,FileInfo.COMPRESSION_NONE,0,false);
		tryColor8Map(BaseImage5x4,FileInfo.COMPRESSION_NONE,0,false);
		tryColor8Map(BaseImage4x6,FileInfo.COMPRESSION_NONE,0,false);
		
		tryColor8Map(BaseImage7x2,FileInfo.COMPRESSION_NONE,63,false);  // test header specifically
		// COLOR8 compressed LZW		
		// COLOR8 compressed LZW_WITH_DIFFERENCING
		// COLOR8 compressed PACK_BITS
	}

	private byte[] gray16SignedPixelBytes(int[][] inputData, FileInfo fi, int compression, int headerBytes, boolean intelByteOrder)
	{
		// set FileInfo fields for subsequent calls
		
		fi.fileType = FileInfo.GRAY16_SIGNED;
		fi.compression = compression;
		fi.intelByteOrder = intelByteOrder;
		fi.height = inputData.length;
		fi.width = inputData[0].length;
		
		byte[] output = new byte[fi.height * fi.width * 2];
		
		int i = 0;
		for (int[] row : inputData)
			for (int pix : row)
			{
				output[2*i]   = (byte)((pix & 0xff00) >> 8);
				output[2*i+1] = (byte)((pix & 0x00ff) >> 0);
				i++;
			}
		
		if (intelByteOrder)
			; // nothing to do for a byte oriented data stream
		
		if (compression == FileInfo.LZW)
			; // output = lzwCompress(output); // compress the output data
		else if (compression == FileInfo.LZW_WITH_DIFFERENCING)
			; // output = lzwCompress(output); // compress the output data
		else if (compression == FileInfo.PACK_BITS)
			; // output = packBitsCompress(output); // compress the output data

		// else assuming its COMPRESSION_NONE : do nothing		
		
		// create a bogus header if desired
		//   note that this may not work correctly as skip is figured from strips too investigate further
		if (headerBytes > 0)
		{
			fi.offset = headerBytes;
			fi.longOffset = headerBytes;
			output = prependFakeHeader(headerBytes,output);
		}
		
		return output;
	}

	private short[] gray16SignedExpectedOutput(int[][] image, boolean intelByteOrder)
	{
		short[] output = new short[image.length * image[0].length];
		
		int i = 0;
		for (int[] row : image)
			for (int pix : row)
				output[i++] = (short) (32768 + pix); // bias taken from ImageReader.readPixels()
		return output;
	}

	private void tryGray16SignedImage(int[][] baseImage, int compression, int headerBytes, boolean intelByteOrder)
	{
		FileInfo fi = new FileInfo();
		byte[] pixInData = gray16SignedPixelBytes(baseImage,fi,compression,headerBytes,intelByteOrder);
		Object pixels = callReadPixels(fi,pixInData);
		assertNotNull(pixels);
		assertTrue(pixels instanceof short[]);
		assertArrayEquals(gray16SignedExpectedOutput(baseImage,intelByteOrder),(short[])pixels);
	}
	
	// FileInfo.GRAY16_SIGNED
	//   subcases:
	//     compression
	//     fi.intelByteOrder
	private void readGray16SignedFileType()
	{
		//Object pixels;

		// GRAY16_SIGNED uncompressed
		//pixels = readPixelHelper(FileInfo.GRAY16_SIGNED,FileInfo.COMPRESSION_NONE,3,3,Pix3x3short);

		// MAY NEED TO REDESIGN THIS TEST:
		//   readPixels() biases input GRAY16_SIGNED bytes by 32768 - match behavior in test
		//short[] inPixelsXformed = new short[Pix3x3short.length];
		//for (int i = 0; i < inPixelsXformed.length; i++)
		//	inPixelsXformed[i] = (short) (32768 + Pix3x3short[i]);

		//assertNotNull(pixels);
		//assertTrue(pixels instanceof short[]);
		//assertArrayEquals(inPixelsXformed,(short[])pixels);

		// GRAY16_SIGNED uncompressed
		tryGray16SignedImage(BaseImage1x1,FileInfo.COMPRESSION_NONE,0,false);
		tryGray16SignedImage(BaseImage3x3,FileInfo.COMPRESSION_NONE,0,false);
		tryGray16SignedImage(BaseImage1x9,FileInfo.COMPRESSION_NONE,0,false);
		tryGray16SignedImage(BaseImage7x2,FileInfo.COMPRESSION_NONE,0,false);
		tryGray16SignedImage(BaseImage5x4,FileInfo.COMPRESSION_NONE,0,false);
		tryGray16SignedImage(BaseImage4x6,FileInfo.COMPRESSION_NONE,0,false);
		
		tryGray16SignedImage(BaseImage7x2,FileInfo.COMPRESSION_NONE,1003,false);  // test header specifically

		// GRAY_SIGNED LZW
		
		// GRAY_SIGNED LZW_WITH_DIFFERENCING
		
		// GRAY_SIGNED PACK_BITS
	}

	
	private byte[] gray16UnsignedPixelBytes(int[][] inputData, FileInfo fi, int compression, int headerBytes, boolean intelByteOrder)
	{
		// set FileInfo fields for subsequent calls
		
		fi.fileType = FileInfo.GRAY16_UNSIGNED;
		fi.compression = compression;
		fi.intelByteOrder = intelByteOrder;
		fi.height = inputData.length;
		fi.width = inputData[0].length;
		
		byte[] output = new byte[fi.height * fi.width * 2];
		
		int i = 0;
		for (int[] row : inputData)
			for (int pix : row)
			{
				output[2*i]   = (byte)((pix & 0xff00) >> 8);
				output[2*i+1] = (byte)((pix & 0x00ff) >> 0);
				i++;
			}
		
		if (intelByteOrder)
			; // nothing to do for a byte oriented data stream
		
		if (compression == FileInfo.LZW)
			; // output = lzwCompress(output); // compress the output data
		else if (compression == FileInfo.LZW_WITH_DIFFERENCING)
			; // output = lzwCompress(output); // compress the output data
		else if (compression == FileInfo.PACK_BITS)
			; // output = packBitsCompress(output); // compress the output data

		// else assuming its COMPRESSION_NONE : do nothing		
		
		// create a bogus header if desired
		//   note that this may not work correctly as skip is figured from strips too investigate further
		if (headerBytes > 0)
		{
			fi.offset = headerBytes;
			fi.longOffset = headerBytes;
			output = prependFakeHeader(headerBytes,output);
		}
		
		return output;
	}

	private short[] gray16UnsignedExpectedOutput(int[][] image, boolean intelByteOrder)
	{
		short[] output = new short[image.length * image[0].length];
		
		int i = 0;
		for (int[] row : image)
			for (int pix : row)
				output[i++] = (short)pix;
		return output;
	}

	private void tryGray16UnsignedImage(int[][] baseImage, int compression, int headerBytes, boolean intelByteOrder)
	{
		FileInfo fi = new FileInfo();
		byte[] pixInData = gray16UnsignedPixelBytes(baseImage,fi,compression,headerBytes,intelByteOrder);
		Object pixels = callReadPixels(fi,pixInData);
		assertNotNull(pixels);
		assertTrue(pixels instanceof short[]);
		assertArrayEquals(gray16UnsignedExpectedOutput(baseImage,intelByteOrder),(short[])pixels);
	}
	
	// FileInfo.GRAY16_UNSIGNED
	//   subcases:
	//     compression
	//     fi.intelByteOrder
	private void readGray16UnsignedFileType()
	{
		//Object pixels;

		// GRAY16_UNSIGNED uncompressed
		//pixels = readPixelHelper(FileInfo.GRAY16_UNSIGNED,FileInfo.COMPRESSION_NONE,3,3,Pix3x3short);
		//assertNotNull(pixels);
		//assertTrue(pixels instanceof short[]);
		//assertArrayEquals(Pix3x3short,(short[])pixels);

		// GRAY16_UNSIGNED uncompressed
		tryGray16UnsignedImage(BaseImage1x1,FileInfo.COMPRESSION_NONE,0,false);
		tryGray16UnsignedImage(BaseImage3x3,FileInfo.COMPRESSION_NONE,0,false);
		tryGray16UnsignedImage(BaseImage1x9,FileInfo.COMPRESSION_NONE,0,false);
		tryGray16UnsignedImage(BaseImage7x2,FileInfo.COMPRESSION_NONE,0,false);
		tryGray16UnsignedImage(BaseImage5x4,FileInfo.COMPRESSION_NONE,0,false);
		tryGray16UnsignedImage(BaseImage4x6,FileInfo.COMPRESSION_NONE,0,false);
		
		tryGray16UnsignedImage(BaseImage7x2,FileInfo.COMPRESSION_NONE,16,false);  // test header specifically

		// GRAY_UNSIGNED LZW
		
		// GRAY_UNSIGNED LZW_WITH_DIFFERENCING
		
		// GRAY_UNSIGNED PACK_BITS
	}
	
	private byte[] gray32IntPixelBytes(int[][] inputData, FileInfo fi, int compression, int headerBytes, boolean intelByteOrder)
	{
		// set FileInfo fields for subsequent calls
		
		fi.fileType = FileInfo.GRAY32_INT;
		fi.compression = compression;
		fi.intelByteOrder = intelByteOrder;
		fi.height = inputData.length;
		fi.width = inputData[0].length;
		
		byte[] output = new byte[fi.height * fi.width * 4];
		
		int i = 0;
		for (int[] row : inputData)
			for (int pix : row)
			{
				output[4*i]   = (byte)((pix & 0xff000000) >> 24);
				output[4*i+1] = (byte)((pix & 0x00ff0000) >> 16);
				output[4*i+2] = (byte)((pix & 0x0000ff00) >> 8);
				output[4*i+3] = (byte)((pix & 0x000000ff) >> 0);
				i++;
			}
		
		if (intelByteOrder)
			; // nothing to do for a byte oriented data stream
		
		if (compression == FileInfo.LZW)
			; // output = lzwCompress(output); // compress the output data
		else if (compression == FileInfo.LZW_WITH_DIFFERENCING)
			; // output = lzwCompress(output); // compress the output data
		else if (compression == FileInfo.PACK_BITS)
			; // output = packBitsCompress(output); // compress the output data

		// else assuming its COMPRESSION_NONE : do nothing		
		
		// create a bogus header if desired
		//   note that this may not work correctly as skip is figured from strips too investigate further
		if (headerBytes > 0)
		{
			fi.offset = headerBytes;
			fi.longOffset = headerBytes;
			output = prependFakeHeader(headerBytes,output);
		}
		
		return output;
	}

	private float[] gray32IntExpectedOutput(int[][] image, boolean intelByteOrder)
	{
		float[] output = new float[image.length * image[0].length];
		
		int i = 0;
		for (int[] row : image)
			for (int pix : row)
				output[i++] = (float)pix;
		return output;
	}

	private void tryGray32IntImage(int[][] baseImage, int compression, int headerBytes, boolean intelByteOrder)
	{
		FileInfo fi = new FileInfo();
		byte[] pixInData = gray32IntPixelBytes(baseImage,fi,compression,headerBytes,intelByteOrder);
		Object pixels = callReadPixels(fi,pixInData);
		assertNotNull(pixels);
		assertTrue(pixels instanceof float[]);
		lociAssertArrayEquals(gray32IntExpectedOutput(baseImage,intelByteOrder),(float[])pixels);
	}

	// FileInfo.GRAY32_INT
	//  subcases
	//  intelByteOrder?
	private void readGray32IntFileType()
	{
		//Object pixels;

		// GRAY32_INT
		//pixels = readPixelHelper(FileInfo.GRAY32_INT,FileInfo.COMPRESSION_NONE,3,3,Pix3x3int);
		//assertNotNull(pixels);
		//assertTrue(pixels instanceof float[]);
		//lociAssertArrayEquals(Pix3x3int,(float[])pixels);

		// GRAY32_INT
		tryGray32IntImage(BaseImage1x1,FileInfo.COMPRESSION_NONE,0,false);
		tryGray32IntImage(BaseImage3x3,FileInfo.COMPRESSION_NONE,0,false);
		tryGray32IntImage(BaseImage1x9,FileInfo.COMPRESSION_NONE,0,false);
		tryGray32IntImage(BaseImage7x2,FileInfo.COMPRESSION_NONE,0,false);
		tryGray32IntImage(BaseImage5x4,FileInfo.COMPRESSION_NONE,0,false);
		tryGray32IntImage(BaseImage4x6,FileInfo.COMPRESSION_NONE,0,false);
		
		tryGray32IntImage(BaseImage7x2,FileInfo.COMPRESSION_NONE,404,false);  // test header specifically
	}
	
	private byte[] gray32UnsignedPixelBytes(int[][] inputData, FileInfo fi, int compression, int headerBytes, boolean intelByteOrder)
	{
		// set FileInfo fields for subsequent calls
		
		fi.fileType = FileInfo.GRAY32_UNSIGNED;
		fi.compression = compression;
		fi.intelByteOrder = intelByteOrder;
		fi.height = inputData.length;
		fi.width = inputData[0].length;
		
		byte[] output = new byte[fi.height * fi.width * 4];
		
		int i = 0;
		for (int[] row : inputData)
			for (int pix : row)
			{
				output[4*i]   = (byte)((pix & 0xff000000) >> 24);
				output[4*i+1] = (byte)((pix & 0x00ff0000) >> 16);
				output[4*i+2] = (byte)((pix & 0x0000ff00) >> 8);
				output[4*i+3] = (byte)((pix & 0x000000ff) >> 0);
				i++;
			}
		
		if (intelByteOrder)
			; // nothing to do for a byte oriented data stream
		
		if (compression == FileInfo.LZW)
			; // output = lzwCompress(output); // compress the output data
		else if (compression == FileInfo.LZW_WITH_DIFFERENCING)
			; // output = lzwCompress(output); // compress the output data
		else if (compression == FileInfo.PACK_BITS)
			; // output = packBitsCompress(output); // compress the output data

		// else assuming its COMPRESSION_NONE : do nothing		
		
		// create a bogus header if desired
		//   note that this may not work correctly as skip is figured from strips too investigate further
		if (headerBytes > 0)
		{
			fi.offset = headerBytes;
			fi.longOffset = headerBytes;
			output = prependFakeHeader(headerBytes,output);
		}
		
		return output;
	}

	private float[] gray32UnsignedExpectedOutput(int[][] image, boolean intelByteOrder)
	{
		float[] output = new float[image.length * image[0].length];
		
		int i = 0;
		for (int[] row : image)
			for (int pix : row)
				output[i++] = (float)pix;
		return output;
	}

	private void tryGray32UnsignedImage(int[][] baseImage, int compression, int headerBytes, boolean intelByteOrder)
	{
		FileInfo fi = new FileInfo();
		byte[] pixInData = gray32UnsignedPixelBytes(baseImage,fi,compression,headerBytes,intelByteOrder);
		Object pixels = callReadPixels(fi,pixInData);
		assertNotNull(pixels);
		assertTrue(pixels instanceof float[]);
		lociAssertArrayEquals(gray32UnsignedExpectedOutput(baseImage,intelByteOrder),(float[])pixels);
	}
	
	// FileInfo.GRAY32_UNSIGNED
	//  subcases
	//  intelByteOrder?
	private void readGray32UnsignedFileType()
	{
		//Object pixels;

		// GRAY32_UNSIGNED
		//pixels = readPixelHelper(FileInfo.GRAY32_UNSIGNED,FileInfo.COMPRESSION_NONE,3,3,Pix3x3int);
		//assertNotNull(pixels);
		//assertTrue(pixels instanceof float[]);
		//lociAssertArrayEquals(Pix3x3int,(float[])pixels);

		// GRAY32_UNSIGNED
		tryGray32UnsignedImage(BaseImage1x1,FileInfo.COMPRESSION_NONE,0,false);
		tryGray32UnsignedImage(BaseImage3x3,FileInfo.COMPRESSION_NONE,0,false);
		tryGray32UnsignedImage(BaseImage1x9,FileInfo.COMPRESSION_NONE,0,false);
		tryGray32UnsignedImage(BaseImage7x2,FileInfo.COMPRESSION_NONE,0,false);
		tryGray32UnsignedImage(BaseImage5x4,FileInfo.COMPRESSION_NONE,0,false);
		tryGray32UnsignedImage(BaseImage4x6,FileInfo.COMPRESSION_NONE,0,false);
		
		tryGray32UnsignedImage(BaseImage7x2,FileInfo.COMPRESSION_NONE,101,false);  // test header specifically
	}
	
	private byte[] gray32FloatPixelBytes(int[][] inputData, FileInfo fi, int compression, int headerBytes, boolean intelByteOrder)
	{
		// set FileInfo fields for subsequent calls
		
		fi.fileType = FileInfo.GRAY32_FLOAT;
		fi.compression = compression;
		fi.intelByteOrder = intelByteOrder;
		fi.height = inputData.length;
		fi.width = inputData[0].length;
		
		byte[] output = new byte[fi.height * fi.width * 4];
		
		int i = 0;
		for (int[] row : inputData)
			for (int pix : row)
			{
				float fPix = (float) pix;
				int bPix = Float.floatToIntBits(fPix);
				output[4*i]   = (byte)((bPix & 0xff000000) >> 24);
				output[4*i+1] = (byte)((bPix & 0x00ff0000) >> 16);
				output[4*i+2] = (byte)((bPix & 0x0000ff00) >> 8);
				output[4*i+3] = (byte)((bPix & 0x000000ff) >> 0);
				i++;
			}
		
		if (intelByteOrder)
			; // nothing to do for a byte oriented data stream
		
		if (compression == FileInfo.LZW)
			; // output = lzwCompress(output); // compress the output data
		else if (compression == FileInfo.LZW_WITH_DIFFERENCING)
			; // output = lzwCompress(output); // compress the output data
		else if (compression == FileInfo.PACK_BITS)
			; // output = packBitsCompress(output); // compress the output data

		// else assuming its COMPRESSION_NONE : do nothing		
		
		// create a bogus header if desired
		//   note that this may not work correctly as skip is figured from strips too investigate further
		if (headerBytes > 0)
		{
			fi.offset = headerBytes;
			fi.longOffset = headerBytes;
			output = prependFakeHeader(headerBytes,output);
		}
		
		return output;
	}

	private float[] gray32FloatExpectedOutput(int[][] image, boolean intelByteOrder)
	{
		float[] output = new float[image.length * image[0].length];
		
		int i = 0;
		for (int[] row : image)
			for (int pix : row)
				output[i++] = (float)pix;
		return output;
	}

	private void tryGray32FloatImage(int[][] baseImage, int compression, int headerBytes, boolean intelByteOrder)
	{
		FileInfo fi = new FileInfo();
		byte[] pixInData = gray32FloatPixelBytes(baseImage,fi,compression,headerBytes,intelByteOrder);
		Object pixels = callReadPixels(fi,pixInData);
		assertNotNull(pixels);
		assertTrue(pixels instanceof float[]);
		lociAssertArrayEquals(gray32FloatExpectedOutput(baseImage,intelByteOrder),(float[])pixels);
	}
	
	// FileInfo.GRAY32_FLOAT
	//  subcases
	//  intelByteOrder?
	private void readGray32FloatFileType()
	{
		//Object pixels;

		// GRAY32_FLOAT
		//pixels = readPixelHelper(FileInfo.GRAY32_FLOAT,FileInfo.COMPRESSION_NONE,3,3,Pix3x3float);
		//assertNotNull(pixels);
		//assertTrue(pixels instanceof float[]);
		//lociAssertArrayEquals(Pix3x3float,(float[])pixels);

		// GRAY32_FLOAT
		tryGray32FloatImage(BaseImage1x1,FileInfo.COMPRESSION_NONE,0,false);
		tryGray32FloatImage(BaseImage3x3,FileInfo.COMPRESSION_NONE,0,false);
		tryGray32FloatImage(BaseImage1x9,FileInfo.COMPRESSION_NONE,0,false);
		tryGray32FloatImage(BaseImage7x2,FileInfo.COMPRESSION_NONE,0,false);
		tryGray32FloatImage(BaseImage5x4,FileInfo.COMPRESSION_NONE,0,false);
		tryGray32FloatImage(BaseImage4x6,FileInfo.COMPRESSION_NONE,0,false);
		
		tryGray32FloatImage(BaseImage7x2,FileInfo.COMPRESSION_NONE,611,false);  // test header specifically
	}
	
	private byte[] gray64FloatPixelBytes(int[][] inputData, FileInfo fi, int compression, int headerBytes, boolean intelByteOrder)
	{
		// set FileInfo fields for subsequent calls
		
		fi.fileType = FileInfo.GRAY64_FLOAT;
		fi.compression = compression;
		fi.intelByteOrder = intelByteOrder;
		fi.height = inputData.length;
		fi.width = inputData[0].length;
		
		byte[] output = new byte[fi.height * fi.width * 8];
		
		int i = 0;
		for (int[] row : inputData)
			for (int pix : row)
			{
				double dPix = (double) pix;
				long bPix = Double.doubleToLongBits(dPix);
				output[8*i+0] = (byte)((bPix & 0xff00000000000000L) >> 56);
				output[8*i+1] = (byte)((bPix & 0x00ff000000000000L) >> 48);
				output[8*i+2] = (byte)((bPix & 0x0000ff0000000000L) >> 40);
				output[8*i+3] = (byte)((bPix & 0x000000ff00000000L) >> 32);
				output[8*i+4] = (byte)((bPix & 0x00000000ff000000L) >> 24);
				output[8*i+5] = (byte)((bPix & 0x0000000000ff0000L) >> 16);
				output[8*i+6] = (byte)((bPix & 0x000000000000ff00L) >> 8);
				output[8*i+7] = (byte)((bPix & 0x00000000000000ffL) >> 0);
				i++;
			}
		
		if (intelByteOrder)
			; // nothing to do for a byte oriented data stream
		
		if (compression == FileInfo.LZW)
			; // output = lzwCompress(output); // compress the output data
		else if (compression == FileInfo.LZW_WITH_DIFFERENCING)
			; // output = lzwCompress(output); // compress the output data
		else if (compression == FileInfo.PACK_BITS)
			; // output = packBitsCompress(output); // compress the output data

		// else assuming its COMPRESSION_NONE : do nothing		
		
		// create a bogus header if desired
		//   note that this may not work correctly as skip is figured from strips too investigate further
		if (headerBytes > 0)
		{
			fi.offset = headerBytes;
			fi.longOffset = headerBytes;
			output = prependFakeHeader(headerBytes,output);
		}
		
		return output;
	}

	private float[] gray64FloatExpectedOutput(int[][] image, boolean intelByteOrder)
	{
		float[] output = new float[image.length * image[0].length];
		
		int i = 0;
		for (int[] row : image)
			for (int pix : row)
				output[i++] = (float)pix;
		return output;
	}

	private void tryGray64FloatImage(int[][] baseImage, int compression, int headerBytes, boolean intelByteOrder)
	{
		FileInfo fi = new FileInfo();
		byte[] pixInData = gray64FloatPixelBytes(baseImage,fi,compression,headerBytes,intelByteOrder);
		Object pixels = callReadPixels(fi,pixInData);
		assertNotNull(pixels);
		assertTrue(pixels instanceof float[]);
		lociAssertArrayEquals(gray64FloatExpectedOutput(baseImage,intelByteOrder),(float[])pixels);
	}
	
	// FileInfo.GRAY64_FLOAT
	//   sub cases
	//   intelByteOrder
	private void readGray64FloatFileType(){
		//Object pixels;

		// GRAY64_FLOAT
		//pixels = readPixelHelper(FileInfo.GRAY64_FLOAT,FileInfo.COMPRESSION_NONE,3,3,Pix3x3double);
		//assertNotNull(pixels);
		//assertTrue(pixels instanceof float[]);
		//lociAssertArrayEquals(Pix3x3double,(float[])pixels);

		// GRAY64_FLOAT
		tryGray64FloatImage(BaseImage1x1,FileInfo.COMPRESSION_NONE,0,false);
		tryGray64FloatImage(BaseImage3x3,FileInfo.COMPRESSION_NONE,0,false);
		tryGray64FloatImage(BaseImage1x9,FileInfo.COMPRESSION_NONE,0,false);
		tryGray64FloatImage(BaseImage7x2,FileInfo.COMPRESSION_NONE,0,false);
		tryGray64FloatImage(BaseImage5x4,FileInfo.COMPRESSION_NONE,0,false);
		tryGray64FloatImage(BaseImage4x6,FileInfo.COMPRESSION_NONE,0,false);
		
		tryGray64FloatImage(BaseImage7x2,FileInfo.COMPRESSION_NONE,382,false);  // test header specifically
	}
	
	private byte[] rgbPixelBytes(int[][] inputData, FileInfo fi, int compression, int headerBytes, boolean intelByteOrder)
	{
		// set FileInfo fields for subsequent calls
		
		fi.fileType = FileInfo.RGB;
		fi.compression = compression;
		fi.intelByteOrder = intelByteOrder;
		fi.height = inputData.length;
		fi.width = inputData[0].length;
		
		byte[] output = new byte[fi.height * fi.width * 3];
		
		int i = 0;
		for (int[] row : inputData)
			for (int pix : row)
			{
				output[3*i+0] = (byte)((pix & 0xff0000) >> 16);
				output[3*i+1] = (byte)((pix & 0x00ff00) >> 8);
				output[3*i+2] = (byte)((pix & 0x0000ff) >> 0);
				i++;
			}
		
		if (intelByteOrder)
			; // nothing to do for a byte oriented data stream
		
		if (compression == FileInfo.LZW)
			; // output = lzwCompress(output); // compress the output data
		else if (compression == FileInfo.LZW_WITH_DIFFERENCING)
			; // output = lzwCompress(output); // compress the output data
		else if (compression == FileInfo.PACK_BITS)
			; // output = packBitsCompress(output); // compress the output data

		// else assuming its COMPRESSION_NONE : do nothing		
		
		// create a bogus header if desired
		//   note that this may not work correctly as skip is figured from strips too investigate further
		if (headerBytes > 0)
		{
			fi.offset = headerBytes;
			fi.longOffset = headerBytes;
			output = prependFakeHeader(headerBytes,output);
		}
		
		return output;
	}

	private int[] rgbExpectedOutput(int[][] image, boolean intelByteOrder)
	{
		int[] output = new int[image.length * image[0].length];
		
		// NOTICE that input is rgb but output is argb
		
		int i = 0;
		for (int[] row : image)
			for (int pix : row)
				output[i++] = 0xff000000 | ((pix & 0xff0000) >> 16) | ((pix & 0xff00) >> 8) | ((pix & 0xff) >> 0);

		return output;
	}

	private void tryRgbImage(int[][] baseImage, int compression, int headerBytes, boolean intelByteOrder)
	{
		FileInfo fi = new FileInfo();
		byte[] pixInData = rgbPixelBytes(baseImage,fi,compression,headerBytes,intelByteOrder);
		Object pixels = callReadPixels(fi,pixInData);
		assertNotNull(pixels);
		assertTrue(pixels instanceof int[]);
		assertArrayEquals(rgbExpectedOutput(baseImage,intelByteOrder),(int[])pixels);
	}
	
	// FileInfo.RGB:
	//   sub cases
	//   compression
	//   intelByteOrder
	private void readRgbFileType()
	{
		//Object pixels;

		// RGB uncompressed
		//pixels = readPixelHelper(FileInfo.RGB,FileInfo.COMPRESSION_NONE,3,3,Pix3x3rgb);
		//assertNotNull(pixels);
		//assertTrue(pixels instanceof int[]);
		// notice the switch of expected pixels here from rgb to argb
		//assertArrayEquals(Pix3x3argb,(int[])pixels);

		tryRgbImage(BaseImage1x1,FileInfo.COMPRESSION_NONE,0,false);
		tryRgbImage(BaseImage3x3,FileInfo.COMPRESSION_NONE,0,false);
		tryRgbImage(BaseImage1x9,FileInfo.COMPRESSION_NONE,0,false);
		tryRgbImage(BaseImage7x2,FileInfo.COMPRESSION_NONE,0,false);
		tryRgbImage(BaseImage5x4,FileInfo.COMPRESSION_NONE,0,false);
		tryRgbImage(BaseImage4x6,FileInfo.COMPRESSION_NONE,0,false);
		
		tryRgbImage(BaseImage7x2,FileInfo.COMPRESSION_NONE,873,false);  // test header specifically
	}
	
	private byte[] bgrPixelBytes(int[][] inputData, FileInfo fi, int compression, int headerBytes, boolean intelByteOrder)
	{
		// set FileInfo fields for subsequent calls
		
		fi.fileType = FileInfo.BGR;
		fi.compression = compression;
		fi.intelByteOrder = intelByteOrder;
		fi.height = inputData.length;
		fi.width = inputData[0].length;
		
		byte[] output = new byte[fi.height * fi.width * 3];
		
		int i = 0;
		for (int[] row : inputData)
			for (int pix : row)
			{
				output[3*i+0] = (byte)((pix & 0x0000ff) >> 0);
				output[3*i+1] = (byte)((pix & 0x00ff00) >> 8);
				output[3*i+2] = (byte)((pix & 0xff0000) >> 16);
				i++;
			}
		
		if (intelByteOrder)
			; // nothing to do for a byte oriented data stream
		
		if (compression == FileInfo.LZW)
			; // output = lzwCompress(output); // compress the output data
		else if (compression == FileInfo.LZW_WITH_DIFFERENCING)
			; // output = lzwCompress(output); // compress the output data
		else if (compression == FileInfo.PACK_BITS)
			; // output = packBitsCompress(output); // compress the output data

		// else assuming its COMPRESSION_NONE : do nothing		
		
		// create a bogus header if desired
		//   note that this may not work correctly as skip is figured from strips too investigate further
		if (headerBytes > 0)
		{
			fi.offset = headerBytes;
			fi.longOffset = headerBytes;
			output = prependFakeHeader(headerBytes,output);
		}
		
		return output;
	}

	private int[] bgrExpectedOutput(int[][] image, boolean intelByteOrder)
	{
		int[] output = new int[image.length * image[0].length];
		
		// NOTICE that input is bgr but output is argb
		
		int i = 0;
		for (int[] row : image)
			for (int pix : row)
				output[i++] = 0xff000000 | ((pix & 0xff0000) >> 16) | ((pix & 0xff00) >> 8) | ((pix & 0xff) >> 0);

		return output;
	}

	private void tryBgrImage(int[][] baseImage, int compression, int headerBytes, boolean intelByteOrder)
	{
		FileInfo fi = new FileInfo();
		byte[] pixInData = bgrPixelBytes(baseImage,fi,compression,headerBytes,intelByteOrder);
		Object pixels = callReadPixels(fi,pixInData);
		assertNotNull(pixels);
		assertTrue(pixels instanceof int[]);
		assertArrayEquals(bgrExpectedOutput(baseImage,intelByteOrder),(int[])pixels);
	}
	
	// FileInfo.BGR:
	//   sub cases
	//   compression
	//   intelByteOrder
	private void readBgrFileType()
	{
		//Object pixels;

		// BGR uncompressed
		//pixels = readPixelHelper(FileInfo.BGR,FileInfo.COMPRESSION_NONE,3,3,Pix3x3bgr);
		//assertNotNull(pixels);
		//assertTrue(pixels instanceof int[]);
		// notice the switch of expected pixels here from rgb to argb
		//assertArrayEquals(Pix3x3argb,(int[])pixels);

		tryBgrImage(BaseImage1x1,FileInfo.COMPRESSION_NONE,0,false);
		tryBgrImage(BaseImage3x3,FileInfo.COMPRESSION_NONE,0,false);
		tryBgrImage(BaseImage1x9,FileInfo.COMPRESSION_NONE,0,false);
		tryBgrImage(BaseImage7x2,FileInfo.COMPRESSION_NONE,0,false);
		tryBgrImage(BaseImage5x4,FileInfo.COMPRESSION_NONE,0,false);
		tryBgrImage(BaseImage4x6,FileInfo.COMPRESSION_NONE,0,false);
		
		tryBgrImage(BaseImage7x2,FileInfo.COMPRESSION_NONE,873,false);  // test header specifically
	}
	
	private byte[] argbPixelBytes(int[][] inputData, FileInfo fi, int compression, int headerBytes, boolean intelByteOrder)
	{
		// set FileInfo fields for subsequent calls
		
		fi.fileType = FileInfo.ARGB;
		fi.compression = compression;
		fi.intelByteOrder = intelByteOrder;
		fi.height = inputData.length;
		fi.width = inputData[0].length;
		
		byte[] output = new byte[fi.height * fi.width * 4];
		
		int i = 0;
		for (int[] row : inputData)
			for (int pix : row)
			{
				output[4*i+0] = (byte)((pix & 0xff000000) >> 24);
				output[4*i+1] = (byte)((pix & 0x00ff0000) >> 16);
				output[4*i+2] = (byte)((pix & 0x0000ff00) >> 8);
				output[4*i+3] = (byte)((pix & 0x000000ff) >> 0);
				i++;
			}
		
		if (intelByteOrder)
			; // nothing to do for a byte oriented data stream
		
		if (compression == FileInfo.LZW)
			; // output = lzwCompress(output); // compress the output data
		else if (compression == FileInfo.LZW_WITH_DIFFERENCING)
			; // output = lzwCompress(output); // compress the output data
		else if (compression == FileInfo.PACK_BITS)
			; // output = packBitsCompress(output); // compress the output data

		// else assuming its COMPRESSION_NONE : do nothing		
		
		// create a bogus header if desired
		//   note that this may not work correctly as skip is figured from strips too investigate further
		if (headerBytes > 0)
		{
			fi.offset = headerBytes;
			fi.longOffset = headerBytes;
			output = prependFakeHeader(headerBytes,output);
		}
		
		return output;
	}

	private int[] argbExpectedOutput(int[][] image, boolean intelByteOrder)
	{
		int[] output = new int[image.length * image[0].length];
		
		int i = 0;
		for (int[] row : image)
			for (int pix : row)
				output[i++] = 0xff000000 | ((pix & 0xff0000) >> 16) | ((pix & 0xff00) >> 8) | ((pix & 0xff) >> 0);

		return output;
	}

	private void tryArgbImage(int[][] baseImage, int compression, int headerBytes, boolean intelByteOrder)
	{
		FileInfo fi = new FileInfo();
		byte[] pixInData = argbPixelBytes(baseImage,fi,compression,headerBytes,intelByteOrder);
		Object pixels = callReadPixels(fi,pixInData);
		assertNotNull(pixels);
		assertTrue(pixels instanceof int[]);
		assertArrayEquals(argbExpectedOutput(baseImage,intelByteOrder),(int[])pixels);
	}

	// FileInfo.ARGB:
	//   sub cases
	//   compression
	//   intelByteOrder
	private void readArgbFileType()
	{
		//Object pixels;

		// ARGB uncompressed
		//pixels = readPixelHelper(FileInfo.ARGB,FileInfo.COMPRESSION_NONE,3,3,Pix3x3argb);
		//assertNotNull(pixels);
		//assertTrue(pixels instanceof int[]);
		//assertArrayEquals(Pix3x3argb,(int[])pixels);

		tryArgbImage(BaseImage1x1,FileInfo.COMPRESSION_NONE,0,false);
		tryArgbImage(BaseImage3x3,FileInfo.COMPRESSION_NONE,0,false);
		tryArgbImage(BaseImage1x9,FileInfo.COMPRESSION_NONE,0,false);
		tryArgbImage(BaseImage7x2,FileInfo.COMPRESSION_NONE,0,false);
		tryArgbImage(BaseImage5x4,FileInfo.COMPRESSION_NONE,0,false);
		tryArgbImage(BaseImage4x6,FileInfo.COMPRESSION_NONE,0,false);
		
		tryArgbImage(BaseImage7x2,FileInfo.COMPRESSION_NONE,501,false);  // test header specifically
	}
	
	private byte[] abgrPixelBytes(int[][] inputData, FileInfo fi, int compression, int headerBytes, boolean intelByteOrder)
	{
		// set FileInfo fields for subsequent calls
		
		fi.fileType = FileInfo.ABGR;
		fi.compression = compression;
		fi.intelByteOrder = intelByteOrder;
		fi.height = inputData.length;
		fi.width = inputData[0].length;
		
		byte[] output = new byte[fi.height * fi.width * 4];
		
		int i = 0;
		for (int[] row : inputData)
			for (int pix : row)
			{
				// NOTICE that ABGR stored as bgra
				
				output[4*i+0] = (byte)((pix & 0x000000ff) >> 0);
				output[4*i+1] = (byte)((pix & 0x0000ff00) >> 8);
				output[4*i+2] = (byte)((pix & 0x00ff0000) >> 16);
				output[4*i+3] = (byte)((pix & 0xff000000) >> 24);
				i++;
			}
		
		if (intelByteOrder)
			; // nothing to do for a byte oriented data stream
		
		if (compression == FileInfo.LZW)
			; // output = lzwCompress(output); // compress the output data
		else if (compression == FileInfo.LZW_WITH_DIFFERENCING)
			; // output = lzwCompress(output); // compress the output data
		else if (compression == FileInfo.PACK_BITS)
			; // output = packBitsCompress(output); // compress the output data

		// else assuming its COMPRESSION_NONE : do nothing		
		
		// create a bogus header if desired
		//   note that this may not work correctly as skip is figured from strips too investigate further
		if (headerBytes > 0)
		{
			fi.offset = headerBytes;
			fi.longOffset = headerBytes;
			output = prependFakeHeader(headerBytes,output);
		}
		
		return output;
	}

	private int[] abgrExpectedOutput(int[][] image, boolean intelByteOrder)
	{
		int[] output = new int[image.length * image[0].length];
		
		int i = 0;
		for (int[] row : image)
			for (int pix : row)
				output[i++] = 0xff000000 | ((pix & 0xff0000) >> 16) | ((pix & 0xff00) >> 8) | ((pix & 0xff) >> 0);

		return output;
	}

	private void tryAbgrImage(int[][] baseImage, int compression, int headerBytes, boolean intelByteOrder)
	{
		FileInfo fi = new FileInfo();
		byte[] pixInData = abgrPixelBytes(baseImage,fi,compression,headerBytes,intelByteOrder);
		Object pixels = callReadPixels(fi,pixInData);
		assertNotNull(pixels);
		assertTrue(pixels instanceof int[]);
		assertArrayEquals(abgrExpectedOutput(baseImage,intelByteOrder),(int[])pixels);
	}

	// FileInfo.ABGR:
	//   sub cases
	//   compression
	//   intelByteOrder
	private void readAbgrFileType()
	{
		//Object pixels;

		// ABGR uncompressed
		//pixels = readPixelHelper(FileInfo.ABGR,FileInfo.COMPRESSION_NONE,3,3,Pix3x3abgr);
		//assertNotNull(pixels);
		//assertTrue(pixels instanceof int[]);
		// notice the switch of expected pixels here from abgr to argb
		//assertArrayEquals(Pix3x3argb,(int[])pixels);

		tryAbgrImage(BaseImage1x1,FileInfo.COMPRESSION_NONE,0,false);
		tryAbgrImage(BaseImage3x3,FileInfo.COMPRESSION_NONE,0,false);
		tryAbgrImage(BaseImage1x9,FileInfo.COMPRESSION_NONE,0,false);
		tryAbgrImage(BaseImage7x2,FileInfo.COMPRESSION_NONE,0,false);
		tryAbgrImage(BaseImage5x4,FileInfo.COMPRESSION_NONE,0,false);
		tryAbgrImage(BaseImage4x6,FileInfo.COMPRESSION_NONE,0,false);
		
		tryAbgrImage(BaseImage7x2,FileInfo.COMPRESSION_NONE,222,false);  // test header specifically
	}
	
	private byte[] bargPixelBytes(int[][] inputData, FileInfo fi, int compression, int headerBytes, boolean intelByteOrder)
	{
		// set FileInfo fields for subsequent calls
		
		fi.fileType = FileInfo.BARG;
		fi.compression = compression;
		fi.intelByteOrder = intelByteOrder;
		fi.height = inputData.length;
		fi.width = inputData[0].length;
		
		byte[] output = new byte[fi.height * fi.width * 4];
		
		int i = 0;
		for (int[] row : inputData)
			for (int pix : row)
			{				
				output[4*i+0] = (byte)((pix & 0x000000ff) >> 0);
				output[4*i+1] = (byte)((pix & 0xff000000) >> 24);
				output[4*i+2] = (byte)((pix & 0x00ff0000) >> 16);
				output[4*i+3] = (byte)((pix & 0x0000ff00) >> 8);
				i++;
			}
		
		if (intelByteOrder)
			; // nothing to do for a byte oriented data stream
		
		if (compression == FileInfo.LZW)
			; // output = lzwCompress(output); // compress the output data
		else if (compression == FileInfo.LZW_WITH_DIFFERENCING)
			; // output = lzwCompress(output); // compress the output data
		else if (compression == FileInfo.PACK_BITS)
			; // output = packBitsCompress(output); // compress the output data

		// else assuming its COMPRESSION_NONE : do nothing		
		
		// create a bogus header if desired
		//   note that this may not work correctly as skip is figured from strips too investigate further
		if (headerBytes > 0)
		{
			fi.offset = headerBytes;
			fi.longOffset = headerBytes;
			output = prependFakeHeader(headerBytes,output);
		}
		
		return output;
	}

	private int[] bargExpectedOutput(int[][] image, boolean intelByteOrder)
	{
		int[] output = new int[image.length * image[0].length];
	
		// NOTICE input is BARG but output is argb
		int i = 0;
		for (int[] row : image)
			for (int pix : row)
				output[i++] = 0xff000000 | ((pix & 0xff0000) >> 16) | ((pix & 0xff00) >> 8) | ((pix & 0xff) >> 0);

		return output;
	}

	private void tryBargImage(int[][] baseImage, int compression, int headerBytes, boolean intelByteOrder)
	{
		FileInfo fi = new FileInfo();
		byte[] pixInData = bargPixelBytes(baseImage,fi,compression,headerBytes,intelByteOrder);
		Object pixels = callReadPixels(fi,pixInData);
		assertNotNull(pixels);
		assertTrue(pixels instanceof int[]);
		assertArrayEquals(bargExpectedOutput(baseImage,intelByteOrder),(int[])pixels);
	}

	// FileInfo.BARG:
	//   sub cases
	//   compression
	//   intelByteOrder
	private void readBargFileType()
	{
		//Object pixels;
	
		// BARG uncompressed
		//pixels = readPixelHelper(FileInfo.BARG,FileInfo.COMPRESSION_NONE,3,3,Pix3x3barg);
		//assertNotNull(pixels);
		//assertTrue(pixels instanceof int[]);
		// notice the switch of expected pixels here from barg to argb
		//assertArrayEquals(Pix3x3argb,(int[])pixels);

		tryBargImage(BaseImage1x1,FileInfo.COMPRESSION_NONE,0,false);
		tryBargImage(BaseImage3x3,FileInfo.COMPRESSION_NONE,0,false);
		tryBargImage(BaseImage1x9,FileInfo.COMPRESSION_NONE,0,false);
		tryBargImage(BaseImage7x2,FileInfo.COMPRESSION_NONE,0,false);
		tryBargImage(BaseImage5x4,FileInfo.COMPRESSION_NONE,0,false);
		tryBargImage(BaseImage4x6,FileInfo.COMPRESSION_NONE,0,false);
		
		tryBargImage(BaseImage7x2,FileInfo.COMPRESSION_NONE,777,false);  // test header specifically
	}
	
	private byte[] rgbPlanarPixelBytes(int[][] inputData, FileInfo fi, int compression, int headerBytes, boolean intelByteOrder)
	{
		// set FileInfo fields for subsequent calls
		
		fi.fileType = FileInfo.RGB_PLANAR;
		fi.compression = compression;
		fi.intelByteOrder = intelByteOrder;
		fi.height = inputData.length;
		fi.width = inputData[0].length;
		
		byte[] output = new byte[fi.height * fi.width * 3];
		
		int i = 0;
		for (int[] row : inputData)
			for (int pix : row)
			{				
				output[(0*fi.width*fi.height)+i] = (byte)((pix & 0x00ff0000) >> 16);
				output[(1*fi.width*fi.height)+i] = (byte)((pix & 0x0000ff00) >> 8);
				output[(2*fi.width*fi.height)+i] = (byte)((pix & 0x000000ff) >> 0);
				i++;
			}
		
		if (intelByteOrder)
			; // nothing to do for a byte oriented data stream
		
		if (compression == FileInfo.LZW)
			; // output = lzwCompress(output); // compress the output data
		else if (compression == FileInfo.LZW_WITH_DIFFERENCING)
			; // output = lzwCompress(output); // compress the output data
		else if (compression == FileInfo.PACK_BITS)
			; // output = packBitsCompress(output); // compress the output data

		// else assuming its COMPRESSION_NONE : do nothing		
		
		// create a bogus header if desired
		//   note that this may not work correctly as skip is figured from strips too investigate further
		if (headerBytes > 0)
		{
			fi.offset = headerBytes;
			fi.longOffset = headerBytes;
			output = prependFakeHeader(headerBytes,output);
		}
		
		return output;
	}

	private int[] rgbPlanarExpectedOutput(int[][] image, boolean intelByteOrder)
	{
		int[] output = new int[image.length * image[0].length];
	
		// NOTICE input is rgb planar but output is argb
		int i = 0;
		for (int[] row : image)
			for (int pix : row)
				output[i++] = 0xff000000 | ((pix & 0xff0000) >> 16) | ((pix & 0xff00) >> 8) | ((pix & 0xff) >> 0);

		return output;
	}

	private void tryRgbPlanarImage(int[][] baseImage, int compression, int headerBytes, boolean intelByteOrder)
	{
		FileInfo fi = new FileInfo();
		byte[] pixInData = rgbPlanarPixelBytes(baseImage,fi,compression,headerBytes,intelByteOrder);
		Object pixels = callReadPixels(fi,pixInData);
		assertNotNull(pixels);
		assertTrue(pixels instanceof int[]);
		assertArrayEquals(rgbPlanarExpectedOutput(baseImage,intelByteOrder),(int[])pixels);
	}

	// FileInfo.RGB_PLANAR:
	//   sub cases
	//    compression
	private void readRgbPlanarFileType()
	{
		//Object pixels;
	
		// RGB_PLANAR uncompressed
		//pixels = readPixelHelper(FileInfo.RGB_PLANAR,FileInfo.COMPRESSION_NONE,3,3,Pix3x3rgbPlanar);
		//assertNotNull(pixels);
		//assertTrue(pixels instanceof int[]);
		// notice the switch of expected pixels here from barg to argb
		//assertArrayEquals(Pix3x3argb,(int[])pixels);

		tryRgbPlanarImage(BaseImage1x1,FileInfo.COMPRESSION_NONE,0,false);
		tryRgbPlanarImage(BaseImage3x3,FileInfo.COMPRESSION_NONE,0,false);
		tryRgbPlanarImage(BaseImage1x9,FileInfo.COMPRESSION_NONE,0,false);
		tryRgbPlanarImage(BaseImage7x2,FileInfo.COMPRESSION_NONE,0,false);
		tryRgbPlanarImage(BaseImage5x4,FileInfo.COMPRESSION_NONE,0,false);
		tryRgbPlanarImage(BaseImage4x6,FileInfo.COMPRESSION_NONE,0,false);
		
		tryRgbPlanarImage(BaseImage7x2,FileInfo.COMPRESSION_NONE,923,false);  // test header specifically
	}
	
	private byte[] bitmapPixelBytes(int[][] inputData, FileInfo fi, int compression, int headerBytes, boolean intelByteOrder)
	{
		// set FileInfo fields for subsequent calls
		
		fi.fileType = FileInfo.BITMAP;
		fi.compression = compression;
		fi.intelByteOrder = intelByteOrder;
		fi.height = inputData.length;
		fi.width = inputData[0].length;
		
		byte[] output = new byte[fi.height * fi.width];
		
		int i = 0;
		for (int[] row : inputData)
			for (int pix : row)
			{
				//int builtPix = 0;
				//for (int j = 7; j >= 0; j--)
				//{
				//	if ((pix & (1 << j)) != 0)
				//		builtPix |= (1 << j);
				//	output[i++] = (byte) builtPix;
				//}
				output[i++] = (byte)(pix & 0xff);
			}
		
		if (intelByteOrder)
			; // nothing to do for a byte oriented data stream
		
		if (compression == FileInfo.LZW)
			; // output = lzwCompress(output); // compress the output data
		else if (compression == FileInfo.LZW_WITH_DIFFERENCING)
			; // output = lzwCompress(output); // compress the output data
		else if (compression == FileInfo.PACK_BITS)
			; // output = packBitsCompress(output); // compress the output data

		// else assuming its COMPRESSION_NONE : do nothing		
		
		// create a bogus header if desired
		//   note that this may not work correctly as skip is figured from strips too investigate further
		if (headerBytes > 0)
		{
			fi.offset = headerBytes;
			fi.longOffset = headerBytes;
			output = prependFakeHeader(headerBytes,output);
		}
		
		return output;
	}

	private byte[] bitmapExpectedOutput(int[][] image, boolean intelByteOrder)
	{
		byte[] output = new byte[image.length * image[0].length];
	
		// from earlier testing it was case that
		//   input = {128,64,32}
		//   output = {255,0,0,0,255,0,0,0,255}
		// this following code does not seem to match this
		
		// NOTICE I am only using the lowest byte of the input data
		
		int i = 0;
		for (int[] row : image)
			for (int pix: row)
				output[i++] = (byte) (pix & 0xff);
		
		return output;
	}

	private void tryBitmapImage(int[][] baseImage, int compression, int headerBytes, boolean intelByteOrder)
	{
		FileInfo fi = new FileInfo();
		byte[] pixInData = bitmapPixelBytes(baseImage,fi,compression,headerBytes,intelByteOrder);
		Object pixels = callReadPixels(fi,pixInData);
		assertNotNull(pixels);
		assertTrue(pixels instanceof byte[]);
		assertArrayEquals(bitmapExpectedOutput(baseImage,intelByteOrder),(byte[])pixels);
	}

	// FileInfo.BITMAP:
	private void readBitmapFileType()
	{
		byte[] input = new byte[] {(byte)128,64,32};
		byte[] expectedOutput = {(byte)255, 0, 0, 0, (byte)255, 0, 0, 0, (byte)255};
	
		Object pixels;
	
		// BITMAP
		pixels = readPixelHelper(FileInfo.BITMAP,FileInfo.COMPRESSION_NONE,3,3,input);
		assertNotNull(pixels);
		assertTrue(pixels instanceof byte[]);
		assertArrayEquals(expectedOutput,(byte[])pixels);

//		tryBitmapImage(BaseImage1x1,FileInfo.COMPRESSION_NONE,0,false);
//		tryBitmapImage(BaseImage3x3,FileInfo.COMPRESSION_NONE,0,false);
//		tryBitmapImage(BaseImage1x9,FileInfo.COMPRESSION_NONE,0,false);
//		tryBitmapImage(BaseImage7x2,FileInfo.COMPRESSION_NONE,0,false);
//		tryBitmapImage(BaseImage5x4,FileInfo.COMPRESSION_NONE,0,false);
//		tryBitmapImage(BaseImage4x6,FileInfo.COMPRESSION_NONE,0,false);
		
//		tryBitmapImage(BaseImage7x2,FileInfo.COMPRESSION_NONE,386,false);  // test header specifically
	}
	
	//TODO
	// FileInfo.RGB48:
	//   sub cases
	//   compression
	//   intelByteOrder
	private void readRgb48FileType()
	{
		Object pixels;
	
		// RGB48 uncompressed
		byte[] inBytes = new byte[] {0,0,0,0,0,11,0,0,0,0,0,12,0,0,0,0,0,13,0,0,0,0,0,21,0,0,0,0,0,22,0,0,0,0,0,23,0,0,0,0,0,31,0,0,0,0,0,32,0,0,0,0,0,33};
		short[][] expShorts = new short[][] {{0,0,0,0,0,0,0,0,0},{0,0,0,0,0,0,0,0,0},{11,12,13,21,22,23,31,32,33}};
		FileInfo fi = new FileInfo();
		fi.fileType = FileInfo.RGB48;
		fi.compression = FileInfo.COMPRESSION_NONE;
		fi.width = 3;
		fi.height = 3;
		fi.stripLengths = new int[] {18,18,18};
		fi.stripOffsets = new int[] {0,18,36};
		ByteArrayInputStream stream = new ByteArrayInputStream(inBytes);
		ImageReader rdr = new ImageReader(fi);
		pixels = rdr.readPixels(stream);
		assertNotNull(pixels);
		assertTrue(pixels instanceof short[][]);
		short[][] outPix = (short[][])pixels;
		assertArrayEquals(expShorts,outPix);
	}
	
	//TODO
	// FileInfo.RGB48_PLANAR:
	//   sub cases
	//   compression
	//   intelByteOrder
	private void readRgb48PlanarFileType()
	{
		Object pixels;
		
		byte[] inBytes = new byte[] {0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,11,0,12,0,13,0,21,0,22,0,23,0,31,0,32,0,33};
		short[][] expShorts = new short[][] {{0,0,0,0,0,0,0,0,0},{0,0,0,0,0,0,0,0,0},{11,12,13,21,22,23,31,32,33}};

		// it reads three straight 16 bit images (red, then green, then blue)
		// compression needs to be tested
		// pixel channels are always treated as unsigned shorts
		
		FileInfo fi = new FileInfo();
		fi.fileType = FileInfo.RGB48_PLANAR;
		fi.compression = FileInfo.COMPRESSION_NONE;
		fi.width = 3;
		fi.height = 3;
		
		ByteArrayInputStream stream = new ByteArrayInputStream(inBytes);
		ImageReader rdr = new ImageReader(fi);
		pixels = rdr.readPixels(stream);
		
		assertNotNull(pixels);
		assertTrue(pixels instanceof Object[]);
		
		Object[] planeList = (Object[]) pixels;
		assertTrue(planeList[0] instanceof short[]);		
		assertTrue(planeList[1] instanceof short[]);		
		assertTrue(planeList[2] instanceof short[]);		
		
		assertArrayEquals(expShorts[0],(short[])planeList[0]);
		assertArrayEquals(expShorts[1],(short[])planeList[1]);
		assertArrayEquals(expShorts[2],(short[])planeList[2]);
	}
	
	// helper: input ints assumed to be in 12-bit range
	private byte[] encode12bit(int[] inPix)
	{
		int outSize = inPix.length * 12 / 8;
		
		if (inPix.length % 2 == 1)
			outSize++;
		
		byte[] output = new byte[outSize];
		
		for (int i = 0, o = 0; i < inPix.length; i++)
		{
			if (i%2 == 0) // even
			{
				// set this byte's 8 bits plus next byte's high 4 bits
				// use 12 bits of the input int
				output[o] = (byte)((inPix[i] & 0xff0) >> 4) ;
				output[o+1] = (byte)((inPix[i] & 0x00f) << 4);				
				o += 1;  // finished 1 pixel
			}
			else // odd
			{
				// set this byte's low 4 bits and next byte's 8 bits
				// use 12 bits of the input int
				output[o] = (byte)(output[o] | ((inPix[i] & 0xf00) >> 8));
				output[o+1] = (byte)(inPix[i] & 0x0ff);
				o += 2;  // finished 2 pixels
			}
		}
		
		return output;
	}
	
	//TODO
	// FileInfo.GRAY12_UNSIGNED:
	private void readGray12UnsignedFileType()
	{
		Object pixels;
				
		byte[] input = encode12bit(new int[] {11,12,13,14,21,22,23,24,31,32,33,34});
		short[] expected = new short[] {11,12,13,14,21,22,23,24,31,32,33,34};

		// GRAY12_UNSIGNED
		pixels = readPixelHelper(FileInfo.GRAY12_UNSIGNED,FileInfo.COMPRESSION_NONE,3,4,input);
		assertNotNull(pixels);
		assertTrue(pixels instanceof short[]);
		assertArrayEquals(expected,(short[])pixels);
		
		if (IJInfo.RUN_ENHANCED_TESTS)
		{
			// this commented code fails due to ImageReader bug for 12-bit images - can only handle even number of pixels
			input = encode12bit(new int[] {11,12,13,21,22,23,31,32,33});
			expected = new short[] {11,12,13,21,22,23,31,32,33};
			// GRAY12_UNSIGNED
			
			pixels = readPixelHelper(FileInfo.GRAY12_UNSIGNED,FileInfo.COMPRESSION_NONE,3,3,input);
			assertNotNull(pixels);
			assertTrue(pixels instanceof short[]);
			assertArrayEquals(expected,(short[])pixels);
		}
	}

	private byte[] gray24UnsignedPixelBytes(int[][] inputData, FileInfo fi, int compression, int headerBytes, boolean intelByteOrder)
	{
		// set FileInfo fields for subsequent calls
		
		fi.fileType = FileInfo.GRAY24_UNSIGNED;
		fi.compression = compression;
		fi.intelByteOrder = intelByteOrder;
		fi.height = inputData.length;
		fi.width = inputData[0].length;
		
		byte[] output = new byte[fi.height * fi.width * 3];
		
		// notice this code will not work for ints out of range 0..255
		int i = 0;
		for (int[] row : inputData)
			for (int pix : row)
			{
				output[i++] = (byte) pix;
				output[i++] = 0;
				output[i++] = 0;
			}
		
		if (intelByteOrder)
			; // nothing to do for a byte oriented data stream
		
		if (compression == FileInfo.LZW)
			; // output = lzwCompress(output); // compress the output data
		else if (compression == FileInfo.LZW_WITH_DIFFERENCING)
			; // output = lzwCompress(output); // compress the output data
		else if (compression == FileInfo.PACK_BITS)
			; // output = packBitsCompress(output); // compress the output data

		// else assuming its COMPRESSION_NONE : do nothing		
		
		// create a bogus header if desired
		//   note that this may not work correctly as skip is figured from strips too investigate further
		if (headerBytes > 0)
		{
			fi.offset = headerBytes;
			fi.longOffset = headerBytes;
			output = prependFakeHeader(headerBytes,output);
		}
		
		return output;
	}

	private float[] gray24UnsignedExpectedOutput(int[][] image, boolean intelByteOrder)
	{
		float[] output = new float[image.length * image[0].length];
			
		int i = 0;
		for (int[] row : image)
			for (int pix: row)
				output[i++] = (float)pix;
		
		return output;
	}

	private void tryGray24UnsignedImage(int[][] baseImage, int compression, int headerBytes, boolean intelByteOrder)
	{
		FileInfo fi = new FileInfo();
		byte[] pixInData = gray24UnsignedPixelBytes(baseImage,fi,compression,headerBytes,intelByteOrder);
		Object pixels = callReadPixels(fi,pixInData);
		assertNotNull(pixels);
		assertTrue(pixels instanceof float[]);
		lociAssertArrayEquals(gray24UnsignedExpectedOutput(baseImage,intelByteOrder),(float[])pixels);
	}

	// FileInfo.GRAY24_UNSIGNED:
	private void readGray24UnsignedFileType()
	{
		//byte[] input = new byte[] {11,0,0,12,0,0,13,0,0,21,0,0,22,0,0,23,0,0,31,0,0,32,0,0,33,0,0};
		//float[] expected = new float[] {11,12,13,21,22,23,31,32,33};
		
		//Object pixels;

		// GRAY24_UNSIGNED
		//pixels = readPixelHelper(FileInfo.GRAY24_UNSIGNED,FileInfo.COMPRESSION_NONE,3,3,input);
		//assertNotNull(pixels);
		//assertTrue(pixels instanceof float[]);
		//lociAssertArrayEquals(expected,(float[])pixels);

		tryGray24UnsignedImage(BaseImage1x1,FileInfo.COMPRESSION_NONE,0,false);
		tryGray24UnsignedImage(BaseImage3x3,FileInfo.COMPRESSION_NONE,0,false);
		tryGray24UnsignedImage(BaseImage1x9,FileInfo.COMPRESSION_NONE,0,false);
		tryGray24UnsignedImage(BaseImage7x2,FileInfo.COMPRESSION_NONE,0,false);
		tryGray24UnsignedImage(BaseImage5x4,FileInfo.COMPRESSION_NONE,0,false);
		tryGray24UnsignedImage(BaseImage4x6,FileInfo.COMPRESSION_NONE,0,false);
		
		tryGray24UnsignedImage(BaseImage7x2,FileInfo.COMPRESSION_NONE,885,false);  // test header specifically
	}
	
	@Test
	public void testReadPixelsFromInputStream() {
		readBogusFileType();
		readGray8FileType();
		readColor8FileType();
		readGray16SignedFileType();
		readGray16UnsignedFileType();
		readGray32IntFileType();
		readGray32UnsignedFileType();
		readGray32FloatFileType();
		readGray64FloatFileType();
		readRgbFileType();
		readBgrFileType();
		readArgbFileType();
		readArgbFileType();
		readBargFileType();
		readRgbPlanarFileType();
		readBitmapFileType();
		readRgb48FileType();
		readRgb48PlanarFileType();
		readGray12UnsignedFileType();
		readGray24UnsignedFileType();
	}

	@Test
	public void testReadPixelsFromInputStreamLong() {
		//fail("Not yet implemented");
	}

	@Test
	public void testReadPixelsFromURL() {
		//fail("Not yet implemented");
	}

	@Test
	public void testLzwUncompress() {
		//fail("Not yet implemented");
	}

	@Test
	public void testPackBitsUncompress() {
		//fail("Not yet implemented");
	}

	@Test
	public void testPublicIVarsMinAndMax() {
		//fail("Not yet implemented");
	}
}
