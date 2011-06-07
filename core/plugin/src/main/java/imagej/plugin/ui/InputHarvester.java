//
// InputHarvester.java
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

package imagej.plugin.ui;

import imagej.plugin.Plugin;
import imagej.plugin.PluginException;
import imagej.plugin.PluginModule;
import imagej.plugin.process.PluginPreprocessor;

// TODO - Use Module instead of PluginModule<?>, and move to imagej.module.ui.

/**
 * An input harvester is responsible for creating an {@link InputPanel}
 * containing widgets representing the inputs of a {@link PluginModule},
 * displaying the panel, harvesting the final widget values, and updating the
 * {@link PluginModule}'s input values to match.
 * <p>
 * Typically the input harvester will be a {@link PluginPreprocessor} that
 * performs these steps just prior to {@link Plugin} execution.
 * </p>
 * 
 * @author Curtis Rueden
 */
public interface InputHarvester {

	/**
	 * Constructs an empty {@link InputPanel}. Widgets are added later using the
	 * {@link #buildPanel} method.
	 */
	InputPanel createInputPanel();

	/**
	 * Populates the given {@link InputPanel} with widgets corresponding to the
	 * given {@link PluginModule} instance.
	 * 
	 * @param inputPanel The panel to populate.
	 * @param module The module whose inputs should be translated into widgets.
	 * @throws PluginException if the panel cannot be populated for some reason.
	 *           This may occur due to an input of unsupported type.
	 */
	void buildPanel(InputPanel inputPanel, PluginModule<?> module)
		throws PluginException;

	/**
	 * Gathers input values from the user or other source. For example, a
	 * graphical user interface could present a dialog box.
	 */
	boolean harvestInputs(InputPanel inputPanel, PluginModule<?> module);

	/** Does any needed processing, after input values have been harvested. */
	void processResults(InputPanel inputPanel, PluginModule<?> module);

}
