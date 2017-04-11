/*
 * #%L
 * ImageJ software for multidimensional image processing and analysis.
 * %%
 * Copyright (C) 2009 - 2015 Board of Regents of the University of
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

package net.imagej.app;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import net.imagej.ImageJ;
import net.imagej.ImageJService;
import net.imagej.Main;

import org.junit.Test;
import org.scijava.plugin.Plugin;
import org.scijava.service.AbstractService;
import org.scijava.service.Service;

/**
 * Tests {@link Main}.
 *
 * @author Curtis Rueden
 */
public class MainTest {

	/** Tests launching an alternate main method. */
	@Test
	public void testMains() {
		final ImageJ ij = new ImageJ();
		ij.launch("--main", Concatenate.class.getName(), "kung", "-", "fu");
		assertEquals("kung-fu", Concatenate.s);
		final boolean headless = ij.ui().isHeadless();
		assertEquals(headless, ij.get(LitmusService.class).isDisposed());
		if (!headless) {
			// Since we didn't run headlessly we need to manually dispose the context
			ij.getContext().dispose();
		}
	}

	/**
	 * Tests that the {@link org.scijava.Context} is disposed after running
	 * headlessly.
	 */
	@Test
	public void testHeadless() {
		final ImageJ ij = new ImageJ();
		ij.launch("--headless");
		assertTrue(ij.get(LitmusService.class).isDisposed());
	}

	// -- Helper classes --

	/** A service which knows whether it has been disposed yet. */
	@Plugin(type = Service.class)
	public static class LitmusService extends AbstractService implements
		ImageJService
	{

		private boolean disposed;

		@Override
		public void dispose() {
			disposed = true;
		}

		public boolean isDisposed() {
			return disposed;
		}
	}

	/** Class containing a handy main method for testing. */
	public static class Concatenate {

		public static String s;

		public static void main(final String... args) {
			final StringBuilder sb = new StringBuilder();
			for (final String arg : args) {
				sb.append(arg);
			}
			s = sb.toString();
		}
	}

}
