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

import imagej.IService;
import imagej.Service;
import imagej.ext.InstantiableException;
import imagej.ext.module.Module;
import imagej.ext.module.ModuleException;
import imagej.ext.module.ModuleInfo;
import imagej.ext.module.ModuleRunner;
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
public class PluginService implements IService {

	private final PluginIndex pluginIndex = new PluginIndex();

	/**
	 * Rediscovers all plugins available on the classpath. Note that this will
	 * clear any individual plugins added programmatically.
	 */
	public void reloadPlugins() {
		pluginIndex.clear();
		for (final IndexItem<PluginFinder, IPluginFinder> item : Index.load(
			PluginFinder.class, IPluginFinder.class))
		{
			try {
				final IPluginFinder finder = item.instance();
				final ArrayList<PluginInfo<?>> plugins = new ArrayList<PluginInfo<?>>();
				finder.findPlugins(plugins);
				pluginIndex.addAll(plugins);
			}
			catch (final InstantiationException e) {
				Log.warn("Invalid plugin finder: " + item, e);
			}
		}
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
	 * Gets the list of known plugins that can function as modules (i.e.,
	 * {@link RunnablePlugin}s).
	 */
	public List<ModuleInfo> getModules() {
		// CTR FIXME - rework to avoid this HACKy method; use ModuleService instead
		final ArrayList<ModuleInfo> modules = new ArrayList<ModuleInfo>();
		for (final PluginInfo<?> info : pluginIndex.get(RunnablePlugin.class)) {
			modules.add((ModuleInfo) info);
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
		List<PluginInfo<?>> outputList = pluginIndex.get(type);
		return (List) outputList;
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
		System.out.println("getPluginsOfClass: " + className);//TEMP
		for (final PluginInfo<?> entry : getPlugins()) {
			System.out.println("Checking entry: " + entry.getClassName());//TEMP
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
			// should never happen, but just in case...
			Log.warn("Not a ModuleInfo: " + info);
		}
		if (moduleInfo == null) {
			Log.error("No such plugin: " + pluginClass.getName());
			return; // no modules found
		}
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

}
