//
// OptionsColors.java
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
 * Runs the Edit::Options::Colors dialog.
 * 
 * @author Barry DeZonia
 */
@Plugin(type = OptionsPlugin.class, menu = {
	@Menu(label = "Edit", mnemonic = 'e'),
	@Menu(label = "Options", mnemonic = 'o'),
	@Menu(label = "Colors...", weight = 9) })
public class OptionsColors extends OptionsPlugin {

	// TODO - use ColorRGB for fgColor

	@Parameter(label = "Foreground", choices = { "red", "green", "blue",
		"magenta", "cyan", "yellow", "orange", "black", "white" })
	private String fgColor = "black";

	// TODO - use ColorRGB for bgColor

	@Parameter(label = "Background", choices = { "red", "green", "blue",
		"magenta", "cyan", "yellow", "orange", "black", "white" })
	private String bgColor = "white";

	// TODO - use ColorRGB for selColor

	@Parameter(label = "Selection", choices = { "red", "green", "blue",
		"magenta", "cyan", "yellow", "orange", "black", "white" })
	private String selColor = "yellow";

	// -- OptionsColors methods --

	public String getFgColor() {
		return fgColor;
	}

	public String getBgColor() {
		return bgColor;
	}

	public String getSelColor() {
		return selColor;
	}

	public void setFgColor(final String fgColor) {
		this.fgColor = fgColor;
	}

	public void setBgColor(final String bgColor) {
		this.bgColor = bgColor;
	}

	public void setSelColor(final String selColor) {
		this.selColor = selColor;
	}

}
