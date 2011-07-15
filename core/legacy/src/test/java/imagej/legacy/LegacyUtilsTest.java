package imagej.legacy;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;
import ij.ImagePlus;
import ij.ImageStack;
import ij.process.ByteProcessor;

import org.junit.Test;



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
