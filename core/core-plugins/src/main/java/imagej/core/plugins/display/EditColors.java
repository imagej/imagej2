//
// EditColors.java
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

package imagej.core.plugins.display;

import imagej.ImageJ;
import imagej.display.ColorMode;
import imagej.display.DatasetView;
import imagej.display.DisplayService;
import imagej.ext.plugin.ImageJPlugin;
import imagej.ext.plugin.Menu;
import imagej.ext.plugin.Parameter;
import imagej.ext.plugin.Plugin;
import imagej.ext.plugin.PreviewPlugin;

/**
 * Plugin that allows toggling between different color modes.
 * 
 * @author Curtis Rueden
 */
@Plugin(menu = { @Menu(label = "Image"), @Menu(label = "Color"),
	@Menu(label = "Edit Colors", weight = -5) },
	iconPath = "/icons/plugins/color_wheel.png")
public class EditColors implements ImageJPlugin, PreviewPlugin {

	public static final String GRAYSCALE = "Grayscale";
	public static final String COLOR = "Color";
	public static final String COMPOSITE = "Composite";

	@Parameter
	private DatasetView view = ImageJ.get(DisplayService.class)
		.getActiveDatasetView();

	// TODO: Add support for enums to plugin framework?

	@Parameter(label = "Color mode", persist = false, choices = {
		GRAYSCALE, COLOR, COMPOSITE })
	private String modeString = GRAYSCALE;

	public EditColors() {
		if (view != null) setColorMode(view.getColorMode());
	}

	@Override
	public void run() {
		if (view == null) return;
		view.setColorMode(getColorMode());
		view.update();
	}

	@Override
	public void preview() {
		run();
	}

	public DatasetView getView() {
		return view;
	}

	public void setView(final DatasetView view) {
		this.view = view;
	}

	public ColorMode getColorMode() {
		return ColorMode.get(modeString);
	}

	public void setColorMode(final ColorMode colorMode) {
		modeString = colorMode.getLabel();
	}

}
