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

package imagej.core.plugins.overlay;

import imagej.ImageJ;
import imagej.data.Dataset;
import imagej.data.overlay.BinaryMaskOverlay;
import imagej.data.overlay.Overlay;
import imagej.ext.menu.MenuConstants;
import imagej.ext.module.ItemIO;
import imagej.ext.plugin.ImageJPlugin;
import imagej.ext.plugin.Menu;
import imagej.ext.plugin.Parameter;
import imagej.ext.plugin.Plugin;
import imagej.util.ColorRGB;

import java.util.Arrays;

import net.imglib2.Cursor;
import net.imglib2.RandomAccess;
import net.imglib2.img.Img;
import net.imglib2.img.ImgPlus;
import net.imglib2.img.NativeImg;
import net.imglib2.img.array.ArrayImgFactory;
import net.imglib2.img.basictypeaccess.BitAccess;
import net.imglib2.img.transform.ImgTranslationAdapter;
import net.imglib2.roi.BinaryMaskRegionOfInterest;
import net.imglib2.type.logic.BitType;
import net.imglib2.type.numeric.RealType;

/**
 * TODO
 * 
 * @author Lee Kamentsky
 */
@Plugin(menu = {
	@Menu(label = MenuConstants.PROCESS_LABEL,
		weight = MenuConstants.PROCESS_WEIGHT,
		mnemonic = MenuConstants.PROCESS_MNEMONIC),
	@Menu(label = "Binary", mnemonic = 'b'),
	@Menu(label = "Convert to Mask", weight = 1) }, headless = true)
public class ImageToBinaryMask implements ImageJPlugin {

	@Parameter(label = "Threshold", description = "The threshold that "
		+ "separates background (mask) from foreground (region of interest).")
	private double threshold;

	@Parameter(label = "Input image",
		description = "The image to be converted to a binary mask.")
	private Dataset input;

	@Parameter(label = "Output mask",
		description = "The overlay that is the result of the operation",
		type = ItemIO.OUTPUT)
	private Overlay output;

	@Parameter(label = "Overlay color",
		description = "The color used to display the overlay")
	private ColorRGB color = new ColorRGB(255, 0, 0);

	@Parameter(label = "Overlay alpha", description = "The transparency "
		+ "(transparent = 0) or opacity (opaque=255) of the overlay", min = "0",
		max = "255")
	private int alpha = 128;

	@Parameter(persist = false)
	private ImageJ context;

	@Override
	public void run() {
		final ImgPlus<? extends RealType<?>> imgplus = input.getImgPlus();
		final long[] dimensions = new long[imgplus.numDimensions()];
		final long[] position = new long[imgplus.numDimensions()];
		final long[] min = new long[imgplus.numDimensions()];
		final long[] max = new long[imgplus.numDimensions()];
		Arrays.fill(min, Long.MAX_VALUE);
		Arrays.fill(max, Long.MIN_VALUE);
		/*
		 * First pass - find minima and maxima so we can use a shrunken image in some cases.
		 */
		final Cursor<? extends RealType<?>> c = imgplus.localizingCursor();
		while (c.hasNext()) {
			c.next();
			if (c.get().getRealDouble() >= threshold) {
				for (int i = 0; i < imgplus.numDimensions(); i++) {
					final long p = c.getLongPosition(i);
					if (p < min[i]) min[i] = p;
					if (p > max[i]) max[i] = p;
				}
			}
		}
		if (min[0] == Long.MAX_VALUE) {
			throw new IllegalStateException(
				"The threshold value is lower than that of any pixel");
		}
		for (int i = 0; i < imgplus.numDimensions(); i++) {
			dimensions[i] = max[i] - min[i] + 1;
		}
		final NativeImg<BitType, BitAccess> nativeMask =
			new ArrayImgFactory<BitType>().createBitInstance(dimensions, 1);
		final BitType t = new BitType(nativeMask);
		nativeMask.setLinkedType(t);
		final Img<BitType> mask =
			new ImgTranslationAdapter<BitType, NativeImg<BitType, BitAccess>>(
				nativeMask, min);
		final RandomAccess<BitType> raMask = mask.randomAccess();
		c.reset();
		while (c.hasNext()) {
			if (c.next().getRealDouble() >= threshold) {
				c.localize(position);
				raMask.setPosition(position);
				raMask.get().set(true);
			}
		}
		output =
			new BinaryMaskOverlay(context,
				new BinaryMaskRegionOfInterest<BitType, Img<BitType>>(mask));
		output.setAlpha(alpha);
		output.setFillColor(color);
		for (int i = 0; i < imgplus.numDimensions(); i++) {
			output.setAxis(imgplus.axis(i), i);
		}
	}

}
