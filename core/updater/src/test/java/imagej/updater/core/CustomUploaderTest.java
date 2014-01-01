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
 * 
 * The views and conclusions contained in the software and documentation are
 * those of the authors and should not be interpreted as representing official
 * policies, either expressed or implied, of any organization.
 * #L%
 */

package imagej.updater.core;

import static imagej.updater.core.UpdaterTestUtils.writeJar;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeTrue;
import imagej.updater.calvin.HobbesUploader;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashSet;
import java.util.Set;

import org.junit.Test;
import org.scijava.util.CheckSezpoz;
import org.scijava.util.ClassUtils;

/**
 * Tests the auto-install feature of the updater.
 * <p>
 * When an update site has upload information for a protocol that no local
 * component can handle, we try to install the file
 * <i>jars/ij-updater-&lt;protocol&gt;-&lt;version&gt;.jar</i> (if it is
 * installable). This integration test verifies that that functionality is not
 * broken.
 * </p>
 * 
 * @author Johannes Schindelin
 */
public class CustomUploaderTest {

	@Test
	public void testCustomUploader() throws Exception {
		checkSezpoz();
		final ClassLoader loader = makeClassLoaderExcluding(getClass().getClassLoader(), HobbesUploader.class);
		callStaticMethod(loader, getClass(), "realTest", new Class<?>[] { Class.class }, new Object[] { HobbesUploader.class });
	}

	private void checkSezpoz() throws IOException {
		final String url = ClassUtils.getLocation(getClass()).toString();
		if (!url.startsWith("file:") || !url.endsWith("target/test-classes/")) {
			System.err.println("Unexpected location of " + getClass() + ": " + url);
			return;
		}
		final File classes = new File(url.substring(5));
		final File sources = new File(classes, "../../src/test/java");
		CheckSezpoz.fix(classes, sources);
	}

	private static ClassLoader makeClassLoaderExcluding(final ClassLoader loader, final Class<?>... classes) {
		assumeTrue(loader instanceof URLClassLoader);
		final URL[] urls = ((URLClassLoader)loader).getURLs();

		final Set<String> excluding  = new HashSet<String>();
		for (final Class<?> clazz : classes) {
			excluding.add(clazz.getName());
		}

		return new URLClassLoader(urls, loader.getParent()) {
			@Override
			public Class<?> loadClass(final String name) throws ClassNotFoundException {
				if (excluding.contains(name)) throw new ClassNotFoundException("Excluded!");
				return super.loadClass(name);
			}
		};
	}

	private static void callStaticMethod(final ClassLoader loader, final Class<?> clazz, final String methodName,
			final Class<?>[] argTypes, final Object[] args) throws Exception, NoSuchMethodException {
		final Class<?> otherClass = loader.loadClass(clazz.getName());
		final Method method = otherClass.getDeclaredMethod(methodName, argTypes);
		method.setAccessible(true);
		method.invoke(null, args);
	}

	@SuppressWarnings("unused")
	private static void realTest(final Class<?> clazz) throws Exception {
		Thread.currentThread().setContextClassLoader(CustomUploaderTest.class.getClassLoader());
		FilesCollection files = UpdaterTestUtils.initialize(new String[0]);

		assertFalse(hasHobbes(files));

		// copy the custom uploader to the working directory
		final String filename = "ij-updater-hobbes-1.0.0-SNAPSHOT.jar";
		final File target = files.prefix("jars/" + filename);
		writeJar(target, clazz);

		files = UpdaterTestUtils.main(files, new String[] { "upload", "--update-site", "ImageJ", "jars/" + filename });
		target.delete();
		files = UpdaterTestUtils.main(files, new String[] { "list" });

		assertTrue(hasHobbes(files));

		files.removeUpdateSite("Hobbes");
		assertTrue(UpdaterTestUtils.cleanup(files));
	}

	private static boolean hasHobbes(final FilesCollection files) {
		files.addUpdateSite("Hobbes", "hobbes://dummy/", "hobbes:123", "", -1);
		try {
			final FilesUploader filesUploader = new FilesUploader(null, files, "Hobbes", null);
			return filesUploader.hasUploader();
		} catch (IllegalArgumentException e) {
			if (!"No uploader found for protocol hobbes".equals(e.getMessage())) {
				e.printStackTrace();
			}
		}
		return false;
	}

}
