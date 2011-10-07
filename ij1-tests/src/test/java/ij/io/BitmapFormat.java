//
// BitmapFormat.java
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
public class BitmapFormat extends PixelFormat {

	BitmapFormat()
	{
		super("Bitmap",1,1,1);  // super(String name, int numSamples, int bitsPerSample, int planes)
	}
	
	boolean canDoImageCombo(int compression, ByteOrder.Value byteOrder, int headerBytes, boolean stripped)
	{
		if (compression != FileInfo.COMPRESSION_NONE)
			return false;
		
		if (byteOrder == ByteOrder.Value.INTEL)
			return false;

		if (stripped)
			return false;
		
		return true;
	}

	byte[] nativeBytes(long pix, ByteOrder.Value byteOrder)
	{
		// since this is multiple pixels per byte the basic model does not fit
		// getBytes shows how pixels are arranged
		
		return null;
	}
	
	byte[] getBytes(long[][] image, int compression, ByteOrder.Value byteOrder, int headerBytes, boolean inStrips, FileInfo fi)
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

	Object pixelsFromBytes(byte[] bytes, ByteOrder.Value order)
	{
		// this method not tested by ImageWriter. Therefore no implementation until it will be used.
		return null;
	}
}

