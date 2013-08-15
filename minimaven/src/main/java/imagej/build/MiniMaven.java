/*
 * #%L
 * ImageJ software for multidimensional image processing and analysis.
 * %%
 * Copyright (C) 2009 - 2013 Board of Regents of the University of
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

package imagej.build;

import imagej.build.minimaven.BuildEnvironment;
import imagej.build.minimaven.Coordinate;
import imagej.build.minimaven.MavenProject;

import java.io.File;
import java.io.PrintStream;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Set;
import java.util.Stack;
import java.util.TreeSet;

/**
 * The main-class for a simple and small drop-in replacement of Maven.
 * 
 * Naturally, MiniMaven does not pretend to be as powerful as Maven. But it does the job
 * as far as ImageJ2 and Fiji are concerned, and it does not download half the internet
 * upon initial operation.
 * 
 * @author Johannes Schindelin
 */
public class MiniMaven {
	private final static void usage() {
		System.err.println("Usage: MiniMaven [options...] [command]\n\n"
				+ "Supported commands:\n"
				+ "compile\n"
				+ "\tcompile the project\n"
				+ "jar\n"
				+ "\tcompile the project into a .jar file\n"
				+ "install\n"
				+ "\tcompile & install the project and its dependencies\n"
				+ "run\n"
				+ "\trun the project\n"
				+ "compile-and-run\n"
				+ "\tcompile and run the project\n"
				+ "clean\n"
				+ "\tclean the project\n"
				+ "get-dependencies\n"
				+ "\tdownload the dependencies of the project\n"
				+ "list\n"
				+ "\tshow list of projects\n"
				+ "dependency-tree\n"
				+ "\tshow the tree of depending projects\n\n"
				+ "Options:\n"
				+ "-D<key>=<value>\n"
				+ "\tset a system property");
		System.exit(1);
	}

	public static void main(String[] args) throws Exception {
		int offset;
		for (offset = 0; offset < args.length && args[offset].charAt(0) == '-'; offset++) {
			final String option = args[offset];
			if (option.startsWith("-D")) {
				final int equals = option.indexOf('=', 2);
				final String value;
				if (equals < 0) value = "true";
				else value = option.substring(equals + 1);
				System.setProperty(option.substring(2, equals < 0 ? option.length() : equals), value);
			}
			if (option.equals("-U")) {
				System.setProperty("minimaven.updateinterval", "0");
			}
			else {
				usage();
			}
		}
		String command = "compile-and-run";
		if (args.length == offset + 1)
			command = args[offset];
		else if (args.length > offset + 1)
			usage();

		final PrintStream err = System.err;
		final BuildEnvironment env = new BuildEnvironment(err,
			"true".equals(getSystemProperty("minimaven.download.automatically", "true")),
			"true".equals(getSystemProperty("minimaven.verbose", "false")),
			"true".equals(getSystemProperty("minimaven.debug", "false")));
		final MavenProject root = env.parse(new File("pom.xml"), null);
		final String artifactId = getSystemProperty("artifactId", root.getArtifactId().equals("pom-ij-base") || root.getArtifactId().equals("pom-imagej") ? "ij-app" : root.getArtifactId());

		MavenProject pom = findPOM(root, artifactId);
		if (pom == null) {
			final String specifiedArtifactId = System.getProperty("artifactId");
			if (specifiedArtifactId != null) {
				System.err.println("Could not find project for artifactId '" + artifactId + "'!");
				System.exit(1);
			}
			pom = root;
		}
		if (command.equals("compile") || command.equals("build") || command.equals("compile-and-run")) {
			pom.build();
			if (command.equals("compile-and-run"))
				command = "run";
			else
				return;
		}
		else if (command.equals("jar") || command.equals("jars")) {
			if (!pom.getBuildFromSource()) {
				System.err.println("Cannot build " + pom + " from source");
				System.exit(1);
			}
			pom.buildJar();
			if (command.equals("jars"))
				pom.copyDependencies(pom.getTarget(), true);
			return;
		}
		else if (command.equals("install")) {
			pom.buildAndInstall();
			return;
		}
		if (command.equals("clean"))
			pom.clean();
		else if (command.equals("get") || command.equals("get-dependencies"))
			pom.downloadDependencies();
		else if (command.equals("run")) {
			final String mainClass = getSystemProperty("mainClass", pom.getMainClass());
			if (mainClass == null) {
				err.println("No main class specified in pom " + pom.getCoordinate());
				System.exit(1);
			}
			final String[] paths = pom.getClassPath(false).split(File.pathSeparator);
			final URL[] urls = new URL[paths.length];
			for (int i = 0; i < urls.length; i++)
				urls[i] = new URL("file:" + paths[i] + (paths[i].endsWith(".jar") ? "" : "/"));
			final URLClassLoader classLoader = new URLClassLoader(urls);
			// needed for sezpoz
			Thread.currentThread().setContextClassLoader(classLoader);
			final Class<?> clazz = classLoader.loadClass(mainClass);
			final Method main = clazz.getMethod("main", new Class[] { String[].class });
			main.invoke(null, new Object[] { new String[0] });
		}
		else if (command.equals("classpath"))
			err.println(pom.getClassPath(false));
		else if (command.equals("list")) {
			final Set<MavenProject> result = new TreeSet<MavenProject>();
			final Stack<MavenProject> stack = new Stack<MavenProject>();
			stack.push(pom.getRoot());
			while (!stack.empty()) {
				pom = stack.pop();
				if (result.contains(pom) || !pom.getBuildFromSource())
					continue;
				result.add(pom);
				for (MavenProject child : pom.getChildren())
					stack.push(child);
			}
			for (final MavenProject pom2 : result)
				System.err.println(pom2);
		}
		else if (command.equals("dependency-tree")) {
			final MavenProject parent = pom.getParent();
			if (parent != null) {
				err.println("(parent: " + parent.getGAV() + ")");
			}
			showDependencyTree(err, pom, "");
		}
		else {
			err.println("Unhandled command: " + command);
			usage();
		}
	}

	protected static void showDependencyTree(final PrintStream err, final MavenProject pom, final String prefix) {
		err.println(prefix + pom.getGAV());
		if ("pom".equals(pom.getPackaging())) {
			for (final MavenProject child : pom.getChildren()) {
				showDependencyTree(err, child, prefix + "\t");
			}
		} else {
			for (final Coordinate coordinate : pom.getDirectDependencies()) try {
				final MavenProject dependency = pom.findPOM(coordinate, true, false);
				if (dependency == null) {
					err.println(prefix + coordinate.getGAV() + " (not found)");
				} else {
					showDependencyTree(err, dependency, prefix + "\t");
				}
			} catch (final Throwable t) {
				err.println(prefix + coordinate.getGAV() + ": " + t);
			}
		}
	}

	protected static void showTree(final PrintStream err, final MavenProject pom, final String prefix) {
		err.println(prefix + pom.getGAV());
		final MavenProject[] children = pom.getChildren();
		for (int i = 0; i < children.length; i++) {
			showTree(err, children[i], prefix + "\t");
		}
	}

	protected static MavenProject findPOM(MavenProject root, String artifactId) {
		if (artifactId == null || artifactId.equals(root.getArtifactId()))
			return root;
		for (MavenProject child : root.getChildren()) {
			MavenProject pom = findPOM(child, artifactId);
			if (pom != null)
				return pom;
		}
		return null;
	}

	protected static String getSystemProperty(String key, String defaultValue) {
		String result = System.getProperty(key);
		return result == null ? defaultValue : result;
	}
}
