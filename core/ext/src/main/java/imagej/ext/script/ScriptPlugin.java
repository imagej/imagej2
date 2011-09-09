package imagej.ext.script;

import imagej.ext.plugin.ImageJPlugin;
import imagej.ext.plugin.Parameter;
import imagej.ext.plugin.Plugin;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import sun.org.mozilla.javascript.Context;
import sun.org.mozilla.javascript.ImporterTopLevel;
import sun.org.mozilla.javascript.Scriptable;

/**
 * Executes a script.
 * 
 * @author Johannes Schindelin
 */
@SuppressWarnings("restriction")
@Plugin(menuPath = "Plugins>Script>Run Script")
public class ScriptPlugin implements ImageJPlugin {

	@Parameter
	protected File path;

	@Override
	public void run() {
		// TODO make a nice SezPoz-discoverable interface for scripting
		// languages
		final Context context = Context.enter();
		final Scriptable scope = new ImporterTopLevel(context);
		try {
			final Object result = context.evaluateReader(scope, new FileReader(
					path), path.getName(), 1, null);
			if (result != null)
				System.out.println(result.toString());
		} catch (final IOException e) {
			e.printStackTrace(System.err);
		}
	}

}
