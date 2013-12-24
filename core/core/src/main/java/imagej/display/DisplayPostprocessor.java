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

package imagej.display;

import imagej.module.Module;
import imagej.module.ModuleItem;
import imagej.module.process.AbstractPostprocessorPlugin;
import imagej.module.process.PostprocessorPlugin;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.scijava.Priority;
import org.scijava.log.LogService;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

/**
 * Displays outputs upon completion of a module execution.
 * 
 * @author Curtis Rueden
 * @author Lee Kamentsky
 * @author Barry DeZonia
 */
@Plugin(type = PostprocessorPlugin.class,
	priority = Priority.VERY_LOW_PRIORITY)
public class DisplayPostprocessor extends AbstractPostprocessorPlugin {

	@Parameter(required = false)
	private DisplayService displayService;

	@Parameter(required = false)
	private LogService log;

	@Override
	public void process(final Module module) {
		if (displayService == null) return;

		for (final ModuleItem<?> outputItem : module.getInfo().outputs()) {
			final Object value = outputItem.getValue(module);
			final String name = defaultName(outputItem);
			handleOutput(name, value);
		}
	}

	// -- Helper methods --

	/**
	 * Displays output objects.
	 * 
	 * @param defaultName The default name for the display, if not already set.
	 * @param output The object to display.
	 */
	private void handleOutput(final String defaultName, final Object output) {
		if (output == null) {
			// ignore null outputs
			return;
		}

		if (output instanceof Display) {
			// output is itself a display; just update it
			final Display<?> display = (Display<?>) output;
			display.update();
			return;
		}

		final boolean addToExisting = addToExisting(output);
		final ArrayList<Display<?>> displays = new ArrayList<Display<?>>();

		// get list of existing displays currently visualizing this output
		final List<Display<?>> existingDisplays =
			displayService.getDisplays(output);
		displays.addAll(existingDisplays);

		if (displays.isEmpty()) {
			// output was not already displayed
			final Display<?> activeDisplay = displayService.getActiveDisplay();

			if (addToExisting && activeDisplay.canDisplay(output)) {
				// add output to existing display if possible
				activeDisplay.display(output);
				displays.add(activeDisplay);
			}
			else {
				// create a new display for the output
				final Display<?> display = displayService.createDisplay(output);
				if (display != null) {
					displays.add(display);
					if (display.getName() == null) {
						// set a default name based on the parameter
						display.setName(defaultName);
					}
				}
			}
		}

		if (!displays.isEmpty()) {
			// output was successfully associated with at least one display
			for (final Display<?> display : displays) {
				display.update();
			}
			return;
		}

		if (output instanceof Map) {
			// handle each item of the map separately
			final Map<?, ?> map = (Map<?, ?>) output;
			for (final Object key : map.keySet()) {
				final String itemName = key.toString();
				final Object itemValue = map.get(key);
				handleOutput(itemName, itemValue);
			}
			return;
		}

		if (output instanceof Collection) {
			// handle each item of the collection separately
			final Collection<?> collection = (Collection<?>) output;
			for (final Object item : collection) {
				handleOutput(defaultName, item);
			}
			return;
		}

		// no available displays for this type of output
		if (log != null) {
			final String valueClass =
				output == null ? "null" : output.getClass().getName();
			log.warn("Ignoring unsupported output: " + defaultName + " [" +
				valueClass + "]");
		}
	}

	private boolean addToExisting(final Object output) {
		// TODO - find a general way to decide this
		return false;
	}

	private String defaultName(final ModuleItem<?> item) {
		final String label = item.getLabel();
		if (label != null && !label.isEmpty()) return label;
		final String name = item.getName();
		if (name != null && !name.isEmpty()) return name;
		return "Unnamed";
	}

}
