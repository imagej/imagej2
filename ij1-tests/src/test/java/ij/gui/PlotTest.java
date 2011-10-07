//
// PlotTest.java
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.awt.Color;
import java.awt.Font;

import org.junit.Test;

/**
 * Unit tests for {@link Plot}.
 *
 * @author Barry DeZonia
 */
public class PlotTest {

	// Note - Plot does not really have any accessors. Only package access variables. Therefore I can't do much if
	//   any testing. Code below just makes sure that concstants are correct and that methods exist at compile time.
	
	private static final float[] XVals = new float[]{1,2,3,4};
	private static final float[] YVals = new float[]{2,4,6,8};
	private static final double[] XValsD = new double[]{1,2,3,4};
	private static final double[] YValsD = new double[]{2,4,6,8};
	
	Plot p;

	private Plot newPlot()
	{
		return new Plot("title","xLabel","yLabel",XVals,YVals,Plot.DEFAULT_FLAGS);
	}
	
	@Test
	public void testPublicConstants()
	{
	    assertEquals(0,Plot.CIRCLE);
	    assertEquals(1,Plot.X);
	    assertEquals(3,Plot.BOX);
	    assertEquals(4,Plot.TRIANGLE);
	    assertEquals(5,Plot.CROSS);
	    assertEquals(6,Plot.DOT);
	    assertEquals(2,Plot.LINE);
	    assertEquals(0x1,Plot.X_NUMBERS);
	    assertEquals(0x2,Plot.Y_NUMBERS);
	    assertEquals(0x4,Plot.X_TICKS);
	    assertEquals(0x8,Plot.Y_TICKS);
	    assertEquals(0x10,Plot.X_GRID);
	    assertEquals(0x20,Plot.Y_GRID);
	    assertEquals(0x40,Plot.X_FORCE2GRID);
	    assertEquals(0x80,Plot.Y_FORCE2GRID);
	    assertEquals(Plot.X_NUMBERS +Plot.Y_NUMBERS+Plot.X_GRID+Plot.Y_GRID, Plot.DEFAULT_FLAGS); 
	    assertEquals(60,Plot.LEFT_MARGIN);
	    assertEquals(18,Plot.RIGHT_MARGIN);
	    assertEquals(15,Plot.TOP_MARGIN);
	    assertEquals(40,Plot.BOTTOM_MARGIN);
	}
	
	@Test
	public void testPlotStringStringStringFloatArrayFloatArrayInt() {
		p = new Plot("title","xLabel","yLabel",XVals,YVals,Plot.DEFAULT_FLAGS);
		assertNotNull(p);
	}

	@Test
	public void testPlotStringStringStringFloatArrayFloatArray() {
		p = new Plot("title","xLabel","yLabel",XVals,YVals);
		assertNotNull(p);
	}

	@Test
	public void testPlotStringStringStringDoubleArrayDoubleArrayInt() {
		p = new Plot("title","xLabel","yLabel",XValsD,YValsD,Plot.DEFAULT_FLAGS);
		assertNotNull(p);
	}

	@Test
	public void testPlotStringStringStringDoubleArrayDoubleArray() {
		p = new Plot("title","xLabel","yLabel",XValsD,YValsD);
		assertNotNull(p);
	}

	@Test
	public void testSetLimits() {
		p = newPlot();
		p.setLimits(0, 200, 4, 106);
	}

	@Test
	public void testSetSize() {
		p = newPlot();
		p.setSize(200, 420);
	}

	@Test
	public void testAddPointsFloatArrayFloatArrayInt() {
		p = newPlot();
		p.addPoints(XVals,YVals,XVals.length);
	}

	@Test
	public void testAddPointsDoubleArrayDoubleArrayInt() {
		p = newPlot();
		p.addPoints(XValsD,YValsD,XValsD.length);
	}

	@Test
	public void testAddErrorBarsFloatArray() {
		p = newPlot();
		p.addErrorBars(XVals);
	}

	@Test
	public void testAddErrorBarsDoubleArray() {
		p = newPlot();
		p.addErrorBars(XValsD);
	}

	@Test
	public void testAddLabel() {
		p = newPlot();
		p.addLabel(2.0,6.0,"gitchy");
	}

	@Test
	public void testSetJustification() {
		p = newPlot();
		p.setJustification(2);  // supposedly uses constants such as ImageProcessor.CENTER but it doesn't exist
	}

	@Test
	public void testSetColor() {
		p = newPlot();
		p.setColor(Color.blue);
	}

	@Test
	public void testSetLineWidth() {
		p = newPlot();
		p.setLineWidth(3);
	}

	@Test
	public void testDrawLine() {
		p = newPlot();
		p.drawLine(1, 2, 7, 6);
	}

	@Test
	public void testChangeFont() {
		p = newPlot();
		Font font = Font.decode("System");
		p.changeFont(font);
	}

	@Test
	public void testDraw() {
		p = newPlot();
		p.draw();
	}
	
	@Test
	public void testGetProcessor() {
		p = newPlot();
		p.getProcessor();
	}

	@Test
	public void testGetImagePlus() {
		p = newPlot();
		p.getImagePlus();
	}

	/* Removed 7-20-10
	 * because it shows a window and Hudson is not happy with this.
	@Test
	public void testShow() {
		p = newPlot();
		p.show();
	}
	*/

}
