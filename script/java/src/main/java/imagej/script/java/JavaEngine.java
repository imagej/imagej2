package imagej.script.java;

import imagej.build.minimaven.BuildEnvironment;
import imagej.build.minimaven.MavenProject;
import imagej.script.AbstractScriptEngine;
import imagej.util.LineOutputStream;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.io.Reader;
import java.io.StringReader;
import java.io.Writer;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.script.ScriptException;

public class JavaEngine extends AbstractScriptEngine {

	{
		engineScopeBindings = new JavaEngineBindings();
	}

	@Override
	public Object eval(String script) throws ScriptException {
		return eval(new StringReader(script));
	}

	@Override
	public Object eval(Reader reader) throws ScriptException {
		File file = new File((String)get(FILENAME));
		if (!file.exists()) {
			throw new ScriptException("TODO: write temporary file");
		}
		String mainClass = null;
		final File pomFile;
		if (file.getName().equals("pom.xml")) {
			pomFile = file;
		} else {
			try {
				mainClass = getFullClassName(file);
			} catch (IOException e) {
				throw new ScriptException(e);
			}
			pomFile = getPOMFile(file);
		}
		final Writer writer = getContext().getErrorWriter();
		final PrintStream err = writer == null ? System.err : new PrintStream(new LineOutputStream() {

			@Override
			public void println(final String line) throws IOException {
				writer.append(line).append('\n');
			}

		});
		BuildEnvironment env = new BuildEnvironment(err, true, true, false);
		try {
			MavenProject project = env.parse(pomFile, null);
			project.build(true);
			if (mainClass == null) {
				mainClass = project.getMainClass();
				if (mainClass == null) {
					throw new ScriptException("No main class found for file " + file);
				}
			}

			// make class loader
			String[] paths = project.getClassPath(false).split(File.pathSeparator);
			URL[] urls = new URL[paths.length];
			for (int i = 0; i < urls.length; i++)
				urls[i] = new URL("file:" + paths[i] + (paths[i].endsWith(".jar") ? "" : "/"));
			URLClassLoader classLoader = new URLClassLoader(urls);

			// needed for sezpoz
			Thread.currentThread().setContextClassLoader(classLoader);

			// launch main class
			Class<?> clazz = classLoader.loadClass(mainClass);
			Method main = clazz.getMethod("main", new Class[] { String[].class });
			main.invoke(null, new Object[] { new String[0] });
		} catch (Exception e) {
			throw new ScriptException(e);
		}
		return null;
	}

	private File getPOMFile(final File file) throws ScriptException {
		for (File dir = file.isDirectory() ? file : file.getParentFile(); dir != null; dir = dir.getParentFile()) {
			final File candidate = new File(dir, "pom.xml");
			if (candidate.exists()) {
				return candidate;
			}
		}
		throw new ScriptException("TODO: Generate pom.xml on the fly");
	}

	private static String getFullClassName(final File file) throws IOException {
		String name = file.getName();
		if (!name.endsWith(".java")) {
			throw new UnsupportedOperationException();
		}
		name = name.substring(0, name.length() - 5);

		Pattern packagePattern = Pattern.compile("package ([a-zA-Z0-9_]*).*");
		final BufferedReader reader = new BufferedReader(new FileReader(file));
		for (;;) {
			String line = reader.readLine().trim();
			if (line == null) return name;
			Matcher matcher = packagePattern.matcher(line);
			while (line.startsWith("/*")) {
				int end = line.indexOf("*/", 2);
				while (end < 0) {
						line = reader.readLine();
						if (line == null) return name;
						end = line.indexOf("*/");
				}
				line = line.substring(end + 2).trim();
			}
			if (line.equals("") || line.startsWith("//")) continue;
			if (matcher.matches()) {
				return matcher.group(1) + "." + name;
			}
			return name; // the 'package' statement must be the first in the file
		}
	}
}
