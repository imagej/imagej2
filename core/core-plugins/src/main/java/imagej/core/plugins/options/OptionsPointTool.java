//
// OptionsPointTool.java
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
 * Runs the Edit::Options::Point Tool... dialog
 * 
 * @author Barry DeZonia
 */
@Plugin(menu = {
	@Menu(label = "Edit", mnemonic = 'e'),
	@Menu(label = "Options", mnemonic = 'o'),
	@Menu(label = "Point Tool...", weight = 7) })
public class OptionsPointTool extends OptionsPlugin {

	@Parameter(label = "Mark Width (pixels)",
		persistKey = SettingsKeys.OPTIONS_POINT_MARK_WIDTH)
	private int markWidth;
	
	@Parameter(label = "Auto-Measure",
		persistKey = SettingsKeys.OPTIONS_POINT_AUTO_MEASURE)
	private boolean autoMeasure;
	
	@Parameter(label = "Auto-Next Slice",
		persistKey = SettingsKeys.OPTIONS_POINT_AUTOSLICE)
	private boolean autoNextSlice;
	
	@Parameter(label = "Add to ROI Manager",
		persistKey = SettingsKeys.OPTIONS_POINT_ADD_ROI)
	private boolean addToRoiMgr;
	
	@Parameter(label = "Label Points",
		persistKey = SettingsKeys.OPTIONS_POINT_LABEL_POINTS)
	private boolean labelPoints;
	
	@Parameter(label = "Selection Color", choices =
	{"red","green","blue","magenta", "cyan", "yellow", "orange", "black", "white"},
		persistKey = SettingsKeys.OPTIONS_POINT_SELECTION_COLOR)
	private String selectionColor;

}
