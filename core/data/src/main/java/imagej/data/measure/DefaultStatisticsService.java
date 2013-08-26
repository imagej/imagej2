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
import net.imglib2.img.Img;
import net.imglib2.meta.IntervalUtils;
import net.imglib2.ops.function.Function;
import net.imglib2.ops.function.real.RealAlphaTrimmedMeanFunction;
import net.imglib2.ops.function.real.RealArithmeticMeanFunction;
import net.imglib2.ops.function.real.RealContraharmonicMeanFunction;
import net.imglib2.ops.function.real.RealGeometricMeanFunction;
import net.imglib2.ops.function.real.RealHarmonicMeanFunction;
import net.imglib2.ops.function.real.RealImageFunction;
import net.imglib2.ops.function.real.RealMaxFunction;
import net.imglib2.ops.function.real.RealMedianFunction;
import net.imglib2.ops.function.real.RealMidpointFunction;
import net.imglib2.ops.function.real.RealMinFunction;
import net.imglib2.ops.function.real.RealPopulationKurtosisExcessFunction;
import net.imglib2.ops.function.real.RealPopulationKurtosisFunction;
import net.imglib2.ops.function.real.RealPopulationSkewFunction;
import net.imglib2.ops.function.real.RealPopulationStdDevFunction;
import net.imglib2.ops.function.real.RealPopulationVarianceFunction;
import net.imglib2.ops.function.real.RealProductFunction;
import net.imglib2.ops.function.real.RealSampleKurtosisExcessFunction;
import net.imglib2.ops.function.real.RealSampleKurtosisFunction;
import net.imglib2.ops.function.real.RealSampleSkewFunction;
import net.imglib2.ops.function.real.RealSampleStdDevFunction;
import net.imglib2.ops.function.real.RealSampleVarianceFunction;
import net.imglib2.ops.function.real.RealSumFunction;
import net.imglib2.ops.function.real.RealSumOfSquaredDeviationsFunction;
import net.imglib2.ops.function.real.RealTrimmedMeanFunction;
import net.imglib2.ops.function.real.RealWeightedAverageFunction;
import net.imglib2.ops.function.real.RealWeightedSumFunction;
import net.imglib2.ops.pointset.HyperVolumePointSet;
import net.imglib2.ops.pointset.PointSet;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.real.DoubleType;

import org.scijava.plugin.Plugin;
import org.scijava.service.AbstractService;
import org.scijava.service.Service;

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
 * A service for computing some statistics upon regions of {@link Dataset}s.
 * 
 * @author Barry DeZonia
 */
@Plugin(type = Service.class)
public class DefaultStatisticsService extends AbstractService implements
	StatisticsService
{

	// -- Parameters --

	// later
	// @Parameter
	// private MeasurementService mSrv;

	// -- StatisticsService methods --

	@Override
	public double alphaTrimmedMean(final Dataset ds, final PointSet region,
		final double alpha)
	{
		final Function<long[], DoubleType> imgFunc = imgFunc(ds);
		final Function<PointSet, DoubleType> func =
			new RealAlphaTrimmedMeanFunction<DoubleType>(imgFunc, alpha);
		return measure(func, region);
	}

	@Override
	public double alphaTrimmedMean(final Dataset ds, final double alpha) {
		return alphaTrimmedMean(ds, allOf(ds), alpha);
	}

	@Override
	public double arithmeticMean(final Dataset ds, final PointSet region) {
		final Function<long[], DoubleType> imgFunc = imgFunc(ds);
		final Function<PointSet, DoubleType> func =
			new RealArithmeticMeanFunction<DoubleType>(imgFunc);
		return measure(func, region);
	}

	@Override
	public double arithmeticMean(final Dataset ds) {
		return arithmeticMean(ds, allOf(ds));
	}

	@Override
	public double contraharmomicMean(final Dataset ds, final PointSet region,
		final double order)
	{
		final Function<long[], DoubleType> imgFunc = imgFunc(ds);
		final Function<PointSet, DoubleType> func =
			new RealContraharmonicMeanFunction<DoubleType>(imgFunc, order);
		return measure(func, region);
	}

	@Override
	public double contraharmomicMean(final Dataset ds, final double order) {
		return contraharmomicMean(ds, allOf(ds), order);
	}

	@Override
	public double geometricMean(final Dataset ds, final PointSet region) {
		final Function<long[], DoubleType> imgFunc = imgFunc(ds);
		final Function<PointSet, DoubleType> func =
			new RealGeometricMeanFunction<DoubleType>(imgFunc);
		return measure(func, region);
	}

	@Override
	public double geometricMean(final Dataset ds) {
		return geometricMean(ds, allOf(ds));
	}

	@Override
	public double harmonicMean(final Dataset ds, final PointSet region) {
		final Function<long[], DoubleType> imgFunc = imgFunc(ds);
		final Function<PointSet, DoubleType> func =
			new RealHarmonicMeanFunction<DoubleType>(imgFunc);
		return measure(func, region);
	}

	@Override
	public double harmonicMean(final Dataset ds) {
		return harmonicMean(ds, allOf(ds));
	}

	@Override
	public double maximum(final Dataset ds, final PointSet region) {
		final Function<long[], DoubleType> imgFunc = imgFunc(ds);
		final Function<PointSet, DoubleType> func =
			new RealMaxFunction<DoubleType>(imgFunc);
		return measure(func, region);
	}

	@Override
	public double maximum(final Dataset ds) {
		return maximum(ds, allOf(ds));
	}

	@Override
	public double median(final Dataset ds, final PointSet region) {
		final Function<long[], DoubleType> imgFunc = imgFunc(ds);
		final Function<PointSet, DoubleType> func =
			new RealMedianFunction<DoubleType>(imgFunc);
		return measure(func, region);
	}

	@Override
	public double median(final Dataset ds) {
		return median(ds, allOf(ds));
	}

	@Override
	public double midpoint(final Dataset ds, final PointSet region) {
		final Function<long[], DoubleType> imgFunc = imgFunc(ds);
		final Function<PointSet, DoubleType> func =
			new RealMidpointFunction<DoubleType>(imgFunc);
		return measure(func, region);
	}

	@Override
	public double midpoint(final Dataset ds) {
		return midpoint(ds, allOf(ds));
	}

	@Override
	public double minimum(final Dataset ds, final PointSet region) {
		final Function<long[], DoubleType> imgFunc = imgFunc(ds);
		final Function<PointSet, DoubleType> func =
			new RealMinFunction<DoubleType>(imgFunc);
		return measure(func, region);
	}

	@Override
	public double minimum(final Dataset ds) {
		return minimum(ds, allOf(ds));
	}

	@Override
	public double populationKurtosis(final Dataset ds, final PointSet region) {
		final Function<long[], DoubleType> imgFunc = imgFunc(ds);
		final Function<PointSet, DoubleType> func =
			new RealPopulationKurtosisFunction<DoubleType>(imgFunc);
		return measure(func, region);
	}

	@Override
	public double populationKurtosis(final Dataset ds) {
		return populationKurtosis(ds, allOf(ds));
	}

	@Override
	public double
		populationKurtosisExcess(final Dataset ds, final PointSet region)
	{
		final Function<long[], DoubleType> imgFunc = imgFunc(ds);
		final Function<PointSet, DoubleType> func =
			new RealPopulationKurtosisExcessFunction<DoubleType>(imgFunc);
		return measure(func, region);
	}

	@Override
	public double populationKurtosisExcess(final Dataset ds) {
		return populationKurtosisExcess(ds, allOf(ds));
	}

	@Override
	public double populationSkew(final Dataset ds, final PointSet region) {
		final Function<long[], DoubleType> imgFunc = imgFunc(ds);
		final Function<PointSet, DoubleType> func =
			new RealPopulationSkewFunction<DoubleType>(imgFunc);
		return measure(func, region);
	}

	@Override
	public double populationSkew(final Dataset ds) {
		return populationSkew(ds, allOf(ds));
	}

	@Override
	public double populationStdDev(final Dataset ds, final PointSet region) {
		final Function<long[], DoubleType> imgFunc = imgFunc(ds);
		final Function<PointSet, DoubleType> func =
			new RealPopulationStdDevFunction<DoubleType>(imgFunc);
		return measure(func, region);
	}

	@Override
	public double populationStdDev(final Dataset ds) {
		return populationStdDev(ds, allOf(ds));
	}

	@Override
	public double populationVariance(final Dataset ds, final PointSet region) {
		final Function<long[], DoubleType> imgFunc = imgFunc(ds);
		final Function<PointSet, DoubleType> func =
			new RealPopulationVarianceFunction<DoubleType>(imgFunc);
		return measure(func, region);
	}

	@Override
	public double populationVariance(final Dataset ds) {
		return populationVariance(ds, allOf(ds));
	}

	@Override
	public double product(final Dataset ds, final PointSet region) {
		final Function<long[], DoubleType> imgFunc = imgFunc(ds);
		final Function<PointSet, DoubleType> func =
			new RealProductFunction<DoubleType>(imgFunc);
		return measure(func, region);
	}

	@Override
	public double product(final Dataset ds) {
		return product(ds, allOf(ds));
	}

	@Override
	public double sampleKurtosis(final Dataset ds, final PointSet region) {
		final Function<long[], DoubleType> imgFunc = imgFunc(ds);
		final Function<PointSet, DoubleType> func =
			new RealSampleKurtosisFunction<DoubleType>(imgFunc);
		return measure(func, region);
	}

	@Override
	public double sampleKurtosis(final Dataset ds) {
		return sampleKurtosis(ds, allOf(ds));
	}

	@Override
	public double sampleKurtosisExcess(final Dataset ds, final PointSet region) {
		final Function<long[], DoubleType> imgFunc = imgFunc(ds);
		final Function<PointSet, DoubleType> func =
			new RealSampleKurtosisExcessFunction<DoubleType>(imgFunc);
		return measure(func, region);
	}

	@Override
	public double sampleKurtosisExcess(final Dataset ds) {
		return sampleKurtosisExcess(ds, allOf(ds));
	}

	@Override
	public double sampleSkew(final Dataset ds, final PointSet region) {
		final Function<long[], DoubleType> imgFunc = imgFunc(ds);
		final Function<PointSet, DoubleType> func =
			new RealSampleSkewFunction<DoubleType>(imgFunc);
		return measure(func, region);
	}

	@Override
	public double sampleSkew(final Dataset ds) {
		return sampleSkew(ds, allOf(ds));
	}

	@Override
	public double sampleStdDev(final Dataset ds, final PointSet region) {
		final Function<long[], DoubleType> imgFunc = imgFunc(ds);
		final Function<PointSet, DoubleType> func =
			new RealSampleStdDevFunction<DoubleType>(imgFunc);
		return measure(func, region);
	}

	@Override
	public double sampleStdDev(final Dataset ds) {
		return sampleStdDev(ds, allOf(ds));
	}

	@Override
	public double sampleVariance(final Dataset ds, final PointSet region) {
		final Function<long[], DoubleType> imgFunc = imgFunc(ds);
		final Function<PointSet, DoubleType> func =
			new RealSampleVarianceFunction<DoubleType>(imgFunc);
		return measure(func, region);
	}

	@Override
	public double sampleVariance(final Dataset ds) {
		return sampleVariance(ds, allOf(ds));
	}

	@Override
	public double sum(final Dataset ds, final PointSet region) {
		final Function<long[], DoubleType> imgFunc = imgFunc(ds);
		final Function<PointSet, DoubleType> func =
			new RealSumFunction<DoubleType>(imgFunc);
		return measure(func, region);
	}

	@Override
	public double sum(final Dataset ds) {
		return sum(ds, allOf(ds));
	}

	@Override
	public double sumOfSquaredDeviations(final Dataset ds, final PointSet region)
	{
		final Function<long[], DoubleType> imgFunc = imgFunc(ds);
		final Function<PointSet, DoubleType> func =
			new RealSumOfSquaredDeviationsFunction<DoubleType>(imgFunc);
		return measure(func, region);
	}

	@Override
	public double sumOfSquaredDeviations(final Dataset ds) {
		return sumOfSquaredDeviations(ds, allOf(ds));
	}

	@Override
	public double trimmedMean(Dataset ds, PointSet region, int halfTrimSize) {
		final Function<long[], DoubleType> imgFunc = imgFunc(ds);
		final Function<PointSet, DoubleType> func =
			new RealTrimmedMeanFunction<DoubleType>(imgFunc, halfTrimSize);
		return measure(func, region);
	}

	@Override
	public double trimmedMean(Dataset ds, int halfTrimSize) {
		return trimmedMean(ds, allOf(ds), halfTrimSize);
	}

	@Override
	public double weightedAverage(final Dataset ds, final PointSet region,
		final double[] weights)
	{
		final Function<long[], DoubleType> imgFunc = imgFunc(ds);
		final Function<PointSet, DoubleType> func =
			new RealWeightedAverageFunction<DoubleType>(imgFunc, weights);
		return measure(func, region);
	}

	@Override
	public double weightedSum(final Dataset ds, final PointSet region,
		final double[] weights)
	{
		final Function<long[], DoubleType> imgFunc = imgFunc(ds);
		final Function<PointSet, DoubleType> func =
			new RealWeightedSumFunction<DoubleType>(imgFunc, weights);
		return measure(func, region);
	}

	@Override
	public PointSet allOf(final Dataset ds) {
		return new HyperVolumePointSet(IntervalUtils.getDims(ds));
	}

	// -- private helpers --

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private RealImageFunction<?, DoubleType> imgFunc(final Dataset ds) {
		final Img<? extends RealType<?>> imgPlus = ds.getImgPlus();
		return new RealImageFunction(imgPlus, new DoubleType());
	}

	private double measure(final Function<PointSet, DoubleType> func,
		final PointSet region)
	{
		final DoubleType output = new DoubleType();
		func.compute(region, output);
		return output.getRealDouble();
	}

}
