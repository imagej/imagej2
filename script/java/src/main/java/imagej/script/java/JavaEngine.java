package imagej.script.java;

import imagej.build.minimaven.BuildEnvironment;
import imagej.build.minimaven.MavenProject;
import imagej.script.AbstractScriptEngine;
import imagej.util.LineOutputStream;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.io.Reader;
import java.io.StringReader;
import java.io.Writer;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;

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
		if (file.getName().equals("pom.xml")) {
			final Writer writer = getContext().getErrorWriter();
			final PrintStream err = writer == null ? System.err : new PrintStream(new LineOutputStream() {

				@Override
				public void println(final String line) throws IOException {
					writer.append(line).append('\n');
				}

			});
			BuildEnvironment env = new BuildEnvironment(err, true, true, false);
			try {
				MavenProject pom = env.parse(file, null);
				pom.build(true);
				String mainClass = pom.getMainClass();

				// make class loader
				String[] paths = pom.getClassPath(false).split(File.pathSeparator);
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

		}
		return null;
	}

}
