/*
 * #%L
 * ImageJ software for multidimensional image processing and analysis.
 * %%
 * Copyright (C) 2009 - 2013 Board of Regents of the University of
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
import net.imglib2.type.numeric.RealType;

/**
 * Supports bidirectional synchronization between color {@link ImagePlus}es and
 * merged {@link Dataset}s.
 * 
 * @author Barry DeZonia
 */
public class ColorPixelHarmonizer implements DataHarmonizer {

	private double[] savedPlane;
	private int savedPos;

	public void savePlane(int pos, double[] plane) {
		savedPos = pos;
		savedPlane = plane;
	}

	/**
	 * Assigns the data values of a color {@link Dataset} from a paired
	 * {@link ImagePlus}. Assumes the Dataset and ImagePlus have compatible
	 * dimensions and are both of type color. Gets values via
	 * {@link ImageProcessor}::get(). Does not change the Dataset's metadata.
	 */
	@Override
	public void updateDataset(final Dataset ds, final ImagePlus imp) {
		final int xIndex = ds.getAxisIndex(Axes.X);
		final int yIndex = ds.getAxisIndex(Axes.Y);
		final int cIndex = ds.getAxisIndex(Axes.CHANNEL);
		final int zIndex = ds.getAxisIndex(Axes.Z);
		final int tIndex = ds.getAxisIndex(Axes.TIME);
		final int xSize = imp.getWidth();
		final int ySize = imp.getHeight();
		final int cSize = imp.getNChannels();
		final int zSize = imp.getNSlices();
		final int tSize = imp.getNFrames();
		final ImageStack stack = imp.getStack();
		int imagejPlaneNumber = 1;
		final RandomAccess<? extends RealType<?>> accessor =
			ds.getImgPlus().randomAccess();
		int slice = imp.getCurrentSlice();
		for (int t = 0; t < tSize; t++) {
			if (tIndex >= 0) accessor.setPosition(t, tIndex);
			for (int z = 0; z < zSize; z++) {
				if (zIndex >= 0) accessor.setPosition(z, zIndex);
				for (int c = 0; c < cSize; c++) {
					final ImageProcessor proc = stack.getProcessor(imagejPlaneNumber++);
					// TEMP HACK THAT FIXES VIRT STACK PROB BUT SLOW
					// imp.setPosition(planeNum - 1);
					for (int y = 0; y < ySize; y++) {
						accessor.setPosition(y, yIndex);
						for (int x = 0; x < xSize; x++) {
							accessor.setPosition(x, xIndex);
							final int value;
							if (savedPos == imagejPlaneNumber - 1) {
								int index = xSize * y + x;
								value = (int) savedPlane[index];
							}
							else {
								value = proc.get(x, y);
							}
							final int rValue = (value >> 16) & 0xff;
							final int gValue = (value >> 8) & 0xff;
							final int bValue = (value >> 0) & 0xff;
							accessor.setPosition(c * 3, cIndex);
							accessor.get().setReal(rValue);
							accessor.fwd(cIndex);
							accessor.get().setReal(gValue);
							accessor.fwd(cIndex);
							accessor.get().setReal(bValue);
						}
					}
				}
			}
		}
		// NB - virtual stack fix
		stack.getProcessor(slice);

		ds.update();
	}

	/**
	 * Assigns the data values of a color {@link ImagePlus} from a paired
	 * {@link Dataset}. Assumes the Dataset and ImagePlus have compatible
	 * dimensions and that the data planes are not directly mapped. Also assumes
	 * that the Dataset has isRGBMerged() true. Sets values via
	 * {@link ImageProcessor}::set(). Does not change the ImagePlus' metadata.
	 */
	@Override
	public void updateLegacyImage(final Dataset ds, final ImagePlus imp) {
		final int xIndex = ds.getAxisIndex(Axes.X);
		final int yIndex = ds.getAxisIndex(Axes.Y);
		final int cIndex = ds.getAxisIndex(Axes.CHANNEL);
		final int zIndex = ds.getAxisIndex(Axes.Z);
		final int tIndex = ds.getAxisIndex(Axes.TIME);
		final int xSize = imp.getWidth();
		final int ySize = imp.getHeight();
		final int cSize = imp.getNChannels();
		final int zSize = imp.getNSlices();
		final int tSize = imp.getNFrames();
		final ImageStack stack = imp.getStack();
		int imagejPlaneNumber = 1;
		final RandomAccess<? extends RealType<?>> accessor =
			ds.getImgPlus().randomAccess();
		int slice = imp.getCurrentSlice();
		for (int t = 0; t < tSize; t++) {
			if (tIndex >= 0) accessor.setPosition(t, tIndex);
			for (int z = 0; z < zSize; z++) {
				if (zIndex >= 0) accessor.setPosition(z, zIndex);
				for (int c = 0; c < cSize; c++) {
					final ImageProcessor proc = stack.getProcessor(imagejPlaneNumber++);
					// TEMP HACK THAT FIXES VIRT STACK PROB BUT SLOW
					// imp.setPosition(planeNum - 1);
					for (int y = 0; y < ySize; y++) {
						accessor.setPosition(y, yIndex);
						for (int x = 0; x < xSize; x++) {
							accessor.setPosition(x, xIndex);

							accessor.setPosition(3 * c, cIndex);
							final int rValue = ((int) accessor.get().getRealDouble()) & 0xff;

							accessor.fwd(cIndex);
							final int gValue = ((int) accessor.get().getRealDouble()) & 0xff;

							accessor.fwd(cIndex);
							final int bValue = ((int) accessor.get().getRealDouble()) & 0xff;

							final int intValue =
								(0xff << 24) | (rValue << 16) | (gValue << 8) | (bValue);

							proc.set(x, y, intValue);
						}
					}
				}
			}
		}
		// NB - virtual stack fix
		stack.getProcessor(slice);
	}

}
