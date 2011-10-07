//
// LzwDiffEncoder.java
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
public class LzwDiffEncoder {
	
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
		byte[] adjustedInput = differentiate(input, format, fi);

		byte[] output = LzwEncoder.encode(adjustedInput);

		return output;
	}
}

