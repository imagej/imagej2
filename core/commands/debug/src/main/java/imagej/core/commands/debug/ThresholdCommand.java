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

package imagej.core.commands.debug;

import imagej.command.ContextCommand;
import imagej.data.display.ImageDisplay;
import imagej.data.display.OverlayService;
import imagej.data.overlay.Overlay;
import imagej.data.overlay.ThresholdOverlay;
import imagej.plugin.Parameter;
import imagej.plugin.Plugin;

import java.util.List;

import net.imglib2.ops.pointset.PointSet;

/**
 * A thresholding example using {@link PointSet}s from Imglib2 OPS
 * 
 * @author Barry DeZonia
 */
@Plugin(menuPath = "Plugins>Sandbox>Threshold Toy")
public class ThresholdCommand extends ContextCommand {

	// -- Parameters --

	@Parameter
	private OverlayService osrv;
	
	@Parameter
	private ImageDisplay display;
	
	@Parameter(label="Min")
	private Double minThresh;
	
	@Parameter(label="Max")
	private Double maxThresh;

	// -- Command methods --
	
	// TODO - this should really set the default threshold values to be used by
	// any creation of a new threshold overlay. And of course modify the existing
	// thresholds of the curr threshold overlay if there is one. So we need an
	// OptionsThreshold class perhaps. Also this plugin should be interactive
	// updating the display as the threshold values/ are altered in the dialog.
	
	@Override
	public void run() {
		if (minThresh > maxThresh) {
			cancel("Threshold: minimum value cannot be larger than maximum value.");
			return;
		}
		ThresholdOverlay thresholdOverlay = null;
		List<Overlay> overlays = osrv.getOverlays(display);
		for (Overlay o : overlays) {
			if (o instanceof ThresholdOverlay) {
				thresholdOverlay = (ThresholdOverlay) o;
				break;
			}
		}
		if (thresholdOverlay != null) {
			thresholdOverlay.setRange(minThresh, maxThresh);
			thresholdOverlay.update();
		}
	}

}
