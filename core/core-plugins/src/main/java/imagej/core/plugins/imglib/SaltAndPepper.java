//
// SaltAndPepper.java
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

package imagej.core.plugins.imglib;

import java.util.Random;

import mpicbg.imglib.algorithm.OutputAlgorithm;
import mpicbg.imglib.cursor.Cursor;
import mpicbg.imglib.cursor.LocalizableByDimCursor;
import mpicbg.imglib.image.Image;
import mpicbg.imglib.type.numeric.RealType;
import mpicbg.imglib.type.numeric.integer.UnsignedByteType;
import imagej.Rect;
import imagej.model.Dataset;
import imagej.plugin.ImageJPlugin;
import imagej.plugin.Menu;
import imagej.plugin.Parameter;
import imagej.plugin.Plugin;

// TODO - IJ1's implementation works on the current ROI rectangle. This plugin works on whole plane

/**
 * Implements the same functionality as IJ1's Salt and Pepper plugin. Assigns
 * random pixels to 255 or 0. 0 and 255 assignments are each evenly balanaced at
 * 2.5% of the image. Conforms to the Imglib OutputAlgorithm interface.
 * 
 * @author Barry DeZonia
 */
@Plugin(menu = {
	@Menu(label = "Process", mnemonic = 'p'),
	@Menu(label = "Noise", mnemonic = 'n'),
	@Menu(label = "Salt and Pepper", weight = 3) })
public class SaltAndPepper implements ImageJPlugin {

	// -- instance variables that are Parameters --

	@Parameter
	private Dataset input;

	@Parameter(output = true)
	private Dataset output;

	// -- public interface --

	@Override
	public void run() {
		ImglibOutputAlgorithmRunner runner =
			new ImglibOutputAlgorithmRunner(new SaltAndPepperAlgorithm());
		output = runner.run();
	}

	// -- private interface --

	/**
	 * implementation of the salt and pepper algorithm as an Imglib
	 * OutputAlgorithm
	 */
	private class SaltAndPepperAlgorithm implements OutputAlgorithm {

		// -- instance variables --

		private Image<?> inputImage;
		private Image<?> outputImage;
		private String errMessage = "No error";
		private Rect selection;
		
		// working cursor
		private LocalizableByDimCursor<? extends RealType<?>> outputCursor;
		private int[] outputPosition; // workspace for setting output position

		/** make sure input is 2d */
		@Override
		public boolean checkInput() {
			if (input == null) // TODO - remove later
			{
				Image<UnsignedByteType> junkImage =
					Dataset.createPlanarImage("", new UnsignedByteType(), new int[] {
						200, 200 });
				Cursor<UnsignedByteType> cursor = junkImage.createCursor();
				int index = 0;
				for (UnsignedByteType pixRef : cursor)
					pixRef.set((index++) % 256);
				cursor.close();
				input = new Dataset(junkImage);
				input.setSelection(20, 30, 150, 175);
			}

			selection = input.getSelection();

			inputImage = input.getImage();

			if (inputImage.getNumDimensions() != 2) {
				errMessage = "Only 2d images supported";
				return false;
			}

			outputImage = inputImage.createNewImage();

			initOutputImageVariables();

			return true;
		}

		/** get error message - really only valid if checkInput() returns false */
		@Override
		public String getErrorMessage() {
			return errMessage;
		}

		/**
		 * assigns the output image from the input image replacing 5% of the pixels
		 * with 0 or 255.
		 */
		@Override
		public boolean process() {
			Random rng = new Random();

			rng.setSeed(System.currentTimeMillis());

			double percentToChange = 0.05;

			long numPixels = (long) (inputImage.getNumPixels() * percentToChange);

			int ox = selection.x;
			int oy = selection.y;
			int w = selection.width;
			int h = selection.height;
			
			if (w <= 0) w = inputImage.getDimension(0);
			if (h <= 0) h = inputImage.getDimension(1);
			
			for (long p = 0; p < numPixels / 2; p++) {
				int randomX, randomY;

				randomX = ox + rng.nextInt(w);
				randomY = oy + rng.nextInt(h);
				setOutputPixel(randomX, randomY, 255);

				randomX = ox + rng.nextInt(w);
				randomY = oy + rng.nextInt(h);
				setOutputPixel(randomX, randomY, 0);
			}

			outputCursor.close(); // FINALLY close working cursor

			return true;
		}

		/**
		 * returns the output image created by this algorithm. nonexistent before
		 * checkInput(0 called. not valid before process() run.
		 */
		@Override
		public Image<?> getResult() {
			return outputImage;
		}

		// -- private helper --

		/**
		 * copies the input image's values to the out image as is. also leaves
		 * outputCursor open for later use
		 */
		private void initOutputImageVariables() {
			LocalizableByDimCursor<? extends RealType<?>> inputCursor =
				(LocalizableByDimCursor<? extends RealType<?>>) inputImage
					.createLocalizableByDimCursor();

			outputCursor =
				(LocalizableByDimCursor<? extends RealType<?>>) outputImage
					.createLocalizableByDimCursor();

			outputPosition = outputImage.createPositionArray();

			while (inputCursor.hasNext()) {
				inputCursor.next();
				outputCursor.setPosition(inputCursor);

				double value = inputCursor.getType().getRealDouble();

				outputCursor.getType().setReal(value);
			}

			inputCursor.close();

			// NB: DO NOT CLOSE outputCursor - we'll reuse it
		}

		// -- private helper --

		/**
		 * sets a value at a specific (x,y) location in the output image to a given
		 * value
		 */
		private void setOutputPixel(int x, int y, double value) {
			outputPosition[0] = x;
			outputPosition[1] = y;

			outputCursor.setPosition(outputPosition);

			outputCursor.getType().setReal(value);
		}
	}
}
