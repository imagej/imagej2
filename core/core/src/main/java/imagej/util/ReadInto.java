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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;

/**
 * This class takes an {@link InputStream} and either accumulates the read bytes
 * in a {@link String} or outputs to a {@link PrintStream}.
 * <p>
 * Its intended use is to catch the output and error streams of {@link Process}
 * instances.
 * </p>
 * 
 * @author Johannes Schindelin
 */
public class ReadInto extends Thread {

	protected BufferedReader reader;
	protected PrintStream out;
	protected StringBuilder buffer = new StringBuilder();
	protected boolean done;

	/**
	 * Construct a ReadInto thread and start it right away.
	 * 
	 * @param in the stream to read
	 * @param out the stream to print to; if it is null, the {@link #toString()}
	 *          method will have the output instead
	 */
	public ReadInto(final InputStream in, final PrintStream out) {
		reader = new BufferedReader(new InputStreamReader(in));
		this.out = out;
		start();
	}

	/**
	 * The main method.
	 * <p>
	 * It runs until interrupted, or until the {@link InputStream} ends, whichever
	 * comes first.
	 * </p>
	 */
	@Override
	public void run() {
		try {
			for (;;) {
				while (!reader.ready()) {
					if (done) return;
					Thread.sleep(500);
				}
				final String line = reader.readLine();
				if (line == null) break;
				if (out != null) out.println(line);
				else buffer.append(line).append("\n");
				Thread.sleep(0);
			}
		}
		catch (final InterruptedException e) { /* just stop */}
		catch (final IOException e) { /* just stop */}
		try {
			reader.close();
		}
		catch (final IOException e) { /* just stop */}
	}

	public void done() {
		done = true;
	}

	/**
	 * Return the output as a {@link String} unless a {@link PrintStream} was
	 * specified in the constructor.
	 */
	@Override
	public String toString() {
		if (out != null) return "ReadInto " + out;
		return buffer.toString();
	}
}
