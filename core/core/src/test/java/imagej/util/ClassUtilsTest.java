/*
 * #%L
 * ImageJ software for multidimensional image processing and analysis.
 * %%
 * Copyright (C) 2009 - 2012 Board of Regents of the University of
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

package imagej.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.jar.JarOutputStream;
import java.util.zip.ZipEntry;

import org.junit.Test;

/**
 * Tests {@link ClassUtils}.
 * 
 * @author Johannes Schindelin
 */
public class ClassUtilsTest {

	@Test
	public void testUnpackedClass() throws IOException {
		final File tmpDir = FileUtils.createTemporaryDirectory("class-utils-test", "");
		final String path = getClass().getName().replace('.', '/') + ".class";
		final File classFile = new File(tmpDir, path);
		assertTrue(classFile.getParentFile().exists() ||
			classFile.getParentFile().mkdirs());
		copy(getClass().getResource("/" + path).openStream(),
			new FileOutputStream(classFile), true);

		final ClassLoader classLoader =
			new URLClassLoader(new URL[] { tmpDir.toURI().toURL() }, null);
		final URL location = ClassUtils.getLocation(getClass().getName(),
			classLoader);
		assertEquals(tmpDir, FileUtils.urlToFile(location));
	}

	@Test
	public void testClassInJar() throws IOException {
		final File jar = File.createTempFile("class-utils-test", ".jar");
		final JarOutputStream out = new JarOutputStream(new FileOutputStream(jar));
		final String path = getClass().getName().replace('.', '/') + ".class";
		out.putNextEntry(new ZipEntry(path));
		copy(getClass().getResource("/" + path).openStream(), out, true);

		final ClassLoader classLoader =
			new URLClassLoader(new URL[] { jar.toURI().toURL() }, null);
		final URL location = ClassUtils.getLocation(getClass().getName(), classLoader);
		assertEquals(jar, FileUtils.urlToFile(location));
	}

	/**
	 * Copies bytes from an {@link InputStream} to an {@link OutputStream}.
	 * 
	 * @param in the source
	 * @param out the sink
	 * @param closeOut whether to close the sink after we're done
	 * @throws IOException
	 */
	private static void copy(final InputStream in, final OutputStream out,
		final boolean closeOut) throws IOException
	{
		final byte[] buffer = new byte[16384];
		for (;;) {
			final int count = in.read(buffer);
			if (count < 0) break;
			out.write(buffer, 0, count);
		}
		in.close();
		if (closeOut) out.close();
	}

}
