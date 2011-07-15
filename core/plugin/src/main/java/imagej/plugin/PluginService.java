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

package imagej.plugin;

import imagej.IService;
import imagej.Service;
import imagej.event.Events;
import imagej.module.Module;
import imagej.module.ModuleException;
import imagej.module.ModuleInfo;
import imagej.module.ModuleRunner;
import imagej.module.event.ModuleInfoAddedEvent;
import imagej.module.event.ModuleInfoRemovedEvent;
import imagej.plugin.finder.IPluginFinder;
import imagej.plugin.finder.PluginFinder;
import imagej.plugin.process.PostprocessorPlugin;
import imagej.plugin.process.PreprocessorPlugin;
import imagej.util.Log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import net.java.sezpoz.Index;
import net.java.sezpoz.IndexItem;

/**
 * Service for keeping track of available plugins. Available plugins
 * are discovered using a library called SezPoz. Loading of the actual plugin
 * classes can be deferred until a particular plugin's first execution.
 * 
 * @author Curtis Rueden
 * @see IPlugin
 * @see Plugin
 */
@Service(priority = Service.NORMAL_PRIORITY)
public class PluginService implements IService {

	/** The complete list of known plugins. */
	private final List<PluginInfo<?>> plugins = new ArrayList<PluginInfo<?>>();

	/** Table of plugin lists, organized by plugin type. */
	private final Map<Class<?>, ArrayList<PluginInfo<?>>> pluginLists =
		new ConcurrentHashMap<Class<?>, ArrayList<PluginInfo<?>>>();

	/**
	 * Rediscovers all plugins available on the classpath. Note that this will
	 * clear any individual plugins added programmatically.
	 */
	public void reloadPlugins() {
		findPlugins();
		classifyPlugins();
		sortPlugins();
	}

	/** Manually registers a plugin with the plugin service. */
	public void addPlugin(final PluginModuleInfo<?> plugin) {
		plugins.add(plugin);
		registerType(plugin, true);
		Events.publish(new ModuleInfoAddedEvent(plugin));
	}

	/** Manually unregisters a plugin with the plugin service. */
	public void removePlugin(final PluginModuleInfo<?> plugin) {
		plugins.remove(plugin);
		unregisterType(plugin);
		Events.publish(new ModuleInfoRemovedEvent(plugin));
	}

	/** Gets the list of known plugins. */
	public List<PluginInfo<?>> getPlugins() {
		return Collections.unmodifiableList(plugins);
	}

	/**
	 * Gets the list of known plugins that can function as modules (i.e.,
	 * {@link RunnablePlugin}s).
	 */
	public List<ModuleInfo> getModules() {
		// CTR FIXME - rework to avoid this HACKy method; use ModuleService instead
		final ArrayList<ModuleInfo> modules = new ArrayList<ModuleInfo>();
		for (final PluginInfo<?> info : plugins) {
			if (info instanceof ModuleInfo) {
				final ModuleInfo moduleInfo = (ModuleInfo) info;
				modules.add(moduleInfo);
			}
		}
		return modules;
	}

	/**
	 * Gets the list of plugins labeled with the given plugin type (e.g.,
	 * {@link ImageJPlugin}).
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public <P extends IPlugin> List<PluginInfo<P>> getPluginsOfType(
		final Class<P> type)
	{
		ArrayList<PluginInfo<?>> outputList = pluginLists.get(type);
		if (outputList == null) outputList = new ArrayList<PluginInfo<?>>();
		return (List) Collections.unmodifiableList(outputList);
	}

	/**
	 * Gets the list of plugins of the given class. Most modern plugins will have
	 * only a single match, but some special plugin classes (such as
	 * imagej.legacy.LegacyPlugin) may match many entries.
	 */
	public <P extends IPlugin> List<PluginInfo<P>> getPluginsOfClass(
		final Class<P> pluginClass)
	{
		final ArrayList<PluginInfo<P>> entries = new ArrayList<PluginInfo<P>>();
		final String className = pluginClass.getName();
		for (final PluginInfo<?> entry : plugins) {
			if (entry.getClassName().equals(className)) {
				@SuppressWarnings("unchecked")
				final PluginInfo<P> match = (PluginInfo<P>) entry;
				entries.add(match);
			}
		}
		return entries;
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
		final List<PluginInfo<R>> infos = getPluginsOfClass(pluginClass);
		ModuleInfo moduleInfo = null;
		for (final PluginInfo<R> info : infos) {
			if (info instanceof ModuleInfo) {
				moduleInfo = (ModuleInfo) info;
				break;
			}
		}
		if (moduleInfo == null) return; // no modules found
		run(moduleInfo, separateThread);
	}

	/** Executes the module represented by the given module info. */
	public <R extends RunnablePlugin> void run(final ModuleInfo info,
		final boolean separateThread)
	{
		try {
			run(info.createModule(), separateThread);
		}
		catch (final ModuleException e) {
			Log.error("Could not execute plugin: " + info, e);
		}
	}

	/** Executes the given module. */
	public void run(final Module module, final boolean separateThread) {
		// TODO - Implement a better threading mechanism for launching plugins.
		// Perhaps a ThreadService so that the UI can query currently
		// running plugins and so forth?
		if (separateThread) {
			final String className = module.getInfo().getDelegateClassName();
			final String threadName = "ModuleRunner-" + className;
			new Thread(new Runnable() {

				@Override
				public void run() {
					final List<PreprocessorPlugin> pre =
						createInstances(PreprocessorPlugin.class);
					final List<PostprocessorPlugin> post =
						createInstances(PostprocessorPlugin.class);
					new ModuleRunner(module, pre, post).run();
				}
			}, threadName).start();
		}
		else module.run();
	}

	// -- IService methods --

	@Override
	public void initialize() {
		reloadPlugins();
	}

	// -- Helper methods --

	/** Discovers and invokes all plugin finders. */
	private void findPlugins() {
		plugins.clear();
		for (final IndexItem<PluginFinder, IPluginFinder> item : Index.load(
			PluginFinder.class, IPluginFinder.class))
		{
			try {
				final IPluginFinder finder = item.instance();
				finder.findPlugins(plugins);
			}
			catch (final InstantiationException e) {
				Log.warn("Invalid plugin finder: " + item, e);
			}
		}
	}

	/** Classifies plugins according to type. */
	private void classifyPlugins() {
		pluginLists.clear();
		for (final PluginInfo<?> entry : plugins) {
			registerType(entry, false);
		}
	}

	/** Sorts plugin lists by priority. */
	private void sortPlugins() {
		for (final ArrayList<PluginInfo<?>> pluginList : pluginLists.values()) {
			Collections.sort(pluginList);
		}
	}

	/**
	 * Inserts the given plugin into the appropriate type list.
	 * 
	 * @param entry {@link PluginInfo} to insert.
	 * @param sorted Whether the plugin list is currently sorted. If true, the
	 *          {@link PluginInfo} will be inserted into the correct sorted
	 *          position. If false, the {@link PluginInfo} is merely appended.
	 */
	private void registerType(final PluginInfo<?> entry, final boolean sorted) {
		final Class<?> type = entry.getPluginType();
		ArrayList<PluginInfo<?>> pluginList = pluginLists.get(type);
		if (pluginList == null) {
			pluginList = new ArrayList<PluginInfo<?>>();
			pluginLists.put(type, pluginList);
		}
		if (sorted) {
			final int index = Collections.binarySearch(pluginList, entry);
			if (index < 0) pluginList.add(-index - 1, entry);
		}
		else pluginList.add(entry);
	}

	/** Removes the given plugin from the appropriate type list. */
	private void unregisterType(final PluginInfo<?> entry) {
		final Class<?> type = entry.getPluginType();
		final ArrayList<PluginInfo<?>> pluginList = pluginLists.get(type);
		if (pluginList == null) {
			Log.warn("unregisterType: empty type list for entry: " + entry);
			return;
		}
		final int index = Collections.binarySearch(pluginList, entry);
		if (index < 0) {
			Log.warn("unregisterType: unknown plugin entry: " + entry);
			return;
		}
		pluginList.remove(index);
	}

}
