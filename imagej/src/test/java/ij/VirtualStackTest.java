package ij;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import ij.process.ByteProcessor;
import ij.process.DataConstants;
import ij.process.ImageProcessor;

import java.awt.image.ColorModel;

import org.junit.Test;

public class VirtualStackTest {

	// ******* instance vars

	VirtualStack vs;

	// ****************************  HELPERS  ***************************************************

	private VirtualStack nSliceStack(int n)
	{
		VirtualStack st = new VirtualStack(3,4,null,"/fred/jones/blooka");
		for (int i = 0; i < n; i++)
		{
			int sliceNumber = i+1;
			st.addSlice(""+sliceNumber);
			st.setSliceLabel(""+i, sliceNumber);
		}
		return st;
	}

	private void expectDeleteSuccess(VirtualStack v, int sliceNumber)
	{
		int currSize = v.getSize();
		assertTrue((sliceNumber >= 1) && (sliceNumber <= currSize));
		v.deleteSlice(sliceNumber);
		assertEquals(currSize-1,v.getSize());
	}

	private void expectDeleteFailure(VirtualStack v, int sliceNumber)
	{
		try {
			v.deleteSlice(sliceNumber);
			fail();
		} catch (IllegalArgumentException e) {
			assertTrue(true);
		}
	}

	private void expectStack(VirtualStack v, int[] sliceNumbers)
	{
		assertEquals(v.getSize(),sliceNumbers.length);
		for (int i = 0; i < sliceNumbers.length; i++)
			assertEquals(""+sliceNumbers[i],vs.getSliceLabel(i+1));
	}

	// ****************************  TESTS  ***************************************************

	@Test
	public void testVirtualStack() {
		vs = new VirtualStack();
		assertNotNull(vs);
	}

	@Test
	public void testVirtualStackIntIntColorModelString() {
		int w = 121;
		int h = 243;
		ColorModel cm = null;
		String path = "/blug/snoot/gix";
		vs = new VirtualStack(w,h,cm,path);
		assertNotNull(vs);
	}

	@Test
	public void testAddSliceString() {

		// null string
		try {
			vs = new VirtualStack();
			vs.addSlice(null);
			fail();
		} catch (IllegalArgumentException e) {
			assertTrue(true);
		}

		// anything else should be okay

		// try ones with the same name : surprisingly works
		vs = new VirtualStack(3,4,null,"/fred/jones/blooka");
		assertNotNull(vs);
		vs.addSlice("zako");
		assertEquals(1,vs.getSize());
		vs.addSlice("zako");
		assertEquals(2,vs.getSize());

		// try a bunch at once : exercises memory expansion
		vs = new VirtualStack(3,4,null,"/fred/jones/blooka");
		assertNotNull(vs);
		for (int i = 0; i < 400; i++)
		{
			int sliceNumber = i+1;
			vs.addSlice(""+sliceNumber);
			vs.setSliceLabel(""+sliceNumber, sliceNumber);
			assertEquals(sliceNumber,vs.getSize());
			assertEquals("1",vs.getSliceLabel(1));  // make sure original one copied as we expand.
			assertEquals(""+sliceNumber,vs.getSliceLabel(sliceNumber));
		}
	}

	@Test
	public void testAddSliceStringObject() {
		// add slices via this method should fail
		vs = new VirtualStack(3,4,null,"/fred/jones/blooka");
		assertNotNull(vs);
		assertEquals(0,vs.getSize());
		vs.addSlice("zorp",new byte[]{1,2});
		assertEquals(0,vs.getSize());
	}

	@Test
	public void testAddSliceStringImageProcessor() {
		// add slices via this method should fail
		vs = new VirtualStack(3,4,null,"/fred/jones/blooka");
		assertNotNull(vs);
		assertEquals(0,vs.getSize());
		vs.addSlice("zorp",new ByteProcessor(1,2,new byte[]{1,2},null));
		assertEquals(0,vs.getSize());
	}

	@Test
	public void testAddSliceStringImageProcessorInt() {
		// add slices via this method should fail
		vs = new VirtualStack(3,4,null,"/fred/jones/blooka");
		assertNotNull(vs);
		assertEquals(0,vs.getSize());
		vs.addSlice("zorp",new ByteProcessor(1,2,new byte[]{1,2},null),0);
		assertEquals(0,vs.getSize());
	}

	@Test
	public void testAddSliceStringBooleanObject() {
		// add slices via this method should fail
		vs = new VirtualStack(3,4,null,"/fred/jones/blooka");
		assertNotNull(vs);
		assertEquals(0,vs.getSize());
		vs.addSlice("zorp",true,new byte[12]);
		vs.addSlice("porp",false,new byte[12]);
		assertEquals(0,vs.getSize());
	}

	@Test
	public void testDeleteSlice() {

		// try on empty VirtualStack
		vs = nSliceStack(0);
		expectDeleteFailure(vs,0);
		expectDeleteFailure(vs,1);

		// try illegal args with a valid stack
		vs = nSliceStack(1);
		expectDeleteFailure(vs,0);
		expectDeleteFailure(vs,2);

		// try deleting from a one stack
		vs = nSliceStack(1);
		expectDeleteSuccess(vs,1);

		// delete the first from a three stack
		vs = nSliceStack(3);
		expectDeleteSuccess(vs,1);
		expectStack(vs,new int[]{2,3});

		// delete the middle from a three stack
		vs = nSliceStack(3);
		expectDeleteSuccess(vs,2);
		expectStack(vs,new int[]{1,3});

		// delete the last from a three stack
		vs = nSliceStack(3);
		expectDeleteSuccess(vs,3);
		expectStack(vs,new int[]{1,2});
	}

	@Test
	public void testDeleteLastSlice() {

		// try on an empty stack
		vs = nSliceStack(0);
		vs.deleteLastSlice();
		assertEquals(0,vs.getSize());

		// try on a one stack
		vs = nSliceStack(1);
		vs.deleteLastSlice();
		assertEquals(0,vs.getSize());

		// try on an N stack
		vs = nSliceStack(5);
		vs.deleteLastSlice();
		assertEquals(4,vs.getSize());
		assertEquals("4",vs.getSliceLabel(4));
	}

	@Test
	public void testGetPixels() {
		// try on real data
		vs = new VirtualStack(2,3,null,DataConstants.DATA_DIR);
		vs.addSlice("gray8-2x3-sub1.tif");
		vs.addSlice("gray8-2x3-sub2.tif");
		Object obj = vs.getPixels(1);
		assertTrue(obj instanceof byte[]);
		byte[] bytes = (byte[]) obj;
		assertArrayEquals(new byte[]{0,40,0,40,120,-96},bytes);
	}

	@Test
	public void testSetPixels() {
		// setPixels should have no effect
		//   note getPixels() returns a new array of pixels with each call. So can't test object==.
		vs = new VirtualStack(2,3,null,DataConstants.DATA_DIR);
		vs.addSlice("gray8-2x3-sub1.tif");
		vs.addSlice("gray8-2x3-sub2.tif");
		byte[] origBytes = (byte[])vs.getPixels(1);
		vs.setPixels(new byte[]{1,2,3,4,5,6,7,8,9,10,9,8},1);
		byte[] nextBytes = (byte[])vs.getPixels(1);
		assertArrayEquals(origBytes,nextBytes);  // test that its unchanged
	}

	@Test
	public void testGetImageArray() {
		// should always return null
		vs = nSliceStack(4);
		assertNull(vs.getImageArray());
	}

	@Test
	public void testGetSize() {
		// try on numerous differently sized stacks
		for (int i = 0; i < 100; i++)
		{
		  vs = nSliceStack(i);
		  assertEquals(i,vs.getSize());
		}
	}

	@Test
	public void testGetSliceLabel() {
		// setup vstack
		vs = new VirtualStack(2,3,null,DataConstants.DATA_DIR);
		vs.addSlice("gray8-2x3-sub1.tif");
		vs.addSlice("gray8-2x3-sub2.tif");

		// make sure labels reflect structure of stack
		assertEquals("gray8-2x3-sub1",vs.getShortSliceLabel(1));
		assertEquals("gray8-2x3-sub2",vs.getShortSliceLabel(2));
	}

	@Test
	public void testSetSliceLabel() {
		// setSliceLabel does nothing

		// setup vstack
		vs = new VirtualStack(2,3,null,"data");
		vs.addSlice("gray8-2x3-sub1.tif");
		vs.addSlice("gray8-2x3-sub2.tif");

		// try to change labels
		vs.setSliceLabel("Flubblepop", 1);
		vs.setSliceLabel("Sweego", 2);

		// test that it had no effect
		assertEquals("gray8-2x3-sub1",vs.getShortSliceLabel(1));
		assertEquals("gray8-2x3-sub2",vs.getShortSliceLabel(2));
	}

	@Test
	public void testGetProcessor() {
		// setup vstack
		vs = new VirtualStack(2,3,null,DataConstants.DATA_DIR);
		vs.addSlice("gray8-2x3-sub1.tif");
		vs.addSlice("gray8-2x3-sub2.tif");

		// test that it works
		ImageProcessor proc;

		// try out of bounds entries
		if (IJInfo.RUN_ENHANCED_TESTS)
		{
			// this results in an arrayindex == -1 exception
			proc = vs.getProcessor(0);

			// this results in a null ptr exception
			proc = vs.getProcessor(3);
		}

		// try valid entries and test returned data
		proc = vs.getProcessor(1);
		assertTrue(proc instanceof ByteProcessor);
		assertEquals(2,proc.getWidth());
		assertEquals(3,proc.getHeight());
		assertArrayEquals(new byte[]{0,40,0,40,120,-96},(byte[])proc.getPixels());

		proc = vs.getProcessor(2);
		assertTrue(proc instanceof ByteProcessor);
		assertEquals(2,proc.getWidth());
		assertEquals(3,proc.getHeight());
		assertArrayEquals(new byte[]{0,40,0,40,120,-96},(byte[])proc.getPixels());
	}

	@Test
	public void testIsVirtual() {
		// should always be true
		vs = new VirtualStack();
		assertTrue(vs.isVirtual());
	}

	@Test
	public void testTrim() {

		// try trimming a big stack over and over
		vs = nSliceStack(104);
		for (int i = 0; i < 25; i++)

		// should be unchanged
		assertEquals(104,vs.getSize());
	}

	@Test
	public void testSaveChanges() {

		// try saving changes over and over on a big stack
		vs = nSliceStack(104);
		for (int i = 0; i < 104; i++)
		{
			int sliceNumber = i+1;
			// should always be -1
			assertEquals(-1,vs.saveChanges(sliceNumber));
		}
	}

	@Test
	public void testGetDirectory() {
		// one simple test
		vs = new VirtualStack(4,3,null,"mySuperDuperPath");
		assertEquals("mySuperDuperPath",vs.getDirectory());
	}

	@Test
	public void testGetFileName() {

		// test that each filename is equal to its original slice name

		vs = new VirtualStack(2,3,null,"wusgoBoof");

		vs.addSlice("snort");
		vs.addSlice("snicker");

		assertEquals("snort",vs.getFileName(1));
		assertEquals("snicker",vs.getFileName(2));
	}

	@Test
	public void testSetAndGetBitDepth() {

		// should be able to set and get any bitdepth - they have no side effects

		vs = new VirtualStack();
		vs.setBitDepth(1005);
		assertEquals(1005,vs.getBitDepth());
	}
	
	@Test
	public void testMemUsage()
	{
		long memoryBefore = Runtime.getRuntime().freeMemory();
		vs = new VirtualStack(12000,20000,null,"/data/tmp.gif");
		long memoryAfter = Runtime.getRuntime().freeMemory();
		assertTrue((memoryBefore - memoryAfter) < 2048);
	}
}
