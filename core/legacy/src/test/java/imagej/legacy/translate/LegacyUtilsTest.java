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

package imagej.legacy.translate;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;
import ij.ImagePlus;
import ij.ImageStack;
import ij.process.ByteProcessor;
import imagej.data.Extents;
import imagej.data.Position;
import imagej.legacy.translate.LegacyUtils;

import net.imglib2.meta.Axes;
import net.imglib2.meta.AxisType;

import org.junit.Test;

/**
 * Unit tests for {@link LegacyUtils}.
 * 
 * @author Barry DeZonia
 */
public class LegacyUtilsTest {

	/*
	 * This test is illustrative of an issue in IJ1 where the internal stack gets
	 * deleted in certain cases. If you setProcessor() on an ImagePlus with a
	 * stack of 1 slice the stack gets deleted. A subsequent call to getStack()
	 * will hatch a new one using the pixels of the current ImageProcessor.
	 * Basically to avoid problems the legacy layer should never call
	 * setProcessor() or if it does it should do a getStack() rather than cache
	 * calls to previous getStack() calls.
	 */
	@Test
	public void testStackKiller() {
		final ImageStack stack = new ImageStack(2, 2);
		final byte[] slice = new byte[] { 1, 2, 3, 4 };
		stack.addSlice("one slice", slice);
		final ImagePlus imp = new ImagePlus("fred", stack);
		assertEquals(stack, imp.getStack());
		final byte[] slice2 = new byte[] { 5, 6, 7, 8 };
		final ByteProcessor proc = new ByteProcessor(2, 2, slice2, null);
		imp.setProcessor(proc);
		final ImageStack secondStack = imp.getStack();
		assertNotSame(stack, secondStack);
		assertEquals(slice, stack.getPixels(1));
		assertEquals(slice2, secondStack.getPixels(1));
	}
	
	/*
	 * Makes sure calculation of IJ1 channels is correctly invertible
	 */
	@Test
	public void testRasterization() {
		final long[][] dimsList = {{1,1,1}, {1,2,3}, {2,3,4}, {5,4,3}, {4,2,7}}; 
		final AxisType[] axes = {Axes.CHANNEL, Axes.SPECTRA, Axes.FREQUENCY};
		for (long[] dims : dimsList) {
			// setup
			long numChannels = 1;
			for (long dim : dims)
				numChannels *= dim;
			
			// test from long index back to long index
			for (long channel = 0; channel < numChannels; channel++) {
				long[] channelPositions = new long[dims.length];
				LegacyUtils.fillChannelIndices(dims, axes, channel, channelPositions);
				long ij1Pos = LegacyUtils.calcIJ1ChannelPos(dims, axes, channelPositions);
				assertEquals(channel, ij1Pos);
			}
			
			// test from long[] index back to long[] index
			long[] channelPositions1 = new long[dims.length];
			long[] channelPositions2 = new long[dims.length];
			Extents extents = new Extents(dims);
			Position pos = extents.createPosition();
			while (pos.hasNext()) {
				pos.fwd();
				pos.localize(channelPositions1);
				long ij1Channel = LegacyUtils.calcIJ1ChannelPos(dims, axes, channelPositions1);
				LegacyUtils.fillChannelIndices(dims, axes, ij1Channel, channelPositions2);
				for (int i = 0; i < channelPositions1.length; i++)
					assertEquals(channelPositions1[i], channelPositions2[i]);
			}
		}
	}

}
