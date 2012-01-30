
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
