/*
 * #%L
 * ImageJ software for multidimensional image processing and analysis.
 * %%
 * Copyright (C) 2009 - 2013 Board of Regents of the University of
 * Wisconsin-Madison, Broad Institute of MIT and Harvard, and Max Planck
 * Institute of Molecular Cell Biology and Genetics.
 * %%
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * 
 * The views and conclusions contained in the software and documentation are
 * those of the authors and should not be interpreted as representing official
 * policies, either expressed or implied, of any organization.
 * #L%
 */

package imagej.util;

import java.io.IOException;
import java.io.OutputStream;

/**
 * This class hides a line-based output behind an {@link OutputStream}.
 * <p>
 * Only the abstract method {@link #println(String)} needs to be overridden; the
 * other methods in {@link OutputStream} are overridden in this class already to
 * buffer lines until they are complete and then flush them.
 * </p>
 * 
 * @author Johannes Schindelin
 */
public abstract class LineOutputStream extends OutputStream {

	public byte[] buffer = new byte[16384];
	public int len;

	/**
	 * This method is all that needs to be implemented.
	 * 
	 * @param line the line to print
	 */
	public abstract void println(String line) throws IOException;

	/**
	 * Adds a single byte to the current line buffer, or {@link #flush()} the
	 * current line if it is a new-line character.
	 * 
	 * @param b the byte to write
	 */
	@Override
	public synchronized void write(final int b) throws IOException {
		ensure(len + 1);
		buffer[len++] = (byte) b;
		if (b == '\n') flush();
	}

	/**
	 * Adds bytes to the current line buffer. Whenever a new-line character is
	 * encountered, {@link #flush()} the current line buffer.
	 * 
	 * @param buf the bytes to write
	 */
	@Override
	public synchronized void write(final byte[] buf) throws IOException {
		write(buf, 0, buf.length);
	}

	/**
	 * Adds bytes to the current line buffer. Whenever a new-line character is
	 * encountered, {@link #flush()} the current line buffer.
	 * 
	 * @param buf the bytes to write
	 * @param offset the offset into the buffer
	 * @param length how many bytes to add
	 */
	@Override
	public synchronized void write(final byte[] buf, int offset, int length) throws IOException {
		int eol = length;
		while (eol > 0) {
			if (buf[eol - 1] == '\n') break;
			eol--;
		}
		if (eol >= 0) {
			ensure(len + eol);
			System.arraycopy(buf, offset, this.buffer, len, eol);
			len += eol;
			flush();
			length -= eol;
			if (length == 0) return;
			offset += eol;
		}
		ensure(len + length);
		System.arraycopy(buf, offset, this.buffer, len, length);
		len += length;
	}

	/** Flushes the current line buffer if any bytes are left in it. */
	@Override
	public void close() throws IOException {
		flush();
	}

	/**
	 * If any bytes are in the current line buffer, output the line via
	 * {@link #println(String)}, stripping any trailing new-line characters.
	 */
	@Override
	public synchronized void flush() throws IOException {
		if (len > 0) {
			if (buffer[len - 1] == '\n') len--;
			println(new String(buffer, 0, len));
		}
		len = 0;
	}

	/**
	 * Increases the size of the line buffer if necessary.
	 * 
	 * @param length the required minimal length
	 */
	protected synchronized void ensure(final int length) {
		if (buffer.length >= length) return;

		int newLength = buffer.length * 3 / 2;
		if (newLength < length) newLength = length + 16;
		final byte[] newBuffer = new byte[newLength];
		System.arraycopy(buffer, 0, newBuffer, 0, len);
		buffer = newBuffer;
	}

}
