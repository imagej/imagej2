/*
 * #%L
 * ImageJ software for multidimensional image processing and analysis.
 * %%
 * Copyright (C) 2009 - 2013 Board of Regents of the University of
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

import imagej.command.Command;
import imagej.command.CommandInfo;
import imagej.command.CommandService;
import imagej.plugin.InitPreprocessor;
import imagej.plugin.PreprocessorPlugin;
import imagej.plugin.ValidityPreprocessor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.scijava.log.LogService;
import org.scijava.plugin.AbstractPTService;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.plugin.PluginInfo;
import org.scijava.service.Service;
import org.scijava.util.ClassUtils;

/**
 * Default service for keeping track of the available options and their
 * settings.
 * 
 * @author Curtis Rueden
 * @author Barry DeZonia
 * @see OptionsPlugin
 */
@Plugin(type = Service.class)
public class DefaultOptionsService extends AbstractPTService<OptionsPlugin>
	implements OptionsService
{

	@Parameter
	private LogService log;

	@Parameter
	private CommandService commandService;

	// -- OptionsService methods --

	@Override
	public List<OptionsPlugin> getOptions() {
		// get the list of available options plugins
		final List<PluginInfo<OptionsPlugin>> infos =
			getPluginService().getPluginsOfType(OptionsPlugin.class);

		// instantiate one instance of each options plugin
		final ArrayList<OptionsPlugin> optionsPlugins =
			new ArrayList<OptionsPlugin>();
		for (final PluginInfo<? extends OptionsPlugin> info : infos) {
			optionsPlugins.add(createInstance(info));
		}

		return optionsPlugins;
	}

	@Override
	public <O extends OptionsPlugin> O getOptions(final Class<O> optionsClass) {
		@SuppressWarnings("unchecked")
		final O options = (O) createInstance(getOptionsInfo(optionsClass));
		return options;
	}

	@Override
	public OptionsPlugin getOptions(final String className) {
		return createInstance(getOptionsInfo(className));
	}

	@Override
	public <O extends OptionsPlugin> Object getOption(
		final Class<O> optionsClass, final String name)
	{
		return getInput(getOptionsInfo(optionsClass), name);
	}

	@Override
	public Object getOption(final String className, final String name) {
		return getInput(getOptionsInfo(className), name);
	}

	@Override
	public <O extends OptionsPlugin> Map<String, Object> getOptionsMap(
		final Class<O> optionsClass)
	{
		return getInputs(getOptionsInfo(optionsClass));
	}

	@Override
	public Map<String, Object> getOptionsMap(final String className) {
		return getInputs(getOptionsInfo(className));
	}

	@Override
	public <O extends OptionsPlugin> void setOption(final Class<O> optionsClass,
		final String name, final Object value)
	{
		final CommandInfo info = getOptionsInfo(optionsClass);
		if (info == null) return;
		setOption(info, name, value);
	}

	@Override
	public void setOption(final String className, final String name,
		final Object value)
	{
		final CommandInfo info = getOptionsInfo(className);
		if (info == null) return;
		setOption(info, name, value);
	}

	@Override
	public <O extends OptionsPlugin> void setOption(final CommandInfo info,
		final String name, final Object value)
	{
		final OptionsPlugin optionsPlugin = createInstance(info);
		if (optionsPlugin == null) return; // cannot set option

		// assign value with correct type
		final Class<?> type = info.getInput(name).getType();
		final Object typedValue = ClassUtils.convert(value, type);
		optionsPlugin.setInput(name, typedValue);

		// persist the option value, and publish an OptionsEvent
		optionsPlugin.run();
	}

	// -- PTService methods --

	@Override
	public Class<OptionsPlugin> getPluginType() {
		return OptionsPlugin.class;
	}

	// -- Helper methods --

	/**
	 * Creates an instance of the {@link OptionsPlugin} described by the given
	 * {@link PluginInfo}, preprocessing it with available preprocessors.
	 */
	private OptionsPlugin
		createInstance(final PluginInfo<? extends Command> info)
	{
		if (info == null) return null;

		// instantiate the options plugin
		final Command command = getPluginService().createInstance(info);
		if (command == null) return null;
		if (!(command instanceof OptionsPlugin)) return null;
		final OptionsPlugin optionsPlugin = (OptionsPlugin) command;

		// execute key preprocessors on the newly created options plugin
		final ArrayList<PluginInfo<PreprocessorPlugin>> preInfos =
			new ArrayList<PluginInfo<PreprocessorPlugin>>();
		final PluginInfo<PreprocessorPlugin> validityInfo =
			getPluginService().getPlugin(ValidityPreprocessor.class,
				PreprocessorPlugin.class);
		preInfos.add(validityInfo);
		final PluginInfo<PreprocessorPlugin> initInfo =
			getPluginService().getPlugin(InitPreprocessor.class,
				PreprocessorPlugin.class);
		preInfos.add(initInfo);
		final List<PreprocessorPlugin> pre =
			getPluginService().createInstances(preInfos);
		for (final PreprocessorPlugin pp : pre) {
			pp.process(optionsPlugin);
		}

		return optionsPlugin;
	}

	private <O extends OptionsPlugin> CommandInfo getOptionsInfo(
		final Class<O> optionsClass)
	{
		final CommandInfo info = commandService.getCommand(optionsClass);
		if (info == null) {
			log.error("No such options class: " + optionsClass.getName());
		}
		return info;
	}

	private CommandInfo getOptionsInfo(final String className) {
		final CommandInfo info = commandService.getCommand(className);
		if (info == null) {
			log.error("No such options class: " + className);
			return null;
		}
		if (!OptionsPlugin.class.isAssignableFrom(info.getPluginType())) {
			log.error("Not an options plugin: " + className);
			// not an options plugin
			return null;
		}
		return info;
	}

	private Object getInput(final CommandInfo info, final String name) {
		if (info == null) return null;
		final OptionsPlugin optionsPlugin = createInstance(info);
		if (optionsPlugin == null) return null;
		return optionsPlugin.getInput(name);
	}

	private Map<String, Object> getInputs(final CommandInfo info) {
		if (info == null) return null;
		final OptionsPlugin optionsPlugin = createInstance(info);
		if (optionsPlugin == null) return null;
		return optionsPlugin.getInputs();
	}

}
