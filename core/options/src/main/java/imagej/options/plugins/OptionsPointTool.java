/*
 * #%L
 * ImageJ software for multidimensional image processing and analysis.
 * %%
 * Copyright (C) 2009 - 2012 Board of Regents of the University of
 * Wisconsin-Madison, Broad Institute of MIT and Harvard, and Max Planck
 * Institute of Molecular Cell Biology and Genetics.
 * %%
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * 
 * The views and conclusions contained in the software and documentation are
 * those of the authors and should not be interpreted as representing official
 * policies, either expressed or implied, of any organization.
 * #L%
 */

package imagej.options.plugins;

import imagej.ext.menu.MenuConstants;
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
	@Menu(label = MenuConstants.EDIT_LABEL, weight = MenuConstants.EDIT_WEIGHT,
		mnemonic = MenuConstants.EDIT_MNEMONIC),
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
