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

import imagej.service.Service;

import java.util.Collection;
import java.util.List;

/**
 * Interface for service that keeps track of available plugins.
 * <p>
 * The plugin service keeps a master index of all plugins known to the system.
 * At heart, a plugin is a piece of functionality that extends ImageJ's
 * capabilities. Plugins take many forms; see {@link ImageJPlugin} for details.
 * </p>
 * <p>
 * The default plugin service discovers available plugins on the classpath.
 * </p>
 * <p>
 * A <em>plugin</em> is distinct from a {@link imagej.module.Module} in that
 * plugins extend ImageJ's functionality in some way, taking many forms, whereas
 * modules are always runnable code with typed inputs and outputs. There is a
 * particular type of plugin called a {@link imagej.command.Command} which is
 * also a module, but many plugins (e.g., {@link imagej.tool.Tool}s and
 * {@link imagej.display.Display}s) are not modules.
 * </p>
 * 
 * @author Curtis Rueden
 * @see ImageJPlugin
 */
public interface PluginService extends Service {

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

	/**
	 * Gets the first available plugin of the given class, or null if none.
	 * 
	 * @param <P> The <em>class</em> of the plugin to look up.
	 */
	<P extends ImageJPlugin> PluginInfo<ImageJPlugin>
		getPlugin(Class<P> pluginClass);

	/**
	 * Gets the first available plugin of the given class, or null if none.
	 * 
	 * @param <PT> The <em>type</em> of the plugin to look up. See
	 *          {@link ImageJPlugin} for a list of common plugin types.
	 * @param <P> The <em>class</em> of the plugin to look up.
	 */
	<PT extends ImageJPlugin, P extends PT> PluginInfo<PT>
		getPlugin(Class<P> pluginClass, Class<PT> type);

	/**
	 * Gets the first available plugin of the given class name, or null if none.
	 */
	PluginInfo<ImageJPlugin> getPlugin(String className);

	/**
	 * Gets the list of plugins of the given type (e.g.,
	 * {@link imagej.command.Command}).
	 * 
	 * @param <PT> The <em>type</em> of plugins to look up. See
	 *          {@link ImageJPlugin} for a list of common plugin types.
	 */
	<PT extends ImageJPlugin> List<PluginInfo<PT>>
		getPluginsOfType(Class<PT> type);

	/**
	 * Gets the list of plugins of the given class.
	 * <p>
	 * Most classes will have only a single match, but some special classes (such
	 * as {@code imagej.legacy.LegacyCommand}) may match many entries.
	 * </p>
	 * 
	 * @param <P> The <em>class</em> of plugins to look up.
	 */
	<P extends ImageJPlugin> List<PluginInfo<ImageJPlugin>>
		getPluginsOfClass(Class<P> pluginClass);

	/**
	 * Gets the list of plugins of the given class.
	 * <p>
	 * Most classes will have only a single match, but some special classes (such
	 * as {@code imagej.legacy.LegacyCommand}) may match many entries.
	 * </p>
	 * 
	 * @param <PT> The <em>type</em> of plugins to look up. See
	 *          {@link ImageJPlugin} for a list of common plugin types.
	 * @param <P> The <em>class</em> of plugins to look up.
	 */
	<PT extends ImageJPlugin, P extends PT> List<PluginInfo<PT>>
		getPluginsOfClass(Class<P> pluginClass, Class<PT> type);

	/**
	 * Gets the list of plugins with the given class name.
	 * <p>
	 * Most classes will have only a single match, but some special classes (such
	 * as {@code imagej.legacy.LegacyCommand}) may match many entries.
	 * </p>
	 */
	List<PluginInfo<ImageJPlugin>> getPluginsOfClass(String className);

	/**
	 * Creates one instance each of the available plugins of the given type.
	 * <p>
	 * Note that in the case of commands, this method does <em>not</em> do any
	 * preprocessing on the command instances, so parameters will not be
	 * auto-populated, initializers will not be executed, etc.
	 * </p>
	 * 
	 * @param <PT> The <em>type</em> of plugins to instantiate. See
	 *          {@link ImageJPlugin} for a list of common plugin types.
	 */
	<PT extends ImageJPlugin> List<PT> createInstancesOfType(Class<PT> type);

	/**
	 * Creates an instance of each of the plugins on the given list.
	 * <p>
	 * If the plugin implements the {@link imagej.Contextual} interface, the
	 * appropriate context is injected. Similarly, if the plugin implements the
	 * {@link imagej.Prioritized} interface, the appropriate priority is injected.
	 * </p>
	 * <p>
	 * Note that in the case of commands, this method does <em>not</em> do any
	 * preprocessing on the command instances, so parameters will not be
	 * auto-populated, initializers will not be executed, etc.
	 * </p>
	 * 
	 * @param <PT> The <em>type</em> of plugins to instantiate. See
	 *          {@link ImageJPlugin} for a list of common plugin types.
	 */
	<PT extends ImageJPlugin> List<PT>
		createInstances(List<PluginInfo<PT>> infos);

	/**
	 * Creates an instance of the given plugin.
	 * <p>
	 * If the plugin implements the {@link imagej.Contextual} interface, the
	 * appropriate context is injected. Similarly, if the plugin implements the
	 * {@link imagej.Prioritized} interface, the appropriate priority is injected.
	 * </p>
	 * <p>
	 * Note that in the case of commands, this method does <em>not</em> do any
	 * preprocessing on the command instances, so parameters will not be
	 * auto-populated, initializers will not be executed, etc.
	 * </p>
	 * 
	 * @param <PT> The <em>type</em> of plugin to instantiate. See
	 *          {@link ImageJPlugin} for a list of common plugin types.
	 */
	<PT extends ImageJPlugin> PT createInstance(PluginInfo<PT> info);

}
