//
// Gray64FloatFormat.java
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

class Gray64FloatFormat extends PixelFormat
{
	Gray64FloatFormat()
	{
		super("Gray64Float",1,64,1);  // super(String name, int numSamples, int bitsPerSample, int planes)
	}
	
	boolean canDoImageCombo(int compression, ByteOrder.Value byteOrder, int headerBytes, boolean stripped)
	{
		if (compression != FileInfo.COMPRESSION_NONE)
			return false;
	
		if (stripped)
			return false;
		
		return true;
	}
	
	byte[] nativeBytes(long pix, ByteOrder.Value byteOrder)
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
		
		if (byteOrder == ByteOrder.Value.INTEL)
			PixelArranger.reverse(output);

		return output;
	}
	
	byte[] getBytes(long[][] image, int compression, ByteOrder.Value byteOrder, int headerBytes, boolean inStrips, FileInfo fi)
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

	Object pixelsFromBytes(byte[] bytes, ByteOrder.Value order)
	{
		// this method not tested by ImageWriter. Therefore no implementation until it will be used.
		return null;
	}
}

