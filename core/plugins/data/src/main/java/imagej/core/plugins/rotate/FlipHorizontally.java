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

package imagej.core.plugins.rotate;

import imagej.data.Dataset;
import imagej.data.Extents;
import imagej.data.Position;
import imagej.data.display.ImageDisplay;
import imagej.data.display.ImageDisplayService;
import imagej.data.display.OverlayService;
import imagej.ext.menu.MenuConstants;
import imagej.ext.plugin.ImageJPlugin;
import imagej.ext.plugin.Menu;
import imagej.ext.plugin.Parameter;
import imagej.ext.plugin.Plugin;
import imagej.util.RealRect;
import net.imglib2.RandomAccess;
import net.imglib2.img.ImgPlus;
import net.imglib2.meta.Axes;
import net.imglib2.type.numeric.RealType;

/**
 * Modifies an input Dataset by flipping its pixels horizontally. Flips all
 * image pixels unless a selected region is available from the OverlayService.
 * 
 * @author Barry DeZonia
 */
@Plugin(menu = {
	@Menu(label = MenuConstants.IMAGE_LABEL, weight = MenuConstants.IMAGE_WEIGHT,
		mnemonic = MenuConstants.IMAGE_MNEMONIC),
	@Menu(label = "Transform", mnemonic = 't'),
	@Menu(label = "Flip Horizontally", weight = 1) }, headless = true)
public class FlipHorizontally implements ImageJPlugin {

	// -- instance variables that are Parameters --

	@Parameter(persist = false)
	private ImageDisplayService imageDisplayService;

	@Parameter(persist = false)
	private OverlayService overlayService;

	@Parameter(persist = false)
	private ImageDisplay display;

	// -- public interface --

	@Override
	public void run() {
		final Dataset input = imageDisplayService.getActiveDataset(display);
		final RealRect selection = overlayService.getSelectionBounds(display);
		flipPixels(input, selection);
	}

	// -- private interface --

	private void flipPixels(final Dataset input, final RealRect selection) {

		final long[] dims = input.getDims();
		final int xAxis = input.getAxisIndex(Axes.X);
		final int yAxis = input.getAxisIndex(Axes.Y);
		if ((xAxis < 0) || (yAxis < 0)) throw new IllegalArgumentException(
			"cannot flip image that does not have XY planes");

		long oX = 0;
		long oY = 0;
		long width = dims[xAxis];
		long height = dims[yAxis];

		if ((selection.width >= 1) && (selection.height >= 1)) {
			oX = (long) selection.x;
			oY = (long) selection.y;
			width = (long) selection.width;
			height = (long) selection.height;
		}

		final long[] planeDims = new long[dims.length - 2];
		int d = 0;
		for (int i = 0; i < dims.length; i++) {
			if (i == xAxis) continue;
			if (i == yAxis) continue;
			planeDims[d++] = dims[i];
		}

		final Position planePos = new Extents(planeDims).createPosition();

		if (dims.length == 2) { // a single plane
			flipPlane(input, xAxis, yAxis, new long[] {}, oX, oY, width, height);
		}
		else { // has multiple planes
			final long[] planeIndex = new long[planeDims.length];
			while (planePos.hasNext()) {
				planePos.fwd();
				planePos.localize(planeIndex);
				flipPlane(input, xAxis, yAxis, planeIndex, oX, oY, width, height);
			}
		}
		input.update();
	}

	private void flipPlane(final Dataset input, final int xAxis, final int yAxis,
		final long[] planeIndex, final long oX, final long oY, final long width,
		final long height)
	{
		if (height == 1) return;

		final ImgPlus<? extends RealType<?>> imgPlus = input.getImgPlus();

		final RandomAccess<? extends RealType<?>> acc1 = imgPlus.randomAccess();
		final RandomAccess<? extends RealType<?>> acc2 = imgPlus.randomAccess();

		final long[] pos1 = new long[planeIndex.length + 2];
		final long[] pos2 = new long[planeIndex.length + 2];

		int d = 0;
		for (int i = 0; i < pos1.length; i++) {
			if (i == xAxis) continue;
			if (i == yAxis) continue;
			pos1[i] = planeIndex[d];
			pos2[i] = planeIndex[d];
			d++;
		}

		long col1, col2;

		if ((width & 1) == 0) { // even number of cols
			col2 = width / 2;
			col1 = col2 - 1;
		}
		else { // odd number of cols
			col2 = width / 2 + 1;
			col1 = col2 - 2;
		}

		while (col1 >= 0) {
			pos1[xAxis] = oX + col1;
			pos2[xAxis] = oX + col2;
			for (long y = oY; y < oY + height; y++) {
				pos1[yAxis] = y;
				pos2[yAxis] = y;
				acc1.setPosition(pos1);
				acc2.setPosition(pos2);
				final double value1 = acc1.get().getRealDouble();
				final double value2 = acc2.get().getRealDouble();
				acc1.get().setReal(value2);
				acc2.get().setReal(value1);
			}
			col1--;
			col2++;
		}
	}
}
