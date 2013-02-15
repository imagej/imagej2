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

// Ported from Antti Niemesto's HistThresh Matlab toolbox
//   Relicensed BSD 2-12-13

/**
 * TODO
 *
 * @author Barry DeZonia
 * @author Gabriel Landini
 */
public class Utils {

	public static boolean bimodalTest(double[] y) {
		int len = y.length;
		int modes = 0;

		for (int k = 1; k < len - 1; k++) {
			if (y[k - 1] < y[k] && y[k + 1] < y[k]) {
				modes++;
				if (modes > 2) return false;
			}
		}
		return (modes == 2);
	}

	/**
	 * The partial sum A from C. A. Glasbey, "An analysis of histogram-based
	 * thresholding algorithms," CVGIP: Graphical Models and Image Processing,
	 * vol. 55, pp. 532-537, 1993.
	 */
	public static double A(long[] y, int j) {
		double x = 0;
		for (int i = 0; i <= j; i++)
			x += y[i];
		return x;
	}

	/**
	 * The partial sum B from C. A. Glasbey, "An analysis of histogram-based
	 * thresholding algorithms," CVGIP: Graphical Models and Image Processing,
	 * vol. 55, pp. 532-537, 1993.
	 */
	public static double B(long[] y, int j) {
		double x = 0;
		for (int i = 0; i <= j; i++)
			x += y[i] * i;
		return x;
	}

	/**
	 * The partial sum C from C. A. Glasbey, "An analysis of histogram-based
	 * thresholding algorithms," CVGIP: Graphical Models and Image Processing,
	 * vol. 55, pp. 532-537, 1993.
	 */
	public static double C(long[] y, int j) {
		double x = 0;
		for (int i = 0; i <= j; i++)
			x += y[i] * i * i;
		return x;
	}

	/**
	 * The partial sum D from C. A. Glasbey, "An analysis of histogram-based
	 * thresholding algorithms," CVGIP: Graphical Models and Image Processing,
	 * vol. 55, pp. 532-537, 1993.
	 */
	public static double D(long[] y, int j) {
		double x = 0;
		for (int i = 0; i <= j; i++)
			x += y[i] * i * i * i;
		return x;
	}
}
