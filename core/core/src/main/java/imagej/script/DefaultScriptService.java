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
import imagej.util.ColorRGB;
import imagej.util.ColorRGBA;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;

import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import org.scijava.Context;
import org.scijava.log.LogService;
import org.scijava.plugin.AbstractSingletonService;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.service.Service;
import org.scijava.util.ClassUtils;

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
	private ScriptLanguageIndex scriptLanguageIndex = null;

	/** Index of available scripts, by script <em>file</em>. */
	private final HashMap<File, ScriptInfo> scripts =
		new HashMap<File, ScriptInfo>();

	/** Table of short type names to associated {@link Class}. */
	private HashMap<String, Class<?>> typeMap;

	// -- ScriptService methods - scripting languages --

	/** Gets the index of available scripting languages. */
	@Override
	public synchronized ScriptLanguageIndex getIndex() {
		if (scriptLanguageIndex == null) {
			scriptLanguageIndex = new ScriptLanguageIndex();
			reloadLanguages();
			reloadScripts();
		}
		return scriptLanguageIndex;
	}

	@Override
	public List<ScriptLanguage> getLanguages() {
		return new ArrayList<ScriptLanguage>(getIndex());
	}

	@Override
	public ScriptLanguage getLanguageByExtension(final String extension) {
		return getIndex().getByExtension(extension);
	}

	@Override
	public ScriptLanguage getLanguageByName(final String name) {
		return getIndex().getByName(name);
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
	public Future<ScriptModule> run(final File file, final boolean process,
		final Object... inputs)
	{
		return run(getOrCreate(file), process, inputs);
	}

	@Override
	public Future<ScriptModule> run(final File file, final boolean process,
		final Map<String, Object> inputMap)
	{
		return run(getOrCreate(file), process, inputMap);
	}

	@Override
	public Future<ScriptModule> run(final String path, final String script,
		final boolean process, final Object... inputs)
	{
		return run(path, new StringReader(script), process, inputs);
	}

	@Override
	public Future<ScriptModule> run(final String path, final String script,
		final boolean process, final Map<String, Object> inputMap)
	{
		return run(path, new StringReader(script), process, inputMap);
	}

	@Override
	public Future<ScriptModule> run(final String path, final Reader reader,
		final boolean process, final Object... inputs)
	{
		return run(new ScriptInfo(getContext(), path, reader), process, inputs);
	}

	@Override
	public Future<ScriptModule> run(final String path, final Reader reader,
		final boolean process, final Map<String, Object> inputMap)
	{
		return run(new ScriptInfo(getContext(), path, reader), process, inputMap);
	}

	@Override
	public Future<ScriptModule> run(final ScriptInfo info, final boolean process,
		final Object... inputs)
	{
		return cast(moduleService.run(info, process, inputs));
	}

	@Override
	public Future<ScriptModule> run(final ScriptInfo info, final boolean process,
		final Map<String, Object> inputMap)
	{
		return cast(moduleService.run(info, process, inputMap));
	}

	@Override
	public boolean canHandleFile(final File file) {
		return getIndex().canHandleFile(file);
	}

	@Override
	public boolean canHandleFile(final String fileName) {
		return getIndex().canHandleFile(fileName);
	}

	@Override
	public void initialize(final ScriptEngine engine, final String fileName,
		final Writer writer, final Writer errorWriter)
	{
		engine.put(ScriptEngine.FILENAME, fileName);
		final ScriptContext context = engine.getContext();
		if (writer != null) context.setWriter(writer);
		if (writer != null) context.setErrorWriter(errorWriter);
	}

	@Override
	public synchronized Class<?> lookupClass(final String typeName)
		throws ScriptException
	{
		if (typeMap == null) buildTypes();

		final Class<?> type = typeMap.get(typeName);
		if (type != null) return type;

		final Class<?> c = ClassUtils.loadClass(typeName);
		if (c != null) {
			typeMap.put(typeName, c);
			return c;
		}

		throw new ScriptException("Unknown type: " + typeName);
	}

	// -- PTService methods --

	@Override
	public Class<ScriptLanguage> getPluginType() {
		return ScriptLanguage.class;
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

	/**
	 * Gets a {@link ScriptInfo} for the given file, creating a new one if
	 * none are registered with the service.
	 */
	private ScriptInfo getOrCreate(final File file) {
		final ScriptInfo info = getScript(file);
		if (info != null) return info;
		return new ScriptInfo(getContext(), file);
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

	private void buildTypes() {
		typeMap = new HashMap<String, Class<?>>();

		// primitives
		addTypes(boolean.class, byte.class, char.class, double.class, float.class,
			int.class, long.class, short.class);

		// primitive wrappers
		addTypes(Boolean.class, Byte.class, Character.class, Double.class,
			Float.class, Integer.class, Long.class, Short.class);

		// built-in types
		addTypes(Context.class, ColorRGB.class, ColorRGBA.class, File.class,
			String.class);

		// service types
		for (final Service service : getContext().getServiceIndex()) {
			addTypes(service.getClass());
		}
	}

	private void addTypes(final Class<?>... types) {
		for (final Class<?> type : types) {
			addType(type);
		}
	}

	private void addType(final Class<?> type) {
		if (type == null) return;
		typeMap.put(type.getSimpleName(), type);
		// NB: Recursively add supertypes.
		addType(type.getSuperclass());
		addTypes(type.getInterfaces());
	}

}
