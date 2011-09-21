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

package imagej.options.plugins;

import imagej.ext.plugin.Menu;
import imagej.ext.plugin.Parameter;
import imagej.ext.plugin.Plugin;
import imagej.options.OptionsPlugin;

/**
 * Runs the Edit::Options::Arrow Tool dialog.
 * 
 * @author Barry DeZonia
 */
@Plugin(type = OptionsPlugin.class, menu = {
	@Menu(label = "Edit", mnemonic = 'e'),
	@Menu(label = "Options", mnemonic = 'o'),
	@Menu(label = "Arrow Tool...", weight = 6) })
public class OptionsArrowTool extends OptionsPlugin {

	@Parameter(label = "Width", min = "1", max = "50")
	private int arrowWidth = 2;

	@Parameter(label = "Size", min = "0", max = "30")
	private int arrowSize = 10;

	// TODO - use ColorRGB for arrowColor

	@Parameter(label = "Color", choices = { "red", "green", "blue", "magenta",
		"cyan", "yellow", "orange", "black", "white" })
	private String arrowColor = "black";

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

	public String getArrowColor() {
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

	public void setArrowColor(final String arrowColor) {
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
