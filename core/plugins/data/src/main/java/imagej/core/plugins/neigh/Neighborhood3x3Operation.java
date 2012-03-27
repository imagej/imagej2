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

package imagej.core.plugins.neigh;

import imagej.data.Dataset;
import imagej.data.Extents;
import imagej.data.Position;
import imagej.util.RealRect;
import net.imglib2.Cursor;
import net.imglib2.RandomAccess;
import net.imglib2.RandomAccessible;
import net.imglib2.img.Img;
import net.imglib2.type.numeric.RealType;
import net.imglib2.view.Views;

/**
 * Neighborhood3x3Operation - a helper class for 3x3 neighborhood operation
 * plugins such as SmoothDataValues, SharpenDataValues, and FindEdges. Does the
 * work of communicating with a Neighborhood3x3Watcher.
 * 
 * @author Barry DeZonia
 */
public class Neighborhood3x3Operation {

	// -- instance variables --

	private final Dataset input;
	private Img<? extends RealType<?>> inputImage;
	private Img<? extends RealType<?>> inputImageCopy;
	private final RealRect selection;
	private final Neighborhood3x3Watcher watcher;

	// -- constructor --

	public Neighborhood3x3Operation(final Dataset input,
		final RealRect selection, final Neighborhood3x3Watcher watcher)
	{
		this.input = input;
		this.watcher = watcher;
		this.selection = selection;

		if (watcher == null) throw new IllegalArgumentException(
			"neighborhood watcher cannot be null!");
	}

	// -- public interface --

	public void run() {
		checkInput();
		setupWorkingData();
		runAssignment();
	}

	// -- private interface --

	/**
	 * Make sure we have an input image and that it's dimensionality is correct
	 */
	private void checkInput() {
		if (input == null) throw new IllegalArgumentException(
			"input Dataset is null");

		if (input.getImgPlus() == null) throw new IllegalArgumentException(
			"input Img is null");

		// if (input.getImage().numDimensions() != 2)
		// throw new IllegalArgumentException("input image is not 2d but has " +
		// input.getImage().numDimensions() + " dimensions");
	}

	private void setupWorkingData() {
		inputImage = input.getImgPlus();
		inputImageCopy = cloneImage(inputImage);
	}

	private void runAssignment() {
		final long[] planeDims = new long[inputImage.numDimensions() - 2];
		for (int i = 0; i < planeDims.length; i++)
			planeDims[i] = inputImage.dimension(i + 2);
		final Extents extents = new Extents(planeDims);
		final Position planePos = extents.createPosition();
		if (planeDims.length == 0) { // dataset is 2d only
			applyOperationToPlane(planePos);
		}
		else { // 3 or more dimensions
			while (planePos.hasNext()) {
				planePos.fwd();
				applyOperationToPlane(planePos);
			}
		}
		input.update();
	}

	private void applyOperationToPlane(final Position planePos) {

		final long[] imageDims = new long[inputImage.numDimensions()];
		inputImage.dimensions(imageDims);

		if (selection.width == 0) selection.width = (int) imageDims[0];

		if (selection.height == 0) selection.height = (int) imageDims[1];

		// output is done by changin input image in place
		final RandomAccess<? extends RealType<?>> outputAccessor =
			inputImage.randomAccess();

		// input is a copy of the original data with out of bounds access enabled
		final RandomAccessible<? extends RealType<?>> inputInterval =
			Views.extendMirrorSingle(inputImageCopy);

		final RandomAccess<? extends RealType<?>> extendedInput =
			inputInterval.randomAccess();

		// initialize the watcher
		watcher.setup();

		final long[] inputPosition = new long[imageDims.length];
		final long[] localInputPosition = new long[imageDims.length];

		for (int i = 2; i < inputPosition.length; i++) {
			inputPosition[i] = planePos.getLongPosition(i - 2);
			localInputPosition[i] = planePos.getLongPosition(i - 2);
		}

		final long minX = (long) selection.x;
		final long minY = (long) selection.y;
		final long width = (long) selection.width;
		final long height = (long) selection.height;
		for (long y = minY; y < minY + height; y++) {
			inputPosition[1] = y;
			for (long x = minX; x < minX + width; x++) {
				inputPosition[0] = x;
				watcher.initializeNeighborhood(inputPosition);

				for (int dy = -1; dy <= 1; dy++) {
					localInputPosition[1] = inputPosition[1] + dy;
					for (int dx = -1; dx <= 1; dx++) {
						localInputPosition[0] = inputPosition[0] + dx;
						extendedInput.setPosition(localInputPosition);
						final double localValue = extendedInput.get().getRealDouble();
						watcher.visitLocation(dx, dy, localValue);
					}
				}
				// assign output
				outputAccessor.setPosition(inputPosition);
				outputAccessor.get().setReal(watcher.calcOutputValue());
			}
		}
	}

	// TODO - eliminate when ImgLib allows ability to duplicate/clone an Img
	// TODO - find a way to eliminate use of raw types here
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private Img<? extends RealType<?>> cloneImage(final Img image) {
		// TODO - used to be able to call Image::clone()
		// For now copy data by hand

		final long[] dimensions = new long[image.numDimensions()];
		image.dimensions(dimensions);

		final Img<? extends RealType<?>> copyOfImg =
			image.factory().create(dimensions, image.firstElement());

		final long[] position = new long[dimensions.length];

		final Cursor<? extends RealType<?>> cursor = image.localizingCursor();

		final RandomAccess<? extends RealType<?>> access =
			copyOfImg.randomAccess();

		while (cursor.hasNext()) {
			cursor.next();
			final double currValue = cursor.get().getRealDouble();
			cursor.localize(position);
			access.setPosition(position);
			access.get().setReal(currValue);
		}

		return copyOfImg;
	}
}
