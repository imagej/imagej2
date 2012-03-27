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

package imagej.legacy.translate;

import ij.ImagePlus;
import ij.ImageStack;
import ij.process.ImageProcessor;
import imagej.data.Dataset;
import net.imglib2.RandomAccess;
import net.imglib2.meta.Axes;
import net.imglib2.meta.AxisType;
import net.imglib2.type.logic.BitType;
import net.imglib2.type.numeric.RealType;

/**
 * Supports bidirectional synchronization between {@link ImagePlus}es and gray
 * {@link Dataset}s. Single channel color {@link ImagePlus}es are not supported
 * here. But multichannel color {@link ImagePlus}es are handled and treated as
 * gray data.
 * 
 * @author Barry DeZonia
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
	public void updateDataset(final Dataset ds, final ImagePlus imp) {
		final RealType<?> type = ds.getType();
		final double typeMin = type.getMinValue();
		final double typeMax = type.getMaxValue();
		final RandomAccess<? extends RealType<?>> accessor =
			ds.getImgPlus().randomAccess();
		final long[] dims = ds.getDims();
		final AxisType[] axes = ds.getAxes();
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
					LegacyUtils.fillChannelIndices(dims, axes, c, pos);
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
	public void updateLegacyImage(final Dataset ds, final ImagePlus imp) {
		final boolean bitData = ds.getType() instanceof BitType;
		final RandomAccess<? extends RealType<?>> accessor =
			ds.getImgPlus().randomAccess();
		final long[] dims = ds.getDims();
		final AxisType[] axes = ds.getAxes();
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
					LegacyUtils.fillChannelIndices(dims, axes, c, pos);
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

}
