//
// ModuleService.java
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

package imagej.ext.module;

import imagej.AbstractService;
import imagej.ImageJ;
import imagej.Service;
import imagej.event.EventService;
import imagej.ext.module.event.ModulesAddedEvent;
import imagej.ext.module.event.ModulesRemovedEvent;
import imagej.ext.module.process.ModulePostprocessor;
import imagej.ext.module.process.ModulePreprocessor;
import imagej.util.ClassUtils;
import imagej.util.Log;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Service for keeping track of and executing available modules.
 * 
 * @author Curtis Rueden
 * @see Module
 * @see ModuleInfo
 */
@Service
public class ModuleService extends AbstractService {

	private final EventService eventService;

	/** Index of registered modules. */
	private final ModuleIndex moduleIndex = new ModuleIndex();

	// -- Constructors --

	public ModuleService() {
		// NB: Required by SezPoz.
		super(null);
		throw new UnsupportedOperationException();
	}

	public ModuleService(final ImageJ context, final EventService eventService) {
		super(context);
		this.eventService = eventService;
	}

	// -- ModuleService methods --

	/** Gets the index of available modules. */
	public ModuleIndex getIndex() {
		return moduleIndex;
	}

	/** Manually registers a module with the module service. */
	public void addModule(final ModuleInfo module) {
		moduleIndex.add(module);
		eventService.publish(new ModulesAddedEvent(module));
	}

	/** Manually unregisters a module with the module service. */
	public void removeModule(final ModuleInfo module) {
		moduleIndex.remove(module);
		eventService.publish(new ModulesRemovedEvent(module));
	}

	/** Manually registers a list of modules with the module service. */
	public void addModules(final Collection<? extends ModuleInfo> modules) {
		moduleIndex.addAll(modules);
		eventService.publish(new ModulesAddedEvent(modules));
	}

	/** Manually unregisters a list of modules with the module service. */
	public void removeModules(final Collection<? extends ModuleInfo> modules) {
		moduleIndex.removeAll(modules);
		eventService.publish(new ModulesRemovedEvent(modules));
	}

	/** Gets the list of available modules. */
	public List<ModuleInfo> getModules() {
		return moduleIndex.getAll();
	}

	/**
	 * Executes the given module, without any pre- or postprocessing.
	 * 
	 * @param info The module to instantiate and run.
	 * @param separateThread Whether to execute the module in a new thread.
	 * @param inputValues List of input parameter values, in the same order
	 *          declared by the {@link ModuleInfo}. Passing a number of values
	 *          that differs from the number of input parameters is allowed, but
	 *          will issue a warning. Passing a value of a type incompatible with
	 *          the associated input parameter will issue an error and ignore that
	 *          value.
	 * @return module instance that was executed
	 */
	public Module run(final ModuleInfo info, final boolean separateThread,
		final Object... inputValues)
	{
		return run(info, null, null, separateThread, inputValues);
	}

	/**
	 * Executes the given module.
	 * 
	 * @param info The module to instantiate and run.
	 * @param pre List of preprocessing steps to perform.
	 * @param post List of postprocessing steps to perform.
	 * @param separateThread Whether to execute the module in a new thread.
	 * @param inputValues List of input parameter values, in the same order
	 *          declared by the {@link ModuleInfo}. Passing a number of values
	 *          that differs from the number of input parameters is allowed, but
	 *          will issue a warning. Passing a value of a type incompatible with
	 *          the associated input parameter will issue an error and ignore that
	 *          value.
	 * @return module instance that was executed
	 */
	public Module run(final ModuleInfo info,
		final List<? extends ModulePreprocessor> pre,
		final List<? extends ModulePostprocessor> post,
		final boolean separateThread, final Object... inputValues)
	{
		return run(info, pre, post, separateThread, createMap(info, inputValues));
	}

	/**
	 * Executes the given module.
	 * 
	 * @param info The module to instantiate and run.
	 * @param pre List of preprocessing steps to perform.
	 * @param post List of postprocessing steps to perform.
	 * @param separateThread Whether to execute the module in a new thread.
	 * @param inputMap Table of input parameter values, with keys matching the
	 *          {@link ModuleInfo}'s input parameter names. Passing a value of a
	 *          type incompatible with the associated input parameter will issue
	 *          an error and ignore that value.
	 * @return module instance that was executed
	 */
	public Module run(final ModuleInfo info,
		final List<? extends ModulePreprocessor> pre,
		final List<? extends ModulePostprocessor> post,
		final boolean separateThread, final Map<String, Object> inputMap)
	{
		try {
			final Module module = info.createModule();
			run(module, pre, post, separateThread, inputMap);
			return module;
		}
		catch (final ModuleException e) {
			Log.error("Could not execute module: " + info, e);
		}
		return null;
	}

	/**
	 * Executes the given module, without any pre- or postprocessing.
	 * 
	 * @param module The module to run.
	 * @param separateThread Whether to execute the module in a new thread.
	 * @param inputValues List of input parameter values, in the same order
	 *          declared by the module's {@link ModuleInfo}. Passing a number of
	 *          values that differs from the number of input parameters is
	 *          allowed, but will issue a warning. Passing a value of a type
	 *          incompatible with the associated input parameter will issue an
	 *          error and ignore that value.
	 */
	public void run(final Module module, final boolean separateThread,
		final Object... inputValues)
	{
		run(module, null, null, separateThread, inputValues);
	}

	/**
	 * Executes the given module.
	 * 
	 * @param module The module to run.
	 * @param pre List of preprocessing steps to perform.
	 * @param post List of postprocessing steps to perform.
	 * @param separateThread Whether to execute the module in a new thread.
	 * @param inputValues List of input parameter values, in the same order
	 *          declared by the module's {@link ModuleInfo}. Passing a number of
	 *          values that differs from the number of input parameters is
	 *          allowed, but will issue a warning. Passing a value of a type
	 *          incompatible with the associated input parameter will issue an
	 *          error and ignore that value.
	 */
	public void run(final Module module,
		final List<? extends ModulePreprocessor> pre,
		final List<? extends ModulePostprocessor> post,
		final boolean separateThread, final Object... inputValues)
	{
		run(module, pre, post, separateThread, createMap(module.getInfo(),
			inputValues));
	}

	/**
	 * Executes the given module.
	 * 
	 * @param module The module to run.
	 * @param pre List of preprocessing steps to perform.
	 * @param post List of postprocessing steps to perform.
	 * @param separateThread Whether to execute the module in a new thread.
	 * @param inputMap Table of input parameter values, with keys matching the
	 *          module's {@link ModuleInfo}'s input parameter names. Passing a
	 *          value of a type incompatible with the associated input parameter
	 *          will issue an error and ignore that value.
	 */
	public void run(final Module module,
		final List<? extends ModulePreprocessor> pre,
		final List<? extends ModulePostprocessor> post,
		final boolean separateThread, final Map<String, Object> inputMap)
	{
		assignInputs(module, inputMap);

		// TODO - Implement a better threading mechanism for launching modules.
		// Perhaps a ThreadService so that the UI can query currently
		// running modules and so forth?
		if (separateThread) {
			final String className = module.getInfo().getDelegateClassName();
			final String threadName =
				"ImageJ-" + getContext().getID() + "-ModuleRunner-" + className;
			new Thread(new Runnable() {

				@Override
				public void run() {
					new ModuleRunner(module, pre, post).run();
				}

			}, threadName).start();
		}
		else module.run();
	}

	// -- IService methods --

	@Override
	public void initialize() {
		// no action needed
	}

	// -- Helper methods --

	/**
	 * Converts the given list of values into an input map for use with a module
	 * of the specified {@link ModuleInfo}.
	 */
	private Map<String, Object> createMap(final ModuleInfo info,
		final Object[] values)
	{
		if (values == null || values.length == 0) return null;

		final HashMap<String, Object> inputMap = new HashMap<String, Object>();
		int i = -1;
		for (final ModuleItem<?> input : info.inputs()) {
			i++;
			if (i >= values.length) continue; // no more values
			final String name = input.getName();
			final Object value = values[i];
			inputMap.put(name, value);
		}
		if (i != values.length) {
			Log.warn("Argument mismatch: " + values.length + " of " + i +
				" inputs provided.");
		}
		return inputMap;
	}

	/** Sets the given module's input values to those in the given map. */
	private void assignInputs(final Module module,
		final Map<String, Object> inputMap)
	{
		if (inputMap == null) return; // no inputs to assign

		for (final String name : inputMap.keySet()) {
			final ModuleItem<?> input = module.getInfo().getInput(name);
			if (input == null) {
				Log.error("No such input: " + name);
				continue;
			}
			final Object value = inputMap.get(name);
			final Class<?> type = input.getType();
			final Object converted = ClassUtils.convert(value, type);
			if (value != null && converted == null) {
				Log.error("For input " + name + ": incompatible object " +
					value.getClass().getName() + " for type " + type.getName());
				continue;
			}
			module.setInput(name, converted);
			module.setResolved(name, true);
		}
	}

}
