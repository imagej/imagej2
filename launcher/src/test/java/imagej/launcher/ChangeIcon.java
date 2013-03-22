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

package imagej.launcher;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;

import org.scijava.util.ProcessUtils;

/**
 * This class attaches an icon to a Windows version of the ImageJ launcher.
 * 
 * It returns gracefully if there is no such launcher.
 * 
 * @author Johannes Schindelin
 */
public class ChangeIcon {
	private static void usage() {
		System.err.println("Usage: ChangeIcon (ImageJ-win32.exe|ImageJ-win64.exe) ImageJ.ico");
		System.exit(1);
	}

	public static void main(final String... args) {
		if (args.length != 2)
			usage();

		final File executable = new File(args[0]);
		for (final File file : new File[] { executable, new File(args[1]) }) {
			if (!file.exists()) {
				System.err.println("Cannot find '" + file + "'; not changing icon");
				return; // fail gracefully
			}
		}

		try {
			// copy the executable
			final File debug = new File(executable.getParentFile(), "debug.exe");
			if (!debug.exists())
				debug.createNewFile();
			final FileChannel source = new FileInputStream(executable).getChannel();
			final FileChannel target = new FileOutputStream(debug).getChannel();
			target.transferFrom(source, 0, source.size());
			source.close();
			target.close();

			// call a copy to change the icon (needs to be a copy due to Windows' locking)
			ProcessUtils.exec(null, System.err, System.out, debug.getPath(),
				"--set-icon", executable.getPath(), args[1]);

			System.err.println("Changed icon of '" + executable + "' to '" + args[1] + "'");
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}
	}
}
