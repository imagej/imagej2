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
import imagej.ext.module.Module;
import imagej.ext.module.ModuleItem;
import imagej.ext.plugin.DynamicPlugin;
import imagej.ext.plugin.Parameter;
import imagej.ext.plugin.PluginModule;
import imagej.ext.plugin.PluginModuleInfo;
import imagej.ext.plugin.PluginService;
import imagej.ext.plugin.RunnablePlugin;
import imagej.options.event.OptionsEvent;

// TODO - outline for how to address issues with options (initializing, aggregating into 1 dialog)

// 1. iterate over all inputs: array of PluginModuleItem?
//     - or, split common logic into ParameterHelper class?
// 2. for each input, call loadValue
// 3. if loaded value is null, ignore; else set parameter to equal loaded value
// This should solve IJ2<->IJ1 initialization problem
// All IJ2 options will be set always, when asked using OptionsService
// Legacy layer just needs to query OptionsService.getOption(Class, String) for values

// Other issue is tabbed options dialog
// can get all OptionsPlugins from the OptionsService
// can iterate over all values of OptionsPlugin?
// 1. Remove all menu parameters from Plugin annotations
// 2. Create Options plugin, extends DynamicPlugin
//    - Aggregates inputs from all OptionsPlugins from OptionsService & PluginService
//    - assigns "group" field matching name of OptionsPlugin
//    - Would iterate over options: one tab per OptionsPlugin class?
//    - One widget per field of that class -- reuse InputWidget logic
//    - List<PluginModuleInfo<OptionsPlugin>> infos = pluginService.getRunnablePlugins(OptionsPlugin.class)
//      from those, can get name & label (for use setting group name, which we'll use for tab name)
// "Best" approach: a "grouped" set of inputs rendered as tabs by input harvester
// that get rendered as tabs by Swing, but potentially something else in other UIs
// Then InputHarvester can "just handle" the Options special plugin

/**
 * Base class for all options-oriented plugins.
 * 
 * @author Barry DeZonia
 * @author Curtis Rueden
 */
public class OptionsPlugin extends DynamicPlugin {

	// -- Parameters --

	@Parameter(persist = false)
	protected EventService eventService;

	// -- OptionsPlugin methods --

	/** Loads option values from persistent storage. */
	public void load() {
		final PluginModule<?> module = createModule(this);
		for (final ModuleItem<?> input : module.getInfo().inputs()) {
			loadInput(module, input);
		}
	}

	/** Saves option values to persistent storage. */
	public void save() {
		final PluginModule<?> module = createModule(this);
		for (final ModuleItem<?> input : module.getInfo().inputs()) {
			saveInput(module, input);
		}
	}

	// -- Runnable methods --

	@Override
	public void run() {
		save();
		eventService.publish(new OptionsEvent(this));
	}

	// -- Helper methods --

	private <R extends RunnablePlugin> PluginModule<R> createModule(
		final R plugin)
	{
		final PluginService pluginService = ImageJ.get(PluginService.class);
		@SuppressWarnings("unchecked")
		final Class<R> pluginClass = (Class<R>) plugin.getClass();
		final PluginModuleInfo<R> info =
			pluginService.getRunnablePlugin(pluginClass);
		return new PluginModule<R>(info, plugin);
	}

	private <T> void loadInput(final Module module, final ModuleItem<T> input) {
		final T value = input.loadValue();
		if (value != null) input.setValue(module, value);
	}

	private <T> void saveInput(final Module module, final ModuleItem<T> input) {
		final T value = input.getValue(module);
		input.saveValue(value);
	}

}
