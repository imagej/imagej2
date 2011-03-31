//
// FlipVertically.java
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

import mpicbg.imglib.cursor.Cursor;
import mpicbg.imglib.image.Image;
import mpicbg.imglib.type.numeric.integer.UnsignedShortType;
import imagej.core.plugins.imglib.ImglibDataTransform;
import imagej.core.plugins.rotate.XYFlipper.FlipCoordinateTransformer;
import imagej.data.Dataset;
import imagej.plugin.ImageJPlugin;
import imagej.plugin.Menu;
import imagej.plugin.Parameter;
import imagej.plugin.Plugin;
import imagej.util.Rect;

/**
 * Creates an output Dataset that is a duplicate of an input Dataset flipped
 * vertically
 * 
 * @author Barry DeZonia
 */
@Plugin(menu = {
	@Menu(label = "Image", mnemonic = 'i'),
	@Menu(label = "Transform", mnemonic = 't'),
	@Menu(label = "Flip Vertically", weight = 2) })
public class FlipVertically implements ImageJPlugin {

	// -- instance variables that are Parameters --

	@Parameter
	private Dataset input;

	@Parameter(output = true)
	private Dataset output;

	// -- public interface --

	@Override
	public void run() {
		FlipCoordinateTransformer flipTransformer = new VertFlipTransformer(input);
		XYFlipper flipper = new XYFlipper(input, flipTransformer);
		ImglibDataTransform runner =
			new ImglibDataTransform(input, flipper);
		runner.run();
		output = input;
	}

	// -- private interface --

	private class VertFlipTransformer implements FlipCoordinateTransformer {

		private int maxY;
		
		VertFlipTransformer(Dataset input) {
			maxY = input.getImage().getDimension(1);
			Rect currentSelection = input.getSelection();
			if (currentSelection.height > 0)
				maxY = currentSelection.y + currentSelection.height;
		}
		
		@Override
		public void calcOutputPosition(int[] inputDimensions, int[] inputPosition,
			int[] outputPosition)
		{
			outputPosition[0] = inputPosition[0];
			outputPosition[1] = maxY - inputPosition[1] - 1;
		}

		@Override
		public int[] calcOutputDimensions(int[] inputDimensions) {
			return inputDimensions.clone();
		}
		
		@Override
		public boolean isShapePreserving() {
			return true;
		}
	}
}
