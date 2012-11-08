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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import imagej.plugin.ImageJPlugin;
import imagej.plugin.Plugin;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.net.URL;
import java.net.URLClassLoader;

import net.java.sezpoz.Index;
import net.java.sezpoz.IndexItem;

import org.junit.Test;

/**
 * Unit tests for {@link CheckSezpoz}.
 * 
 * @author Johannes Schindelin
 */
public class CheckSezpozTest {

	@Test
	public void testIsAnnotation() throws Exception {
		final File tmpDirectory = FileUtils.createTemporaryDirectory("test-sezpoz", "");
		final File file = new File(tmpDirectory, "Charlieee.java");
		assertAnnotation(file, "/* Hello */ @Is", true);
		assertAnnotation(file, "/* Hello class \n" + "*/@Blob", true);
		assertAnnotation(file, "/* Hello class */\n" + "// @Blob\n" + " class",
			false);
		assertAnnotation(file,
			"/* Hello class */ import Someclass; /* nothing */ @Annotation", true);
		FileUtils.deleteRecursively(tmpDirectory);
	}

	protected void assertAnnotation(final File file, final String contents,
		final boolean expectAnnotation) throws Exception
	{
		final FileOutputStream out = new FileOutputStream(file);
		out.write(contents.getBytes());
		out.close();
		final boolean hasAnnotation = CheckSezpoz.hasAnnotation(file);
		assertEquals(hasAnnotation, expectAnnotation);
	}

	@Test
	public void testBasic() throws Exception {
		final File tmpDirectory = FileUtils.createTemporaryDirectory("test-sezpoz", "");
		final File classes = new File(tmpDirectory, "target/classes");
		assertTrue(classes.mkdirs());
		final File sources = new File(tmpDirectory, "src/main/java");
		assertTrue(sources.mkdirs());

		final File source = new File(sources, "Annotated.java");
		final FileWriter writer = new FileWriter(source);
		writer.append("import imagej.ImageJ;\n"
			+ "import imagej.Prioritized;\n"
			+ "import imagej.plugin.Plugin;\n"
			+ "import imagej.service.Service;\n"
			+ "\n"
			+ "@Plugin(type = Service.class)\n"
			+ "public class Annotated implements Service {\n"
			+ "\tpublic void initialize() { }\n"
			+ "\tpublic double getPriority() { return 0; }\n"
			+ "\tpublic void setPriority(final double priority) { }\n"
			+ "\tpublic int compareTo(final Prioritized other) { return 0; }\n"
			+ "\tpublic ImageJ getContext() { return null; }\n"
			+ "\tpublic void setContext(final ImageJ context) { }\n"
			+ "}\n");
		writer.close();

		ProcessUtils.exec(sources, System.err, System.out, "javac", "-classpath",
			System.getProperty("java.class.path"), "Annotated.java");

		// to make sure the annotation processor "has not run", we need to copy the
		// .class file
		copy(new FileInputStream(new File(sources, "Annotated.class")),
			new FileOutputStream(new File(classes, "Annotated.class")));

		assertFalse(new File(classes, "META-INF/MANIFEST.MF").exists());
		assertFalse(new File(classes, "META-INF/annotations").exists());
		assertFalse(CheckSezpoz.checkDirectory(classes));
		assertTrue(CheckSezpoz.getLatestCheck(classes.getParentFile()) > 0);

		// second run succeeds
		assertTrue(CheckSezpoz.checkDirectory(classes));
		assertTrue(new File(classes,
			"META-INF/annotations/imagej.plugin.Plugin").exists());

		ClassLoader savedLoader = Thread.currentThread().getContextClassLoader();
		Thread.currentThread().setContextClassLoader(
			new URLClassLoader(new URL[] { classes.toURI().toURL() }));
		assertTrue(sezpozFindsClass(Plugin.class, ImageJPlugin.class, "Annotated"));
		Thread.currentThread().setContextClassLoader(savedLoader);
		FileUtils.deleteRecursively(tmpDirectory);
	}

	protected <S extends Annotation, T> boolean sezpozFindsClass(
		final Class<S> s, final Class<T> t, final String className)
	{
		for (final IndexItem<S, T> item : Index.load(s, t)) {
			if (item.className().equals(className)) return true;
		}
		return false;
	}

	/**
	 * Copy an InputStream into an OutputStream
	 */
	protected static void copy(final InputStream in, final OutputStream out)
		throws IOException
	{
		final byte[] buffer = new byte[16384];
		for (;;) {
			final int count = in.read(buffer);
			if (count < 0) break;
			out.write(buffer, 0, count);
		}
		in.close();
		out.close();
	}
}
