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
 * Using the file extension to choose the appropriate engine.
 * 
 * @author Johannes Schindelin
 * @author Grant Harris
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
		// Could use a FileChooser to select script, then
		String fileExtension = getFileExtension(path.getPath());
		final ScriptEngine engine = scriptManager.getEngineByExtension(fileExtension);
		try {
			engine.put(ScriptEngine.FILENAME, path.getPath());
			// TODO
			// Bind java objects to script engine and for script access
			// e.g. get current Display
			// scriptEngine.put("currentDisplay", currentDisplay) ;
			// same effect as: getBindings(ScriptContext.ENGINE_SCOPE).put. 
			final Object result = engine.eval(new FileReader(path));
			if (result != null) {
				System.out.println(result.toString());
			}
		} catch (final ScriptException e) {
			e.printStackTrace(System.err);
		} catch (final IOException e) {
			e.printStackTrace(System.err);
		}
	}

	String getFileExtension(String filePath) {
		File f = new File(filePath);
		String name = f.getName();
		int k = name.lastIndexOf(".");
		String ext = null;
		if (k != -1) {
			ext = name.substring(k + 1, name.length());
		}
		return ext;
	}

}
