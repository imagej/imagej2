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
import imagej.data.Dataset;
import imagej.data.Metadata;
import imagej.util.Dimensions;
import net.imglib2.img.Axis;

/**
 * Translates between legacy and modern ImageJ image structures for non-RGB
 * data.
 * 
 * @author Curtis Rueden
 * @author Barry DeZonia
 */
public class GrayscaleImageTranslator implements ImageTranslator {

	@Override
	public Dataset createDataset(final ImagePlus imp) {
		final int w = imp.getWidth();
		final int h = imp.getHeight();
		final int c = imp.getNChannels();
		final int z = imp.getNSlices();
		final int t = imp.getNFrames();
		final long[] dims = new long[] { w, h, c, z, t };
		final Metadata metadata = LegacyMetadata.create(imp);
		final int bitsPerPixel = imp.getBitDepth();
		final boolean signed = isSigned(imp);
		final boolean floating = isFloating(imp);
		final String name = metadata.getName();
		final Axis[] axes = metadata.getAxes();
		final Dataset dataset =
			Dataset.create(dims, name, axes, bitsPerPixel, signed, floating);

		// copy planes by reference
		long planeCount = Dimensions.getTotalPlanes(dataset.getDims());
		for (int p = 0; p < planeCount; p++) {
			final Object plane = imp.getStack().getPixels(p + 1);
			dataset.setPlane(p, plane);
		}

		return dataset;
	}

	@Override
	public ImagePlus createLegacyImage(final Dataset dataset) {
//		return ImageJFunctions.displayAsVirtualStack(dataset.getImage(),
//			dataset.getMetadata().getName());
		// FIXME
		return null;
	}

	// -- Helper methods --

	private boolean isSigned(final ImagePlus imp) {
		final int type = imp.getType();
		return type == ImagePlus.GRAY32;
	}

	private boolean isFloating(final ImagePlus imp) {
		final int type = imp.getType();
		return type == ImagePlus.GRAY16;
	}

}
