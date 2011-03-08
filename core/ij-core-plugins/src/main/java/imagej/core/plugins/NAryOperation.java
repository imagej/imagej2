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

package imagej.core.plugins;

import imagej.model.Dataset;

import imglib.ops.function.RealFunction;
import imglib.ops.operation.AssignOperation;

import java.util.ArrayList;
import java.util.List;

import mpicbg.imglib.cursor.Cursor;
import mpicbg.imglib.image.Image;
import mpicbg.imglib.type.numeric.integer.UnsignedShortType;

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
	 * the list of input Datasets that will be fed as input to the user supplied
	 * function
	 */
	private List<Dataset> inputs;

	/**
	 * the output Dataset that will be filled with computation of user supplied
	 * function from inputs
	 */
	private Dataset output;

	/** The imglib-ops function to execute. */
	private RealFunction function;

	// -- constructors --

	/**
	 * this constructor a convenience for those plugins that work from a single
	 * input Dataset
	 */
	public NAryOperation(Dataset input, RealFunction function) {
		this.inputs = new ArrayList<Dataset>();
		this.inputs.add(input);
		this.function = function;
		this.output = null;
		if (!function.canAccept(1)) throw new IllegalArgumentException(
			"NAryOperation constructor - given function cannot accept a single input");

		if (input == null) // TODO - temporary code to test these until IJ2
		// plugins can correctly fill a List<Dataset>
		// @Parameter
		{
			Image<UnsignedShortType> junkImage =
				Dataset.createPlanarImage("", new UnsignedShortType(), new int[] { 200,
					200 });
			Cursor<UnsignedShortType> cursor = junkImage.createCursor();
			int index = 0;
			for (UnsignedShortType pixRef : cursor)
				pixRef.set(index++);
			cursor.close();
			inputs = new ArrayList<Dataset>();
			inputs.add(new Dataset(junkImage));
		}

	}

	/**
	 * this constructor a convenience for those plugins that work from a two input
	 * Datasets
	 */
	public NAryOperation(Dataset input1, Dataset input2, RealFunction function) {
		this.inputs = new ArrayList<Dataset>();
		this.inputs.add(input1);
		this.inputs.add(input2);
		this.function = function;
		this.output = null;
		if (!function.canAccept(2)) throw new IllegalArgumentException(
			"NAryOperation constructor - given function cannot accept two inputs");

		if (input1 == null) // TODO - temporary code to test these until IJ2
		// plugins can correctly fill a List<Dataset>
		// @Parameter
		{
			Image<UnsignedShortType> junkImage1 =
				Dataset.createPlanarImage("", new UnsignedShortType(), new int[] { 200,
					200 });
			Cursor<UnsignedShortType> cursor = junkImage1.createCursor();
			int index = 0;
			for (UnsignedShortType pixRef : cursor)
				pixRef.set(index++);
			cursor.close();

			inputs.set(0, new Dataset(junkImage1));
		}

		if (input2 == null) // TODO - temporary code to test these until IJ2
		// plugins can correctly fill a List<Dataset>
		// @Parameter
		{
			Image<UnsignedShortType> junkImage2 =
				Dataset.createPlanarImage("", new UnsignedShortType(), new int[] { 200,
					200 });
			Cursor<UnsignedShortType> cursor = junkImage2.createCursor();
			int index = 0;
			for (UnsignedShortType pixRef : cursor)
				pixRef.set(65535 - index++);
			cursor.close();

			inputs.set(1, new Dataset(junkImage2));
		}
	}

	/**
	 * takes a List of Datasets as input, a Dataset to store results in (can be
	 * null) and a function to be applied
	 */
	public NAryOperation(List<Dataset> inputs, RealFunction function) {
		this.inputs = inputs;
		this.function = function;
		this.output = null;
		if (!function.canAccept(inputs.size())) throw new IllegalArgumentException(
			"NAryOperation constructor - given function cannot accept " +
				inputs.size() + " inputs");
	}

	// -- public interface --

	/**
	 * helper method that allows output Dataset of an operation to be set or
	 * changed
	 */
	public void setOutput(Dataset output) {
		this.output = output;
	}

	/**
	 * runs the plugin applying the operation's function to the input and
	 * assigning it to the output
	 */
	public Dataset run() {
		if (function == null) throw new IllegalStateException(
			"NAryOperation::run() - function reference is improperly initialized (null)");

		// @SuppressWarnings("unchecked")
		final Image[] inputImages = new Image[inputs.size()];

		for (int i = 0; i < inputImages.length; i++) {
			inputImages[i] = imageFromDataset(inputs.get(i));
		}

		final Image outputImage;
		if (output != null) outputImage = imageFromDataset(output);
		else outputImage = zeroDataImageWithSameAttributes(inputImages[0]);
		// TODO - must be given at least one input image or prev line will throw
		// null ptr exception

		final AssignOperation operation =
			new AssignOperation(inputImages, outputImage, function);

		operation.execute();

		if (output != null) return output;
		else return datasetFromImage(outputImage);
	}

	// -- private interface --

	/** make an image that has same type and dimensions as Dataset */
	private Image imageFromDataset(Dataset dataset) {
		// @SuppressWarnings("unchecked")
		return dataset.getImage();
	}

	/** make an image that has same type, container, and dimensions as refImage */
	private Image zeroDataImageWithSameAttributes(Image refImage) {
		return refImage.createNewImage(refImage.getDimensions());
	}

	/** make a Dataset from an Image */
	private Dataset datasetFromImage(Image image) {
		return new Dataset(image);
	}

}
