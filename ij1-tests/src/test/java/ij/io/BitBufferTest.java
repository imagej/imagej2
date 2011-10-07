//
// BitBufferTest.java
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

// ERROR conditions found in original code that are uncaught
// 1) getBits(99) on a 48 bit buffer just gives the 32 bits - no exception thrown, no overflow testing
// 2) BitBuffer(null) will generate an uncaught runtime exception

import static org.junit.Assert.*;

import org.junit.Test;

import ij.io.BitBuffer;
import ij.IJInfo;

public class BitBufferTest {
	private BitBuffer bits = null;

	private BitBuffer bitsFromBytes(byte[] bytes){
		return new BitBuffer(bytes);
	}

	@Test
	public void testBitBuffer() {

		// original IJ code for BitBuffer does not check for null array
		//   - later runtime errors possible
		if (IJInfo.RUN_ENHANCED_TESTS)
		{
			bits = bitsFromBytes(null);
			assertNotNull(bits);
		}
		
		bits = bitsFromBytes(new byte[]{1});
		assertNotNull(bits);
	}

	@Test
	public void testGetBits() {
	
		// can't test bitsFromBytes(null) followed by getBits() as original code would bomb
		// test against for now and support existing behavior but need to fix in code
		if (IJInfo.RUN_ENHANCED_TESTS)
		{
			bits = bitsFromBytes(null);
			assertEquals(0,bits.getBits(1));
		}

		// test if end of file works with empty buffer
		bits = bitsFromBytes(new byte[] {});

		assertEquals(0,bits.getBits(0));

		// see if EOF works for smallest case
		bits = bitsFromBytes(new byte[] {1});

		bits.getBits(8);
		assertEquals(-1,bits.getBits(1));

		// see if bits pulled out in order correctly
		//   far end : LSB
		bits = bitsFromBytes(new byte[] {1});

		assertEquals(0,bits.getBits(7));
		assertEquals(1,bits.getBits(1));
		assertEquals(-1,bits.getBits(1));

		// see if bits pulled out in order correctly
		//   near end : MSB
		bits = bitsFromBytes(new byte[] {(byte) (0xff & 128)});

		assertEquals(1,bits.getBits(1));
		assertEquals(0,bits.getBits(7));
		assertEquals(-1,bits.getBits(1));

		// see if pulling pieces of byte out of all 1's works
		bits = bitsFromBytes(new byte[] {(byte) (0xff & 255)});

		assertEquals(15,bits.getBits(4));
		assertEquals(3,bits.getBits(2));
		assertEquals(1,bits.getBits(1));
		assertEquals(1,bits.getBits(1));
		assertEquals(-1,bits.getBits(1));

		// see if pulling out multiple bytes in a row works
		bits = bitsFromBytes(new byte[] {1,2,3});

		assertEquals(1,bits.getBits(8));
		assertEquals(2,bits.getBits(8));
		assertEquals(3,bits.getBits(8));
		assertEquals(-1,bits.getBits(8));

		// see if pulling out more than 8 bits at a time work
		bits = bitsFromBytes(new byte[] {1,3});

		assertEquals(259,bits.getBits(16));
		assertEquals(-1,bits.getBits(1));
		
		// test if seeking past end of file works
		bits = bitsFromBytes(new byte[] {1,1,1,1});

		assertEquals(16843009,bits.getBits(55));  // this behavior is questionable: 55 bits asked for and 32 returned

		// test what happens when we overflow an int and enough data is present
		bits = bitsFromBytes(new byte[] {(byte)(0xff & 255),(byte)(0xff & 255),(byte)(0xff & 255),(byte)(0xff & 255),(byte)(0xff & 255)});
		
		// have to supply IJ's current return value for now as code will only return 32 bits correctly and then -1 beyond that
		assertEquals(-1,bits.getBits(33));
	}

}
