/*
 * #%L
 * ImageJ software for multidimensional image processing and analysis.
 * %%
 * Copyright (C) 2009 - 2014 Board of Regents of the University of
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
 * #L%
 */

package imagej.command;

import imagej.ImageJPlugin;
import imagej.module.Module;
import imagej.module.ModuleService;
import imagej.module.process.PostprocessorPlugin;
import imagej.module.process.PreprocessorPlugin;
import imagej.service.ImageJService;

import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;

import org.scijava.event.EventService;
import org.scijava.plugin.PTService;
import org.scijava.plugin.PluginInfo;
import org.scijava.plugin.PluginService;

/**
 * Interface for service that keeps track of available commands.
 * <p>
 * A <em>command</em> is a particular type of {@link ImageJPlugin} that is also
 * a {@link Module}; i.e., it is {@link Runnable}, with typed inputs and
 * outputs.
 * <p>
 * The command service keeps a master index of all commands known to the system.
 * It asks the {@link PluginService} for available commands, then takes care of
 * registering them with the {@link ModuleService}.
 * </p>
 * 
 * @author Curtis Rueden
 * @see ImageJPlugin
 * @see ModuleService
 * @see PluginService
 */
public interface CommandService extends PTService<Command>, ImageJService {

	EventService getEventService();

	ModuleService getModuleService();

	/** Gets the list of all available {@link Command}s). */
	List<CommandInfo> getCommands();

	/** Gets the list of {@link Command}s corresponding to the given plugins. */
	<CT extends Command> List<CommandInfo> getCommands(
		final List<PluginInfo<CT>> plugins);

	/** Gets the first available command of the given class, or null if none. */
	<C extends Command> CommandInfo getCommand(Class<C> commandClass);

	/**
	 * Gets the first available command of the given class name, or null if none.
	 */
	CommandInfo getCommand(String className);

	/** Gets the list of {@link Command}s of the given type. */
	<CT extends Command> List<CommandInfo> getCommandsOfType(Class<CT> type);

	/**
	 * Gets the list of commands of the given class.
	 * <p>
	 * Most classes will have only a single match, but some special classes (such
	 * as {@code imagej.legacy.LegacyCommand}) may match many entries.
	 * </p>
	 */
	<C extends Command> List<CommandInfo> getCommandsOfClass(
		Class<C> commandClass);

	/**
	 * Gets the list of commands with the given class name.
	 * <p>
	 * Most classes will have only a single match, but some special classes (such
	 * as {@code imagej.legacy.LegacyCommand}) may match many entries.
	 * </p>
	 */
	List<CommandInfo> getCommandsOfClass(String className);

	/**
	 * Executes the first command of the given class name.
	 * <p>
	 * If no command with the given class name is registered with the service,
	 * then a default one is created and then executed. This default command is
	 * <em>not</em> registered with the service for subsequent usage.
	 * </p>
	 * 
	 * @param className Class name of the command to execute.
	 * @param process If true, executes the command with pre- and postprocessing
	 *          steps from all available {@link PreprocessorPlugin}s and
	 *          {@link PostprocessorPlugin}s in the plugin index; if false,
	 *          executes the command with no pre- or postprocessing.
	 * @param inputs List of input parameter names and values. The expected order
	 *          is in pairs: an input name followed by its value, for each desired
	 *          input to populate. Leaving some inputs unpopulated is allowed.
	 *          Passing the name of an input that is not valid for the plugin, or
	 *          passing a value of a type incompatible with the associated input
	 *          parameter, will issue an error and ignore that name/value pair.
	 * @return {@link Future} of the module instance being executed. Calling
	 *         {@link Future#get()} will block until execution is complete.
	 */
	Future<CommandModule>
		run(String className, boolean process, Object... inputs);

	/**
	 * Executes the first command of the given class name.
	 * <p>
	 * If no command with the given class name is registered with the service,
	 * then a default one is created and then executed. This default command is
	 * <em>not</em> registered with the service for subsequent usage.
	 * </p>
	 * 
	 * @param className Class name of the command to execute.
	 * @param process If true, executes the command with pre- and postprocessing
	 *          steps from all available {@link PreprocessorPlugin}s and
	 *          {@link PostprocessorPlugin}s in the plugin index; if false,
	 *          executes the command with no pre- or postprocessing.
	 * @param inputMap Table of input parameter values, with keys matching the
	 *          plugin's input parameter names. Passing a value of a type
	 *          incompatible with the associated input parameter will issue an
	 *          error and ignore that value.
	 * @return {@link Future} of the module instance being executed. Calling
	 *         {@link Future#get()} will block until execution is complete.
	 */
	Future<CommandModule> run(String className, boolean process,
		Map<String, Object> inputMap);

	/**
	 * Executes the first command of the given class.
	 * <p>
	 * If no command of the given class is registered with the service, then a
	 * default one is created and then executed. This default command is
	 * <em>not</em> registered with the service for subsequent usage.
	 * </p>
	 * 
	 * @param <C> Class of the command to execute.
	 * @param commandClass Class object of the command to execute.
	 * @param process If true, executes the command with pre- and postprocessing
	 *          steps from all available {@link PreprocessorPlugin}s and
	 *          {@link PostprocessorPlugin}s in the plugin index; if false,
	 *          executes the command with no pre- or postprocessing.
	 * @param inputs List of input parameter names and values. The expected order
	 *          is in pairs: an input name followed by its value, for each desired
	 *          input to populate. Leaving some inputs unpopulated is allowed.
	 *          Passing the name of an input that is not valid for the plugin, or
	 *          passing a value of a type incompatible with the associated input
	 *          parameter, will issue an error and ignore that name/value pair.
	 * @return {@link Future} of the module instance being executed. Calling
	 *         {@link Future#get()} will block until execution is complete.
	 */
	<C extends Command> Future<CommandModule> run(Class<C> commandClass,
		boolean process, Object... inputs);

	/**
	 * Executes the first command of the given class.
	 * <p>
	 * If no command of the given class is registered with the service, then a
	 * default one is created and then executed. This default command is
	 * <em>not</em> registered with the service for subsequent usage.
	 * </p>
	 * 
	 * @param <C> Class of the command to execute.
	 * @param commandClass Class object of the command to execute.
	 * @param process If true, executes the command with pre- and postprocessing
	 *          steps from all available {@link PreprocessorPlugin}s and
	 *          {@link PostprocessorPlugin}s in the plugin index; if false,
	 *          executes the command with no pre- or postprocessing.
	 * @param inputMap Table of input parameter values, with keys matching the
	 *          plugin's input parameter names. Passing a value of a type
	 *          incompatible with the associated input parameter will issue an
	 *          error and ignore that value.
	 * @return {@link Future} of the module instance being executed. Calling
	 *         {@link Future#get()} will block until execution is complete.
	 */
	<C extends Command> Future<CommandModule> run(Class<C> commandClass,
		boolean process, Map<String, Object> inputMap);

	/**
	 * Executes the given command.
	 * 
	 * @param info The command to instantiate and run.
	 * @param process If true, executes the command with pre- and postprocessing
	 *          steps from all available {@link PreprocessorPlugin}s and
	 *          {@link PostprocessorPlugin}s in the plugin index; if false,
	 *          executes the command with no pre- or postprocessing.
	 * @param inputs List of input parameter names and values. The expected order
	 *          is in pairs: an input name followed by its value, for each desired
	 *          input to populate. Leaving some inputs unpopulated is allowed.
	 *          Passing the name of an input that is not valid for the plugin, or
	 *          passing a value of a type incompatible with the associated input
	 *          parameter, will issue an error and ignore that name/value pair.
	 * @return {@link Future} of the module instance being executed. Calling
	 *         {@link Future#get()} will block until execution is complete.
	 */
	Future<CommandModule>
		run(CommandInfo info, boolean process, Object... inputs);

	/**
	 * Executes the given command.
	 * 
	 * @param info The command to instantiate and run.
	 * @param process If true, executes the command with pre- and postprocessing
	 *          steps from all available {@link PreprocessorPlugin}s and
	 *          {@link PostprocessorPlugin}s in the plugin index; if false,
	 *          executes the command with no pre- or postprocessing.
	 * @param inputMap Table of input parameter values, with keys matching the
	 *          plugin's input parameter names. Passing a value of a type
	 *          incompatible with the associated input parameter will issue an
	 *          error and ignore that value.
	 * @return {@link Future} of the module instance being executed. Calling
	 *         {@link Future#get()} will block until execution is complete.
	 */
	Future<CommandModule> run(CommandInfo info, boolean process,
		Map<String, Object> inputMap);

}
