/*
 * #%L
 * ImageJ software for multidimensional image processing and analysis.
 * %%
 * Copyright (C) 2009 - 2014 Board of Regents of the University of
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

package imagej.plugins.commands.overlay;

import imagej.command.Command;
import imagej.command.ContextCommand;
import imagej.data.display.ImageDisplay;
import imagej.data.display.OverlayInfoList;
import imagej.data.display.OverlayService;
import imagej.data.overlay.Overlay;
import imagej.data.overlay.ThresholdOverlay;
import imagej.data.threshold.ThresholdService;
import imagej.menu.MenuConstants;

import java.util.List;

import org.scijava.plugin.Menu;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

/**
 * After running this plugin the current display will now reference (and
 * show) the overlays that are currently selected in the Overlay Manager.
 * 
 * @author Barry DeZonia
 */
@Plugin(type = Command.class, menu = {
	@Menu(label = MenuConstants.IMAGE_LABEL, weight = MenuConstants.IMAGE_WEIGHT,
		mnemonic = MenuConstants.IMAGE_MNEMONIC),
	@Menu(label = "Overlay", mnemonic = 'o'),
	@Menu(label = "From Overlay Manager", weight = 1, mnemonic = 'f') },
	headless = true)
public class FromOverlayManager extends ContextCommand {

	// -- Parameters --
	
	@Parameter(required = true)
	private ImageDisplay display;
	
	@Parameter
	private OverlayService ovrSrv;
	
	@Parameter
	private ThresholdService threshSrv;

	// -- Command methods --
	
	@Override
	public void run() {
		OverlayInfoList overlayList = ovrSrv.getOverlayInfo();
		List<Overlay> selectedRoiMgrOverlays = overlayList.selectedOverlays();
		List<Overlay> currOverlays = ovrSrv.getOverlays(display);
		boolean changes = false;
		for (Overlay overlay : selectedRoiMgrOverlays) {
			if (currOverlays.contains(overlay)) continue;
			Overlay ov = overlay;
			boolean hadThresh = false;
			// do not display one ThresholdOverlay in two displays.
			if (overlay instanceof ThresholdOverlay) {
				// instead get thresh overlay for display and set its range
				hadThresh = threshSrv.hasThreshold(display);
				ThresholdOverlay mgrThresh = (ThresholdOverlay) overlay;
				ThresholdOverlay dispThresh = threshSrv.getThreshold(display);
				dispThresh.setRange(mgrThresh.getRangeMin(), mgrThresh.getRangeMax());
				ov = dispThresh;
			}
			changes = true;
			if (!hadThresh) display.display(ov);
		}
		if (changes) display.update();
	}
	
	// -- accessors --
	
	public ImageDisplay getImageDisplay() {
		return display;
	}
	
	public void setImageDisplay(ImageDisplay disp) {
		display = disp;
	}

}
