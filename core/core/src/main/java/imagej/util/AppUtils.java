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

import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Useful methods for obtaining details of the ImageJ application environment.
 * 
 * @author Johannes Schindelin
 * @author Curtis Rueden
 */
public final class AppUtils {

	private AppUtils() {
		// prevent instantiation of utility class
	}

	/**
	 * Gets the ImageJ root directory. If the <code>ij.dir</code> property is set,
	 * it is used. Otherwise, we scan up the tree from this class for a suitable
	 * directory.
	 */
	public static File getBaseDirectory() {
		final String property = System.getProperty("ij.dir");
		if (property != null) {
			final File dir = new File(property);
			if (dir.isDirectory()) return dir;
		}

		// look for valid base directory relative to this class
		final File corePath =
			getBaseDirectory(AppUtils.class.getName(), "core/core");
		if (corePath != null) return corePath;

		// HACK: Look for valid base directory relative to ImageJ launcher class.
		// We will reach this logic, e.g., if the application is running via
		// "mvn exec:exec" from the app directory. In that case, most classes
		// (including this one) will be located in JARs in the local Maven
		// repository cache (~/.m2/repository), so the corePath will be null.
		// However, the classes of ij-app will be located in app/target/classes,
		// so we search up the tree from one of those.
		final File appPath = getBaseDirectory("imagej.Main", "app");
		if (appPath != null) return appPath;

		// last resort: use current working directory
		return new File(".").getAbsoluteFile();
	}

	/**
	 * Gets the base file system directory containing the given class file.
	 * 
	 * @param className The fully qualified name of the class from which the base
	 *          directory should be derived.
	 */
	public static File getBaseDirectory(final String className) {
		return getBaseDirectory(className, null);
	}

	/**
	 * Gets the base file system directory containing the given class file.
	 * 
	 * @param className The fully qualified name of the class from which the base
	 *          directory should be derived.
	 * @param baseSubdirectory A hint for what to expect for a directory structure
	 *          beneath the application base directory.
	 */
	public static File getBaseDirectory(final String className,
		final String baseSubdirectory)
	{
		final File classLocation = ClassUtils.getLocation(className);
		return getBaseDirectory(classLocation, baseSubdirectory);
	}

	/**
	 * Gets the base file system directory from the given known class location.
	 * 
	 * @param classLocation The location from which the base directory should be
	 *          derived.
	 * @param baseSubdirectory A hint for what to expect for a directory structure
	 *          beneath the application base directory.
	 */
	public static File getBaseDirectory(final File classLocation,
		final String baseSubdirectory)
	{
		String path = classLocation.getAbsolutePath();

		if (path.contains("/.m2/repository/")) {
			// NB: The class is in a JAR in the Maven repository cache.
			// We cannot find the application base directory relative to this path.
			return null;
		}

		// check whether the class is in a Maven build directory
		String basePrefix = "/";
		if (baseSubdirectory != null) basePrefix += baseSubdirectory + "/";

		final String targetClassesSuffix = basePrefix + "target/classes";
		if (path.endsWith(targetClassesSuffix)) {
			// NB: The class is a file beneath the Maven build directory
			// ("target/classes").
			path = path.substring(0, path.length() - targetClassesSuffix.length());
			return new File(path);
		}

		final Pattern pattern =
			Pattern.compile(".*(" + Pattern.quote(basePrefix + "target/") +
				"[^/]*\\.jar)");
		final Matcher matcher = pattern.matcher(path);
		if (matcher.matches()) {
			// NB: The class is in the Maven build directory inside a JAR file
			// ("target/[something].jar").
			final int index = matcher.start(1);
			path = path.substring(0, index);
			return new File(path);
		}

		if (path.endsWith(".jar")) {
			final File jarDirectory = classLocation.getParentFile();
			// NB: The class is in a JAR file, which we assume is nested one level
			// deep (i.e., directly beneath the application base directory).
			return jarDirectory.getParentFile();
		}

		// NB: As a last resort, we use the class location directly.
		return classLocation;
	}

}
