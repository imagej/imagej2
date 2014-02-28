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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeTrue;

import java.awt.GraphicsEnvironment;
import java.awt.HeadlessException;
import java.lang.reflect.InvocationTargetException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests that the legacy headless code works as expected.
 * 
 * @author Johannes Schindelin
 */
public class LegacyHeadlessTest {

	static {
		LegacyInjector.preinit();
	}

	private String threadName;

	@Before
	public void saveThreadName() {
		threadName = Thread.currentThread().getName();
	}

	@After
	public void restoreThreadName() {
		if (threadName != null) Thread.currentThread().setName(threadName);
	}

	@Test
	public void testHeadless() throws Exception {
		assertTrue(runExampleDialogPlugin(true));
	}

	@Test
	public void testPatchIsRequired() throws Exception {
		assumeTrue(GraphicsEnvironment.isHeadless());
		assertFalse(runExampleDialogPlugin(false));
	}

	@Test
	public void saveDialog() throws Exception {
		assertTrue(runExamplePlugin(true, "SaveDialog", "file=README.txt", "true"));
	}

	private static boolean runExampleDialogPlugin(final boolean patchHeadless) throws Exception {
		return runExamplePlugin(patchHeadless, "the argument", "prefix=[*** ]", "*** the argument");
	}

	private static boolean runExamplePlugin(final boolean patchHeadless, final String arg, final String macroOptions, final String expectedValue) throws Exception {
		final ClassLoader loader = new LegacyClassLoader(patchHeadless) {
			{
				addURL(Utils.getLocation(Headless_Example_Plugin.class));
			}
		};
		final LegacyEnvironment ij1 = new LegacyEnvironment(loader, patchHeadless);
		try {
			ij1.setMacroOptions(macroOptions);
			final String value = ij1.runPlugIn(
					Headless_Example_Plugin.class.getName(), arg).toString();
			assertEquals(expectedValue, value);
			return true;
		} catch (final Throwable t) {
			if (!(t instanceof InvocationTargetException)
					|| t.getCause() == null
					|| !(t.getCause() instanceof HeadlessException)) {
				t.printStackTrace();
			}
			return false;
		}
	}

}
