package ij.gui;

import static org.junit.Assert.*;

import org.junit.Test;

import java.awt.*;
import java.awt.image.BufferedImage;

import ij.Assert;
import ij.IJInfo;
import ij.process.*;

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
