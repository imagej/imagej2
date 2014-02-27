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
package imagej.patcher;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.JarURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * Provides some functions originally found in SciJava-common.
 * <p>
 * The <i>ij1-patcher</i> project was originally part of the ImageJ2 project,
 * relying on functions provided by the <i>scijava-common</i> project. However,
 * to be as standalone as possible, we replicate the functions here to avoid the
 * additional dependency.
 * </p>
 * <p>
 * This class is <b>not</b> part of the API of <i>ij1-patcher</i>; Projects
 * which desire to use the functionality contained herein should depend on
 * <i>scijava-common</i> instead.
 * </p>
 * 
 * @author "Johannes Schindelin"
 */
class Utils {

	/**
	 * Gets the base location of the given class.
	 * <p>
	 * If the class is directly on the file system (e.g.,
	 * "/path/to/my/package/MyClass.class") then it will return the base directory
	 * (e.g., "file:/path/to").
	 * </p>
	 * <p>
	 * If the class is within a JAR file (e.g.,
	 * "/path/to/my-jar.jar!/my/package/MyClass.class") then it will return the
	 * path to the JAR (e.g., "file:/path/to/my-jar.jar").
	 * </p>
	 * <p>
	 * This method was extracted from SciJava-common's {@code ClassUtils} class.
	 * </p>
	 * 
	 * @param c The class whose location is desired.
	 * @see #urlToFile(URL) to convert the result to a {@link File}.
	 */
	static URL getLocation(final Class<?> c) {
		if (c == null) return null; // could not load the class

		// try the easy way first
		try {
			final URL codeSourceLocation =
				c.getProtectionDomain().getCodeSource().getLocation();
			if (codeSourceLocation != null) return codeSourceLocation;
		}
		catch (final SecurityException e) {
			// NB: Cannot access protection domain.
		}
		catch (final NullPointerException e) {
			// NB: Protection domain or code source is null.
		}

		// NB: The easy way failed, so we try the hard way. We ask for the class
		// itself as a resource, then strip the class's path from the URL string,
		// leaving the base path.

		// get the class's raw resource path
		final URL classResource = c.getResource(c.getSimpleName() + ".class");
		if (classResource == null) return null; // cannot find class resource

		final String url = classResource.toString();
		final String suffix = c.getCanonicalName().replace('.', '/') + ".class";
		if (!url.endsWith(suffix)) return null; // weird URL

		// strip the class's path from the URL string
		final String base = url.substring(0, url.length() - suffix.length());

		String path = base;

		// remove the "jar:" prefix and "!/" suffix, if present
		if (path.startsWith("jar:")) path = path.substring(4, path.length() - 2);

		try {
			return new URL(path);
		}
		catch (final MalformedURLException e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * Checks whether a class with the given name exists.
	 * 
	 * @param className the class name
	 * @return whether the current thread's context class loader knows the class
	 */
	static boolean hasClass(final String className) {
		return hasClass(className, null);
	}

	/**
	 * Checks whether a class with the given name exists.
	 * 
	 * @param className the class name
	 * @param classLoader the loader to ask for the class, if {@code null} is
	 *          passed, the current thread's context class loader will be asked
	 * @return whether the class loader knows the class
	 */
	static boolean hasClass(final String className,
		final ClassLoader classLoader)
	{
		return loadClass(className, classLoader) != null;
	}

	/**
	 * Tries to load the class with the given name.
	 * <p>
	 * This method uses the specified {@link ClassLoader} (or the current thread's
	 * context class loader if {@code null} was passed as the {@code classLoader}
	 * parameter. If the class cannot be found no exception is thrown but
	 * {@code null} is returned instead.
	 * </p>
	 * <p>
	 * This method is a stripped down version of the {@code loadClass} method in
	 * SciJava-common's {@code ClassUtils} class.
	 * </p>
	 * 
	 * @param name The name of the class to load.
	 * @param classLoader The class loader with which to load the class; if null,
	 *          the current thread's context class loader will be used.
	 */
	static Class<?> loadClass(final String className,
		final ClassLoader classLoader)
	{
		// load the class!
		try {
			final ClassLoader cl =
				classLoader == null ? Thread.currentThread().getContextClassLoader()
					: classLoader;
			return cl.loadClass(className);
		}
		catch (final ClassNotFoundException e) {
			return null;
		}
	}

	/**
	 * Recursively lists the contents of the referenced directory.
	 * <p>
	 * Directories are excluded from the result. Supported protocols include
	 * {@code file} and {@code jar}.
	 * </p>
	 * <p>
	 * This method was extracted from SciJava-common's {@code FileUtils} class.
	 * </p>
	 * 
	 * @param directory The directory whose contents should be listed.
	 * @return A collection of {@link URL}s representing the directory's contents.
	 * @see #listContents(URL, boolean, boolean)
	 */
	static Collection<URL> listContents(final URL directory) {
		return appendContents(new ArrayList<URL>(), directory, true, true);
	}

	/**
	 * Add contents from the referenced directory to an existing collection.
	 * <p>
	 * Supported protocols include {@code file} and {@code jar}.
	 * </p>
	 * <p>
	 * This method was extracted from SciJava-common's {@code FileUtils} class.
	 * </p>
	 * 
	 * @param result The collection to which contents should be added.
	 * @param directory The directory whose contents should be listed.
	 * @param recurse Whether to append contents recursively, as opposed to only
	 *          the directory's direct contents.
	 * @param filesOnly Whether to exclude directories in the resulting collection
	 *          of contents.
	 * @return A collection of {@link URL}s representing the directory's contents.
	 */
	static Collection<URL> appendContents(final Collection<URL> result,
		final URL directory, final boolean recurse, final boolean filesOnly)
	{
		if (directory == null) return result; // nothing to append
		final String protocol = directory.getProtocol();
		if (protocol.equals("file")) {
			final File dir = urlToFile(directory);
			final File[] list = dir.listFiles();
			if (list != null) {
				for (final File file : list) {
					try {
						if (!filesOnly || file.isFile()) {
							result.add(file.toURI().toURL());
						}
						if (recurse && file.isDirectory()) {
							appendContents(result, file.toURI().toURL(), recurse, filesOnly);
						}
					}
					catch (final MalformedURLException e) {
						e.printStackTrace();
					}
				}
			}
		}
		else if (protocol.equals("jar")) {
			try {
				final String url = directory.toString();
				final int bang = url.indexOf("!/");
				if (bang < 0) return result;
				final String prefix = url.substring(bang + 2);
				final String baseURL = url.substring(0, bang + 2);

				final JarURLConnection connection =
					(JarURLConnection) new URL(baseURL).openConnection();
				final JarFile jar = connection.getJarFile();
				final Enumeration<JarEntry> entries = jar.entries();
				while (entries.hasMoreElements()) {
					final JarEntry entry = entries.nextElement();
					final String urlEncoded =
						new URI(null, null, entry.getName(), null).toString();
					if (urlEncoded.length() > prefix.length() && // omit directory itself
						urlEncoded.startsWith(prefix))
					{
						if (filesOnly && urlEncoded.endsWith("/")) {
							// URL is directory; exclude it
							continue;
						}
						if (!recurse) {
							// check whether this URL is a *direct* child of the directory
							final int slash = urlEncoded.indexOf("/", prefix.length());
							if (slash >= 0 && slash != urlEncoded.length() - 1) {
								// not a direct child
								continue;
							}
						}
						result.add(new URL(baseURL + urlEncoded));
					}
				}
				jar.close();
			}
			catch (final IOException e) {
				e.printStackTrace();
			}
			catch (final URISyntaxException e) {
				throw new IllegalArgumentException(e);
			}
		}
		return result;
	}

	/**
	 * Converts the given URL string to its corresponding {@link File}.
	 * <p>
	 * This method was extracted from SciJava-common's {@code FileUtils} class.
	 * </p>
	 * 
	 * @param url The URL to convert.
	 * @return A file path suitable for use with e.g. {@link FileInputStream}
	 * @throws IllegalArgumentException if the URL does not correspond to a file.
	 */
	static File urlToFile(final URL url) {
		String path = url.toString();
		if (path.startsWith("jar:")) {
			// remove "jar:" prefix and "!/" suffix
			final int index = path.indexOf("!/");
			path = path.substring(4, index);
		}
		try {
			return new File(new URL(path).toURI());
		}
		catch (final MalformedURLException e) {
			// NB: URL is not completely well-formed.
		}
		catch (final URISyntaxException e) {
			// NB: URL is not completely well-formed.
		}
		if (path.startsWith("file:")) {
			// pass through the URL as-is, minus "file:" prefix
			path = path.substring(5);
			return new File(path);
		}
		throw new IllegalArgumentException("Invalid URL: " + url);
	}

}
