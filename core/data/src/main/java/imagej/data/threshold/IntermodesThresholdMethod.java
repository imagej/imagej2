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

// NB - this plugin adapted from Gabriel Landini's code of his AutoThreshold
// plugin found in Fiji (version 1.14).

/**
 * Implements an intermodes threshold method by Prewitt & Mendelsohn.
 * 
 * @author Barry DeZonia
 * @author Gabriel Landini
 */
@Plugin(type = AutoThresholdMethod.class, name = "Intermodes")
public class IntermodesThresholdMethod implements AutoThresholdMethod {

	private String errMsg = null;

	@Override
	public int getThreshold(long[] histogram) {
		// J. M. S. Prewitt and M. L. Mendelsohn, "The analysis of cell images," in
		// Annals of the New York Academy of Sciences, vol. 128, pp. 1035-1053,
		// 1966.
		// ported to ImageJ plugin by G.Landini from Antti Niemisto's Matlab code
		// (relicensed BSD 2-12-13)
		// Original Matlab code Copyright (C) 2004 Antti Niemisto
		// See http://www.cs.tut.fi/~ant/histthresh/ for an excellent slide
		// presentation and the original Matlab code.
		//
		// Assumes a bimodal histogram. The histogram needs is smoothed (using a
		// running average of size 3, iteratively) until there are only two local
		// maxima.
		// j and k
		// Threshold t is (j+k)/2.
		// Images with histograms having extremely unequal peaks or a broad and
		// ??at valley are unsuitable for this method.
		double[] iHisto = new double[histogram.length];
		int iter = 0;
		int threshold = -1;
		for (int i = 0; i < histogram.length; i++)
			iHisto[i] = histogram[i];

		while (!Utils.bimodalTest(iHisto)) {
			// smooth with a 3 point running mean filter
			double previous = 0, current = 0, next = iHisto[0];
			for (int i = 0; i < histogram.length - 1; i++) {
				previous = current;
				current = next;
				next = iHisto[i + 1];
				iHisto[i] = (previous + current + next) / 3;
			}
			iHisto[histogram.length - 1] = (current + next) / 3;
			iter++;
			if (iter > 10000) {
				errMsg = "Intermodes Threshold not found after 10000 iterations.";
				return -1;
			}
		}

		// The threshold is the mean between the two peaks.
		int tt = 0;
		for (int i = 1; i < histogram.length - 1; i++) {
			if (iHisto[i - 1] < iHisto[i] && iHisto[i + 1] < iHisto[i]) {
				tt += i;
				// IJ.log("mode:" +i);
			}
		}
		threshold = (int) Math.floor(tt / 2.0);
		return threshold;
	}

	@Override
	public String getMessage() {
		return errMsg;
	}
}
