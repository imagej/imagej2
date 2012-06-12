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

package imagej.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class CheckSezpoz {

	public static boolean verbose;

	public static final String FILE_NAME = "latest-sezpoz-check.txt";

	/**
	 * Check the annotations of all CLASSPATH components Optionally, it only
	 * checks the non-.jar components of the CLASSPATH. This is for Eclipse.
	 * Eclipse fails to run the annotation processor at each incremental build. In
	 * contrast to Maven, Eclipse usually does not build .jar files, though, so we
	 * can have a very quick check at startup if the annotation processor was not
	 * run correctly and undo the damage.
	 * 
	 * @param checkJars whether to inspect .jar components of the CLASSPATH
	 * @return false, when the annotation processor had to be run
	 * @throws IOException
	 */
	public static boolean check(final boolean checkJars) throws IOException {
		boolean upToDate = true;
		for (final String path : System.getProperty("java.class.path").split(
			File.pathSeparator))
		{
			if (!checkJars && path.endsWith(".jar")) continue;
			if (!check(new File(path))) upToDate = false;
		}
		return upToDate;
	}

	/**
	 * Check the annotations of a CLASSPATH component
	 * 
	 * @param file the CLASSPATH component (.jar file or directory)
	 * @return false, when the annotation processor had to be run
	 * @throws IOException
	 */
	public static boolean check(final File file) throws IOException {
		if (!file.exists()) return true;
		if (file.isDirectory()) return checkDirectory(file);
		else if (file.isFile() && file.getName().endsWith(".jar")) checkJar(file);
		else Log.warn("Skipping sezpoz check of " + file);
		return true;
	}

	/**
	 * Check the annotations of a directory in the CLASSPATH
	 * 
	 * @param file the CLASSPATH component directory
	 * @return false, when the annotation processor had to be run
	 * @throws IOException
	 */
	public static boolean checkDirectory(final File classes) throws IOException {
		if (!classes.getPath().endsWith("target/classes")) {
			Log.warn("Ignoring non-Maven build directory: " + classes.getPath());
			return true;
		}
		final File source =
			new File(classes.getParentFile().getParentFile(), "src/main/java");
		if (!source.isDirectory()) {
			Log.warn("No src/main/java found for " + classes);
			return true;
		}
		final long latestCheck = getLatestCheck(classes.getParentFile());
		final boolean upToDate = checkDirectory(classes, source, latestCheck);
		if (!upToDate) return !fix(classes, source);
		return true;
	}

	protected static long getLatestCheck(final File targetDirectory) {
		try {
			final File file = new File(targetDirectory, FILE_NAME);
			if (!file.exists()) return -1;
			final BufferedReader reader = new BufferedReader(new FileReader(file));
			String firstLine = reader.readLine();
			reader.close();
			if (firstLine == null) return -1;
			if (firstLine.endsWith("\n")) firstLine =
				firstLine.substring(0, firstLine.length() - 1);
			return Long.parseLong(firstLine);
		}
		catch (final IOException e) {
			return -1;
		}
		catch (final NumberFormatException e) {
			return -1;
		}
	}

	protected static long getLatestCheck(final JarFile jar) {
		return -1;
	}

	private static void setLatestCheck(final File targetDirectory) {
		final File file = new File(targetDirectory, FILE_NAME);
		// let's make sure this file has LF-terminated lines
		try {
			final Date date = new Date();
			final String content =
				"" + date.getTime() + "\n" +
					DateFormat.getDateTimeInstance().format(date) + "\n";
			final OutputStream out = new FileOutputStream(file);
			out.write(content.getBytes());
			out.close();
		}
		catch (final IOException e) {
			e.printStackTrace();
			Log.error("Failure updating the Sezpoz check timestamp", e);
		}
	}

	public static boolean checkDirectory(final File classes, final File source,
		final long olderThan) throws IOException
	{
		if (classes.getName().equals("META-INF") || !source.isDirectory()) return true;

		final File[] list = classes.listFiles();
		if (list == null) return true;
		for (final File file : list) {
			final String name = file.getName();
			if (file.isDirectory()) {
				if (!checkDirectory(file, new File(source, name), olderThan)) return false;
			}
			else if (file.isFile() && file.lastModified() > olderThan) {
				return false;
			}
		}
		return true;
	}

	public static void checkJar(final File file) throws IOException {
		final JarFile jar = new JarFile(file);
		final long mtime = getLatestCheck(jar);
		if (mtime < 0) {
			// Eclipse cannot generate .jar files (except in manual mode).
			// Assume everything is alright
			return;
		}
		for (final JarEntry entry : iterate(jar.entries())) {
			if (entry.getTime() > mtime) {
				throw new IOException("Annotations for " + entry + " in " + file +
					" are out-of-date!");
			}
		}
	}

	/**
	 * Run sezpoz on the sources, writing the annotations into the classes'
	 * META-INF/annotations/ directory
	 * 
	 * @param classes the output directory
	 * @param source the directory containing the source files
	 * @return whether anything in META-INF/annotations/* changed
	 */
	public static boolean fix(final File classes, final File sources) {
		final Method aptProcess;
		try {
			final Class<?> aptClass =
				CheckSezpoz.class.getClassLoader().loadClass("com.sun.tools.apt.Main");
			aptProcess =
				aptClass.getMethod("process", new Class[] { String[].class });
		}
		catch (final Exception e) {
			Log.error("Could not fix " + sources + ": apt not found", e);
			return false;
		}
		if (!sources.exists()) {
			Log.error("Sources are not in the expected place: " + sources);
			return false;
		}

		final List<String> aptArgs = new ArrayList<String>();
		aptArgs.add("-nocompile");
		if (verbose) aptArgs.add("-verbose");
		aptArgs.add("-factory");
		aptArgs.add("net.java.sezpoz.impl.IndexerFactory");
		aptArgs.add("-d");
		aptArgs.add(classes.getPath());
		final int count = aptArgs.size();
		addJavaPathsRecursively(aptArgs, sources);
		// do nothing if there is nothing to
		if (count == aptArgs.size()) return false;
		// remove possibly outdated annotations
		final File[] obsoleteAnnotations =
			new File(classes, "META-INF/annotations").listFiles();
		if (obsoleteAnnotations != null) {
			for (final File annotation : obsoleteAnnotations)
				annotation.delete();
		}

		final String[] args = aptArgs.toArray(new String[aptArgs.size()]);
		try {
			Log.warn("Updating the annotation index in " + classes);
			aptProcess.invoke(null, new Object[] { args });
		}
		catch (final Exception e) {
			Log.error("Could not fix " + sources + ": apt failed", e);
			return false;
		}

		setLatestCheck(classes.getParentFile());
		return true;
	}

	protected static void addJavaPathsRecursively(final List<String> list,
		final File directory)
	{
		final File[] files = directory.listFiles();
		if (files == null) return;
		for (final File file : files) {
			if (file.isDirectory()) addJavaPathsRecursively(list, file);
			else if (file.isFile() && file.getName().endsWith(".java")) list.add(file
				.getPath());
		}
	}

	protected static void touch(final File file) throws IOException {
		new FileOutputStream(file, true).close();
	}

	public static <T> Iterable<T> iterate(final Enumeration<T> en) {
		final Iterator<T> iterator = new Iterator<T>() {

			@Override
			public boolean hasNext() {
				return en.hasMoreElements();
			}

			@Override
			public T next() {
				return en.nextElement();
			}

			@Override
			public void remove() {
				throw new UnsupportedOperationException();
			}
		};

		return new Iterable<T>() {

			@Override
			public Iterator<T> iterator() {
				return iterator;
			}
		};
	}
}
