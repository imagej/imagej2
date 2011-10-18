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

import net.imglib2.RandomAccess;
import net.imglib2.img.Axes;
import net.imglib2.img.Axis;
import net.imglib2.type.logic.BitType;
import net.imglib2.type.numeric.RealType;
import ij.ImagePlus;
import ij.ImageStack;
import ij.process.ImageProcessor;
import imagej.data.Dataset;


/**
 * Supports bidirectional synchronization between ImagePluses and gray Datasets.
 * Single channel color ImagePluses are not supported here. But multichannel
 * color ImagePluses are handled and treated as gray data.
 * 
 * @author Barry DeZonia
 *
 */
public class GrayPixelHarmonizer implements DataHarmonizer {

	/**
	 * Assigns the data values of a {@link Dataset} from a paired
	 * {@link ImagePlus}. Assumes the Dataset and ImagePlus have compatible
	 * dimensions and that the data planes are not directly mapped. Gets values
	 * via {@link ImageProcessor}::getf(). In cases where there is a narrowing of
	 * data into IJ2 types the data is range clamped. Does not change the
	 * Dataset's metadata.
	 */
	@Override
	public void updateDataset(Dataset ds, ImagePlus imp) {
		final RealType<?> type = ds.getType();
		final double typeMin = type.getMinValue();
		final double typeMax = type.getMaxValue();
		final RandomAccess<? extends RealType<?>> accessor =
			ds.getImgPlus().randomAccess();
		final long[] dims = ds.getDims();
		final Axis[] axes = ds.getAxes();
		final int xIndex = ds.getAxisIndex(Axes.X);
		final int yIndex = ds.getAxisIndex(Axes.Y);
		final int zIndex = ds.getAxisIndex(Axes.Z);
		final int tIndex = ds.getAxisIndex(Axes.TIME);
		final int xSize = imp.getWidth();
		final int ySize = imp.getHeight();
		final int zSize = imp.getNSlices();
		final int tSize = imp.getNFrames();
		final int cSize = imp.getNChannels();
		final ImageStack stack = imp.getStack();
		int planeNum = 1;
		final long[] pos = new long[dims.length];
		for (int t = 0; t < tSize; t++) {
			if (tIndex >= 0) pos[tIndex] = t;
			for (int z = 0; z < zSize; z++) {
				if (zIndex >= 0) pos[zIndex] = z;
				for (int c = 0; c < cSize; c++) {
					fillChannelIndices(dims, axes, c, pos);
					final ImageProcessor proc = stack.getProcessor(planeNum++);
					for (int x = 0; x < xSize; x++) {
						if (xIndex >= 0) pos[xIndex] = x;
						for (int y = 0; y < ySize; y++) {
							if (yIndex >= 0) pos[yIndex] = y;
							accessor.setPosition(pos);
							float value = proc.getf(x, y);
							if (value < typeMin) value = (float) typeMin;
							if (value > typeMax) value = (float) typeMax;
							accessor.get().setReal(value);
						}
					}
				}
			}
		}
		ds.update();
	}

	/**
	 * Assigns the data values of an {@link ImagePlus} from a paired
	 * {@link Dataset}. Assumes the Dataset and ImagePlus are not directly mapped.
	 * It is possible that multiple IJ2 axes are encoded as a single set of
	 * channels in the ImagePlus. Sets values via {@link ImageProcessor}::setf().
	 * Some special case code is in place to assure that BitType images go to IJ1
	 * as 0/255 value images. Does not change the ImagePlus' metadata.
	 */
	@Override
	public void updateLegacyImage(Dataset ds, ImagePlus imp) {
		final boolean bitData = ds.getType() instanceof BitType;
		final RandomAccess<? extends RealType<?>> accessor =
			ds.getImgPlus().randomAccess();
		final long[] dims = ds.getDims();
		final Axis[] axes = ds.getAxes();
		final int xIndex = ds.getAxisIndex(Axes.X);
		final int yIndex = ds.getAxisIndex(Axes.Y);
		final int zIndex = ds.getAxisIndex(Axes.Z);
		final int tIndex = ds.getAxisIndex(Axes.TIME);
		final int xSize = imp.getWidth();
		final int ySize = imp.getHeight();
		final int zSize = imp.getNSlices();
		final int tSize = imp.getNFrames();
		final int cSize = imp.getNChannels();
		final ImageStack stack = imp.getStack();
		int planeNum = 1;
		final long[] pos = new long[dims.length];
		for (int t = 0; t < tSize; t++) {
			if (tIndex >= 0) pos[tIndex] = t;
			for (int z = 0; z < zSize; z++) {
				if (zIndex >= 0) pos[zIndex] = z;
				for (int c = 0; c < cSize; c++) {
					fillChannelIndices(dims, axes, c, pos);
					final ImageProcessor proc = stack.getProcessor(planeNum++);
					for (int x = 0; x < xSize; x++) {
						if (xIndex >= 0) pos[xIndex] = x;
						for (int y = 0; y < ySize; y++) {
							if (yIndex >= 0) pos[yIndex] = y;
							accessor.setPosition(pos);
							float value = accessor.get().getRealFloat();
							if (bitData) if (value > 0) value = 255;
							proc.setf(x, y, value);
						}
					}
				}
			}
		}
	}

	// -- private interface --
	
	/**
	 * Fills IJ1 incompatible indices of a position array. The channel from IJ1
	 * is rasterized into potentially multiple indices in IJ2's position array.
	 * For instance an IJ2 image with CHANNELs and SPECTRA get encoded as
	 * multiple channels in IJ!. When coming back from IJ1 need to rasterize
	 * the its single channel index back into (CHANNEL,SPECTRA) pairs.
	 * @param dims - the dimensions of the IJ2 Dataset
	 * @param axes - the axes labels that match the Dataset dimensions
	 * @param channelIndex - the index of the IJ1 encoded channel position
	 * @param pos - the position array to fill with rasterized values
	 */
	private void fillChannelIndices(final long[] dims, final Axis[] axes,
		final long channelIndex, final long[] pos)
	{
		long workingIndex = channelIndex;
		for (int i = 0; i < dims.length; i++) {
			final Axis axis = axes[i];
			// skip axes we don't encode as channels
			if (axis == Axes.X) continue;
			if (axis == Axes.Y) continue;
			if (axis == Axes.Z) continue;
			if (axis == Axes.TIME) continue;
			// calc index of encoded channels
			final long subIndex = workingIndex % dims[i];
			pos[i] = subIndex;
			workingIndex = workingIndex / dims[i];
		}
	}

}
