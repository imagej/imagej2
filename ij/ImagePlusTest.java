package ij;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.awt.Image;
import java.awt.Color;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.awt.image.IndexColorModel;
import java.util.Arrays;
import java.util.Properties;

//import ij.gui.ImageWindow;
//import ij.gui.StackWindow;
//import ij.io.FileInfo;
import ij.gui.Line;
import ij.gui.Roi;
import ij.gui.StackWindow;
import ij.io.Assert;
import ij.measure.Calibration;
import ij.process.ImageProcessor;
import ij.process.ByteProcessor;
import ij.process.ShortProcessor;
import ij.process.ColorProcessor;
import ij.process.FloatProcessor;
import ij.process.ImageStatistics;
import ij.measure.Measurements;

public class ImagePlusTest {

	ImagePlus ip;
	ImageStack st;
	ImageProcessor proc;
	
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
		/* OBSOLETE
		assertEquals(1.0,ip.pixelHeight,0.0);
		assertEquals(1.0,ip.pixelWidth,0.0);
		assertEquals("pixel",ip.unit);
		assertEquals("pixel",ip.units);
		assertFalse(ip.sCalibrated);
		*/
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
		proc = new ByteProcessor(20,45);
		ip = new ImagePlus("Houdini", proc);
		assertNotNull(ip);
		assertEquals(proc,ip.getProcessor());
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
		
		st = new ImageStack(2,3);
		st.addSlice("Zaphod",new byte[] {1,2,3,4,5,6});
		ip = new ImagePlus("Beeplebrox",st);
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
		// note - for now cannot test
		if (IJInfo.RUN_GUI_TESTS) {
		}
	}

	@Test
	public void testDrawIntIntIntInt() {
		// note - for now cannot test
		if (IJInfo.RUN_GUI_TESTS) {
		}
	}

	@Test
	public void testUpdateAndDraw() {
		// note - for now cannot test
		if (IJInfo.RUN_GUI_TESTS) {
		}
	}

	@Test
	public void testUpdateChannelAndDraw() {
		// note - for now cannot test
		if (IJInfo.RUN_GUI_TESTS) {
		}
	}

	@Test
	public void testGetChannelProcessor() {
		// getChannelProcessor() overridden by other classes. Default should be same as getProcessor()
		ip = new ImagePlus("data/gray8-2x3-sub1.tif");
		proc = ip.getProcessor();
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
		// note - for now cannot test
		if (IJInfo.RUN_GUI_TESTS) {
		}
	}

	@Test
	public void testUpdateAndRepaintWindow() {
		// note - for now cannot test
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
		// note - no need to test
		//  just calls show(String) method with argument "".
		// if next test thorough this routine automatically handled
	}

	// TODO: think how to test better
	@Test
	public void testShowString() {
		// note - for now cannot test
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
		// note - can't really test. Its a getter with no side effects. Can't predict its value since its
		// different after every creation of an ImagePlus.
	}

	@Test
	public void testSetImage() {
		BufferedImage b;
		
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
		
		// TODO: might need to test more re: setupProcessor() call inside setImage()
		
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
		
		// TODO: think how to test the setprocessor2 side effects better?
		// otherwise fall through and setProc2() gets run internally so test some vars afterwards
		//   try with null title
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
		//   try with non-null title
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
		// note - will only test non-gui capabilities of method
		
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
		//   so just need to test that title did not change
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

		// note - sets a private variable. And getFileInfo() can't act as a getter because it has a lot of side effects.
		// As it is I can't really test this method.
		
	}

	@Test
	public void testGetWindow() {
		// note - can only test nonGUI part of this code
		ip = new ImagePlus();
		assertNull(ip.getWindow());
	}

	@Test
	public void testIsVisible() {
		// note - can only test nonGUI part of this code
		ip = new ImagePlus();
		assertFalse(ip.isVisible());
	}

	@Test
	public void testSetWindow() {
		// note - for now cannot test
		if (IJInfo.RUN_GUI_TESTS) {
		}
	}

	@Test
	public void testGetCanvas() {
		// note - for now cannot test
		if (IJInfo.RUN_GUI_TESTS) {
		}
	}

	// Note: can't really test and relies on package visibility which could change
	@Test
	public void testSetColor() {
		
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
		// can't rely on as packages may change : assertNull(ip.ip);
		ip.setColor(Color.yellow);
		// can't do due to access: assertEquals(ip.ip.drawingColor,Color.black);
		
		// try to set color of processor when it exists
		//   ... should set something lower -> defers to ImageProcessor.setColor() -> test there
		ip = new ImagePlus("MyChemicalRomance", proc);
		// can't rely on as packages may change : assertEquals(proc,ip.ip);
		ip.setColor(Color.magenta);
		// can't do due to access: assertEquals(ip.ip.drawingColor,Color.magenta);
	}

	@Test
	public void testIsProcessor() {
		
		ip = new ImagePlus();
		assertFalse(ip.isProcessor());
		
		proc = new ByteProcessor(1,3,new byte[] {1,2,3},new IndexColorModel(8,1,new byte[]{1},new byte[]{2},new byte[]{3}));
		ip = new ImagePlus("FredFred",proc);
		assertTrue(ip.isProcessor());
	}

	@Test
	public void testGetProcessor() {
		// no img and no proc returns null
		ip = new ImagePlus();
		assertNull(ip.getProcessor());
		
		// otherwise it gets into the method
		
		//   uncalibrated subcase
		proc = new ByteProcessor(1,3,new byte[] {1,2,3},new IndexColorModel(8,1,new byte[]{1},new byte[]{2},new byte[]{3}));
		ip = new ImagePlus("FredFred",proc);
		assertEquals(proc,ip.getProcessor());
		assertEquals(Line.getWidth(),proc.getLineWidth());
		assertNull(proc.getCalibrationTable());

		//   calibrated subcase
		proc = new ByteProcessor(1,3,new byte[] {1,2,3},new IndexColorModel(8,1,new byte[]{1},new byte[]{2},new byte[]{3}));
		ip = new ImagePlus("FredFred",proc);
		ip.getCalibration().setFunction(Calibration.STRAIGHT_LINE, new double[] {3,5}, "Splutterflits");
		assertEquals(proc,ip.getProcessor());
		assertEquals(Line.getWidth(),proc.getLineWidth());
		assertNotNull(proc.getCalibrationTable());
		
		// note - untested side effect - Recorder records image was updated (sets a flag to true)
	}

	@Test
	public void testTrimProcessor() {
		byte[] bytes = new byte[] {6,5,4};
		
		// default case - not locked and not null proc
		proc = new ByteProcessor(1,3,new byte[] {1,2,3},new IndexColorModel(8,1,new byte[]{1},new byte[]{2},new byte[]{3}));
		proc.setSnapshotPixels(bytes);
		ip = new ImagePlus("Hookah",proc);
		ip.trimProcessor();
		assertNull(proc.getSnapshotPixels());
		
		// if locked
		proc = new ByteProcessor(1,3,new byte[] {1,2,3},new IndexColorModel(8,1,new byte[]{1},new byte[]{2},new byte[]{3}));
		proc.setSnapshotPixels(bytes);
		ip = new ImagePlus("Hookah",proc);
		ip.lock();
		ip.trimProcessor();
		assertEquals(bytes,proc.getSnapshotPixels());
		
		// no need to test null proc case
	}

	/* OBSOLETE
	@Test
	public void testKillProcessor() {
		ip = new ImagePlus();
		ip.killProcessor();
	}
	*/

	@Test
	public void testGetMask() {
		
		// if roi is null
		proc = new ByteProcessor(1,3,new byte[] {1,2,3},new IndexColorModel(8,1,new byte[]{1},new byte[]{2},new byte[]{3}));
		ip = new ImagePlus("CornPalace",proc);
		assertEquals(new Rectangle(0,0,1,3),proc.getRoi());
		proc.setRoi(new Rectangle(0,1,0,1));
		assertEquals(new Rectangle(0,1,0,1),proc.getRoi());
		assertNull(ip.getMask());
		assertEquals(new Rectangle(0,0,1,3),proc.getRoi());
		
		// else roi not null and rectangular roi
		proc = new ByteProcessor(1,3,new byte[] {1,2,3},new IndexColorModel(8,1,new byte[]{1},new byte[]{2},new byte[]{3}));
		proc.setRoi(new Rectangle(0,1,0,1));
		ip = new ImagePlus("CowMadHouse",proc);
		assertNull(ip.getMask());  // always should return null for rectangle roi's

		// else roi not null and nonrectangular roi
		Roi region = new Roi(0,0,1,1,1); // last param key : arcsize != 0
		proc = new ByteProcessor(2,2,new byte[] {1,2,3,4},new IndexColorModel(8,1,new byte[]{1},new byte[]{2},new byte[]{3}));
		proc.setRoi(new Roi(new Rectangle(0,0,1,0)));
		ip = new ImagePlus("ChickenLooneyBin",proc);
		ip.setRoi(region);
		assertNotNull(ip.getMask());
		// TODO - fails this next test though object inspection makes them look the same.
		//        Once again I need my own equals() code - one for each kind of processor.
		//assertEquals(region.getMask(),ip.getProcessor().getMask());
		assertEquals(region.getBounds(),ip.getProcessor().getRoi());
	}

	// note - ImageStatistics does not override equals() so I need to do some testing of my own
	//   Will test a subset of fields. Will also test floats for equality - usually a no-no but desired here I think
	
	private boolean imageStatsEquals(ImageStatistics a, ImageStatistics b)
	{
		if (!Arrays.equals(a.histogram, b.histogram))
			return false;

		if (a.pixelCount != b.pixelCount)	
			return false;

		if (a.mode != b.mode)	
			return false;

		if (a.dmode != b.dmode)	
			return false;

		if (a.area != b.area)	
			return false;

		if (a.min != b.min)	
			return false;

		if (a.max != b.max)	
			return false;

		if (a.mean != b.mean)	
			return false;

		if (a.median != b.median)	
			return false;

		if (a.stdDev != b.stdDev)	
			return false;

		if (a.skewness != b.skewness)	
			return false;

		if (a.kurtosis != b.kurtosis)	
			return false;

		if (a.xCentroid != b.xCentroid)	
			return false;

		if (a.yCentroid != b.yCentroid)	
			return false;

		if (a.xCenterOfMass != b.xCenterOfMass)
			return false;

		if (a.yCenterOfMass != b.yCenterOfMass)
			return false;

		return true;
	}
	
	@Test
	public void testGetStatistics() {
		ImageStatistics expected,actual;
		proc = new ByteProcessor(1,3,new byte[] {1,2,3},new IndexColorModel(8,1,new byte[]{1},new byte[]{2},new byte[]{3}));
		ip = new ImagePlus("HilarityEnsues",proc);
		expected = ip.getStatistics(Measurements.AREA + Measurements.MEAN + Measurements.MODE + Measurements.MIN_MAX);
		actual = ip.getStatistics();
		assertTrue(imageStatsEquals(expected,actual));
	}

	@Test
	public void testGetStatisticsInt() {
		ImageStatistics expected,actual;
		
		proc = new ByteProcessor(1,3,new byte[] {1,2,3},new IndexColorModel(8,1,new byte[]{1},new byte[]{2},new byte[]{3}));
		ip = new ImagePlus("MySocksAreBunched",proc);
		expected = ip.getStatistics(Measurements.AREA + Measurements.MEAN + Measurements.MODE + Measurements.MIN_MAX,256,0.0,0.0);
		actual = ip.getStatistics(Measurements.AREA + Measurements.MEAN + Measurements.MODE + Measurements.MIN_MAX);
		assertTrue(imageStatsEquals(expected,actual));
	}

	@Test
	public void testGetStatisticsIntInt() {
		ImageStatistics expected,actual;
		
		proc = new ByteProcessor(1,3,new byte[] {1,2,3},new IndexColorModel(8,1,new byte[]{1},new byte[]{2},new byte[]{3}));
		ip = new ImagePlus("GaleForceWinds",proc);
		expected = ip.getStatistics(Measurements.AREA + Measurements.MEAN + Measurements.MODE + Measurements.MIN_MAX,0,0.0,0.0);
		actual = ip.getStatistics(Measurements.AREA + Measurements.MEAN + Measurements.MODE + Measurements.MIN_MAX,0);
		assertTrue(imageStatsEquals(expected,actual));
		
		proc = new ByteProcessor(1,3,new byte[] {1,2,3},new IndexColorModel(8,1,new byte[]{1},new byte[]{2},new byte[]{3}));
		ip = new ImagePlus("GaleForceWinds",proc);
		expected = ip.getStatistics(Measurements.AREA + Measurements.MEAN + Measurements.MODE + Measurements.MIN_MAX,3,0.0,0.0);
		actual = ip.getStatistics(Measurements.AREA + Measurements.MEAN + Measurements.MODE + Measurements.MIN_MAX,3);
		assertTrue(imageStatsEquals(expected,actual));

		proc = new ByteProcessor(1,3,new byte[] {1,2,3},new IndexColorModel(8,1,new byte[]{1},new byte[]{2},new byte[]{3}));
		ip = new ImagePlus("GaleForceWinds",proc);
		expected = ip.getStatistics(Measurements.AREA + Measurements.MEAN + Measurements.MODE + Measurements.MIN_MAX,7,0.0,0.0);
		actual = ip.getStatistics(Measurements.AREA + Measurements.MEAN + Measurements.MODE + Measurements.MIN_MAX,7);
		assertTrue(imageStatsEquals(expected,actual));

		proc = new ByteProcessor(1,3,new byte[] {1,2,3},new IndexColorModel(8,1,new byte[]{1},new byte[]{2},new byte[]{3}));
		ip = new ImagePlus("GaleForceWinds",proc);
		expected = ip.getStatistics(Measurements.AREA + Measurements.MEAN + Measurements.MODE + Measurements.MIN_MAX,1024,0.0,0.0);
		actual = ip.getStatistics(Measurements.AREA + Measurements.MEAN + Measurements.MODE + Measurements.MIN_MAX,1024);
		assertTrue(imageStatsEquals(expected,actual));
	}

	@Test
	public void testGetStatisticsIntIntDoubleDouble() {
		ImageStatistics actual;

		// try {1,2,3}
		proc = new ByteProcessor(1,3,new byte[] {1,2,3},new IndexColorModel(8,1,new byte[]{1},new byte[]{2},new byte[]{3}));
		ip = new ImagePlus("SuperPotato",proc);
		actual = ip.getStatistics(Measurements.AREA + Measurements.MEAN + Measurements.MODE + Measurements.MIN_MAX,3,0.0,0.0);
		assertEquals(3.0,actual.area,Assert.DOUBLE_TOL);
		assertEquals(2.0,actual.mean,Assert.DOUBLE_TOL);
		assertEquals(1.0,actual.stdDev,Assert.DOUBLE_TOL);
		assertEquals(0.0,actual.xCenterOfMass,Assert.DOUBLE_TOL);
		assertEquals(0.0,actual.yCenterOfMass,Assert.DOUBLE_TOL);
		assertEquals(0.0,actual.histogram[0],Assert.DOUBLE_TOL);
		assertEquals(1.0,actual.histogram[1],Assert.DOUBLE_TOL);
		assertEquals(1.0,actual.histogram[2],Assert.DOUBLE_TOL);
		assertEquals(1.0,actual.histogram[3],Assert.DOUBLE_TOL);
		assertEquals(0.0,actual.histogram[4],Assert.DOUBLE_TOL);

		// try {1,1,2,2,3,3,1,2,1}
		proc = new ByteProcessor(3,3,new byte[] {1,1,2,2,3,3,1,2,1},new IndexColorModel(8,1,new byte[]{1},new byte[]{2},new byte[]{3}));
		ip = new ImagePlus("SuperPotato",proc);
		actual = ip.getStatistics(Measurements.AREA + Measurements.MEAN + Measurements.MODE + Measurements.MIN_MAX,3,0.0,0.0);
		assertEquals(9.0,actual.area,Assert.DOUBLE_TOL);
		assertEquals(16.0/9,actual.mean,Assert.DOUBLE_TOL);
		assertEquals(0.83333333333,actual.stdDev,Assert.DOUBLE_TOL);
		assertEquals(0.0,actual.xCenterOfMass,Assert.DOUBLE_TOL);
		assertEquals(0.0,actual.yCenterOfMass,Assert.DOUBLE_TOL);
		assertEquals(0.0,actual.histogram[0],Assert.DOUBLE_TOL);
		assertEquals(4.0,actual.histogram[1],Assert.DOUBLE_TOL);
		assertEquals(3.0,actual.histogram[2],Assert.DOUBLE_TOL);
		assertEquals(2.0,actual.histogram[3],Assert.DOUBLE_TOL);
		assertEquals(0.0,actual.histogram[4],Assert.DOUBLE_TOL);
	}

	@Test
	public void testGetTitle() {
		ip = new ImagePlus(null,(Image)null);
		assertEquals("",ip.getTitle());
		
		ip = new ImagePlus("TitleToCar",(Image)null);
		assertEquals("TitleToCar",ip.getTitle());
	}

	@Test
	public void testGetShortTitle() {
		
		// null string input
		ip = new ImagePlus(null,(Image)null);
		assertEquals("",ip.getShortTitle());
		
		// no spaces or periods
		ip = new ImagePlus("HelloWorld",(Image)null);
		assertEquals("HelloWorld",ip.getShortTitle());

		// only a space
		ip = new ImagePlus(" ",(Image)null);
		assertEquals("",ip.getShortTitle());
		
		// space at beginning
		ip = new ImagePlus(" HelloWorld",(Image)null);
		assertEquals("",ip.getShortTitle());
		
		// space at end
		ip = new ImagePlus("HelloWorld ",(Image)null);
		assertEquals("HelloWorld",ip.getShortTitle());
		
		// space in middle
		ip = new ImagePlus("Hello World",(Image)null);
		assertEquals("Hello",ip.getShortTitle());

		// dot at beginning
		ip = new ImagePlus(".HelloWorld",(Image)null);
		assertEquals(".HelloWorld",ip.getShortTitle());

		// dot at end
		ip = new ImagePlus("HelloWorld.",(Image)null);
		assertEquals("HelloWorld",ip.getShortTitle());

		// dot in middle
		ip = new ImagePlus("Hello.World",(Image)null);
		assertEquals("Hello",ip.getShortTitle());
	
		// dot in middle before space
		ip = new ImagePlus("Hello.Fred World",(Image)null);
		assertEquals("Hello",ip.getShortTitle());

		// dot all alone
		ip = new ImagePlus(".",(Image)null);
		assertEquals(".",ip.getShortTitle());
	}

	@Test
	public void testSetTitle() {
		// note - there is some gui stuff here that is not tested
		if (IJInfo.RUN_GUI_TESTS)
		{
		}
		
		ip = new ImagePlus("TrainWreck",(Image)null);
		ip.setTitle(null);
		assertEquals("TrainWreck",ip.getTitle());
		
		ip = new ImagePlus("HorseAndBuggy",(Image)null);
		ip.setTitle("MopAndBroom");
		assertEquals("MopAndBroom",ip.getTitle());
	}

	@Test
	public void testGetWidth() {
		// just a getter - do a compile time test
		ip = new ImagePlus();
		ip.getWidth();
	}

	@Test
	public void testGetHeight() {
		// just a getter - do a compile time test
		ip = new ImagePlus();
		ip.getHeight();
	}

	@Test
	public void testGetStackSize() {
		
		ip = new ImagePlus();
		assertEquals(1,ip.getStackSize());

		st = new ImageStack(2,2);
		st.addSlice("GrandmaKat", new byte[] {0,8,4,1});
		st.addSlice("GrandpaHuff", new byte[] {0,8,4,1});
		st.addSlice("UncleRemus", new byte[] {0,8,4,1});
		st.addSlice("AuntRomulus", new byte[] {0,8,4,1});
		st.addSlice("BabyAchilles", new byte[] {0,8,4,1});
		
		ip.setStack("CarribeanDreams", st);
		assertEquals(5,ip.getStackSize());
		
		st.deleteLastSlice();
		assertEquals(4,ip.getStackSize());
	}

	@Test
	public void testGetImageStackSize() {
		// note - getImageStackSize() is nearly identical to getStackSize() - one needs to be retired?
		// same tests will suffice
		
		ip = new ImagePlus();
		assertEquals(1,ip.getImageStackSize());

		st = new ImageStack(2,2);
		st.addSlice("GrandmaKat", new byte[] {0,8,4,1});
		st.addSlice("GrandpaHuff", new byte[] {0,8,4,1});
		st.addSlice("UncleRemus", new byte[] {0,8,4,1});
		st.addSlice("AuntRomulus", new byte[] {0,8,4,1});
		st.addSlice("BabyAchilles", new byte[] {0,8,4,1});
		
		ip.setStack("CarribeanDreams", st);
		assertEquals(5,ip.getImageStackSize());
		
		st.deleteLastSlice();
		assertEquals(4,ip.getImageStackSize());
	}

	// there is some gui code in the setDimensions method that is untested here
	@Test
	public void testSetDimensions() {
		
		// empty stack
		ip = new ImagePlus();
		ip.setDimensions(1,2,3);
		assertEquals(1,ip.getNChannels());
		assertEquals(1,ip.getNSlices());
		assertEquals(ip.getStackSize(),ip.getNFrames());
		
		// stack but dimensions don't match
		ip = new ImagePlus("Kerbam",(Image)null);
		st = new ImageStack(2,2);
		st.addSlice("Oranges", new byte[] {1,2,3,4});
		st.addSlice("Apples", new byte[] {1,2,3,4});
		st.addSlice("Pears", new byte[] {1,2,3,4});
		ip.setStack("Kaboom",st);
		ip.setDimensions(1,2,3);
		assertEquals(1,ip.getNChannels());
		assertEquals(ip.getStackSize(),ip.getNSlices());
		assertEquals(1,ip.getNFrames());
		
		// stack with matching dimensions
		ip = new ImagePlus("Kerbam",(Image)null);
		st = new ImageStack(1,4);
		st.addSlice("Oranges", new byte[] {1,2,3,4});
		st.addSlice("Apples", new byte[] {1,2,3,4});
		st.addSlice("Pears", new byte[] {1,2,3,4});
		st.addSlice("Quinces", new byte[] {1,2,3,4});
		st.addSlice("Kiwis", new byte[] {1,2,3,4});
		st.addSlice("Bananas", new byte[] {1,2,3,4});
		ip.setStack("Kaboom",st);
		ip.setDimensions(1,2,3);
		assertEquals(1,ip.getNChannels());
		assertEquals(2,ip.getNSlices());
		assertEquals(3,ip.getNFrames());
	}

	@Test
	public void testIsHyperStack() {
		// due to lack of gui during tests, inside isHyperStack() - isDisplayedHyperStack() always false

		// openAs false, nDimensions <= 3
		ip = new ImagePlus("ack phooey", (Image)null);
		st = new ImageStack(1,4);
		st.addSlice("ouch",new byte[] {1,2,3,4});
		st.addSlice("yowee",new byte[] {1,2,3,4});
		st.addSlice("zounds",new byte[] {1,2,3,4});
		ip.setStack("wonder twins activate", st);
		ip.setDimensions(1,1,3);
		ip.setOpenAsHyperStack(false);
		assertFalse(ip.isHyperStack());
		
		// openAs false, nDimensions > 3
		ip = new ImagePlus("ack phooey", (Image)null);
		st = new ImageStack(1,4);
		st.addSlice("ouch",new byte[] {1,2,3,4});
		st.addSlice("yowee",new byte[] {1,2,3,4});
		st.addSlice("zounds",new byte[] {1,2,3,4});
		st.addSlice("oof",new byte[] {1,2,3,4});
		ip.setStack("wonder twins activate", st);
		ip.setDimensions(1,1,4);
		ip.setOpenAsHyperStack(false);
		assertFalse(ip.isHyperStack());
		
		// openAs true, nDimensions <= 3
		ip = new ImagePlus("ack phooey", (Image)null);
		st = new ImageStack(1,4);
		st.addSlice("ouch",new byte[] {1,2,3,4});
		st.addSlice("yowee",new byte[] {1,2,3,4});
		st.addSlice("zounds",new byte[] {1,2,3,4});
		ip.setStack("wonder twins activate", st);
		ip.setDimensions(1,1,3);
		ip.setOpenAsHyperStack(true);
		assertFalse(ip.isHyperStack());
		
		// openAs true, nDimensions > 3
		ip = new ImagePlus("ack phooey", (Image)null);
		st = new ImageStack(1,4);
		st.addSlice("ouch",new byte[] {1,2,3,4});
		st.addSlice("yowee",new byte[] {1,2,3,4});
		st.addSlice("zounds",new byte[] {1,2,3,4});
		st.addSlice("oof",new byte[] {1,2,3,4});
		ip.setStack("wonder twins activate", st);
		ip.setDimensions(1,2,2);
		ip.setOpenAsHyperStack(true);
		assertTrue(ip.isHyperStack());
	}

	@Test
	public void testGetNDimensions() {
		// all dims <= 1
		ip = new ImagePlus("Agent007", (Image)null);
		st = new ImageStack(2,3);
		st.addSlice("suave",new byte[] {1,2,3,4,5,6});
		ip.setStack("MoneyPenny",st);
		ip.setDimensions(1,1,1);
		assertEquals(2,ip.getNDimensions());

		ip = new ImagePlus("Agent007", (Image)null);
		st = new ImageStack(2,3);
		st.addSlice("suave",new byte[] {1,2,3,4,5,6});
		st.addSlice("debonair",new byte[] {1,2,3,4,5,6});
		st.addSlice("sophisticated",new byte[] {1,2,3,4,5,6});
		st.addSlice("handsome",new byte[] {1,2,3,4,5,6});
		st.addSlice("humorous",new byte[] {1,2,3,4,5,6});
		st.addSlice("aloof",new byte[] {1,2,3,4,5,6});
		st.addSlice("calm",new byte[] {1,2,3,4,5,6});
		st.addSlice("composed",new byte[] {1,2,3,4,5,6});
		ip.setStack("MoneyPenny", st);

		// one dim > 1
		ip.setDimensions(1,1,8);
		assertEquals(3,ip.getNDimensions());
		ip.setDimensions(1,8,1);
		assertEquals(3,ip.getNDimensions());
		ip.setDimensions(8,1,1);
		assertEquals(3,ip.getNDimensions());

		// two dims > 1
		ip.setDimensions(1,2,4);
		assertEquals(4,ip.getNDimensions());
		ip.setDimensions(1,4,2);
		assertEquals(4,ip.getNDimensions());
		ip.setDimensions(2,1,4);
		assertEquals(4,ip.getNDimensions());
		ip.setDimensions(2,4,1);
		assertEquals(4,ip.getNDimensions());
		ip.setDimensions(4,1,2);
		assertEquals(4,ip.getNDimensions());
		ip.setDimensions(4,2,1);
		assertEquals(4,ip.getNDimensions());

		// three dims > 1
		ip.setDimensions(2,2,2);
		assertEquals(5,ip.getNDimensions());
	}

	@Test
	public void testIsDisplayedHyperStack() {
		// note - method requires a gui - can't fully test
		ip = new ImagePlus();
		assertFalse(ip.isDisplayedHyperStack());
	}

	@Test
	public void testGetNChannels() {
		// stack does not match dimensions
		ip = new ImagePlus("Groening", (Image)null);
		st = new ImageStack(2,3);
		st.addSlice("silly",new byte[] {1,2,3,4,5,6});
		ip.setStack("AyeCarumba",st);
		ip.setDimensions(1,1,3);
		assertEquals(1,ip.getNChannels());
		ip.setDimensions(1,3,1);
		assertEquals(1,ip.getNChannels());
		ip.setDimensions(3,1,1);
		assertEquals(1,ip.getNChannels());

		// stack matches dimensions
		ip = new ImagePlus("Agent007", (Image)null);
		st = new ImageStack(2,3);
		st.addSlice("suave",new byte[] {1,2,3,4,5,6});
		st.addSlice("debonair",new byte[] {1,2,3,4,5,6});
		st.addSlice("sophisticated",new byte[] {1,2,3,4,5,6});
		st.addSlice("handsome",new byte[] {1,2,3,4,5,6});
		st.addSlice("humorous",new byte[] {1,2,3,4,5,6});
		st.addSlice("aloof",new byte[] {1,2,3,4,5,6});
		st.addSlice("calm",new byte[] {1,2,3,4,5,6});
		st.addSlice("composed",new byte[] {1,2,3,4,5,6});
		ip.setStack("MoneyPenny", st);

		// one dim > 1
		ip.setDimensions(1,1,8);
		assertEquals(1,ip.getNChannels());
		ip.setDimensions(1,8,1);
		assertEquals(1,ip.getNChannels());
		ip.setDimensions(8,1,1);
		assertEquals(8,ip.getNChannels());

		// two dims > 1
		ip.setDimensions(1,2,4);
		assertEquals(1,ip.getNChannels());
		ip.setDimensions(1,4,2);
		assertEquals(1,ip.getNChannels());
		ip.setDimensions(2,1,4);
		assertEquals(2,ip.getNChannels());
		ip.setDimensions(2,4,1);
		assertEquals(2,ip.getNChannels());
		ip.setDimensions(4,1,2);
		assertEquals(4,ip.getNChannels());
		ip.setDimensions(4,2,1);
		assertEquals(4,ip.getNChannels());

		// three dims > 1
		ip.setDimensions(2,2,2);
		assertEquals(2,ip.getNChannels());
	}

	@Test
	public void testGetNSlices() {
		// stack does not match dimensions
		ip = new ImagePlus("Groening", (Image)null);
		st = new ImageStack(2,3);
		st.addSlice("silly",new byte[] {1,2,3,4,5,6});
		ip.setStack("AyeCarumba",st);
		ip.setDimensions(1,1,3);
		assertEquals(1,ip.getNSlices());
		ip.setDimensions(1,3,1);
		assertEquals(1,ip.getNSlices());
		ip.setDimensions(3,1,1);
		assertEquals(1,ip.getNSlices());

		// stack matches dimensions
		ip = new ImagePlus("Agent007", (Image)null);
		st = new ImageStack(2,3);
		st.addSlice("suave",new byte[] {1,2,3,4,5,6});
		st.addSlice("debonair",new byte[] {1,2,3,4,5,6});
		st.addSlice("sophisticated",new byte[] {1,2,3,4,5,6});
		st.addSlice("handsome",new byte[] {1,2,3,4,5,6});
		st.addSlice("humorous",new byte[] {1,2,3,4,5,6});
		st.addSlice("aloof",new byte[] {1,2,3,4,5,6});
		st.addSlice("calm",new byte[] {1,2,3,4,5,6});
		st.addSlice("composed",new byte[] {1,2,3,4,5,6});
		ip.setStack("MoneyPenny", st);

		// one dim > 1
		ip.setDimensions(1,1,8);
		assertEquals(1,ip.getNSlices());
		ip.setDimensions(1,8,1);
		assertEquals(8,ip.getNSlices());
		ip.setDimensions(8,1,1);
		assertEquals(1,ip.getNSlices());

		// two dims > 1
		ip.setDimensions(1,2,4);
		assertEquals(2,ip.getNSlices());
		ip.setDimensions(1,4,2);
		assertEquals(4,ip.getNSlices());
		ip.setDimensions(2,1,4);
		assertEquals(1,ip.getNSlices());
		ip.setDimensions(2,4,1);
		assertEquals(4,ip.getNSlices());
		ip.setDimensions(4,1,2);
		assertEquals(1,ip.getNSlices());
		ip.setDimensions(4,2,1);
		assertEquals(2,ip.getNSlices());

		// three dims > 1
		ip.setDimensions(2,2,2);
		assertEquals(2,ip.getNSlices());
	}

	@Test
	public void testGetNFrames() {
		// stack does not match dimensions
		ip = new ImagePlus("Groening", (Image)null);
		st = new ImageStack(2,3);
		st.addSlice("silly",new byte[] {1,2,3,4,5,6});
		ip.setStack("AyeCarumba",st);
		ip.setDimensions(1,1,3);
		assertEquals(1,ip.getNFrames());
		ip.setDimensions(1,3,1);
		assertEquals(1,ip.getNFrames());
		ip.setDimensions(3,1,1);
		assertEquals(1,ip.getNFrames());

		// stack matches dimensions
		ip = new ImagePlus("Agent007", (Image)null);
		st = new ImageStack(2,3);
		st.addSlice("suave",new byte[] {1,2,3,4,5,6});
		st.addSlice("debonair",new byte[] {1,2,3,4,5,6});
		st.addSlice("sophisticated",new byte[] {1,2,3,4,5,6});
		st.addSlice("handsome",new byte[] {1,2,3,4,5,6});
		st.addSlice("humorous",new byte[] {1,2,3,4,5,6});
		st.addSlice("aloof",new byte[] {1,2,3,4,5,6});
		st.addSlice("calm",new byte[] {1,2,3,4,5,6});
		st.addSlice("composed",new byte[] {1,2,3,4,5,6});
		ip.setStack("MoneyPenny", st);

		// one dim > 1
		ip.setDimensions(1,1,8);
		assertEquals(8,ip.getNFrames());
		ip.setDimensions(1,8,1);
		assertEquals(1,ip.getNFrames());
		ip.setDimensions(8,1,1);
		assertEquals(1,ip.getNFrames());

		// two dims > 1
		ip.setDimensions(1,2,4);
		assertEquals(4,ip.getNFrames());
		ip.setDimensions(1,4,2);
		assertEquals(2,ip.getNFrames());
		ip.setDimensions(2,1,4);
		assertEquals(4,ip.getNFrames());
		ip.setDimensions(2,4,1);
		assertEquals(1,ip.getNFrames());
		ip.setDimensions(4,1,2);
		assertEquals(2,ip.getNFrames());
		ip.setDimensions(4,2,1);
		assertEquals(1,ip.getNFrames());

		// three dims > 1
		ip.setDimensions(2,2,2);
		assertEquals(2,ip.getNFrames());
	}

	@Test
	public void testGetDimensions() {
		ip = new ImagePlus("MobyDick",(Image)null);
		st = new ImageStack(2,3);
		st.addSlice("1",new byte[] {1,2,3,4,5,6});
		st.addSlice("2",new byte[] {1,2,3,4,5,6});
		st.addSlice("3",new byte[] {1,2,3,4,5,6});
		st.addSlice("4",new byte[] {1,2,3,4,5,6});
		st.addSlice("5",new byte[] {1,2,3,4,5,6});
		st.addSlice("6",new byte[] {1,2,3,4,5,6});
		st.addSlice("7",new byte[] {1,2,3,4,5,6});
		st.addSlice("8",new byte[] {1,2,3,4,5,6});
		ip.setStack("TharSheBlows", st);
		
		// dimensions do not match stack
		ip.setDimensions(1, 1, 1);
		assertArrayEquals(new int[] {2,3,1,8,1}, ip.getDimensions());
		ip.setDimensions(1, 1, 5);
		assertArrayEquals(new int[] {2,3,1,8,1}, ip.getDimensions());
		ip.setDimensions(1, 5, 1);
		assertArrayEquals(new int[] {2,3,1,8,1}, ip.getDimensions());
		ip.setDimensions(5, 1, 1);
		assertArrayEquals(new int[] {2,3,1,8,1}, ip.getDimensions());

		// dimensions match stack
		ip.setDimensions(1, 1, 8);
		assertArrayEquals(new int[] {2,3,1,1,8}, ip.getDimensions());
		ip.setDimensions(1, 8, 1);
		assertArrayEquals(new int[] {2,3,1,8,1}, ip.getDimensions());
		ip.setDimensions(8, 1, 1);
		assertArrayEquals(new int[] {2,3,8,1,1}, ip.getDimensions());
		ip.setDimensions(1, 2, 4);
		assertArrayEquals(new int[] {2,3,1,2,4}, ip.getDimensions());
		ip.setDimensions(1, 4, 2);
		assertArrayEquals(new int[] {2,3,1,4,2}, ip.getDimensions());
		ip.setDimensions(2, 1, 4);
		assertArrayEquals(new int[] {2,3,2,1,4}, ip.getDimensions());
		ip.setDimensions(2, 4, 1);
		assertArrayEquals(new int[] {2,3,2,4,1}, ip.getDimensions());
		ip.setDimensions(4, 1, 2);
		assertArrayEquals(new int[] {2,3,4,1,2}, ip.getDimensions());
		ip.setDimensions(4, 2, 1);
		assertArrayEquals(new int[] {2,3,4,2,1}, ip.getDimensions());
		ip.setDimensions(2, 2, 2);
		assertArrayEquals(new int[] {2,3,2,2,2}, ip.getDimensions());
	}

	@Test
	public void testGetType() {
		// TODO - figure out how to get a COLOR_256 image
		//proc = new ByteProcessor(1,3,new byte[] {1,2,3},new IndexColorModel(8,1,new byte[]{1},new byte[]{2},new byte[]{3}));
		//ip = new ImagePlus("SoupySales",proc);
		//assertEquals(ImagePlus.COLOR_256,ip.getType());

		proc = new ByteProcessor(1,3,new byte[] {1,2,3}, null);
		ip = new ImagePlus("SoupySales",proc);
		assertEquals(ImagePlus.GRAY8,ip.getType());

		proc = new ShortProcessor(1,3,new short[] {1,2,3},null);
		ip = new ImagePlus("SoupySales",proc);
		assertEquals(ImagePlus.GRAY16,ip.getType());

		proc = new ColorProcessor(1,3,new int[] {1,2,3});
		ip = new ImagePlus("SoupySales",proc);
		assertEquals(ImagePlus.COLOR_RGB,ip.getType());

		proc = new FloatProcessor(1,3,new int[] {1,2,3});
		ip = new ImagePlus("SoupySales",proc);
		assertEquals(ImagePlus.GRAY32,ip.getType());
	}

	@Test
	public void testGetBitDepth() {
		// TODO - figure out how to get a COLOR_256 image
		//proc = new ByteProcessor(1,3,new byte[] {1,2,3},new IndexColorModel(8,1,new byte[]{1},new byte[]{2},new byte[]{3}));
		//ip = new ImagePlus("SoupySales",proc);
		//assertEquals(8,ip.getBitDepth());

		proc = new ByteProcessor(1,3,new byte[] {1,2,3}, null);
		ip = new ImagePlus("SoupySales",proc);
		assertEquals(8,ip.getBitDepth());

		proc = new ShortProcessor(1,3,new short[] {1,2,3},null);
		ip = new ImagePlus("SoupySales",proc);
		assertEquals(16,ip.getBitDepth());

		proc = new ColorProcessor(1,3,new int[] {1,2,3});
		ip = new ImagePlus("SoupySales",proc);
		assertEquals(24,ip.getBitDepth());

		proc = new FloatProcessor(1,3,new int[] {1,2,3});
		ip = new ImagePlus("SoupySales",proc);
		assertEquals(32,ip.getBitDepth());
	}

	@Test
	public void testGetBytesPerPixel() {
		// TODO - figure out how to get a COLOR_256 image
		//proc = new ByteProcessor(1,3,new byte[] {1,2,3},new IndexColorModel(8,1,new byte[]{1},new byte[]{2},new byte[]{3}));
		//ip = new ImagePlus("SoupySales",proc);
		//assertEquals(1,ip.getBytesPerPixel());

		proc = new ByteProcessor(1,3,new byte[] {1,2,3},null);
		ip = new ImagePlus("SoupySales",proc);
		assertEquals(1,ip.getBytesPerPixel());

		proc = new ShortProcessor(1,3,new short[] {1,2,3},null);
		ip = new ImagePlus("SoupySales",proc);
		assertEquals(2,ip.getBytesPerPixel());

		proc = new ColorProcessor(1,3,new int[] {1,2,3});
		ip = new ImagePlus("SoupySales",proc);
		assertEquals(4,ip.getBytesPerPixel());

		proc = new FloatProcessor(1,3,new int[] {1,2,3});
		ip = new ImagePlus("SoupySales",proc);
		assertEquals(4,ip.getBytesPerPixel());
	}

	@Test
	public void testPropertyMethods() {
		ip = new ImagePlus();
		assertNull(ip.getProperties());
		assertNull(ip.getProperty("Anything"));
		ip.setProperty("Anything", new Float(4.0));
		assertNotNull(ip.getProperties());
		assertEquals(4.0f,ip.getProperty("Anything"));
		assertNull(ip.getProperty("SomethingElse"));
		ip.setProperty("Anything",null);
		assertNull(ip.getProperty("Anything"));
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

	/* OBSOLETE

	@Test
	public void testSetDisplayListVector() {
	}

	@Test
	public void testGetDisplayList() {
	}

	@Test
	public void testSetDisplayListShapeColorBasicStroke() {
	}

	@Test
	public void testSetDisplayListRoiColorIntColor() {
	}

	*/

	@Test
	public void testClone() {
		fail("Not yet implemented");
	}

	@Test
	public void testToString() {
		fail("Not yet implemented");
	}

}
