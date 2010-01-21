package ij.io;


import static org.junit.Assert.*;

import java.io.File;

import ij.io.DirectoryChooser;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class DirectoryChooserTest {

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testDirectoryChooser() {
		if (IJInfo.runGuiTests) {
			DirectoryChooser chooser = new DirectoryChooser("Choose any path");
			assertEquals(true,true);
		}
	}

	// this test is not done in a OS agnostic fashion!!! must fix
	// do these tests need to go away? Or do we need to make mock calls to choose a directory?
	@Test
	public void testGetDirectory() {
		if (IJInfo.runGuiTests) {
			DirectoryChooser chooser = new DirectoryChooser("Choose path called /System/");
			assertEquals("/System/", chooser.getDirectory());
		}
	}

	@Test
	public void testSetDefaultDirectory() {
		if (IJInfo.runGuiTests) {
			DirectoryChooser.setDefaultDirectory("/Library/");
			DirectoryChooser chooser = new DirectoryChooser("See that path == /Library/");
			assertEquals(true,true);
		}
	}

}
