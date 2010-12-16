package ij.gui;

import static org.junit.Assert.*;
import ij.Assert;
import ij.IJInfo;

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
		
		float savedLineWidth;
		Polygon p;
		
		// strokewidth == 1
		line = new Line(1.5,2.7,22.4,4.1);
		savedLineWidth = line.getStrokeWidth();
		line.setStrokeWidth(1);
		p = line.getPolygon();
		line.setStrokeWidth(savedLineWidth);
		assertEquals(2,p.npoints);
		assertEquals(1,p.xpoints[0]);
		assertEquals(2,p.ypoints[0]);
		assertEquals(22,p.xpoints[1]);
		assertEquals(4,p.ypoints[1]);
		
		// strokewidth != 1
		line = new Line(1.5,2.7,22.4,4.1);
		line.setStrokeWidth(3);
		p = line.getPolygon();
		line.setStrokeWidth(savedLineWidth);
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

		float savedLineWidth;
		ImageProcessor proc;
		
		// try case where strokewidth == 1
		proc = new ShortProcessor(3,4,new short[12],null);
		proc.setColor(23);
		proc.setLineWidth(5);
		assertEquals(5,proc.getLineWidth());
		line = new Line(0,0,2,3);
		savedLineWidth = line.getStrokeWidth();
		line.setStrokeWidth(1);
		assertEquals(1,line.getStrokeWidth(),Assert.DOUBLE_TOL);
		line.drawPixels(proc);
		line.setStrokeWidth(savedLineWidth);
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
		savedLineWidth = line.getStrokeWidth();
		line.setStrokeWidth(2);
		assertEquals(2,line.getStrokeWidth(),Assert.DOUBLE_TOL);
		line.drawPixels(proc);
		line.setStrokeWidth(savedLineWidth);
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

	@Test
	public void testContains() {
		line = new Line(2,2,17,4);

		float savedLineWidth = line.getStrokeWidth();
		
		line.setStrokeWidth(1);
		assertFalse(line.contains(2,2));
		assertFalse(line.contains(17,4));
		assertFalse(line.contains(9,3));

		line.setStrokeWidth(2);
		assertTrue(line.contains(2,2));
		assertTrue(line.contains(17,4));
		assertTrue(line.contains(9,3));
		
		line.setStrokeWidth(savedLineWidth);
	}

	@Test
	public void testIsHandle() {
		// isHandle() relies on having an imagecanvas to get coord from. this is false in our case. can't test.
		if (IJInfo.RUN_GUI_TESTS)
		{
		}
	}

	@Test
	public void testSetStrokeWidth() {
		Color savedColor = Roi.getColor();
		
		line = new Line(7,3,6,1);
		float savedLineWidth = line.getStrokeWidth();
		assertTrue(Math.abs(savedLineWidth - 14) > 0.1);
		line.setStrokeWidth(14);
		assertEquals(14,line.getStrokeWidth(),Assert.DOUBLE_TOL);
		//assertEquals(BasicStroke.CAP_SQUARE,line.getStroke().getEndCap());
		//assertEquals(BasicStroke.JOIN_MITER,line.getStroke().getLineJoin());
		line.setStrokeWidth(savedLineWidth);
		
		// note that setStrokeWidth sets wideLine true when roi color and stroke color are the same changing behavior
		line = new Line(7,3,6,1);
		savedLineWidth = line.getStrokeWidth();
		Roi.setColor(line.getStrokeColor());
		line.setStrokeWidth(3);
		line.setStrokeWidth(8);
		assertEquals(8,line.getStrokeWidth(),Assert.DOUBLE_TOL);
		//assertEquals(BasicStroke.CAP_BUTT,line.getStroke().getEndCap());
		//assertEquals(BasicStroke.JOIN_BEVEL,line.getStroke().getLineJoin());
		line.setStrokeWidth(savedLineWidth);
		
		Roi.setColor(savedColor);
	}

	@Test
	public void testGetRawLength() {
		line = new Line(0,0,4.5,0);
		assertEquals(4.5,line.getRawLength(),Assert.DOUBLE_TOL);

		line = new Line(-4.5,0,0,0);
		assertEquals(4.5,line.getRawLength(),Assert.DOUBLE_TOL);

		line = new Line(0,3.1,0,0);
		assertEquals(3.1,line.getRawLength(),Assert.DOUBLE_TOL);

		line = new Line(0,-3.1,0,0);
		assertEquals(3.1,line.getRawLength(),Assert.DOUBLE_TOL);

		line = new Line(77.6,14.9,33.8,101.6);
		assertEquals(97.13563,line.getRawLength(),Assert.DOUBLE_TOL);
	}

	@Test
	public void testGetPixels() {
		ImagePlus ip;
		double[] vals;
		
		// a 3x3 image with an X through it
		ip = new ImagePlus("Zakko",new ByteProcessor(3,3,new byte[]{1,0,1,0,1,0,1,0,1},null));

		// strokewidth == 1, try a line along edge
		line = new Line(0,0,2,0);
		float savedLineWidth = line.getStrokeWidth();
		ip.setRoi(line);
		line.setStrokeWidth(1);
		vals = line.getPixels();
		line.setStrokeWidth(savedLineWidth);
		assertEquals(3,vals.length);
		Assert.assertDoubleArraysEqual(new double[]{1,0,1}, vals, Assert.DOUBLE_TOL);

		// strokewidth == 1, try a line across diagonal
		line = new Line(0,0,2,2);
		savedLineWidth = line.getStrokeWidth();
		ip.setRoi(line);
		line.setStrokeWidth(1);
		vals = line.getPixels();
		line.setStrokeWidth(savedLineWidth);
		assertEquals(4,vals.length); // notice its 4 ... unexpected
		Assert.assertDoubleArraysEqual(new double[]{1,1,1,1}, vals, Assert.DOUBLE_TOL);

		// strokewidth != 1, try a line along edge
		line = new Line(0,0,2,0);
		savedLineWidth = line.getStrokeWidth();
		ip.setRoi(line);
		line.setStrokeWidth(2);
		vals = line.getPixels();
		line.setStrokeWidth(savedLineWidth);
		assertEquals(2,vals.length);
		Assert.assertDoubleArraysEqual(new double[]{1.0,0.00333333}, vals, Assert.DOUBLE_TOL);

		// strokewidth != 1, try a line across diagonal
		line = new Line(0,0,2,2);
		savedLineWidth = line.getStrokeWidth();
		ip.setRoi(line);
		line.setStrokeWidth(2);
		vals = line.getPixels();
		line.setStrokeWidth(savedLineWidth);
		assertEquals(3,vals.length);
		Assert.assertDoubleArraysEqual(new double[]{0.646446600,0.50199,0.40115381777}, vals, Assert.DOUBLE_TOL);
	}

	@Test
	public void testSetAndGetWidth() {
		// note that setWidth() has some gui related code for determining max that we can't test
		int savedWidth = Line.getWidth();

		assertTrue(savedWidth != -1);

		Line.setWidth(-1);
		assertEquals(1,Line.getWidth(),Assert.DOUBLE_TOL);

		Line.setWidth(0);
		assertEquals(1,Line.getWidth(),Assert.DOUBLE_TOL);

		Line.setWidth(1);
		assertEquals(1,Line.getWidth(),Assert.DOUBLE_TOL);

		Line.setWidth(2);
		assertEquals(2,Line.getWidth(),Assert.DOUBLE_TOL);

		Line.setWidth(103);
		assertEquals(103,Line.getWidth(),Assert.DOUBLE_TOL);

		Line.setWidth(499);
		assertEquals(499,Line.getWidth(),Assert.DOUBLE_TOL);

		Line.setWidth(500);
		assertEquals(500,Line.getWidth(),Assert.DOUBLE_TOL);

		Line.setWidth(501);
		assertEquals(500,Line.getWidth(),Assert.DOUBLE_TOL);

		Line.setWidth(savedWidth);
	}

}
