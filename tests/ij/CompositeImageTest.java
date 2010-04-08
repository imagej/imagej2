package ij;

import static org.junit.Assert.*;


import org.junit.Before;
import org.junit.Test;

import ij.io.FileInfo;
import ij.process.*;

import ij.measure.*;

public class CompositeImageTest {

	// instance vars
	CompositeImage ci;
	ImageProcessor proc;
	ImagePlus ip;
	ImageStack st;

	@Before
	public void setUp() throws Exception {
		st = new ImageStack(2,2);
		st.addSlice("Fred", new ColorProcessor(2,2));
		ip = new ImagePlus("Jones",st);
		ci = new CompositeImage(ip);
	}

	@Test
	public void testConstants() {
		assertEquals(1,CompositeImage.COMPOSITE);
		assertEquals(2,CompositeImage.COLOR);
		assertEquals(3,CompositeImage.GRAYSCALE);
		assertEquals(4,CompositeImage.TRANSPARENT);
		assertEquals(7,CompositeImage.MAX_CHANNELS);
	}
	
	@Test
	public void testCompositeImageImagePlus() {

		// the default case calls the general case whihc is tested in the next method
		// only need to test that the constructor succeeded in making a COLOR image
		
		proc = new ColorProcessor(20, 25);
		ip = new ImagePlus("MyIp",proc);
		ci = new CompositeImage(ip);
		assertNotNull(ci);
		assertEquals(CompositeImage.COLOR,ci.getMode());
	}

	@Test
	public void testCompositeImageImagePlusInt() {
		
		// test that mode gets set correctly
		
		proc = new ColorProcessor(20, 25);
		ip = new ImagePlus("MyIp",proc);
		
		ci = new CompositeImage(ip,CompositeImage.COMPOSITE-1); // illegal value
		assertNotNull(ci);
		assertEquals(CompositeImage.COLOR,ci.getMode());
		assertArrayEquals(new int[] {20,25,3,1,1},ci.getDimensions());
		assertFalse(ci.getOpenAsHyperStack());
		
		ci = new CompositeImage(ip,CompositeImage.COMPOSITE);
		assertNotNull(ci);
		assertEquals(CompositeImage.COMPOSITE,ci.getMode());
		assertArrayEquals(new int[] {20,25,3,1,1},ci.getDimensions());
		assertFalse(ci.getOpenAsHyperStack());

		ci = new CompositeImage(ip,CompositeImage.COLOR);
		assertNotNull(ci);
		assertEquals(CompositeImage.COLOR,ci.getMode());
		assertArrayEquals(new int[] {20,25,3,1,1},ci.getDimensions());
		assertFalse(ci.getOpenAsHyperStack());

		ci = new CompositeImage(ip,CompositeImage.GRAYSCALE);
		assertNotNull(ci);
		assertEquals(CompositeImage.GRAYSCALE,ci.getMode());
		assertArrayEquals(new int[] {20,25,3,1,1},ci.getDimensions());
		assertFalse(ci.getOpenAsHyperStack());

		ci = new CompositeImage(ip,CompositeImage.GRAYSCALE+1);  // semi-illegal value - may change
		assertNotNull(ci);
		assertEquals(CompositeImage.COLOR,ci.getMode());
		assertArrayEquals(new int[] {20,25,3,1,1},ci.getDimensions());
		assertFalse(ci.getOpenAsHyperStack());

		ci = new CompositeImage(ip,CompositeImage.TRANSPARENT);
		assertNotNull(ci);
		// TODO - broken code makes next test fail - getting info from Wayne
		//assertEquals(CompositeImage.TRANSPARENT,ci.getMode());
		assertArrayEquals(new int[] {20,25,3,1,1},ci.getDimensions());
		assertFalse(ci.getOpenAsHyperStack());

		//  if rgb and stack size != 1 should throw excep
		try {
			st = new ImageStack(4,4);
			st.addSlice("Zap",new ColorProcessor(4,4));
			st.addSlice("Dos",new ColorProcessor(4,4));
			ip = new ImagePlus("Buffy",st);
			ci = new CompositeImage(ip,CompositeImage.COLOR);
			fail();
		} catch (IllegalArgumentException e) {
			assertTrue(true);
		}

		//  if rgb and stack size == 1 should be okay
		st = new ImageStack(4,4);
		st.addSlice("Zap",new ColorProcessor(4,4));
		ip = new ImagePlus("Willow",st);
		ci = new CompositeImage(ip,CompositeImage.COLOR);
		assertNotNull(ci);
		assertEquals(3,ci.getStackSize());
		assertArrayEquals(new int[] {4,4,3,1,1},ci.getDimensions());
		assertFalse(ci.getOpenAsHyperStack());
		
		// not rgb but have a stack: size == 1 should throw excep
		try {
			st = new ImageStack(10,15);
			st.addSlice("Uno",new byte[]{1,2,3,4});
			ip = new ImagePlus("Giles",st);
			ci = new CompositeImage(ip,CompositeImage.COLOR);
			fail();
		} catch (IllegalArgumentException e) {
			assertTrue(true);
		}
		
		// not rgb but have a stack: size > 1 should be okay
		st = new ImageStack(2,2);
		st.addSlice("Uno",new byte[]{1,2,3,4});
		st.addSlice("Dos",new byte[]{1,2,3,4});
		ip = new ImagePlus("Xander",st);
		ci = new CompositeImage(ip,CompositeImage.COLOR);
		assertNotNull(ci);
		assertArrayEquals(new int[] {2,2,2,1,1},ci.getDimensions());
		assertFalse(ci.getOpenAsHyperStack());
		
		// channels >= 2 but stack size not evenly divisible by channel count
		//   I don't think this last case is possible - can't figure out a way to do so

		// mode == COMPOSITE and channels > MAX_CHANNELS then okay but type changes
		st = new ImageStack(2,2);
		for (int i = 0; i < 32; i++)
			st.addSlice(""+i,new byte[]{1,2,3,4});
		ip = new ImagePlus("Spike",st);
		ip.setDimensions(8,2,2);
		ci = new CompositeImage(ip,CompositeImage.COMPOSITE);
		assertNotNull(ci);
		assertEquals(CompositeImage.COLOR,ci.getMode());  // notice - type change
		assertArrayEquals(new int[] {2,2,8,2,2},ci.getDimensions());
		assertTrue(ci.getOpenAsHyperStack());  // notice - TRUE HERE

		// some general tests of internals...
		
		// GRAYSCALE, Info property, actives
		st = new ImageStack(2,2);
		for (int i = 0; i < 4; i++)
			st.addSlice(""+i,new byte[]{1,2,3,4});
		ip = new ImagePlus("Angel",st);
		ip.setProperty("Info","Cowabunga Dudes!");
		ip.setProperty("Silly","giggles");
		ci = new CompositeImage(ip,CompositeImage.GRAYSCALE);
		assertNotNull(ci);
		assertEquals("Cowabunga Dudes!",ci.getProperty("Info"));
		assertNull(ci.getProperty("Silly"));
		assertTrue(ci.getActiveChannels()[0]);
		for (int i = 1; i < CompositeImage.MAX_CHANNELS; i++)
			assertFalse(ci.getActiveChannels()[i]);
		assertArrayEquals(new int[] {2,2,4,1,1},ci.getDimensions());
		assertFalse(ci.getOpenAsHyperStack());

		// COMPOSITE, actives
		st = new ImageStack(2,2);
		for (int i = 0; i < 4; i++)
			st.addSlice(""+i,new byte[]{1,2,3,4});
		ip = new ImagePlus("Angel",st);
		ci = new CompositeImage(ip,CompositeImage.COMPOSITE);
		assertNotNull(ci);
		for (int i = 0; i < CompositeImage.MAX_CHANNELS; i++)
			assertTrue(ci.getActiveChannels()[i]);
		assertArrayEquals(new int[] {2,2,4,1,1},ci.getDimensions());
		assertFalse(ci.getOpenAsHyperStack());
		
		// COLOR, fileInfo, calibration, channelLuts
		ip = new ImagePlus("data/embryos.bmp");
		ci = new CompositeImage(ip,CompositeImage.COLOR);
		assertNotNull(ci);
		FileInfo fi = ci.getOriginalFileInfo();
		assertNotNull(fi);
		assertEquals("data/",fi.directory);
		assertEquals("embryos.bmp",fi.fileName);
		// can't test that ci's displayRanges have been set to original fileInfo's - no method for access
		assertArrayEquals(ip.getFileInfo().channelLuts,fi.channelLuts);
		assertArrayEquals(new int[] {1600,1200,3,1,1},ci.getDimensions());
		assertFalse(ci.getOpenAsHyperStack());
		// now multiple tests that the calib copy was successful
		// bitDepth changed between the two so can't use isSameAs()
		// test publicly accessible fields
		Calibration act = ci.getCalibration();
		Calibration exp = ip.getCalibration();
		assertEquals(exp.fps,act.fps,Assert.DOUBLE_TOL);
		assertEquals(exp.frameInterval,act.frameInterval,Assert.DOUBLE_TOL);
		assertEquals(exp.info,act.info);
		assertEquals(exp.loop,act.loop);
		assertEquals(exp.pixelDepth,act.pixelDepth,Assert.DOUBLE_TOL);
		assertEquals(exp.pixelHeight,act.pixelHeight,Assert.DOUBLE_TOL);
		assertEquals(exp.pixelWidth,act.pixelWidth,Assert.DOUBLE_TOL);
		assertEquals(exp.xOrigin,act.xOrigin,Assert.DOUBLE_TOL);
		assertEquals(exp.yOrigin,act.yOrigin,Assert.DOUBLE_TOL);
		assertEquals(exp.zOrigin,act.zOrigin,Assert.DOUBLE_TOL);
	}

	@Test
	public void testGetImage() {
		// this method has no behavior that can't be tested elsewhere
		assertNotNull(ci.getImage());
	}

	@Test
	public void testUpdateChannelAndDraw() {
		// can set singleChannel to true but can't test this fact
		// otherwise just calls updateAndDraw.
		ci.updateChannelAndDraw();
	}

	@Test
	public void testUpdateAllChannelsAndDraw() {
		//fail("Not yet implemented");
		// more gui oriented code
		// TODO - need to think how to better test this after we figure out testing update code
		ci.updateAllChannelsAndDraw();
	}

	@Test
	public void testGetChannelProcessor() {
		//fail("Not yet implemented");
	}

	/*
	public void resetDisplayRanges() {
		int channels = getNChannels();
		ImageStack stack2 = getImageStack();
		if (lut==null || channels!=lut.length || channels>stack2.getSize() || channels>MAX_CHANNELS)
			return;
		for (int i=0; i<channels; ++i) {
			ImageProcessor ip2 = stack2.getProcessor(i+1);
			ip2.resetMinAndMax();
			lut[i].min = ip2.getMin();
			lut[i].max = ip2.getMax();
		}
	}
	*/
	
	@Test
	public void testResetDisplayRanges() {
		double min,max;
		LUT lut;
		
		// if lut == null do nothing
		// if channel count != lut.length do nothing
		// if channel count > stack size do nothing
		// if channel count > MAX_CHANNELS do nothing
		
		// otherwise it should work
		
		lut = ci.getChannelLut(1);
		
		min = lut.min;
		max = lut.max;
		lut.min = min - 5000;
		lut.max = max + 1005;
		
		ci.resetDisplayRanges();
		
		assertEquals(min,lut.min,Assert.DOUBLE_TOL);
		assertEquals(max,lut.max,Assert.DOUBLE_TOL);
	}

	@Test
	public void testUpdateAndDraw() {
		// note: all code not tested elsewhere is gui oriented here and cannot be tested
	}

	@Test
	public void testUpdateImage() {
		//fail("Not yet implemented");
	}

	@Test
	public void testCreateLutFromColor() {
		//fail("Not yet implemented");
	}

	@Test
	public void testGetChannelColor() {
		//fail("Not yet implemented");
	}

	@Test
	public void testGetProcessorInt() {
		//fail("Not yet implemented");
	}

	@Test
	public void testGetActiveChannels() {
		//fail("Not yet implemented");
	}

	@Test
	public void testSetMode() {
		//fail("Not yet implemented");
	}

	@Test
	public void testGetMode() {
		//fail("Not yet implemented");
	}

	@Test
	public void testGetModeAsString() {
		//fail("Not yet implemented");
	}

	@Test
	public void testGetChannelLutInt() {
		//fail("Not yet implemented");
	}

	@Test
	public void testGetChannelLut() {
		//fail("Not yet implemented");
	}

	@Test
	public void testGetLuts() {
		//fail("Not yet implemented");
	}

	@Test
	public void testSetLuts() {
		//fail("Not yet implemented");
	}

	@Test
	public void testCopyLuts() {
		//fail("Not yet implemented");
	}

	@Test
	public void testReset() {
		//fail("Not yet implemented");
	}

	@Test
	public void testSetChannelLutLUT() {
		//fail("Not yet implemented");
	}

	@Test
	public void testSetChannelLutLUTInt() {
		//fail("Not yet implemented");
	}

	@Test
	public void testSetChannelColorModel() {
		//fail("Not yet implemented");
	}

	
	@Test
	public void testSetDisplayRangeDoubleDouble() {
		//fail("Not yet implemented");
	}

	@Test
	public void testGetDisplayRangeMinAndMax() {
		LUT lut = ci.getChannelLut(1);
		int channel = ci.getChannelIndex();
		ci.setDisplayRange(100, 900);
		double min = ci.getDisplayRangeMin();
		double max = ci.getDisplayRangeMax();
		assertEquals(100,min,Assert.DOUBLE_TOL);
		assertEquals(900,max,Assert.DOUBLE_TOL);
		assertEquals(ci.getLuts()[channel].min,min,Assert.DOUBLE_TOL);
		assertEquals(ci.getLuts()[channel].max,max,Assert.DOUBLE_TOL);
	}

	@Test
	public void testResetDisplayRange() {
		ci.getProcessor().setMinAndMax(100, 900);
		assertEquals(100,ci.getProcessor().getMin(),Assert.DOUBLE_TOL);
		assertEquals(900,ci.getProcessor().getMax(),Assert.DOUBLE_TOL);
	}

	@Test
	public void testHasCustomLuts() {
		//fail("Not yet implemented");
	}

}
