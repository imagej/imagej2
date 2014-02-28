package imagej.patcher;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.jar.JarOutputStream;
import java.util.zip.ZipEntry;

public class TestUtils {
	/**
	 * Makes a temporary directory for use with unit tests.
	 * <p>
	 * When the unit test runs in a Maven context, the temporary directory will be
	 * created in the <i>target/</i> directory corresponding to the calling class
	 * instead of <i>/tmp/</i>.
	 * </p>
	 * 
	 * @param prefix the prefix for the directory's name
	 * @return the reference to the newly-created temporary directory
	 * @throws IOException
	 */
	public static File createTemporaryDirectory(final String prefix) throws IOException {
		return createTemporaryDirectory(prefix, getCallingClass(null));
	}

	/**
	 * Makes a temporary directory for use with unit tests.
	 * <p>
	 * When the unit test runs in a Maven context, the temporary directory will be
	 * created in the corresponding <i>target/</i> directory instead of
	 * <i>/tmp/</i>.
	 * </p>
	 * 
	 * @param prefix the prefix for the directory's name
	 * @param forClass the class for context (to determine whether there's a
	 *          <i>target/<i> directory)
	 * @return the reference to the newly-created temporary directory
	 * @throws IOException
	 */
	public static File createTemporaryDirectory(final String prefix,
		final Class<?> forClass) throws IOException
	{
		final URL directory = Utils.getLocation(forClass);
		if (directory != null && "file".equals(directory.getProtocol())) {
			final String path = directory.getPath();
			if (path != null && path.endsWith("/target/test-classes/")) {
				final File baseDirectory =
					new File(path.substring(0, path.length() - 13));
				final File file = File.createTempFile(prefix, "", baseDirectory);
				if (file.delete() && file.mkdir()) return file;
			}
		}
		return createTemporaryDirectory(prefix, "", null);
	}

	/**
	 * Returns the class of the caller (excluding the specified class).
	 * <p>
	 * Sometimes it is convenient to determine the caller's context, e.g. to
	 * determine whether running in a maven-surefire-plugin context (in which case
	 * the location of the caller's class would end in
	 * <i>target/test-classes/</i>).
	 * </p>
	 * 
	 * @param excluding the class to exclude (or null)
	 * @return the class of the caller
	 */
	public static Class<?> getCallingClass(final Class<?> excluding) {
		final String thisClassName = TestUtils.class.getName();
		final String thisClassName2 = excluding == null ? null : excluding.getName();
		final Thread currentThread = Thread.currentThread();
		for (final StackTraceElement element : currentThread.getStackTrace()) {
			final String thatClassName = element.getClassName();
			if (thatClassName == null || thatClassName.equals(thisClassName) ||
				thatClassName.equals(thisClassName2) ||
				thatClassName.startsWith("java.lang.")) {
				continue;
			}
			final ClassLoader loader = currentThread.getContextClassLoader();
			try {
				return loader.loadClass(element.getClassName());
			}
			catch (ClassNotFoundException e) {
				throw new UnsupportedOperationException("Could not load " +
					element.getClassName() + " with the current context class loader (" +
					loader + ")!");
			}
		}
		throw new UnsupportedOperationException("No calling class outside " + thisClassName + " found!");
	}

	/**
	 * Creates a temporary directory.
	 * <p>
	 * Since there is no atomic operation to do that, we create a temporary file,
	 * delete it and create a directory in its place. To avoid race conditions, we
	 * use the optimistic approach: if the directory cannot be created, we try to
	 * obtain a new temporary file rather than erroring out.
	 * </p>
	 * <p>
	 * It is the caller's responsibility to make sure that the directory is
	 * deleted.
	 * </p>
	 * 
	 * @param prefix The prefix string to be used in generating the file's name;
	 *          see {@link File#createTempFile(String, String, File)}
	 * @param suffix The suffix string to be used in generating the file's name;
	 *          see {@link File#createTempFile(String, String, File)}
	 * @param directory The directory in which the file is to be created, or null
	 *          if the default temporary-file directory is to be used
	 * @return: An abstract pathname denoting a newly-created empty directory
	 * @throws IOException
	 */
	public static File createTemporaryDirectory(final String prefix,
		final String suffix, final File directory) throws IOException
	{
		for (int counter = 0; counter < 10; counter++) {
			final File file = File.createTempFile(prefix, suffix, directory);

			if (!file.delete()) {
				throw new IOException("Could not delete file " + file);
			}

			// in case of a race condition, just try again
			if (file.mkdir()) return file;
		}
		throw new IOException(
			"Could not create temporary directory (too many race conditions?)");
	}

	/**
	 * Deletes a directory recursively.
	 * 
	 * @param directory The directory to delete.
	 * @return whether it succeeded (see also {@link File#delete()})
	 */
	public static boolean deleteRecursively(final File directory) {
		if (directory == null) return true;
		final File[] list = directory.listFiles();
		if (list == null) return true;
		for (final File file : list) {
			if (file.isFile()) {
				if (!file.delete()) return false;
			}
			else if (file.isDirectory()) {
				if (!deleteRecursively(file)) return false;
			}
		}
		return directory.delete();
	}

	/**
	 * Bundles the given classes in a new .jar file.
	 * 
	 * @param jarFile the output file
	 * @param classNames the classes to include
	 * @throws IOException
	 */
	public static void makeJar(final File jarFile, final String... classNames) throws IOException {
		final JarOutputStream jar = new JarOutputStream(new FileOutputStream(jarFile));
		final byte[] buffer = new byte[16384];
		final StringBuilder pluginsConfig = new StringBuilder();
		for (final String className : classNames) {
			final String path = className.replace('.',  '/') + ".class";
			final InputStream in = TestUtils.class.getResourceAsStream("/" + path);
			final ZipEntry entry = new ZipEntry(path);
			jar.putNextEntry(entry);
			for (;;) {
				int count = in.read(buffer);
				if (count < 0) break;
				jar.write(buffer,  0, count);
			}
			if (className.indexOf('_') >= 0) {
				final String name = className.substring(className.lastIndexOf('.') + 1).replace('_', ' ');
				pluginsConfig.append("Plugins, \"").append(name).append("\", ").append(className).append("\n");
			}
			in.close();
		}
		if (pluginsConfig.length() > 0) {
			final ZipEntry entry = new ZipEntry("plugins.config");
			jar.putNextEntry(entry);
			jar.write(pluginsConfig.toString().getBytes());
		}
		jar.close();
	}
}
