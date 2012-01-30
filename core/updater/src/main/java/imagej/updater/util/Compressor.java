//
// Compressor.java
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

package imagej.updater.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/*
 * Main role is to compress/decompress file data.
 * Utility to get entire byte data of a file from InputStream exists too.
 */
public class Compressor {

	// Decompress a file
	public static byte[] decompress(final InputStream in) throws IOException {
		final GZIPInputStream gzipInputStream = new GZIPInputStream(in);
		final ByteArrayOutputStream bout = new ByteArrayOutputStream(65536);
		int data;
		while ((data = gzipInputStream.read()) != -1) {
			bout.write(data);
		}
		gzipInputStream.close();
		bout.close();
		return bout.toByteArray();
	}

	// Takes in file's data, compress, and save to destination
	public static void compressAndSave(final byte[] data, final OutputStream out)
		throws IOException
	{
		final GZIPOutputStream dout = new GZIPOutputStream(out);
		dout.write(data);
		dout.close();
	}

	// Get entire byte data
	public static byte[] readStream(final InputStream input) throws IOException {
		byte[] buffer = new byte[1024];
		int offset = 0, len = 0;
		for (;;) {
			if (offset == buffer.length) buffer = realloc(buffer, 2 * buffer.length);
			len = input.read(buffer, offset, buffer.length - offset);
			if (len < 0) return realloc(buffer, offset);
			offset += len;
		}
	}

	private static byte[] realloc(final byte[] buffer, final int newLength) {
		if (newLength == buffer.length) return buffer;
		final byte[] newBuffer = new byte[newLength];
		System.arraycopy(buffer, 0, newBuffer, 0, Math
			.min(newLength, buffer.length));
		return newBuffer;
	}
}
