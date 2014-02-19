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

import java.lang.reflect.Method;

import ij.Macro;

import org.junit.Test;

public class HeadlessEnvironmentTest {
	static {
		LegacyInjector.preinit();
	}

	@Test
	public void testMacro() throws Exception {
		final LegacyEnvironment ij1 = new LegacyEnvironment(null, true);
		final String propertyName = "headless.test.property" + Math.random();
		final String propertyValue = "Hello, world!";
		System.setProperty(propertyName, "(unset)");
		assertFalse(propertyValue.equals(System.getProperty(propertyName)));
		ij1.runMacro("call(\"java.lang.System.setProperty\", \"" + propertyName
				+ "\", getArgument());", propertyValue);
		assertEquals(propertyValue, System.getProperty(propertyName));
	}

	@Test
	public void testEncapsulation() throws Exception {
		/*
		 * ImageJ 1.x' ij.Macro.getOptions() always returns null unless the
		 * thread name starts with "Run$_".
		 */
		final Thread thread = Thread.currentThread();
		final String savedName = thread.getName();
		thread.setName("Run$_" + savedName);
		try {
			Macro.setOptions("(unset)");
			assertEquals("(unset) ", Macro.getOptions());
			final LegacyEnvironment ij1 = new LegacyEnvironment(null, true);
			ij1.runMacro("call(\"ij.Macro.setOptions\", \"Hello, world!\");",
					null);
			assertEquals("(unset) ", Macro.getOptions());
			final Method getOptions = ij1.getClassLoader()
					.loadClass("ij.Macro").getMethod("getOptions");
			assertEquals("Hello, world! ", getOptions.invoke(null));
		} finally {
			thread.setName(savedName);
		}
	}
}
