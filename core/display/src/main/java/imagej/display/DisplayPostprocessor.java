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

package imagej.display;

import imagej.ImageJ;
import imagej.data.Dataset;
import imagej.object.ObjectManager;
import imagej.plugin.Plugin;
import imagej.plugin.PluginEntry;
import imagej.plugin.PluginException;
import imagej.plugin.PluginManager;
import imagej.plugin.PluginModule;
import imagej.plugin.process.PluginPostprocessor;
import imagej.util.Log;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Displays output {@link Dataset}s upon completion of a plugin execution.
 * 
 * @author Curtis Rueden
 */
@Plugin(type = PluginPostprocessor.class)
public class DisplayPostprocessor implements PluginPostprocessor {

	@Override
	public void process(final PluginModule<?> module) {
		final Map<String, Object> outputs = module.getOutputs();
		handleOutput(outputs.values());
	}

	/** Displays output datasets. */
	public void handleOutput(final Object value) {
		if (value instanceof Collection) {
			final Collection<?> collection = (Collection<?>) value;
			for (final Object item : collection)
				handleOutput(item);
		}
		else if (value instanceof Dataset) {
			final Dataset dataset = (Dataset) value;
			if (!isDisplayed(dataset))
				displayDataset(dataset);
		}
		else {
			// ignore non-Dataset output
		}
	}

	// -- Helper methods --

	/** Determines whether the given dataset is currently displayed onscreen. */
	private boolean isDisplayed(final Dataset dataset) {
		// TODO: Keep a reference count instead of manually counting here?
		final ObjectManager objectManager = ImageJ.get(ObjectManager.class);
		final List<Display> displays = objectManager.getObjects(Display.class);
		for (final Display display : displays) {
			for (final DisplayView view : display.getViews()) {
				if (dataset == view.getDataObject()) return true;
			}
		}
		return false;
	}

	private void displayDataset(final Dataset dataset) {
		// get available display plugins from the plugin manager
		final PluginManager pluginManager = ImageJ.get(PluginManager.class);
		final List<PluginEntry<Display>> plugins =
			pluginManager.getPlugins(Display.class);

		for (final PluginEntry<Display> pe : plugins) {
			try {
				final Display displayPlugin = pe.createInstance();
				// display dataset using the first compatible DisplayPlugin
				// TODO: prompt user with dialog box if multiple matches
				if (displayPlugin.canDisplay(dataset)) {
					displayPlugin.display(dataset);
					break;
				}
			}
			catch (final PluginException e) {
				Log.error("Invalid display plugin: " + pe, e);
			}
		}
	}

}
