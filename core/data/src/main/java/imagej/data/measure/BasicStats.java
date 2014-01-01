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
 * 
 * The views and conclusions contained in the software and documentation are
 * those of the authors and should not be interpreted as representing official
 * policies, either expressed or implied, of any organization.
 * #L%
 */

package imagej.data.measure;

// TODO - add skew, kurtosis, etc. Optionally add flags that specify which ones
// to calculate and only calc those of interest to speed computation time.

/**
 * A class that packages together a set of basic statistics such as sample mean,
 * sample variance, etc.
 * 
 * @author Barry DeZonia
 *
 */
public class BasicStats {
	
	// -- instance variables --
	
	/** sample mean */
	private double xbar;
	
	/** biased estimate of standard deviation */
	private double sn;
	
	/** unbiased estimate of standard deviation */
	private double sn1;
	
	/** biased estimate of variance */
	private double s2n;
	
	/** unbiased estimate of variance */
	private double s2n1;
	
	// -- constructor --
	
	/** Default constructor */
	public BasicStats() { }
	
	// -- BasicStats public methods --
	
	/** Returns the sample mean. */
	public double getXBar() { return xbar; }
	
	/**
	 * Returns the (biased) estimate of the sample standard deviation. It equals
	 * the square root of the (biased) estimate of the sample variance.
	 */
	public double getSn() { return sn; }
	
	/**
	 * Returns the (unbiased) estimate of the sample standard deviation. It equals
	 * the square root of the (unbiased) estimate of the sample variance.
	 */
	public double getSn1() { return sn1; }

	/**
	 * Returns the (biased) estimate of the sample variance. It equals
	 * the sum of squared deviations divided by N (N = number of samples).
	 */
	public double getS2n() { return s2n; }
	
	/**
	 * Returns the (unbiased) estimate of the sample variance. It equals
	 * the sum of squared deviations divided by N-1 (N = number of samples).
	 */
	public double getS2n1() { return s2n1; }

	/**
	 * Calculates the statistics from a sample and records them for later
	 * retrieval via the public getters of this class.
	 * 
	 * @param data
	 * The set of values in the sample of the population.
	 */
	public void calcStats(double[] data) {
		// Reference: MathWorld.com
		int n = data.length;
		double sum;
		sum = 0;
		for (int i = 0; i < data.length; i++) {
			sum += data[i];
		}
		xbar = (n == 0) ? 0 : sum / n;
		sum = 0;
		for (int i = 0; i < data.length; i++) {
			double dev = data[i] - xbar;
			sum += dev * dev;
		}
		s2n = (n <= 0) ? 0 : sum / n;
		s2n1 = (n <= 1) ? 0 : sum / (n-1);
		sn = Math.sqrt(s2n);
		sn1 = Math.sqrt(s2n1);
	}
	
	/** Creates a new BasicStats object */
	public BasicStats create() { return new BasicStats(); }
}
