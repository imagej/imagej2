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

package imagej.plugins.commands.neigh;

import imagej.command.Command;
import imagej.command.ContextCommand;
import imagej.data.Dataset;
import imagej.data.display.ImageDisplay;
import imagej.data.display.ImageDisplayService;
import imagej.data.display.OverlayService;
import imagej.menu.MenuConstants;

import org.scijava.ItemIO;
import org.scijava.plugin.Menu;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.util.RealRect;

/**
 * Runs the Find Edges plugin
 * 
 * @author Barry DeZonia
 */
@Plugin(type = Command.class, menu = {
	@Menu(label = MenuConstants.PROCESS_LABEL,
		weight = MenuConstants.PROCESS_WEIGHT,
		mnemonic = MenuConstants.PROCESS_MNEMONIC),
	@Menu(label = "Find Edges", weight = 3) }, headless = true)
public class FindEdges extends ContextCommand {

	// -- instance variables that are Parameters --

	@Parameter
	private ImageDisplayService imageDisplayService;

	@Parameter
	private OverlayService overlayService;

	@Parameter(type = ItemIO.BOTH)
	private ImageDisplay display;

	// -- public interface --

	/**
	 * Sets the output Dataset to the result of the find edges operation
	 */
	@Override
	public void run() {
		final Dataset input = imageDisplayService.getActiveDataset(display);
		final RealRect selection = overlayService.getSelectionBounds(display);
		final Neighborhood3x3Operation operation =
			new Neighborhood3x3Operation(input, selection,
				new FindEdgesWatcher(input));
		operation.run();
	}

	public void setDisplay(ImageDisplay disp) {
		display = disp;
	}
	
	public ImageDisplay getDisplay() {
		return display;
	}

	// -- private interface --

	private class FindEdgesWatcher implements Neighborhood3x3Watcher {

		/**
		 * n - contains a local copy of the 9 values of a 3x3 neighborhood
		 */
		private double[] n;
		private final boolean integerDataset;
		private final double typeMinValue;
		private final double typeMaxValue;

		public FindEdgesWatcher(final Dataset input) {
			integerDataset = input.isInteger();
			typeMinValue = input.getType().getMinValue();
			typeMaxValue = input.getType().getMaxValue();
		}

		/**
		 * Create the local neighborhood variables
		 */
		@Override
		public void setup() {
			n = new double[9];
		}

		/**
		 * At each new neighborhood start tracking neighbor 0
		 */
		@Override
		public void initializeNeighborhood(final long[] position) {
			// nothing to do
		}

		/**
		 * Every time we visit a location within the neighborhood we update our
		 * local copy
		 */
		@Override
		public void visitLocation(final int dx, final int dy, final double value) {
			final int index = (dy + 1) * (3) + (dx + 1);
			n[index] = value;
		}

		/**
		 * Calculates the value of a pixel from the input neighborhood. Algorithm
		 * taken from legacy ImageJ.
		 */
		@Override
		public double calcOutputValue() {

			final double sum1 = n[0] + 2 * n[1] + n[2] - n[6] - 2 * n[7] - n[8];

			final double sum2 = n[0] + 2 * n[3] + n[6] - n[2] - 2 * n[5] - n[8];

			double value = Math.sqrt(sum1 * sum1 + sum2 * sum2);

			if (integerDataset) {
				if (value < typeMinValue) value = typeMinValue;
				if (value > typeMaxValue) value = typeMaxValue;
			}

			return value;
		}
	}

}
