/*
 * #%L
 * ImageJ software for multidimensional image processing and analysis.
 * %%
 * Copyright (C) 2009 - 2013 Board of Regents of the University of
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
 * 
 * The views and conclusions contained in the software and documentation are
 * those of the authors and should not be interpreted as representing official
 * policies, either expressed or implied, of any organization.
 * #L%
 */

package imagej.legacy;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeTrue;

import java.awt.GraphicsEnvironment;
import java.awt.HeadlessException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;

import javassist.ClassPool;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.scijava.Context;
import org.scijava.util.ClassUtils;

/**
 * Tests that the legacy headless code works as expected.
 * 
 * @author Johannes Schindelin
 */
public class LegacyHeadlessTest {

	private String threadName;

	@Before
	public void saveThreadName() {
		threadName = Thread.currentThread().getName();
	}

	@After
	public void restoreThreadName() {
		//if (threadName != null) Thread.currentThread().setName(threadName);
	}

	@Test
	public void testHeadless() {
		assertTrue(runExamplePlugin(getClassLoader(true)));
	}

	@Test
	public void testPatchIsRequired() {
		assumeTrue(GraphicsEnvironment.isHeadless());
		assertFalse(runExamplePlugin(getClassLoader(false)));
	}

	private static boolean runExamplePlugin(final ClassLoader loader) {
		try {
			final String value = runPlugIn(loader,
					Headless_Example_Plugin.class.getName(), "the argument",
					"prefix=[*** ]").toString();
			assertEquals("*** the argument", value);
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

	private static ClassLoader getClassLoader(final boolean patchHeadless) {
		final URL[] urls = {
				ClassUtils.getLocation(ij.IJ.class),
				ClassUtils.getLocation(DefaultLegacyService.class),
				ClassUtils.getLocation(Context.class),
				ClassUtils.getLocation(Headless_Example_Plugin.class)
		};
		// use the bootstrap class loader as parent so that ij.IJ must resolve
		// via the new class loader
		final ClassLoader parent = ClassLoader.getSystemClassLoader().getParent();
		try {
			assertFalse(parent.loadClass("ij.IJ") != null);
		} catch (ClassNotFoundException e) {
			// ignore
		}
		final ClassLoader loader = new URLClassLoader(urls, parent);
		if (patchHeadless) {
			final CodeHacker hacker = new CodeHacker(loader, new ClassPool());
			new LegacyHeadless(hacker).patch();
			hacker.loadClasses();
		}
		return loader;
	}

	private static Object runPlugIn(final ClassLoader loader,
			final String className, final String arg, final String macroOptions)
			throws ClassNotFoundException, SecurityException,
			NoSuchMethodException, IllegalArgumentException,
			IllegalAccessException, InvocationTargetException {
		if (macroOptions != null) {
			Thread.currentThread().setName("Run$_ImageJ 1.x requires the macro's "
				+ "Thread's name to start with: Run$_, otherwise "
				+ "Macro.getOptions() always returns null");
			final Class<?> macro = loader.loadClass("ij.Macro");
			final Method method = macro.getMethod("setOptions", String.class);
			method.invoke(null,  macroOptions);
		}
		final Class<?> ij = loader.loadClass("ij.IJ");
		final Method method = ij.getMethod("runPlugIn", String.class, String.class);
		return method.invoke(null, className, arg);
	}

}
