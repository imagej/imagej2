//
// DefaultScriptService.java
//

/*
ImageJ software for multidimensional image processing and analysis.

Copyright (c) 2010, ImageJDev.org.
All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met:
    * Redistributions of source code must retain the above copyright
      notice, this list of conditions and the following disclaimer.
    * Redistributions in binary form must reproduce the above copyright
      notice, this list of conditions and the following disclaimer in the
      documentation and/or other materials provided with the distribution.
    * Neither the names of the ImageJDev.org developers nor the
      names of its contributors may be used to endorse or promote products
      derived from this software without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
POSSIBILITY OF SUCH DAMAGE.
*/

package imagej.script;

import imagej.InstantiableException;
import imagej.command.CommandInfo;
import imagej.log.LogService;
import imagej.plugin.Parameter;
import imagej.plugin.Plugin;
import imagej.plugin.PluginInfo;
import imagej.plugin.PluginService;
import imagej.service.AbstractService;
import imagej.service.Service;
import imagej.util.FileUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

/**
 * A service discovering all available script languages and convenience methods
 * to interact with them
 * 
 * @author Johannes Schindelin
 */
@Plugin(type = Service.class)
public class DefaultScriptService extends AbstractService implements ScriptService {

	@Parameter
	private PluginService pluginService;

	@Parameter
	private LogService log;

	public final static String CONTEXT = "IJ";

	/** Index of registered script languages. */
	private final ScriptLanguageIndex scriptLanguageIndex =
		new ScriptLanguageIndex();

	@Override
	public void initialize() {
		reloadScriptLanguages();

		final ArrayList<CommandInfo<?>> plugins = new ArrayList<CommandInfo<?>>();
		new ScriptFinder(this).findPlugins(plugins);
		pluginService.addPlugins(plugins);

	}

	// -- ScriptService methods --

	@Override
	public PluginService getPluginService() {
		return pluginService;
	}

	@Override
	public LogService getLogService() {
		return log;
	}

	/** Gets the index of available script languages. */
	@Override
	public ScriptLanguageIndex getIndex() {
		return scriptLanguageIndex;
	}

	@Override
	public List<ScriptEngineFactory> getLanguages() {
		return new ArrayList<ScriptEngineFactory>(scriptLanguageIndex);
	}

	@Override
	public ScriptEngineFactory getByFileExtension(final String fileExtension) {
		return scriptLanguageIndex.getByFileExtension(fileExtension);
	}

	@Override
	public ScriptEngineFactory getByName(final String name) {
		return scriptLanguageIndex.getByName(name);
	}

	@Override
	public Object eval(final File file) throws FileNotFoundException,
		ScriptException
	{
		final String fileExtension = FileUtils.getExtension(file);
		final ScriptEngineFactory language = getByFileExtension(fileExtension);
		if (language == null) {
			throw new UnsupportedOperationException(
				"Could not determine language for file extension " + fileExtension);
		}
		final ScriptEngine engine = language.getScriptEngine();
		initialize(engine, file.getPath(), null, null);
		return engine.eval(new FileReader(file));
	}

	@Override
	public Object eval(final String filename, final Reader reader)
			throws IOException, ScriptException {
		final String fileExtension = FileUtils.getExtension(filename);
		final ScriptEngineFactory language = getByFileExtension(fileExtension);
		if (language == null) {
			throw new UnsupportedOperationException(
				"Could not determine language for file extension " + fileExtension);
		}
		final ScriptEngine engine = language.getScriptEngine();
		initialize(engine, filename, null, null);
		return engine.eval(reader);
	}

	@Override
	public boolean canHandleFile(final File file) {
		return scriptLanguageIndex.canHandleFile(file);
	}

	public void reloadScriptLanguages() {
		scriptLanguageIndex.clear();
		for (final PluginInfo<? extends ScriptLanguage> item : pluginService.getPluginsOfType(ScriptLanguage.class))
		{
			try {
				final ScriptEngineFactory language = item.createInstance();
				scriptLanguageIndex.add(language, false);
			} catch (InstantiableException e) {
				log.error("Invalid script language: " + item, e);
			}
		}

		/*
		 *  Now look for the ScriptEngines in javax.scripting. We only do that
		 *  now since the javax.scripting framework does not provide all the
		 *  functionality we might want to use in ImageJ2.
		 */
		final ScriptEngineManager manager = new javax.script.ScriptEngineManager();
		for (final ScriptEngineFactory factory : manager.getEngineFactories()) {
			scriptLanguageIndex.add(factory, true);
		}
	}

	@Override
	public void initialize(final ScriptEngine engine, final String fileName,
		final Writer writer, final Writer errorWriter)
	{
		engine.put(ScriptEngine.FILENAME, fileName);
		engine.put(CONTEXT, getContext());
		final ScriptContext context = engine.getContext();
		if (writer != null) context.setWriter(writer);
		if (writer != null) context.setErrorWriter(errorWriter);
	}

}
