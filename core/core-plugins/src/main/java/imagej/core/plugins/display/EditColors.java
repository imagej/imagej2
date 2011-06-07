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
import imagej.display.Display;
import imagej.display.DisplayManager;
import imagej.display.DisplayView;
import imagej.plugin.ImageJPlugin;
import imagej.plugin.Menu;
import imagej.plugin.Parameter;
import imagej.plugin.Plugin;
import imagej.plugin.PreviewPlugin;
import net.imglib2.display.CompositeXYProjector;
import net.imglib2.type.numeric.ARGBType;
import net.imglib2.type.numeric.RealType;

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

	// TODO - Use DisplayView (DatasetView?) parameter instead of getting the
	// active display from the DisplayManager.

	@Parameter(label = "Color mode", persist = false, choices = {
		EditColors.GRAYSCALE, EditColors.COLOR, EditColors.COMPOSITE })
	private String modeString = EditColors.GRAYSCALE;

	private ColorMode colorMode;

	public EditColors() {
		final DatasetView view = getActiveDisplayView();
		if (view != null) setColorMode(view.getColorMode());
		else { // view == null
			if (modeString.equals(COLOR)) setColorMode(ColorMode.COLOR);
			else if (modeString.equals(COMPOSITE)) setColorMode(ColorMode.COMPOSITE);
			else setColorMode(ColorMode.GRAYSCALE);
		}
	}

	@Override
	public void run() {
		final DatasetView view = getActiveDisplayView();
		if (view == null) return;
		final CompositeXYProjector<? extends RealType<?>, ARGBType> proj =
			view.getProjector();
		view.resetColorTables(colorMode == ColorMode.GRAYSCALE);
		proj.setComposite(colorMode == ColorMode.COMPOSITE);
		proj.map();
		view.update();
	}

	@Override
	public void preview() {
		run();
	}

	public ColorMode getColorMode() {
		return colorMode;
	}

	public void setColorMode(final ColorMode colorMode) {
		switch (colorMode) {
			case COLOR:
				modeString = EditColors.COLOR;
				break;
			case COMPOSITE:
				modeString = EditColors.COMPOSITE;
				break;
			case GRAYSCALE:
				modeString = EditColors.GRAYSCALE;
				break;
			default:
				throw new IllegalStateException("unknown display mode " + colorMode);
		}
		this.colorMode = colorMode;
	}

	private DatasetView getActiveDisplayView() {
		final DisplayManager manager = ImageJ.get(DisplayManager.class);
		final Display display = manager.getActiveDisplay();
		if (display == null) {
			return null; // headless UI or no open images
		}
		final DisplayView activeView = display.getActiveView();
		return activeView instanceof DatasetView ? (DatasetView) activeView : null;
	}

}
