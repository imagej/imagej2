//
// UpdaterTest.java
//

/*
ImageJ software for multidimensional image processing and analysis.

Copyright (c) 2010, ImageJDev.org.
All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met:
    * Redistributions of source code must retain the above copyright
      notice, this list of conditions and the following disclaimer.
    * Redistributions in binary form must reproduce the above copyright
      notice, this list of conditions and the following disclaimer in the
      documentation and/or other materials provided with the distribution.
    * Neither the names of the ImageJDev.org developers nor the
      names of its contributors may be used to endorse or promote products
      derived from this software without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
POSSIBILITY OF SUCH DAMAGE.
*/

package imagej.updater.core;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertTrue;
import imagej.updater.core.FileObject.Status;
import imagej.updater.core.FilesCollection.UpdateSite;
import imagej.updater.util.Progress;
import imagej.updater.util.StderrProgress;
import imagej.updater.util.Util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.zip.GZIPOutputStream;

import javax.xml.parsers.ParserConfigurationException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.xml.sax.SAXException;

public class UpdaterTest {

	final Progress progress = new StderrProgress();
	protected File ijRoot, webRoot;

	//
	// Setup
	//

	@Before
	public void setup() throws IOException {
		ijRoot = createTempDirectory("testUpdaterIJRoot");
		webRoot = createTempDirectory("testUpdaterWebRoot");

		System.err.println("ij: " + ijRoot + ", web: " + webRoot);
	}

	@After
	public void release() {
		rmRF(ijRoot);
		rmRF(webRoot);
	}

	//
	// The tests
	//

	@Test
	public void testUtilityMethods() {
		final long newTimestamp = 20200101000000l;
		assertEquals(newTimestamp, Long.parseLong(Util.timestamp(Util
			.timestamp2millis(newTimestamp))));
	}

	@Test
	public void testInitialUpload() throws Exception {

		final File localDb = new File(ijRoot, "db.xml.gz");

		// The progress indicator

		initializeUpdateSite();

		// Write some files

		final File ijLauncher = writeFile(ijRoot, "ImageJ-linux32", "false");
		ijLauncher.setExecutable(true);

		writeJar(ijRoot, "jars/narf.jar", "README.txt", "Hello");
		writeJar(ijRoot, "jars/egads.jar", "ClassLauncher", "oioioi");

		// Initialize FilesCollection

		FilesCollection files = readDb(false, false);

		// Write the (empty) files collection with the update site information

		assertEquals(0, files.size());
		files.write();
		assertTrue(localDb.exists());

		// Update with the local files

		final Checksummer czechsummer = new Checksummer(files, progress);
		czechsummer.updateFromLocal();

		assertEquals(3, files.size());

		final FileObject ij = files.get("ImageJ-linux32");
		final FileObject narf = files.get("jars/narf.jar");
		final FileObject egads = files.get("jars/egads.jar");

		assertNotSame(null, ij);
		assertNotSame(null, narf);
		assertNotSame(null, egads);

		assertEquals(true, ij.executable);
		assertEquals(false, narf.executable);
		assertEquals(false, egads.executable);

		assertNotSame(ij.current.checksum, narf.current.checksum);
		assertNotSame(narf.current.checksum, egads.current.checksum);
		assertNotSame(egads.current.checksum, ij.current.checksum);

		assertCount(3, files.localOnly());

		for (final FileObject file : files.localOnly()) {
			file.stageForUpload(files, FilesCollection.DEFAULT_UPDATE_SITE);
		}

		assertCount(3, files.toUpload());

		upload(files);

		// Simulate update when everything is up-to-date

		files = readDb(true, true);

		assertCount(3, files);
		assertCount(3, files.upToDate());

		// Simulate update when local db.xml.gz is missing

		localDb.delete();

		files = readDb(false, true);

		assertCount(3, files);
		assertCount(3, files.upToDate());
	}

	@Test
	public void testFilters() throws Exception {
		initializeUpdateSite("macros/Hello.txt", "macros/Comma.txt",
			"macros/World.txt");

		// Make sure that the local db.xml.gz is synchronized with the remote one

		FilesCollection files = readDb(true, true);
		files.write();

		// Modify/delete/add files

		writeFile("macros/World.txt", "not enough");
		writeFile("jars/hello.jar");
		new File(ijRoot, "macros/Comma.txt").delete();

		// Chronological order must be preserved

		files = files.clone(new ArrayList<FileObject>());
		new XMLFileReader(files).read(FilesCollection.DEFAULT_UPDATE_SITE);
		assertCount(3, files);

		final String[] names = new String[4];
		int counter = 0;
		for (final FileObject file : files) {
			names[counter++] = file.getFilename();
		};
		assertEquals(3, counter);
		names[counter++] = "jars/hello.jar";

		files = readDb(true, true);
		counter = 0;
		for (final FileObject file : files) {
			assertEquals("FileObject " + counter, names[counter++], file
				.getFilename());
		}

		// Check that the filters return the correct counts

		assertCount(4, files);
		assertStatus(Status.MODIFIED, files, "macros/World.txt");
		assertCount(1, files.upToDate());
		assertCount(3, files.uploadable());
	}

	//
	// Utility functions
	//

	protected void initializeUpdateSite(final String... fileNames)
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

			for (final String name : fileNames) {
				writeFile(name);
			}

			// Initialize db.xml.gz

			final FilesCollection files = readDb(false);
			assertEquals(0, files.size());

			files.write();
			assertTrue(localDb.exists());

			final Checksummer czechsummer = new Checksummer(files, progress);
			czechsummer.updateFromLocal();

			for (final String name : fileNames) {
				final FileObject file = files.get(name);
				org.junit.Assert.assertNotNull(name, file);
				file.stageForUpload(files, FilesCollection.DEFAULT_UPDATE_SITE);
			}
			uploader = new FilesUploader(files, FilesCollection.DEFAULT_UPDATE_SITE);
			assertTrue(uploader.login());
			uploader.upload(progress);
			assertTrue(remoteDb.exists());
			assertNotSame(remoteDb.length(), remoteDbSize);
		}

	}

	protected FilesCollection readDb(final boolean readLocalDb,
		final boolean runChecksummer) throws IOException,
		ParserConfigurationException, SAXException
	{
		final FilesCollection files = new FilesCollection(ijRoot);
		final File localDb = new File(ijRoot, "db.xml.gz");
		if (readLocalDb) files.read(localDb);
		else {
			assertFalse(localDb.exists());

			// Initialize default update site

			final UpdateSite updateSite =
				files.getUpdateSite(FilesCollection.DEFAULT_UPDATE_SITE);
			assertNotNull(updateSite);

			updateSite.url = webRoot.toURI().toURL().toString() + "/";
			updateSite.sshHost = "file:localhost";
			updateSite.uploadDirectory = webRoot.getAbsolutePath() + "/";
		}
		new XMLFileReader(files).read(FilesCollection.DEFAULT_UPDATE_SITE);
		if (runChecksummer) {
			// We're too fast, cannot trust the cached checksums
			new File(ijRoot, ".checksums").delete();
			final Checksummer czechsummer = new Checksummer(files, progress);
			czechsummer.updateFromLocal();
		}
		return files;
	}

	protected void update(final FilesCollection files) throws IOException {
		final Installer installer = new Installer(files, progress);
		installer.start();
		assertTrue(new File(ijRoot, "update").isDirectory());
		installer.moveUpdatedIntoPlace();
		assertFalse(new File(ijRoot, "update").exists());
	}

	protected void upload(final FilesCollection files) throws Exception {
		final FilesUploader uploader =
			new FilesUploader(files, FilesCollection.DEFAULT_UPDATE_SITE);
		assertTrue(uploader.login());
		uploader.upload(progress);
		files.write();
	}

	protected static void assertStatus(final Status status,
		final FilesCollection files, final String filename)
	{
		final FileObject file = files.get(filename);
		assertNotNull("Object " + filename, file);
		assertEquals("Status of " + filename, status, file.getStatus());
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

	/**
	 * Create a temporary directory
	 * 
	 * @param prefix the prefix as for {@link File.createTempFile}
	 * @return the File object describing the directory
	 * @throws IOException
	 */
	protected File createTempDirectory(final String prefix) throws IOException {
		final File file = File.createTempFile(prefix, "");
		file.delete();
		file.mkdir();
		return file;
	}

	/**
	 * Delete a directory recursively
	 * 
	 * @param directory
	 * @return whether it succeeded (see also {@link File.delete()})
	 */
	protected boolean rmRF(final File directory) {
		if (directory == null) {
			return true;
		}
		final File[] list = directory.listFiles();
		if (list == null) {
			return true;
		}
		for (final File file : list) {
			if (file.isFile()) {
				if (!file.delete()) {
					return false;
				}
			}
			else if (file.isDirectory()) {
				if (!rmRF(file)) {
					return false;
				}
			}
		}
		return directory.delete();
	}

	/**
	 * Write a trivial .jar file into the ijRoot
	 * 
	 * @param name the name of the .jar file
	 * @return the File object for the .jar file
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	protected File writeJar(final String name) throws FileNotFoundException,
		IOException
	{
		return writeJar(ijRoot, name, name, name);
	}

	/**
	 * Write a .jar file into the ijRoot
	 * 
	 * @param name the name of the .jar file
	 * @param args a list of entry name / contents pairs
	 * @return the File object for the .jar file
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	protected File writeJar(final String name, final String... args)
		throws FileNotFoundException, IOException
	{
		return writeJar(ijRoot, name, args);
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
	protected File writeJar(final File dir, final String name,
		final String... args) throws FileNotFoundException, IOException
	{
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
	protected File writeGZippedFile(final File dir, final String name,
		final String content) throws FileNotFoundException, IOException
	{
		final File file = new File(dir, name);
		file.getParentFile().mkdirs();
		writeStream(new GZIPOutputStream(new FileOutputStream(file)), content, true);
		return file;
	}

	/**
	 * Write a text file into the ijRoot
	 * 
	 * @param name The file name
	 * @return the File object for the file that was written to
	 * @throws IOException
	 * @throws FileNotFoundException
	 */
	protected File writeFile(final String name) throws FileNotFoundException,
		IOException
	{
		if (name.endsWith(".jar")) return writeJar(name);
		return writeFile(ijRoot, name, name);
	}

	/**
	 * Write a text file into the ijRoot
	 * 
	 * @param name The file name
	 * @param content The contents to write
	 * @return the File object for the file that was written to
	 * @throws IOException
	 * @throws FileNotFoundException
	 */
	protected File writeFile(final String name, final String content)
		throws FileNotFoundException, IOException
	{
		return writeFile(ijRoot, name, content);
	}

	/**
	 * Write a text file
	 * 
	 * @param dir The directory into which to write
	 * @param name The file name
	 * @param content The contents to write
	 * @return the File object for the file that was written to
	 * @throws IOException
	 * @throws FileNotFoundException
	 */
	protected File writeFile(final File dir, final String name,
		final String content) throws FileNotFoundException, IOException
	{
		final File file = new File(dir, name);
		file.getParentFile().mkdirs();
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
	protected void writeStream(final OutputStream out, final String content,
		final boolean close)
	{
		final PrintWriter writer = new PrintWriter(out);
		writer.println(content);
		if (close) {
			writer.close();
		}
	}

}
