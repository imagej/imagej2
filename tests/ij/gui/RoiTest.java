package ij.gui;

import static org.junit.Assert.*;

import org.junit.Test;

import ij.Assert;
import ij.IJ;
import ij.ImagePlus;
import ij.Undo;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.geom.PathIterator;
import java.util.*;

import ij.*;
import ij.measure.Calibration;
import ij.process.*;

public class RoiTest {

	Roi roi;

	// helper method
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
	
	// helper method
	private boolean doublesEqual(Double[] a, Double[] b, double tol) {
		
		if (a.length != b.length)
			return false;
		
		for (int i = 0; i < a.length; i++)
			if (Math.abs(a[i]-b[i]) > tol)
				return false;
		
		return true;
	}

	// helper method
	private boolean polysEqual(Polygon a, Polygon b){
		Double[] da = new Double[]{}, db = new Double[]{};
		ArrayList<Double> ptsA = getCoords(a);
		ArrayList<Double> ptsB = getCoords(b);
		return doublesEqual(ptsA.toArray(da), ptsB.toArray(db), Assert.DOUBLE_TOL);
	}
	
	// helper method
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

	// helper method
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
		
		// note - can't test some subcases
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
		Roi newRoi = (Roi) roi.clone();
		assertNotNull(newRoi);
		assertEquals(roi,newRoi);
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

	// helper method
	private void tryOneDraw(int w, int h, Rectangle region, byte[] expectedResults) {
		ImageProcessor proc = new ByteProcessor(w,h,new byte[w*h],null);
		proc.setValue(3);
		roi = new Roi(region);
		roi.drawPixels(proc);
		assertArrayEquals(expectedResults,(byte[])proc.getPixels());
	}
	
	@Test
	public void testDrawPixelsImageProcessor() {
		
		tryOneDraw(1,1,new Rectangle(0,0,1,1), new byte[]{3});
		tryOneDraw(2,2,new Rectangle(0,0,1,1), new byte[]{3,0,0,0});
		tryOneDraw(2,2,new Rectangle(1,0,1,2), new byte[]{0,3,0,3});
		tryOneDraw(3,3,new Rectangle(0,0,3,3), new byte[]{3,3,3,3,0,3,3,3,3});
		tryOneDraw(3,3,new Rectangle(1,1,1,1), new byte[]{0,0,0,0,3,0,0,0,0});
		tryOneDraw(3,3,new Rectangle(0,1,2,2), new byte[]{0,0,0,3,3,0,3,3,0});
	}

	@Test
	public void testContains() {
		roi = new Roi(0,0,4,3);
		
		// corners
		assertTrue(roi.contains(0,0));
		assertTrue(roi.contains(0,2));
		assertTrue(roi.contains(3,2));
		assertTrue(roi.contains(3,0));
		
		// an interior point
		assertTrue(roi.contains(1,1));
		
		// outside the boundaries
		assertFalse(roi.contains(0,-1));
		assertFalse(roi.contains(-1,0));
		assertFalse(roi.contains(0,3));
		assertFalse(roi.contains(4,2));
		assertFalse(roi.contains(4,0));
		assertFalse(roi.contains(1000,2000));
		assertFalse(roi.contains(-2,-2));
	}

	@Test
	public void testIsHandle() {
		// note - can't test. Needs instance var ic to be nonnull to return anything other than -1. This instance var
		//   is only set when the Roi's associated ImagePlus has a Window with an Canvas. Since gui is not active we
		//   can't recreate these conditions.
		roi = new Roi(1,2,3,4);
		assertEquals(-1,roi.isHandle(1, 1));
	}

	// helper method
	private void tryUpdateCase(boolean add, boolean sub, int expectedState) {
		Roi.previousRoi = new Roi(1,2,3,4);
		assertEquals(Roi.NO_MODS,Roi.previousRoi.modState);
		roi.update(add, sub);
		assertEquals(expectedState,Roi.previousRoi.modState);
	}
	
	@Test
	public void testUpdate() {
		Roi savedRoi = Roi.previousRoi;
		
		roi = new Roi(8,7,24,33);

		// try all possible cases when previousRoi is null
		Roi.previousRoi = null;
		
		roi.update(false, false);
		assertNull(Roi.previousRoi);
		
		roi.update(false, true);
		assertNull(Roi.previousRoi);
		
		roi.update(true, false);
		assertNull(Roi.previousRoi);
		
		roi.update(true, true);
		assertNull(Roi.previousRoi);
		
		// try all possible cases when previousRoi is nonNull
		
		tryUpdateCase(false,false,Roi.NO_MODS);
		tryUpdateCase(false,true,Roi.SUBTRACT_FROM_ROI);
		tryUpdateCase(true,false,Roi.ADD_TO_ROI);
		tryUpdateCase(true,true,Roi.ADD_TO_ROI);
		
		Roi.previousRoi = savedRoi;
	}

	@Test
	public void testGetMask() {
		ImageProcessor proc;
		
		// regular rect
		roi = new Roi(0,0,3,3);
		assertNull(roi.getMask());
		
		// rounded rect
		roi = new Roi(0,0,3,3,1);
		proc = roi.getMask();
		assertNotNull(proc);
		assertArrayEquals(new byte[]{0,-1,-1,-1,-1,-1,-1,-1,-1},(byte[])proc.getPixels()); // -1 == 255

		// rounded rect
		roi = new Roi(0,0,4,4,3);
		proc = roi.getMask();
		assertNotNull(proc);
		assertArrayEquals(new byte[]{0,0,-1,0,0,-1,-1,-1,-1,-1,-1,-1,0,-1,-1,-1},(byte[])proc.getPixels()); // -1 == 255
	}

	@Test
	public void testStartPaste() {
		ImageProcessor proc = new ByteProcessor(1,1,new byte[]{1},null);
		ImagePlus ip = new ImagePlus("Loki",proc);
		roi = new Roi(1,2,3,4);
		roi.setImage(ip);
		ImageProcessor proc2 = new ByteProcessor(1,1,new byte[]{7},null);
		ImagePlus ip2 = new ImagePlus("Hercules",proc2);
		
		assertNull(proc.getSnapshotPixels());
		assertEquals(Roi.NOT_PASTING,roi.getPasteMode());
		roi.startPaste(ip2);
		assertNotNull(proc.getSnapshotPixels());
		assertEquals(Blitter.COPY,roi.getPasteMode());
		
		// underlying method does some gui stuff we can't test
	}


	@Test
	public void testEndPaste() {
		ImageProcessor proc, proc2;
		ImagePlus ip, ip2;
		byte[] snapshot;
		int origPasteMode;
		
		origPasteMode = Roi.pasteMode;

		// try to endPaste with no paste started
		proc = new ByteProcessor(1,1,new byte[]{1},null);
		ip = new ImagePlus("Loki",proc);
		roi = new Roi(1,2,3,4);
		roi.setImage(ip);
		assertNull(proc.getSnapshotPixels());
		assertEquals(Roi.NOT_PASTING,roi.getPasteMode());
		
		roi.endPaste();
		assertNull(proc.getSnapshotPixels());
		assertEquals(Roi.NOT_PASTING,roi.getPasteMode());
		

		// try a legit paste in COPY mode
		proc = new ByteProcessor(1,1,new byte[]{1},null);
		ip = new ImagePlus("Loki",proc);
		proc2 = new ByteProcessor(1,1,new byte[]{7},null);
		ip2 = new ImagePlus("Hercules",proc2);
		roi = new Roi(0,0,1,1);
		roi.setImage(ip);
		ip.setRoi(roi);
		
		Roi.setPasteMode(Blitter.COPY);
		roi.startPaste(ip2);
		snapshot = (byte[]) proc.getSnapshotPixels();
		assertNotNull(snapshot);
		assertEquals(1,snapshot[0]);
		assertEquals(Blitter.COPY,roi.getPasteMode());
		
		roi.endPaste();
		
		assertEquals(Roi.NOT_PASTING,roi.getPasteMode());
		snapshot = (byte[]) proc.getSnapshotPixels();
		assertEquals(7,snapshot[0]);
		
		// try a legit paste in some other mode
		proc = new ByteProcessor(1,1,new byte[]{1},null);
		ip = new ImagePlus("Loki",proc);
		proc2 = new ByteProcessor(1,1,new byte[]{7},null);
		ip2 = new ImagePlus("Hercules",proc2);
		roi = new Roi(0,0,1,1);
		roi.setImage(ip);
		ip.setRoi(roi);
		
		Roi.setPasteMode(Blitter.ADD);
		roi.startPaste(ip2);
		snapshot = (byte[]) proc.getSnapshotPixels();
		assertNotNull(snapshot);
		assertEquals(1,snapshot[0]);
		assertEquals(Blitter.ADD,roi.getPasteMode());
		
		roi.endPaste();
		
		assertEquals(Roi.NOT_PASTING,roi.getPasteMode());
		snapshot = (byte[]) proc.getSnapshotPixels();
		assertEquals(8,snapshot[0]);
		
		// try a non-rectangular roi
		proc = new ByteProcessor(3,3,new byte[] {0,0,0,0,0,0,0,0,0},null);
		ip = new ImagePlus("Loki",proc);
		proc2 = new ByteProcessor(2,2,new byte[]{1,2,3,4},null);
		ip2 = new ImagePlus("Hercules",proc2);
		roi = new OvalRoi(1,1,1,1);
		roi.setImage(ip);
		ip.setRoi(roi);
		
		Roi.setPasteMode(Blitter.COPY);
		roi.startPaste(ip2);
		
		roi.endPaste();
		assertArrayEquals(new byte[]{0,0,0,0,1,2,0,3,4},(byte[])proc.getPixels());
		
		// restore static variables
		Roi.setPasteMode(origPasteMode);
	}

	@Test
	public void testAbortPaste() {
		ImageProcessor proc, proc2;
		ImagePlus ip, ip2;
		byte[] snapshot;

		// start and abort a paste
		
		// setup
		proc = new ByteProcessor(1,1,new byte[]{1},null);
		ip = new ImagePlus("Loki",proc);
		proc2 = new ByteProcessor(1,1,new byte[]{7},null);
		ip2 = new ImagePlus("Hercules",proc2);
		roi = new Roi(0,0,1,1);
		roi.setImage(ip);
		ip.setRoi(roi);
		
		roi.startPaste(ip2);
		
		assertEquals(Blitter.COPY,roi.getPasteMode());
		snapshot = (byte[]) proc.getSnapshotPixels();
		assertNotNull(snapshot);
		assertEquals(1,snapshot[0]);
		
		roi.abortPaste();
		
		assertEquals(Roi.NOT_PASTING,roi.getPasteMode());
		snapshot = (byte[]) proc.getSnapshotPixels();
		assertEquals(1,snapshot[0]);
	}

	// note - Roi.java: this method belongs elsewhere in some utility class
	@Test
	public void testGetAngle() {
		Calibration cal;
		ImagePlus ip;
		ImageProcessor proc;
		
		// try basic functionality
		roi = new Roi(0,0,22,13);
		assertEquals(0, roi.getAngle(0,0,0,0), Assert.DOUBLE_TOL);
		assertEquals(0, roi.getAngle(1,1,1,1), Assert.DOUBLE_TOL);
		assertEquals(-90, roi.getAngle(0,0,0,1), Assert.DOUBLE_TOL);
		assertEquals(0, roi.getAngle(0,0,1,0), Assert.DOUBLE_TOL);
		assertEquals(-45, roi.getAngle(0,0,1,1), Assert.DOUBLE_TOL);
		assertEquals(90, roi.getAngle(0,1,0,0), Assert.DOUBLE_TOL);
		assertEquals(180, roi.getAngle(1,0,0,0), Assert.DOUBLE_TOL);
		assertEquals(135, roi.getAngle(1,1,0,0), Assert.DOUBLE_TOL);
		assertEquals(118.81079, roi.getAngle(8,13,-3,-7), Assert.DOUBLE_TOL);
		assertEquals(-70.55997, roi.getAngle(1,2,7,19), Assert.DOUBLE_TOL);
		assertEquals(142.65065, roi.getAngle(400,300,20,10), Assert.DOUBLE_TOL);
		
		// now include a roi with a calibrated imageplus and the alt key is down
		cal = new Calibration();
		cal.pixelHeight = 1.2;
		cal.pixelWidth = 0.7;
		proc = new ByteProcessor(1,1,new byte[]{1},null);
		ip = new ImagePlus("Pan",proc);
		ip.setCalibration(cal);
		roi = new Roi(1,2,3,4);
		roi.setImage(ip);
		IJ.setKeyDown(KeyEvent.VK_ALT);
		assertTrue(IJ.altKeyDown());
		assertEquals(0, roi.getAngle(0,0,0,0), Assert.DOUBLE_TOL);
		assertEquals(0, roi.getAngle(1,1,1,1), Assert.DOUBLE_TOL);
		assertEquals(-90, roi.getAngle(0,0,0,1), Assert.DOUBLE_TOL);
		assertEquals(0, roi.getAngle(0,0,1,0), Assert.DOUBLE_TOL);
		assertEquals(-45, roi.getAngle(0,0,1,1), Assert.DOUBLE_TOL);
		assertEquals(90, roi.getAngle(0,1,0,0), Assert.DOUBLE_TOL);
		assertEquals(180, roi.getAngle(1,0,0,0), Assert.DOUBLE_TOL);
		assertEquals(135, roi.getAngle(1,1,0,0), Assert.DOUBLE_TOL);
		assertEquals(118.81079, roi.getAngle(8,13,-3,-7), Assert.DOUBLE_TOL);
		assertEquals(-70.55997, roi.getAngle(1,2,7,19), Assert.DOUBLE_TOL);
		assertEquals(142.65065, roi.getAngle(400,300,20,10), Assert.DOUBLE_TOL);
		IJ.setKeyUp(KeyEvent.VK_ALT);
		
		// now include a roi with a calibrated imageplus and the alt key is NOT down
		cal = new Calibration();
		cal.pixelHeight = 1.2;
		cal.pixelWidth = 0.7;
		proc = new ByteProcessor(1,1,new byte[]{1},null);
		ip = new ImagePlus("Pan",proc);
		ip.setCalibration(cal);
		roi = new Roi(1,2,3,4);
		roi.setImage(ip);
		assertFalse(IJ.altKeyDown());
		assertEquals(0, roi.getAngle(0,0,0,0), Assert.DOUBLE_TOL);
		assertEquals(0, roi.getAngle(1,1,1,1), Assert.DOUBLE_TOL);
		assertEquals(-90, roi.getAngle(0,0,0,1), Assert.DOUBLE_TOL);
		assertEquals(0, roi.getAngle(0,0,1,0), Assert.DOUBLE_TOL);
		assertEquals(-59.74356, roi.getAngle(0,0,1,1), Assert.DOUBLE_TOL);
		assertEquals(90, roi.getAngle(0,1,0,0), Assert.DOUBLE_TOL);
		assertEquals(180, roi.getAngle(1,0,0,0), Assert.DOUBLE_TOL);
		assertEquals(120.25644, roi.getAngle(1,1,0,0), Assert.DOUBLE_TOL);
		assertEquals(107.78797, roi.getAngle(8,13,-3,-7), Assert.DOUBLE_TOL);
		assertEquals(-78.36637, roi.getAngle(1,2,7,19), Assert.DOUBLE_TOL);
		assertEquals(127.39313, roi.getAngle(400,300,20,10), Assert.DOUBLE_TOL);
	}

	@Test
	public void testSetAndGetColor() {

		Color savedColor = Roi.getColor();
		
		Roi.setColor(Color.black);
		assertEquals(Color.black,Roi.getColor());
		
		Roi.setColor(Color.magenta);
		assertEquals(Color.magenta,Roi.getColor());

		Roi.setColor(savedColor);
	}

	@Test
	public void testSetAndGetStrokeColor() {
		roi = new Roi(1,2,3,4);
		roi.setStrokeColor(Color.orange);
		assertEquals(Color.orange,roi.getStrokeColor());
		
		roi.setStrokeColor(Color.green);
		assertEquals(Color.green,roi.getStrokeColor());
	}

	@Test
	public void testSetAndGetFillColor() {
		roi = new Roi(1,2,3,4);
		roi.setFillColor(Color.red);
		assertEquals(Color.red,roi.getFillColor());
		
		roi.setFillColor(Color.cyan);
		assertEquals(Color.cyan,roi.getFillColor());
	}

	@Test
	public void testSetAndGetDefaultFillColor() {
		Color savedColor = Roi.getDefaultFillColor();
		
		Roi.setDefaultFillColor(Color.yellow);
		assertEquals(Color.yellow,Roi.getDefaultFillColor());
		
		Roi.setDefaultFillColor(Color.white);
		assertEquals(Color.white,Roi.getDefaultFillColor());

		Roi.setDefaultFillColor(savedColor);
	}

	@Test
	public void testCopyAttributes() {
		Roi roi2 = new Roi(7,2,43,22);
		roi2.setFillColor(Color.orange);
		roi2.setStrokeColor(Color.blue);
		roi2.setStroke(new BasicStroke());
		
		roi = new Roi(1,2,3,4);
		
		assertFalse(roi.getFillColor() == roi2.getFillColor());
		assertFalse(roi.getStrokeColor() == roi2.getStrokeColor());
		assertFalse(roi.getStroke() == roi2.getStroke());
		
		roi.copyAttributes(roi2);

		assertTrue(roi.getFillColor() == roi2.getFillColor());
		assertTrue(roi.getStrokeColor() == roi2.getStrokeColor());
		assertTrue(roi.getStroke() == roi2.getStroke());
	}

	@Test
	public void testUpdateWideLine() {

		Color c, savedColor;
		
		savedColor = Roi.getColor();
		Roi.setColor(new Color(13, 99, 44));
		
		// isLine() false : should do nothing
		roi = new Roi(4,2,6,9);
		roi.setStrokeWidth(19);
		assertEquals(19,roi.getStrokeWidth(),Assert.FLOAT_TOL);
		roi.updateWideLine(99);
		assertEquals(19,roi.getStrokeWidth(),Assert.FLOAT_TOL);
		
		// isLine() true cases follow from here on in
		
		// make sure wideline is true after this
		roi = new Line(1,7,3,5);
		roi.updateWideLine(7);
		assertEquals(7,roi.getStrokeWidth(),Assert.DOUBLE_TOL);
		roi.setStrokeWidth(3);
		assertEquals(BasicStroke.CAP_BUTT,roi.getStroke().getEndCap());
		assertEquals(BasicStroke.JOIN_BEVEL,roi.getStroke().getLineJoin());

		// getStrokeColor != null case
		roi = new Line(1,7,3,5);
		roi.setStrokeColor(Color.magenta);
		assertNotNull(roi.getStrokeColor());
		roi.updateWideLine(17);
		assertEquals(17,roi.getStrokeWidth(),Assert.DOUBLE_TOL);

		// getStrokeColor == null case
		roi = new Line(1,7,3,5);
		roi.setStrokeColor(null);
		assertNull(roi.getStrokeColor());
		roi.updateWideLine(11);
		assertEquals(11,roi.getStrokeWidth(),Assert.DOUBLE_TOL);
		c = roi.getStrokeColor();
		assertNotNull(c);
		assertEquals(77,c.getAlpha());
		assertEquals(Roi.getColor().getRed(),c.getRed());
		assertEquals(Roi.getColor().getGreen(),c.getGreen());
		assertEquals(Roi.getColor().getBlue(),c.getBlue());
		
		Roi.setColor(savedColor);
	}

	@Test
	public void testSetNonScalable() {
		// note - just a setter. Might be used by TextRoi but all in nonpublic methods. Could be hard to tease out behavior.
		//   Just do a compile time test
		roi = new Roi(1,2,3,4);
		roi.setNonScalable(false);
		roi.setNonScalable(true);
	}

	@Test
	public void testSetStrokeWidth() {
		// wide line case
		roi = new Line(1,7,3,5);
		roi.updateWideLine(7);
		assertEquals(7,roi.getStrokeWidth(),Assert.DOUBLE_TOL);
		roi.setStrokeWidth(3);
		assertEquals(BasicStroke.CAP_BUTT,roi.getStroke().getEndCap());
		assertEquals(BasicStroke.JOIN_BEVEL,roi.getStroke().getLineJoin());
		
		// regular case and width <= 1
		roi = new Roi(1,2,3,4);
		roi.setFillColor(Color.orange);
		assertNotNull(roi.getFillColor());
		roi.setStrokeWidth(0.99999f);
		assertEquals(0.99999,roi.getStrokeWidth(),Assert.DOUBLE_TOL);
		assertNotNull(roi.getFillColor());

		// regular case and width > 1
		roi = new Roi(1,2,3,4);
		roi.setFillColor(Color.orange);
		assertNotNull(roi.getFillColor());
		roi.setStrokeWidth(1.00001f);
		assertEquals(1.00001,roi.getStrokeWidth(),Assert.DOUBLE_TOL);
		assertNull(roi.getFillColor());
	}

	@Test
	public void testGetStrokeWidth() {
		// mostly tested in previous test
		// need to test case where Roi's stroke == null
		roi = new Roi(1,2,3,4);
		assertNull(roi.getStroke());
		assertEquals(1,roi.getStrokeWidth(),Assert.DOUBLE_TOL);
	}

	@Test
	public void testSetAndGetStroke() {
		roi = new Roi(1,2,3,4);
		BasicStroke bs = new BasicStroke();
		assertFalse(bs == roi.getStroke());
		roi.setStroke(bs);
		assertTrue(bs == roi.getStroke());
	}

	@Test
	public void testSetAndGetName() {
		roi = new Roi(1,2,3,4);
		assertFalse("Floopy".equals(roi.getName()));
		roi.setName("Floopy");
		assertTrue("Floopy".equals(roi.getName()));
	}

	@Test
	public void testsPasteMode() {
		int savedMode = Roi.getCurrentPasteMode();
		
		Roi.setPasteMode(Blitter.AND);
		assertEquals(Blitter.AND, Roi.getCurrentPasteMode());
		
		Roi.setPasteMode(Blitter.ADD);
		assertEquals(Blitter.ADD, Roi.getCurrentPasteMode());
		
		Roi.setPasteMode(Blitter.AVERAGE);
		assertEquals(Blitter.AVERAGE, Roi.getCurrentPasteMode());
		
		Roi.setPasteMode(Blitter.COPY);
		assertEquals(Blitter.COPY, Roi.getCurrentPasteMode());
		
		Roi.setPasteMode(Blitter.COPY_INVERTED);
		assertEquals(Blitter.COPY_INVERTED, Roi.getCurrentPasteMode());
		
		Roi.setPasteMode(Blitter.COPY_TRANSPARENT);
		assertEquals(Blitter.COPY_TRANSPARENT, Roi.getCurrentPasteMode());
		
		Roi.setPasteMode(Blitter.COPY_ZERO_TRANSPARENT);
		assertEquals(Blitter.COPY_ZERO_TRANSPARENT, Roi.getCurrentPasteMode());
		
		Roi.setPasteMode(Blitter.DIFFERENCE);
		assertEquals(Blitter.DIFFERENCE, Roi.getCurrentPasteMode());
		
		Roi.setPasteMode(Blitter.DIVIDE);
		assertEquals(Blitter.DIVIDE, Roi.getCurrentPasteMode());
		
		Roi.setPasteMode(Blitter.MAX);
		assertEquals(Blitter.MAX, Roi.getCurrentPasteMode());
		
		Roi.setPasteMode(Blitter.MIN);
		assertEquals(Blitter.MIN, Roi.getCurrentPasteMode());
		
		Roi.setPasteMode(Blitter.MULTIPLY);
		assertEquals(Blitter.MULTIPLY, Roi.getCurrentPasteMode());
		
		Roi.setPasteMode(Blitter.OR);
		assertEquals(Blitter.OR, Roi.getCurrentPasteMode());
		
		Roi.setPasteMode(Blitter.SUBTRACT);
		assertEquals(Blitter.SUBTRACT, Roi.getCurrentPasteMode());
		
		Roi.setPasteMode(Blitter.XOR);
		assertEquals(Blitter.XOR, Roi.getCurrentPasteMode());
		
		Roi.setPasteMode(savedMode);
	}

	@Test
	public void testGetPasteMode() {
		
		int savedMode = Roi.getCurrentPasteMode();

		// should be not pasting if clipboard is empty
		
		roi = new Roi(1,2,3,4);
		assertEquals(Roi.NOT_PASTING, roi.getPasteMode());
		
		// try when clipboard populated
		
		ImagePlus img1 = new ImagePlus("Fred",new ByteProcessor(1,1,new byte[]{1},null));
		ImagePlus img2 = new ImagePlus("Zach",new ByteProcessor(1,1,new byte[]{1},null));
		roi.setImage(img1);
		roi.startPaste(img2);
		Roi.setPasteMode(Blitter.XOR);
		assertEquals(Blitter.XOR, roi.getPasteMode());

		Roi.setPasteMode(savedMode);
	}

	@Test
	public void testSetRoundRectArcSize() {

		roi = new Roi(1,2,3,4);
		assertFalse(roi.isDrawingTool());
		
		roi.setRoundRectArcSize(13);
		assertTrue(roi.isDrawingTool());
	}

	@Test
	public void testIsArea() {
		roi = new Roi(1,2,3,4);
		assertEquals(Roi.RECTANGLE,roi.getType());
		assertTrue(roi.isArea());
		
		roi = new PolygonRoi(new int[]{1},new int[]{3},1,Roi.POLYGON);
		assertEquals(Roi.POLYGON,roi.getType());
		assertTrue(roi.isArea());
		
		roi = new PolygonRoi(new int[]{1},new int[]{3},1,Roi.FREEROI);
		assertEquals(Roi.FREEROI,roi.getType());
		assertTrue(roi.isArea());
		
		roi = new PolygonRoi(new int[]{1},new int[]{3},1,Roi.FREELINE);
		assertEquals(Roi.FREELINE,roi.getType());
		assertFalse(roi.isArea());
		
		roi = new PolygonRoi(new int[]{1},new int[]{3},1,Roi.POLYLINE);
		assertEquals(Roi.POLYLINE,roi.getType());
		assertFalse(roi.isArea());
		
		roi = new PolygonRoi(new int[]{1},new int[]{3},1,Roi.TRACED_ROI);
		assertEquals(Roi.TRACED_ROI,roi.getType());
		assertTrue(roi.isArea());
		
		roi = new PolygonRoi(new int[]{1},new int[]{3},1,Roi.ANGLE);
		assertEquals(Roi.ANGLE,roi.getType());
		assertFalse(roi.isArea());
		
		roi = new PolygonRoi(new int[]{1},new int[]{3},1,Roi.POINT);
		assertEquals(Roi.POINT,roi.getType());
		assertFalse(roi.isArea());
		
		roi = new OvalRoi(1,2,3,4);
		assertEquals(Roi.OVAL,roi.getType());
		assertTrue(roi.isArea());
		
		roi = new Line(1,2,3,4);
		assertEquals(Roi.LINE,roi.getType());
		assertFalse(roi.isArea());

		roi = new Roi(1,2,3,4);
		roi = new ShapeRoi(roi,1,2,false,false,false,100);
		assertEquals(Roi.COMPOSITE,roi.getType());
		assertTrue(roi.isArea());
	}

	@Test
	public void testIsLine() {
		roi = new Roi(1,2,3,4);
		assertEquals(Roi.RECTANGLE,roi.getType());
		assertFalse(roi.isLine());
		
		roi = new PolygonRoi(new int[]{1},new int[]{3},1,Roi.POLYGON);
		assertEquals(Roi.POLYGON,roi.getType());
		assertFalse(roi.isLine());
		
		roi = new PolygonRoi(new int[]{1},new int[]{3},1,Roi.FREEROI);
		assertEquals(Roi.FREEROI,roi.getType());
		assertFalse(roi.isLine());
		
		roi = new PolygonRoi(new int[]{1},new int[]{3},1,Roi.FREELINE);
		assertEquals(Roi.FREELINE,roi.getType());
		assertTrue(roi.isLine());
		
		roi = new PolygonRoi(new int[]{1},new int[]{3},1,Roi.POLYLINE);
		assertEquals(Roi.POLYLINE,roi.getType());
		assertTrue(roi.isLine());
		
		roi = new PolygonRoi(new int[]{1},new int[]{3},1,Roi.TRACED_ROI);
		assertEquals(Roi.TRACED_ROI,roi.getType());
		assertFalse(roi.isLine());
		
		roi = new PolygonRoi(new int[]{1},new int[]{3},1,Roi.ANGLE);
		assertEquals(Roi.ANGLE,roi.getType());
		assertFalse(roi.isLine());
		
		roi = new PolygonRoi(new int[]{1},new int[]{3},1,Roi.POINT);
		assertEquals(Roi.POINT,roi.getType());
		assertFalse(roi.isLine());
		
		roi = new OvalRoi(1,2,3,4);
		assertEquals(Roi.OVAL,roi.getType());
		assertFalse(roi.isLine());
		
		roi = new Line(1,2,3,4);
		assertEquals(Roi.LINE,roi.getType());
		assertTrue(roi.isLine());

		roi = new Roi(1,2,3,4);
		roi = new ShapeRoi(roi,1,2,false,false,false,100);
		assertEquals(Roi.COMPOSITE,roi.getType());
		assertFalse(roi.isLine());
	}

	@Test
	public void testIsDrawingTool() {

		roi = new Roi(1,2,3,4);
		assertFalse(roi.isDrawingTool());
		
		roi.setRoundRectArcSize(13);
		assertTrue(roi.isDrawingTool());
	}

	@Test
	public void testIsVisible() {
		// note - underlying code will always return false when gui not active. can't really test
		
		roi = new Roi(1,2,3,4);
		assertFalse(roi.isVisible());
	}

	@Test
	public void testEqualsObject() {
		Roi roi1, roi2;
		
		roi1 = new Roi(1,2,3,4);
		
		// test a against a nonRoi object
		assertFalse(roi1.equals("Zappaploo"));
		
		// types differ
		roi2 = new Line(1,2,3,4);
		assertFalse(roi1.equals(roi2));
		
		// bounds differ
		roi2 = new Roi(1,2,4,5);
		assertFalse(roi1.equals(roi2));

		// length differ
		/*
		 * unreachable test case: need to define two rois of same type and same bounds whose length is not equal. not possible.
		assertFalse(roi1.equals(roi2));
		*/
		
		// otherwise should be equal
		roi1 = new Roi(3,4,7,8);
		roi2 = new Roi(3,4,7,8);
		assertTrue(roi1.equals(roi2));
	}

	@Test
	public void testToString() {
		roi = new Roi(5,-3,18,8080);
		assertEquals(Roi.RECTANGLE,roi.getType());
		assertEquals("Roi[Rectangle, x=5, y=-3, width=18, height=8080]",roi.toString());
		
		roi = new PolygonRoi(new int[]{1,7},new int[]{3,19},2,Roi.POLYGON);
		assertEquals(Roi.POLYGON,roi.getType());
		assertEquals("Roi[Polygon, x=1, y=3, width=6, height=16]",roi.toString());
		
		roi = new PolygonRoi(new int[]{3,2,7},new int[]{9,12,9},3,Roi.FREEROI);
		assertEquals(Roi.FREEROI,roi.getType());
		assertEquals("Roi[Freehand, x=2, y=9, width=5, height=3]",roi.toString());
		
		roi = new PolygonRoi(new int[]{1,2,3,4},new int[]{6,7,8,9},4,Roi.FREELINE);
		assertEquals(Roi.FREELINE,roi.getType());
		assertEquals("Roi[Freeline, x=1, y=6, width=3, height=3]",roi.toString());
		
		roi = new PolygonRoi(new int[]{8,22,55},new int[]{19,3,99},3,Roi.POLYLINE);
		assertEquals(Roi.POLYLINE,roi.getType());
		assertEquals("Roi[Polyline, x=8, y=3, width=47, height=96]",roi.toString());
		
		roi = new PolygonRoi(new int[]{12,2,8,4},new int[]{1,7,9,31},4,Roi.TRACED_ROI);
		assertEquals(Roi.TRACED_ROI,roi.getType());
		assertEquals("Roi[Traced, x=2, y=1, width=10, height=30]",roi.toString());
		
		roi = new PolygonRoi(new int[]{1,2,3},new int[]{7,4,5},3,Roi.ANGLE);
		assertEquals(Roi.ANGLE,roi.getType());
		assertEquals("Roi[Angle, x=1, y=4, width=2, height=3]",roi.toString());
		
		roi = new PolygonRoi(new int[]{1},new int[]{3},1,Roi.POINT);
		assertEquals(Roi.POINT,roi.getType());
		assertEquals("Roi[Point, x=1, y=3, width=0, height=0]",roi.toString());
		
		roi = new OvalRoi(8,6,7,11);
		assertEquals(Roi.OVAL,roi.getType());
		assertEquals("Roi[Oval, x=8, y=6, width=7, height=11]",roi.toString());
		
		roi = new Line(9,22,909,303);
		assertEquals(Roi.LINE,roi.getType());
		assertEquals("Roi[Straight Line, x=9, y=22, width=900, height=281]",roi.toString());

		roi = new Roi(1,2,3,4);
		roi = new ShapeRoi(roi,1,2,false,false,false,100);
		assertEquals(Roi.COMPOSITE,roi.getType());
		assertEquals("Roi[Composite, x=1, y=2, width=3, height=4]",roi.toString());
	}

}
