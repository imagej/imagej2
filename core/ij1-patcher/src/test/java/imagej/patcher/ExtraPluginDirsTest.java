/*
 * #%L
 * ImageJ software for multidimensional image processing and analysis.
 * %%
 * Copyright (C) 2009 - 2014 Board of Regents of the University of
 * Wisconsin-Madison, Broad Institute of MIT and Harvard, and Max Planck
 * Institute of Molecular Cell Biology and Genetics.
 * %%
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * #L%
 */

package imagej.patcher;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests the support for <i>ij1.plugin.dirs</i> (falling back to the <i>.plugins/</i> subdirectory of <i>user.home</i>).
 * 
 * @author Johannes Schindelin
 */
public class ExtraPluginDirsTest {

	static {
		LegacyInjector.preinit();
	}

	private File tmpDir;

	@After
	public void rmRFTmpDir() {
		if (tmpDir != null && tmpDir.isDirectory()) {
			TestUtils.deleteRecursively(tmpDir);
		}
	}

	@Before
	public void makeTmpDir() throws IOException {
		tmpDir = TestUtils.createTemporaryDirectory("legacy-");
	}

	@Test
	public void findsExtraPluginDir() throws Exception {
		final File jarFile = new File(tmpDir, "Set_Property.jar");
		TestUtils.makeJar(jarFile, Set_Property.class.getName());
		assertTrue(jarFile.getAbsolutePath() + " exists", jarFile.exists());
		System.setProperty("ij1.plugin.dirs", tmpDir.getAbsolutePath());

		final String key = "random-" + Math.random();
		System.setProperty(key, "321");
		final LegacyEnvironment ij1 = new LegacyEnvironment(null, false);
		ij1.run("Set Property", "key=" + key + " value=123");
		assertEquals("123", System.getProperty(key));
	}

	@Test
	public void knowsAboutJarsDirectory() throws Exception {
		final File pluginsDir = new File(tmpDir, "plugins");
		assertTrue(pluginsDir.mkdirs());
		final File jarsDir = new File(tmpDir, "jars");
		assertTrue(jarsDir.mkdirs());
		LegacyEnvironment ij1 = new LegacyEnvironment(null, false);
		final String helperClassName = TestUtils.class.getName();
		try {
			assertNull(ij1.runPlugIn(helperClassName, null));
		} catch (Throwable t) {
			/* all okay, we did not find the class */
		}
		final File jarFile = new File(jarsDir, "helper.jar");
		TestUtils.makeJar(jarFile, helperClassName);
		System.setProperty("plugins.dir", pluginsDir.getAbsolutePath());
		ij1 = new LegacyEnvironment(null, false);
		try {
			assertNotNull(ij1.runPlugIn(helperClassName, null));
		} catch (Throwable t) {
			t.printStackTrace();
			assertNull("Should have found " + helperClassName + " in " + jarFile);
		}
	}
}
