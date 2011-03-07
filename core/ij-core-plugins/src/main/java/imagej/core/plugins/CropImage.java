//
// CropImage.java
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
import imagej.plugin.Parameter;
import imagej.plugin.Plugin;
import mpicbg.imglib.algorithm.OutputAlgorithm;
import mpicbg.imglib.cursor.Cursor;
import mpicbg.imglib.cursor.LocalizableByDimCursor;
import mpicbg.imglib.image.Image;
import mpicbg.imglib.type.numeric.RealType;
import mpicbg.imglib.type.numeric.integer.UnsignedShortType;

// TODO - minX, minY, maxX, maxY are treated as harvested variables (for simple testing). Should make them passed
//        in parameters to constructor

// TODO - the IJ1 crop plugin can do a lot more than this can. Investigate its abilities and replicate them as needed

/**
 * Creates an output Dataset by cropping an input Dataset in X & Y. Works on images of any dimensionality. X & Y are assumed
 * to be the first two dimensions.
 *
 * @author Barry DeZonia
 *
 */
@Plugin(
		menuPath = "PureIJ2>Image>Crop"
)
public class CropImage implements ImageJPlugin
{
	// -- instance variables that are Parameters --

	@Parameter
	Dataset input;
	
	@Parameter(output=true)
	Dataset output;
	
	@Parameter
	private int minX;

	@Parameter
	private int minY;

	@Parameter
	private int maxX;

	@Parameter
	private int maxY;

	// -- public interface --

	/** runs the crop process and returns the output as a Dataset */
	@Override
	public void run()
	{
		OutputAlgorithm algorithm = new CropAlgorithm();
		ImglibOutputAlgorithmRunner runner = new ImglibOutputAlgorithmRunner(algorithm);
		output = runner.run();
	}
	
	// -- private interface --

	/** CropAlgorithm is responsible for creating the cropped image from the input Dataset. It is an Imglib OutputAlgorithm. */
	private class CropAlgorithm implements OutputAlgorithm
	{
		private String errMessage = "No error";
		private Image<? extends RealType<?>> inputImage;
		private Image<? extends RealType<?>> outputImage;

		/** returns false if there is any problem with the input data. returns true otherwise. */
		@Override
		public boolean checkInput()
		{
			if (input == null)  // TODO - temporary code to test these until IJ2 plugins can correctly fill a Dataset @Parameter
			{
				Image<UnsignedShortType> junkImage = Dataset.createPlanarImage("", new UnsignedShortType(), new int[]{200,200});
				Cursor<UnsignedShortType> cursor = junkImage.createCursor();
				int index = 0;
				for (UnsignedShortType pixRef : cursor)
					pixRef.set(index++);
				cursor.close();
				input = new Dataset(junkImage);
			}

			inputImage = (Image<? extends RealType<?>>) input.getImage();
			
			int[] newDimensions = inputImage.getDimensions().clone();
			
			newDimensions[0] = maxX - minX + 1;
			newDimensions[1] = maxY - minY + 1;
				
			outputImage = inputImage.createNewImage(newDimensions);
			
			return true;
		}

		/** returns the current error message */
		@Override
		public String getErrorMessage()
		{
			return errMessage;
		}

		/** runs the cropping process */
		@Override
		public boolean process()
		{
			LocalizableByDimCursor<? extends RealType<?>> inputCursor = inputImage.createLocalizableByDimCursor();
			LocalizableByDimCursor<? extends RealType<?>> outputCursor = outputImage.createLocalizableByDimCursor();

			int[] tmpPosition = outputImage.createPositionArray();

			while (outputCursor.hasNext())
			{
				outputCursor.next();
				
				outputCursor.getPosition(tmpPosition);
				
				tmpPosition[0] += minX;
				tmpPosition[1] += minY;
				
				inputCursor.setPosition(tmpPosition);
				
				double value = inputCursor.getType().getRealDouble();
				
				outputCursor.getType().setReal(value);
			}

			inputCursor.close();
			outputCursor.close();
			
			return true;
		}

		/** returns the resulting output image. not valid before checkInput() and process() have been called. */
		@Override
		public Image<?> getResult()
		{
			return outputImage;
		}
		
	}
}
