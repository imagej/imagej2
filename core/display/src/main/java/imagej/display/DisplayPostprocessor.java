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
import imagej.data.DataObject;
import imagej.data.Dataset;
import imagej.data.roi.Overlay;
import imagej.ext.module.Module;
import imagej.ext.plugin.Plugin;
import imagej.ext.plugin.process.PostprocessorPlugin;
import imagej.object.ObjectService;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Displays output {@link Dataset}s upon completion of a module execution.
 * 
 * @author Curtis Rueden
 * @author Lee Kamentsky
 */
@Plugin(type = PostprocessorPlugin.class)
public class DisplayPostprocessor implements PostprocessorPlugin {

	@Override
	public void process(final Module module) {
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
			if (!isDisplayed(dataset)) {
				final DisplayService displayService = ImageJ.get(DisplayService.class);
				displayService.createDisplay(dataset);
			}
		}
		else if (value instanceof Overlay) {
			final Overlay overlay = (Overlay) value;
			if (!isDisplayed(overlay)) {
				displayOverlay(overlay);
			}
		}
		else {
			// ignore unsupported output type
		}
	}

	// -- Helper methods --

	/** Determines whether the given data object is currently displayed onscreen. */
	private boolean isDisplayed(final DataObject dataset) {
		// TODO: Keep a reference count instead of manually counting here?
		final ObjectService objectService = ImageJ.get(ObjectService.class);
		final List<Display> displays = objectService.getObjects(Display.class);
		for (final Display display : displays) {
			for (final DisplayView view : display.getViews()) {
				if (dataset == view.getDataObject()) return true;
			}
		}
		return false;
	}

	private void displayOverlay(final Overlay overlay) {
		// Add the overlay to the currently active display
		final DisplayService displayService = ImageJ.get(DisplayService.class);
		displayService.getActiveDisplay().display(overlay);
	}

}
