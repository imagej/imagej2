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

package imagej.options;

import imagej.command.DynamicCommand;
import imagej.module.ModuleItem;
import imagej.options.event.OptionsEvent;

import org.scijava.event.EventService;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.SingletonPlugin;
import org.scijava.util.Prefs;

// TODO - outline for how to address issues with options (initializing, aggregating into 1 dialog)

// 1. iterate over all inputs: array of CommandModuleItem?
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
// 2. Create Options plugin, extends DynamicCommand
//    - Aggregates inputs from all OptionsPlugins from OptionsService & PluginService
//    - assigns "group" field matching name of OptionsPlugin
//    - Would iterate over options: one tab per OptionsPlugin class?
//    - One widget per field of that class -- reuse InputWidget logic
//    - List<CommandInfo<OptionsPlugin>> infos = pluginService.getCommands(OptionsPlugin.class)
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
public class OptionsPlugin extends DynamicCommand implements SingletonPlugin {

	// -- Parameters --

	@Parameter
	protected EventService eventService;

	// -- OptionsPlugin methods --

	/** Loads option values from persistent storage. */
	public void load() {
		for (final ModuleItem<?> input : getInfo().inputs()) {
			loadInput(input);
		}
	}

	/** Saves option values to persistent storage. */
	public void save() {
		for (final ModuleItem<?> input : getInfo().inputs()) {
			saveInput(input);
		}
	}

	/** Clears option values from persistent storage. */
	public void reset() {
		Prefs.clear(getClass());
	}

	// -- Runnable methods --

	@Override
	public void run() {
		save();

		// NB: Clear "resolved" status of all inputs.
		// Otherwise, no inputs are harvested on next run.
		for (final ModuleItem<?> input : getInfo().inputs()) {
			setResolved(input.getName(), false);
		}

		eventService.publish(new OptionsEvent(this));
	}

	// -- Helper methods --

	private <T> void loadInput(final ModuleItem<T> input) {
		final T value = input.loadValue();
		if (value != null) input.setValue(this, value);
	}

	private <T> void saveInput(final ModuleItem<T> input) {
		final T value = input.getValue(this);
		input.saveValue(value);
	}

}
