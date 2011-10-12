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

package imagej.core.plugins.imglib;

import imagej.ImageJ;
import imagej.data.Dataset;
import imagej.data.display.ImageDisplay;
import imagej.data.display.ImageDisplayService;
import imagej.data.display.OverlayService;
import imagej.ext.MenuEntry;
import imagej.ext.plugin.ImageJPlugin;
import imagej.ext.plugin.Menu;
import imagej.ext.plugin.Parameter;
import imagej.ext.plugin.Plugin;
import imagej.util.RealRect;
import net.imglib2.Cursor;
import net.imglib2.RandomAccess;
import net.imglib2.img.Img;
import net.imglib2.type.numeric.RealType;

// TODO - the IJ1 crop plugin can do a lot more than this can.
// Investigate its abilities and replicate them as needed.

//TODO - add correct weight to @Plugin annotation.

/**
 * Creates an output Dataset by cropping an input Dataset in X & Y. Works on
 * images of any dimensionality. X & Y are assumed to be the first two
 * dimensions.
 * 
 * @author Barry DeZonia
 */
@Plugin(menu = {
	@Menu(label = "Image", weight = MenuEntry.IMAGE_WEIGHT, mnemonic = 'i'),
	@Menu(label = "Crop", accelerator = "shift control X") })
@SuppressWarnings({ "rawtypes", "unchecked" })
public class CropImage implements ImageJPlugin {

	// -- instance variables that are Parameters --

	@Parameter
	private ImageDisplay display;

	// -- public interface --

	/** Runs the crop process on the given display's active dataset. */
	@Override
	public void run() {
		final ImageDisplayService imageDisplayService =
			ImageJ.get(ImageDisplayService.class);
		final OverlayService overlayService = ImageJ.get(OverlayService.class);

		final Dataset dataset = imageDisplayService.getActiveDataset(display);
		final RealRect bounds = overlayService.getSelectionBounds(display);

		final OutputAlgorithm algorithm = new CropAlgorithm(dataset, bounds);
		final ImgLibDataTransform runner =
			new ImgLibDataTransform(dataset, algorithm);
		runner.run();
	}

	// -- private interface --

	/**
	 * CropAlgorithm is responsible for creating the cropped image from the input
	 * Dataset. It is an ImgLib OutputAlgorithm.
	 */
	private class CropAlgorithm implements OutputAlgorithm {

		// TODO - had to make this raw to avoid compiler errors
		private final Img inputImage;
		private final long minX, maxX, minY, maxY;

		private final String errMessage = "No error";
		private Img<? extends RealType<?>> outputImage;

		public CropAlgorithm(final Dataset dataset, final RealRect bounds) {
			inputImage = dataset.getImgPlus();
			minX = (long) bounds.x;
			minY = (long) bounds.y;
			maxX = (long) (bounds.x + bounds.width - 1);
			maxY = (long) (bounds.y + bounds.height - 1);
			/*
			long[] dims = dataset.getDims();
			System.out.print("Ds dims: ");
			for (int i = 0; i < dims.length; i++)
				System.out.print((i==0 ? "" : ",")+dims[i]);
			System.out.println(" min("+minX+","+ minY+") max("+maxX+","+ maxY+")");
			*/
		}

		@Override
		public boolean checkInput() {
			final long[] newDimensions = new long[inputImage.numDimensions()];

			inputImage.dimensions(newDimensions);
			newDimensions[0] = maxX - minX + 1;
			newDimensions[1] = maxY - minY + 1;

			// TODO - in inputImage not a raw type this won't compile
			outputImage =
				inputImage.factory().create(newDimensions, inputImage.firstElement());

			return true;
		}

		@Override
		public String getErrorMessage() {
			return errMessage;
		}

		/** Runs the cropping process. */
		@Override
		public boolean process() {
			final RandomAccess<? extends RealType<?>> inputAccessor =
				inputImage.randomAccess();

			final Cursor<? extends RealType<?>> outputCursor =
				outputImage.localizingCursor();

			final long[] tmpPosition = new long[outputImage.numDimensions()];

			while (outputCursor.hasNext()) {
				outputCursor.next();

				outputCursor.localize(tmpPosition);

				tmpPosition[0] += minX;
				tmpPosition[1] += minY;

				inputAccessor.setPosition(tmpPosition);

				final double value = inputAccessor.get().getRealDouble();

				outputCursor.get().setReal(value);
			}

			return true;
		}

		@Override
		public Img<? extends RealType<?>> getResult() {
			return outputImage;
		}

	}

}
