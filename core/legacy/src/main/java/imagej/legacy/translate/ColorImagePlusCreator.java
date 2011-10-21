//
//
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

package imagej.legacy.translate;

import ij.ImagePlus;
import ij.ImageStack;
import imagej.ImageJ;
import imagej.data.Dataset;
import imagej.data.display.ImageDisplay;
import imagej.data.display.ImageDisplayService;

/**
 * Creates ImagePluses from ImageDisplays containing color merged data
 *  
 * @author Barry DeZonia
 *
 */
public class ColorImagePlusCreator implements ImagePlusCreator {

	// -- instance variables --

	private final ColorPixelHarmonizer pixelHarmonizer = new ColorPixelHarmonizer();
	private final MetadataHarmonizer metadataHarmonizer = new MetadataHarmonizer();
	
	// -- public interface --

	/**
	 * Creates a color {@link ImagePlus} from a color {@link ImageDisplay}.
	 * Expects input expects input ImageDisplay to have isRgbMerged() set with 3
	 * channels of unsigned byte data.
	 */
	@Override
	public ImagePlus createLegacyImage(final ImageDisplay display) {
		final ImageDisplayService imageDisplayService =
			ImageJ.get(ImageDisplayService.class);
		final Dataset ds = imageDisplayService.getActiveDataset(display);
		final ImagePlus imp = makeColorImagePlus(ds);
		pixelHarmonizer.updateLegacyImage(ds, imp);
		metadataHarmonizer.updateLegacyImage(ds, imp);
		return imp;
	}

	// -- private interface --

	/**
	 * Makes a color {@link ImagePlus} from a color {@link Dataset}. The ImagePlus
	 * will have the same X, Y, Z, & T dimensions. C will be 1. The data values
	 * and metadata are not assigned. Throws an exception if the dataset is not
	 * color compatible.
	 */
	private ImagePlus makeColorImagePlus(final Dataset ds) {
		if (!LegacyUtils.isColorCompatible(ds)) {
			throw new IllegalArgumentException("Dataset is not color compatible");
		}

		final int[] dimIndices = new int[5];
		final int[] dimValues = new int[5];
		LegacyUtils.getImagePlusDims(ds, dimIndices, dimValues);
		final int w = dimValues[0];
		final int h = dimValues[1];
		final int c = dimValues[2] / 3;
		final int z = dimValues[3];
		final int t = dimValues[4];

		final ImageStack stack = new ImageStack(w, h, c * z * t);

		for (int i = 0; i < c * z * t; i++)
			stack.setPixels(new int[w * h], i + 1);

		final ImagePlus imp = new ImagePlus(ds.getName(), stack);

		imp.setDimensions(c, z, t);

		return imp;
	}
}
