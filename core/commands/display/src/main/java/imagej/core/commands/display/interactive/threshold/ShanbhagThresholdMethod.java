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
 * Implements Shanbhag's threshold method for ImageJ.
 * 
 * @author Barry DeZonia
 * @author Gabriel Landini
 */
@Plugin(type = AutoThresholdMethod.class, name = "Shanbhag")
public class ShanbhagThresholdMethod implements AutoThresholdMethod {

	@Override
	public int getThreshold(long[] histogram) {
		// Shanhbag A.G. (1994) "Utilization of Information Measure as a Means of
		// Image Thresholding" Graphical Models and Image Processing, 56(5): 414-419
		// Ported to ImageJ plugin by G.Landini from E Celebi's fourier_0.8 routines
		int threshold;
		int ih, it;
		int first_bin;
		int last_bin;
		double term;
		double tot_ent; /* total entropy */
		double min_ent; /* max entropy */
		double ent_back; /* entropy of the background pixels at a given threshold */
		double ent_obj; /* entropy of the object pixels at a given threshold */
		double[] norm_histo = new double[histogram.length]; /* normalized histogram */
		double[] P1 = new double[histogram.length]; /* cumulative normalized histogram */
		double[] P2 = new double[histogram.length];

		int total = 0;
		for (ih = 0; ih < histogram.length; ih++)
			total += histogram[ih];

		for (ih = 0; ih < histogram.length; ih++)
			norm_histo[ih] = (double) histogram[ih] / total;

		P1[0] = norm_histo[0];
		P2[0] = 1.0 - P1[0];
		for (ih = 1; ih < histogram.length; ih++) {
			P1[ih] = P1[ih - 1] + norm_histo[ih];
			P2[ih] = 1.0 - P1[ih];
		}

		/* Determine the first non-zero bin */
		first_bin = 0;
		for (ih = 0; ih < histogram.length; ih++) {
			if (!(Math.abs(P1[ih]) < 2.220446049250313E-16)) {
				first_bin = ih;
				break;
			}
		}

		/* Determine the last non-zero bin */
		last_bin = histogram.length - 1;
		for (ih = histogram.length - 1; ih >= first_bin; ih--) {
			if (!(Math.abs(P2[ih]) < 2.220446049250313E-16)) {
				last_bin = ih;
				break;
			}
		}

		// Calculate the total entropy each gray-level
		// and find the threshold that maximizes it
		threshold = -1;
		min_ent = Double.MAX_VALUE;

		for (it = first_bin; it <= last_bin; it++) {
			/* Entropy of the background pixels */
			ent_back = 0.0;
			term = 0.5 / P1[it];
			for (ih = 1; ih <= it; ih++) { // 0+1?
				ent_back -= norm_histo[ih] * Math.log(1.0 - term * P1[ih - 1]);
			}
			ent_back *= term;

			/* Entropy of the object pixels */
			ent_obj = 0.0;
			term = 0.5 / P2[it];
			for (ih = it + 1; ih < histogram.length; ih++) {
				ent_obj -= norm_histo[ih] * Math.log(1.0 - term * P2[ih]);
			}
			ent_obj *= term;

			/* Total entropy */
			tot_ent = Math.abs(ent_back - ent_obj);

			if (tot_ent < min_ent) {
				min_ent = tot_ent;
				threshold = it;
			}
		}
		return threshold;
	}

}
