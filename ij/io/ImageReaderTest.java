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

	// for future testing when holes fixed in existing implementations
	
	static final byte[] pix3x3byte = new byte[] {11,12,13,21,22,23,31,32,33};
	static final short[] pix3x3short = new short[] {11,12,13,21,22,23,31,32,33};
	static final int[] pix3x3int = new int[] {11,12,13,21,22,23,31,32,33};
	static final float[] pix3x3float = new float[] {11,12,13,21,22,23,31,32,33};
	static final double[] pix3x3double = new double[] {11,12,13,21,22,23,31,32,33};
	
	ByteVector bv;
	
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
	public void testByteVectorCons(){
		
		// test default constructor
		bv = new ByteVector();
		assertNotNull(bv);
		assertEquals(0,bv.size());
	}

	@Test
	public void testByteVectorSize(){

		// this next test crashes on original IJ
		if (!IJInfo.testVsOrigIJ){
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

		if (!IJInfo.testVsOrigIJ)
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
		if (!IJInfo.testVsOrigIJ)
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
		if (!IJInfo.testVsOrigIJ)
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

	@Test
	public void testImageReader() {
		
		FileInfo f = new FileInfo();
		ImageReader reader = new ImageReader(f);
		
		assertNotNull(reader);
	}

	private Object readPixelHelper(FileInfo fi, byte[] inBytes){
		ByteArrayInputStream stream = new ByteArrayInputStream(inBytes);
		ImageReader rdr = new ImageReader(fi);
		return rdr.readPixels(stream);
	}
	
	// unknown file type
	private void testReadBogusFileType()
	{
		byte[] inBytes = new byte[] {5,3,1};
		
		FileInfo fi = new FileInfo();		
		fi.fileType = -184625640;  // crazy file type
		
		Object pixels = readPixelHelper(fi,inBytes);
		
		assertNull(pixels);
	}
	
	// FileInfo.GRAY8 and FileInfo.COLOR8
	//   subcases:
	//     compression
	//     skip()
	
	private void testRead8bitPixelData(){
		Object pixels;
		byte[] inBytes, outBytes;
		FileInfo fi;

		// GRAY8 uncompressed
		inBytes = pix3x3byte;
		fi = new FileInfo();
		fi.height = 3;
		fi.width = 3;
		fi.fileType = FileInfo.GRAY8;
		fi.compression = FileInfo.COMPRESSION_NONE;
		pixels= readPixelHelper(fi,inBytes);
		assertNotNull(pixels);
		assertTrue(pixels instanceof byte[]);
		outBytes = (byte[]) pixels;
		assertArrayEquals(inBytes,outBytes);
		
		// GRAY8 compressed LZW
		// GRAY8 compressed LZW_WITH_DIFFERENCING
		// GRAY8 compressed PACK_BITS
		
		// COLOR8 untested for now -> code inspection shows it uses same code as GRAY8
		// COLOR8 uncompressed
		// COLOR8 compressed LZW		
		// COLOR8 compressed LZW_WITH_DIFFERENCING
		// COLOR8 compressed PACK_BITS

	}

	private byte[] convertToBytes(Object inArray)
	{
		if (inArray instanceof byte[])
			return ((byte[])inArray).clone();
		
		byte[] outputBytes;
		
		if (inArray instanceof short[])
		{
			short[] arr = (short[]) inArray;
			outputBytes = new byte[arr.length * 2];
			
			for (int i = 0; i < arr.length; i++)
			{
				outputBytes[2*i] = (byte)((arr[i] & 0xff00) >> 8);
				outputBytes[2*i+1] = (byte)(arr[i] & 0x00ff);
			}
			return outputBytes;
		}
		
		if (inArray instanceof int[])
		{
			int[] arr = (int[]) inArray;
			outputBytes = new byte[arr.length * 4];
			
			for (int i = 0; i < arr.length; i++)
			{
				outputBytes[4*i] = (byte)((arr[i] & 0xff000000L) >> 24);
				outputBytes[4*i+1] = (byte)((arr[i] & 0x00ff0000L) >> 16);
				outputBytes[4*i+2] = (byte)((arr[i] & 0x0000ff00L) >> 8);
				outputBytes[4*i+3] = (byte)((arr[i] & 0x000000ffL) >> 0);
			}
			return outputBytes;
		}
		
		if (inArray instanceof long[])
		{
			// for now not needed I think - fall through to nul return below
		}
		
		if (inArray instanceof float[])
		{
			float[] arr = (float[]) inArray;
			outputBytes = new byte[arr.length * 4];
			
			for (int i = 0; i < arr.length; i++)
			{
				int bits = Float.floatToIntBits(arr[i]);
				
				outputBytes[4*i] = (byte)((bits & 0xff000000L) >> 24);
				outputBytes[4*i+1] = (byte)((bits & 0x00ff0000L) >> 16);
				outputBytes[4*i+2] = (byte)((bits & 0x0000ff00L) >> 8);
				outputBytes[4*i+3] = (byte)((bits & 0x000000ffL) >> 0);
			}
			
			return outputBytes;
		}
		
		if (inArray instanceof double[])
		{
			// for now not needed I think - fall through to nul return below
		}
		
		return null;
	}
	
	// FileInfo.GRAY16_SIGNED and FileInfo.GRAY16_UNSIGNED:
	//   subcases:
	//     compression
	//     fi.intelByteOrder

	private void testRead16bitPixelData(){
		Object pixels;
		FileInfo fi;
		byte[] inBytes;
		short[] inPixels;

		// GRAY16_UNSIGNED uncompressed
		inPixels = pix3x3short;
		inBytes = convertToBytes(inPixels);
		fi = new FileInfo();
		fi.height = 3;
		fi.width = 3;
		fi.fileType = FileInfo.GRAY16_UNSIGNED;
		fi.compression = FileInfo.COMPRESSION_NONE;
		pixels= readPixelHelper(fi,inBytes);
		assertNotNull(pixels);
		assertTrue(pixels instanceof short[]);
		assertArrayEquals(inPixels,(short[])pixels);

		// GRAY_UNSIGNED LZW
		
		// GRAY_UNSIGNED LZW_WITH_DIFFERENCING
		
		// GRAY_UNSIGNED PACK_BITS
		
		// GRAY16_SIGNED uncompressed
		//inPixels = pix3x3short;
		//inBytes = convertToBytes(inPixels);
		//fi = new FileInfo();
		//fi.height = 3;
		//fi.width = 3;
		//fi.fileType = FileInfo.GRAY16_SIGNED;
		//fi.compression = FileInfo.COMPRESSION_NONE;
		//pixels= readPixelHelper(fi,inBytes);
		//assertNotNull(pixels);
		//assertTrue(pixels instanceof short[]);
		//assertArrayEquals(inPixels,(short[])pixels);

		// GRAY_SIGNED LZW
		
		// GRAY_SIGNED LZW_WITH_DIFFERENCING
		
		// GRAY_SIGNED PACK_BITS
	}
	
	// FileInfo.GRAY32_INT, FileInfo.GRAY32_UNSIGNED, FileInfo.GRAY32_FLOAT:
	private void testRead32bitPixelData(){
		Object pixels;
		FileInfo fi;
		byte[] inBytes;
		int[] inPixels;
		float[] inPixelsF;

		// GRAY32_UNSIGNED
		inPixels = pix3x3int;
		inBytes = convertToBytes(inPixels);
		fi = new FileInfo();
		fi.height = 3;
		fi.width = 3;
		fi.fileType = FileInfo.GRAY32_UNSIGNED;
		fi.compression = FileInfo.COMPRESSION_NONE;
		pixels= readPixelHelper(fi,inBytes);
		assertNotNull(pixels);
		assertTrue(pixels instanceof float[]);
		for (int i = 0; i < inPixels.length; i++)
			assertEquals((float)inPixels[i],((float[])pixels)[i],0.001f);
		
		// GRAY32_INT
		inPixels = pix3x3int;
		inBytes = convertToBytes(inPixels);
		fi = new FileInfo();
		fi.height = 3;
		fi.width = 3;
		fi.fileType = FileInfo.GRAY32_INT;
		fi.compression = FileInfo.COMPRESSION_NONE;
		pixels= readPixelHelper(fi,inBytes);
		assertNotNull(pixels);
		assertTrue(pixels instanceof float[]);
		for (int i = 0; i < inPixels.length; i++)
			assertEquals((float)inPixels[i],((float[])pixels)[i],0.001f);

		// GRAY32_FLOAT
		inPixelsF = pix3x3float;
		inBytes = convertToBytes(inPixelsF);
		fi = new FileInfo();
		fi.height = 3;
		fi.width = 3;
		fi.fileType = FileInfo.GRAY32_FLOAT;
		fi.compression = FileInfo.COMPRESSION_NONE;
		pixels= readPixelHelper(fi,inBytes);
		assertNotNull(pixels);
		assertTrue(pixels instanceof float[]);
		for (int i = 0; i < inPixels.length; i++)
			assertEquals(inPixelsF[i],((float[])pixels)[i],0.001f);
	}
	
	// FileInfo.GRAY64_FLOAT
	//   sub cases
	//   intelByteOrder
	private void testRead64bitPixelData(){
		fail("not implemented");
	}
	
	// FileInfo.RGB:
	// FileInfo.BGR:
	// FileInfo.ARGB:
	// FileInfo.ABGR:
	// FileInfo.BARG:
	//   sub cases - compression
	//   intelByteOrder
   private void testReadRGBPixelData()
    {
		fail("not implemented");
    }
    
	// FileInfo.RGB_PLANAR:
	//   sub cases - compression
    private void testReadRGBPlanarPixelData()
    {
		fail("not implemented");
    }
    
	// FileInfo.BITMAP:
    private void testRead1bitPixelData()
    {
		fail("not implemented");
    }
    
	// FileInfo.RGB48:
	// FileInfo.RGB48_PLANAR:
	//   sub cases
    //   compression
	//   intelByteOrder
    private void testRead48bitData()
    {
		fail("not implemented");
    }
    
	// FileInfo.GRAY12_UNSIGNED:
    private void testRead12bitData()
    {
		fail("not implemented");
    }
    
	// FileInfo.GRAY24_UNSIGNED:
	private void testRead24bitData()
	{
		fail("not implemented");
	}
	
	@Test
	public void testReadPixelsFromInputStream() {
		testReadBogusFileType();
		testRead8bitPixelData();
		testRead16bitPixelData();
		testRead32bitPixelData();
		testRead64bitPixelData();
	    testReadRGBPixelData();
	    testReadRGBPlanarPixelData();
	    testRead1bitPixelData();
	    testRead48bitData();
	    testRead12bitData();
		testRead24bitData();
	}

	@Test
	public void testReadPixelsFromInputStreamLong() {
		fail("Not yet implemented");
	}

	@Test
	public void testReadPixelsFromURL() {
		fail("Not yet implemented");
	}

	@Test
	public void testLzwUncompress() {
		fail("Not yet implemented");
	}

	@Test
	public void testPackBitsUncompress() {
		fail("Not yet implemented");
	}

	@Test
	public void testPublicIVarsMinAndMax() {
		fail("Not yet implemented");
	}
}
