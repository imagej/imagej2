//
// PluginService.java
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

package imagej.ext.plugin;

import imagej.AbstractService;
import imagej.ImageJ;
import imagej.Service;
import imagej.ext.InstantiableException;
import imagej.ext.module.Module;
import imagej.ext.module.ModuleInfo;
import imagej.ext.module.ModuleService;
import imagej.ext.plugin.finder.IPluginFinder;
import imagej.ext.plugin.finder.PluginFinder;
import imagej.ext.plugin.process.PostprocessorPlugin;
import imagej.ext.plugin.process.PreprocessorPlugin;
import imagej.util.Log;

import java.util.ArrayList;
import java.util.List;

import net.java.sezpoz.Index;
import net.java.sezpoz.IndexItem;

/**
 * Service for keeping track of available plugins. Available plugins are
 * discovered using a library called SezPoz. Loading of the actual plugin
 * classes can be deferred until a particular plugin's first execution.
 * 
 * @author Curtis Rueden
 * @see IPlugin
 * @see Plugin
 */
@Service
public class PluginService extends AbstractService {

	private final ModuleService moduleService;

	/** Index of registered plugins. */
	private final PluginIndex pluginIndex = new PluginIndex();

	// -- Constructors --

	public PluginService() {
		// NB: Required by SezPoz.
		super(null);
		throw new UnsupportedOperationException();
	}

	public PluginService(final ImageJ context, final ModuleService moduleService)
	{
		super(context);
		this.moduleService = moduleService;
	}

	// -- PluginService methods --

	public ModuleService getModuleService() {
		return moduleService;
	}

	/** Gets the index of available plugins. */
	public PluginIndex getIndex() {
		return pluginIndex;
	}

	/**
	 * Rediscovers all plugins available on the classpath. Note that this will
	 * clear any individual plugins added programmatically.
	 */
	public void reloadPlugins() {
		// remove old runnable plugins from module service
		moduleService.removeModules(getRunnablePlugins());

		pluginIndex.clear();
		for (final IndexItem<PluginFinder, IPluginFinder> item : Index.load(
			PluginFinder.class, IPluginFinder.class))
		{
			try {
				final IPluginFinder finder = item.instance();
				final ArrayList<PluginInfo<?>> plugins =
					new ArrayList<PluginInfo<?>>();
				finder.findPlugins(plugins);
				pluginIndex.addAll(plugins);
			}
			catch (final InstantiationException e) {
				Log.warn("Invalid plugin finder: " + item, e);
			}
		}

		// add new runnable plugins to module service
		moduleService.addModules(getRunnablePlugins());
	}

	/** Manually registers a plugin with the plugin service. */
	public void addPlugin(final PluginInfo<?> plugin) {
		pluginIndex.add(plugin);
	}

	/** Manually unregisters a plugin with the plugin service. */
	public void removePlugin(final PluginInfo<?> plugin) {
		pluginIndex.remove(plugin);
	}

	/** Gets the list of known plugins. */
	public List<PluginInfo<?>> getPlugins() {
		return pluginIndex.getAll();
	}

	/**
	 * Gets the list of plugins of the given type (e.g., {@link ImageJPlugin}).
	 */
	public <P extends IPlugin> List<PluginInfo<P>> getPluginsOfType(
		final Class<P> type)
	{
		final List<PluginInfo<?>> list = pluginIndex.get(type);
		@SuppressWarnings({ "rawtypes", "unchecked" })
		final List<PluginInfo<P>> result = (List) list;
		return result;
	}

	/**
	 * Gets the list of plugins of the given class.
	 * <p>
	 * Most plugins will have only a single match, but some special plugin classes
	 * (such as imagej.legacy.LegacyPlugin) may match many entries.
	 * </p>
	 */
	public <P extends IPlugin> List<PluginInfo<P>> getPluginsOfClass(
		final Class<P> pluginClass)
	{
		final ArrayList<PluginInfo<P>> result = new ArrayList<PluginInfo<P>>();
		getPluginsOfClass(pluginClass.getName(), getPlugins(), result);
		return result;
	}

	/**
	 * Gets the list of plugins with the given class name.
	 * <p>
	 * Most plugins will have only a single match, but some special plugin classes
	 * (such as imagej.legacy.LegacyPlugin) may match many entries.
	 * </p>
	 */
	public List<PluginInfo<IPlugin>> getPluginsOfClass(final String className) {
		final ArrayList<PluginInfo<IPlugin>> result =
			new ArrayList<PluginInfo<IPlugin>>();
		getPluginsOfClass(className, getPlugins(), result);
		return result;
	}

	/** Gets the list of executable plugins (i.e., {@link RunnablePlugin}s). */
	public List<PluginModuleInfo<RunnablePlugin>> getRunnablePlugins() {
		return getRunnablePluginsOfType(RunnablePlugin.class);
	}

	/** Gets the list of executable plugins of the given type. */
	public <R extends RunnablePlugin> List<PluginModuleInfo<R>>
		getRunnablePluginsOfType(final Class<R> type)
	{
		final List<PluginInfo<?>> list = pluginIndex.get(type);
		@SuppressWarnings({ "rawtypes", "unchecked" })
		final List<PluginModuleInfo<R>> result = (List) list;
		return result;
	}

	/**
	 * Gets the list of executable plugins of the given class.
	 * <p>
	 * Most plugins will have only a single match, but some special plugin classes
	 * (such as imagej.legacy.LegacyPlugin) may match many entries.
	 * </p>
	 */
	public <R extends RunnablePlugin> List<PluginModuleInfo<R>>
		getRunnablePluginsOfClass(final Class<R> pluginClass)
	{
		final ArrayList<PluginModuleInfo<R>> result =
			new ArrayList<PluginModuleInfo<R>>();
		getPluginsOfClass(pluginClass.getName(), getRunnablePlugins(), result);
		return result;
	}

	/**
	 * Gets the list of executable plugins with the given class name.
	 * <p>
	 * Most plugins will have only a single match, but some special plugin classes
	 * (such as imagej.legacy.LegacyPlugin) may match many entries.
	 * </p>
	 */
	public List<PluginModuleInfo<RunnablePlugin>> getRunnablePluginsOfClass(
		final String className)
	{
		final ArrayList<PluginModuleInfo<RunnablePlugin>> result =
			new ArrayList<PluginModuleInfo<RunnablePlugin>>();
		getPluginsOfClass(className, getRunnablePlugins(), result);
		return result;
	}

	/** Creates one instance each of the available plugins of the given type. */
	public <P extends IPlugin> List<P> createInstances(final Class<P> type) {
		return createInstances(getPluginsOfType(type));
	}

	/** Creates an instance of each of the plugins on the given list. */
	public <P extends IPlugin> List<P> createInstances(
		final List<PluginInfo<P>> infos)
	{
		final ArrayList<P> list = new ArrayList<P>();
		for (final PluginInfo<P> info : infos) {
			try {
				list.add(info.createInstance());
			}
			catch (final InstantiableException e) {
				Log.warn("Cannot create plugin: " + info.getClassName());
			}
		}
		return list;
	}

	/** Executes the first runnable plugin of the given class. */
	public <R extends RunnablePlugin> void run(final Class<R> pluginClass,
		final boolean separateThread)
	{
		final List<PluginModuleInfo<R>> plugins =
			getRunnablePluginsOfClass(pluginClass);
		if (plugins == null || plugins.size() == 0) {
			// no matching plugins
			Log.error("No such plugin: " + pluginClass.getName());
			return;
		}
		run(plugins.get(0), separateThread);
	}

	/**
	 * Executes the given module, with pre- and postprocessing steps from all
	 * available {@link PreprocessorPlugin}s and {@link PostprocessorPlugin}s in
	 * the plugin index.
	 * 
	 * @param info The module to instantiate and run.
	 * @param separateThread Whether to execute the module in a new thread.
	 */
	public void run(final ModuleInfo info, final boolean separateThread) {
		final List<PreprocessorPlugin> pre =
			createInstances(PreprocessorPlugin.class);
		final List<PostprocessorPlugin> post =
			createInstances(PostprocessorPlugin.class);
		moduleService.run(info, pre, post, separateThread);
	}

	/**
	 * Executes the given module, with pre- and postprocessing steps from all
	 * available {@link PreprocessorPlugin}s and {@link PostprocessorPlugin}s in
	 * the plugin index.
	 * 
	 * @param module The module to run.
	 * @param separateThread Whether to execute the module in a new thread.
	 */
	public void run(final Module module, final boolean separateThread) {
		final List<PreprocessorPlugin> pre =
			createInstances(PreprocessorPlugin.class);
		final List<PostprocessorPlugin> post =
			createInstances(PostprocessorPlugin.class);
		moduleService.run(module, pre, post, separateThread);
	}

	// -- IService methods --

	@Override
	public void initialize() {
		reloadPlugins();
	}

	// -- Helper methods --

	private <T extends PluginInfo<?>> void getPluginsOfClass(
		final String className, final List<? extends PluginInfo<?>> srcList,
		final List<T> destList)
	{
		for (final PluginInfo<?> info : srcList) {
			if (info.getClassName().equals(className)) {
				@SuppressWarnings("unchecked")
				final T match = (T) info;
				destList.add(match);
			}
		}
	}

}
