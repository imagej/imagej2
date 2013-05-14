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
 * Implements a Triangle algorithm threshold method from Zack, Rogers, & Latt.
 * 
 * @author Barry DeZonia
 * @author Gabriel Landini
 */
@Plugin(type = ThresholdMethod.class, name = "Triangle")
public class TriangleThresholdMethod extends AbstractThresholdMethod {

	@Override
	public int getThreshold(long[] histogram) {
		// Zack, G. W., Rogers, W. E. and Latt, S. A., 1977,
		// Automatic Measurement of Sister Chromatid Exchange Frequency,
		// Journal of Histochemistry and Cytochemistry 25 (7), pp. 741-753
		//
		// modified from Johannes Schindelin plugin
		//
		// find min and max
		int min = 0, max = 0, min2 = 0;
		long dmax = 0;
		for (int i = 0; i < histogram.length; i++) {
			if (histogram[i] > 0) {
				min = i;
				break;
			}
		}
		if (min > 0) min--; // line to the (p==0) point, not to histogram[min]

		// The Triangle algorithm cannot tell whether the data is skewed to one side
		// or another. This causes a problem as there are 2 possible thresholds
		// between the max and the 2 extremes of the histogram. Here I propose to
		// find out to which side of the max point the data is furthest, and use
		// that as the other extreme.
		for (int i = histogram.length - 1; i > 0; i--) {
			if (histogram[i] > 0) {
				min2 = i;
				break;
			}
		}
		// line to the (p==0) point, not to histogram[min]
		if (min2 < histogram.length - 1) min2++;

		for (int i = 0; i < histogram.length; i++) {
			if (histogram[i] > dmax) {
				max = i;
				dmax = histogram[i];
			}
		}
		// find which is the furthest side
		// IJ.log(""+min+" "+max+" "+min2);
		boolean inverted = false;
		if ((max - min) < (min2 - max)) {
			// reverse the histogram
			// IJ.log("Reversing histogram.");
			inverted = true;
			int left = 0; // index of leftmost element
			int right = histogram.length - 1; // index of rightmost element
			while (left < right) {
				// exchange the left and right elements
				long temp = histogram[left];
				histogram[left] = histogram[right];
				histogram[right] = temp;
				// move the bounds toward the center
				left++;
				right--;
			}
			min = histogram.length - 1 - min2;
			max = histogram.length - 1 - max;
		}

		if (min == max) {
			// IJ.log("Triangle:  min == max.");
			return min;
		}

		// describe line by nx * x + ny * y - d = 0
		double nx, ny, d;
		// nx is just the max frequency as the other point has freq=0
		// lowest value bmin = (p=0)% in the image
		nx = histogram[max]; // -min; // histogram[min];
		ny = min - max;
		d = Math.sqrt(nx * nx + ny * ny);
		nx /= d;
		ny /= d;
		d = nx * min + ny * histogram[min];

		// find split point
		int split = min;
		double splitDistance = 0;
		for (int i = min + 1; i <= max; i++) {
			double newDistance = nx * i + ny * histogram[i] - d;
			if (newDistance > splitDistance) {
				split = i;
				splitDistance = newDistance;
			}
		}
		split--;

		if (inverted) {
			// The histogram might be used for something else, so let's reverse it
			// back
			int left = 0;
			int right = histogram.length - 1;
			while (left < right) {
				long temp = histogram[left];
				histogram[left] = histogram[right];
				histogram[right] = temp;
				left++;
				right--;
			}
			return (histogram.length - 1 - split);
		}
		return split;
	}

	@Override
	public String getMessage() {
		return null;
	}

}
