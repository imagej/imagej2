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

// NB - this plugin adapted from Gabriel Landini's code of his AutoThreshold
// plugin found in Fiji (version 1.14).

/**
 * Implements an IsoData (intermeans) threshold method by Ridler & Calvard.
 * 
 * @author Barry DeZonia
 * @author Gabriel Landini
 */
@Plugin(type = ThresholdMethod.class, name = "IsoData")
public class IsoDataThresholdMethod extends AbstractThresholdMethod {

	private String errMsg = null;

	@Override
	public long getThreshold(Histogram1d<?> hist) {
		long[] histogram = hist.toLongArray();
		// Also called intermeans
		// Iterative procedure based on the isodata algorithm [T.W. Ridler,
		// S. Calvard, Picture thresholding using an iterative selection method,
		// IEEE Trans. System, Man and Cybernetics, SMC-8 (1978) 630-632.]
		// The procedure divides the image into objects and background by taking an
		// initial threshold, then the averages of the pixels at or below the
		// threshold and pixels above are computed. The averages of those two values
		// are computed, the threshold is incremented and the process is repeated
		// until the threshold is larger than the composite average. That is,
		// threshold = (average background + average objects)/2
		// The code in ImageJ that implements this function is the
		// getAutoThreshold() method in the ImageProcessor class.
		//
		// From: Tim Morris (dtm@ap.co.umist.ac.uk)
		// Subject: Re: Thresholding method?
		// posted to sci.image.processing on 1996/06/24
		// The algorithm implemented in NIH Image sets the threshold as that grey
		// value, G, for which the average of the averages of the grey values
		// below and above G is equal to G. It does this by initialising G to the
		// lowest sensible value and iterating:

		// L = the average grey value of pixels with intensities < G
		// H = the average grey value of pixels with intensities > G
		// is G = (L + H)/2?
		// yes => exit
		// no => increment G and repeat
		//
		// There is a discrepancy with IJ because of slightly different methods
		long l, toth, totl, h;
		int i, g = 0;
		for (i = 1; i < histogram.length; i++) {
			if (histogram[i] > 0) {
				g = i + 1;
				break;
			}
		}
		while (true) {
			l = 0;
			totl = 0;
			for (i = 0; i < g; i++) {
				totl = totl + histogram[i];
				l = l + (histogram[i] * i);
			}
			h = 0;
			toth = 0;
			for (i = g + 1; i < histogram.length; i++) {
				toth += histogram[i];
				h += (histogram[i] * i);
			}
			if (totl > 0 && toth > 0) {
				l /= totl;
				h /= toth;
				if (g == (int) Math.round((l + h) / 2.0)) break;
			}
			g++;
			if (g > histogram.length - 2) {
				errMsg = "IsoData Threshold not found.";
				return -1;
			}
		}
		return g;
	}

	@Override
	public String getMessage() {
		return errMsg;
	}

}
