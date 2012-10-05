package fiji.build;

import java.io.File;
import java.io.PrintStream;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Set;
import java.util.Stack;
import java.util.TreeSet;

import fiji.build.minimaven.BuildEnvironment;
import fiji.build.minimaven.POM;

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
		POM root = env.parse(new File("pom.xml"), null);
		String command = args.length == 0 ? "compile-and-run" : args[0];
		String artifactId = getSystemProperty("artifactId", root.getArtifactId().equals("pom-ij-base") ? "ij-app" : root.getArtifactId());

		POM pom = findPOM(root, artifactId);
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
			Set<POM> result = new TreeSet<POM>();
			Stack<POM> stack = new Stack<POM>();
			stack.push(pom.getRoot());
			while (!stack.empty()) {
				pom = stack.pop();
				if (result.contains(pom) || !pom.getBuildFromSource())
					continue;
				result.add(pom);
				for (POM child : pom.getChildren())
					stack.push(child);
			}
			for (POM pom2 : result)
				System.err.println(pom2);
		}
		else
			err.println("Unhandled command: " + command + "\n" + usage);
	}

	protected static POM findPOM(POM root, String artifactId) {
		if (artifactId == null || artifactId.equals(root.getArtifactId()))
			return root;
		for (POM child : root.getChildren()) {
			POM pom = findPOM(child, artifactId);
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
