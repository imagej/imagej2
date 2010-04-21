package ij.gui;

import static org.junit.Assert.*;

import ij.*;
import ij.process.*;

import org.junit.Test;

public class ArrowTest {

	Arrow ar;
	
	@Test
	public void testPublicConstantsAndVars() {
		assertEquals(0,Arrow.FILLED);
		assertEquals(1,Arrow.NOTCHED);
		assertEquals(2,Arrow.OPEN);
		assertEquals("arrow.double",Arrow.DOUBLE_HEADED_KEY);
		assertEquals("arrow.size",Arrow.SIZE_KEY);
		assertEquals("arrow.style",Arrow.STYLE_KEY);
		assertEquals("arrow.width",Arrow.WIDTH_KEY);
		assertArrayEquals(new String[] {"Filled", "Notched", "Open"},Arrow.styles);
	}
	
	@Test
	public void testArrowDoubleDoubleDoubleDouble() {
		
		// constructor sets style to defaultStyle - all internal and untestable
		// constructor sets headsize to defaultHeadSize - all internal and untestable
		// constructor sets doubleheaded to defaultDoubleHeaded - all internal and untestable
		
		ar = new Arrow(-1,-1,-1,-1);
		assertNotNull(ar);
		assertEquals(2,ar.getStrokeWidth(),Assert.DOUBLE_TOL);

		ar = new Arrow(1,1,1,1);
		assertNotNull(ar);
		assertEquals(2,ar.getStrokeWidth(),Assert.DOUBLE_TOL);

		ar = new Arrow(1,2,3,4);
		assertNotNull(ar);
		assertEquals(2,ar.getStrokeWidth(),Assert.DOUBLE_TOL);

		ar = new Arrow(4,3,2,1);
		assertNotNull(ar);
		assertEquals(2,ar.getStrokeWidth(),Assert.DOUBLE_TOL);

		ar = new Arrow(1.4,2.7,12.3,-15.2);
		assertNotNull(ar);
		assertEquals(2,ar.getStrokeWidth(),Assert.DOUBLE_TOL);
	}

	@Test
	public void testArrowIntIntImagePlus() {
		// note - untestable. ctor crashes. it assumes that the ImagePlus has an ImageCanvas which is only true if gui running
		if (IJInfo.RUN_GUI_TESTS) {
			ar = new Arrow(0,0,new ImagePlus("Frubb",new ByteProcessor(2,2,new byte[]{1,2,3,4},null)));
			assertNotNull(ar);
			assertEquals(Arrow.getDefaultWidth(), ar.getStrokeWidth(), Assert.DOUBLE_TOL);
		}
	}

	@Test
	public void testDraw() {
		// note - untestable. again it needs an ImageCanvas to do anything and also a Graphics object
	}

	@Test
	public void testDrawPixelsImageProcessor() {
		ImageProcessor proc;
		
		int dims = 10;
		int refVal = 73;
		
		// single headed - diagonal line
		proc = new ByteProcessor(dims,dims,new byte[dims*dims],null);
		proc.setColor(refVal);
		ar = new Arrow(1,1,3,3);
		ar.drawPixels(proc);
		RoiHelpers.validateResult(proc,refVal,new int[]{0,1,10,11,12,21,22});

		// single headed - horizontal line
		proc = new ByteProcessor(dims,dims,new byte[dims*dims],null);
		proc.setColor(refVal);
		ar = new Arrow(2,2,5,2);
		ar.drawPixels(proc);
		RoiHelpers.validateResult(proc,refVal,new int[]{10,11,20,21,22,23,24,30,31});

		// single headed - vertical line
		proc = new ByteProcessor(dims,dims,new byte[dims*dims],null);
		proc.setColor(refVal);
		ar = new Arrow(2,2,2,5);
		ar.drawPixels(proc);
		RoiHelpers.validateResult(proc,refVal,new int[]{1,2,3,11,12,13,21,22,23,32,42});
		
		// double headed - diagonal line
		proc = new ByteProcessor(dims,dims,new byte[dims*dims],null);
		proc.setColor(refVal);
		ar = new Arrow(1,1,3,3);
		ar.setDoubleHeaded(true);
		ar.drawPixels(proc);
		RoiHelpers.validateResult(proc,refVal,new int[]{0,1,10,11,12,21,22,23,32,33,34,35,43,44,45,46,47,53,54,55,56,57,58,59,64,65,66,
												67,68,69,74,75,76,77,78,79,85,86,87,88,89,95,96,97,98,99});

		// double headed - horizontal line
		proc = new ByteProcessor(dims,dims,new byte[dims*dims],null);
		proc.setColor(refVal);
		ar = new Arrow(2,2,5,2);
		ar.setDoubleHeaded(true);
		ar.drawPixels(proc);
		RoiHelpers.validateResult(proc,refVal,new int[]{8,9,10,11,15,16,17,18,19,20,21,22,23,24,25,26,27,28,29,30,31,35,36,37,38,39,48,49});

		// double headed - vertical line
		proc = new ByteProcessor(dims,dims,new byte[dims*dims],null);
		proc.setColor(refVal);
		ar = new Arrow(2,2,2,5);
		ar.setDoubleHeaded(true);
		ar.drawPixels(proc);
		RoiHelpers.validateResult(proc,refVal,new int[]{1,2,3,11,12,13,21,22,23,32,42,52,61,62,63,71,72,73,81,82,83,90,91,92,93,94});		
	}

	@Test
	public void testIsDrawingTool() {
		
		// isDrawingTool() should always return true
		
		ar = new Arrow(0,0,0,0);
		assertTrue(ar.isDrawingTool());

		ar = new Arrow(-5,0,-1,10000);
		assertTrue(ar.isDrawingTool());
	}

	@Test
	public void testSetAndGetDefaultWidth() {
		double savedWidth = Arrow.getDefaultWidth();
		
		Arrow.setDefaultWidth(22.3);
		assertEquals(22.3,Arrow.getDefaultWidth(),Assert.DOUBLE_TOL);
		
		Arrow.setDefaultWidth(savedWidth);
	}

	@Test
	public void testSetStyle() {
		// note - no access except this setter and no way to test side effects
		// check for compile time access
		ar = new Arrow(1,2,3,4);
		ar.setStyle(Arrow.NOTCHED);
	}

	@Test
	public void testSetAndGetDefaultStyle() {
		int savedStyle = Arrow.getDefaultStyle();
		
		Arrow.setDefaultStyle(Arrow.FILLED);
		assertEquals(Arrow.FILLED,Arrow.getDefaultStyle());
		
		Arrow.setDefaultStyle(savedStyle);
	}

	@Test
	public void testSetHeadSize() {
		// note : no publicly tested state
		// just a compile time check
		ar = new Arrow(1,2,3,4);
		ar.setHeadSize(-1);
		ar.setHeadSize(14.2);
	}

	@Test
	public void testSetAndGetDefaultHeadSize() {
		double savedSize = Arrow.getDefaultHeadSize();
		
		Arrow.setDefaultHeadSize(1006);
		assertEquals(1006,Arrow.getDefaultHeadSize(),Assert.DOUBLE_TOL);
		
		Arrow.setDefaultHeadSize(savedSize);
	}

	@Test
	public void testSetDoubleHeaded() {
		// tested elsewhere in drawPixels(ImageProcessor)
		// just put in compile time test
		ar = new Arrow(3,5,7,9);
		ar.setDoubleHeaded(true);
		ar.setDoubleHeaded(false);
	}

	@Test
	public void testSetAndGetDefaultDoubleHeaded() {
		boolean savedDH = Arrow.getDefaultDoubleHeaded();
		
		Arrow.setDefaultDoubleHeaded(false);
		assertEquals(false,Arrow.getDefaultDoubleHeaded());
		
		Arrow.setDefaultDoubleHeaded(true);
		assertEquals(true,Arrow.getDefaultDoubleHeaded());
		
		Arrow.setDefaultDoubleHeaded(savedDH);
	}

}
