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
import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeTrue;

import org.junit.Test;

import ij.IJ;

/**
 * Tests that the legacy Java agent identifies the correct spot where ImageJ 1.x classes were loaded.
 * 
 * @author Johannes Schindelin
 */
public class JavaAgentIT {

	private StackTraceElement[] trace;

	@Test
	public void testAgentInit() throws Exception {
		assumeTrue("init".equals(System.getProperty("legacy.agent.mode")));
		IJ.log("Now ij.IJ is loaded.");
		assertNotNull(IJ.class.getField("_hooks"));
	}

	@Test
	public void testAgentDebug() {
		assumeTrue("debug".equals(System.getProperty("legacy.agent.mode")));
		try {
			trace = Thread.currentThread().getStackTrace();
			IJ.log("Now ij.IJ would be loaded.");
			assertTrue("This code should not be reached", false);
		} catch (final ExceptionInInitializerError e) {
			final Throwable e2 = e.getCause();
			assertTrue(e2 != null);
			final String message = e2.getMessage();
			assertTrue("Message should begin with 'Loading ij/IJ': " + message,
				message.startsWith("Loading ij/IJ "));
			final StackTraceElement[] stackTrace = e2.getStackTrace();
			assertEquals(getFileName(stackTrace, 0), getFileName(trace, 1));
			assertEquals(getLineNumber(stackTrace, 0), getLineNumber(trace, 1) + 1);
			System.err.println("All is fine, we got the exception:");
			e2.printStackTrace();
		}
	}

	private int getLineNumber(final StackTraceElement[] trace, int no) {
		if (trace == null || trace.length <= no) return -1;
		return trace[no].getLineNumber();
	}

	private String getFileName(final StackTraceElement[] trace, int no) {
		if (trace == null || trace.length <= no) return null;
		return trace[no].getFileName();
	}

}

