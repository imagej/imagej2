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

package imagej.ext.script;

import imagej.ext.plugin.ImageJPlugin;
import imagej.ext.plugin.Parameter;
import imagej.ext.plugin.Plugin;
import imagej.util.FileUtils;
import imagej.util.Log;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

/**
 * Executes a script, using the file extension to choose the appropriate engine.
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
		final String fileExtension = FileUtils.getExtension(path);
		final ScriptEngine engine =
			scriptManager.getEngineByExtension(fileExtension);
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
		}
		catch (final ScriptException e) {
			Log.error(e);
		}
		catch (final IOException e) {
			Log.error(e);
		}
	}

}
