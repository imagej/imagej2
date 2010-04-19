package ij.gui;

import static org.junit.Assert.*;
import ij.Assert;

import org.junit.Test;

import java.awt.*;
import java.awt.event.KeyEvent;

import ij.*;
import ij.measure.Calibration;
import ij.process.*;

public class LineTest {

	Line line;
	
	@Test
	public void testLineIntIntIntInt() {
		
		// create line roi
		line = new Line(4, 1, 3, 8);
		assertNotNull(line);
		
		// check public fields
		assertEquals(4,line.x1);
		assertEquals(3,line.x2);
		assertEquals(1,line.y1);
		assertEquals(8,line.y2);
		assertEquals(4,line.x1d,Assert.DOUBLE_TOL);
		assertEquals(3,line.x2d,Assert.DOUBLE_TOL);
		assertEquals(1,line.y1d,Assert.DOUBLE_TOL);
		assertEquals(8,line.y2d,Assert.DOUBLE_TOL);
		
		// check underlying values
		Rectangle r = line.getBounds();
		assertEquals(Roi.NORMAL,line.getState());
		assertEquals(Roi.LINE,line.getType());
		assertEquals(3,r.x);
		assertEquals(1,r.y);
		assertEquals(1,r.width);
		assertEquals(7,r.height);
	}

	@Test
	public void testLineDoubleDoubleDoubleDouble() {
		// create line roi
		line = new Line(4.7, 1.5, 3.999, 8.1);
		assertNotNull(line);
		
		// check public fields
		assertEquals(4,line.x1);
		assertEquals(3,line.x2);
		assertEquals(1,line.y1);
		assertEquals(8,line.y2);
		assertEquals(4.7,line.x1d,Assert.DOUBLE_TOL);
		assertEquals(3.999,line.x2d,Assert.DOUBLE_TOL);
		assertEquals(1.5,line.y1d,Assert.DOUBLE_TOL);
		assertEquals(8.1,line.y2d,Assert.DOUBLE_TOL);
		
		// check underlying values
		Rectangle r = line.getBounds();
		assertEquals(Roi.NORMAL,line.getState());
		assertEquals(Roi.LINE,line.getType());
		assertEquals(4,r.x);
		assertEquals(2,r.y);
		assertEquals(1,r.width);
		assertEquals(7,r.height);
	}

	@Test
	public void testLineIntIntImagePlus() {
		
		// the superclass constructor assumes the imageplus has an imagecanvas open. since we don't it crashes.
		if (IJInfo.RUN_ENHANCED_TESTS)
		{
			// create line roi
			ImagePlus ip = new ImagePlus("OrangeNavel",new FloatProcessor(2,3,new float[]{0,9,8,7,6,5},null));
			line = new Line(1,1,ip);
			assertNotNull(line);
			
			// note - once we can run this test we'll need to define nonzero values in the following assertions.
			
			// check public fields
			assertEquals(0,line.x1);
			assertEquals(0,line.x2);
			assertEquals(0,line.y1);
			assertEquals(0,line.y2);
			assertEquals(0,line.x1d,Assert.DOUBLE_TOL);
			assertEquals(0,line.x2d,Assert.DOUBLE_TOL);
			assertEquals(0,line.y1d,Assert.DOUBLE_TOL);
			assertEquals(0,line.y2d,Assert.DOUBLE_TOL);
			
			// check underlying values
			Rectangle r = line.getBounds();
			assertEquals(Roi.NORMAL,line.getState());
			assertEquals(Roi.LINE,line.getType());
			assertEquals(0,r.x);
			assertEquals(0,r.y);
			assertEquals(0,r.width);
			assertEquals(0,r.height);
		}
	}

	@Test
	public void testLineIntIntIntIntImagePlus() {
		// create line roi
		ImagePlus ip = new ImagePlus("OrangeNavel",new FloatProcessor(2,3,new float[]{0,9,8,7,6,5},null));
		line = new Line(1,0,5,15,ip);
		assertNotNull(line);
		
		// check public fields
		assertEquals(1,line.x1);
		assertEquals(5,line.x2);
		assertEquals(0,line.y1);
		assertEquals(15,line.y2);
		assertEquals(1,line.x1d,Assert.DOUBLE_TOL);
		assertEquals(5,line.x2d,Assert.DOUBLE_TOL);
		assertEquals(0,line.y1d,Assert.DOUBLE_TOL);
		assertEquals(15,line.y2d,Assert.DOUBLE_TOL);
		
		// check underlying values
		Rectangle r = line.getBounds();
		assertEquals(Roi.NORMAL,line.getState());
		assertEquals(Roi.LINE,line.getType());
		assertEquals(1,r.x);
		assertEquals(0,r.y);
		assertEquals(4,r.width);
		assertEquals(15,r.height);
		
		// see that image set correctly
		assertEquals(ip,line.getImage());
	}

	@Test
	public void testGetLength() {
		ImagePlus ip;
		Calibration cal;
		
		// no associated imageplus
		line = new Line(6.3,8.8,5.9,2.2);
		assertEquals(6.61211,line.getLength(),Assert.DOUBLE_TOL);
		
		// imageplus exists but alt key down
		ip = new ImagePlus("OrangeNavel",new FloatProcessor(2,3,new float[]{0,9,8,7,6,5},null));
		cal = new Calibration();
		cal.pixelHeight = 0.7;
		cal.pixelWidth = 1.2;
		ip.setCalibration(cal);
		IJ.setKeyDown(KeyEvent.VK_ALT);
		line = new Line(1,0,5,15,ip);
		assertEquals(15.52417,line.getLength(),Assert.DOUBLE_TOL);
		
		// image plus exists and alt key NOT down
		ip = new ImagePlus("OrangeNavel",new FloatProcessor(2,3,new float[]{0,9,8,7,6,5},null));
		cal = new Calibration();
		cal.pixelHeight = 0.7;
		cal.pixelWidth = 1.2;
		ip.setCalibration(cal);
		IJ.setKeyUp(KeyEvent.VK_ALT);
		line = new Line(1,0,5,15,ip);
		assertEquals(11.54513,line.getLength(),Assert.DOUBLE_TOL);
	}

	@Test
	public void testGetBounds() {
		line = new Line(1.5,2.7,22.4,4.1);
		Rectangle r = line.getBounds();
		assertEquals(2,r.x);
		assertEquals(3,r.y);
		assertEquals(21,r.width);
		assertEquals(1,r.height);
	}

	@Test
	public void testGetPolygon() {
		Polygon p;
		
		// strokewidth == 1
		line = new Line(1.5,2.7,22.4,4.1);
		line.setStrokeWidth(1);
		p = line.getPolygon();
		assertEquals(2,p.npoints);
		assertEquals(1,p.xpoints[0]);
		assertEquals(2,p.ypoints[0]);
		assertEquals(22,p.xpoints[1]);
		assertEquals(4,p.ypoints[1]);
		
		// strokewidth != 1
		line = new Line(1.5,2.7,22.4,4.1);
		line.setStrokeWidth(3);
		p = line.getPolygon();
		assertEquals(4,p.npoints);
		assertEquals(1,p.xpoints[0]);
		assertEquals(1,p.ypoints[0]);
		assertEquals(1,p.xpoints[1]);
		assertEquals(3,p.ypoints[1]);
		assertEquals(22,p.xpoints[2]);
		assertEquals(5,p.ypoints[2]);
		assertEquals(22,p.xpoints[3]);
		assertEquals(3,p.ypoints[3]);
	}

	@Test
	public void testNudgeCorner() {
		// can't test. relies on imageplus having an imagecanvas to get coords from. we don't have this.
		// compile time test
		line = new Line(8,4,7,5);
		line.nudgeCorner(0);
	}

	@Test
	public void testDraw() {
		if (IJInfo.RUN_GUI_TESTS) {
			// can't test this. needs an imagecanvas.
		}
	}

	@Test
	public void testDrawPixelsImageProcessor() {
		
		ImageProcessor proc;
		
		// try case where strokewidth == 1
		proc = new ShortProcessor(3,4,new short[12],null);
		proc.setColor(23);
		proc.setLineWidth(5);
		assertEquals(5,proc.getLineWidth());
		line = new Line(0,0,2,3);
		line.setStrokeWidth(1);
		assertEquals(1,line.getStrokeWidth(),Assert.DOUBLE_TOL);
		line.drawPixels(proc);
		assertEquals(1,proc.getLineWidth());
		assertEquals(23,proc.get(0,0));
		assertEquals(0,proc.get(0,1));
		assertEquals(0,proc.get(0,2));
		assertEquals(0,proc.get(0,3));
		assertEquals(0,proc.get(1,0));
		assertEquals(23,proc.get(1,1));
		assertEquals(23,proc.get(1,2));
		assertEquals(0,proc.get(1,3));
		assertEquals(0,proc.get(2,0));
		assertEquals(0,proc.get(2,1));
		assertEquals(0,proc.get(2,2));
		assertEquals(23,proc.get(2,3));

		// try case where strokewidth != 1
		proc = new ShortProcessor(3,4,new short[12],null);
		proc.setColor(23);
		proc.setLineWidth(5);
		assertEquals(5,proc.getLineWidth());
		line = new Line(0,0,2,3);
		line.setStrokeWidth(2);
		assertEquals(2,line.getStrokeWidth(),Assert.DOUBLE_TOL);
		line.drawPixels(proc);
		assertEquals(1,proc.getLineWidth());
		assertEquals(23,proc.get(0,0));
		assertEquals(0,proc.get(0,1));
		assertEquals(23,proc.get(0,2));
		assertEquals(23,proc.get(0,3));
		assertEquals(0,proc.get(1,0));
		assertEquals(0,proc.get(1,1));
		assertEquals(0,proc.get(1,2));
		assertEquals(0,proc.get(1,3));
		assertEquals(23,proc.get(2,0));
		assertEquals(23,proc.get(2,1));
		assertEquals(0,proc.get(2,2));
		assertEquals(23,proc.get(2,3));
	}

	/*
	public boolean contains(int x, int y) {
		if (getStrokeWidth()>1)
			return getPolygon().contains(x, y);
		else
			return false;
	}

	 */
	@Test
	public void testContains() {
		line = new Line(2,2,17,4);

		line.setStrokeWidth(1);
		assertFalse(line.contains(2,2));
		assertFalse(line.contains(17,4));

		line.setStrokeWidth(2);
		assertTrue(line.contains(2,2));
		assertTrue(line.contains(17,4));
	}

	@Test
	public void testIsHandle() {
		fail("Not yet implemented");
	}

	@Test
	public void testSetStrokeWidth() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetRawLength() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetPixels() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetWidth() {
		fail("Not yet implemented");
	}

	@Test
	public void testSetWidth() {
		fail("Not yet implemented");
	}

}
