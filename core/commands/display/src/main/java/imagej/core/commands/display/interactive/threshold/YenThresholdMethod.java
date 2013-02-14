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

package imagej.core.commands.display.interactive.threshold;

import imagej.plugin.Plugin;

// NB - this plugin adapted from Gabriel Landini's code of his AutoThreshold
// plugin found in Fiji.

/**
 * Implements Yen's threshold method for ImageJ.
 * 
 * @author Barry DeZonia
 * @author Gabriel Landini
 */
@Plugin(type = AutoThresholdMethod.class, name = "Yen")
public class YenThresholdMethod implements AutoThresholdMethod {

	@Override
	public int getThreshold(long[] histogram) {
		// Implements Yen thresholding method
		// 1) Yen J.C., Chang F.J., and Chang S. (1995) "A New Criterion
		// for Automatic Multilevel Thresholding" IEEE Trans. on Image
		// Processing, 4(3): 370-378
		// 2) Sezgin M. and Sankur B. (2004) "Survey over Image Thresholding
		// Techniques and Quantitative Performance Evaluation" Journal of
		// Electronic Imaging, 13(1): 146-165
		// http://citeseer.ist.psu.edu/sezgin04survey.html
		//
		// M. Emre Celebi
		// 06.15.2007
		// Ported to ImageJ plugin by G.Landini from E Celebi's fourier_0.8 routines
		int threshold;
		int ih, it;
		double crit;
		double max_crit;
		double[] norm_histo = new double[histogram.length]; /* normalized histogram */
		double[] P1 = new double[histogram.length]; /* cumulative normalized histogram */
		double[] P1_sq = new double[histogram.length];
		double[] P2_sq = new double[histogram.length];

		long total = 0;
		for (ih = 0; ih < histogram.length; ih++)
			total += histogram[ih];

		for (ih = 0; ih < histogram.length; ih++)
			norm_histo[ih] = (double) histogram[ih] / total;

		P1[0] = norm_histo[0];
		for (ih = 1; ih < histogram.length; ih++)
			P1[ih] = P1[ih - 1] + norm_histo[ih];

		P1_sq[0] = norm_histo[0] * norm_histo[0];
		for (ih = 1; ih < histogram.length; ih++)
			P1_sq[ih] = P1_sq[ih - 1] + norm_histo[ih] * norm_histo[ih];

		P2_sq[histogram.length - 1] = 0.0;
		for (ih = histogram.length - 2; ih >= 0; ih--)
			P2_sq[ih] = P2_sq[ih + 1] + norm_histo[ih + 1] * norm_histo[ih + 1];

		/* Find the threshold that maximizes the criterion */
		threshold = -1;
		max_crit = Double.MIN_VALUE;
		for (it = 0; it < histogram.length; it++) {
			crit =
				-1.0 *
					((P1_sq[it] * P2_sq[it]) > 0.0 ? Math.log(P1_sq[it] * P2_sq[it])
						: 0.0) +
					2 *
					((P1[it] * (1.0 - P1[it])) > 0.0 ? Math.log(P1[it] * (1.0 - P1[it]))
						: 0.0);
			if (crit > max_crit) {
				max_crit = crit;
				threshold = it;
			}
		}
		return threshold;
	}

}
