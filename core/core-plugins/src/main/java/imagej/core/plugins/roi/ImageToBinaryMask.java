//
// ImageToBinaryMask.java
//

/*
ImageJ software for multidimensional image processing and analysis.

Copyright (c) 2010, ImageJDev.org.
All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met:
    * Redistributions of source code must retain the above copyright
      notice, this list of conditions and the following disclaimer.
    * Redistributions in binary form must reproduce the above copyright
      notice, this list of conditions and the following disclaimer in the
      documentation and/or other materials provided with the distribution.
    * Neither the names of the ImageJDev.org developers nor the
      names of its contributors may be used to endorse or promote products
      derived from this software without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
POSSIBILITY OF SUCH DAMAGE.
*/

package imagej.core.plugins.roi;

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
import imagej.data.Dataset;
import imagej.data.roi.BinaryMaskOverlay;
import imagej.data.roi.Overlay;
import imagej.plugin.ImageJPlugin;
import imagej.plugin.Menu;
import imagej.plugin.Parameter;
import imagej.plugin.Plugin;
import imagej.util.ColorRGB;

/**
 * TODO
 * 
 * @author Lee Kamentsky
 */
@Plugin(menu = { @Menu(label = "Process", mnemonic = 'p'),
	@Menu(label = "Binary", mnemonic = 'b'),
	@Menu(label = "Convert to Mask [IJ2]", weight = 1)})
public class ImageToBinaryMask implements ImageJPlugin {
	@Parameter(
			label="Threshold",
			description = "The threshold that separates background (mask) from foreground (region of interest).")
	double threshold;
	@Parameter(
			label = "Input image",
			description = "The image to be converted to a binary mask.")
	private Dataset input;
	
	@Parameter(
			label = "Output mask",
			description = "The overlay that is the result of the operation",
			output = true)
	private Overlay output;
	@Parameter(label = "Overlay color",
			description = "The color used to display the overlay")
	private ColorRGB color = new ColorRGB(255,0,0);

	@Parameter(label = "Overlay alpha", description = "The transparency (transparent = 0) or opacity (opaque=255) of the overlay",min="0", max="255")
	private int alpha = 128;
	/* (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run() {
		ImgPlus<? extends RealType<?>> imgplus = input.getImgPlus();
		Img<? extends RealType<?>> img = imgplus.getImg();
		long [] dimensions = new long[img.numDimensions()];
		long [] position = new long[img.numDimensions()];
		long [] min = new long[img.numDimensions()];
		long [] max = new long[img.numDimensions()];
		Arrays.fill(min, Long.MAX_VALUE);
		Arrays.fill(max, Long.MIN_VALUE);
		/*
		 * First pass - find minima and maxima so we can use a shrunken image in some cases.
		 */
		Cursor<? extends RealType<?>> c = img.localizingCursor();
		while(c.hasNext()) {
			c.next();
			if (c.get().getRealDouble() >= threshold) {
				for (int i=0; i<img.numDimensions(); i++) {
					long p = c.getLongPosition(i);
					if (p < min[i]) min[i] = p;
					if (p > max[i]) max[i] = p;
				}
			}
		}
		if (min[0] == Long.MAX_VALUE) {
			throw new IllegalStateException("The threshold value is lower than that of any pixel");
		}
		for (int i=0; i< img.numDimensions(); i++) {
			dimensions[i] = max[i] - min[i] + 1;
		}
		NativeImg<BitType,BitAccess> nativeMask = new ArrayImgFactory<BitType>().createBitInstance(dimensions, 1);
		BitType t = new BitType(nativeMask);
		nativeMask.setLinkedType(t);
		Img<BitType> mask = new ImgTranslationAdapter<BitType, NativeImg<BitType, BitAccess>>(nativeMask, min);
		RandomAccess<BitType> raMask = mask.randomAccess();
		c.reset();
		while (c.hasNext()) {
			if (c.next().getRealDouble() >= threshold) {
				c.localize(position);
				raMask.setPosition(position);
				raMask.get().set(true);
			}
		}
		output = new BinaryMaskOverlay(new BinaryMaskRegionOfInterest<BitType, Img<BitType>>(mask));
		output.setAlpha(alpha);
		output.setFillColor(color);
	}
}
