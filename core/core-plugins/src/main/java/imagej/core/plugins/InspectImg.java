/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package imagej.core.plugins;

import imagej.ImageJ;
import imagej.data.roi.Overlay;
import imagej.display.AbstractDatasetView;
import imagej.display.Display;
import imagej.display.DisplayManager;
import imagej.display.OverlayManager;
import imagej.plugin.ImageJPlugin;
import imagej.plugin.Plugin;
import imagej.util.diag.Diagnostics;
import imagej.util.diag.inspect.Inspector;
import java.util.List;

/**
 *
 * @author GBH
 */
@Plugin(menuPath = "Image>InspectImg")
public class InspectImg implements ImageJPlugin {

	@Override
	public void run() {
				final DisplayManager manager = ImageJ.get(DisplayManager.class);
		final Display display = manager.getActiveDisplay();
		if (display == null) {
			return; // headless UI or no open images
		}
		AbstractDatasetView dsView = (AbstractDatasetView) display.getActiveView();
		Inspector.inspect(dsView);		
	}
	
}
