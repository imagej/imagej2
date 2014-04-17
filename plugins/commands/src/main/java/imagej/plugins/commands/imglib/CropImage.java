/*
 * #%L
 * ImageJ software for multidimensional image processing and analysis.
 * %%
 * Copyright (C) 2009 - 2014 Board of Regents of the University of
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
 * #L%
 */

package imagej.plugins.commands.imglib;

import imagej.command.Command;
import imagej.command.ContextCommand;
import imagej.data.Dataset;
import imagej.data.display.ImageDisplay;
import imagej.data.display.ImageDisplayService;
import imagej.data.display.OverlayService;
import imagej.data.overlay.Overlay;
import imagej.data.overlay.ThresholdOverlay;
import imagej.menu.MenuConstants;

import java.util.ArrayList;
import java.util.List;

import net.imglib2.Cursor;
import net.imglib2.RandomAccess;
import net.imglib2.img.Img;
import net.imglib2.meta.Axes;
import net.imglib2.meta.ImgPlus;
import net.imglib2.type.numeric.RealType;

import org.scijava.ItemIO;
import org.scijava.plugin.Menu;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.util.RealRect;

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
@Plugin(type = Command.class, menu = {
	@Menu(label = MenuConstants.IMAGE_LABEL, weight = MenuConstants.IMAGE_WEIGHT,
		mnemonic = MenuConstants.IMAGE_MNEMONIC),
	@Menu(label = "Crop", accelerator = "shift ^X") }, headless = true)
public class CropImage extends ContextCommand {

	// -- instance variables that are Parameters --

	@Parameter
	private ImageDisplayService imageDisplayService;

	@Parameter
	private OverlayService overlayService;

	@Parameter(type = ItemIO.BOTH)
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
		// bounds could be a single point
		if (bounds.width == 0) bounds.width = 1;
		if (bounds.height == 0) bounds.height = 1;

		final ImgPlus<? extends RealType<?>> croppedData =
			generateCroppedData(dataset, bounds);

		// update all overlays
		//   delete all but remember those wholly contained in the selected region 
		final double[] toNewOrigin = new double[2];
		toNewOrigin[0] = -bounds.x;
		toNewOrigin[1] = -bounds.y;
		List<Overlay> newOverlays = new ArrayList<Overlay>();
		for (final Overlay overlay : overlayService.getOverlays(display)) {
			Overlay newOverlay = null;
			if (!(overlay instanceof ThresholdOverlay) &&
				overlayContained(overlay, bounds))
			{
				// can't just move() the overlay. JHotDraw gets confused. So delete all
				// overlays at current position and add back copies translated to new
				// origin
				newOverlay = overlay.duplicate();
				newOverlay.move(toNewOrigin);
				newOverlays.add(newOverlay);
			}
			if (!(overlay instanceof ThresholdOverlay)) {
				overlayService.removeOverlay(display, overlay);
			}
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

		// here the duplicated and origin translated overlays are attached
		overlayService.addOverlays(display, newOverlays);
	}

	public void setDisplay(ImageDisplay disp) {
		display = disp;
	}
	
	public ImageDisplay getDisplay() {
		return display;
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
		ImgPlus<? extends RealType<?>> newImgPlus = ImgPlus.wrap(outputImage, ds);
		copyColorTables(ds, newImgPlus);
		return newImgPlus;
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

		xIndex = dataset.dimensionIndex(Axes.X);
		yIndex = dataset.dimensionIndex(Axes.Y);

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

	private void copyColorTables(Dataset input, ImgPlus<?> output) {
		int count = input.getColorTableCount();
		output.initializeColorTables(count);
		for (int i = 0; i < count; i++) {
			output.setColorTable(input.getColorTable(i), i);
		}
	}

	private boolean overlayContained(Overlay overlay, RealRect bounds) {
		if (overlay.realMin(0) < bounds.x) return false;
		if (overlay.realMin(1) < bounds.y) return false;
		if (overlay.realMax(0) > bounds.x + bounds.width) return false;
		if (overlay.realMax(1) > bounds.y + bounds.height) return false;
		return true;
	}
}
