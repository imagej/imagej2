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

// TODO: test updating the updater somehow (store timestamp of .jar/.class and launch the new one if changed? That might be _very_ fragile...)
// TODO: check that multiple upload sites cannot be uploaded to in one go (stageForUpload() should throw an exception in that case)
// TODO: test cross-site dependency
// TODO: test native dependencies
// TODO: what to do with files that Fiji provides already? Take newer?
// TODO: make a nice button to add Fiji...
// TODO: should we have a list of alternative update sites per FileObject so that we can re-parse the alternatives when an update site was removed? Or just tell the user that there was a problem and we need to reparse everything?
// TODO: make a proper upgrade plan for the Fiji Updater

package imagej.updater.core;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertTrue;
import imagej.updater.core.Conflicts.Conflict;
import imagej.updater.core.Conflicts.Resolution;
import imagej.updater.core.FileObject.Action;
import imagej.updater.core.FileObject.Status;
import imagej.updater.core.FilesCollection.UpdateSite;
import imagej.updater.util.Progress;
import imagej.updater.util.StderrProgress;
import imagej.updater.util.Util;
import imagej.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
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

		// bend over for Microsoft
		final boolean isWindows = Util.getPlatform().startsWith("win");
		final String launcherName = isWindows ? "ImageJ-win32.exe" : "ImageJ-linux32";

		final File ijLauncher = writeFile(ijRoot, launcherName, "false");
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

		final FileObject ij = files.get(launcherName);
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
		assertCount(2, files.uploadable());
	}

	@Test
	public void testUpdater() throws Exception {
		final String filename = "macros/hello.ijm";
		final File file = new File(ijRoot, filename);
		final File db = new File(ijRoot, "db.xml.gz");
		initializeUpdateSite(filename);

		// New files should be staged for install by default

		assertTrue(file.delete());
		assertFalse(file.exists());

		// Pretend that db.xml.gz is out-of-date
		FilesCollection files = readDb(true, true);
		files.getUpdateSite(FilesCollection.DEFAULT_UPDATE_SITE).timestamp =
			19991224134121l;
		files.write();

		files = readDb(true, true);

		assertCount(1, files);
		assertCount(1, files.shownByDefault());
		assertStatus(Status.NEW, files, filename);
		assertAction(Action.INSTALL, files, filename);

		// Start the update
		update(files);
		assertTrue(file.exists());

		assertTrue("Recorded remote timestamp", files.getUpdateSite(
			FilesCollection.DEFAULT_UPDATE_SITE).isLastModified(
			new File(webRoot, "db.xml.gz").lastModified()));
		assertStatus(Status.INSTALLED, files, filename);
		assertAction(Action.INSTALLED, files, filename);

		// Modified files should be left alone in a fresh install

		assertTrue(db.delete());
		writeFile(file, "modified");

		files = readDb(false, true);
		assertCount(1, files);
		assertCount(0, files.shownByDefault());
		assertStatus(Status.MODIFIED, files, filename);
		assertAction(Action.MODIFIED, files, filename);

	}

	@Test
	public void testUploadConflicts() throws Exception {
		initializeUpdateSite("macros/obsolete.ijm", "macros/dependency.ijm");

		FilesCollection files = readDb(true, true);
		files.write();

		final FileObject[] list = makeList(files);
		assertEquals(2, list.length);

		final File obsolete = files.prefix(list[0]);
		assertEquals("obsolete.ijm", obsolete.getName());
		final File dependency = files.prefix(list[0]);

		// Make sure files are checksummed again when their timestamp changed

		final String name = "macros/dependencee.ijm";
		final File dependencee = new File(ijRoot, name);
		writeFile(dependencee, "not yet uploaded");
		touch(dependencee, 20030115203432l);

		files = readDb(true, true);
		assertCount(3, files);

		FileObject object = files.get(name);
		assertNotNull(object);
		object.stageForUpload(files, FilesCollection.DEFAULT_UPDATE_SITE);
		assertAction(Action.UPLOAD, files, name);
		object.addDependency(list[0].getFilename(), obsolete);
		object.addDependency(list[1].getFilename(), dependency);

		writeFile(dependencee, "still not uploaded");

		Conflicts conflicts = new Conflicts(files);
		conflicts.conflicts = new ArrayList<Conflict>();
		conflicts.listUploadIssues();
		assertCount(1, conflicts.conflicts);
		Conflict conflict = conflicts.conflicts.get(0);
		assertEquals(conflict.getConflict(), "The timestamp of " + name +
			" changed in the meantime");

		final Resolution[] resolutions = conflict.getResolutions();
		assertEquals(1, resolutions.length);
		assertEquals(20030115203432l, object.newTimestamp);
		resolutions[0].resolve();
		assertNotSame(20030115203432l, object.newTimestamp);

		// Make sure that the resolution allows the upload to succeed

		upload(files);

		// Make sure that obsolete dependencies are detected and repaired

		files = readDb(true, true);

		assertTrue(obsolete.delete());
		writeFile("macros/independent.ijm");
		writeFile(dependencee, "a new version");

		files = readDb(true, true);
		object = files.get(name);
		assertNotNull(object);
		assertStatus(Status.MODIFIED, files, name);
		assertStatus(Status.NOT_INSTALLED, files, list[0].getFilename());
		assertStatus(Status.LOCAL_ONLY, files, "macros/independent.ijm");

		assertCount(2, files.uploadable());

		for (final FileObject object2 : files.uploadable()) {
			object2.stageForUpload(files, FilesCollection.DEFAULT_UPDATE_SITE);
		}
		object = files.get("macros/obsolete.ijm");
		object.setAction(files, Action.REMOVE);

		conflicts = new Conflicts(files);
		conflicts.conflicts = new ArrayList<Conflict>();
		conflicts.listUploadIssues();
		assertCount(2, conflicts.conflicts);

		assertEquals("macros/dependencee.ijm", conflicts.conflicts.get(1)
			.getFilename());
		conflict = conflicts.conflicts.get(0);
		assertEquals("macros/obsolete.ijm", conflict.getFilename());

		// Resolve by breaking the dependency

		final Resolution resolution = conflict.getResolutions()[1];
		assertEquals("Break the dependency", resolution.getDescription());
		resolution.resolve();

		conflicts.conflicts = new ArrayList<Conflict>();
		conflicts.listUploadIssues();
		assertCount(0, conflicts.conflicts);

	}

	@Test
	public void testUpdateConflicts() throws Exception {
		initializeUpdateSite("macros/obsoleted.ijm", "macros/dependency.ijm",
			"macros/locally-modified.ijm", "macros/dependencee.ijm");

		// Add the dependency relations

		FilesCollection files = readDb(true, true);
		FileObject[] list = makeList(files);
		assertEquals(4, list.length);
		FileObject obsoleted = list[0];
		FileObject dependency = list[1];
		FileObject locallyModified = list[2];
		FileObject dependencee = list[3];
		dependencee.addDependency(obsoleted.getFilename(), Util.getTimestamp(files
			.prefix(obsoleted)), true);
		dependencee.addDependency(files, dependency);
		dependencee.addDependency(files, locallyModified);

		assertTrue(files.prefix(obsoleted).delete());
		new Checksummer(files, progress).updateFromLocal();
		assertStatus(Status.NOT_INSTALLED, obsoleted);
		obsoleted.setAction(files, Action.REMOVE);
		writeFile(files.prefix(locallyModified), "modified");
		upload(files);

		assertTrue(files.prefix(dependency).delete());
		assertTrue(files.prefix(dependencee).delete());

		// Now pretend a fresh install

		assertTrue(new File(ijRoot, "db.xml.gz").delete());
		files = readDb(false, true);
		list = makeList(files);
		assertEquals(4, list.length);
		obsoleted = list[0];
		dependency = list[1];
		locallyModified = list[2];
		dependencee = list[3];
		assertStatus(Status.OBSOLETE_UNINSTALLED, obsoleted);
		assertStatus(Status.NEW, dependency);
		assertStatus(Status.MODIFIED, locallyModified);
		assertStatus(Status.NEW, dependencee);

		// Now trigger the conflicts

		writeFile(obsoleted.getFilename());
		new Checksummer(files, progress).updateFromLocal();
		assertStatus(Status.OBSOLETE, obsoleted);

		dependencee.setAction(files, Action.INSTALL);
		dependency.setAction(files, Action.NEW);

		final Conflicts conflicts = new Conflicts(files);
		conflicts.conflicts = new ArrayList<Conflict>();
		conflicts.listUpdateIssues();
		assertCount(3, conflicts.conflicts);

		Conflict conflict = conflicts.conflicts.get(0);
		assertEquals(locallyModified.getFilename(), conflict.getFilename());
		Resolution[] resolutions = conflict.getResolutions();
		assertEquals(2, resolutions.length);
		assertTrue(resolutions[0].getDescription().startsWith("Keep"));
		assertTrue(resolutions[1].getDescription().startsWith("Update"));
		conflict.resolutions[0].resolve();

		conflict = conflicts.conflicts.get(1);
		assertEquals(obsoleted.getFilename(), conflict.getFilename());
		resolutions = conflict.getResolutions();
		assertEquals(2, resolutions.length);
		assertTrue(resolutions[0].getDescription().startsWith("Uninstall"));
		assertTrue(resolutions[1].getDescription().startsWith("Do not update"));
		conflict.resolutions[0].resolve();

		conflict = conflicts.conflicts.get(2);
		assertEquals(null, conflict.getFilename());
		resolutions = conflict.getResolutions();
		assertEquals(1, resolutions.length);
		assertTrue(resolutions[0].getDescription().startsWith("Install"));
		conflict.resolutions[0].resolve();

		update(files);

		assertFalse(files.prefix(obsoleted).exists());
	}

	//
	// Debug functions
	//

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
					"/ui/awt-swing/swing/updater/target/classes/";
			final ClassLoader loader =
				new java.net.URLClassLoader(
					new java.net.URL[] { new java.net.URL(url) });
			final Class<?> clazz =
				loader.loadClass("imagej.updater.gui.UpdaterFrame");
			final java.lang.reflect.Constructor<?> ctor =
				clazz.getConstructor(FilesCollection.class);
			final Object updaterFrame = ctor.newInstance(files);
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
			Log.error(t);
		}
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

			List<String> list = new ArrayList<String>();
			for (final String name : fileNames) {
				writeFile(name);
				list.add(name);
			}

			// Initialize db.xml.gz

			final FilesCollection files = readDb(false, false);
			assertEquals(0, files.size());

			files.write();
			assertTrue(localDb.exists());

			final Checksummer czechsummer = new Checksummer(files, progress);
			czechsummer.updateFromLocal(list);

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

	protected FileObject[] makeList(final FilesCollection files) {
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
		final FileObject file = files.get(filename);
		assertNotNull("Object " + filename, file);
		assertEquals("Status of " + filename, action, file.getAction());
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
	 * Change the mtime of a file
	 * 
	 * @param file the file to touch
	 * @param timestamp the mtime as pseudo-long (YYYYMMDDhhmmss)
	 */
	protected void touch(final File file, final long timestamp) {
		final long millis = Util.timestamp2millis(timestamp);
		file.setLastModified(millis);
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
		return writeFile(file, content);
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
	protected File writeFile(final File file, final String content)
		throws FileNotFoundException, IOException
	{
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
