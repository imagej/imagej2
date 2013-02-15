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

package imagej.updater.util;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * A helper class for the Checksummer.
 * 
 * When checksumming .properties files for the Updater, we would like to ignore
 * comments, because java.util.Properties writes the current date into a comment
 * when writing pom.properties and nar.properties files. This date is a
 * non-functional change which we would like to ignore when checking whether a
 * .jar file is up-to-date.
 * 
 * This class takes an InputStream that is expected to represent a .properties
 * file and offers an InputStream which skips the lines starting with a '#'.
 * 
 * @author Johannes Schindelin
 */
public class SkipHashedLines extends BufferedInputStream {
	protected boolean atLineStart;

	public SkipHashedLines(final InputStream in) {
		super(in, 1024);
		atLineStart = true;
	}

	@Override
	public int read() throws IOException {
		int ch = super.read();
		if (atLineStart) {
			if (ch == '#')
				while ((ch = read()) != '\n' && ch != -1)
					; // do nothing
			else
				atLineStart = false;
		}
		else if (ch == '\n')
			atLineStart = true;
		return ch;
	}

	@Override
	public int read(final byte[] b) throws IOException {
		return read(b, 0, b.length);
	}

	@Override
	public int read(final byte[] b, final int off, final int len) throws IOException {
		int count = 0;
		while (count < len) {
			int ch = read();
			if (ch < 0)
				return count == 0 ? -1 : count;
			b[off + count] = (byte)ch;
			count++;
		}
		return count;
	}

	@Override
	public long skip(final long n) throws IOException {
		throw new IOException("unsupported skip");
	}
}
