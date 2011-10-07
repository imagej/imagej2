//
// ImageRoiTest.java
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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import ij.Assert;
import ij.IJInfo;
import ij.process.ByteProcessor;
import ij.process.ColorProcessor;

import java.awt.Color;
import java.awt.image.BufferedImage;

import org.junit.Test;

/**
 * Unit tests for {@link ImageRoi}.
 *
 * @author Barry DeZonia
 */
public class ImageRoiTest {

	ImageRoi roi;
	
	@Test
	public void testImageRoiIntIntBufferedImage() {
		
		roi = new ImageRoi(1, 1, new BufferedImage(4, 5, 8));

		assertNotNull(roi);
		assertEquals(Roi.RECTANGLE,roi.getType());
		assertFalse(roi.isDrawingTool());  // test that arcsize == 0
		assertEquals(4,roi.getBounds().width);
		assertEquals(5,roi.getBounds().height);
		assertEquals(Roi.NORMAL,roi.getState());
		assertEquals(Roi.defaultFillColor,roi.getFillColor());
	}

	@Test
	public void testImageRoiIntIntImageProcessor() {

		roi = new ImageRoi(1, 1, new ByteProcessor(2,3,new byte[]{6,5,4,3,2,1},null));

		assertNotNull(roi);
		assertEquals(Color.black,roi.getStrokeColor());
		assertEquals(Roi.RECTANGLE,roi.getType());
		assertFalse(roi.isDrawingTool());  // test that arcsize == 0
		assertEquals(2,roi.getBounds().width);
		assertEquals(3,roi.getBounds().height);
		assertEquals(Roi.NORMAL,roi.getState());
		assertEquals(Roi.defaultFillColor,roi.getFillColor());
	}

	@Test
	public void testSetComposite() {
		roi = new ImageRoi(1, 1, new ByteProcessor(2,3,new byte[]{6,5,4,3,2,1},null));
		
		// no way to test internals. a setter with no getter and side effects are only graphical
		// so just do compile time tests
		roi.setComposite(null);
	}

	@Test
	public void testSetAndGetOpacity() {
		
		// note that ImageRoi::setOpacity() has some untestable side effects (setting private var composite)
		
		roi = new ImageRoi(1, 1, new ColorProcessor(4,2,new int[]{8,8,6,6,4,4,2,2}));
		
		// default
		assertEquals(1.0,roi.getOpacity(),Assert.DOUBLE_TOL);
	
		// outside allowable range - negative
		roi.setOpacity(-0.1);
		assertEquals(0.0,roi.getOpacity(),Assert.DOUBLE_TOL);

		// allowable range
		roi.setOpacity(0.1);
		assertEquals(0.1,roi.getOpacity(),Assert.DOUBLE_TOL);

		// allowable range
		roi.setOpacity(0.4);
		assertEquals(0.4,roi.getOpacity(),Assert.DOUBLE_TOL);

		// allowable range
		roi.setOpacity(0.7);
		assertEquals(0.7,roi.getOpacity(),Assert.DOUBLE_TOL);

		// allowable range
		roi.setOpacity(1.0);
		assertEquals(1.0,roi.getOpacity(),Assert.DOUBLE_TOL);

		// outside allowable range - positive
		roi.setOpacity(1.1);
		assertEquals(1.0,roi.getOpacity(),Assert.DOUBLE_TOL);
	}

	@Test
	public void testDraw() {
		if (IJInfo.RUN_GUI_TESTS) {
			// all gui related - can't test yet
		}
	}

}
