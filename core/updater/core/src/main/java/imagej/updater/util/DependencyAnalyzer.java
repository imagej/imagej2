//
// DependencyAnalyzer.java
//

/*
ImageJ software for multidimensional image processing and analysis.

Copyright (c) 2010, ImageJDev.org.
All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met:
    * Redistributions of source code must retain the above copyright
      notice, this list of conditions and the following disclaimer.
    * Redistributions in binary form must reproduce the above copyright
      notice, this list of conditions and the following disclaimer in the
      documentation and/or other materials provided with the distribution.
    * Neither the names of the ImageJDev.org developers nor the
      names of its contributors may be used to endorse or promote products
      derived from this software without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
POSSIBILITY OF SUCH DAMAGE.
*/

package imagej.updater.util;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * This class generates a list of dependencies for a given plugin. The
 * dependencies are based on the existing plugins in the user's ImageJ
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

	public DependencyAnalyzer() {
		map = new Class2JarFilesMap();
	}

	public Iterable<String> getDependencies(String filename) throws IOException {
		if (!filename.endsWith(".jar") || !new File(filename).exists()) return null;

		final Set<String> result = new LinkedHashSet<String>();
		final Set<String> handled = new HashSet<String>();

		final JarFile jar = new JarFile(filename);
		filename = Util.stripPrefix(filename, Util.imagejRoot);
		for (final JarEntry file : Collections.list(jar.entries())) {
			if (!file.getName().endsWith(".class")) continue;

			final InputStream input = jar.getInputStream(file);
			final byte[] code = Compressor.readStream(input);
			final ByteCodeAnalyzer analyzer = new ByteCodeAnalyzer(code);

			final Set<String> allClassNames = new HashSet<String>();
			for (final String name : analyzer)
				addClassAndInterfaces(allClassNames, handled, name);

			for (final String name : allClassNames) {
				UserInterface.get().debug("Considering name from analyzer: " + name);
				final List<String> allJars = map.get(name);
				if (allJars == null || allJars.contains(filename)) continue;
				if (allJars.size() > 1) {
					UserInterface.get().log(
						"Warning: class " + name + ", referenced in " + filename +
							", is in more than one jar:");
					for (final String j : allJars)
						UserInterface.get().log("  " + j);
					UserInterface.get().log("... adding all as dependency.");
				}
				for (final String j : allJars) {
					result.add(j);
					UserInterface.get().debug(
						"... adding dep " + j + " for " + filename + " because of class " +
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
				Compressor.readStream(getClass().getResourceAsStream(resourceName));
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
			final byte[] code = Compressor.readStream(input);
			final ByteCodeAnalyzer analyzer = new ByteCodeAnalyzer(code, true);
			if (analyzer.containsDebugInfo()) return true;
		}
		return false;
	}
}
