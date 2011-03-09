//
// SetBackgroundToNaN.java
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
import imagej.plugin.ImageJPlugin;
import imagej.plugin.Menu;
import imagej.plugin.Parameter;
import imagej.plugin.Plugin;
import mpicbg.imglib.algorithm.OutputAlgorithm;
import mpicbg.imglib.cursor.Cursor;
import mpicbg.imglib.image.Image;
import mpicbg.imglib.type.numeric.RealType;
import mpicbg.imglib.type.numeric.real.FloatType;

/**
 * Fills an output Dataset with the values of an input Dataset. All the values
 * in the input Dataset that are outside user defined thresholds are assigned
 * NaN.
 * 
 * @author Barry DeZonia
 */
@Plugin(menu = {
	@Menu(label = "Process", mnemonic = 'p'),
	@Menu(label = "Math", mnemonic = 'm'),
	@Menu(label = "NaN Background", weight = 17) })
public class SetBackgroundToNaN implements ImageJPlugin {

	// -- instance variables --

	@Parameter
	private Dataset input;

	@Parameter(output = true)
	private Dataset output;

	@Parameter(label = "Low threshold")
	private double loThreshold;

	@Parameter(label = "High threshold")
	private double hiThreshold;

	// -- public interface --

	/** runs this plugin */
	@Override
	public void run() {
		if (input == null) // TODO - temporary code to test these until IJ2
		// plugins can correctly fill a Dataset @Parameter
		{
			Image<FloatType> junkImage =
				Dataset.createPlanarImage("", new FloatType(), new int[] { 200, 200 });
			Cursor<FloatType> cursor = junkImage.createCursor();
			int index = 0;
			for (FloatType pixRef : cursor)
				pixRef.set(index++);
			cursor.close();
			input = new Dataset(junkImage);
		}

		if (input.isFloat()) {
			OutputAlgorithm algorithm = new SetToNaN(input, loThreshold, hiThreshold);
			ImglibOutputAlgorithmRunner runner =
				new ImglibOutputAlgorithmRunner(algorithm);
			output = runner.run();
		}
	}

	// -- private interface --

	/** private implementation of algorithm */
	private class SetToNaN implements OutputAlgorithm {

		private Image<?> inputImage;
		private Image<?> outputImage;
		private double loThreshold;
		private double hiThreshold;
		private String errMessage = "No error";

		public SetToNaN(Dataset in, double loThreshold, double hiThreshold) {
			inputImage = in.getImage(); // TODO - failure is a real possibility
			// here (example: pass Image<FloatType>
			// when declared plugin of DoubleType
			outputImage = inputImage.createNewImage();
			this.loThreshold = loThreshold;
			this.hiThreshold = hiThreshold;
		}

		@Override
		public boolean checkInput() {
			return true;
		}

		@Override
		public String getErrorMessage() {
			return errMessage;
		}

		@Override
		public boolean process() {
			Cursor<? extends RealType<?>> inputCursor =
				(Cursor<? extends RealType<?>>) inputImage.createCursor();
			Cursor<? extends RealType<?>> outputCursor =
				(Cursor<? extends RealType<?>>) outputImage.createCursor();

			while (inputCursor.hasNext() && outputCursor.hasNext()) {
				double inputValue = inputCursor.next().getRealDouble();

				if ((inputValue < loThreshold) || (inputValue > hiThreshold)) outputCursor
					.next().setReal(Double.NaN);
				else outputCursor.next().setReal(inputValue);
			}

			inputCursor.close();
			outputCursor.close();

			return true;
		}

		@Override
		public Image<?> getResult() {
			return outputImage;
		}

	}
}
