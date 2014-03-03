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

package imagej.legacy;

import static org.junit.Assume.assumeTrue;
import imagej.patcher.LegacyClassLoader;
import imagej.patcher.LegacyEnvironment;
import imagej.patcher.LegacyInjector;

import java.awt.GraphicsEnvironment;
import java.net.URL;
import java.net.URLClassLoader;

import org.junit.Test;

/**
 * Ensures that <i>Help>Switch to Modern Mode</i> in patched ImageJ 1.x works as
 * advertised.
 * 
 * @author Johannes Schindelin
 */
public class SwitchToModernTest {

	static {
		LegacyInjector.preinit();
	}

	@Test
	public void testSwitchToModernMode() throws Exception {
		assumeTrue(!GraphicsEnvironment.isHeadless());

		final Thread thread = Thread.currentThread();
		final ClassLoader savedContextClassLoader = thread.getContextClassLoader();

		try {
			final ClassLoader thisLoader = getClass().getClassLoader();
			final URL[] urls = thisLoader instanceof URLClassLoader ? ((URLClassLoader)thisLoader).getURLs() : new URL[0];
			final ClassLoader loader = new LegacyClassLoader(false) {
				{
					for (final URL url : urls) {
						addURL(url);
					}
				}
			};
			final LegacyEnvironment ij1 = new LegacyEnvironment(loader, false);
			ij1.runMacro("call(\"ij.IJ.redirectErrorMessages\");", "");
			ij1.run("Switch to Modern Mode", "");
		} finally {
			thread.setContextClassLoader(savedContextClassLoader);
		}
	}
}
