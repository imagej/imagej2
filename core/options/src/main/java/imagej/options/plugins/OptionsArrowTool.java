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
import imagej.util.ColorRGB;
import imagej.util.Colors;

/**
 * Runs the Edit::Options::Arrow Tool dialog.
 * 
 * @author Barry DeZonia
 */
@Plugin(type = OptionsPlugin.class, menu = {
	@Menu(label = MenuConstants.EDIT_LABEL, weight = MenuConstants.EDIT_WEIGHT,
		mnemonic = MenuConstants.EDIT_MNEMONIC),
	@Menu(label = "Options", mnemonic = 'o'),
	@Menu(label = "Arrow Tool...", weight = 6) })
public class OptionsArrowTool extends OptionsPlugin {

	@Parameter(label = "Width", min = "1", max = "50")
	private int arrowWidth = 2;

	@Parameter(label = "Size", min = "0", max = "30")
	private int arrowSize = 10;

	@Parameter(label = "Color")
	private ColorRGB arrowColor = Colors.BLACK;

	// TODO - use enum for arrowStyle

	@Parameter(label = "Style", choices = { "Filled", "Notched", "Open",
		"Headless" })
	private String arrowStyle = "Filled";

	@Parameter(label = "Outline")
	private boolean arrowOutline = false;

	@Parameter(label = "Double headed")
	private boolean arrowDoubleHeaded = false;

	// -- OptionsArrowTool methods --

	public OptionsArrowTool() {
		load(); // NB: Load persisted values *after* field initialization.
	}

	public int getArrowWidth() {
		return arrowWidth;
	}

	public int getArrowSize() {
		return arrowSize;
	}

	public ColorRGB getArrowColor() {
		return arrowColor;
	}

	public String getArrowStyle() {
		return arrowStyle;
	}

	public boolean isArrowOutline() {
		return arrowOutline;
	}

	public boolean isArrowDoubleHeaded() {
		return arrowDoubleHeaded;
	}

	public void setArrowWidth(final int arrowWidth) {
		this.arrowWidth = arrowWidth;
	}

	public void setArrowSize(final int arrowSize) {
		this.arrowSize = arrowSize;
	}

	public void setArrowColor(final ColorRGB arrowColor) {
		this.arrowColor = arrowColor;
	}

	public void setArrowStyle(final String arrowStyle) {
		this.arrowStyle = arrowStyle;
	}

	public void setArrowOutline(final boolean arrowOutline) {
		this.arrowOutline = arrowOutline;
	}

	public void setArrowDoubleHeaded(final boolean arrowDoubleHeaded) {
		this.arrowDoubleHeaded = arrowDoubleHeaded;
	}

}
