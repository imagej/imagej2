//
// PointRoiTest.java
//

/*
ImageJ software for multidimensional image processing and analysis.

Copyright (c) 2010, ImageJDev.org.
All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met:
    * Redistributions of source code must retain the above copyright
      notice, this list of conditions and the following disclaimer.
    * Redistributions in binary form must reproduce the above copyright
      notice, this list of conditions and the following disclaimer in the
      documentation and/or other materials provided with the distribution.
    * Neither the names of the ImageJDev.org developers nor the
      names of its contributors may be used to endorse or promote products
      derived from this software without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
POSSIBILITY OF SUCH DAMAGE.
*/

package ij.gui;

import static org.junit.Assert.*;

import org.junit.Test;

import ij.*;
import ij.process.*;

import java.awt.*;

public class PointRoiTest {

	PointRoi p;

	private static final int[] xPoints = new int[] {1,2,3,4,5};
	private static final int[] yPoints = new int[] {22,14,88,99,45};
	private static final Polygon tstPoly = new Polygon(xPoints,yPoints,xPoints.length);
	
	// helper
	private void validateConstruction(PointRoi p, int w, int h)
	{
		assertNotNull(p);
		assertEquals(Roi.POINT,p.getType());
		assertEquals(w,p.getBounds().width);
		assertEquals(h,p.getBounds().height);
	}
	
	// helper
	private boolean isCornerOf(Polygon p, int x, int y)
	{
		for (int i = 0; i < p.npoints; i++)
			if ((p.xpoints[i] == x) && (p.ypoints[i] == y))
				return true;
		return false;
	}
	
	// helper
	private void validatePixels(ImageProcessor proc, int refValue, Polygon poly) {
		// test that pixels drawn correctly
		for (int x = 0; x < proc.getWidth(); x++)
			for (int y = 0; y < proc.getHeight(); y++)
				if (isCornerOf(poly,x,y))
					assertEquals(refValue,proc.get(x,y));
				else
					assertEquals(0,proc.get(x,y));
	}

	// helper
	private void validateDrawPixels(Polygon poly)
	{
		int refValue = 23;
		ImageProcessor proc = new ShortProcessor(100,100,new short[100*100],null);		
		proc.setColor(refValue);
		p = new PointRoi(poly);
		p.drawPixels(proc);
		validatePixels(proc,refValue,poly);
	}
	
	// helper
	private void validateMask(ImageProcessor proc, Polygon refPoly)
	{
		Rectangle originRect = refPoly.getBounds();
		int w = proc.getWidth();
		int h = proc.getHeight();
		for (int x = 0; x < w; x++)
			for (int y = 0; y < h; y++)
				if (isCornerOf(refPoly,originRect.x+x,originRect.y+y))
					assertEquals(255,proc.get(x,y));
				else
					assertEquals(0,proc.get(x,y));
	}
	
	// helper
	private void validateGetMask(Polygon poly)
	{
		PointRoi p = new PointRoi(poly);
		ImageProcessor proc = p.getMask();
		validateMask(proc,poly);
	}
	
	// helper
	private void validateAdd(Polygon poly, int x, int y)
	{
		PointRoi tmp;
		
		p = new PointRoi(poly);
		assertFalse(p.contains(x,y));
		tmp = p.addPoint(x,y);
		assertTrue(tmp.contains(x,y));
	}

	// helper
	private void validateSubtraction(int[] ptsX, int[] ptsY, int[] roiX, int[] roiY, int[] expX, int[] expY) {
		PointRoi pts = new PointRoi(ptsX, ptsY, ptsX.length);
		Roi roi = new PolygonRoi(roiX, roiY, roiX.length, Roi.POLYGON);
		PointRoi p = pts.subtractPoints(roi);
		if (expX.length == 0)
			assertNull(p);
		else
		{
			assertNotNull(p);
			assertEquals(expX.length,p.nPoints);
			for (int i = 0; i < expX.length; i++)
			{
				assertEquals(expX[i],p.x+p.xp[i]);
				assertEquals(expY[i],p.y+p.yp[i]);
			}
		}
	}
	
	// ***********************************  TESTS  *******************************************************
	
	@Test
	public void testPointRoiIntArrayIntArrayInt() {
		// pass in garbage data : nulls
		p = new PointRoi((int[])null,(int[])null,0);
		validateConstruction(p,1,1);

		// pass in more garbage data : x and y of different length
		p = new PointRoi(new int[]{1}, new int[] {1,2}, 1);
		validateConstruction(p,1,1);
		
		// pass in valid data
		p = new PointRoi(xPoints, yPoints, xPoints.length);
		validateConstruction(p,5,86);
	}

	@Test
	public void testPointRoiPolygon() {

		// this next test generates a NullPtrException
		if (IJInfo.RUN_ENHANCED_TESTS)
		{
			// pass in garbage
			p = new PointRoi((Polygon)null);
			validateConstruction(p,0,0);
		}
		
		// pass in valid data
		p = new PointRoi(tstPoly);
		validateConstruction(p,5,86);
	}

	@Test
	public void testPointRoiIntInt() {
		// try weird point
		p = new PointRoi(-5,-25);
		validateConstruction(p,1,1);
		
		// try zero point
		p = new PointRoi(0,0);
		validateConstruction(p,1,1);

		// try typical point
		p = new PointRoi(183,422);
		validateConstruction(p,1,1);
	}

	@Test
	public void testPointRoiIntIntImagePlus() {
		
		ImagePlus ip;
		
		// try with null imageplus
		p = new PointRoi(2,2,null);
		validateConstruction(p,1,1);
		
		// TODO - with null ip test effects on Recorder
		
		// note - can't test this next call. imageplus needs an open imagecanvas to create off screen coords.
		//   since gui not active we don't have an imagecanvas
		if (IJInfo.RUN_ENHANCED_TESTS)
		{
			// try with valid imageplus
			ip = new ImagePlus("Torpor",new ShortProcessor(2,2,new short[2*2], null));
			p = new PointRoi(17,5,ip);
			validateConstruction(p,1,1);
		}
	}

	@Test
	public void testDraw() {
		// note - can't test. needs a Graphic context which we don't have.
	}

	@Test
	public void testDrawPixelsImageProcessor() {
		
		// degenerate case
		validateDrawPixels(new Polygon(new int[]{}, new int[]{}, 0));
		
		// general case
		validateDrawPixels(tstPoly);
	}

	@Test
	public void testContains() {
		// test many points
		p = new PointRoi(tstPoly);
		for (int x = -100; x < 200; x++)
			for (int y = -100; y < 200; y++)
				assertEquals(isCornerOf(tstPoly,x,y),p.contains(x,y));
	}

	@Test
	public void testGetMask() {
		
		// degenerate case
		validateGetMask(new Polygon(new int[]{}, new int[]{}, 0));

		// single point case
		validateGetMask(new Polygon(new int[]{1}, new int[]{1}, 1));

		// general case 1
		validateGetMask(new Polygon(new int[]{1,2,3}, new int[]{8,7,4}, 3));

		// general case 2
		validateGetMask(tstPoly);
	}

	@Test
	public void testAddPointIntInt() {

		// degenerate case
		validateAdd(new Polygon(new int[]{}, new int[]{}, 0), 108,-14);

		// general case
		validateAdd(tstPoly, 12, 44);
	}

	// This is a tricky method to test. Java has some peculiar rules on when a point is contained() in a Polygon. Certain 
	// corner points of a Polygon report they are outside the Polygon (at least the topmost and rightmost points and probably
	// others on the boundary). The contains() behavior is documented in Java's docs for the Shape Interface. What gets
	// complicated here is the ability to subtract points from a PointRoi that are on the boundary of a Polygon that includes
	// them.
	
	@Test
	public void testSubtractPointsRoi() {
		Roi roi;
		PointRoi pts;

		// since Polygon.contains is so finicky I can't test edge cases
		
		// empty pointroi and empty roi
		validateSubtraction(new int[]{}, new int[]{},
							new int[]{}, new int[]{},
							new int[]{}, new int[]{});

		// empty point roi and valid roi
		validateSubtraction(new int[]{}, new int[]{},
							new int[]{1}, new int[]{1},
							new int[]{}, new int[]{});
		
		// 1 pt pointroi and empty roi
		validateSubtraction(new int[]{1}, new int[]{1},
							new int[]{}, new int[]{},
							new int[]{1}, new int[]{1});

		// 1 pt point roi and valid roi no intersect
		validateSubtraction(new int[]{1}, new int[]{1},
							new int[]{2,3,3,2}, new int[]{2,2,3,3},
							new int[]{1}, new int[]{1});

		// 1 pt point roi and valid roi intersect
		validateSubtraction(new int[]{1}, new int[]{1},
							new int[]{0,2,2,0}, new int[]{0,0,2,2},
							new int[]{}, new int[]{});

		// multipt roi and empty roi
		validateSubtraction(new int[]{1,2}, new int[]{1,2},
							new int[]{}, new int[]{},
							new int[]{1,2}, new int[]{1,2});

		// multipt roi and valid roi no intersect
		validateSubtraction(new int[]{1,2}, new int[]{1,2},
							new int[]{2,4,4,2}, new int[]{3,3,4,4},
							new int[]{1,2}, new int[]{1,2});

		// multipt roi and valid roi intersect (multiple subcases)
		
		// delete first corner
		validateSubtraction(new int[]{1,5,5,1}, new int[]{1,1,5,5},
							new int[]{0,2,2,0}, new int[]{0,0,2,2},
							new int[]{5,5,1}, new int[]{1,5,5});
	
		// delete second corner
		validateSubtraction(new int[]{1,5,5,1}, new int[]{1,1,5,5},
							new int[]{4,6,6,4}, new int[]{0,0,2,2},
							new int[]{1,5,1}, new int[]{1,5,5});
		
		// delete third corner
		validateSubtraction(new int[]{1,5,5,1}, new int[]{1,1,5,5},
							new int[]{4,6,6,4}, new int[]{4,4,6,6},
							new int[]{1,5,1}, new int[]{1,1,5});

		// delete fourth corner
		validateSubtraction(new int[]{1,5,5,1}, new int[]{1,1,5,5},
							new int[]{0,2,2,0}, new int[]{4,4,6,6},
							new int[]{1,5,5}, new int[]{1,1,5});

		// delete two corners at once
		validateSubtraction(new int[]{1,5,5,1}, new int[]{1,1,5,5},
							new int[]{4,6,6,4}, new int[]{0,0,6,6},
							new int[]{1,1}, new int[]{1,5});
		
		// delete all corners : roi a superset of pointroi
		validateSubtraction(new int[]{1,2,2,1}, new int[]{1,1,2,2},
							new int[]{0,3,3,0}, new int[]{0,0,3,3},
							new int[]{}, new int[]{});
		
		/* Edge cases that just don't work intuitively - don't want to test against

		pts = new PointRoi(new int[]{0,1,1,0}, new int[] {0,0,1,1}, 4);
		roi = new PointRoi(new int[]{0},new int[]{0},1);
		p = pts.subtractPoints(roi);
		assertEquals(4,p.nPoints);  // wow - nonintuitive

		pts = new PointRoi(new int[]{0,1,1,0}, new int[] {0,0,1,1}, 4);
		roi = new PointRoi(new int[]{1},new int[]{0},1);
		p = pts.subtractPoints(roi);
		assertEquals(4,p.nPoints);  // wow - nonintuitive
		
		pts = new PointRoi(new int[]{0,1,1,0}, new int[] {0,0,1,1}, 4);
		roi = new PointRoi(new int[]{0},new int[]{1},1);
		p = pts.subtractPoints(roi);
		assertEquals(4,p.nPoints);  // wow - nonintuitive
		
		pts = new PointRoi(new int[]{0,1,1,0}, new int[] {0,0,1,1}, 4);
		roi = new PointRoi(new int[]{1},new int[]{1},1);
		p = pts.subtractPoints(roi);
		assertEquals(4,p.nPoints);  // wow - nonintuitive

		// intersect along an edge
		pts = new PointRoi(new int[]{1,0,1}, new int[]{0,1,1},3);
		roi = new PolygonRoi(new int[]{0,1,0}, new int[] {0,0,1}, 3, Roi.POLYGON);
		p = pts.subtractPoints(roi);
		assertEquals(3,p.nPoints);
		// ... also do a couple more asserts here on values

		// roi same as pointroi - this case surprisingly fails to return null. Again it's Java's handling of edge cases.
		pts = new PointRoi(new int[]{0,1,1,0}, new int[]{0,0,1,1},4);
		roi = new PolygonRoi(new int[]{0,1,1,0}, new int[] {0,0,1,1}, 4, Roi.POLYGON);
		p = pts.subtractPoints(roi);
		assertEquals(3,p.nPoints);
		
		for (int i = 0; i < 3; i++)
			System.out.print("("+p.xp[i]+","+p.yp[i]+")");
		System.out.println();
		
		*/
	}
}
