package ij.io;

import static org.junit.Assert.*;

import org.junit.Test;

import ij.io.DirectoryChooser;
import ij.IJInfo;

public class DirectoryChooserTest {

	@Test
	public void testDirectoryChooser() {
		if (IJInfo.RUN_GUI_TESTS) {
			DirectoryChooser chooser = new DirectoryChooser("Choose any path");
			assertEquals(true,true);
		}
	}

	// this test is not done in a OS agnostic fashion!!! must fix
	// do these tests need to go away? Or do we need to make mock calls to choose a directory?
	@Test
	public void testGetDirectory() {
		if (IJInfo.RUN_GUI_TESTS) {
			DirectoryChooser chooser = new DirectoryChooser("Choose path called /System/");
			assertEquals("/System/", chooser.getDirectory());
		}
	}

	@Test
	public void testSetDefaultDirectory() {
		if (IJInfo.RUN_GUI_TESTS) {
			DirectoryChooser.setDefaultDirectory("/Library/");
			DirectoryChooser chooser = new DirectoryChooser("See that path == /Library/");
			assertEquals(true,true);
		}
	}

}
