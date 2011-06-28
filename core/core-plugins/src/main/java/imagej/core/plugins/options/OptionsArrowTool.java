//
// OptionsArrowTool.java
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

package imagej.core.plugins.options;

import imagej.plugin.Menu;
import imagej.plugin.Parameter;
import imagej.plugin.Plugin;
import imagej.util.SettingsKeys;

/**
 * Runs the Edit::Options::Arrow Tool... dialog
 * 
 * @author Barry DeZonia
 */

@Plugin(menu = { @Menu(label = "Edit", mnemonic = 'e'),
	@Menu(label = "Options", mnemonic = 'o'),
	@Menu(label = "Arrow Tool...", weight = 6) })
public class OptionsArrowTool extends OptionsPlugin  {

	@Parameter(label = "Width", min = "1", max = "50",
		persistKey = SettingsKeys.OPTIONS_ARROW_WIDTH)
	private int arrowWidth;

	@Parameter(label = "Size", min = "0", max = "30",
		persistKey = SettingsKeys.OPTIONS_ARROW_SIZE)
	private int arrowSize;

	@Parameter(label = "Color", choices = { "red", "green", "blue", "magenta",
		"cyan", "yellow", "orange", "black", "white" },
		persistKey = SettingsKeys.OPTIONS_ARROW_COLOR)
	private String arrowColor;

	@Parameter(label = "Style", choices = { "Filled", "Notched", "Open",
		"Headless" }, persistKey = SettingsKeys.OPTIONS_ARROW_STYLE)
	private String arrowStyle;

	@Parameter(label = "Double headed",
		persistKey = SettingsKeys.OPTIONS_ARROW_DOUBLEHEADED)
	private boolean arrowDoubleHeaded;

}
