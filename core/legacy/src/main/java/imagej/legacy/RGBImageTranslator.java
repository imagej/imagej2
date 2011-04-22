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
import imagej.data.Dataset;
import imagej.data.Metadata;
import net.imglib2.type.numeric.integer.UnsignedByteType;

/**
 * Translates between legacy and modern ImageJ image structures for RGB data.
 * 
 * @author Barry DeZonia
 * @author Curtis Rueden
 */
public class RGBImageTranslator implements ImageTranslator {

	/**
	 * Expects input {@link ImagePlus} to be of type {@link ImagePlus#COLOR_RGB}
	 * with one channel.
	 */
	@Override
	public Dataset createDataset(final ImagePlus imp) {
		if (imp.getType() != ImagePlus.COLOR_RGB) {
			throw new IllegalArgumentException(
				"an ImagePlus of type COLOR_RGB is required for this operation");
		}

		if (imp.getNChannels() != 1) {
			throw new IllegalArgumentException(
				"expected color image to have a single channel of ARGB data");
		}

		final int w = imp.getWidth();
		final int h = imp.getHeight();
		final int c = 3;
		final int z = imp.getNSlices();
		final int t = imp.getNFrames();

		final long[] imageDims = new long[] { w, h, c, z, t };

		final Metadata metadata = LegacyMetadata.create(imp);
		final Dataset dataset =
			Dataset.create(new UnsignedByteType(), imageDims, metadata);

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
		if (!dataset.isRgbMerged()) {
			throw new IllegalArgumentException(
				"A merged dataset is required for this operation");
		}

		final long[] dims = dataset.getDims();
		if (dims.length != 5) {
			throw new IllegalArgumentException(
				"Expected dataset to have 5 dimensions");
		}

		// check width
		final long width = dims[0];
		if (width > Integer.MAX_VALUE) {
			throw new IllegalArgumentException("Width out of range: " + width);
		}
		final int w = (int) width;

		// check height
		final long height = dims[1];
		if (height > Integer.MAX_VALUE) {
			throw new IllegalArgumentException("Height out of range: " + height);
		}
		final int h = (int) height;

		// check channels
		final long c = dims[2];
		if (c != 3) {
			throw new IllegalArgumentException("Expected dataset to have "
				+ "channel dimension be the 3rd dimension with value of 3");
		}

		// check slices and frames
		final long z = dims[3];
		final long t = dims[4];
		if (c * z * t > Integer.MAX_VALUE) {
			throw new IllegalArgumentException("Too many planes: z=" + z + ", t=" +
				t);
		}

		final ImageStack stack = new ImageStack(w, h);

		int planeIndex = 0;
		for (long tIndex = 0; tIndex < t; tIndex++) {
			for (long zIndex = 0; zIndex < z; zIndex++) {
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
