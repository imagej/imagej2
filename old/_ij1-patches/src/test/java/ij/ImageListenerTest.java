package ij;

// implement the interface so that we have compile time check it exists

import static org.junit.Assert.*;

import org.junit.Test;

public class ImageListenerTest {

	class FakeIL implements ImageListener {

		public void imageClosed(ImagePlus imp) {
			// do nothing
		}

		public void imageOpened(ImagePlus imp) {
			// do nothing
		}

		public void imageUpdated(ImagePlus imp) {
			// do nothing
		}
		
	}
	
	@Test
	public void testExistence() {
		assertTrue(true);
	}
}
