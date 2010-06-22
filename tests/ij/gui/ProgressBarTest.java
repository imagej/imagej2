package ij.gui;

import static org.junit.Assert.*;

import java.awt.Dimension;

import org.junit.Test;

public class ProgressBarTest {

	ProgressBar bar;
	
	@Test
	public void testProgressBar() {
		bar = new ProgressBar(100, 20);
		assertNotNull(bar);
		assertEquals(0,bar.getWidth());   // huh. surprising.
		assertEquals(0,bar.getHeight());  // huh. surprising.
	}

	@Test
	public void testShowDouble() {
		bar = new ProgressBar(100, 20);
		bar.show(20.0);
		// nothing to test - its a visual method
	}

	@Test
	public void testShowDoubleBoolean() {
		bar = new ProgressBar(100, 20);
		bar.show(20.0,false);
		// nothing to test - its a visual method
		bar.show(20.0,true);
		// nothing to test - its a visual method
	}

	@Test
	public void testShowIntInt() {
		bar = new ProgressBar(100, 20);
		for (int i = 0; i < 5; i++)
			bar.show(i,5);
		// nothing to test - its a visual method
	}

	@Test
	public void testUpdateGraphics() {
		// can't test - it needs a Graphics context to work
	}

	@Test
	public void testPaintGraphics() {
		// can't test - it needs a Graphics context to work
	}

	@Test
	public void testGetPreferredSize() {
		bar = new ProgressBar(1600, 384);
		Dimension size = bar.getPreferredSize();
		assertEquals(1600,size.width);
		assertEquals(384,size.height);
	}

	@Test
	public void testSetBatchMode() {
		// no accessor and only visual differences in code - can only test API existence
		bar = new ProgressBar(10,3);
		bar.setBatchMode(false);
		bar.setBatchMode(true);
	}

}
