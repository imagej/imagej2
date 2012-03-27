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

package imagej.core.plugins.imglib;

import imagej.data.Dataset;
import imagej.data.display.ImageDisplay;
import imagej.data.display.ImageDisplayService;
import imagej.data.display.OverlayService;
import imagej.data.overlay.Overlay;
import imagej.ext.menu.MenuConstants;
import imagej.ext.plugin.ImageJPlugin;
import imagej.ext.plugin.Menu;
import imagej.ext.plugin.Parameter;
import imagej.ext.plugin.Plugin;
import imagej.util.RealRect;
import net.imglib2.Cursor;
import net.imglib2.RandomAccess;
import net.imglib2.img.Img;
import net.imglib2.img.ImgPlus;
import net.imglib2.meta.Axes;
import net.imglib2.type.numeric.RealType;

// TODO - the IJ1 crop plugin can do a lot more than this can.
// Investigate its abilities and replicate them as needed.

//TODO - add correct weight to @Plugin annotation.

/**
 * Replaces the pixels of an input Dataset by cropping in X & Y using its
 * currently selected region. Works on images of any dimensionality.
 * 
 * @author Barry DeZonia
 */
@SuppressWarnings({ "rawtypes", "unchecked" })
@Plugin(menu = {
	@Menu(label = MenuConstants.IMAGE_LABEL, weight = MenuConstants.IMAGE_WEIGHT,
		mnemonic = MenuConstants.IMAGE_MNEMONIC),
	@Menu(label = "Crop", accelerator = "shift control X") }, headless = true)
public class CropImage implements ImageJPlugin {

	// -- instance variables that are Parameters --

	@Parameter
	private ImageDisplayService imageDisplayService;

	@Parameter
	private OverlayService overlayService;

	@Parameter
	private ImageDisplay display;

	// -- other instance variables --

	private Img inputImage;
	private long minX, maxX, minY, maxY;
	private int xIndex, yIndex;
	private Img<? extends RealType<?>> outputImage;

	// -- public interface --

	/**
	 * Runs the crop process on the given display's active dataset.
	 */
	@Override
	public void run() {
		final Dataset dataset = imageDisplayService.getActiveDataset(display);
		final RealRect bounds = overlayService.getSelectionBounds(display);

		final ImgPlus<? extends RealType<?>> croppedData =
			generateCroppedData(dataset, bounds);

		// remove all overlays
		// TODO - could be a problem when multiple datasets in one display
		// TODO - could just remove those that are not wholly contained in
		// crop region. Could translate or recreate others.
		for (final Overlay overlay : overlayService.getOverlays(display)) {
			overlayService.removeOverlay(display, overlay);
		}

		// BDZ - HACK - FIXME
		// 10-14-11 resetting zoom will cause canvas to prefer new smaller size
		// and sizeAppropriately() correctly. TODO sizeAppropriately() changed
		// recently. This tweak maybe not needed anymore. Could instead add some
		// code to tell canvas it's size is invalid. Or have panel listen for
		// DatasetRestructuredEvents and reset canvas size there. Not sure how
		// that would interact with current event sequences. Note that this hack
		// is only somewhat effective. The resize does not always happen even with
		// this. Once again we have a display timing bug. See what CTR figures out
		// about timing issues with the elimination of redoLayout(). Related ticket
		// is #826.
		// note 11-1-11: removed to see if needed. Remove all these comments and
		// this hack if unneeded after #826 handled.
		//
		// display.getCanvas().setZoom(1);

		dataset.setImgPlus(croppedData);

		// TODO - could create an overlay that selects all afterwards or
		// move original one. Or just make sure region is marked as selected
		// for (final DataView view : display) {
		// view.setSelected(true);
		// }
	}

	// -- private interface --

	/**
	 * Creates an ImgPlus containing data from crop region of an input Dataset
	 */
	private ImgPlus<? extends RealType<?>> generateCroppedData(final Dataset ds,
		final RealRect bounds)
	{
		setup(ds, bounds);
		copyPixels();
		return ImgPlus.wrap(outputImage, ds);
	}

	/**
	 * Initializes working variables used by copyPixels()
	 */
	private void setup(final Dataset dataset, final RealRect bounds) {
		inputImage = dataset.getImgPlus();

		minX = (long) bounds.x;
		minY = (long) bounds.y;
		maxX = (long) (bounds.x + bounds.width - 1);
		maxY = (long) (bounds.y + bounds.height - 1);

		xIndex = dataset.getAxisIndex(Axes.X);
		yIndex = dataset.getAxisIndex(Axes.Y);

		final long[] newDimensions = new long[inputImage.numDimensions()];
		inputImage.dimensions(newDimensions);
		newDimensions[xIndex] = maxX - minX + 1;
		newDimensions[yIndex] = maxY - minY + 1;

		// TODO - if inputImage is not a raw type this won't compile
		outputImage =
			inputImage.factory().create(newDimensions, inputImage.firstElement());
	}

	/**
	 * Fills cropped image data container from the input Dataset.
	 */
	private void copyPixels() {
		final RandomAccess<? extends RealType<?>> inputAccessor =
			inputImage.randomAccess();

		final Cursor<? extends RealType<?>> outputCursor =
			outputImage.localizingCursor();

		final long[] tmpPosition = new long[outputImage.numDimensions()];

		while (outputCursor.hasNext()) {
			outputCursor.next();

			outputCursor.localize(tmpPosition);

			tmpPosition[xIndex] += minX;
			tmpPosition[yIndex] += minY;

			inputAccessor.setPosition(tmpPosition);

			final double value = inputAccessor.get().getRealDouble();

			outputCursor.get().setReal(value);
		}
	}

}
