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

package imagej.updater.util;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * This class generates a list of dependencies for a given file. The
 * dependencies are based on the existing files in the user's ImageJ
 * directories. It uses the static class ByteCodeAnalyzer to analyze every
 * single class file in the given JAR file, which will determine the classes
 * relied on ==> And in turn their JAR files, i.e.: The dependencies themselves
 * This class is needed to avoid running out of PermGen space (which happens if
 * you load a ton of classes into a classloader). The magic numbers and offsets
 * are taken from
 * http://java.sun.com/docs/books/jvms/second_edition/html/ClassFile.doc.html
 */
public class DependencyAnalyzer {

	private final Class2JarFilesMap map;

	public DependencyAnalyzer(final File imagejRoot) {
		map = new Class2JarFilesMap(imagejRoot);
	}

	public Iterable<String> getDependencies(final File imagejRoot,
		final String path) throws IOException
	{
		if (!path.endsWith(".jar")) return null;

		final File file = new File(imagejRoot, path);
		if (!file.exists()) return null;

		final Set<String> result = new LinkedHashSet<String>();
		final Set<String> handled = new HashSet<String>();

		final JarFile jar = new JarFile(file);
		for (final JarEntry entry : Collections.list(jar.entries())) {
			if (!entry.getName().endsWith(".class")) continue;

			final InputStream input = jar.getInputStream(entry);
			final byte[] code = Util.readStreamAsBytes(input);
			final ByteCodeAnalyzer analyzer = new ByteCodeAnalyzer(code);

			final Set<String> allClassNames = new HashSet<String>();
			for (final String name : analyzer)
				addClassAndInterfaces(allClassNames, handled, name);

			for (final String name : allClassNames) {
				UpdaterUserInterface.get().debug("Considering name from analyzer: " + name);
				final List<String> jars = map.get(name);
				if (jars == null) continue;
				final List<String> dependencies = new ArrayList<String>();
				for (final String dependency : jars) {
					if (!exclude(path, dependency)) dependencies.add(dependency);
				}
				if (dependencies.size() > 1) {
					UpdaterUserInterface.get().log(
						"Warning: class " + name + ", referenced in " + path +
							", is in more than one jar:");
					for (final String j : dependencies)
						UpdaterUserInterface.get().log("  " + j);
					UpdaterUserInterface.get().log("... adding all as dependency.");
				}
				for (final String j : dependencies) {
					result.add(j);
					UpdaterUserInterface.get().debug(
						"... adding dep " + j + " for " + path + " because of class " +
							name);
				}
			}
		}
		return result;
	}

	protected void addClassAndInterfaces(final Set<String> allClassNames,
		final Set<String> handled, final String className)
	{
		if (className == null || className.startsWith("[") ||
			handled.contains(className)) return;
		handled.add(className);
		final String resourceName = "/" + className.replace('.', '/') + ".class";
		if (ClassLoader.getSystemClassLoader().getResource(resourceName) != null) return;
		allClassNames.add(className);
		try {
			final byte[] buffer =
				Util.readStreamAsBytes(getClass().getResourceAsStream(resourceName));
			final ByteCodeAnalyzer analyzer = new ByteCodeAnalyzer(buffer);
			addClassAndInterfaces(allClassNames, handled, analyzer.getSuperclass());
			for (final String iface : analyzer.getInterfaces())
				addClassAndInterfaces(allClassNames, handled, iface);
		}
		catch (final Exception e) { /* ignore */}
	}

	public static boolean containsDebugInfo(final String filename)
		throws IOException
	{
		if (!filename.endsWith(".jar") || !new File(filename).exists()) return false;

		final JarFile jar = new JarFile(filename);
		for (final JarEntry file : Collections.list(jar.entries())) {
			if (!file.getName().endsWith(".class")) continue;

			final InputStream input = jar.getInputStream(file);
			final byte[] code = Util.readStreamAsBytes(input);
			final ByteCodeAnalyzer analyzer = new ByteCodeAnalyzer(code, true);
			if (analyzer.containsDebugInfo()) return true;
		}
		return false;
	}

	/**
	 * Exclude some dependencies Sometimes we just know better. for example,
	 * slf4j-api.jar and slf4j-log4j12.jar contain circular references, so we
	 * force one direction.
	 * 
	 * @param jarPath the path of the .jar file
	 * @param dependency the path of the dependency to exclude
	 * @return whether it should be forced to have no dependencies
	 */
	protected boolean exclude(final String jarPath, final String dependency) {
		return jarPath.equals(dependency) ||
			dependency.equals("jars/javac.jar") ||
			(jarPath.equals("jars/slf4j-api.jar") && dependency
				.equals("jars/slf4j-log4j12.jar")) ||
			(jarPath.equals("jars/logkit.jar") && dependency
				.equals("jars/avalon-framework.jar")) ||
			(jarPath.equals("jars/bsh.jar") && dependency.equals("jars/testng.jar")) ||
			(jarPath.equals("jars/testng.jar") && dependency.equals("jars/guice.jar"));
	}

}
