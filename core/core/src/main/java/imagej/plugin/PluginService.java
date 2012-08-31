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

package imagej.plugin;

import imagej.module.Module;
import imagej.module.ModuleInfo;
import imagej.module.ModuleService;
import imagej.service.Service;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;

/**
 * Interface for service that keeps track of available plugins.
 * <p>
 * The plugin service keeps a master index of all plugins known to the system.
 * At heart, a plugin is a piece of functionality that extends ImageJ's
 * capabilities. Plugins take many forms; see {@link IPlugin} for details.
 * </p>
 * <p>
 * The default plugin service discovers available plugins on the classpath.
 * Plugins that are runnable as modules are reported to the
 * {@link ModuleService}.
 * </p>
 * <p>
 * A <em>plugin</em> is distinct from a <em>module</em> in that plugins extend
 * ImageJ's functionality in some way, taking many forms, whereas modules are
 * always runnable code with typed inputs and outputs. There is a particular
 * type of plugin called a {@link imagej.plugin.RunnablePlugin} which is
 * also a module, but many plugins (e.g., {@link imagej.tool.Tool}s and
 * {@link imagej.display.Display}s) are not modules.
 * </p>
 * 
 * @author Curtis Rueden
 * @see IPlugin
 * @see imagej.module.ModuleService
 */
public interface PluginService extends Service {

	ModuleService getModuleService();

	/** Gets the index of available plugins. */
	PluginIndex getIndex();

	/**
	 * Rediscovers all plugins available on the classpath. Note that this will
	 * clear any individual plugins added programmatically.
	 */
	void reloadPlugins();

	/** Manually registers a plugin with the plugin service. */
	void addPlugin(PluginInfo<?> plugin);

	/** Manually registers plugins with the plugin service. */
	<T extends PluginInfo<?>> void addPlugins(Collection<T> plugins);

	/** Manually unregisters a plugin with the plugin service. */
	void removePlugin(PluginInfo<?> plugin);

	/** Manually unregisters plugins with the plugin service. */
	<T extends PluginInfo<?>> void removePlugins(Collection<T> plugins);

	/** Gets the list of known plugins. */
	List<PluginInfo<?>> getPlugins();

	/** Gets the first available plugin of the given class, or null if none. */
	<P extends IPlugin> PluginInfo<P> getPlugin(Class<P> pluginClass);

	/**
	 * Gets the first available plugin of the given class name, or null if none.
	 */
	PluginInfo<IPlugin> getPlugin(String className);

	/**
	 * Gets the list of plugins of the given type (e.g., {@link RunnablePlugin}).
	 */
	<P extends IPlugin> List<PluginInfo<? extends P>> getPluginsOfType(
		Class<P> type);

	/**
	 * Gets the list of plugins of the given class.
	 * <p>
	 * Most plugins will have only a single match, but some special plugin classes
	 * (such as imagej.legacy.LegacyPlugin) may match many entries.
	 * </p>
	 */
	<P extends IPlugin> List<PluginInfo<P>> getPluginsOfClass(
		Class<P> pluginClass);

	/**
	 * Gets the list of plugins with the given class name.
	 * <p>
	 * Most plugins will have only a single match, but some special plugin classes
	 * (such as imagej.legacy.LegacyPlugin) may match many entries.
	 * </p>
	 */
	List<PluginInfo<IPlugin>> getPluginsOfClass(String className);

	/** Gets the list of executable plugins (i.e., {@link RunnablePlugin}s). */
	List<PluginModuleInfo<RunnablePlugin>> getRunnablePlugins();

	/**
	 * Gets the first available executable plugin of the given class, or null if
	 * none.
	 */
	<R extends RunnablePlugin> PluginModuleInfo<R> getRunnablePlugin(
		Class<R> pluginClass);

	/**
	 * Gets the first available executable plugin of the given class name, or null
	 * if none.
	 */
	PluginModuleInfo<RunnablePlugin> getRunnablePlugin(String className);

	/** Gets the list of executable plugins of the given type. */
	<R extends RunnablePlugin> List<PluginModuleInfo<R>>
		getRunnablePluginsOfType(Class<R> type);

	/**
	 * Gets the list of executable plugins of the given class.
	 * <p>
	 * Most plugins will have only a single match, but some special plugin classes
	 * (such as imagej.legacy.LegacyPlugin) may match many entries.
	 * </p>
	 */
	<R extends RunnablePlugin> List<PluginModuleInfo<R>>
		getRunnablePluginsOfClass(Class<R> pluginClass);

	/**
	 * Gets the list of executable plugins with the given class name.
	 * <p>
	 * Most plugins will have only a single match, but some special plugin classes
	 * (such as imagej.legacy.LegacyPlugin) may match many entries.
	 * </p>
	 */
	List<PluginModuleInfo<RunnablePlugin>> getRunnablePluginsOfClass(
		String className);

	/**
	 * Creates one instance each of the available plugins of the given type.
	 * <p>
	 * Note that this method does <em>not</em> do any preprocessing on the plugin
	 * instances, so parameters will not be auto-populated, initializers will not
	 * be executed, etc.
	 * </p>
	 */
	<P extends IPlugin> List<? extends P> createInstancesOfType(Class<P> type);

	/**
	 * Creates an instance of each of the plugins on the given list.
	 * <p>
	 * Note that this method does <em>not</em> do any preprocessing on the plugin
	 * instances, so parameters will not be auto-populated, initializers will not
	 * be executed, etc.
	 * </p>
	 */
	<P extends IPlugin> List<? extends P> createInstances(
		List<PluginInfo<? extends P>> infos);

	/**
	 * Executes the first runnable plugin of the given class name.
	 * 
	 * @param className Class name of the plugin to execute.
	 * @param inputs List of input parameter names and values. The expected order
	 *          is in pairs: an input name followed by its value, for each desired
	 *          input to populate. Leaving some inputs unpopulated is allowed.
	 *          Passing the name of an input that is not valid for the plugin, or
	 *          passing a value of a type incompatible with the associated input
	 *          parameter, will issue an error and ignore that name/value pair.
	 * @return {@link Future} of the module instance being executed. Calling
	 *         {@link Future#get()} will block until execution is complete.
	 */
	Future<Module> run(String className, Object... inputs);

	/**
	 * Executes the first runnable plugin of the given class name.
	 * 
	 * @param className Class name of the plugin to execute.
	 * @param inputMap Table of input parameter values, with keys matching the
	 *          plugin's input parameter names. Passing a value of a type
	 *          incompatible with the associated input parameter will issue an
	 *          error and ignore that value.
	 * @return {@link Future} of the module instance being executed. Calling
	 *         {@link Future#get()} will block until execution is complete.
	 */
	Future<Module> run(String className, Map<String, Object> inputMap);

	/**
	 * Executes the first runnable plugin of the given class.
	 * 
	 * @param <R> Class of the plugin to execute.
	 * @param pluginClass Class object of the plugin to execute.
	 * @param inputs List of input parameter names and values. The expected order
	 *          is in pairs: an input name followed by its value, for each desired
	 *          input to populate. Leaving some inputs unpopulated is allowed.
	 *          Passing the name of an input that is not valid for the plugin, or
	 *          passing a value of a type incompatible with the associated input
	 *          parameter, will issue an error and ignore that name/value pair.
	 * @return {@link Future} of the module instance being executed. Calling
	 *         {@link Future#get()} will block until execution is complete.
	 */
	<R extends RunnablePlugin> Future<PluginModule<R>> run(Class<R> pluginClass,
		Object... inputs);

	/**
	 * Executes the first runnable plugin of the given class.
	 * 
	 * @param <R> Class of the plugin to execute.
	 * @param pluginClass Class object of the plugin to execute.
	 * @param inputMap Table of input parameter values, with keys matching the
	 *          plugin's input parameter names. Passing a value of a type
	 *          incompatible with the associated input parameter will issue an
	 *          error and ignore that value.
	 * @return {@link Future} of the module instance being executed. Calling
	 *         {@link Future#get()} will block until execution is complete.
	 */
	<R extends RunnablePlugin> Future<PluginModule<R>> run(Class<R> pluginClass,
		Map<String, Object> inputMap);

	/**
	 * Executes the given module, with pre- and postprocessing steps from all
	 * available {@link PreprocessorPlugin}s and {@link PostprocessorPlugin}s in
	 * the plugin index.
	 * 
	 * @param info The module to instantiate and run.
	 * @param inputs List of input parameter names and values. The expected order
	 *          is in pairs: an input name followed by its value, for each desired
	 *          input to populate. Leaving some inputs unpopulated is allowed.
	 *          Passing the name of an input that is not valid for the plugin, or
	 *          passing a value of a type incompatible with the associated input
	 *          parameter, will issue an error and ignore that name/value pair.
	 * @return {@link Future} of the module instance being executed. Calling
	 *         {@link Future#get()} will block until execution is complete.
	 */
	Future<Module> run(ModuleInfo info, Object... inputs);

	/**
	 * Executes the given module, with pre- and postprocessing steps from all
	 * available {@link PreprocessorPlugin}s and {@link PostprocessorPlugin}s in
	 * the plugin index.
	 * 
	 * @param info The module to instantiate and run.
	 * @param inputMap Table of input parameter values, with keys matching the
	 *          {@link ModuleInfo}'s input parameter names. Passing a value of a
	 *          type incompatible with the associated input parameter will issue
	 *          an error and ignore that value.
	 * @return {@link Future} of the module instance being executed. Calling
	 *         {@link Future#get()} will block until execution is complete.
	 */
	Future<Module> run(ModuleInfo info, Map<String, Object> inputMap);

	/**
	 * Executes the given module, with pre- and postprocessing steps from all
	 * available {@link PreprocessorPlugin}s and {@link PostprocessorPlugin}s in
	 * the plugin index.
	 * 
	 * @param module The module to run.
	 * @param inputs List of input parameter names and values. The expected order
	 *          is in pairs: an input name followed by its value, for each desired
	 *          input to populate. Leaving some inputs unpopulated is allowed.
	 *          Passing the name of an input that is not valid for the plugin, or
	 *          passing a value of a type incompatible with the associated input
	 *          parameter, will issue an error and ignore that name/value pair.
	 * @return {@link Future} of the module instance being executed. Calling
	 *         {@link Future#get()} will block until execution is complete.
	 */
	<M extends Module> Future<M> run(M module, Object... inputs);

	/**
	 * Executes the given module, with pre- and postprocessing steps from all
	 * available {@link PreprocessorPlugin}s and {@link PostprocessorPlugin}s in
	 * the plugin index.
	 * 
	 * @param module The module to run.
	 * @param inputMap Table of input parameter values, with keys matching the
	 *          module's {@link ModuleInfo}'s input parameter names. Passing a
	 *          value of a type incompatible with the associated input parameter
	 *          will issue an error and ignore that value.
	 * @return {@link Future} of the module instance being executed. Calling
	 *         {@link Future#get()} will block until execution is complete.
	 */
	<M extends Module> Future<M> run(M module, Map<String, Object> inputMap);

}
