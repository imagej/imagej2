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

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Arrays;

/**
 * Useful methods for working with processes.
 * 
 * @author Johannes Schindelin
 */
public final class ProcessUtils {

	private ProcessUtils() {
		// prevent instantiation of utility class
	}

	/**
	 * Executes a program. This is a convenience method mainly to be able to catch
	 * the output of programs as shell scripts can do by wrapping calls in $(...).
	 * 
	 * @param workingDirectory the directory in which to execute the program
	 * @param err the {@link PrintStream} to print the program's error stream to;
	 *          if null is passed, the error goes straight to Nirvana (not the
	 *          band, though).
	 * @param out the {@link PrintStream} to print the program's output to; if
	 *          null is passed, the output will be accumulated into a
	 *          {@link String} and returned upon exit.
	 * @param args the command-line to execute, split into components
	 * @return the output of the program if {@code out} is null, otherwise an
	 *         empty {@link String}
	 * @throws RuntimeException if interrupted or the program failed to execute
	 *           successfully.
	 */
	public static String exec(final File workingDirectory,
		final PrintStream err, final PrintStream out, final String... args)
	{
		try {
			final Process process =
				Runtime.getRuntime().exec(args, null, workingDirectory);
			process.getOutputStream().close();
			final ReadInto errThread = new ReadInto(process.getErrorStream(), err);
			final ReadInto outThread = new ReadInto(process.getInputStream(), out);
			try {
				process.waitFor();
				errThread.done();
				errThread.join();
				outThread.done();
				outThread.join();
			}
			catch (final InterruptedException e) {
				process.destroy();
				errThread.interrupt();
				outThread.interrupt();
				err.println("Interrupted!");
				throw new RuntimeException(e);
			}
			if (process.exitValue() != 0) {
				throw new RuntimeException("exit status " + process.exitValue() +
					": " + Arrays.toString(args) + "\n" + err);
			}
			return outThread.toString();
		}
		catch (final IOException e) {
			throw new RuntimeException(e);
		}
	}

}
