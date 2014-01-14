/*
 * #%L
 * ImageJ software for multidimensional image processing and analysis.
 * %%
 * Copyright (C) 2009 - 2014 Board of Regents of the University of
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
 * #L%
 */

package imagej.data.threshold;

import net.imglib2.histogram.Histogram1d;

import org.scijava.plugin.Plugin;

// NB - this plugin adapted from Gabriel Landini's code of his AutoThreshold
// plugin found in Fiji (version 1.14).

/**
 * Implements Otsu's threshold method.
 * 
 * @author Barry DeZonia
 * @author Gabriel Landini
 */
@Plugin(type = ThresholdMethod.class, name = "Otsu")
public class OtsuThresholdMethod extends AbstractThresholdMethod {

	@Override
	public long getThreshold(Histogram1d<?> hist) {
		long[] histogram = hist.toLongArray();
		// Otsu's threshold algorithm
		// C++ code by Jordan Bevik <Jordan.Bevic@qtiworld.com>
		// ported to ImageJ plugin by G.Landini
		int k, kStar; // k = the current threshold; kStar = optimal threshold
		int L = histogram.length; // The total intensity of the image
		long N1, N; // N1 = # points with intensity <=k; N = total number of points
		long Sk; // The total intensity for all histogram points <=k
		long S;
		double BCV, BCVmax; // The current Between Class Variance and maximum BCV
		double num, denom; // temporary bookkeeping

		// Initialize values:
		S = 0;
		N = 0;
		for (k = 0; k < L; k++) {
			S += k * histogram[k]; // Total histogram intensity
			N += histogram[k]; // Total number of data points
		}

		Sk = 0;
		N1 = histogram[0]; // The entry for zero intensity
		BCV = 0;
		BCVmax = 0;
		kStar = 0;

		// Look at each possible threshold value,
		// calculate the between-class variance, and decide if it's a max
		for (k = 1; k < L - 1; k++) { // No need to check endpoints k = 0 or k = L-1
			Sk += k * histogram[k];
			N1 += histogram[k];

			// The float casting here is to avoid compiler warning about loss of
			// precision and
			// will prevent overflow in the case of large saturated images
			denom = (double) (N1) * (N - N1); // Maximum value of denom is (N^2)/4 =
																				// approx. 3E10

			if (denom != 0) {
				// Float here is to avoid loss of precision when dividing
				num = ((double) N1 / N) * S - Sk; // Maximum value of num = 255*N =
																					// approx 8E7
				BCV = (num * num) / denom;
			}
			else BCV = 0;

			if (BCV >= BCVmax) { // Assign the best threshold found so far
				BCVmax = BCV;
				kStar = k;
			}
		}
		// kStar += 1; // Use QTI convention that intensity -> 1 if intensity >= k
		// (the algorithm was developed for I-> 1 if I <= k.)
		return kStar;
	}

	@Override
	public String getMessage() {
		return null;
	}

}
