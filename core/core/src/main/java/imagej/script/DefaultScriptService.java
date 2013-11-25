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

package imagej.script;

import imagej.command.CommandService;
import imagej.module.Module;
import imagej.module.ModuleService;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Future;

import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;
import javax.script.ScriptEngineManager;

import org.scijava.log.LogService;
import org.scijava.plugin.AbstractSingletonService;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.service.Service;

/**
 * Default service for working with scripting languages.
 * 
 * @author Johannes Schindelin
 * @author Curtis Rueden
 */
@Plugin(type = Service.class)
public class DefaultScriptService extends
	AbstractSingletonService<ScriptLanguage> implements ScriptService
{

	@Parameter
	private ModuleService moduleService;

	@Parameter
	private CommandService commandService;

	@Parameter
	private LogService log;

	/** Index of registered scripting languages. */
	private final ScriptLanguageIndex scriptLanguageIndex =
		new ScriptLanguageIndex();

	/** Index of available scripts, by script <em>file</em>. */
	private final HashMap<File, ScriptInfo> scripts =
		new HashMap<File, ScriptInfo>();

	// -- ScriptService methods - scripting languages --

	/** Gets the index of available scripting languages. */
	@Override
	public ScriptLanguageIndex getIndex() {
		return scriptLanguageIndex;
	}

	@Override
	public List<ScriptLanguage> getLanguages() {
		return new ArrayList<ScriptLanguage>(scriptLanguageIndex);
	}

	@Override
	public ScriptLanguage getByFileExtension(final String fileExtension) {
		return scriptLanguageIndex.getByFileExtension(fileExtension);
	}

	@Override
	public ScriptLanguage getByName(final String name) {
		return scriptLanguageIndex.getByName(name);
	}

	// -- ScriptService methods - scripts --

	@Override
	public Collection<ScriptInfo> getScripts() {
		return Collections.unmodifiableCollection(scripts.values());
	}

	@Override
	public ScriptInfo getScript(final File scriptFile) {
		return scripts.get(scriptFile);
	}

	@Override
	public Future<ScriptModule> run(final File file) {
		// lookup existing script
		ScriptInfo info = getScript(file);
		if (info == null) {
			// wrap file as a script ad hoc
			info = new ScriptInfo(getContext(), file);
		}
		return cast(commandService.run(info));
	}

	@Override
	public Future<ScriptModule> run(final String path, final Reader reader) {
		final ScriptInfo info = new ScriptInfo(getContext(), path, reader);
		return cast(commandService.run(info));
	}

	@Override
	public boolean canHandleFile(final File file) {
		return scriptLanguageIndex.canHandleFile(file);
	}

	@Override
	public boolean canHandleFile(final String fileName) {
		return scriptLanguageIndex.canHandleFile(fileName);
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

	// -- PTService methods --

	@Override
	public Class<ScriptLanguage> getPluginType() {
		return ScriptLanguage.class;
	}

	// -- Service methods --

	@Override
	public void initialize() {
		reloadLanguages();
		reloadScripts();
	}

	// -- Helper methods --

	private void reloadLanguages() {
		// remove previously discovered scripting languages
		scriptLanguageIndex.clear();

		// add ScriptLanguage plugins
		for (final ScriptLanguage language : getInstances()) {
			scriptLanguageIndex.add(language, false);
		}

		// Now look for the ScriptEngines in javax.scripting. We only do that
		// now since the javax.scripting framework does not provide all the
		// functionality we might want to use in ImageJ2.
		final ScriptEngineManager manager = new javax.script.ScriptEngineManager();
		for (final ScriptEngineFactory factory : manager.getEngineFactories()) {
			scriptLanguageIndex.add(factory, true);
		}
	}

	private void reloadScripts() {
		// remove previously discovered scripts
		moduleService.removeModules(scripts.values());
		scripts.clear();

		// discover available scripts
		final ArrayList<ScriptInfo> scriptList = new ArrayList<ScriptInfo>();
		new ScriptFinder(this).findScripts(scriptList);

		// add newly discovered scripts
		for (final ScriptInfo info : scriptList) {
			scripts.put(asFile(info.getPath()), info);
		}
		moduleService.addModules(scriptList);
	}

	private File asFile(final String path) {
		final File file = new File(path);
		try {
			return file.getCanonicalFile();
		}
		catch (final IOException exc) {
			log.warn(exc);
			return file.getAbsoluteFile();
		}
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private Future<ScriptModule> cast(final Future<Module> future) {
		return (Future) future;
	}

}
