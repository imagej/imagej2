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

package imagej.core.plugins.display;

import imagej.data.Dataset;
import imagej.data.display.ImageDisplay;
import imagej.ext.menu.MenuConstants;
import imagej.ext.module.ItemIO;
import imagej.ext.plugin.ImageJPlugin;
import imagej.ext.plugin.Menu;
import imagej.ext.plugin.Parameter;
import imagej.ext.plugin.Plugin;
import imagej.ui.UIService;


// TODO - write code that captures part of the screen as a merged color Dataset.
// Then use it for Capture Screen, Capture Image, and Flatten. Can get rid of
// the capture logic in the ImageDisplayViewer hierarchy. And can get rid of
// ImageGrabber maybe. Would also fix issue where current Flatten code draws
// JHotDraw ellipses less well than how they appear in the canvas.

/**
 * Captures the current view of an {@link ImageDisplay} to a color merged
 * {@link Dataset}. Includes overlay graphics. Unlike the FLatten command this
 * plugin includes window frame graphics in the captured image.
 * 
 * @author Barry DeZonia
 *
 */
@Plugin(menu = {
	@Menu(label = MenuConstants.PLUGINS_LABEL, weight = MenuConstants.PLUGINS_WEIGHT,
		mnemonic = MenuConstants.PLUGINS_MNEMONIC),
	@Menu(label = "Utilities"),
	@Menu(label = "Capture Image", weight = 20)})
public class CaptureImage implements ImageJPlugin {

	// -- Parameters --
	
	@Parameter(required=true)
	private UIService uiService;

	@Parameter(required=true)
	private ImageDisplay display;
	
	@Parameter(type=ItemIO.OUTPUT)
	private Dataset dataset;

	// -- accessors --
	
	public void setUIService(UIService srv) {
		uiService = srv;
	}
	
	public UIService getUIService() {
		return uiService;
	}
	
	public void setImageDisplay(ImageDisplay disp) {
		display = disp;
	}

	public ImageDisplay getImageDisplay() {
		return display;
	}
	
	// -- run() method --
	
	@Override
	public void run() {
		// TODO
		// find the screen coordinates of the window frame.
		// then capture the rectangle as an RGB and convert to Dataset
		uiService.showDialog("This command is not yet implemented.");
	}

}
