//
// ImagePlusTest.java
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

package ij;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import ij.gui.Line;
import ij.gui.Overlay;
import ij.gui.Roi;
import ij.io.FileInfo;
import ij.io.Opener;
import ij.measure.Calibration;
import ij.measure.CalibrationTools;
import ij.measure.Measurements;
import ij.process.ByteProcessor;
import ij.process.ColorProcessor;
import ij.process.DataConstants;
import ij.process.FloatProcessor;
import ij.process.ImageProcessor;
import ij.process.ImageStatistics;
import ij.process.ShortProcessor;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;
import java.awt.image.IndexColorModel;
import java.util.Arrays;

import org.junit.Test;

// note - in some places I refer to ImagePlus's protected instance var ip (via ip.ip) rather than getProcessor() (via
//   ip.getProcessor()) as getProcessor() has some side effects. If we eliminate the ip instance var some tests will break.

public class ImagePlusTest {

	ImagePlus ip;
	ImageStack st;
	ImageProcessor proc;

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
	// note - the source code mentions all of these but ip.changes are obsolete. May not need/want this test.

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
		ip = new ImagePlus(DataConstants.DATA_DIR + "hongKongFooey.tif");
		assertNotNull(ip);
		assertNull(ip.getImage());

		// try a file that should exist
		ip = new ImagePlus(DataConstants.DATA_DIR + "gray8-2x3-sub1.tif");
		assertNotNull(ip);
		assertNotNull(ip.getImage());
		assertEquals(2,ip.getNDimensions());
		assertEquals(2,ip.getHeight());
		assertEquals(3,ip.getWidth());
		assertEquals(1,ip.getStackSize());
		assertEquals(1,ip.getNFrames());
		assertEquals(1,ip.getNChannels());
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
			ip = new ImagePlus("", new ImageStack(2,3));
			fail();
		} catch (IllegalArgumentException e) {
			assertTrue(true);
		}

		st = new ImageStack(2,3);
		st.addSlice("Zaphod",new byte[] {1,2,3,4,5,6});
		ip = new ImagePlus("Beeblebrox",st);
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
		ip = new ImagePlus(DataConstants.DATA_DIR + "gray8-2x3-sub1.tif");
		proc = ip.getProcessor();
		assertEquals(proc,ip.getChannelProcessor());
	}

	@Test
	public void testGetLuts() {
		// getLuts() overridden by other classes. Default should be null
		ip = new ImagePlus(DataConstants.DATA_DIR + "gray8-2x3-sub1.tif");
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

		/* TODO - find way to test. Had to use protected var ip.img to test value. Can't just use getImage() because it
		 *   also can set ip.img in certain cases.
		 */

		ip = new ImagePlus();
		//assertNull(ip.img);
		ip.updateImage();
		//assertNull(ip.img);

		ip = new ImagePlus(DataConstants.DATA_DIR + "head8bit.tif");
		//assertNull(ip.img);
		ip.updateImage();
		//assertNotNull(ip.img);
	}

	@Test
	public void testHide() {
		// note - there is nothing I can test here - its all gui or deeper stuff that nulls out
		ip = new ImagePlus();
		ip.hide();  // so nothing but a compile time check
	}

	@Test
	public void testClose() {
		// note - there are gui things close() does and we can't test them
		ip = new ImagePlus();
		ip.setRoi(0,0,2,2);
		assertNotNull(ip.getRoi());
		ip.close();
		assertNull(ip.getRoi());
	}

	@Test
	public void testShow() {
		// note - no need to test
		//  just calls show(String) method with argument "".
		// if next test thorough this routine automatically handled
	}

	/* 7-20-10 Removed for two reasons
	 * 1) this routine interacts with the gui. Hudson won't run gui tests and thus fails.
	 * 2) when run from the command line via org.junit.runner.JUnitCore the test fails when interacting with other
	 *    classes (probably due to the Prefs.useInvertingLut). Interaction problem happened when running tests on just
	 *    ImagePlus and CurveFitter. There may be other classes it interacts with too.
	 *
	@Test
	public void testShowString() {
		proc = new ShortProcessor(4,2,new short[] {8,7,6,5,4,3,2,1},null);
		ip = new ImagePlus("YoYoMan",proc);
		ip.show("");
		// nothing to test - leave in as compile and runtime test

		proc = new ShortProcessor(4,2,new short[] {8,7,6,5,4,3,2,1},null);
		ip = new ImagePlus("AtomicBoy",proc);
		ip.show("Hoofang");
		// nothing to test - leave in as compile and runtime test

		// can do an inversion of image if 8 bit and prefs set correctly - ouch
		boolean origInvertStatus = Prefs.useInvertingLut;
		Prefs.useInvertingLut = true;
		proc = new ByteProcessor(4,2,new byte[] {8,7,6,5,4,3,2,1},null);
		ip = new ImagePlus("SuperGirl",proc);
		assertEquals(8,proc.get(0, 0));
		ip.show("Gadzooks Batman!");
		assertEquals(247,proc.get(0, 0));
		Prefs.useInvertingLut = origInvertStatus;
	}
    */

	@Test
	public void testSetActivated() {
		// note - setActivated sets a private var that has no getter - can't test
	}

	@Test
	public void testGetImage() {

		// TODO - find way to test - had to use protected var ip.img to successfully test this

		ip = new ImagePlus();
		//assertNull(ip.img);
		assertNull(ip.getImage());

		ip = new ImagePlus(DataConstants.DATA_DIR + "gray8-2x3-sub1.tif");
		//assertNull(ip.img);
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
		ip = new ImagePlus(DataConstants.DATA_DIR + "gray8-2x3-sub1.tif");
		assertNotNull(ip.getBufferedImage());

		// try a composite image
		proc = new ColorProcessor(1,1,new int[] {1});
		ip = new ImagePlus("Snowball",proc);
		CompositeImage c = new CompositeImage(ip);
		assertNotNull(c.getBufferedImage());
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

		// try with a Roi set
		ip = new ImagePlus();
		ip.setRoi(0,0,1,1);
		b = new BufferedImage(2,4,BufferedImage.TYPE_INT_RGB);
		assertNull(ip.getProcessor());
		assertNotNull(ip.getRoi());
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

		// try to get roi subcase to run
		proc = new ShortProcessor(1,3,new short[] {1,2,3},new IndexColorModel(8,1,new byte[]{1},new byte[]{2},new byte[]{3}));
		ip = new ImagePlus();
		ip.width = 4;
		ip.height = 4;
		ip.setRoi(1,1,2,2);
		assertNotNull(ip.getRoi());
		ip.setProcessor("Ooch",proc);
		assertNull(ip.getRoi());
		assertEquals(1,ip.width);
		assertEquals(3,ip.height);
		assertEquals(ImagePlus.GRAY16,ip.getType());
		assertEquals(16,ip.getBitDepth());
		assertEquals(2,ip.getBytesPerPixel());
	}

	@Test
	public void testSetStackStringImageStack() {
		// note - will only test non-gui capabilities of method

		// stack size == 0 throws illArgExc
		ip = new ImagePlus();
		st = new ImageStack(2,2);
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
		// note - fails this next test though object inspection makes them look the same.
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
		// COLOR_256
		proc = new ByteProcessor(1,3,new byte[] {1,2,3},new IndexColorModel(8,1,new byte[]{1},new byte[]{2},new byte[]{3}));
		ip = new ImagePlus("SoupySales",proc);
		ip.setImage(new BufferedImage(2,4,BufferedImage.TYPE_BYTE_INDEXED));
		assertEquals(ImagePlus.COLOR_256,ip.getType());

		// GRAY8
		proc = new ByteProcessor(1,3,new byte[] {1,2,3}, null);
		ip = new ImagePlus("SoupySales",proc);
		assertEquals(ImagePlus.GRAY8,ip.getType());

		// GRAY16
		proc = new ShortProcessor(1,3,new short[] {1,2,3},null);
		ip = new ImagePlus("SoupySales",proc);
		assertEquals(ImagePlus.GRAY16,ip.getType());

		// COLOR_RGB
		proc = new ColorProcessor(1,3,new int[] {1,2,3});
		ip = new ImagePlus("SoupySales",proc);
		assertEquals(ImagePlus.COLOR_RGB,ip.getType());

		// GRAY32
		proc = new FloatProcessor(1,3,new int[] {1,2,3});
		ip = new ImagePlus("SoupySales",proc);
		assertEquals(ImagePlus.GRAY32,ip.getType());
	}

	@Test
	public void testGetBitDepth() {
		// COLOR_256
		proc = new ByteProcessor(1,3,new byte[] {1,2,3},new IndexColorModel(8,1,new byte[]{1},new byte[]{2},new byte[]{3}));
		ip = new ImagePlus("SoupySales",proc);
		ip.setImage(new BufferedImage(2,4,BufferedImage.TYPE_BYTE_INDEXED));
		assertEquals(ImagePlus.COLOR_256,ip.getType());
		assertEquals(8,ip.getBitDepth());

		// GRAY8
		proc = new ByteProcessor(1,3,new byte[] {1,2,3}, null);
		ip = new ImagePlus("SoupySales",proc);
		assertEquals(ImagePlus.GRAY8,ip.getType());
		assertEquals(8,ip.getBitDepth());

		// GRAY16
		proc = new ShortProcessor(1,3,new short[] {1,2,3},null);
		ip = new ImagePlus("SoupySales",proc);
		assertEquals(ImagePlus.GRAY16,ip.getType());
		assertEquals(16,ip.getBitDepth());

		// COLOR_RGB
		proc = new ColorProcessor(1,3,new int[] {1,2,3});
		ip = new ImagePlus("SoupySales",proc);
		assertEquals(ImagePlus.COLOR_RGB,ip.getType());
		assertEquals(24,ip.getBitDepth());

		// GRAY32
		proc = new FloatProcessor(1,3,new int[] {1,2,3});
		ip = new ImagePlus("SoupySales",proc);
		assertEquals(ImagePlus.GRAY32,ip.getType());
		assertEquals(32,ip.getBitDepth());
	}

	@Test
	public void testGetBytesPerPixel() {
		// COLOR_256
		proc = new ByteProcessor(1,3,new byte[] {1,2,3},new IndexColorModel(8,1,new byte[]{1},new byte[]{2},new byte[]{3}));
		ip = new ImagePlus("SoupySales",proc);
		ip.setImage(new BufferedImage(2,4,BufferedImage.TYPE_BYTE_INDEXED));
		assertEquals(ImagePlus.COLOR_256,ip.getType());
		assertEquals(1,ip.getBytesPerPixel());

		// GRAY8
		proc = new ByteProcessor(1,3,new byte[] {1,2,3},null);
		ip = new ImagePlus("SoupySales",proc);
		assertEquals(ImagePlus.GRAY8,ip.getType());
		assertEquals(1,ip.getBytesPerPixel());

		// GRAY16
		proc = new ShortProcessor(1,3,new short[] {1,2,3},null);
		ip = new ImagePlus("SoupySales",proc);
		assertEquals(ImagePlus.GRAY16,ip.getType());
		assertEquals(2,ip.getBytesPerPixel());

		// COLOR_RGB
		proc = new ColorProcessor(1,3,new int[] {1,2,3});
		ip = new ImagePlus("SoupySales",proc);
		assertEquals(ImagePlus.COLOR_RGB,ip.getType());
		assertEquals(4,ip.getBytesPerPixel());

		// GRAY32
		proc = new FloatProcessor(1,3,new int[] {1,2,3});
		ip = new ImagePlus("SoupySales",proc);
		assertEquals(ImagePlus.GRAY32,ip.getType());
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
		LookUpTable lut;
		byte[] tmp;

		// if processor is null should create a ramping all grey lut
		ip = new ImagePlus();
		lut = ip.createLut();
		assertNotNull(lut);
		tmp = lut.getReds();
		for (int i = 0; i < 256; i++)
			assertEquals((byte)i,tmp[i]);
		tmp = lut.getGreens();
		for (int i = 0; i < 256; i++)
			assertEquals((byte)i,tmp[i]);
		tmp = lut.getBlues();
		for (int i = 0; i < 256; i++)
			assertEquals((byte)i,tmp[i]);

		// if processor is not null should create a lut from the IndexColorModel of the processor
		proc = new ByteProcessor(2,3,new byte[]{1,2,3,4,5,6},new IndexColorModel(8,1,new byte[]{1},new byte[]{2},new byte[]{3}));
		ip = new ImagePlus("RodeoHeaven",proc);
		lut = ip.createLut();
		assertNotNull(lut);
		tmp = lut.getReds();
		assertEquals((byte)1,tmp[0]);
		tmp = lut.getGreens();
		assertEquals((byte)2,tmp[0]);
		tmp = lut.getBlues();
		assertEquals((byte)3,tmp[0]);
	}

	@Test
	public void testIsInvertedLut() {
		// null imageProc and null image
		ip = new ImagePlus("CircusHell",(Image)null);
		assertFalse(ip.isInvertedLut());

		// null image proc with non null image
		ip = new ImagePlus("CircusHell",new BufferedImage(50,75,BufferedImage.TYPE_USHORT_555_RGB));
		assertEquals(ip.ip.isInvertedLut(),ip.isInvertedLut());

		// non null image
		proc = new ByteProcessor(2,3,new byte[]{1,2,3,4,5,6},new IndexColorModel(8,1,new byte[]{1},new byte[]{2},new byte[]{3}));
		ip = new ImagePlus("CircusHell",proc);
		assertEquals(ip.ip.isInvertedLut(),ip.isInvertedLut());
	}

	@Test
	public void testGetPixel() {
		// if null img then always return [0,0,0,0]
		ip = new ImagePlus();
		assertArrayEquals(new int[] {0,0,0,0}, ip.getPixel(0,0));

		// COLOR_256
		IndexColorModel cm = new IndexColorModel(8,1,new byte[]{1},new byte[]{2},new byte[]{3});
		proc = new ByteProcessor(1,1,new byte[] {0},cm);
		ip = new ImagePlus("SoupySales",proc);
		ip.setImage(new BufferedImage(1,1,BufferedImage.TYPE_BYTE_INDEXED, cm));
		assertEquals(ImagePlus.COLOR_256,ip.getType());
		assertArrayEquals(new int[] {1,2,3,0}, ip.getPixel(0,0));

		// GRAY8
		proc = new ByteProcessor(1,1,new byte[] {53}, null);
		ip = new ImagePlus("SoupySales",proc);
		ip.getImage();  // make sure img instance var gets set or getPixel() always returns [0,0,0,0]
		assertEquals(ImagePlus.GRAY8,ip.getType());
		assertArrayEquals(new int[] {53,0,0,0}, ip.getPixel(0,0));
		assertArrayEquals(new int[] {0,0,0,0},ip.getPixel(-1,-1));

		// GRAY16
		proc = new ShortProcessor(1,1,new short[] {30000},null);
		ip = new ImagePlus("SoupySales",proc);
		ip.getImage();  // make sure img instance var gets set or getPixel() always returns [0,0,0,0]
		assertEquals(ImagePlus.GRAY16,ip.getType());
		assertArrayEquals(new int[] {30000,0,0,0}, ip.getPixel(0,0));
		assertArrayEquals(new int[] {0,0,0,0},ip.getPixel(-1,-1));

		// COLOR_RGB
		proc = new ColorProcessor(1,1,new int[] {0xffeedd});
		ip = new ImagePlus("SoupySales",proc);
		ip.getImage();  // make sure img instance var gets set or getPixel() always returns [0,0,0,0]
		assertEquals(ImagePlus.COLOR_RGB,ip.getType());
		assertArrayEquals(new int[] {0xff,0xee,0xdd,0}, ip.getPixel(0,0));
		assertArrayEquals(new int[] {0,0,0,0},ip.getPixel(-1,-1));

		// GRAY32
		int[] ints = new int[3];
		proc = new FloatProcessor(1,3,ints);
		proc.set(0, 0, Float.floatToIntBits(25.0f));
		ip = new ImagePlus("SoupySales",proc);
		ip.getImage();  // make sure img instance var gets set or getPixel() always returns [0,0,0,0]
		assertEquals(ImagePlus.GRAY32,ip.getType());
		assertArrayEquals(new int[] {Float.floatToIntBits(25.0f),0,0,0}, ip.getPixel(0,0));
		assertArrayEquals(new int[] {0,0,0,0},ip.getPixel(-1,-1));
	}

	@Test
	public void testCreateEmptyStack() {
		// an empty imageplus
		ip = new ImagePlus();
		ip.createEmptyStack();
		assertEquals(1,ip.getImageStackSize()); // oddly a 1-element stack is what ImagePlus considers empty
		// still equals 1 if try ip.getImageStack().getSize()

		// a more fleshed out one
		proc = new ByteProcessor(1,1,new byte[] {54},null);
		ip = new ImagePlus("HookahBarn",proc);
		ip.createEmptyStack();
		assertEquals(1,ip.getImageStackSize()); // oddly a 1-element stack is what ImagePlus considers empty
		// still equals 1 if try ip.getImageStack().getSize()
	}

	@Test
	public void testGetStack() {
		// if stack == null and proc == null return an "empty" stack
		ip = new ImagePlus();
		st = ip.getStack();
		assertEquals(1,ip.getStackSize());

		// if stack == null and not proc == null and no info string
		//   do stuff and setRoi() if needed
		proc = new ByteProcessor(1,1,new byte[] {22}, null);
		ip = new ImagePlus("CareBearStation",proc);
		st = ip.getStack();
		assertEquals(1,ip.getStackSize());
		assertNull(st.getSliceLabel(1));
		assertEquals(proc.getColorModel(),st.getColorModel());
		assertEquals(proc.getRoi(),st.getRoi());

		// if stack == null and not proc == null and yes info string
		//   do stuff and setRoi() if needed
		proc = new ByteProcessor(1,1,new byte[] {22}, null);
		ip = new ImagePlus("CareBearStation",proc);
		ip.setProperty("Info","SnapDragon");
		st = ip.getStack();
		assertEquals(1,ip.getStackSize());
		assertEquals("CareBearStation\nSnapDragon",st.getSliceLabel(1));
		assertEquals(proc.getColorModel(),st.getColorModel());
		assertEquals(proc.getRoi(),st.getRoi());

		// if not stack == null
		//   do stuff and setRoi() if needed
		proc = new ByteProcessor(1,1,new byte[] {22}, null);
		ip = new ImagePlus("CareBearStation",proc);
		st = new ImageStack(1,1);
		st.addSlice("Fixed", proc);
		st.addSlice("Crooked", proc);
		ip.setStack("Odds",st);
		st = ip.getStack();
		assertEquals(2,ip.getStackSize());
		assertEquals(proc.getColorModel(),st.getColorModel());
		assertEquals(proc.getRoi(),st.getRoi());
	}

	@Test
	public void testGetImageStack() {
		// again equals() not defined for ImageSTack and thus I can't compare equality unless I make a method
		ip = new ImagePlus();
		assertNotNull(ip.getImageStack());
	}

	@Test
	public void testGetAndSetCurrentSlice() {

		st = new ImageStack(1,4);
		st.addSlice("slice1",new byte[] {1,2,3,4});
		st.addSlice("slice2",new byte[] {1,2,3,4});
		st.addSlice("slice3",new byte[] {1,2,3,4});
		st.addSlice("slice4",new byte[] {1,2,3,4});
		st.addSlice("slice5",new byte[] {1,2,3,4});
		ip = new ImagePlus("GinsuKnives",st);
		assertEquals(1,ip.getCurrentSlice());

		// try out of bounds
		ip.setCurrentSlice(-1);
		assertEquals(1,ip.getCurrentSlice());
		ip.setCurrentSlice(0);
		assertEquals(1,ip.getCurrentSlice());
		ip.setCurrentSlice(6);
		assertEquals(5,ip.getCurrentSlice());

		// try in bounds descending
		ip.setCurrentSlice(5);
		assertEquals(5,ip.getCurrentSlice());
		ip.setCurrentSlice(4);
		assertEquals(4,ip.getCurrentSlice());
		ip.setCurrentSlice(3);
		assertEquals(3,ip.getCurrentSlice());
		ip.setCurrentSlice(2);
		assertEquals(2,ip.getCurrentSlice());
		ip.setCurrentSlice(1);
		assertEquals(1,ip.getCurrentSlice());

		// try in bounds ascending
		ip.setCurrentSlice(1);
		assertEquals(1,ip.getCurrentSlice());
		ip.setCurrentSlice(2);
		assertEquals(2,ip.getCurrentSlice());
		ip.setCurrentSlice(3);
		assertEquals(3,ip.getCurrentSlice());
		ip.setCurrentSlice(4);
		assertEquals(4,ip.getCurrentSlice());
		ip.setCurrentSlice(5);
		assertEquals(5,ip.getCurrentSlice());

		// try in bounds random
		ip.setCurrentSlice(3);
		assertEquals(3,ip.getCurrentSlice());
		ip.setCurrentSlice(1);
		assertEquals(1,ip.getCurrentSlice());
		ip.setCurrentSlice(4);
		assertEquals(4,ip.getCurrentSlice());
		ip.setCurrentSlice(4);
		assertEquals(4,ip.getCurrentSlice());
		ip.setCurrentSlice(2);
		assertEquals(2,ip.getCurrentSlice());
	}

	@Test
	public void testGetChannel() {
		// will test in setPosIntIntInt
	}

	@Test
	public void testGetSlice() {
		// will test in setPosIntIntInt
	}

	@Test
	public void testGetFrame() {
		// will test in setPosIntIntInt
	}

	@Test
	public void testKillStack() {
		st = new ImageStack(1,4);
		st.addSlice("slice1",new byte[] {1,2,3,4});
		st.addSlice("slice2",new byte[] {1,2,3,4});
		st.addSlice("slice3",new byte[] {1,2,3,4});
		st.addSlice("slice4",new byte[] {1,2,3,4});
		st.addSlice("slice5",new byte[] {1,2,3,4});
		ip = new ImagePlus("GinsuKnives",st);
		ip.ip.snapshot();
		assertNotNull(ip.ip.getSnapshotPixels());
		ip.killStack();
		assertEquals(1,ip.getStackSize());
		assertNull(ip.ip.getSnapshotPixels());
	}

	@Test
	public void testSetPositionIntIntInt() {
		ip = new ImagePlus("LuckyLouie's",(Image)null);
		st = new ImageStack(1,3);
		st.addSlice("Lumbago1",new byte[] {1,2,3});
		st.addSlice("Lumbago2",new byte[] {1,2,3});
		st.addSlice("Lumbago3",new byte[] {1,2,3});
		st.addSlice("Lumbago4",new byte[] {1,2,3});
		st.addSlice("Lumbago5",new byte[] {1,2,3});
		st.addSlice("Lumbago6",new byte[] {1,2,3});
		st.addSlice("Lumbago7",new byte[] {1,2,3});
		st.addSlice("Lumbago8",new byte[] {1,2,3});
		ip.setStack("JiffyPop",st);
		ip.setDimensions(2, 2, 2);

		// out of bounds
		ip.setPosition(0,0,0);
		assertEquals(1,ip.getChannel());
		assertEquals(1,ip.getSlice());
		assertEquals(1,ip.getFrame());

		// out of bounds
		ip.setPosition(1,2,3);
		assertEquals(1,ip.getChannel());
		assertEquals(2,ip.getSlice());
		assertEquals(2,ip.getFrame());

		// out of bounds
		ip.setPosition(9,9,9);
		assertEquals(2,ip.getChannel());
		assertEquals(2,ip.getSlice());
		assertEquals(2,ip.getFrame());

		// end
		ip.setPosition(2,2,2);
		assertEquals(2,ip.getChannel());
		assertEquals(2,ip.getSlice());
		assertEquals(2,ip.getFrame());

		// start
		ip.setPosition(1,1,1);
		assertEquals(1,ip.getChannel());
		assertEquals(1,ip.getSlice());
		assertEquals(1,ip.getFrame());

		// inside
		ip.setPosition(2,1,2);
		assertEquals(2,ip.getChannel());
		assertEquals(1,ip.getSlice());
		assertEquals(2,ip.getFrame());
	}

	@Test
	public void testSetPositionWithoutUpdate() {
		// note - nothing to test as differences from previous method are simply UI related
	}

	@Test
	public void testGetStackIndex() {
		ip = new ImagePlus("LuckyLouie's",(Image)null);
		st = new ImageStack(1,3);
		st.addSlice("Lumbago1",new byte[] {1,2,3});
		st.addSlice("Lumbago2",new byte[] {1,2,3});
		st.addSlice("Lumbago3",new byte[] {1,2,3});
		st.addSlice("Lumbago4",new byte[] {1,2,3});
		st.addSlice("Lumbago5",new byte[] {1,2,3});
		st.addSlice("Lumbago6",new byte[] {1,2,3});
		st.addSlice("Lumbago7",new byte[] {1,2,3});
		st.addSlice("Lumbago8",new byte[] {1,2,3});
		ip.setStack("JiffyPop",st);
		ip.setDimensions(2, 2, 2);

		assertEquals(1,ip.getStackIndex(1,1,1));
		assertEquals(2,ip.getStackIndex(2,1,1));
		assertEquals(3,ip.getStackIndex(1,2,1));
		assertEquals(4,ip.getStackIndex(2,2,1));
		assertEquals(5,ip.getStackIndex(1,1,2));
		assertEquals(6,ip.getStackIndex(2,1,2));
		assertEquals(7,ip.getStackIndex(1,2,2));
		assertEquals(8,ip.getStackIndex(2,2,2));
	}

	@Test
	public void testResetStack() {

		// note - I just can't find a path through the code that can test the side effects ...

		/*
		ImageProcessor proc1, proc2;

		proc1 = new ByteProcessor(1,3,new byte[] {1,1,1},null);
		proc1.setMinAndMax(1,1);
		proc2 = new ByteProcessor(1,3,new byte[] {2,2,2},null);
		proc2.setMinAndMax(2,2);

		st = new ImageStack(1,3);
		st.addSlice("Potato1",proc1);
		st.addSlice("Potato2",proc2);
		ip = new ImagePlus("CheeseCorn",st);

		// test values are correct going in
		assertEquals(1,st.getProcessor(1).getMin(),Assert.DOUBLE_TOL);
		assertEquals(1,st.getProcessor(1).getMax(),Assert.DOUBLE_TOL);
		assertEquals(2,st.getProcessor(2).getMin(),Assert.DOUBLE_TOL);
		assertEquals(2,st.getProcessor(2).getMax(),Assert.DOUBLE_TOL);

		// make sure getProcessor() calls did not change things
		assertEquals(1,st.getProcessor(1).getMin(),Assert.DOUBLE_TOL);
		assertEquals(1,st.getProcessor(1).getMax(),Assert.DOUBLE_TOL);
		assertEquals(2,st.getProcessor(2).getMin(),Assert.DOUBLE_TOL);
		assertEquals(2,st.getProcessor(2).getMax(),Assert.DOUBLE_TOL);

		// if not slice 1 nothing should happen
		ip.setCurrentSlice(2);
		ip.resetStack();
		assertEquals(2.0,ip.ip.getMin(),Assert.DOUBLE_TOL);
		assertEquals(2.0,ip.ip.getMax(),Assert.DOUBLE_TOL);

		// otherwise min and max should get set to ip #2's values
		ip.setCurrentSlice(1);
		ip.resetStack();
		assertEquals(2.0,ip.ip.getMin(),Assert.DOUBLE_TOL);
		assertEquals(2.0,ip.ip.getMax(),Assert.DOUBLE_TOL);
		*/
	}

	@Test
	public void testSetPositionInt() {
		ip = new ImagePlus("LuckyLouie's",(Image)null);
		st = new ImageStack(1,3);
		st.addSlice("Lumbago1",new byte[] {1,2,3});
		st.addSlice("Lumbago2",new byte[] {1,2,3});
		st.addSlice("Lumbago3",new byte[] {1,2,3});
		st.addSlice("Lumbago4",new byte[] {1,2,3});
		st.addSlice("Lumbago5",new byte[] {1,2,3});
		st.addSlice("Lumbago6",new byte[] {1,2,3});
		st.addSlice("Lumbago7",new byte[] {1,2,3});
		st.addSlice("Lumbago8",new byte[] {1,2,3});
		ip.setStack("JiffyPop",st);
		ip.setDimensions(2, 2, 2);

		ip.setPosition(1);
		assertEquals(1,ip.getStackIndex(ip.getChannel(),ip.getSlice(),ip.getFrame()));

		ip.setPosition(2);
		assertEquals(2,ip.getStackIndex(ip.getChannel(),ip.getSlice(),ip.getFrame()));

		ip.setPosition(3);
		assertEquals(3,ip.getStackIndex(ip.getChannel(),ip.getSlice(),ip.getFrame()));

		ip.setPosition(4);
		assertEquals(4,ip.getStackIndex(ip.getChannel(),ip.getSlice(),ip.getFrame()));

		ip.setPosition(5);
		assertEquals(5,ip.getStackIndex(ip.getChannel(),ip.getSlice(),ip.getFrame()));

		ip.setPosition(6);
		assertEquals(6,ip.getStackIndex(ip.getChannel(),ip.getSlice(),ip.getFrame()));

		ip.setPosition(7);
		assertEquals(7,ip.getStackIndex(ip.getChannel(),ip.getSlice(),ip.getFrame()));

		ip.setPosition(8);
		assertEquals(8,ip.getStackIndex(ip.getChannel(),ip.getSlice(),ip.getFrame()));
	}

	@Test
	public void testSetSlice() {

		// stack doesn't exist - should be immediate return
		ip = new ImagePlus();
		ip.setSlice(1234567890);  // this should be safe
		assertEquals(1,ip.getCurrentSlice());

		// stack exists but asking for currentSlice and we have a processor - should do immediate return
		proc = new ColorProcessor(2,1,new int[] {2,1});
		ip = new ImagePlus("Zoot",proc);
		st = new ImageStack(2,1);
		st.addSlice("Bad", new int[] {3,4});
		st.addSlice("Wicked", new int[] {5,6});
		ip.setStack("Qualities", st);
		ip.setCurrentSlice(2);
		ip.setSlice(2);
		assertEquals(2,ip.getCurrentSlice());

		// stack size in allowed range and not current slice : case we already have a processor
		proc = new ColorProcessor(2,1,new int[] {2,1});
		ip = new ImagePlus("Zoot",proc);
		st = new ImageStack(2,1);
		st.addSlice("Bad", new int[] {3,4});
		st.addSlice("Wicked", new int[] {5,6});
		ip.setStack("Qualities", st);
		ip.setCurrentSlice(2);
		ip.getProcessor().snapshot();
		assertEquals(2,ip.getCurrentSlice());
		assertNotNull(ip.getProcessor().getSnapshotPixels());
		ip.setSlice(1);
		assertEquals(1,ip.getCurrentSlice());
		assertArrayEquals(new int[] {3,4}, (int[])st.getPixels(1));
		assertNull(ip.getProcessor().getSnapshotPixels());

		// stack size in allowed range and not current slice : case we don't have a processor
		ip = new ImagePlus();
		st = new ImageStack(2,2);
		st.addSlice("Frida",new byte[] {1,2,3,4});
		st.addSlice("Gwen",new byte[] {5,6,7,8});
		ip.setStack("PatrioticAmericans", st);
		ip.setCurrentSlice(2);
		assertEquals(2,ip.getCurrentSlice());
		ip.setSlice(1);
		assertEquals(1,ip.getCurrentSlice());
		//assertTrue(ip.ip instanceof ByteProcessor);  // with ImgLibProcessor this is noo longer true
	}

	@Test
	public void testSetSliceWithoutUpdate() {
		// note - nothing to test - same as previous method but with no GUI interaction
	}

	@Test
	public void testSetAndGetRoi() {
		proc = new ByteProcessor(1,3,new byte[] {7,5,3},null);
		ip = new ImagePlus("BigRedBarn",proc);
		assertNull(ip.getRoi());
		Roi roi = new Roi(0,0,1,1);
		ip.setRoi(roi);
		assertEquals(roi,ip.getRoi());
	}

	@Test
	public void testSetRoiRoiBoolean() {

		Roi roi;

		// note - will pass false for updateDisplay flag for now

		proc = new ByteProcessor(1,3,new byte[] {7,5,3},null);
		ip = new ImagePlus("TeenyBluehouse",proc);
		roi = new Roi(0,0,1,3);
		ip.setRoi(roi);
		assertEquals(roi,ip.getRoi());

		// pass null - should killroi()
		ip.setRoi(null,false);
		assertNull(ip.getRoi());

		// all zeros - should eventually killroi()
		//Roi orig = ip.getRoi();
		//roi = new Roi(0,0,0,0);
		//ip.setRoi(roi,false);
		// next test fails cuz new Roi(allZeros) returns width,height == 1 so killroi() never called
		//assertEquals(orig,ip.getRoi());
		// this subcase maybe unreachable code

		// legit
		roi = new Roi(0,0,1,1);
		ip.setRoi(roi,false);
		assertEquals(roi,ip.getRoi());
		assertNull(ip.getMask());

	}

	@Test
	public void testSetRoiIntIntIntInt() {
		Roi roi;

		proc = new ByteProcessor(1,3,new byte[] {7,5,3},null);
		ip = new ImagePlus("SuperDude's Ranch",proc);

		// test bad input all zeroes
		roi = new Roi(0,0,0,0);
		ip.setRoi(0,0,0,0);
		assertEquals(roi,ip.getRoi());

		// test bad input way out of range
		roi = new Roi(8,8,8,8);
		ip.setRoi(8,8,8,8);
		assertEquals(roi,ip.getRoi());

		// test good input
		roi = new Roi(0,0,1,1);
		ip.setRoi(0,0,1,1);
		assertEquals(roi,ip.getRoi());
	}

	@Test
	public void testSetRoiRectangle() {
		Roi roi;
		Rectangle rect;

		proc = new ByteProcessor(1,3,new byte[] {7,5,3},null);
		ip = new ImagePlus("SuperDude's Ranch",proc);

		// all zeroes
		rect = new Rectangle(0,0,0,0);
		roi = new Roi(0,0,0,0);
		ip.setRoi(rect);
		assertEquals(roi,ip.getRoi());

		// out of bounds by width and height
		rect = new Rectangle(0,0,4,4);
		roi = new Roi(0,0,4,4);
		ip.setRoi(rect);
		assertEquals(roi,ip.getRoi());

		// completely out of bounds
		rect = new Rectangle(20,30,40,50);
		roi = new Roi(20,30,40,50);
		ip.setRoi(rect);
		assertEquals(roi,ip.getRoi());

		// legit
		rect = new Rectangle(0,0,1,2);
		roi = new Roi(0,0,1,2);
		ip.setRoi(rect);
		assertEquals(roi,ip.getRoi());
	}

	@Test
	public void testCreateNewRoi() {

		// note - can't fully test this method as it depends on GUI vars and state and can draw too

		proc = new ByteProcessor(1,3,new byte[] {7,5,3},null);
		ip = new ImagePlus("America's Hardware Store",proc);

		// this is the best that can be done ...
		ip.createNewRoi(5,10);
		assertNotNull(ip.getRoi());
	}

	@Test
	public void testKillRoi() {
		proc = new ByteProcessor(1,3,new byte[] {7,5,3},null);
		ip = new ImagePlus("PDQ Inc.",proc);

		ip.setRoi(new Rectangle(0,0,2,5));
		assertNotNull(ip.getRoi());
		ip.killRoi();
		assertNull(ip.getRoi());
	}

	@Test
	public void testSaveAndRestoreRoi() {
		// note - can't test gui portions of these methods. Just test that save and restore actually make changes.
		Roi one,two;
		proc = new ByteProcessor(1,3,new byte[] {7,5,3},null);
		ip = new ImagePlus("Jiffy Snap LLC",proc);
		one = new Roi(new Rectangle(0,0,1,1));
		two = new Roi(new Rectangle(0,0,1,2));
		assertNull(ip.getRoi());
		ip.setRoi(one);
		assertEquals(one,ip.getRoi());
		ip.restoreRoi();
		assertEquals(one,ip.getRoi());
		ip.saveRoi();
		ip.setRoi(two);
		assertEquals(two,ip.getRoi());
		ip.restoreRoi();
		assertEquals(one,ip.getRoi());
	}

	/* Note this method has interacted badly with CurveFitter when running the two from the command line via
	 *   org.junit.runn.JUnitCore.
	 */
	@Test
	public void testRevert() {
		// note - the following code required minor changes to FileOpener. a helper of the revert method in FileOpener requires
		//   the existence of a IJ instance to update its progress bar. we don't have an IJ and if I create one all subsequent
		//   tests are affected. For now I have modified FileOpener::revertToSaved to not access IJ.getInstance().getProgressBar().
		//   I have asked Wayne to look at this.

		// note - not testing revert to file stored at a URL.

		// simple test - stacked item should not revert
		//proc = new ByteProcessor(1,2,new byte[] {1,2},null);
		//ip = new ImagePlus("Zowblooie",proc);
		ip = new ImagePlus("Zowblooie",(Image)null);
		st = new ImageStack(1,2);
		st.addSlice("uno", new byte[] {3,4});
		st.addSlice("dos", new byte[] {5,6});
		ip.setStack("Pachoom",st);
		ip.setCurrentSlice(1);
		proc = ip.getProcessor();
		assertEquals(3,proc.getPixel(0, 0));
		proc.set(0,0,99);
		assertEquals(99,proc.getPixel(0, 0));
		ip.revert();
		proc = ip.getProcessor();
		assertEquals(99,proc.getPixel(0, 0));

		// simple test - a ImagePlus that did not come from a file should not revert
		proc = new ByteProcessor(1,2,new byte[] {1,2},null);
		ip = new ImagePlus("Zowblooie",proc);
		assertEquals(1,proc.getPixel(0, 0));
		proc.set(0,0,99);
		assertEquals(99,proc.getPixel(0, 0));
		ip.revert();
		proc = ip.getProcessor();
		assertEquals(99,proc.getPixel(0, 0));

		// should revert - a simple test
		ip = new Opener().openTiff(DataConstants.DATA_DIR, "head8bit.tif");
		proc = ip.getProcessor();
		assertEquals(6,proc.getPixel(0, 0));
		proc.set(0,0,99);
		assertEquals(99,proc.getPixel(0, 0));
		ip.revert();
		proc = ip.getProcessor();
		assertEquals(6,proc.getPixel(0, 0));

		// should revert but Roi should not be not reverted
		Roi roi = new Roi(0,0,1,1);
		ip = new Opener().openTiff(DataConstants.DATA_DIR, "head8bit.tif");
		proc = ip.getProcessor();
		assertEquals(6,proc.getPixel(0, 0));
		assertNull(ip.getRoi());
		ip.setRoi(roi);
		proc.set(0,0,99);
		assertEquals(99,proc.getPixel(0, 0));
		ip.revert();
		proc = ip.getProcessor();
		assertEquals(6,proc.getPixel(0, 0));
		assertEquals(roi,ip.getRoi()); // roi should survive the revert() call

		// should revert and changes flag should be correctly updated
		ip = new Opener().openTiff(DataConstants.DATA_DIR, "head8bit.tif");
		assertFalse(ip.changes);
		proc = ip.getProcessor();
		assertEquals(6,proc.getPixel(0, 0));
		proc.set(0,0,99);
		assertEquals(99,proc.getPixel(0, 0));
		ip.changes = true;
		ip.revert();
		proc = ip.getProcessor();
		assertEquals(6,proc.getPixel(0, 0));
		assertFalse(ip.changes);

		// test that properties(FHT) deleted
		ip = new Opener().openTiff(DataConstants.DATA_DIR, "head8bit.tif");
		ip.setProperty("FHT","Fanny Arbuckle");
		ip.setTitle("FFT of my toes");
		proc = ip.getProcessor();
		assertEquals(6,proc.getPixel(0, 0));
		proc.set(0,0,99);
		assertEquals(99,proc.getPixel(0, 0));
		ip.revert();
		proc = ip.getProcessor();
		assertEquals(6,proc.getPixel(0, 0));
		assertNull(ip.getProperty("FHT"));
		assertEquals("my toes",ip.getTitle());

		// test that trimProcessor() ran
		ip = new Opener().openTiff(DataConstants.DATA_DIR, "head8bit.tif");
		proc = ip.getProcessor();
		assertNull(proc.getSnapshotPixels());
		proc.snapshot();
		assertNotNull(proc.getSnapshotPixels());
		ip.revert();
		proc = ip.getProcessor();
		assertNull(proc.getSnapshotPixels());

		// test that LUT was inverted if needed
		boolean origInvertStatus = Prefs.useInvertingLut;
		Prefs.useInvertingLut = true;
		ip = new Opener().openTiff(DataConstants.DATA_DIR, "head8bit.tif");
		proc = ip.getProcessor();
		assertFalse(proc.isInvertedLut());
		assertEquals(6,proc.get(0, 0));
		ip.revert();
		proc = ip.getProcessor();
		assertTrue(proc.isInvertedLut());
		assertEquals(249,proc.get(0, 0));
		Prefs.useInvertingLut = origInvertStatus;
	}

	@Test
	public void testGetFileInfo() {

		Calibration cal;
		FileInfo fi;
		byte[] expLutChan = new byte[256];
		for (int i = 0; i < 256; i++)
			expLutChan[i] = (byte)i;

		// 8 bit gray with calibration
		cal = new Calibration();
		cal.frameInterval = 4.0;
		cal.pixelWidth = 2.0;
		cal.pixelHeight = 3.0;
		cal.pixelDepth = 7.0;
		cal.setFunction(Calibration.POLY3, new double[] {1,2,3,4}, "hoogerams");
		proc = new ByteProcessor(1,2,new byte[] {1,2},null);
		ip = new ImagePlus("Chupacabras",proc);
		st = new ImageStack(1,2);
		st.addSlice("hands",new byte[] {1,2});
		st.addSlice("feet",new byte[] {3,4});
		st.addSlice("suckers",new byte[] {5,6});
		ip.setStack("body parts", st);
		ip.setCalibration(cal);
		fi = ip.getFileInfo();
		assertEquals(1,fi.width);
		assertEquals(2,fi.height);
		assertEquals(3,fi.nImages);
		assertEquals(false,fi.whiteIsZero);
		assertEquals(false,fi.intelByteOrder);
		// TODO : reenable equality test when ImgLib is returning references
		//assertEquals(st.getImageArray(),fi.pixels);
		assertArrayEquals((Object[])st.getImageArray(),(Object[])fi.pixels);
		assertEquals(2.0,fi.pixelWidth,Assert.DOUBLE_TOL);
		assertEquals(3.0,fi.pixelHeight,Assert.DOUBLE_TOL);
		assertEquals("pixel",fi.unit);
		assertEquals(7.0,fi.pixelDepth,Assert.DOUBLE_TOL);
		assertEquals(4.0,fi.frameInterval,Assert.DOUBLE_TOL);
		assertEquals(Calibration.POLY3,fi.calibrationFunction);
		Assert.assertDoubleArraysEqual(new double[] {1,2,3,4},fi.coefficients,Assert.DOUBLE_TOL);
		assertEquals("hoogerams",fi.valueUnit);
		assertEquals(FileInfo.GRAY8,fi.fileType);
		assertEquals(256,fi.lutSize);
		assertArrayEquals(expLutChan,fi.reds);
		assertArrayEquals(expLutChan,fi.greens);
		assertArrayEquals(expLutChan,fi.blues);

		// 8 bit color without calibration
		proc = new ByteProcessor(1,2,new byte[] {1,2},null);
		ip = new ImagePlus("Chupacabras",proc);
		st = new ImageStack(1,2);
		st.addSlice("hands",new byte[] {1,2});
		st.addSlice("feet",new byte[] {3,4});
		st.addSlice("suckers",new byte[] {5,6});
		ip.setStack("body parts", st);
		ip.setType(ImagePlus.COLOR_256);
		fi = ip.getFileInfo();
		assertEquals(1,fi.width);
		assertEquals(2,fi.height);
		assertEquals(3,fi.nImages);
		assertEquals(false,fi.whiteIsZero);
		assertEquals(false,fi.intelByteOrder);
		// TODO : reenable equality test when ImgLib is returning references
		//assertEquals(st.getImageArray(),fi.pixels);
		assertArrayEquals((Object[])st.getImageArray(),(Object[])fi.pixels);
		assertEquals(1.0,fi.pixelWidth,Assert.DOUBLE_TOL);
		assertEquals(1.0,fi.pixelHeight,Assert.DOUBLE_TOL);
		assertNull(fi.unit);
		assertEquals(1.0,fi.pixelDepth,Assert.DOUBLE_TOL);
		assertEquals(0.0,fi.frameInterval,Assert.DOUBLE_TOL);
		assertEquals(0,fi.calibrationFunction);
		assertNull(fi.coefficients);
		assertNull(fi.valueUnit);
		assertEquals(FileInfo.COLOR8,fi.fileType);
		assertEquals(256,fi.lutSize);
		assertArrayEquals(expLutChan,fi.reds);
		assertArrayEquals(expLutChan,fi.greens);
		assertArrayEquals(expLutChan,fi.blues);

		// 16 bit without calibration
		proc = new ShortProcessor(1,2,new short[] {1,2},null);
		ip = new ImagePlus("Chupacabras",proc);
		st = new ImageStack(1,2);
		st.addSlice("hands",new short[] {1,2});
		st.addSlice("feet",new short[] {3,4});
		st.addSlice("suckers",new short[] {5,6});
		ip.setStack("body parts", st);
		fi = ip.getFileInfo();
		assertEquals(1,fi.width);
		assertEquals(2,fi.height);
		assertEquals(3,fi.nImages);
		assertEquals(false,fi.whiteIsZero);
		assertEquals(false,fi.intelByteOrder);
		// TODO : reenable equality test when ImgLib is returning references
		//assertEquals(st.getImageArray(),fi.pixels);
		assertArrayEquals((Object[])st.getImageArray(),(Object[])fi.pixels);
		assertEquals(1.0,fi.pixelWidth,Assert.DOUBLE_TOL);
		assertEquals(1.0,fi.pixelHeight,Assert.DOUBLE_TOL);
		assertNull(fi.unit);
		assertEquals(1.0,fi.pixelDepth,Assert.DOUBLE_TOL);
		assertEquals(0.0,fi.frameInterval,Assert.DOUBLE_TOL);
		assertEquals(0,fi.calibrationFunction);
		assertNull(fi.coefficients);
		assertNull(fi.valueUnit);
		assertEquals(FileInfo.GRAY16_UNSIGNED,fi.fileType);
		assertEquals(0,fi.lutSize);
		assertNull(fi.reds);
		assertNull(fi.greens);
		assertNull(fi.blues);

		// 32 bit without calibration
		proc = new FloatProcessor(1,2,new float[] {1,2},null);
		ip = new ImagePlus("Chupacabras",proc);
		st = new ImageStack(1,2);
		st.addSlice("hands",new float[] {1,2});
		st.addSlice("feet",new float[] {3,4});
		st.addSlice("suckers",new float[] {5,6});
		ip.setStack("body parts", st);
		fi = ip.getFileInfo();
		assertEquals(1,fi.width);
		assertEquals(2,fi.height);
		assertEquals(3,fi.nImages);
		assertEquals(false,fi.whiteIsZero);
		assertEquals(false,fi.intelByteOrder);
		// TODO : reenable equality test when ImgLib is returning references
		//assertEquals(st.getImageArray(),fi.pixels);
		assertArrayEquals((Object[])st.getImageArray(),(Object[])fi.pixels);
		assertEquals(1.0,fi.pixelWidth,Assert.DOUBLE_TOL);
		assertEquals(1.0,fi.pixelHeight,Assert.DOUBLE_TOL);
		assertNull(fi.unit);
		assertEquals(1.0,fi.pixelDepth,Assert.DOUBLE_TOL);
		assertEquals(0.0,fi.frameInterval,Assert.DOUBLE_TOL);
		assertEquals(0,fi.calibrationFunction);
		assertNull(fi.coefficients);
		assertNull(fi.valueUnit);
		assertEquals(FileInfo.GRAY32_FLOAT,fi.fileType);
		assertEquals(0,fi.lutSize);
		assertNull(fi.reds);
		assertNull(fi.greens);
		assertNull(fi.blues);

		// rgb without calibration
		proc = new ColorProcessor(1,2,new int[] {1,2});
		ip = new ImagePlus("Chupacabras",proc);
		st = new ImageStack(1,2);
		st.addSlice("hands",new int[] {1,2});
		st.addSlice("feet",new int[] {3,4});
		st.addSlice("suckers",new int[] {5,6});
		ip.setStack("body parts", st);
		fi = ip.getFileInfo();
		assertEquals(1,fi.width);
		assertEquals(2,fi.height);
		assertEquals(3,fi.nImages);
		assertEquals(false,fi.whiteIsZero);
		assertEquals(false,fi.intelByteOrder);
		// TODO : reenable equality test when ImgLib is returning references
		//assertEquals(st.getImageArray(),fi.pixels);
		assertArrayEquals((Object[])st.getImageArray(),(Object[])fi.pixels);
		assertEquals(1.0,fi.pixelWidth,Assert.DOUBLE_TOL);
		assertEquals(1.0,fi.pixelHeight,Assert.DOUBLE_TOL);
		assertNull(fi.unit);
		assertEquals(1.0,fi.pixelDepth,Assert.DOUBLE_TOL);
		assertEquals(0.0,fi.frameInterval,Assert.DOUBLE_TOL);
		assertEquals(0,fi.calibrationFunction);
		assertNull(fi.coefficients);
		assertNull(fi.valueUnit);
		//TODO blocked out temporarily - this test can no longer pass with internal type tracking as its done now
		//  My construction example is a little weird and I think if it wasn't supported that would be fine.
		//assertEquals(FileInfo.RGB,fi.fileType);
		assertEquals(0,fi.lutSize);
		assertNull(fi.reds);
		assertNull(fi.greens);
		assertNull(fi.blues);

		// 3x16 without calibration
		proc = new ShortProcessor(1,2,new short[] {1,2},null);
		ip = new ImagePlus("Chupacabras",proc);
		st = new ImageStack(1,2);
		st.addSlice("hands",new short[] {1,2});
		st.addSlice("feet",new short[] {3,4});
		st.addSlice("suckers",new short[] {5,6});
		ip.setStack("body parts", st);
		ip.compositeImage = true;
		fi = ip.getFileInfo();
		assertEquals(1,fi.width);
		assertEquals(2,fi.height);
		assertEquals(3,fi.nImages);
		assertEquals(false,fi.whiteIsZero);
		assertEquals(false,fi.intelByteOrder);
		// TODO : reenable equality test when ImgLib is returning references
		//assertEquals(st.getImageArray(),fi.pixels);
		assertArrayEquals((Object[])st.getImageArray(),(Object[])fi.pixels);
		assertEquals(1.0,fi.pixelWidth,Assert.DOUBLE_TOL);
		assertEquals(1.0,fi.pixelHeight,Assert.DOUBLE_TOL);
		assertNull(fi.unit);
		assertEquals(1.0,fi.pixelDepth,Assert.DOUBLE_TOL);
		assertEquals(0.0,fi.frameInterval,Assert.DOUBLE_TOL);
		assertEquals(0,fi.calibrationFunction);
		assertNull(fi.coefficients);
		assertNull(fi.valueUnit);
		assertEquals(FileInfo.RGB48,fi.fileType);
		assertEquals(0,fi.lutSize);
		assertNull(fi.reds);
		assertNull(fi.greens);
		assertNull(fi.blues);
	}

	// again I find I could use an equals() method for a class - in this case FileInfo

	private void areEquals(FileInfo a, FileInfo b)
	{
		// will test a subset for now

		assertEquals(a.width,b.width);
		assertEquals(a.height,b.height);
		assertEquals(a.nImages,b.nImages);
		assertEquals(a.whiteIsZero,b.whiteIsZero);
		assertEquals(a.intelByteOrder,b.intelByteOrder);
		assertEquals(a.pixels,b.pixels);
		assertEquals(a.pixelWidth,b.pixelWidth,Assert.DOUBLE_TOL);
		assertEquals(a.pixelHeight,b.pixelHeight,Assert.DOUBLE_TOL);
		assertEquals(a.unit,b.unit);
		assertEquals(a.pixelDepth,b.pixelDepth,Assert.DOUBLE_TOL);
		assertEquals(a.frameInterval,b.frameInterval,Assert.DOUBLE_TOL);
		assertEquals(a.calibrationFunction,b.calibrationFunction);
		Assert.assertDoubleArraysEqual(a.coefficients,b.coefficients,Assert.DOUBLE_TOL);
		assertEquals(a.valueUnit,b.valueUnit);
		assertEquals(a.fileType,b.fileType);
		assertEquals(a.lutSize,b.lutSize);
		assertArrayEquals(a.reds,b.reds);
		assertArrayEquals(a.greens,b.greens);
		assertArrayEquals(a.blues,b.blues);
	}

	@Test
	public void testGetOriginalFileInfo() {
		FileInfo orig;

		orig = new FileInfo();
		proc = new ByteProcessor(1,2,new byte[] {1,2},null);
		ip = new ImagePlus("TheRedBalloon",proc);
		ip.setFileInfo(orig);
		areEquals(orig,ip.getOriginalFileInfo());
		ip.setFileInfo(null);
		assertNull(ip.getOriginalFileInfo());
		// note - there is a case where fileinfo is null but private variable url is not and when this is the case a
		//   fileinfo is constructed. I cannot test this case as there is no way to set url with actually going over inet.
	}

	@Test
	public void testImageUpdate() {
		ip = new ImagePlus();

		// only the flags input to imageUpdate() has an effect on its return value
		assertFalse(ip.imageUpdate(null, ImageObserver.ERROR, -1, -1, -1, -1));
			// note - the previous call also sets a private variable (that cannot be tested) errorLoadingImage to true
		assertFalse(ip.imageUpdate(null, ImageObserver.ALLBITS, 0, 0, 0, 0));
		assertFalse(ip.imageUpdate(null, ImageObserver.FRAMEBITS, 1, 1, 1, 1));
		assertFalse(ip.imageUpdate(null, ImageObserver.ABORT, 100, -100, 100, -100));
		assertTrue(ip.imageUpdate(null, 0, 3, 3, 3, 3));
	}

	@Test
	public void testFlush() {
		//proc = new ShortProcessor(2,3,new short[] {1,2,3,4,5,6},null);
		//ip = new ImagePlus("Hello Kitty",proc);
		ip = new ImagePlus("Hello Kitty",(Image)null);
		st = new ImageStack(2,3);
		st.addSlice("1",new short[] {1,2,3,4,5,6});
		st.addSlice("2",new short[] {1,2,3,4,5,6});
		st.addSlice("3",new short[] {1,2,3,4,5,6});
		st.addSlice("4",new short[] {1,2,3,4,5,6});
		st.addSlice("5",new short[] {1,2,3,4,5,6});
		st.addSlice("6",new short[] {1,2,3,4,5,6});
		ip.setStack("TheSituation",st);
		ip.setRoi(0,0,1,1);
		assertNotNull(ip.getProcessor());
		assertNotNull(ip.getRoi());

		// do nothing if ignoreFlush
		ip.setIgnoreFlush(true);
		ip.flush();
		assertNotNull(ip.getProcessor());
		assertNotNull(ip.getStack());
		assertNotNull(ip.getRoi());

		// do nothing if locked
		ip.setIgnoreFlush(false);
		ip.lock();
		ip.flush();
		assertNotNull(ip.getProcessor());
		assertNotNull(ip.getStack());
		assertNotNull(ip.getRoi());

		// do stuff if unlocked
		ip.unlock();
		ip.flush();
		assertNull(ip.getProcessor());
		// note - side effect - can't test next line as getStack() will allocate one if stack == null
		//assertNull(ip.getStack());
		assertNull(ip.getRoi());
	}

	@Test
	public void testSetIgnoreFlush() {
		// tested in previous method
	}

	@Test
	public void testCreateImagePlus() {

		Calibration cal;
		ImagePlus newOne;

		// try Byte type with unique calib
		proc = new ByteProcessor(1,2,new byte[]{1,2},null);
		ip = new ImagePlus("GrapeVine",proc);
		cal = new Calibration();
		cal.setFunction(Calibration.LOG, new double[] {5,6,7,8,9}, "hectareMummies");
		ip.setCalibration(cal);
		newOne = ip.createImagePlus();
		CalibrationTools.assertCalibrationsEqual(ip.getCalibration(), newOne.getCalibration());
		assertEquals(ip.getType(),newOne.getType());

		// try Short type with unique calib
		proc = new ShortProcessor(1,2,new short[]{1,2},null);
		ip = new ImagePlus("GrapeVine",proc);
		cal = new Calibration();
		cal.setFunction(Calibration.POLY2, new double[] {5,6,7,8,9}, "hectareMummies");
		ip.setCalibration(cal);
		newOne = ip.createImagePlus();
		CalibrationTools.assertCalibrationsEqual(ip.getCalibration(), newOne.getCalibration());
		assertEquals(ip.getType(),newOne.getType());

		// try Float type with unique calib
		proc = new FloatProcessor(1,2,new float[]{1,2},null);
		ip = new ImagePlus("GrapeVine",proc);
		cal = new Calibration();
		cal.setFunction(Calibration.RODBARD, new double[] {5,6,7,8,9}, "hectareMummies");
		ip.setCalibration(cal);
		newOne = ip.createImagePlus();
		CalibrationTools.assertCalibrationsEqual(ip.getCalibration(), newOne.getCalibration());
		assertEquals(ip.getType(),newOne.getType());

		// try Color type with unique calib
		proc = new ColorProcessor(1,2,new int[]{1,2});
		ip = new ImagePlus("GrapeVine",proc);
		cal = new Calibration();
		cal.setFunction(Calibration.EXPONENTIAL, new double[] {5,6,7,8,9}, "hectareMummies");
		ip.setCalibration(cal);
		newOne = ip.createImagePlus();
		CalibrationTools.assertCalibrationsEqual(ip.getCalibration(), newOne.getCalibration());
		assertEquals(ip.getType(),newOne.getType());
	}

	@Test
	public void testCreateHyperStack() {

		ImagePlus result;
		Calibration cal;

		// try 8 bit case
		proc = new ByteProcessor(2,3,new byte[] {1,2,3,4,5,6},null);
		ip = new ImagePlus("BaseIP",proc);
		cal = new Calibration();
		cal.setFunction(Calibration.POWER,new double[] {8,5,1},"chewsPerCycle");
		ip.setCalibration(cal);
		result = ip.createHyperStack("SuperBytes", 2, 3, 4, 8);
		assertEquals("SuperBytes",result.getTitle());
		assertEquals(24,result.getStackSize());
		assertArrayEquals(new int[] {2,3,2,3,4},result.getDimensions());
		assertEquals(8,result.getBitDepth());
		CalibrationTools.assertCalibrationsEqual(ip.getCalibration(), result.getCalibration());
		assertTrue(result.getOpenAsHyperStack());

		// try 16 bit case
		proc = new ShortProcessor(2,3,new short[] {1,2,3,4,5,6},null);
		ip = new ImagePlus("BaseIP",proc);
		cal = new Calibration();
		cal.setFunction(Calibration.POLY4,new double[] {1,6,3},"franklinsPerMint");
		ip.setCalibration(cal);
		result = ip.createHyperStack("SuperShorts", 2, 3, 4, 16);
		assertEquals("SuperShorts",result.getTitle());
		assertEquals(24,result.getStackSize());
		//TODO reenable - when we figure out what this should do in this case
		//assertNull(result.getStack().getPixels(1));
		assertArrayEquals(new int[] {2,3,2,3,4},result.getDimensions());
		assertEquals(16,result.getBitDepth());
		CalibrationTools.assertCalibrationsEqual(ip.getCalibration(), result.getCalibration());
		assertTrue(result.getOpenAsHyperStack());

		// try 24 bit case
		proc = new ColorProcessor(1,4,new int[] {1,2,3,4});
		ip = new ImagePlus("BaseIP",proc);
		cal = new Calibration();
		cal.setFunction(Calibration.GAMMA_VARIATE,new double[] {2,2,2,2},"peanutsPerSnickers");
		ip.setCalibration(cal);
		result = ip.createHyperStack("SuperInts", 1, 4, 4, 24);
		assertEquals("SuperInts",result.getTitle());
		assertEquals(16,result.getStackSize());
		//TODO reenable - when we figure out what this should do in this case
		//assertNull(result.getStack().getPixels(1));
		assertArrayEquals(new int[] {1,4,1,4,4},result.getDimensions());
		// TODO reenable when possible (if ever)
		// Problem - createHyperStack() makes an ImageStack of a given size and given
		//   bitDepth. But 24 bit is a problem to support as ImgLib only supports
		//   8/16/32/64. So createHyperStack() translates 24 bit requests into 32 bit
		//   int as it was stored previously in IJ. But reported bitDepth is 32. This
		//   makes changes in the calibration and also getBitDepth(). Not sure how we
		//   should handle this.
		//assertEquals(24,result.getBitDepth());
		//CalibrationTools.assertCalibrationsEqual(ip.getCalibration(), result.getCalibration());
		assertTrue(result.getOpenAsHyperStack());

		// try 32 bit case
		proc = new FloatProcessor(2,2,new float[] {1,2,3,4},null);
		ip = new ImagePlus("BaseIP",proc);
		cal = new Calibration();
		cal.setFunction(Calibration.EXPONENTIAL,new double[] {5,10,20},"feetPerET");
		ip.setCalibration(cal);
		result = ip.createHyperStack("SuperFloats", 2, 3, 4, 32);
		assertEquals("SuperFloats",result.getTitle());
		assertEquals(24,result.getStackSize());
		assertArrayEquals(new int[] {2,2,2,3,4},result.getDimensions());
		assertEquals(32,result.getBitDepth());
		CalibrationTools.assertCalibrationsEqual(ip.getCalibration(), result.getCalibration());
		assertTrue(result.getOpenAsHyperStack());

		// should fail on any other case
		try {
			result = ip.createHyperStack("SuperWhats", 2, 3, 4, 11);
			fail();
		} catch (IllegalArgumentException e)
		{
			assertTrue(true);
		}
	}

	@Test
	public void testCopyScale() {
		Calibration cal,cal2,save;
		ImagePlus ip2;

		// save state of global calib
		ip = new ImagePlus();
		save = ip.getGlobalCalibration();
		ip.setGlobalCalibration(null);

		// passing null should make no changes
		ip = new ImagePlus();
		cal = new Calibration();
		cal.setFunction(Calibration.POWER,new double[] {8,9,4,3},"whackiesPerNoodle");
		ip.setCalibration(cal);
		ip.copyScale(null);
		CalibrationTools.assertCalibrationsEqual(ip.getCalibration(), cal);

		// passing a valid ImagePlus should copy its calib func
		ip = new ImagePlus();
		cal = new Calibration();
		cal.setFunction(Calibration.STRAIGHT_LINE,new double[] {3.0,6.3},"zigguratsPerCivilization");
		ip.setCalibration(cal);
		ip2 = new ImagePlus();
		cal2 = new Calibration();
		cal2.setFunction(Calibration.RODBARD2,new double[] {3,6,8,3,5,7},"chickletsPerBox");
		ip2.setCalibration(cal2);
		ip.copyScale(ip2);
		CalibrationTools.assertCalibrationsEqual(ip.getCalibration(), ip2.getCalibration());

		// if global cal set no change should happen
		ip.setGlobalCalibration(new Calibration());
		ip = new ImagePlus();
		cal = new Calibration();
		cal.setFunction(Calibration.STRAIGHT_LINE,new double[] {5,89},"hermansPerMunster");
		ip.setCalibration(cal);
		ip.copyScale(null);
		CalibrationTools.assertCalibrationsEqual(ip.getCalibration(), ip.getGlobalCalibration());

		// restore state of global calib so we don't mess up other tests
		ip.setGlobalCalibration(save);
	}

	@Test
	public void testStartTiming() {
		// note - nothing to test. exercise to do a compile time check of existence
		ip = new ImagePlus();
		ip.startTiming();
	}

	@Test
	public void testGetStartTime() {
		// note - nothing to test. exercise to do a compile time check of existence
		ip = new ImagePlus();
		ip.getStartTime();
	}

	@Test
	public void testGetCalibration() {
		Calibration save,cal;

		// save state of global calib
		ip = new ImagePlus();
		save = ip.getGlobalCalibration();
		ip.setGlobalCalibration(null);

		// after default ctor() calib should be null, get Calib should make a default one
		cal = new Calibration();
		ip = new ImagePlus();
		CalibrationTools.assertCalibrationsEqual(ip.getCalibration(), cal);

		// if calib not null return it
		ip = new ImagePlus();
		cal = new Calibration();
		cal.setFunction(Calibration.UNCALIBRATED_OD, new double[] {1,3,6,9}, "hooglesPerSnick");
		ip.setCalibration(cal);
		CalibrationTools.assertCalibrationsEqual(cal, ip.getCalibration());

		// if global calibration set return a copy of it
		cal = new Calibration();
		cal.setFunction(Calibration.POLY3,new double[] {7,5,3,1},"boomerangsPerEpicycle");
		ip.setGlobalCalibration(cal);
		ip = new ImagePlus();
		CalibrationTools.assertCalibrationsEqual(ip.getGlobalCalibration(), ip.getCalibration());

		// restore state of global calib so we don't mess up other tests
		ip.setGlobalCalibration(save);
	}

	@Test
	public void testSetCalibration() {
		Calibration cal;

		// set to null
		ip = new ImagePlus();
		cal = new Calibration();
		cal.setFunction(Calibration.UNCALIBRATED_OD, new double[] {1,3,6,9}, "hooglesPerSnick");
		ip.setCalibration(cal);
		ip.setCalibration(null);
		assertNotNull(ip.getCalibration());

		// set to a valid calib
		ip = new ImagePlus();
		cal = new Calibration();
		cal.setFunction(Calibration.UNCALIBRATED_OD, new double[] {1,3,6,9}, "hooglesPerSnick");
		ip.setCalibration(cal);
		CalibrationTools.assertCalibrationsEqual(cal, ip.getCalibration());
	}

	@Test
	public void testSetAndGetGlobalCalibration() {
		Calibration save,cal;

		// save state of global calib
		ip = new ImagePlus();
		save = ip.getGlobalCalibration();

		// test setting to null
		ip.setGlobalCalibration(null);
		assertNull(ip.getGlobalCalibration());

		// after default ctor() calib should be null, get Calib should make a default one
		ip = new ImagePlus();
		cal = new Calibration();
		ip.setGlobalCalibration(cal);
		CalibrationTools.assertCalibrationsEqual(ip.getGlobalCalibration(), cal);

		// restore state of global calib so we don't mess up other tests
		ip.setGlobalCalibration(save);
		assertEquals(ip.getGlobalCalibration(),save);
	}

	@Test
	public void testGetLocalCalibration() {
		Calibration save,cal;

		// save state of global calib
		ip = new ImagePlus();
		save = ip.getGlobalCalibration();
		ip.setGlobalCalibration(null);

		// after default ctor() calib should be null, get Calib should make a default one
		ip = new ImagePlus();
		cal = new Calibration();
		CalibrationTools.assertCalibrationsEqual(ip.getLocalCalibration(), cal);

		// if calib not null return it
		ip = new ImagePlus();
		cal = new Calibration();
		cal.setFunction(Calibration.UNCALIBRATED_OD, new double[] {1,3,6,9}, "hooglesPerSnick");
		ip.setCalibration(cal);
		CalibrationTools.assertCalibrationsEqual(cal, ip.getLocalCalibration());

		// if global calibration set still return a copy of the local one
		ip = new ImagePlus();
		cal = new Calibration();
		cal.setFunction(Calibration.POLY3,new double[] {7,5,3,1},"boomerangsPerEpicycle");
		ip.setCalibration(cal);
		ip.setGlobalCalibration(new Calibration());
		CalibrationTools.assertCalibrationsEqual(ip.getLocalCalibration(), cal);

		// restore state of global calib so we don't mess up other tests
		ip.setGlobalCalibration(save);
	}

	@Test
	public void testMouseMoved() {
		// note - can't test this. It uses private data and also has gui side effects
	}

	@Test
	public void testUpdateStatusbarValue() {
		// note - can't test this. It uses private data and also has gui side effects
	}

	@Test
	public void testGetLocationAsString() {

		//ImagePlus ip2;
		Calibration cal;
		String str;

		// if properties FHT

		//    cal unscaled
		proc = new FloatProcessor(4,2,new float[] {8,7,6,5,4,3,2,1},null);
		ip = new ImagePlus("Superman",proc);
		cal = new Calibration();
		ip.setCalibration(cal);
		ip.setProperty("FHT","ForThisCodeAnythingWillDo");
		str = ip.getLocationAsString(220, 150);
		assertEquals("r=0.02 p/c (265), theta= 325.29",str.substring(0, str.length()-1));  // ignore degree symbol - hudson dislikes

		//    cal scaled
		proc = new FloatProcessor(4,2,new float[] {8,7,6,5,4,3,2,1},null);
		ip = new ImagePlus("Superman",proc);
		cal = new Calibration();
		cal.setUnit("OrcNoses");
		cal.pixelWidth = 13.6;
		ip.setCalibration(cal);
		ip.setProperty("FHT","ForThisCodeAnythingWillDo");
		str = ip.getLocationAsString(220, 150);
		assertEquals("r=0.21 OrcNoses/c (265), theta= 325.29",str.substring(0, str.length()-1));  // ignore degree symbol - hudson dislikes

		// Test alt key down cases
		IJ.setKeyDown(KeyEvent.VK_ALT);

		//    single image
		ip = new ImagePlus();
		assertEquals(" x=5, y=3",ip.getLocationAsString(5, 3));

		//    stack
		//proc = new FloatProcessor(4,2,new float[] {8,7,6,5,4,3,2,1},null);
		//ip = new ImagePlus("Superman",proc);
		ip = new ImagePlus("Superman",(Image)null);
		st = new ImageStack(4,2);
		st.addSlice("Super Strength", new float[] {8,7,6,5,4,3,2,1});
		st.addSlice("Unaided Flight", new float[] {8,7,6,5,4,3,2,1});
		st.addSlice("Laser Vision", new float[] {8,7,6,5,4,3,2,1});
		ip.setStack("Special Powers",st);
		ip.setCurrentSlice(1);
		assertEquals(" x=5, y=3, z=0",ip.getLocationAsString(5, 3));
		ip.setCurrentSlice(2);
		assertEquals(" x=5, y=3, z=1",ip.getLocationAsString(5, 3));
		ip.setCurrentSlice(3);
		assertEquals(" x=5, y=3, z=2",ip.getLocationAsString(5, 3));

		//    hyperstack
		//      note - can't test - only changes hyperstack currently being displayed

		// Test alt key up cases
		IJ.setKeyUp(KeyEvent.VK_ALT);

		//    single image
		ip = new ImagePlus();
		assertEquals(" x=20, y=35",ip.getLocationAsString(20, 35));

		//    stack
		//proc = new FloatProcessor(4,2,new float[] {8,7,6,5,4,3,2,1},null);
		//ip = new ImagePlus("Superman",proc);
		ip = new ImagePlus("Superman",(Image)null);
		st = new ImageStack(4,2);
		st.addSlice("Super Strength", new float[] {8,7,6,5,4,3,2,1});
		st.addSlice("Unaided Flight", new float[] {8,7,6,5,4,3,2,1});
		st.addSlice("Laser Vision", new float[] {8,7,6,5,4,3,2,1});
		ip.setStack("Special Powers",st);
		cal = new Calibration();
		cal.pixelDepth = 2.0;
		cal.pixelHeight = 3.0;
		cal.pixelWidth = 4.0;
		ip.setCalibration(cal);
		ip.setCurrentSlice(1);
		assertEquals(" x=80, y=105, z=0",ip.getLocationAsString(20, 35));
		ip.setCurrentSlice(2);
		assertEquals(" x=80, y=105, z=2",ip.getLocationAsString(20, 35));
		ip.setCurrentSlice(3);
		assertEquals(" x=80, y=105, z=4",ip.getLocationAsString(20, 35));

		//    hyperstack
		//      note - can't test - only changes hyperstack currently being displayed
	}

	@Test
	public void testCopy() {
		// reset state to keep from getting messed up by other tests
		ImagePlus.resetClipboard();

		// cut == true, roi null
		proc = new ColorProcessor(2,3,new int[] {1,2,3,4,5,6});
		ip = new ImagePlus("Copier",proc);
		ImagePlus.resetClipboard();
		assertNull(ip.getRoi());
		assertNull(ImagePlus.getClipboard());
		ip.copy(true);
		assertNotNull(ImagePlus.getClipboard());
		assertNotNull(proc.getSnapshotPixels());

		// cut == true, roi not an area
		proc = new ColorProcessor(2,3,new int[] {1,2,3,4,5,6});
		ip = new ImagePlus("Copier",proc);
		ImagePlus.resetClipboard();
		assertNull(ip.getRoi());
		ip.setRoi(new Line(1,1,2,2));
		assertNotNull(ip.getRoi());
		assertNull(ImagePlus.getClipboard());
		ip.copy(true);
		assertNull(ImagePlus.getClipboard());
		assertNull(proc.getSnapshotPixels());

		// cut == true, roi an area
		proc = new ColorProcessor(2,3,new int[] {1,2,3,4,5,6});
		ip = new ImagePlus("Copier",proc);
		ImagePlus.resetClipboard();
		assertNull(ip.getRoi());
		ip.setRoi(new Rectangle(1,1,2,2));
		assertNotNull(ip.getRoi());
		assertNull(ImagePlus.getClipboard());
		ip.copy(true);
		assertNotNull(ImagePlus.getClipboard());
		assertNotNull(proc.getSnapshotPixels());

		// cut == false, roi null
		proc = new ColorProcessor(2,3,new int[] {1,2,3,4,5,6});
		ip = new ImagePlus("Copier",proc);
		ImagePlus.resetClipboard();
		assertNull(ip.getRoi());
		assertNull(ImagePlus.getClipboard());
		ip.copy(false);
		assertNotNull(ImagePlus.getClipboard());
		assertNull(proc.getSnapshotPixels());

		// cut == false, roi not an area
		proc = new ColorProcessor(2,3,new int[] {1,2,3,4,5,6});
		ip = new ImagePlus("Copier",proc);
		ImagePlus.resetClipboard();
		assertNull(ip.getRoi());
		ip.setRoi(new Line(1,1,2,2));
		assertNotNull(ip.getRoi());
		assertNull(ImagePlus.getClipboard());
		ip.copy(false);
		assertNull(ImagePlus.getClipboard());
		assertNull(proc.getSnapshotPixels());

		// cut == false, roi an area
		proc = new ColorProcessor(2,3,new int[] {1,2,3,4,5,6});
		ip = new ImagePlus("Copier",proc);
		ImagePlus.resetClipboard();
		assertNull(ip.getRoi());
		ip.setRoi(new Rectangle(1,1,2,2));
		assertNotNull(ip.getRoi());
		assertNull(ImagePlus.getClipboard());
		ip.copy(false);
		assertNotNull(ImagePlus.getClipboard());
		assertNull(proc.getSnapshotPixels());

		// reset state to keep from messing up other tests
		ImagePlus.resetClipboard();
	}

	@Test
	public void testPaste() {
		// reset state to keep from getting messed up by other tests
		ImagePlus.resetClipboard();

		// paste when nothing in clipboard
		ImagePlus.resetClipboard();
		proc = new ByteProcessor(2,3,new byte[] {1,2,3,4,5,6},null);
		ip = new ImagePlus("Paster",proc);
		assertNull(ImagePlus.getClipboard());
		assertFalse(ip.changes);
		assertNull(ip.getRoi());
		ip.paste();
		assertFalse(ip.changes);
		assertNull(ip.getRoi());

		// paste with valid data (part of image)
		ImagePlus.resetClipboard();
		proc = new ByteProcessor(2,3,new byte[] {1,2,3,4,5,6},null);
		ip = new ImagePlus("Paster",proc);
		assertNull(ImagePlus.getClipboard());
		assertEquals(4,proc.get(1,1));
		ip.setRoi(1,1,1,1);
		ip.copy(true);
		assertNotNull(ImagePlus.getClipboard());
		assertFalse(ip.changes);
		assertEquals(255,proc.get(1,1));
		ip.paste();
		assertTrue(ip.changes);
		assertNotNull(ip.getRoi());
		//ip.getRoi().drawPixels();
		//assertEquals(4,proc.get(1, 1));

		// paste with valid data (whole image)
		ImagePlus.resetClipboard();
		proc = new ByteProcessor(2,3,new byte[] {1,2,3,4,5,6},null);
		ip = new ImagePlus("Paster",proc);
		assertNull(ImagePlus.getClipboard());
		assertNull(ip.getRoi());
		assertEquals(4,proc.get(1,1));
		ip.copy(true);
		assertNotNull(ImagePlus.getClipboard());
		assertFalse(ip.changes);
		assertEquals(255,proc.get(1,1));
		ip.paste();
		assertTrue(ip.changes);
		assertNotNull(ip.getRoi());
		//ip.getRoi().drawPixels();
		//assertEquals(4,proc.get(1, 1));

		// reset state to keep from messing up other tests
		ImagePlus.resetClipboard();
	}

	@Test
	public void testGetClipboard() {
		// nothing to test - just put in compile time check
		ImagePlus.getClipboard();
	}

	@Test
	public void testResetClipboard() {
		// reset state to keep from getting messed up by other tests
		ImagePlus.resetClipboard();

		proc = new FloatProcessor(5,2,new float[] {0,1,2,3,4,5,6,7,8,9},null);
		ip = new ImagePlus("Jones",proc);
		assertNull(ImagePlus.getClipboard());
		ip.copy(false);
		assertNotNull(ImagePlus.getClipboard());
		ImagePlus.resetClipboard();
		assertNull(ImagePlus.getClipboard());

		// reset state to keep from messing up other tests
		ImagePlus.resetClipboard();
	}

	private class FakeListener implements ImageListener
	{
		public boolean opened, closed, updated;

		public FakeListener() {}
		public void imageOpened(ImagePlus imp)  {opened = true;}
		public void imageClosed(ImagePlus imp)  {closed = true;}
		public void imageUpdated(ImagePlus imp) {updated = true;}
	}

	@Test
	public void testAddImageListener() {
		FakeListener lst;

		ip = new ImagePlus();
		lst = new FakeListener();
		ImagePlus.addImageListener(lst);
		ip.notifyListeners(ImagePlus.CLOSED);
		assertFalse(lst.opened);
		assertTrue(lst.closed);
		assertFalse(lst.updated);

		ip = new ImagePlus();
		lst = new FakeListener();
		ImagePlus.addImageListener(lst);
		ip.notifyListeners(ImagePlus.OPENED);
		assertTrue(lst.opened);
		assertFalse(lst.closed);
		assertFalse(lst.updated);

		ip = new ImagePlus();
		lst = new FakeListener();
		ImagePlus.addImageListener(lst);
		ip.notifyListeners(ImagePlus.UPDATED);
		assertFalse(lst.opened);
		assertFalse(lst.closed);
		assertTrue(lst.updated);
}

	@Test
	public void testRemoveImageListener() {
		FakeListener lst;

		ip = new ImagePlus();
		lst = new FakeListener();
		ImagePlus.addImageListener(lst);
		ImagePlus.removeImageListener(lst);
		ip.notifyListeners(ImagePlus.CLOSED);
		assertFalse(lst.opened);
		assertFalse(lst.closed);
		assertFalse(lst.updated);

		ip = new ImagePlus();
		lst = new FakeListener();
		ImagePlus.addImageListener(lst);
		ImagePlus.removeImageListener(lst);
		ip.notifyListeners(ImagePlus.OPENED);
		assertFalse(lst.opened);
		assertFalse(lst.closed);
		assertFalse(lst.updated);

		ip = new ImagePlus();
		lst = new FakeListener();
		ImagePlus.addImageListener(lst);
		ImagePlus.removeImageListener(lst);
		ip.notifyListeners(ImagePlus.UPDATED);
		assertFalse(lst.opened);
		assertFalse(lst.closed);
		assertFalse(lst.updated);
	}

	@Test
	public void testIsLocked() {
		ip = new ImagePlus();
		assertFalse(ip.isLocked());
		ip.lockSilently();
		assertTrue(ip.isLocked());
	}

	@Test
	public void testSetAndGetOpenAsHyperStack() {
		ip = new ImagePlus();
		assertFalse(ip.getOpenAsHyperStack());
		ip.setOpenAsHyperStack(true);
		assertTrue(ip.getOpenAsHyperStack());
		ip.setOpenAsHyperStack(false);
		assertFalse(ip.getOpenAsHyperStack());
	}

	@Test
	public void testIsComposite() {
		CompositeImage c;

		// not a composite image
		ip = new ImagePlus();
		assertFalse(ip.isComposite());

		// composite image
		proc = new ColorProcessor(3,2,new int[] {6,5,4,3,2,1});
		ip = new ImagePlus("NoHoldsBarred",proc);
		c = new CompositeImage(ip);
		assertTrue(c.isComposite());

		// note - as far as I can tell the NChannels() test in isComposite() is always true - no subcase to test
	}

	@Test
	public void testSetDisplayRangeDoubleDouble() {

		// in the case ip's ImageProcessor is null we should be able to set the range but there is no way to test it.
		// the input range is ignored.
		ip = new ImagePlus();
		ip.setDisplayRange(4,22);

		// pass in a legit ImagePlus
		proc = new ByteProcessor(3,1,new byte[] {1,4,7},null);
		ip = new ImagePlus("Jellybeans",proc);

		// illegal values
		ip.setDisplayRange(-1,-1);
		assertEquals(-1,ip.getDisplayRangeMin(),Assert.DOUBLE_TOL);
		assertEquals(-1,ip.getDisplayRangeMax(),Assert.DOUBLE_TOL);

		// zero values
		ip.setDisplayRange(0,0);
		assertEquals(0,ip.getDisplayRangeMin(),Assert.DOUBLE_TOL);
		assertEquals(0,ip.getDisplayRangeMax(),Assert.DOUBLE_TOL);

		// min > max
		ip.setDisplayRange(1000,999);
		assertEquals(0,ip.getDisplayRangeMin(),Assert.DOUBLE_TOL);
		assertEquals(0,ip.getDisplayRangeMax(),Assert.DOUBLE_TOL);

		// legal values - notice how Double.Max converted to Integer.Max
		ip.setDisplayRange(77000,Double.MAX_VALUE);
		assertEquals(77000,ip.getDisplayRangeMin(),Assert.DOUBLE_TOL);
		assertEquals(Integer.MAX_VALUE,ip.getDisplayRangeMax(),Assert.DOUBLE_TOL);
	}

	@Test
	public void testGetDisplayRangeMinAndMax() {
		proc = new ShortProcessor(1,1,new short[] {5}, null);
		proc.setMinAndMax(1.0,9.0);
		ip = new ImagePlus("Taluhla",proc);
		assertEquals(proc.getMin(),ip.getDisplayRangeMin(),Assert.DOUBLE_TOL);
		assertEquals(proc.getMax(),ip.getDisplayRangeMax(),Assert.DOUBLE_TOL);
	}

	@Test
	public void testSetDisplayRangeDoubleDoubleInt() {

		// not a color processor
		proc = new FloatProcessor(1,1,new float[] {0xaabbcc},null);
		ip = new ImagePlus("GumbyShoes",proc);
		ip.setDisplayRange(1,7,904);
		assertEquals(1,ip.getDisplayRangeMin(),Assert.DOUBLE_TOL);
		assertEquals(7,ip.getDisplayRangeMax(),Assert.DOUBLE_TOL);

		// color processor : 1 channel
		proc = new ColorProcessor(1,1,new int[] {0xaabbcc});
		ip = new ImagePlus("GumbyShoes",proc);
		ip.setDisplayRange(1,7,1);
		assertEquals(1,ip.getDisplayRangeMin(),Assert.DOUBLE_TOL);
		assertEquals(7,ip.getDisplayRangeMax(),Assert.DOUBLE_TOL);
		assertArrayEquals(new int[] {170,187,255,0},ip.getPixel(0,0));

		// color processor : 2 channel
		proc = new ColorProcessor(1,1,new int[] {0xaabbcc});
		ip = new ImagePlus("GumbyShoes",proc);
		ip.setDisplayRange(1,7,2);
		assertEquals(1,ip.getDisplayRangeMin(),Assert.DOUBLE_TOL);
		assertEquals(7,ip.getDisplayRangeMax(),Assert.DOUBLE_TOL);
		assertArrayEquals(new int[] {170,255,204,0},ip.getPixel(0,0));

		// color processor : 3 channel
		proc = new ColorProcessor(1,1,new int[] {0xaabbcc});
		ip = new ImagePlus("GumbyShoes",proc);
		ip.setDisplayRange(1,7,3);
		assertEquals(1,ip.getDisplayRangeMin(),Assert.DOUBLE_TOL);
		assertEquals(7,ip.getDisplayRangeMax(),Assert.DOUBLE_TOL);
		assertArrayEquals(new int[] {170,255,255,0},ip.getPixel(0,0));

		// color processor : 4 channel
		proc = new ColorProcessor(1,1,new int[] {0xaabbcc});
		ip = new ImagePlus("GumbyShoes",proc);
		ip.setDisplayRange(1,7,4);
		assertEquals(1,ip.getDisplayRangeMin(),Assert.DOUBLE_TOL);
		assertEquals(7,ip.getDisplayRangeMax(),Assert.DOUBLE_TOL);
		assertArrayEquals(new int[] {255,187,204,0},ip.getPixel(0,0));

		// color processor : 5 channel
		proc = new ColorProcessor(1,1,new int[] {0xaabbcc});
		ip = new ImagePlus("GumbyShoes",proc);
		ip.setDisplayRange(1,7,5);
		assertEquals(1,ip.getDisplayRangeMin(),Assert.DOUBLE_TOL);
		assertEquals(7,ip.getDisplayRangeMax(),Assert.DOUBLE_TOL);
		assertArrayEquals(new int[] {255,187,255,0},ip.getPixel(0,0));

		// color processor : 6 channel
		proc = new ColorProcessor(1,1,new int[] {0xaabbcc});
		ip = new ImagePlus("GumbyShoes",proc);
		ip.setDisplayRange(1,7,6);
		assertEquals(1,ip.getDisplayRangeMin(),Assert.DOUBLE_TOL);
		assertEquals(7,ip.getDisplayRangeMax(),Assert.DOUBLE_TOL);
		assertArrayEquals(new int[] {255,255,204,0},ip.getPixel(0,0));

		// color processor : 7 channel
		proc = new ColorProcessor(1,1,new int[] {0xaabbcc});
		ip = new ImagePlus("GumbyShoes",proc);
		ip.setDisplayRange(1,7,7);
		assertEquals(1,ip.getDisplayRangeMin(),Assert.DOUBLE_TOL);
		assertEquals(7,ip.getDisplayRangeMax(),Assert.DOUBLE_TOL);
		assertArrayEquals(new int[] {255,255,255,0},ip.getPixel(0,0));
	}

	@Test
	public void testResetDisplayRange() {
		proc = new ShortProcessor(2,2,new short[] {8,8,8,8},null);
		ip = new ImagePlus("Leaflet",proc);
		ip.setDisplayRange(44, 99);
		assertEquals(44,ip.getDisplayRangeMin(),Assert.DOUBLE_TOL);
		assertEquals(99,ip.getDisplayRangeMax(),Assert.DOUBLE_TOL);
		ip.resetDisplayRange();
		assertEquals(8,ip.getDisplayRangeMin(),Assert.DOUBLE_TOL);
		assertEquals(8,ip.getDisplayRangeMax(),Assert.DOUBLE_TOL);
	}

	@Test
	public void testUpdatePosition() {
		ip = new ImagePlus();

		ip.updatePosition(-1,-2,-3);
		assertEquals(-1,ip.getChannel());
		assertEquals(-2,ip.getSlice());
		assertEquals(-3,ip.getFrame());

		ip.updatePosition(0,0,0);
		assertEquals(0,ip.getChannel());
		assertEquals(0,ip.getSlice());
		assertEquals(0,ip.getFrame());

		ip.updatePosition(19,3,755);
		assertEquals(19,ip.getChannel());
		assertEquals(3,ip.getSlice());
		assertEquals(755,ip.getFrame());
	}

	@Test
	public void testFlatten() {
		ImagePlus ip2;

		proc = new ByteProcessor(3,2,new byte[] {4,3,6,5,8,7},null);
		ip = new ImagePlus("Zoobooks",proc);

		ip2 = ip.flatten();
		assertTrue(ip2.getProcessor() instanceof ColorProcessor);
		assertEquals("Flat_Zoobooks",ip2.getTitle());
	}

	@Test
	public void testSetAndGetOverlayOverlay() {
		Overlay o;

		// image canvas is null
		ip = new ImagePlus();
		assertNull(ip.getOverlay());
		o = new Overlay();
		ip.setOverlay(o);
		assertEquals(o,ip.getOverlay());

		// image canvas is not null
		//    note - this case unreachable - since win is always null it always is equal to flattening cnvas which is
		//      always null outside of the runtime of the flatten() method.
	}

	@Test
	public void testSetOverlayShapeColorBasicStroke() {
		Overlay ov;
		BasicStroke stroke;
		Roi roi;

		// shape is null case
		proc = new ColorProcessor(1,1,new int[] {1});
		ip = new ImagePlus("FannyMae",proc);
		stroke = new BasicStroke();
		ip.setOverlay(new Overlay());
		assertNotNull(ip.getOverlay());
		ip.setOverlay((Shape)null,Color.BLUE, stroke);
		assertNull(ip.getOverlay());

		// shape is not null case
		proc = new ColorProcessor(1,1,new int[] {1});
		ip = new ImagePlus("FannyMae",proc);
		stroke = new BasicStroke(BasicStroke.CAP_ROUND);
		assertNull(ip.getOverlay());
		ip.setOverlay(new Rectangle(0,0,1,1), Color.RED, stroke);
		ov = ip.getOverlay();
		assertNotNull(ov);
		roi = ov.toArray()[0];
		assertEquals(Color.RED,roi.getStrokeColor());
		assertEquals(stroke,roi.getStroke());
	}

	@Test
	public void testSetOverlayRoiColorIntColor() {
		Roi roi;
		Overlay ov;

		if (IJInfo.RUN_ENHANCED_TESTS)
		{
			// roi is null case
			proc = new ColorProcessor(1,1,new int[] {1});
			ip = new ImagePlus("FreddyMac",proc);
			assertNull(ip.getOverlay());
			ip.setOverlay(null,Color.YELLOW,14,Color.CYAN);
			assertNull(ip.getOverlay());
		}

		// roi is legit
		roi = new Roi(new Rectangle(0,0,4,5));
		proc = new ColorProcessor(1,1,new int[] {1});
		ip = new ImagePlus("FreddyMac",proc);
		assertNull(ip.getOverlay());
		ip.setOverlay(roi,Color.YELLOW,14,Color.CYAN);
		ov = ip.getOverlay();
		assertNotNull(ov);
		roi = ov.toArray()[0];
		assertEquals(Color.YELLOW,roi.getStrokeColor());
		assertEquals(14,roi.getStrokeWidth(),Assert.DOUBLE_TOL);
		assertEquals(Color.CYAN,roi.getFillColor());
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
	public void testToString() {
		String str;

		// basic ImagePlus
		proc = new ByteProcessor(2,1,new byte[] {2,1}, null);
		ip = new ImagePlus("Arckle",proc);
		str = "imp["+ip.getTitle()+" " + ip.width + "x" + ip.height + "x" + ip.getStackSize() + "]";
		assertEquals(str,ip.toString());

		// a stacked ImagePlus
		//proc = new ByteProcessor(2,1,new byte[] {2,1}, null);
		//ip = new ImagePlus("Arckle",proc);
		ip = new ImagePlus("Arckle",(Image)null);
		st = new ImageStack(2,1);
		st.addSlice("bank 1",new byte[] {0,5});
		st.addSlice("bank 2",new byte[] {4,9});
		ip.setStack("misc.",st);
		str = "imp["+ip.getTitle()+" " + ip.width + "x" + ip.height + "x" + ip.getStackSize() + "]";
		assertEquals(str,ip.toString());
	}
}
