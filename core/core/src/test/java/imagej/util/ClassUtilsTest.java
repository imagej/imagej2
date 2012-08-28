package imagej.util;

import static org.junit.Assert.*;

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

public class ClassUtilsTest {

	@Test
	public void testUnpackedClass() throws IOException {
		File tmpDir = createTempDirectory("class-utils-test");
		String path = getClass().getName().replace('.', '/') + ".class";
		File classFile = new File(tmpDir, path);
		assertTrue(classFile.getParentFile().exists() || classFile.getParentFile().mkdirs());
		copy(getClass().getResource("/" + path).openStream(), new FileOutputStream(classFile), true);

		ClassLoader classLoader = new URLClassLoader(new URL[] { tmpDir.toURI().toURL() }, null);
		assertEquals(tmpDir, ClassUtils.getLocation(getClass().getName(), classLoader));
	}

	@Test
	public void testClassInJar() throws IOException {
		File jar = File.createTempFile("class-utils-test", ".jar");
		JarOutputStream out = new JarOutputStream(new FileOutputStream(jar));
		String path = getClass().getName().replace('.', '/') + ".class";
		out.putNextEntry(new ZipEntry(path));
		copy(getClass().getResource("/" + path).openStream(), out, true);

		ClassLoader classLoader = new URLClassLoader(new URL[] { jar.toURI().toURL() }, null);
		assertEquals(jar, ClassUtils.getLocation(getClass().getName(), classLoader));
	}

	/**
	 * Create a temporary directory
	 * 
	 * @param prefix the prefix as for {@link File#createTempFile(String, String)}
	 * @return the File object describing the directory
	 * @throws IOException
	 */
	protected static File createTempDirectory(final String prefix) throws IOException {
		final File file = File.createTempFile(prefix, "");
		file.delete();
		file.mkdir();
		return file;
	}

	/**
	 * Copies bytes from an {@link InputStream} to an {@link OutputStream}
	 * 
	 * @param in the source
	 * @param out the sink
	 * @param closeOut whether to close the sink after we're done
	 * @throws IOException
	 */
	protected static void copy(final InputStream in, final OutputStream out, boolean closeOut) throws IOException {
		byte[] buffer = new byte[16384];
		for (;;) {
			int count = in.read(buffer);
			if (count < 0) break;
			out.write(buffer, 0, count);
		}
		in.close();
		if (closeOut) out.close();
	}

}
