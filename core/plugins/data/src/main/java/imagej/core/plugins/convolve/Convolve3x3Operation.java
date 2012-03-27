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

package imagej.core.plugins.convolve;

import imagej.core.plugins.neigh.Neighborhood3x3Operation;
import imagej.core.plugins.neigh.Neighborhood3x3Watcher;
import imagej.data.Dataset;
import imagej.util.RealRect;

/**
 * Convolve3x3Operation is used for general 3x3 convolution. It takes a 3x3
 * kernel as input. Kernel is actually stored as a 1-D array such that
 * {0,1,2,3,4,5,6,7,8} implies this shape: {{0,1,2},{3,4,5},{6,7,8}}. This class
 * is used by the various Shadow implementations, SharpenDataValues,
 * SmoothDataValues, etc.
 * 
 * @author Barry DeZonia
 */
public class Convolve3x3Operation {

	// -- instance variables --

	/**
	 * The kernel to convolve an input Dataset by
	 */
	private final double[] kernel;

	/**
	 * The 3x3 operation that will run on the input Dataset and call back this
	 * class as needed
	 */
	private final Neighborhood3x3Operation neighOperation;

	// -- constructor --

	/**
	 * Constructor. takes an input Dataset and a kernel that will be used to
	 * calculate data values.
	 */
	public Convolve3x3Operation(final Dataset input, final RealRect selection,
		final double[] kernel)
	{
		this.kernel = kernel;
		this.neighOperation =
			new Neighborhood3x3Operation(input, selection,
				new ConvolveWatcher(input));

		if (kernel.length != 9) throw new IllegalArgumentException(
			"kernel must contain nine elements (shaped 3x3)");
	}

	// -- public interface --

	/**
	 * Runs the convolution and replaces pixels in place with convolved values
	 */
	public void run() {
		neighOperation.run();
	}

	// -- private interface --

	/**
	 * ConvolveWatcher is where the actual convolution value of one output pixel
	 * is calculated. The watcher is called from Neighborhood3x3Operation visiting
	 * each pixel in the input image (and all its immediate neighbors) once.
	 * ConvolveWatcher tallies that information and returns appropriate values as
	 * necessary.
	 */
	@SuppressWarnings("synthetic-access")
	private class ConvolveWatcher implements Neighborhood3x3Watcher {

		private double scale;
		private double sum;
		private final boolean integerDataset;
		private final double typeMinValue;
		private final double typeMaxValue;

		public ConvolveWatcher(final Dataset ds) {
			integerDataset = ds.isInteger();
			typeMinValue = ds.getType().getMinValue();
			typeMaxValue = ds.getType().getMaxValue();
		}

		/** Precalculates the kernel scale for use later. */
		@Override
		public void setup() {
			scale = 0;
			for (int i = 0; i < kernel.length; i++)
				scale += kernel[i];
			if (scale == 0) scale = 1;
		}

		/** At each new neighborhood reset its value sum to 0. */
		@Override
		public void initializeNeighborhood(final long[] position) {
			sum = 0;
		}

		/**
		 * For each pixel visited in the 3x3 neighborhood add the kernel scaled
		 * value.
		 */
		@Override
		public void visitLocation(final int dx, final int dy, final double value) {
			final int index = (dy + 1) * (3) + (dx + 1);
			sum += value * kernel[index];
		}

		/**
		 * Called after all pixels in neighborhood visited - divide the sum by the
		 * kernel scale.
		 */
		@Override
		public double calcOutputValue() {
			double value;

			if (integerDataset) {
				value = (sum + (scale / 2)) / scale;
				if (value < typeMinValue) value = typeMinValue;
				if (value > typeMaxValue) value = typeMaxValue;
			}
			else {
				value = sum / scale;
			}
			return value;
		}

	}

}
