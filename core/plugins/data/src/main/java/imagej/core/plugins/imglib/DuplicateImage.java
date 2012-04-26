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

import net.imglib2.img.ImgPlus;
import net.imglib2.meta.Axes;
import net.imglib2.meta.AxisType;
import net.imglib2.type.numeric.RealType;
import imagej.ImageJ;
import imagej.core.plugins.restructure.RestructureUtils;
import imagej.data.Dataset;
import imagej.data.DefaultDataset;
import imagej.data.display.ImageDisplay;
import imagej.data.display.OverlayService;
import imagej.ext.menu.MenuConstants;
import imagej.ext.module.ItemIO;
import imagej.ext.plugin.ImageJPlugin;
import imagej.ext.plugin.Menu;
import imagej.ext.plugin.Parameter;
import imagej.ext.plugin.Plugin;
import imagej.util.RealRect;

/**
 * Fills an output Dataset with the contents of an input Dataset.
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
	private ImageDisplay display;

	@Parameter
	private Dataset input;

	@Parameter(type = ItemIO.OUTPUT)
	private Dataset output;

	// -- DuplicateImage methods --

	public Dataset getOutput() {
		return output;
	}

	// -- RunnablePlugin methods --

	@Override
	public void run() {
		int xAxis = input.getAxisIndex(Axes.X);
		int yAxis = input.getAxisIndex(Axes.Y);
		if ((xAxis < 0) || (yAxis < 0))
			throw new IllegalArgumentException("X and Y axes required");
		long[] dims = input.getDims();
		RealRect bounds = overlayService.getSelectionBounds(display);
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
		AxisType[] axes = input.getAxes();
		ImgPlus<? extends RealType<?>> dstImgPlus =
				RestructureUtils.createNewImgPlus(input, dstSpan, axes);
		RestructureUtils.copyHyperVolume(
				input.getImgPlus(), srcOrigin, srcSpan,
				dstImgPlus, dstOrigin, dstSpan);
		output = new DefaultDataset(context, dstImgPlus);
		// TODO - problems
		// in IJ1 the ROI is also created in the new image. i.e. an ellipse
		//    can be transferred
	}

}
