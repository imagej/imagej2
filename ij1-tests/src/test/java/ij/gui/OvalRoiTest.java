//
// OvalRoiTest.java
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

import java.awt.*;
import java.awt.event.KeyEvent;

import ij.*;
import ij.measure.Calibration;
import ij.process.*;

public class OvalRoiTest {

	OvalRoi o;
	
	@Test
	public void testOvalRoiIntIntIntInt() {
		// try bad input
		o = new OvalRoi(0,0,0,0);
		assertNotNull(o);
		assertEquals(Roi.OVAL,o.getType());

		// try bad input
		o = new OvalRoi(-1,-1,-1,-1);
		assertNotNull(o);
		assertEquals(Roi.OVAL,o.getType());

		// try valid input but small
		o = new OvalRoi(0,0,1,1);
		assertNotNull(o);
		assertEquals(Roi.OVAL,o.getType());

		// try valid input but large
		o = new OvalRoi(4400,16202,50003,74001);
		assertNotNull(o);
		assertEquals(Roi.OVAL,o.getType());
	}

	@Test
	public void testOvalRoiIntIntImagePlus() {
		ImagePlus ip;
		
		// invalid input (null ImagePlus)
		o = new OvalRoi(0,0,null);
		assertNotNull(o);
		assertEquals(Roi.OVAL,o.getType());
		
		// valid input (0,0 of 2,2)
		ip = new ImagePlus("Quatorze",new ShortProcessor(2,2,new short[2*2],null));
		o = new OvalRoi(0,0,ip);
		assertNotNull(o);
		assertEquals(Roi.OVAL,o.getType());

		// invalid input (origin otuside image)
		ip = new ImagePlus("Quatorze",new ShortProcessor(2,2,new short[2*2],null));
		o = new OvalRoi(14,22,ip);
		assertNotNull(o);
		assertEquals(Roi.OVAL,o.getType());
	}

	@Test
	public void testGetLength() {
		int w,h;
		double uw,uh;
		
		// valid input, no associated and calibrated imageplus
		w = 10; h = 8;
		uw = 1.0; uh = 1.0;
		o = new OvalRoi(0,0,w,h);
		assertEquals(Math.PI*(w*uw+h*uh)/2.0,o.getLength(),Assert.DOUBLE_TOL);
		
		// valid input, with calibrated imageplus
		ImagePlus ip = new ImagePlus("Sixto",new FloatProcessor(30,30,new float[30*30],null));
		Calibration cal = new Calibration();
		cal.pixelHeight = 1.3;
		cal.pixelWidth = 0.8;
		ip.setCalibration(cal);
		// start an OvalRoi with a single point
		w = 7; h = 22;
		uw = cal.pixelWidth;
		uh = cal.pixelHeight;
		o = new OvalRoi(0,0,ip);
		// expand oval's width
		for (int i = 0; i < w; i++)
			o.nudgeCorner(KeyEvent.VK_RIGHT);
		// expand oval's height
		for (int i = 0; i < h; i++)
			o.nudgeCorner(KeyEvent.VK_DOWN);
		assertEquals(Math.PI*(w*uw+h*uh)/2.0,o.getLength(),Assert.DOUBLE_TOL);
	}

	@Test
	public void testGetPolygon() {
		Polygon p;
		
		// will do simple regression testing
		
		// valid case
		o = new OvalRoi(0,0,2,6);
		p = o.getPolygon();
		assertNotNull(p);
		assertEquals(4,p.npoints);
		assertEquals(2,p.xpoints[0],Assert.DOUBLE_TOL);
		assertEquals(6,p.ypoints[0],Assert.DOUBLE_TOL);
		assertEquals(0,p.xpoints[1],Assert.DOUBLE_TOL);
		assertEquals(6,p.ypoints[1],Assert.DOUBLE_TOL);
		assertEquals(0,p.xpoints[2],Assert.DOUBLE_TOL);
		assertEquals(0,p.ypoints[2],Assert.DOUBLE_TOL);
		assertEquals(2,p.xpoints[3],Assert.DOUBLE_TOL);
		assertEquals(0,p.ypoints[3],Assert.DOUBLE_TOL);

		// bad data case
		o = new OvalRoi(0,0,0,0);
		p = o.getPolygon();
		assertNotNull(p);
		assertEquals(4,p.npoints);
		assertEquals(1,p.xpoints[0],Assert.DOUBLE_TOL);
		assertEquals(1,p.ypoints[0],Assert.DOUBLE_TOL);
		assertEquals(0,p.xpoints[1],Assert.DOUBLE_TOL);
		assertEquals(1,p.ypoints[1],Assert.DOUBLE_TOL);
		assertEquals(0,p.xpoints[2],Assert.DOUBLE_TOL);
		assertEquals(0,p.ypoints[2],Assert.DOUBLE_TOL);
		assertEquals(1,p.xpoints[3],Assert.DOUBLE_TOL);
		assertEquals(0,p.ypoints[3],Assert.DOUBLE_TOL);
	}

	@Test
	public void testDraw() {
		// note - can't test. requires an imagecanvas and thus a gui.
	}

	@Test
	public void testDrawPixelsImageProcessor() {
		
		// will do simple regression testing

		ImageProcessor proc;
		int[] expectedNonZeroes;
		int dims;
		int refVal = 45;

		// small valid case
		dims = 6;
		proc = new ShortProcessor(dims,dims,new short[dims*dims],null);
		proc.setColor(refVal);
		o = new OvalRoi(1,2,3,3);
		o.drawPixels(proc);
		expectedNonZeroes = new int[]{13,14,15,16,19,22,25,28,31,32,33,34};
		RoiHelpers.validateResult(proc, refVal, expectedNonZeroes);
		
		// try another valid case
		dims = 11;
		proc = new ShortProcessor(dims,dims,new short[dims*dims],null);
		proc.setColor(refVal);
		o = new OvalRoi(3,1,8,8);
		o.drawPixels(proc);
		expectedNonZeroes = new int[]{16,17,18,19,20,26,27,31,32,36,37,43,47,58,69,80,81,87,92,93,97,98,104,105,106,107,108};
		RoiHelpers.validateResult(proc, refVal, expectedNonZeroes);
	}

	// TODO note - bugs in OvalRoi.contains() reported to Wayne. He is not fixing. A correct implementation affects ShapeRoi
	//          and Fill negatively. Maybe revisit later.
	@Test
	public void testContains() {

		// circular shape with left corner of region at origin
		o = new OvalRoi(0,0,4,4);
		
		// check nonsense vals
		assertFalse(o.contains(-1,-1));
		assertFalse(o.contains(5,5));
		
		// check corners of region which should be outside of enclosed circle 
		assertFalse(o.contains(0,0));
		assertFalse(o.contains(4,4));
		
		// check all four tangent points
		assertTrue(o.contains(0,2));
		assertTrue(o.contains(2,0));
		//TODO - fails with 144.a3 code: assertTrue(o.contains(2,4));
		//TODO - fails with 144.a3 code: assertTrue(o.contains(4,2));
		
		// check some interior points
		assertTrue(o.contains(2,2));
		assertTrue(o.contains(2,3));
		assertTrue(o.contains(3,2));
		assertTrue(o.contains(1,1));
		//TODO - fails with 144.a3 code: assertTrue(o.contains(3,3));

		// elliptical shape with left corner of region not at 0,0
		o = new OvalRoi(1,1,6,10);

		// check nonsense vals
		assertFalse(o.contains(-1,-1));
		assertFalse(o.contains(8,4));

		// check corners of region which should be outside of enclosed circle 
		assertFalse(o.contains(1,1));
		assertFalse(o.contains(7,11));
		// check all four tangent points
		assertTrue(o.contains(1,6));
		assertTrue(o.contains(4,1));
		//TODO - fails with 144.a3 code: assertTrue(o.contains(4,11));
		//TODO - fails with 144.a3 code: assertTrue(o.contains(7,6));
		
		// check some interior points
		assertTrue(o.contains(4,6));
		assertTrue(o.contains(3,5));
		assertTrue(o.contains(5,7));
		assertTrue(o.contains(3,7));
		assertTrue(o.contains(3,3));
	}

	@Test
	public void testIsHandle() {
		// note - can't fully test - requires an active imagecanvas which we don't have
		
		// compile time test (and also value test - it should always return -1 when we have no imagecanvas)
		o = new OvalRoi(1,2,3,4);
		assertEquals(-1,o.isHandle(1,2));

		// compile time test (and also value test - it should always return -1 when we have no imagecanvas)
		o = new OvalRoi(-4000,1000,-38000,62000);
		assertEquals(-1,o.isHandle(0,0));
	}

	@Test
	public void testGetMask() {
		
		// simple regression testing
		
		ImageProcessor proc;
		int[] expectedNonZeroes;

		// illegal oval
		o = new OvalRoi(0,0,0,0);
		proc = o.getMask();
		expectedNonZeroes = new int[]{0};
		RoiHelpers.validateResult(proc, 255, expectedNonZeroes);
		
		// simplest possible oval
		o = new OvalRoi(0,0,1,1);
		proc = o.getMask();
		expectedNonZeroes = new int[]{0};
		RoiHelpers.validateResult(proc, 255, expectedNonZeroes);
		
		// noncircular, offset from 0,0
		o = new OvalRoi(2,2,4,6);
		proc = o.getMask();
		expectedNonZeroes = new int[]{1,2,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,21,22};
		RoiHelpers.validateResult(proc, 255, expectedNonZeroes);
		
		// 1 pixel high
		o = new OvalRoi(0,0,5,1);
		proc = o.getMask();
		expectedNonZeroes = new int[]{0,1,2,3,4};
		RoiHelpers.validateResult(proc, 255, expectedNonZeroes);
		
		// 1 pixel wide
		o = new OvalRoi(0,0,1,5);
		proc = o.getMask();
		expectedNonZeroes = new int[]{0,1,2,3,4};
		RoiHelpers.validateResult(proc, 255, expectedNonZeroes);

		// noncircular, offset from 0,0, somewhat bigger
		o = new OvalRoi(1,2,8,5);
		proc = o.getMask();
		expectedNonZeroes = new int[]{2,3,4,5,8,9,10,11,12,13,14,15,16,17,18,19,20,21,22,23,24,25,26,27,28,29,30,31,
										34,35,36,37};
		RoiHelpers.validateResult(proc, 255, expectedNonZeroes);
	}

}
