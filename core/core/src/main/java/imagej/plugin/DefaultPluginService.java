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
 * Default service for keeping track of available plugins.
 * <p>
 * Available plugins are discovered using a library called <a
 * href="http://sezpoz.java.net/">SezPoz</a>. Loading of the actual plugin
 * classes can be deferred until a particular plugin is actually needed.
 * </p>
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
	public <P extends ImageJPlugin> PluginInfo<ImageJPlugin> getPlugin(
		final Class<P> pluginClass)
	{
		return ListUtils.first(getPluginsOfClass(pluginClass));
	}

	@Override
	public <PT extends ImageJPlugin, P extends PT> PluginInfo<PT>
		getPlugin(final Class<P> pluginClass, final Class<PT> type)
	{
		return ListUtils.first(getPluginsOfClass(pluginClass, type));
	}

	@Override
	public PluginInfo<ImageJPlugin> getPlugin(final String className) {
		return ListUtils.first(getPluginsOfClass(className));
	}

	@Override
	public <PT extends ImageJPlugin> List<PluginInfo<PT>> getPluginsOfType(
		final Class<PT> type)
	{
		return pluginIndex.getPlugins(type);
	}

	@Override
	public <P extends ImageJPlugin> List<PluginInfo<ImageJPlugin>>
		getPluginsOfClass(final Class<P> pluginClass)
	{
		// NB: Since we have the class in question, we attempt to determine its
		// plugin type and limit our search to plugins of that type.
		final Class<? extends ImageJPlugin> pluginType = getPluginType(pluginClass);
		if (pluginType == null) {
			// NB: We failed to guess the type hierarchy of the class in question.
			// We must scan *all* plugins for a match.
			return getPluginsOfClass(pluginClass, ImageJPlugin.class);
		}
		// NB: We successfully determined the plugin's type.
		// We limit our search to plugins of that type.
		@SuppressWarnings({ "rawtypes", "unchecked" })
		final List<PluginInfo<ImageJPlugin>> result =
			(List) getPluginsOfClass(pluginClass, getPluginType(pluginClass));
		return result;
	}

	@Override
	public <PT extends ImageJPlugin, P extends PT> List<PluginInfo<PT>>
		getPluginsOfClass(final Class<P> pluginClass, final Class<PT> type)
	{
		final ArrayList<PluginInfo<PT>> result =
			new ArrayList<PluginInfo<PT>>();
		final String className = pluginClass.getName();
		findPluginsOfClass(className, getPluginsOfType(type), result);
		return result;
	}

	@Override
	public List<PluginInfo<ImageJPlugin>> getPluginsOfClass(
		final String className)
	{
		final ArrayList<PluginInfo<ImageJPlugin>> result =
			new ArrayList<PluginInfo<ImageJPlugin>>();
		// NB: Since we cannot load the class in question, and hence cannot
		// know its type hierarchy, we must scan *all* plugins for a match.
		final List<PluginInfo<?>> allPlugins = getPlugins();
		findPluginsOfClass(className, allPlugins, result);
		return result;
	}

	@Override
	public <PT extends ImageJPlugin> List<PT> createInstancesOfType(
		final Class<PT> type)
	{
		final List<PluginInfo<PT>> plugins = getPluginsOfType(type);
		return createInstances(plugins);
	}

	@Override
	public <PT extends ImageJPlugin> List<PT> createInstances(
		final List<PluginInfo<PT>> infos)
	{
		final ArrayList<PT> list = new ArrayList<PT>();
		for (final PluginInfo<? extends PT> info : infos) {
			final PT p = createInstance(info);
			if (p != null) list.add(p);
		}
		return list;
	}

	@Override
	public <PT extends ImageJPlugin> PT createInstance(final PluginInfo<PT> info) {
		try {
			final PT p = info.createInstance();
			getContext().inject(p);
			Priority.inject(p, info.getPriority());
			return p;
		}
		catch (final InstantiableException exc) {
			log.error("Cannot create plugin: " + info.getClassName());
		}
		return null;
	}

	// -- Service methods --

	@Override
	public void initialize() {
		pluginIndex = getContext().getPluginIndex();
	}

	// -- Utility methods --

	/**
	 * Transfers plugins of the given class from the source list to the
	 * destination list. Note that because this method compares class name
	 * strings, it does not need to actually load the class in question.
	 * 
	 * @param className The class name of the desired plugins.
	 * @param srcList The list to scan for matching plugins.
	 * @param destList The list to which matching plugins are added.
	 */
	public static <T extends PluginInfo<?>> void findPluginsOfClass(
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

	/**
	 * Gets the plugin type of the given plugin class, as declared by its
	 * {@code @Plugin} annotation (i.e., {@link Plugin#type()}).
	 * 
	 * @param pluginClass The plugin class whose plugin type is needed.
	 * @return The plugin type, or null if no {@link Plugin} annotation exists for
	 *         the given class.
	 */
	public static <PT extends ImageJPlugin, P extends PT> Class<PT> getPluginType(
		final Class<P> pluginClass)
	{
		final Plugin annotation = pluginClass.getAnnotation(Plugin.class);
		if (annotation == null) return null;
		@SuppressWarnings("unchecked")
		final Class<PT> type = (Class<PT>) annotation.type();
		return type;
	}

}
