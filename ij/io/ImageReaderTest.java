package ij.io;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.*;

import loci.formats.codec.CodecOptions;
import loci.formats.codec.LZWCodec;
import loci.formats.codec.JPEGCodec;

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
//       below for appropriate classes.
//     Planar pixel formats below pass data in one strip. RgbPlanar does one strip for all three planes. Rgb48Planar does one strip
//       per plane. ImageReader inspections show that the written code there could conceivably handle multiple strips per plane. However
//       this may be an oversight in the original source code. Since the readPixels() subcase that reads these formats just reads the
//       underlying 8 and 16 bit subcases the strip lengths and offsets are reused and thus the strips have to be encoded with a fixed
//       size as big as the maximum compressed strip size. I doubt people are doing this but might need to test this.

public class ImageReaderTest {

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
	
	// swap this as desired for debugging
	//static final long[][] BaseTestImage = BaseImage4x6;
	//static final long[][] BaseTestImage = BaseImage5x5;
	static final long[][] BaseTestImage = Base48BitImage6x6;

	final long[][][] Images = new long[][][] {BaseImage1x1, BaseImage3x3, BaseImage3x3, BaseImage1x9, BaseImage7x2, BaseImage5x4, BaseImage4x6,
			Base24BitImage5x5, Base48BitImage6x6};

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
	
	final int[] CompressionModes = new int[] {FileInfo.COMPRESSION_NONE, FileInfo.LZW, FileInfo.LZW_WITH_DIFFERENCING, FileInfo.PACK_BITS,
			FileInfo.JPEG, FileInfo.COMPRESSION_UNKNOWN};
	
	final int[] HeaderOffsets = new int[] {0,10,100,1000,203,356,404,513,697,743,819,983};

	final boolean[] EncodeAsStrips = new boolean[] {false, true};
	
	static final float FLOAT_TOL = 0.00001f;

	// PackbitsEncoderNaive is designed with two things in mind
	//   - test ImageReader's ability to handle more than one style of packbits encoded data
	//   - stand in for RealPackBitsEncoder if we think it is ever faulty
	
	static class PackbitsEncoderNaive {
		
		PackbitsEncoderNaive() {}
		
		static public byte[] encode(byte[] input)
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
	
	static class PackbitsEncoder {
		
		PackbitsEncoder() {}

		// one byte lookahead
		static public byte[] encode(byte[] input)
		{
			int inputLen = input.length;

			if (inputLen == 0) return new byte[] {};
			if (inputLen == 1) return new byte[] {0,input[0]};

			// else two or more bytes in list

			ByteVector output = new ByteVector();

			// setup initial state
			int curr = 0;
			int offset = 1;
			boolean inMatch = false;

			if (input[0] == input[1])
				inMatch = true;

			while (curr+offset < inputLen)
			{
				if (curr+offset+1 >= inputLen) // next char is beyond end of input
				{
					if (inMatch)
					{
						output.add((byte)-offset);
						output.add(input[curr]);
					}
					else  // not in match
					{
						output.add((byte)offset);
						for (int i = 0; i < offset+1; i++)
							output.add(input[curr+i]);
					}
					curr = inputLen;
					offset = 0;
				}
				else if (input[curr+offset] == input[curr+offset+1]) // next char matches
				{	                                      
					if (inMatch)
					{
						if (offset < 127)  // not counted max num of pairs in a run
							offset++;
						else // offset == 127 : in this case offset ==  num pairs found so far
						{
							// write out the matched block
							output.add((byte)-127);
							output.add(input[curr]);

							// start over at next char
							if ((curr+offset+1) == (inputLen-1))  // the next char is the last char
							{
								// write out 0, input[inputLen-1]
								output.add((byte)0);
								output.add(input[inputLen-1]);
								curr = inputLen;
								offset = 0;
							}
							else // at least two more chars - reset state
							{
								curr += offset+1;
								offset = 1;
								if (input[curr] == input[curr+1])
									inMatch = true;
								else
									inMatch = false;
							}
						}
					}
					else // not currently in match
					{
						// write out the unmatched data
						output.add((byte)(offset-1));
						for (int i = 0; i < offset; i++)
							output.add(input[curr+i]);
						inMatch = true;
						curr += offset;  // not +1 cuz we'll reconsider the curr one as part of new run
						offset=1;
					}
				}
				else // next char is different from me
				{
					if (inMatch == false)
					{
						if (offset < 127)  // not reached max non-run length
							offset++;
						else // offset == 127: reached max non-run length
						{
							// write out matched data
							output.add((byte)offset);
							for (int i = 0; i < offset+1; i++)
								output.add(input[curr+i]);
								
							// start over at next char
							if (curr+offset+1 == inputLen-1)  // is the next char the last one?
							{
								output.add((byte)0);
								output.add(input[inputLen-1]);
								curr = inputLen;
								offset = 0;
							}
							else  // next char is available - reset state
							{
								curr += offset+1;
								offset = 1;
								if (input[curr] == input[curr+1])
									inMatch = true;
								else
									inMatch = false;
							}
						}
					}
					else // in a match
					{
						// match must end
						output.add((byte)-offset);
						output.add(input[curr]);
						
						// start over at next char
						if (curr+offset+1 == inputLen-1)  // is the next char the last one?
						{
							output.add((byte)0);
							output.add(input[inputLen-1]);
							curr = inputLen;
							offset = 0;
						}
						else  // next char is available - reset state
						{
							curr += offset+1;
							offset = 1;
							if (input[curr] == input[curr+1])
								inMatch = true;
							else
								inMatch = false;
						}
					}
				}
			}

			return output.toByteArray();
		}

		static private byte[] xOnes(int x)
		{
			byte[] output = new byte[x];
			for (int i = 0; i < x; i++)
				output[i] = 1;
			return output;
		}
		
		static private void runTests()
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
			
			// test bigger one edge cases
			assertArrayEquals(new byte[] {-125,1},encode(xOnes(126)));
			assertArrayEquals(new byte[] {-126,1},encode(xOnes(127)));
			assertArrayEquals(new byte[] {-127,1},encode(xOnes(128)));
			assertArrayEquals(new byte[] {-127,1,0,1},encode(xOnes(129)));
			assertArrayEquals(new byte[] {-127,1,-1,1},encode(xOnes(130)));
			assertArrayEquals(new byte[] {-127,1,-5,1},encode(xOnes(134)));
			assertArrayEquals(new byte[] {-127,1,-126,1},encode(xOnes(255)));
			assertArrayEquals(new byte[] {-127,1,-127,1},encode(xOnes(256)));
			assertArrayEquals(new byte[] {-127,1,-127,1,0,1},encode(xOnes(257)));
			assertArrayEquals(new byte[] {-127,1,-127,1,-1,1},encode(xOnes(258)));
			
			// untested
			//   long runs of unmatched data
		}
	}

	static class JpegEncoder {
		JpegEncoder() {}
		
		static public byte[] encode(byte[] input, PixelFormat format, FileInfo fi)
		{
			byte[] output = null;
			try {
				CodecOptions codecOptions = new CodecOptions();
				codecOptions.height = fi.height;
				codecOptions.width = fi.width;
				codecOptions.channels = format.numSamples();
				codecOptions.bitsPerSample = format.bitsPerSample();
				codecOptions.interleaved = (format.planes() == 1);
				codecOptions.littleEndian = fi.intelByteOrder;
				//codecOptions.signed = (format == gray16SignedFormat);
				codecOptions.signed = false;
				output = new JPEGCodec().compress(input, codecOptions);
			} catch (Exception e) {
				e.printStackTrace();
			}
			return output;
		}
	}
	
	static class LzwEncoder {
		
		LzwEncoder() {}
		
		static public byte[] encode(byte[] input)
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
	
	static class LzwDiffEncoder {
		
		LzwDiffEncoder() {}
		
		// unfortunately this method needed to be tweaked outside the overall design to accommodate intel byte orders for 16 bit
		//   sample depths. We could eliminate the extra code at the expense of redesigning some of the other classes.
		
		static private byte[] differentiate(byte[] input, PixelFormat format, FileInfo fi)
		{
			int offset = format.numSamples();
			
			// multiplane data is stored contiguously a plane at a time : offset to neighbor byte
			if (format.planes() > 1)
				offset = 1;
			
			byte[] data = input.clone();
			
			// if 16 bit samples must calc differences on original pixel data and not bytes
			if (format.bitsPerSample() == 16)
			{
				int actualPix = data.length / 2;
				
				for (int b = actualPix-1; b >= 0; b--)
				{
					if (b / offset % fi.width == 0)
						continue;
					
					int currPix, prevPix;
					
					if (fi.intelByteOrder)
					{
						currPix = ((data[b*2+0] & 0xff) << 0) | ((data[b*2+1] & 0xff) << 8);
						prevPix = ((data[b*2-2] & 0xff) << 0) | ((data[b*2-1] & 0xff) << 8);
						currPix -= prevPix;
						data[b*2+0] = (byte)((currPix & 0x00ff) >> 0);
						data[b*2+1] = (byte)((currPix & 0xff00) >> 8);
					}
					else
					{
						currPix = ((data[b*2+0] & 0xff) << 8) | ((data[b*2+1] & 0xff) << 0);
						prevPix = ((data[b*2-2] & 0xff) << 8) | ((data[b*2-1] & 0xff) << 0);
						currPix -= prevPix;
						data[b*2+0] = (byte)((currPix & 0xff00) >> 8);
						data[b*2+1] = (byte)((currPix & 0x00ff) >> 0);
					}
				}
				
			}
			else // input data is of type byte and we can calc differences as is
				for (int b=data.length-1; b>=0; b--)
				{
					// this code adapted from TiffCompression code in BioFormats
					if (b / offset % fi.width == 0)
						continue;
					data[b] -= data[b - offset];
				}
		      
			return data;
		}
		
		static public byte[] encode(byte[] input, PixelFormat format, FileInfo fi)
		{
			input = differentiate(input, format, fi);
			
			byte[] output = LzwEncoder.encode(input);

			return output;
		}
	}

	static class TwelveBitEncoder
	{
		TwelveBitEncoder() {}
		
		static private byte[] encode(long[][] inPix)
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
	
	// Note:
	//   the readPixels() test near the bottom is difficult to debug: make helper classes where breakpoints can be easily handled
	
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
		{
			if (expected[i] != actual[i])
			{
				fail("myAssertArrayEquals(int[],int[]) items differ at index " + i + ": expected "+ expected[i] + " and got " + actual[i]);
			}
		}
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
		if (expected == actual)
			return;
		
		if ((expected == null) || (actual == null))
			fail("assertSame() one argument is null : (expcted=" + expected + ",actual=" + actual + ")");
		
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

		void runTest(long[][] image, int compression, ByteOrder byteOrder, int headerOffset, boolean inStrips)
		{
			if (theFormat.canDoImageCombo(compression,byteOrder,headerOffset,inStrips))
			{				
				FileInfo fi = new FileInfo();
				
				byte[] pixBytes = theFormat.getBytes(image,compression,byteOrder,headerOffset,inStrips,fi);
				
				ByteArrayInputStream byteStream = new ByteArrayInputStream(pixBytes);
				
				ImageReader rdr = new ImageReader(fi);
				
				Object actualPixels = rdr.readPixels(byteStream);
				
				Object expectedPixels = theFormat.expectedResults(image);
				
				assertSame(expectedPixels, actualPixels);
			}
		}
	}
	
	static class PixelArranger
	{
		PixelArranger() {}

		static void reverse(byte[] bytes)
		{
			int totBytes = bytes.length;
			int last = totBytes - 1;
			int halfLen = totBytes / 2;
			for (int i = 0; i < halfLen; i++)
			{
				byte tmp = bytes[i];
				bytes[i] = bytes[last-i];
				bytes[last-i] = tmp;
			}
			// note should work for even and odd lengths as middle element of an odd list need not be swapped
		}

		static byte[] compress(PixelFormat format, FileInfo fi, byte[] input)
		{
			byte[] compressed = input;
			
			if (fi.compression == FileInfo.LZW)
				compressed = LzwEncoder.encode(input);
			else if (fi.compression == FileInfo.LZW_WITH_DIFFERENCING)
				compressed = LzwDiffEncoder.encode(input,format,fi);
			else if (fi.compression == FileInfo.PACK_BITS)
				compressed = PackbitsEncoder.encode(input);
			else if (fi.compression == FileInfo.JPEG)
				compressed = JpegEncoder.encode(input,format,fi);
			else
				; // do nothing

			return compressed;
		}
		
		static private byte[] prependFakeHeader(int headerBytes, byte[] pixData)
		{
			byte[] header = new byte[headerBytes];
			byte[] output = new byte[header.length + pixData.length];
			System.arraycopy(header,0,output,0,header.length);
			System.arraycopy(pixData,0,output,header.length,pixData.length);
			return output;
		}
		
		static byte[] attachHeader(FileInfo fi, int headerBytes, byte[] pixData)
		{
			fi.offset = headerBytes;
			fi.longOffset = headerBytes;
			return prependFakeHeader(headerBytes,pixData);	
		}

		static byte[] arrangeInStrips(PixelFormat format, long[][] image, FileInfo fi)
		{
			ByteOrder myByteOrder = ByteOrder.DEFAULT;
			if (fi.intelByteOrder)
				myByteOrder = ByteOrder.INTEL;
			
			int rows = image.length;
			int cols = image[0].length;
			
			// for testing purpose we'll only make 1, 2, or 3 strips
			int strips;
			
			if ((rows % 3) == 0)
				strips = rows / 3;
			else if ((rows % 2) == 0)
				strips = rows / 2;
			else
				strips = 1;
			
			fi.stripLengths = new int[strips];
			fi.stripOffsets = new int[strips];
			fi.rowsPerStrip = rows / strips;

			byte[] output = new byte[] {};
			
			for (int s = 0; s < strips; s++)
			{
				byte[] strip = new byte[fi.rowsPerStrip * cols * format.nativeBytes(0,myByteOrder).length];
				
				int i = 0;
				for (int r = 0; r < fi.rowsPerStrip; r++)
				{
					for (int c = 0; c < cols; c++)
					{
						byte[] pixBytes = format.nativeBytes(image[fi.rowsPerStrip*s + r][c], myByteOrder);
						for (int k = 0; k < pixBytes.length; k++)
							strip[i++] = pixBytes[k];
					}
				}
				
				strip = compress(format,fi,strip);

				// calc offsets
				fi.stripLengths[s] = strip.length;
				fi.stripOffsets[s] = (s == 0 ? 0 : (fi.stripOffsets[s-1] + fi.stripLengths[s-1]));
				
				// concat strip to output
				byte[] temp = new byte[output.length + strip.length];
				System.arraycopy(output,0,temp,0,output.length);
				System.arraycopy(strip,0,temp,output.length,strip.length);
				output = temp;
			}

			return output;
		}
		
		static byte[] arrangeContiguously(PixelFormat format, long[][] image, FileInfo fi)
		{
			ByteOrder myByteOrder = ByteOrder.DEFAULT;
			if (fi.intelByteOrder)
				myByteOrder = ByteOrder.INTEL;
			
			byte[] output = new byte[fi.height * fi.width * format.nativeBytes(0,myByteOrder).length];
			
			int i = 0;
			for (long[] row : image)
				for (long pix : row)
				{
					byte[] bytes = format.nativeBytes(pix,myByteOrder);
					for (int k = 0; k < bytes.length; k++)
						output[i++] = bytes[k];
				}
		
			output = compress(format,fi,output);
			
			fi.stripOffsets = new int[] {0};
			fi.stripLengths = new int[] {output.length};
			fi.rowsPerStrip = fi.height;
			
			return output;
		}
		
		// TODO: if multistrip planar testing needed
		//    make arrangeAsPlanesContiguously() and arrangeAsPlanesStripped()
		//    and then call here appropriately based on stripped parameter
		//    making such a routine would entail encoding each strip ahead of time, figuring the biggest strip, using that size
		//      to repeatedly fill the stripLengths and stripOffsets, etc. See how compressEach planes is used below.
		
		static byte[] arrangeAsPlanes(PixelFormat format, long[][] image, FileInfo fi, boolean stripped, boolean compressEachPlane)
		{
			ByteOrder myByteOrder = ByteOrder.DEFAULT;
			if (fi.intelByteOrder)
				myByteOrder = ByteOrder.INTEL;

			int planes = format.planes();
			int bytesPerPix = format.nativeBytes(0,myByteOrder).length;
			int pixBytesPerPlane = bytesPerPix / planes;
			
			byte[][] planeData = new byte[planes][];
			
			for (int i = 0; i < planes; i++)
				planeData[i] = new byte[fi.height * fi.width * pixBytesPerPlane];

			int offset = 0;
			for (long[] row : image)
				for (long pix : row)
				{
					byte[] bytes = format.nativeBytes(pix,myByteOrder);
					int b = 0;
					for (int p = 0; p < planes; p++)
						for (int i = 0; i < pixBytesPerPlane; i++)
							planeData[p][offset+i] = bytes[b++];
					offset += pixBytesPerPlane;
				}

			byte[] output = new byte[]{};
			
			if (compressEachPlane)
			{
				// compress the planes
				for (int p = 0; p < planes; p++)
					planeData[p] = compress(format,fi,planeData[p]);

				int biggestPlane = 0;
				for (int p = 0; p < planes; p++)
					if (planeData[p].length > biggestPlane)
						biggestPlane = planeData[p].length;
				
				output = new byte[planes * biggestPlane * pixBytesPerPlane];

				// finally combine planes : note that the written planes are <= biggestPlane in length
				for (int p = 0; p < planes; p++)
					System.arraycopy(planeData[p], 0, output, biggestPlane*p, planeData[p].length);

				fi.stripOffsets = new int[] {0};
				fi.stripLengths = new int[] {biggestPlane};
				fi.rowsPerStrip = fi.height;
			}
			else  // compress all as one plane
			{
				output = new byte[fi.height * fi.width * bytesPerPix];
				
				int planeLength = fi.height * fi.width * pixBytesPerPlane;
				
				for (int p = 0; p < planes; p++)
					System.arraycopy(planeData[p], 0, output, planeLength*p, planeLength);
				
				output = compress(format,fi,output);

				fi.stripOffsets = new int[] {0};
				fi.stripLengths = new int[] {output.length};
				fi.rowsPerStrip = fi.height;
			}
			
			return output;
		}
	}
	
	abstract class PixelFormat {
		
		private String name;  // might be useful for debugging purposes. otherwise could be an interface
		private int numSamples;
		private int bitsPerSample;
		private int planes;
		
		public int numSamples()    { return numSamples; }
		public int bitsPerSample() { return bitsPerSample; }
		public int planes()        { return planes; }
		public String name()       { return name; }
		
		
		abstract boolean canDoImageCombo(int compression, ByteOrder byteOrder, int headerBytes, boolean stripped);
		abstract byte[] nativeBytes(long pixel, ByteOrder byteOrder);
		abstract byte[] getBytes(long[][] image, int compression, ByteOrder byteOrder, int headerBytes, boolean inStrips, FileInfo fi);
		abstract Object expectedResults(long[][] inputImage);
		
		PixelFormat(String name, int numSamples, int bitsPerSample, int planes)
		{
			this.name = name;
			this.numSamples = numSamples;
			this.bitsPerSample = bitsPerSample;
			this.planes = planes;
		}
	}
	
	class Gray8Format extends PixelFormat
	{
		Gray8Format()
		{
			super("Gray8",1,8,1);
		}
		
		boolean canDoImageCombo(int compression, ByteOrder byteOrder, int headerBytes, boolean stripped)
		{
			if (compression == FileInfo.COMPRESSION_UNKNOWN)
				return false;
			if (compression == FileInfo.JPEG)
				return false;
			
			if (byteOrder == ByteOrder.INTEL)
				return false;

			if (stripped && (compression == FileInfo.COMPRESSION_NONE))
				return false;

			return true;
		}
		
		byte[] nativeBytes(long pix, ByteOrder byteOrder)
		{
			return new byte[] {(byte)(pix & 0xff)};
		}
		
		byte[] getBytes(long[][] image, int compression, ByteOrder byteOrder, int headerBytes, boolean inStrips, FileInfo fi)
		{
			initializeFileInfo(fi,FileInfo.GRAY8,compression,byteOrder,image.length,image[0].length);

			byte[] output;
			
			if (inStrips)
				output = PixelArranger.arrangeInStrips(this,image,fi);
			else
				output = PixelArranger.arrangeContiguously(this,image,fi);
			
			output = PixelArranger.attachHeader(fi,headerBytes,output);

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
			super("Color8",1,8,1);
		}
		
		boolean canDoImageCombo(int compression, ByteOrder byteOrder, int headerBytes, boolean stripped)
		{
			if (compression == FileInfo.COMPRESSION_UNKNOWN)
				return false;
			if (compression == FileInfo.JPEG)
				return false;
			
			if (byteOrder == ByteOrder.INTEL)
				return false;

			if (stripped && (compression == FileInfo.COMPRESSION_NONE))
				return false;

			return true;
		}
		
		byte[] nativeBytes(long pix, ByteOrder byteOrder)
		{
			return new byte[] {(byte)(pix & 0xff)};
		}
		
		byte[] getBytes(long[][] image, int compression, ByteOrder byteOrder, int headerBytes, boolean inStrips, FileInfo fi)
		{
			initializeFileInfo(fi,FileInfo.COLOR8,compression,byteOrder,image.length,image[0].length);

			byte[] output;
			
			if (inStrips)
				output = PixelArranger.arrangeInStrips(this,image,fi);
			else
				output = PixelArranger.arrangeContiguously(this,image,fi);
			
			output = PixelArranger.attachHeader(fi,headerBytes,output);

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
			super("Gray16Signed",1,16,1);
		}
		
		boolean canDoImageCombo(int compression, ByteOrder byteOrder, int headerBytes, boolean stripped)
		{
			if (compression == FileInfo.COMPRESSION_UNKNOWN)
				return false;
			if (compression == FileInfo.JPEG)
				return false;
			if (compression == FileInfo.PACK_BITS)
				return false;
			
			if (stripped && (compression == FileInfo.COMPRESSION_NONE))
				return false;
			
			return true;
		}
		
		byte[] nativeBytes(long pix, ByteOrder byteOrder)
		{
			byte[] output = new byte[2];
			
			output[0] = (byte)((pix & 0xff00) >> 8);
			output[1] = (byte)((pix & 0x00ff) >> 0);
			
			if (byteOrder == byteOrder.INTEL)
				PixelArranger.reverse(output);
			
			return output;
		}
		
		byte[] getBytes(long[][] image, int compression, ByteOrder byteOrder, int headerBytes, boolean inStrips, FileInfo fi)
		{
			initializeFileInfo(fi,FileInfo.GRAY16_SIGNED,compression,byteOrder,image.length,image[0].length);

			byte[] output;
			
			if (inStrips)
				output = PixelArranger.arrangeInStrips(this,image,fi);
			else
				output = PixelArranger.arrangeContiguously(this,image,fi);
			
			output = PixelArranger.attachHeader(fi,headerBytes,output);

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
			super("Gray16Unsigned",1,16,1);
		}
		
		boolean canDoImageCombo(int compression, ByteOrder byteOrder, int headerBytes, boolean stripped)
		{
			if (compression == FileInfo.COMPRESSION_UNKNOWN)
				return false;
			if (compression == FileInfo.JPEG)
				return false;
			if (compression == FileInfo.PACK_BITS)
				return false;
			
			if (stripped && (compression == FileInfo.COMPRESSION_NONE))
				return false;

			return true;
		}
		
		byte[] nativeBytes(long pix, ByteOrder byteOrder)
		{
			byte[] output = new byte[2];
			
			output[0] = (byte)((pix & 0xff00) >> 8);
			output[1] = (byte)((pix & 0x00ff) >> 0);
			
			if (byteOrder == byteOrder.INTEL)
				PixelArranger.reverse(output);

			return output;
		}
		
		byte[] getBytes(long[][] image, int compression, ByteOrder byteOrder, int headerBytes, boolean inStrips, FileInfo fi)
		{
			initializeFileInfo(fi,FileInfo.GRAY16_UNSIGNED,compression,byteOrder,image.length,image[0].length);			

			byte[] output;
			
			if (inStrips)
				output = PixelArranger.arrangeInStrips(this,image,fi);
			else
				output = PixelArranger.arrangeContiguously(this,image,fi);
			
			output = PixelArranger.attachHeader(fi,headerBytes,output);

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
			super("Gray32Int",1,32,1);
		}
		
		boolean canDoImageCombo(int compression, ByteOrder byteOrder, int headerBytes, boolean stripped)
		{
			if (compression != FileInfo.COMPRESSION_NONE)
				return false;

			if (stripped)
				return false;
			
			return true;
		}
		
		byte[] nativeBytes(long pix, ByteOrder byteOrder)
		{
			byte[] output = new byte[4];
			
			output[0] = (byte)((pix & 0xff000000) >> 24);
			output[1] = (byte)((pix & 0x00ff0000) >> 16);
			output[2] = (byte)((pix & 0x0000ff00) >> 8);
			output[3] = (byte)((pix & 0x000000ff) >> 0);
			
			if (byteOrder == byteOrder.INTEL)
				PixelArranger.reverse(output);

			return output;
		}
		
		byte[] getBytes(long[][] image, int compression, ByteOrder byteOrder, int headerBytes, boolean inStrips, FileInfo fi)
		{
			initializeFileInfo(fi,FileInfo.GRAY32_INT,compression,byteOrder,image.length,image[0].length);

			byte[] output;
			
			if (inStrips)
				output = PixelArranger.arrangeInStrips(this,image,fi);
			else
				output = PixelArranger.arrangeContiguously(this,image,fi);
			
			output = PixelArranger.attachHeader(fi,headerBytes,output);

			return output;
		}

		Object expectedResults(long[][] inputImage)
		{
			float[] output = new float[inputImage.length * inputImage[0].length];
			
			int i = 0;
			for (long[] row : inputImage)
				for (long pix : row)
					output[i++] = (float)(int)(pix & 0xffffffffL);
			return output;
		}
	}

	class Gray32UnsignedFormat extends PixelFormat
	{
		Gray32UnsignedFormat()
		{
			super("Gray32Unsigned",1,32,1);
		}
		
		boolean canDoImageCombo(int compression, ByteOrder byteOrder, int headerBytes, boolean stripped)
		{
			if (compression != FileInfo.COMPRESSION_NONE)
				return false;
			
			if (stripped)
				return false;
			
			return true;
		}
		
		byte[] nativeBytes(long pix, ByteOrder byteOrder)
		{
			byte[] output = new byte[4];
			
			output[0] = (byte)((pix & 0xff000000) >> 24);
			output[1] = (byte)((pix & 0x00ff0000) >> 16);
			output[2] = (byte)((pix & 0x0000ff00) >> 8);
			output[3] = (byte)((pix & 0x000000ff) >> 0);

			if (byteOrder == byteOrder.INTEL)
				PixelArranger.reverse(output);

			return output;
		}
		
		byte[] getBytes(long[][] image, int compression, ByteOrder byteOrder, int headerBytes, boolean inStrips, FileInfo fi)
		{
			initializeFileInfo(fi,FileInfo.GRAY32_UNSIGNED,compression,byteOrder,image.length,image[0].length);

			byte[] output;
			
			if (inStrips)
				output = PixelArranger.arrangeInStrips(this,image,fi);
			else
				output = PixelArranger.arrangeContiguously(this,image,fi);
			
			output = PixelArranger.attachHeader(fi,headerBytes,output);

			return output;
		}

		Object expectedResults(long[][] inputImage)
		{
			float[] output = new float[inputImage.length * inputImage[0].length];
			
			int i = 0;
			for (long[] row : inputImage)
				for (long pix : row)
					output[i++] = (float)(pix & 0xffffffffL);
			return output;
		}
	}

	class Gray32FloatFormat extends PixelFormat
	{
		Gray32FloatFormat()
		{
			super("Gray32Float",1,32,1);
		}
		
		boolean canDoImageCombo(int compression, ByteOrder byteOrder, int headerBytes, boolean stripped)
		{
			if (compression != FileInfo.COMPRESSION_NONE)
				return false;
			
			if (stripped)
				return false;
			
			return true;
		}
		
		byte[] nativeBytes(long pix, ByteOrder byteOrder)
		{
			byte[] output = new byte[4];
			
			float fPix = (float) pix;

			int bPix = Float.floatToIntBits(fPix);
			
			output[0] = (byte)((bPix & 0xff000000) >> 24);
			output[1] = (byte)((bPix & 0x00ff0000) >> 16);
			output[2] = (byte)((bPix & 0x0000ff00) >> 8);
			output[3] = (byte)((bPix & 0x000000ff) >> 0);
			
			if (byteOrder == byteOrder.INTEL)
				PixelArranger.reverse(output);

			return output;
		}
		
		byte[] getBytes(long[][] image, int compression, ByteOrder byteOrder, int headerBytes, boolean inStrips, FileInfo fi)
		{
			initializeFileInfo(fi,FileInfo.GRAY32_FLOAT,compression,byteOrder,image.length,image[0].length);
			
			byte[] output;
			
			// ALWAYS contiguous in this case
			output = PixelArranger.arrangeContiguously(this,image,fi);
			
			output = PixelArranger.attachHeader(fi,headerBytes,output);

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
			super("Gray64Float",1,64,1);
		}
		
		boolean canDoImageCombo(int compression, ByteOrder byteOrder, int headerBytes, boolean stripped)
		{
			if (compression != FileInfo.COMPRESSION_NONE)
				return false;
		
			if (stripped)
				return false;
			
			return true;
		}
		
		byte[] nativeBytes(long pix, ByteOrder byteOrder)
		{
			byte[] output = new byte[8];

			double dPix = (double) pix;

			long bPix = Double.doubleToLongBits(dPix);

			output[0] = (byte)((bPix & 0xff00000000000000L) >> 56);
			output[1] = (byte)((bPix & 0x00ff000000000000L) >> 48);
			output[2] = (byte)((bPix & 0x0000ff0000000000L) >> 40);
			output[3] = (byte)((bPix & 0x000000ff00000000L) >> 32);
			output[4] = (byte)((bPix & 0x00000000ff000000L) >> 24);
			output[5] = (byte)((bPix & 0x0000000000ff0000L) >> 16);
			output[6] = (byte)((bPix & 0x000000000000ff00L) >> 8);
			output[7] = (byte)((bPix & 0x00000000000000ffL) >> 0);
			
			if (byteOrder == byteOrder.INTEL)
				PixelArranger.reverse(output);

			return output;
		}
		
		byte[] getBytes(long[][] image, int compression, ByteOrder byteOrder, int headerBytes, boolean inStrips, FileInfo fi)
		{
			initializeFileInfo(fi,FileInfo.GRAY64_FLOAT,compression,byteOrder,image.length,image[0].length);
			

			byte[] output;

			// ALWAYS contiguous in this case
			output = PixelArranger.arrangeContiguously(this,image,fi);
			
			output = PixelArranger.attachHeader(fi,headerBytes,output);

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
			super("Rgb",3,8,1);
		}
		
		boolean canDoImageCombo(int compression, ByteOrder byteOrder, int headerBytes, boolean stripped)
		{
			if (compression == FileInfo.COMPRESSION_UNKNOWN)
				return false;
			if (compression == FileInfo.JPEG)  // TODO: remove this restriction to test jpeg compression
				return false;
			
			if (byteOrder == ByteOrder.INTEL)
				return false;
			
			if (stripped && (compression == FileInfo.COMPRESSION_NONE))
				return false;
			
			return true;
		}
		
		byte[] nativeBytes(long pix, ByteOrder byteOrder)
		{
			byte[] output = new byte[3];
			
			output[0] = (byte)((pix & 0xff0000) >> 16);
			output[1] = (byte)((pix & 0x00ff00) >> 8);
			output[2] = (byte)((pix & 0x0000ff) >> 0);
			
			return output;
		}
		
		byte[] getBytes(long[][] image, int compression, ByteOrder byteOrder, int headerBytes, boolean inStrips, FileInfo fi)
		{
			initializeFileInfo(fi,FileInfo.RGB,compression,byteOrder,image.length,image[0].length);
			
			byte[] output;
			
			if (inStrips)
				output = PixelArranger.arrangeInStrips(this,image,fi);
			else
				output = PixelArranger.arrangeContiguously(this,image,fi);
			
			output = PixelArranger.attachHeader(fi,headerBytes,output);

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
			super("Bgr",3,8,1);
		}
		
		boolean canDoImageCombo(int compression, ByteOrder byteOrder, int headerBytes, boolean stripped)
		{
			if (compression == FileInfo.COMPRESSION_UNKNOWN)
				return false;
			if (compression == FileInfo.JPEG)  // TODO: remove this restriction to test jpeg compression
				return false;
			
			if (byteOrder == ByteOrder.INTEL)
				return false;
			
			if (stripped && (compression == FileInfo.COMPRESSION_NONE))
				return false;
			
			return true;
		}
		
		byte[] nativeBytes(long pix, ByteOrder byteOrder)
		{
			byte[] output = new byte[3];
			
			output[0] = (byte)((pix & 0x0000ff) >> 0);
			output[1] = (byte)((pix & 0x00ff00) >> 8);
			output[2] = (byte)((pix & 0xff0000) >> 16);
			
			return output;
		}
		
		byte[] getBytes(long[][] image, int compression, ByteOrder byteOrder, int headerBytes, boolean inStrips, FileInfo fi)
		{
			initializeFileInfo(fi,FileInfo.BGR,compression,byteOrder,image.length,image[0].length);
			
			byte[] output;
			
			if (inStrips)
				output = PixelArranger.arrangeInStrips(this,image,fi);
			else
				output = PixelArranger.arrangeContiguously(this,image,fi);
			
			output = PixelArranger.attachHeader(fi,headerBytes,output);

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
			super("Argb",4,8,1);
		}
		
		boolean canDoImageCombo(int compression, ByteOrder byteOrder, int headerBytes, boolean stripped)
		{
			if (compression == FileInfo.COMPRESSION_UNKNOWN)
				return false;
			if (compression == FileInfo.JPEG)  // TODO: remove this restriction to test jpeg compression
				return false;
			
			if (stripped && (compression == FileInfo.COMPRESSION_NONE))
				return false;
			
			return true;
		}
		
		byte[] nativeBytes(long pix, ByteOrder byteOrder)
		{
			byte[] output = new byte[4];
					
			if (byteOrder == ByteOrder.INTEL)
			{
				output[0] = (byte)((pix & 0x00ff0000) >> 16);
				output[1] = (byte)((pix & 0x0000ff00) >> 8);
				output[2] = (byte)((pix & 0x000000ff) >> 0);
				output[3] = (byte)((pix & 0xff000000) >> 24);
			}
			else
			{
				output[0] = (byte)((pix & 0xff000000) >> 24);
				output[1] = (byte)((pix & 0x00ff0000) >> 16);
				output[2] = (byte)((pix & 0x0000ff00) >> 8);
				output[3] = (byte)((pix & 0x000000ff) >> 0);
			}

			return output;
		}
		
		byte[] getBytes(long[][] image, int compression, ByteOrder byteOrder, int headerBytes, boolean inStrips, FileInfo fi)
		{
			initializeFileInfo(fi,FileInfo.ARGB,compression,byteOrder,image.length,image[0].length);
			
			byte[] output;
			
			if (inStrips)
				output = PixelArranger.arrangeInStrips(this,image,fi);
			else
				output = PixelArranger.arrangeContiguously(this,image,fi);
			
			output = PixelArranger.attachHeader(fi,headerBytes,output);

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
			super("Abgr",4,8,1);
		}
		
		boolean canDoImageCombo(int compression, ByteOrder byteOrder, int headerBytes, boolean stripped)
		{
			if (compression == FileInfo.COMPRESSION_UNKNOWN)
				return false;
			if (compression == FileInfo.JPEG)  // TODO: remove this restriction to test jpeg compression
				return false;
			
			if (byteOrder == ByteOrder.INTEL)
				return false;

			if (stripped && (compression == FileInfo.COMPRESSION_NONE))
				return false;
			
			return true;
		}
		
		byte[] nativeBytes(long pix, ByteOrder byteOrder)
		{
			byte[] output = new byte[4];
			
			output[0] = (byte)((pix & 0x000000ff) >> 0);
			output[1] = (byte)((pix & 0x0000ff00) >> 8);
			output[2] = (byte)((pix & 0x00ff0000) >> 16);
			output[3] = (byte)((pix & 0xff000000) >> 24);

			return output;
		}
		
		byte[] getBytes(long[][] image, int compression, ByteOrder byteOrder, int headerBytes, boolean inStrips, FileInfo fi)
		{
			initializeFileInfo(fi,FileInfo.ABGR,compression,byteOrder,image.length,image[0].length);
			
			byte[] output;
			
			if (inStrips)
				output = PixelArranger.arrangeInStrips(this,image,fi);
			else
				output = PixelArranger.arrangeContiguously(this,image,fi);
			
			output = PixelArranger.attachHeader(fi,headerBytes,output);

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
			super("Barg",4,8,1);
		}
		
		boolean canDoImageCombo(int compression, ByteOrder byteOrder, int headerBytes, boolean stripped)
		{
			if (compression == FileInfo.COMPRESSION_UNKNOWN)
				return false;
			if (compression == FileInfo.JPEG)  // TODO: remove this restriction to test jpeg compression
				return false;
			
			if (byteOrder == ByteOrder.INTEL)
				return false;

			if (stripped && (compression == FileInfo.COMPRESSION_NONE))
				return false;
			
			return true;
		}
		
		byte[] nativeBytes(long pix, ByteOrder byteOrder)
		{
			byte[] output = new byte[4];
			
			output[0] = (byte)((pix & 0x000000ff) >> 0);
			output[1] = (byte)((pix & 0xff000000) >> 24);
			output[2] = (byte)((pix & 0x00ff0000) >> 16);
			output[3] = (byte)((pix & 0x0000ff00) >> 8);
			
			return output;
		}
		
		byte[] getBytes(long[][] image, int compression, ByteOrder byteOrder, int headerBytes, boolean inStrips, FileInfo fi)
		{
			initializeFileInfo(fi,FileInfo.BARG,compression,byteOrder,image.length,image[0].length);
			
			byte[] output;
			
			if (inStrips)
				output = PixelArranger.arrangeInStrips(this,image,fi);
			else
				output = PixelArranger.arrangeContiguously(this,image,fi);
			
			output = PixelArranger.attachHeader(fi,headerBytes,output);

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
			super("RgbPlanar",3,8,3);
		}
		
		boolean canDoImageCombo(int compression, ByteOrder byteOrder, int headerBytes, boolean stripped)
		{
			if (compression == FileInfo.COMPRESSION_UNKNOWN)
				return false;
			if (compression == FileInfo.JPEG)  // TODO: remove this restriction to test jpeg compression
				return false;
			
			if (byteOrder == ByteOrder.INTEL)
				return false;

			if (stripped && (compression == FileInfo.COMPRESSION_NONE))
				return false;
			
			return true;
		}
		
		byte[] nativeBytes(long pix, ByteOrder byteOrder)
		{
			byte[] output = new byte[3];

			output[0] = (byte)((pix & 0x00ff0000) >> 16);
			output[1] = (byte)((pix & 0x0000ff00) >> 8);
			output[2] = (byte)((pix & 0x000000ff) >> 0);
			
			return output;
		}
		
		byte[] getBytes(long[][] image, int compression, ByteOrder byteOrder, int headerBytes, boolean inStrips, FileInfo fi)
		{
			initializeFileInfo(fi,FileInfo.RGB_PLANAR,compression,byteOrder,image.length,image[0].length);
			
			byte[] output = PixelArranger.arrangeAsPlanes(this, image, fi, inStrips, false);
			
			output = PixelArranger.attachHeader(fi,headerBytes,output);

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
			super("Bitmap",1,1,1);
		}
		
		boolean canDoImageCombo(int compression, ByteOrder byteOrder, int headerBytes, boolean stripped)
		{
			if (compression != FileInfo.COMPRESSION_NONE)
				return false;
			
			if (byteOrder == ByteOrder.INTEL)
				return false;

			if (stripped)
				return false;
			
			return true;
		}

		byte[] nativeBytes(long pix, ByteOrder byteOrder)
		{
			// since this is multiple pixels per byte the basic model does not fit
			// getBytes shows how pixels are arranged
			
			return null;
		}
		
		byte[] getBytes(long[][] image, int compression, ByteOrder byteOrder, int headerBytes, boolean inStrips, FileInfo fi)
		{
			initializeFileInfo(fi,FileInfo.BITMAP,compression,byteOrder,image.length,image[0].length);
			
			int pixPerRow = (int) Math.ceil(fi.width / 8.0);
			
			byte[] output = new byte[fi.height * pixPerRow];

			// note that I am only using the lowest 1 bit of the image long for testing purposes
			
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

			output = PixelArranger.attachHeader(fi,headerBytes,output);
			
			return output;
		}

		Object expectedResults(long[][] inputImage)
		{
			int rows = inputImage.length;
			int cols = inputImage[0].length;
			
			byte[] output = new byte[rows * cols];
		
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
			super("Rgb48",3,16,1);
		}
		
		boolean canDoImageCombo(int compression, ByteOrder byteOrder, int headerBytes, boolean stripped)
		{
			if (compression == FileInfo.LZW_WITH_DIFFERENCING)
				return false;
			if (compression == FileInfo.COMPRESSION_UNKNOWN)
				return false;
			if (compression == FileInfo.JPEG)  // TODO: remove this restriction to test jpeg compression
				return false;

			// this class always works with strips
			if (stripped == false)
				return false;
			
			return true;
		}

		byte[] nativeBytes(long pix, ByteOrder byteOrder)
		{
			byte[] output = new byte[6];
			
			
			if (byteOrder == byteOrder.INTEL)
			{
				output[0] = (byte)((pix & 0xff00000000L) >> 32);
				output[1] = (byte)((pix & 0xff0000000000L) >> 40);
				output[2] = (byte)((pix & 0xff0000L) >> 16);
				output[3] = (byte)((pix & 0xff000000L) >> 24);
				output[4] = (byte)((pix & 0xffL) >> 0);
				output[5] = (byte)((pix & 0xff00L) >> 8);
			}
			else
			{
				output[0] = (byte)((pix & 0xff0000000000L) >> 40);
				output[1] = (byte)((pix & 0xff00000000L) >> 32);
				output[2] = (byte)((pix & 0xff000000L) >> 24);
				output[3] = (byte)((pix & 0xff0000L) >> 16);
				output[4] = (byte)((pix & 0xff00L) >> 8);
				output[5] = (byte)((pix & 0xffL) >> 0);
			}
			
			return output;
		}
		
		byte[] getBytes(long[][] image, int compression, ByteOrder byteOrder, int headerBytes, boolean inStrips, FileInfo fi)
		{
			initializeFileInfo(fi,FileInfo.RGB48,compression,byteOrder,image.length,image[0].length);

			// ALWAYS only do stripped data for this format
			
			byte[] output = PixelArranger.arrangeInStrips(this,image,fi);

			output = PixelArranger.attachHeader(fi,headerBytes,output);
			
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
			super("Rgb48Planar",3,16,3);
		}
		
		boolean canDoImageCombo(int compression, ByteOrder byteOrder, int headerBytes, boolean stripped)
		{
			if (compression == FileInfo.COMPRESSION_UNKNOWN)
				return false;
			if (compression == FileInfo.JPEG)  // TODO: remove this restriction to test jpeg compression
				return false;
			if (compression == FileInfo.PACK_BITS)
				return false;
			
			// this method always exercises strips
			if (stripped == false)
				return false;
			
			return true;
		}

		byte[] nativeBytes(long pix, ByteOrder byteOrder)
		{
			byte[] output = new byte[6];
			
			long channel1 = ((pix & 0x00000000ffffL) >> 0);
			long channel2 = ((pix & 0x0000ffff0000L) >> 16);
			long channel3 = ((pix & 0xffff00000000L) >> 32);
			//
			// divide the long into three channels
			if (byteOrder == byteOrder.INTEL)
			{
				output[0] = (byte) ((channel1 & 0x00ff) >> 0);
				output[1] = (byte) ((channel1 & 0xff00) >> 8);
				output[2] = (byte) ((channel2 & 0x00ff) >> 0);
				output[3] = (byte) ((channel2 & 0xff00) >> 8);
				output[4] = (byte) ((channel3 & 0x00ff) >> 0);
				output[5] = (byte) ((channel3 & 0xff00) >> 8);
			}
			else
			{
				output[0] = (byte) ((channel1 & 0xff00) >> 8);
				output[1] = (byte) ((channel1 & 0x00ff) >> 0);
				output[2] = (byte) ((channel2 & 0xff00) >> 8);
				output[3] = (byte) ((channel2 & 0x00ff) >> 0);
				output[4] = (byte) ((channel3 & 0xff00) >> 8);
				output[5] = (byte) ((channel3 & 0x00ff) >> 0);
			}
			
			return output;
		}
		
		byte[] getBytes(long[][] image, int compression, ByteOrder byteOrder, int headerBytes, boolean inStrips, FileInfo fi)
		{
			initializeFileInfo(fi,FileInfo.RGB48_PLANAR,compression,byteOrder,image.length,image[0].length);
			
			byte[] output = PixelArranger.arrangeAsPlanes(this, image, fi, inStrips, true);
			
			output = PixelArranger.attachHeader(fi,headerBytes,output);

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
			super("Gray12Unsigned",1,12,1);
		}
		
		boolean canDoImageCombo(int compression, ByteOrder byteOrder, int headerBytes, boolean stripped)
		{
			if (compression != FileInfo.COMPRESSION_NONE)
				return false;

			if (byteOrder == ByteOrder.INTEL)
				return false;
			
			if (stripped)
				return false;
			
			return true;
		}

		byte[] nativeBytes(long pix, ByteOrder byteOrder)
		{
			// since this format spans byte boundaries it cannot work with the basic model
			// see twelveBitEncoder() for an idea how the pixels are arranged
			
			return null;
		}
		
		byte[] getBytes(long[][] image, int compression, ByteOrder byteOrder, int headerBytes, boolean inStrips, FileInfo fi)
		{
			initializeFileInfo(fi,FileInfo.GRAY12_UNSIGNED,compression,byteOrder,image.length,image[0].length);
			
			byte[] output = TwelveBitEncoder.encode(image);
			
			// if (byteOrder == ByteOrder.INTEL)
			//	;  // nothing to do

			output = PixelArranger.attachHeader(fi,headerBytes,output);
			
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
			super("Gray24Unsigned",1,24,1);
		}
		
		boolean canDoImageCombo(int compression, ByteOrder byteOrder, int headerBytes, boolean stripped)
		{
			if (compression != FileInfo.COMPRESSION_NONE)
				return false;

			if (byteOrder == ByteOrder.INTEL)
				return false;
			
			if (stripped)
				return false;

			return true;
		}

		byte[] nativeBytes(long pix, ByteOrder byteOrder)
		{
			byte[] output = new byte[3];
			
			output[0] = (byte) ((pix & 0x0000ff) >> 0);
			output[1] = (byte) ((pix & 0x00ff00) >> 8);
			output[2] = (byte) ((pix & 0xff0000) >> 16);

			return output;
		}
		
		byte[] getBytes(long[][] image, int compression, ByteOrder byteOrder, int headerBytes, boolean inStrips, FileInfo fi)
		{
			initializeFileInfo(fi,FileInfo.GRAY24_UNSIGNED,compression,byteOrder,image.length,image[0].length);
			
			byte[] output;
			
			// ALWAYS arrange contiguously in this case
			output = PixelArranger.arrangeContiguously(this,image,fi);
			
			output = PixelArranger.attachHeader(fi,headerBytes,output);

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
		// run test on basic functionality for each pixel type
		//   these end up getting run twice but these next calls simplify debugging
		gray8Tester.runTest(BaseTestImage,FileInfo.COMPRESSION_NONE,ByteOrder.DEFAULT,0,false);
		color8Tester.runTest(BaseTestImage,FileInfo.COMPRESSION_NONE,ByteOrder.DEFAULT,0,false);
		gray16SignedTester.runTest(BaseTestImage,FileInfo.COMPRESSION_NONE,ByteOrder.DEFAULT,0,false);
		gray16UnsignedTester.runTest(BaseTestImage,FileInfo.COMPRESSION_NONE,ByteOrder.DEFAULT,0,false);
		gray32IntTester.runTest(BaseTestImage,FileInfo.COMPRESSION_NONE,ByteOrder.DEFAULT,0,false);
		gray32UnsignedTester.runTest(BaseTestImage,FileInfo.COMPRESSION_NONE,ByteOrder.DEFAULT,0,false);
		gray32FloatTester.runTest(BaseTestImage,FileInfo.COMPRESSION_NONE,ByteOrder.DEFAULT,0,false);
		gray64FloatTester.runTest(BaseTestImage,FileInfo.COMPRESSION_NONE,ByteOrder.DEFAULT,0,false);
		rgbTester.runTest(BaseTestImage,FileInfo.COMPRESSION_NONE,ByteOrder.DEFAULT,0,false);
		bgrTester.runTest(BaseTestImage,FileInfo.COMPRESSION_NONE,ByteOrder.DEFAULT,0,false);
		argbTester.runTest(BaseTestImage,FileInfo.COMPRESSION_NONE,ByteOrder.DEFAULT,0,false);
		abgrTester.runTest(BaseTestImage,FileInfo.COMPRESSION_NONE,ByteOrder.DEFAULT,0,false);
		bargTester.runTest(BaseTestImage,FileInfo.COMPRESSION_NONE,ByteOrder.DEFAULT,0,false);
		rgbPlanarTester.runTest(BaseTestImage,FileInfo.COMPRESSION_NONE,ByteOrder.DEFAULT,0,false);
		bitmapTester.runTest(BaseTestImage,FileInfo.COMPRESSION_NONE,ByteOrder.DEFAULT,0,false);
		rgb48Tester.runTest(BaseTestImage,FileInfo.COMPRESSION_NONE,ByteOrder.DEFAULT,0,false);
		rgb48PlanarTester.runTest(BaseTestImage,FileInfo.COMPRESSION_NONE,ByteOrder.DEFAULT,0,false);
		gray12UnsignedTester.runTest(BaseTestImage,FileInfo.COMPRESSION_NONE,ByteOrder.DEFAULT,0,false);
		gray24UnsignedTester.runTest(BaseTestImage,FileInfo.COMPRESSION_NONE,ByteOrder.DEFAULT,0,false);

		// now run all legal combos of input parameters
		for (FormatTester tester : Testers)
			for (long[][] image : Images)
				for (int compression : CompressionModes)
					for (ByteOrder byteOrder : ByteOrders)
						for (int headerOffset : HeaderOffsets)
							for (boolean stripped : EncodeAsStrips)
							{
/*
								System.out.println("tester("+tester.name()+") image("+image.length+"x"+image[0].length+") compress("+compression+") byteOrder("+byteOrder+") header("+headerOffset+") stripped("+stripped+")");
								if ((tester.name() == "Rgb48") && (image.length == 1) && (image[0].length == 1) && (compression == 1) &&
										(byteOrder == ByteOrder.INTEL) && (headerOffset == 0) && (stripped))
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
		
		assertEquals(rdr.min,0.0,FLOAT_TOL);
		assertEquals(rdr.max,0.0,FLOAT_TOL);

		rdr.min = 4000.0;
		rdr.max = 8888.7;
		
		assertEquals(rdr.min,4000.0,FLOAT_TOL);
		assertEquals(rdr.max,8888.7,FLOAT_TOL);
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
