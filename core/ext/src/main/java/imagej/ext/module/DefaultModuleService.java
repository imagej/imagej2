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

package imagej.ext.module;

import imagej.ImageJ;
import imagej.event.EventService;
import imagej.ext.Accelerator;
import imagej.ext.MenuPath;
import imagej.ext.module.event.ModulesAddedEvent;
import imagej.ext.module.event.ModulesRemovedEvent;
import imagej.ext.module.process.ModulePostprocessor;
import imagej.ext.module.process.ModulePreprocessor;
import imagej.service.AbstractService;
import imagej.service.Service;
import imagej.thread.ThreadService;
import imagej.util.ClassUtils;
import imagej.util.Log;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 * Default service for keeping track of and executing available modules.
 * 
 * @author Curtis Rueden
 * @see Module
 * @see ModuleInfo
 */
@Service
public class DefaultModuleService extends AbstractService implements ModuleService {

	private final EventService eventService;
	private final ThreadService threadService;

	/** Index of registered modules. */
	private final ModuleIndex moduleIndex = new ModuleIndex();

	// -- Constructors --

	public DefaultModuleService() {
		// NB: Required by SezPoz.
		super(null);
		throw new UnsupportedOperationException();
	}

	public DefaultModuleService(final ImageJ context, final EventService eventService,
		final ThreadService threadService)
	{
		super(context);
		this.eventService = eventService;
		this.threadService = threadService;
	}

	// -- ModuleService methods --

	@Override
	public ModuleIndex getIndex() {
		return moduleIndex;
	}

	@Override
	public void addModule(final ModuleInfo module) {
		if (moduleIndex.add(module))
			eventService.publish(new ModulesAddedEvent(module));
	}

	@Override
	public void removeModule(final ModuleInfo module) {
		if (moduleIndex.remove(module))
			eventService.publish(new ModulesRemovedEvent(module));
	}

	@Override
	public void addModules(final Collection<? extends ModuleInfo> modules) {
		if (moduleIndex.addAll(modules))
			eventService.publish(new ModulesAddedEvent(modules));
	}

	@Override
	public void removeModules(final Collection<? extends ModuleInfo> modules) {
		if (moduleIndex.removeAll(modules))
			eventService.publish(new ModulesRemovedEvent(modules));
	}

	@Override
	public List<ModuleInfo> getModules() {
		return moduleIndex.getAll();
	}

	@Override
	public ModuleInfo getModuleForAccelerator(final Accelerator acc) {
		for (final ModuleInfo info : getModules()) {
			final MenuPath menuPath = info.getMenuPath();
			if (menuPath == null || menuPath.isEmpty()) continue;
			if (acc.equals(menuPath.getLeaf().getAccelerator())) return info;
		}
		return null;
	}

	@Override
	public Future<Module>
		run(final ModuleInfo info, final Object... inputValues)
	{
		return run(info, null, null, inputValues);
	}

	@Override
	public Future<Module> run(final ModuleInfo info,
		final List<? extends ModulePreprocessor> pre,
		final List<? extends ModulePostprocessor> post,
		final Object... inputValues)
	{
		return run(info, pre, post, createMap(info, inputValues));
	}

	@Override
	public Future<Module> run(final ModuleInfo info,
		final List<? extends ModulePreprocessor> pre,
		final List<? extends ModulePostprocessor> post,
		final Map<String, Object> inputMap)
	{
		try {
			final Module module = info.createModule();
			return run(module, pre, post, inputMap);
		}
		catch (final ModuleException e) {
			Log.error("Could not execute module: " + info, e);
		}
		return null;
	}

	@Override
	public Future<Module> run(final Module module, final Object... inputValues) {
		return run(module, null, null, inputValues);
	}

	@Override
	public Future<Module> run(final Module module,
		final List<? extends ModulePreprocessor> pre,
		final List<? extends ModulePostprocessor> post,
		final Object... inputValues)
	{
		return run(module, pre, post, createMap(module.getInfo(), inputValues));
	}

	@Override
	public <M extends Module> Future<M> run(final M module,
		final List<? extends ModulePreprocessor> pre,
		final List<? extends ModulePostprocessor> post,
		final Map<String, Object> inputMap)
	{
		assignInputs(module, inputMap);
		final ModuleRunner runner = new ModuleRunner(module, pre, post);
		@SuppressWarnings("unchecked")
		final Future<M> future = (Future<M>) threadService.run(runner);
		return future;
	}

	@Override
	public <M extends Module> M waitFor(final Future<M> future) {
		try {
			return future.get();
		}
		catch (final InterruptedException e) {
			Log.error("Module execution interrupted", e);
		}
		catch (final ExecutionException e) {
			Log.error("Error during module execution", e);
		}
		return null;
	}

	@Override
	public <T> ModuleItem<T> getSingleInput(final Module module,
		final Class<T> type)
	{
		return getSingleItem(module, type, module.getInfo().inputs());
	}

	@Override
	public <T> ModuleItem<T> getSingleOutput(final Module module,
		final Class<T> type)
	{
		return getSingleItem(module, type, module.getInfo().outputs());
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
		i++;
		if (i != values.length) {
			Log.warn("Argument mismatch: " + values.length + " of " + i +
				" inputs provided:");
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

	private <T> ModuleItem<T> getSingleItem(final Module module,
		final Class<T> type, final Iterable<ModuleItem<?>> items)
	{
		ModuleItem<T> result = null;
		for (final ModuleItem<?> item : items) {
			final String name = item.getName();
			final boolean resolved = module.isResolved(name);
			if (resolved) continue; // skip resolved inputs
			if (!type.isAssignableFrom(item.getType())) continue;
			if (result != null) return null; // multiple matching items
			@SuppressWarnings("unchecked")
			final ModuleItem<T> typedItem = (ModuleItem<T>) item;
			result = typedItem;
		}
		return result;
	}

}
