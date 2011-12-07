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

package imagej.options.plugins;

import imagej.ext.plugin.Menu;
import imagej.ext.plugin.Parameter;
import imagej.ext.plugin.Plugin;
import imagej.options.OptionsPlugin;
import imagej.util.ColorRGB;

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

	@Parameter(label = "Foreground")
	private ColorRGB fgColor = new ColorRGB(0,0,0); // black

	@Parameter(label = "Background")
	private ColorRGB bgColor = new ColorRGB(255,255,255); // white

	@Parameter(label = "Selection")
	private ColorRGB selColor = new ColorRGB(255,255,0); // yellow

	// -- OptionsColors methods --

	public OptionsColors() {
		load(); // NB: Load persisted values *after* field initialization.
	}
	
	public ColorRGB getFgColor() {
		return fgColor;
	}

	public ColorRGB getBgColor() {
		return bgColor;
	}

	public ColorRGB getSelColor() {
		return selColor;
	}

	public void setFgColor(final ColorRGB fgColor) {
		this.fgColor = fgColor;
	}

	public void setBgColor(final ColorRGB bgColor) {
		this.bgColor = bgColor;
	}

	public void setSelColor(final ColorRGB selColor) {
		this.selColor = selColor;
	}

}
