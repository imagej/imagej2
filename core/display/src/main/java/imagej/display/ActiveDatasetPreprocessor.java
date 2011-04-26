//
// ActiveDatasetPreprocessor.java
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
import imagej.module.ModuleItem;
import imagej.plugin.Plugin;
import imagej.plugin.PluginModule;
import imagej.plugin.PluginModuleItem;
import imagej.plugin.process.PluginPreprocessor;

/**
 * Assigns the active {@link Dataset} when there is one single unresolved
 * {@link Dataset} parameter. Hence, rather than a dialog prompting the user to
 * choose a {@link Dataset}, the active {@link Dataset} is used automatically.
 * In the case of more than one {@link Dataset} parameter, the active
 * {@link Dataset} is not used and instead the user must select. This behavior
 * is consistent with ImageJ v1.x.
 * 
 * @author Curtis Rueden
 */
@Plugin(type = PluginPreprocessor.class, priority = 0)
public class ActiveDatasetPreprocessor implements PluginPreprocessor {

	// -- PluginPreprocessor methods --

	@Override
	public boolean canceled() {
		return false;
	}

	// -- PluginProcessor methods --

	@Override
	public void process(final PluginModule<?> module) {
		final Iterable<ModuleItem> inputs = module.getInfo().inputs();

		final PluginModuleItem item = getSingleDatasetInput(inputs);
		if (item == null) return; // no single Dataset input to assign

		final DisplayManager displayManager = ImageJ.get(DisplayManager.class);
		final Display activeDisplay = displayManager.getActiveDisplay();
		if (activeDisplay == null) return; // no active display
		final Dataset value = activeDisplay.getDataset();
		if (value == null) return; // no associated dataset
		module.setInput(item.getName(), value);
		item.setResolved(true);
	}

	// -- Helper methods --

	private PluginModuleItem getSingleDatasetInput(
		final Iterable<ModuleItem> inputs)
	{
		PluginModuleItem result = null;
		for (final ModuleItem item : inputs) {
			final PluginModuleItem pmi = (PluginModuleItem) item;
			final boolean resolved = pmi.isResolved();
			if (resolved) continue; // skip resolved inputs
			if (!Dataset.class.isAssignableFrom(item.getType())) continue;
			if (result != null) return null; // there are multiple Dataset inputs
			result = pmi;
		}
		return result;
	}

}
