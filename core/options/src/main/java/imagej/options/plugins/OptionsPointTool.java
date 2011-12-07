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

package imagej.options.plugins;

import imagej.ext.plugin.Menu;
import imagej.ext.plugin.Parameter;
import imagej.ext.plugin.Plugin;
import imagej.options.OptionsPlugin;

/**
 * Runs the Edit::Options::Point Tool dialog.
 * 
 * @author Barry DeZonia
 */
@Plugin(type = OptionsPlugin.class, menu = {
	@Menu(label = "Edit", mnemonic = 'e'),
	@Menu(label = "Options", mnemonic = 'o'),
	@Menu(label = "Point Tool...", weight = 7) })
public class OptionsPointTool extends OptionsPlugin {

	@Parameter(label = "Mark Width (pixels)")
	private int markWidth = 0;

	@Parameter(label = "Auto-Measure")
	private boolean autoMeasure = false;

	@Parameter(label = "Auto-Next Slice")
	private boolean autoNextSlice = false;

	@Parameter(label = "Add to ROI Manager")
	private boolean addToRoiMgr = false;

	@Parameter(label = "Label Points")
	private boolean labelPoints = true;

	// -- OptionsPointTool methods --

	public OptionsPointTool() {
		load(); // NB: Load persisted values *after* field initialization.
	}
	
	public int getMarkWidth() {
		return markWidth;
	}

	public boolean isAutoMeasure() {
		return autoMeasure;
	}

	public boolean isAutoNextSlice() {
		return autoNextSlice;
	}

	public boolean isAddToRoiMgr() {
		return addToRoiMgr;
	}

	public boolean isLabelPoints() {
		return labelPoints;
	}

	public void setMarkWidth(final int markWidth) {
		this.markWidth = markWidth;
	}

	public void setAutoMeasure(final boolean autoMeasure) {
		this.autoMeasure = autoMeasure;
	}

	public void setAutoNextSlice(final boolean autoNextSlice) {
		this.autoNextSlice = autoNextSlice;
	}

	public void setAddToRoiMgr(final boolean addToRoiMgr) {
		this.addToRoiMgr = addToRoiMgr;
	}

	public void setLabelPoints(final boolean labelPoints) {
		this.labelPoints = labelPoints;
	}

}
