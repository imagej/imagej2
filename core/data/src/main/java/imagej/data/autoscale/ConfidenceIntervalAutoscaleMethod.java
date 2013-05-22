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

package imagej.data.autoscale;

import net.imglib2.IterableInterval;
import net.imglib2.algorithm.histogram.Histogram1d;
import net.imglib2.algorithm.histogram.Real1dBinMapper;
import net.imglib2.ops.util.Tuple2;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.real.DoubleType;

import org.scijava.plugin.Plugin;

/**
 * Computes a 95% confidence interval from the entire set of values in an
 * {@link IterableInterval}.
 * 
 * @author bdezonia
 */
@Plugin(type = AutoscaleMethod.class, name = "95% CI")
public class ConfidenceIntervalAutoscaleMethod extends AbstractAutoscaleMethod {

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public Tuple2<Double, Double> getRange(IterableInterval<RealType> interval) {
		// pass one through data
		AutoscaleService service = getContext().getService(AutoscaleService.class);
		Tuple2<Double, Double> range = service.getDefaultIntervalRange(interval);
		// pass two through data
		Real1dBinMapper mapper =
			new Real1dBinMapper(range.get1(), range.get2(), 1000, false);
		Histogram1d<RealType> histogram = new Histogram1d<RealType>(mapper);
		histogram.countData(interval);
		// determine bin number containing > 2.5%
		long totValues = histogram.dfd().totalValues();
		long twoPtFivePercent = (long) Math.floor(0.025 * totValues);
		long soFar = 0;
		int bottom = 0;
		while (soFar < twoPtFivePercent) {
			soFar += histogram.frequency(bottom++);
		}
		// determine bin number containing < 97.5%
		soFar = 0;
		int top = 999;
		while (soFar < twoPtFivePercent) {
			soFar += histogram.frequency(top--);
		}
		// determine approx boundaries
		DoubleType approxMin = new DoubleType();
		DoubleType approxMax = new DoubleType();
		histogram.getLowerBound(bottom, approxMin);
		histogram.getUpperBound(top, approxMax);
		double min = approxMin.getRealDouble();
		double max = approxMax.getRealDouble();
		// return them
		return new Tuple2<Double, Double>(min, max);
	}

}
