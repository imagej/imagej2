//
// FlipHorizontally.java
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

import imagej.core.plugins.XYFlipper.FlipCoordinateTransformer;
import imagej.model.Dataset;
import imagej.plugin.ImageJPlugin;
import imagej.plugin.Parameter;
import imagej.plugin.Plugin;

/**
 * Creates an output Dataset that is a duplicate of an input Dataset flipped
 * horizontally
 * 
 * @author Barry DeZonia
 * 
 */
@Plugin(menuPath = "Image>Transform>Flip Horizontally")
public class FlipHorizontally implements ImageJPlugin {
	// -- instance variables that are Parameters --

	@Parameter
	private Dataset input;

	@Parameter(output = true)
	private Dataset output;

	// -- public interface --

	@Override
	public void run() {
		FlipCoordinateTransformer flipTransformer = new HorzFlipTransformer();
		XYFlipper flipper = new XYFlipper(input, flipTransformer);
		ImglibOutputAlgorithmRunner runner = new ImglibOutputAlgorithmRunner(
				flipper);
		output = runner.run();
	}

	// -- private interface --

	private class HorzFlipTransformer implements FlipCoordinateTransformer {
		@Override
		public void calcOutputPosition(int[] inputDimensions,
				int[] inputPosition, int[] outputPosition) {
			outputPosition[0] = inputDimensions[0] - inputPosition[0] - 1;
			outputPosition[1] = inputPosition[1];
		}

		@Override
		public int[] calcOutputDimensions(int[] inputDimensions) {
			return inputDimensions.clone();
		}
	}
}
