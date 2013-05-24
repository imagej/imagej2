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

import org.scijava.plugin.Plugin;

/**
 * Computes a confidence interval containing percentages of the an entire set of
 * values in an {@link IterableInterval}.
 * 
 * @author Barry DeZonia
 */
@Plugin(type = AutoscaleMethod.class, name = "Confidence Interval")
public class ConfidenceIntervalAutoscaleMethod<T extends RealType<T>> extends
	AbstractAutoscaleMethod<T>
{

	// -- instance variables --

	private double lowerTail, upperTail;

	// -- ConfidenceIntervalAutoscaleMethod methods --

	/**
	 * Construct a confidence interval that contains 95% of the data range.
	 */
	public ConfidenceIntervalAutoscaleMethod() {
		this(0.025, 0.025); // fit 95% of the data range
	}

	/**
	 * Construct a confidence interval with user specified percentages of the data
	 * range. Ranges are specified as fractions of 1. They must sum to less than
	 * 1.
	 * 
	 * @param lowerTailProportion The proportion of the distribution to be treated
	 *          as lower tail values
	 * @param upperTailProportion The proportion of the distribution to be treated
	 *          as upper tail values
	 */
	public ConfidenceIntervalAutoscaleMethod(double lowerTailProportion,
		double upperTailProportion)
	{
		setTailProportions(lowerTailProportion, upperTailProportion);
	}

	/**
	 * Returns the fractional proportion of the distribution to count as in lower
	 * tail.
	 */
	public double getLowerTailProportion() {
		return lowerTail;
	}

	/**
	 * Returns the fractional proportion of the distribution to count as in upper
	 * tail.
	 */
	public double getUpperTailProportion() {
		return upperTail;
	}

	/**
	 * Sets the fractional proportions of the distribution.
	 * 
	 * @param lower A fraction of 1 specifying the size of the lower tail
	 * @param upper A fraction of 1 specifying the size of the upper tail
	 */
	public void setTailProportions(double lower, double upper) {
		if (lower < 0 || lower > 1) {
			throw new IllegalArgumentException(
				"lower tail fraction must be between 0 and 1");
		}
		if (upper < 0 || upper > 1) {
			throw new IllegalArgumentException(
				"upper tail fraction must be between 0 and 1");
		}
		if (lower + upper >= 1) {
			throw new IllegalArgumentException("tails must not span whole data range");
		}
		lowerTail = lower;
		upperTail = upper;
	}

	// -- AutoscaleMethod methods --

	@Override
	public Tuple2<Double, Double> getRange(IterableInterval<T> interval) {
		// pass one through data
		AutoscaleService service = getContext().getService(AutoscaleService.class);
		Tuple2<Double, Double> range = service.getDefaultIntervalRange(interval);
		// pass two through data
		Real1dBinMapper<T> mapper =
			new Real1dBinMapper<T>(range.get1(), range.get2(), 1000, false);
		Histogram1d<T> histogram = new Histogram1d<T>(mapper);
		histogram.countData(interval);
		// calc some sizes
		long totValues = histogram.dfd().totalValues();
		long lowerSize = (long) Math.floor(lowerTail * totValues);
		long upperSize = (long) Math.floor(upperTail * totValues);
		// determine bin number containing > than lower tail size
		long soFar = 0;
		int bottom = 0;
		while (soFar < lowerSize) {
			soFar += histogram.frequency(bottom++);
		}
		while (histogram.frequency(bottom) == 0) {
			bottom++;
		}
		// determine bin number containing < upper tail size
		soFar = 0;
		int top = 999;
		while (soFar < upperSize) {
			soFar += histogram.frequency(top--);
		}
		while (histogram.frequency(top) == 0) {
			top--;
		}
		// determine approx boundaries
		T approxMin = interval.firstElement().createVariable();
		T approxMax = approxMin.createVariable();
		histogram.getLowerBound(bottom, approxMin);
		histogram.getUpperBound(top, approxMax);
		double min = approxMin.getRealDouble();
		double max = approxMax.getRealDouble();
		// return them
		return new Tuple2<Double, Double>(min, max);
	}

}
