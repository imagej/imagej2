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

package imagej.options;

import imagej.ImageJ;
import imagej.event.EventService;
import imagej.ext.InstantiableException;
import imagej.ext.module.Module;
import imagej.ext.module.ModuleException;
import imagej.ext.plugin.IPlugin;
import imagej.ext.plugin.PluginInfo;
import imagej.ext.plugin.PluginModuleInfo;
import imagej.ext.plugin.PluginService;
import imagej.service.AbstractService;
import imagej.service.Service;
import imagej.util.ClassUtils;
import imagej.util.Log;

import java.util.List;
import java.util.Map;

/**
 * Service for keeping track of the available options and their settings.
 * 
 * @author Curtis Rueden
 * @author Barry DeZonia
 * @see OptionsPlugin
 */
@Service
public class OptionsService extends AbstractService {

	private final EventService eventService;
	private final PluginService pluginService;

	// -- Constructors --

	public OptionsService() {
		// NB: Required by SezPoz.
		super(null);
		throw new UnsupportedOperationException();
	}

	public OptionsService(final ImageJ context, final EventService eventService,
		final PluginService pluginService)
	{
		super(context);
		this.eventService = eventService;
		this.pluginService = pluginService;
	}

	// -- OptionsService methods --

	public EventService getEventService() {
		return eventService;
	}

	public PluginService getPluginService() {
		return pluginService;
	}

	/** Gets a list of all available options. */
	public List<OptionsPlugin> getOptions() {
		return pluginService.createInstances(OptionsPlugin.class);
	}

	/** Gets options associated with the given options plugin, or null if none. */
	public <O extends OptionsPlugin> O getOptions(final Class<O> optionsClass) {
		return createInstance(getOptionsInfo(optionsClass));
	}

	/** Gets options associated with the given options plugin, or null if none. */
	public OptionsPlugin getOptions(final String className) {
		return createInstance(getOptionsInfo(className));
	}

	/** Gets the option with the given name, from the specified options plugin. */
	public <O extends OptionsPlugin> Object getOption(
		final Class<O> optionsClass, final String name)
	{
		return getInput(getOptionsInfo(optionsClass), name);
	}

	/** Gets the option with the given name, from the specified options plugin. */
	public Object getOption(final String className, final String name) {
		return getInput(getOptionsInfo(className), name);
	}

	/** Gets a map of all options from the given options plugin. */
	public <O extends OptionsPlugin> Map<String, Object> getOptionsMap(
		final Class<O> optionsClass)
	{
		return getInputs(getOptionsInfo(optionsClass));
	}

	/** Gets a map of all options from the given options plugin. */
	public Map<String, Object> getOptionsMap(final String className) {
		return getInputs(getOptionsInfo(className));
	}

	/**
	 * Sets the option with the given name, from the specified options plugin, to
	 * the given value.
	 */
	public <O extends OptionsPlugin> void setOption(final Class<O> optionsClass,
		final String name, final Object value)
	{
		final PluginModuleInfo<O> info = getOptionsInfo(optionsClass);
		if (info == null) return;
		setOption(info, name, value);
	}

	/**
	 * Sets the option with the given name, from the specified options plugin, to
	 * the given value.
	 */
	public void setOption(final String className, final String name,
		final Object value)
	{
		final PluginModuleInfo<OptionsPlugin> info = getOptionsInfo(className);
		if (info == null) return;
		setOption(info, name, value);
	}

	/**
	 * Sets the option with the given name, from the specified options plugin, to
	 * the given value.
	 */
	public <O extends OptionsPlugin> void setOption(
		final PluginModuleInfo<O> info, final String name, final Object value)
	{
		final Module module;
		try {
			module = info.createModule();
		}
		catch (final ModuleException e) {
			Log.error("Cannot create module: " + info.getClassName());
			return;
		}

		// assign value with correct type
		final Class<?> type = info.getInput(name).getType();
		final Object typedValue = ClassUtils.convert(value, type);
		module.setInput(name, typedValue);

		// persist the option value, and publish an OptionsEvent
		module.run();
	}

	// -- Helper methods --

	private <P extends IPlugin> P createInstance(final PluginInfo<P> info) {
		if (info == null) return null;
		try {
			return info.createInstance();
		}
		catch (final InstantiableException e) {
			Log.error("Cannot create plugin: " + info.getClassName());
		}
		return null;
	}

	private <O extends OptionsPlugin> PluginModuleInfo<O> getOptionsInfo(
		final Class<O> optionsClass)
	{
		final PluginModuleInfo<O> info =
			pluginService.getRunnablePlugin(optionsClass);
		if (info == null) {
			Log.error("No such options class: " + optionsClass.getName());
		}
		return info;
	}

	private PluginModuleInfo<OptionsPlugin>
		getOptionsInfo(final String className)
	{
		final PluginModuleInfo<?> info =
			pluginService.getRunnablePlugin(className);
		if (info == null) {
			Log.error("No such options class: " + className);
			return null;
		}
		if (!OptionsPlugin.class.isAssignableFrom(info.getPluginType())) {
			Log.error("Not an options plugin: " + className);
			// not an options plugin
			return null;
		}
		@SuppressWarnings("unchecked")
		final PluginModuleInfo<OptionsPlugin> typedInfo =
			(PluginModuleInfo<OptionsPlugin>) info;
		return typedInfo;
	}

	private Object getInput(final PluginModuleInfo<?> info, final String name) {
		if (info == null) return null;
		try {
			return info.createModule().getInput(name);
		}
		catch (final ModuleException e) {
			Log.error("Cannot create module: " + info.getClassName());
		}
		return null;
	}

	private Map<String, Object> getInputs(final PluginModuleInfo<?> info) {
		if (info == null) return null;
		try {
			return info.createModule().getInputs();
		}
		catch (final ModuleException e) {
			Log.error("Cannot create module: " + info.getClassName());
		}
		return null;
	}

}
