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

package imagej.data.threshold;

import org.scijava.plugin.Plugin;
import org.scijava.plugin.SortablePlugin;

//NB - this plugin adapted from Gabriel Landini's code of his AutoThreshold
//plugin found in Fiji (version 1.14).

/**
 * Implements Huang's threshold method by Huang & Wang.
 * 
 * @author Barry DeZonia
 * @author Gabriel Landini
 */
@Plugin(type = ThresholdMethod.class, name = "Huang")
public class HuangThresholdMethod extends SortablePlugin implements
	ThresholdMethod
{

	@Override
	public int getThreshold(long[] histogram) {
		// Implements Huang's fuzzy thresholding method
		// Uses Shannon's entropy function (one can also use Yager's entropy
		// function) Huang L.-K. and Wang M.-J.J. (1995) "Image Thresholding by
		// Minimizing the Measures of Fuzziness" Pattern Recognition, 28(1): 41-51
		// Reimplemented (to handle 16-bit efficiently) by Johannes Schindelin
		// Jan 31, 2011

		// find first and last non-empty bin
		int first, last;
		for (first = 0; first < histogram.length && histogram[first] == 0; first++)
		{
			// do nothing
		}
		for (last = histogram.length - 1; last > first && histogram[last] == 0; last--)
		{
			// do nothing
		}
		if (first == last) return 0;

		// calculate the cumulative density and the weighted cumulative density
		double[] S = new double[last + 1], W = new double[last + 1];
		S[0] = histogram[0];
		for (int i = Math.max(1, first); i <= last; i++) {
			S[i] = S[i - 1] + histogram[i];
			W[i] = W[i - 1] + i * histogram[i];
		}

		// precalculate the summands of the entropy given the absolute difference x
		// - mu (integral)
		double C = last - first;
		double[] Smu = new double[last + 1 - first];
		for (int i = 1; i < Smu.length; i++) {
			double mu = 1 / (1 + Math.abs(i) / C);
			Smu[i] = -mu * Math.log(mu) - (1 - mu) * Math.log(1 - mu);
		}

		// calculate the threshold
		int bestThreshold = 0;
		double bestEntropy = Double.MAX_VALUE;
		for (int threshold = first; threshold <= last; threshold++) {
			double entropy = 0;
			int mu = (int) Math.round(W[threshold] / S[threshold]);
			for (int i = first; i <= threshold; i++)
				entropy += Smu[Math.abs(i - mu)] * histogram[i];
			mu =
				(int) Math.round((W[last] - W[threshold]) / (S[last] - S[threshold]));
			for (int i = threshold + 1; i <= last; i++)
				entropy += Smu[Math.abs(i - mu)] * histogram[i];

			if (bestEntropy > entropy) {
				bestEntropy = entropy;
				bestThreshold = threshold;
			}
		}

		return bestThreshold;
	}

	@Override
	public String getMessage() {
		return null;
	}

}
