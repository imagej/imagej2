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

package imagej.core.plugins;

import java.util.Random;

import mpicbg.imglib.algorithm.OutputAlgorithm;
import mpicbg.imglib.cursor.Cursor;
import mpicbg.imglib.cursor.LocalizableByDimCursor;
import mpicbg.imglib.image.Image;
import mpicbg.imglib.type.numeric.RealType;
import mpicbg.imglib.type.numeric.integer.UnsignedByteType;
import imagej.model.Dataset;
import imagej.plugin.ImageJPlugin;
import imagej.plugin.Parameter;
import imagej.plugin.Plugin;

// TODO - IJ1's implementation works on the current ROI rectangle. This plugin works on whole plane

/**
 * TODO
 *
 * @author Barry DeZonia
 */
@Plugin(
	menuPath = "PureIJ2>Process>Noise>Salt and Pepper"
)
public class SaltAndPepper implements ImageJPlugin
{
	// ***************  instance variables that are Parameters ***************************************************************

	@Parameter
	private Dataset input;
	
	@Parameter(output=true)
	private Dataset output;

	// ***************  public interface ***************************************************************

	@Override
	public void run()
	{
		ImglibOutputAlgorithmRunner runner = new ImglibOutputAlgorithmRunner(new SaltAndPepperAlgorithm());
		output = runner.run();
	}
	
	// ***************  private interface ***************************************************************

	private class SaltAndPepperAlgorithm implements OutputAlgorithm
	{

		private Image<?> inputImage;
		private Image<?> outputImage;
		private String errMessage = "No error";
		private LocalizableByDimCursor<? extends RealType<?>> outputCursor;  // working cursor
		private int[] outputPosition;  // workspace for setting output position

		@Override
		public boolean checkInput()
		{
			if (input == null)  // TODO - remove later
			{
				Image<UnsignedByteType> junkImage = Dataset.createPlanarImage("", new UnsignedByteType(), new int[]{200,200});
				Cursor<UnsignedByteType> cursor = junkImage.createCursor();
				int index = 0;
				for (UnsignedByteType pixRef : cursor)
					pixRef.set((index++) % 256);
				cursor.close();
				input = new Dataset(junkImage);
			}
			
			inputImage = input.getImage();
			
			if (inputImage.getNumDimensions() != 2)
			{
				errMessage = "Only 2d images supported";
				return false;
			}
			
			outputImage = inputImage.createNewImage();

			initOutputImageVariables();

			return true;
		}

		@Override
		public String getErrorMessage()
		{
			return errMessage;
		}

		@Override
		public boolean process()
		{
			Random rng = new Random();

			rng.setSeed(System.currentTimeMillis());

			double percentToChange = 0.05;
			
			long numPixels = (long)(inputImage.getNumPixels() * percentToChange);
			
			int width = inputImage.getDimension(0);
			
			int height = inputImage.getDimension(1);
			
			for (long p = 0; p < numPixels/2; p++)
			{
				int randomX, randomY;
				
				randomX = rng.nextInt(width); 
				randomY = rng.nextInt(height);
				setOutputPixel(randomX, randomY, 255);
				
				randomX = rng.nextInt(width); 
				randomY = rng.nextInt(height);
				setOutputPixel(randomX, randomY, 0);
			}
			
			outputCursor.close();  // FINALLY close working cursor
			
			return true;
		}

		@Override
		public Image<?> getResult()
		{
			return outputImage;
		}
		
		private void initOutputImageVariables()
		{
			LocalizableByDimCursor<? extends RealType<?>> inputCursor = 
				(LocalizableByDimCursor<? extends RealType<?>>) inputImage.createLocalizableByDimCursor();
			
			outputCursor = (LocalizableByDimCursor<? extends RealType<?>>) outputImage.createLocalizableByDimCursor();
			
			outputPosition = outputImage.createPositionArray();
			
			while (inputCursor.hasNext())
			{
				inputCursor.next();
				outputCursor.setPosition(inputCursor);
				
				double value = inputCursor.getType().getRealDouble();
				
				outputCursor.getType().setReal(value);
			}
			
			inputCursor.close();
			
			// **** DO NOT CLOSE outputCursor - we'll reuse it
		}
		
		private void setOutputPixel(int x, int y, double value)
		{
			outputPosition[0] = x;
			outputPosition[1] = y;
			
			outputCursor.setPosition(outputPosition);
			
			outputCursor.getType().setReal(value);
		}
	}
}
