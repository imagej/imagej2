package ij.gui;

import static org.junit.Assert.*;

import java.awt.Dimension;

import org.junit.Test;

public class TrimmedButtonTest {
	
	TrimmedButton b;

	@Test
	public void testTrimmedButton() {
		b = new TrimmedButton("Hookey Booyah", 4);
		Dimension dims = b.getMinimumSize();
		assertEquals(-4,dims.width);
		assertEquals(0,dims.height);
	}

	@Test
	public void testGetMinimumSize() {
		b = new TrimmedButton("Hookey Booyah", 1);
		Dimension dims = b.getMinimumSize();
		assertEquals(-1,dims.width);
		assertEquals(0,dims.height);
	}

	@Test
	public void testGetPreferredSize() {
		b = new TrimmedButton("Hookey Booyah", -2);
		Dimension dims = b.getPreferredSize();
		assertEquals(2,dims.width);
		assertEquals(0,dims.height);
	}

}
