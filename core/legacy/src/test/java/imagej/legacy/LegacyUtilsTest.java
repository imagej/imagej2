//
// LegacyUtilsTest.java
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

package imagej.legacy;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;
import ij.ImagePlus;
import ij.ImageStack;
import ij.process.ByteProcessor;
import imagej.legacy.translate.LegacyUtils;

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
		ImageStack stack = new ImageStack(2,2);
		byte[] slice = new byte[]{1,2,3,4};
		stack.addSlice("one slice", slice);
		ImagePlus imp = new ImagePlus("fred",stack);
		assertEquals(stack, imp.getStack());
		byte[] slice2 = new byte[]{5,6,7,8};
		ByteProcessor proc = new ByteProcessor(2,2,slice2,null);
		imp.setProcessor(proc);
		ImageStack secondStack = imp.getStack();
		assertNotSame(stack, secondStack);
		assertEquals(slice, stack.getPixels(1));
		assertEquals(slice2, secondStack.getPixels(1));
	}

}
