//
// PixelArranger.java
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
public class PixelArranger {
	
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
		ByteOrder.Value myByteOrder = ByteOrder.Value.DEFAULT;
		if (fi.intelByteOrder)
			myByteOrder = ByteOrder.Value.INTEL;
		
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
		ByteOrder.Value myByteOrder = ByteOrder.Value.DEFAULT;
		if (fi.intelByteOrder)
			myByteOrder = ByteOrder.Value.INTEL;
		
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
		ByteOrder.Value myByteOrder = ByteOrder.Value.DEFAULT;
		if (fi.intelByteOrder)
			myByteOrder = ByteOrder.Value.INTEL;

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

