package ij.gui;

import static org.junit.Assert.*;

import java.awt.Color;
import java.awt.Dimension;

import ij.ImageJ;
import ij.Prefs;
import ij.plugin.MacroInstaller;

import org.junit.Test;

public class ToolbarTest {

	Toolbar bar;
	
	@Test
	public void testPublicConstants()
	{
		assertEquals(0,Toolbar.RECTANGLE);
		assertEquals(1,Toolbar.OVAL);
		assertEquals(2,Toolbar.POLYGON);
		assertEquals(3,Toolbar.FREEROI);
		assertEquals(4,Toolbar.LINE);
		assertEquals(5,Toolbar.POLYLINE);
		assertEquals(6,Toolbar.FREELINE);
		assertEquals(7,Toolbar.POINT);
		assertEquals(7,Toolbar.CROSSHAIR);
		assertEquals(8,Toolbar.WAND);
		assertEquals(9,Toolbar.TEXT);
		assertEquals(10,Toolbar.SPARE1);
		assertEquals(11,Toolbar.MAGNIFIER);
		assertEquals(12,Toolbar.HAND);
		assertEquals(13,Toolbar.DROPPER);
		assertEquals(14,Toolbar.ANGLE);
		assertEquals(15,Toolbar.SPARE2);
		assertEquals(16,Toolbar.SPARE3);
		assertEquals(17,Toolbar.SPARE4);
		assertEquals(18,Toolbar.SPARE5);
		assertEquals(19,Toolbar.SPARE6);
		assertEquals(20,Toolbar.SPARE7);
		assertEquals(21,Toolbar.SPARE8);
		assertEquals(22,Toolbar.SPARE9);
		assertEquals(650,Toolbar.DOUBLE_CLICK_THRESHOLD);
	}
	
	@Test
	public void testToolbar() {
		bar = new Toolbar();
		assertNotNull(bar);
	}

	@Test
	public void testGetToolId() {
		assertEquals(0,Toolbar.getToolId());
		// I have no means to set it to anything else
	}

	@Test
	public void testGetToolIdString() {
		bar = new Toolbar();
		assertEquals(-1,bar.getToolId("fred"));
		assertEquals(22,bar.getToolId("Switch to alternate macro tool sets"));
	}

	@Test
	public void testGetInstance() {
		// it seems that Toolbar might have been designed to be a singleton but its impl is broken.
		// not sure I should enforce this test.
		bar = new Toolbar();
		assertEquals(bar,Toolbar.getInstance());
		Toolbar other = new Toolbar();
		assertEquals(other,Toolbar.getInstance());     // it gets weird here
	}

	@Test
	public void testPaintGraphics() {
		// can't test this as it needs a graphics context
	}

	@Test
	public void testSetToolString() {
		
		bar = new Toolbar();
		assertFalse(bar.setTool("porkChopAccelerator"));
		
		// can't test beyond this. I tried. The setTool(String) method updates the UI. Without an existing
		//   Graphics context it generates a null ptr excep. even starting method with new ImageJ() didn't help.

		//assertTrue(bar.setTool("round"));
		//assertTrue(bar.setTool("rect"));
		//assertTrue(bar.setTool("ellip"));
		//assertTrue(bar.setTool("oval"));
	    //assertTrue(bar.setTool("brush"));
	    //assertTrue(bar.setTool("polygon"));
	    //assertTrue(bar.setTool("polyline"));
	    //assertTrue(bar.setTool("freeline"));
	    //assertTrue(bar.setTool("line"));
		//assertTrue(bar.setTool("arrow"));
		//assertTrue(bar.setTool("free"));
		//assertTrue(bar.setTool("multi"));
		//assertTrue(bar.setTool("point"));
		//assertTrue(bar.setTool("wand"));
		//assertTrue(bar.setTool("text"));
		//assertTrue(bar.setTool("hand"));
		//assertTrue(bar.setTool("zoom"));
		//assertTrue(bar.setTool("dropper"));
		//assertTrue(bar.setTool("color"));
		//assertTrue(bar.setTool("angle"));
		
		// also note that if I could run the above tests I would still have many untested side effects. flesh them
		// out if we ever get past the null ptr exception problem
	}

	@Test
	public void testGetToolName() {
		// since I can't set the tool this will be a feeble test
		assertEquals("rectangle",Toolbar.getToolName());
	}

	@Test
	public void testSetToolInt() {
		// again can't test without a null ptr excep
	}

	@Test
	public void testSetAndGetColor() {
		bar = new Toolbar();
		assertEquals(Color.black,bar.getColor());
		bar.setColor(Color.magenta);
		assertEquals(Color.magenta,bar.getColor());
		bar.setColor(Color.white);
	}

	@Test
	public void testSetAndGetForegroundColor() {
		assertEquals(Color.white,Toolbar.getForegroundColor());
		Toolbar.setForegroundColor(Color.red);
		assertEquals(Color.red,Toolbar.getForegroundColor());
		Toolbar.setForegroundColor(Color.white);
	}

	@Test
	public void testSetAndGetBackgroundColor() {
		assertEquals(Color.white,Toolbar.getBackgroundColor());
		Toolbar.setBackgroundColor(Color.green);
		assertEquals(Color.green,Toolbar.getBackgroundColor());
		Toolbar.setBackgroundColor(Color.white);
	}

	@Test
	public void testGetBrushSize() {
		assertEquals(0,Toolbar.getBrushSize());
		Toolbar.setBrushSize(14);                // I can set it
		assertEquals(0,Toolbar.getBrushSize());  //   but internal "brushEnabled" is false so returns 0.
		
		// once again I need a graphics context if I hope to enable brush
	}

	@Test
	public void testGetRoundRectArcSize() {
		//fail("Not yet implemented");
	}

	@Test
	public void testSetRoundRectArcSize() {
		// a quirk in initialization allows me to call setTool("round"). Without this call getRoundRectArcSize() would
		//   always return 0.
		bar = new Toolbar();
		bar.setTool("round");
		Toolbar.setRoundRectArcSize(99);
		assertEquals(99,Toolbar.getRoundRectArcSize());
		Toolbar.setRoundRectArcSize(0);
		assertEquals(0,Toolbar.getRoundRectArcSize());
	}

	@Test
	public void testGetMultiPointMode() {
		// can't change multipoint mode because setTool() will crash. can only test initial value.
		assertFalse(Toolbar.getMultiPointMode());
	}

	@Test
	public void testGetButtonSize() {
		assertEquals(26,Toolbar.getButtonSize());
	}

	@Test
	public void testMousePressed() {
		// won't test - needs gui and events
	}

	@Test
	public void testRestorePreviousTool() {
		// can't test - uses setTool() which crashes without Graphics context
	}

	@Test
	public void testMouseReleased() {
		// mouseReleased does nothing. make sure it exists though
		bar = new Toolbar();
		bar.mouseReleased(null);
	}

	@Test
	public void testMouseExited() {
		// mouseReleased does nothing. make sure it exists though
		bar = new Toolbar();
		bar.mouseExited(null);
	}

	@Test
	public void testMouseClicked() {
		// mouseReleased does nothing. make sure it exists though
		bar = new Toolbar();
		bar.mouseClicked(null);
	}

	@Test
	public void testMouseEntered() {
		// mouseReleased does nothing. make sure it exists though
		bar = new Toolbar();
		bar.mouseEntered(null);
	}

	@Test
	public void testMouseDragged() {
		// mouseReleased does nothing. make sure it exists though
		bar = new Toolbar();
		bar.mouseDragged(null);
	}

	@Test
	public void testItemStateChanged() {
		// won't test - needs gui
	}

	@Test
	public void testActionPerformed() {
		// won't test - needs gui
	}

	@Test
	public void testGetPreferredSize() {
		bar = new Toolbar();
		Dimension dims = bar.getPreferredSize();
		assertEquals(546,dims.width);
		assertEquals(26,dims.height);
	}

	@Test
	public void testGetMinimumSize() {
		bar = new Toolbar();
		Dimension dims = bar.getMinimumSize();
		assertEquals(546,dims.width);
		assertEquals(26,dims.height);
	}

	@Test
	public void testMouseMoved() {
		// gui oriented - won't test
	}

	@Test
	public void testAddTool() {
		bar = new Toolbar();
		assertEquals(10,bar.addTool("Fluffy Goats"));
		assertEquals(15,bar.addTool("Wonky Cars"));
		assertEquals(16,bar.addTool("Marbled Roast"));
		assertEquals(17,bar.addTool("Stretched Fingers"));
		assertEquals(18,bar.addTool("Enlarged Feet"));
		assertEquals(19,bar.addTool("Zippy Ants"));
		assertEquals(20,bar.addTool("Giant Rutabagas"));
		assertEquals(21,bar.addTool("Unsafe Water"));
		assertEquals(-1,bar.addTool("Hairy Cavemen"));
		assertEquals(-1,bar.addTool("Spritely Butterflies"));
		assertEquals(-1,bar.addTool("Diffident Poets"));
		assertEquals(-1,bar.addTool("Beleaguered Umpires"));
		assertEquals(-1,bar.addTool("Obtuse Deities"));
		
		// there is a lot of functionality in addTool(). We don't have access to getters for names to test results. And
		// we don't have real tools to install. addTool() can call setTool() which will crash if we're not careful. So
		// testing very lightly here.
	}

	@Test
	public void testAddMacroTool() {
		
		bar = new Toolbar();
		
		// install a bunch
		bar.addMacroTool("Zorch", (MacroInstaller) null, -1);
		bar.addMacroTool("Porch", (MacroInstaller) null, -1);
		bar.addMacroTool("Borcht", (MacroInstaller) null, -1);
		bar.addMacroTool("Stork", (MacroInstaller) null, -1);
		bar.addMacroTool("Fork", (MacroInstaller) null, -1);
		bar.addMacroTool("Pork", (MacroInstaller) null, -1);
		
		// check their tool id's
		assertEquals(10,bar.getToolId("Zorch"));
		assertEquals(15,bar.getToolId("Porch"));
		assertEquals(16,bar.getToolId("Borcht"));
		assertEquals(17,bar.getToolId("Stork"));
		assertEquals(18,bar.getToolId("Fork"));
		assertEquals(19,bar.getToolId("Pork"));

		// now install one with flag 0 that says clear previous ones
		bar.addMacroTool("Weegle", (MacroInstaller) null, 0);
		
		// make sure new one in correct place
		assertEquals(10,bar.getToolId("Weegle"));

		// make sure old ones are gone
		assertEquals(-1,bar.getToolId("Zorch"));
		assertEquals(-1,bar.getToolId("Porch"));
		assertEquals(-1,bar.getToolId("Borcht"));
		assertEquals(-1,bar.getToolId("Stork"));
		assertEquals(-1,bar.getToolId("Fork"));
		assertEquals(-1,bar.getToolId("Pork"));
	}

}
