package ij.measure;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import ij.Assert;
import ij.ImagePlus;
import ij.plugin.filter.Analyzer;
import ij.gui.NewImage;

public class CalibrationTest {

	Calibration c;
	
	@Before
	public void setUp() throws Exception {
		c = new Calibration();
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testAllPublicConstants() {

		// make sure all constants exist with correct values

		assertEquals(0,Calibration.STRAIGHT_LINE);
		assertEquals(1,Calibration.POLY2);
		assertEquals(2,Calibration.POLY3);
		assertEquals(3,Calibration.POLY4);
		assertEquals(4,Calibration.EXPONENTIAL);
		assertEquals(5,Calibration.POWER);
		assertEquals(6,Calibration.LOG);
		assertEquals(7,Calibration.RODBARD);
		assertEquals(8,Calibration.GAMMA_VARIATE);
		assertEquals(9,Calibration.LOG2);
		assertEquals(10,Calibration.RODBARD2);
		assertEquals(20,Calibration.NONE);
		assertEquals(21,Calibration.UNCALIBRATED_OD);
		assertEquals(22,Calibration.CUSTOM);
	}
	
	@Test
	public void testAllPublicFields() {
		
		// make sure all public fields exist and can be assigned to
		// mainly a compile time check

		c.pixelWidth = 14.2;
		c.pixelHeight = -7.6;
		c.pixelDepth = 101.4;
		c.frameInterval = 99.9;
		c.fps = 43.7;
		c.loop = true;
		c.xOrigin = 1005.9;
		c.yOrigin = 400.3;
		c.zOrigin = 77.1;
		c.info = "Splappppp";
	}
	
	@Test
	public void testCalibration() {

		// make sure default construction has correct values
		
		// first test the public values
		
		assertEquals(1.0,c.pixelWidth,Assert.DOUBLE_TOL);
		assertEquals(1.0,c.pixelHeight,Assert.DOUBLE_TOL);
		assertEquals(1.0,c.pixelDepth,Assert.DOUBLE_TOL);
		assertEquals(0.0,c.frameInterval,Assert.DOUBLE_TOL);
		assertEquals(0.0,c.fps,Assert.DOUBLE_TOL);
		assertFalse(c.loop);
		assertEquals(0.0,c.xOrigin,Assert.DOUBLE_TOL);
		assertEquals(0.0,c.yOrigin,Assert.DOUBLE_TOL);
		assertEquals(0.0,c.zOrigin,Assert.DOUBLE_TOL);
		assertNull(c.info);
		
		// test the private data as possible using accessors
		
		assertNull(c.getCoefficients());
		assertEquals("pixel",c.getUnit());
		assertEquals("pixel",c.getXUnit());
		assertEquals("pixel",c.getYUnit());
		assertEquals("pixel",c.getZUnit());
		// no safe accessor exists for units
		assertEquals("Gray Value",c.getValueUnit());
		assertEquals("sec",c.getTimeUnit());
		assertEquals(Calibration.NONE,c.getFunction());
		assertNull(c.getCTable());
		// no accessor exists for invertedLut
		// no accessor exists for bitDepth
		assertFalse(c.zeroClip());
		// no accessor exists for invertY
	}

	@Test
	public void testCalibrationImagePlus() {

		// no need to test this case. The prev method does all the testing possible for this case
		//   since this constructor only can set invertedLut and bitDepth which are not accessible.

		// do put in some code to make sure this constructor exists in any refactor
		ImagePlus imp = new ImagePlus();
		Calibration c = new Calibration(imp);
	}

	@Test
	public void testScaled() {
		
		c = new Calibration();
		assertFalse(c.scaled());
		
		c = new Calibration();
		c.pixelWidth = 2;
		assertTrue(c.scaled());
		
		c = new Calibration();
		c.pixelHeight = 0.1;
		assertTrue(c.scaled());
		
		c = new Calibration();
		c.pixelDepth = 79.5;
		assertTrue(c.scaled());

		c = new Calibration();
		c.setUnit("cm");
		assertTrue(c.scaled());
		
		c = new Calibration();
		c.setUnit("megaslams");
		assertTrue(c.scaled());
	}

	@Test
	public void testSetAndGetUnit() {

		c.setUnit(null);
		assertEquals("pixel",c.getUnit());

		c.setUnit("");
		assertEquals("pixel",c.getUnit());

		c.setUnit("um");
		assertEquals("\u00B5m",c.getUnit());

		c.setUnit("megoframs");
		assertEquals("megoframs",c.getUnit());
		
		// NOTE: the private instance var "units" is changed by these calls but we can't track it ...
	}

	@Test
	public void testSetAndGetXUnit() {

		c.setXUnit(null);
		assertEquals("pixel",c.getXUnit());

		c.setXUnit("angstrom");
		assertEquals("angstrom",c.getXUnit());

		c.setXUnit("cayahugas");
		assertEquals("cayahugas",c.getXUnit());
	}

	@Test
	public void testSetAndGetYUnit() {

		c.setYUnit(null);
		assertEquals("pixel",c.getYUnit());

		c.setYUnit("angstrom");
		assertEquals("angstrom",c.getYUnit());

		c.setYUnit("cayahugas");
		assertEquals("cayahugas",c.getYUnit());
	}

	@Test
	public void testSetAndGetZUnit() {

		c.setZUnit(null);
		assertEquals("pixel",c.getZUnit());

		c.setZUnit("angstrom");
		assertEquals("angstrom",c.getZUnit());

		c.setZUnit("cayahugas");
		assertEquals("cayahugas",c.getZUnit());
	}

	@Test
	public void testGetUnits() {

		assertEquals("pixels",c.getUnits());
		
		c = new Calibration();
		c.setUnit("pixel");
		assertEquals("pixels",c.getUnits());
		
		c.setUnit("micron");
		assertEquals("microns",c.getUnits());

		c.setUnit("inch");
		assertEquals("inches",c.getUnits());

		c.setUnit("abrahams");
		assertEquals("abrahams",c.getUnits());
	}

	@Test
	public void testSetAndGetTimeUnit() {
		c.setTimeUnit(null);
		assertEquals("sec",c.getTimeUnit());

		c.setTimeUnit("");
		assertEquals("sec",c.getTimeUnit());

		c.setTimeUnit("sec");
		assertEquals("sec",c.getTimeUnit());

		c.setTimeUnit("eons");
		assertEquals("eons",c.getTimeUnit());

	}

	@Test
	public void testGetX() {

		c.xOrigin = 0.0;
		c.pixelWidth = 1.0;
		assertEquals(4.0,c.getX(4.0),Assert.DOUBLE_TOL);

		c.xOrigin = 5.0;
		c.pixelWidth = 4.0;
		assertEquals(20.0,c.getX(10.0),Assert.DOUBLE_TOL);

		c.xOrigin = -5.0;
		c.pixelWidth = 10.0;
		assertEquals(150.0,c.getX(10.0),Assert.DOUBLE_TOL);
	}

	@Test
	public void testGetY() {

		c.yOrigin = 0.0;
		c.pixelHeight = 1.0;
		assertEquals(4.0,c.getY(4.0),Assert.DOUBLE_TOL);

		c.yOrigin = 5.0;
		c.pixelHeight = 4.0;
		assertEquals(20.0,c.getY(10.0),Assert.DOUBLE_TOL);

		c.yOrigin = -5.0;
		c.pixelHeight = 10.0;
		assertEquals(150.0,c.getY(10.0),Assert.DOUBLE_TOL);
	}

	@Test
	public void testGetZ() {

		c.zOrigin = 0.0;
		c.pixelDepth = 1.0;
		assertEquals(4.0,c.getZ(4.0),Assert.DOUBLE_TOL);

		c.zOrigin = 5.0;
		c.pixelDepth = 4.0;
		assertEquals(20.0,c.getZ(10.0),Assert.DOUBLE_TOL);

		c.zOrigin = -5.0;
		c.pixelDepth = 10.0;
		assertEquals(150.0,c.getZ(10.0),Assert.DOUBLE_TOL);
	}

	@Test
	public void testGetYDoubleInt() {
		
		double y;
		int imageHeight;
		
		// general case
		c = new Calibration();
		y = 75.0;
		imageHeight = 400;
		c.yOrigin = 50.0;
		c.pixelHeight = 3;
		c.setInvertY(false);
		assertEquals(((y-c.yOrigin)*c.pixelHeight),c.getY(y,imageHeight),Assert.DOUBLE_TOL);

		// invertY specified true
		c = new Calibration();
		y = 75.0;
		imageHeight = 400;
		c.yOrigin = 50.0;
		c.pixelHeight = 3;
		c.setInvertY(true);
		assertEquals(((c.yOrigin-y)*c.pixelHeight),c.getY(y,imageHeight),Assert.DOUBLE_TOL);

		// invertY specified true and yOrigin == 0.0
		c = new Calibration();
		y = 75.0;
		imageHeight = 400;
		c.yOrigin = 0.0;
		c.pixelHeight = 3;
		c.setInvertY(true);
		assertEquals(((imageHeight-y-1)*c.pixelHeight),c.getY(y,imageHeight),Assert.DOUBLE_TOL);

		// for next two cases set needed flag
		
		int measurements = Analyzer.getMeasurements();
		measurements |= Analyzer.INVERT_Y;
		Analyzer.setMeasurements(measurements);

		// invertY specified false but Analyzer::INVERT_Y true
		c = new Calibration();
		y = 75.0;
		imageHeight = 400;
		c.yOrigin = 10.0;
		c.pixelHeight = 3;
		c.setInvertY(false);
		assertEquals(((c.yOrigin-y)*c.pixelHeight),c.getY(y,imageHeight),Assert.DOUBLE_TOL);

		// invertY specified false but Analyzer::INVERT_Y true and origin = 0.0
		c = new Calibration();
		y = 75.0;
		imageHeight = 400;
		c.yOrigin = 0.0;
		c.pixelHeight = 3;
		c.setInvertY(false);
		assertEquals(((imageHeight-y-1)*c.pixelHeight),c.getY(y,imageHeight),Assert.DOUBLE_TOL);
	}

	// this method defined because of uncertain furute of Calibration::eqausl(Calibration c)
	
	private void assertCalibsSame(Calibration expected, Calibration actual)
	{
		// if we can change Calibration::equals() to take an Object or if we write our own
		// Calibration::eqausl(Obejct) then do this code ...

		//assertEquals(expected, actual);

		// else I'll need to write a bunch of methods to test public variables and methods

		assertTrue(actual.isSameAs(expected));
	}
	
	@Test
	public void testSetFunctionIntDoubleArrayStringBoolean() {
		
		// test two general combo cases
		double[] coefficients = new double [] {88.4};
		
		// case 1 - unit == null
		c = new Calibration();
		c.setFunction(Calibration.LOG, coefficients, null, true);
		assertEquals(Calibration.LOG,c.getFunction());
		assertEquals(coefficients,c.getCoefficients());
		assertEquals("Gray Value",c.getValueUnit());
		assertEquals(true,c.zeroClip());
		//assertNull(c.getCTable()); // when function not NONE this method has side effects - can't test ctable val
		
		// case 2 - unit != null
		c = new Calibration();
		c.setFunction(Calibration.GAMMA_VARIATE, coefficients, "chains", true);
		assertEquals(Calibration.GAMMA_VARIATE,c.getFunction());
		assertEquals(coefficients,c.getCoefficients());
		assertEquals("chains",c.getValueUnit());
		assertEquals(true,c.zeroClip());
		//assertNull(c.getCTable()); // when function not NONE this method has side effects - can't test ctable val

		// now test special cases

		Calibration savedC;
		
		// this should disableDensityCalibration and otherwise leave c unchanged
		c = new Calibration();
		c.setFunction(Calibration.NONE, new double[] {}, "zuds", false);
		assertEquals(Calibration.NONE,c.getFunction());
		assertEquals(null,c.getCoefficients());
		assertEquals(null,c.getCTable());
		assertEquals("Gray Value",c.getValueUnit());
		// won't test that all other fields are the same
		
		// this combo should do nothing since coefficients are null
		c = new Calibration();
		savedC = (Calibration)c.clone();
		c.setFunction(Calibration.STRAIGHT_LINE, null, null, false);
		assertCalibsSame(savedC,c);

		// this combo should do nothing since coefficients are null
		c = new Calibration();
		savedC = (Calibration)c.clone();
		c.setFunction(Calibration.POLY2, null, null, false);
		assertCalibsSame(savedC,c);

		// this combo should do nothing since coefficients are null
		c = new Calibration();
		savedC = (Calibration)c.clone();
		c.setFunction(Calibration.POLY3, null, null, false);
		assertCalibsSame(savedC,c);

		// this combo should do nothing since coefficients are null
		c = new Calibration();
		savedC = (Calibration)c.clone();
		c.setFunction(Calibration.POLY4, null, null, false);
		assertCalibsSame(savedC,c);

		// this combo should do nothing since coefficients are null
		c = new Calibration();
		savedC = (Calibration)c.clone();
		c.setFunction(Calibration.EXPONENTIAL, null, null, false);
		assertCalibsSame(savedC,c);

		// this combo should do nothing since coefficients are null
		c = new Calibration();
		savedC = (Calibration)c.clone();
		c.setFunction(Calibration.POWER, null, null, false);
		assertCalibsSame(savedC,c);

		// this combo should do nothing since coefficients are null
		c = new Calibration();
		savedC = (Calibration)c.clone();
		c.setFunction(Calibration.LOG, null, null, false);
		assertCalibsSame(savedC,c);

		// this combo should do nothing since coefficients are null
		c = new Calibration();
		savedC = (Calibration)c.clone();
		c.setFunction(Calibration.RODBARD, null, null, false);
		assertCalibsSame(savedC,c);

		// this combo should do nothing since coefficients are null
		c = new Calibration();
		savedC = (Calibration)c.clone();
		c.setFunction(Calibration.GAMMA_VARIATE, null, null, false);
		assertCalibsSame(savedC,c);

		// this combo should do nothing since coefficients are null
		c = new Calibration();
		savedC = (Calibration)c.clone();
		c.setFunction(Calibration.LOG2, null, null, false);
		assertCalibsSame(savedC,c);

		// this combo should do nothing since coefficients are null
		c = new Calibration();
		savedC = (Calibration)c.clone();
		c.setFunction(Calibration.RODBARD2, null, null, false);
		assertCalibsSame(savedC,c);
		
		// this combo sets some instance vars even when coefficients are null 
		c = new Calibration();
		c.setFunction(Calibration.UNCALIBRATED_OD, null, null, false);
		assertEquals(Calibration.UNCALIBRATED_OD,c.getFunction());
		assertEquals(null,c.getCoefficients());
		// assertEquals(null,c.getCTable()); : can't call this - side effects
		assertFalse(c.zeroClip());
		assertEquals("Gray Value",c.getValueUnit());

		// this combo sets some instance vars even when coefficients are null 
		c = new Calibration();
		c.setFunction(Calibration.CUSTOM, null, null, false);
		assertEquals(Calibration.CUSTOM,c.getFunction());
		assertEquals(null,c.getCoefficients());
		// assertEquals(null,c.getCTable()); : can't call this - side effects
		assertFalse(c.zeroClip());
		assertEquals("Gray Value",c.getValueUnit());
	}

	@Test
	public void testSetFunctionIntDoubleArrayString() {
		// general case test in prev method did the heavy lifting
		// now just need to test that zeroClip set correctly
		c.setFunction(Calibration.LOG, new double[] {}, "km", true);
		assertTrue(c.zeroClip());
		c.setFunction(Calibration.LOG, new double[] {}, "km");
		assertFalse(c.zeroClip());
	}

	@Test
	public void testSetImage() {
		Calibration cSaved;
		ImagePlus imp;
		double[] coeffs;
		
		// test null case
		c = new Calibration();
		cSaved = (Calibration) c.clone();
		c.setImage(null);
		assertCalibsSame(cSaved,c);
		
		// test imp.depth == 16 && imp.calib == signed 16 bit
			// sixteen bit
		imp = NewImage.createShortImage("MyFakeImage", 200, 150, 1, 0);
			// and calib isSigned16Bit()
		coeffs = new double[] {-32768.0,1.0};
		c = new Calibration(imp);
		c.setFunction(Calibration.STRAIGHT_LINE, coeffs, null);
			// run the method we're testing
		c.setImage(imp);
		assertEquals(Calibration.STRAIGHT_LINE,c.getFunction());
		Assert.assertDoubleArraysEqual(coeffs, c.getCoefficients(),Assert.DOUBLE_TOL);
		assertEquals("Gray Value",c.getValueUnit());
			// can't test bit depth as its private with no accessor
		
		// imp.depth == 16 && imp.calib != signed16 bit
		imp = NewImage.createRGBImage("MyFakeImage", 200, 150, 1, 0);
		c = new Calibration(imp);
		// run the method we're testing
		c.setImage(imp);

		// imp.depth != 16 case 1 - rgb
		imp = NewImage.createRGBImage("MyFakeImage", 200, 150, 1, 0);
		c = new Calibration(imp);
		// run the method we're testing
		c.setImage(imp);
		// this should have disabled stuff
		assertEquals(Calibration.NONE,c.getFunction());
		assertEquals(null,c.getCoefficients());
		assertEquals(null,c.getCTable());
		assertEquals("Gray Value",c.getValueUnit());

		// imp.depth != 16 case 2 - gray32
		imp = NewImage.createFloatImage("MyFakeImage", 200, 150, 1, 0);
		c = new Calibration(imp);
		c.setValueUnit("SpiffyTop");
		// run the method we're testing
		c.setImage(imp);
		// this should have disabled stuff
		assertEquals(Calibration.NONE,c.getFunction());
		assertEquals(null,c.getCoefficients());
		assertEquals(null,c.getCTable());
		assertEquals("SpiffyTop",c.getValueUnit());
		
		// imp.depth != 16 case 3 - newBitDepth != bitDepth
		c = new Calibration();  // bitdepth of 8
		imp = NewImage.createShortImage("MyFakeImage", 200, 150, 1, 0);
		// run the method we're testing
		c.setImage(imp);
		// this should have disabled stuff
		assertEquals(Calibration.NONE,c.getFunction());
		assertEquals(null,c.getCoefficients());
		assertEquals(null,c.getCTable());
		assertEquals("Gray Value",c.getValueUnit());
 	}

	@Test
	public void testDisableDensityCalibration() {
		c.setFunction(Calibration.GAMMA_VARIATE, new double[] { 99 }, "picometers");
		c.disableDensityCalibration();
		assertEquals(Calibration.NONE,c.getFunction());
		assertNull(c.getCoefficients());
		assertNull(c.getCTable());
		assertEquals("Gray Value",c.getValueUnit());
	}

	@Test
	public void testSetAndGetValueUnit() {
		c.setValueUnit(null);
		assertEquals("Gray Value",c.getValueUnit());
		c.setValueUnit("");
		assertEquals("",c.getValueUnit());
		c.setValueUnit("hippityHops");
		assertEquals("hippityHops",c.getValueUnit());
	}

	@Test
	public void testGetCoefficients() {
		assertNull(c.getCoefficients());
		
		double[] coeffs = new double[] { 6,5,4,3,2,1 };

		c.setFunction(Calibration.LOG, coeffs, "anythingUnderTheSun");
		assertEquals(coeffs,c.getCoefficients());
	}

	@Test
	public void testCalibrated() {

		assertFalse(c.calibrated());
		
		c.setFunction(Calibration.NONE, new double[] { 1,2,3,4,5,6,7,8,9,10 }, "grand poobahs");
		assertFalse(c.calibrated());
		
		c.setFunction(Calibration.STRAIGHT_LINE, new double[] { 1,2,3,4,5,6,7,8,9,10 }, "grand poobahs");
		assertTrue(c.calibrated());
		
		c.setFunction(Calibration.POLY2, new double[] { 1,2,3,4,5,6,7,8,9,10 }, "grand poobahs");
		assertTrue(c.calibrated());
		
		c.setFunction(Calibration.POLY3, new double[] { 1,2,3,4,5,6,7,8,9,10 }, "grand poobahs");
		assertTrue(c.calibrated());
		
		c.setFunction(Calibration.POLY4, new double[] { 1,2,3,4,5,6,7,8,9,10 }, "grand poobahs");
		assertTrue(c.calibrated());
		
		c.setFunction(Calibration.EXPONENTIAL, new double[] { 1,2,3,4,5,6,7,8,9,10 }, "grand poobahs");
		assertTrue(c.calibrated());
		
		c.setFunction(Calibration.POWER, new double[] { 1,2,3,4,5,6,7,8,9,10 }, "grand poobahs");
		assertTrue(c.calibrated());
		
		c.setFunction(Calibration.LOG, new double[] { 1,2,3,4,5,6,7,8,9,10 }, "grand poobahs");
		assertTrue(c.calibrated());
		
		c.setFunction(Calibration.RODBARD, new double[] { 1,2,3,4,5,6,7,8,9,10 }, "grand poobahs");
		assertTrue(c.calibrated());
		
		c.setFunction(Calibration.GAMMA_VARIATE, new double[] { 1,2,3,4,5,6,7,8,9,10 }, "grand poobahs");
		assertTrue(c.calibrated());
		
		c.setFunction(Calibration.LOG2, new double[] { 1,2,3,4,5,6,7,8,9,10 }, "grand poobahs");
		assertTrue(c.calibrated());
		
		c.setFunction(Calibration.RODBARD2, new double[] { 1,2,3,4,5,6,7,8,9,10 }, "grand poobahs");
		assertTrue(c.calibrated());
		
		c.setFunction(Calibration.UNCALIBRATED_OD, new double[] { 1,2,3,4,5,6,7,8,9,10 }, "grand poobahs");
		assertTrue(c.calibrated());
		
		c.setFunction(Calibration.CUSTOM, new double[] { 1,2,3,4,5,6,7,8,9,10 }, "grand poobahs");
		assertTrue(c.calibrated());
	}

	@Test
	public void testGetFunction() {
		c.setFunction(Calibration.CUSTOM, new double[] { 1,2,3,4,5,6,7,8,9,10 }, "grand poobahs");
		assertEquals(Calibration.CUSTOM,c.getFunction());
	}

	// NOTE - I am not going to test the validity of the CTable that gets calculated as a side effect. I
	//  will only test that a CTable is passed back when needed and null when appropriate
	
	@Test
	public void testGetCTable() {

		ImagePlus imp;
		float[] values;
		
		// by default function should == NONE which allows getCTable() to return null
		assertNull(c.getCTable());
		
		// 16 bit
		imp = NewImage.createShortImage("Arigula", 40, 30, 1, 0);
		
			// straight line to rodbard 2 and coeffs set
			c = new Calibration(imp);
			c.setFunction(Calibration.STRAIGHT_LINE,new double[]{1,2,3,4,5},"Sinatras");
			values = c.getCTable();
			assertNotNull(values);
			c = new Calibration(imp);
			c.setFunction(Calibration.POLY2,new double[]{1,2,3,4,5},"Sinatras");
			values = c.getCTable();
			assertNotNull(values);
			c = new Calibration(imp);
			c.setFunction(Calibration.POLY3,new double[]{1,2,3,4,5},"Sinatras");
			values = c.getCTable();
			assertNotNull(values);
			c = new Calibration(imp);
			c.setFunction(Calibration.POLY4,new double[]{1,2,3,4,5},"Sinatras");
			values = c.getCTable();
			assertNotNull(values);
			c = new Calibration(imp);
			c.setFunction(Calibration.EXPONENTIAL,new double[]{1,2,3,4,5},"Sinatras");
			values = c.getCTable();
			assertNotNull(values);
			c = new Calibration(imp);
			c.setFunction(Calibration.POWER,new double[]{1,2,3,4,5},"Sinatras");
			values = c.getCTable();
			assertNotNull(values);
			c = new Calibration(imp);
			c.setFunction(Calibration.LOG,new double[]{1,2,3,4,5},"Sinatras");
			values = c.getCTable();
			assertNotNull(values);
			c = new Calibration(imp);
			c.setFunction(Calibration.RODBARD,new double[]{1,2,3,4,5},"Sinatras");
			values = c.getCTable();
			assertNotNull(values);
			c = new Calibration(imp);
			c.setFunction(Calibration.GAMMA_VARIATE,new double[]{1,2,3,4,5},"Sinatras");
			values = c.getCTable();
			assertNotNull(values);
			c = new Calibration(imp);
			c.setFunction(Calibration.LOG2,new double[]{1,2,3,4,5},"Sinatras");
			values = c.getCTable();
			assertNotNull(values);
			c = new Calibration(imp);
			c.setFunction(Calibration.RODBARD2,new double[]{1,2,3,4,5},"Sinatras");
			values = c.getCTable();
			assertNotNull(values);
			
			// else
			c = new Calibration(imp);
			c.setFunction(Calibration.NONE,new double[]{1,2,3,4,5},"Sinatras");
			values = c.getCTable();
			assertNull(values);
			c = new Calibration(imp);
			c.setFunction(Calibration.UNCALIBRATED_OD,new double[]{1,2,3,4,5},"Sinatras");
			values = c.getCTable();
			assertNull(values);
			c = new Calibration(imp);
			c.setFunction(Calibration.CUSTOM,new double[]{1,2,3,4,5},"Sinatras");
			values = c.getCTable();
			assertNull(values);
			c = new Calibration(imp);
			c.setFunction(Calibration.EXPONENTIAL,null,"Sinatras"); // null coeffs
			values = c.getCTable();
			assertNull(values);

		
		// other non 8 bit types
		imp = NewImage.createFloatImage("Arigula", 40, 30, 1, 0);
			c = new Calibration(imp);
			assertNull(c.getCTable());
		imp = NewImage.createRGBImage("Arigula", 40, 30, 1, 0);
			c = new Calibration(imp);
			assertNull(c.getCTable());

		// else 8 bit
		imp = NewImage.createByteImage("Arigula", 40, 30, 1, 0);
			
			// straight line to rodbard 2 (and also uncalib_od) and coeffs set
			
			c = new Calibration(imp);
			c.setFunction(Calibration.STRAIGHT_LINE,new double[]{1,2,3,4,5},"Sinatras");
			values = c.getCTable();
			assertNotNull(values);
			c = new Calibration(imp);
			c.setFunction(Calibration.POLY2,new double[]{1,2,3,4,5},"Sinatras");
			values = c.getCTable();
			assertNotNull(values);
			c = new Calibration(imp);
			c.setFunction(Calibration.POLY3,new double[]{1,2,3,4,5},"Sinatras");
			values = c.getCTable();
			assertNotNull(values);
			c = new Calibration(imp);
			c.setFunction(Calibration.POLY4,new double[]{1,2,3,4,5},"Sinatras");
			values = c.getCTable();
			assertNotNull(values);
			c = new Calibration(imp);
			c.setFunction(Calibration.EXPONENTIAL,new double[]{1,2,3,4,5},"Sinatras");
			values = c.getCTable();
			assertNotNull(values);
			c = new Calibration(imp);
			c.setFunction(Calibration.POWER,new double[]{1,2,3,4,5},"Sinatras");
			values = c.getCTable();
			assertNotNull(values);
			c = new Calibration(imp);
			c.setFunction(Calibration.LOG,new double[]{1,2,3,4,5},"Sinatras");
			values = c.getCTable();
			assertNotNull(values);
			c = new Calibration(imp);
			c.setFunction(Calibration.RODBARD,new double[]{1,2,3,4,5},"Sinatras");
			values = c.getCTable();
			assertNotNull(values);
			c = new Calibration(imp);
			c.setFunction(Calibration.GAMMA_VARIATE,new double[]{1,2,3,4,5},"Sinatras");
			values = c.getCTable();
			assertNotNull(values);
			c = new Calibration(imp);
			c.setFunction(Calibration.LOG2,new double[]{1,2,3,4,5},"Sinatras");
			values = c.getCTable();
			assertNotNull(values);
			c = new Calibration(imp);
			c.setFunction(Calibration.RODBARD2,new double[]{1,2,3,4,5},"Sinatras");
			values = c.getCTable();
			assertNotNull(values);
			c = new Calibration(imp);
			c.setFunction(Calibration.UNCALIBRATED_OD,new double[]{1,2,3,4,5},"Sinatras");
			values = c.getCTable();
			assertNotNull(values);

			// else
			
			c = new Calibration(imp);
			c.setFunction(Calibration.NONE,new double[]{1,2,3,4,5},"Sinatras");
			values = c.getCTable();
			assertNull(values);
			c = new Calibration(imp);
			c.setFunction(Calibration.CUSTOM,new double[]{1,2,3,4,5},"Sinatras");
			values = c.getCTable();
			assertNull(values);
			c = new Calibration(imp);
			c.setFunction(Calibration.EXPONENTIAL,null,"Sinatras"); // null coeffs
			values = c.getCTable();
			assertNull(values);
	}

	private void tryXFloats(int x, boolean expectSuccess, Calibration c)
	{
		Calibration clone = (Calibration) c.clone();
		float[] floats = new float[x];
		try {
			clone.setCTable(floats,"anything");
			if (expectSuccess)
				assertTrue(true);
			else
				fail();
		} catch (IllegalArgumentException e) {
			if (expectSuccess)
				fail();
			else
				assertTrue(true);
		}
	}
	
	@Test
	public void testSetCTable() {
		// if table null then disabledensitycalib and change nothing else
		c = new Calibration();
		c.setCTable(null,"anything");
		assertEquals(Calibration.NONE,c.getFunction());
		assertNull(c.getCoefficients());
		assertNull(c.getCTable());
		assertEquals("Gray Value",c.getValueUnit());
		
		// if bitDepth == 16 and table.length !- 65536 throw IllArgExxc
		ImagePlus imp = NewImage.createShortImage("MyFakeImage", 200, 150, 1, 0);
		c = new Calibration(imp);

		tryXFloats(0,false,c);
		tryXFloats(1000,false,c);
		tryXFloats(65535,false,c);
		tryXFloats(65536,true,c);
		tryXFloats(65537,false,c);

		float[] cTable = new float[] {1,2,3,4,5,6,7,8,9};
		
		// pass a value unit
		c = new Calibration();
		String tmp = c.getValueUnit();
		c.setCTable(cTable,"nonplussed");
		assertEquals(cTable,c.getCTable());
		assertEquals(Calibration.CUSTOM, c.getFunction());
		assertNull(c.getCoefficients());
		assertFalse(c.zeroClip());
		assertEquals("nonplussed",c.getValueUnit());
		
		// pass no value unit
		c = new Calibration();
		String origValUnit = c.getValueUnit();
		c.setCTable(cTable,null);
		assertEquals(cTable,c.getCTable());
		assertEquals(Calibration.CUSTOM, c.getFunction());
		assertNull(c.getCoefficients());
		assertFalse(c.zeroClip());
		assertEquals(origValUnit,c.getValueUnit());
	}

	@Test
	public void testGetCValueInt() {
		
		// function == NONE
		c = new Calibration();
		c.setFunction(Calibration.NONE,null,"donutsPerParsec");
		assertEquals(1.0,c.getCValue(1),Assert.DOUBLE_TOL);
		
		// function STRAIGHT_LINE .. RODBARD2 and non-null coeffs
		c = new Calibration();
		c.setFunction(Calibration.STRAIGHT_LINE,new double[]{1,2,3,4,5},"donutsPerParsec");
		assertEquals(3.0,c.getCValue(1),Assert.DOUBLE_TOL);
		c = new Calibration();
		c.setFunction(Calibration.POLY2,new double[]{1,2,3,4,5},"donutsPerParsec");
		assertEquals(6.0,c.getCValue(1),Assert.DOUBLE_TOL);
		c = new Calibration();
		c.setFunction(Calibration.POLY3,new double[]{1,2,3,4,5},"donutsPerParsec");
		assertEquals(10.0,c.getCValue(1),Assert.DOUBLE_TOL);
		c = new Calibration();
		c.setFunction(Calibration.POLY4,new double[]{1,2,3,4,5},"donutsPerParsec");
		assertEquals(15.0,c.getCValue(1),Assert.DOUBLE_TOL);
		c = new Calibration();
		c.setFunction(Calibration.EXPONENTIAL,new double[]{1,2,3,4,5},"donutsPerParsec");
		assertEquals(7.38905609893065,c.getCValue(1),Assert.DOUBLE_TOL);
		c = new Calibration();
		c.setFunction(Calibration.POWER,new double[]{1,2,3,4,5},"donutsPerParsec");
		assertEquals(1.0,c.getCValue(1),Assert.DOUBLE_TOL);
		c = new Calibration();
		c.setFunction(Calibration.LOG,new double[]{1,2,3,4,5},"donutsPerParsec");
		assertEquals(0.6931471805599453,c.getCValue(1),Assert.DOUBLE_TOL);
		c = new Calibration();
		c.setFunction(Calibration.RODBARD,new double[]{1,2,3,4,5},"donutsPerParsec");
		assertEquals(1.3,c.getCValue(1),Assert.DOUBLE_TOL);
		c = new Calibration();
		c.setFunction(Calibration.GAMMA_VARIATE,new double[]{1,2,3,4,5},"donutsPerParsec");
		assertEquals(0.0,c.getCValue(1),Assert.DOUBLE_TOL);
		c = new Calibration();
		c.setFunction(Calibration.LOG2,new double[]{1,2,3,4,5},"donutsPerParsec");
		assertEquals(-12.815510557964274,c.getCValue(1),Assert.DOUBLE_TOL);
		c = new Calibration();
		c.setFunction(Calibration.RODBARD2,new double[]{1,2,3,4,5},"donutsPerParsec");
		assertEquals(0.0,c.getCValue(1),Assert.DOUBLE_TOL);
		c = new Calibration();
		
		// otherwise various fall through cases
		
		// coeffs null
		c = new Calibration();
		c.setFunction(Calibration.STRAIGHT_LINE,null,"HomersPerBart");
		assertEquals(1.0,c.getCValue(1),Assert.DOUBLE_TOL);

		// function defined out of supported range
		c = new Calibration();
		c.setFunction(Calibration.UNCALIBRATED_OD,new double[]{1,2,3,4,5},"HomersPerBart");
		assertEquals(2.4065401554107,c.getCValue(1),Assert.DOUBLE_TOL);

		// cTable != null
		c = new Calibration();
		c.setCTable(new float[] {9,2,7,4,5.5f}, null);
		assertEquals(-1.0,c.getCValue(-1),Assert.DOUBLE_TOL);
		assertEquals(9.0,c.getCValue(0),Assert.DOUBLE_TOL);
		assertEquals(2.0,c.getCValue(1),Assert.DOUBLE_TOL);
		assertEquals(7.0,c.getCValue(2),Assert.DOUBLE_TOL);
		assertEquals(4.0,c.getCValue(3),Assert.DOUBLE_TOL);
		assertEquals(5.5,c.getCValue(4),Assert.DOUBLE_TOL);
		assertEquals(6.0,c.getCValue(6),Assert.DOUBLE_TOL);
		
		// cTable == null and then calculating
		// Not testing - counting on CurveFitter tests
	}

	@Test
	public void testGetCValueDouble() {
		// function == NONE
		c = new Calibration();
		c.setFunction(Calibration.NONE,null,"donutsPerParsec");
		assertEquals(1.4,c.getCValue(1.4),Assert.DOUBLE_TOL);
		
		// function STRAIGHT_LINE .. RODBARD2 and non-null coeffs
		c = new Calibration();
		c.setFunction(Calibration.STRAIGHT_LINE,new double[]{1,2,3,4,5},"donutsPerParsec");
		assertEquals(3.8,c.getCValue(1.4),Assert.DOUBLE_TOL);
		c = new Calibration();
		c.setFunction(Calibration.POLY2,new double[]{1,2,3,4,5},"donutsPerParsec");
		assertEquals(9.68,c.getCValue(1.4),Assert.DOUBLE_TOL);
		c = new Calibration();
		c.setFunction(Calibration.POLY3,new double[]{1,2,3,4,5},"donutsPerParsec");
		assertEquals(20.656,c.getCValue(1.4),Assert.DOUBLE_TOL);
		c = new Calibration();
		c.setFunction(Calibration.POLY4,new double[]{1,2,3,4,5},"donutsPerParsec");
		assertEquals(39.863999999999,c.getCValue(1.4),Assert.DOUBLE_TOL);
		c = new Calibration();
		c.setFunction(Calibration.EXPONENTIAL,new double[]{1,2,3,4,5},"donutsPerParsec");
		assertEquals(16.444646771097048,c.getCValue(1.4),Assert.DOUBLE_TOL);
		c = new Calibration();
		c.setFunction(Calibration.POWER,new double[]{1,2,3,4,5},"donutsPerParsec");
		assertEquals(1.96,c.getCValue(1.4),Assert.DOUBLE_TOL);
		c = new Calibration();
		c.setFunction(Calibration.LOG,new double[]{1,2,3,4,5},"donutsPerParsec");
		assertEquals(1.0296194171811581,c.getCValue(1.4),Assert.DOUBLE_TOL);
		c = new Calibration();
		c.setFunction(Calibration.RODBARD,new double[]{1,2,3,4,5},"donutsPerParsec");
		assertEquals(1.5364963503649633,c.getCValue(1.4),Assert.DOUBLE_TOL);
		c = new Calibration();
		c.setFunction(Calibration.GAMMA_VARIATE,new double[]{1,2,3,4,5},"donutsPerParsec");
		assertEquals(0.11581918950860276,c.getCValue(1.4),Assert.DOUBLE_TOL);
		c = new Calibration();
		c.setFunction(Calibration.LOG2,new double[]{1,2,3,4,5},"donutsPerParsec");
		assertEquals(-12.815510557964274,c.getCValue(1.4),Assert.DOUBLE_TOL);
		c = new Calibration();
		c.setFunction(Calibration.RODBARD2,new double[]{1,2,3,4,5},"donutsPerParsec");
		assertEquals(1.176696810829104,c.getCValue(1.4),Assert.DOUBLE_TOL);
		c = new Calibration();
		
		// otherwise fall through case : call integer version
		c = new Calibration();
		c.setFunction(Calibration.POLY2,new double[]{1,2,3,4,5},"donutsPerParsec");
		assertEquals(9.68,c.getCValue(1.4),Assert.DOUBLE_TOL);
	}

	@Test
	public void testGetRawValue() {

		ImagePlus imp = NewImage.createByteImage("Jinkees", 30, 70, 1, 0);
		
		// function = NONE
		c = new Calibration(imp);
		c.setFunction(Calibration.NONE, new double[] { 6,2,9,8 }, "arrividiercis");
		assertEquals(17.0,c.getRawValue(17.0),Assert.DOUBLE_TOL);

		// function = STRAIGHT_LINE and coeffs !+ null and len 2 and second one not 0.0
		c = new Calibration(imp);
		c.setFunction(Calibration.STRAIGHT_LINE, new double[] { 6,3 }, "arrividiercis");
		assertEquals(2.0,c.getRawValue(12.0),Assert.DOUBLE_TOL);
		
		// passed in table null - calls makeTable
		// NOTE - will not test this subcase. makeCTable() indirectly tested elsewhere.
		//   There is no code that tests the validity of the constructed cTable but and test of CurveFitter
		//   should take care of this.
		//c = new Calibration(imp);
		//c.setCTable(null, "joules");
		
		// passed in table not null - given a value it should return index (as a double) of the first nearest curve value
		c = new Calibration(imp);
		c.setCTable(new float[] { 1, 3, 6, 10, 6, 3, 1 }, "pascals");
		assertEquals(0.0,c.getRawValue(1),Assert.DOUBLE_TOL);
		assertEquals(1.0,c.getRawValue(3),Assert.DOUBLE_TOL);
		assertEquals(2.0,c.getRawValue(6),Assert.DOUBLE_TOL);
		assertEquals(3.0,c.getRawValue(10),Assert.DOUBLE_TOL);
		assertEquals(0.0,c.getRawValue(2),Assert.DOUBLE_TOL);
		assertEquals(1.0,c.getRawValue(4.1),Assert.DOUBLE_TOL);
		assertEquals(3.0,c.getRawValue(8.1),Assert.DOUBLE_TOL);
		assertEquals(3.0,c.getRawValue(99.7),Assert.DOUBLE_TOL);
		assertEquals(0.0,c.getRawValue(0),Assert.DOUBLE_TOL);
		assertEquals(0.0,c.getRawValue(-1),Assert.DOUBLE_TOL);
	}

	@Test
	public void testCopy() {
		c.setFunction(Calibration.CUSTOM,new double[] { 4,5,6,7 }, "gigahops");
		c.setInvertY(true);
		c.setTimeUnit("seconds");
		c.setZUnit("kernelsPerMile");
		Calibration copy = c.copy();
		assertCalibsSame(c,copy);
	}

	@Test
	public void testClone() {
	
		c.setFunction(Calibration.POWER,new double[] { 4,5,6,7 }, "femtometers");
		c.setInvertY(true);
		c.setTimeUnit("years");
		c.setZUnit("popsiclesPerInch");
		Calibration clone = (Calibration) c.clone();
		assertCalibsSame(c,clone);
	}

	@Test
	public void testEqualsCalibration() {
		Calibration c2;
		
		c2 = c.copy();
		assertTrue(c.equals(c2));
		
		c2 = new Calibration();
		c2.pixelWidth = 4.0;
		assertFalse(c.equals(c2));
		
		c2 = new Calibration();
		c2.pixelHeight = 4.0;
		assertFalse(c.equals(c2));
		
		c2 = new Calibration();
		c2.pixelDepth = 4.0;
		assertFalse(c.equals(c2));
		
		c2 = new Calibration();
		c2.setUnit("picoDeGallos");
		assertFalse(c.equals(c2));
		
		c2 = new Calibration();
		c2.setValueUnit("heptograms");
		assertFalse(c.equals(c2));
		
		c2 = new Calibration();
		c2.setFunction(Calibration.LOG2,new double[] {0,5,1},"zoolanders");
		assertFalse(c.equals(c2));
	}

	@Test
	public void testIsSigned16Bit() {
		
		assertFalse(c.isSigned16Bit());

		ImagePlus imp = NewImage.createShortImage("MyFakeImage", 200, 150, 1, 0);
		double[] coeffs = new double[] {-32768.0,1.0};
		c = new Calibration(imp);
		c.setFunction(Calibration.STRAIGHT_LINE, coeffs, null);
		
		assertTrue(c.isSigned16Bit());
	}

	@Test
	public void testZeroClip() {
		// zeroClip() is a getter with no parallel setter. The setFunction() method can set zeroClip.
		c.setFunction(Calibration.EXPONENTIAL, new double[] {44.3,10.0}, "inch", true);
		assertTrue(c.zeroClip());
		c.setFunction(Calibration.EXPONENTIAL, new double[] {44.3,10.0}, "inch");
		assertFalse(c.zeroClip());
	}

	@Test
	public void testSetInvertY() {
		
		// make sure analyzer in a good state
		int measurements = Analyzer.getMeasurements();
		measurements &= ~Analyzer.INVERT_Y;
		Analyzer.setMeasurements(measurements);

		double y;
		int imageHeight;
		
		// general case
		c = new Calibration();
		y = 75.0;
		imageHeight = 400;
		c.yOrigin = 50.0;
		c.pixelHeight = 3;
		c.setInvertY(false);
		assertEquals(((y-c.yOrigin)*c.pixelHeight),c.getY(y,imageHeight),Assert.DOUBLE_TOL);

		// invertY specified true
		c = new Calibration();
		y = 75.0;
		imageHeight = 400;
		c.yOrigin = 50.0;
		c.pixelHeight = 3;
		c.setInvertY(true);
		assertEquals(((c.yOrigin-y)*c.pixelHeight),c.getY(y,imageHeight),Assert.DOUBLE_TOL);
	}

	@Test
	public void testToString() {

		assertEquals("w=1.0, h=1.0, d=1.0, unit=pixel, f=20, nc=null, table=null, vunit=Gray Value",c.toString());
		
		// change all key values
		c.pixelWidth = 2.7;
		c.pixelHeight = 7.4;
		c.pixelDepth = 4.1;
		c.setUnit("centaurs");
		c.setFunction(Calibration.POLY4, new double[]{1,2,3}, "matchboxCars");
		// we won't set cTable
		c.setValueUnit("griffons");
		
		// include some fields that shouldn't make a difference
		c.frameInterval = 5;
		c.loop = false;
		c.xOrigin = 45;
		c.info = "Ziggy Stardust";

		assertEquals("w=2.7, h=7.4, d=4.1, unit=centaurs, f=3, nc=3, table=null, vunit=griffons",c.toString());
	}

}
