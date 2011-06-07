//
// FindEdges.java
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

import imagej.core.plugins.neigh.Neighborhood3x3Watcher;
import imagej.data.Dataset;
import imagej.plugin.ImageJPlugin;
import imagej.plugin.Menu;
import imagej.plugin.Parameter;
import imagej.plugin.Plugin;

/**
 * Runs the Find Edges plugin
 * 
 * @author Barry DeZonia
 */
@Plugin(menu = {
	@Menu(label = "Process", mnemonic = 'p'),
	@Menu(label = "Find Edges", weight = 3) })
public class FindEdges implements ImageJPlugin {

	// -- instance variables that are Parameters --

	@Parameter
	private Dataset input;

	// -- public interface --

	/** sets the output Dataset to the result of the find edges operation */
	@Override
	public void run() {
		Neighborhood3x3Operation operation =
			new Neighborhood3x3Operation(input, new FindEdgesWatcher(input));
		operation.run();
	}

	// -- private interface --

	private class FindEdgesWatcher implements Neighborhood3x3Watcher {

		/** n - contains a local copy of the 9 values of a 3x3 neighborhood */
		private double[] n;
		private boolean integerDataset;
		private double typeMinValue;
		private double typeMaxValue;

		public FindEdgesWatcher(Dataset input) {
			integerDataset = input.isInteger();
			typeMinValue = input.getType().getMinValue();
			typeMaxValue = input.getType().getMaxValue();
			n = new double[9];
		}
		
		/** create the local neighborhood variables */
		@Override
		public void setup() {
		}

		/** at each new neighborhood start tracking neighbor 0 */
		@Override
		public void initializeNeighborhood(long[] position) {
			// nothing to do
		}

		/**
		 * every time we visit a location within the neighborhood we update our
		 * local copy
		 */
		@Override
		public void visitLocation(int dx, int dy, double value) {
			int index = (dy + 1) * (3) + (dx + 1);
			n[index] = value;
		}

		/**
		 * calculates the value of a pixel from the input neighborhood. algorithm
		 * taken from IJ1.
		 */
		@Override
		public double calcOutputValue() {

			double sum1 = n[0] + 2 * n[1] + n[2] - n[6] - 2 * n[7] - n[8];

			double sum2 = n[0] + 2 * n[3] + n[6] - n[2] - 2 * n[5] - n[8];

			double value = Math.sqrt(sum1 * sum1 + sum2 * sum2);
			
			if (integerDataset) {
				if (value < typeMinValue) value = typeMinValue;
				if (value > typeMaxValue) value = typeMaxValue;
			}
			
			return value;
		}
	}

}
