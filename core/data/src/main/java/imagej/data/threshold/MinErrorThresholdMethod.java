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
 * Implements a minimum error threshold method by Kittler & Illingworth and
 * Glasbey.
 * 
 * @author Barry DeZonia
 * @author Gabriel Landini
 */
@Plugin(type = ThresholdMethod.class, name = "MinError(I)")
public class MinErrorThresholdMethod extends AbstractThresholdMethod {

	private String errMsg = null;

	@Override
	public long getThreshold(Histogram1d<?> hist) {
		long[] histogram = hist.toLongArray();
		// Kittler and J. Illingworth, "Minimum error thresholding," Pattern
		// Recognition, vol. 19, pp. 41-47, 1986.
		// C. A. Glasbey, "An analysis of histogram-based thresholding algorithms,"
		// CVGIP: Graphical Models and Image Processing, vol. 55, pp. 532-537, 1993.
		// Ported to ImageJ plugin by G.Landini from Antti Niemisto's Matlab code
		// (relicensed BSD 2-12-13)
		// Original Matlab code Copyright (C) 2004 Antti Niemisto
		// See http://www.cs.tut.fi/~ant/histthresh/ for an excellent slide
		// presentation and the original Matlab code.

		// Initial estimate for the threshold is found with the MEAN algorithm.
		int threshold = (int) new MeanThresholdMethod().getThreshold(hist);
		int Tprev = -2;
		double mu, nu, p, q, sigma2, tau2, w0, w1, w2, sqterm, temp;
		// int counter=1;
		while (threshold != Tprev) {
			// Calculate some statistics.
			mu = Utils.B(histogram, threshold) / Utils.A(histogram, threshold);
			nu =
				(Utils.B(histogram, histogram.length - 1) - Utils.B(histogram,
					threshold)) /
					(Utils.A(histogram, histogram.length - 1) - Utils.A(histogram,
						threshold));
			p =
				Utils.A(histogram, threshold) /
					Utils.A(histogram, histogram.length - 1);
			q =
				(Utils.A(histogram, histogram.length - 1) - Utils.A(histogram,
					threshold)) /
					Utils.A(histogram, histogram.length - 1);
			sigma2 =
				Utils.C(histogram, threshold) / Utils.A(histogram, threshold) -
					(mu * mu);
			tau2 =
				(Utils.C(histogram, histogram.length - 1) - Utils.C(histogram,
					threshold)) /
					(Utils.A(histogram, histogram.length - 1) - Utils.A(histogram,
						threshold)) - (nu * nu);

			// The terms of the quadratic equation to be solved.
			w0 = 1.0 / sigma2 - 1.0 / tau2;
			w1 = mu / sigma2 - nu / tau2;
			w2 =
				(mu * mu) / sigma2 - (nu * nu) / tau2 +
					Math.log10((sigma2 * (q * q)) / (tau2 * (p * p)));

			// If the next threshold would be imaginary, return with the current one.
			sqterm = (w1 * w1) - w0 * w2;
			if (sqterm < 0) {
				errMsg =
					"MinError(I): not converging. Try \'Ignore black/white\' options";
				return threshold;
			}

			// The updated threshold is the integer part of the solution of the
			// quadratic equation.
			Tprev = threshold;
			temp = (w1 + Math.sqrt(sqterm)) / w0;

			if (Double.isNaN(temp)) {
				errMsg =
					"MinError(I): NaN, not converging. Try \'Ignore black/white\' options";
				threshold = Tprev;
			}
			else threshold = (int) Math.floor(temp);
			// IJ.log("Iter: "+ counter+++"  t:"+threshold);
		}
		return threshold;
	}

	@Override
	public String getMessage() {
		return errMsg;
	}

}
