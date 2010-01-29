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
	
	private byte[] prependFakeHeader(int headerBytes, byte[] pixData)
	{
		byte[] header = new byte[headerBytes];
		byte[] output = new byte[header.length + pixData.length];
		System.arraycopy(header,0,output,0,header.length);
		System.arraycopy(pixData,0,output,header.length,pixData.length);
		return output;
	}
	
	// note: assumes everyX is even!
	private byte[] intelSwap(byte[] input, int everyX)
	{
		byte[] output = new byte[input.length];
		
		for (int i = 0; i < input.length; i += everyX)
			for (int j = 0; j < everyX; j++)
				output[i+j] = input[i+everyX-1-j];
		
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
	
		//if (intelByteOrder)
		//	; // nothing to do for a byte oriented data stream
		
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

		// GRAY8 uncompressed
		tryGray8Map(BaseImage1x1,FileInfo.COMPRESSION_NONE,0,false);
		tryGray8Map(BaseImage3x3,FileInfo.COMPRESSION_NONE,0,false);
		tryGray8Map(BaseImage1x9,FileInfo.COMPRESSION_NONE,0,false);
		tryGray8Map(BaseImage7x2,FileInfo.COMPRESSION_NONE,0,false);
		tryGray8Map(BaseImage5x4,FileInfo.COMPRESSION_NONE,0,false);
		tryGray8Map(BaseImage4x6,FileInfo.COMPRESSION_NONE,0,false);
		
		tryGray8Map(BaseImage7x2,FileInfo.COMPRESSION_NONE,63,false);  // test header specifically
		tryGray8Map(BaseImage7x2,FileInfo.COMPRESSION_NONE,0,true);    // test intel specifically
		
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
	
		//if (intelByteOrder)
		//	; // nothing to do for a byte oriented data stream
		
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

		// COLOR8 uncompressed
		tryColor8Map(BaseImage1x1,FileInfo.COMPRESSION_NONE,0,false);
		tryColor8Map(BaseImage3x3,FileInfo.COMPRESSION_NONE,0,false);
		tryColor8Map(BaseImage1x9,FileInfo.COMPRESSION_NONE,0,false);
		tryColor8Map(BaseImage7x2,FileInfo.COMPRESSION_NONE,0,false);
		tryColor8Map(BaseImage5x4,FileInfo.COMPRESSION_NONE,0,false);
		tryColor8Map(BaseImage4x6,FileInfo.COMPRESSION_NONE,0,false);
		
		tryColor8Map(BaseImage7x2,FileInfo.COMPRESSION_NONE,63,false);  // test header specifically
		tryColor8Map(BaseImage7x2,FileInfo.COMPRESSION_NONE,0,true);    // test intel specifically
		
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
			output = intelSwap(output,2);
		
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

		// GRAY16_SIGNED uncompressed
		tryGray16SignedImage(BaseImage1x1,FileInfo.COMPRESSION_NONE,0,false);
		tryGray16SignedImage(BaseImage3x3,FileInfo.COMPRESSION_NONE,0,false);
		tryGray16SignedImage(BaseImage1x9,FileInfo.COMPRESSION_NONE,0,false);
		tryGray16SignedImage(BaseImage7x2,FileInfo.COMPRESSION_NONE,0,false);
		tryGray16SignedImage(BaseImage5x4,FileInfo.COMPRESSION_NONE,0,false);
		tryGray16SignedImage(BaseImage4x6,FileInfo.COMPRESSION_NONE,0,false);
		
		tryGray16SignedImage(BaseImage7x2,FileInfo.COMPRESSION_NONE,1003,false);  // test header specifically
		tryGray16SignedImage(BaseImage7x2,FileInfo.COMPRESSION_NONE,0,true);      // test intel specifically

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
			output = intelSwap(output,2);
		
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
		// GRAY16_UNSIGNED uncompressed
		tryGray16UnsignedImage(BaseImage1x1,FileInfo.COMPRESSION_NONE,0,false);
		tryGray16UnsignedImage(BaseImage3x3,FileInfo.COMPRESSION_NONE,0,false);
		tryGray16UnsignedImage(BaseImage1x9,FileInfo.COMPRESSION_NONE,0,false);
		tryGray16UnsignedImage(BaseImage7x2,FileInfo.COMPRESSION_NONE,0,false);
		tryGray16UnsignedImage(BaseImage5x4,FileInfo.COMPRESSION_NONE,0,false);
		tryGray16UnsignedImage(BaseImage4x6,FileInfo.COMPRESSION_NONE,0,false);
		
		tryGray16UnsignedImage(BaseImage7x2,FileInfo.COMPRESSION_NONE,16,false);  // test header specifically
		tryGray16UnsignedImage(BaseImage7x2,FileInfo.COMPRESSION_NONE,0,true);    // test intel specifically

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
			output = intelSwap(output,4);
		
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
		// GRAY32_INT
		tryGray32IntImage(BaseImage1x1,FileInfo.COMPRESSION_NONE,0,false);
		tryGray32IntImage(BaseImage3x3,FileInfo.COMPRESSION_NONE,0,false);
		tryGray32IntImage(BaseImage1x9,FileInfo.COMPRESSION_NONE,0,false);
		tryGray32IntImage(BaseImage7x2,FileInfo.COMPRESSION_NONE,0,false);
		tryGray32IntImage(BaseImage5x4,FileInfo.COMPRESSION_NONE,0,false);
		tryGray32IntImage(BaseImage4x6,FileInfo.COMPRESSION_NONE,0,false);
		
		tryGray32IntImage(BaseImage7x2,FileInfo.COMPRESSION_NONE,404,false);  // test header specifically
		tryGray32IntImage(BaseImage7x2,FileInfo.COMPRESSION_NONE,0,true);     // test intel specifically
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
			output = intelSwap(output,4);
		
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
		// GRAY32_UNSIGNED
		tryGray32UnsignedImage(BaseImage1x1,FileInfo.COMPRESSION_NONE,0,false);
		tryGray32UnsignedImage(BaseImage3x3,FileInfo.COMPRESSION_NONE,0,false);
		tryGray32UnsignedImage(BaseImage1x9,FileInfo.COMPRESSION_NONE,0,false);
		tryGray32UnsignedImage(BaseImage7x2,FileInfo.COMPRESSION_NONE,0,false);
		tryGray32UnsignedImage(BaseImage5x4,FileInfo.COMPRESSION_NONE,0,false);
		tryGray32UnsignedImage(BaseImage4x6,FileInfo.COMPRESSION_NONE,0,false);
		
		tryGray32UnsignedImage(BaseImage7x2,FileInfo.COMPRESSION_NONE,101,false);  // test header specifically
		tryGray32UnsignedImage(BaseImage7x2,FileInfo.COMPRESSION_NONE,0,true);     // test intel specifically
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
			output = intelSwap(output,4);
		
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
		// GRAY32_FLOAT
		tryGray32FloatImage(BaseImage1x1,FileInfo.COMPRESSION_NONE,0,false);
		tryGray32FloatImage(BaseImage3x3,FileInfo.COMPRESSION_NONE,0,false);
		tryGray32FloatImage(BaseImage1x9,FileInfo.COMPRESSION_NONE,0,false);
		tryGray32FloatImage(BaseImage7x2,FileInfo.COMPRESSION_NONE,0,false);
		tryGray32FloatImage(BaseImage5x4,FileInfo.COMPRESSION_NONE,0,false);
		tryGray32FloatImage(BaseImage4x6,FileInfo.COMPRESSION_NONE,0,false);
		
		tryGray32FloatImage(BaseImage7x2,FileInfo.COMPRESSION_NONE,611,false);  // test header specifically
		tryGray32FloatImage(BaseImage7x2,FileInfo.COMPRESSION_NONE,0,true);     // test intel specifically
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
			output = intelSwap(output,8);
		
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
		// GRAY64_FLOAT
		tryGray64FloatImage(BaseImage1x1,FileInfo.COMPRESSION_NONE,0,false);
		tryGray64FloatImage(BaseImage3x3,FileInfo.COMPRESSION_NONE,0,false);
		tryGray64FloatImage(BaseImage1x9,FileInfo.COMPRESSION_NONE,0,false);
		tryGray64FloatImage(BaseImage7x2,FileInfo.COMPRESSION_NONE,0,false);
		tryGray64FloatImage(BaseImage5x4,FileInfo.COMPRESSION_NONE,0,false);
		tryGray64FloatImage(BaseImage4x6,FileInfo.COMPRESSION_NONE,0,false);
		
		tryGray64FloatImage(BaseImage7x2,FileInfo.COMPRESSION_NONE,382,false);  // test header specifically
		tryGray64FloatImage(BaseImage7x2,FileInfo.COMPRESSION_NONE,0,true);     // test intel specifically
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
		tryRgbImage(BaseImage1x1,FileInfo.COMPRESSION_NONE,0,false);
		tryRgbImage(BaseImage3x3,FileInfo.COMPRESSION_NONE,0,false);
		tryRgbImage(BaseImage1x9,FileInfo.COMPRESSION_NONE,0,false);
		tryRgbImage(BaseImage7x2,FileInfo.COMPRESSION_NONE,0,false);
		tryRgbImage(BaseImage5x4,FileInfo.COMPRESSION_NONE,0,false);
		tryRgbImage(BaseImage4x6,FileInfo.COMPRESSION_NONE,0,false);
		
		tryRgbImage(BaseImage7x2,FileInfo.COMPRESSION_NONE,873,false);  // test header specifically
		tryRgbImage(BaseImage7x2,FileInfo.COMPRESSION_NONE,0,true);     // test intel specifically
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
		// BGR uncompressed

		tryBgrImage(BaseImage1x1,FileInfo.COMPRESSION_NONE,0,false);
		tryBgrImage(BaseImage3x3,FileInfo.COMPRESSION_NONE,0,false);
		tryBgrImage(BaseImage1x9,FileInfo.COMPRESSION_NONE,0,false);
		tryBgrImage(BaseImage7x2,FileInfo.COMPRESSION_NONE,0,false);
		tryBgrImage(BaseImage5x4,FileInfo.COMPRESSION_NONE,0,false);
		tryBgrImage(BaseImage4x6,FileInfo.COMPRESSION_NONE,0,false);
		
		tryBgrImage(BaseImage7x2,FileInfo.COMPRESSION_NONE,873,false);  // test header specifically
		tryBgrImage(BaseImage7x2,FileInfo.COMPRESSION_NONE,0,true);     // test intel specifically
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
				if (intelByteOrder)
				{
					output[4*i+0] = (byte)((pix & 0x00ff0000) >> 16);
					output[4*i+1] = (byte)((pix & 0x0000ff00) >> 8);
					output[4*i+2] = (byte)((pix & 0x000000ff) >> 0);
					output[4*i+3] = (byte)((pix & 0xff000000) >> 24);
				}
				else
				{
					output[4*i+0] = (byte)((pix & 0xff000000) >> 24);
					output[4*i+1] = (byte)((pix & 0x00ff0000) >> 16);
					output[4*i+2] = (byte)((pix & 0x0000ff00) >> 8);
					output[4*i+3] = (byte)((pix & 0x000000ff) >> 0);
				}
				i++;
			}
		
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
		// ARGB uncompressed
		tryArgbImage(BaseImage1x1,FileInfo.COMPRESSION_NONE,0,false);
		tryArgbImage(BaseImage3x3,FileInfo.COMPRESSION_NONE,0,false);
		tryArgbImage(BaseImage1x9,FileInfo.COMPRESSION_NONE,0,false);
		tryArgbImage(BaseImage7x2,FileInfo.COMPRESSION_NONE,0,false);
		tryArgbImage(BaseImage5x4,FileInfo.COMPRESSION_NONE,0,false);
		tryArgbImage(BaseImage4x6,FileInfo.COMPRESSION_NONE,0,false);
		
		tryArgbImage(BaseImage7x2,FileInfo.COMPRESSION_NONE,501,false);  // test header specifically
		tryArgbImage(BaseImage7x2,FileInfo.COMPRESSION_NONE,0,true);     // test intel specifically
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
		// ABGR uncompressed

		tryAbgrImage(BaseImage1x1,FileInfo.COMPRESSION_NONE,0,false);
		tryAbgrImage(BaseImage3x3,FileInfo.COMPRESSION_NONE,0,false);
		tryAbgrImage(BaseImage1x9,FileInfo.COMPRESSION_NONE,0,false);
		tryAbgrImage(BaseImage7x2,FileInfo.COMPRESSION_NONE,0,false);
		tryAbgrImage(BaseImage5x4,FileInfo.COMPRESSION_NONE,0,false);
		tryAbgrImage(BaseImage4x6,FileInfo.COMPRESSION_NONE,0,false);
		
		tryAbgrImage(BaseImage7x2,FileInfo.COMPRESSION_NONE,222,false);  // test header specifically
		tryAbgrImage(BaseImage7x2,FileInfo.COMPRESSION_NONE,0,true);     // test intel specifically
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
		// BARG uncompressed

		tryBargImage(BaseImage1x1,FileInfo.COMPRESSION_NONE,0,false);
		tryBargImage(BaseImage3x3,FileInfo.COMPRESSION_NONE,0,false);
		tryBargImage(BaseImage1x9,FileInfo.COMPRESSION_NONE,0,false);
		tryBargImage(BaseImage7x2,FileInfo.COMPRESSION_NONE,0,false);
		tryBargImage(BaseImage5x4,FileInfo.COMPRESSION_NONE,0,false);
		tryBargImage(BaseImage4x6,FileInfo.COMPRESSION_NONE,0,false);
		
		tryBargImage(BaseImage7x2,FileInfo.COMPRESSION_NONE,777,false);  // test header specifically
		tryBargImage(BaseImage7x2,FileInfo.COMPRESSION_NONE,0,true);     // test intel specifically
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
		// RGB_PLANAR uncompressed

		tryRgbPlanarImage(BaseImage1x1,FileInfo.COMPRESSION_NONE,0,false);
		tryRgbPlanarImage(BaseImage3x3,FileInfo.COMPRESSION_NONE,0,false);
		tryRgbPlanarImage(BaseImage1x9,FileInfo.COMPRESSION_NONE,0,false);
		tryRgbPlanarImage(BaseImage7x2,FileInfo.COMPRESSION_NONE,0,false);
		tryRgbPlanarImage(BaseImage5x4,FileInfo.COMPRESSION_NONE,0,false);
		tryRgbPlanarImage(BaseImage4x6,FileInfo.COMPRESSION_NONE,0,false);
		
		tryRgbPlanarImage(BaseImage7x2,FileInfo.COMPRESSION_NONE,923,false);  // test header specifically
		tryRgbPlanarImage(BaseImage7x2,FileInfo.COMPRESSION_NONE,0,true);     // test intel specifically
	}
	
	private byte[] bitmapPixelBytes(int[][] inputData, FileInfo fi, int compression, int headerBytes, boolean intelByteOrder)
	{
		int rows = inputData.length;
		int cols = inputData[0].length;
		
		// set FileInfo fields for subsequent calls
		
		fi.fileType = FileInfo.BITMAP;
		fi.compression = compression;
		fi.intelByteOrder = intelByteOrder;
		fi.height = inputData.length;
		fi.width = inputData[0].length;
		
		int pixPerRow = (int) Math.ceil(fi.width / 8.0);
		
		byte[] output = new byte[fi.height * pixPerRow];

		// note that I am only using the lowest 1 bit of the int for testing purposes
		
		int i = 0;
		byte currByte = 0;
		
		for (int r = 0; r < rows; r++)
			for (int c = 0; c < cols; c++)
			{
				if ((inputData[r][c] & 1) == 1) // if odd
					currByte |= (1 << (7-(c%8)));
				if (((c%8) == 7) || (c == (cols-1)))
				{
					output[i++] = currByte;
					currByte = 0;
				}
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
		int rows = image.length;
		int cols = image[0].length;
		
		byte[] output = new byte[rows * cols];
	
		// from earlier testing it was case that
		//   input = {128,64,32}
		//   output = {255,0,0,0,255,0,0,0,255}
		// this following code does not seem to match this
		
		int i = 0;
		for (int r = 0; r < rows; r++)
			for (int c = 0; c < cols; c++)
				output[i++] = ((image[r][c] & 1) == 1) ? (byte)255 : 0;
		
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
		// BITMAP

		tryBitmapImage(BaseImage1x1,FileInfo.COMPRESSION_NONE,0,false);
		tryBitmapImage(BaseImage3x3,FileInfo.COMPRESSION_NONE,0,false);
		tryBitmapImage(BaseImage1x9,FileInfo.COMPRESSION_NONE,0,false);
		tryBitmapImage(BaseImage7x2,FileInfo.COMPRESSION_NONE,0,false);
		tryBitmapImage(BaseImage5x4,FileInfo.COMPRESSION_NONE,0,false);
		tryBitmapImage(BaseImage4x6,FileInfo.COMPRESSION_NONE,0,false);
		
		tryBitmapImage(BaseImage7x2,FileInfo.COMPRESSION_NONE,386,false);  // test header specifically
		tryBitmapImage(BaseImage7x2,FileInfo.COMPRESSION_NONE,0,true);     // test intel specifically
	}
	
	private byte[] rgb48PixelBytes(int[][] inputData, FileInfo fi, int compression, int headerBytes, boolean intelByteOrder)
	{
		// set FileInfo fields for subsequent calls
		
		fi.fileType = FileInfo.RGB48;
		fi.compression = compression;
		fi.intelByteOrder = intelByteOrder;
		fi.height = inputData.length;
		fi.width = inputData[0].length;
		
		byte[] output = new byte[fi.height * fi.width * 6];

		// note that I am only using the lowest 8 bits of the int for testing purposes
		
		int i = 0;
		for (int[] row : inputData)
			for (int wholeInt : row)
			{
				for (int n = 0; n < 5; n++)
					output[i++] = 0;
				output[i++] = (byte) (wholeInt & 0xff);
			}
		
		if (intelByteOrder)
			output = intelSwap(output,2);
		
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

	private short[][] rgb48ExpectedOutput(int[][] image, boolean intelByteOrder)
	{
		int rows = image.length;
		int cols = image[0].length;

		short[][] output = new short[3][];
		
		for (int i = 0; i < 3; i++)
			output[i] = new short[rows*cols];
		
		// at this point three arrays representing the 16 bit planes and all are zero
		
		// set the last plane of data
		int i = 0;
		for (int r = 0; r < rows; r++)
			for (int c = 0; c < cols; c++)
				output[2][i++] = (short) (image[r][c] & 0xff);  // NOTICE I am only using the lowest byte of the input data
		
		return output;
	}

	private void tryRgb48Image(int[][] baseImage, int compression, int headerBytes, boolean intelByteOrder)
	{
		FileInfo fi = new FileInfo();
		byte[] pixInData = rgb48PixelBytes(baseImage,fi,compression,headerBytes,intelByteOrder);
		
		// Set strip info: Needed for this case
		
		// lets do one strip per row
		fi.stripLengths = new int[baseImage.length];
		fi.stripOffsets = new int[baseImage.length];
		for (int i = 0; i < baseImage.length; i++)
		{
			fi.stripLengths[i] = 6*baseImage[0].length;
			fi.stripOffsets[i] = 6*i;
		}
		
		Object pixels = callReadPixels(fi,pixInData);
		assertNotNull(pixels);
		assertTrue(pixels instanceof short[][]);
		assertArrayEquals(rgb48ExpectedOutput(baseImage,intelByteOrder),(short[][])pixels);
	}

	// FileInfo.RGB48:
	//   sub cases
	//   compression
	//   intelByteOrder
	private void readRgb48FileType()
	{
		// RGB48 uncompressed

		tryRgb48Image(BaseImage1x1,FileInfo.COMPRESSION_NONE,0,false);
		tryRgb48Image(BaseImage3x3,FileInfo.COMPRESSION_NONE,0,false);
		tryRgb48Image(BaseImage1x9,FileInfo.COMPRESSION_NONE,0,false);
		tryRgb48Image(BaseImage7x2,FileInfo.COMPRESSION_NONE,0,false);
		tryRgb48Image(BaseImage5x4,FileInfo.COMPRESSION_NONE,0,false);
		tryRgb48Image(BaseImage4x6,FileInfo.COMPRESSION_NONE,0,false);
		
		tryRgb48Image(BaseImage7x2,FileInfo.COMPRESSION_NONE,559,false);  // test header specifically
		tryRgb48Image(BaseImage7x2,FileInfo.COMPRESSION_NONE,0,true);     // test intel specifically
	}
	

	private byte[] rgb48PlanarPixelBytes(int[][] inputData, FileInfo fi, int compression, int headerBytes, boolean intelByteOrder)
	{
		int rows = inputData.length;
		int cols = inputData[0].length;
		
		// set FileInfo fields for subsequent calls
		
		fi.fileType = FileInfo.RGB48_PLANAR;
		fi.compression = compression;
		fi.intelByteOrder = intelByteOrder;
		fi.height = inputData.length;
		fi.width = inputData[0].length;
		
		byte[] output = new byte[fi.height * fi.width * 6];

		// note that I am only using the lowest 8 bits of the int for testing purposes
		
		int i = 0;
		
		while (i < 2*2*rows*cols)
		{
			output[i++] = 0;
		}
		
		for (int[] row : inputData)
			for (int wholeInt : row)
			{
				output[i++] = 0;
				output[i++] = (byte) (wholeInt & 0xff);
			}
		
		if (intelByteOrder)
			output = intelSwap(output,2);
		
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

	private short[][] rgb48PlanarExpectedOutput(int[][] image, boolean intelByteOrder)
	{
		int rows = image.length;
		int cols = image[0].length;

		short[][] output = new short[3][];
		
		for (int i = 0; i < 3; i++)
			output[i] = new short[rows*cols];
		
		// at this point three arrays representing the 16 bit planes and all are zero
		
		// set the last plane of data
		int i = 0;
		for (int r = 0; r < rows; r++)
			for (int c = 0; c < cols; c++)
				output[2][i++] = (short) (image[r][c] & 0xffff);  // NOTICE I am only using the lowest word of the input data
		
		return output;
	}

	private void tryRgb48PlanarImage(int[][] baseImage, int compression, int headerBytes, boolean intelByteOrder)
	{
		FileInfo fi = new FileInfo();
		byte[] pixInData = rgb48PlanarPixelBytes(baseImage,fi,compression,headerBytes,intelByteOrder);
		
		// Set strip info: Needed for this case - If I add this test dies cuz readPixels() thinks data is compressed
		
		// lets do one strip per row
		//fi.stripLengths = new int[baseImage.length];
		//fi.stripOffsets = new int[baseImage.length];
		//for (int i = 0; i < baseImage.length; i++)
		//{
		//	fi.stripLengths[i] = 6*baseImage[0].length;
		//	fi.stripOffsets[i] = 6*baseImage[0].length*i;
		//}
		
		Object pixels = callReadPixels(fi,pixInData);
		assertNotNull(pixels);
		assertTrue(pixels instanceof Object[]);
		
		Object[] planeList = (Object[]) pixels;
		assertTrue(planeList[0] instanceof short[]);		
		assertTrue(planeList[1] instanceof short[]);		
		assertTrue(planeList[2] instanceof short[]);		
		
		short[][] expShorts = rgb48PlanarExpectedOutput(baseImage,intelByteOrder);
		
		assertNotNull(expShorts);
		assertTrue(expShorts.length == 3);
		assertArrayEquals(expShorts[0],(short[])planeList[0]);
		assertArrayEquals(expShorts[1],(short[])planeList[1]);
		assertArrayEquals(expShorts[2],(short[])planeList[2]);
	}

	// FileInfo.RGB48_PLANAR:
	//   sub cases
	//   compression
	//   intelByteOrder
	private void readRgb48PlanarFileType()
	{
		tryRgb48PlanarImage(BaseImage1x1,FileInfo.COMPRESSION_NONE,0,false);
		tryRgb48PlanarImage(BaseImage3x3,FileInfo.COMPRESSION_NONE,0,false);
		tryRgb48PlanarImage(BaseImage1x9,FileInfo.COMPRESSION_NONE,0,false);
		tryRgb48PlanarImage(BaseImage7x2,FileInfo.COMPRESSION_NONE,0,false);
		tryRgb48PlanarImage(BaseImage5x4,FileInfo.COMPRESSION_NONE,0,false);
		tryRgb48PlanarImage(BaseImage4x6,FileInfo.COMPRESSION_NONE,0,false);
		
		tryRgb48PlanarImage(BaseImage7x2,FileInfo.COMPRESSION_NONE,559,false);  // test header specifically
		tryRgb48PlanarImage(BaseImage7x2,FileInfo.COMPRESSION_NONE,0,true);     // test intel specifically
	}
	
	// helper: input ints assumed to be in 12-bit range
	private byte[] encode12bit(int[][] inPix)
	{
		int rows = inPix.length;
		int cols = inPix[0].length;
		
		int bytesPerRow = (int) Math.ceil(cols * 1.5); // 1.5 bytes per pix
		
		byte[] output = new byte[rows * bytesPerRow];

		int o = 0;
		for (int r = 0; r < rows; r++)
			for (int c = 0; c < cols; c++)
				if (c%2 == 0) // even numbered column
				{
					// set this byte's 8 bits plus next byte's high 4 bits
					// use 12 bits of the input int
					output[o] = (byte)((inPix[r][c] & 0xff0) >> 4) ;
					output[o+1] = (byte)((inPix[r][c] & 0x00f) << 4);				
					o += 1;  // finished 1 pixel
					if (c == cols-1)
						o += 1; // if end of row then next pixel completed also
				}
				else // odd numbered column
				{
					// set this byte's low 4 bits and next byte's 8 bits
					// use 12 bits of the input int
					output[o] = (byte)(output[o] | ((inPix[r][c] & 0xf00) >> 8));
					output[o+1] = (byte)(inPix[r][c] & 0x0ff);
					o += 2;  // finished 2 pixels
				}
		
		return output;
	}
	

	private byte[] gray12UnsignedPixelBytes(int[][] inputData, FileInfo fi, int compression, int headerBytes, boolean intelByteOrder)
	{		
		// set FileInfo fields for subsequent calls
		
		fi.fileType = FileInfo.GRAY12_UNSIGNED;
		fi.compression = compression;
		fi.intelByteOrder = intelByteOrder;
		fi.height = inputData.length;
		fi.width = inputData[0].length;
		
		byte[] output = encode12bit(inputData);
		
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

	private short[] gray12UnsignedExpectedOutput(int[][] image, boolean intelByteOrder)
	{
		short[] output = new short[image.length * image[0].length];
			
		int i = 0;
		for (int[] row : image)
			for (int pix: row)
				output[i++] = (short)pix;
		
		return output;
	}

	private void tryGray12UnsignedImage(int[][] baseImage, int compression, int headerBytes, boolean intelByteOrder)
	{
		FileInfo fi = new FileInfo();
		byte[] pixInData = gray12UnsignedPixelBytes(baseImage,fi,compression,headerBytes,intelByteOrder);
		Object pixels = callReadPixels(fi,pixInData);
		assertNotNull(pixels);
		assertTrue(pixels instanceof short[]);
		assertArrayEquals(gray12UnsignedExpectedOutput(baseImage,intelByteOrder),(short[])pixels);
	}

	// FileInfo.GRAY12_UNSIGNED:
	private void readGray12UnsignedFileType()
	{
		// GRAY12_UNSIGNED

		tryGray12UnsignedImage(BaseImage1x1,FileInfo.COMPRESSION_NONE,0,false);
		tryGray12UnsignedImage(BaseImage3x3,FileInfo.COMPRESSION_NONE,0,false);
		tryGray12UnsignedImage(BaseImage1x9,FileInfo.COMPRESSION_NONE,0,false);
		tryGray12UnsignedImage(BaseImage7x2,FileInfo.COMPRESSION_NONE,0,false);
		tryGray12UnsignedImage(BaseImage5x4,FileInfo.COMPRESSION_NONE,0,false);
		tryGray12UnsignedImage(BaseImage4x6,FileInfo.COMPRESSION_NONE,0,false);
		
		tryGray12UnsignedImage(BaseImage7x2,FileInfo.COMPRESSION_NONE,885,false);  // test header specifically
		tryGray12UnsignedImage(BaseImage7x2,FileInfo.COMPRESSION_NONE,0,true);     // test intel specifically
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
		// GRAY24_UNSIGNED

		tryGray24UnsignedImage(BaseImage1x1,FileInfo.COMPRESSION_NONE,0,false);
		tryGray24UnsignedImage(BaseImage3x3,FileInfo.COMPRESSION_NONE,0,false);
		tryGray24UnsignedImage(BaseImage1x9,FileInfo.COMPRESSION_NONE,0,false);
		tryGray24UnsignedImage(BaseImage7x2,FileInfo.COMPRESSION_NONE,0,false);
		tryGray24UnsignedImage(BaseImage5x4,FileInfo.COMPRESSION_NONE,0,false);
		tryGray24UnsignedImage(BaseImage4x6,FileInfo.COMPRESSION_NONE,0,false);
		
		tryGray24UnsignedImage(BaseImage7x2,FileInfo.COMPRESSION_NONE,885,false);  // test header specifically
		tryGray24UnsignedImage(BaseImage7x2,FileInfo.COMPRESSION_NONE,0,true);     // test intel specifically
	}
	
	// if time were less of a factor a refactor would be nice
	//   make readXFileType a class/interface
	//   define each one as an extension/impl
	//   keep an array of readers
	//   keep an array of test images
	//   iterate over images, applying all readers, with all values of flags
	// then adding a new image (might happen regularly) or a new filetype (less often?) is a lot easier to accommodate
	
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
