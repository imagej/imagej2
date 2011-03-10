package ij;

import static org.junit.Assert.*;
import org.junit.Test;

// implement the interface so that we have compile time check it exists

public class CommandListenerTest {
	class FakeCL implements CommandListener {
		public String commandExecuting(String command) { return null; }
	}
	
	@Test
	public void testExistence() {
		assertTrue(true);
	}
}
