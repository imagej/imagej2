/*
 * #%L
 * ImageJ software for multidimensional image processing and analysis.
 * %%
 * Copyright (C) 2009 - 2012 Board of Regents of the University of
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
import imagej.plugin.Plugin;
import imagej.service.AbstractService;
import imagej.service.Service;
import net.imglib2.img.Img;
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
import net.imglib2.ops.function.real.RealWeightedAverageFunction;
import net.imglib2.ops.function.real.RealWeightedSumFunction;
import net.imglib2.ops.pointset.HyperVolumePointSet;
import net.imglib2.ops.pointset.PointSet;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.real.DoubleType;

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
 * A service for computing some basic statistics upon regions of
 * {@link Dataset}s.
 * 
 * @author Barry DeZonia
 *
 */
@Plugin(type=Service.class)
public class DefaultStatisticsService extends AbstractService implements
	StatisticsService
{
	// -- Parameters --
	
	// later
	//@Parameter
	//private MeasurementService mSrv;
	
	// -- StatisticsService methods --
	
	@Override
	public double alphaTrimmedMean(Dataset ds, PointSet region, int halfTrimSize)
	{
		Function<long[],DoubleType> imgFunc = imgFunc(ds);
		Function<PointSet,DoubleType> func =
				new RealAlphaTrimmedMeanFunction<DoubleType>(imgFunc, halfTrimSize);
		return measure(func, region);
	}

	@Override
	public double alphaTrimmedMean(Dataset ds, int halfTrimSize)
	{
		return alphaTrimmedMean(ds, allOf(ds), halfTrimSize);
	}

	@Override
	public double arithmeticMean(Dataset ds, PointSet region) {
		Function<long[],DoubleType> imgFunc = imgFunc(ds);
		Function<PointSet,DoubleType> func =
				new RealArithmeticMeanFunction<DoubleType>(imgFunc);
		return measure(func, region);
	}
	
	@Override
	public double arithmeticMean(Dataset ds) {
		return arithmeticMean(ds, allOf(ds));
	}
	
	@Override
	public double contraharmomicMean(Dataset ds, PointSet region, double order) {
		Function<long[],DoubleType> imgFunc = imgFunc(ds);
		Function<PointSet,DoubleType> func =
				new RealContraharmonicMeanFunction<DoubleType>(imgFunc, order);
		return measure(func, region);
	}

	@Override
	public double contraharmomicMean(Dataset ds, double order) {
		return contraharmomicMean(ds, allOf(ds), order);
	}

	@Override
	public double geometricMean(Dataset ds, PointSet region) {
		Function<long[],DoubleType> imgFunc = imgFunc(ds);
		Function<PointSet,DoubleType> func =
				new RealGeometricMeanFunction<DoubleType>(imgFunc);
		return measure(func, region);
	}
	
	@Override
	public double geometricMean(Dataset ds) {
		return geometricMean(ds, allOf(ds));
	}
	
	@Override
	public double harmonicMean(Dataset ds, PointSet region) {
		Function<long[],DoubleType> imgFunc = imgFunc(ds);
		Function<PointSet,DoubleType> func =
				new RealHarmonicMeanFunction<DoubleType>(imgFunc);
		return measure(func, region);
	}

	@Override
	public double harmonicMean(Dataset ds) {
		return harmonicMean(ds, allOf(ds));
	}

	@Override
	public double maximum(Dataset ds, PointSet region) {
		Function<long[],DoubleType> imgFunc = imgFunc(ds);
		Function<PointSet,DoubleType> func =
				new RealMaxFunction<DoubleType>(imgFunc);
		return measure(func, region);
	}
	
	@Override
	public double maximum(Dataset ds) {
		return maximum(ds, allOf(ds));
	}
	
	@Override
	public double median(Dataset ds, PointSet region) {
		Function<long[],DoubleType> imgFunc = imgFunc(ds);
		Function<PointSet,DoubleType> func =
				new RealMedianFunction<DoubleType>(imgFunc);
		return measure(func, region);
	}

	@Override
	public double median(Dataset ds) {
		return median(ds, allOf(ds));
	}

	@Override
	public double midpoint(Dataset ds, PointSet region) {
		Function<long[],DoubleType> imgFunc = imgFunc(ds);
		Function<PointSet,DoubleType> func =
				new RealMidpointFunction<DoubleType>(imgFunc);
		return measure(func, region);
	}
	
	@Override
	public double midpoint(Dataset ds) {
		return midpoint(ds, allOf(ds));
	}
	
	@Override
	public double minimum(Dataset ds, PointSet region) {
		Function<long[],DoubleType> imgFunc = imgFunc(ds);
		Function<PointSet,DoubleType> func =
				new RealMinFunction<DoubleType>(imgFunc);
		return measure(func, region);
	}
	
	@Override
	public double minimum(Dataset ds) {
		return minimum(ds, allOf(ds));
	}
	

	@Override
	public double populationKurtosis(Dataset ds, PointSet region) {
		Function<long[],DoubleType> imgFunc = imgFunc(ds);
		Function<PointSet,DoubleType> func =
				new RealPopulationKurtosisFunction<DoubleType>(imgFunc);
		return measure(func, region);
	}

	@Override
	public double populationKurtosis(Dataset ds) {
		return populationKurtosis(ds, allOf(ds));
	}

	@Override
	public double populationKurtosisExcess(Dataset ds, PointSet region) {
		Function<long[],DoubleType> imgFunc = imgFunc(ds);
		Function<PointSet,DoubleType> func =
				new RealPopulationKurtosisExcessFunction<DoubleType>(imgFunc);
		return measure(func, region);
	}

	@Override
	public double populationKurtosisExcess(Dataset ds) {
		return populationKurtosisExcess(ds, allOf(ds));
	}

	@Override
	public double populationSkew(Dataset ds, PointSet region) {
		Function<long[],DoubleType> imgFunc = imgFunc(ds);
		Function<PointSet,DoubleType> func =
				new RealPopulationSkewFunction<DoubleType>(imgFunc);
		return measure(func, region);
	}

	@Override
	public double populationSkew(Dataset ds) {
		return populationSkew(ds, allOf(ds));
	}

	@Override
	public double populationStdDev(Dataset ds, PointSet region) {
		Function<long[],DoubleType> imgFunc = imgFunc(ds);
		Function<PointSet,DoubleType> func =
				new RealPopulationStdDevFunction<DoubleType>(imgFunc);
		return measure(func, region);
	}
	
	@Override
	public double populationStdDev(Dataset ds) {
		return populationStdDev(ds, allOf(ds));
	}
	
	@Override
	public double populationVariance(Dataset ds, PointSet region) {
		Function<long[],DoubleType> imgFunc = imgFunc(ds);
		Function<PointSet,DoubleType> func =
				new RealPopulationVarianceFunction<DoubleType>(imgFunc);
		return measure(func, region);
	}
	
	@Override
	public double populationVariance(Dataset ds) {
		return populationVariance(ds, allOf(ds));
	}
	
	@Override
	public double product(Dataset ds, PointSet region) {
		Function<long[],DoubleType> imgFunc = imgFunc(ds);
		Function<PointSet,DoubleType> func =
				new RealProductFunction<DoubleType>(imgFunc);
		return measure(func, region);
	}
	
	@Override
	public double product(Dataset ds) {
		return product(ds, allOf(ds));
	}
	
	@Override
	public double sampleKurtosis(Dataset ds, PointSet region) {
		Function<long[],DoubleType> imgFunc = imgFunc(ds);
		Function<PointSet,DoubleType> func =
				new RealSampleKurtosisFunction<DoubleType>(imgFunc);
		return measure(func, region);
	}

	@Override
	public double sampleKurtosis(Dataset ds) {
		return sampleKurtosis(ds, allOf(ds));
	}

	@Override
	public double sampleKurtosisExcess(Dataset ds, PointSet region) {
		Function<long[],DoubleType> imgFunc = imgFunc(ds);
		Function<PointSet,DoubleType> func =
				new RealSampleKurtosisExcessFunction<DoubleType>(imgFunc);
		return measure(func, region);
	}

	@Override
	public double sampleKurtosisExcess(Dataset ds) {
		return sampleKurtosisExcess(ds, allOf(ds));
	}

	@Override
	public double sampleSkew(Dataset ds, PointSet region) {
		Function<long[],DoubleType> imgFunc = imgFunc(ds);
		Function<PointSet,DoubleType> func =
				new RealSampleSkewFunction<DoubleType>(imgFunc);
		return measure(func, region);
	}

	@Override
	public double sampleSkew(Dataset ds) {
		return sampleSkew(ds, allOf(ds));
	}

	@Override
	public double sampleStdDev(Dataset ds, PointSet region) {
		Function<long[],DoubleType> imgFunc = imgFunc(ds);
		Function<PointSet,DoubleType> func =
				new RealSampleStdDevFunction<DoubleType>(imgFunc);
		return measure(func, region);
	}

	@Override
	public double sampleStdDev(Dataset ds) {
		return sampleStdDev(ds, allOf(ds));
	}

	@Override
	public double sampleVariance(Dataset ds, PointSet region) {
		Function<long[],DoubleType> imgFunc = imgFunc(ds);
		Function<PointSet,DoubleType> func =
				new RealSampleVarianceFunction<DoubleType>(imgFunc);
		return measure(func, region);
	}

	@Override
	public double sampleVariance(Dataset ds) {
		return sampleVariance(ds, allOf(ds));
	}

	@Override
	public double sum(Dataset ds, PointSet region) {
		Function<long[],DoubleType> imgFunc = imgFunc(ds);
		Function<PointSet,DoubleType> func =
				new RealSumFunction<DoubleType>(imgFunc);
		return measure(func, region);
	}
	
	@Override
	public double sum(Dataset ds) {
		return sum(ds, allOf(ds));
	}
	
	@Override
	public double sumOfSquaredDeviations(Dataset ds, PointSet region) {
		Function<long[],DoubleType> imgFunc = imgFunc(ds);
		Function<PointSet,DoubleType> func =
				new RealSumOfSquaredDeviationsFunction<DoubleType>(imgFunc);
		return measure(func, region);
	}
	
	@Override
	public double sumOfSquaredDeviations(Dataset ds) {
		return sumOfSquaredDeviations(ds, allOf(ds));
	}
	
	@Override
	public double weightedAverage(Dataset ds, PointSet region, double[] weights) {
		Function<long[],DoubleType> imgFunc = imgFunc(ds);
		Function<PointSet,DoubleType> func =
				new RealWeightedAverageFunction<DoubleType>(imgFunc,weights);
		return measure(func, region);
	}
	
	@Override
	public double weightedSum(Dataset ds, PointSet region, double[] weights) {
		Function<long[],DoubleType> imgFunc = imgFunc(ds);
		Function<PointSet,DoubleType> func =
				new RealWeightedSumFunction<DoubleType>(imgFunc,weights);
		return measure(func, region);
	}

	@Override
	public PointSet allOf(Dataset ds) {
		return new HyperVolumePointSet(ds.getDims());
	}
	
	// -- private helpers --
	
	@SuppressWarnings({"unchecked","rawtypes"})
	private RealImageFunction<?,DoubleType> imgFunc(Dataset ds) {
		Img<? extends RealType<?>> imgPlus = ds.getImgPlus();
		return new RealImageFunction(imgPlus, new DoubleType());
	}
	
	private double measure(Function<PointSet,DoubleType> func, PointSet region) {
		DoubleType output = new DoubleType();
		func.compute(region, output);
		return output.getRealDouble();
	}
}
