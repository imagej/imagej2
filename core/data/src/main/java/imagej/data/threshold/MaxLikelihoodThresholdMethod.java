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

import net.imglib2.histogram.Histogram1d;

import org.scijava.plugin.Plugin;

// This plugin code ported from the original MatLab code of the max likelihood
// threshold method (th_maxlik) as written in Antti Niemisto's 1.03 version of
// the HistThresh Toolbox (relicensed BSD 2-12-13)

/**
 * Implements a maximum likelihood threshold method by Dempster, Laird, & Rubin
 * and Glasbey.
 * 
 * @author Barry DeZonia
 */
@Plugin(type = ThresholdMethod.class, name = "MaxLikelihood")
public class MaxLikelihoodThresholdMethod extends AbstractThresholdMethod {

	private static final int MAX_ATTEMPTS = 10000;

	private String errMessage;

	@Override
	public long getThreshold(Histogram1d<?> hist) {
		long[] histogram = hist.toLongArray();
		/*
			T =  th_maxlik(I,n)
			
			Find a global threshold for a grayscale image using the maximum likelihood
			via expectation maximization method.
			
			In:
			 I    grayscale image
			 n    maximum graylevel (defaults to 255)
			
			Out:
			 T    threshold
			
			References: 
			
			A. P. Dempster, N. M. Laird, and D. B. Rubin, "Maximum likelihood from
			incomplete data via the EM algorithm," Journal of the Royal Statistical
			Society, Series B, vol. 39, pp. 1-38, 1977.
			
			C. A. Glasbey, "An analysis of histogram-based thresholding algorithms,"
			CVGIP: Graphical Models and Image Processing, vol. 55, pp. 532-537, 1993.
			
			Copyright (C) 2004-2013 Antti Niemistˆ
			See README for more copyright information.
		*/

		final int n = histogram.length - 1;

		// I == the Image as double data

		// % Calculate the histogram.
		//y = hist(I(:),0:n);
		long[] y = histogram;

		// % The initial estimate for the threshold is found with the MINIMUM
		// % algorithm.
		MinimumThresholdMethod method = new MinimumThresholdMethod();
		int T = (int) method.getThreshold(hist);

		double eps = 0.0000001;

		//% Calculate initial values for the statistics.
		double mu = Utils.B(y,T)/Utils.A(y,T);
		double nu = (Utils.B(y,n)-Utils.B(y,T))/(Utils.A(y,n)-Utils.A(y,T));
		double p = Utils.A(y,T)/Utils.A(y,n);
		double q = (Utils.A(y,n)-Utils.A(y,T)) / Utils.A(y,n);
		double sigma2 = Utils.C(y,T)/Utils.A(y,T)-(mu*mu);
		double tau2 =
			(Utils.C(y, n) - Utils.C(y, T)) / (Utils.A(y, n) - Utils.A(y, T)) -
				(nu * nu);

		//% Return if sigma2 or tau2 are zero, to avoid division by zero
		if (sigma2 == 0 | tau2 == 0) return -1;

		double mu_prev = Double.NaN;
		double nu_prev = Double.NaN;
		double p_prev = Double.NaN;
		double q_prev = Double.NaN;
		double sigma2_prev = Double.NaN;
		double tau2_prev = Double.NaN;

		double[] ind = indices(n + 1);
		double[] ind2 = new double[n + 1];
		double[] phi = new double[n + 1];
		double[] gamma = new double[n + 1];
		double[] tmp1 = new double[n + 1];
		double[] tmp2 = new double[n + 1];
		double[] tmp3 = new double[n + 1];
		double[] tmp4 = new double[n + 1];

		sqr(ind, ind2);

		int attempts = 0;
		while (true) {
			if (attempts++ > MAX_ATTEMPTS) {
				errMessage =
					"Max likelihood method not converging after " + MAX_ATTEMPTS +
						" attempts.";
				return -1;
			}
		  for (int i = 0; i <= n; i++) {
		  	double dmu2 = (i-mu)*(i-mu);
		  	double dnu2 = (i-nu)*(i-nu);
		    phi[i] = p/Math.sqrt(sigma2) * Math.exp(-dmu2 / (2*sigma2)) /
		        (p/Math.sqrt(sigma2) * Math.exp(-dmu2 / (2*sigma2)) +
		         (q/Math.sqrt(tau2)) * Math.exp(-dnu2 / (2*tau2)));
		  }

			minus(1, phi, gamma);
		  double F = mul(phi,y);
		  double G = mul(gamma,y);
		  p_prev = p;
		  q_prev = q;
		  mu_prev = mu;
		  nu_prev = nu;
		  sigma2_prev = nu;
		  tau2_prev = nu;
			double Ayn = Utils.A(y, n);
			p = F / Ayn;
			q = G / Ayn;
			scale(ind, phi, tmp1);
			mu = mul(tmp1, y) / F;
			scale(ind, gamma, tmp2);
			nu = mul(tmp2, y) / G;
			scale(ind2, phi, tmp3);
			sigma2 = mul(tmp3, y) / F - (mu * mu);
			scale(ind2, gamma, tmp4);
			tau2 = mul(tmp4, y) / G - (nu * nu);

		  if (Math.abs(mu-mu_prev) < eps) break;
		  if (Math.abs(nu-nu_prev) < eps) break;
		  if (Math.abs(p-p_prev) < eps) break;
		  if (Math.abs(q-q_prev) < eps) break;
		  if (Math.abs(sigma2-sigma2_prev) < eps) break;
		  if (Math.abs(tau2-tau2_prev) < eps) break;
		}

		//% The terms of the quadratic equation to be solved.
		double w0 = 1/sigma2-1/tau2;
		double w1 = mu/sigma2-nu/tau2;
		double w2 =
			(mu * mu) / sigma2 - (nu * nu) / tau2 +
				Math.log10((sigma2 * (q * q)) / (tau2 * (p * p)));
		  
		//% If the threshold would be imaginary, return with threshold set to zero.
		double sqterm = w1*w1-w0*w2;
		if (sqterm < 0) {
			errMessage = "Max likelihood threshold would be imaginary";
			return -1;
		}

		//% The threshold is the integer part of the solution of the quadratic
		//% equation.
		return (int) Math.floor((w1+Math.sqrt(sqterm))/w0);
	}

	@Override
	public String getMessage() {
		return errMessage;
	}

	// does a single row*col multiplcation of a matrix multiply

	double mul(double[] row, long col[]) {
		if (row.length != col.length) throw new IllegalArgumentException(
			"row/col lengths differ");
		double sum = 0;
		for (int i = 0; i < row.length; i++) {
			sum += row[i] * col[i];
		}
		return sum;
	}

	void scale(double[] list1, double[] list2, double[] output) {
		if ((list1.length != list2.length) || (list1.length != output.length)) {
			throw new IllegalArgumentException("list lengths differ");
		}
		for (int i = 0; i < list1.length; i++)
			output[i] = list1[i] * list2[i];
	}

	void sqr(double[] in, double[] out) {
		if (in.length != out.length) {
			throw new IllegalArgumentException("list lengths differ");
		}
		for (int i = 0; i < in.length; i++)
			out[i] = in[i] * in[i];
	}

	double[] indices(int n) {
		double[] indices = new double[n];
		for (int i = 0; i < n; i++)
			indices[i] = i;
		return indices;
	}

	void minus(long num, double[] phi, double[] gamma) {
		if (phi.length != gamma.length) {
			throw new IllegalArgumentException("list lengths differ");
		}
		for (int i = 0; i < phi.length; i++) {
			gamma[i] = num - phi[i];
		}
	}

}
