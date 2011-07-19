//
// Rotate90DegreesLeft.java
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

import imagej.core.plugins.imglib.ImglibDataTransform;
import imagej.core.plugins.rotate.XYFlipper.FlipCoordinateTransformer;
import imagej.data.Dataset;
import imagej.ext.plugin.ImageJPlugin;
import imagej.ext.plugin.Menu;
import imagej.ext.plugin.Parameter;
import imagej.ext.plugin.Plugin;

// TODO - IJ1 updates the calibration so that pixel width & depth swap after this operation. Must implement here.

/**
 * Creates an output Dataset that is a duplicate of an input Dataset rotated 90
 * degrees to the left
 * 
 * @author Barry DeZonia
 */
@Plugin(menu = {
	@Menu(label = "Image", mnemonic = 'i'),
	@Menu(label = "Transform", mnemonic = 't'),
	@Menu(label = "Rotate 90 Degrees Left", weight = 5) })
public class Rotate90DegreesLeft implements ImageJPlugin {

	// -- instance variables that are Parameters --

	@Parameter
	private Dataset input;

	// -- public interface --

	@Override
	@SuppressWarnings("unchecked")
	public void run() {
		FlipCoordinateTransformer flipTransformer = new NinetyLeftTransformer();
		XYFlipper flipper = new XYFlipper(input, flipTransformer);
		ImglibDataTransform runner = new ImglibDataTransform(input, flipper);
		runner.run();
	}

	// -- private interface --

	private class NinetyLeftTransformer implements FlipCoordinateTransformer {
		
		public NinetyLeftTransformer(){
			// nothing to do
		}

		@Override
		public void calcOutputPosition(long[] inputDimensions, long[] inputPosition,
			long[] outputPosition)
		{
			outputPosition[1] = inputDimensions[0] - inputPosition[0] - 1;
			outputPosition[0] = inputPosition[1];
			for (int i = 2; i < inputDimensions.length; i++)
				outputPosition[i] = inputPosition[i];
		}

		@Override
		public long[] calcOutputDimensions(long[] inputDimensions) {
			long[] outputDims = inputDimensions.clone();

			outputDims[0] = inputDimensions[1];
			outputDims[1] = inputDimensions[0];

			return outputDims;
		}
		
		@Override
		public boolean isShapePreserving() {
			return false;
		}
	}
}
