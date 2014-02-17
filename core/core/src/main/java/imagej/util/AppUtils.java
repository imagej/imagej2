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
 * #L%
 */

package imagej.util;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import org.scijava.util.FileUtils;

/**
 * Useful methods for obtaining details of the ImageJ application environment.
 * 
 * @author Johannes Schindelin
 * @author Curtis Rueden
 * @author Barry DeZonia
 */
public final class AppUtils {

	private AppUtils() {
		// prevent instantiation of utility class
	}

	/**
	 * Gets the ImageJ root directory. If the {@code ij.dir} property is set, it
	 * is used. Otherwise, we scan up the tree from this class for a suitable
	 * directory.
	 * 
	 * @see org.scijava.util.AppUtils#getBaseDirectory(String, Class, String)
	 */
	public static File getBaseDirectory() {
		return org.scijava.util.AppUtils.getBaseDirectory("ij.dir", AppUtils.class,
			"core/core");
	}

	/**
	 * Finds {@link URL}s of resources known to ImageJ. Both JAR files and files
	 * on disk are searched, according to the following mechanism:
	 * <ol>
	 * <li>Resources at the given {@code pathPrefix} are discovered using
	 * {@link ClassLoader#getResources(String)} with the current thread's context
	 * class loader. In particular, this invocation discovers resources in JAR
	 * files beneath the given {@code pathPrefix}.</li>
	 * <li>The directory named {@code pathPrefix} beneath the
	 * {@link AppUtils#getBaseDirectory() ImageJ application base directory}.</li>
	 * </ol>
	 * <p>
	 * In both cases, resources are then recursively scanned using SciJava
	 * Common's {@link FileUtils#listContents(URL)}, and anything matching the
	 * given {@code regex} pattern is added to the output map.
	 * </p>
	 * <p>
	 * Note that the {@code pathPrefix} directory is scanned <em>after</em> the
	 * URL resources, so that users can more easily override resources provided
	 * inside JAR files by placing a resource of the same name within the
	 * {@code pathPrefix} directory.
	 * </p>
	 * 
	 * @param regex The regex to use when matching resources, or null to match
	 *          everything.
	 * @param pathPrefix The path to search for resources.
	 * @return A map of URLs referencing the matched resources.
	 */
	public static Map<String, URL> findResources(final String regex,
		final String pathPrefix)
	{
		// scan URL resource paths first
		final ClassLoader loader = Thread.currentThread().getContextClassLoader();
		final ArrayList<URL> urls = new ArrayList<URL>();
		try {
			urls.addAll(Collections.list(loader.getResources(pathPrefix + "/")));
		}
		catch (final IOException exc) {
			// error loading resources; proceed with an empty list
		}

		// scan directory second; user can thus override resources from JARs
		final File baseDirectory = AppUtils.getBaseDirectory();
		if (baseDirectory != null) {
			try {
				urls.add(new File(baseDirectory, pathPrefix).toURI().toURL());
			}
			catch (final MalformedURLException exc) {
				// error adding directory; proceed without it
			}
		}

		return findResources(regex, urls);
	}

	/**
	 * Finds {@link URL}s of resources known to ImageJ.
	 * <p>
	 * Each of the given {@link URL}s is recursively scanned using SciJava
	 * Common's {@link FileUtils#listContents(URL)}, and anything matching the
	 * given {@code regex} pattern is added to the output map.</li>
	 * 
	 * @param regex The regex to use when matching resources, or null to match
	 *          everything.
	 * @param urls Paths to search for resources.
	 * @return A map of URLs referencing the matched resources.
	 */
	public static Map<String, URL> findResources(final String regex,
		final Iterable<URL> urls)
	{
		final HashMap<String, URL> result = new HashMap<String, URL>();
		final Pattern pattern = regex == null ? null : Pattern.compile(regex);
		for (final URL url : urls) {
			getResources(pattern, result, url);
		}
		return result;
	}

	// -- Helper methods --

	private static void getResources(final Pattern pattern,
		final Map<String, URL> result, final URL base)
	{
		final String prefix = urlPath(base);
		if (prefix == null) return; // unsupported base URL

		for (final URL url : FileUtils.listContents(base)) {
			final String s = urlPath(url);
			if (s == null || !s.startsWith(prefix)) continue;

			if (pattern == null || pattern.matcher(s).matches()) {
				// this resource matches the pattern
				final String key = urlPath(s.substring(prefix.length()));
				if (key != null) result.put(key, url);
			}
		}
	}

	private static String urlPath(final URL url) {
		try {
			return url.toURI().toString();
		}
		catch (final URISyntaxException exc) {
			return null;
		}
	}

	private static String urlPath(final String path) {
		try {
			return new URI(path).getPath();
		}
		catch (final URISyntaxException exc) {
			return null;
		}
	}

}
