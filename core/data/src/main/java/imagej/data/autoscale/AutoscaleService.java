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

import imagej.plugin.SingletonService;

import java.util.List;
import java.util.Map;

import net.imglib2.IterableInterval;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.ops.util.Tuple2;
import net.imglib2.type.numeric.RealType;

/**
 * Interface for service that works with autoscale algorithms.
 * 
 * @author Barry DeZonia
 * @see AutoscaleMethod
 */
public interface AutoscaleService extends SingletonService<AutoscaleMethod> {

	/** Returns a map of available {@link AutoscaleMethod}s, indexed by name. */
	Map<String, AutoscaleMethod> getAutoscaleMethods();

	/**
	 * Returns the names of all available {@link AutoscaleMethod}s, ordered by
	 * priority.
	 */
	List<String> getAutoscaleMethodNames();

	/**
	 * Returns the {@link AutoscaleMethod} associated with the given name.
	 */
	AutoscaleMethod getAutoscaleMethod(String name);

	/**
	 * Returns the default autoscaling method.
	 */
	AutoscaleMethod getDefaultAutoscaleMethod();

	// TODO - remove this method?

	/**
	 * Calculates the range of interest from the data contained in the given
	 * {@link IterableInterval} using the default autoscale method.
	 * 
	 * @return The calculated range of values stored (min,max) in a Tuple2
	 */
	@SuppressWarnings("rawtypes")
	Tuple2<Double, Double> getDefaultIntervalRange(
		IterableInterval<? extends RealType<?>> interval);

	/**
	 * Calculates the range of interest from the data contained in the given
	 * {@link RandomAccessibleInterval} using the default autoscale method.
	 * 
	 * @return The calculated range of values stored (min,max) in a Tuple2
	 */
	@SuppressWarnings("rawtypes")
	Tuple2<Double, Double> getDefaultRandomAccessRange(
		RandomAccessibleInterval<? extends RealType<?>> interval);
}
