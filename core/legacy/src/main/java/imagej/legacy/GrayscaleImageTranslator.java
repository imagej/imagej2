//
// GrayscaleImageTranslator.java
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
import imagej.data.Dataset;
import imagej.util.Dimensions;
import imagej.util.Index;
import imagej.util.Log;
import net.imglib2.img.Axes;
import net.imglib2.img.Axis;

/**
 * Translates between legacy and modern ImageJ image structures for non-RGB
 * data.
 * 
 * @author Curtis Rueden
 * @author Barry DeZonia
 */
public class GrayscaleImageTranslator implements ImageTranslator {

	private LegacyMetadataTranslator metadataTranslator = 
		new LegacyMetadataTranslator();
	
	@Override
	public Dataset createDataset(final ImagePlus imp) {
		final int x = imp.getWidth();
		final int y = imp.getHeight();
		final int c = imp.getNChannels();
		final int z = imp.getNSlices();
		final int t = imp.getNFrames();
		final long[] dims = new long[] { x, y, c, z, t };
		final String name = imp.getTitle();
		final Axis[] axes = { Axes.X, Axes.Y, Axes.CHANNEL, Axes.Z, Axes.TIME };
		final int bitsPerPixel = imp.getBitDepth();
		final boolean signed = isSigned(imp);
		final boolean floating = isFloating(imp);
		final Dataset dataset =
			Dataset.create(dims, name, axes, bitsPerPixel, signed, floating);

		// copy planes by reference
		final long planeCount = Dimensions.getTotalPlanes(dataset.getDims());
		for (int p = 0; p < planeCount; p++) {
			final Object plane = imp.getStack().getPixels(p + 1);
			if (plane == null) {
				Log.error("Could not extract plane from ImageStack: " + p);
			}
			dataset.setPlane(p, plane);
		}

		// set metadata
		metadataTranslator.setDatasetMetadata(dataset, imp);
		
		return dataset;
	}

	@Override
	public ImagePlus createLegacyImage(final Dataset dataset) {
		final long[] dims = dataset.getDims();

		// check width
		final int xIndex = dataset.getAxisIndex(Axes.X);
		if (xIndex != 0) {
			throw new IllegalArgumentException("Expected X as dimension #0");
		}
		final long width = dims[xIndex];
		if (width > Integer.MAX_VALUE) {
			throw new IllegalArgumentException("Width out of range: " + width);
		}
		final int w = (int) width;

		// check height
		final int yIndex = dataset.getAxisIndex(Axes.Y);
		if (yIndex != 1) {
			throw new IllegalArgumentException("Expected Y as dimension #1");
		}
		final long height = dims[yIndex];
		if (height > Integer.MAX_VALUE) {
			throw new IllegalArgumentException("Height out of range: " + height);
		}
		final int h = (int) height;

		// check channels, slices and frames
		final int cIndex = dataset.getAxisIndex(Axes.CHANNEL);
		final int zIndex = dataset.getAxisIndex(Axes.Z);
		final int tIndex = dataset.getAxisIndex(Axes.TIME);
		final long cCount = cIndex < 0 ? 1 : dims[cIndex];
		final long zCount = zIndex < 0 ? 1 : dims[zIndex];
		final long tCount = tIndex < 0 ? 1 : dims[tIndex];
		if (cCount * zCount * tCount > Integer.MAX_VALUE) {
			throw new IllegalArgumentException(
				message("Too many planes", cCount, zCount, tCount));
		}
		
		// make sure there are not any other axis types present
		int ijCompatAxesPresent = 2;
		if (cIndex >= 0) ijCompatAxesPresent++;
		if (zIndex >= 0) ijCompatAxesPresent++;
		if (tIndex >= 0) ijCompatAxesPresent++;
		if (ijCompatAxesPresent != dims.length)
			throw new IllegalArgumentException(
				"Dataset has one or more axes that can not be classified as"+
				" X, Y, Z, C, or T");
		
		final ImageStack stack = new ImageStack(w, h);

		final long[] planeDims = new long[dims.length - 2];
		for (int i = 0; i < planeDims.length; i++)
			planeDims[i] = dims[i + 2];
		final long[] planePos = new long[planeDims.length];

		for (long t = 0; t < tCount; t++) {
			if (tIndex >= 0) planePos[tIndex - 2] = t;
			for (long z = 0; z < zCount; z++) {
				if (zIndex >= 0) planePos[zIndex - 2] = z;
				for (long c = 0; c < cCount; c++) {
					if (cIndex >= 0) planePos[cIndex - 2] = c;
					final long no = Index.indexNDto1D(planeDims, planePos);
					if (no > Integer.MAX_VALUE) {
						throw new IllegalArgumentException(
							message("Plane out of range", c, z, t) + ", no=" + no);
					}
					final Object plane = dataset.getPlane((int) no);
					if (plane == null) {
						Log.error(message("Could not extract plane from Dataset", c, z, t));
					}
					stack.addSlice(null, plane);
				}
			}
		}

		ImagePlus imp = new ImagePlus(dataset.getName(), stack);
		
		// set metadata
		metadataTranslator.setImagePlusMetadata(dataset, imp);
		
		return imp;
	}

	// -- Helper methods --

	private boolean isSigned(final ImagePlus imp) {
		final int type = imp.getType();
		return type == ImagePlus.GRAY32;
	}

	private boolean isFloating(final ImagePlus imp) {
		final int type = imp.getType();
		return type == ImagePlus.GRAY32;
	}

	private String message(final String message, final long c, final long z,
		final long t)
	{
		return message + ": c=" + c + ", z=" + z + ", t=" + t;
	}

}
