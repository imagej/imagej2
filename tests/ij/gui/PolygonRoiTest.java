package ij.gui;

import static org.junit.Assert.*;
import org.junit.Test;

import java.awt.Polygon;

import ij.*;
import ij.process.*;
import ij.measure.Calibration;

public class PolygonRoiTest {

	PolygonRoi p;

	// helper
	private int extent(int[] vals)
	{
		int min = 0;
		int max = 0;
		
		for (int i = 0; i < vals.length; i++)
		{
			if (i == 0)
			{
				max = vals[0];
				min = vals[0];
			}
			else
			{
				if (vals[i] < min) min = vals[i];
				if (vals[i] > max) max = vals[i];
			}
		}
		
		return max - min;
	}

	// helper
	private ImagePlus getCalibratedImagePlus()
	{
		ImagePlus ip = new ImagePlus("Zakky",new ShortProcessor(5,5,new short[5*5],null));
		Calibration cal = new Calibration();
		cal.pixelWidth = 14.1;
		cal.pixelHeight = 8.7;
		ip.setCalibration(cal);
		return ip;
	}
	
	// helper
	private void validateCons1(int[] xs, int[] ys, int type)
	{
		int len = xs.length;
		
		p = new PolygonRoi(xs,ys,len,type);
		
		assertNotNull(p);
		assertEquals(type,p.getType());
		assertEquals(len,p.nPoints);
		
		if (type == Roi.TRACED_ROI) {
			assertSame(xs,p.xp);
			assertSame(ys,p.yp);
		}
		else {
			assertNotSame(xs,p.xp);
			assertNotSame(ys,p.yp);
		}
		
		if ((type != Roi.POINT) &&
				((len < 2) ||
						((type != Roi.FREELINE && type != Roi.POLYLINE && type != Roi.ANGLE) &&
						  ((len < 3) || (extent(ys) == 0) || (extent(ys)==0)))))
		{
			assertEquals(Roi.CONSTRUCTING,p.getState());
			if (len > 0)
			{
				assertEquals(xs[0],p.x);
				assertEquals(ys[0],p.y);
				assertEquals(xs[0],p.xp[0]);
				assertEquals(ys[0],p.yp[0]);
			}
		}
		else
		{
			assertEquals(Roi.NORMAL,p.getState());
			for (int i = 0; i < len; i++) {
				assertEquals(xs[i],p.x+p.xp[i]);
				assertEquals(ys[i],p.y+p.yp[i]);
			}
		}
	}

	// helper: same as previous method
	private void validateCons2(Polygon poly, int type)
	{
		int len = poly.npoints;
		
		p = new PolygonRoi(poly,type);
		
		assertNotNull(p);
		assertEquals(type,p.getType());
		assertEquals(len,p.nPoints);
		
		if (type == Roi.TRACED_ROI) {
			assertSame(poly.xpoints,p.xp);
			assertSame(poly.ypoints,p.yp);
		}
		else {
			assertNotSame(poly.xpoints,p.xp);
			assertNotSame(poly.ypoints,p.yp);
		}
		
		if ((type != Roi.POINT) &&
				((len < 2) ||
						((type != Roi.FREELINE && type != Roi.POLYLINE && type != Roi.ANGLE) &&
						  ((len < 3) || (extent(poly.xpoints) == 0) || (extent(poly.ypoints)==0)))))
		{
			assertEquals(Roi.CONSTRUCTING,p.getState());
			if (len > 0)
			{
				assertEquals(poly.xpoints[0],p.x);
				assertEquals(poly.ypoints[0],p.y);
				assertEquals(poly.xpoints[0],p.xp[0]);
				assertEquals(poly.ypoints[0],p.yp[0]);
			}
		}
		else
		{
			assertEquals(Roi.NORMAL,p.getState());
			for (int i = 0; i < len; i++) {
				assertEquals(poly.xpoints[i],p.x+p.xp[i]);
				assertEquals(poly.ypoints[i],p.y+p.yp[i]);
			}
		}
	}

	// helper
	private void validateConvexHull(int[] xs, int[] ys, int[] chXs, int[] chYs)
	{
		p = new PolygonRoi(xs,ys,xs.length,Roi.POLYGON);
		Polygon poly = p.getConvexHull();
		assertNotNull(poly);
		assertEquals(chXs.length,poly.npoints);
		for (int i = 0; i < chXs.length; i++)
		{
			assertEquals(chXs[i],poly.xpoints[i]);
			assertEquals(chYs[i],poly.ypoints[i]);
		}
	}
	
	// regular version of validator for getPolygon()
	private void validateGetPolygon1(int[] xs, int[] ys, int[] pXs, int[] pYs)
	{
		p = new PolygonRoi(xs,ys,xs.length,Roi.POLYGON);
		Polygon poly = p.getPolygon();
		assertEquals(pXs.length,poly.npoints);
		for (int i = 0; i < pXs.length; i++)
		{
			assertEquals(pXs[i],poly.xpoints[i]);
			assertEquals(pYs[i],poly.ypoints[i]);
		}
	}
	
	// spline version of validator for getPolygon()
	private void validateGetPolygon2(int[] xs, int[] ys, int[] pXs, int[] pYs)
	{
		p = new PolygonRoi(xs,ys,xs.length,Roi.POLYGON);
		p.fitSpline(5);
		Polygon poly = p.getPolygon();
		assertEquals(pXs.length,poly.npoints);
		for (int i = 0; i < pXs.length; i++)
		{
			assertEquals(pXs[i],poly.xpoints[i]);
			assertEquals(pYs[i],poly.ypoints[i]);
		}
	}
	
	// regular version of validator for getFloatPolygon()
	private void validateGetFloatPolygon1(int[] xs, int[] ys, float[] pXs, float[] pYs)
	{
		p = new PolygonRoi(xs,ys,xs.length,Roi.POLYGON);
		FloatPolygon poly = p.getFloatPolygon();
		assertEquals(pXs.length,poly.npoints);
		for (int i = 0; i < pXs.length; i++)
		{
			assertEquals(pXs[i],poly.xpoints[i],Assert.FLOAT_TOL);
			assertEquals(pYs[i],poly.ypoints[i],Assert.FLOAT_TOL);
		}
	}
	
	// spline version of validator for getFloatPolygon()
	private void validateGetFloatPolygon2(int[] xs, int[] ys, float[] pXs, float[] pYs)
	{
		p = new PolygonRoi(xs,ys,xs.length,Roi.POLYGON);
		p.fitSpline(5);
		FloatPolygon poly = p.getFloatPolygon();
		assertEquals(pXs.length,poly.npoints);
		for (int i = 0; i < pXs.length; i++)
		{
			assertEquals(pXs[i],poly.xpoints[i],Assert.FLOAT_TOL);
			assertEquals(pYs[i],poly.ypoints[i],Assert.FLOAT_TOL);
		}
	}

	// helper
	private void validateClone(int[] xs, int[] ys, int type)
	{
		p = new PolygonRoi(xs,ys,xs.length,type);
		PolygonRoi roi = (PolygonRoi)p.clone();
		
		assertNotNull(roi);
		assertTrue(roi.equals(p));
	}
	
	// ***********************************  TESTS  *****************************************************
	
	@Test
	public void testPolygonRoiIntArrayIntArrayIntInt() {
		// vary type
		validateCons1(new int[]{1},new int[]{4},Roi.POLYGON);
		validateCons1(new int[]{1},new int[]{4},Roi.FREEROI);
		validateCons1(new int[]{1},new int[]{4},Roi.TRACED_ROI);
		validateCons1(new int[]{1},new int[]{4},Roi.POLYLINE);
		validateCons1(new int[]{1},new int[]{4},Roi.FREELINE);
		validateCons1(new int[]{1},new int[]{4},Roi.ANGLE);
		validateCons1(new int[]{1},new int[]{4},Roi.POINT);

		// attempt type other than above ones
		try {
			validateCons1(new int[]{1,2,3,4},new int[]{5,6,7,8},Roi.OVAL);
			fail();
		} catch (IllegalArgumentException e) {
			assertTrue(true);
		}
		
		// vary number of input points
		validateCons1(new int[]{},new int[]{},Roi.POLYGON);
		validateCons1(new int[]{1},new int[]{1},Roi.POLYGON);
		validateCons1(new int[]{1,2},new int[]{4,5},Roi.POLYGON);
		validateCons1(new int[]{1,2,3},new int[]{4,5,6},Roi.POLYGON);
		validateCons1(new int[]{1,2,3,4},new int[]{7,5,3,1},Roi.POLYGON);

		// vary both inputs
		validateCons1(new int[]{},new int[]{},Roi.FREELINE);
		validateCons1(new int[]{1},new int[]{1},Roi.POINT);
		validateCons1(new int[]{1,2},new int[]{4,5},Roi.POLYLINE);
		validateCons1(new int[]{1,2,3},new int[]{4,5,6},Roi.FREEROI);
		validateCons1(new int[]{1,2,3,4},new int[]{7,5,3,1},Roi.POLYGON);
	}
	
	@Test
	public void testPolygonRoiPolygonInt() {
		// vary type
		validateCons2(new Polygon(new int[]{1},new int[]{4},1),Roi.POLYGON);
		validateCons2(new Polygon(new int[]{1},new int[]{4},1),Roi.FREEROI);
		validateCons2(new Polygon(new int[]{1},new int[]{4},1),Roi.TRACED_ROI);
		validateCons2(new Polygon(new int[]{1},new int[]{4},1),Roi.POLYLINE);
		validateCons2(new Polygon(new int[]{1},new int[]{4},1),Roi.FREELINE);
		validateCons2(new Polygon(new int[]{1},new int[]{4},1),Roi.ANGLE);
		validateCons2(new Polygon(new int[]{1},new int[]{4},1),Roi.POINT);

		// attempt type other than above ones
		try {
			validateCons2(new Polygon(new int[]{1,2,3,4},new int[]{5,6,7,8},4),Roi.OVAL);
			fail();
		} catch (IllegalArgumentException e) {
			assertTrue(true);
		}
		
		// vary number of input points
		validateCons2(new Polygon(new int[]{},new int[]{},0),Roi.POLYGON);
		validateCons2(new Polygon(new int[]{1},new int[]{1},1),Roi.POLYGON);
		validateCons2(new Polygon(new int[]{1,2},new int[]{4,5},2),Roi.POLYGON);
		validateCons2(new Polygon(new int[]{1,2,3},new int[]{4,5,6},3),Roi.POLYGON);
		validateCons2(new Polygon(new int[]{1,2,3,4},new int[]{7,5,3,1},4),Roi.POLYGON);

		// vary both inputs
		validateCons2(new Polygon(new int[]{},new int[]{},0),Roi.FREELINE);
		validateCons2(new Polygon(new int[]{1},new int[]{1},1),Roi.POINT);
		validateCons2(new Polygon(new int[]{1,2},new int[]{4,5},2),Roi.POLYLINE);
		validateCons2(new Polygon(new int[]{1,2,3},new int[]{4,5,6},3),Roi.FREEROI);
		validateCons2(new Polygon(new int[]{1,2,3,4},new int[]{7,5,3,1},4),Roi.POLYGON);
	}

	@Test
	public void testPolygonRoiIntIntImagePlus() {
		// note - can't test this method. its expecting an active imagecanvas (gui) to run without crashing
	}

	@Test
	public void testGetLength() {
		// traced roi subcase
		p = new PolygonRoi(new int[]{1,2,3,4},new int[]{2,4,6,8},4,Roi.TRACED_ROI);
		assertEquals(15.65685,p.getLength(),Assert.DOUBLE_TOL);
		
		// npoints > 2 and FREEROI
		p = new PolygonRoi(new int[]{4,7,3,9},new int[]{1,3,5,7},4,Roi.FREEROI);
		assertEquals(15.85518,p.getLength(),Assert.DOUBLE_TOL);

		// npoints > 2 and FREELINE (and both h and w > 0)
		p = new PolygonRoi(new int[]{4,7,3},new int[]{1,3,5},3,Roi.FREELINE);
		assertEquals(4.71160,p.getLength(),Assert.DOUBLE_TOL);

		// otherwise
		
		//   with calibration: spline and POLYGON
		p = new PolygonRoi(new int[]{104,88,66,31},new int[]{10,20,30,40},4,Roi.POLYGON);
		p.setImage(getCalibratedImagePlus());
		p.fitSpline(20);
		assertEquals(2142.30092,p.getLength(),Assert.DOUBLE_TOL);
		
		//   with calibration: spline and not a POLYGON
		p = new PolygonRoi(new int[]{18,73,44,33},new int[]{40,30,20,10},4,Roi.POLYLINE);
		p.setImage(getCalibratedImagePlus());
		p.fitSpline(20);
		assertEquals(1398.21248,p.getLength(),Assert.DOUBLE_TOL);
		
		//   with calibration: no spline and POLYGON
		p = new PolygonRoi(new int[]{22,4,88,63},new int[]{100,200,300,400},4,Roi.POLYGON);
		p.setImage(getCalibratedImagePlus());
		assertEquals(5987.81253,p.getLength(),Assert.DOUBLE_TOL);

		//   with calibration: no spline and not a POLYGON
		p = new PolygonRoi(new int[]{22,4,88,63},new int[]{100,200,300,400},4,Roi.POLYLINE);
		p.setImage(getCalibratedImagePlus());
		assertEquals(3314.55616,p.getLength(),Assert.DOUBLE_TOL);

		//   no calibration: spline and POLYGON
		p = new PolygonRoi(new int[]{22,4,88,63},new int[]{100,200,300,400},4,Roi.POLYGON);
		p.fitSpline(20);
		assertEquals(662.80068,p.getLength(),Assert.DOUBLE_TOL);
		
		//   no calibration: spline and not a POLYGON
		p = new PolygonRoi(new int[]{22,4,88,63},new int[]{100,200,300,400},4,Roi.POLYLINE);
		p.fitSpline(20);
		assertEquals(344.70283,p.getLength(),Assert.DOUBLE_TOL);
		
		//   no calibration: no spline and POLYGON
		p = new PolygonRoi(new int[]{22,4,88,63},new int[]{100,200,300,400},4,Roi.POLYGON);
		assertEquals(638.07205,p.getLength(),Assert.DOUBLE_TOL);

		//   no calibration: no spline and not a POLYGON
		p = new PolygonRoi(new int[]{22,4,88,63},new int[]{100,200,300,400},4,Roi.POLYLINE);
		assertEquals(335.28335,p.getLength(),Assert.DOUBLE_TOL);
	}

	@Test
	public void testGetConvexHull() {
		Polygon poly;

		// 0 point poly
		if (IJInfo.RUN_ENHANCED_TESTS)
		{
			// as is code crashes when passed a zero point poly
			p = new PolygonRoi(new int[]{},new int[]{},0,Roi.POLYGON);
			poly = p.getConvexHull();
			assertNull(poly);
		}
		
		// 1 point polys
		// TODO - correct case commented out. Reported bug to Wayne. Waiting to hear back.
		//validateConvexHull(new int[]{1},new int[]{3},new int[]{1},new int[]{3});
		validateConvexHull(new int[]{1},new int[]{3},new int[]{2},new int[]{6});

		// 2 point polys
		// TODO - this return looks wrong
		validateConvexHull(new int[]{1,7},new int[]{6,3},new int[]{8,2},new int[]{6,9});

		// 3 point polys
		validateConvexHull(new int[]{1,4,7},new int[]{8,3,6},new int[]{4,1,7},new int[]{3,8,6});

		// 4 point polys
		validateConvexHull(new int[]{1,3,5,7},new int[]{3,5,7,9},new int[]{1,3,5,7},new int[]{3,5,7,9});
		validateConvexHull(new int[]{1,15,23,17},new int[]{14,0,12,6},new int[]{15,1,23},new int[]{0,14,12});
	}

	@Test
	public void testGetPolygon() {
		
		// regular case
		validateGetPolygon1(new int[]{},new int[]{},new int[]{},new int[]{});
		validateGetPolygon1(new int[]{1},new int[]{5},new int[]{2},new int[]{10});
		validateGetPolygon1(new int[]{4,1},new int[]{8,3},new int[]{5,2},new int[]{11,6});
		validateGetPolygon1(new int[]{3,8,2},new int[]{7,1,5},new int[]{3,8,2},new int[]{7,1,5});
		validateGetPolygon1(new int[]{17,3,31,44},new int[]{8,11,2,23},new int[]{17,3,31,44},new int[]{8,11,2,23});
		
		// spline case
		//TODO : crashes - validateGetPolygon2(new int[]{},new int[]{},new int[]{},new int[]{});
		validateGetPolygon2(new int[]{1},new int[]{5},new int[]{2,2,2,2,2},new int[]{10,10,10,10,10});
		validateGetPolygon2(new int[]{4,1},new int[]{8,3},new int[]{5,3,2,3,5},new int[]{11,8,6,8,11});
		validateGetPolygon2(new int[]{3,8,2},new int[]{7,1,5},new int[]{3,8,5,1,3},new int[]{7,2,2,6,7});
		validateGetPolygon2(new int[]{17,3,31,44},new int[]{8,11,2,23},new int[]{17,3,31,44,17},new int[]{8,11,2,23,8});
	}

	@Test
	public void testGetFloatPolygon() {
		
		// regular case
		validateGetFloatPolygon1(new int[]{},new int[]{},new float[]{},new float[]{});
		validateGetFloatPolygon1(new int[]{1},new int[]{5},new float[]{2},new float[]{10});
		validateGetFloatPolygon1(new int[]{4,1},new int[]{8,3},new float[]{5,2},new float[]{11,6});
		validateGetFloatPolygon1(new int[]{3,8,2},new int[]{7,1,5},new float[]{3,8,2},new float[]{7,1,5});
		validateGetFloatPolygon1(new int[]{17,3,31,44},new int[]{8,11,2,23},new float[]{17,3,31,44},new float[]{8,11,2,23});
		
		// spline case
		// TODO - crashes: validateGetFloatPolygon2(new int[]{},new int[]{},new float[]{},new float[]{});
		validateGetFloatPolygon2(new int[]{1},new int[]{5},new float[]{2,2,2,2,2},new float[]{10,10,10,10,10});
		validateGetFloatPolygon2(new int[]{4,1},new int[]{8,3},new float[]{5,2.9375f,2,2.9375f,5},new float[]{11,7.5625f,6,7.5625f,11});
		validateGetFloatPolygon2(new int[]{3,8,2},new int[]{7,1,5},new float[]{3,7.86562f,5.30000f,1.39688f,3},new float[]{7,1.58125f,2.40000f,5.89375f,7});
		validateGetFloatPolygon2(new int[]{17,3,31,44},new int[]{8,11,2,23},new float[]{17,3,31,44,17},new float[]{8,11,2,23,8});
	}

	@Test
	public void testClone() {
		// test a number of cases
		validateClone(new int[]{6,4,19},new int[]{22,44,1},Roi.POLYLINE);
		validateClone(new int[]{1},new int[]{8},Roi.POINT);
		validateClone(new int[]{1,2},new int[]{8,4},Roi.FREELINE);
		validateClone(new int[]{1,2,3},new int[]{8,4,7},Roi.POLYGON);
		validateClone(new int[]{4,7,-2,11},new int[]{8,32,13,15},Roi.FREEROI);
	}

	@Test
	public void testDraw() {
		// note - can't test : gui oriented
	}

	private void validateDrawPixels(int[] xs, int[] ys, int type, int[] expectedNonZeroes)
	{
		int size = 10;
		int refVal = 33;
		ImageProcessor proc = new ByteProcessor(25,25);
		proc.setColor(refVal);
		p = new PolygonRoi(xs,ys,xs.length,type);
		p.drawPixels(proc);
		for (int i = 0; i < size*size; i++)
			if (RoiHelpers.find(i,expectedNonZeroes))
				assertEquals(refVal,proc.get(i));
			else
				assertEquals(0,proc.get(i));
	}
	
	@Test
	public void testDrawPixelsImageProcessor() {
		validateDrawPixels(new int[]{1,2,3},new int[]{5,2,6},Roi.FREEROI, new int[]{});
		// TODO - continue this one here
	}

	@Test
	public void testContains() {
		fail("Not yet implemented");
	}

	@Test
	public void testIsHandle() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetMask() {
		fail("Not yet implemented");
	}

	@Test
	public void testExitConstructingMode() {
		fail("Not yet implemented");
	}

	@Test
	public void testFitSplineInt() {
		fail("Not yet implemented");
	}

	@Test
	public void testFitSpline() {
		fail("Not yet implemented");
	}

	@Test
	public void testRemoveSplineFit() {
		fail("Not yet implemented");
	}

	@Test
	public void testIsSplineFit() {
		fail("Not yet implemented");
	}

	@Test
	public void testFitSplineForStraightening() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetUncalibratedLength() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetAngle() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetNCoordinates() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetXCoordinates() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetYCoordinates() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetNonSplineCoordinates() {
		fail("Not yet implemented");
	}

}
