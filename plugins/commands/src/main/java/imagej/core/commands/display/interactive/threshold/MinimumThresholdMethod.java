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

import org.scijava.plugin.Plugin;

// NB - this plugin adapted from Gabriel Landini's code of his AutoThreshold
// plugin found in Fiji (version 1.14).

/**
 * Implements a minimum threshold method by Prewitt & Mendelsohn.
 * 
 * @author Barry DeZonia
 * @author Gabriel Landini
 */
@Plugin(type = AutoThresholdMethod.class, name = "Minimum")
public class MinimumThresholdMethod implements AutoThresholdMethod {

	private String errMsg;

	@Override
	public int getThreshold(long[] histogram) {
		if (histogram.length < 2) return 0;
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
		// Threshold t is such that ytâˆ’1 > yt â‰¤ yt+1.
		// Images with histograms having extremely unequal peaks or a broad and
		// ??at valley are unsuitable for this method.
		int iter = 0;
		int max = -1;
		double[] iHisto = new double[histogram.length];

		for (int i = 0; i < histogram.length; i++) {
			iHisto[i] = histogram[i];
			if (histogram[i] > 0) max = i;
		}
		double[] tHisto = iHisto;

		while (!Utils.bimodalTest(iHisto)) {
			// smooth with a 3 point running mean filter
			for (int i = 1; i < histogram.length - 1; i++)
				tHisto[i] = (iHisto[i - 1] + iHisto[i] + iHisto[i + 1]) / 3;
			// 0 outside
			tHisto[0] = (iHisto[0] + iHisto[1]) / 3;
			// 0 outside
			tHisto[histogram.length - 1] =
				(iHisto[histogram.length - 2] + iHisto[histogram.length - 1]) / 3;
			iHisto = tHisto;
			iter++;
			if (iter > 10000) {
				errMsg = "Minimum Threshold not found after 10000 iterations.";
				return -1;
			}
		}
		// The threshold is the minimum between the two peaks.
		// NB - BDZ updated code to match HistThresh 1.0.3 implementation
		double[] y = iHisto;
		boolean peakFound = false;
		for (int k = 1; k < max; k++) {
			// IJ.log(" "+i+"  "+iHisto[i]);
			if (y[k - 1] < y[k] && y[k + 1] < y[k]) peakFound = true;
			if (peakFound && y[k - 1] >= y[k] && y[k + 1] >= y[k]) return k;
		}
		return -1;
	}

	@Override
	public String getMessage() {
		return errMsg;
	}

}
