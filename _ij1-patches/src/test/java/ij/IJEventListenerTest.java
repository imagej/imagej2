package ij;

import static org.junit.Assert.*;

import org.junit.Test;

public class IJEventListenerTest {

	// implement the interface so that we have compile time check it exists
	
	class FakeEL implements IJEventListener {

		public void eventOccurred(int eventID) {
			// do nothing
		}
	}
	
	@Test
	public void testConstants() {
		assertEquals(0,IJEventListener.FOREGROUND_COLOR_CHANGED);
		assertEquals(1,IJEventListener.BACKGROUND_COLOR_CHANGED);
		assertEquals(2,IJEventListener.COLOR_PICKER_CLOSED);
		assertEquals(3,IJEventListener.LOG_WINDOW_CLOSED);
		assertEquals(4,IJEventListener.TOOL_CHANGED);
	}
	
	@Test
	public void testEventOccurred() {
		// do nothing
	}

}
