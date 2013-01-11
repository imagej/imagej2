/*
 * #%L
 * ImageJ software for multidimensional image processing and analysis.
 * %%
 * Copyright (C) 2009 - 2012 Board of Regents of the University of
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
import imagej.util.ClassUtils;
import imagej.util.FileUtils;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarException;
import java.util.jar.JarFile;
import java.util.regex.Pattern;

/**
 * The LutFinder determines the locations of all .lut files known to ImageJ.
 * 
 * @author Barry DeZonia
 */
public class LutFinder {

	public static final String LUT_DIRECTORY = AppUtils.getBaseDirectory() +
		File.separator + "luts";

	/**
	 * Finds the {@link URL}s of the .lut files known to ImageJ. .lut files can
	 * reside in the standard Jar file or in the luts subdirectory of the
	 * application.
	 * 
	 * @return A collection of URLs referencing the known .lut files
	 */
	public Collection<URL> findLuts() {
		URL jarURL = getJarURL();
		URL dirURL = getDirectoryURL();
		Collection<URL> jarLutURLs = getLuts(jarURL);
		Collection<URL> dirLutURLs = getLuts(dirURL);
		HashMap<String, URL> combined = new HashMap<String, URL>();
		// do jar luts first
		putAll(jarLutURLs, combined);
		// do file luts second: user can thus override jar luts if desired
		putAll(dirLutURLs, combined);
		return combined.values();
	}

	private URL getDirectoryURL() {
		try {
			return new URL("file://" + LUT_DIRECTORY);
		}
		catch (MalformedURLException e) {
			return null;
		}
	}

	private URL getJarURL() {
		// TODO FIXME HACK this is not done
		return ClassUtils.getLocation(this.getClass());
	}

	private Collection<URL> filter(Collection<URL> urlCollection, String regex) {
		ArrayList<URL> list = new ArrayList<URL>();
		Pattern p = Pattern.compile(regex);
		for (URL url : urlCollection) {
			if (p.matcher(url.toString()).matches()) list.add(url);
		}
		return list;
	}

	private Collection<URL> getLuts(URL base) {
		Collection<URL> urls = FileUtils.listContents(base);
		return filter(urls, ".*\\.lut$");
	}

	private void putAll(Collection<URL> urls, Map<String, URL> map) {
		for (URL url : urls) {
			// TODO - this id form is limiting. One can only have one lut per short
			// name no matter where in tree hierarchy it is.
			String id = url.toString();
			int lastSlash = id.lastIndexOf("/");
			if (lastSlash >= 0) id = id.substring(lastSlash + 1, id.length());
			map.put(id, url);
		}
	}


	// *************************************************************************
	// OLD OLD OLD
	// *************************************************************************

	/**
	 * Finds the {@link URL}s of the .lut files known to the system. .lut files
	 * can reside in the standard Jar file or in the luts subdirectory of the
	 * application. This discovery could be extended to allow .lut files from the
	 * web.
	 * 
	 * @return A collection of URLs referencing the known .lut files
	 */
	public Collection<URL> oldFindLuts() {
		HashMap<String, URL> urls = new HashMap<String, URL>();
		findJarLuts(urls); // do 1st - Jar LUTs
		findFileSystemLuts(LUT_DIRECTORY, urls); // do 2nd - can override Jar LUTs
		return urls.values();
	}

	// -- private helpers --

	private void findFileSystemLuts(String dirName, Map<String, URL> urls) {
		File f = new File(dirName);
		if (!f.exists()) return;
		if (!f.isDirectory()) return;
		String[] files = f.list();
		for (String file : files) {
			StringBuilder builder = new StringBuilder();
			builder.append(dirName);
			builder.append(File.separator);
			builder.append(file);
			String pathSoFar = builder.toString();
			if (file.endsWith(".lut")) {
				try {
					URL url = new URL("file", "", pathSoFar);
					urls.put(file, url);
				}
				catch (Exception e) {
					// ignore MalformedURLs
				}
			}
			else { // not a .lut file but maybe a directory
				if (file.equals(".")) continue;
				if (file.equals("..")) continue;
				f = new File(pathSoFar);
				if (f.isDirectory()) findFileSystemLuts(pathSoFar, urls);
			}
		}
	}

	private void findJarLuts(Map<String, URL> urls) {
		File jar = new File("jarFileName"); // FIXME
		Pattern pattern = Pattern.compile(".*\\.lut$");
		Collection<String> lutNames = getResourceNamesFromJar(jar, pattern);
		for (String lutName : lutNames) {
			URL url = getClass().getResource("/luts/" + lutName);
			urls.put(lutName, url);
		}
		/*
		// URL location = ClassUtils.getLocation(this.getClass());
		// System.out.println("Location of LutFinder class is " + location);
		// String tmp = location.toString();
		// String name = tmp.substring(0, tmp.lastIndexOf("/"));

		URL url = getClass().getResource("/luts/luts.jar");
		System.out.println("Location of /luts resource in correct jar is " + url);
		String fname = url.toString();
		fname = fname.substring(5, fname.length());
		File file = new File(fname);
		Pattern pattern = Pattern.compile(".*\\.lut$");
		Collection<String> lutNames = getResourceNamesFromJarFile(file, pattern);
		for (String lutName : lutNames) {
			URL u = getClass().getResource("luts/luts.jar/" + lutName);
			System.out.println(u.toString());
		}
		*/
	}


	/**
	 * Adapted from some StackOverflow code
	 * 
	 * @author Barry DeZonia
	 */
	private static Collection<String> getResourceNamesFromJar(
		final File file,
		final Pattern pattern)
	{
		final ArrayList<String> retval = new ArrayList<String>();
		try {
			JarFile jf = new JarFile(file);
			final Enumeration<? extends JarEntry> e = jf.entries();
			while (e.hasMoreElements()) {
				final JarEntry je = e.nextElement();
				final String fileName = je.getName();
				final boolean accept = pattern.matcher(fileName).matches();
				if (accept) {
					retval.add(fileName);
				}
			}
			jf.close();
		}
		catch (final JarException e) {
			throw new Error(e);
		}
		catch (final IOException e) {
			throw new Error(e);
		}
		return retval;
	}

}
