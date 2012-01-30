
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

	public Class2JarFilesMap() {
		addDirectory("plugins");
		addDirectory("jars");
	}

	private void addDirectory(final String directory) {
		final File dir = new File(Util.fijiRoot + "/" + directory);
		if (!dir.isDirectory()) return;
		final String[] list = dir.list();
		for (int i = 0; i < list.length; i++) {
			final String path = directory + "/" + list[i];
			if (list[i].endsWith(".jar")) try {
				addJar(path);
			}
			catch (final IOException e) {
				UserInterface.get().log("Warning: could not open " + path);
			}
			else addDirectory(path);
		}
	}

	private void addJar(final String jar) throws IOException {
		try {
			final JarFile file = new JarFile(Util.fijiRoot + "/" + jar);
			final Enumeration<JarEntry> entries = file.entries();
			while (entries.hasMoreElements()) {
				final String name = (entries.nextElement()).getName();
				if (name.endsWith(".class")) addClass(Util.stripSuffix(name, ".class")
					.replace('/', '.'), jar);
			}
		}
		catch (final ZipException e) {
			UserInterface.get().log("Warning: could not open " + jar);
		}
	}

	/*
	 * batik.jar contains these, for backwards compatibility, but we
	 * do not want to have batik.jar as a dependency for every XML
	 * handling plugin...
	 */
	private boolean ignore(final String name, final String jar) {
		if (jar.endsWith("/batik.jar")) return name.startsWith("org.xml.") ||
			name.startsWith("org.w3c.") || name.startsWith("javax.xml.") ||
			name.startsWith("org.mozilla.javascript.");
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

	public static void main(final String[] args) {
		final Class2JarFilesMap map = new Class2JarFilesMap();

		if (args.length == 0) for (final String className : map.keySet()) {
			System.out.print("class " + className +
				" is in the following jar files: ");
			printJarsForClass(map, className, true);
		}
		else for (int i = 0; i < args.length; i++) {
			System.out.println("class " + args[i] +
				" is in the following jar files: ");
			printJarsForClass(map, args[i], false);
		}
	}
}
