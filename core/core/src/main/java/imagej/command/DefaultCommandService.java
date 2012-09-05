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

package imagej.command;

import imagej.event.EventHandler;
import imagej.event.EventService;
import imagej.log.LogService;
import imagej.module.Module;
import imagej.module.ModuleInfo;
import imagej.module.ModuleService;
import imagej.plugin.ImageJPlugin;
import imagej.plugin.Parameter;
import imagej.plugin.Plugin;
import imagej.plugin.PluginInfo;
import imagej.plugin.PluginService;
import imagej.plugin.PostprocessorPlugin;
import imagej.plugin.PreprocessorPlugin;
import imagej.plugin.ServicePreprocessor;
import imagej.plugin.event.PluginsAddedEvent;
import imagej.plugin.event.PluginsRemovedEvent;
import imagej.service.AbstractService;
import imagej.service.Service;
import imagej.util.ListUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;

/**
 * Default service for working with {@link Command}s. Available commands are
 * obtained from the plugin service. Loading of the actual command classes can
 * be deferred until a particular command's first execution.
 * 
 * @author Curtis Rueden
 * @see Command
 */
@Plugin(type = Service.class)
public class DefaultCommandService extends AbstractService implements
	CommandService
{

	@Parameter
	private LogService log;

	@Parameter
	private EventService eventService;

	@Parameter
	private PluginService pluginService;

	@Parameter
	private ModuleService moduleService;

	/** Mapping from vanilla plugin metadata to command metadata objects. */
	private HashMap<PluginInfo<?>, CommandInfo<?>> commandMap;

	// -- CommandService methods --

	@Override
	public EventService getEventService() {
		return eventService;
	}

	@Override
	public PluginService getPluginService() {
		return pluginService;
	}

	@Override
	public ModuleService getModuleService() {
		return moduleService;
	}

	@Override
	public List<CommandInfo<Command>> getCommands() {
		return getCommandsOfType(Command.class);
	}

	@Override
	public <C extends Command> CommandInfo<C> getCommand(
		final Class<C> commandClass)
	{
		return ListUtils.first(getCommandsOfClass(commandClass));
	}

	@Override
	public CommandInfo<Command> getCommand(final String className) {
		return ListUtils.first(getCommandsOfClass(className));
	}

	@Override
	public <C extends Command> List<CommandInfo<C>> getCommandsOfType(
		final Class<C> type)
	{
		final List<PluginInfo<C>> plugins = pluginService.getPluginsOfType(type);
		final List<CommandInfo<C>> commands = getTypedCommands(plugins);
		return commands;
	}

	@Override
	public <C extends Command> List<CommandInfo<C>> getCommandsOfClass(
		final Class<C> commandClass)
	{
		final List<PluginInfo<C>> plugins =
			pluginService.getPluginsOfClass(commandClass);
		final List<CommandInfo<C>> commands = getTypedCommands(plugins);
		return commands;
	}

	@Override
	public List<CommandInfo<Command>> getCommandsOfClass(final String className) {
		final List<PluginInfo<ImageJPlugin>> plugins =
			pluginService.getPluginsOfClass(className);
		final List<CommandInfo<Command>> commands = getCommands(downcast(plugins));
		return commands;
	}

	@Override
	public <C extends Command> CommandInfo<C> populateServices(final C command) {
		@SuppressWarnings("unchecked")
		final Class<C> c = (Class<C>) command.getClass();
		final CommandInfo<C> info = getCommand(c);
		final CommandModule<C> module = new CommandModule<C>(info, command);
		final ServicePreprocessor servicePreprocessor = new ServicePreprocessor();
		servicePreprocessor.setContext(getContext());
		servicePreprocessor.process(module);
		return info;
	}

	@Override
	public Future<Module> run(final String className, final Object... inputs) {
		final CommandInfo<?> command = getCommand(className);
		if (!checkCommand(command, className)) return null;
		return run(command, inputs);
	}

	@Override
	public Future<Module> run(final String className,
		final Map<String, Object> inputMap)
	{
		final CommandInfo<?> command = getCommand(className);
		if (!checkCommand(command, className)) return null;
		return run(command, inputMap);
	}

	@Override
	public <C extends Command> Future<CommandModule<C>> run(
		final Class<C> commandClass, final Object... inputs)
	{
		final CommandInfo<C> command = getCommand(commandClass);
		if (!checkCommand(command, commandClass.getName())) return null;
		@SuppressWarnings({ "rawtypes", "unchecked" })
		final Future<CommandModule<C>> future = (Future) run(command, inputs);
		return future;
	}

	@Override
	public <C extends Command> Future<CommandModule<C>> run(
		final Class<C> commandClass, final Map<String, Object> inputMap)
	{
		final CommandInfo<C> command = getCommand(commandClass);
		if (!checkCommand(command, commandClass.getName())) return null;
		@SuppressWarnings({ "rawtypes", "unchecked" })
		final Future<CommandModule<C>> future = (Future) run(command, inputMap);
		return future;
	}

	@Override
	public Future<Module> run(final ModuleInfo info, final Object... inputs) {
		return moduleService.run(info, pre(), post(), inputs);
	}

	@Override
	public Future<Module> run(final ModuleInfo info,
		final Map<String, Object> inputMap)
	{
		return moduleService.run(info, pre(), post(), inputMap);
	}

	@Override
	public <M extends Module> Future<M> run(final M module,
		final Object... inputs)
	{
		return moduleService.run(module, pre(), post(), inputs);
	}

	@Override
	public <M extends Module> Future<M> run(final M module,
		final Map<String, Object> inputMap)
	{
		return moduleService.run(module, pre(), post(), inputMap);
	}

	// -- Service methods --

	@Override
	public void initialize() {
		commandMap = new HashMap<PluginInfo<?>, CommandInfo<?>>();

		// inform the module service of available commands
		final List<PluginInfo<Command>> plugins =
			pluginService.getPluginsOfType(Command.class);
		addCommands(downcast(plugins));

		subscribeToEvents(eventService);
	}

	// -- Event handlers --

	@EventHandler
	protected void onEvent(final PluginsRemovedEvent event) {
		removeCommands(event.getItems());
	}

	@EventHandler
	protected void onEvent(final PluginsAddedEvent event) {
		addCommands(event.getItems());
	}

	// -- Helper methods --

	/** Creates the preprocessor chain. */
	private List<? extends PreprocessorPlugin> pre() {
		return pluginService.createInstancesOfType(PreprocessorPlugin.class);
	}

	/** Creates the postprocessor chain. */
	private List<? extends PostprocessorPlugin> post() {
		return pluginService.createInstancesOfType(PostprocessorPlugin.class);
	}

	/** Logs an error if the given command is null. */
	private boolean checkCommand(final CommandInfo<?> command, final String name)
	{
		if (command == null) {
			log.error("No such command: " + name);
			return false;
		}
		return true;
	}

	/** Adds new commands to the module service. */
	private void addCommands(final List<PluginInfo<?>> plugins) {
		// extract commands from the list of plugins
		final List<CommandInfo<?>> commands = new ArrayList<CommandInfo<?>>();
		for (final PluginInfo<?> info : plugins) {
			if (!isCommand(info)) continue;
			@SuppressWarnings("unchecked")
			final PluginInfo<? extends Command> typedInfo =
				(PluginInfo<? extends Command>) info;
			final CommandInfo<?> commandInfo = wrapAsCommand(typedInfo);
			commands.add(commandInfo);

			// record association between plugin info and derived command info
			commandMap.put(info, commandInfo);
		}

		// add extracted commands to the module service
		moduleService.addModules(commands);
	}

	/** Removes old commands from the module service. */
	private void removeCommands(final List<PluginInfo<?>> plugins) {
		final List<CommandInfo<Command>> commands = getCommands(plugins);

		for (final CommandInfo<Command> info : commands) {
			// clear association between plugin info and derived command info
			commandMap.remove(info);
		}

		// remove extracted commands from the module service
		moduleService.removeModules(commands);
	}

	/**
	 * Gets the command corresponding to each plugin on the given list. The
	 * linkage is obtained from the {@link #commandMap}.
	 */
	private List<CommandInfo<Command>> getCommands(
		final List<PluginInfo<?>> plugins)
	{
		final List<CommandInfo<Command>> commands =
			new ArrayList<CommandInfo<Command>>();
		for (final PluginInfo<?> info : plugins) {
			@SuppressWarnings("unchecked")
			final CommandInfo<Command> commandInfo =
				(CommandInfo<Command>) commandMap.get(info);
			if (commandInfo == null) continue;
			commands.add(commandInfo);
		}
		return commands;
	}

	/**
	 * Gets the command corresponding to each plugin on the given list. The
	 * linkage is obtained from the {@link #commandMap}.
	 */
	private <C extends Command> List<CommandInfo<C>> getTypedCommands(
		final List<PluginInfo<C>> plugins)
	{
		final List<CommandInfo<Command>> commands = getCommands(downcast(plugins));
		@SuppressWarnings({ "rawtypes", "unchecked" })
		final List<CommandInfo<C>> typedCommands = (List) commands;
		return typedCommands;
	}

	/** Determines whether the given plugin is a command. */
	private boolean isCommand(final PluginInfo<?> info) {
		return Command.class.isAssignableFrom(info.getPluginType());
	}

	/** Converts the given plugin into a command. */
	private <C extends Command> CommandInfo<C> wrapAsCommand(
		final PluginInfo<C> pluginInfo)
	{
		if (pluginInfo instanceof CommandInfo) {
			// plugin info is already a command info
			return (CommandInfo<C>) pluginInfo;
		}
		// wrap the plugin's metadata in a command info
		final String className = pluginInfo.getClassName();
		final Class<C> type = pluginInfo.getPluginType();
		final Plugin annotation = pluginInfo.getAnnotation();
		return new CommandInfo<C>(className, type, annotation);
	}

	/** A HACK for downcasting a list of plugins. */
	private <P extends ImageJPlugin> List<PluginInfo<?>> downcast(
		final List<PluginInfo<P>> plugins)
	{
		// HACK: It seems that List<PluginInfo<? extends P>> cannot be used to
		// fulfill a method argument of type List<PluginInfo<?>>. Probably something
		// relating to (lack of) covariance of generics that I am too stupid to
		// understand. So we brute force it!
		@SuppressWarnings({ "rawtypes", "unchecked" })
		final List<PluginInfo<?>> typedPlugins = (List) plugins;
		return typedPlugins;
	}

}
