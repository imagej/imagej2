//
// LineColor.java
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

package imagej.core.plugins.roi;

import imagej.ImageJ;
import imagej.data.roi.AbstractLineOverlay;
import imagej.data.roi.Overlay;
import imagej.display.Display;
import imagej.display.DisplayManager;
import imagej.display.OverlayManager;
import imagej.plugin.ImageJPlugin;
import imagej.plugin.Menu;
import imagej.plugin.Parameter;
import imagej.plugin.Plugin;
import imagej.util.ColorRGB;

import java.util.ArrayList;
import java.util.List;

/**
 * A plugin to change the line color of the selected overlays.
 * 
 * @author Curtis Rueden
 */
@Plugin(menu = { @Menu(label = "Image", mnemonic = 'i'),
	@Menu(label = "Overlays", mnemonic = 'o'),
	@Menu(label = "Line color", mnemonic = 'c') })
public class LineColor implements ImageJPlugin {

	@Parameter
	private ColorRGB lineColor;

	public LineColor() {
		// set default value to line color of first selected overlay
		final List<AbstractLineOverlay> selected = getSelectedOverlays();
		if (selected.size() > 0) lineColor = selected.get(0).getLineColor();
	}

	@Override
	public void run() {
		// change line color of all selected overlays
		final List<AbstractLineOverlay> selected = getSelectedOverlays();
		for (final AbstractLineOverlay overlay : selected) {
			overlay.setLineColor(lineColor);
		}
	}

	public ColorRGB getLineColor() {
		return lineColor;
	}

	private List<AbstractLineOverlay> getSelectedOverlays() {
		final ArrayList<AbstractLineOverlay> result =
			new ArrayList<AbstractLineOverlay>();

		final DisplayManager displayManager = ImageJ.get(DisplayManager.class);
		final Display display = displayManager.getActiveDisplay();
		final OverlayManager overlayManager = ImageJ.get(OverlayManager.class);
		final List<Overlay> overlays = overlayManager.getOverlays(display);
		for (final Overlay overlay : overlays) {
			// TODO - ignore overlays that aren't selected
			// if (!overlay.isSelected()) continue;

			// ignore non-line overlays
			if (!(overlay instanceof AbstractLineOverlay)) continue;
			// TODO - Add line color to top-level Overlay interface.
			// It's fine if an overlay subtype doesn't implement it
			// but better not to need instanceof checks. We probably
			// don't need the AbstractLineOverlay class at all, actually.
			final AbstractLineOverlay lineOverlay = (AbstractLineOverlay) overlay;

			result.add(lineOverlay);
		}
		return result;
	}

}
