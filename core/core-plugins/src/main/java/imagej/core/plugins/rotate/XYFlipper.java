//
// XYFlipper.java
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

package imagej.core.plugins.rotate;

import imagej.core.plugins.imglib.OutputAlgorithm;
import imagej.data.Dataset;
import imagej.data.Extents;
import imagej.data.Position;
import imagej.util.IntRect;
import net.imglib2.RandomAccess;
import net.imglib2.img.Img;
import net.imglib2.type.numeric.RealType;

// TODO - in IJ1 this flips single plane in active window. do we want to extend to all planes???

// TODO - XYFlipper could be renamed to something else. It takes XY data and transforms it some other space */

/**
 * XYFlipper is used by FlipVertically, FlipHorizontally, Rotate90DegreesLeft
 * and Rotate90DegreesRight
 * 
 * @author Barry DeZonia
 */
@SuppressWarnings({"rawtypes","unchecked"})
public class XYFlipper implements OutputAlgorithm {

	// -- instance variables --

	private Dataset input;

	private String errMessage = "No error";

	private Img<? extends RealType<?>> outputImage;

	private FlipCoordinateTransformer flipper;
	
	private long[] inputDimensions;

	private RandomAccess<? extends RealType<?>> inputAccessor;
	
	private RandomAccess<? extends RealType<?>> outputAccessor;
	
	// -- exported interface --

	/**
	 * This interface is exported for use by algorithms that want to create images
	 * from 2d input data
	 */
	interface FlipCoordinateTransformer {

		/**
		 * Maps an input image's dimensions to the output image's coordinate space
		 */
		long[] calcOutputDimensions(long[] inputDimensions);

		/**
		 * Maps a position within an input image's coordinate space to the output
		 * image's coordinate space
		 */
		void calcOutputPosition(long[] inputDimensions, long[] inputPosition,
			long[] outputPosition);
		
		/**
		 * Returns if this transformation does not reorder X & Y axes
		 */
		boolean isShapePreserving();
	}

	// -- constructor --

	public XYFlipper(Dataset input, FlipCoordinateTransformer flipper) {
		this.input = input;
		this.flipper = flipper;
	}

	// -- public interface : implementation of OutputAlgorithm methods --

	/**
	 * Makes sure input is okay and creates output image */
	@Override
	public boolean checkInput() {
		Img inputImage = input.getImgPlus();  // TODO - raw type required here
		
		inputDimensions = new long[inputImage.numDimensions()];

		inputImage.dimensions(inputDimensions);
		
		long[] outputDimensions = flipper.calcOutputDimensions(inputDimensions);

		outputImage = inputImage.factory().create(outputDimensions, inputImage.firstElement());

		return true;
	}

	/**
	 * Returns the current error message. only valid of checkInput() returns false
	 */
	@Override
	public String getErrorMessage() {
		return errMessage;
	}

	/**
	 * Fills the output image from the input image doing coordinate
	 * transformations as needed
	 */
	@Override
	public boolean process() {
		Img<? extends RealType<?>> inputImage = input.getImgPlus();

		inputAccessor = inputImage.randomAccess();
		outputAccessor = outputImage.randomAccess();

		long width = inputDimensions[0];
		long height = inputDimensions[1];
		
		IntRect selectedRegion = input.getSelection();
		
		int rx, ry, rw, rh;
		
		if (flipper.isShapePreserving() &&
				(selectedRegion.width > 0) &&
				(selectedRegion.height > 0)) {
			rx = selectedRegion.x;
			ry = selectedRegion.y;
			rw = selectedRegion.width;
			rh = selectedRegion.height;
		}
		else {
			rx = 0;
			ry = 0;
			rw = (int)width;
			rh = (int)height;
		}

		long[] planeDims = new long[inputImage.numDimensions()-2];
		for (int i = 0; i < planeDims.length; i++)
			planeDims[i] = inputDimensions[i+2];
		Extents extents = new Extents(planeDims);
		Position planePos = extents.createPosition();
		if (planeDims.length == 0) { // 2d Dataset
			processPlane(planePos, rx, ry, rw, rh);
		}
		else {  // more than two dimensions
			long totalPlanes = extents.numElements();
			for (long pNum = 0; pNum < totalPlanes; pNum++) {
				planePos.setIndex(pNum);
				processPlane(planePos, rx, ry, rw, rh);
			}
		}
		return true;
	}

	/**
	 * Returns the resulting output image
	 */
	@Override
	public Img<? extends RealType<?>> getResult() {
		return outputImage;
	}
	
	private void processPlane(Position planePos, int rx, int ry, int rw, int rh) {
		
		long[] inputPosition = new long[planePos.numDimensions()+2];
		long[] outputPosition = new long[planePos.numDimensions()+2];

		for (int i = 2; i < inputPosition.length; i++)
			inputPosition[i] = planePos.getLongPosition(i-2);
		
		for (int y = ry; y < rh; y++) {
			inputPosition[1] = y;

			for (int x = rx; x < rw; x++) {
				inputPosition[0] = x;

				flipper.calcOutputPosition(inputDimensions, inputPosition,
					outputPosition);

				inputAccessor.setPosition(inputPosition);
				outputAccessor.setPosition(outputPosition);

				double value = inputAccessor.get().getRealDouble();

				outputAccessor.get().setReal(value);
			}
		}
	}
}
