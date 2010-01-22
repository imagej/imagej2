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
	// file type ABGR is stored as BGRA! go figure
	static final int[] Pix3x3abgr = new int[] {0x110000ff, 0x120000ff, 0x130000ff, 0x210000ff, 0x220000ff, 0x230000ff, 0x310000ff, 0x320000ff, 0x330000ff};
	static final byte[] Pix3x3rgbPlanar = new byte[] {00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,0x11,0x12,0x13,0x21,0x22,0x23,0x31,0x32,0x33};
	static final byte[] Pix3x3bitmap = new byte[] {};
	
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
		for (int i=0; i < bytes.length; i++)
			bv.add(bytes[i]);
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
	
	// FileInfo.GRAY8
	//   subcases:
	//     compression
	//     skip()
	private void readGray8FileType()
	{
		Object pixels;

		// GRAY8 uncompressed
		pixels = readPixelHelper(FileInfo.GRAY8,FileInfo.COMPRESSION_NONE,3,3,Pix3x3byte);

		assertNotNull(pixels);
		assertTrue(pixels instanceof byte[]);
		assertArrayEquals(Pix3x3byte,(byte[])pixels);
		
		// GRAY8 compressed LZW
		// GRAY8 compressed LZW_WITH_DIFFERENCING
		// GRAY8 compressed PACK_BITS
	}

	// FileInfo.COLOR8
	//   subcases:
	//     compression
	//     skip()
	private void readColor8FileType()
	{
		Object pixels;

		// COLOR8 uncompressed
		pixels = readPixelHelper(FileInfo.COLOR8,FileInfo.COMPRESSION_NONE,3,3,Pix3x3byte);
		assertNotNull(pixels);
		assertTrue(pixels instanceof byte[]);
		assertArrayEquals(Pix3x3byte,(byte[])pixels);

		// COLOR8 compressed LZW		
		// COLOR8 compressed LZW_WITH_DIFFERENCING
		// COLOR8 compressed PACK_BITS
	}

	// FileInfo.GRAY16_SIGNED
	//   subcases:
	//     compression
	//     fi.intelByteOrder
	private void readGray16SignedFileType()
	{
		Object pixels;

		// GRAY16_SIGNED uncompressed
		pixels = readPixelHelper(FileInfo.GRAY16_SIGNED,FileInfo.COMPRESSION_NONE,3,3,Pix3x3short);

		// MAY NEED TO REDESIGN THIS TEST:
		//   readPixels() biases input GRAY16_SIGNED bytes by 32768 - match behavior in test
		short[] inPixelsXformed = new short[Pix3x3short.length];
		for (int i = 0; i < inPixelsXformed.length; i++)
			inPixelsXformed[i] = (short) (32768 + Pix3x3short[i]);

		assertNotNull(pixels);
		assertTrue(pixels instanceof short[]);
		assertArrayEquals(inPixelsXformed,(short[])pixels);

		// GRAY_SIGNED LZW
		
		// GRAY_SIGNED LZW_WITH_DIFFERENCING
		
		// GRAY_SIGNED PACK_BITS
	}
	
	// FileInfo.GRAY16_UNSIGNED
	//   subcases:
	//     compression
	//     fi.intelByteOrder
	private void readGray16UnsignedFileType()
	{
		Object pixels;

		// GRAY16_UNSIGNED uncompressed
		pixels = readPixelHelper(FileInfo.GRAY16_UNSIGNED,FileInfo.COMPRESSION_NONE,3,3,Pix3x3short);
		assertNotNull(pixels);
		assertTrue(pixels instanceof short[]);
		assertArrayEquals(Pix3x3short,(short[])pixels);

		// GRAY_UNSIGNED LZW
		
		// GRAY_UNSIGNED LZW_WITH_DIFFERENCING
		
		// GRAY_UNSIGNED PACK_BITS
	}
	
	// FileInfo.GRAY32_INT
	//  subcases
	//  intelByteOrder?
	private void readGray32IntFileType()
	{
		Object pixels;

		// GRAY32_INT
		pixels = readPixelHelper(FileInfo.GRAY32_INT,FileInfo.COMPRESSION_NONE,3,3,Pix3x3int);
		assertNotNull(pixels);
		assertTrue(pixels instanceof float[]);
		assertTrue(Pix3x3int.length == ((float[])pixels).length);
		for (int i = 0; i < Pix3x3int.length; i++)
			assertEquals((float)Pix3x3int[i],((float[])pixels)[i],FLOAT_TOL);
	}
	
	// FileInfo.GRAY32_UNSIGNED
	//  subcases
	//  intelByteOrder?
	private void readGray32UnsignedFileType()
	{
		Object pixels;

		// GRAY32_UNSIGNED
		pixels = readPixelHelper(FileInfo.GRAY32_UNSIGNED,FileInfo.COMPRESSION_NONE,3,3,Pix3x3int);
		assertNotNull(pixels);
		assertTrue(pixels instanceof float[]);
		assertTrue(Pix3x3int.length == ((float[])pixels).length);
		for (int i = 0; i < Pix3x3int.length; i++)
			assertEquals((float)Pix3x3int[i],((float[])pixels)[i],FLOAT_TOL);
	}
	
	// FileInfo.GRAY32_FLOAT
	//  subcases
	//  intelByteOrder?
	private void readGray32FloatFileType()
	{
		Object pixels;

		// GRAY32_FLOAT
		pixels = readPixelHelper(FileInfo.GRAY32_FLOAT,FileInfo.COMPRESSION_NONE,3,3,Pix3x3float);
		assertNotNull(pixels);
		assertTrue(pixels instanceof float[]);
		assertTrue(Pix3x3float.length == ((float[])pixels).length);
		for (int i = 0; i < Pix3x3float.length; i++)
			assertEquals((float)Pix3x3float[i],((float[])pixels)[i],FLOAT_TOL);
	}
	
	// FileInfo.GRAY64_FLOAT
	//   sub cases
	//   intelByteOrder
	private void readGray64FloatFileType(){
		Object pixels;

		// GRAY64_FLOAT
		pixels = readPixelHelper(FileInfo.GRAY64_FLOAT,FileInfo.COMPRESSION_NONE,3,3,Pix3x3double);
		assertNotNull(pixels);
		assertTrue(pixels instanceof float[]);
		assertTrue(Pix3x3double.length == ((float[])pixels).length);
		for (int i = 0; i < Pix3x3double.length; i++)
			assertEquals((float)Pix3x3double[i],((float[])pixels)[i],FLOAT_TOL);
	}
	
	// FileInfo.RGB:
	//   sub cases
	//   compression
	//   intelByteOrder
	private void readRGBFileType()
	{
		Object pixels;

		// RGB uncompressed
		pixels = readPixelHelper(FileInfo.RGB,FileInfo.COMPRESSION_NONE,3,3,Pix3x3rgb);
		assertNotNull(pixels);
		assertTrue(pixels instanceof int[]);
		// notice the switch of expected pixels here from rgb to argb
		assertArrayEquals(Pix3x3argb,(int[])pixels);
	}
	
	// FileInfo.BGR:
	//   sub cases
	//   compression
	//   intelByteOrder
	private void readBGRFileType()
	{
		Object pixels;

		// BGR uncompressed
		pixels = readPixelHelper(FileInfo.BGR,FileInfo.COMPRESSION_NONE,3,3,Pix3x3bgr);
		assertNotNull(pixels);
		assertTrue(pixels instanceof int[]);
		// notice the switch of expected pixels here from rgb to argb
		assertArrayEquals(Pix3x3argb,(int[])pixels);
	}
	
	// FileInfo.ARGB:
	//   sub cases
	//   compression
	//   intelByteOrder
	private void readARGBFileType()
	{
		Object pixels;

		// ARGB uncompressed
		pixels = readPixelHelper(FileInfo.ARGB,FileInfo.COMPRESSION_NONE,3,3,Pix3x3argb);
		assertNotNull(pixels);
		assertTrue(pixels instanceof int[]);
		assertArrayEquals(Pix3x3argb,(int[])pixels);
	}
	
	// FileInfo.ABGR:
	//   sub cases
	//   compression
	//   intelByteOrder
	private void readABGRFileType()
	{
		Object pixels;

		// ABGR uncompressed
		pixels = readPixelHelper(FileInfo.ABGR,FileInfo.COMPRESSION_NONE,3,3,Pix3x3abgr);
		assertNotNull(pixels);
		assertTrue(pixels instanceof int[]);
		// notice the switch of expected pixels here from abgr to argb
		assertArrayEquals(Pix3x3argb,(int[])pixels);
	}
	
	// FileInfo.BARG:
	//   sub cases
	//   compression
	//   intelByteOrder
	private void readBARGFileType()
    {
		Object pixels;

		// BARG uncompressed
		pixels = readPixelHelper(FileInfo.BARG,FileInfo.COMPRESSION_NONE,3,3,Pix3x3barg);
		assertNotNull(pixels);
		assertTrue(pixels instanceof int[]);
		// notice the switch of expected pixels here from barg to argb
		assertArrayEquals(Pix3x3argb,(int[])pixels);
    }
    
	// FileInfo.RGB_PLANAR:
	//   sub cases
   //    compression
    private void readRGBPlanarFileType()
    {
		Object pixels;

		// RGB_PLANAR uncompressed
		pixels = readPixelHelper(FileInfo.RGB_PLANAR,FileInfo.COMPRESSION_NONE,3,3,Pix3x3rgbPlanar);
		assertNotNull(pixels);
		assertTrue(pixels instanceof int[]);
		// notice the switch of expected pixels here from barg to argb
		assertArrayEquals(Pix3x3argb,(int[])pixels);
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
     }
    
	// FileInfo.RGB48:
	//   sub cases
    //   compression
	//   intelByteOrder
    private void readRGB48FileType()
    {
		//Object pixels;

		// RGB48 uncompressed
		//pixels = readPixelHelper(FileInfo.RGB48,FileInfo.COMPRESSION_NONE,3,3,Pix3x3rgb48);
		//assertNotNull(pixels);
		//assertTrue(pixels instanceof short[][]);
		// notice the switch of expected pixels from A to B
		//assertArrayEquals(null,(short[][])pixels);
    }
    
	// FileInfo.RGB48_PLANAR:
	//   sub cases
    //   compression
	//   intelByteOrder
    private void readRGB48PlanarFileType()
    {
		//fail("not implemented");
    }
    
	// FileInfo.GRAY12_UNSIGNED:
    private void readGray12UnsugnedFileType()
    {
		//fail("not implemented");
    }
    
	// FileInfo.GRAY24_UNSIGNED:
	private void readGray24UnsignedFileType()
	{
		byte[] input = new byte[] {11,0,0,12,0,0,13,0,0,21,0,0,22,0,0,23,0,0,31,0,0,32,0,0,33,0,0};
		float[] expected = new float[] {11,12,13,21,22,23,31,32,33};
		
		Object pixels;

		// GRAY24_UNSIGNED
		pixels = readPixelHelper(FileInfo.GRAY24_UNSIGNED,FileInfo.COMPRESSION_NONE,3,3,input);
		assertNotNull(pixels);
		assertTrue(pixels instanceof float[]);
		assertTrue(expected.length == ((float[])pixels).length);
		for (int i = 0; i < expected.length; i++)
			assertEquals(expected[i],((float[])pixels)[i],FLOAT_TOL);
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
		readRGBFileType();
		readBGRFileType();
		readARGBFileType();
		readABGRFileType();
		readBARGFileType();
		readRGBPlanarFileType();
		readBitmapFileType();
		readRGB48FileType();
		readRGB48PlanarFileType();
		readGray12UnsugnedFileType();
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
