//
// PixelFormat.java
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
public abstract class PixelFormat {
	
	private String name;  // might be useful for debugging purposes. otherwise could be an interface
	private int numSamples;
	private int bitsPerSample;
	private int planes;
	
	public int numSamples()    { return numSamples; }
	public int bitsPerSample() { return bitsPerSample; }
	public int planes()        { return planes; }
	public String name()       { return name; }
	
	
	abstract boolean canDoImageCombo(int compression, ByteOrder.Value byteOrder, int headerBytes, boolean stripped);
	abstract byte[] nativeBytes(long pixel, ByteOrder.Value byteOrder);
	abstract byte[] getBytes(long[][] image, int compression, ByteOrder.Value byteOrder, int headerBytes, boolean inStrips, FileInfo fi);
	abstract Object expectedResults(long[][] inputImage);
	abstract Object pixelsFromBytes(byte[] bytes, ByteOrder.Value byteOrder);
	
	PixelFormat(String name, int numSamples, int bitsPerSample, int planes)
	{
		this.name = name;
		this.numSamples = numSamples;
		this.bitsPerSample = bitsPerSample;
		this.planes = planes;
	}
	
	// since each format calls this method I've located it in PixelFormat. But it could be a static method somewhere else
	
	protected void initializeFileInfo(FileInfo fi, int ftype, int compression, ByteOrder.Value byteOrder, int rows, int cols)
	{
		fi.fileType = ftype;
		fi.compression = compression;
		if (byteOrder == ByteOrder.Value.INTEL)
			fi.intelByteOrder = true;
		else
			fi.intelByteOrder = false;
		fi.height = rows;
		fi.width = cols;
	}
	
}
