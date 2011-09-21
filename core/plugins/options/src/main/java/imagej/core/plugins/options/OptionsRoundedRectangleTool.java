//
// OptionsRoundedRectangleTool.java
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

import imagej.ext.options.OptionsPlugin;
import imagej.ext.plugin.Menu;
import imagej.ext.plugin.Parameter;
import imagej.ext.plugin.Plugin;

/**
 * Runs the Edit::Options::Rounded Rectangle Tool dialog.
 * 
 * @author Barry DeZonia
 */
@Plugin(type = OptionsPlugin.class, menu = {
	@Menu(label = "Edit", mnemonic = 'e'),
	@Menu(label = "Options", mnemonic = 'o'),
	@Menu(label = "Rounded Rect Tool...", weight = 5) })
public class OptionsRoundedRectangleTool extends OptionsPlugin {

	@Parameter(label = "Stroke Width", min = "1", max = "25")
	private int strokeWidth = 2;

	@Parameter(label = "Corner Diameter", min = "0", max = "200")
	private int cornerDiameter = 20;

	// TODO - use ColorRGB for strokeColor

	@Parameter(label = "Stroke Color", choices = { "black", "white", "red",
		"green", "blue", "cyan", "magenta", "yellow", "gray" })
	private String strokeColor = "black";

	// TODO - use ColorRGB for fillColor

	@Parameter(label = "Fill Color", choices = { "none", "black", "white", "red",
		"green", "blue", "cyan", "magenta", "yellow", "gray" })
	private String fillColor = "none";

	// -- OptionsRoundedRectangle methods --

	public OptionsRoundedRectangleTool() {
		load(); // NB: Load persisted values *after* field initialization.
	}
	
	public int getStrokeWidth() {
		return strokeWidth;
	}

	public int getCornerDiameter() {
		return cornerDiameter;
	}

	public String getStrokeColor() {
		return strokeColor;
	}

	public String getFillColor() {
		return fillColor;
	}

	public void setStrokeWidth(final int strokeWidth) {
		this.strokeWidth = strokeWidth;
	}

	public void setCornerDiameter(final int cornerDiameter) {
		this.cornerDiameter = cornerDiameter;
	}

	public void setStrokeColor(final String strokeColor) {
		this.strokeColor = strokeColor;
	}

	public void setFillColor(final String fillColor) {
		this.fillColor = fillColor;
	}
}
