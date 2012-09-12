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

package imagej.core.commands.assign.noisereduce;

import imagej.ImageJ;
import imagej.data.Dataset;
import imagej.data.DefaultDataset;
import imagej.event.StatusService;

import net.imglib2.img.ImgPlus;
import net.imglib2.ops.function.Function;
import net.imglib2.ops.img.SerialImageAssignment;
import net.imglib2.ops.input.PointSetInputIterator;
import net.imglib2.ops.pointset.HyperVolumePointSet;
import net.imglib2.ops.pointset.PointSet;
import net.imglib2.type.numeric.RealType;

/**
 * TODO
 * 
 * @author Barry DeZonia
 */
public class Reducer<U extends RealType<U>,V extends RealType<V>>
{
	private final ImageJ context;
	
	private final Function<PointSet,V> inputFunction;
	
	private final PointSet neighborhood;
	
	private final ImgPlus<U> input;

	private final StatusService statusService; 
	
	// -- public interface --

	
	public Reducer(ImageJ context, ImgPlus<U> input, Function<PointSet,V> func,
		PointSet neighborhood)
	{
		this.context = context;
		this.input = input;
		this.inputFunction = func;
		this.neighborhood = neighborhood;
		this.statusService = context.getService(StatusService.class);
	}

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
		statusService.showStatus(neighDescrip + " ... beginning processing");
	}
	
	private void notifyUserAtEnd(String neighDescrip) {
		statusService.showStatus(neighDescrip + " ... completed processing");
	}
}
