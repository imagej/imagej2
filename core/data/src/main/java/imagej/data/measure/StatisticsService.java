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

package imagej.data.measure;

import imagej.data.Dataset;
import imagej.service.IJService;
import net.imglib2.ops.pointset.PointSet;

// TODO - make MeasurementService smarter. Compute values without always
// revisiting the pixels. This current impl goes over pixels once for each
// measurement. I will mock something up soon. BDZ

// TODO - this service is limited to only the kinds of stats it knows. It can't
// measure a user provided statistic. This can be done by calling the
// MeasurementService more directly.
// Also this service calcs one measure at a time. Our future MeasurementService
// should be able to batch them together. It also might be nice to be able to
// specify to this service gather min,var,stdev,median in an array of output
// doubles.

// TODO - see what IJ1 calculates and provide such methods here and in OPS

// NOTE - this could be done down in OPS with less trouble maybe

/**
 * A service for computing basic statistics upon regions of {@link Dataset}s.
 * 
 * @author Barry DeZonia
 */
public interface StatisticsService extends IJService {

	// -- StatisticsService methods --

	/**
	 * Returns an estimate of the alpha trimmed mean of the values within a
	 * {@link PointSet} region of a {@link Dataset}. Alpha must range be >= 0 and
	 * < 0.5.
	 * 
	 * @param ds The Dataset to measure
	 * @param region The PointSet region upon which to calculate
	 * @param alpha The proportion of values to trim from each end of the set of
	 *          samples collected
	 * @return The measured value
	 */
	double alphaTrimmedMean(Dataset ds, PointSet region, double alpha);

	/**
	 * Returns an estimate of the alpha trimmed mean of the values of a
	 * {@link Dataset}. Alpha must range be >= 0 and < 0.5.
	 * 
	 * @param ds The Dataset to measure
	 * @param alpha The proportion of values to trim from each end of the set of
	 *          samples collected
	 * @return The measured value
	 */
	double alphaTrimmedMean(Dataset ds, double alpha);

	/**
	 * Returns an estimate of the arithmetic mean of the values within a
	 * {@link PointSet} region of a {@link Dataset}
	 * 
	 * @param ds The Dataset to measure
	 * @param region The PointSet region upon which to calculate
	 * @return The measured value
	 */
	double arithmeticMean(Dataset ds, PointSet region);

	/**
	 * Returns an estimate of the arithmetic mean of the values of a
	 * {@link Dataset}
	 * 
	 * @param ds The Dataset to measure
	 * @return The measured value
	 */
	double arithmeticMean(Dataset ds);

	/**
	 * Returns an estimate of the contraharmonic mean of the values within a
	 * {@link PointSet} region of a {@link Dataset}
	 * 
	 * @param ds The Dataset to measure
	 * @param region The PointSet region upon which to calculate
	 * @param order The desired order of the contraharmonic mean
	 * @return The measured value
	 */
	double contraharmomicMean(Dataset ds, PointSet region, double order);

	/**
	 * Returns an estimate of the contraharmonic mean of the values within a
	 * {@link Dataset}
	 * 
	 * @param ds The Dataset to measure
	 * @param order The desired order of the contraharmonic mean
	 * @return The measured value
	 */
	double contraharmomicMean(Dataset ds, double order);

	/**
	 * Returns an estimate of the geometric mean of the values within a
	 * {@link PointSet} region of a {@link Dataset}
	 * 
	 * @param ds The Dataset to measure
	 * @param region The PointSet region upon which to calculate
	 * @return The measured value
	 */
	double geometricMean(Dataset ds, PointSet region);

	/**
	 * Returns an estimate of the geometric mean of the values within a
	 * {@link Dataset}
	 * 
	 * @param ds The Dataset to measure
	 * @return The measured value
	 */
	double geometricMean(Dataset ds);

	/**
	 * Returns an estimate of the harmonic mean of the values within a
	 * {@link PointSet} region of a {@link Dataset}
	 * 
	 * @param ds The Dataset to measure
	 * @param region The PointSet region upon which to calculate
	 * @return The measured value
	 */
	double harmonicMean(Dataset ds, PointSet region);

	/**
	 * Returns an estimate of the harmonic mean of the values within a
	 * {@link Dataset}
	 * 
	 * @param ds The Dataset to measure
	 * @return The measured value
	 */
	double harmonicMean(Dataset ds);

	/**
	 * Returns the maximum value from the set of values within a {@link PointSet}
	 * region of a {@link Dataset}
	 * 
	 * @param ds The Dataset to measure
	 * @param region The PointSet region upon which to calculate
	 * @return The measured value
	 */
	double maximum(Dataset ds, PointSet region);

	/**
	 * Returns the maximum value from the set of values within a {@link Dataset}
	 * 
	 * @param ds The Dataset to measure
	 * @return The measured value
	 */
	double maximum(Dataset ds);

	/**
	 * Returns the median value from the set of values within a {@link PointSet}
	 * region of a {@link Dataset}
	 * 
	 * @param ds The Dataset to measure
	 * @param region The PointSet region upon which to calculate
	 * @return The measured value
	 */
	double median(Dataset ds, PointSet region);

	/**
	 * Returns the median value from the set of values within a {@link Dataset}
	 * 
	 * @param ds The Dataset to measure
	 * @return The measured value
	 */
	double median(Dataset ds);

	/**
	 * Returns the point midway between the minimum value and the maximum value
	 * from the set of values within a {@link PointSet} region of a
	 * {@link Dataset}
	 * 
	 * @param ds The Dataset to measure
	 * @param region The PointSet region upon which to calculate
	 * @return The measured value
	 */
	double midpoint(Dataset ds, PointSet region);

	/**
	 * Returns the point midway between the minimum value and the maximum value
	 * from the set of values within a {@link Dataset}
	 * 
	 * @param ds The Dataset to measure
	 * @return The measured value
	 */
	double midpoint(Dataset ds);

	/**
	 * Returns the minimum value from the set of values within a {@link PointSet}
	 * region of a {@link Dataset}
	 * 
	 * @param ds The Dataset to measure
	 * @param region The PointSet region upon which to calculate
	 * @return The measured value
	 */
	double minimum(Dataset ds, PointSet region);

	/**
	 * Returns the minimum value from the set of values within a {@link Dataset}
	 * 
	 * @param ds The Dataset to measure
	 * @return The measured value
	 */
	double minimum(Dataset ds);

	/**
	 * Returns the (biased) kurtosis of all the values within a {@link PointSet}
	 * region of a {@link Dataset}
	 * 
	 * @param ds The Dataset to measure
	 * @param region The PointSet region upon which to calculate
	 * @return The measured value
	 */
	double populationKurtosis(Dataset ds, PointSet region);

	/**
	 * Returns the (biased) kurtosis of all the values within a {@link Dataset}
	 * 
	 * @param ds The Dataset to measure
	 * @return The measured value
	 */
	double populationKurtosis(Dataset ds);

	/**
	 * Returns the (biased) kurtosis excess of all the values within a
	 * {@link PointSet} region of a {@link Dataset}
	 * 
	 * @param ds The Dataset to measure
	 * @param region The PointSet region upon which to calculate
	 * @return The measured value
	 */
	double populationKurtosisExcess(Dataset ds, PointSet region);

	/**
	 * Returns the (biased) kurtosis excess of all the values within a
	 * {@link Dataset}
	 * 
	 * @param ds The Dataset to measure
	 * @return The measured value
	 */
	double populationKurtosisExcess(Dataset ds);

	/**
	 * Returns the (biased) skew of all the values within a {@link PointSet}
	 * region of a {@link Dataset}
	 * 
	 * @param ds The Dataset to measure
	 * @param region The PointSet region upon which to calculate
	 * @return The measured value
	 */
	double populationSkew(Dataset ds, PointSet region);

	/**
	 * Returns the (biased) skew of all the values within a {@link Dataset}
	 * 
	 * @param ds The Dataset to measure
	 * @return The measured value
	 */
	double populationSkew(Dataset ds);

	/**
	 * Returns the (biased) estimate of the sample standard deviation of the
	 * values within a {@link PointSet} region of a {@link Dataset}. It equals the
	 * square root of the (biased) estimate of the sample variance.
	 * 
	 * @param ds The Dataset to measure
	 * @param region The PointSet region upon which to calculate
	 * @return The measured value
	 */
	double populationStdDev(Dataset ds, PointSet region);

	/**
	 * Returns the (biased) estimate of the sample standard deviation of the
	 * values within a {@link Dataset}. It equals the square root of the (biased)
	 * estimate of the sample variance.
	 * 
	 * @param ds The Dataset to measure
	 * @return The measured value
	 */
	double populationStdDev(Dataset ds);

	/**
	 * Returns the (biased) estimate of the sample variance of the values within a
	 * {@link PointSet} region of a {@link Dataset}. It divides the sum of squared
	 * deviations from the mean by N (N == number of samples).
	 * 
	 * @param ds The Dataset to measure
	 * @param region The PointSet region upon which to calculate
	 * @return The measured value
	 */
	double populationVariance(Dataset ds, PointSet region);

	/**
	 * Returns the (biased) estimate of the sample variance of the values within a
	 * {@link Dataset}. It divides the sum of squared deviations from the mean by
	 * N (N == number of samples).
	 * 
	 * @param ds The Dataset to measure
	 * @return The measured value
	 */
	double populationVariance(Dataset ds);

	/**
	 * Returns the product of all the values within a {@link PointSet} region of a
	 * {@link Dataset}
	 * 
	 * @param ds The Dataset to measure
	 * @param region The PointSet region upon which to calculate
	 * @return The measured value
	 */
	double product(Dataset ds, PointSet region);

	/**
	 * Returns the product of all the values within a {@link Dataset}
	 * 
	 * @param ds The Dataset to measure
	 * @return The measured value
	 */
	double product(Dataset ds);

	/**
	 * Returns the (unbiased) kurtosis of all the values within a {@link PointSet}
	 * region of a {@link Dataset}
	 * 
	 * @param ds The Dataset to measure
	 * @param region The PointSet region upon which to calculate
	 * @return The measured value
	 */
	double sampleKurtosis(Dataset ds, PointSet region);

	/**
	 * Returns the (unbiased) kurtosis of all the values within a {@link Dataset}
	 * 
	 * @param ds The Dataset to measure
	 * @return The measured value
	 */
	double sampleKurtosis(Dataset ds);

	/**
	 * Returns the (unbiased) kurtosis excess of all the values within a
	 * {@link PointSet} region of a {@link Dataset}
	 * 
	 * @param ds The Dataset to measure
	 * @param region The PointSet region upon which to calculate
	 * @return The measured value
	 */
	double sampleKurtosisExcess(Dataset ds, PointSet region);

	/**
	 * Returns the (unbiased) kurtosis excess of all the values within a
	 * {@link Dataset}
	 * 
	 * @param ds The Dataset to measure
	 * @return The measured value
	 */
	double sampleKurtosisExcess(Dataset ds);

	/**
	 * Returns the (unbiased) skew of all the values within a {@link PointSet}
	 * region of a {@link Dataset}
	 * 
	 * @param ds The Dataset to measure
	 * @param region The PointSet region upon which to calculate
	 * @return The measured value
	 */
	double sampleSkew(Dataset ds, PointSet region);

	/**
	 * Returns the (unbiased) skew of all the values within a {@link Dataset}
	 * 
	 * @param ds The Dataset to measure
	 * @return The measured value
	 */
	double sampleSkew(Dataset ds);

	/**
	 * Returns the (unbiased) estimate of the sample standard deviation of the
	 * values within a {@link PointSet} region of a {@link Dataset}. It equals the
	 * square root of the (unbiased) estimate of the sample variance.
	 * 
	 * @param ds The Dataset to measure
	 * @param region The PointSet region upon which to calculate
	 * @return The measured value
	 */
	double sampleStdDev(Dataset ds, PointSet region);

	/**
	 * Returns the (unbiased) estimate of the sample standard deviation of the
	 * values within a {@link Dataset}. It equals the square root of the
	 * (unbiased) estimate of the sample variance.
	 * 
	 * @param ds The Dataset to measure
	 * @return The measured value
	 */
	double sampleStdDev(Dataset ds);

	/**
	 * Returns the (unbiased) estimate of the sample variance of the values within
	 * a {@link PointSet} region of a {@link Dataset}. It divides the sum of
	 * squared deviations from the mean by N-1 (N == number of samples).
	 * 
	 * @param ds The Dataset to measure
	 * @param region The PointSet region upon which to calculate
	 * @return The measured value
	 */
	double sampleVariance(Dataset ds, PointSet region);

	/**
	 * Returns the (unbiased) estimate of the sample variance of the values within
	 * a {@link Dataset}. It divides the sum of squared deviations from the mean
	 * by N-1 (N == number of samples).
	 * 
	 * @param ds The Dataset to measure
	 * @return The measured value
	 */
	double sampleVariance(Dataset ds);

	/**
	 * Returns the sum of all the values within a {@link PointSet} region of a
	 * {@link Dataset}
	 * 
	 * @param ds The Dataset to measure
	 * @param region The PointSet region upon which to calculate
	 * @return The measured value
	 */
	double sum(Dataset ds, PointSet region);

	/**
	 * Returns the sum of all the values within a {@link Dataset}
	 * 
	 * @param ds The Dataset to measure
	 * @return The measured value
	 */
	double sum(Dataset ds);

	/**
	 * Returns the sum of squared deviations from the mean for a set of values
	 * within a {@link PointSet} region of a {@link Dataset}
	 * 
	 * @param ds The Dataset to measure
	 * @param region The PointSet region upon which to calculate
	 * @return The measured value
	 */
	double sumOfSquaredDeviations(Dataset ds, PointSet region);

	/**
	 * Returns the sum of squared deviations from the mean for a set of values
	 * within a {@link Dataset}
	 * 
	 * @param ds The Dataset to measure
	 * @return The measured value
	 */
	double sumOfSquaredDeviations(Dataset ds);

	/**
	 * Returns an estimate of the trimmed mean of the values within a
	 * {@link PointSet} region of a {@link Dataset}.
	 * 
	 * @param ds The Dataset to measure
	 * @param region The PointSet region upon which to calculate
	 * @param halfTrimSize The number of values to trim from each end of the set
	 *          of samples collected
	 * @return The measured value
	 */
	double trimmedMean(Dataset ds, PointSet region, int halfTrimSize);

	/**
	 * Returns an estimate of the trimmed mean of the values of a {@link Dataset}.
	 * 
	 * @param ds The Dataset to measure
	 * @param halfTrimSize The number of values to trim from each end of the set
	 *          of samples collected
	 * @return The measured value
	 */
	double trimmedMean(Dataset ds, int halfTrimSize);

	/**
	 * Returns the weighted average of the values within a {@link PointSet} region
	 * of a {@link Dataset}. The weights must be provided and there must be the
	 * same number of weights as there are points in the region.
	 * 
	 * @param ds The Dataset to measure
	 * @param region The PointSet region upon which to calculate
	 * @param weights The weights to apply to each value in the region
	 * @return The measured value
	 */
	double weightedAverage(Dataset ds, PointSet region, double[] weights);

	/**
	 * Returns the weighted sum of the values within a {@link PointSet} region of
	 * a {@link Dataset}. The weights must be provided and there must be the same
	 * number of weights as there are points in the region.
	 * 
	 * @param ds The Dataset to measure
	 * @param region The PointSet region upon which to calculate
	 * @param weights The weights to apply to each value in the region
	 * @return The measured value
	 */
	double weightedSum(Dataset ds, PointSet region, double[] weights);

	/**
	 * A convenience function for defining a {@link PointSet} that encompasses all
	 * the points within a {@link Dataset}.
	 * 
	 * @param ds The Dataset of interest
	 * @return A PointSet that includes all points within the Dataset
	 */
	PointSet allOf(Dataset ds);

}
