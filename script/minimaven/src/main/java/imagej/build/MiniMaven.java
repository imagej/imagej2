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

package imagej.build;

import imagej.build.minimaven.BuildEnvironment;
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
 * TODO
 * 
 * @author Johannes Schindelin
 */
public class MiniMaven {
	public static void ensureIJDirIsSet() {
		String ijDir = System.getProperty("ij.dir");
		if (ijDir != null && new File(ijDir).isDirectory())
			return;
		ijDir = MiniMaven.class.getResource("MiniMaven.class").toString();
		for (String prefix : new String[] { "jar:", "file:" })
			if (ijDir.startsWith(prefix))
				ijDir = ijDir.substring(prefix.length());
		int bang = ijDir.indexOf("!/");
		if (bang >= 0)
			ijDir = ijDir.substring(0, bang);
		else {
			String suffix = "/" + MiniMaven.class.getName().replace('.', '/') + ".class";
			if (ijDir.endsWith(suffix))
				ijDir = ijDir.substring(0, ijDir.length() - suffix.length());
			else
				throw new RuntimeException("Funny ?-) " + ijDir);
		}
		for (String suffix : new String[] { "src-plugins/fake/target/classes", "fake.jar", "fake", "/", "jars", "/", "build", "/" })
			if (ijDir.endsWith(suffix))
				ijDir = ijDir.substring(0, ijDir.length() - suffix.length());
		System.setProperty("ij.dir", ijDir);
	}

	private final static String usage = "Usage: MiniMaven [command]\n"
		+ "\tSupported commands: compile, run, compile-and-run, clean, get-dependencies";

	public static void main(String[] args) throws Exception {
		ensureIJDirIsSet();
		PrintStream err = System.err;
		BuildEnvironment env = new BuildEnvironment(err,
			"true".equals(getSystemProperty("minimaven.download.automatically", "true")),
			"true".equals(getSystemProperty("minimaven.verbose", "false")),
			false);
		MavenProject root = env.parse(new File("pom.xml"), null);
		String command = args.length == 0 ? "compile-and-run" : args[0];
		String artifactId = getSystemProperty("artifactId", root.getArtifactId().equals("pom-ij-base") ? "ij-app" : root.getArtifactId());

		MavenProject pom = findPOM(root, artifactId);
		if (pom == null)
			pom = root;
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
		if (command.equals("clean"))
			pom.clean();
		else if (command.equals("get") || command.equals("get-dependencies"))
			pom.downloadDependencies();
		else if (command.equals("run")) {
			String mainClass = getSystemProperty("mainClass", pom.getMainClass());
			if (mainClass == null) {
				err.println("No main class specified in pom " + pom.getCoordinate());
				System.exit(1);
			}
			String[] paths = pom.getClassPath(false).split(File.pathSeparator);
			URL[] urls = new URL[paths.length];
			for (int i = 0; i < urls.length; i++)
				urls[i] = new URL("file:" + paths[i] + (paths[i].endsWith(".jar") ? "" : "/"));
			URLClassLoader classLoader = new URLClassLoader(urls);
			// needed for sezpoz
			Thread.currentThread().setContextClassLoader(classLoader);
			Class<?> clazz = classLoader.loadClass(mainClass);
			Method main = clazz.getMethod("main", new Class[] { String[].class });
			main.invoke(null, new Object[] { new String[0] });
		}
		else if (command.equals("classpath"))
			err.println(pom.getClassPath(false));
		else if (command.equals("list")) {
			Set<MavenProject> result = new TreeSet<MavenProject>();
			Stack<MavenProject> stack = new Stack<MavenProject>();
			stack.push(pom.getRoot());
			while (!stack.empty()) {
				pom = stack.pop();
				if (result.contains(pom) || !pom.getBuildFromSource())
					continue;
				result.add(pom);
				for (MavenProject child : pom.getChildren())
					stack.push(child);
			}
			for (MavenProject pom2 : result)
				System.err.println(pom2);
		}
		else
			err.println("Unhandled command: " + command + "\n" + usage);
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
