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
import imagej.data.event.DatasetChangedEvent;
import imagej.event.Events;
import imagej.util.Index;
import imagej.util.Log;
import imagej.util.Rect;
import mpicbg.imglib.cursor.LocalizableByDimCursor;
import mpicbg.imglib.cursor.special.RegionOfInterestCursor;
import mpicbg.imglib.image.Image;
import mpicbg.imglib.outofbounds.OutOfBoundsStrategyFactory;
import mpicbg.imglib.outofbounds.OutOfBoundsStrategyMirrorFactory;
import mpicbg.imglib.type.numeric.RealType;

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
	private Image<?> inputImage;
	private Image<?> inputImageCopy;
	private Rect selection;
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
	 * make sure we have an input image and that it's dimensionality is correct
	 */
	private void checkInput() {
		if (input == null)
			throw new IllegalArgumentException("input Dataset is null");
		
		if (input.getImage() == null)
			throw new IllegalArgumentException("input Image is null");

		//if (input.getImage().getNumDimensions() != 2)
		//	throw new IllegalArgumentException("input image is not 2d but has " + input.getImage().getNumDimensions() + " dimensions");
	}


	private void setupWorkingData()
	{
		inputImage = input.getImage();
		inputImageCopy = inputImage.clone();
		selection = input.getSelection();
	}

	private void runAssignment()
	{
		int[] planeDims = new int[inputImage.getDimensions().length - 2];
		for (int i = 0; i < planeDims.length; i++)
			planeDims[i] = inputImage.getDimension(i+2);
		int totalPlanes = Index.getRasterLength(planeDims);
		for (int plane = 0; plane < totalPlanes; plane++) {
			int[] planeIndex = Index.rasterToPosition(planeDims, plane);
			applyOperationToPlane(planeIndex);
		}
		Events.publish(new DatasetChangedEvent(input));
	}
	
	private void applyOperationToPlane(int[] planeIndex) {
		LocalizableByDimCursor<? extends RealType<?>> outputCursor =
			(LocalizableByDimCursor<? extends RealType<?>>) inputImage
				.createLocalizableByDimCursor();

		int[] origin = inputImage.createPositionArray();
		origin[0] = selection.x;
		origin[1] = selection.y;
		for (int i = 2; i < origin.length; i++)
			origin[i] = planeIndex[i-2];
		
		int[] span = inputImage.getDimensions();
		if (selection.width > 0)
			span[0] = selection.width;
		if (selection.height > 0)
			span[1] = selection.height;
		for (int i = 2; i < span.length; i++)
			span[i] = 1;
		
		OutOfBoundsStrategyFactory factory =
			new OutOfBoundsStrategyMirrorFactory();
		
		LocalizableByDimCursor<? extends RealType<?>> inputCursor =
			(LocalizableByDimCursor<? extends RealType<?>>) inputImageCopy
				.createLocalizableByDimCursor(factory);

		RegionOfInterestCursor<? extends RealType<?>> neighCursor =
			new RegionOfInterestCursor(inputCursor, origin, span);
		
		int[] inputPosition = new int[inputCursor.getNumDimensions()];
		int[] localInputPosition = new int[inputCursor.getNumDimensions()];

		// initialize the watcher
		watcher.setup();

		// walk the selected region of the input image copy
		
		while (neighCursor.hasNext()) {
			
			// locate input cursor on next location
			neighCursor.fwd();

			// remember the location so that the input image cursor can use it
			neighCursor.getPosition(inputPosition);
			
			// NOTE - inputPosition is in relative coordinates of neigh cursor.
			//  Must translate to input coord space.
			for (int i = 0; i < inputPosition.length; i++) {
				inputPosition[i] += origin[i];
				localInputPosition[i] = inputPosition[i];
			}
			
			// let watcher know we are visiting a new neighborhood
			watcher.initializeNeighborhood(inputPosition);

			// iterate over the 3x3 neighborhood
			for (int dy = -1; dy <= 1; dy++) {

				localInputPosition[1] = inputPosition[1] + dy;
				
				for (int dx = -1; dx <= 1; dx++) {

					localInputPosition[0] = inputPosition[0] + dx;

					// move the input cursor there
					inputCursor.setPosition(localInputPosition);

					// update watcher about the position and value of the
					// subneighborhood location
					double localValue = inputCursor.getType().getRealDouble();
					
					watcher.visitLocation(dx, dy, localValue);
				}
			}

			// assign output
			outputCursor.setPosition(inputPosition);
			
			outputCursor.getType().setReal(watcher.calcOutputValue());
		}

		neighCursor.close();
		inputCursor.close();
		outputCursor.close();
	}
}
