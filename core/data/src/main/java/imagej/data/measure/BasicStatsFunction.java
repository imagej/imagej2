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

import net.imglib2.ops.function.Function;
import net.imglib2.ops.function.real.PrimitiveDoubleArray;
import net.imglib2.ops.pointset.PointSet;
import net.imglib2.ops.pointset.PointSetIterator;
import net.imglib2.type.numeric.RealType;


/**
 * BasicStatsFunction is a {@link Function} that will calculate basic statistics
 * from another input Function.
 * <p>
 * This Function can be seen as an example of a Function that computes multiple
 * output values at one time. Another method available is via the {@link
 * MeasurementService} API.
 * <p>
 * The advantage of a method like this is that such custom Functions can reuse
 * their computations internally as needed to improve performance.
 * 
 * @author Barry DeZonia
 *
 * @param <T>
 * The backing type of the input Function. 
 * 
 */
public class BasicStatsFunction<T extends RealType<T>>
	implements Function<PointSet,BasicStats>
{
	// -- instance variables --
	
	private final Function<long[],T> otherFunc;
	private final T tmp;
	private final PrimitiveDoubleArray data;
	private PointSet lastPointSet; 
	private PointSetIterator iter;
	
	// -- constructor --

	/**
	 * Creates a BasicStatsFunction on another {@link Function}.
	 * @param func
	 * The other Function to compute the BasicStats of.
	 * @param tmp
	 * A variable of the type of the other Function that can be used for temporary
	 * calculations.
	 */
	public BasicStatsFunction(Function<long[],T> func, T tmp)
	{
		this.otherFunc = func;
		this.tmp = tmp.createVariable();
		this.data = new PrimitiveDoubleArray();
		this.lastPointSet = null;
		this.iter = null;
	}
	
	// -- Function methods --

	// TODO - this was written as an example. Check actual definitions at
	// mathworld.com to make sure our stat definitions are correct.
	
	/**
	 * Computes the {@link BasicStats} of the other {@link Function} over the
	 * input region {@PointSet}.
	 */
	@Override
	public void compute(PointSet input, BasicStats output) {
		if (iter == null || lastPointSet != input)
			iter = input.createIterator();
		else
			iter.reset();
		lastPointSet = input;
		data.clear();
		double sumMean = 0;
		while (iter.hasNext()) {
			long[] coord = iter.next();
			otherFunc.compute(coord, tmp);
			// for some functions the previous compute() could be expensive (think of
			// virtual Imgs for instance) and thus cache values and use them in later
			// calculations.
			data.add(tmp.getRealDouble());
			sumMean += tmp.getRealDouble();
		}
		int numElems = data.size();
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

	/**
	 * Creates a {@link BasicStats}. This is part of the {@link Function} api and
	 * helps support parallelization. 
	 */
	@Override
	public BasicStats createOutput() {
		return new BasicStats();
	}
	
	/**
	 * Creates a copy of this BasicStatsFunction. This is part of the {@link
	 * Function} api and helps support parallelization. 
	 */
	@Override
	public Function<PointSet, BasicStats> copy() {
		return new BasicStatsFunction<T>(otherFunc, tmp);
	}
	
}
