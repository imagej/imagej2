package ij.io;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.*;

import src.IJInfo;
import ij.io.Assert;

// NOTES
//   This suite of tests also exercises the public class ByteVector which is defined in ImageReader.java
//   Due to the API definitions of ByteVector it is difficult to exercise single methods. The tests tend
//   to be interdependent so some methods are tested in multiple places.

// IMPORTANT: I have made changes to our local copy of ImageJ to work around bugs in ImageReader.
//   - 4 byte pixels with LZW and PACK_BITS compression: ImageReader.readPixels() had different code for channel selection
//       when the code called was either readChunkyRGB() or readCompressedChunkyRGB(). Fixed in local repository.
//   - gray16 signed data reading when compressed LZW_WITH_DIFFERENCING: the 32768 bias was being computed before
//       the differences were being used. I have updated ImageReader in the local repository.
//   - lurking bug in ImageReader for 16-bit files? If strips > 1 then it assumes its compressed regardless of the
//       compression flag. However uncompress() doesn't know what to do with it and does no uncompression.

// TODO if needed
//   readPixels()
//     JPEG code in place but failing tests - this is due to the fact that ImageJ uses the JDK's inbuilt JPEG support which is lossy.
//       I think for now we'll not test it (we'll assume Java knows what its doing). JPEG capabilities blocked in canDoImageCombo()
//       of each relevant PixelFormat.
//     Planar pixel formats (RgbPlanarFormat and Rgb48PlanarFormat) pass data in one strip. RgbPlanar does one strip for all three planes.
//       Rgb48Planar does one strip per plane. ImageReader inspections show that the written code there could conceivably handle multiple
//       strips per plane. However this may be an oversight in the original source code. Since the readPixels() subcase that reads these
//       formats just reads the underlying 8 and 16 bit subcases the strip lengths and offsets are reused and thus the strips have to be
//       encoded with a fixed size as big as the maximum compressed strip size. I doubt people are doing this but might need to test this.

public class ImageReaderTest {

/*
	static final long[][] BaseImage1x1 = {{77}};
	static final long[][] BaseImage3x3 = {{11,12,13},{21,22,23},{31,32,33}};
	static final long[][] BaseImage1x9 = {{11,12,13,14,15,16,17,18,19}};
	static final long[][] BaseImage7x2 = {{11,12},{21,22},{31,32},{41,42},{51,52},{61,62},{71,72}};
	static final long[][] BaseImage5x4 = {{255,255,255,255},{127,127,127,127},{63,63,63,63},{31,31,31,31},{15,15,15,15}};
	static final long[][] BaseImage4x6 =	{
												{0,255,100,200,77,153},
												{255,254,253,252,251,250},
												{1,2,3,4,5,6},
												{0,0,0,0,0,0},
												{67,67,67,67,67,67},
												{8,99,8,99,8,255}
											};
	static final long[][] Base24BitImage5x5 =	{	{0xffffff,0xff0000,0x00ff00,0x0000ff, 0},
													{16777216,100000,5999456,7070708,4813},
													{1,10,100,1000,10000},
													{0,0,0,0,0},
													{88,367092,1037745,88,4}
												};
	static final long[][] Base48BitImage6x6 = 
		{	{0xffffffffffffL, 0xffffffffff00L, 0xffffffff0000L, 0xffffff000000L, 0xffff00000000L, 0xff0000000000L},
			{0,0xffffffffffffL,0,0xffffffffffffL,0,0xffffffffffffL},
			{1,2,3,4,5,6},
			{0xff0000000000L,0x00ff00000000L, 0x0000ff000000,0x000000ff0000,0x00000000ff00,0x0000000000ff},
			{111111111111L,222222222222L,333333333333L,444444444444L,555555555555L,666666666666L},
			{0,567,0,582047483,0,1},
			{12345,554224678,90909090,9021,666666,3145926}
		};
	
	static final long[][] BaseTestImage = Base48BitImage6x6;

	final long[][][] Images = new long[][][] {BaseImage1x1, BaseImage3x3, BaseImage3x3, BaseImage1x9, BaseImage7x2, BaseImage5x4, BaseImage4x6,
			Base24BitImage5x5, Base48BitImage6x6};
*/
	private FormatTester gray8Tester= new FormatTester(new Gray8Format());
	private FormatTester color8Tester= new FormatTester(new Color8Format());
	private FormatTester gray16SignedTester= new FormatTester(new Gray16SignedFormat());
	private FormatTester gray16UnsignedTester= new FormatTester(new Gray16UnsignedFormat());
	private FormatTester gray32IntTester= new FormatTester(new Gray32IntFormat());
	private FormatTester gray32UnsignedTester= new FormatTester(new Gray32UnsignedFormat());
	private FormatTester gray32FloatTester= new FormatTester(new Gray32FloatFormat());
	private FormatTester gray64FloatTester= new FormatTester(new Gray64FloatFormat());
	private FormatTester rgbTester= new FormatTester(new RgbFormat());
	private FormatTester bgrTester= new FormatTester(new BgrFormat());
	private FormatTester argbTester= new FormatTester(new ArgbFormat());
	private FormatTester abgrTester= new FormatTester(new AbgrFormat());
	private FormatTester bargTester= new FormatTester(new BargFormat());
	private FormatTester rgbPlanarTester= new FormatTester(new RgbPlanarFormat());
	private FormatTester bitmapTester= new FormatTester(new BitmapFormat());
	private FormatTester rgb48Tester= new FormatTester(new Rgb48Format());
	private FormatTester rgb48PlanarTester= new FormatTester(new Rgb48PlanarFormat());
	private FormatTester gray12UnsignedTester= new FormatTester(new Gray12UnsignedFormat());
	private FormatTester gray24UnsignedTester= new FormatTester(new Gray24UnsignedFormat());

	final FormatTester[] Testers = new FormatTester[] {gray8Tester, color8Tester, gray16SignedTester, gray16UnsignedTester, gray32IntTester,
			gray32UnsignedTester, gray32FloatTester, gray64FloatTester, rgbTester, bgrTester, argbTester, abgrTester, bargTester, rgbPlanarTester,
			bitmapTester, rgb48Tester, rgb48PlanarTester, gray12UnsignedTester, gray24UnsignedTester};
	
	final ByteOrder.Value[] ByteOrders = new ByteOrder.Value[] {ByteOrder.Value.DEFAULT,ByteOrder.Value.INTEL};
	
	final int[] CompressionModes = new int[] {FileInfo.COMPRESSION_NONE, FileInfo.LZW, FileInfo.LZW_WITH_DIFFERENCING, FileInfo.PACK_BITS,
			FileInfo.JPEG, FileInfo.COMPRESSION_UNKNOWN};
	
	final int[] HeaderOffsets = new int[] {0,10,100,1000,203,356,404,513,697,743,819,983};

	final boolean[] EncodeAsStrips = new boolean[] {false, true};

	class FormatTester {
		
		PixelFormat theFormat;
		
		FormatTester(PixelFormat format)
		{
			this.theFormat = format;
		}
		
		String name()
		{
			return theFormat.name();
		}

		void runTest(long[][] image, int compression, ByteOrder.Value byteOrder, int headerOffset, boolean inStrips)
		{
			if (theFormat.canDoImageCombo(compression,byteOrder,headerOffset,inStrips))
			{				
				FileInfo fi = new FileInfo();
				
				byte[] pixBytes = theFormat.getBytes(image,compression,byteOrder,headerOffset,inStrips,fi);
				
				ByteArrayInputStream byteStream = new ByteArrayInputStream(pixBytes);
				
				ImageReader rdr = new ImageReader(fi);
				
				Object actualPixels = rdr.readPixels(byteStream);
				
				Object expectedPixels = theFormat.expectedResults(image);
				
				Assert.assertArraysSame(expectedPixels, actualPixels);
			}
		}
	}
	
	// *********************** ImageReader Tests  **************************************
	
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
	public void testImageReader() {
		
		FileInfo f = new FileInfo();
		ImageReader reader = new ImageReader(f);
		
		assertNotNull(reader);
	}

	// unknown file type
	@Test
	public void testBogusFileType()
	{
		FileInfo fi = new FileInfo();
		
		fi.compression = FileInfo.COMPRESSION_NONE;
		fi.height = 1;
		fi.width = 3;
		
		byte[] inBytes = new byte[] {5,3,1};

		ByteArrayInputStream stream = new ByteArrayInputStream(inBytes);

		ImageReader rdr;
		Object pixels;
		
		fi.fileType = -1;
		rdr = new ImageReader(fi);
		pixels = rdr.readPixels(stream);
		assertNull(pixels);

		fi.fileType = -18462564;
		rdr = new ImageReader(fi);
		pixels = rdr.readPixels(stream);
		assertNull(pixels);

		fi.fileType = 1014;
		rdr = new ImageReader(fi);
		pixels = rdr.readPixels(stream);
		assertNull(pixels);
	}
	
	@Test
	public void testReadPixelsFromInputStream()
	{
/*	
		// run test on basic functionality for each pixel type
		//   these end up getting run twice but these next calls simplify debugging
		gray8Tester.runTest(BaseTestImage,FileInfo.COMPRESSION_NONE,ByteOrder.Value.DEFAULT,0,false);
		color8Tester.runTest(BaseTestImage,FileInfo.COMPRESSION_NONE,ByteOrder.Value.DEFAULT,0,false);
		gray16SignedTester.runTest(BaseTestImage,FileInfo.COMPRESSION_NONE,ByteOrder.Value.DEFAULT,0,false);
		gray16UnsignedTester.runTest(BaseTestImage,FileInfo.COMPRESSION_NONE,ByteOrder.Value.DEFAULT,0,false);
		gray32IntTester.runTest(BaseTestImage,FileInfo.COMPRESSION_NONE,ByteOrder.Value.DEFAULT,0,false);
		gray32UnsignedTester.runTest(BaseTestImage,FileInfo.COMPRESSION_NONE,ByteOrder.Value.DEFAULT,0,false);
		gray32FloatTester.runTest(BaseTestImage,FileInfo.COMPRESSION_NONE,ByteOrder.Value.DEFAULT,0,false);
		gray64FloatTester.runTest(BaseTestImage,FileInfo.COMPRESSION_NONE,ByteOrder.Value.DEFAULT,0,false);
		rgbTester.runTest(BaseTestImage,FileInfo.COMPRESSION_NONE,ByteOrder.Value.DEFAULT,0,false);
		bgrTester.runTest(BaseTestImage,FileInfo.COMPRESSION_NONE,ByteOrder.Value.DEFAULT,0,false);
		argbTester.runTest(BaseTestImage,FileInfo.COMPRESSION_NONE,ByteOrder.Value.DEFAULT,0,false);
		abgrTester.runTest(BaseTestImage,FileInfo.COMPRESSION_NONE,ByteOrder.Value.DEFAULT,0,false);
		bargTester.runTest(BaseTestImage,FileInfo.COMPRESSION_NONE,ByteOrder.Value.DEFAULT,0,false);
		rgbPlanarTester.runTest(BaseTestImage,FileInfo.COMPRESSION_NONE,ByteOrder.Value.DEFAULT,0,false);
		bitmapTester.runTest(BaseTestImage,FileInfo.COMPRESSION_NONE,ByteOrder.Value.DEFAULT,0,false);
		rgb48Tester.runTest(BaseTestImage,FileInfo.COMPRESSION_NONE,ByteOrder.Value.DEFAULT,0,false);
		rgb48PlanarTester.runTest(BaseTestImage,FileInfo.COMPRESSION_NONE,ByteOrder.Value.DEFAULT,0,false);
		gray12UnsignedTester.runTest(BaseTestImage,FileInfo.COMPRESSION_NONE,ByteOrder.Value.DEFAULT,0,false);
		gray24UnsignedTester.runTest(BaseTestImage,FileInfo.COMPRESSION_NONE,ByteOrder.Value.DEFAULT,0,false);
*/

		// run all legal combos of input parameters
		for (FormatTester tester : Testers)
			for (long[][] image : Images.Images)
				for (int compression : CompressionModes)
					for (ByteOrder.Value byteOrder : ByteOrders)
						for (int headerOffset : HeaderOffsets)
							for (boolean stripped : EncodeAsStrips)
							{
/*
								System.out.println("tester("+tester.name()+") image("+image.length+"x"+image[0].length+") compress("+compression+") byteOrder("+byteOrder+") header("+headerOffset+") stripped("+stripped+")");
								if ((tester.name() == "Rgb48") && (image.length == 1) && (image[0].length == 1) && (compression == 1) &&
										(byteOrder == ByteOrder.Value.INTEL) && (headerOffset == 0) && (stripped))
								{
									System.out.println("About to fail");
								}
*/
								tester.runTest(image,compression,byteOrder,headerOffset,stripped);
							}
	
	}
	
	// since readPixels heavily tested above this method will do minimal testing
	
	@Test
	public void testReadPixelsFromInputStreamLong() {

		FileInfo fi = new FileInfo();
		fi.width = 3;
		fi.height = 3;
		fi.fileType = FileInfo.COLOR8;
		
		ImageReader rdr = new ImageReader(fi);
		
		byte[] bytes = new byte[] {1,2,3,4,5,6,7,8,9};
		ByteArrayInputStream str = new ByteArrayInputStream(bytes);
		
		Object pixels = rdr.readPixels(str,0L);
		
		assertNotNull(pixels);
		assertTrue(pixels instanceof byte[]);
		assertArrayEquals(bytes,(byte[]) pixels);

		bytes = new byte[] {0,0,0,0,1,2,3,4,5,6,7,8,9};
		str = new ByteArrayInputStream(bytes);
		
		pixels = rdr.readPixels(str,4);
		
		assertNotNull(pixels);
		assertTrue(pixels instanceof byte[]);
		assertArrayEquals(new byte[] {1,2,3,4,5,6,7,8,9},(byte[]) pixels);
	}


	@Test
	public void testReadPixelsFromURL() {

		ImageReader rdr = new ImageReader(new FileInfo());

		// malformed URL
		assertNull(rdr.readPixels("ashdjjfj"));
		
		// stream that can't be opened
		assertNull(rdr.readPixels("http://fred.joe.john.edu/zoobat/ironman/guppy.tiff"));

		// another stream that can't be opened
		assertNull(rdr.readPixels("http://www.yahoo.com/ooglywooglygugglychoogly.tiff"));
		
		// not testing positive case:
		//   - underlying code simply sets up a stream and calls readPixels() on it. We've thoroughly tested this above.
		//   - don't have a file of pixels sitting on a web server somewhere to access and don't want this dependency.
		//      could do a file:/// url to ij-tests data directory and setup FileInfo beforehand 
	}

	@Test
	public void testLzwUncompress() {
		try {
			byte[] bytes = {1,4,8,44,13,99,(byte)200,(byte)255,67,54,98,(byte)171,113};
			byte[] compressedBytes = LzwEncoder.encode(bytes);
			ImageReader rdr = new ImageReader(new FileInfo());
			assertArrayEquals(bytes,rdr.lzwUncompress(compressedBytes));
		}
		catch (Exception e)
		{
			fail();
		}
	}


	@Test
	public void testPackBitsUncompress() {
		
		// FIRST test my encodeBitsReal() method
		PackbitsEncoder.runTests();
		
		// then test that ImageReader is returning the same info
		
		try {
			byte[] bytes = {1,4,8,44,44,44,44,13,99,(byte)200,(byte)255,67,54,98,98,98,(byte)171,113,113,113,113};

			ImageReader rdr = new ImageReader(new FileInfo());
			
			byte[] compressedBytes = PackbitsEncoderNaive.encode(bytes);
			assertArrayEquals(bytes,rdr.packBitsUncompress(compressedBytes,bytes.length));

			compressedBytes = PackbitsEncoder.encode(bytes);
			assertArrayEquals(bytes,rdr.packBitsUncompress(compressedBytes,bytes.length));
		}
		catch (Exception e)
		{
			fail();
		}
	}

	@Test
	public void testPublicIVarsMinAndMax() {
		ImageReader rdr = new ImageReader(new FileInfo());
		
		assertEquals(rdr.min,0.0,Assert.FLOAT_TOL);
		assertEquals(rdr.max,0.0,Assert.FLOAT_TOL);

		rdr.min = 4000.0;
		rdr.max = 8888.7;
		
		assertEquals(rdr.min,4000.0,Assert.FLOAT_TOL);
		assertEquals(rdr.max,8888.7,Assert.FLOAT_TOL);
	}

	// *********************** ByteVector Tests  **************************************
	
	@Test
	public void testByteVectorCons(){

		ByteVector bv;
		
		// test default constructor
		bv = new ByteVector();
		assertNotNull(bv);
		assertEquals(0,bv.size());
	}

	@Test
	public void testByteVectorSize(){

		ByteVector bv;
		
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

		ByteVector bv;
		
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

		ByteVector bv;
		
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

		ByteVector bv;

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

		ByteVector bv;

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

		ByteVector bv;

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

		ByteVector bv;

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

}
