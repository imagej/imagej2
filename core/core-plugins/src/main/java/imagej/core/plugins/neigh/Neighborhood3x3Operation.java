//
// Neighborhood3x3Operation.java
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

package imagej.core.plugins.neigh;

import imagej.data.Dataset;
import imagej.data.Extents;
import imagej.data.Position;
import imagej.util.IntRect;
import net.imglib2.Cursor;
import net.imglib2.RandomAccess;
import net.imglib2.RandomAccessible;
import net.imglib2.img.Img;
import net.imglib2.type.numeric.RealType;
import net.imglib2.view.Views;

/**
 * Neighborhood3x3Operation - a helper class for 3x3 neighborhood operation
 * plugins such as SmoothDataValues, SharpenDataValues, and FindEdges. Does
 * the work of communicating with a Neighborhood3x3Watcher.
 * 
 * @author Barry DeZonia
 */
public class Neighborhood3x3Operation {

	// -- instance variables --

	private Dataset input;
	private Img<? extends RealType<?>> inputImage;
	private Img<? extends RealType<?>> inputImageCopy;
	private IntRect selection;
	private Neighborhood3x3Watcher watcher;

	// -- constructor --

	public Neighborhood3x3Operation(Dataset input, Neighborhood3x3Watcher watcher)
	{
		this.input = input;
		this.watcher = watcher;

		if (watcher == null) throw new IllegalArgumentException(
			"neighborhood watcher cannot be null!");
	}

	// -- public interface --

	public void run()
	{
		checkInput();
		setupWorkingData();
		runAssignment();
	}

	// -- private interface --

	/**
	 * Make sure we have an input image and that it's dimensionality is correct
	 */
	private void checkInput() {
		if (input == null)
			throw new IllegalArgumentException("input Dataset is null");
		
		if (input.getImgPlus() == null)
			throw new IllegalArgumentException("input Img is null");

		//if (input.getImage().numDimensions() != 2)
		//	throw new IllegalArgumentException("input image is not 2d but has " + input.getImage().numDimensions() + " dimensions");
	}


	private void setupWorkingData()
	{
		inputImage = input.getImgPlus();
		inputImageCopy = cloneImage(inputImage);
		selection = input.getSelection();
	}

	private void runAssignment()
	{
		long[] planeDims = new long[inputImage.numDimensions() - 2];
		for (int i = 0; i < planeDims.length; i++)
			planeDims[i] = inputImage.dimension(i+2);
		if (planeDims.length == 0) { // dataset is 2d only
			applyOperationToPlane(new long[]{});
		}
		else {
			Extents extents = new Extents(planeDims);
			Position pos = extents.createPosition();
			long[] planeIndex = new long[planeDims.length];
			long totalPlanes = extents.numElements();
			for (long plane = 0; plane < totalPlanes; plane++) {
				pos.setIndex(plane);
				pos.localize(planeIndex);
				applyOperationToPlane(planeIndex);
			}
		}
		input.update();
	}
	
	private void applyOperationToPlane(long[] planeIndex) {

		long[] imageDims = new long[inputImage.numDimensions()];
		inputImage.dimensions(imageDims);
		
		if (selection.width == 0)
			selection.width = (int) imageDims[0];

		if (selection.height == 0)
			selection.height = (int) imageDims[1];

		// output is done by changin input image in place
		RandomAccess<? extends RealType<?>> outputAccessor =
			inputImage.randomAccess();

		// input is a copy of the original data with out of bounds access enabled
		RandomAccessible<? extends RealType<?>> inputInterval =
			Views.extendMirrorSingle(inputImageCopy);

		RandomAccess<? extends RealType<?>> extendedInput =
			inputInterval.randomAccess();

		// initialize the watcher
		watcher.setup();
		
		long[] inputPosition = new long[imageDims.length];
		long[] localInputPosition = new long[imageDims.length];

		for (int i = 2; i < inputPosition.length; i++) {
			inputPosition[i] = planeIndex[i-2];
			localInputPosition[i] = planeIndex[i-2];
		}

		for (long y = selection.y; y < selection.height; y++) {
			inputPosition[1] = y;
			for (long x = selection.x; x < selection.width; x++) {
				inputPosition[0] = x;
				watcher.initializeNeighborhood(inputPosition);
	
				for (int dy = -1; dy <= 1; dy++) {
					localInputPosition[1] = inputPosition[1] + dy;
					for (int dx = -1; dx <= 1; dx++) {
						localInputPosition[0] = inputPosition[0] + dx;
						extendedInput.setPosition(localInputPosition);
						double localValue = extendedInput.get().getRealDouble();
						watcher.visitLocation(dx, dy, localValue);
					}
				}
				// assign output
				outputAccessor.setPosition(inputPosition);
				outputAccessor.get().setReal(watcher.calcOutputValue());
			}
		}
	}

	// TODO - eliminate when Imglib allows ability to duplicate/clone an Img
	// TODO - find a way to eliminate use of raw types here
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private Img<? extends RealType<?>> cloneImage(Img image) {
		// TODO - used to be able to call Image::clone()
		//  For now copy data by hand

		long[] dimensions = new long[image.numDimensions()];
		image.dimensions(dimensions);
		
		Img<? extends RealType<?>> copyOfImg =
			image.factory().create(dimensions, image.firstElement());
		
		long[] position = new long[dimensions.length];
		
		Cursor<? extends RealType<?>> cursor = image.localizingCursor();

		RandomAccess<? extends RealType<?>> access = copyOfImg.randomAccess();
		
		while (cursor.hasNext()) {
			cursor.next();
			double currValue = cursor.get().getRealDouble();
			cursor.localize(position);
			access.setPosition(position);
			access.get().setReal(currValue);
		}
		
		return copyOfImg;
	}
}
