//
// PackbitsEncoder.java
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

// A real PackBitsEncoder is needed to test packbits compression in ImageReader::readPixels()

/**
 * TODO
 *
 * @author Barry DeZonia
 */
public class PackbitsEncoder {
	
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
	
	static void runTests()
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
