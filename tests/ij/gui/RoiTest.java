package ij.gui;

import static org.junit.Assert.*;

import org.junit.Test;

import ij.Assert;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.geom.PathIterator;
import java.util.*;

import ij.*;
import ij.measure.Calibration;
import ij.process.*;

public class RoiTest {

	Roi roi;

	private ArrayList<Double> getCoords(Polygon p) {
		ArrayList<Double> vals = new ArrayList<Double>();
		
		for (PathIterator iter = p.getPathIterator(null); !iter.isDone();)
		{
			double[] coords = new double[2];
			iter.currentSegment(coords);
			vals.add(coords[0]);
			vals.add(coords[1]);
			iter.next();
		}
		return vals;
	}
	
	private boolean doublesEqual(Double[] a, Double[] b, double tol) {
		
		if (a.length != b.length)
			return false;
		
		for (int i = 0; i < a.length; i++)
			if (Math.abs(a[i]-b[i]) > tol)
				return false;
		
		return true;
	}

	private boolean polysEqual(Polygon a, Polygon b){
		Double[] da = new Double[]{}, db = new Double[]{};
		ArrayList<Double> ptsA = getCoords(a);
		ArrayList<Double> ptsB = getCoords(b);
		return doublesEqual(ptsA.toArray(da), ptsB.toArray(db), Assert.DOUBLE_TOL);
	}
	
	private boolean rectsEqual(Rectangle a, Rectangle b) {
		if (a.x != b.x) return false;
		if (a.y != b.y) return false;
		if (a.width != b.width) return false;
		if (a.height != b.height) return false;
		return true;
	}

	@Test
	public void testConstantsAndVars() {
		
		// compile time test : make sure public static vars exist
		assertTrue(Roi.previousRoi == Roi.previousRoi);
		assertTrue(Roi.onePixelWide instanceof BasicStroke);
		
		// make sure constants exist with the correct values
		assertEquals(0,Roi.CONSTRUCTING);
		assertEquals(1,Roi.MOVING);
		assertEquals(2,Roi.RESIZING);
		assertEquals(3,Roi.NORMAL);
		assertEquals(4,Roi.MOVING_HANDLE);
		assertEquals(0,Roi.RECTANGLE);
		assertEquals(1,Roi.OVAL);
		assertEquals(2,Roi.POLYGON);
		assertEquals(3,Roi.FREEROI);
		assertEquals(4,Roi.TRACED_ROI);
		assertEquals(5,Roi.LINE);
		assertEquals(6,Roi.POLYLINE);
		assertEquals(7,Roi.FREELINE);
		assertEquals(8,Roi.ANGLE);
		assertEquals(9,Roi.COMPOSITE);
		assertEquals(10,Roi.POINT);
		assertEquals(5,Roi.HANDLE_SIZE); 
		assertEquals(-1,Roi.NOT_PASTING); 
	}

	private void testValues(Roi roi, double perim, String name, int state, int type, int x, int y)
	{
		assertNotNull(roi);
		assertEquals(perim,roi.getLength(),Assert.DOUBLE_TOL);
		assertEquals(name,roi.getName());
		assertEquals(state,roi.getState());
		assertEquals(type,roi.getType());
		assertEquals(x,roi.getBounds().x);
		assertEquals(y,roi.getBounds().y);
	}
	
	@Test
	public void testRoiIntIntIntInt() {
		int x,y,w,h;
		
		x = 2;
		y = 3;
		w = 7;
		h = 8;
		
		roi = new Roi(x,y,w,h);
		testValues(roi,30,null,Roi.NORMAL,Roi.RECTANGLE,x,y);
		
		// additional testing:
		//  this constructor version calls Roi(x,y,w,h,0) so test that constructor with arc length of 0
	}

	@Test
	public void testRoiIntIntIntIntInt() {
		int x,y,w,h,arcSize;

		// try crazy vals
		x = -4;
		y = -13;
		w = -1;
		h = -1;
		arcSize = -1;

		roi = new Roi(x,y,w,h,arcSize);
		assertNotNull(roi);
		
		// try legit vals : arcsize = 0
		x = 4;
		y = 9;
		w = 16;
		h = 12;
		arcSize = 0;
		
		roi = new Roi(x,y,w,h,arcSize);
		testValues(roi,56,null,Roi.NORMAL,Roi.RECTANGLE,x,y);

		// try legit vals : arcsize != 0
		x = 14;
		y = 22;
		w = 77;
		h = 25;
		arcSize = 7;
		
		roi = new Roi(x,y,w,h,arcSize);
		testValues(roi,204,null,Roi.NORMAL,Roi.RECTANGLE,x,y);
	}

	@Test
	public void testRoiRectangle() {
		Rectangle rect;

		// try a bad rectangle
		rect = new Rectangle(0,0,0,0);
		roi = new Roi(rect);
		assertNotNull(roi);
		
		// try a legit rectangle
		rect = new Rectangle(33,8,1,17);
		roi = new Roi(rect);
		testValues(roi,36,null,Roi.NORMAL,Roi.RECTANGLE,33,8);
	}

	@Test
	public void testRoiIntIntImagePlus() {
		int sx,sy;
		ImagePlus ip;
		ImageProcessor proc;
		
		// try unusual data
		proc = new ByteProcessor(3,2,new byte[] {1,2,3,4,5,6},null);
		ip = new ImagePlus("Zoopy",proc);
		sx = -1000;
		sy = -2000;
		roi = new Roi(sx,sy,ip);
		assertNotNull(roi);
		
		// try valid data
		proc = new ByteProcessor(3,2,new byte[] {1,2,3,4,5,6},null);
		ip = new ImagePlus("Zoopy",proc);
		sx = 1;
		sy = 1;
		roi = new Roi(sx,sy,ip);
		testValues(roi,0,null,Roi.CONSTRUCTING,Roi.RECTANGLE,sx,sy);
	}

	@Test
	public void testRoiIntIntImagePlusInt() {
		int sx,sy;
		ImagePlus ip;
		ImageProcessor proc;

		// try valid data w/ arcSize == 0
		proc = new ByteProcessor(3,2,new byte[] {1,2,3,4,5,6},null);
		ip = new ImagePlus("Zoopy",proc);
		sx = 1;
		sy = 1;
		roi = new Roi(sx,sy,ip,0);
		testValues(roi,0,null,Roi.CONSTRUCTING,Roi.RECTANGLE,sx,sy);

		// try valid data w/ arcSize nonzero
		proc = new ByteProcessor(3,2,new byte[] {1,2,3,4,5,6},null);
		ip = new ImagePlus("Zoopy",proc);
		sx = 1;
		sy = 1;
		roi = new Roi(sx,sy,ip,3);
		testValues(roi,0,null,Roi.CONSTRUCTING,Roi.RECTANGLE,sx,sy);
	}

	@Test
	public void testSetLocation() {
		int x,y,w,h;
		
		// try legit vals
		x = 4;
		y = 9;
		w = 22;
		h = 14;
		
		roi = new Roi(x,y,w,h);

		assertEquals(x,roi.getBounds().x);
		assertEquals(y,roi.getBounds().y);
		
		roi.setLocation(-50,123);

		assertEquals(-50,roi.getBounds().x);
		assertEquals(123,roi.getBounds().y);
	}

	@Test
	public void testSetImage() {
		
		ImageProcessor proc = new ByteProcessor(3,3,new byte[] {1,2,3,4,5,6,7,8,9},null);
		ImagePlus ip = new ImagePlus("Wackaloop",proc);
		
		roi = new Roi(1,2,3,4);
		assertNull(roi.getImage());
		roi.setImage(ip);
		assertEquals(ip,roi.getImage());
	}

	@Test
	public void testGetImageID() {
		ImageProcessor proc = new ByteProcessor(3,3,new byte[] {1,2,3,4,5,6,7,8,9},null);
		ImagePlus ip = new ImagePlus("Wackaloop",proc);
		
		roi = new Roi(1,2,3,4);
		assertEquals(0,roi.getImageID());
		roi.setImage(ip);
		assertEquals(ip.getID(),roi.getImageID());
	}

	@Test
	public void testGetType() {
		roi = new Roi(1,2,3,4);
		assertEquals(Roi.RECTANGLE,roi.getType());
		
		roi = new PolygonRoi(new int[]{1},new int[]{3},1,Roi.POLYGON);
		assertEquals(Roi.POLYGON,roi.getType());
		
		roi = new PolygonRoi(new int[]{1},new int[]{3},1,Roi.FREEROI);
		assertEquals(Roi.FREEROI,roi.getType());
		
		roi = new PolygonRoi(new int[]{1},new int[]{3},1,Roi.FREELINE);
		assertEquals(Roi.FREELINE,roi.getType());
		
		roi = new PolygonRoi(new int[]{1},new int[]{3},1,Roi.POLYLINE);
		assertEquals(Roi.POLYLINE,roi.getType());
		
		roi = new PolygonRoi(new int[]{1},new int[]{3},1,Roi.TRACED_ROI);
		assertEquals(Roi.TRACED_ROI,roi.getType());
		
		roi = new PolygonRoi(new int[]{1},new int[]{3},1,Roi.ANGLE);
		assertEquals(Roi.ANGLE,roi.getType());
		
		roi = new PolygonRoi(new int[]{1},new int[]{3},1,Roi.POINT);
		assertEquals(Roi.POINT,roi.getType());
		
		roi = new OvalRoi(1,2,3,4);
		assertEquals(Roi.OVAL,roi.getType());
		
		roi = new Line(1,2,3,4);
		assertEquals(Roi.LINE,roi.getType());

		roi = new Roi(1,2,3,4);
		roi = new ShapeRoi(roi,1,2,false,false,false,100);
		assertEquals(Roi.COMPOSITE,roi.getType());
	}


	@Test
	public void testGetTypeAsString() {
		roi = new Roi(1,2,3,4);
		assertEquals("Rectangle",roi.getTypeAsString());
		
		roi = new PolygonRoi(new int[]{1},new int[]{3},1,Roi.POLYGON);
		assertEquals("Polygon",roi.getTypeAsString());
		
		roi = new PolygonRoi(new int[]{1},new int[]{3},1,Roi.FREEROI);
		assertEquals("Freehand",roi.getTypeAsString());
		
		roi = new PolygonRoi(new int[]{1},new int[]{3},1,Roi.FREELINE);
		assertEquals("Freeline",roi.getTypeAsString());
		
		roi = new PolygonRoi(new int[]{1},new int[]{3},1,Roi.POLYLINE);
		assertEquals("Polyline",roi.getTypeAsString());
		
		roi = new PolygonRoi(new int[]{1},new int[]{3},1,Roi.TRACED_ROI);
		assertEquals("Traced",roi.getTypeAsString());
		
		roi = new PolygonRoi(new int[]{1},new int[]{3},1,Roi.ANGLE);
		assertEquals("Angle",roi.getTypeAsString());
		
		roi = new PolygonRoi(new int[]{1},new int[]{3},1,Roi.POINT);
		assertEquals("Point",roi.getTypeAsString());
		
		roi = new OvalRoi(1,2,3,4);
		assertEquals("Oval",roi.getTypeAsString());
		
		roi = new Line(1,2,3,4);
		assertEquals("Straight Line",roi.getTypeAsString());

		roi = new ShapeRoi(new Roi(1,2,3,4),1,2,false,false,false,100);
		assertEquals("Composite",roi.getTypeAsString());
	}

	@Test
	public void testGetState() {
		roi = new Roi(1,2,3,4);
		assertEquals(Roi.NORMAL,roi.getState());

		roi = new PolygonRoi(new int[]{1},new int[]{3},1,Roi.POLYLINE);
		assertEquals(Roi.CONSTRUCTING,roi.getState());
		
		// can't test
		// MOVING - can't get an ImageCanvas w/o gui so code never reached
		// RESIZING - not present in Roi code - dead constant?
		// MOVING_HANDLE - only settable via protected method
	}

	@Test
	public void testGetLength() {
		int w,h;

		// pathological case 1
		roi = new Roi(0,0,-1,-1);
		assertEquals(4,roi.getLength(),Assert.DOUBLE_TOL);  // TODO - looks like a bug

		// pathological case 2
		roi = new Roi(0,0,0,0);
		assertEquals(4,roi.getLength(),Assert.DOUBLE_TOL);  // TODO - looks like a bug

		// most basic case
		w = 1; h = 1;
		roi = new Roi(0,0,w,h);
		assertEquals(2*w+2*h,roi.getLength(),Assert.DOUBLE_TOL);

		// arbitrary case
		w = 14; h = 33;
		roi = new Roi(0,0,w,h);
		assertEquals(2*w+2*h,roi.getLength(),Assert.DOUBLE_TOL);
		
		// arbitrary case with x and y scaling
		ImageProcessor proc = new ByteProcessor(1,1,new byte[]{1},null);
		ImagePlus ip = new ImagePlus("Fred",proc);
		Calibration cal = new Calibration();
		cal.pixelWidth = 0.5;
		cal.pixelHeight = 0.33;
		ip.setCalibration(cal);
		roi = new Roi(0,0,1,1);
		roi.setImage(ip);
		Rectangle r = roi.getBounds();
		double expected = 2.0*r.width*cal.pixelWidth + 2.0*r.height*cal.pixelHeight; 
		assertEquals(expected,roi.getLength(),Assert.DOUBLE_TOL);
	}

	@Test
	public void testGetFeretsDiameter() {
		
		// try a rectangle
		roi = new Roi(1,1,6,9);
		assertEquals(10.81665,roi.getFeretsDiameter(),Assert.DOUBLE_TOL);
		
		// try a polyline
		roi = new PolygonRoi(new int[]{1,4,7,9},new int[]{3,18,24,33},4,Roi.POLYLINE);
		assertEquals(31.04835,roi.getFeretsDiameter(),Assert.DOUBLE_TOL);		
	}

	@Test
	public void testGetFeretValues() {
		double[] values;
		
		// try a rectangle
		roi = new Roi(2,8,14,33);
		values = roi.getFeretValues();
		Assert.assertDoubleArraysEqual(new double[]{35.84690,112.98872,14,2,8},
										values, Assert.DOUBLE_TOL);
		// try a polyline
		roi = new PolygonRoi(new int[]{13,7,19,22},new int[]{11,14,8,16},4,Roi.POLYLINE);
		values = roi.getFeretValues();
		Assert.assertDoubleArraysEqual(new double[]{15.13275,172.40536,7.53998,7,14},
										values, Assert.DOUBLE_TOL);
		
		// try an oval
		roi = new OvalRoi(4,22,88,27);
		values = roi.getFeretValues();
		Assert.assertDoubleArraysEqual(new double[]{88.14193,176.74805,27,4,33},
										values, Assert.DOUBLE_TOL);
		
		// try an angle
		roi = new PolygonRoi(new int[]{1,4,9},new int[]{3,17,11},3,Roi.ANGLE);
		values = roi.getFeretValues();
		Assert.assertDoubleArraysEqual(new double[]{14.31782,102.09476,6.16189,1,3},
										values, Assert.DOUBLE_TOL);
		
		// try a point
		roi = new PolygonRoi(new int[]{33},new int[]{14},1,Roi.POINT);
		values = roi.getFeretValues();
		Assert.assertDoubleArraysEqual(new double[]{0,0,0,33,14},
										values, Assert.DOUBLE_TOL);
		
		// try an Roi with a scaled calibration
		ImageProcessor proc = new ByteProcessor(1,1,new byte[]{1},null);
		ImagePlus ip = new ImagePlus("Janus",proc);
		Calibration cal = new Calibration();
		cal.pixelWidth = 0.6;
		cal.pixelHeight = 0.125;
		ip.setCalibration(cal);
		roi = new PolygonRoi(new int[]{6,8,10,12,14},new int[]{22,18,41,33,15},5,Roi.POLYLINE);
		roi.setImage(ip);
		values = roi.getFeretValues();
		Assert.assertDoubleArraysEqual(new double[]{4.87910,10.33110,2.98468,3.60000,2.75},
										values, Assert.DOUBLE_TOL);
	}

	@Test
	public void testGetConvexHull() {

		// other Rois override things like getPolygon() and getConvexHull() so can only test rectangles
		
		roi = new Roi(0,0,0,0);
		assertTrue(polysEqual(roi.getPolygon(),roi.getConvexHull()));
		
		roi = new Roi(-1,-1,-1,-1);
		assertTrue(polysEqual(roi.getPolygon(),roi.getConvexHull()));
		
		roi = new Roi(1,1,1,1);
		assertTrue(polysEqual(roi.getPolygon(),roi.getConvexHull()));
		
		roi = new Roi(1,6,13,8);
		assertTrue(polysEqual(roi.getPolygon(),roi.getConvexHull()));
		
		roi = new Roi(22,14,88,106);
		assertTrue(polysEqual(roi.getPolygon(),roi.getConvexHull()));
	}

	@Test
	public void testGetBounds() {
		Rectangle r;
		
		if (IJInfo.RUN_ENHANCED_TESTS)
		{
			r = new  Rectangle(0,0,0,0);
			roi = new Roi(r);
			assertTrue(rectsEqual(r,roi.getBounds()));
			
			r = new  Rectangle(-1,-1,-1,-1);
			roi = new Roi(r);
			assertTrue(rectsEqual(r,roi.getBounds()));
		}
		
		r = new  Rectangle(1,1,1,1);
		roi = new Roi(r);
		assertTrue(rectsEqual(r,roi.getBounds()));
		
		r = new  Rectangle(1,99,66,13);
		roi = new Roi(r);
		assertTrue(rectsEqual(r,roi.getBounds()));
		
		r = new  Rectangle(200,2000,10000,9678);
		roi = new Roi(r);
		assertTrue(rectsEqual(r,roi.getBounds()));
	}

	@Test
	public void testGetPolygon() {
		Rectangle r;
		
		if (IJInfo.RUN_ENHANCED_TESTS)
		{
			r = new  Rectangle(0,0,0,0);
			roi = new Roi(r);
			assertTrue(rectsEqual(r,roi.getPolygon().getBounds()));

			r = new  Rectangle(-1,-1,-1,-1);
			roi = new Roi(r);
			assertTrue(rectsEqual(r,roi.getPolygon().getBounds()));
		}
		
		r = new  Rectangle(1,1,1,1);
		roi = new Roi(r);
		assertTrue(rectsEqual(r,roi.getPolygon().getBounds()));
		
		r = new  Rectangle(1,99,66,13);
		roi = new Roi(r);
		assertTrue(rectsEqual(r,roi.getPolygon().getBounds()));
		
		r = new  Rectangle(200,2000,10000,9678);
		roi = new Roi(r);
		assertTrue(rectsEqual(r,roi.getPolygon().getBounds()));
	}

	@Test
	public void testGetFloatPolygon() {
		roi = new Roi(1,2,3,4);
		assertNull(roi.getFloatPolygon());
	}

	@Test
	public void testClone() {
		ImageProcessor proc = new ByteProcessor(1,1,new byte[]{1},null);
		ImagePlus ip = new ImagePlus("Fweek",proc);
		roi = new Roi(1,2,3,4);
		roi.setImage(ip);
		assertNotNull(roi.clone());
	}

	@Test
	public void testNudge() {
		
		// the underlying nudge() method has some gui components. Can't fully test.
		
		Rectangle r;
		ImageProcessor proc = new FloatProcessor(2,2,new float[]{1,2,3,4},null);
		ImagePlus ip = new ImagePlus("Fwang",proc);

		// UP arrow
		
		// setup
		r = new Rectangle(1,1,1,1);
		roi = new Roi(r);
		roi.setImage(ip);

		// nudge once - should work
		roi.nudge(KeyEvent.VK_UP);
		assertTrue(rectsEqual(new Rectangle(1,0,1,1),roi.getBounds()));

		// nudge again - should hit edge and do nothing
		roi.nudge(KeyEvent.VK_UP);
		assertTrue(rectsEqual(new Rectangle(1,0,1,1),roi.getBounds()));

		// DOWN arrow
		
		// setup
		r = new Rectangle(0,0,1,1);
		roi = new Roi(r);
		roi.setImage(ip);

		// nudge once - should work
		roi.nudge(KeyEvent.VK_DOWN);
		assertTrue(rectsEqual(new Rectangle(0,1,1,1),roi.getBounds()));

		// nudge again - should hit edge and do nothing
		roi.nudge(KeyEvent.VK_DOWN);
		assertTrue(rectsEqual(new Rectangle(0,1,1,1),roi.getBounds()));

		// RIGHT arrow
		
		// setup
		r = new Rectangle(0,0,1,1);
		roi = new Roi(r);
		roi.setImage(ip);

		// nudge once - should work
		roi.nudge(KeyEvent.VK_RIGHT);
		assertTrue(rectsEqual(new Rectangle(1,0,1,1),roi.getBounds()));

		// nudge again - should hit edge and do nothing
		roi.nudge(KeyEvent.VK_RIGHT);
		assertTrue(rectsEqual(new Rectangle(1,0,1,1),roi.getBounds()));

		// LEFT arrow
		
		// setup
		r = new Rectangle(1,1,1,1);
		roi = new Roi(r);
		roi.setImage(ip);

		// nudge once - should work
		roi.nudge(KeyEvent.VK_LEFT);
		assertTrue(rectsEqual(new Rectangle(0,1,1,1),roi.getBounds()));

		// nudge again - should hit edge and do nothing
		roi.nudge(KeyEvent.VK_LEFT);
		assertTrue(rectsEqual(new Rectangle(0,1,1,1),roi.getBounds()));
		
		// PASTE test after reaching edge
		// nudge during a paste even beyond edge of bounds - should work
		ImagePlus ip2 = new ImagePlus("Fwob",new FloatProcessor(1,1,new float[]{3},null));
		roi.startPaste(ip2);
		roi.nudge(KeyEvent.VK_LEFT);
		assertTrue(rectsEqual(new Rectangle(-1,1,1,1),roi.getBounds()));
		
		// note : can't test shape != RECTANGLE but it should also allow nudging beyond borders
	}

	@Test
	public void testNudgeCorner() {
		Rectangle r;
		ImageProcessor proc = new FloatProcessor(2,2,new float[]{1,2,3,4},null);
		ImagePlus ip = new ImagePlus("Pitfwang",proc);

		// UP arrow
		
		// setup
		r = new Rectangle(0,0,2,2);
		roi = new Roi(r);
		roi.setImage(ip);
		
		// nudge corner once - should work
		roi.nudgeCorner(KeyEvent.VK_UP);
		assertTrue(rectsEqual(new Rectangle(0,0,2,1),roi.getBounds()));
		
		// nudge again - should hit boundary and fail
		roi.nudgeCorner(KeyEvent.VK_UP);
		assertTrue(rectsEqual(new Rectangle(0,0,2,1),roi.getBounds()));

		// DOWN arrow
		
		// setup
		r = new Rectangle(0,0,1,1);
		roi = new Roi(r);
		roi.setImage(ip);
		
		// nudge corner once - should work
		roi.nudgeCorner(KeyEvent.VK_DOWN);
		assertTrue(rectsEqual(new Rectangle(0,0,1,2),roi.getBounds()));
		
		// nudge again - should hit boundary and fail
		roi.nudgeCorner(KeyEvent.VK_DOWN);
		assertTrue(rectsEqual(new Rectangle(0,0,1,2),roi.getBounds()));

		// LEFT arrow
		
		// setup
		r = new Rectangle(0,0,2,2);
		roi = new Roi(r);
		roi.setImage(ip);
		
		// nudge corner once - should work
		roi.nudgeCorner(KeyEvent.VK_LEFT);
		assertTrue(rectsEqual(new Rectangle(0,0,1,2),roi.getBounds()));
		
		// nudge again - should hit boundary and fail
		roi.nudgeCorner(KeyEvent.VK_LEFT);
		assertTrue(rectsEqual(new Rectangle(0,0,1,2),roi.getBounds()));

		// RIGHT arrow
		
		// setup
		r = new Rectangle(0,0,1,1);
		roi = new Roi(r);
		roi.setImage(ip);
		
		// nudge corner once - should work
		roi.nudgeCorner(KeyEvent.VK_RIGHT);
		assertTrue(rectsEqual(new Rectangle(0,0,2,1),roi.getBounds()));
		
		// nudge again - should hit boundary and fail
		roi.nudgeCorner(KeyEvent.VK_RIGHT);
		assertTrue(rectsEqual(new Rectangle(0,0,2,1),roi.getBounds()));
	}

	@Test
	public void testDraw() {
		if (IJInfo.RUN_GUI_TESTS) {
			// note - this method is all gui
		}
	}

	@Test
	public void testDrawOverlay() {
		if (IJInfo.RUN_GUI_TESTS) {
			// note - this method is all gui
		}
	}

	@Test
	public void testDrawPixelsImageProcessor() {
		ImageProcessor proc = new ByteProcessor(3,3,new byte[]{0,0,0,0,0,0,0,0,0},null);
		Rectangle r = new Rectangle(0,0,3,3);
		roi = new Roi(r);
		roi.drawPixels(proc);
		// TODO - left off here
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
	public void testUpdate() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetMask() {
		fail("Not yet implemented");
	}

	@Test
	public void testStartPaste() {
		fail("Not yet implemented");
	}

	@Test
	public void testEndPaste() {
		fail("Not yet implemented");
	}

	@Test
	public void testAbortPaste() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetAngle() {
		fail("Not yet implemented");
	}

	@Test
	public void testSetColor() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetColor() {
		fail("Not yet implemented");
	}

	@Test
	public void testSetStrokeColor() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetStrokeColor() {
		fail("Not yet implemented");
	}

	@Test
	public void testSetFillColor() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetFillColor() {
		fail("Not yet implemented");
	}

	@Test
	public void testSetDefaultFillColor() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetDefaultFillColor() {
		fail("Not yet implemented");
	}

	@Test
	public void testCopyAttributes() {
		fail("Not yet implemented");
	}

	@Test
	public void testUpdateWideLine() {
		fail("Not yet implemented");
	}

	@Test
	public void testSetNonScalable() {
		fail("Not yet implemented");
	}

	@Test
	public void testSetStrokeWidth() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetStrokeWidth() {
		fail("Not yet implemented");
	}

	@Test
	public void testSetStroke() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetStroke() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetName() {
		fail("Not yet implemented");
	}

	@Test
	public void testSetName() {
		fail("Not yet implemented");
	}

	@Test
	public void testSetPasteMode() {
		fail("Not yet implemented");
	}

	@Test
	public void testSetRoundRectArcSize() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetPasteMode() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetCurrentPasteMode() {
		fail("Not yet implemented");
	}

	@Test
	public void testIsArea() {
		fail("Not yet implemented");
	}

	@Test
	public void testIsLine() {
		fail("Not yet implemented");
	}

	@Test
	public void testIsDrawingTool() {
		fail("Not yet implemented");
	}

	@Test
	public void testIsVisible() {
		fail("Not yet implemented");
	}

	@Test
	public void testEqualsObject() {
		fail("Not yet implemented");
	}

	@Test
	public void testToString() {
		fail("Not yet implemented");
	}

}
