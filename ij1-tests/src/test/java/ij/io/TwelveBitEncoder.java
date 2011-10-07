//
// TwelveBitEncoder.java
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

/**
 * TODO
 *
 * @author Barry DeZonia
 */
public class TwelveBitEncoder {

	TwelveBitEncoder() {}
	
	static public byte[] encode(long[][] inPix)
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

