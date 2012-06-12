
package imagej.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import imagej.service.IService;
import imagej.service.Service;

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

public class CheckSezpozTest {

	@Test
	public void testBasic() throws Exception {
		final File tmpDirectory = createTempDirectory("test-sezpoz");
		final File classes = new File(tmpDirectory, "target/classes");
		assertTrue(classes.mkdirs());
		final File sources = new File(tmpDirectory, "src/main/java");
		assertTrue(sources.mkdirs());

		final File source = new File(sources, "Annotated.java");
		final FileWriter writer = new FileWriter(source);
		writer.append("import imagej.ImageJ;\n"
			+ "import imagej.service.Service;\n"
			+ "import imagej.service.IService;\n" + "\n" + "@Service\n"
			+ "public class Annotated implements IService {\n"
			+ "\tpublic ImageJ getContext() { return null; }\n" + "}\n");
		writer.close();

		final Process process =
			Runtime.getRuntime().exec(
				new String[] { "javac", "-classpath",
					System.getProperty("java.class.path"), "Annotated.java" }, null,
				sources);
		assertEquals(process.waitFor(), 0);

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
		assertTrue(new File(classes, "META-INF/annotations/imagej.service.Service")
			.exists());

		Thread.currentThread().setContextClassLoader(
			new URLClassLoader(new URL[] { classes.toURI().toURL() }));
		assertTrue(sezpozFindsClass(Service.class, IService.class, "Annotated"));
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
	 * Create a temporary directory
	 * 
	 * @param prefix the prefix as for {@link File.createTempFile}
	 * @return the File object describing the directory
	 * @throws IOException
	 */
	protected static File createTempDirectory(final String prefix)
		throws IOException
	{
		final File file = File.createTempFile(prefix, "");
		file.delete();
		file.mkdir();
		return file;
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
