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
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.zip.ZipException;

@SuppressWarnings("serial")
public class Class2JarFilesMap extends HashMap<String, ArrayList<String>> {

	protected final File imagejRoot;

	public Class2JarFilesMap(final File imagejRoot) {
		this.imagejRoot = imagejRoot;
		addDirectory("plugins");
		addDirectory("jars");
	}

	private void addDirectory(final String directory) {
		final File dir = new File(imagejRoot, directory);
		if (!dir.isDirectory()) return;
		final String[] list = dir.list();
		for (int i = 0; i < list.length; i++) {
			final String path = directory + "/" + list[i];
			if (list[i].endsWith(".jar")) try {
				addJar(path);
			}
			catch (final IOException e) {
				UpdaterUserInterface.get().log("Warning: could not open " + path);
			}
			else addDirectory(path);
		}
	}

	private void addJar(final String jar) throws IOException {
		try {
			final JarFile file = new JarFile(new File(imagejRoot, jar));
			final Enumeration<JarEntry> entries = file.entries();
			while (entries.hasMoreElements()) {
				final String name = (entries.nextElement()).getName();
				if (name.endsWith(".class")) addClass(Util.stripSuffix(name, ".class")
					.replace('/', '.'), jar);
			}
		}
		catch (final ZipException e) {
			UpdaterUserInterface.get().log("Warning: could not open " + jar);
		}
	}

	/*
	 * A couple of .jar files contain classes for backwards compatibility
	 * (e.g. batik.jar or loci_tools.jar which contain all the XML
	 * classes introduced with Java 1.4)... We do not want them to be marked
	 * as dependencies of everything.
	 */
	private boolean ignore(final String name, final String jar) {
		if (jar.endsWith("/batik.jar") || jar.endsWith("/xml-apis.jar") ||
			jar.endsWith("/loci_tools.jar")) return name.startsWith("org.xml.") ||
			name.startsWith("org.w3c.") || name.startsWith("javax.xml.") ||
			name.startsWith("org.mozilla.javascript.") ||
			name.startsWith("org.apache.xml.serializer.");
		if (jar.endsWith("/jython.jar") || jar.endsWith("/jruby.jar")) return name
			.startsWith("com.sun.jna.") ||
			name.startsWith("jline.");
		if (jar.endsWith("/ij.jar")) return name.startsWith("javax.script.");
		return false;
	}

	private void addClass(final String className, final String jar) {
		if (ignore(className, jar)) return;
		if (containsKey(className)) {
			final List<String> jarList = get(className);
			jarList.add(jar);
		}
		else {
			// Make the ArrayList initially of capacity 1, since it's
			// rare to have a class in multiple jar files.
			final ArrayList<String> jarList = new ArrayList<String>(1);
			jarList.add(jar);
			put(className, jarList);
		}
	}

	public static void printJarsForClass(final Class2JarFilesMap map,
		final String className, final boolean oneLine)
	{
		final String indent = "    ";
		final List<String> classes = map.get(className);
		if (!oneLine) System.out.print(indent);
		boolean firstTime = true;
		for (final String jarFile : classes) {
			if (firstTime) firstTime = false;
			else System.out.print(oneLine ? ", " : "\n" + indent);
			System.out.print(jarFile);
		}
		System.out.println();
	}

}
