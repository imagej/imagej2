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

package imagej.data.autoscale;


/**
 * DataRange allows one to construct a real interval between two real values.
 * One can query measures like min, max, or extent.
 * 
 * @author Barry DeZonia
 */
public class DataRange {

	// -- instance variables --

	private final double min, max;

	// -- DataRange methods --

	/**
	 * Constructs a data range given two end points of an interval. Order of
	 * points is unimportant.
	 * 
	 * @param p1 One end point of the data range.
	 * @param p2 The other end point of the data range.
	 */
	public DataRange(double p1, double p2) {
		min = Math.min(p1, p2);
		max = Math.max(p1, p2);
	}

	/**
	 * Returns the smallest end point of the data range.
	 */
	public double getMin() {
		return min;
	}

	/**
	 * Returns the largest end point of the data range.
	 */
	public double getMax() {
		return max;
	}

	/**
	 * Returns the extent of the data range (max - min).
	 */
	public double getExtent() {
		return max - min;
	}
}
