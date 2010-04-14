package ij;

import static org.junit.Assert.*;
import org.junit.Test;

public class UndoTest {
	@Test
	public void testConstants() {
		assertEquals(0,Undo.NOTHING);
		assertEquals(1,Undo.FILTER);
		assertEquals(2,Undo.TYPE_CONVERSION);
		assertEquals(3,Undo.PASTE);
		assertEquals(4,Undo.COMPOUND_FILTER);
		assertEquals(5,Undo.COMPOUND_FILTER_DONE);
		assertEquals(6,Undo.TRANSFORM);
		assertEquals(7,Undo.OVERLAY_ADDITION);
	}
	
	// I don't think I can test this
	//   I can't test internals as it is all private state with no accessors
	//   I can't test side effects
	//     it relies on WindowManager to determine which ImagePlus to manipulate
	//     I somehow need to fake WindowManager into thinking my imagePlus is the one that is active
	//       but it is tied to threads and windows neither of which I have access to.
}
