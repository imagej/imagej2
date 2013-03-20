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

package imagej.updater.core;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import imagej.updater.core.FileObject.Action;
import imagej.updater.core.FileObject.Status;
import imagej.updater.core.FilesCollection.UpdateSite;
import imagej.updater.util.Progress;
import imagej.updater.util.Util;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import javax.xml.parsers.ParserConfigurationException;

import org.scijava.log.LogService;
import org.scijava.util.FileUtils;
import org.xml.sax.SAXException;

/**
 * A container of functions useful for testing the updater/uploader.
 * 
 * @author Johannes Schindelin
 */
public class UpdaterTestUtils {

	/**
	 * This is a hack, albeit not completely a dumb one. As long as you have
	 * swing-updater compiled and up-to-date, you can use this method to inspect
	 * the state at any given moment
	 * 
	 * @param files The collection of files, including the current update site and
	 *          IJ root.
	 */
	protected void show(final FilesCollection files) {
		try {
			String url = getClass().getResource("UpdaterTest.class").toString();
			final String suffix =
				"/core/updater/core/target/test-classes/imagej/updater/core/UpdaterTest.class";
			assertTrue(url.endsWith(suffix));
			url =
				url.substring(0, url.length() - suffix.length()) +
					"/ui/swing/updater/target/classes/";
			final ClassLoader loader =
				new java.net.URLClassLoader(
					new java.net.URL[] { new java.net.URL(url) });
			final Class<?> clazz =
				loader.loadClass("imagej.updater.gui.UpdaterFrame");
			final java.lang.reflect.Constructor<?> ctor =
				clazz.getConstructor(LogService.class, UploaderService.class, FilesCollection.class);
			final Object updaterFrame = ctor.newInstance(Util.getLogService(), null, files);
			final java.lang.reflect.Method setVisible =
				clazz.getMethod("setVisible", boolean.class);
			setVisible.invoke(updaterFrame, true);
			final java.lang.reflect.Method isVisible = clazz.getMethod("isVisible");
			for (;;) {
				Thread.sleep(1000);
				if (isVisible.invoke(updaterFrame).equals(Boolean.FALSE)) break;
			}
		}
		catch (final Throwable t) {
			t.printStackTrace();
		}
	}

	//
	// Utility functions
	//

	protected static File makeIJRoot(final File webRoot) throws IOException {
		final File ijRoot = FileUtils.createTemporaryDirectory("testUpdaterIJRoot", "");
		writeGZippedFile(ijRoot, "db.xml.gz", "<pluginRecords><update-site name=\""
				+ FilesCollection.DEFAULT_UPDATE_SITE + "\" timestamp=\"0\" url=\""
				+ webRoot.toURI().toURL().toString() + "\" ssh-host=\"file:localhost\" "
				+ "upload-directory=\"" + webRoot.getAbsolutePath() + "\"/></pluginRecords>");
		return ijRoot;
	}

	protected static void initializeUpdateSite(final File ijRoot, final File webRoot, final Progress progress, final String... fileNames)
			throws Exception
		{
		final File localDb = new File(ijRoot, "db.xml.gz");
		final File remoteDb = new File(webRoot, "db.xml.gz");

		// Initialize update site

		final String url = webRoot.toURI().toURL().toString() + "/";
		final String sshHost = "file:localhost";
		final String uploadDirectory = webRoot.getAbsolutePath() + "/";

		assertFalse(localDb.exists());
		assertFalse(remoteDb.exists());

		FilesUploader uploader =
			FilesUploader.initialUpload(url, sshHost, uploadDirectory);
		assertTrue(uploader.login());
		uploader.upload(progress);

		assertFalse(localDb.exists());
		assertTrue(remoteDb.exists());
		final long remoteDbSize = remoteDb.length();

		if (fileNames.length > 0) {
			// Write files

			final List<String> list = new ArrayList<String>();
			for (final String name : fileNames) {
				writeFile(new File(ijRoot, name), name);
				list.add(name);
			}

			// Initialize db.xml.gz

			final FilesCollection files = readDb(false, false, ijRoot, webRoot, progress);
			assertEquals(0, files.size());

			files.write();
			assertTrue(localDb.exists());

			final Checksummer czechsummer = new Checksummer(files, progress);
			czechsummer.updateFromLocal(list);

			for (final String name : fileNames) {
				final FileObject file = files.get(name);
				assertNotNull(name, file);
				file.stageForUpload(files, FilesCollection.DEFAULT_UPDATE_SITE);
			}
			uploader = new FilesUploader(files, FilesCollection.DEFAULT_UPDATE_SITE);
			assertTrue(uploader.login());
			uploader.upload(progress);
			assertTrue(remoteDb.exists());
			assertNotEqual(remoteDb.length(), remoteDbSize);
		}

	}

	protected static FilesCollection readDb(final boolean readLocalDb,
			final boolean runChecksummer, final File ijRoot, final File webRoot, final Progress progress) throws IOException,
			ParserConfigurationException, SAXException {
		final FilesCollection files = new FilesCollection(ijRoot);

		// Initialize default update site

		final UpdateSite updateSite =
			files.getUpdateSite(FilesCollection.DEFAULT_UPDATE_SITE);
		assertNotNull(updateSite);

		updateSite.url = webRoot.toURI().toURL().toString() + "/";
		updateSite.sshHost = "file:localhost";
		updateSite.uploadDirectory = webRoot.getAbsolutePath() + "/";

		final File localDb = new File(ijRoot, "db.xml.gz");
		if (runChecksummer) {
			// We're too fast, cannot trust the cached checksums
			new File(ijRoot, ".checksums").delete();
		}
		if (readLocalDb && runChecksummer) {
			files.downloadIndexAndChecksum(progress);
			return files;
		}
		if (readLocalDb) files.read(localDb);
		else {
			assertFalse(localDb.exists());

		}
		new XMLFileReader(files).read(FilesCollection.DEFAULT_UPDATE_SITE);
		if (runChecksummer) {
			final Checksummer czechsummer = new Checksummer(files, progress);
			czechsummer.updateFromLocal();
		}
		return files;
	}

	protected static void update(final FilesCollection files, final Progress progress) throws IOException {
		final File ijRoot = files.prefix(".");
		final Installer installer = new Installer(files, progress);
		installer.start();
		assertTrue(new File(ijRoot, "update").isDirectory());
		installer.moveUpdatedIntoPlace();
		assertFalse(new File(ijRoot, "update").exists());
	}

	protected static void upload(final FilesCollection files, final String updateSite, final Progress progress) throws Exception {
		for (final FileObject file : files.toUpload())
			assertEquals(updateSite, file.updateSite);
		final FilesUploader uploader =
			new FilesUploader(files, updateSite);
		assertTrue(uploader.login());
		uploader.upload(progress);
		files.write();
	}

	protected static FileObject[] makeList(final FilesCollection files) {
		final List<FileObject> list = new ArrayList<FileObject>();
		for (final FileObject object : files)
			list.add(object);
		return list.toArray(new FileObject[list.size()]);
	}

	protected static void assertStatus(final Status status,
		final FilesCollection files, final String filename)
	{
		final FileObject file = files.get(filename);
		assertStatus(status, file);
	}

	protected static void
		assertStatus(final Status status, final FileObject file)
	{
		assertNotNull("Object " + file.getFilename(), file);
		assertEquals("Status of " + file.getFilename(), status, file.getStatus());
	}

	protected static void assertAction(final Action action,
		final FilesCollection files, final String filename)
	{
		assertAction(action, files.get(filename));
	}

	protected static void assertAction(final Action action,
		final FileObject file)
	{
		assertNotNull("Object " + file, file);
		assertEquals("Action of " + file.filename, action, file.getAction());
	}

	protected static void assertNotEqual(final Object object1,
		final Object object2)
	{
		if (object1 == null) {
			assertNotNull(object2);
		}
		else {
			assertFalse(object1.equals(object2));
		}
	}

	protected static void assertNotEqual(final long long1, final long long2) {
		assertTrue(long1 != long2);
	}

	protected static void
		assertCount(final int count, final Iterable<?> iterable)
	{
		assertEquals(count, count(iterable));
	}

	protected static int count(final Iterable<?> iterable) {
		int count = 0;
		for (@SuppressWarnings("unused")
		final Object object : iterable)
		{
			count++;
		}
		return count;
	}

	protected static void print(final Iterable<?> iterable) {
		System.err.println("{");
		int count = 0;
		for (final Object object : iterable) {
			System.err.println("\t" +
				++count +
				": " +
				object +
				(object instanceof FileObject ? " = " +
					((FileObject) object).getStatus() + "/" +
					((FileObject) object).getAction() : ""));
		}
		System.err.println("}");
	}

	/**
	 * Change the mtime of a file
	 * 
	 * @param file the file to touch
	 * @param timestamp the mtime as pseudo-long (YYYYMMDDhhmmss)
	 */
	protected static void touch(final File file, final long timestamp) {
		final long millis = Util.timestamp2millis(timestamp);
		file.setLastModified(millis);
	}

	/**
	 * Write a .jar file
	 * 
	 * @param dir which directory to write into
	 * @param name the name of the .jar file
	 * @param args a list of entry name / contents pairs
	 * @return the File object for the .jar file
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	protected static File writeJar(final File dir, final String name,
		final String... args) throws FileNotFoundException, IOException
	{
		assertTrue((args.length % 2) == 0);
		final File file = new File(dir, name);
		file.getParentFile().mkdirs();
		final JarOutputStream jar = new JarOutputStream(new FileOutputStream(file));
		for (int i = 0; i + 1 < args.length; i += 2) {
			final JarEntry entry = new JarEntry(args[i]);
			jar.putNextEntry(entry);
			jar.write(args[i + 1].getBytes());
			jar.closeEntry();
		}
		jar.close();
		return file;
	}

	protected static File writeJar(final File file, Class<?>... classes) throws FileNotFoundException, IOException {
		file.getParentFile().mkdirs();
		final byte[] buffer = new byte[32768];
		final JarOutputStream jar = new JarOutputStream(new FileOutputStream(file));
		for (int i = 0; i < classes.length; i++) {
			final String path = classes[i].getName().replace('.', '/') + ".class";
			final JarEntry entry = new JarEntry(path);
			jar.putNextEntry(entry);
			final InputStream in = classes[i].getResourceAsStream("/" + path);
			for (;;) {
				int count = in.read(buffer);
				if (count < 0)
					break;
				jar.write(buffer, 0, count);
			}
			in.close();
			jar.closeEntry();
		}
		jar.close();
		return file;
	}

	/**
	 * Write a .gz file
	 * 
	 * @param dir The directory into which to write
	 * @param name The file name
	 * @param content The contents to write
	 * @return the File object for the file that was written to
	 * @throws IOException
	 * @throws FileNotFoundException
	 */
	protected static File writeGZippedFile(final File dir, final String name,
		final String content) throws FileNotFoundException, IOException
	{
		final File file = new File(dir, name);
		file.getParentFile().mkdirs();
		writeStream(new GZIPOutputStream(new FileOutputStream(file)), content, true);
		return file;
	}

	/**
	 * Write a text file
	 * 
	 * @param file The file into which to write
	 * @param content The contents to write
	 * @return the File object for the file that was written to
	 * @throws IOException
	 * @throws FileNotFoundException
	 */
	public static File writeFile(final File file, final String content)
		throws FileNotFoundException, IOException
	{
		final File dir = file.getParentFile();
		if (!dir.isDirectory()) dir.mkdirs();
		final String name = file.getName();
		if (name.endsWith(".jar")) return writeJar(dir, name, content, content);

		writeStream(new FileOutputStream(file), content, true);
		return file;
	}

	/**
	 * Write a string
	 * 
	 * @param out where to write to
	 * @param content what to write
	 * @param close whether to close the stream
	 */
	protected static void writeStream(final OutputStream out, final String content,
		final boolean close)
	{
		final PrintWriter writer = new PrintWriter(out);
		writer.println(content);
		if (close) {
			writer.close();
		}
	}

	/**
	 * Read a gzip'ed stream and return what we got as a String
	 * 
	 * @param in the input stream as compressed by gzip
	 * @return the contents, as a String
	 * @throws IOException
	 */
	protected static String readGzippedStream(final InputStream in) throws IOException {
		return readStream(new GZIPInputStream(in));
	}

	/**
	 * Read a stream and return what we got as a String
	 * 
	 * @param in the input stream
	 * @return the contents, as a String
	 * @throws IOException
	 */
	protected static String readStream(final InputStream in) throws IOException {
		final ByteArrayOutputStream out = new ByteArrayOutputStream();
		final byte[] buffer = new byte[16384];
		for (;;) {
			int count = in.read(buffer);
			if (count < 0) break;
			out.write(buffer, 0, count);
		}
		in.close();
		out.close();
		return out.toString();
	}

}
