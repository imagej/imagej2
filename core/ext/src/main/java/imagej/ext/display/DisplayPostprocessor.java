//
// DisplayPostprocessor.java
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

package imagej.ext.display;

import imagej.ImageJ;
import imagej.ext.module.Module;
import imagej.ext.module.ModuleItem;
import imagej.ext.plugin.Plugin;
import imagej.ext.plugin.process.PostprocessorPlugin;
import imagej.util.Log;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Displays outputs upon completion of a module execution.
 * 
 * @author Curtis Rueden
 * @author Lee Kamentsky
 */
@Plugin(type = PostprocessorPlugin.class)
public class DisplayPostprocessor implements PostprocessorPlugin {

	@Override
	public void process(final Module module) {
		for (final ModuleItem<?> outputItem : module.getInfo().outputs()) {
			final String name = outputItem.getName();
			final String label = outputItem.getLabel();
			final String displayName =
				label == null || label.isEmpty() ? name : label;
			final Object value = outputItem.getValue(module);
			handleOutput(displayName, value);
		}
	}

	/** Displays output objects. */
	public void handleOutput(final String name, final Object output) {
		// TODO - find a general way to decide this flag
		final boolean addToExisting = false;

		final DisplayService displayService = ImageJ.get(DisplayService.class);

		// get list of existing displays containing this output
		final List<Display<?>> existingDisplays =
			displayService.getDisplaysContaining(output);
		final ArrayList<Display<?>> displays = new ArrayList<Display<?>>();
		displays.addAll(existingDisplays);

		if (displays.isEmpty()) {
			// output was not already displayed
			final Display<?> activeDisplay = displayService.getActiveDisplay();

			if (addToExisting && activeDisplay.canDisplay(output)) {
				// add output to existing display if possible
				activeDisplay.display(output);
			}
			else {
				// create a new display for the output
				final Display<?> display = displayService.createDisplay(name, output);
				if (display != null) displays.add(display);
			}
		}

		if (!displays.isEmpty()) {
			// success!
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
				handleOutput(name, item);
			}
			return;
		}

		// no available displays for this type of output
		final String valueClass =
			output == null ? "null" : output.getClass().getName();
		Log.warn("Ignoring unsupported output: " + valueClass);
	}

}
