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

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;

import net.imglib2.Cursor;
import net.imglib2.img.Img;
import net.imglib2.meta.Axes;
import net.imglib2.meta.AxisType;
import net.imglib2.ops.function.Function;
import net.imglib2.ops.function.real.PrimitiveDoubleArray;
import net.imglib2.ops.function.real.RealAdaptiveMedianFunction;
import net.imglib2.ops.function.real.RealArithmeticMeanFunction;
import net.imglib2.ops.function.real.RealImageFunction;
import net.imglib2.ops.function.real.RealMaxFunction;
import net.imglib2.ops.function.real.RealMedianFunction;
import net.imglib2.ops.function.real.RealMinFunction;
import net.imglib2.ops.pointset.HyperVolumePointSet;
import net.imglib2.ops.pointset.PointSet;
import net.imglib2.ops.pointset.PointSetIterator;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.complex.ComplexDoubleType;
import net.imglib2.type.numeric.real.DoubleType;
import imagej.data.Dataset;
import imagej.data.DatasetService;
import imagej.log.LogService;
import imagej.plugin.Parameter;
import imagej.plugin.Plugin;
import imagej.service.AbstractService;
import imagej.service.Service;

// TODO - this implementation is somewhat broken. It relies on the idea that
// a measurement function has a constructor with a single argument that
// represents the data to measure. But some measurement functions have
// constructors that take data plus some other params to construct itself.
// These cannot be used in the measure() methods below.
// In regards to this problem CTR mentioned that we could somehow use the
// Command/Module code to provide all the types a Function constructor might
// need as @Parameters. I'll need to investigate but this might lead to code
// duplication (a Command for Median and a Function for Median).

// After discussing optimization with Aivar some ideas: (fleshed out below)
//   - optimize measurement api so that values can be reused rather than recalc
//     For instance you could write a Function<PointSet,BundleOfStats>.
//     The function could gather a family of stats and utilize partial results.
//     So have a function that computes mean AND std dev and uses the result of
//     the mean to calc the std dev. Gathering all moments is similar idea.
//   - make general where you can pass it a set of measurements to make and it
//     will do so passing over data once for efficiency.
//     i.e. calc(avg, skew, median, ...)

/**
 * A service that simplifies the measurement of statistics from data.
 * 
 * @author Barry DeZonia
 *
 */
@Plugin(type = Service.class)
public class MeasurementService extends AbstractService {

	// -- Parameters --
	
	@Parameter
	private LogService log;
	
	// -- MeasurementService methods --

	// OBSOLETE
	/**
	 * Makes a measurable function. Takes an <@link Img> and wraps a (presumably)
	 * statistical function around it. The function can then be queried for values.
	 * @param img
	 * 	The <@link Img> containing the data to measure.
	 * @param funcClass
	 * 	The class of the desired measurable function. Used to instantiate a new
	 * 	measurable function. 
	 * @return
	 * 	A measurable function.
	 */
	// OBSOLETE
	@SuppressWarnings("unchecked")
	public <T extends RealType<T>, C extends Function<PointSet,T>>
		Function<PointSet,T> getMeasureFunction(
			Img<? extends RealType<?>> img, Class<C> funcClass, T type)
	{
		// TODO - eliminate reflection code
		//   Ideally a Function should support a no-arg constructor and have a
		//   setOtherFunc(Function f) method. Then can safely initialize without
		//   reflection. Maybe need another interface that functions can implement
		//   that will allow function to be set later.
		//  Note that this seems problematic. A Function, being typed, likely cannot
		//   have a no-arg constructor. And some methods would have multi-arg
		//   constructors that wouldn't adhere to an interface. Multi-arg ctors are
		//   a problem for this whole service.
		@SuppressWarnings("rawtypes")
		RealImageFunction<?,T> data = new RealImageFunction(img, type);
		Constructor<?>[] ctors = funcClass.getConstructors();
		for (Constructor<?> ctor : ctors) {
			Class<?>[] paramTypes = ctor.getParameterTypes();
			if (paramTypes.length != 1) continue;
			if (!paramTypes[0].isAssignableFrom(data.getClass())) continue;
			try {
				return (Function<PointSet,T>) ctor.newInstance(data);
			}
			catch (Exception e) {
				log.error("Couldn't wrap function around data (exception thrown)");
				return null;
			}
		}
		log.error(
			"Couldn't wrap function around data (no suitable 1-arg constructor found)"
			);
		return null;
	}
	// OBSOLETE

	
	// OBSOLETE
	/**
	 * Makes a measurable function. Takes an <@link Dataset> and wraps a
	 * (presumably) statistical function around it. The function can then be
	 * queried for values.
	 * @param ds
	 * 	The <@link Dataset> containing the data to measure.
	 * @param funcClass
	 * 	The class of the desired measurable function. Used to instantiate a new
	 * 	measurable function. 
	 * @return
	 * 	A measurable function.
	 */
	// OBSOLETE
	public <T extends RealType<T>, C extends Function<PointSet,T>>
	Function<PointSet,T> getMeasureFunction(
		Dataset ds, Class<C> funcClass, T type)
	{
		return getMeasureFunction(ds.getImgPlus(), funcClass, type);
	}
	// OBSOLETE
	
	// OBSOLETE
	/**
	 * Computes a measurement from a set of points and a provided measurable
	 * {@link Function}. The measurable function is queried for its value.
	 *  
	 * @param ps
	 * The set of points (as a {@link PointSet}) to feed as input to the
	 * measurable function.
	 * @param func
	 * The measurable function of interest.
	 * @param output
	 * The variable that will be filled with the measurement value
	 */
	// OBSOLETE
	public <T> void measure(PointSet ps, Function<PointSet,T> func, T output)
	{
		func.compute(ps, output);
	}
	// OBSOLETE

	// OBSOLETE
	/**
	 * Computes a measurement from a set of points of a {@link Img}. The class
	 * of a measurable function is provided. From this class a measurable function
	 * is instantiated which refers to the {@link Img}. Then that measurable
	 * function is queried for its value.
	 *  
	 * @param img
	 * The {@link Img} to measure. 
	 * @param ps
	 * The set of points within the {@link Img} (as a {@link PointSet}) to
	 * measure.
	 * @param funcClass
	 * The class of the measurable function of interest.
	 * @param output
	 * The variable that will be filled with the measurement value
	 */
	// OBSOLETE
	public <T extends RealType<T>, C extends Function<PointSet,T>>
	void measure(Img<? extends RealType<?>> img,
								PointSet ps, Class<C> funcClass, T output)
	{
		Function<PointSet,T> func = getMeasureFunction(img, funcClass, output);
		measure(ps, func, output);
	}
	// OBSOLETE

	// OBSOLETE
	/**
	 * Computes a measurement from a set of points of a {@link Dataset}. The class
	 * of a measurable function is provided. From this class a measurable function
	 * is instantiated which refers to the {@link Dataset}. Then that measurable
	 * function is queried for its value.
	 *  
	 * @param ds
	 * The {@link Dataset} to measure. 
	 * @param ps
	 * The set of points within the {@link Dataset} (as a {@link PointSet}) to
	 * measure.
	 * @param funcClass
	 * The class of the measurable function of interest.
	 * @param output
	 * The variable that will be filled with the measurement value
	 */
	// OBSOLETE
	public <T extends RealType<T>, C extends Function<PointSet,T>>
	void measure(Dataset ds, PointSet ps, Class<C> funcClass, T output)
	{
		measure(ds.getImgPlus(), ps, funcClass, output);
	}
	// OBSOLETE

	// -- private helpers --

	// TODO: example interface that might be needed to avoid reflection
	
	@SuppressWarnings("unused")
	private interface SettableFunction<T> {
		void setFunction(Function<PointSet,T> function);
	}

	/*
	 * This code to become public API. It really does nothing of consequence.
	 */
	
	private <T> void measure(Function<PointSet,T> func, PointSet region, T output) {
		func.compute(region, output);
	}

	@SuppressWarnings({"unchecked","rawtypes"})
	private <T extends RealType<T>>
		RealImageFunction<?,T>
			imgFunction(Img<? extends RealType<?>> img, T outputType)
	{
		return new RealImageFunction(img, outputType);
	}

	private <T extends RealType<T>>
		RealImageFunction<?,T>
			imgFunction(Dataset ds, T outputType)
	{
		return imgFunction(ds.getImgPlus(), outputType);
	}

	/*
			------------------------------------------------------------------------
 			Here is an example that would be a measurement that can reuse values in
 			a specific way that the function wants. This can save some cpu cycles.
			------------------------------------------------------------------------
	 */
	private class MeanAndStdDev {
		private double mean, stdDev;
		public double getMean() { return mean; }
		public double getStdDev() { return stdDev; }
		public void setMean(double val) { mean = val; }
		public void setStdDev(double val) { stdDev = val; }
		public MeanAndStdDev create() { return new MeanAndStdDev(); }
	}
	
	private class MeanAndStdDevFunction<T extends RealType<T>>
		implements Function<PointSet,MeanAndStdDev>
	{
		private final Function<long[],T> otherFunc;
		private final MeanAndStdDev type;
		private final T tmp;
		private PointSet lastPointSet; 
		private PointSetIterator iter;
		
		public MeanAndStdDevFunction(Function<long[],T> func, T tmp,
			MeanAndStdDev type)
		{
			this.otherFunc = func;
			this.tmp = tmp.createVariable();
			this.type = type;
			this.iter = null;
		}
		
		@Override
		public void compute(PointSet input, MeanAndStdDev output) {
			if (iter == null || lastPointSet != input)
				iter = input.createIterator();
			else
				iter.reset();
			lastPointSet = input;
			PrimitiveDoubleArray data = new PrimitiveDoubleArray();
			while (iter.hasNext()) {
				long[] coord = iter.next();
				otherFunc.compute(coord, tmp);
				data.add(tmp.getRealDouble());
			}
			int numElems = data.size();
			double sumMean = 0;
			for (int i = 0; i < numElems; i++) {
				sumMean += data.get(i);
			}
			double mean = (numElems == 0) ? 0 : sumMean / numElems;
			double sumVariance = 0;
			for (int i = 0; i < numElems; i++) {
				double term = data.get(i) - sumMean;
				sumVariance += (term * term);
			}
			double variance = (numElems <= 1) ? 0 : sumVariance / (numElems-1);
			double stdDev = Math.sqrt(variance);
			output.setMean(mean);
			output.setStdDev(stdDev);
		}

		@Override
		public MeanAndStdDev createOutput() {
			return type.create();
		}

		@Override
		public Function<PointSet, MeanAndStdDev> copy() {
			return new MeanAndStdDevFunction<T>(otherFunc, tmp, type.create());
		}
		
	}

	private void example1() {
		Function<PointSet,MeanAndStdDev> func = null;
		PointSet region = null;
		MeanAndStdDev output = null;
		measure(func,region,output);
		System.out.println("mean    = " + output.getMean());
		System.out.println("std dev = " + output.getStdDev());
	}
	
	/*
	------------------------------------------------------------------------
		Here is an example of grabbing multiple measurements for the same set
		of input data (allowing one pass over big images).
	------------------------------------------------------------------------
	 */

	// Limitation: all the measures have to be of the same output type T
	
	private class MeasurementSet<T> {
		
		private List<String> names = new ArrayList<String>();
		private List<Function<PointSet,T>> funcs =
				new ArrayList<Function<PointSet,T>>();
		private List<T> variables = new ArrayList<T>();
		
		public int getNumMeasurements() {
			return names.size();
		}
		
		public void add(String name, Function<PointSet,T> func, T variable) {
			names.add(name);
			funcs.add(func);
			variables.add(variable);
		}
		
		public String getMeasurementName(int i) {
			return names.get(i);
		}
		
		public Function<PointSet,T> getFunction(int i) {
			return funcs.get(i);
		}
		
		public T getVariable(int i) {
			return variables.get(i);
		}
		
		public MeasurementSet<T> create() {
			return new MeasurementSet<T>();
		}
	}
	
	private class MultiMeasureFunction<T> implements Function<PointSet,MeasurementSet<T>> {

		private MeasurementSet<T> set;

		public MultiMeasureFunction(MeasurementSet<T> set) {
			this.set = set;
		}
		
		@Override
		public void compute(PointSet input, MeasurementSet<T> output) {
			for (int i = 0; i < output.getNumMeasurements(); i++) {
				T variable = output.getVariable(i); 
				output.getFunction(i).compute(input, variable);
			}
		}

		@Override
		public MeasurementSet<T> createOutput() {
			return set.create();
		}

		@Override
		public Function<PointSet, MeasurementSet<T>> copy() {
			return new MultiMeasureFunction<T>(set.create());
		}
		
	}
	
	private void example2() {
		PointSet region = null;
		
		MeasurementSet<ComplexDoubleType> measurements = null;
		MultiMeasureFunction<ComplexDoubleType> multimeasureFunc = null;
		measure(multimeasureFunc, region, measurements);
		for (int i = 0; i < measurements.getNumMeasurements(); i++) {
			System.out.println(measurements.getMeasurementName(i) + " = " +
					measurements.getVariable(i));
		}

		RealImageFunction<?, DoubleType> imgFunc = null;
		RealMinFunction<DoubleType> minFunc =
				new RealMinFunction<DoubleType>(imgFunc);
		RealMaxFunction<DoubleType> maxFunc =
				new RealMaxFunction<DoubleType>(imgFunc);
		RealArithmeticMeanFunction<DoubleType> avgFunc =
				new RealArithmeticMeanFunction<DoubleType>(imgFunc);
		MeasurementSet<DoubleType> mSet2 = new MeasurementSet<DoubleType>();
		mSet2.add("min", minFunc, new DoubleType());
		mSet2.add("max", maxFunc, new DoubleType());
		mSet2.add("avg", avgFunc, new DoubleType());
		MultiMeasureFunction<DoubleType> mFunc2 = null;
		measure(mFunc2, region, mSet2);
		for (int i = 0; i < mSet2.getNumMeasurements(); i++) {
			System.out.println(mSet2.getMeasurementName(i) + " = " +
					mSet2.getVariable(i));
		}
	}

	/*
	------------------------------------------------------------------------
		Example on how to invoke code
	------------------------------------------------------------------------
	 */
	
	@SuppressWarnings("unused")
	public void testMe() {
		DatasetService dsSrv = getContext().getService(DatasetService.class);
		Dataset ds = dsSrv.create(
			new long[]{5,5}, "junk", new AxisType[]{Axes.X, Axes.Y}, 8, false, false);
		Cursor<? extends RealType<?>> cursor = ds.getImgPlus().cursor();
		int i = 0;
		while (cursor.hasNext()) {
			cursor.next().setReal(i++);
		}
		PointSet pts = new HyperVolumePointSet(ds.getDims());
		DoubleType output = new DoubleType();
		// the desired version of code with less informative compiler warning
		measure(ds, pts, RealMinFunction.class, output);
		System.out.println("Min is " + output.getRealDouble());
		measure(ds, pts, RealMaxFunction.class, output);
		System.out.println("Max is " + output.getRealDouble());
		// version of code with more informative compiler warning (in Eclipse)
		RealMedianFunction<DoubleType> median = new RealMedianFunction<DoubleType>(null);
		measure(ds, pts, (Class<Function<PointSet,DoubleType>>)median.getClass(), output);
		System.out.println("Median is " + output.getRealDouble());
		// NOTE: this one should fail to construct since it has no 1-arg constructor
		// which points out a limitation with the whole service approach.
		measure(ds, pts, RealAdaptiveMedianFunction.class, output);
		System.out.println("Adaptive Median is " + output.getRealDouble());
	}
}
