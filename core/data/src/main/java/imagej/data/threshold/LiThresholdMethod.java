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
 * Implements Li's threshold method by Li & Lee, and Li & Tam, and Sezgin &
 * Sankur.
 * 
 * @author Barry DeZonia
 * @author Gabriel Landini
 */
@Plugin(type = AutoThresholdMethod.class, name = "Li")
public class LiThresholdMethod implements AutoThresholdMethod {

	@Override
	public int getThreshold(long[] histogram) {
		// Implements Li's Minimum Cross Entropy thresholding method
		// This implementation is based on the iterative version (Ref. 2) of the
		// algorithm
		// 1) Li C.H. and Lee C.K. (1993) "Minimum Cross Entropy Thresholding" 
		//    Pattern Recognition, 26(4): 617-625
		// 2) Li C.H. and Tam P.K.S. (1998) "An Iterative Algorithm for Minimum 
		//    Cross Entropy Thresholding"Pattern Recognition Letters, 18(8): 771-776
		// 3) Sezgin M. and Sankur B. (2004) "Survey over Image Thresholding 
		//    Techniques and Quantitative Performance Evaluation" Journal of 
		//    Electronic Imaging, 13(1): 146-165 
		//    http://citeseer.ist.psu.edu/sezgin04survey.html
		// Ported to ImageJ plugin by G.Landini from E Celebi's fourier_0.8 routines
		int threshold;
		int ih;
		int num_pixels;
		int sum_back; /* sum of the background pixels at a given threshold */
		int sum_obj;  /* sum of the object pixels at a given threshold */
		int num_back; /* number of background pixels at a given threshold */
		int num_obj;  /* number of object pixels at a given threshold */
		double old_thresh;
		double new_thresh;
		double mean_back; /* mean of the background pixels at a given threshold */
		double mean_obj;  /* mean of the object pixels at a given threshold */
		double mean;  /* mean gray-level in the image */
		double tolerance; /* threshold tolerance */
		double temp;

		tolerance=0.5;
		num_pixels = 0;
		for (ih = 0; ih < histogram.length; ih++ )
			num_pixels += histogram[ih];

		/* Calculate the mean gray-level */
		mean = 0.0;
		for ( ih = 0; ih < histogram.length; ih++ ) //0 + 1?
			mean += ih * histogram[ih];
		mean /= num_pixels;
		/* Initial estimate */
		new_thresh = mean;

		do{
			old_thresh = new_thresh;
			threshold = (int) (old_thresh + 0.5);	/* range */
			/* Calculate the means of background and object pixels */
			/* Background */
			sum_back = 0;
			num_back = 0;
			for ( ih = 0; ih <= threshold; ih++ ) {
				sum_back += ih * histogram[ih];
				num_back += histogram[ih];
			}
			mean_back = ( num_back == 0 ? 0.0 : ( sum_back / ( double ) num_back ) );
			/* Object */
			sum_obj = 0;
			num_obj = 0;
			for ( ih = threshold + 1; ih < histogram.length; ih++ ) {
				sum_obj += ih * histogram[ih];
				num_obj += histogram[ih];
			}
			mean_obj = ( num_obj == 0 ? 0.0 : ( sum_obj / ( double ) num_obj ) );

			/* Calculate the new threshold: Equation (7) in Ref. 2 */
			//new_thresh = simple_round ( ( mean_back - mean_obj ) / ( Math.log ( mean_back ) - Math.log ( mean_obj ) ) );
			//simple_round ( double x ) {
			// return ( int ) ( IS_NEG ( x ) ? x - .5 : x + .5 );
			//}
			//
			//#define IS_NEG( x ) ( ( x ) < -DBL_EPSILON ) 
			//DBL_EPSILON = 2.220446049250313E-16
			temp = ( mean_back - mean_obj ) / ( Math.log ( mean_back ) - Math.log ( mean_obj ) );

			if (temp < -2.220446049250313E-16)
				new_thresh = (int) (temp - 0.5);
			else
				new_thresh = (int) (temp + 0.5);
			/*  Stop the iterations when the difference between the
			new and old threshold values is less than the tolerance */
		}
		while ( Math.abs ( new_thresh - old_thresh ) > tolerance );
		return threshold;
	}

	@Override
	public String getMessage() {
		return null;
	}
}
