/*
 * #%L
 * ImageJ software for multidimensional image processing and analysis.
 * %%
 * Copyright (C) 2009 - 2014 Board of Regents of the University of
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

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.Set;

/**
 * A helper class for the Checksummer.
 * 
 * When checksumming manifests for the Updater, we would like to ignore
 * certain entries such as Implementation-Build because they do not provide
 * substantial changes and are likely to differ between builds even if the
 * real content does not.
 * 
 * This class takes an InputStream that is expected to represent a manifest
 * and offers an InputStream which skips the mentioned entries.
 * 
 * @author Johannes Schindelin
 */
public class FilterManifest extends ByteArrayInputStream {
	protected static enum Skip {
		ARCHIVER_VERSION,
		BUILT_BY,
		BUILD_JDK,
		CLASS_PATH,
		CREATED_BY,
		IMPLEMENTATION_BUILD
	}
	protected static Set<String> skip = new HashSet<String>();
	static {
		for (Skip s : Skip.values())
			skip.add(s.toString().toUpperCase().replace('_', '-'));
	}

	public FilterManifest(final InputStream in) throws IOException {
		this(in, true);
	}

	public FilterManifest(final InputStream in, boolean keepOnlyMainClass) throws IOException {
		super(filter(in, keepOnlyMainClass));
	}

	protected static byte[] filter(final InputStream in, boolean keepOnlyMainClass) throws IOException {
		return filter(new BufferedReader(new InputStreamReader(in)), keepOnlyMainClass);
	}

	protected static byte[] filter(final BufferedReader reader, boolean keepOnlyMainClass) throws IOException {
		StringBuilder builder = new StringBuilder();
		for (;;) {
			String line = reader.readLine();
			if (line == null) {
				break;
			}
			if (keepOnlyMainClass) {
				if (line.toUpperCase().startsWith("Main-Class:")) builder.append(line).append('\n');
				continue;
			}
			int colon = line.indexOf(':');
			if (colon > 0 && skip.contains(line.substring(0, colon).toUpperCase())) {
				continue;
			}
			builder.append(line).append('\n');
		}
		reader.close();
		return builder.toString().getBytes();
	}
}
