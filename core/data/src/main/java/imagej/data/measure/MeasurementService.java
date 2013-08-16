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

import java.util.List;

import net.imglib2.img.Img;
import net.imglib2.ops.function.Function;
import net.imglib2.ops.function.real.RealImageFunction;
import net.imglib2.ops.pointset.PointSet;
import net.imglib2.type.numeric.RealType;

// TODO - this service and all related classes do not have to be in the
// imagej.data package (ij-data subproject). There is only the one reliance on
// Dataset that is only for a convenience function that can reside elsewhere.
// Maybe MeasurementService should move to ij-core and we eliminate the Dataset
// convenience function from this class. Of course that would make core reliant
// on Imglib2 OPS.

/**
 * A service that simplifies the measurement of values from data.
 * 
 * @author Barry DeZonia
 */
public interface MeasurementService extends IJService {

	/**
	 * Measures the value of a {@link Function} given an input region
	 * {@link PointSet} and places it in a given output value.
	 * 
	 * @param func The function to measure.
	 * @param region The set of points over which to evaluate the function.
	 * @param output The variable to place the measurement result in.
	 */
	<T> void measure(Function<PointSet, T> func, PointSet region, T output);

	/**
	 * Measures the values of a list of {@link Function}s given an input region
	 * {@link PointSet} and places the computed values in the given output list.
	 * 
	 * @param funcs The list of functions to measure.
	 * @param region The set of points over which to evaluate the functions.
	 * @param outputs The list of variables to place the measurement results in.
	 */
	<T> void measure(List<Function<PointSet, T>> funcs, PointSet region,
		List<T> outputs);

	/**
	 * Creates a {@link RealImageFunction} from an {@link Img} and a given output
	 * type. This is a convenience constructor. RealImageFunctions give read
	 * access to Img data.
	 * 
	 * @param img The Img containing the data values we want read access to.
	 * @param outputType The type of output that the wrapped Function will fill
	 *          during computation.
	 * @return A Function wrapping the Img.
	 */
	<T extends RealType<T>> RealImageFunction<?, T> imgFunction(
		Img<? extends RealType<?>> img, T outputType);

	/**
	 * Creates a {@link RealImageFunction} from an {@link Dataset} and a given
	 * output type. This is a convenience constructor. RealImageFunctions give
	 * read access to data.
	 * 
	 * @param ds The Dataset containing the data values we want read access to.
	 * @param outputType The type of output that the wrapped Function will fill
	 *          during computation.
	 * @return A Function wrapping the Dataset data.
	 */
	<T extends RealType<T>> RealImageFunction<?, T> imgFunction(Dataset ds,
		T outputType);

}
