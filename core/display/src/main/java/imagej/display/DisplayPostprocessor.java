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

import imagej.data.Dataset;
import imagej.manager.Managers;
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
 * TODO
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
			final int numUpdated = updateDisplays(dataset);
			if (numUpdated == 0) displayDataset(dataset);
		}
		else {
			// ignore non-Dataset output
		}
	}

	// -- Helper methods --

	/** Updates displays that are currently rendering the dataset. */
	private int updateDisplays(final Dataset dataset) {
		// CTR FIXME - Instead of this postprocessor updating the displays,
		// it would make much more sense for the datasets to publish
		// DatasetChangedEvents and for the displays to subscribe to them,
		// then update themselves.

		final ObjectManager objectManager = Managers.get(ObjectManager.class);
		final List<Display> displays = objectManager.getObjects(Display.class);
		Log.debug("Checking " + displays.size() + " existing displays...");//TEMP

		int numUpdated = 0;
		for (final Display display : displays) {
			Log.debug("Checking display: " + display);//TEMP
			if (dataset == display.getDataset()) {
				display.update();
				numUpdated++;
			}
		}
		return numUpdated;
	}

	private void displayDataset(final Dataset dataset) {
		// get available display plugins from the plugin manager
		final PluginManager pluginManager = Managers.get(PluginManager.class);
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
