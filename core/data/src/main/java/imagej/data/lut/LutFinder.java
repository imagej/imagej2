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

package imagej.data.lut;

import imagej.util.AppUtils;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import org.scijava.util.FileUtils;
import org.scijava.util.IteratorPlus;

/**
 * The LutFinder determines the locations of all .lut files known to ImageJ.
 * 
 * @author Barry DeZonia
 */
public class LutFinder {

	private final static Pattern lutsPattern = Pattern.compile(".*\\.lut$");
	private static final File LUT_DIRECTORY;

	static {
		final File appBaseDirectory = AppUtils.getBaseDirectory();
		LUT_DIRECTORY = appBaseDirectory == null ?
				null : new File(appBaseDirectory, "luts");
	}

	/**
	 * Finds the {@link URL}s of the .lut files known to ImageJ. .lut files can
	 * reside in the standard Jar file or in the luts subdirectory of the
	 * application.
	 * 
	 * @return A collection of URLs referencing the known .lut files
	 */
	public Map<String, URL> findLuts() {
		final HashMap<String, URL> result = new HashMap<String, URL>();
		try {
			for (final URL jarURL : getJarURLs()) {
				getLuts(result, jarURL);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		// do file luts second: user can thus override jar luts if desired
		final URL dirURL = getDirectoryURL();
		if (dirURL != null) getLuts(result, dirURL);
		return result;
	}

	// -- private helpers --

	private Iterable<URL> getJarURLs() throws IOException {
		return new IteratorPlus<URL>(getClass().getClassLoader().getResources("luts/"));
	}

	private URL getDirectoryURL() {
		if (LUT_DIRECTORY == null) return null;
		try {
			return LUT_DIRECTORY.toURI().toURL();
		}
		catch (MalformedURLException e) {
			return null;
		}
	}

	private void getLuts(final Map<String, URL> result, final URL base) {
		final String prefix = base.toString();
		for (final URL url : FileUtils.listContents(base)) {
			final String string = url.toString();
			if (!string.startsWith(prefix)) continue;
			if (lutsPattern.matcher(string).matches()) result.put(string.substring(prefix.length()), url);
		}
	}

}
