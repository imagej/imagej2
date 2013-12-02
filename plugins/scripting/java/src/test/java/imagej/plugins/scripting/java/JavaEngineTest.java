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

package imagej.plugins.scripting.java;

import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeTrue;
import imagej.script.ScriptLanguage;
import imagej.script.ScriptService;
import imagej.test.TestUtils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Reader;

import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptException;

import org.junit.Before;
import org.junit.Test;
import org.scijava.Context;
import org.scijava.object.ObjectService;
import org.scijava.util.FileUtils;

/**
 * Tests the Java 'scripting' backend.
 * 
 * @author Johannes Schindelin
 */
public class JavaEngineTest {

	@Before
	public void assumeJavaC() {
		boolean found = false;
		try {
			final ClassLoader classLoader = getClass().getClassLoader();
			found = classLoader.loadClass("com.sun.tools.javac.Main") != null;
		}
		catch (final Throwable t) {
			// NB: No action needed.
		}
		assumeTrue(found);
	}

	@Test
	public void minimalProjectFromPOM() throws Exception {
		final File dir = makeMinimalProject();

		boolean result = false;
		try {
			evalJava(new File(dir, "pom.xml"));
		}
		catch (final ScriptException e) {
			final Throwable cause = e.getCause();
			result =
				cause != null && cause.getCause() != null &&
					cause.getCause().getMessage().equals("success");
			if (!result) e.printStackTrace();
		}
		assertTrue(result);

		assertTrue(new File(dir, "target").isDirectory());

		final File jar = new File(dir, "target/MinimalTest-1.0.0.jar");
		assertTrue(jar.exists());

		System.gc();
		assertTrue(FileUtils.deleteRecursively(dir));
	}

	@Test
	public void minimalProjectFromSource() throws Exception {
		final File dir = makeMinimalProject();

		final String source = "" + //
			"package minimaven;\n" + //
			"public class MinimalTest2 {\n" + //
			"\tpublic static void main(final String[] args) throws Exception {\n" + //
			"\t\tthrow new RuntimeException(\"other main class\");\n" + //
			"\t}\n" + //
			"}\n";
		final String path = "minimaven/MinimalTest2.java";
		writeFiles(dir, path, source);
		boolean result = false;
		try {
			evalJava(new File(dir, "src/main/java/" + path));
		}
		catch (final ScriptException e) {
			try {
				result =
					e.getCause().getCause().getMessage().equals("other main class");
			}
			catch (final Throwable t) {
				e.printStackTrace();
			}
		}
		assertTrue(result);

		assertTrue(new File(dir, "target").isDirectory());

		final File jar = new File(dir, "target/MinimalTest-1.0.0.jar");
		assertTrue(jar.exists());

		System.gc();
		assertTrue(FileUtils.deleteRecursively(dir));
	}

	@Test
	public void testEvalReader() throws Exception {
		final String source = "" + //
			"package pinky.brain;\n" + //
			"public class TakeOverTheWorld {\n" + //
			"\tpublic static void main(final String[] arguments) {\n" + //
			"\t\tthrow new RuntimeException(\"Egads!\");\n" + //
			"\t}\n" + //
			"}";

		final ScriptEngine miniMaven = miniMaven();
		boolean result = false;
		try {
			miniMaven.eval(source);
		}
		catch (final ScriptException e) {
			try {
				result = e.getCause().getCause().getMessage().equals("Egads!");
			}
			catch (final Throwable t) {
				e.printStackTrace();
			}
		}
		assertTrue(result);
	}

	// -- helper functions

	private File makeMinimalProject() throws IOException {
		final String source = "" + //
			"package minimaven;\n" + //
			"public class MinimalTest {\n" + //
			"\tpublic static void main(final String[] args) throws Exception {\n" + //
			"\t\tthrow new RuntimeException(\"success\");\n" + //
			"\t}\n" + //
			"}\n";
		final File dir =
			makeProject("minimaven.MinimalTest", "minimaven/MinimalTest.java", source);
		return dir;
	}

	private ScriptEngine evalJava(final File file) throws ScriptException {
		final ScriptEngine miniMaven = miniMaven();
		miniMaven.put(ScriptEngine.FILENAME, file.getPath());
		final ScriptContext context = miniMaven.getContext();
		context.setWriter(new OutputStreamWriter(System.out));
		context.setErrorWriter(new OutputStreamWriter(System.err));
		miniMaven.eval((Reader) null);
		return miniMaven;
	}

	private File makeProject(final String mainClass, final String... args)
		throws IOException
	{
		final File dir = TestUtils.createTemporaryDirectory("java-");

		final FileWriter pom = new FileWriter(new File(dir, "pom.xml"));
		final String xml = "" + //
			"<?xml version=\"2.0\" encoding=\"UTF-8\"?>\n" + //
			"<project xmlns=\"http://maven.apache.org/POM/4.0.0\"\n" + //
			" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" + //
			" xsi:schemaLocation=\"http://maven.apache.org/POM/4.0.0\n" + //
			" http://maven.apache.org/xsd/maven-4.0.0.xsd\">\n" + //
			" <modelVersion>4.0.0</modelVersion>\n" + //
			" <groupId>net.imagej</groupId>\n" + //
			" <artifactId>MinimalTest</artifactId>\n" + //
			" <version>1.0.0</version>\n" + //
			" <build>\n" + //
			"  <plugins>\n" + //
			"   <plugin>\n" + //
			"    <artifactId>maven-jar-plugin</artifactId>\n" + //
			"    <configuration>\n" + //
			"     <archive>\n" + //
			"      <manifest>\n" + //
			"       <mainClass>" + mainClass + "</mainClass>\n" + //
			"      </manifest>\n" + //
			"     </archive>\n" + //
			"    </configuration>\n" + //
			"   </plugin>\n" + //
			"  </plugins>\n" + //
			" </build>\n" + //
			"</project>\n";
		pom.write(xml);
		pom.close();

		writeFiles(dir, args);

		return dir;
	}

	private void writeFiles(final File dir, final String... args)
		throws IOException
	{
		assertTrue("need filename/content pairs", (args.length % 2) == 0);

		for (int i = 0; i < args.length; i += 2) {
			final File src = new File(dir, "src/main/java/" + args[i]);
			final File srcDir = src.getParentFile();
			assertTrue(srcDir.isDirectory() || srcDir.mkdirs());
			final FileWriter writer = new FileWriter(src);
			writer.write(args[i + 1]);
			writer.close();
		}
	}

	private ScriptEngine miniMaven() {
		final Context context =
			new Context(ScriptService.class, ObjectService.class);
		final ObjectService objectService = context.getService(ObjectService.class);
		final ScriptLanguage java =
			objectService.getObjects(JavaScriptLanguage.class).get(0);
		return java.getScriptEngine();
	}

}
