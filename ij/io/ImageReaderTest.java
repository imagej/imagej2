package ij.io;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.*;
import java.util.*;

import loci.formats.codec.CodecOptions;
import loci.formats.codec.LZWCodec;

// NOTES
//   This suite of tests also exercises the public class ByteVector which is defined in ImageReader.java
//   Due to the API definitions of ByteVector it is difficult to exercise single methods. The tests tend
//   to be interdependent so some methods are tested in multiple places.

// TODO
//   only readPixels() not totally finished
//     the 4 byte pixel (LZW and PACK_BITS) compression tests are in place and working because I changed ImageReader
//       they will start failing when ImageJ pulled from original repository
//     implement and test LZW DIFF compression tests
//       LEFT OFF: lzwdiff code is in place but bias code not implemented yet and no tests exercise it yet.
//     I need something for JPEG compression - it's called in the chunkyRGB cases in ImageReader
//     The tests below do not pass more than one strip to readPixels() - will want to test that better
//   refactor
//     check that I've setup canDoCombo() correctly for all pixel types after done

public class ImageReaderTest {

	static final long[][] BaseImage1x1 = {{77}};
	static final long[][] BaseImage3x3 = {{11,12,13},{21,22,23},{31,32,33}};
	static final long[][] BaseImage1x9 = {{11,12,13,14,15,16,17,18,19}};
	static final long[][] BaseImage7x2 = {{11,12},{21,22},{31,32},{41,42},{51,52},{61,62},{71,72}};
	static final long[][] BaseImage5x4 = {{255,255,255,255},{127,127,127,127},{63,63,63,63},{31,31,31,31},{15,15,15,15}};
	static final long[][] BaseImage4x6 = {{0,255,100,200,77,153},{255,254,253,252,251,250},{1,2,3,4,5,6},{0,0,0,0,0,0},{67,67,67,67,67,67},{8,99,8,99,8,255}};
	static final long[][] Base24BitImage5x5 = {{0xffffff,0xff0000,0x00ff00,0x0000ff, 0},
												{16777216,100000,5999456,7070708,4813},
												{1,10,100,1000,10000},
												{0,0,0,0,0},
												{88,367092,1037745,88,4}};

	// swap this as desired for debugging
	//static final long[][] BaseTestImage = BaseImage4x6;
	static final long[][] BaseTestImage = Base24BitImage5x5;

	final long[][][] Images = new long[][][] {BaseImage1x1, BaseImage3x3, BaseImage3x3, BaseImage1x9, BaseImage7x2, BaseImage5x4, BaseImage4x6,
			Base24BitImage5x5};

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
	
	enum ByteOrder { DEFAULT, INTEL };
	
	final ByteOrder[] ByteOrders = new ByteOrder[] {ByteOrder.DEFAULT, ByteOrder.INTEL};
	
	final int[] CompressionModes = new int[] {FileInfo.COMPRESSION_NONE, FileInfo.LZW, FileInfo.LZW_WITH_DIFFERENCING, FileInfo.PACK_BITS, FileInfo.JPEG,FileInfo.COMPRESSION_UNKNOWN};
	
	final int[] HeaderOffsets = new int[] {0,10,100,1000,203,356,404,513,697,743,819,983};

	static final float FLOAT_TOL = 0.00001f;

	interface ByteEncoder{
		byte[] encode(byte[] input);
	}
	
	private NaivePackbitsEncoder packbitsEncoderNaive = new NaivePackbitsEncoder();
	private RealPackbitsEncoder packbitsEncoderReal = new RealPackbitsEncoder();
	private LzwEncoder lzwEncoder = new LzwEncoder();
	private LzwDiffEncoder lzwDiffEncoder = new LzwDiffEncoder();
	private TwelveBitEncoder twelveBitEncoder = new TwelveBitEncoder();
	
	// swap this as desired for debugging
	private ByteEncoder packbitsEncoder = packbitsEncoderReal;
//	private ByteEncoder packbitsEncoder = packbitsEncoderNaive;

	// NaivePackBitsEncoder is designed with two things in mind
	//   - test ImageReader's ability to handle more than one style of packbits encoded data
	//   - stand in for RealPackBitsEncoder if we think it is ever faulty
	
	class NaivePackbitsEncoder implements ByteEncoder {
		
		NaivePackbitsEncoder() {}
		
		public byte[] encode(byte[] input)
		{
			byte[] output = new byte[input.length*2];
			int i = 0;
			for (byte b : input)
			{
				output[i++] = 0;
				output[i++] = b;
			}
			return output;
		}
	}
	
	// RealPackBitsEncoder is needed to test packbits compression in ImageReader::readPixels()
	
	class RealPackbitsEncoder implements ByteEncoder {
		
		RealPackbitsEncoder() {}

		private boolean moreInput(byte[] input, int pnum)
		{
			return pnum < input.length;
		}

		private boolean twoBytesInARow(byte[] input, int pnum)
		{
			if (pnum+1 >= input.length)
				return false;
			if (input[pnum] != input[pnum+1])
				return false;
			return true;
		}

		public byte[] encode(byte[] input)
		{
			ByteVector compressedData = new ByteVector();
			ArrayList<Byte> unpairedData = new ArrayList<Byte>();
			int i = 0;
			while (moreInput(input,i))
			{
				byte currByte = input[i];
				int pairs = 0;
				while ((moreInput(input,i)) && (twoBytesInARow(input,i))) {
					pairs++;
					i++;
				}
		    
				if (pairs > 0)  // don't count second byte of pair twice
					i++;

				for (int n = 0; n < pairs; n+=127)
				{
					int pairsInThisChunk = Math.min(pairs - n,127);
					compressedData.add((byte)-pairsInThisChunk);
					compressedData.add(currByte);
				}

				unpairedData.clear();
				while ((moreInput(input,i)) && (!twoBytesInARow(input,i)))
					unpairedData.add(input[i++]);

				int numBytes = unpairedData.size();
				for (int n = 0; n < numBytes; n += 128)
				{
					int bytesInThisChunk = Math.min(numBytes,128);
					compressedData.add((byte)(bytesInThisChunk-1));
					for (int bnum = 0; bnum < bytesInThisChunk; bnum++)
						compressedData.add(unpairedData.get(n+bnum));
				}

			}  // while moreInput()

			return compressedData.toByteArray();
		}
		
		private void runTests()
		{
			assertArrayEquals(new byte[]{},encode(new byte[]{}));                         // {} case
			assertArrayEquals(new byte[]{0,0},encode(new byte[]{0}));                     // {a} case
			assertArrayEquals(new byte[]{-1,0},encode(new byte[]{0,0}));                  // {aa} case
			assertArrayEquals(new byte[]{1,0,1},encode(new byte[]{0,1}));                 // {ab} case
			assertArrayEquals(new byte[]{-2,0},encode(new byte[]{0,0,0}));                // {aaa} case
			assertArrayEquals(new byte[]{-1,0,0,1},encode(new byte[]{0,0,1}));            // {aab} case
			assertArrayEquals(new byte[]{2,0,1,0},encode(new byte[]{0,1,0}));             // {aba} case
			assertArrayEquals(new byte[]{-3,0},encode(new byte[]{0,0,0,0}));              // {aaaa} case
			assertArrayEquals(new byte[]{-2,0,0,1},encode(new byte[]{0,0,0,1}));          // {aaab} case
			assertArrayEquals(new byte[]{-1,0,1,1,0},encode(new byte[]{0,0,1,0}));        // {aaba} case
			assertArrayEquals(new byte[]{1,0,1,-1,0},encode(new byte[]{0,1,0,0}));        // {abaa} case
			assertArrayEquals(new byte[]{-1,0,-1,1},encode(new byte[]{0,0,1,1}));         // {aabb} case
			assertArrayEquals(new byte[]{1,0,1,-1,0},encode(new byte[]{0,1,0,0}));        // {abab} case
			assertArrayEquals(new byte[]{0,0,-1,1,0,0},encode(new byte[]{0,1,1,0}));      // {abba} case
			assertArrayEquals(new byte[]{0,0,-2,1},encode(new byte[]{0,1,1,1}));          // {abbb} case
			// TODO : need to test a 127, 128, and more bytes tests to check edge cases
		}
	}

	class LzwEncoder implements ByteEncoder {
		
		LzwEncoder() {}
		
		public byte[] encode(byte[] input)
		{
			byte[] output = null;
			try {
				output = new LZWCodec().compress(input, CodecOptions.getDefaultOptions()); // compress the output data
			} catch (Exception e)
			{
				e.printStackTrace();
			}
			return output;
		}
	}
	
	class LzwDiffEncoder implements ByteEncoder {
		
		LzwDiffEncoder() {}
		
		public byte[] encode(byte[] input)
		{
			byte[] output = lzwEncoder.encode(input);
			// TODO now bias the output appropriately ...
			;
			return output;
		}
	}

	class TwelveBitEncoder
	{
		TwelveBitEncoder() {}
		
		private byte[] encode(long[][] inPix)
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
	}
	
	// the readPixels test is difficult to debug: make helper classes
	
	void myAssertArrayEquals(byte[] expected, byte[] actual)
	{
		if (expected == null)
			fail("myAssertArrayEquals(byte[],byte[]) passed in null data for first parameter");

		if (actual == null)
			fail("myAssertArrayEquals(byte[],byte[]) passed in null data for second parameter");
		
		if (expected.length != actual.length)
			fail("myAssertArrayEquals(byte[],byte[]) array lengths differ: expected "+expected.length + " and got " + actual.length);
		
		for (int i = 0; i < expected.length; i++)
			if (expected[i] != actual[i])
				fail("myAssertArrayEquals(byte[],byte[]) items differ at index " + i + ": expected "+ expected[i] + " and got " + actual[i]);
	}
	
	void myAssertArrayEquals(short[] expected, short[] actual)
	{
		if (expected == null)
			fail("myAssertArrayEquals(short[],short[]) passed in null data for first parameter");

		if (actual == null)
			fail("myAssertArrayEquals(short[],short[]) passed in null data for second parameter");
		
		if (expected.length != actual.length)
			fail("myAssertArrayEquals(short[],short[]) array lengths differ: expected "+expected.length + " and got " + actual.length);
		
		for (int i = 0; i < expected.length; i++)
			if (expected[i] != actual[i])
				fail("myAssertArrayEquals(short[],short[]) items differ at index " + i + ": expected "+ expected[i] + " and got " + actual[i]);
	}
	
	void myAssertArrayEquals(int[] expected, int[] actual)
	{
		if (expected == null)
			fail("myAssertArrayEquals(int[],int[]) passed in null data for first parameter");

		if (actual == null)
			fail("myAssertArrayEquals(int[],int[]) passed in null data for second parameter");
		
		if (expected.length != actual.length)
			fail("myAssertArrayEquals(int[],int[]) array lengths differ: expected "+expected.length + " and got " + actual.length);
		
		for (int i = 0; i < expected.length; i++)
			if (expected[i] != actual[i])
				fail("myAssertArrayEquals(int[],int[]) items differ at index " + i + ": expected "+ expected[i] + " and got " + actual[i]);
	}
	
	void myAssertArrayEquals(long[] expected, long[] actual)
	{
		if (expected == null)
			fail("myAssertArrayEquals(long[],long[]) passed in null data for first parameter");

		if (actual == null)
			fail("myAssertArrayEquals(long[],long[]) passed in null data for second parameter");
		
		if (expected.length != actual.length)
			fail("myAssertArrayEquals(long[],long[]) array lengths differ: expected "+expected.length + " and got " + actual.length);
		
		for (int i = 0; i < expected.length; i++)
			if (expected[i] != actual[i])
				fail("myAssertArrayEquals(long[],long[]) items differ at index " + i + ": expected "+ expected[i] + " and got " + actual[i]);
	}
	
	void myAssertArrayEquals(float[] expected, float[] actual)
	{
		if (expected == null)
			fail("myAssertArrayEquals(float[],float[]) passed in null data for first parameter");

		if (actual == null)
			fail("myAssertArrayEquals(float[],float[]) passed in null data for second parameter");
		
		if (expected.length != actual.length)
			fail("myAssertArrayEquals(float[],float[]) array lengths differ: expected "+expected.length + " and got " + actual.length);
		
		for (int i = 0; i < expected.length; i++)
			if (Math.abs(expected[i]-actual[i]) > FLOAT_TOL)
				fail("myAssertArrayEquals(float[],float[]) items differ at index " + i + ": expected "+ expected[i] + " and got " + actual[i]);
	}
	
	void myAssertArrayEquals(short[][] expected, short[][] actual)
	{
		if (expected == null)
			fail("myAssertArrayEquals(short[][],short[][]) passed in null data for first parameter");

		if (actual == null)
			fail("myAssertArrayEquals(short[][],short[][]) passed in null data for second parameter");
		
		if (expected.length != actual.length)
			fail("myAssertArrayEquals(short[][],short[][]) array lengths differ: expected "+expected.length + " and got " + actual.length);
		
		for (int i = 0; i < expected.length; i++)
			if (expected[i].length != actual[i].length)
				fail("myAssertArrayEquals(short[][],short[][]) sub array lengths differ at index " + i + ": expected "+ expected[i].length + " and got " + actual[i].length);
			else
				for (int j = 0; j < expected[i].length; j++)
					if (expected[i][j] != actual[i][j])
						fail("myAssertArrayEquals(short[][],short[][]) items differ at index [" + i + "][" + j + "] : expected " + expected[i][j] + " and got " + actual[i][j]);
	}
	
	void myAssertArrayEquals(Object[] expected, Object[] actual)
	{
		if (expected == null)
			fail("myAssertArrayEquals(Object[],Object[]) passed in null data for first parameter");

		if (actual == null)
			fail("myAssertArrayEquals(Object[],Object[]) passed in null data for second parameter");
		
		if (expected.length != actual.length)
			fail("myAssertArrayEquals(Object[],Object[]) array lengths differ: expected " + expected.length + " and got actual " + actual.length);
		
		for (int i = 0; i < expected.length; i++)
			myAssertArrayEquals((short[])expected[i],(short[])actual[i]);
	}
	
	void assertSame(Object expected, Object actual)
	{
		if ((expected == null) || (actual == null))
			fail("assertSame() expects non-null arguments : (" + expected + "," + actual + ")");
		
		Class<?> aClass = expected.getClass();
		Class<?> bClass = actual.getClass();
		
		if (aClass != bClass)
			fail("assertSame() passed incompatible Objects : (" + aClass.getName() + "," + bClass.getName() + ")");
		
		if (expected instanceof byte[])
			myAssertArrayEquals((byte[])expected,(byte[])actual);
		else if (expected instanceof short[])
			myAssertArrayEquals((short[])expected,(short[])actual);
		else if (expected instanceof int[])
			myAssertArrayEquals((int[])expected,(int[])actual);
		else if (expected instanceof long[])
			myAssertArrayEquals((long[])expected,(long[])actual);
		else if (expected instanceof float[])
			myAssertArrayEquals((float[])expected,(float[])actual);
		else if (expected instanceof short[][])
			myAssertArrayEquals((short[][])expected,(short[][])actual);
		else if (expected instanceof Object[])
			myAssertArrayEquals((Object[])expected,(Object[])actual);
		else
			fail("assertSame() passed unsupported data format type : (" + aClass.getName() + ")");
	}
	
	private void initializeFileInfo(FileInfo fi, int ftype, int compression, ByteOrder byteOrder, int rows, int cols)
	{
		fi.fileType = ftype;
		fi.compression = compression;
		if (byteOrder == ByteOrder.INTEL)
			fi.intelByteOrder = true;
		else
			fi.intelByteOrder = false;
		fi.height = rows;
		fi.width = cols;
	}
	
	private byte[] intelSwap(byte[] input, int everyX)
	{
		byte[] output = new byte[input.length];
		
		for (int i = 0; i < input.length; i += everyX)
			for (int j = 0; j < everyX; j++)            // TODO: should this be everyX/2 ???
				output[i+j] = input[i+everyX-1-j];
		
		return output;
	}

	private byte[] compress(FileInfo fi, int compression, byte[] input)
	{
		byte[] compressed = input;
		
		if (fi.compression == FileInfo.LZW)
			compressed = lzwEncoder.encode(input);
		else if (fi.compression == FileInfo.LZW_WITH_DIFFERENCING)
			compressed = lzwDiffEncoder.encode(input);
		else if (fi.compression == FileInfo.PACK_BITS)
			compressed = packbitsEncoder.encode(input);
		else
			; // do nothing

		fi.stripLengths = new int[] {compressed.length};
		fi.stripOffsets = new int[] {0};
		fi.rowsPerStrip = fi.height;  // this only seems to be needed for PACK_BITS compression

		return compressed;
	}
	
	private byte[] prependFakeHeader(int headerBytes, byte[] pixData)
	{
		byte[] header = new byte[headerBytes];
		byte[] output = new byte[header.length + pixData.length];
		System.arraycopy(header,0,output,0,header.length);
		System.arraycopy(pixData,0,output,header.length,pixData.length);
		return output;
	}
	
	private byte[] attachHeader(FileInfo fi, int headerBytes, byte[] pixData)
	{
		fi.offset = headerBytes;
		fi.longOffset = headerBytes;
		return prependFakeHeader(headerBytes,pixData);	
	}
	
	class FormatTester {
		
		PixelFormat theFormat;
		
		FormatTester(PixelFormat format)
		{
			this.theFormat = format;
		}
		
		void runTest(long[][] image, int compression, ByteOrder byteOrder, int headerOffset)
		{
			if (theFormat.canDoImageCombo(compression,byteOrder,headerOffset))
			{
				FileInfo fi = new FileInfo();
				
				byte[] pixBytes = theFormat.getBytes(image,compression,byteOrder,headerOffset,fi);
				
				ByteArrayInputStream byteStream = new ByteArrayInputStream(pixBytes);
				
				ImageReader rdr = new ImageReader(fi);
				
				Object actualPixels = rdr.readPixels(byteStream);
				
				Object expectedPixels = theFormat.expectedResults(image);
				
				assertSame(expectedPixels, actualPixels);
			}
		}
	}
	
	abstract class PixelFormat {
		
		private String name;  // might be useful for debugging purposes. otherwise could be an interface
		
		PixelFormat(String name)
		{
			this.name = name;
		}
		
		abstract boolean canDoImageCombo(int compression, ByteOrder byteOrder, int headerBytes);
		abstract byte[] getBytes(long[][] image, int compression, ByteOrder byteOrder, int headerBytes, FileInfo fi);
		abstract Object expectedResults(long[][] inputImage);
		
	}
	
	class Gray8Format extends PixelFormat
	{
		Gray8Format()
		{
			super("Gray8");
		}
		
		boolean canDoImageCombo(int compression, ByteOrder byteOrder, int headerBytes)
		{
			// TODO: until refactored don't handle lzw-diff
			if (compression == FileInfo.LZW_WITH_DIFFERENCING)
				return false;
			if (compression == FileInfo.COMPRESSION_UNKNOWN)
				return false;
			if (compression == FileInfo.JPEG)
				return false;
			
			if (byteOrder == ByteOrder.INTEL)
				return false;

			return true;
		}
		
		byte[] getBytes(long[][] image, int compression, ByteOrder byteOrder, int headerBytes, FileInfo fi)
		{
			initializeFileInfo(fi,FileInfo.GRAY8,compression,byteOrder,image.length,image[0].length);
			
			byte[] output = new byte[fi.height * fi.width];
			
			int i = 0;
			for (long[] row : image)
				for (long pix : row)
					output[i++] = (byte)(pix & 0xff);
		
			//if (byteOrder == ByteOrder.INTEL)
			//	; // nothing to do

			output = compress(fi,compression,output);

			output = attachHeader(fi,headerBytes,output);
			
			return output;
		}
		
		Object expectedResults(long[][] inputImage)
		{
			byte[] output = new byte[inputImage.length * inputImage[0].length];
			
			int i = 0;
			for (long[] row : inputImage)
				for (long pix : row)
					output[i++] = (byte) (pix & 0xff);
			
			return output;
		}
	}
	
	class Color8Format extends PixelFormat
	{
		Color8Format()
		{
			super("Color8");
		}
		
		boolean canDoImageCombo(int compression, ByteOrder byteOrder, int headerBytes)
		{
			// TODO: until refactored don't handle lzw-diff
			if (compression == FileInfo.LZW_WITH_DIFFERENCING)
				return false;
			if (compression == FileInfo.COMPRESSION_UNKNOWN)
				return false;
			if (compression == FileInfo.JPEG)
				return false;
			
			if (byteOrder == ByteOrder.INTEL)
				return false;

			return true;
		}
		
		byte[] getBytes(long[][] image, int compression, ByteOrder byteOrder, int headerBytes, FileInfo fi)
		{
			initializeFileInfo(fi,FileInfo.COLOR8,compression,byteOrder,image.length,image[0].length);
			
			byte[] output = new byte[fi.height * fi.width];
			
			int i = 0;
			for (long[] row : image)
				for (long pix : row)
					output[i++] = (byte)(pix & 0xff);
		
			//if (byteOrder == ByteOrder.INTEL)
			//	; // nothing to do
			
			output = compress(fi,compression,output);

			output = attachHeader(fi,headerBytes,output);
			
			return output;
		}

		Object expectedResults(long[][] inputImage)
		{
			byte[] output = new byte[inputImage.length * inputImage[0].length];
			
			int i = 0;
			for (long[] row : inputImage)
				for (long pix : row)
					output[i++] = (byte) (pix & 0xff);
			
			return output;
		}
	}
	
	class Gray16SignedFormat extends PixelFormat
	{
		Gray16SignedFormat()
		{
			super("Gray16Signed");
		}
		
		boolean canDoImageCombo(int compression, ByteOrder byteOrder, int headerBytes)
		{
			// TODO: until refactored don't handle lzw-diff
			if (compression == FileInfo.LZW_WITH_DIFFERENCING)
				return false;
			if (compression == FileInfo.COMPRESSION_UNKNOWN)
				return false;
			if (compression == FileInfo.JPEG)
				return false;
			if (compression == FileInfo.PACK_BITS)
				return false;
			
			return true;
		}
		
		byte[] getBytes(long[][] image, int compression, ByteOrder byteOrder, int headerBytes, FileInfo fi)
		{
			initializeFileInfo(fi,FileInfo.GRAY16_SIGNED,compression,byteOrder,image.length,image[0].length);
			
			byte[] output = new byte[fi.height * fi.width * 2];
			
			int i = 0;
			for (long[] row : image)
				for (long pix : row)
				{
					output[2*i]   = (byte)((pix & 0xff00) >> 8);
					output[2*i+1] = (byte)((pix & 0x00ff) >> 0);
					i++;
				}
			
			if (byteOrder == ByteOrder.INTEL)
				output = intelSwap(output,2);
			
			output = compress(fi,compression,output);

			output = attachHeader(fi,headerBytes,output);
			
			return output;
		}

		Object expectedResults(long[][] inputImage)
		{
			short[] output = new short[inputImage.length * inputImage[0].length];
			
			int i = 0;
			for (long[] row : inputImage)
				for (long pix : row)
					output[i++] = (short) (32768 + (pix & 0xffff)); // bias taken from ImageReader.readPixels()
			return output;
		}
	}

	class Gray16UnsignedFormat extends PixelFormat
	{
		Gray16UnsignedFormat()
		{
			super("Gray16Unsigned");
		}
		
		boolean canDoImageCombo(int compression, ByteOrder byteOrder, int headerBytes)
		{
			// TODO: until refactored don't handle lzw-diff
			if (compression == FileInfo.LZW_WITH_DIFFERENCING)
				return false;
			if (compression == FileInfo.COMPRESSION_UNKNOWN)
				return false;
			if (compression == FileInfo.JPEG)
				return false;
			if (compression == FileInfo.PACK_BITS)
				return false;
			
			return true;
		}
		
		byte[] getBytes(long[][] image, int compression, ByteOrder byteOrder, int headerBytes, FileInfo fi)
		{
			initializeFileInfo(fi,FileInfo.GRAY16_UNSIGNED,compression,byteOrder,image.length,image[0].length);
			
			byte[] output = new byte[fi.height * fi.width * 2];
			
			int i = 0;
			for (long[] row : image)
				for (long pix : row)
				{
					output[2*i]   = (byte)((pix & 0xff00) >> 8);
					output[2*i+1] = (byte)((pix & 0x00ff) >> 0);
					i++;
				}
			
			if (byteOrder == ByteOrder.INTEL)
				output = intelSwap(output,2);
			
			output = compress(fi,compression,output);

			output = attachHeader(fi,headerBytes,output);
			
			return output;
		}

		Object expectedResults(long[][] inputImage)
		{
			short[] output = new short[inputImage.length * inputImage[0].length];
			
			int i = 0;
			for (long[] row : inputImage)
				for (long pix : row)
					output[i++] = (short)(pix & 0xffff);
			return output;
		}
	}

	class Gray32IntFormat extends PixelFormat
	{
		Gray32IntFormat()
		{
			super("Gray32Int");
		}
		
		boolean canDoImageCombo(int compression, ByteOrder byteOrder, int headerBytes)
		{
			if (compression != FileInfo.COMPRESSION_NONE)
				return false;
			
			return true;
		}
		
		byte[] getBytes(long[][] image, int compression, ByteOrder byteOrder, int headerBytes, FileInfo fi)
		{
			initializeFileInfo(fi,FileInfo.GRAY32_INT,compression,byteOrder,image.length,image[0].length);
			
			byte[] output = new byte[fi.height * fi.width * 4];
			
			int i = 0;
			for (long[] row : image)
				for (long pix : row)
				{
					output[4*i]   = (byte)((pix & 0xff000000) >> 24);
					output[4*i+1] = (byte)((pix & 0x00ff0000) >> 16);
					output[4*i+2] = (byte)((pix & 0x0000ff00) >> 8);
					output[4*i+3] = (byte)((pix & 0x000000ff) >> 0);
					i++;
				}
						
			if (byteOrder == ByteOrder.INTEL)
				output = intelSwap(output,4);
			
			//output = compress(fi,compression,output);

			output = attachHeader(fi,headerBytes,output);
			
			return output;
		}

		Object expectedResults(long[][] inputImage)
		{
			float[] output = new float[inputImage.length * inputImage[0].length];
			
			int i = 0;
			for (long[] row : inputImage)
				for (long pix : row)
					output[i++] = (float)pix;
			return output;
		}
	}

	class Gray32UnsignedFormat extends PixelFormat
	{
		Gray32UnsignedFormat()
		{
			super("Gray32Unsigned");
		}
		
		boolean canDoImageCombo(int compression, ByteOrder byteOrder, int headerBytes)
		{
			if (compression != FileInfo.COMPRESSION_NONE)
				return false;
			
			return true;
		}
		
		byte[] getBytes(long[][] image, int compression, ByteOrder byteOrder, int headerBytes, FileInfo fi)
		{
			initializeFileInfo(fi,FileInfo.GRAY32_UNSIGNED,compression,byteOrder,image.length,image[0].length);
			
			byte[] output = new byte[fi.height * fi.width * 4];
			
			int i = 0;
			for (long[] row : image)
				for (long pix : row)
				{
					output[4*i]   = (byte)((pix & 0xff000000) >> 24);
					output[4*i+1] = (byte)((pix & 0x00ff0000) >> 16);
					output[4*i+2] = (byte)((pix & 0x0000ff00) >> 8);
					output[4*i+3] = (byte)((pix & 0x000000ff) >> 0);
					i++;
				}
						
			if (byteOrder == ByteOrder.INTEL)
				output = intelSwap(output,4);
			
			//output = compress(fi,compression,output);

			output = attachHeader(fi,headerBytes,output);
			
			return output;
		}

		Object expectedResults(long[][] inputImage)
		{
			float[] output = new float[inputImage.length * inputImage[0].length];
			
			int i = 0;
			for (long[] row : inputImage)
				for (long pix : row)
					output[i++] = (float)pix;
			return output;
		}
	}

	class Gray32FloatFormat extends PixelFormat
	{
		Gray32FloatFormat()
		{
			super("Gray32Float");
		}
		
		boolean canDoImageCombo(int compression, ByteOrder byteOrder, int headerBytes)
		{
			if (compression != FileInfo.COMPRESSION_NONE)
				return false;
			
			return true;
		}
		
		byte[] getBytes(long[][] image, int compression, ByteOrder byteOrder, int headerBytes, FileInfo fi)
		{
			initializeFileInfo(fi,FileInfo.GRAY32_FLOAT,compression,byteOrder,image.length,image[0].length);
			
			byte[] output = new byte[fi.height * fi.width * 4];
			
			int i = 0;
			for (long[] row : image)
				for (long pix : row)
				{
					float fPix = (float) pix;
					int bPix = Float.floatToIntBits(fPix);
					output[4*i]   = (byte)((bPix & 0xff000000) >> 24);
					output[4*i+1] = (byte)((bPix & 0x00ff0000) >> 16);
					output[4*i+2] = (byte)((bPix & 0x0000ff00) >> 8);
					output[4*i+3] = (byte)((bPix & 0x000000ff) >> 0);
					i++;
				}
						
			if (byteOrder == ByteOrder.INTEL)
				output = intelSwap(output,4);
			
			//output = compress(fi,compression,output);

			output = attachHeader(fi,headerBytes,output);
			
			return output;
		}

		Object expectedResults(long[][] inputImage)
		{
			float[] output = new float[inputImage.length * inputImage[0].length];
			
			int i = 0;
			for (long[] row : inputImage)
				for (long pix : row)
					output[i++] = (float)pix;
			return output;
		}
	}

	class Gray64FloatFormat extends PixelFormat
	{
		Gray64FloatFormat()
		{
			super("Gray64Float");
		}
		
		boolean canDoImageCombo(int compression, ByteOrder byteOrder, int headerBytes)
		{
			if (compression != FileInfo.COMPRESSION_NONE)
				return false;
			
			return true;
		}
		
		byte[] getBytes(long[][] image, int compression, ByteOrder byteOrder, int headerBytes, FileInfo fi)
		{
			initializeFileInfo(fi,FileInfo.GRAY64_FLOAT,compression,byteOrder,image.length,image[0].length);
			
			byte[] output = new byte[fi.height * fi.width * 8];
			
			int i = 0;
			for (long[] row : image)
				for (long pix : row)
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
						
			if (byteOrder == ByteOrder.INTEL)
				output = intelSwap(output,8);
			
			//output = compress(fi,compression,output);

			output = attachHeader(fi,headerBytes,output);
			
			return output;
		}

		Object expectedResults(long[][] inputImage)
		{
			float[] output = new float[inputImage.length * inputImage[0].length];
			
			int i = 0;
			for (long[] row : inputImage)
				for (long pix : row)
					output[i++] = (float)pix;
			return output;
		}
	}

	class RgbFormat extends PixelFormat
	{
		RgbFormat()
		{
			super("Rgb");
		}
		
		boolean canDoImageCombo(int compression, ByteOrder byteOrder, int headerBytes)
		{
			// TODO: until refactored don't handle lzw-diff
			if (compression == FileInfo.LZW_WITH_DIFFERENCING)
				return false;
			if (compression == FileInfo.COMPRESSION_UNKNOWN)
				return false;
			if (compression == FileInfo.JPEG)  // TODO: remove this restriction when working
				return false;
			
			if (byteOrder == ByteOrder.INTEL)
				return false;
			
			return true;
		}
		
		byte[] getBytes(long[][] image, int compression, ByteOrder byteOrder, int headerBytes, FileInfo fi)
		{
			initializeFileInfo(fi,FileInfo.RGB,compression,byteOrder,image.length,image[0].length);
			
			byte[] output = new byte[fi.height * fi.width * 3];
			
			int i = 0;
			for (long[] row : image)
				for (long pix : row)
				{
					output[3*i+0] = (byte)((pix & 0xff0000) >> 16);
					output[3*i+1] = (byte)((pix & 0x00ff00) >> 8);
					output[3*i+2] = (byte)((pix & 0x0000ff) >> 0);
					i++;
				}
						
			//if (byteOrder == ByteOrder.INTEL)
			//	; // nothing to do
			
			output = compress(fi,compression,output);

			output = attachHeader(fi,headerBytes,output);
			
			return output;
		}

		Object expectedResults(long[][] inputImage)
		{
			int[] output = new int[inputImage.length * inputImage[0].length];
			
			// NOTICE that input is rgb but output is argb
			
			int i = 0;
			for (long[] row : inputImage)
				for (long pix : row)
					output[i++] = (int)(0xff000000 | (pix & 0xffffff));

			return output;
		}
	}

	class BgrFormat extends PixelFormat
	{
		BgrFormat()
		{
			super("Bgr");
		}
		
		boolean canDoImageCombo(int compression, ByteOrder byteOrder, int headerBytes)
		{
			// TODO: until refactored don't handle lzw-diff
			if (compression == FileInfo.LZW_WITH_DIFFERENCING)
				return false;
			if (compression == FileInfo.COMPRESSION_UNKNOWN)
				return false;
			if (compression == FileInfo.JPEG)  // TODO: remove this restriction when working
				return false;
			
			if (byteOrder == ByteOrder.INTEL)
				return false;
			
			return true;
		}
		
		byte[] getBytes(long[][] image, int compression, ByteOrder byteOrder, int headerBytes, FileInfo fi)
		{
			initializeFileInfo(fi,FileInfo.BGR,compression,byteOrder,image.length,image[0].length);
			
			byte[] output = new byte[fi.height * fi.width * 3];
			
			int i = 0;
			for (long[] row : image)
				for (long pix : row)
				{
					output[3*i+0] = (byte)((pix & 0x0000ff) >> 0);
					output[3*i+1] = (byte)((pix & 0x00ff00) >> 8);
					output[3*i+2] = (byte)((pix & 0xff0000) >> 16);
					i++;
				}
						
			//if (byteOrder == ByteOrder.INTEL)
			//	; // nothing to do
			
			output = compress(fi,compression,output);

			output = attachHeader(fi,headerBytes,output);
			
			return output;
		}

		Object expectedResults(long[][] inputImage)
		{
			int[] output = new int[inputImage.length * inputImage[0].length];
			
			// NOTICE that input is bgr but output is argb
			
			int i = 0;
			for (long[] row : inputImage)
				for (long pix : row)
					output[i++] = (int)(0xff000000 | (pix & 0xffffff));

			return output;
		}
	}
	
	class ArgbFormat extends PixelFormat
	{
		ArgbFormat()
		{
			super("Argb");
		}
		
		boolean canDoImageCombo(int compression, ByteOrder byteOrder, int headerBytes)
		{
			// TODO: until refactored don't handle lzw-diff
			if (compression == FileInfo.LZW_WITH_DIFFERENCING)
				return false;
			if (compression == FileInfo.COMPRESSION_UNKNOWN)
				return false;
			if (compression == FileInfo.JPEG)  // TODO: remove this restriction when working
				return false;
			
			return true;
		}
		
		byte[] getBytes(long[][] image, int compression, ByteOrder byteOrder, int headerBytes, FileInfo fi)
		{
			initializeFileInfo(fi,FileInfo.ARGB,compression,byteOrder,image.length,image[0].length);
			
			byte[] output = new byte[fi.height * fi.width * 4];
			
			int i = 0;
			for (long[] row : image)
				for (long pix : row)
				{
					if (byteOrder == ByteOrder.INTEL)
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
						
			output = compress(fi,compression,output);

			output = attachHeader(fi,headerBytes,output);
			
			return output;
		}

		Object expectedResults(long[][] inputImage)
		{
			int[] output = new int[inputImage.length * inputImage[0].length];
			
			// NOTICE that input is bgr but output is argb
			
			int i = 0;
			for (long[] row : inputImage)
				for (long pix : row)
					output[i++] = (int)(0xff000000 | (pix & 0xffffff));

			return output;
		}
	}
	
	class AbgrFormat extends PixelFormat
	{
		AbgrFormat()
		{
			super("Abgr");
		}
		
		boolean canDoImageCombo(int compression, ByteOrder byteOrder, int headerBytes)
		{
			// TODO: until refactored don't handle lzw-diff
			if (compression == FileInfo.LZW_WITH_DIFFERENCING)
				return false;
			if (compression == FileInfo.COMPRESSION_UNKNOWN)
				return false;
			if (compression == FileInfo.JPEG)  // TODO: remove this restriction when working
				return false;
			
			if (byteOrder == ByteOrder.INTEL)
				return false;

			return true;
		}
		
		byte[] getBytes(long[][] image, int compression, ByteOrder byteOrder, int headerBytes, FileInfo fi)
		{
			initializeFileInfo(fi,FileInfo.ABGR,compression,byteOrder,image.length,image[0].length);
			
			byte[] output = new byte[fi.height * fi.width * 4];
			
			int i = 0;
			for (long[] row : image)
				for (long pix : row)
				{
					output[4*i+0] = (byte)((pix & 0x000000ff) >> 0);
					output[4*i+1] = (byte)((pix & 0x0000ff00) >> 8);
					output[4*i+2] = (byte)((pix & 0x00ff0000) >> 16);
					output[4*i+3] = (byte)((pix & 0xff000000) >> 24);
					i++;
				}
						
			//if (byteOrder == ByteOrder.INTEL)
			//	; // nothing to do

			output = compress(fi,compression,output);

			output = attachHeader(fi,headerBytes,output);
			
			return output;
		}

		Object expectedResults(long[][] inputImage)
		{
			int[] output = new int[inputImage.length * inputImage[0].length];
			
			// NOTICE that input is abgr but output is argb
			
			int i = 0;
			for (long[] row : inputImage)
				for (long pix : row)
					output[i++] = (int)(0xff000000 | (pix & 0xffffff));

			return output;
		}
	}
	
	class BargFormat extends PixelFormat
	{
		BargFormat()
		{
			super("Barg");
		}
		
		boolean canDoImageCombo(int compression, ByteOrder byteOrder, int headerBytes)
		{
			// TODO: until refactored don't handle lzw-diff
			if (compression == FileInfo.LZW_WITH_DIFFERENCING)
				return false;
			if (compression == FileInfo.COMPRESSION_UNKNOWN)
				return false;
			if (compression == FileInfo.JPEG)  // TODO: remove this restriction when working
				return false;
			
			if (byteOrder == ByteOrder.INTEL)
				return false;

			return true;
		}
		
		byte[] getBytes(long[][] image, int compression, ByteOrder byteOrder, int headerBytes, FileInfo fi)
		{
			initializeFileInfo(fi,FileInfo.BARG,compression,byteOrder,image.length,image[0].length);
			
			byte[] output = new byte[fi.height * fi.width * 4];
			
			int i = 0;
			for (long[] row : image)
				for (long pix : row)
				{
					output[4*i+0] = (byte)((pix & 0x000000ff) >> 0);
					output[4*i+1] = (byte)((pix & 0xff000000) >> 24);
					output[4*i+2] = (byte)((pix & 0x00ff0000) >> 16);
					output[4*i+3] = (byte)((pix & 0x0000ff00) >> 8);
					i++;
				}
						
			//if (byteOrder == ByteOrder.INTEL)
			//	; // nothing to do

			output = compress(fi,compression,output);

			output = attachHeader(fi,headerBytes,output);
			
			return output;
		}

		Object expectedResults(long[][] inputImage)
		{
			int[] output = new int[inputImage.length * inputImage[0].length];
			
			// NOTICE that input is barg but output is argb
			
			int i = 0;
			for (long[] row : inputImage)
				for (long pix : row)
					output[i++] = (int)(0xff000000 | (pix & 0xffffff));

			return output;
		}
	}

	class RgbPlanarFormat extends PixelFormat
	{
		RgbPlanarFormat()
		{
			super("RgbPlanar");
		}
		
		boolean canDoImageCombo(int compression, ByteOrder byteOrder, int headerBytes)
		{
			// TODO: until refactored don't handle lzw-diff
			if (compression == FileInfo.LZW_WITH_DIFFERENCING)
				return false;
			if (compression == FileInfo.COMPRESSION_UNKNOWN)
				return false;
			if (compression == FileInfo.JPEG)  // TODO: remove this restriction when working
				return false;
			
			if (byteOrder == ByteOrder.INTEL)
				return false;

			return true;
		}
		
		byte[] getBytes(long[][] image, int compression, ByteOrder byteOrder, int headerBytes, FileInfo fi)
		{
			initializeFileInfo(fi,FileInfo.RGB_PLANAR,compression,byteOrder,image.length,image[0].length);
			
			int planeSize = fi.height * fi.width;
			
			byte[] output = new byte[planeSize * 3];
			
			int i = 0;
			for (long[] row : image)
				for (long pix : row)
				{				
					output[(0*planeSize)+i] = (byte)((pix & 0x00ff0000) >> 16);
					output[(1*planeSize)+i] = (byte)((pix & 0x0000ff00) >> 8);
					output[(2*planeSize)+i] = (byte)((pix & 0x000000ff) >> 0);
					i++;
				}
						
			//if (byteOrder == ByteOrder.INTEL)
			//	; // nothing to do

			output = compress(fi,compression,output);

			output = attachHeader(fi,headerBytes,output);
			
			return output;
		}

		Object expectedResults(long[][] inputImage)
		{
			int[] output = new int[inputImage.length * inputImage[0].length];
			
			// NOTICE input is rgb planar but output is argb
			
			int i = 0;
			for (long[] row : inputImage)
				for (long pix : row)
					output[i++] = (int)(0xff000000 | (pix & 0xffffff));

			return output;
		}
	}

	class BitmapFormat extends PixelFormat
	{
		BitmapFormat()
		{
			super("Bitmap");
		}
		
		boolean canDoImageCombo(int compression, ByteOrder byteOrder, int headerBytes)
		{
			if (compression != FileInfo.COMPRESSION_NONE)
				return false;
			
			if (byteOrder == ByteOrder.INTEL)
				return false;

			return true;
		}

		byte[] getBytes(long[][] image, int compression, ByteOrder byteOrder, int headerBytes, FileInfo fi)
		{
			initializeFileInfo(fi,FileInfo.BITMAP,compression,byteOrder,image.length,image[0].length);
			
			int pixPerRow = (int) Math.ceil(fi.width / 8.0);
			
			byte[] output = new byte[fi.height * pixPerRow];

			// note that I am only using the lowest 1 bit of the int for testing purposes
			
			int i = 0;
			byte currByte = 0;
			
			int rows = fi.height;
			int cols = fi.width;
			for (int r = 0; r < rows; r++)
				for (int c = 0; c < cols; c++)
				{
					if ((image[r][c] & 1) == 1) // if odd
						currByte |= (1 << (7-(c%8)));
					if (((c%8) == 7) || (c == (cols-1)))
					{
						output[i++] = currByte;
						currByte = 0;
					}
				}
						
			//if (byteOrder == ByteOrder.INTEL)
			//	; // nothing to do

			//output = compress(fi,compression,output);

			output = attachHeader(fi,headerBytes,output);
			
			return output;
		}

		Object expectedResults(long[][] inputImage)
		{
			int rows = inputImage.length;
			int cols = inputImage[0].length;
			
			byte[] output = new byte[rows * cols];
		
			// from earlier testing it was case that
			//   input = {128,64,32}
			//   output = {255,0,0,0,255,0,0,0,255}
			// this following code does not seem to match this
			
			int i = 0;
			for (int r = 0; r < rows; r++)
				for (int c = 0; c < cols; c++)
					output[i++] = ((inputImage[r][c] & 1) == 1) ? (byte)255 : 0;
			
			return output;
		}		
	}

	class Rgb48Format extends PixelFormat
	{
		Rgb48Format()
		{
			super("Rgb48");
		}
		
		boolean canDoImageCombo(int compression, ByteOrder byteOrder, int headerBytes)
		{
			// TODO: until refactored don't handle lzw-diff
			if (compression == FileInfo.LZW_WITH_DIFFERENCING)
				return false;
			if (compression == FileInfo.COMPRESSION_UNKNOWN)
				return false;
			if (compression == FileInfo.JPEG)  // TODO: remove this restriction when working
				return false;
			
			return true;
		}

		byte[] getBytes(long[][] image, int compression, ByteOrder byteOrder, int headerBytes, FileInfo fi)
		{
			initializeFileInfo(fi,FileInfo.RGB48,compression,byteOrder,image.length,image[0].length);
			
			// Set strip info: Needed for this case: for now lets do one strip per row
			fi.stripLengths = new int[image.length];
			fi.stripOffsets = new int[image.length];
			for (int i = 0; i < image.length; i++)
			{
				fi.stripLengths[i] = 6*image[0].length;
				// was this and seemed to work fi.stripOffsets[i] = 6*i;
				fi.stripOffsets[i] = (i == 0 ? 0 : (fi.stripOffsets[i-1] + fi.stripLengths[i]));
			}
			
			byte[] output = new byte[fi.height * fi.width * 6];

			// note that I am only using the lowest 8 bits of the int for testing purposes
			
			int i = 0;
			for (long[] row : image)
				for (long wholeLong : row)
				{
					output[i++] = (byte)((wholeLong & 0xff0000000000L) >> 40);
					output[i++] = (byte)((wholeLong & 0xff00000000L) >> 32);
					output[i++] = (byte)((wholeLong & 0xff000000L) >> 24);
					output[i++] = (byte)((wholeLong & 0xff0000L) >> 16);
					output[i++] = (byte)((wholeLong & 0xff00L) >> 8);
					output[i++] = (byte)((wholeLong & 0xffL) >> 0);
				}
						
			if (byteOrder == ByteOrder.INTEL)
				output = intelSwap(output,2);

			output = compress(fi,compression,output);

			output = attachHeader(fi,headerBytes,output);
			
			return output;
		}

		Object expectedResults(long[][] inputImage)
		{
			int rows = inputImage.length;
			int cols = inputImage[0].length;

			short[][] output = new short[3][];
			
			for (int i = 0; i < 3; i++)
				output[i] = new short[rows*cols];
			
			int i = 0;
			for (int r = 0; r < rows; r++)
				for (int c = 0; c < cols; c++)
				{
					output[0][i] = (short) ((inputImage[r][c] & 0xffff00000000L) >> 32);
					output[1][i] = (short) ((inputImage[r][c] & 0x0000ffff0000L) >> 16);
					output[2][i] = (short) ((inputImage[r][c] & 0x00000000ffffL) >> 0);
					i++;
				}
			
			return output;
		}		
	}

	class Rgb48PlanarFormat extends PixelFormat
	{
		Rgb48PlanarFormat()
		{
			super("Rgb48Planar");
		}
		
		boolean canDoImageCombo(int compression, ByteOrder byteOrder, int headerBytes)
		{
			// TODO: until refactored don't handle lzw-diff
			if (compression == FileInfo.LZW_WITH_DIFFERENCING)
				return false;
			if (compression == FileInfo.COMPRESSION_UNKNOWN)
				return false;
			if (compression == FileInfo.JPEG)  // TODO: remove this restriction when working
				return false;
			if (compression == FileInfo.PACK_BITS)
				return false;
			
			return true;
		}

		byte[] getBytes(long[][] image, int compression, ByteOrder byteOrder, int headerBytes, FileInfo fi)
		{
			initializeFileInfo(fi,FileInfo.RGB48_PLANAR,compression,byteOrder,image.length,image[0].length);
			
			// Set strip info:
			//   let's do one big strip of data : any more strips and decompression code runs in ImageReader (TODO that could be a bug there)
			fi.stripLengths = new int[] {fi.height * fi.width * 6};
			fi.stripOffsets = new int[] {0};
			
			// note that I am only using the lowest 8 bits of the input int pixel for testing purposes

			int rows = fi.height;
			int cols = fi.width;
			int totPix = rows * cols;

			byte[] plane1 = new byte[totPix*2];
			byte[] plane2 = new byte[totPix*2];
			byte[] plane3 = new byte[totPix*2];
			
			// populate planes from int data
			int i = 0;
			for (long[] row : image)
				for (long wholeLong : row)
				{
					long channel1 = ((wholeLong & 0x00000000ffffL) >> 0);
					long channel2 = ((wholeLong & 0x0000ffff0000L) >> 16);
					long channel3 = ((wholeLong & 0xffff00000000L) >> 32);
					
					// divide the int into three channels
					plane1[2*i+0] = (byte) ((channel1 & 0xff00) >> 8);
					plane1[2*i+1] = (byte) ((channel1 & 0x00ff) >> 0);
					plane2[2*i+0] = (byte) ((channel2 & 0xff00) >> 8);
					plane2[2*i+1] = (byte) ((channel2 & 0x00ff) >> 0);
					plane3[2*i+0] = (byte) ((channel3 & 0xff00) >> 8);
					plane3[2*i+1] = (byte) ((channel3 & 0x00ff) >> 0);
					i++;
				}
			
			if (byteOrder == ByteOrder.INTEL)
			{
				plane1 = intelSwap(plane1,2);
				plane2 = intelSwap(plane2,2);
				plane3 = intelSwap(plane3,2);
			}
			
			int biggestPlane = Math.max(Math.max(plane1.length, plane2.length), plane2.length);

			if (compression == FileInfo.LZW)
			{
				plane1 = lzwEncoder.encode(plane1); // compress the output data
				plane2 = lzwEncoder.encode(plane2); // compress the output data
				plane3 = lzwEncoder.encode(plane3); // compress the output data
			
				biggestPlane = Math.max(Math.max(plane1.length, plane2.length), plane2.length);
				
				// does not work
				//output = new byte[plane1.length + plane2.length + plane3.length];
				//fi.stripLengths = new int[] {output.length};
				//fi.stripOffsets = new int[] {0};
				
				// apparently the 48 bit planar type stores three sixteen bit images.
				// when read the stripOffsets are reused so each plane must be stored in the same size strip.
				// so must allocate the overall pixel array to be big enough to contain the biggest plane three times and
				// setup the strip offsets to be the same for each plane.
				
				fi.stripLengths = new int[] {biggestPlane};
				fi.stripOffsets = new int[] {0};
			}
			else if (compression == FileInfo.LZW_WITH_DIFFERENCING)
			{
				plane1 = lzwDiffEncoder.encode(plane1); // compress the output data
				plane2 = lzwDiffEncoder.encode(plane2); // compress the output data
				plane3 = lzwDiffEncoder.encode(plane3); // compress the output data
			
				biggestPlane = Math.max(Math.max(plane1.length, plane2.length), plane2.length);
				
				// does not work
				//output = new byte[plane1.length + plane2.length + plane3.length];
				//fi.stripLengths = new int[] {output.length};
				//fi.stripOffsets = new int[] {0};
				
				// apparently the 48 bit planar type stores three sixteen bit images.
				// when read the stripOffsets are reused so each plane must be stored in the same size strip.
				// so must allocate the overall pixel array to be big enough to contain the biggest plane three times and
				// setup the strip offsets to be the same for each plane.
				
				fi.stripLengths = new int[] {biggestPlane};
				fi.stripOffsets = new int[] {0};
			}

			byte[] output = new byte[biggestPlane*3];

			// finally combine planes : note that the written planes are <= biggestPlane in length
			System.arraycopy(plane1, 0, output, 0, plane1.length);
			System.arraycopy(plane2, 0, output, biggestPlane, plane2.length);
			System.arraycopy(plane3, 0, output, 2*biggestPlane, plane3.length);

			output = attachHeader(fi,headerBytes,output);
			
			return output;
		}

		
		Object expectedResults(long[][] inputImage)
		{
			int rows = inputImage.length;
			int cols = inputImage[0].length;

			short[][] planes = new short[3][];
			
			
			for (int i = 0; i < 3; i++)
				planes[i] = new short[rows*cols];
			
			int i = 0;
			for (int r = 0; r < rows; r++)
				for (int c = 0; c < cols; c++)
				{
					planes[0][i] = (short) ((inputImage[r][c] & 0x00000000ffffL) >> 0);
					planes[1][i] = (short) ((inputImage[r][c] & 0x0000ffff0000L) >> 16);
					planes[2][i] = (short) ((inputImage[r][c] & 0xffff00000000L) >> 32);
					i++;
				}
			
			Object[] output = new Object[3];
			
			output[0] = planes[0];
			output[1] = planes[1];
			output[2] = planes[2];
			
			return output;
		}		
	}
		
	class Gray12UnsignedFormat extends PixelFormat
	{
		Gray12UnsignedFormat()
		{
			super("Gray12Unsigned");
		}
		
		boolean canDoImageCombo(int compression, ByteOrder byteOrder, int headerBytes)
		{
			if (compression != FileInfo.COMPRESSION_NONE)
				return false;

			if (byteOrder == ByteOrder.INTEL)
				return false;
			
			return true;
		}

		byte[] getBytes(long[][] image, int compression, ByteOrder byteOrder, int headerBytes, FileInfo fi)
		{
			initializeFileInfo(fi,FileInfo.GRAY12_UNSIGNED,compression,byteOrder,image.length,image[0].length);
			
			byte[] output = twelveBitEncoder.encode(image);
			
			// if (byteOrder == ByteOrder.INTEL)
			//	;  // nothing to do

			output = attachHeader(fi,headerBytes,output);
			
			return output;
		}

		Object expectedResults(long[][] inputImage)
		{
			short[] output = new short[inputImage.length * inputImage[0].length];
			
			int i = 0;
			for (long[] row : inputImage)
				for (long pix: row)
					output[i++] = (short)(pix & 0xfff);
			
			return output;
		}		
	}

	class Gray24UnsignedFormat extends PixelFormat
	{
		Gray24UnsignedFormat()
		{
			super("Gray24Unsigned");
		}
		
		boolean canDoImageCombo(int compression, ByteOrder byteOrder, int headerBytes)
		{
			if (compression != FileInfo.COMPRESSION_NONE)
				return false;

			if (byteOrder == ByteOrder.INTEL)
				return false;
			
			return true;
		}

		byte[] getBytes(long[][] image, int compression, ByteOrder byteOrder, int headerBytes, FileInfo fi)
		{
			initializeFileInfo(fi,FileInfo.GRAY24_UNSIGNED,compression,byteOrder,image.length,image[0].length);
			
			byte[] output = new byte[fi.height * fi.width * 3];
			
			int i = 0;
			for (long[] row : image)
				for (long pix : row)
				{
					output[i++] = (byte) ((pix & 0x0000ff) >> 0);
					output[i++] = (byte) ((pix & 0x00ff00) >> 8);
					output[i++] = (byte) ((pix & 0xff0000) >> 16);
				}

			// if (byteOrder == ByteOrder.INTEL)
			//	; // nothing to do

			//output = compress(fi,compression,output);

			output = attachHeader(fi,headerBytes,output);
			
			return output;
		}

		Object expectedResults(long[][] inputImage)
		{
			float[] output = new float[inputImage.length * inputImage[0].length];
				
			int i = 0;
			for (long[] row : inputImage)
				for (long pix: row)
					output[i++] = (float)(pix & 0xffffff);
			
			return output;
		}		
	}
	
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

	// *********************** ImageReaderTest helper methods  **************************************

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
	

	// *********************** ImageReader Tests  **************************************

	@Test
	public void testImageReader() {
		
		FileInfo f = new FileInfo();
		ImageReader reader = new ImageReader(f);
		
		assertNotNull(reader);
	}

	// unknown file type
	private void runBogusFileTypeTests()
	{
		FileInfo fi = new FileInfo();
		
		fi.compression = FileInfo.COMPRESSION_NONE;
		fi.height = 1;
		fi.width = 3;
		
		byte[] inBytes = new byte[] {5,3,1};
		
		Object pixels;
		
		fi.fileType = -1;
		pixels = callReadPixels(fi,inBytes);
		assertNull(pixels);

		fi.fileType = -18462564;
		pixels = callReadPixels(fi,inBytes);
		assertNull(pixels);

		fi.fileType = 1014;
		pixels = callReadPixels(fi,inBytes);
		assertNull(pixels);
	}
	
	private byte[] gray8PixelBytes(long[][] inputData, FileInfo fi, int compression, int headerBytes, boolean intelByteOrder)
	{
		// set FileInfo fields for subsequent calls
		
		fi.fileType = FileInfo.GRAY8;
		fi.compression = compression;
		fi.intelByteOrder = intelByteOrder;
		fi.height = inputData.length;
		fi.width = inputData[0].length;
		
		byte[] output = new byte[fi.height * fi.width];
		
		int i = 0;
		for (long[] row : inputData)
			for (long pix : row)
				output[i++] = (byte)(pix & 0xff);
	
		if (intelByteOrder)
			; // nothing to do
		
		if (compression == FileInfo.LZW)
		{
			output = lzwEncoder.encode(output);
			fi.stripLengths = new int[] {output.length};
			fi.stripOffsets = new int[] {0};
		}
		else if (compression == FileInfo.LZW_WITH_DIFFERENCING)
		{
			output = lzwDiffEncoder.encode(output);
			fi.stripLengths = new int[] {output.length};
			fi.stripOffsets = new int[] {0};
		}
		else if (compression == FileInfo.PACK_BITS)
		{
			output = packbitsEncoder.encode(output);
			fi.stripLengths = new int[] {output.length};
			fi.stripOffsets = new int[] {0};
			fi.rowsPerStrip = fi.height;
		}

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

	private byte[] gray8ExpectedOutput(long[][] image, boolean intelByteOrder)
	{
		byte[] output = new byte[image.length * image[0].length];
		
		int i = 0;
		for (long[] row : image)
			for (long pix : row)
				output[i++] = (byte) (pix & 0xff);
		
		return output;
	}

	private void tryGray8Image(long[][] baseImage, int compression, int headerBytes, boolean intelByteOrder)
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
	private void runGray8FileTypeTests()
	{
		tryGray8Image(BaseImage1x1,FileInfo.COMPRESSION_NONE,0,false);
		tryGray8Image(BaseImage3x3,FileInfo.COMPRESSION_NONE,0,false);
		tryGray8Image(BaseImage1x9,FileInfo.COMPRESSION_NONE,0,false);
		tryGray8Image(BaseImage7x2,FileInfo.COMPRESSION_NONE,0,false);
		tryGray8Image(BaseImage5x4,FileInfo.COMPRESSION_NONE,0,false);
		tryGray8Image(BaseImage4x6,FileInfo.COMPRESSION_NONE,0,false);
		tryGray8Image(BaseTestImage,FileInfo.COMPRESSION_NONE,0,false);
		
		tryGray8Image(BaseTestImage,FileInfo.COMPRESSION_NONE,63,false);  // test header specifically
		tryGray8Image(BaseTestImage,FileInfo.COMPRESSION_NONE,0,true);    // test intel specifically
		tryGray8Image(BaseTestImage,FileInfo.LZW,0,false);    // test LZW compression specifically
		tryGray8Image(BaseTestImage,FileInfo.PACK_BITS,0,false);    // test PACK_BITS compression specifically
	}

	private byte[] color8PixelBytes(long[][] inputData, FileInfo fi, int compression, int headerBytes, boolean intelByteOrder)
	{
		// set FileInfo fields for subsequent calls
		
		fi.fileType = FileInfo.COLOR8;
		fi.compression = compression;
		fi.intelByteOrder = intelByteOrder;
		fi.height = inputData.length;
		fi.width = inputData[0].length;
		
		byte[] output = new byte[fi.height * fi.width];
		
		int i = 0;
		for (long[] row : inputData)
			for (long pix : row)
				output[i++] = (byte)(pix & 0xff);
	
		//if (intelByteOrder)
		//	; // nothing to do for a byte oriented data stream
		
		if (compression == FileInfo.LZW)
		{
			output = lzwEncoder.encode(output);
			fi.stripLengths = new int[] {output.length};
			fi.stripOffsets = new int[] {0};
		}
		else if (compression == FileInfo.LZW_WITH_DIFFERENCING)
		{
			output = lzwDiffEncoder.encode(output);
			fi.stripLengths = new int[] {output.length};
			fi.stripOffsets = new int[] {0};
		}
		else if (compression == FileInfo.PACK_BITS)
		{
			output = packbitsEncoder.encode(output);
			fi.stripLengths = new int[] {output.length};
			fi.stripOffsets = new int[] {0};
			fi.rowsPerStrip = fi.height;
		}

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

	private byte[] color8ExpectedOutput(long[][] image, boolean intelByteOrder)
	{
		byte[] output = new byte[image.length * image[0].length];
		
		int i = 0;
		for (long[] row : image)
			for (long pix : row)
				output[i++] = (byte) (pix & 0xff);
		
		return output;
	}

	private void tryColor8Image(long[][] baseImage, int compression, int headerBytes, boolean intelByteOrder)
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
	private void runColor8FileTypeTests()
	{
		tryColor8Image(BaseImage1x1,FileInfo.COMPRESSION_NONE,0,false);
		tryColor8Image(BaseImage3x3,FileInfo.COMPRESSION_NONE,0,false);
		tryColor8Image(BaseImage1x9,FileInfo.COMPRESSION_NONE,0,false);
		tryColor8Image(BaseImage7x2,FileInfo.COMPRESSION_NONE,0,false);
		tryColor8Image(BaseImage5x4,FileInfo.COMPRESSION_NONE,0,false);
		tryColor8Image(BaseImage4x6,FileInfo.COMPRESSION_NONE,0,false);
		tryColor8Image(BaseTestImage,FileInfo.COMPRESSION_NONE,0,false);
		
		tryColor8Image(BaseTestImage,FileInfo.COMPRESSION_NONE,63,false);  // test header specifically
		tryColor8Image(BaseTestImage,FileInfo.COMPRESSION_NONE,0,true);    // test intel specifically
		tryColor8Image(BaseTestImage,FileInfo.LZW,0,false);    // test LZW compression specifically
		tryColor8Image(BaseTestImage,FileInfo.PACK_BITS,0,false);    // test PACK_BITS compression specifically
	}

	private byte[] gray16SignedPixelBytes(long[][] inputData, FileInfo fi, int compression, int headerBytes, boolean intelByteOrder)
	{
		// set FileInfo fields for subsequent calls
		
		fi.fileType = FileInfo.GRAY16_SIGNED;
		fi.compression = compression;
		fi.intelByteOrder = intelByteOrder;
		fi.height = inputData.length;
		fi.width = inputData[0].length;
		
		byte[] output = new byte[fi.height * fi.width * 2];
		
		int i = 0;
		for (long[] row : inputData)
			for (long pix : row)
			{
				output[2*i]   = (byte)((pix & 0xff00) >> 8);
				output[2*i+1] = (byte)((pix & 0x00ff) >> 0);
				i++;
			}
		
		if (intelByteOrder)
			output = intelSwap(output,2);
		
		if (compression == FileInfo.LZW)
		{
			output = lzwEncoder.encode(output);
			fi.stripLengths = new int[] {output.length};
			fi.stripOffsets = new int[] {0};
		}
		else if (compression == FileInfo.LZW_WITH_DIFFERENCING)
		{
			output = lzwDiffEncoder.encode(output);
			fi.stripLengths = new int[] {output.length};
			fi.stripOffsets = new int[] {0};
		}
		else if (compression == FileInfo.PACK_BITS)
			;
		
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

	private short[] gray16SignedExpectedOutput(long[][] image, boolean intelByteOrder)
	{
		short[] output = new short[image.length * image[0].length];
		
		int i = 0;
		for (long[] row : image)
			for (long pix : row)
				output[i++] = (short) (32768 + (pix & 0xffff)); // bias taken from ImageReader.readPixels()
		return output;
	}

	private void tryGray16SignedImage(long[][] baseImage, int compression, int headerBytes, boolean intelByteOrder)
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
	private void runGray16SignedFileTypeTests()
	{
		tryGray16SignedImage(BaseImage1x1,FileInfo.COMPRESSION_NONE,0,false);
		tryGray16SignedImage(BaseImage3x3,FileInfo.COMPRESSION_NONE,0,false);
		tryGray16SignedImage(BaseImage1x9,FileInfo.COMPRESSION_NONE,0,false);
		tryGray16SignedImage(BaseImage7x2,FileInfo.COMPRESSION_NONE,0,false);
		tryGray16SignedImage(BaseImage5x4,FileInfo.COMPRESSION_NONE,0,false);
		tryGray16SignedImage(BaseImage4x6,FileInfo.COMPRESSION_NONE,0,false);
		tryGray16SignedImage(BaseTestImage,FileInfo.COMPRESSION_NONE,0,false);
		
		tryGray16SignedImage(BaseTestImage,FileInfo.COMPRESSION_NONE,1003,false);  // test header specifically
		tryGray16SignedImage(BaseTestImage,FileInfo.COMPRESSION_NONE,0,true);      // test intel specifically
		tryGray16SignedImage(BaseTestImage,FileInfo.LZW,0,false);    // test LZW compression specifically
		// NO PACK_BITS
	}

	
	private byte[] gray16UnsignedPixelBytes(long[][] inputData, FileInfo fi, int compression, int headerBytes, boolean intelByteOrder)
	{
		// set FileInfo fields for subsequent calls
		
		fi.fileType = FileInfo.GRAY16_UNSIGNED;
		fi.compression = compression;
		fi.intelByteOrder = intelByteOrder;
		fi.height = inputData.length;
		fi.width = inputData[0].length;
		
		byte[] output = new byte[fi.height * fi.width * 2];
		
		int i = 0;
		for (long[] row : inputData)
			for (long pix : row)
			{
				output[2*i]   = (byte)((pix & 0xff00) >> 8);
				output[2*i+1] = (byte)((pix & 0x00ff) >> 0);
				i++;
			}
		
		if (intelByteOrder)
			output = intelSwap(output,2);
		
		if (compression == FileInfo.LZW)
		{
			output = lzwEncoder.encode(output);
			fi.stripLengths = new int[] {output.length};
			fi.stripOffsets = new int[] {0};
		}
		else if (compression == FileInfo.LZW_WITH_DIFFERENCING)
		{
			output = lzwDiffEncoder.encode(output);
			fi.stripLengths = new int[] {output.length};
			fi.stripOffsets = new int[] {0};
		}
		else if (compression == FileInfo.PACK_BITS)
			;

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

	private short[] gray16UnsignedExpectedOutput(long[][] image, boolean intelByteOrder)
	{
		short[] output = new short[image.length * image[0].length];
		
		int i = 0;
		for (long[] row : image)
			for (long pix : row)
				output[i++] = (short)(pix & 0xffff);
		return output;
	}

	private void tryGray16UnsignedImage(long[][] baseImage, int compression, int headerBytes, boolean intelByteOrder)
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
	private void runGray16UnsignedFileTypeTests()
	{
		tryGray16UnsignedImage(BaseImage1x1,FileInfo.COMPRESSION_NONE,0,false);
		tryGray16UnsignedImage(BaseImage3x3,FileInfo.COMPRESSION_NONE,0,false);
		tryGray16UnsignedImage(BaseImage1x9,FileInfo.COMPRESSION_NONE,0,false);
		tryGray16UnsignedImage(BaseImage7x2,FileInfo.COMPRESSION_NONE,0,false);
		tryGray16UnsignedImage(BaseImage5x4,FileInfo.COMPRESSION_NONE,0,false);
		tryGray16UnsignedImage(BaseImage4x6,FileInfo.COMPRESSION_NONE,0,false);
		tryGray16UnsignedImage(BaseTestImage,FileInfo.COMPRESSION_NONE,0,false);
		
		tryGray16UnsignedImage(BaseTestImage,FileInfo.COMPRESSION_NONE,16,false);  // test header specifically
		tryGray16UnsignedImage(BaseTestImage,FileInfo.COMPRESSION_NONE,0,true);    // test intel specifically
		tryGray16UnsignedImage(BaseTestImage,FileInfo.LZW,0,false);    // test LZW compression specifically
		// NO PACK_BITS
	}
	
	private byte[] gray32IntPixelBytes(long[][] inputData, FileInfo fi, int compression, int headerBytes, boolean intelByteOrder)
	{
		// set FileInfo fields for subsequent calls
		
		fi.fileType = FileInfo.GRAY32_INT;
		fi.compression = compression;
		fi.intelByteOrder = intelByteOrder;
		fi.height = inputData.length;
		fi.width = inputData[0].length;
		
		byte[] output = new byte[fi.height * fi.width * 4];
		
		int i = 0;
		for (long[] row : inputData)
			for (long pix : row)
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
			;
		else if (compression == FileInfo.LZW_WITH_DIFFERENCING)
			;
		else if (compression == FileInfo.PACK_BITS)
			;

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

	private float[] gray32IntExpectedOutput(long[][] image, boolean intelByteOrder)
	{
		float[] output = new float[image.length * image[0].length];
		
		int i = 0;
		for (long[] row : image)
			for (long pix : row)
				output[i++] = (float)pix;
		return output;
	}

	private void tryGray32IntImage(long[][] baseImage, int compression, int headerBytes, boolean intelByteOrder)
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
	private void runGray32IntFileTypeTests()
	{
		tryGray32IntImage(BaseImage1x1,FileInfo.COMPRESSION_NONE,0,false);
		tryGray32IntImage(BaseImage3x3,FileInfo.COMPRESSION_NONE,0,false);
		tryGray32IntImage(BaseImage1x9,FileInfo.COMPRESSION_NONE,0,false);
		tryGray32IntImage(BaseImage7x2,FileInfo.COMPRESSION_NONE,0,false);
		tryGray32IntImage(BaseImage5x4,FileInfo.COMPRESSION_NONE,0,false);
		tryGray32IntImage(BaseImage4x6,FileInfo.COMPRESSION_NONE,0,false);
		tryGray32IntImage(BaseTestImage,FileInfo.COMPRESSION_NONE,0,false);
		
		tryGray32IntImage(BaseTestImage,FileInfo.COMPRESSION_NONE,404,false);  // test header specifically
		tryGray32IntImage(BaseTestImage,FileInfo.COMPRESSION_NONE,0,true);     // test intel specifically
		// NO LZW
		// NO LZW_DIFFERENCING
		// NO PACK_BITS
	}
	
	private byte[] gray32UnsignedPixelBytes(long[][] inputData, FileInfo fi, int compression, int headerBytes, boolean intelByteOrder)
	{
		// set FileInfo fields for subsequent calls
		
		fi.fileType = FileInfo.GRAY32_UNSIGNED;
		fi.compression = compression;
		fi.intelByteOrder = intelByteOrder;
		fi.height = inputData.length;
		fi.width = inputData[0].length;
		
		byte[] output = new byte[fi.height * fi.width * 4];
		
		int i = 0;
		for (long[] row : inputData)
			for (long pix : row)
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
			;
		else if (compression == FileInfo.LZW_WITH_DIFFERENCING)
			;
		else if (compression == FileInfo.PACK_BITS)
			;

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

	private float[] gray32UnsignedExpectedOutput(long[][] image, boolean intelByteOrder)
	{
		float[] output = new float[image.length * image[0].length];
		
		int i = 0;
		for (long[] row : image)
			for (long pix : row)
				output[i++] = (float)pix;
		return output;
	}

	private void tryGray32UnsignedImage(long[][] baseImage, int compression, int headerBytes, boolean intelByteOrder)
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
	private void runGray32UnsignedFileTypeTests()
	{
		tryGray32UnsignedImage(BaseImage1x1,FileInfo.COMPRESSION_NONE,0,false);
		tryGray32UnsignedImage(BaseImage3x3,FileInfo.COMPRESSION_NONE,0,false);
		tryGray32UnsignedImage(BaseImage1x9,FileInfo.COMPRESSION_NONE,0,false);
		tryGray32UnsignedImage(BaseImage7x2,FileInfo.COMPRESSION_NONE,0,false);
		tryGray32UnsignedImage(BaseImage5x4,FileInfo.COMPRESSION_NONE,0,false);
		tryGray32UnsignedImage(BaseImage4x6,FileInfo.COMPRESSION_NONE,0,false);
		tryGray32UnsignedImage(BaseTestImage,FileInfo.COMPRESSION_NONE,0,false);
		
		tryGray32UnsignedImage(BaseTestImage,FileInfo.COMPRESSION_NONE,101,false);  // test header specifically
		tryGray32UnsignedImage(BaseTestImage,FileInfo.COMPRESSION_NONE,0,true);     // test intel specifically
		// NO LZW
		// NO LZW_DIFFERENCING
		// NO PACK_BITS
	}
	
	private byte[] gray32FloatPixelBytes(long[][] inputData, FileInfo fi, int compression, int headerBytes, boolean intelByteOrder)
	{
		// set FileInfo fields for subsequent calls
		
		fi.fileType = FileInfo.GRAY32_FLOAT;
		fi.compression = compression;
		fi.intelByteOrder = intelByteOrder;
		fi.height = inputData.length;
		fi.width = inputData[0].length;
		
		byte[] output = new byte[fi.height * fi.width * 4];
		
		int i = 0;
		for (long[] row : inputData)
			for (long pix : row)
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
			;
		else if (compression == FileInfo.LZW_WITH_DIFFERENCING)
			;
		else if (compression == FileInfo.PACK_BITS)
			;

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

	private float[] gray32FloatExpectedOutput(long[][] image, boolean intelByteOrder)
	{
		float[] output = new float[image.length * image[0].length];
		
		int i = 0;
		for (long[] row : image)
			for (long pix : row)
				output[i++] = (float)pix;
		return output;
	}

	private void tryGray32FloatImage(long[][] baseImage, int compression, int headerBytes, boolean intelByteOrder)
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
	private void runGray32FloatFileTypeTests()
	{
		tryGray32FloatImage(BaseImage1x1,FileInfo.COMPRESSION_NONE,0,false);
		tryGray32FloatImage(BaseImage3x3,FileInfo.COMPRESSION_NONE,0,false);
		tryGray32FloatImage(BaseImage1x9,FileInfo.COMPRESSION_NONE,0,false);
		tryGray32FloatImage(BaseImage7x2,FileInfo.COMPRESSION_NONE,0,false);
		tryGray32FloatImage(BaseImage5x4,FileInfo.COMPRESSION_NONE,0,false);
		tryGray32FloatImage(BaseImage4x6,FileInfo.COMPRESSION_NONE,0,false);
		tryGray32FloatImage(BaseTestImage,FileInfo.COMPRESSION_NONE,0,false);
		
		tryGray32FloatImage(BaseTestImage,FileInfo.COMPRESSION_NONE,611,false);  // test header specifically
		tryGray32FloatImage(BaseTestImage,FileInfo.COMPRESSION_NONE,0,true);     // test intel specifically
		
		// NO LZW
		// NO LZW_DIFFERENCING
		// NO PACK_BITS
	}
	
	private byte[] gray64FloatPixelBytes(long[][] inputData, FileInfo fi, int compression, int headerBytes, boolean intelByteOrder)
	{
		// set FileInfo fields for subsequent calls
		
		fi.fileType = FileInfo.GRAY64_FLOAT;
		fi.compression = compression;
		fi.intelByteOrder = intelByteOrder;
		fi.height = inputData.length;
		fi.width = inputData[0].length;
		
		byte[] output = new byte[fi.height * fi.width * 8];
		
		int i = 0;
		for (long[] row : inputData)
			for (long pix : row)
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
			;
		else if (compression == FileInfo.LZW_WITH_DIFFERENCING)
			;
		else if (compression == FileInfo.PACK_BITS)
			;

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

	private float[] gray64FloatExpectedOutput(long[][] image, boolean intelByteOrder)
	{
		float[] output = new float[image.length * image[0].length];
		
		int i = 0;
		for (long[] row : image)
			for (long pix : row)
				output[i++] = (float)pix;
		return output;
	}

	private void tryGray64FloatImage(long[][] baseImage, int compression, int headerBytes, boolean intelByteOrder)
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
	private void runGray64FloatFileTypeTests()
	{
		tryGray64FloatImage(BaseImage1x1,FileInfo.COMPRESSION_NONE,0,false);
		tryGray64FloatImage(BaseImage3x3,FileInfo.COMPRESSION_NONE,0,false);
		tryGray64FloatImage(BaseImage1x9,FileInfo.COMPRESSION_NONE,0,false);
		tryGray64FloatImage(BaseImage7x2,FileInfo.COMPRESSION_NONE,0,false);
		tryGray64FloatImage(BaseImage5x4,FileInfo.COMPRESSION_NONE,0,false);
		tryGray64FloatImage(BaseImage4x6,FileInfo.COMPRESSION_NONE,0,false);
		tryGray64FloatImage(BaseTestImage,FileInfo.COMPRESSION_NONE,0,false);
		
		tryGray64FloatImage(BaseTestImage,FileInfo.COMPRESSION_NONE,382,false);  // test header specifically
		tryGray64FloatImage(BaseTestImage,FileInfo.COMPRESSION_NONE,0,true);     // test intel specifically
		// NO LZW
		// NO LZW_DIFFERENCING
		// NO PACK_BITS
	}
	
	private byte[] rgbPixelBytes(long[][] inputData, FileInfo fi, int compression, int headerBytes, boolean intelByteOrder)
	{
		// set FileInfo fields for subsequent calls
		
		fi.fileType = FileInfo.RGB;
		fi.compression = compression;
		fi.intelByteOrder = intelByteOrder;
		fi.height = inputData.length;
		fi.width = inputData[0].length;
		
		byte[] output = new byte[fi.height * fi.width * 3];
		
		int i = 0;
		for (long[] row : inputData)
			for (long pix : row)
			{
				output[3*i+0] = (byte)((pix & 0xff0000) >> 16);
				output[3*i+1] = (byte)((pix & 0x00ff00) >> 8);
				output[3*i+2] = (byte)((pix & 0x0000ff) >> 0);
				i++;
			}
		
		if (intelByteOrder)
			; // nothing to do
		
		if (compression == FileInfo.LZW)
		{
			output = lzwEncoder.encode(output);
			fi.stripLengths = new int[] {output.length};
			fi.stripOffsets = new int[] {0};
		}
		else if (compression == FileInfo.LZW_WITH_DIFFERENCING)
		{
			output = lzwDiffEncoder.encode(output);
			fi.stripLengths = new int[] {output.length};
			fi.stripOffsets = new int[] {0};
		}
		else if (compression == FileInfo.PACK_BITS)
		{
			output = packbitsEncoder.encode(output);
			fi.stripLengths = new int[] {output.length};
			fi.stripOffsets = new int[] {0};
			fi.rowsPerStrip = fi.height;
		}

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

	private int[] rgbExpectedOutput(long[][] image, boolean intelByteOrder)
	{
		int[] output = new int[image.length * image[0].length];
		
		// NOTICE that input is rgb but output is argb
		
		int i = 0;
		for (long[] row : image)
			for (long pix : row)
				output[i++] = (int)(0xff000000 | (pix & 0xffffff));

		return output;
	}

	private void tryRgbImage(long[][] baseImage, int compression, int headerBytes, boolean intelByteOrder)
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
	private void runRgbFileTypeTests()
	{
		tryRgbImage(BaseImage1x1,FileInfo.COMPRESSION_NONE,0,false);
		tryRgbImage(BaseImage3x3,FileInfo.COMPRESSION_NONE,0,false);
		tryRgbImage(BaseImage1x9,FileInfo.COMPRESSION_NONE,0,false);
		tryRgbImage(BaseImage7x2,FileInfo.COMPRESSION_NONE,0,false);
		tryRgbImage(BaseImage5x4,FileInfo.COMPRESSION_NONE,0,false);
		tryRgbImage(BaseImage4x6,FileInfo.COMPRESSION_NONE,0,false);
		tryRgbImage(BaseTestImage,FileInfo.COMPRESSION_NONE,0,false);

		tryRgbImage(BaseTestImage,FileInfo.COMPRESSION_NONE,873,false);  // test header specifically
		tryRgbImage(BaseTestImage,FileInfo.COMPRESSION_NONE,0,true);     // test intel specifically
		tryRgbImage(BaseTestImage,FileInfo.LZW,0,false);    // test LZW compression specifically
		tryRgbImage(BaseTestImage,FileInfo.PACK_BITS,0,false);    // test PACK_BITS compression specifically
	}
	
	private byte[] bgrPixelBytes(long[][] inputData, FileInfo fi, int compression, int headerBytes, boolean intelByteOrder)
	{
		// set FileInfo fields for subsequent calls
		
		fi.fileType = FileInfo.BGR;
		fi.compression = compression;
		fi.intelByteOrder = intelByteOrder;
		fi.height = inputData.length;
		fi.width = inputData[0].length;
		
		byte[] output = new byte[fi.height * fi.width * 3];
		
		int i = 0;
		for (long[] row : inputData)
			for (long pix : row)
			{
				output[3*i+0] = (byte)((pix & 0x0000ff) >> 0);
				output[3*i+1] = (byte)((pix & 0x00ff00) >> 8);
				output[3*i+2] = (byte)((pix & 0xff0000) >> 16);
				i++;
			}
		
		if (intelByteOrder)
			; // nothing to do
		
		if (compression == FileInfo.LZW)
		{
			output = lzwEncoder.encode(output);
			fi.stripLengths = new int[] {output.length};
			fi.stripOffsets = new int[] {0};
		}
		else if (compression == FileInfo.LZW_WITH_DIFFERENCING)
		{
			output = lzwDiffEncoder.encode(output);
			fi.stripLengths = new int[] {output.length};
			fi.stripOffsets = new int[] {0};
		}
		else if (compression == FileInfo.PACK_BITS)
		{
			output = packbitsEncoder.encode(output);
			fi.stripLengths = new int[] {output.length};
			fi.stripOffsets = new int[] {0};
			fi.rowsPerStrip = fi.height;
		}

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

	private int[] bgrExpectedOutput(long[][] image, boolean intelByteOrder)
	{
		int[] output = new int[image.length * image[0].length];
		
		// NOTICE that input is bgr but output is argb
		
		int i = 0;
		for (long[] row : image)
			for (long pix : row)
				output[i++] = (int)(0xff000000 | (pix & 0xffffff));

		return output;
	}

	private void tryBgrImage(long[][] baseImage, int compression, int headerBytes, boolean intelByteOrder)
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
	private void runBgrFileTypeTests()
	{
		tryBgrImage(BaseImage1x1,FileInfo.COMPRESSION_NONE,0,false);
		tryBgrImage(BaseImage3x3,FileInfo.COMPRESSION_NONE,0,false);
		tryBgrImage(BaseImage1x9,FileInfo.COMPRESSION_NONE,0,false);
		tryBgrImage(BaseImage7x2,FileInfo.COMPRESSION_NONE,0,false);
		tryBgrImage(BaseImage5x4,FileInfo.COMPRESSION_NONE,0,false);
		tryBgrImage(BaseImage4x6,FileInfo.COMPRESSION_NONE,0,false);
		tryBgrImage(BaseTestImage,FileInfo.COMPRESSION_NONE,0,false);
		
		tryBgrImage(BaseTestImage,FileInfo.COMPRESSION_NONE,873,false);  // test header specifically
		tryBgrImage(BaseTestImage,FileInfo.COMPRESSION_NONE,0,true);     // test intel specifically
		tryBgrImage(BaseTestImage,FileInfo.LZW,0,false);    // test LZW compression specifically
		tryBgrImage(BaseTestImage,FileInfo.PACK_BITS,0,false);    // test PACK_BITS compression specifically
	}
	
	private byte[] argbPixelBytes(long[][] inputData, FileInfo fi, int compression, int headerBytes, boolean intelByteOrder)
	{
		// set FileInfo fields for subsequent calls
		
		fi.fileType = FileInfo.ARGB;
		fi.compression = compression;
		fi.intelByteOrder = intelByteOrder;
		fi.height = inputData.length;
		fi.width = inputData[0].length;
		
		byte[] output = new byte[fi.height * fi.width * 4];
		
		int i = 0;
		for (long[] row : inputData)
			for (long pix : row)
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
		{
			output = lzwEncoder.encode(output);
			fi.stripLengths = new int[] {output.length};
			fi.stripOffsets = new int[] {0};
		}
		else if (compression == FileInfo.LZW_WITH_DIFFERENCING)
		{
			output = lzwDiffEncoder.encode(output);
			fi.stripLengths = new int[] {output.length};
			fi.stripOffsets = new int[] {0};
		}
		else if (compression == FileInfo.PACK_BITS)
		{
			output = packbitsEncoder.encode(output);
			fi.stripLengths = new int[] {output.length};
			fi.stripOffsets = new int[] {0};
			fi.rowsPerStrip = fi.height;
		}

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

	private int[] argbExpectedOutput(long[][] image, boolean intelByteOrder)
	{
		int[] output = new int[image.length * image[0].length];
		
		int i = 0;
		for (long[] row : image)
			for (long pix : row)
				output[i++] = (int)(0xff000000 | (pix & 0xffffff));

		return output;
	}

	private void tryArgbImage(long[][] baseImage, int compression, int headerBytes, boolean intelByteOrder)
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
	private void runArgbFileTypeTests()
	{
		tryArgbImage(BaseImage1x1,FileInfo.COMPRESSION_NONE,0,false);
		tryArgbImage(BaseImage3x3,FileInfo.COMPRESSION_NONE,0,false);
		tryArgbImage(BaseImage1x9,FileInfo.COMPRESSION_NONE,0,false);
		tryArgbImage(BaseImage7x2,FileInfo.COMPRESSION_NONE,0,false);
		tryArgbImage(BaseImage5x4,FileInfo.COMPRESSION_NONE,0,false);
		tryArgbImage(BaseImage4x6,FileInfo.COMPRESSION_NONE,0,false);
		tryArgbImage(BaseTestImage,FileInfo.COMPRESSION_NONE,0,false);
		
		tryArgbImage(BaseTestImage,FileInfo.COMPRESSION_NONE,501,false);  // test header specifically
		tryArgbImage(BaseTestImage,FileInfo.COMPRESSION_NONE,0,true);     // test intel specifically
		// TODO: these fail due to readPixels() bug
		tryArgbImage(BaseTestImage,FileInfo.LZW,0,false);    // test LZW compression specifically
		tryArgbImage(BaseTestImage,FileInfo.PACK_BITS,0,false);    // test PACK_BITS compression specifically
	}
	
	private byte[] abgrPixelBytes(long[][] inputData, FileInfo fi, int compression, int headerBytes, boolean intelByteOrder)
	{
		// set FileInfo fields for subsequent calls
		
		fi.fileType = FileInfo.ABGR;
		fi.compression = compression;
		fi.intelByteOrder = intelByteOrder;
		fi.height = inputData.length;
		fi.width = inputData[0].length;
		
		byte[] output = new byte[fi.height * fi.width * 4];
		
		int i = 0;
		for (long[] row : inputData)
			for (long pix : row)
			{
				// NOTICE that ABGR stored as bgra
				
				output[4*i+0] = (byte)((pix & 0x000000ff) >> 0);
				output[4*i+1] = (byte)((pix & 0x0000ff00) >> 8);
				output[4*i+2] = (byte)((pix & 0x00ff0000) >> 16);
				output[4*i+3] = (byte)((pix & 0xff000000) >> 24);
				i++;
			}
		
		if (intelByteOrder)
			; // nothing to do
		
		if (compression == FileInfo.LZW)
		{
			output = lzwEncoder.encode(output);
			fi.stripLengths = new int[] {output.length};
			fi.stripOffsets = new int[] {0};
		}
		else if (compression == FileInfo.LZW_WITH_DIFFERENCING)
		{
			output = lzwDiffEncoder.encode(output);
			fi.stripLengths = new int[] {output.length};
			fi.stripOffsets = new int[] {0};
		}
		else if (compression == FileInfo.PACK_BITS)
		{
			output = packbitsEncoder.encode(output);
			fi.stripLengths = new int[] {output.length};
			fi.stripOffsets = new int[] {0};
			fi.rowsPerStrip = fi.height;
		}

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

	private int[] abgrExpectedOutput(long[][] image, boolean intelByteOrder)
	{
		// NOTICE that input is abgr but output is argb

		int[] output = new int[image.length * image[0].length];
		
		int i = 0;
		for (long[] row : image)
			for (long pix : row)
				output[i++] = (int)(0xff000000 | (pix & 0xffffff));

		return output;
	}

	private void tryAbgrImage(long[][] baseImage, int compression, int headerBytes, boolean intelByteOrder)
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
	private void runAbgrFileTypeTests()
	{
		tryAbgrImage(BaseImage1x1,FileInfo.COMPRESSION_NONE,0,false);
		tryAbgrImage(BaseImage3x3,FileInfo.COMPRESSION_NONE,0,false);
		tryAbgrImage(BaseImage1x9,FileInfo.COMPRESSION_NONE,0,false);
		tryAbgrImage(BaseImage7x2,FileInfo.COMPRESSION_NONE,0,false);
		tryAbgrImage(BaseImage5x4,FileInfo.COMPRESSION_NONE,0,false);
		tryAbgrImage(BaseImage4x6,FileInfo.COMPRESSION_NONE,0,false);
		tryAbgrImage(BaseTestImage,FileInfo.COMPRESSION_NONE,0,false);
		
		tryAbgrImage(BaseTestImage,FileInfo.COMPRESSION_NONE,222,false);  // test header specifically
		tryAbgrImage(BaseTestImage,FileInfo.COMPRESSION_NONE,0,true);     // test intel specifically
		// TODO: these fail due to readPixels() bug
		tryAbgrImage(BaseTestImage,FileInfo.LZW,0,false);    // test LZW compression specifically
		tryAbgrImage(BaseTestImage,FileInfo.PACK_BITS,0,false);    // test PACK_BITS compression specifically
	}
	
	private byte[] bargPixelBytes(long[][] inputData, FileInfo fi, int compression, int headerBytes, boolean intelByteOrder)
	{
		// set FileInfo fields for subsequent calls
		
		fi.fileType = FileInfo.BARG;
		fi.compression = compression;
		fi.intelByteOrder = intelByteOrder;
		fi.height = inputData.length;
		fi.width = inputData[0].length;
		
		byte[] output = new byte[fi.height * fi.width * 4];
		
		int i = 0;
		for (long[] row : inputData)
			for (long pix : row)
			{				
				output[4*i+0] = (byte)((pix & 0x000000ff) >> 0);
				output[4*i+1] = (byte)((pix & 0xff000000) >> 24);
				output[4*i+2] = (byte)((pix & 0x00ff0000) >> 16);
				output[4*i+3] = (byte)((pix & 0x0000ff00) >> 8);
				i++;
			}
		
		if (intelByteOrder)
			; // nothing to do
		
		if (compression == FileInfo.LZW)
		{
			output = lzwEncoder.encode(output);
			fi.stripLengths = new int[] {output.length};
			fi.stripOffsets = new int[] {0};
		}
		else if (compression == FileInfo.LZW_WITH_DIFFERENCING)
		{
			output = lzwDiffEncoder.encode(output);
			fi.stripLengths = new int[] {output.length};
			fi.stripOffsets = new int[] {0};
		}
		else if (compression == FileInfo.PACK_BITS)
		{
			output = packbitsEncoder.encode(output);
			fi.stripLengths = new int[] {output.length};
			fi.stripOffsets = new int[] {0};
			fi.rowsPerStrip = fi.height;
		}

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

	private int[] bargExpectedOutput(long[][] image, boolean intelByteOrder)
	{
		int[] output = new int[image.length * image[0].length];
	
		// NOTICE input is BARG but output is argb
		int i = 0;
		for (long[] row : image)
			for (long pix : row)
				output[i++] = (int)(0xff000000 | (pix & 0xffffff));

		return output;
	}

	private void tryBargImage(long[][] baseImage, int compression, int headerBytes, boolean intelByteOrder)
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
	private void runBargFileTypeTests()
	{
		tryBargImage(BaseImage1x1,FileInfo.COMPRESSION_NONE,0,false);
		tryBargImage(BaseImage3x3,FileInfo.COMPRESSION_NONE,0,false);
		tryBargImage(BaseImage1x9,FileInfo.COMPRESSION_NONE,0,false);
		tryBargImage(BaseImage7x2,FileInfo.COMPRESSION_NONE,0,false);
		tryBargImage(BaseImage5x4,FileInfo.COMPRESSION_NONE,0,false);
		tryBargImage(BaseImage4x6,FileInfo.COMPRESSION_NONE,0,false);
		tryBargImage(BaseTestImage,FileInfo.COMPRESSION_NONE,0,false);
		
		tryBargImage(BaseTestImage,FileInfo.COMPRESSION_NONE,777,false);  // test header specifically
		tryBargImage(BaseTestImage,FileInfo.COMPRESSION_NONE,0,true);     // test intel specifically
		// TODO: these fail due to readPixels() bug
		tryBargImage(BaseTestImage,FileInfo.LZW,0,false);     // test LZW specifically
		tryBargImage(BaseTestImage,FileInfo.PACK_BITS,0,false);     // test PACK_BITS specifically
	}
	
	private byte[] rgbPlanarPixelBytes(long[][] inputData, FileInfo fi, int compression, int headerBytes, boolean intelByteOrder)
	{
		// set FileInfo fields for subsequent calls
		
		fi.fileType = FileInfo.RGB_PLANAR;
		fi.compression = compression;
		fi.intelByteOrder = intelByteOrder;
		fi.height = inputData.length;
		fi.width = inputData[0].length;
		
		int planeSize = fi.height * fi.width;
		
		byte[] output = new byte[planeSize * 3];
		
		int i = 0;
		for (long[] row : inputData)
			for (long pix : row)
			{				
				output[(0*planeSize)+i] = (byte)((pix & 0x00ff0000) >> 16);
				output[(1*planeSize)+i] = (byte)((pix & 0x0000ff00) >> 8);
				output[(2*planeSize)+i] = (byte)((pix & 0x000000ff) >> 0);
				i++;
			}
		
		if (intelByteOrder)
			; // nothing to do
		
		if (compression == FileInfo.LZW)
		{
			output = lzwEncoder.encode(output);
			fi.stripLengths = new int[] {output.length};
			fi.stripOffsets = new int[] {0};
		}
		else if (compression == FileInfo.LZW_WITH_DIFFERENCING)
		{
			output = lzwDiffEncoder.encode(output);
			fi.stripLengths = new int[] {output.length};
			fi.stripOffsets = new int[] {0};
		}
		else if (compression == FileInfo.PACK_BITS)
		{
			output = packbitsEncoder.encode(output);
			fi.stripLengths = new int[] {output.length};
			fi.stripOffsets = new int[] {0};
			fi.rowsPerStrip = fi.height;
		}

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

	private int[] rgbPlanarExpectedOutput(long[][] image, boolean intelByteOrder)
	{
		int[] output = new int[image.length * image[0].length];
	
		// NOTICE input is rgb planar but output is argb
		int i = 0;
		for (long[] row : image)
			for (long pix : row)
				output[i++] = (int)(0xff000000 | (pix & 0xffffff));

		return output;
	}

	private void tryRgbPlanarImage(long[][] baseImage, int compression, int headerBytes, boolean intelByteOrder)
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
	private void runRgbPlanarFileTypeTests()
	{
		tryRgbPlanarImage(BaseImage1x1,FileInfo.COMPRESSION_NONE,0,false);
		tryRgbPlanarImage(BaseImage3x3,FileInfo.COMPRESSION_NONE,0,false);
		tryRgbPlanarImage(BaseImage1x9,FileInfo.COMPRESSION_NONE,0,false);
		tryRgbPlanarImage(BaseImage7x2,FileInfo.COMPRESSION_NONE,0,false);
		tryRgbPlanarImage(BaseImage5x4,FileInfo.COMPRESSION_NONE,0,false);
		tryRgbPlanarImage(BaseImage4x6,FileInfo.COMPRESSION_NONE,0,false);
		tryRgbPlanarImage(BaseTestImage,FileInfo.COMPRESSION_NONE,0,false);
		
		tryRgbPlanarImage(BaseTestImage,FileInfo.COMPRESSION_NONE,923,false);  // test header specifically
		tryRgbPlanarImage(BaseTestImage,FileInfo.COMPRESSION_NONE,0,true);     // test intel specifically
		tryRgbPlanarImage(BaseTestImage,FileInfo.LZW,0,false);     // test LZW specifically
		tryRgbPlanarImage(BaseTestImage,FileInfo.PACK_BITS,0,false);     // test PACK_BITS specifically
	}
	
	private byte[] bitmapPixelBytes(long[][] inputData, FileInfo fi, int compression, int headerBytes, boolean intelByteOrder)
	{
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
		
		int rows = fi.height;
		int cols = fi.width;
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
			; // nothing to do
		
		if (compression == FileInfo.LZW)
			;
		else if (compression == FileInfo.LZW_WITH_DIFFERENCING)
			;
		else if (compression == FileInfo.PACK_BITS)
			;

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

	private byte[] bitmapExpectedOutput(long[][] image, boolean intelByteOrder)
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

	private void tryBitmapImage(long[][] baseImage, int compression, int headerBytes, boolean intelByteOrder)
	{
		FileInfo fi = new FileInfo();
		byte[] pixInData = bitmapPixelBytes(baseImage,fi,compression,headerBytes,intelByteOrder);
		Object pixels = callReadPixels(fi,pixInData);
		assertNotNull(pixels);
		assertTrue(pixels instanceof byte[]);
		assertArrayEquals(bitmapExpectedOutput(baseImage,intelByteOrder),(byte[])pixels);
	}

	// FileInfo.BITMAP:
	private void runBitmapFileTypeTests()
	{
		tryBitmapImage(BaseImage1x1,FileInfo.COMPRESSION_NONE,0,false);
		tryBitmapImage(BaseImage3x3,FileInfo.COMPRESSION_NONE,0,false);
		tryBitmapImage(BaseImage1x9,FileInfo.COMPRESSION_NONE,0,false);
		tryBitmapImage(BaseImage7x2,FileInfo.COMPRESSION_NONE,0,false);
		tryBitmapImage(BaseImage5x4,FileInfo.COMPRESSION_NONE,0,false);
		tryBitmapImage(BaseImage4x6,FileInfo.COMPRESSION_NONE,0,false);
		tryBitmapImage(BaseTestImage,FileInfo.COMPRESSION_NONE,0,false);
		
		tryBitmapImage(BaseTestImage,FileInfo.COMPRESSION_NONE,386,false);  // test header specifically
		tryBitmapImage(BaseTestImage,FileInfo.COMPRESSION_NONE,0,true);     // test intel specifically
		// NO LZW
		// NO LZW_DIFFERENCING
		// NO PACK_BITS
	}

	private byte[] rgb48PixelBytes(long[][] inputData, FileInfo fi, int compression, int headerBytes, boolean intelByteOrder)
	{
		// set FileInfo fields for subsequent calls
		
		fi.fileType = FileInfo.RGB48;
		fi.compression = compression;
		fi.intelByteOrder = intelByteOrder;
		fi.height = inputData.length;
		fi.width = inputData[0].length;
		
		// Set strip info: Needed for this case: for now lets do one strip per row
		fi.stripLengths = new int[inputData.length];
		fi.stripOffsets = new int[inputData.length];
		for (int i = 0; i < inputData.length; i++)
		{
			fi.stripLengths[i] = 6*inputData[0].length;
			// was this and seemed to work fi.stripOffsets[i] = 6*i;
			fi.stripOffsets[i] = (i == 0 ? 0 : (fi.stripOffsets[i-1] + fi.stripLengths[i]));
		}
		
		byte[] output = new byte[fi.height * fi.width * 6];

		// note that I am only using the lowest 8 bits of the int for testing purposes
		
		int i = 0;
		for (long[] row : inputData)
			for (long wholeLong : row)
			{
				output[i++] = (byte)((wholeLong & 0xff0000000000L) >> 40);
				output[i++] = (byte)((wholeLong & 0xff00000000L) >> 32);
				output[i++] = (byte)((wholeLong & 0xff000000L) >> 24);
				output[i++] = (byte)((wholeLong & 0xff0000L) >> 16);
				output[i++] = (byte)((wholeLong & 0xff00L) >> 8);
				output[i++] = (byte)((wholeLong & 0xffL) >> 0);
			}
		
		if (intelByteOrder)
			output = intelSwap(output,2);
		
		if (compression == FileInfo.LZW)
		{
			output = lzwEncoder.encode(output);
			fi.stripLengths = new int[] {output.length};
			fi.stripOffsets = new int[] {0};
		}
		else if (compression == FileInfo.LZW_WITH_DIFFERENCING)
		{
			output = lzwDiffEncoder.encode(output);
			fi.stripLengths = new int[] {output.length};
			fi.stripOffsets = new int[] {0};
		}
		else if (compression == FileInfo.PACK_BITS)
		{
			output = packbitsEncoder.encode(output);
			fi.stripLengths = new int[] {output.length};
			fi.stripOffsets = new int[] {0};
			fi.rowsPerStrip = fi.height;
		}

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

	private short[][] rgb48ExpectedOutput(long[][] image, boolean intelByteOrder)
	{
		int rows = image.length;
		int cols = image[0].length;

		short[][] output = new short[3][];
		
		for (int i = 0; i < 3; i++)
			output[i] = new short[rows*cols];
		
		int i = 0;
		for (int r = 0; r < rows; r++)
			for (int c = 0; c < cols; c++)
			{
				output[0][i] = (short) ((image[r][c] & 0xffff00000000L) >> 32);
				output[1][i] = (short) ((image[r][c] & 0x0000ffff0000L) >> 16);
				output[2][i] = (short) ((image[r][c] & 0x00000000ffffL) >> 0);
				i++;
			}
		
		return output;
	}

	private void tryRgb48Image(long[][] baseImage, int compression, int headerBytes, boolean intelByteOrder)
	{
		FileInfo fi = new FileInfo();
		byte[] pixInData = rgb48PixelBytes(baseImage,fi,compression,headerBytes,intelByteOrder);
		
		Object pixels = callReadPixels(fi,pixInData);
		assertNotNull(pixels);
		assertTrue(pixels instanceof short[][]);
		short[][] expected = rgb48ExpectedOutput(baseImage,intelByteOrder);
		assertArrayEquals(expected,(short[][])pixels);
	}

	// FileInfo.RGB48:
	//   sub cases
	//   compression
	//   intelByteOrder
	private void runRgb48FileTypeTests()
	{
//		final long[][] test48Bit = {{0xff0000000000L},{0x00ff00000000L},{0x0000ff000000L},{0x000000ff0000L},{0x00000000ff00L},{0x0000000000ffL}};
//		tryRgb48Image(test48Bit,FileInfo.COMPRESSION_NONE,0,false);

		tryRgb48Image(BaseImage1x1,FileInfo.COMPRESSION_NONE,0,false);
		tryRgb48Image(BaseImage3x3,FileInfo.COMPRESSION_NONE,0,false);
		tryRgb48Image(BaseImage1x9,FileInfo.COMPRESSION_NONE,0,false);
		tryRgb48Image(BaseImage7x2,FileInfo.COMPRESSION_NONE,0,false);
		tryRgb48Image(BaseImage5x4,FileInfo.COMPRESSION_NONE,0,false);
		tryRgb48Image(BaseImage4x6,FileInfo.COMPRESSION_NONE,0,false);
		tryRgb48Image(BaseTestImage,FileInfo.COMPRESSION_NONE,0,false);
		
		tryRgb48Image(BaseTestImage,FileInfo.COMPRESSION_NONE,559,false);  // test header specifically
		tryRgb48Image(BaseTestImage,FileInfo.COMPRESSION_NONE,0,true);     // test intel specifically
		tryRgb48Image(BaseTestImage,FileInfo.LZW,0,false);     // test LZW specifically
		tryRgb48Image(BaseTestImage,FileInfo.PACK_BITS,0,false);     // test PACK_BITS specifically
		// NO LZW_DIFFERENCING
	}
	

	private byte[] rgb48PlanarPixelBytes(long[][] inputData, FileInfo fi, int compression, int headerBytes, boolean intelByteOrder)
	{
		// set FileInfo fields for subsequent calls
		
		fi.fileType = FileInfo.RGB48_PLANAR;
		fi.compression = compression;
		fi.intelByteOrder = intelByteOrder;
		fi.height = inputData.length;
		fi.width = inputData[0].length;
		
		// Set strip info:
		//   let's do one big strip of data : any more strips and decompression code runs in ImageReader (TODO that could be a bug there)
		fi.stripLengths = new int[] {fi.height * fi.width * 6};
		fi.stripOffsets = new int[] {0};
		
		// note that I am only using the lowest 8 bits of the input int pixel for testing purposes

		int rows = fi.height;
		int cols = fi.width;
		int totPix = rows * cols;

		byte[] plane1 = new byte[totPix*2];
		byte[] plane2 = new byte[totPix*2];
		byte[] plane3 = new byte[totPix*2];
		
		// populate planes from int data
		int i = 0;
		for (long[] row : inputData)
			for (long wholeLong : row)
			{
				long channel1 = ((wholeLong & 0x00000000ffffL) >> 0);
				long channel2 = ((wholeLong & 0x0000ffff0000L) >> 16);
				long channel3 = ((wholeLong & 0xffff00000000L) >> 32);
				
				// divide the int into three channels
				plane1[2*i+0] = (byte) ((channel1 & 0xff00) >> 8);
				plane1[2*i+1] = (byte) ((channel1 & 0x00ff) >> 0);
				plane2[2*i+0] = (byte) ((channel2 & 0xff00) >> 8);
				plane2[2*i+1] = (byte) ((channel2 & 0x00ff) >> 0);
				plane3[2*i+0] = (byte) ((channel3 & 0xff00) >> 8);
				plane3[2*i+1] = (byte) ((channel3 & 0x00ff) >> 0);
				i++;
			}
		
		if (intelByteOrder)
		{
			plane1 = intelSwap(plane1,2);
			plane2 = intelSwap(plane2,2);
			plane3 = intelSwap(plane3,2);
		}
		
		int biggestPlane = Math.max(Math.max(plane1.length, plane2.length), plane2.length);

		byte[] output = new byte[biggestPlane*3];

		if (compression == FileInfo.LZW)
		{
			plane1 = lzwEncoder.encode(plane1); // compress the output data
			plane2 = lzwEncoder.encode(plane2); // compress the output data
			plane3 = lzwEncoder.encode(plane3); // compress the output data
		
			biggestPlane = Math.max(Math.max(plane1.length, plane2.length), plane2.length);
			
			// does not work
			//output = new byte[plane1.length + plane2.length + plane3.length];
			//fi.stripLengths = new int[] {output.length};
			//fi.stripOffsets = new int[] {0};
			
			// apparently the 48 bit planar type stores three sixteen bit images.
			// when read the stripOffsets are reused so each plane must be stored in the same size strip.
			// so must allocate the overall pixel array to be big enough to contain the biggest plane three times and
			// setup the strip offsets to be the same for each plane.
			
			fi.stripLengths = new int[] {biggestPlane};
			fi.stripOffsets = new int[] {0};
		}
		else if (compression == FileInfo.LZW_WITH_DIFFERENCING)
		{
			plane1 = lzwDiffEncoder.encode(plane1); // compress the output data
			plane2 = lzwDiffEncoder.encode(plane2); // compress the output data
			plane3 = lzwDiffEncoder.encode(plane3); // compress the output data
		
			biggestPlane = Math.max(Math.max(plane1.length, plane2.length), plane2.length);
			
			// does not work
			//output = new byte[plane1.length + plane2.length + plane3.length];
			//fi.stripLengths = new int[] {output.length};
			//fi.stripOffsets = new int[] {0};
			
			// apparently the 48 bit planar type stores three sixteen bit images.
			// when read the stripOffsets are reused so each plane must be stored in the same size strip.
			// so must allocate the overall pixel array to be big enough to contain the biggest plane three times and
			// setup the strip offsets to be the same for each plane.
			
			fi.stripLengths = new int[] {biggestPlane};
			fi.stripOffsets = new int[] {0};
		}
		else if (compression == FileInfo.PACK_BITS)
			;

		// else assuming its COMPRESSION_NONE : do nothing		

		// finally combine planes : note that the written planes are <= biggestPlane in length
		System.arraycopy(plane1, 0, output, 0, plane1.length);
		System.arraycopy(plane2, 0, output, biggestPlane, plane2.length);
		System.arraycopy(plane3, 0, output, 2*biggestPlane, plane3.length);
		
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

	private short[][] rgb48PlanarExpectedOutput(long[][] image, boolean intelByteOrder)
	{
		int rows = image.length;
		int cols = image[0].length;

		short[][] output = new short[3][];
		
		for (int i = 0; i < 3; i++)
			output[i] = new short[rows*cols];
		
		int i = 0;
		for (int r = 0; r < rows; r++)
			for (int c = 0; c < cols; c++)
			{
				output[0][i] = (short) ((image[r][c] & 0x00000000ffffL) >> 0);
				output[1][i] = (short) ((image[r][c] & 0x0000ffff0000L) >> 16);
				output[2][i] = (short) ((image[r][c] & 0xffff00000000L) >> 32);
				i++;
			}
		
		return output;
	}

	private void tryRgb48PlanarImage(long[][] baseImage, int compression, int headerBytes, boolean intelByteOrder)
	{
		FileInfo fi = new FileInfo();
		byte[] pixInData = rgb48PlanarPixelBytes(baseImage,fi,compression,headerBytes,intelByteOrder);
		
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
	private void runRgb48PlanarFileTypeTests()
	{
		tryRgb48PlanarImage(BaseImage1x1,FileInfo.COMPRESSION_NONE,0,false);
		tryRgb48PlanarImage(BaseImage3x3,FileInfo.COMPRESSION_NONE,0,false);
		tryRgb48PlanarImage(BaseImage1x9,FileInfo.COMPRESSION_NONE,0,false);
		tryRgb48PlanarImage(BaseImage7x2,FileInfo.COMPRESSION_NONE,0,false);
		tryRgb48PlanarImage(BaseImage5x4,FileInfo.COMPRESSION_NONE,0,false);
		tryRgb48PlanarImage(BaseImage4x6,FileInfo.COMPRESSION_NONE,0,false);
		tryRgb48PlanarImage(BaseTestImage,FileInfo.COMPRESSION_NONE,0,false);
		
		tryRgb48PlanarImage(BaseTestImage,FileInfo.COMPRESSION_NONE,559,false);  // test header specifically
		tryRgb48PlanarImage(BaseTestImage,FileInfo.COMPRESSION_NONE,0,true);     // test intel specifically
		tryRgb48PlanarImage(BaseTestImage,FileInfo.LZW,0,false);     // test LZW specifically
		// NO PACK_BITS
	}
	
	// helper: input ints assumed to be in 12-bit range
	private byte[] encode12bit(long[][] inPix)
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
	

	private byte[] gray12UnsignedPixelBytes(long[][] inputData, FileInfo fi, int compression, int headerBytes, boolean intelByteOrder)
	{		
		// set FileInfo fields for subsequent calls
		
		fi.fileType = FileInfo.GRAY12_UNSIGNED;
		fi.compression = compression;
		fi.intelByteOrder = intelByteOrder;
		fi.height = inputData.length;
		fi.width = inputData[0].length;
		
		byte[] output = encode12bit(inputData);
		
		if (intelByteOrder)
			; // nothing to do
		
		if (compression == FileInfo.LZW)
			;
		else if (compression == FileInfo.LZW_WITH_DIFFERENCING)
			;
		else if (compression == FileInfo.PACK_BITS)
			;

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

	private short[] gray12UnsignedExpectedOutput(long[][] image, boolean intelByteOrder)
	{
		short[] output = new short[image.length * image[0].length];
			
		int i = 0;
		for (long[] row : image)
			for (long pix: row)
				output[i++] = (short)(pix & 0xfff);
		
		return output;
	}

	private void tryGray12UnsignedImage(long[][] baseImage, int compression, int headerBytes, boolean intelByteOrder)
	{
		FileInfo fi = new FileInfo();
		byte[] pixInData = gray12UnsignedPixelBytes(baseImage,fi,compression,headerBytes,intelByteOrder);
		Object pixels = callReadPixels(fi,pixInData);
		assertNotNull(pixels);
		assertTrue(pixels instanceof short[]);
		assertArrayEquals(gray12UnsignedExpectedOutput(baseImage,intelByteOrder),(short[])pixels);
	}

	// FileInfo.GRAY12_UNSIGNED:
	private void runGray12UnsignedFileTypeTests()
	{
		tryGray12UnsignedImage(BaseImage1x1,FileInfo.COMPRESSION_NONE,0,false);
		tryGray12UnsignedImage(BaseImage3x3,FileInfo.COMPRESSION_NONE,0,false);
		tryGray12UnsignedImage(BaseImage1x9,FileInfo.COMPRESSION_NONE,0,false);
		tryGray12UnsignedImage(BaseImage7x2,FileInfo.COMPRESSION_NONE,0,false);
		tryGray12UnsignedImage(BaseImage5x4,FileInfo.COMPRESSION_NONE,0,false);
		tryGray12UnsignedImage(BaseImage4x6,FileInfo.COMPRESSION_NONE,0,false);
		tryGray12UnsignedImage(BaseTestImage,FileInfo.COMPRESSION_NONE,0,false);
		
		tryGray12UnsignedImage(BaseTestImage,FileInfo.COMPRESSION_NONE,885,false);  // test header specifically
		tryGray12UnsignedImage(BaseTestImage,FileInfo.COMPRESSION_NONE,0,true);     // test intel specifically
		// NO LZW
		// NO LZW_DIFFERENCING
		// NO PACK_BITS
	}

	private byte[] gray24UnsignedPixelBytes(long[][] inputData, FileInfo fi, int compression, int headerBytes, boolean intelByteOrder)
	{
		// set FileInfo fields for subsequent calls
		
		fi.fileType = FileInfo.GRAY24_UNSIGNED;
		fi.compression = compression;
		fi.intelByteOrder = intelByteOrder;
		fi.height = inputData.length;
		fi.width = inputData[0].length;
		
		byte[] output = new byte[fi.height * fi.width * 3];
		
		int i = 0;
		for (long[] row : inputData)
			for (long pix : row)
			{
				output[i++] = (byte) ((pix & 0x0000ff) >> 0);
				output[i++] = (byte) ((pix & 0x00ff00) >> 8);
				output[i++] = (byte) ((pix & 0xff0000) >> 16);
			}
		
		if (intelByteOrder)
			; // nothing to do
		
		if (compression == FileInfo.LZW)
			;
		else if (compression == FileInfo.LZW_WITH_DIFFERENCING)
			;
		else if (compression == FileInfo.PACK_BITS)
			;

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

	private float[] gray24UnsignedExpectedOutput(long[][] image, boolean intelByteOrder)
	{
		float[] output = new float[image.length * image[0].length];
			
		int i = 0;
		for (long[] row : image)
			for (long pix: row)
				output[i++] = (float)(pix & 0xffffff);
		
		return output;
	}

	private void tryGray24UnsignedImage(long[][] baseImage, int compression, int headerBytes, boolean intelByteOrder)
	{
		FileInfo fi = new FileInfo();
		byte[] pixInData = gray24UnsignedPixelBytes(baseImage,fi,compression,headerBytes,intelByteOrder);
		Object pixels = callReadPixels(fi,pixInData);
		assertNotNull(pixels);
		assertTrue(pixels instanceof float[]);
		lociAssertArrayEquals(gray24UnsignedExpectedOutput(baseImage,intelByteOrder),(float[])pixels);
	}

	// FileInfo.GRAY24_UNSIGNED:
	private void runGray24UnsignedFileTypeTests()
	{
		tryGray24UnsignedImage(BaseImage1x1,FileInfo.COMPRESSION_NONE,0,false);
		tryGray24UnsignedImage(BaseImage3x3,FileInfo.COMPRESSION_NONE,0,false);
		tryGray24UnsignedImage(BaseImage1x9,FileInfo.COMPRESSION_NONE,0,false);
		tryGray24UnsignedImage(BaseImage7x2,FileInfo.COMPRESSION_NONE,0,false);
		tryGray24UnsignedImage(BaseImage5x4,FileInfo.COMPRESSION_NONE,0,false);
		tryGray24UnsignedImage(BaseImage4x6,FileInfo.COMPRESSION_NONE,0,false);
		tryGray24UnsignedImage(BaseTestImage,FileInfo.COMPRESSION_NONE,0,false);
		
		tryGray24UnsignedImage(BaseTestImage,FileInfo.COMPRESSION_NONE,885,false);  // test header specifically
		tryGray24UnsignedImage(BaseTestImage,FileInfo.COMPRESSION_NONE,0,true);     // test intel specifically
		// NO LZW
		// NO LZW_DIFFERENCING
		// NO PACK_BITS
	}
	
	// if time were less of a factor a refactor would be nice
	//   make runXFileTypeTests as classes/interfaces
	//   define each one as an extension/impl
	//   keep an array of readers
	//   keep an array of test images
	//   iterate over images, applying all readers, with all values of flags
	// then adding a new image (might happen regularly) or a new filetype (less often?) is a lot easier to accommodate
	
	@Test
	public void testReadPixelsFromInputStream() {
		runBogusFileTypeTests();
		runGray8FileTypeTests();
		runColor8FileTypeTests();
		runGray16SignedFileTypeTests();
		runGray16UnsignedFileTypeTests();
		runGray32IntFileTypeTests();
		runGray32UnsignedFileTypeTests();
		runGray32FloatFileTypeTests();
		runGray64FloatFileTypeTests();
		runRgbFileTypeTests();
		runBgrFileTypeTests();
		runArgbFileTypeTests();
		runAbgrFileTypeTests();
		runBargFileTypeTests();
		runRgbPlanarFileTypeTests();
		runBitmapFileTypeTests();
		runRgb48FileTypeTests();
		runRgb48PlanarFileTypeTests();
		runGray12UnsignedFileTypeTests();
		runGray24UnsignedFileTypeTests();
	}

	@Test
	public void testReadPixelsInABetterWay()
	{
		// run test on basic functionality for each pixel type
		//   these end up getting run twice but these next calls simplify debugging
		gray8Tester.runTest(BaseTestImage,FileInfo.COMPRESSION_NONE,ByteOrder.DEFAULT,0);
		color8Tester.runTest(BaseTestImage,FileInfo.COMPRESSION_NONE,ByteOrder.DEFAULT,0);
		gray16SignedTester.runTest(BaseTestImage,FileInfo.COMPRESSION_NONE,ByteOrder.DEFAULT,0);
		gray16UnsignedTester.runTest(BaseTestImage,FileInfo.COMPRESSION_NONE,ByteOrder.DEFAULT,0);
		gray32IntTester.runTest(BaseTestImage,FileInfo.COMPRESSION_NONE,ByteOrder.DEFAULT,0);
		gray32UnsignedTester.runTest(BaseTestImage,FileInfo.COMPRESSION_NONE,ByteOrder.DEFAULT,0);
		gray32FloatTester.runTest(BaseTestImage,FileInfo.COMPRESSION_NONE,ByteOrder.DEFAULT,0);
		gray64FloatTester.runTest(BaseTestImage,FileInfo.COMPRESSION_NONE,ByteOrder.DEFAULT,0);
		rgbTester.runTest(BaseTestImage,FileInfo.COMPRESSION_NONE,ByteOrder.DEFAULT,0);
		bgrTester.runTest(BaseTestImage,FileInfo.COMPRESSION_NONE,ByteOrder.DEFAULT,0);
		argbTester.runTest(BaseTestImage,FileInfo.COMPRESSION_NONE,ByteOrder.DEFAULT,0);
		abgrTester.runTest(BaseTestImage,FileInfo.COMPRESSION_NONE,ByteOrder.DEFAULT,0);
		bargTester.runTest(BaseTestImage,FileInfo.COMPRESSION_NONE,ByteOrder.DEFAULT,0);
		rgbPlanarTester.runTest(BaseTestImage,FileInfo.COMPRESSION_NONE,ByteOrder.DEFAULT,0);
		bitmapTester.runTest(BaseTestImage,FileInfo.COMPRESSION_NONE,ByteOrder.DEFAULT,0);
		rgb48Tester.runTest(BaseTestImage,FileInfo.COMPRESSION_NONE,ByteOrder.DEFAULT,0);
		rgb48PlanarTester.runTest(BaseTestImage,FileInfo.COMPRESSION_NONE,ByteOrder.DEFAULT,0);
		gray12UnsignedTester.runTest(BaseTestImage,FileInfo.COMPRESSION_NONE,ByteOrder.DEFAULT,0);
		gray24UnsignedTester.runTest(BaseTestImage,FileInfo.COMPRESSION_NONE,ByteOrder.DEFAULT,0);
		
		// now run all legal combos of input parameters
		for (FormatTester tester : Testers)
			for (long[][] image : Images)
				for (int compression : CompressionModes)
					for (ByteOrder byteOrder : ByteOrders)
						for (int headerOffset : HeaderOffsets)
							tester.runTest(image,compression,byteOrder,headerOffset);
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
		// don't really know a good way to test this
		//   it looks like you need to have a inet resource to do this
		//   I could do a file url that points to a file of bytes in a known location but that is a bit troublesome
		//   Maybe we need an ij-tests data directory to store data files that we can locate for testing
		// it looks like you need to setup a correct FileInfo that describes data in the remote file and you
		//   are just reading basic pixel data
	}

	@Test
	public void testLzwUncompress() {
		try {
			byte[] bytes = {1,4,8,44,13,99,(byte)200,(byte)255,67,54,98,(byte)171,113};
			byte[] compressedBytes = lzwEncoder.encode(bytes);
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
		packbitsEncoderReal.runTests();
		
		// then test that ImageReader is returning the same info
		
		try {
			byte[] bytes = {1,4,8,44,44,44,44,13,99,(byte)200,(byte)255,67,54,98,98,98,(byte)171,113,113,113,113};

			ImageReader rdr = new ImageReader(new FileInfo());
			
			byte[] compressedBytes = packbitsEncoderNaive.encode(bytes);
			assertArrayEquals(bytes,rdr.packBitsUncompress(compressedBytes,bytes.length));

			compressedBytes = packbitsEncoderReal.encode(bytes);
			assertArrayEquals(bytes,rdr.packBitsUncompress(compressedBytes,bytes.length));
		}
		catch (Exception e)
		{
			System.out.println(e.getMessage());
			fail();
		}
	}

	@Test
	public void testPublicIVarsMinAndMax() {
		ImageReader rdr = new ImageReader(new FileInfo());
		
		assertEquals(rdr.min,0.0,FLOAT_TOL);
		assertEquals(rdr.max,0.0,FLOAT_TOL);

		rdr.min = 4000.0;
		rdr.max = 8888.7;
		
		assertEquals(rdr.min,4000.0,FLOAT_TOL);
		assertEquals(rdr.max,8888.7,FLOAT_TOL);
	}
}
