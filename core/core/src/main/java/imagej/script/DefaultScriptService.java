/*
 * #%L
 * ImageJ software for multidimensional image processing and analysis.
 * %%
 * Copyright (C) 2009 - 2014 Board of Regents of the University of
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
 * #L%
 */

package imagej.script;

import imagej.command.CommandService;
import imagej.module.Module;
import imagej.module.ModuleService;
import imagej.util.AppUtils;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;

import javax.script.ScriptEngineFactory;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import org.scijava.Context;
import org.scijava.Gateway;
import org.scijava.InstantiableException;
import org.scijava.Priority;
import org.scijava.log.LogService;
import org.scijava.object.LazyObjects;
import org.scijava.plugin.AbstractSingletonService;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.plugin.PluginInfo;
import org.scijava.plugin.PluginService;
import org.scijava.service.Service;
import org.scijava.util.ClassUtils;
import org.scijava.util.ColorRGB;
import org.scijava.util.ColorRGBA;

/**
 * Default service for working with scripting languages.
 * 
 * @author Johannes Schindelin
 * @author Curtis Rueden
 */
@Plugin(type = Service.class, priority = Priority.HIGH_PRIORITY)
public class DefaultScriptService extends
	AbstractSingletonService<ScriptLanguage> implements ScriptService
{

	@Parameter
	private PluginService pluginService;

	@Parameter
	private ModuleService moduleService;

	@Parameter
	private CommandService commandService;

	@Parameter
	private LogService log;

	/** Index of registered scripting languages. */
	private ScriptLanguageIndex scriptLanguageIndex;

	/** List of directories to scan for scripts. */
	private ArrayList<File> scriptDirs;

	/** Index of available scripts, by script <em>file</em>. */
	private HashMap<File, ScriptInfo> scripts;

	/** Table of short type names to associated {@link Class}. */
	private HashMap<String, Class<?>> aliasMap;

	// -- ScriptService methods - scripting languages --

	/** Gets the index of available scripting languages. */
	@Override
	public ScriptLanguageIndex getIndex() {
		return scriptLanguageIndex();
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
	public List<File> getScriptDirectories() {
		return Collections.unmodifiableList(scriptDirs());
	}

	@Override
	public Collection<ScriptInfo> getScripts() {
		return Collections.unmodifiableCollection(scripts().values());
	}

	@Override
	public ScriptInfo getScript(final File scriptFile) {
		return scripts().get(scriptFile);
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
	public void addAlias(final Class<?> type) {
		addAlias(type.getSimpleName(), type);
	}

	@Override
	public void addAlias(final String alias, final Class<?> type) {
		aliasMap().put(alias, type);
	}

	@Override
	public synchronized Class<?> lookupClass(final String alias)
		throws ScriptException
	{
		final Class<?> type = aliasMap().get(alias);
		if (type != null) return type;

		final Class<?> c = ClassUtils.loadClass(alias);
		if (c != null) {
			aliasMap().put(alias, c);
			return c;
		}

		throw new ScriptException("Unknown type: " + alias);
	}

	// -- PTService methods --

	@Override
	public Class<ScriptLanguage> getPluginType() {
		return ScriptLanguage.class;
	}

	// -- Service methods --

	@Override
	public void initialize() {
		super.initialize();

		// add scripts to the module index... only when needed!
		moduleService.getIndex().addLater(new LazyObjects<ScriptInfo>() {

			@Override
			public Collection<ScriptInfo> get() {
				return scripts().values();
			}

		});
	}

	// -- Helper methods - lazy initialization --

	/** Gets {@link #scriptLanguageIndex}, initializing if needed. */
	private ScriptLanguageIndex scriptLanguageIndex() {
		if (scriptLanguageIndex == null) initScriptLanguageIndex();
		return scriptLanguageIndex;
	}

	/** Gets {@link #scriptDirs}, initializing if needed. */
	private List<File> scriptDirs() {
		if (scriptDirs == null) initScriptDirs();
		return scriptDirs;
	}

	/** Gets {@link #scripts}, initializing if needed. */
	private HashMap<File, ScriptInfo> scripts() {
		if (scripts == null) initScripts();
		return scripts;
	}

	/** Gets {@link #aliasMap}, initializing if needed. */
	private HashMap<String, Class<?>> aliasMap() {
		if (aliasMap == null) initAliasMap();
		return aliasMap;
	}

	/** Initializes {@link #scriptLanguageIndex}. */
	private synchronized void initScriptLanguageIndex() {
		if (scriptLanguageIndex != null) return; // already initialized

		final ScriptLanguageIndex index = new ScriptLanguageIndex();

		// add ScriptLanguage plugins
		for (final ScriptLanguage language : getInstances()) {
			index.add(language, false);
		}

		// Now look for the ScriptEngines in javax.scripting. We only do that
		// now since the javax.scripting framework does not provide all the
		// functionality we might want to use in ImageJ2.
		final ScriptEngineManager manager = new ScriptEngineManager();
		for (final ScriptEngineFactory factory : manager.getEngineFactories()) {
			index.add(factory, true);
		}

		scriptLanguageIndex = index;
	}

	/** Initializes {@link #scriptDirs}. */
	private synchronized void initScriptDirs() {
		if (scriptDirs != null) return;

		final ArrayList<File> dirs = new ArrayList<File>();

		// append default script directories
		final File baseDir = AppUtils.getBaseDirectory();
		dirs.add(new File(baseDir, "scripts"));

		// append additional script directories from system property
		final String scriptsPath = System.getProperty(SCRIPTS_PATH_PROPERTY);
		if (scriptsPath != null) {
			for (final String dir : scriptsPath.split(File.pathSeparator)) {
				dirs.add(new File(dir));
			}
		}

		scriptDirs = dirs;
	}

	/** Initializes {@link #scripts}. */
	private synchronized void initScripts() {
		if (scripts != null) return; // already initialized

		final HashMap<File, ScriptInfo> map = new HashMap<File, ScriptInfo>();

		final ArrayList<ScriptInfo> scriptList = new ArrayList<ScriptInfo>();
		new ScriptFinder(this).findScripts(scriptList);

		for (final ScriptInfo info : scriptList) {
			map.put(asFile(info.getPath()), info);
		}

		scripts = map;
	}

	/** Initializes {@link #aliasMap}. */
	private synchronized void initAliasMap() {
		if (aliasMap != null) return; // already initialized

		final HashMap<String, Class<?>> map = new HashMap<String, Class<?>>();

		// primitives
		addAliases(map, boolean.class, byte.class, char.class, double.class,
			float.class, int.class, long.class, short.class);

		// primitive wrappers
		addAliases(map, Boolean.class, Byte.class, Character.class, Double.class,
			Float.class, Integer.class, Long.class, Short.class);

		// built-in types
		addAliases(map, Context.class, BigDecimal.class, BigInteger.class,
			ColorRGB.class, ColorRGBA.class, File.class, String.class);

		// service types
		for (final Service service : getContext().getServiceIndex()) {
			addAliases(map, service.getClass());
		}

		// gateway types
		final List<PluginInfo<Gateway>> gatewayPlugins =
			pluginService.getPluginsOfType(Gateway.class);
		for (final PluginInfo<Gateway> info : gatewayPlugins) {
			try {
				addAliases(map, info.loadClass());
			}
			catch (final InstantiableException exc) {
				log.warn("Ignoring invalid gateway: " + info.getClassName(), exc);
			}
		}

		aliasMap = map;
	}

	private void addAliases(final HashMap<String, Class<?>> map,
		final Class<?>... types)
	{
		for (final Class<?> type : types) {
			addAlias(map, type);
		}
	}

	private void
		addAlias(final HashMap<String, Class<?>> map, final Class<?> type)
	{
		if (type == null) return;
		map.put(type.getSimpleName(), type);
		// NB: Recursively add supertypes.
		addAlias(map, type.getSuperclass());
		addAliases(map, type.getInterfaces());
	}

	// -- Helper methods - run --

	/**
	 * Gets a {@link ScriptInfo} for the given file, creating a new one if none
	 * are registered with the service.
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

}
