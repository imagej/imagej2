//
// NAryOperation.java
//

/*
ImageJ software for multidimensional image processing and analysis.

Copyright (c) 2010, ImageJDev.org.
All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met:
    * Redistributions of source code must retain the above copyright
      notice, this list of conditions and the following disclaimer.
    * Redistributions in binary form must reproduce the above copyright
      notice, this list of conditions and the following disclaimer in the
      documentation and/or other materials provided with the distribution.
    * Neither the names of the ImageJDev.org developers nor the
      names of its contributors may be used to endorse or promote products
      derived from this software without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
POSSIBILITY OF SUCH DAMAGE.
*/

package imagej.core.plugins.assign;

import imagej.data.Dataset;

import java.util.ArrayList;
import java.util.List;

import net.imglib2.img.ImgPlus;
import net.imglib2.ops.function.RealFunction;
import net.imglib2.ops.operation.AssignOperation;
import net.imglib2.type.numeric.RealType;

/**
 * Assigns an output Dataset's values by applying a RealFunction to a number of
 * input Datasets. Has a number of convenience constructors for working with 1,
 * 2, or n input Datasets. The user specified RealFunction must accept the same
 * number of parameters as the number of input Datasets.
 * 
 * @author Barry DeZonia
 * @author Curtis Rueden
 */
public class NAryOperation {

	// -- instance variables --

	/**
	 * The list of input Datasets that will be fed as input to the user supplied
	 * function
	 */
	private final List<Dataset> inputs;

	/**
	 * The output Dataset that will be filled with computation of user supplied
	 * function from inputs
	 */
	private Dataset output;

	/**
	 * The imglib-ops function to execute. */
	private final RealFunction function;

	/**
	 * Subregion information used to support working on subregions of inputs */
	private long[] outputOrigin, outputSpan;
	private long[][] inputOrigins, inputSpans;

	// -- constructors --

	/**
	 * This constructor a convenience for those plugins that work from a single
	 * input Dataset
	 */
	public NAryOperation(final Dataset input, final RealFunction function) {
		this.inputs = new ArrayList<Dataset>();
		this.inputs.add(input);
		this.function = function;
		this.output = null;
		if (!function.canAccept(1)) throw new IllegalArgumentException(
			"NAryOperation constructor - given function cannot accept a single input");

		initializeSubregionVariables();
	}

	/**
	 * This constructor a convenience for those plugins that work from a two input
	 * Datasets
	 */
	public NAryOperation(final Dataset input1, final Dataset input2,
		final RealFunction function)
	{
		this.inputs = new ArrayList<Dataset>();
		this.inputs.add(input1);
		this.inputs.add(input2);
		this.function = function;
		this.output = null;
		if (!function.canAccept(2)) throw new IllegalArgumentException(
			"NAryOperation constructor - given function cannot accept two inputs");

		initializeSubregionVariables();
	}

	/**
	 * Takes a List of {@link Dataset}s as input, a {@link Dataset} to store
	 * results in (can be null) and a function to be applied.
	 */
	public NAryOperation(final List<Dataset> inputs,
		final RealFunction function)
	{
		this.inputs = inputs;
		this.function = function;
		this.output = null;
		if (!function.canAccept(inputs.size())) throw new IllegalArgumentException(
			"NAryOperation constructor - given function cannot accept " +
				inputs.size() + " inputs");

		initializeSubregionVariables();
	}

	// -- public interface --

	/**
	 * Sets the output {@link Dataset} of an operation. */
	public void setOutput(final Dataset output) {
		this.output = output;
	}

	public void setOutputRegion(final long[] origin, final long[] span) {
		outputOrigin = origin;
		outputSpan = span;
	}

	public void setInputRegion(final int i, final long[] origin,
		final long[] span)
	{
		inputOrigins[i] = origin;
		inputSpans[i] = span;
	}

	/**
	 * Runs the plugin applying the operation's function to the input and
	 * assigning it to the output.
	 */
	public Dataset run() {
		if (function == null) {
			throw new NullPointerException("function reference is null");
		}

		final List<ImgPlus<? extends RealType<?>>> inputImages =
			new ArrayList<ImgPlus<? extends RealType<?>>>();

		for (int i = 0; i < inputs.size(); i++) {
			final Dataset dataset = inputs.get(i);
			inputImages.add(dataset.getImgPlus());
		}

		// TODO - must be given at least one input image or next line will fail
		if (output == null) output = inputs.get(0).duplicateBlank();

		final ImgPlus<? extends RealType<?>> outputImage = output.getImgPlus();

		final AssignOperation operation =
			new AssignOperation(inputImages, outputImage, function);

		operation.setOutputRegion(outputOrigin, outputSpan);
		for (int i = 0; i < inputImages.size(); i++) {
			operation.setInputRegion(i, inputOrigins[i], inputSpans[i]);
		}

		operation.execute();

		output.update();

		return output;
	}

	// -- private interface --

	/**
	 * Sets up subregion variables to default values. */
	private void initializeSubregionVariables() {
		ImgPlus<? extends RealType<?>> image = inputs.get(0).getImgPlus();
		outputOrigin = new long[image.numDimensions()];
		outputSpan = new long[image.numDimensions()];
		image.dimensions(outputSpan);
		final int numInputs = inputs.size();
		inputOrigins = new long[numInputs][];
		inputSpans = new long[numInputs][];
		for (int i = 0; i < numInputs; i++) {
			image = inputs.get(i).getImgPlus();
			inputOrigins[i] = new long[image.numDimensions()];
			inputSpans[i] = new long[image.numDimensions()];
			image.dimensions(inputSpans[i]);
		}
	}

}
