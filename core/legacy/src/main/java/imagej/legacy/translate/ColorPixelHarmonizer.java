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
		final int x = imp.getWidth();
		final int y = imp.getHeight();
		final int c = imp.getNChannels();
		final int z = imp.getNSlices();
		final int t = imp.getNFrames();
		int imagejPlaneNumber = 1;
		final RandomAccess<? extends RealType<?>> accessor =
			ds.getImgPlus().randomAccess();
		for (int ti = 0; ti < t; ti++) {
			if (tIndex >= 0) accessor.setPosition(ti, tIndex);
			for (int zi = 0; zi < z; zi++) {
				if (zIndex >= 0) accessor.setPosition(zi, zIndex);
				for (int ci = 0; ci < c; ci++) {
					final ImageProcessor proc =
						imp.getStack().getProcessor(imagejPlaneNumber++);
					for (int yi = 0; yi < y; yi++) {
						accessor.setPosition(yi, yIndex);
						for (int xi = 0; xi < x; xi++) {
							accessor.setPosition(xi, xIndex);
							final int value = proc.get(xi, yi);
							final int rValue = (value >> 16) & 0xff;
							final int gValue = (value >> 8) & 0xff;
							final int bValue = (value >> 0) & 0xff;
							accessor.setPosition(ci * 3 + 0, cIndex);
							accessor.get().setReal(rValue);
							accessor.setPosition(ci * 3 + 1, cIndex);
							accessor.get().setReal(gValue);
							accessor.setPosition(ci * 3 + 2, cIndex);
							accessor.get().setReal(bValue);
						}
					}
				}
			}
		}
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
		final int x = imp.getWidth();
		final int y = imp.getHeight();
		final int c = imp.getNChannels();
		final int z = imp.getNSlices();
		final int t = imp.getNFrames();
		int imagejPlaneNumber = 1;
		final RandomAccess<? extends RealType<?>> accessor =
			ds.getImgPlus().randomAccess();
		for (int ti = 0; ti < t; ti++) {
			if (tIndex >= 0) accessor.setPosition(ti, tIndex);
			for (int zi = 0; zi < z; zi++) {
				if (zIndex >= 0) accessor.setPosition(zi, zIndex);
				for (int ci = 0; ci < c; ci++) {
					final ImageProcessor proc =
						imp.getStack().getProcessor(imagejPlaneNumber++);
					for (int yi = 0; yi < y; yi++) {
						accessor.setPosition(yi, yIndex);
						for (int xi = 0; xi < x; xi++) {
							accessor.setPosition(xi, xIndex);

							accessor.setPosition(3 * ci + 0, cIndex);
							final int rValue = ((int) accessor.get().getRealDouble()) & 0xff;

							accessor.setPosition(3 * ci + 1, cIndex);
							final int gValue = ((int) accessor.get().getRealDouble()) & 0xff;

							accessor.setPosition(3 * ci + 2, cIndex);
							final int bValue = ((int) accessor.get().getRealDouble()) & 0xff;

							final int intValue =
								(0xff << 24) | (rValue << 16) | (gValue << 8) | (bValue);

							proc.set(xi, yi, intValue);
						}
					}
				}
			}
		}
	}

}
