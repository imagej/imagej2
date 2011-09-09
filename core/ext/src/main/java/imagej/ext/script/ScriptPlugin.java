package imagej.ext.script;

import imagej.ext.plugin.ImageJPlugin;
import imagej.ext.plugin.Parameter;
import imagej.ext.plugin.Plugin;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

/**
 * Executes a script.
 * 
 * @author Johannes Schindelin
 */
@Plugin(menuPath = "Plugins>Script>Run Script")
public class ScriptPlugin implements ImageJPlugin {

	@Parameter
	protected File path;

	@Override
	public void run() {
		// TODO make a nice SezPoz-discoverable interface for scripting
		// languages
		final ScriptEngineManager scriptManager = new ScriptEngineManager();
		final ScriptEngine javascript = scriptManager
				.getEngineByName("JavaScript");
		try {
			javascript.put(ScriptEngine.FILENAME, path.getPath());
			final Object result = javascript.eval(new FileReader(path));
			if (result != null)
				System.out.println(result.toString());
		} catch (final ScriptException e) {
			e.printStackTrace(System.err);
		} catch (final IOException e) {
			e.printStackTrace(System.err);
		}
	}

}
