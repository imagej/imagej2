//
// RGBImageTranslator.java
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

package imagej.legacy;

import ij.ImagePlus;
import ij.ImageStack;
import ij.process.ColorProcessor;
import imagej.model.Dataset;
import imagej.model.Metadata;
import mpicbg.imglib.image.Image;
import mpicbg.imglib.type.numeric.integer.UnsignedByteType;

/**
 * Translates between legacy and modern ImageJ image structures for RGB data.
 * 
 * @author Barry DeZonia
 */
public class RGBImageTranslator implements ImageTranslator {

	/**
	 * Expects input {@link ImagePlus} to be of type COLOR_RGB with one channel.
	 */
	@Override
	public Dataset createDataset(final ImagePlus imp) {
		if (imp.getType() != ImagePlus.COLOR_RGB) {
			throw new IllegalArgumentException(
				"an ImagePlus of type COLOR_RGB is required for this operation");
		}

		if (imp.getNChannels() != 1) {
			throw new IllegalArgumentException(
				"expected color image to have a single channel of argb data");
		}

		final int w = imp.getWidth();
		final int h = imp.getHeight();
		final int c = 3;
		final int z = imp.getNSlices();
		final int t = imp.getNFrames();

		final int[] imageDims = new int[] { w, h, c, z, t };

		final Image<UnsignedByteType> image =
			Dataset.createPlanarImage(imp.getTitle(), new UnsignedByteType(),
				imageDims);

		final Metadata metadata = LegacyMetadata.create(imp);

		final Dataset dataset = new Dataset(image, metadata);

		final int totPixels = w * h;

		int planeIndex = 0;
		for (int tIndex = 0; tIndex < t; tIndex++) {
			for (int zIndex = 0; zIndex < z; zIndex++) {
				final ColorProcessor proc =
					(ColorProcessor) imp.getStack().getProcessor(planeIndex + 1);
				final byte[] rValues = new byte[totPixels];
				final byte[] gValues = new byte[totPixels];
				final byte[] bValues = new byte[totPixels];
				proc.getRGB(rValues, gValues, bValues);
				dataset.setPlane(3 * planeIndex + 0, rValues);
				dataset.setPlane(3 * planeIndex + 1, gValues);
				dataset.setPlane(3 * planeIndex + 2, bValues);
				planeIndex++;
			}
		}

		dataset.setIsRgbMerged(true);

		return dataset;
	}

	/**
	 * Expects input {@link Dataset} to have isRgbMerged() set with 3 channels.
	 */
	@Override
	public ImagePlus createLegacyImage(final Dataset dataset) {
		if (!dataset.isRgbMerged()) throw new IllegalArgumentException(
			"a merged dataset is required for this operation");

		if (dataset.getImage().getDimension(2) != 3) {
			throw new IllegalArgumentException("expected dataset to have "
				+ "channel dimension be the 3rd dimension with value of 3");
		}

		final int w = dataset.getImage().getDimension(0);
		final int h = dataset.getImage().getDimension(1);
		// c == 3 is already known
		final int z = dataset.getImage().getDimension(3);
		final int t = dataset.getImage().getDimension(4);

		final ImageStack stack = new ImageStack(w, h);

		int planeIndex = 0;
		for (int tIndex = 0; tIndex < t; tIndex++) {
			for (int zIndex = 0; zIndex < z; zIndex++) {
				final byte[] rValues = (byte[]) dataset.getPlane(planeIndex + 0);
				final byte[] gValues = (byte[]) dataset.getPlane(planeIndex + 1);
				final byte[] bValues = (byte[]) dataset.getPlane(planeIndex + 2);
				final ColorProcessor proc = new ColorProcessor(w, h);
				proc.setRGB(rValues, gValues, bValues);
				stack.addSlice(null, proc);
				planeIndex += 3;
			}
		}

		return new ImagePlus(dataset.getMetadata().getName(), stack);
	}

}
