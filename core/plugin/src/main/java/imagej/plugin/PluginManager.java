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
 * Manager component for keeping track of available plugins.
 *
 * @author Curtis Rueden
 */
@Manager(priority = Manager.NORMAL_PRIORITY)
public class PluginManager implements ManagerComponent {

	/** The complete list of known plugins. */
	private List<PluginEntry<?>> plugins = new ArrayList<PluginEntry<?>>();

	/** Table of plugin lists, organized by plugin type. */
	private Map<Class<?>, ArrayList<PluginEntry<?>>> pluginLists =
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

	/** Gets a copy of the list of plugins labeled with the given type. */
	public <T extends BasePlugin> ArrayList<PluginEntry<T>>
		getPlugins(final Class<T> type)
	{
		// TODO - find a way to avoid making a copy of the list here?
		final ArrayList<PluginEntry<T>> outputList =
			new ArrayList<PluginEntry<T>>();
		final ArrayList<PluginEntry<?>> cachedList = pluginLists.get(type);
		if (cachedList != null) {
			for (PluginEntry<?> entry : cachedList) {
				@SuppressWarnings("unchecked")
				final PluginEntry<T> typedEntry = (PluginEntry<T>) entry;
				outputList.add(typedEntry);
			}
		}
		return outputList;
	}

	/**
	 * Searches the plugin index for the {@link PluginEntry} describing the
	 * given plugin class.
	 */
	public <T extends BasePlugin> PluginEntry<T>
		getPluginEntry(final Class<T> pluginClass)
	{
		// TODO - Come up with a faster search mechanism.
		for (final ArrayList<PluginEntry<?>> pluginList : pluginLists.values()) {
			for (PluginEntry<?> entry : pluginList) {
				if (entry.getClassName().equals(pluginClass.getName())) {
					@SuppressWarnings("unchecked")
					final PluginEntry<T> match = (PluginEntry<T>) entry;
					return match;
				}
			}
		}
		return null; // no match
	}

	/**
	 * Executes the plugin represented by the given {@link PluginEntry},
	 * in its own thread.
	 */
	public <T extends RunnablePlugin> void run(final PluginEntry<T> entry) {
		// TODO - Implement a better threading mechanism for launching plugins.
		// Perhaps a ThreadManager so that the UI can query currently
		// running plugins and so forth?
		new Thread(new Runnable() {
			@Override
			public void run() {
				new PluginRunner<T>(entry).run();
			}
		}, "PluginRunner-" + entry.getClassName()).start();
	}

	/**
	 * Executes the plugin represented by the given class,
	 * in its own thread.
	 */
	public <T extends RunnablePlugin> void run(final Class<T> pluginClass) {
		run(getPluginEntry(pluginClass));
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
		for (final IndexItem<PluginFinder, IPluginFinder> item :
			Index.load(PluginFinder.class, IPluginFinder.class))
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
