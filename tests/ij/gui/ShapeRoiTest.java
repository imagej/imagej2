package ij.gui;

import static org.junit.Assert.*;
import org.junit.Test;

import java.awt.*;
import java.awt.geom.*;

import ij.Assert;
import ij.process.*;

public class ShapeRoiTest {

	ShapeRoi s;

	private void consTest(ShapeRoi roi, int x, int y, int w, int h)
	{
		assertNotNull(s);
		Rectangle r = s.getBounds();
		assertEquals(x,r.x);
		assertEquals(y,r.y);
		assertEquals(w,r.width);
		assertEquals(h,r.height);
	}
	
	@Test
	public void testShapeRoiRoi() {

		// all public methods of ShapeRoi takes place below
		//   no real accessors of private data
		//   the private data has hard to test side effects associated with it
		
		// base class constructor sets bounds to our shape's bounds
		
		// try an oval
		s = new ShapeRoi(new OvalRoi(14,2,5,10));
		consTest(s,14,2,5,10);

		// try a line
		s = new ShapeRoi(new Line(15,-5,-13,-2));
		consTest(s,-13,-5,28,3);
		
		// try an image
		s = new ShapeRoi(new ImageRoi(5, 2, new ColorProcessor(5,7,new int[5*7])));
		consTest(s,5,2,5,7);
	}

	@Test
	public void testShapeRoiShape() {
		Shape sh;
		
		// make from an arc
		sh = new Arc2D.Double(1.4, 2.7, 4.7, 9.6, 23.0, 14.5, Arc2D.PIE);
		s = new ShapeRoi(sh);
		consTest(s,1,2,6,11);
		
		// make from a Polygon
		sh = new Polygon(new int[]{1,6,3,7}, new int[] {0,-4,6,1},4);
		s = new ShapeRoi(sh);
		consTest(s,1,-4,6,10);
	}

	@Test
	public void testShapeRoiIntIntShape() {
		Shape sh;

		// try a round rect
		sh = new RoundRectangle2D.Double(17.6,93.7,1008.8,400,8,5);
		s = new ShapeRoi(sh);
		consTest(s,17,93,1010,401);
		
		// try a cubic curve
		sh = new CubicCurve2D.Double(1, 3, 4, 2, 8, 11, 9, 14);
		s = new ShapeRoi(sh);
		consTest(s,1,2,8,12);
	}

	@Test
	public void testShapeRoiFloatArray() {
		float[] floats;
		
		// make a polyline
		floats = new float[]{PathIterator.SEG_MOVETO,1,1,PathIterator.SEG_LINETO,4,7};
		s = new ShapeRoi(floats);
		consTest(s,1,1,3,6);
		
		// make a polygon
		floats = new float[]{PathIterator.SEG_MOVETO,7,3,PathIterator.SEG_LINETO,4,7,PathIterator.SEG_LINETO,13,23,PathIterator.SEG_CLOSE};
		s = new ShapeRoi(floats);
		consTest(s,4,3,9,20);

		// make a curve
		floats = new float[]{PathIterator.SEG_MOVETO,13,1,PathIterator.SEG_QUADTO,7,4,9,2,PathIterator.SEG_QUADTO,8,12,14,16};
		s = new ShapeRoi(floats);
		consTest(s,7,1,7,15);
	}

	private void lengthTest(Roi roi)
	{
		s = new ShapeRoi(roi);
		assertNotNull(s);
		
		// should always equal 0
		
		assertEquals(0,s.getLength(),Assert.DOUBLE_TOL);
	}
	
	@Test
	public void testGetLength() {
		
		// try various roi shapes
		lengthTest(new OvalRoi(1,6,14,3));
		lengthTest(new Roi(1,7,33,1009));
		lengthTest(new Line(14,-33,109,-83));
	}

	private void feretTest(Roi roi, double[] expected)
	{
		s = new ShapeRoi(roi);
		assertNotNull(s);
		double[] feretsVals = s.getFeretValues();
		assertArrayEquals(expected,feretsVals,Assert.DOUBLE_TOL);
	}
	
	@Test
	public void testGetFeretValues() {
		// try various roi shapes
		feretTest(new OvalRoi(0,0,5,10),new double[]{10.44016,0,5.00000,0,0});
		feretTest(new Roi(16,22,8,19),new double[]{20.61544,0,8,0,0});
		feretTest(new TextRoi(4,3,RoiHelpers.getCalibratedImagePlus()),new double[]{0,0,0,0,0});
	}

	private void convexHullTest(Roi roi)
	{
		s = new ShapeRoi(roi);
		assertNotNull(s);
		
		// should always be null
		
		assertNull(s.getConvexHull());
	}
	
	@Test
	public void testGetConvexHull() {
		// try various roi shapes
		convexHullTest(new OvalRoi(0,0,5,10));
		convexHullTest(new Roi(16,22,8,19));
		convexHullTest(new TextRoi(4,3,RoiHelpers.getCalibratedImagePlus()));
	}

	private void cloneTest(Roi roi)
	{
		s = new ShapeRoi(roi);
		ShapeRoi copy = (ShapeRoi) s.clone();
		assertEquals(s,copy);
	}
	
	@Test
	public void testClone() {
		// try various roi shapes
		cloneTest(new OvalRoi(0,0,5,10));
		cloneTest(new Roi(-14,-101,-1000,-2000));
		cloneTest(new Line(90,7,-5,106));
	}

	@Test
	public void testDraw() {
		// can't test - needs a graphics context
	}

	private void drawPixelsTest(Roi roi, int[] expectedNonZeroes)
	{
		Rectangle r = roi.getBounds();
		s = new ShapeRoi(roi);
		ImageProcessor proc = new ByteProcessor(r.width,r.height,new byte[r.width*r.height],null);
		proc.setColor(99);
		s.drawPixels(proc);
		RoiHelpers.printNonzeroIndices(proc);
		RoiHelpers.validateResult(proc, 99, expectedNonZeroes);
	}
	
	@Test
	public void testDrawPixelsImageProcessor() {
		//drawPixelsTest(new OvalRoi(3,7,3,4), new int[]{});  // TODO crash
		//drawPixelsTest(new Line(3,7,5,4), new int[]{});  // TODO crash
		//drawPixelsTest(new Roi(3,7,5,4), new int[]{});  // TODO crash
		//drawPixelsTest(new TextRoi(4, 8, "Zapplepop"), new int[]{});  // TODO crash
		//drawPixelsTest(new PolygonRoi(new int[]{1,5,3}, new int[]{6,9,3},3, Roi.POLYGON), new int[]{});  // TODO crash
	}

	private void containsTest(Shape sh)
	{
		s = new ShapeRoi(sh);
		Rectangle r = s.getBounds();
		for (int x = r.x-10; x < r.x+r.width+10; x++)
			for (int y = r.x-10; y < r.x+r.width+10; y++)
		assertEquals(s.contains(x,y),sh.contains(x,y));
	}
	
	@Test
	public void testContains() {
		containsTest(new Rectangle2D.Double(-14.2,73.9,8.66,-14.5));
		containsTest(new RoundRectangle2D.Double(16.3,52.1,801.4,67.5,3,7));
	}

	private void isHandleTest(Roi roi)
	{
		s = new ShapeRoi(roi);
		assertNotNull(s);
		Rectangle r = s.getBounds();
		//   test all seemingly valid coords for a given roi
		for (int i = r.x-10; i < r.x+r.width+10; i++)
			for (int j = r.y-10; j < r.y+r.height+10; j++)
				// isHandle should always return -1
				assertEquals(-1,s.isHandle(i, j));
	}
	
	@Test
	public void testIsHandle() {
		// an OvalRoi
		isHandleTest(new OvalRoi(0,0,5,10));
		
		// a Line
		isHandleTest(new Line(4,1,8,23));
	}

	private void maskTest(Roi roi, int expCols, int expRows, byte[] expData)
	{
		s = new ShapeRoi(roi);
		ImageProcessor mask = s.getMask();
		assertEquals(expCols,mask.getWidth());
		assertEquals(expRows,mask.getHeight());
		assertArrayEquals(expData,(byte[])((ByteProcessor)mask).getPixels());
	}
	
	@Test
	public void testGetMask() {
		maskTest(new Roi(3,2,6,8), 6, 8,
					new byte[]{-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,
								-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1});
		maskTest(new Line(1,4,3,6), 2, 2, new byte[]{0,0,0,0});  // kind of a surprise here that its all zeroes
		maskTest(new OvalRoi(0,0,3,3), 3, 3, new byte[]{-1,-1,-1,-1,-1,-1,-1,-1,-1});  // surprise its all filled
		maskTest(new OvalRoi(0,0,4,4), 4, 4, new byte[]{0,-1,-1,0,-1,-1,-1,-1,-1,-1,-1,-1,0,-1,-1,0});
	}

	private void printOut(ShapeRoi roi)
	{
		Polygon p = roi.getPolygon();
		System.out.println("Begin");
		for (int i = 0; i < p.npoints; i++)
			System.out.println(p.xpoints[i]+" "+p.ypoints[i]);
	}
	
	private void orTest(ShapeRoi a, ShapeRoi b, ShapeRoi exp)
	{
		s = a.or(b);
		printOut(s);
		assertEquals(exp,s);
	}
	
	@Test
	public void testOr() {
		Shape sh;
		ShapeRoi a,b,exp;
		
		// same area
		sh = new Rectangle2D.Double(0, 0, 1, 1);
		a = new ShapeRoi(sh);
		b = new ShapeRoi(sh);
		exp = new ShapeRoi(sh);
		orTest(a,b,exp);
		
		// intersecting area
		sh = new Rectangle2D.Double(0, 0, 2, 2);
		a = new ShapeRoi(sh);
		sh = new Rectangle2D.Double(1, 0, 2, 2);
		b = new ShapeRoi(sh);
		sh = new Rectangle2D.Double(0, 0, 3, 2);
		exp = new ShapeRoi(sh);
		orTest(a,b,exp);

		// just touching area
		sh = new Rectangle2D.Double(0, 0, 2, 2);
		a = new ShapeRoi(sh);
		sh = new Rectangle2D.Double(2, 0, 2, 2);
		b = new ShapeRoi(sh);
		sh = new Rectangle2D.Double(0, 0, 4, 2);
		exp = new ShapeRoi(sh);
		orTest(a,b,exp);

		// disjoint areas
		sh = new Rectangle2D.Double(0, 0, 2, 2);
		a = new ShapeRoi(sh);
		sh = new Rectangle2D.Double(3, 0, 2, 2);
		b = new ShapeRoi(sh);
		sh = new Rectangle2D.Double(0, 0, 5, 2);
		exp = new ShapeRoi(sh);
		orTest(a,b,exp);
	}

	private void andTest(ShapeRoi a, ShapeRoi b, ShapeRoi exp)
	{
		s = a.or(b);
		printOut(s);
		assertEquals(exp,s);
	}
	
	// TODO - left off working here. Things to note:
	//  1) the testOr() code seems unintuitive in the disjoint areas test.
	//  2) the testAnd() code behaves exactly the same as the testOr()!?!!?
	//  3) above drawPixels(proc) throws exceptions - reported to Wayne
	
	@Test
	public void testAnd() {
		Shape sh;
		ShapeRoi a,b,exp;
		
		// same area
		sh = new Rectangle2D.Double(0, 0, 1, 1);
		a = new ShapeRoi(sh);
		b = new ShapeRoi(sh);
		exp = new ShapeRoi(sh);
		andTest(a,b,exp);
		
		// intersecting area
		sh = new Rectangle2D.Double(0, 0, 2, 2);
		a = new ShapeRoi(sh);
		sh = new Rectangle2D.Double(1, 0, 2, 2);
		b = new ShapeRoi(sh);
		sh = new Rectangle2D.Double(0, 0, 3, 2);
		exp = new ShapeRoi(sh);
		andTest(a,b,exp);

		// just touching area
		sh = new Rectangle2D.Double(0, 0, 2, 2);
		a = new ShapeRoi(sh);
		sh = new Rectangle2D.Double(2, 0, 2, 2);
		b = new ShapeRoi(sh);
		sh = new Rectangle2D.Double(0, 0, 4, 2);
		exp = new ShapeRoi(sh);
		andTest(a,b,exp);

		// disjoint areas
		sh = new Rectangle2D.Double(0, 0, 2, 2);
		a = new ShapeRoi(sh);
		sh = new Rectangle2D.Double(3, 0, 2, 2);
		b = new ShapeRoi(sh);
		sh = new Rectangle2D.Double(0, 0, 5, 2);
		exp = new ShapeRoi(sh);
		andTest(a,b,exp);
	}

	@Test
	public void testXor() {
		fail("Not yet implemented");
	}

	@Test
	public void testNot() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetRois() {
		fail("Not yet implemented");
	}

	@Test
	public void testShapeToRoi() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetShapeAsArray() {
		fail("Not yet implemented");
	}

	@Test
	public void testDrawRoiBrush() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetShape() {
		fail("Not yet implemented");
	}

	@Test
	public void testAddCircle() {
		fail("Not yet implemented");
	}

	@Test
	public void testSubtractCircle() {
		fail("Not yet implemented");
	}

}
