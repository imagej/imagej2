package ij;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import ij.io.FileInfo;
import ij.measure.Calibration;
import ij.process.ByteProcessor;
import ij.process.ColorProcessor;
import ij.process.DataConstants;
import ij.process.ImageProcessor;
import ij.process.LUT;

import java.awt.Color;
import java.awt.image.IndexColorModel;
import java.io.File;

import org.junit.Before;
import org.junit.Test;

public class CompositeImageTest {

	// instance vars
	CompositeImage ci;
	ImageProcessor proc;
	ImagePlus ip;
	ImageStack st;

	// helper method: needed because surprisingly bools1.equals(bools2) seems to compare references
	private boolean boolsEqual(boolean[] a, boolean[] b) {
		if (a.length != b.length)
			return false;
		for (int i = 0; i < a.length; i++)
			if (a[i] != b[i])
				return false;
		return true;
	}

	// helper method: needed because surprisingly bytes1.equals(bytes2) seems to compare references
	private boolean bytesEqual(byte[] a, byte[] b) {
		if (a.length != b.length)
			return false;
		for (int i = 0; i < a.length; i++)
			if (a[i] != b[i])
				return false;
		return true;
	}

	// helper method
	private boolean lutsEqual(LUT a, LUT b) {

		byte[] tmpA = new byte[256];
		byte[] tmpB = new byte[256];

		a.getReds(tmpA);
		b.getReds(tmpB);

		if (!bytesEqual(tmpA,tmpB))
			return false;

		a.getBlues(tmpA);
		b.getBlues(tmpB);

		if (!bytesEqual(tmpA,tmpB))
			return false;

		a.getGreens(tmpA);
		b.getGreens(tmpB);

		if (!bytesEqual(tmpA,tmpB))
			return false;

		return true;
	}

	// helper method
	private boolean lutArraysEqual(LUT[] a, LUT[] b)
	{
		if (a.length != b.length)
			return false;

		for (int i = 0; i < a.length; i++)
			if (!lutsEqual(a[i],b[i]))
				return false;

		return true;
	}

	// helper method
	private boolean colorModelsEqual(IndexColorModel cm1, IndexColorModel cm2)
	{
		if (cm1.hashCode() != cm2.hashCode())
			return false;

		byte[] bytes1 = new byte[256];
		byte[] bytes2 = new byte[256];

		cm1.getReds(bytes1);
		cm2.getReds(bytes2);
		if (!bytesEqual(bytes1,bytes2))
			return false;

		cm1.getGreens(bytes1);
		cm2.getGreens(bytes2);
		if (!bytesEqual(bytes1,bytes2))
			return false;

		cm1.getBlues(bytes1);
		cm2.getBlues(bytes2);
		if (!bytesEqual(bytes1,bytes2))
			return false;

		return true;
	}

	// helper method
	private LUT lut(int v) {
		return new LUT(ByteCreator.repeated(256,v),ByteCreator.repeated(256,v+5),ByteCreator.repeated(256,v+10));
	}

	// helper method
	private LUT[] lutCollection(int[] vals) {

		LUT[] luts = new LUT[vals.length];

		for (int i = 0; i < vals.length; i++)
			luts[i] = lut(vals[i]);

		return luts;
	}

	@Before
	public void setUp() throws Exception {
		st = new ImageStack(2,2);
		st.addSlice("Fred", new ByteProcessor(2,2,new byte[]{1,2,3,4},null));
		st.addSlice("Blane", new ByteProcessor(2,2,new byte[]{4,6,7,8},null));
		st.addSlice("Joe", new ByteProcessor(2,2,new byte[]{9,10,11,12},null));
		ip = new ImagePlus("Jones",st);
		ci = new CompositeImage(ip);
	}

	@Test
	public void testConstants() {
		assertEquals(1,CompositeImage.COMPOSITE);
		assertEquals(2,CompositeImage.COLOR);
		assertEquals(3,CompositeImage.GRAYSCALE);
		//assertEquals(4,CompositeImage.TRANSPARENT);
		assertEquals(7,CompositeImage.MAX_CHANNELS);
	}

	@Test
	public void testCompositeImageImagePlus() {

		// the default case calls the general case whihc is tested in the next method
		// only need to test that the constructor succeeded in making a COLOR image

		st = new ImageStack(2,2);
		st.addSlice("Fred", new ByteProcessor(2,2));
		st.addSlice("Blane", new ByteProcessor(2,2));
		ip = new ImagePlus("MyIp",st);
		ci = new CompositeImage(ip);
		assertNotNull(ci);
		assertEquals(CompositeImage.COLOR,ci.getMode());
	}

	@Test
	public void testCompositeImageImagePlusInt() {

		// test that mode gets set correctly

		st = new ImageStack(20,25);
		st.addSlice("Fred", new ByteProcessor(20,25));
		st.addSlice("Blane", new ByteProcessor(20,25));
		st.addSlice("Garver", new ByteProcessor(20,25));
		ip = new ImagePlus("MyIp",st);

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

		ci = new CompositeImage(ip,CompositeImage.GRAYSCALE+1);  // if TRANSPARENT added by Wayne this will have to change
		assertNotNull(ci);
		assertEquals(CompositeImage.COLOR,ci.getMode());
		assertArrayEquals(new int[] {20,25,3,1,1},ci.getDimensions());
		assertFalse(ci.getOpenAsHyperStack());

		//  if rgb and stack size != 1 should throw excep
		try {
			st = new ImageStack(4,4);
			st.addSlice("Zap",new ColorProcessor(4,4));
			st.addSlice("Frap",new ColorProcessor(4,4));
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
		assertEquals(3,ci.getStackSize());  // though this looks wrong it is correct
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
		ip = new ImagePlus("Cordelia",st);
		ci = new CompositeImage(ip,CompositeImage.COMPOSITE);
		assertNotNull(ci);
		for (int i = 0; i < CompositeImage.MAX_CHANNELS; i++)
			assertTrue(ci.getActiveChannels()[i]);
		assertArrayEquals(new int[] {2,2,4,1,1},ci.getDimensions());
		assertFalse(ci.getOpenAsHyperStack());

		// COLOR, fileInfo, calibration, channelLuts
		ip = new ImagePlus(DataConstants.DATA_DIR + "embryos.bmp");
		ci = new CompositeImage(ip,CompositeImage.COLOR);
		assertNotNull(ci);
		FileInfo fi = ci.getOriginalFileInfo();
		assertNotNull(fi);
		final File expectedDir = new File(DataConstants.DATA_DIR);
		final File actualDir = new File(fi.directory);
		assertEquals(expectedDir.getAbsolutePath(), actualDir.getAbsolutePath());
		assertEquals("embryos.bmp",fi.fileName);
		// can't test that ci's displayRanges have been set to original fileInfo's - no method for access
		assertArrayEquals(ip.getFileInfo().channelLuts,fi.channelLuts);
		assertArrayEquals(new int[] {1600,1200,3,1,1},ci.getDimensions());
		assertFalse(ci.getOpenAsHyperStack());
		// now make multiple tests that the calib copy was successful
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

		// calls more gui code

		// no code of its own to test
		// but it can affect how updateImage() runs

		// I call it from testUpdateImage() below

		// compile time check
		ci.updateChannelAndDraw();
	}

	@Test
	public void testUpdateAllChannelsAndDraw() {
		// calls more gui oriented code

		// inspection shows that updateAllChannelsAndDraw() needs to just see that the we test updateImage()
		//   with customLuts false (and thus singleChannel true) and with customLuts true

		// if mode != COMPOSITE this calls updateChannelAndDraw which we've tested elsewhere
		// if mode == COMPOSITE we need to test updateImage() with singleChannel = false and syncChannels = true
		//   In this case I call it from testUpdateImage() below

		// compile time check
		ci.updateAllChannelsAndDraw();
	}

	@Test
	public void testGetChannelProcessor() {

		// if cip null
		assertTrue(ci.getChannelProcessor().equals(ci.getProcessor()));

		// if channel == -1
		ci = new CompositeImage(ip,CompositeImage.COLOR);
		ci.setMode(CompositeImage.COMPOSITE);  // this sets channel to -1
		ci.reset();  // this populates cip
		assertTrue(ci.getChannelProcessor().equals(ci.getProcessor()));

		// otherwise
		ImageProcessor[] ips = new ImageProcessor[3];

		ci = new CompositeImage(ip,CompositeImage.COMPOSITE);
		ci.reset();  // this populates cip

		ci.setPosition(1,1,1);
		ci.updateImage();
		ips[0] = ci.getChannelProcessor();

		ci.setPosition(2,1,1);
		ci.updateImage();
		ips[1] = ci.getChannelProcessor();

		ci.setPosition(3,1,1);
		ci.updateImage();
		ips[2] = ci.getChannelProcessor();

		for (int i=0; i<ips.length; i++)
			assertNotNull(ips[i]);

		assertFalse(ips[0] == ips[1]);
		assertFalse(ips[1] == ips[2]);
		assertFalse(ips[2] == ips[0]);

		assertFalse(ips[0].equals(ci.getProcessor()));
		assertFalse(ips[1].equals(ci.getProcessor()));
		assertFalse(ips[2].equals(ci.getProcessor()));
	}

	@Test
	public void testResetDisplayRanges() {
		double min,max;
		LUT lut;

		// if lut == null do nothing
		ci = new CompositeImage(ip);
		ci.getProcessor().setMinAndMax(47, 903);
		ci.resetDisplayRanges();
		assertEquals(47,ci.getProcessor().getMin(),Assert.DOUBLE_TOL);
		assertEquals(903,ci.getProcessor().getMax(),Assert.DOUBLE_TOL);

		// if channel count != lut.length do nothing
		// can't replicate conditions
		/*
		ci = new CompositeImage(ip);
		ci.setLuts(lutCollection(new int[] {44,33,22,11})); // purposely not the right number of luts
		ci.getProcessor().setMinAndMax(47, 903);
		ci.resetDisplayRanges();
		assertEquals(47,ci.getProcessor().getMin(),Assert.DOUBLE_TOL);
		assertEquals(903,ci.getProcessor().getMax(),Assert.DOUBLE_TOL);
		*/

		// if channel count > stack size do nothing
		// can't replicate conditions

		// if channel count > MAX_CHANNELS do nothing
		st = new ImageStack(2,2);
		for (int i = 0; i < 24; i++)
			st.addSlice(""+i,new byte[] {1,2,3,4});
		ImagePlus ip2 = new ImagePlus("Fred",st);
		ip2.setDimensions(8,3,1);
		ci = new CompositeImage(ip2);
		lut = ci.getChannelLut(1);
		lut.min = 53;
		lut.max = 192;
		ci.resetDisplayRanges();
		assertEquals(53,lut.min,Assert.DOUBLE_TOL);
		assertEquals(192,lut.max,Assert.DOUBLE_TOL);

		// otherwise it should work
		ci = new CompositeImage(ip);
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

	// note - the underlying methods called here have lots of internal untestable behavior
	@Test
	public void testUpdateImage() {
		// not COMPOSITE - create image
		ci = new CompositeImage(ip,CompositeImage.COLOR);
		assertNull(ci.img);
		ci.updateImage();
		assertNotNull(ci.img);

		// Everything after here is a COMPOSITE image

		// 1 channel
		//   note - this case is logically impossible (see constructor) - can't test

		// out of synch with internal cip tracking
		st = new ImageStack(2,2);
		st.addSlice("zooch", new ByteProcessor(2,2));
		st.addSlice("pooch", new ByteProcessor(2,2));
		ip = new ImagePlus("Fred",st);
		ci = new CompositeImage(ip,CompositeImage.COMPOSITE);
		assertNull(ci.cip);
		ci.updateImage();
		assertNotNull(ci.cip);

		// new channel (also change in currslice/currframe)
		st = new ImageStack(2,2);
		for (int i = 0; i < 24; i++)
			st.addSlice(""+i,new byte[] {(byte)(i*10),2,3,4});
		ip = new ImagePlus("Foop",st);
		ip.setDimensions(2,3,4);
		ci = new CompositeImage(ip,CompositeImage.COMPOSITE);
		ci.updateImage();  // call to set current channel
		assertEquals(0,ci.getProcessor(1).get(0, 0));
		assertEquals(10,ci.getProcessor(2).get(0, 0));
		ci.setPosition(2, 1, 3);
		ci.getProcessor().setMinAndMax(13, 200);
		ci.updateImage();  // call to detect new channel case
		assertEquals(0,ci.getProcessor().getMin(),Assert.DOUBLE_TOL);
		assertEquals(255,ci.getProcessor().getMax(),Assert.DOUBLE_TOL);
		assertEquals(120,ci.getProcessor(1).get(0, 0));
		assertEquals(130,ci.getProcessor(2).get(0, 0));

		// rgbPixels == null
		//   nothing testable

		// single channel and nchannels <= 3
		st = new ImageStack(2,2);
		st.addSlice("zooch", new ByteProcessor(2,2));
		st.addSlice("pooch", new ByteProcessor(2,2));
		ip = new ImagePlus("Fred",st);
		ci = new CompositeImage(ip,CompositeImage.COMPOSITE);
		ci.reset(); // populate stuff used by getProcessor(i)
		assertEquals(0,ci.getProcessor(1).get(0,0));
		ci.getProcessor(1).set(0,0,210);
		assertEquals(210,ci.getProcessor(1).get(0,0));
		assertNull(ci.img);
		ci.updateChannelAndDraw();
		assertNotNull(ci.img);

		// not a single channel (and sync true)
		st = new ImageStack(2,2);
		st.addSlice("zooch", new ByteProcessor(2,2));
		st.addSlice("pooch", new ByteProcessor(2,2));
		ip = new ImagePlus("Fred",st);
		ci = new CompositeImage(ip,CompositeImage.COMPOSITE);
		ci.reset(); // populate stuff used by getProcessor(i)
		assertEquals(0,ci.getProcessor(1).get(0,0));
		ci.getProcessor(1).set(0,0,210);
		assertEquals(210,ci.getProcessor(1).get(0,0));
		assertNull(ci.img);
		ci.updateAllChannelsAndDraw();
		assertNotNull(ci.img);
	}

	@Test
	public void testCreateLutFromColor() {
		LUT lut;
		byte[] tmp = new byte[256];

		byte[] zeroes = ByteCreator.repeated(256,0);
		byte[] ascend = ByteCreator.ascending(256);

		lut = ci.createLutFromColor(Color.black);
		lut.getReds(tmp);
		assertTrue(bytesEqual(zeroes,tmp));
		lut.getGreens(tmp);
		assertTrue(bytesEqual(zeroes,tmp));
		lut.getBlues(tmp);
		assertTrue(bytesEqual(zeroes,tmp));

		lut = ci.createLutFromColor(Color.red);
		lut.getReds(tmp);
		assertTrue(bytesEqual(ascend,tmp));
		lut.getGreens(tmp);
		assertTrue(bytesEqual(zeroes,tmp));
		lut.getBlues(tmp);
		assertTrue(bytesEqual(zeroes,tmp));

		lut = ci.createLutFromColor(Color.green);
		lut.getReds(tmp);
		assertTrue(bytesEqual(zeroes,tmp));
		lut.getGreens(tmp);
		assertTrue(bytesEqual(ascend,tmp));
		lut.getBlues(tmp);
		assertTrue(bytesEqual(zeroes,tmp));

		lut = ci.createLutFromColor(Color.blue);
		lut.getReds(tmp);
		assertTrue(bytesEqual(zeroes,tmp));
		lut.getGreens(tmp);
		assertTrue(bytesEqual(zeroes,tmp));
		lut.getBlues(tmp);
		assertTrue(bytesEqual(ascend,tmp));
	}

	@Test
	public void testGetChannelColor() {
		// lut == null
		assertEquals(Color.black,ci.getChannelColor());

		// mode == grayscale
		ci = new CompositeImage(ip, CompositeImage.GRAYSCALE);
		ci.setLuts(lutCollection(new int[] {98,99,100}));
		assertEquals(Color.black, ci.getChannelColor());

		// no lut for the channel
		//   I don't think this case is reachable code

		// otherwise
		ci = new CompositeImage(ip, CompositeImage.COLOR);
		ci.setLuts(lutCollection(new int[] {98,99,100}));
		ci.setPosition(1,1,1);
		assertEquals(new Color(98,103,108), ci.getChannelColor());
		ci.setPosition(2,1,1);
		assertEquals(new Color(99,104,109), ci.getChannelColor());
		ci.setPosition(3,1,1);
		assertEquals(new Color(0,0,0), ci.getChannelColor());
	}

	@Test
	public void testGetProcessorInt() {

		// cip == null
		assertNull(ci.getProcessor(1));

		// channel < 1
		if (IJInfo.RUN_ENHANCED_TESTS)
		{
			ci = new CompositeImage(ip,CompositeImage.COMPOSITE);
			ci.reset();  // this populates cip
			assertNull(ci.getProcessor(0));
		}

		// channel > cip.length
		ci = new CompositeImage(ip,CompositeImage.COMPOSITE);
		ci.reset();  // this populates cip
		assertNull(ci.getProcessor(4));

		// otherwise
		ci = new CompositeImage(ip,CompositeImage.COMPOSITE);
		ci.reset();  // this populates cip
		assertNotNull(ci.getProcessor(1));
		assertNotNull(ci.getProcessor(2));
		assertNotNull(ci.getProcessor(3));
	}

	@Test
	public void testGetActiveChannels() {
		// nothing really to test so put in a compile time check
		ci.getActiveChannels();
	}

	@Test
	public void testSetMode() {

		ImagePlus ip2;
		boolean[] bools;

		// if mode out of legal range do nothing
		ci = new CompositeImage(ip,CompositeImage.GRAYSCALE);
		ci.setMode(0);
		ci.setMode(4);
		assertEquals(CompositeImage.GRAYSCALE,ci.getMode());

		// if going to mode composite and channels > MAX_CHANNELS go as COLOR
		st = new ImageStack(2,2);
		for (int i = 0; i < 32; i++)
			st.addSlice(""+i,new byte[]{1,2,3,4});
		ip2 = new ImagePlus("Spike",st);
		ip2.setDimensions(8,2,2);
		ci = new CompositeImage(ip2,CompositeImage.GRAYSCALE);
		ci.setMode(CompositeImage.COMPOSITE);
		assertEquals(CompositeImage.COLOR,ci.getMode());
		bools = ci.getActiveChannels();
		for (int i = 0; i < bools.length; i++)
			assertTrue(bools[i]);

		// if changing to COMPOSITE from something else destroy existing img
		// note - can't test this side effect as accessors have side effects that always create a replacement image

		// if mode going to GRAYSCALE free up channel data if non null
		st = new ImageStack(2,2);
		for (int i = 0; i < 4; i++)
			st.addSlice(""+i,new byte[]{1,2,3,4});
		ip2 = new ImagePlus("Spike",st);
		ci = new CompositeImage(ip2,CompositeImage.COMPOSITE);
		ci.reset();  // make sure channel processors are setup
		ci.setMode(CompositeImage.GRAYSCALE);
		assertNull(ci.getProcessor(1));

		// if mode going to COLOR free up channel data if non null
		st = new ImageStack(2,2);
		for (int i = 0; i < 4; i++)
			st.addSlice(""+i,new byte[]{1,2,3,4});
		ip2 = new ImagePlus("Spike",st);
		ci = new CompositeImage(ip2,CompositeImage.COMPOSITE);
		ci.reset();  // make sure channel processors are setup
		ci.setMode(CompositeImage.COLOR);
		assertNull(ci.getProcessor(1));

		// if mode going to GRAYSCALE set the color model to the internal proc's default color model
		byte[] reds = ByteCreator.ascending(256);
		byte[] greens = ByteCreator.descending(256);
		byte[] blues = ByteCreator.repeated(256,51);
		IndexColorModel cm = new IndexColorModel(8, 256, reds, greens, blues);
		st = new ImageStack(2,2);
		for (int i = 0; i < 4; i++)
			st.addSlice(""+i,new byte[]{1,2,3,4});
		ip2 = new ImagePlus("Spike",st);
		ci = new CompositeImage(ip2,CompositeImage.COLOR);
		ci.getProcessor().setColorModel(cm);
		assertFalse(colorModelsEqual(ci.getProcessor().getDefaultColorModel(),
				(IndexColorModel)ci.getProcessor().getColorModel()));
		ci.setMode(CompositeImage.GRAYSCALE);
		assertTrue(colorModelsEqual(ci.getProcessor().getDefaultColorModel(),
				(IndexColorModel)ci.getProcessor().getColorModel()));

		// some gui oriented code (updating Channels()) not tested
	}

	@Test
	public void testGetMode() {

		ci = new CompositeImage(ip,CompositeImage.COLOR);
		assertEquals(CompositeImage.COLOR,ci.getMode());

		ci = new CompositeImage(ip,CompositeImage.GRAYSCALE);
		assertEquals(CompositeImage.GRAYSCALE,ci.getMode());

		ci = new CompositeImage(ip,CompositeImage.COMPOSITE);
		assertEquals(CompositeImage.COMPOSITE,ci.getMode());
	}

	@Test
	public void testGetModeAsString() {

		ci = new CompositeImage(ip,CompositeImage.COLOR);
		assertEquals("color",ci.getModeAsString());

		ci = new CompositeImage(ip,CompositeImage.GRAYSCALE);
		assertEquals("grayscale",ci.getModeAsString());

		ci = new CompositeImage(ip,CompositeImage.COMPOSITE);
		assertEquals("composite",ci.getModeAsString());
	}

	// this method tests both versions of getChannelLut()
	@Test
	public void testGetChannelLut() {
		LUT[] luts = new LUT[3];
		LUT lut;

		ci.setPosition(1,1,1);
		assertEquals(1,ci.getChannel());
		luts[0] = ci.getChannelLut();

		ci.setPosition(2,1,1);
		assertEquals(2,ci.getChannel());
		luts[1] = ci.getChannelLut();

		ci.setPosition(3,1,1);
		assertEquals(3,ci.getChannel());
		luts[2] = ci.getChannelLut();

		// make sure we don't have the same lut over and over
		assertFalse(lutsEqual(luts[0],luts[1]) && lutsEqual(luts[1],luts[2]) && lutsEqual(luts[2],luts[0]));

		assertTrue(lutsEqual(luts[0],ci.getChannelLut(1)));
		assertTrue(lutsEqual(luts[1],ci.getChannelLut(2)));
		assertTrue(lutsEqual(luts[2],ci.getChannelLut(3)));
	}

	@Test
	public void testGetLuts() {
		LUT[] ciLuts, tmp;

		// luts null - creates them and passes back
		ciLuts = ci.getLuts();
		assertEquals(3,ciLuts.length);

		// otherwise passes back cloned luts
		ciLuts = lutCollection(new int[] {7,44,15});
		ci.setLuts(ciLuts);
		tmp = ci.getLuts();
		assertTrue(lutArraysEqual(ciLuts,tmp));
	}

	@Test
	public void testSetLuts() {
		LUT[] ciLuts;

		st = new ImageStack(2,2);
		st.addSlice("zooch", new ByteProcessor(2,2));
		st.addSlice("pooch", new ByteProcessor(2,2));
		st.addSlice("booch", new ByteProcessor(2,2));
		ip = new ImagePlus("Zorba",st);
		ci = new CompositeImage(ip);

		ciLuts = lutCollection(new int[] {4,1,44});
		ci.setLuts(ciLuts);
		assertTrue(lutsEqual(ciLuts[0],ci.getChannelLut(1)));
		assertTrue(lutsEqual(ciLuts[1],ci.getChannelLut(2)));
		assertTrue(lutsEqual(ciLuts[2],ci.getChannelLut(3)));
	}

	@Test
	public void testCopyLuts() {

		LUT[] cLuts = lutCollection(new int[] {10,55,14});
		LUT[] ciLuts = lutCollection(new int[] {1,2,3});
		LUT[] laterLuts;

		CompositeImage c;
		ImagePlus cip;

		// try with non composite image
		cip = new ImagePlus("ZappyDo",new ByteProcessor(3,2));
		ci.setLuts(ciLuts);
		ci.copyLuts(cip);
		laterLuts = ci.getLuts();
		assertArrayEquals(ciLuts,laterLuts);

		// try with composite image channels != ci's channels
		st = new ImageStack(2,2);
		st.addSlice("a",new byte[] {1,2,3,4});
		st.addSlice("b",new byte[] {1,2,3,4});
		cip = new ImagePlus("Zooropa",st);
		c = new CompositeImage(cip);
		ci.setLuts(cLuts);
		ci.copyLuts(c);
		laterLuts = ci.getLuts();
		assertArrayEquals(cLuts,laterLuts);

		// try with compatible CompositeImages
		st = new ImageStack(2,2);
		st.addSlice("a",new byte[] {1,2,3,4});
		st.addSlice("b",new byte[] {1,2,3,4});
		st.addSlice("c",new byte[] {1,2,3,4});
		cip = new ImagePlus("Zooropa",st);
		c = new CompositeImage(cip);
		c.setLuts(cLuts);
		ci.setLuts(ciLuts);
		ci.copyLuts(c);
		laterLuts = ci.getLuts();
		assertFalse(lutArraysEqual(ciLuts,laterLuts));
		assertTrue(lutArraysEqual(cLuts,laterLuts));

		// see that mode changes to c's mode
		st = new ImageStack(2,2);
		st.addSlice("a",new byte[] {1,2,3,4});
		st.addSlice("b",new byte[] {1,2,3,4});
		st.addSlice("c",new byte[] {1,2,3,4});
		cip = new ImagePlus("Zooropa",st);
		c = new CompositeImage(cip,CompositeImage.GRAYSCALE);
		c.setLuts(cLuts);
		ci.setLuts(ciLuts);
		assertFalse(ci.getMode() == c.getMode());
		ci.copyLuts(c);
		assertTrue(ci.getMode() == c.getMode());

		// in the case of a COMPOSITE image see that activated field gets copied too
		//   can't find a way to test this.
		/*
		st = new ImageStack(2,2);
		st.addSlice("a",new byte[] {1,2,3,4});
		st.addSlice("b",new byte[] {1,2,3,4});
		st.addSlice("c",new byte[] {1,2,3,4});
		cip = new ImagePlus("Zooropa",st);
		c = new CompositeImage(cip,CompositeImage.COMPOSITE);
		c.setLuts(cLuts);
		ci.setLuts(ciLuts);
		assertFalse(boolsSame(ci.getActiveChannels(),c.getActiveChannels()));  // always seems to be true rather than false
		ci.copyLuts(c);
		assertTrue(boolsSame(ci.getActiveChannels(),c.getActiveChannels()));
		*/
	}

	@Test
	public void testReset() {

		// COMPOSITE images have some special case code
		LUT[] luts = lutCollection(new int[] {4,19,21});

		luts[0].min = -4000;
		luts[0].max = 9000;

		ci = new CompositeImage(ip,CompositeImage.COMPOSITE);
		ci.setLuts(luts);
		assertNull(ci.getProcessor(1));
		ci.reset();
		assertNotNull(ci.getProcessor(1));
		assertEquals(-4000, ci.getProcessor(1).getMin(), Assert.DOUBLE_TOL);
		assertEquals(9000, ci.getProcessor(1).getMax(), Assert.DOUBLE_TOL);

		// COLOR images do not have special case code
		ci = new CompositeImage(ip,CompositeImage.COLOR);
		assertNull(ci.getProcessor(1));
		ci.reset();
		assertNull(ci.getProcessor(1));

		// GRAYSCALE images do not have special case code
		ci = new CompositeImage(ip,CompositeImage.GRAYSCALE);
		assertNull(ci.getProcessor(1));
		ci.reset();
		assertNull(ci.getProcessor(1));

		// in any case test that luts get created by call to reset
		ci = new CompositeImage(ip,CompositeImage.GRAYSCALE);
		// internal lut table should be null at this point but there is no safe way to test that w/o side effects
		ci.reset();
		// note - reset() called private methods for lut setup that have a lot of special cases. I'm just testing existence.
		assertNotNull(ci.getChannelLut(1));
		assertNotNull(ci.getChannelLut(2));
		assertNotNull(ci.getChannelLut(3));
	}

	@Test
	public void testSetChannelLutLUT() {

		LUT lut = lut(23);

		// default ci I construct has 3 channels

		// try to set beyond LUT array for weird index
		try {
			ci = new CompositeImage(ip);
			ci.setChannelLut(lut,-1);
			fail();
		} catch (IllegalArgumentException e) {
			assertTrue(true);
		}

		// try to set beyond an end of LUT array at 0
		try {
			ci = new CompositeImage(ip);
			ci.setChannelLut(lut,0);
			fail();
		} catch (IllegalArgumentException e) {
			assertTrue(true);
		}

		// try to set beyond an end of LUT array at 4
		try {
			ci = new CompositeImage(ip);
			ci.setChannelLut(lut,4);
			fail();
		} catch (IllegalArgumentException e) {
			assertTrue(true);
		}

		// try channel 1
		ci = new CompositeImage(ip);
		assertFalse(lutsEqual(lut,ci.getChannelLut(1)));
		ci.setChannelLut(lut,1);
		assertTrue(lutsEqual(lut,ci.getChannelLut(1)));

		// try channel 2
		ci = new CompositeImage(ip);
		assertFalse(lutsEqual(lut,ci.getChannelLut(2)));
		ci.setChannelLut(lut,2);
		assertTrue(lutsEqual(lut,ci.getChannelLut(2)));

		// try channel 3
		ci = new CompositeImage(ip);
		assertFalse(lutsEqual(lut,ci.getChannelLut(3)));
		ci.setChannelLut(lut,3);
		assertTrue(lutsEqual(lut,ci.getChannelLut(3)));

		// show that setChanLut changes behavior related to instance var cip
		st = new ImageStack(2,2);
		st.addSlice("a",new byte[] {1,2,3,4});
		st.addSlice("b",new byte[] {1,2,3,4});
		ip = new ImagePlus("Sara",st);
		ci = new CompositeImage(ip,CompositeImage.COMPOSITE);
		ci.reset();  // sets up internal cip vars
		assertNotNull(ci.getProcessor(1));
		ci.setChannelLut(lut,1);
		assertNull(ci.getProcessor(1));
	}

	@Test
	public void testSetChannelLutLUTInt() {

		// create lut channel data
		LUT lut = lut(55);

		ci.setChannelLut(lut,2);

		LUT actual = ci.getChannelLut(2);

		assertEquals(lut,actual);
	}

	@Test
	public void testSetChannelColorModel() {

		// create lut channel data
		byte[] reds = ByteCreator.ascending(256);
		byte[] greens = ByteCreator.descending(256);
		byte[] blues = ByteCreator.repeated(256,51);

		// make a color model
		IndexColorModel cm = new IndexColorModel(8, 256, reds, greens, blues);

		// call the method
		ci.setChannelColorModel(cm);

		// see if there was a lut created with the correct values

		LUT lut = ci.getChannelLut();
		byte[] tmp = new byte[256];

		lut.getReds(tmp);
		assertArrayEquals(reds,tmp);

		lut.getGreens(tmp);
		assertArrayEquals(greens,tmp);

		lut.getBlues(tmp);
		assertArrayEquals(blues,tmp);
	}


	@Test
	public void testSetDisplayRangeDoubleDouble() {

		ci.setDisplayRange(100, 900);

		// make sure max and min set
		double actMin = ci.getDisplayRangeMin();
		double actMax = ci.getDisplayRangeMax();
		assertEquals(100,actMin,Assert.DOUBLE_TOL);
		assertEquals(900,actMax,Assert.DOUBLE_TOL);
	}

	@Test
	public void testGetDisplayRangeMinAndMax() {

		ci.setDisplayRange(100, 900);

		// make sure old invariant holds
		int channel = ci.getChannelIndex();
		assertEquals(ci.getLuts()[channel].min, ci.getDisplayRangeMin(), Assert.DOUBLE_TOL);
		assertEquals(ci.getLuts()[channel].max, ci.getDisplayRangeMax(), Assert.DOUBLE_TOL);
	}

	@Test
	public void testResetDisplayRange() {

		// setup lut mins and maxes to be non default
		LUT lut = ci.getChannelLut(1);
		lut.min = 494;
		lut.max = 88888;
		assertEquals(494, ci.getDisplayRangeMin(), Assert.DOUBLE_TOL);
		assertEquals(88888, ci.getDisplayRangeMax(), Assert.DOUBLE_TOL);

		// setup proc mins and maxes to be non default
		ci.getProcessor().setMinAndMax(100, 900);
		assertEquals(100, ci.getProcessor().getMin(), Assert.DOUBLE_TOL);
		assertEquals(900, ci.getProcessor().getMax(), Assert.DOUBLE_TOL);

		// reset the display range
		ci.resetDisplayRange();

		// see that they are put back to sane values
		assertEquals(0, ci.getProcessor().getMin(), Assert.DOUBLE_TOL);
		assertEquals(255, ci.getProcessor().getMax(), Assert.DOUBLE_TOL);
		assertEquals(0, ci.getDisplayRangeMin(), Assert.DOUBLE_TOL);
		assertEquals(255, ci.getDisplayRangeMax(), Assert.DOUBLE_TOL);
	}

	@Test
	public void testHasCustomLuts() {

		LUT lut = lut(74);

		st = new ImageStack(4,4);
		st.addSlice("blub", new ByteProcessor(4,4));
		st.addSlice("scub", new ByteProcessor(4,4));
		ip = new ImagePlus("Fred",st);

		//  customLut true and color == GRAYSCALE
		ci = new CompositeImage(ip,CompositeImage.GRAYSCALE);
		ci.setChannelLut(lut);
		assertFalse(ci.hasCustomLuts());

		//  customLut true and color != GRAYSCALE
		ci = new CompositeImage(ip,CompositeImage.COLOR);
		ci.setChannelLut(lut);
		assertTrue(ci.hasCustomLuts());

		//  customLut false and color == GRAYSCALE
		ci = new CompositeImage(ip,CompositeImage.GRAYSCALE);
		assertFalse(ci.hasCustomLuts());

		//  customLut false and color != GRAYSCALE
		ci = new CompositeImage(ip,CompositeImage.COLOR);
		assertFalse(ci.hasCustomLuts());
	}

}
