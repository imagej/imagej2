/*
 * #%L
 * ImageJ software for multidimensional image processing and analysis.
 * %%
 * Copyright (C) 2009 - 2014 Board of Regents of the University of
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
 * #L%
 */

package imagej.plugins.commands.assign.noisereduce;

import net.imagej.Dataset;
import net.imagej.DefaultDataset;
import net.imglib2.meta.ImgPlus;
import net.imglib2.ops.function.Function;
import net.imglib2.ops.img.SerialImageAssignment;
import net.imglib2.ops.input.PointSetInputIterator;
import net.imglib2.ops.pointset.HyperVolumePointSet;
import net.imglib2.ops.pointset.PointSet;
import net.imglib2.type.numeric.RealType;

import org.scijava.Context;
import org.scijava.app.StatusService;
import org.scijava.plugin.Parameter;

/**
 * Reduces noise in an {@link ImgPlus} using a given reduction func across a
 * given neighborhood.
 * 
 * @author Barry DeZonia
 */
public class Reducer<U extends RealType<U>,V extends RealType<V>>
{
	private final Context context;
	
	private final Function<PointSet,V> inputFunction;
	
	private final PointSet neighborhood;
	
	private final ImgPlus<U> input;

	@Parameter(required = false)
	private StatusService statusService;
	
	// -- public interface --

	
	public Reducer(Context context, ImgPlus<U> input, Function<PointSet,V> func,
		PointSet neighborhood)
	{
		this.context = context;
		context.inject(this);
		this.input = input;
		this.inputFunction = func;
		this.neighborhood = neighborhood;
	}

	// NOTE - because the neighborhood could be a spherical neighborhood that
	// relies on a WithinRadiusOfPointCondition we cannot parallelize this
	// algorithm. If we did we'd get one point per Thread with only one being
	// updated correctly. One can see by trial that using a regular
	// ImageAssignment here results in only a portion of the image getting noise
	// reduced.
	
	public Dataset reduceNoise(String neighDescrip) {
		notifyUserAtStart(neighDescrip);
		ImgPlus<U> newImg = input.copy();
		long[] dims = new long[newImg.numDimensions()];
		newImg.dimensions(dims);
		PointSet space = new HyperVolumePointSet(dims);
		PointSetInputIterator inputIterator =
				new PointSetInputIterator(space, neighborhood);
		long[] outputOrigin = new long[input.numDimensions()];
		long[] outputSpan = outputOrigin.clone();
		input.dimensions(outputSpan);
		// NB - regular ImageAssignement won't work here for radial neighborhood due
		// to the duplication of neighborhoods for parallelization and its
		// interference with the WithinRadiusOfPointSetOriginCondition.
		SerialImageAssignment<U,V,PointSet> assigner =
				new SerialImageAssignment<U,V,PointSet>(
					newImg,
					inputFunction,
					inputIterator,
					null);
		assigner.assign();
		notifyUserAtEnd(neighDescrip);
		return new DefaultDataset(context, newImg);
	}

	// -- private interface --
	
	private void notifyUserAtStart(String neighDescrip) {
		if (statusService != null)
			statusService.showStatus(neighDescrip + " ... beginning processing");
	}
	
	private void notifyUserAtEnd(String neighDescrip) {
		if (statusService != null)
			statusService.showStatus(neighDescrip + " ... completed processing");
	}
}
