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

import imagej.InstantiableException;
import imagej.Priority;
import imagej.event.EventService;
import imagej.log.LogService;
import imagej.plugin.event.PluginsAddedEvent;
import imagej.plugin.event.PluginsRemovedEvent;
import imagej.service.AbstractService;
import imagej.service.Service;
import imagej.util.ListUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Default service for keeping track of available plugins. Available plugins are
 * discovered using a library called SezPoz. Loading of the actual plugin
 * classes can be deferred until a particular plugin is actually needed.
 * <p>
 * Plugins are added or removed via the plugin service are reported via the
 * event service. (No events are published for plugins directly added to or
 * removed from the {@link PluginIndex}.)
 * </p>
 * 
 * @author Curtis Rueden
 * @see ImageJPlugin
 * @see Plugin
 */
@Plugin(type = Service.class)
public class DefaultPluginService extends AbstractService implements
	PluginService
{

	@Parameter
	private LogService log;

	@Parameter
	private EventService eventService;

	/** Index of registered plugins. */
	private PluginIndex pluginIndex;

	// -- PluginService methods --

	@Override
	public PluginIndex getIndex() {
		return pluginIndex;
	}

	@Override
	public void reloadPlugins() {
		// clear all old plugins, and notify interested parties
		final List<PluginInfo<?>> oldPlugins = pluginIndex.getAll();
		pluginIndex.clear();
		if (oldPlugins.size() > 0) {
			eventService.publish(new PluginsRemovedEvent(oldPlugins));
		}

		// re-discover all available plugins, and notify interested parties
		pluginIndex.discover();
		final List<PluginInfo<?>> newPlugins = pluginIndex.getAll();
		if (newPlugins.size() > 0) {
			eventService.publish(new PluginsAddedEvent(newPlugins));
		}
	}

	@Override
	public void addPlugin(final PluginInfo<?> plugin) {
		if (pluginIndex.add(plugin)) {
			eventService.publish(new PluginsAddedEvent(plugin));
		}
	}

	@Override
	public <T extends PluginInfo<?>> void
		addPlugins(final Collection<T> plugins)
	{
		if (pluginIndex.addAll(plugins)) {
			eventService.publish(new PluginsAddedEvent(plugins));
		}
	}

	@Override
	public void removePlugin(final PluginInfo<?> plugin) {
		if (pluginIndex.remove(plugin)) {
			eventService.publish(new PluginsRemovedEvent(plugin));
		}
	}

	@Override
	public <T extends PluginInfo<?>> void removePlugins(
		final Collection<T> plugins)
	{
		if (pluginIndex.removeAll(plugins)) {
			eventService.publish(new PluginsRemovedEvent(plugins));
		}
	}

	@Override
	public List<PluginInfo<?>> getPlugins() {
		return pluginIndex.getAll();
	}

	@Override
	public <P extends ImageJPlugin> PluginInfo<P>
		getPlugin(final Class<P> pluginClass)
	{
		return ListUtils.first(getPluginsOfClass(pluginClass));
	}

	@Override
	public PluginInfo<ImageJPlugin> getPlugin(final String className) {
		return ListUtils.first(getPluginsOfClass(className));
	}

	@Override
	public <P extends ImageJPlugin> List<PluginInfo<P>> getPluginsOfType(
		final Class<P> type)
	{
		return pluginIndex.getPlugins(type);
	}

	@Override
	public <P extends ImageJPlugin> List<PluginInfo<P>> getPluginsOfClass(
		final Class<P> pluginClass)
	{
		final ArrayList<PluginInfo<P>> result = new ArrayList<PluginInfo<P>>();
		getPluginsOfClass(pluginClass.getName(), getPlugins(), result);
		return result;
	}

	@Override
	public List<PluginInfo<ImageJPlugin>> getPluginsOfClass(final String className) {
		final ArrayList<PluginInfo<ImageJPlugin>> result =
			new ArrayList<PluginInfo<ImageJPlugin>>();
		getPluginsOfClass(className, getPlugins(), result);
		return result;
	}

	@Override
	public <P extends ImageJPlugin> List<P> createInstancesOfType(final Class<P> type)
	{
		final List<PluginInfo<P>> plugins = getPluginsOfType(type);
		@SuppressWarnings({ "rawtypes", "unchecked" })
		final List<PluginInfo<? extends P>> typedPlugins = (List) plugins;
		final List<? extends P> instances = createInstances(typedPlugins);
		@SuppressWarnings({ "rawtypes", "unchecked" })
		final List<P> typedInstances = (List) instances;
		return typedInstances;
	}

	@Override
	public <P extends ImageJPlugin> List<? extends P> createInstances(
		final List<PluginInfo<? extends P>> infos)
	{
		final ArrayList<P> list = new ArrayList<P>();
		for (final PluginInfo<? extends P> info : infos) {
			try {
				final P p = info.createInstance();
				list.add(p);
				getContext().inject(p);
				Priority.inject(p, info.getPriority());
			}
			catch (final InstantiableException e) {
				log.error("Cannot create plugin: " + info.getClassName());
			}
		}
		return list;
	}

	// -- Service methods --

	@Override
	public void initialize() {
		pluginIndex = getContext().getPluginIndex();
	}

	// -- Utility methods --

	/**
	 * Transfers plugins of the given class from the source list to the
	 * destination list.
	 * 
	 * @param className The class name of the desired plugins.
	 * @param srcList The list to scan for matching plugins.
	 * @param destList The list to which matching plugins are added.
	 */
	public static <T extends PluginInfo<?>> void getPluginsOfClass(
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
