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
import ij.measure.Calibration;
import ij.process.ColorProcessor;
import imagej.data.Dataset;
import net.imglib2.img.Axes;
import net.imglib2.img.Axis;
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

		final String name = imp.getTitle();
		final Axis[] axes = { Axes.X, Axes.Y, Axes.CHANNEL, Axes.Z, Axes.TIME };
		final Dataset dataset =
			Dataset.create(new UnsignedByteType(), imageDims, name, axes);

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

		// copy calibration data
		Calibration cal = imp.getCalibration();
		dataset.setCalibration(cal.pixelWidth, 0);
		dataset.setCalibration(cal.pixelHeight, 1);
		dataset.setCalibration(cal.pixelDepth, 3);

		dataset.setRGBMerged(true);
		
		return dataset;
	}

	/**
	 * Expects input {@link Dataset} to have isRgbMerged() set with 3 channels.
	 */
	@Override
	public ImagePlus createLegacyImage(final Dataset dataset) {
		if (!dataset.isRGBMerged()) {
			throw new IllegalArgumentException(
				"A merged dataset is required for this operation");
		}

		if ( ! (dataset.getType() instanceof UnsignedByteType) )
			throw new IllegalArgumentException("Expected a dataset of unsigned byte data type");
			
		final long[] dims = dataset.getDims();

		int xIndex = dataset.getAxisIndex(Axes.X); 
		int yIndex = dataset.getAxisIndex(Axes.Y); 
		int cIndex = dataset.getAxisIndex(Axes.CHANNEL); 
		int zIndex = dataset.getAxisIndex(Axes.Z);
		int tIndex = dataset.getAxisIndex(Axes.TIME);

		// check width
		if (xIndex < 0)
			throw new IllegalArgumentException("X axis not present in dataset!");
		final long width = dims[xIndex];
		if (width > Integer.MAX_VALUE) {
			throw new IllegalArgumentException("Width out of range: " + width);
		}
		final int w = (int) width;

		// check height
		if (yIndex < 0)
			throw new IllegalArgumentException("Y axis not present in dataset!");
		final long height = dims[yIndex];
		if (height > Integer.MAX_VALUE) {
			throw new IllegalArgumentException("Height out of range: " + height);
		}
		final int h = (int) height;

		// check channels
		if (cIndex < 0)
			throw new IllegalArgumentException("Channel axis not present in dataset!");
		final long c = dims[cIndex];
		if (c != 3) {
			throw new IllegalArgumentException("Expected dataset to have 3 channels");
		}

		// check slices
		final long z = (zIndex >= 0) ? dims[zIndex] : 1;
		if (z > Integer.MAX_VALUE) {
			throw new IllegalArgumentException("Slice count out of range: " + z);
		}
		
		// check frames
		final long t = (tIndex >= 0) ? dims[tIndex] : 1;
		if (t > Integer.MAX_VALUE) {
			throw new IllegalArgumentException("Frame count out of range: " + t);
		}

		// check total count of planes
		if (c * z * t > Integer.MAX_VALUE) {
			throw new IllegalArgumentException("Too many planes: z=" + z + ", t=" +
				t);
		}

		//System.out.println("xyczt = "+w+" "+h+" "+c+" "+z+" "+t);

		// make sure there are not any other axis types present
		int ijCompatAxesPresent = 0;
		if (xIndex >= 0) ijCompatAxesPresent++;
		if (yIndex >= 0) ijCompatAxesPresent++;
		if (cIndex >= 0) ijCompatAxesPresent++;
		if (zIndex >= 0) ijCompatAxesPresent++;
		if (tIndex >= 0) ijCompatAxesPresent++;
		if (ijCompatAxesPresent != dims.length)
			throw new IllegalArgumentException("Some dimension other than X, Y, C, Z, or T present in Dataset");

		// set the data values in the ImagePlus
		final ImageStack stack = new ImageStack(w, h);
		long[] position = new long[ijCompatAxesPresent];
		for (int ti = 0; ti < t; ti++) {
			for (int zi = 0; zi < z; zi++) {
				ColorProcessor proc = new ColorProcessor(w, h);
				for (int yi = 0; yi < h; yi++) {
					for (int xi = 0; xi < w; xi++) {
						position[xIndex] = xi;
						position[yIndex] = yi;
						if (zIndex >= 0) position[zIndex] = zi;
						if (tIndex >= 0) position[tIndex] = ti;
						
						position[cIndex] = 0;
						int rValue = (int) dataset.getDoubleValue(position);
						
						position[cIndex] = 1;
						int gValue = (int) dataset.getDoubleValue(position);
						
						position[cIndex] = 2;
						int bValue = (int) dataset.getDoubleValue(position);
						
						int pixValue = 0xff000000 | (rValue<<16) | (gValue<<8) | bValue;
						
						proc.set(xi, yi, pixValue);
					}
				}
				stack.addSlice(null, proc);
			}
		}
		ImagePlus imp = new ImagePlus(dataset.getName(), stack);
		imp.setDimensions(1, (int)z, (int)t);

		// copy calibration data
		Calibration cal = imp.getCalibration();
		cal.pixelWidth = dataset.calibration(xIndex);
		cal.pixelHeight = dataset.calibration(yIndex);
		if (zIndex >= 0)
			cal.pixelDepth = dataset.calibration(zIndex);
		
		return imp;
	}

}
