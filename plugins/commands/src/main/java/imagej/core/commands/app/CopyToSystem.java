/*
 * #%L
 * ImageJ software for multidimensional image processing and analysis.
 * %%
 * Copyright (C) 2009 - 2013 Board of Regents of the University of
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

package imagej.core.commands.app;

import imagej.command.Command;
import imagej.command.ContextCommand;
import imagej.data.display.ImageDisplay;
import imagej.data.display.ImageDisplayService;
import imagej.data.display.OverlayService;
import imagej.data.overlay.Overlay;
import imagej.data.view.DatasetView;
import imagej.menu.MenuConstants;
import imagej.ui.UserInterface;
import imagej.util.ARGBPlane;

import org.scijava.app.StatusService;
import org.scijava.plugin.Menu;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

/**
 * Copies an ARGB image plane to the system clipboard for use by
 * external programs. Plane can be whole plane or currently
 * selected rectangular region.
 * 
 * @author Barry DeZonia
 */
@Plugin(type = Command.class, menu = {
		@Menu(label = MenuConstants.EDIT_LABEL, weight = MenuConstants.EDIT_WEIGHT,
			mnemonic = MenuConstants.EDIT_MNEMONIC),
		@Menu(label = "Copy To System", weight = 12) })
public class CopyToSystem extends ContextCommand {

	@Parameter
	private UserInterface ui;

	@Parameter
	private OverlayService overlayService;
	
	@Parameter
	private ImageDisplayService imageDisplayService;
	
	@Parameter
	private StatusService statusService;
	
	@Parameter
	private ImageDisplay display;
	
	@Override
	public void run() {
		final ARGBPlane pixels = getARGBPixels();
		if (pixels == null) return;
		ui.getSystemClipboard().pixelsToSystemClipboard(pixels);
		final String notice =
				pixels.getWidth() + "x" + pixels.getHeight() +
				" image copied to system clipboard";
		statusService.showStatus(notice);
	}

	public void setDisplay(ImageDisplay display) {
		this.display = display;
	}
	
	private ARGBPlane getARGBPixels() {
		final DatasetView view =
				imageDisplayService.getActiveDatasetView(display);
		if (view == null) return null;
		final Overlay overlay = overlayService.getActiveOverlay(display);
		final long[] dims = display.getDims();
		final int imageWidth = (int) dims[0];
		final int imageHeight = (int) dims[1];
		final int[] argbPixels = view.getScreenImage().getData();
		final int x, y, w, h;
		if (overlay == null) { // no active overlay
			x = 0;
			y = 0;
			w = imageWidth;
			h = imageHeight;
		}
		else { // an active overlay exists
			int ovrMinX = (int) overlay.realMin(0);
			int ovrMinY = (int) overlay.realMin(1);
			int ovrMaxX = (int) (overlay.realMax(0));
			int ovrMaxY = (int) (overlay.realMax(1));
			// overlay bounds can be outside image bounds
			x = Math.max(0, ovrMinX);
			y = Math.max(0, ovrMinY);
			w = Math.min(imageWidth, ovrMaxX) - x + 1;
			h = Math.min(imageHeight, ovrMaxY) - y + 1;
		}
		final ARGBPlane plane = new ARGBPlane(w, h);
		for (int u = 0; u < w; u++) {
			for (int v = 0; v < h; v++) {
				final int argbLoc = (y+v)*imageWidth + (x+u);
				final int argb = argbPixels[argbLoc];
				plane.setARGB(u, v, argb);
			}
		}
		return plane;
	}
}
