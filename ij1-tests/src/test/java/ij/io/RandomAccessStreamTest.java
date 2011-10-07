//
// RandomAccessStreamTest.java
//

/*
ImageJ software for multidimensional image processing and analysis.

Copyright (c) 2010, ImageJDev.org.
All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met:
    * Redistributions of source code must retain the above copyright
      notice, this list of conditions and the following disclaimer.
    * Redistributions in binary form must reproduce the above copyright
      notice, this list of conditions and the following disclaimer in the
      documentation and/or other materials provided with the distribution.
    * Neither the names of the ImageJDev.org developers nor the
      names of its contributors may be used to endorse or promote products
      derived from this software without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
POSSIBILITY OF SUCH DAMAGE.
*/

package ij.io;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import ij.Assert;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.io.RandomAccessFile;

import org.junit.Test;

/**
 * Unit tests for {@link RandomAccessStream}.
 *
 * @author Barry DeZonia
 */
public class RandomAccessStreamTest {

	private RandomAccessStream stream;
	
	private byte[] TEST_BYTES = new byte[]{1,4,2,6,7,3,4,0,7,9,9,9,2,1,4,5,3,8,8,6,1,2,3,4};

	// **** helper methods **********************************************************************************
	
	
	private RandomAccessStream newStream()
	{
		InputStream byteStream = new ByteArrayInputStream(TEST_BYTES);
		return new RandomAccessStream(byteStream);
	}
	
	private byte[] subset(byte[] inBytes, int offset, int len)
	{
		byte[] outBytes = new byte[len];
		for (int i = 0; i < len; i++)
			outBytes[i] = inBytes[offset+i];
		return outBytes;
	}
	
	private long[] longsFromBytes(byte[] bytes, int numBytesPerValue)
	{
		int numToMake = bytes.length / numBytesPerValue;
		
		long[] output = new long[numToMake];
		
		for (int o = 0; o < numToMake; o++)
		{
			int base = o*numBytesPerValue;
			
			long val = 0;
			
			for (int offset = 0; offset < numBytesPerValue; offset++)
				val = (val << 8) | bytes[base+offset];
			
			output[o] = val;
		}
		
		return output;
	}
	
	// **** helper tests **********************************************************************************
	
	private void tryReadFullyXBytes(int numToRead, boolean expectSuccess)
	{
		// create default stream
		stream = newStream();
		assertNotNull(stream);

		try {
			byte[] bytes = new byte[numToRead];
			stream.readFully(bytes);
			if (expectSuccess)
			{
				for (int i = 0; i < bytes.length; i++)
					assertEquals(TEST_BYTES[i],bytes[i]);
			}
			else
			{
				// readFully() fails silently when output array is larger than input data
				// can't fail here as one would expect but test the max bytes possibly read
				for (int i = 0; i < bytes.length && i < TEST_BYTES.length; i++)
					assertEquals(TEST_BYTES[i],bytes[i]);
			}
		} catch(Exception e) {
			if (expectSuccess)
				fail();
			else
				assertTrue(true);
		}
	}
	
	private void tryReadFullyXBytes(int buffSize, int numToRead, boolean expectSuccess)
	{
		// create default stream
		stream = newStream();
		assertNotNull(stream);

		try {
			byte[] bytes = new byte[buffSize];
			stream.readFully(bytes,numToRead);
			if (expectSuccess)
			{
				for (int i = 0; i < numToRead; i++)
					assertEquals(TEST_BYTES[i],bytes[i]);
			}
			else
			{
				// readFully() fails silently when output array is larger than input data
				// can't fail here as one would expect but test the max bytes possibly read
				for (int i = 0; i < numToRead && i < TEST_BYTES.length; i++)
					assertEquals(TEST_BYTES[i],bytes[i]);
			}
		} catch(Exception e) {
			if (expectSuccess)
				fail();
			else
				assertTrue(true);
		}
	}
	
	// **** main tests **********************************************************************************

	@Test
	public void testRandomAccessStreamInputStream() {
		stream = newStream();
		assertNotNull(stream);
	}

	@Test
	public void testRandomAccessStreamRandomAccessFile() {
		
		try {
			File file = File.createTempFile("blah","blah");
			file.deleteOnExit();
			RandomAccessFile randFile = new RandomAccessFile(file, "r");
			
			stream = new RandomAccessStream(randFile);
			assertNotNull(stream);
			
		} catch(Exception e) {
			fail();
		}
	}

	@Test
	public void testClose() {
	
		// create default stream
		stream = newStream();
		assertNotNull(stream);

		// read from stream and then close
		try {
			short s = stream.readShort();
			assertEquals(260,s);
			
			stream.close();
			
		} catch (Exception e) {
			fail();
		}
		
		// try to read again - should throw an exception
		try {
			stream.readShort();
			fail();
		} catch (Exception e) {
			assertTrue(true);
		}
	}

	@Test
	public void testGetFilePointer() {
		// create default stream
		stream = newStream();
		assertNotNull(stream);
		
		try {
			// do a number of reads and test file pointer
			assertEquals(0,stream.getFilePointer());
			stream.readShort();
			assertEquals(2,stream.getFilePointer());
			stream.readInt();
			assertEquals(6,stream.getFilePointer());
			stream.readLong();
			assertEquals(14,stream.getFilePointer());
			
		} catch (Exception e) {
			fail();
		}
	}

	@Test
	public void testGetLongFilePointer() {
		// create default stream
		stream = newStream();
		assertNotNull(stream);
		
		try {
			// do a number of reads and test file pointer
			assertEquals(0,stream.getLongFilePointer());
			stream.readLong();
			assertEquals(8,stream.getLongFilePointer());
			stream.readInt();
			assertEquals(12,stream.getLongFilePointer());
			stream.readShort();
			assertEquals(14,stream.getLongFilePointer());
			
		} catch (Exception e) {
			fail();
		}
		
		// can't really test inputstreams > size of int
	}

	@Test
	public void testSeekLong() {
		// create default stream
		stream = newStream();
		assertNotNull(stream);
		
		// do a couple seek longs and read data
		try {
			stream.seek((long)0);
			assertEquals(260,stream.readShort());
			stream.seek((long)1);
			assertEquals(1026,stream.readShort());
			stream.seek((long)TEST_BYTES.length-2);  // last bytes in file
			assertEquals(772,stream.readShort());
		} catch (Exception e) {
			fail();
		}
		
		// seek before beginning
		try {
			stream.seek((long)-1);
			assertEquals(0,stream.getLongFilePointer());
			assertEquals(260,stream.readShort());
		} catch (Exception e) {
			fail();
		}
		
		// seek after end
		try {
			stream.seek((long)TEST_BYTES.length);
			assertEquals(TEST_BYTES.length,stream.getLongFilePointer());
			assertEquals(-1,stream.read());
		} catch (Exception e) {
			fail();
		}
	}

	@Test
	public void testSeekInt() {
		// create default stream
		stream = newStream();
		assertNotNull(stream);
		
		// do a couple seek longs and read data
		try {
			stream.seek(0);
			assertEquals(260,stream.readShort());
			stream.seek(1);
			assertEquals(1026,stream.readShort());
			stream.seek(TEST_BYTES.length-2);  // last bytes in file
			assertEquals(772,stream.readShort());
		} catch (Exception e) {
			fail();
		}
		
		// seek before beginning
		try {
			stream.seek(-1);
			assertEquals(-1,stream.getFilePointer());
			assertEquals(-1,stream.read());
		} catch (Exception e) {
			fail();
		}
		/*
		*/
		
		// seek after end
		try {
			stream.seek(TEST_BYTES.length);
			assertEquals(TEST_BYTES.length,stream.getFilePointer());
			assertEquals(-1,stream.read());
		} catch (Exception e) {
			fail();
		}
	}

	@Test
	public void testRead() {
		// create default stream
		stream = newStream();
		assertNotNull(stream);
		
		long[] expectedData = longsFromBytes(TEST_BYTES,1);
		
		// read as much as possible
		try {
			for (int i = 0; i < expectedData.length; i++)
				assertEquals(expectedData[i],stream.read());
		} catch (Exception e) {
			fail();
		}
		
		// read beyond end
		try {
			assertEquals(-1,stream.read());
		} catch (Exception e) {
			fail();
		}
	}

	@Test
	public void testReadShort() {
		// create default stream
		stream = newStream();
		assertNotNull(stream);
		
		long[] expectedData = longsFromBytes(TEST_BYTES,2);

		// read as much as possible
		try {
			for (int i = 0; i < expectedData.length; i++)
				assertEquals(expectedData[i],stream.readShort());
		} catch (Exception e) {
			fail();
		}
		
		// read beyond end
		try {
			stream.readShort();
			fail();
		} catch (Exception e) {
			assertTrue(true);
		}
	}

	@Test
	public void testReadInt() {
		// create default stream
		stream = newStream();
		assertNotNull(stream);
		
		long[] expectedData = longsFromBytes(TEST_BYTES,4);

		// read as much as possible
		try {
			for (int i = 0; i < expectedData.length; i++)
				assertEquals(expectedData[i],stream.readInt());
		} catch (Exception e) {
			fail();
		}
		
		// read beyond end
		try {
			stream.readInt();
			fail();
		} catch (Exception e) {
			assertTrue(true);
		}
	}

	@Test
	public void testReadLong() {
		// create default stream
		stream = newStream();
		assertNotNull(stream);
		
		long[] expectedData = longsFromBytes(TEST_BYTES,8);

		// read as much as possible
		try {
			for (int i = 0; i < expectedData.length; i++)
				assertEquals(expectedData[i],stream.readLong());
		} catch (Exception e) {
			fail();
		}
		
		// read beyond end
		try {
			stream.readLong();
			fail();
		} catch (Exception e) {
			assertTrue(true);
		}
	}

	@Test
	public void testReadDouble() {

		// setup test data
		double[] expectedData = new double[] {1,5,3,8,7,0,3,5,1};

		byte[] bytes = new byte[expectedData.length*8];
		
		for (int i = 0; i < expectedData.length; i++)
		{
			long tmp = Double.doubleToLongBits(expectedData[i]);
			bytes[8*i+0] = (byte)((tmp & 0xff00000000000000L) >> 56);
			bytes[8*i+1] = (byte)((tmp & 0xff000000000000L) >> 48);
			bytes[8*i+2] = (byte)((tmp & 0xff0000000000L) >> 40);
			bytes[8*i+3] = (byte)((tmp & 0xff00000000L) >> 32);
			bytes[8*i+4] = (byte)((tmp & 0xff000000L) >> 24);
			bytes[8*i+5] = (byte)((tmp & 0xff0000L) >> 16);
			bytes[8*i+6] = (byte)((tmp & 0xff00L) >> 8);
			bytes[8*i+7] = (byte)((tmp & 0xffL) >> 0);
		}
		
		InputStream byteStream = new ByteArrayInputStream(bytes);

		// create a stream on it
		stream = new RandomAccessStream(byteStream);
		assertNotNull(stream);
		
		// read as much as possible
		try {
			for (int i = 0; i < expectedData.length; i++)
				assertEquals(expectedData[i],stream.readDouble(),Assert.DOUBLE_TOL);
		} catch (Exception e) {
			fail();
		}
		
		// read beyond end
		try {
			stream.readDouble();
			fail();
		} catch (Exception e) {
			assertTrue(true);
		}
	}

	@Test
	public void testReadFloat() {
		
		// setup test data
		float[] expectedData = new float[] {1,5,3,8,7,0,3,5,1};

		byte[] bytes = new byte[expectedData.length*4];
		
		for (int i = 0; i < expectedData.length; i++)
		{
			long tmp = Float.floatToIntBits(expectedData[i]);
			bytes[4*i+0] = (byte)((tmp & 0xff000000L) >> 24);
			bytes[4*i+1] = (byte)((tmp & 0xff0000L) >> 16);
			bytes[4*i+2] = (byte)((tmp & 0xff00L) >> 8);
			bytes[4*i+3] = (byte)((tmp & 0xffL) >> 0);
		}
		
		InputStream byteStream = new ByteArrayInputStream(bytes);
		
		// create stream on it
		stream = new RandomAccessStream(byteStream);
		assertNotNull(stream);
		
		// read as much as possible
		try {
			for (int i = 0; i < expectedData.length; i++)
				assertEquals(expectedData[i],stream.readFloat(),Assert.FLOAT_TOL);
		} catch (Exception e) {
			fail();
		}
		
		// read beyond end
		try {
			stream.readFloat();
			fail();
		} catch (Exception e) {
			assertTrue(true);
		}
	}

	@Test
	public void testReadFullyByteArray() {
		boolean ExpectSuccess = true;
		boolean ExpectFailure = false;
		tryReadFullyXBytes(0,ExpectSuccess);  // read none
		tryReadFullyXBytes(1,ExpectSuccess);  // read one
		tryReadFullyXBytes(TEST_BYTES.length-1,ExpectSuccess);  // read all but one
		tryReadFullyXBytes(TEST_BYTES.length,ExpectSuccess);  // full read of input
		tryReadFullyXBytes(TEST_BYTES.length+1,ExpectFailure);  // try to read more bytes than exist in input array
	}

	@Test
	public void testReadFullyByteArrayInt() {
		boolean ExpectSuccess = true;
		boolean ExpectFailure = false;
		tryReadFullyXBytes(0,0,ExpectSuccess);  // read none
		tryReadFullyXBytes(1,1,ExpectSuccess);  // read one
		tryReadFullyXBytes(TEST_BYTES.length,TEST_BYTES.length-1,ExpectSuccess);  // read all but one
		tryReadFullyXBytes(TEST_BYTES.length,TEST_BYTES.length,ExpectSuccess);  // full read of input
		tryReadFullyXBytes(TEST_BYTES.length,TEST_BYTES.length+1,ExpectFailure);  // try to read more bytes than exist in input array
	}

	@Test
	public void testReadByteArrayIntInt() {
		byte[] bytes;
		int offset, len;

		// try null bytes
		
		bytes = null;
		offset = 0;
		len = 1;
		
		stream = newStream();
		try {
			stream.read(bytes, offset, len);
			fail();
		} catch (NullPointerException e) {
			assertTrue(true);
		} catch (Exception e) {
			fail();
		}

		// try negative offset
		
		bytes = new byte[5];
		offset = -1;
		len = 5;
		
		stream = newStream();
		try {
			stream.read(bytes, offset, len);
			fail();
		} catch (IndexOutOfBoundsException e) {
			assertTrue(true);
		} catch (Exception e) {
			fail();
		}

		// try negative len
		
		bytes = new byte[5];
		offset = 0;
		len = -1;
		
		stream = newStream();
		try {
			stream.read(bytes, offset, len);
			fail();
		} catch (IndexOutOfBoundsException e) {
			assertTrue(true);
		} catch (Exception e) {
			fail();
		}

		// try offset and len together too large
		
		bytes = new byte[5];
		offset = 3;
		len = 3;
		
		stream = newStream();
		try {
			stream.read(bytes, offset, len);
			fail();
		} catch (IndexOutOfBoundsException e) {
			assertTrue(true);
		} catch (Exception e) {
			fail();
		}
		
		// try len == 0
		
		bytes = new byte[5];
		offset = 3;
		len = 0;
		
		stream = newStream();
		try {
			assertEquals(0,stream.read(bytes, offset, len));
		} catch (Exception e) {
			fail();
		}

		// try valid params - full read
		bytes = new byte[TEST_BYTES.length];
		offset = 0;
		len = TEST_BYTES.length;

		stream = newStream();
		try {
			assertEquals(len,stream.read(bytes, offset, len));
			assertArrayEquals(TEST_BYTES,bytes);
		} catch (Exception e) {
			fail();
		}

		// try valid params : len and offset not default values
		bytes = new byte[7];
		offset = 1;
		len = 5;
		byte[] values = subset(TEST_BYTES,0,len);
		byte[] expectedOutput = new byte[bytes.length];
		System.arraycopy(values, 0, expectedOutput, offset, len);

		stream = newStream();
		try {
			assertEquals(len,stream.read(bytes, offset, len));
			assertArrayEquals(expectedOutput,bytes);
		} catch (Exception e) {
			fail();
		}
	}

}
