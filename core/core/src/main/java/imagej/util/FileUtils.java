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

// File path shortening code adapted from:
// from: http://www.rgagnon.com/javadetails/java-0661.html

package imagej.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;

/**
 * Useful methods for working with file paths.
 * 
 * @author Johannes Schindelin
 * @author Grant Harris
 */
public final class FileUtils {

	public static final int DEFAULT_SHORTENER_THRESHOLD = 4;
	public static final String SHORTENER_BACKSLASH_REGEX = "\\\\";
	public static final String SHORTENER_SLASH_REGEX = "/";
	public static final String SHORTENER_BACKSLASH = "\\";
	public static final String SHORTENER_SLASH = "/";
	public static final String SHORTENER_ELLIPSE = "...";

	private FileUtils() {
		// prevent instantiation of utility class
	}

	/**
	 * Extracts the file extension from a file.
	 * 
	 * @param file the file object
	 * @return the file extension (excluding the dot), or the empty string when
	 *         the file name does not contain dots
	 */
	public static String getExtension(final File file) {
		final String name = file.getName();
		final int dot = name.lastIndexOf('.');
		if (dot < 0) return "";
		return name.substring(dot + 1);
	}

	/**
	 * Extracts the file extension from a file name.
	 * 
	 * @param path the path to the file (relative or absolute)
	 * @return the file extension (excluding the dot), or the empty string when
	 *         the file name does not contain dots
	 */
	public static String getExtension(final String path) {
		return getExtension(new File(path));
	}

	/**
	 * Converts the given {@link URL} to its corresponding {@link File}.
	 * <p>
	 * This method is similar to calling {@code new File(url.toURI())} except that
	 * it also handles "jar:file:" URLs, returning the path to the JAR file.
	 * </p>
	 * 
	 * @param url The URL to convert.
	 * @return A file path suitable for use with e.g. {@link FileInputStream}
	 * @throws IllegalArgumentException if the URL does not correspond to a file.
	 */
	public static File urlToFile(final URL url) {
		return urlToFile(url.toString());
	}

	/**
	 * Converts the given URL string to its corresponding {@link File}.
	 * 
	 * @param url The URL to convert.
	 * @return A file path suitable for use with e.g. {@link FileInputStream}
	 * @throws IllegalArgumentException if the URL does not correspond to a file.
	 */
	public static File urlToFile(final String url) {
		String path = url;
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

	/**
	 * Shortens the path to a maximum of 4 path elements.
	 * 
	 * @param path the path to the file (relative or absolute)
	 * @return shortened path
	 */
	public static String shortenPath(final String path) {
		return shortenPath(path, DEFAULT_SHORTENER_THRESHOLD);
	}

	/**
	 * Shortens the path based on the given maximum number of path elements. E.g.,
	 * "C:/1/2/test.txt" returns "C:/1/.../test.txt" if threshold is 1.
	 * 
	 * @param path the path to the file (relative or absolute)
	 * @param threshold the number of directories to keep unshortened
	 * @return shortened path
	 */
	public static String shortenPath(final String path, final int threshold) {
		String regex = SHORTENER_BACKSLASH_REGEX;
		String sep = SHORTENER_BACKSLASH;

		if (path.indexOf("/") > 0) {
			regex = SHORTENER_SLASH_REGEX;
			sep = SHORTENER_SLASH;
		}

		String pathtemp[] = path.split(regex);
		// remove empty elements
		int elem = 0;
		{
			final String newtemp[] = new String[pathtemp.length];
			int j = 0;
			for (int i = 0; i < pathtemp.length; i++) {
				if (!pathtemp[i].equals("")) {
					newtemp[j++] = pathtemp[i];
					elem++;
				}
			}
			pathtemp = newtemp;
		}

		if (elem > threshold) {
			final StringBuilder sb = new StringBuilder();
			int index = 0;

			// drive or protocol
			final int pos2dots = path.indexOf(":");
			if (pos2dots > 0) {
				// case c:\ c:/ etc.
				sb.append(path.substring(0, pos2dots + 2));
				index++;
				// case http:// ftp:// etc.
				if (path.indexOf(":/") > 0 && pathtemp[0].length() > 2) {
					sb.append(SHORTENER_SLASH);
				}
			}
			else {
				final boolean isUNC =
					path.substring(0, 2).equals(SHORTENER_BACKSLASH_REGEX);
				if (isUNC) {
					sb.append(SHORTENER_BACKSLASH).append(SHORTENER_BACKSLASH);
				}
			}

			for (; index <= threshold; index++) {
				sb.append(pathtemp[index]).append(sep);
			}

			if (index == (elem - 1)) {
				sb.append(pathtemp[elem - 1]);
			}
			else {
				sb.append(SHORTENER_ELLIPSE).append(sep).append(pathtemp[elem - 1]);
			}
			return sb.toString();
		}
		return path;
	}

	/**
	 * Compacts a path into a given number of characters. The result is similar to
	 * the Win32 API PathCompactPathExA.
	 * 
	 * @param path the path to the file (relative or absolute)
	 * @param limit the number of characters to which the path should be limited
	 * @return shortened path
	 */
	public static String limitPath(final String path, final int limit) {
		if (path.length() <= limit) return path;

		final char shortPathArray[] = new char[limit];
		final char pathArray[] = path.toCharArray();
		final char ellipseArray[] = SHORTENER_ELLIPSE.toCharArray();

		final int pathindex = pathArray.length - 1;
		final int shortpathindex = limit - 1;

		// fill the array from the end
		int i = 0;
		for (; i < limit; i++) {
			if (pathArray[pathindex - i] != '/' && pathArray[pathindex - i] != '\\') {
				shortPathArray[shortpathindex - i] = pathArray[pathindex - i];
			}
			else {
				break;
			}
		}
		// check how much space is left
		final int free = limit - i;

		if (free < SHORTENER_ELLIPSE.length()) {
			// fill the beginning with ellipse
			for (int j = 0; j < ellipseArray.length; j++) {
				shortPathArray[j] = ellipseArray[j];
			}
		}
		else {
			// fill the beginning with path and leave room for the ellipse
			int j = 0;
			for (; j + ellipseArray.length < free; j++) {
				shortPathArray[j] = pathArray[j];
			}
			// ... add the ellipse
			for (int k = 0; j + k < free; k++) {
				shortPathArray[j + k] = ellipseArray[k];
			}
		}
		return new String(shortPathArray);
	}

	/**
	 * Creates a temporary directory.
	 * <p>
	 * Since there is no atomic operation to do that, we create a temporary file,
	 * delete it and create a directory in its place. To avoid race conditions, we
	 * use the optimistic approach: if the directory cannot be created, we try to
	 * obtain a new temporary file rather than erroring out.
	 * </p>
	 * <p>
	 * It is the caller's responsibility to make sure that the directory is
	 * deleted.
	 * </p>
	 * 
	 * @param prefix The prefix string to be used in generating the file's name;
	 *          see {@link File#createTempFile(String, String, File)}
	 * @param suffix The suffix string to be used in generating the file's name;
	 *          see {@link File#createTempFile(String, String, File)}
	 * @return An abstract pathname denoting a newly-created empty directory
	 * @throws IOException
	 */
	public static File createTemporaryDirectory(final String prefix,
		final String suffix) throws IOException
	{
		return createTemporaryDirectory(prefix, suffix, null);
	}

	/**
	 * Creates a temporary directory.
	 * <p>
	 * Since there is no atomic operation to do that, we create a temporary file,
	 * delete it and create a directory in its place. To avoid race conditions, we
	 * use the optimistic approach: if the directory cannot be created, we try to
	 * obtain a new temporary file rather than erroring out.
	 * </p>
	 * <p>
	 * It is the caller's responsibility to make sure that the directory is
	 * deleted.
	 * </p>
	 * 
	 * @param prefix The prefix string to be used in generating the file's name;
	 *          see {@link File#createTempFile(String, String, File)}
	 * @param suffix The suffix string to be used in generating the file's name;
	 *          see {@link File#createTempFile(String, String, File)}
	 * @param directory The directory in which the file is to be created, or null
	 *          if the default temporary-file directory is to be used
	 * @return: An abstract pathname denoting a newly-created empty directory
	 * @throws IOException
	 */
	public static File createTemporaryDirectory(final String prefix,
		final String suffix, final File directory) throws IOException
	{
		for (int counter = 0; counter < 10; counter++) {
			final File file = File.createTempFile(prefix, suffix, directory);

			if (!file.delete()) {
				throw new IOException("Could not delete file " + file);
			}

			// in case of a race condition, just try again
			if (file.mkdir()) return file;
		}
		throw new IOException(
			"Could not create temporary directory (too many race conditions?)");
	}

	// -- Deprecated methods --

	/** @deprecated Use {@link AppUtils#getBaseDirectory()} instead. */
	@Deprecated
	public static File getBaseDirectory() {
		return AppUtils.getBaseDirectory();
	}

	/** @deprecated Use {@link AppUtils#getBaseDirectory(String)} instead. */
	@Deprecated
	public static String getBaseDirectory(final String className) {
		final File baseDir = AppUtils.getBaseDirectory(className);
		return baseDir == null ? null : baseDir.getAbsolutePath();
	}

	/** @deprecated Use {@link ProcessUtils#exec} instead. */
	@Deprecated
	public static String exec(final File workingDirectory, final PrintStream err,
		final PrintStream out, final String... args)
	{
		return ProcessUtils.exec(workingDirectory, err, out, args);
	}

}
