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

import imagej.Cancelable;
import imagej.data.Dataset;
import imagej.data.Extents;
import imagej.data.Position;
import imagej.data.display.ImageDisplay;
import imagej.data.display.ImageDisplayService;
import imagej.data.display.OverlayService;
import imagej.ext.plugin.RunnablePlugin;
import imagej.ext.plugin.Menu;
import imagej.ext.plugin.Parameter;
import imagej.ext.plugin.Plugin;
import imagej.menu.MenuConstants;
import imagej.module.ItemIO;
import imagej.util.RealRect;

import java.util.Random;

import net.imglib2.RandomAccess;
import net.imglib2.img.Img;
import net.imglib2.type.numeric.RealType;

/**
 * Adds salt and pepper noise to an image. Image must be an integral type.
 * Assigns random pixels to max or min pixel values. These assignments are
 * evenly balanced and total 5% of the image.
 * 
 * @author Barry DeZonia
 */
@Plugin(menu = {
	@Menu(label = MenuConstants.PROCESS_LABEL,
		weight = MenuConstants.PROCESS_WEIGHT,
		mnemonic = MenuConstants.PROCESS_MNEMONIC),
	@Menu(label = "Noise", mnemonic = 'n'),
	@Menu(label = "Salt and Pepper", weight = 3) }, headless = true)
public class SaltAndPepper implements RunnablePlugin, Cancelable {

	// -- instance variables that are Parameters --

	@Parameter
	private ImageDisplayService imageDisplayService;

	@Parameter
	private OverlayService overlayService;

	@Parameter(type = ItemIO.BOTH)
	private ImageDisplay display;
	
	@Parameter(label="Salt Value")
	private double saltValue = 255;

	@Parameter(label="Pepper Value")
	private double pepperValue = 0;

	// -- other instance variables --

	private Dataset input;
	private RealRect selection;
	private Img<? extends RealType<?>> inputImage;
	private RandomAccess<? extends RealType<?>> accessor;
	private long[] position;
	private String err;

	// -- public interface --

	@Override
	public void run() {
		if (!inputOkay()) {
			err = errorString();
			return;
		}
		setupWorkingData();
		assignPixels();
		cleanup();
		input.update();
	}

	@Override
	public boolean isCanceled() {
		return err != null;
	}

	@Override
	public String getCancelReason() {
		return err;
	}

	// -- private interface --

	private boolean inputOkay() {
		input = imageDisplayService.getActiveDataset(display);
		if (input == null) return false;
		if (input.getImgPlus() == null) return false;
		return input.isInteger() && !input.isRGBMerged();
	}

	private void setupWorkingData() {
		selection = overlayService.getSelectionBounds(display);
		inputImage = input.getImgPlus();
		position = new long[inputImage.numDimensions()];
		accessor = inputImage.randomAccess();
	}

	private void assignPixels() {
		final Random rng = new Random();

		rng.setSeed(System.currentTimeMillis());

		final long[] planeDims = new long[inputImage.numDimensions() - 2];
		for (int i = 0; i < planeDims.length; i++)
			planeDims[i] = inputImage.dimension(i + 2);
		final Extents extents = new Extents(planeDims);
		final Position planePos = extents.createPosition();
		if (planeDims.length == 0) { // 2d only
			assignPlanePixels(planePos, rng);
		}
		else { // 3 or more dimsensions
			while (planePos.hasNext()) {
				planePos.fwd();
				assignPlanePixels(planePos, rng);
			}
		}
	}

	private void cleanup() {
		// nothing to do
	}

	private void assignPlanePixels(final Position planePos, final Random rng) {
		// set plane coordinate values once
		for (int i = 2; i < position.length; i++)
			position[i] = planePos.getLongPosition(i - 2);

		final long ou = (long) selection.x;
		final long ov = (long) selection.y;
		long w = (long) selection.width;
		long h = (long) selection.height;

		if (w <= 0) w = inputImage.dimension(0);
		if (h <= 0) h = inputImage.dimension(1);

		final double percentToChange = 0.05;
		final long numPixels = (long) (percentToChange * w * h);

		for (long p = 0; p < numPixels / 2; p++) {
			long randomU, randomV;

			randomU = ou + nextLong(rng, w);
			randomV = ov + nextLong(rng, h);
			setPixel(randomU, randomV, saltValue);

			randomU = ou + nextLong(rng, w);
			randomV = ov + nextLong(rng, h);
			setPixel(randomU, randomV, pepperValue);
		}
	}

	private long nextLong(final Random rng, final long bound) {
		final double val = rng.nextDouble();
		return (long) (val * bound);
	}

	/**
	 * Sets a value at a specific (u,v) location in the image to a given value
	 */
	private void setPixel(final long u, final long v, final double value) {
		position[0] = u;
		position[1] = v;
		accessor.setPosition(position);
		accessor.get().setReal(value);
	}

	private String errorString() {
		return
			"This plugin does not work with merged color nor floating point datasets";
	}
}
