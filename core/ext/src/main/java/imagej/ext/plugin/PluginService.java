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

package imagej.ext.plugin;

import imagej.ext.module.Module;
import imagej.ext.module.ModuleInfo;
import imagej.ext.module.ModuleService;
import imagej.ext.plugin.process.PostprocessorPlugin;
import imagej.ext.plugin.process.PreprocessorPlugin;
import imagej.service.IService;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;

/**
 * Interface for service that keeps track of available plugins.
 * 
 * @author Curtis Rueden
 */
public interface PluginService extends IService {

	ModuleService getModuleService();

	/** Gets the index of available plugins. */
	PluginIndex getIndex();

	/**
	 * Rediscovers all plugins available on the classpath. Note that this will
	 * clear any individual plugins added programmatically.
	 */
	void reloadPlugins();

	/** Manually registers a plugin with the plugin service. */
	void addPlugin(final PluginInfo<?> plugin);

	/** Manually registers plugins with the plugin service. */
	<T extends PluginInfo<?>> void addPlugins(final Collection<T> plugins);

	/** Manually unregisters a plugin with the plugin service. */
	void removePlugin(final PluginInfo<?> plugin);

	/** Manually unregisters plugins with the plugin service. */
	<T extends PluginInfo<?>> void removePlugins(final Collection<T> plugins);

	/** Gets the list of known plugins. */
	List<PluginInfo<?>> getPlugins();

	/** Gets the first available plugin of the given class, or null if none. */
	<P extends IPlugin> PluginInfo<P> getPlugin(final Class<P> pluginClass);

	/**
	 * Gets the first available plugin of the given class name, or null if none.
	 */
	PluginInfo<IPlugin> getPlugin(final String className);

	/**
	 * Gets the list of plugins of the given type (e.g., {@link ImageJPlugin}).
	 */
	<P extends IPlugin> List<PluginInfo<P>> getPluginsOfType(final Class<P> type);

	/**
	 * Gets the list of plugins of the given class.
	 * <p>
	 * Most plugins will have only a single match, but some special plugin classes
	 * (such as imagej.legacy.LegacyPlugin) may match many entries.
	 * </p>
	 */
	<P extends IPlugin> List<PluginInfo<P>> getPluginsOfClass(
		final Class<P> pluginClass);

	/**
	 * Gets the list of plugins with the given class name.
	 * <p>
	 * Most plugins will have only a single match, but some special plugin classes
	 * (such as imagej.legacy.LegacyPlugin) may match many entries.
	 * </p>
	 */
	List<PluginInfo<IPlugin>> getPluginsOfClass(final String className);

	/** Gets the list of executable plugins (i.e., {@link RunnablePlugin}s). */
	List<PluginModuleInfo<RunnablePlugin>> getRunnablePlugins();

	/**
	 * Gets the first available executable plugin of the given class, or null if
	 * none.
	 */
	<R extends RunnablePlugin> PluginModuleInfo<R> getRunnablePlugin(
		final Class<R> pluginClass);

	/**
	 * Gets the first available executable plugin of the given class name, or null
	 * if none.
	 */
	PluginModuleInfo<RunnablePlugin> getRunnablePlugin(final String className);

	/** Gets the list of executable plugins of the given type. */
	<R extends RunnablePlugin> List<PluginModuleInfo<R>>
		getRunnablePluginsOfType(final Class<R> type);

	/**
	 * Gets the list of executable plugins of the given class.
	 * <p>
	 * Most plugins will have only a single match, but some special plugin classes
	 * (such as imagej.legacy.LegacyPlugin) may match many entries.
	 * </p>
	 */
	<R extends RunnablePlugin> List<PluginModuleInfo<R>>
		getRunnablePluginsOfClass(final Class<R> pluginClass);

	/**
	 * Gets the list of executable plugins with the given class name.
	 * <p>
	 * Most plugins will have only a single match, but some special plugin classes
	 * (such as imagej.legacy.LegacyPlugin) may match many entries.
	 * </p>
	 */
	List<PluginModuleInfo<RunnablePlugin>> getRunnablePluginsOfClass(
		final String className);

	/** Creates one instance each of the available plugins of the given type. */
	<P extends IPlugin> List<P> createInstances(final Class<P> type);

	/** Creates an instance of each of the plugins on the given list. */
	<P extends IPlugin> List<P> createInstances(final List<PluginInfo<P>> infos);

	/**
	 * Executes the first runnable plugin of the given class name.
	 * 
	 * @param className Class name of the plugin to execute.
	 * @param inputValues List of input parameter values, in the same order
	 *          declared by the plugin. Passing a number of values that differs
	 *          from the number of input parameters is allowed, but will issue a
	 *          warning. Passing a value of a type incompatible with the
	 *          associated input parameter will issue an error and ignore that
	 *          value.
	 * @return {@link Future} of the module instance being executed. Calling
	 *         {@link Future#get()} will block until execution is complete.
	 */
	Future<Module> run(final String className, final Object... inputValues);

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
	Future<Module>
		run(final String className, final Map<String, Object> inputMap);

	/**
	 * Executes the first runnable plugin of the given class.
	 * 
	 * @param <R> Class of the plugin to execute.
	 * @param pluginClass Class object of the plugin to execute.
	 * @param inputValues List of input parameter values, in the same order
	 *          declared by the plugin. Passing a number of values that differs
	 *          from the number of input parameters is allowed, but will issue a
	 *          warning. Passing a value of a type incompatible with the
	 *          associated input parameter will issue an error and ignore that
	 *          value.
	 * @return {@link Future} of the module instance being executed. Calling
	 *         {@link Future#get()} will block until execution is complete.
	 */
	<R extends RunnablePlugin> Future<PluginModule<R>> run(
		final Class<R> pluginClass, final Object... inputValues);

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
	<R extends RunnablePlugin> Future<PluginModule<R>> run(
		final Class<R> pluginClass, final Map<String, Object> inputMap);

	/**
	 * Executes the given module, with pre- and postprocessing steps from all
	 * available {@link PreprocessorPlugin}s and {@link PostprocessorPlugin}s in
	 * the plugin index.
	 * 
	 * @param info The module to instantiate and run.
	 * @param inputValues List of input parameter values, in the same order
	 *          declared by the {@link ModuleInfo}. Passing a number of values
	 *          that differs from the number of input parameters is allowed, but
	 *          will issue a warning. Passing a value of a type incompatible with
	 *          the associated input parameter will issue an error and ignore that
	 *          value.
	 * @return {@link Future} of the module instance being executed. Calling
	 *         {@link Future#get()} will block until execution is complete.
	 */
	Future<Module> run(final ModuleInfo info, final Object... inputValues);

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
	Future<Module> run(final ModuleInfo info, final Map<String, Object> inputMap);

	/**
	 * Executes the given module, with pre- and postprocessing steps from all
	 * available {@link PreprocessorPlugin}s and {@link PostprocessorPlugin}s in
	 * the plugin index.
	 * 
	 * @param module The module to run.
	 * @param inputValues List of input parameter values, in the same order
	 *          declared by the module's {@link ModuleInfo}. Passing a number of
	 *          values that differs from the number of input parameters is
	 *          allowed, but will issue a warning. Passing a value of a type
	 *          incompatible with the associated input parameter will issue an
	 *          error and ignore that value.
	 * @return {@link Future} of the module instance being executed. Calling
	 *         {@link Future#get()} will block until execution is complete.
	 */
	<M extends Module> Future<M> run(final M module, final Object... inputValues);

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
	<M extends Module> Future<M> run(final M module,
		final Map<String, Object> inputMap);

}
