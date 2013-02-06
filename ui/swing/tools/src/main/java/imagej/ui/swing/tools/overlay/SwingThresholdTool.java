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

package imagej.ui.swing.tools.overlay;

import imagej.data.Dataset;
import imagej.data.display.ImageDisplay;
import imagej.data.display.ImageDisplayService;
import imagej.data.overlay.Overlay;
import imagej.data.overlay.ThresholdOverlay;
import imagej.data.overlay.ThresholdService;
import imagej.plugin.Plugin;
import imagej.ui.swing.overlay.AbstractJHotDrawAdapter;
import imagej.ui.swing.overlay.IJCreationTool;
import imagej.ui.swing.overlay.JHotDrawAdapter;
import imagej.ui.swing.overlay.JHotDrawTool;
import imagej.ui.swing.overlay.SwingThresholdFigure;

import java.awt.Shape;

import org.jhotdraw.draw.Figure;

// TODO - I had code in here that created a default ImgPlus for figure creation
// rather than ever returning nulls. This was cuz with no image open a tool
// selection would cause NPEs. But we make tool INVISIBLE here for now so
// shouldn't happen. And eventually this won't be a tool and we'll never call it
// when no image is open. Be aware of this issue if NPEs crop up.

/**
 * Swing/JHotDraw implementation of threshold tool.
 * 
 * @author Barry DeZonia
 */
@Plugin(type = JHotDrawAdapter.class, name = "Threshold",
	description = "Create threshold overlay", visible = false)
public class SwingThresholdTool extends
	AbstractJHotDrawAdapter<ThresholdOverlay, SwingThresholdFigure>
{
	@Override
	public boolean supports(Overlay overlay, Figure figure) {
		if (!(overlay instanceof ThresholdOverlay)) return false;
		return figure == null || figure instanceof SwingThresholdFigure;
	}

	@Override
	public Overlay createNewOverlay() {
		ImageDisplay display = getDisplayService().getActiveImageDisplay();
		return getThresholdService().getThreshold(display);
	}

	@Override
	public Figure createDefaultFigure() {
		ImageDisplayService dispServ = getDisplayService();
		ImageDisplay display = dispServ.getActiveImageDisplay();
		if (display == null) return null;
		ThresholdOverlay overlay = getThresholdService().getThreshold(display);
		Dataset ds = dispServ.getActiveDataset(display);
		if (ds == null) return null;
		SwingThresholdFigure figure =
			new SwingThresholdFigure(display, ds.getImgPlus(), overlay);
		overlay.setFigure(figure);
		return figure;
	}

	@Override
	public JHotDrawTool getCreationTool(ImageDisplay display) {
		return new IJCreationTool<SwingThresholdFigure>(display, this);
	}

	@Override
	public Shape toShape(SwingThresholdFigure figure) {
		// TODO : do something here? or return null?
		throw new UnsupportedOperationException("Unimplemented");
	}

	private ImageDisplayService getDisplayService() {
		return getContext().getService(ImageDisplayService.class);
	}

	private ThresholdService getThresholdService() {
		return getContext().getService(ThresholdService.class);
	}

}
