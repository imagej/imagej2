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

import java.util.ArrayList;
import java.util.List;

import net.imglib2.ops.function.Function;
import net.imglib2.ops.pointset.PointSet;

/**
 * MeasurementSet is a collection of {@link Function}s and associated outputs.
 * It is used by the {@link MeasurementService} to measure multiple Functions at
 * a time. Note that a MeasurementSet is limited to one type of output. If
 * multiple output types are needed then use multiple MeasurementSets or create
 * your own aggregating class and associated Function (similar to
 * {@link BasicStats} and {@link BasicStatsFunction}).
 * 
 * @author Barry DeZonia
 * @param <T> The type of the output variables that are associated with the
 *          Functions to be measured.
 */
public class MeasurementSet<T> {

	// -- instance variables --

	private final List<Function<PointSet, T>> funcs =
		new ArrayList<Function<PointSet, T>>();
	private final List<T> variables = new ArrayList<T>();

	// -- constructor --

	public MeasurementSet() {}

	// -- MeasurementSet methods --

	/**
	 * Add a {@link Function} and an associated output to the MeasurementSet.
	 */
	public void add(final Function<PointSet, T> func, final T variable) {
		funcs.add(func);
		variables.add(variable);
	}

	/**
	 * Returns the number of measurements in the set.
	 */
	public int getNumMeasurements() {
		return funcs.size();
	}

	/**
	 * Returns the {@link Function} at position i within the MeasurementSet.
	 */
	public Function<PointSet, T> getFunction(final int i) {
		return funcs.get(i);
	}

	/**
	 * Returns the output at position i within the MeasurementSet.
	 */
	public T getVariable(final int i) {
		return variables.get(i);
	}

	/**
	 * Creates a new empty MeasurementSet.
	 */
	public MeasurementSet<T> create() {
		return new MeasurementSet<T>();
	}

}
