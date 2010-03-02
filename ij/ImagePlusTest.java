package ij;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.awt.Image;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.awt.image.IndexColorModel;

import ij.gui.ImageWindow;
import ij.gui.StackWindow;
import ij.io.FileInfo;
import ij.process.ImageProcessor;
import ij.process.ByteProcessor;
import ij.process.ShortProcessor;
import ij.process.ColorProcessor;
import ij.process.FloatProcessor;

public class ImagePlusTest {

	ImagePlus ip;
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}
	
	// make sure our public constants exist and have correct values
	@Test
	public void testPublicConstants() {
		ip = new ImagePlus();
		assertEquals(0,ImagePlus.GRAY8);
		assertEquals(1,ImagePlus.GRAY16);
		assertEquals(2,ImagePlus.GRAY32);
		assertEquals(3,ImagePlus.COLOR_256);
		assertEquals(4,ImagePlus.COLOR_RGB);
	}

	// make sure public variables exist
	// NOTE - the source code mentions all of these but ip.changes are obsolete. May not need/want this test.
	
	@Test
	public void testPublicInstanceVars() {
		ip = new ImagePlus();
		assertFalse(ip.changes);
		assertEquals(1.0,ip.pixelHeight,0.0);
		assertEquals(1.0,ip.pixelWidth,0.0);
		assertEquals("pixel",ip.unit);
		assertEquals("pixel",ip.units);
		assertFalse(ip.sCalibrated);
	}

	@Test
	public void testImagePlus() {
		ip = new ImagePlus();
		assertNotNull(ip);
		assertEquals("null",ip.getTitle());
	}

	@Test
	public void testImagePlusStringImage() {
		// pass in a null image
		ip = new ImagePlus("Cousin",(BufferedImage)null);
		assertNotNull(ip);
		assertEquals("Cousin",ip.getTitle());
		assertNull(ip.getImage());
		
		// pass in a real image
		Image img = new BufferedImage(50,75,BufferedImage.TYPE_USHORT_555_RGB);
		ip = new ImagePlus("Vinny",img);
		assertNotNull(ip);
		assertEquals("Vinny",ip.getTitle());
		assertNotNull(ip.getImage());  // a different image is created internally so can only test existence
	}

	@Test
	public void testImagePlusStringImageProcessor() {
		// note the underlying ImagePlus method does nothing except call a public method. That method is tested later
		// in this file so really there is nothing to do here
		ip = new ImagePlus("Houdini", new ByteProcessor(20,45));
		assertNotNull(ip);
	}

	@Test
	public void testImagePlusString() {
		// ImagePlus(string) : the string is either a file location or a URL
		// note: will not test URL version
		
		// try a file that should not exist
		ip = new ImagePlus("data/hongKongFooey.tif");
		assertNotNull(ip);
		assertNull(ip.getImage());

		// try a file that should exist
		ip = new ImagePlus("data/gray8-2x3-sub1.tif");
		assertNotNull(ip);
		assertNotNull(ip.getImage());
		assertEquals(2,ip.getNDimensions());
		assertEquals(2,ip.getHeight());
		assertEquals(3,ip.getWidth());
		assertEquals(1,ip.getStackSize());
		assertEquals(1,ip.getNFrames());
		assertEquals(1,ip.getNChannels());
		assertEquals(ImagePlus.GRAY8,ip.getType());
		assertEquals(8,ip.getBitDepth());
		assertEquals(1,ip.getBytesPerPixel());
		assertEquals("gray8-2x3-sub1.tif",ip.getTitle());
	}

	@Test
	public void testImagePlusStringImageStack() {

		if (IJInfo.RUN_ENHANCED_TESTS)
		{
			// this next one crashes with null ptr excep
			ip = new ImagePlus((String)null,(ImageStack)null);
			assertNotNull(ip);
		}
		
		// this next text should throw an exception because the stack is empty
		try {
			ip = new ImagePlus("", new ImageStack());
			fail();
		} catch (IllegalArgumentException e) {
			assertTrue(true);
		}
		
		ImageStack stack = new ImageStack(2,3);
		stack.addSlice("Zaphod",new byte[] {1,2,3,4,5,6});
		ip = new ImagePlus("Beeplebrox",stack);
		assertNotNull(ip);
		// note: this constructor call delegates everything to setStack(). So we'll test that down below.
		//   No more testing required beyond existence test
	}

	@Test
	public void testLock() {
		ip = new ImagePlus();
		assertTrue(ip.lock());  // obtain lock
		assertFalse(ip.lock()); // fail to lock a second time
	}

	@Test
	public void testLockSilently() {
		ip = new ImagePlus();
		assertTrue(ip.lockSilently());  // obtain lock
		assertFalse(ip.lockSilently()); // fail to lock a second time
	}

	@Test
	public void testUnlock() {
		ip = new ImagePlus();

		// should be able to unlock over and over
		for (int i = 0; i < 20; i++)
			ip.unlock();
		
		assertTrue(ip.lock());  // obtain lock
		assertFalse(ip.lock());  // try to obtain a second lock
		ip.unlock();
		assertTrue(ip.lock());  // obtain lock again
	}

	@Test
	public void testDraw() {
		// for now cannot test
		if (IJInfo.RUN_GUI_TESTS) {
		}
	}

	@Test
	public void testDrawIntIntIntInt() {
		// for now cannot test
		if (IJInfo.RUN_GUI_TESTS) {
		}
	}

	@Test
	public void testUpdateAndDraw() {
		// for now cannot test
		if (IJInfo.RUN_GUI_TESTS) {
		}
	}

	@Test
	public void testUpdateChannelAndDraw() {
		// for now cannot test
		if (IJInfo.RUN_GUI_TESTS) {
		}
	}

	@Test
	public void testGetChannelProcessor() {
		// getChannelProcessor() overridden by other classes. Default should be same as getProcessor()
		ip = new ImagePlus("data/gray8-2x3-sub1.tif");
		ImageProcessor proc = ip.getProcessor();
		assertEquals(proc,ip.getChannelProcessor());
	}

	@Test
	public void testGetLuts() {
		// getLuts() overridden by other classes. Default should be null
		ip = new ImagePlus("data/gray8-2x3-sub1.tif");
		assertNull(ip.getLuts());
	}

	@Test
	public void testRepaintWindow() {
		// for now cannot test
		if (IJInfo.RUN_GUI_TESTS) {
		}
	}

	@Test
	public void testUpdateAndRepaintWindow() {
		// for now cannot test
		if (IJInfo.RUN_GUI_TESTS) {
		}
	}

	@Test
	public void testUpdateImage() {
		ip = new ImagePlus();
		assertNull(ip.getImage());
		ip.updateImage();
		assertNull(ip.getImage());

		ip = new ImagePlus("data/gray8-2x3-sub1.tif");
		ip.updateImage();
		assertNotNull(ip.getImage());
	}

	@Test
	public void testHide() {
		// TODO: mostly gui code but some non gui - think how to test
	}

	@Test
	public void testClose() {
		// TODO: mostly gui code but some non gui - think how to test
	}

	@Test
	public void testShow() {
		// no need to test
		//  just calls show(String) method with argument "".
		// if next test thorough this routine automatically handled
	}

	@Test
	public void testShowString() {
		// for now cannot test
		if (IJInfo.RUN_GUI_TESTS) {
			// NOTE - when do test make sure it can handle string argument of "".
		}
	}

	@Test
	public void testSetActivated() {
		// NOTE - setActivated sets a private var that has no getter - can't test
	}

	@Test
	public void testGetImage() {
		
		// exercising updateImage() and getImage() in combo is a good test for getImage()
		
		ip = new ImagePlus();
		assertNull(ip.getImage());
		ip.updateImage();
		assertNull(ip.getImage());

		ip = new ImagePlus("data/gray8-2x3-sub1.tif");
		ip.updateImage();
		assertNotNull(ip.getImage());
	}

	@Test
	public void testGetBufferedImage() {

		// try default data
		if (IJInfo.RUN_ENHANCED_TESTS)
		{
			// the call to getBufferedImage() fails if its ip field not set first
			ip = new ImagePlus();
			BufferedImage image = ip.getBufferedImage();
			assertNotNull(image);
		}
		
		// try a non composite image
		ip = new ImagePlus("data/gray8-2x3-sub1.tif");
		assertNotNull(ip.getBufferedImage());
		
		// try a composite image
		// TODO - use an image that isComposite() will return true on
	}

	@Test
	public void testGetID() {
		// can't really test. Its a getter with no side effects. Can't predict its value since its different
		// after every creation of an ImagePlus.
	}

	@Test
	public void testSetImage() {
		BufferedImage b;
		ImagePlus savedIp;
		
		// send in Buffered Image of TYPE_USHORT_GRAY
		ip = new ImagePlus();
		assertNull(ip.getProcessor());
		b = new BufferedImage(2,4,BufferedImage.TYPE_USHORT_GRAY);
		ip.setImage(b);
		assertNotNull(ip.getProcessor());
		assertTrue(ip.getProcessor() instanceof ShortProcessor);
		
		// send in Buffered Image of TYPE_BYTE_GRAY
		ip = new ImagePlus();
		assertNull(ip.getProcessor());
		b = new BufferedImage(2,4,BufferedImage.TYPE_BYTE_GRAY);
		ip.setImage(b);
		assertNotNull(ip.getProcessor());
		assertTrue(ip.getProcessor() instanceof ByteProcessor);
		
		// send in Buffered Image of some other type
		ip = new ImagePlus();
		assertNull(ip.getProcessor());
		b = new BufferedImage(2,4,BufferedImage.TYPE_INT_RGB);
		ip.setImage(b);
		assertNotNull(ip.getProcessor());
		assertTrue(ip.getProcessor() instanceof ColorProcessor);
		assertNull(ip.getRoi());
		assertEquals(4,ip.getHeight());
		assertEquals(2,ip.getWidth());
		assertEquals(ImagePlus.COLOR_RGB,ip.getType());
	}

	@Test
	public void testSetProcessor() {

		ImageProcessor proc;
		ImageStack st;
		
		// throws exception if passed null processor
		ip = new ImagePlus();
		try {
			ip.setProcessor("DoesNotMatterForThisCase",null);
			fail();
		} catch (IllegalArgumentException e) {
			assertTrue(true);
		}
		
		// throws exception if passed processor has no pixels
		ip = new ImagePlus();
		proc = new ByteProcessor(3,5,null,new IndexColorModel(8,1,new byte[]{1},new byte[]{2},new byte[]{3}));
		try {
			ip.setProcessor("DoesNotMatterForThisCase",proc);
			fail();
		} catch (IllegalArgumentException e) {
			assertTrue(true);
		}
		
		// if stack size > 1 and passed processor dims != my dims throw IllegArgExcep
		proc = new ByteProcessor(1,3,new byte[] {1,2,3},new IndexColorModel(8,1,new byte[]{1},new byte[]{2},new byte[]{3}));
		st = new ImageStack(1,3);
		st.addSlice("Slice1",proc);
		st.addSlice("Slice2",proc);
		ip = new ImagePlus();
		ip.width = 1;
		ip.height = 3;
		ip.setStack("TheStack", st);
		ip.height = 4;
		ip.width = 7;
		try {
			ip.setProcessor("DoesNotMatterForThisCase",proc);
			fail();
		} catch (IllegalArgumentException e) {
			assertTrue(true);
		}
		
		// if stack size <= 1 then stack should be null and currSlice should be 1
		proc = new ByteProcessor(1,3,new byte[] {1,2,3},new IndexColorModel(8,1,new byte[]{1},new byte[]{2},new byte[]{3}));
		st = new ImageStack(1,3);
		st.addSlice("Slice1",proc);
		ip = new ImagePlus();
		ip.width = 1;
		ip.height = 3;
		ip.setStack("TheStack", st);
		ip.setProcessor("DoesNotMatterForThisCase",proc);
		assertEquals(1,ip.getStackSize());
		assertEquals(1,ip.currentSlice);
		
		// otherwise fall through and setProc2() gets run internally so test some vars afterwards
		//   null title
		proc = new ByteProcessor(1,3,new byte[] {1,2,3},new IndexColorModel(8,1,new byte[]{1},new byte[]{2},new byte[]{3}));
		st = new ImageStack(1,3);
		st.addSlice("Slice1",proc);
		st.addSlice("Slice2",proc);
		ip = new ImagePlus();
		ip.width = 1;
		ip.height = 3;
		ip.setStack("TheStack", st);
		ip.setProcessor(null,proc);
		assertEquals(2,ip.getStackSize());
		assertEquals("TheStack",ip.getTitle());
		assertEquals(proc,ip.getProcessor());
		assertEquals(ImagePlus.GRAY8,ip.getType());
		assertEquals(8,ip.getBitDepth());
		assertEquals(1,ip.getBytesPerPixel());
		//   non-null title
		proc = new ByteProcessor(1,3,new byte[] {1,2,3},new IndexColorModel(8,1,new byte[]{1},new byte[]{2},new byte[]{3}));
		st = new ImageStack(1,3);
		st.addSlice("Slice1",proc);
		st.addSlice("Slice2",proc);
		ip = new ImagePlus();
		ip.width = 1;
		ip.height = 3;
		ip.setStack("TheStack", st);
		ip.setProcessor("MattersForThisCase",proc);
		assertEquals(2,ip.getStackSize());
		assertEquals("MattersForThisCase",ip.getTitle());
		assertEquals(proc,ip.getProcessor());
		assertEquals(ImagePlus.GRAY8,ip.getType());
		assertEquals(8,ip.getBitDepth());
		assertEquals(1,ip.getBytesPerPixel());
	}

	@Test
	public void testSetStackStringImageStack() {
		// will only test non-gui capabilities of method
		
		ImageStack st;
		ImageProcessor proc;
		
		// stack size == 0 throws illArgExc
		ip = new ImagePlus();
		st = new ImageStack();
		try {
			ip.setStack("Gizzard",st);
			fail();
		} catch (IllegalArgumentException e) {
			assertTrue(true);
		}
		
		// not a virt stack and null imagearray (impossible) or 1st entry of imagearray is null (possible) throw illArgExc
		ip = new ImagePlus();
		proc = new ByteProcessor(1,3,new byte[] {1,2,3},new IndexColorModel(8,1,new byte[]{1},new byte[]{2},new byte[]{3}));
		st = new ImageStack(1,3,14); // 14 empty slices sized 1x3
		try {
			ip.setStack("14Plates",st);
			fail();
		} catch (IllegalArgumentException e) {
			assertTrue(true);
		}

		// otherwise it runs through
		//   ... doesn't seem like there is anything to test
		ip = new ImagePlus();
		ip.width = 1;
		ip.height = 3;
		proc = new ByteProcessor(1,3,new byte[] {1,2,3},new IndexColorModel(8,1,new byte[]{1},new byte[]{2},new byte[]{3}));
		st = new ImageStack(1,3);
		st.addSlice("Slice1",proc);
		ip.setStack("SuperStack",st);
		assertEquals(st,ip.getStack());
	}

	@Test
	public void testSetStackImageStackIntIntInt() {
		ImageStack st;
		
		// input dimensions do not match stack's size
		st = new ImageStack(1,2,24);  // 24 slices of 1x2 images
		ip = new ImagePlus();
		
		for (int chan = 0; chan < 9; chan++)
			for (int slice = 0; slice < 9; slice++)
				for (int frame = 0; frame < 9; frame++)
					if (chan*slice*frame != 24)
						try {
							ip.setStack(st,chan,slice,frame);
							fail();
						} catch (IllegalArgumentException e) {
							assertTrue(true);
						}
				
		// otherwise input dimensions match
		//   .. delegates to setStack() - tested earlier
		//   just need to test that title did not change
		ip = new ImagePlus();
		ip.width = 1;
		ip.height = 2;
		ip.setTitle("AlertAlert");
		st = new ImageStack(1,2,null);  // 1x2 images
		for (int i = 0; i < 24; i++)
			st.addSlice(""+i, new byte[] {1,2});
		ip.setStack(st,2,3,4);
		assertEquals("AlertAlert",ip.getTitle());
	}

	@Test
	public void testSetFileInfo() {

		// Sets a private variable. getFileInfo() is not a getter but instead has a lot of side effects.
		// As it is I can't really test this method.
		
	}

	@Test
	public void testGetWindow() {
		// can only test nonGUI part of this code
		ip = new ImagePlus();
		assertNull(ip.getWindow());
	}

	@Test
	public void testIsVisible() {
		// can only test nonGUI part of this code
		ip = new ImagePlus();
		assertFalse(ip.isVisible());
	}

	@Test
	public void testSetWindow() {
		// for now cannot test
		if (IJInfo.RUN_GUI_TESTS) {
		}
	}

	@Test
	public void testGetCanvas() {
		// for now cannot test
		if (IJInfo.RUN_GUI_TESTS) {
		}
	}

	@Test
	public void testSetColor() {
		
		ImageProcessor proc;
		proc = new ByteProcessor(1,3,new byte[] {1,2,3},new IndexColorModel(8,1,new byte[]{1},new byte[]{2},new byte[]{3}));

		if (IJInfo.RUN_ENHANCED_TESTS)
		{
			// this test crashes with a null ptr exception
			ip = new ImagePlus("MyChemicalRomance", proc);
			ip.setColor(null);
		}
		
		// try to set color of ImagePlus with no processor
		//   ... should do nothing
		ip = new ImagePlus();
		ip.setColor(Color.yellow);
		assertTrue(true);
		
		// try to set color of processor when it exists
		//   ... should set something lower -> defers to ImageProcessor.setColor() -> test there
		ip = new ImagePlus("MyChemicalRomance", proc);
		ip.setColor(Color.magenta);
		assertTrue(true);
	}

	@Test
	public void testIsProcessor() {
		
		ip = new ImagePlus();
		assertFalse(ip.isProcessor());
		
		ImageProcessor proc;
		proc = new ByteProcessor(1,3,new byte[] {1,2,3},new IndexColorModel(8,1,new byte[]{1},new byte[]{2},new byte[]{3}));
		ip = new ImagePlus("FredFred",proc);
		assertTrue(ip.isProcessor());
	}

	@Test
	public void testGetProcessor() {
		// no img and no proc returns null
		ip = new ImagePlus();
		assertNull(ip.getProcessor());
		
		// otherwise get into the method
		ImageProcessor proc;
		proc = new ByteProcessor(1,3,new byte[] {1,2,3},new IndexColorModel(8,1,new byte[]{1},new byte[]{2},new byte[]{3}));
		ip = new ImagePlus("FredFred",proc);
		assertEquals(proc,ip.getProcessor());
	}

	@Test
	public void testTrimProcessor() {
		fail("Not yet implemented");
	}

	@Test
	public void testKillProcessor() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetMask() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetStatistics() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetStatisticsInt() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetStatisticsIntInt() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetStatisticsIntIntDoubleDouble() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetTitle() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetShortTitle() {
		fail("Not yet implemented");
	}

	@Test
	public void testSetTitle() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetWidth() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetHeight() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetStackSize() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetImageStackSize() {
		fail("Not yet implemented");
	}

	@Test
	public void testSetDimensions() {
		fail("Not yet implemented");
	}

	@Test
	public void testIsHyperStack() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetNDimensions() {
		fail("Not yet implemented");
	}

	@Test
	public void testIsDisplayedHyperStack() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetNChannels() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetNSlices() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetNFrames() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetDimensions() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetType() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetBitDepth() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetBytesPerPixel() {
		fail("Not yet implemented");
	}

	@Test
	public void testSetType() {
		fail("Not yet implemented");
	}

	@Test
	public void testSetProperty() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetProperty() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetProperties() {
		fail("Not yet implemented");
	}

	@Test
	public void testCreateLut() {
		fail("Not yet implemented");
	}

	@Test
	public void testIsInvertedLut() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetPixel() {
		fail("Not yet implemented");
	}

	@Test
	public void testCreateEmptyStack() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetStack() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetImageStack() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetCurrentSlice() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetChannel() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetSlice() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetFrame() {
		fail("Not yet implemented");
	}

	@Test
	public void testKillStack() {
		fail("Not yet implemented");
	}

	@Test
	public void testSetPositionIntIntInt() {
		fail("Not yet implemented");
	}

	@Test
	public void testSetPositionWithoutUpdate() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetStackIndex() {
		fail("Not yet implemented");
	}

	@Test
	public void testResetStack() {
		fail("Not yet implemented");
	}

	@Test
	public void testSetPositionInt() {
		fail("Not yet implemented");
	}

	@Test
	public void testSetSlice() {
		fail("Not yet implemented");
	}

	@Test
	public void testSetSliceWithoutUpdate() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetRoi() {
		fail("Not yet implemented");
	}

	@Test
	public void testSetRoiRoi() {
		fail("Not yet implemented");
	}

	@Test
	public void testSetRoiRoiBoolean() {
		fail("Not yet implemented");
	}

	@Test
	public void testSetRoiIntIntIntInt() {
		fail("Not yet implemented");
	}

	@Test
	public void testSetRoiRectangle() {
		fail("Not yet implemented");
	}

	@Test
	public void testCreateNewRoi() {
		fail("Not yet implemented");
	}

	@Test
	public void testKillRoi() {
		fail("Not yet implemented");
	}

	@Test
	public void testSaveRoi() {
		fail("Not yet implemented");
	}

	@Test
	public void testRestoreRoi() {
		fail("Not yet implemented");
	}

	@Test
	public void testRevert() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetFileInfo() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetOriginalFileInfo() {
		fail("Not yet implemented");
	}

	@Test
	public void testImageUpdate() {
		fail("Not yet implemented");
	}

	@Test
	public void testFlush() {
		fail("Not yet implemented");
	}

	@Test
	public void testSetIgnoreFlush() {
		fail("Not yet implemented");
	}

	@Test
	public void testCreateImagePlus() {
		fail("Not yet implemented");
	}

	@Test
	public void testCreateHyperStack() {
		fail("Not yet implemented");
	}

	@Test
	public void testCopyScale() {
		fail("Not yet implemented");
	}

	@Test
	public void testStartTiming() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetStartTime() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetCalibration() {
		fail("Not yet implemented");
	}

	@Test
	public void testSetCalibration() {
		fail("Not yet implemented");
	}

	@Test
	public void testSetGlobalCalibration() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetGlobalCalibration() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetLocalCalibration() {
		fail("Not yet implemented");
	}

	@Test
	public void testMouseMoved() {
		fail("Not yet implemented");
	}

	@Test
	public void testUpdateStatusbarValue() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetLocationAsString() {
		fail("Not yet implemented");
	}

	@Test
	public void testCopy() {
		fail("Not yet implemented");
	}

	@Test
	public void testPaste() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetClipboard() {
		fail("Not yet implemented");
	}

	@Test
	public void testResetClipboard() {
		fail("Not yet implemented");
	}

	@Test
	public void testAddImageListener() {
		fail("Not yet implemented");
	}

	@Test
	public void testRemoveImageListener() {
		fail("Not yet implemented");
	}

	@Test
	public void testIsLocked() {
		fail("Not yet implemented");
	}

	@Test
	public void testSetOpenAsHyperStack() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetOpenAsHyperStack() {
		fail("Not yet implemented");
	}

	@Test
	public void testIsComposite() {
		fail("Not yet implemented");
	}

	@Test
	public void testSetDisplayRangeDoubleDouble() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetDisplayRangeMin() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetDisplayRangeMax() {
		fail("Not yet implemented");
	}

	@Test
	public void testSetDisplayRangeDoubleDoubleInt() {
		fail("Not yet implemented");
	}

	@Test
	public void testResetDisplayRange() {
		fail("Not yet implemented");
	}

	@Test
	public void testUpdatePosition() {
		fail("Not yet implemented");
	}

	@Test
	public void testFlatten() {
		fail("Not yet implemented");
	}

	@Test
	public void testSetOverlayOverlay() {
		fail("Not yet implemented");
	}

	@Test
	public void testSetOverlayShapeColorBasicStroke() {
		fail("Not yet implemented");
	}

	@Test
	public void testSetOverlayRoiColorIntColor() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetOverlay() {
		fail("Not yet implemented");
	}

	@Test
	public void testSetDisplayListVector() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetDisplayList() {
		fail("Not yet implemented");
	}

	@Test
	public void testSetDisplayListShapeColorBasicStroke() {
		fail("Not yet implemented");
	}

	@Test
	public void testSetDisplayListRoiColorIntColor() {
		fail("Not yet implemented");
	}

	@Test
	public void testClone() {
		fail("Not yet implemented");
	}

	@Test
	public void testToString() {
		fail("Not yet implemented");
	}

}
