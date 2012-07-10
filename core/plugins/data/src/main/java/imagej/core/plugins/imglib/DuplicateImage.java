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

package imagej.core.plugins.imglib;

import imagej.ImageJ;
import imagej.core.plugins.restructure.RestructureUtils;
import imagej.data.Dataset;
import imagej.data.DefaultDataset;
import imagej.data.display.ImageDisplay;
import imagej.data.display.OverlayService;
import imagej.data.overlay.Overlay;
import imagej.ext.display.DisplayService;
import imagej.ext.menu.MenuConstants;
import imagej.ext.module.ItemIO;
import imagej.ext.plugin.ImageJPlugin;
import imagej.ext.plugin.Menu;
import imagej.ext.plugin.Parameter;
import imagej.ext.plugin.Plugin;
import imagej.util.RealRect;

import java.util.ArrayList;
import java.util.List;

import net.imglib2.img.ImgPlus;
import net.imglib2.meta.Axes;
import net.imglib2.meta.AxisType;
import net.imglib2.type.numeric.RealType;

/**
 * Duplicates data from one input display to an output display. If there are
 * overlays in the view then only the selected region is duplicated. Otherwise
 * the entire display is duplicated.
 * 
 * @author Barry DeZonia
 */
@Plugin(menu = {
	@Menu(label = MenuConstants.IMAGE_LABEL, weight = MenuConstants.IMAGE_WEIGHT,
		mnemonic = MenuConstants.IMAGE_MNEMONIC),
	@Menu(label = "Duplicate", accelerator = "shift control D") },
	headless = true)
public class DuplicateImage implements ImageJPlugin {

	// -- Plugin parameters --

	@Parameter
	private ImageJ context;
	
	@Parameter
	private OverlayService overlayService;
	
	@Parameter
	private DisplayService displayService;
	
	@Parameter
	private ImageDisplay display;

	@Parameter
	private Dataset dataset;

	@Parameter(type = ItemIO.OUTPUT)
	private ImageDisplay output;

	// -- DuplicateImage methods --

	public ImageDisplay getOutput() {
		return output;
	}

	// -- RunnablePlugin methods --

	@Override
	public void run() {
		int xAxis = dataset.getAxisIndex(Axes.X);
		int yAxis = dataset.getAxisIndex(Axes.Y);
		if ((xAxis < 0) || (yAxis < 0))
			throw new IllegalArgumentException("X and Y axes required");
		long[] dims = dataset.getDims();
		RealRect bounds = overlayService.getSelectionBounds(display);
		// bounds could be a single point
		if (bounds.width == 0) bounds.width = 1;
		if (bounds.height == 0) bounds.height = 1;
		List<Overlay> overlays = overlayService.getOverlays(display);
		long[] srcOrigin = new long[dims.length];
		srcOrigin[xAxis] = (long) bounds.x;
		srcOrigin[yAxis] = (long) bounds.y;
		long[] srcSpan = dims.clone();
		srcSpan[xAxis] = (long) bounds.width;
		srcSpan[yAxis] = (long) bounds.height;
		long[] dstOrigin = new long[dims.length];
		long[] dstSpan = dims.clone();
		dstSpan[xAxis] = (long)(bounds.width);
		dstSpan[yAxis] = (long)(bounds.height);
		AxisType[] axes = dataset.getAxes();
		ImgPlus<? extends RealType<?>> dstImgPlus =
				RestructureUtils.createNewImgPlus(dataset, dstSpan, axes);
		RestructureUtils.copyHyperVolume(
				dataset.getImgPlus(), srcOrigin, srcSpan,
				dstImgPlus, dstOrigin, dstSpan);
		Dataset newDataset = new DefaultDataset(context, dstImgPlus);
		// CTR FIXME (BDZ - the cast?)
		output = (ImageDisplay)
				displayService.createDisplay(newDataset.getName(), newDataset);
		// create new overlays relative to new origin
		double[] toOrigin = new double[2];
		toOrigin[0] = -srcOrigin[0];
		toOrigin[1] = -srcOrigin[1];
		List<Overlay> newOverlays = new ArrayList<Overlay>();
		for (Overlay overlay : overlays) {
			if (overlayContained(overlay, bounds)) {
				Overlay newOverlay = overlay.duplicate();
				newOverlay.move(toOrigin);
				newOverlays.add(newOverlay);
			}
		}
		overlayService.addOverlays(output, newOverlays);
	}

	private boolean overlayContained(Overlay overlay, RealRect bounds) {
		if (overlay.min(0) < bounds.x) return false;
		if (overlay.min(1) < bounds.y) return false;
		if (overlay.max(0) > bounds.x + bounds.width) return false;
		if (overlay.max(1) > bounds.y + bounds.height) return false;
		return true;
	}
}
