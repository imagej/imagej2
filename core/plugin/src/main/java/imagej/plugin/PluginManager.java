//
// PluginManager.java
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

import imagej.Manager;
import imagej.ManagerComponent;
import imagej.plugin.finder.IPluginFinder;
import imagej.plugin.finder.PluginFinder;
import imagej.util.Log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import net.java.sezpoz.Index;
import net.java.sezpoz.IndexItem;

/**
 * Manager component for keeping track of available plugins. Available plugins
 * are discovered using a library called SezPoz. Loading of the actual plugin
 * classes can be deferred until a particular plugin's first execution.
 * 
 * @author Curtis Rueden
 */
@Manager(priority = Manager.NORMAL_PRIORITY)
public class PluginManager implements ManagerComponent {

	/** The complete list of known plugins. */
	private final List<PluginEntry<?>> plugins = new ArrayList<PluginEntry<?>>();

	/** Table of plugin lists, organized by plugin type. */
	private final Map<Class<?>, ArrayList<PluginEntry<?>>> pluginLists =
		new ConcurrentHashMap<Class<?>, ArrayList<PluginEntry<?>>>();

	/** Rediscovers all available plugins. */
	public void reloadPlugins() {
		findPlugins();
		classifyPlugins();
		sortPlugins();
	}

	/** Gets the list of known plugins. */
	public List<PluginEntry<?>> getPlugins() {
		return Collections.unmodifiableList(plugins);
	}

	/**
	 * Gets the list of plugins labeled with the given plugin type (e.g.,
	 * {@link ImageJPlugin}).
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public <T extends BasePlugin> List<PluginEntry<T>> getPluginsOfType(
		final Class<T> type)
	{
		ArrayList<PluginEntry<?>> outputList = pluginLists.get(type);
		if (outputList == null) outputList = new ArrayList<PluginEntry<?>>();
		return (List) Collections.unmodifiableList(outputList);
	}

	/**
	 * Gets the list of plugins of the given class. Most modern plugins will have
	 * only a single match, but some special plugin classes (such as
	 * imagej.legacy.LegacyPlugin) may match many entries.
	 */
	public <T extends BasePlugin> List<PluginEntry<T>> getPluginsOfClass(
		final Class<T> pluginClass)
	{
		final ArrayList<PluginEntry<T>> entries = new ArrayList<PluginEntry<T>>();
		final String className = pluginClass.getName();
		for (final PluginEntry<?> entry : plugins) {
			if (entry.getClassName().equals(className)) {
				@SuppressWarnings("unchecked")
				final PluginEntry<T> match = (PluginEntry<T>) entry;
				entries.add(match);
			}
		}
		return entries;
	}

	/** Executes the first plugin of the given class, in its own thread. */
	public <T extends RunnablePlugin> void run(final Class<T> pluginClass) {
		final List<PluginEntry<T>> entries = getPluginsOfClass(pluginClass);
		if (!entries.isEmpty()) run(entries.get(0));
	}

	/**
	 * Executes the plugin represented by the given {@link PluginEntry}, in its
	 * own thread.
	 */
	public <T extends RunnablePlugin> void run(final PluginEntry<T> entry) {
		run(entry, false);
	}

	/**
	 * Executes the plugin represented by the given {@link PluginEntry}, in its
	 * own thread. For toggle plugins, the state is assigned to the given value.
	 * 
	 * @param entry The {@link PluginEntry} describing the plugin to execute.
	 * @param state The toggle state to assign to the plugin, if applicable.
	 */
	public <T extends RunnablePlugin> void run(final PluginEntry<T> entry,
		final boolean state)
	{
		// TODO - Implement a better threading mechanism for launching plugins.
		// Perhaps a ThreadManager so that the UI can query currently
		// running plugins and so forth?
		new Thread(new Runnable() {

			@Override
			public void run() {
				final PluginRunner<T> runner = new PluginRunner<T>(entry);
				runner.getModule().setSelected(state);
				runner.run();
			}
		}, "PluginRunner-" + entry.getClassName()).start();
	}

	// -- ManagerComponent methods --

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
		for (final PluginEntry<?> entry : plugins) {
			final Class<?> type = entry.getPluginType();
			registerType(entry, type);
		}
	}

	/** Sorts plugin lists by priority. */
	private void sortPlugins() {
		for (final ArrayList<PluginEntry<?>> pluginList : pluginLists.values()) {
			Collections.sort(pluginList);
		}
	}

	private void registerType(final PluginEntry<?> entry, final Class<?> type) {
		ArrayList<PluginEntry<?>> pluginList = pluginLists.get(type);
		if (pluginList == null) {
			pluginList = new ArrayList<PluginEntry<?>>();
			pluginLists.put(type, pluginList);
		}
		pluginList.add(entry);
	}

}
