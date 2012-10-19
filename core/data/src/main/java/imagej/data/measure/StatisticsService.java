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
import net.imglib2.ops.function.real.RealProductFunction;
import net.imglib2.ops.function.real.RealSumFunction;
import net.imglib2.ops.pointset.PointSet;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.real.DoubleType;
import imagej.data.Dataset;
import imagej.plugin.Plugin;
import imagej.service.AbstractService;
import imagej.service.Service;

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
public class StatisticsService extends AbstractService {

	// -- Parameters --
	
	// later
	//@Parameter
	//private MeasurementService mSrv;
	
	// -- StatisticsService methods --
	
	/**
	 * Returns an estimate of the trimmed mean of the values within a
	 * {@link PointSet} region of a {@link Dataset}.
	 *  
	 * @param ds
	 * The Dataset to measure
	 * @param region
	 * The PointSet region upon which to calculate
	 * @param halfTrimSize
	 * The number of values to trim from each end of the set of samples collected
	 * @return
	 * The measured value
	 */
	public double alphaTrimmedMean(Dataset ds, PointSet region, int halfTrimSize)
	{
		Function<long[],DoubleType> imgFunc = imgFunc(ds);
		Function<PointSet,DoubleType> func =
				new RealAlphaTrimmedMeanFunction<DoubleType>(imgFunc,halfTrimSize);
		return measure(func,region);
	}

	/**
	 * Returns an estimate of the arithmetic mean of the values within a
	 * {@link PointSet} region of a {@link Dataset}
	 *  
	 * @param ds
	 * The Dataset to measure
	 * @param region
	 * The PointSet region upon which to calculate
	 * @return
	 * The measured value
	 */
	public double arithmeticMean(Dataset ds, PointSet region) {
		Function<long[],DoubleType> imgFunc = imgFunc(ds);
		Function<PointSet,DoubleType> func =
				new RealArithmeticMeanFunction<DoubleType>(imgFunc);
		return measure(func,region);
	}
	
	/**
	 * Returns an estimate of the contraharmonic mean of the values within a
	 * {@link PointSet} region of a {@link Dataset}
	 *  
	 * @param ds
	 * The Dataset to measure
	 * @param region
	 * The PointSet region upon which to calculate
	 * @param order
	 * The desired order of the contraharmonic mean
	 * @return
	 * The measured value
	 */
	public double contraharmomicMean(Dataset ds, PointSet region, double order) {
		Function<long[],DoubleType> imgFunc = imgFunc(ds);
		Function<PointSet,DoubleType> func =
				new RealContraharmonicMeanFunction<DoubleType>(imgFunc, order);
		return measure(func,region);
	}

	/**
	 * Returns an estimate of the geometric mean of the values within a
	 * {@link PointSet} region of a {@link Dataset}
	 *  
	 * @param ds
	 * The Dataset to measure
	 * @param region
	 * The PointSet region upon which to calculate
	 * @return
	 * The measured value
	 */
	public double geometricMean(Dataset ds, PointSet region) {
		Function<long[],DoubleType> imgFunc = imgFunc(ds);
		Function<PointSet,DoubleType> func =
				new RealGeometricMeanFunction<DoubleType>(imgFunc);
		return measure(func,region);
	}
	
	/**
	 * Returns an estimate of the harmonic mean of the values within a
	 * {@link PointSet} region of a {@link Dataset}
	 *  
	 * @param ds
	 * The Dataset to measure
	 * @param region
	 * The PointSet region upon which to calculate
	 * @return
	 * The measured value
	 */
	public double harmonicMean(Dataset ds, PointSet region) {
		Function<long[],DoubleType> imgFunc = imgFunc(ds);
		Function<PointSet,DoubleType> func =
				new RealHarmonicMeanFunction<DoubleType>(imgFunc);
		return measure(func,region);
	}

	/**
	 * Returns the maximum value from the set of values within a
	 * {@link PointSet} region of a {@link Dataset}
	 *  
	 * @param ds
	 * The Dataset to measure
	 * @param region
	 * The PointSet region upon which to calculate
	 * @return
	 * The measured value
	 */
	public double maximum(Dataset ds, PointSet region) {
		Function<long[],DoubleType> imgFunc = imgFunc(ds);
		Function<PointSet,DoubleType> func =
				new RealMaxFunction<DoubleType>(imgFunc);
		return measure(func,region);
	}
	
	/**
	 * Returns the median value from the set of values within a
	 * {@link PointSet} region of a {@link Dataset}
	 *  
	 * @param ds
	 * The Dataset to measure
	 * @param region
	 * The PointSet region upon which to calculate
	 * @return
	 * The measured value
	 */
	public double median(Dataset ds, PointSet region) {
		Function<long[],DoubleType> imgFunc = imgFunc(ds);
		Function<PointSet,DoubleType> func =
				new RealMedianFunction<DoubleType>(imgFunc);
		return measure(func,region);
	}

	/**
	 * Returns the point midway between the minimum value and the maximum value
	 * from the set of values within a {@link PointSet} region of a
	 * {@link Dataset}
	 *  
	 * @param ds
	 * The Dataset to measure
	 * @param region
	 * The PointSet region upon which to calculate
	 * @return
	 * The measured value
	 */
	public double midpoint(Dataset ds, PointSet region) {
		Function<long[],DoubleType> imgFunc = imgFunc(ds);
		Function<PointSet,DoubleType> func =
				new RealMidpointFunction<DoubleType>(imgFunc);
		return measure(func,region);
	}
	
	/**
	 * Returns the minimum value from the set of values within a
	 * {@link PointSet} region of a {@link Dataset}
	 *  
	 * @param ds
	 * The Dataset to measure
	 * @param region
	 * The PointSet region upon which to calculate
	 * @return
	 * The measured value
	 */
	public double minimum(Dataset ds, PointSet region) {
		Function<long[],DoubleType> imgFunc = imgFunc(ds);
		Function<PointSet,DoubleType> func =
				new RealMinFunction<DoubleType>(imgFunc);
		return measure(func,region);
	}
	
	/**
	 * Returns the product of all the values within a {@link PointSet} region
	 * of a {@link Dataset}
	 *  
	 * @param ds
	 * The Dataset to measure
	 * @param region
	 * The PointSet region upon which to calculate
	 * @return
	 * The measured value
	 */
	public double product(Dataset ds, PointSet region) {
		Function<long[],DoubleType> imgFunc = imgFunc(ds);
		Function<PointSet,DoubleType> func =
				new RealProductFunction<DoubleType>(imgFunc);
		return measure(func,region);
	}
	
	/**
	 * Returns the sum of all the values within a {@link PointSet} region
	 * of a {@link Dataset}
	 *  
	 * @param ds
	 * The Dataset to measure
	 * @param region
	 * The PointSet region upon which to calculate
	 * @return
	 * The measured value
	 */
	public double sum(Dataset ds, PointSet region) {
		Function<long[],DoubleType> imgFunc = imgFunc(ds);
		Function<PointSet,DoubleType> func =
				new RealSumFunction<DoubleType>(imgFunc);
		return measure(func,region);
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
