package ij.measure;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import ij.io.Assert;

public class CalibrationTest {

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

		Calibration c = new Calibration();
		
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
	public void testCalibrationImagePlus() {
		//fail("Not yet implemented");
	}

	@Test
	public void testCalibration() {

		// make sure default construction has correct values
		Calibration c = new Calibration();
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
		// see if there are any accessors on private data
	}

	@Test
	public void testScaled() {
		fail("Not yet implemented");
	}

	@Test
	public void testSetUnit() {
		fail("Not yet implemented");
	}

	@Test
	public void testSetXUnit() {
		fail("Not yet implemented");
	}

	@Test
	public void testSetYUnit() {
		fail("Not yet implemented");
	}

	@Test
	public void testSetZUnit() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetUnit() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetXUnit() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetYUnit() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetZUnit() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetUnits() {
		fail("Not yet implemented");
	}

	@Test
	public void testSetTimeUnit() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetTimeUnit() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetX() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetYDouble() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetYDoubleInt() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetZ() {
		fail("Not yet implemented");
	}

	@Test
	public void testSetFunctionIntDoubleArrayString() {
		fail("Not yet implemented");
	}

	@Test
	public void testSetFunctionIntDoubleArrayStringBoolean() {
		fail("Not yet implemented");
	}

	@Test
	public void testSetImage() {
		fail("Not yet implemented");
	}

	@Test
	public void testDisableDensityCalibration() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetValueUnit() {
		fail("Not yet implemented");
	}

	@Test
	public void testSetValueUnit() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetCoefficients() {
		fail("Not yet implemented");
	}

	@Test
	public void testCalibrated() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetFunction() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetCTable() {
		fail("Not yet implemented");
	}

	@Test
	public void testSetCTable() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetCValueInt() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetCValueDouble() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetRawValue() {
		fail("Not yet implemented");
	}

	@Test
	public void testCopy() {
		fail("Not yet implemented");
	}

	@Test
	public void testClone() {
		fail("Not yet implemented");
	}

	@Test
	public void testEqualsCalibration() {
		fail("Not yet implemented");
	}

	@Test
	public void testIsSigned16Bit() {
		fail("Not yet implemented");
	}

	@Test
	public void testZeroClip() {
		fail("Not yet implemented");
	}

	@Test
	public void testSetInvertY() {
		fail("Not yet implemented");
	}

	@Test
	public void testToString() {
		fail("Not yet implemented");
	}

}
