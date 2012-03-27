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

import imagej.ImageJ;
import imagej.ext.InstantiableException;
import imagej.ext.module.Module;
import imagej.ext.module.ModuleInfo;
import imagej.ext.module.ModuleService;
import imagej.ext.plugin.process.PostprocessorPlugin;
import imagej.ext.plugin.process.PreprocessorPlugin;
import imagej.service.AbstractService;
import imagej.service.Service;
import imagej.util.Log;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;

/**
 * Default service for keeping track of available plugins. Available plugins are
 * discovered using a library called SezPoz. Loading of the actual plugin
 * classes can be deferred until a particular plugin's first execution.
 * 
 * @author Curtis Rueden
 * @see IPlugin
 * @see Plugin
 */
@Service
public class DefaultPluginService extends AbstractService implements
	PluginService
{

	private final ModuleService moduleService;

	/** Index of registered plugins. */
	private final PluginIndex pluginIndex = new PluginIndex();

	// -- Constructors --

	public DefaultPluginService() {
		// NB: Required by SezPoz.
		super(null);
		throw new UnsupportedOperationException();
	}

	public DefaultPluginService(final ImageJ context,
		final ModuleService moduleService)
	{
		super(context);
		this.moduleService = moduleService;

		reloadPlugins();
	}

	// -- PluginService methods --

	@Override
	public ModuleService getModuleService() {
		return moduleService;
	}

	@Override
	public PluginIndex getIndex() {
		return pluginIndex;
	}

	@Override
	public void reloadPlugins() {
		// remove old runnable plugins from module service
		moduleService.removeModules(getRunnablePlugins());

		pluginIndex.clear();
		final ArrayList<PluginInfo<?>> plugins = new ArrayList<PluginInfo<?>>();
		new PluginFinder().findPlugins(plugins);
		pluginIndex.addAll(plugins);

		// add new runnable plugins to module service
		moduleService.addModules(getRunnablePlugins());
	}

	@Override
	public void addPlugin(final PluginInfo<?> plugin) {
		pluginIndex.add(plugin);
		if (plugin instanceof ModuleInfo) {
			moduleService.addModule((ModuleInfo) plugin);
		}
	}

	@Override
	public <T extends PluginInfo<?>> void
		addPlugins(final Collection<T> plugins)
	{
		pluginIndex.addAll(plugins);

		// add new runnable plugins to module service
		final List<ModuleInfo> modules = new ArrayList<ModuleInfo>();
		for (final PluginInfo<?> info : plugins) {
			if (info instanceof ModuleInfo) {
				modules.add((ModuleInfo) info);
			}
		}
		moduleService.addModules(modules);
	}

	@Override
	public void removePlugin(final PluginInfo<?> plugin) {
		pluginIndex.remove(plugin);
		if (plugin instanceof ModuleInfo) {
			moduleService.removeModule((ModuleInfo) plugin);
		}
	}

	@Override
	public <T extends PluginInfo<?>> void removePlugins(
		final Collection<T> plugins)
	{
		pluginIndex.removeAll(plugins);

		// remove old runnable plugins to module service
		final List<ModuleInfo> modules = new ArrayList<ModuleInfo>();
		for (final PluginInfo<?> info : plugins) {
			if (info instanceof ModuleInfo) {
				modules.add((ModuleInfo) info);
			}
		}
		moduleService.removeModules(modules);
	}

	@Override
	public List<PluginInfo<?>> getPlugins() {
		return pluginIndex.getAll();
	}

	@Override
	public <P extends IPlugin> PluginInfo<P>
		getPlugin(final Class<P> pluginClass)
	{
		return first(getPluginsOfClass(pluginClass));
	}

	@Override
	public PluginInfo<IPlugin> getPlugin(final String className) {
		return first(getPluginsOfClass(className));
	}

	@Override
	public <P extends IPlugin> List<PluginInfo<P>> getPluginsOfType(
		final Class<P> type)
	{
		final List<PluginInfo<?>> list = pluginIndex.get(type);
		@SuppressWarnings({ "rawtypes", "unchecked" })
		final List<PluginInfo<P>> result = (List) list;
		return result;
	}

	@Override
	public <P extends IPlugin> List<PluginInfo<P>> getPluginsOfClass(
		final Class<P> pluginClass)
	{
		final ArrayList<PluginInfo<P>> result = new ArrayList<PluginInfo<P>>();
		getPluginsOfClass(pluginClass.getName(), getPlugins(), result);
		return result;
	}

	@Override
	public List<PluginInfo<IPlugin>> getPluginsOfClass(final String className) {
		final ArrayList<PluginInfo<IPlugin>> result =
			new ArrayList<PluginInfo<IPlugin>>();
		getPluginsOfClass(className, getPlugins(), result);
		return result;
	}

	@Override
	public List<PluginModuleInfo<RunnablePlugin>> getRunnablePlugins() {
		return getRunnablePluginsOfType(RunnablePlugin.class);
	}

	@Override
	public <R extends RunnablePlugin> PluginModuleInfo<R> getRunnablePlugin(
		final Class<R> pluginClass)
	{
		return first(getRunnablePluginsOfClass(pluginClass));
	}

	@Override
	public PluginModuleInfo<RunnablePlugin> getRunnablePlugin(
		final String className)
	{
		return first(getRunnablePluginsOfClass(className));
	}

	@Override
	public <R extends RunnablePlugin> List<PluginModuleInfo<R>>
		getRunnablePluginsOfType(final Class<R> type)
	{
		final List<PluginInfo<?>> list = pluginIndex.get(type);
		@SuppressWarnings({ "rawtypes", "unchecked" })
		final List<PluginModuleInfo<R>> result = (List) list;
		return result;
	}

	@Override
	public <R extends RunnablePlugin> List<PluginModuleInfo<R>>
		getRunnablePluginsOfClass(final Class<R> pluginClass)
	{
		final ArrayList<PluginModuleInfo<R>> result =
			new ArrayList<PluginModuleInfo<R>>();
		getPluginsOfClass(pluginClass.getName(), getRunnablePlugins(), result);
		return result;
	}

	@Override
	public List<PluginModuleInfo<RunnablePlugin>> getRunnablePluginsOfClass(
		final String className)
	{
		final ArrayList<PluginModuleInfo<RunnablePlugin>> result =
			new ArrayList<PluginModuleInfo<RunnablePlugin>>();
		getPluginsOfClass(className, getRunnablePlugins(), result);
		return result;
	}

	@Override
	public <P extends IPlugin> List<P> createInstances(final Class<P> type) {
		return createInstances(getPluginsOfType(type));
	}

	@Override
	public <P extends IPlugin> List<P> createInstances(
		final List<PluginInfo<P>> infos)
	{
		final ArrayList<P> list = new ArrayList<P>();
		for (final PluginInfo<P> info : infos) {
			try {
				list.add(info.createInstance());
			}
			catch (final InstantiableException e) {
				Log.error("Cannot create plugin: " + info.getClassName());
			}
		}
		return list;
	}

	@Override
	public Future<Module> run(final String className,
		final Object... inputValues)
	{
		final PluginModuleInfo<?> plugin = getRunnablePlugin(className);
		if (!checkPlugin(plugin, className)) return null;
		return run(plugin, inputValues);
	}

	@Override
	public Future<Module> run(final String className,
		final Map<String, Object> inputMap)
	{
		final PluginModuleInfo<?> plugin = getRunnablePlugin(className);
		if (!checkPlugin(plugin, className)) return null;
		return run(plugin, inputMap);
	}

	@Override
	public <R extends RunnablePlugin> Future<Module> run(
		final Class<R> pluginClass, final Object... inputValues)
	{
		final PluginModuleInfo<R> plugin = getRunnablePlugin(pluginClass);
		if (!checkPlugin(plugin, pluginClass.getName())) return null;
		return run(plugin, inputValues);
	}

	@Override
	public <R extends RunnablePlugin> Future<Module> run(
		final Class<R> pluginClass, final Map<String, Object> inputMap)
	{
		final PluginModuleInfo<R> plugin = getRunnablePlugin(pluginClass);
		if (!checkPlugin(plugin, pluginClass.getName())) return null;
		return run(plugin, inputMap);
	}

	@Override
	public Future<Module>
		run(final ModuleInfo info, final Object... inputValues)
	{
		return moduleService.run(info, pre(), post(), inputValues);
	}

	@Override
	public Future<Module> run(final ModuleInfo info,
		final Map<String, Object> inputMap)
	{
		return moduleService.run(info, pre(), post(), inputMap);
	}

	@Override
	public Future<Module> run(final Module module, final Object... inputValues) {
		return moduleService.run(module, pre(), post(), inputValues);
	}

	@Override
	public Future<Module> run(final Module module,
		final Map<String, Object> inputMap)
	{
		return moduleService.run(module, pre(), post(), inputMap);
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

	/** Gets the first element of the given list, or null if none. */
	private <T> T first(final List<T> list) {
		if (list == null || list.size() == 0) return null;
		return list.get(0);

	}

	private List<PreprocessorPlugin> pre() {
		return createInstances(PreprocessorPlugin.class);
	}

	private List<PostprocessorPlugin> post() {
		return createInstances(PostprocessorPlugin.class);
	}

	private boolean checkPlugin(final PluginModuleInfo<?> plugin,
		final String name)
	{
		if (plugin == null) {
			Log.error("No such plugin: " + name);
			return false;
		}
		return true;
	}

}
