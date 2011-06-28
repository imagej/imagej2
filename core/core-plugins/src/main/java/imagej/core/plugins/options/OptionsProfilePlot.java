//
// OptionsProfilePlot.java
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
 * Runs the Edit::Options::Profile Plot Options... dialog
 * 
 * @author Barry DeZonia
 */
@Plugin(menu = {
	@Menu(label = "Edit", mnemonic = 'e'),
	@Menu(label = "Options", mnemonic = 'o'),
	@Menu(label = "Profile Plot Options...", weight = 4) })
public class OptionsProfilePlot extends OptionsPlugin {

	@Parameter(label = "Width (pixels)",
		persistKey = SettingsKeys.OPTIONS_PROFILEPLOT_WIDTH)
	private int width;
	
	@Parameter(label = "Height (pixels)",
		persistKey = SettingsKeys.OPTIONS_PROFILEPLOT_HEIGHT)
	private int height;
	
	@Parameter(label = "Minimum Y",
		persistKey = SettingsKeys.OPTIONS_PROFILEPLOT_MIN_Y)
	private double minY;
	
	@Parameter(label = "Maximum Y",
		persistKey = SettingsKeys.OPTIONS_PROFILEPLOT_MAX_Y)
	private double maxY;
	
	@Parameter(label = "Fixed y-axis scale",
		persistKey = SettingsKeys.OPTIONS_PROFILEPLOT_FIXED_YSCALE)
	private boolean yFixedScale;

	@Parameter(label = "Do not save x-values",
		persistKey = SettingsKeys.OPTIONS_PROFILEPLOT_DISCARD_X)
	private boolean noSaveXValues;

	@Parameter(label = "Auto-close",
		persistKey = SettingsKeys.OPTIONS_PROFILEPLOT_AUTOCLOSE)
	private boolean autoClose;

	@Parameter(label = "Vertical profile",
		persistKey = SettingsKeys.OPTIONS_PROFILEPLOT_VERTICAL)
	private boolean vertProfile;

	@Parameter(label = "List values",
		persistKey = SettingsKeys.OPTIONS_PROFILEPLOT_LIST_VALUES)
	private boolean listValues;

	@Parameter(label = "Interpolate line profiles",
		persistKey = SettingsKeys.OPTIONS_PROFILEPLOT_INTERPOLATE)
	private boolean interpLineProf;

	@Parameter(label = "Draw grid lines",
		persistKey = SettingsKeys.OPTIONS_PROFILEPLOT_DRAW_GRID)
	private boolean drawGridLines;

}
