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

// TODO: make a nice button to add Fiji...

package imagej.updater.core;

import static imagej.updater.core.UpdaterTestUtils.assertAction;
import static imagej.updater.core.UpdaterTestUtils.assertCount;
import static imagej.updater.core.UpdaterTestUtils.assertNotEqual;
import static imagej.updater.core.UpdaterTestUtils.assertStatus;
import static imagej.updater.core.UpdaterTestUtils.getWebRoot;
import static imagej.updater.core.UpdaterTestUtils.initDb;
import static imagej.updater.core.UpdaterTestUtils.makeIJRoot;
import static imagej.updater.core.UpdaterTestUtils.makeList;
import static imagej.updater.core.UpdaterTestUtils.progress;
import static imagej.updater.core.UpdaterTestUtils.readGzippedStream;
import static imagej.updater.core.UpdaterTestUtils.touch;
import static imagej.updater.core.UpdaterTestUtils.writeGZippedFile;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import imagej.updater.core.Conflicts.Conflict;
import imagej.updater.core.Conflicts.Resolution;
import imagej.updater.core.FileObject.Action;
import imagej.updater.core.FileObject.Status;
import imagej.updater.core.FilesCollection.UpdateSite;
import imagej.updater.test.Dependencee;
import imagej.updater.util.Progress;
import imagej.updater.test.Dependency;
import imagej.updater.util.Util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.regex.Pattern;
import java.util.zip.GZIPOutputStream;

import javax.xml.parsers.ParserConfigurationException;

import org.junit.After;
import org.junit.Test;
import org.scijava.util.FileUtils;
import org.xml.sax.SAXException;

/**
 * Tests various classes of the {@link imagej.updater} package and subpackages.
 * 
 * @author Johannes Schindelin
 */
public class UpdaterTest {

	private FilesCollection files;

	//
	// Setup
	//

	@After
	public void release() {
		if (files != null) UpdaterTestUtils.cleanup(files);
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

		files = initialize();

		// Write some files

		// bend over for Microsoft
		final boolean isWindows = Util.getPlatform().startsWith("win");
		final String launcherName =
			isWindows ? "ImageJ-win32.exe" : "ImageJ-linux32";

		final File ijLauncher = UpdaterTestUtils.writeFile(files.prefix(launcherName), "false");
		ijLauncher.setExecutable(true);

		UpdaterTestUtils.writeJar(files.prefix("jars/narf.jar"), "README.txt", "Hello");
		UpdaterTestUtils.writeJar(files.prefix("jars/egads.jar"), "ClassLauncher", "oioioi");

		// Initialize FilesCollection

		files.read(files.prefix(Util.XML_COMPRESSED));
		new XMLFileReader(files).read(FilesCollection.DEFAULT_UPDATE_SITE);

		// Write the (empty) files collection with the update site information

		assertEquals(0, files.size());
		files.write();
		final File localDb = files.prefix(Util.XML_COMPRESSED);
		assertTrue(localDb.exists());

		// Update with the local files

		final Checksummer czechsummer = new Checksummer(files, progress);
		czechsummer.updateFromLocal();

		assertEquals(3, files.size());

		final FileObject ij = files.get(launcherName);
		final FileObject narf = files.get("jars/narf.jar");
		final FileObject egads = files.get("jars/egads.jar");

		assertNotEqual(null, ij);
		assertNotEqual(null, narf);
		assertNotEqual(null, egads);

		assertEquals(true, ij.executable);
		assertEquals(false, narf.executable);
		assertEquals(false, egads.executable);

		assertNotEqual(ij.current.checksum, narf.current.checksum);
		assertNotEqual(narf.current.checksum, egads.current.checksum);
		assertNotEqual(egads.current.checksum, ij.current.checksum);

		assertCount(3, files.localOnly());

		for (final FileObject file : files.localOnly()) {
			file.stageForUpload(files, FilesCollection.DEFAULT_UPDATE_SITE);
		}

		assertCount(3, files.toUpload());

		upload(files);

		// Simulate update when everything is up-to-date

		files = readDb();

		assertCount(3, files);
		assertCount(3, files.upToDate());

		// Simulate missing db.xml.gz

		files.read();
		files.clear();
		files.downloadIndexAndChecksum(progress);

		assertCount(3, files);
		assertCount(3, files.upToDate());
	}

	@Test
	public void testFilters() throws Exception {
		files = initialize("macros/Hello.txt", "macros/Comma.txt",
			"macros/World.txt");

		// Make sure that the local db.xml.gz is synchronized with the remote one

		files = readDb();
		files.write();

		// Modify/delete/add files

		writeFile("macros/World.txt", "not enough");
		writeFile("jars/hello.jar");
		files.prefix("macros/Comma.txt").delete();
		assertTrue(files.prefix(".checksums").delete());

		// Chronological order must be preserved

		files = files.clone(new ArrayList<FileObject>());
		new XMLFileReader(files).read(FilesCollection.DEFAULT_UPDATE_SITE);
		assertCount(3, files);

		final String[] names = new String[4];
		int counter = 0;
		for (final FileObject file : files) {
			names[counter++] = file.getFilename();
		}
		assertEquals(3, counter);
		names[counter++] = "jars/hello.jar";

		files = readDb();
		counter = 0;
		for (final FileObject file : files) {
			assertEquals("FileObject " + counter, names[counter++], file
				.getFilename());
		}

		// Check that the filters return the correct counts

		assertCount(4, files);
		assertStatus(Status.MODIFIED, files, "macros/World.txt");
		assertCount(1, files.upToDate());

		// Comma(NOT_INSTALLED), World(MODIFIED), hello(LOCAL_ONLY)
		assertCount(3, files.uploadable());
	}

	@Test
	public void testUpdater() throws Exception {
		final String filename = "macros/hello.ijm";
		files = initialize(filename);
		final File file = files.prefix(filename);

		// New files should be staged for install by default

		assertTrue(file.delete());
		assertFalse(file.exists());

		// Pretend that db.xml.gz is out-of-date
		files.getUpdateSite(FilesCollection.DEFAULT_UPDATE_SITE).timestamp =
			19991224134121l;
		files.write();

		files = readDb();

		assertCount(1, files);
		assertCount(1, files.shownByDefault());
		assertStatus(Status.NEW, files, filename);
		assertAction(Action.INSTALL, files, filename);

		// Start the update
		UpdaterTestUtils.update(files, progress);
		assertTrue(file.exists());

		final File webRoot = getWebRoot(files);
		assertTrue("Recorded remote timestamp", files.getUpdateSite(
			FilesCollection.DEFAULT_UPDATE_SITE).isLastModified(
			new File(webRoot, "db.xml.gz").lastModified()));
		assertStatus(Status.INSTALLED, files, filename);
		assertAction(Action.INSTALLED, files, filename);

		// Modified files should be left alone in a fresh install

		initDb(files);
		UpdaterTestUtils.writeFile(file, "modified");

		files = readDb();
		assertCount(1, files);
		assertCount(0, files.shownByDefault());
		assertStatus(Status.MODIFIED, files, filename);
		assertAction(Action.MODIFIED, files, filename);

	}

	@Test
	public void testUploadConflicts() throws Exception {
		files = initialize("macros/obsolete.ijm", "macros/dependency.ijm");

		files = readDb();
		files.write();

		final FileObject[] list = makeList(files);
		assertEquals(2, list.length);

		final File obsolete = files.prefix(list[0]);
		assertEquals("obsolete.ijm", obsolete.getName());
		final File dependency = files.prefix(list[0]);

		// Make sure files are checksummed again when their timestamp changed

		final String name = "macros/dependencee.ijm";
		final File dependencee = files.prefix(name);
		UpdaterTestUtils.writeFile(dependencee, "not yet uploaded");
		touch(dependencee, 20030115203432l);

		files = readDb();
		assertCount(3, files);

		FileObject object = files.get(name);
		assertNotNull(object);
		object.stageForUpload(files, FilesCollection.DEFAULT_UPDATE_SITE);
		assertAction(Action.UPLOAD, files, name);
		object.addDependency(list[0].getFilename(), obsolete);
		object.addDependency(list[1].getFilename(), dependency);

		UpdaterTestUtils.writeFile(dependencee, "still not uploaded");

		Conflicts conflicts = new Conflicts(files);
		conflicts.conflicts = new ArrayList<Conflict>();
		conflicts.listUploadIssues();
		assertCount(1, conflicts.conflicts);
		Conflict conflict = conflicts.conflicts.get(0);
		assertEquals(conflict.getConflict(), "The timestamp of " + name +
			" changed in the meantime");

		final Resolution[] resolutions = conflict.getResolutions();
		assertEquals(1, resolutions.length);
		assertEquals(20030115203432l, object.localTimestamp);
		resolutions[0].resolve();
		assertNotEqual(20030115203432l, object.localTimestamp);

		// Make sure that the resolution allows the upload to succeed

		upload(files);

		// Make sure that obsolete dependencies are detected and repaired

		files = readDb();

		assertTrue(obsolete.delete());
		writeFile("macros/independent.ijm");
		UpdaterTestUtils.writeFile(dependencee, "a new version");

		files = readDb();
		object = files.get(name);
		assertNotNull(object);
		assertStatus(Status.MODIFIED, files, name);
		assertStatus(Status.NOT_INSTALLED, files, list[0].getFilename());
		assertStatus(Status.LOCAL_ONLY, files, "macros/independent.ijm");

		// obsolete(NOT_INSTALLED), dependencee(MODIFIED), independent(LOCAL_ONLY)
		assertCount(3, files.uploadable());

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
		files = initialize("macros/obsoleted.ijm", "macros/dependency.ijm",
			"macros/locally-modified.ijm", "macros/dependencee.ijm");

		// Add the dependency relations

		files = readDb();
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
		assertTrue(files.getUpdateSite(FilesCollection.DEFAULT_UPDATE_SITE).url.startsWith("file:"));
		new Checksummer(files, progress).updateFromLocal();
		assertStatus(Status.NOT_INSTALLED, obsoleted);
		obsoleted.setAction(files, Action.REMOVE);
		UpdaterTestUtils.writeFile(files.prefix(locallyModified), "modified");
		upload(files);

		assertTrue(files.prefix(dependency).delete());
		assertTrue(files.prefix(dependencee).delete());

		// Now pretend a fresh install

		initDb(files);
		files = readDb();
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

		UpdaterTestUtils.update(files, progress);

		assertFalse(files.prefix(obsoleted).exists());
	}

	@Test
	public void testReChecksumming() throws Exception {
		files = initialize();
		writeFile("jars/new.jar");
		new Checksummer(files, progress).updateFromLocal();
		assertStatus(Status.LOCAL_ONLY, files.get("jars/new.jar"));
		writeFile("jars/new.jar", "modified");
		new Checksummer(files, progress).updateFromLocal();
		assertStatus(Status.LOCAL_ONLY, files.get("jars/new.jar"));
	}

	@Test
	public void testStripVersionFromFilename() {
		assertEquals("jars/bio-formats.jar", FileObject.getFilename("jars/bio-formats-4.4-imagej-2.0.0-beta1.jar", true));
		assertEquals(FileObject.getFilename("jars/ij-data-2.0.0.1-beta1.jar", true), FileObject.getFilename("jars/ij-data-2.0.0.1-SNAPSHOT.jar", true));
		assertEquals(FileObject.getFilename("jars/ij-1.44.jar", true), FileObject.getFilename("jars/ij-1.46b.jar", true));
		assertEquals(FileObject.getFilename("jars/javassist.jar", true), FileObject.getFilename("jars/javassist-3.9.0.GA.jar", true));
		assertEquals(FileObject.getFilename("jars/javassist.jar", true), FileObject.getFilename("jars/javassist-3.16.1-GA.jar", true));
		assertEquals(FileObject.getFilename("jars/bsh.jar", true), FileObject.getFilename("jars/bsh-2.0b4.jar", true));
		assertEquals(FileObject.getFilename("jars/mpicbg.jar", true), FileObject.getFilename("jars/mpicbg-20111128.jar", true));
	}

	@Test
	public void testUpdateVersionedJars() throws Exception {
		files = initialize("jars/obsoleted-2.1.jar", "jars/without.jar",
			"jars/with-2.0.jar", "jars/too-old-3.11.jar", "plugins/plugin.jar");

		// Add the dependency relations

		files = readDb();
		FileObject[] list = makeList(files);
		assertEquals(5, list.length);
		FileObject obsoleted = list[0];
		FileObject without = list[1];
		FileObject with = list[2];
		FileObject tooOld = list[3];
		FileObject plugin = list[4];
		plugin.addDependency(obsoleted.getFilename(), Util.getTimestamp(files
			.prefix(obsoleted)), true);
		plugin.addDependency(files, without);
		plugin.addDependency(files, with);
		plugin.addDependency(files, tooOld);

		assertTrue(plugin.dependencies.containsKey("jars/without.jar"));
		assertTrue(plugin.dependencies.containsKey("jars/with.jar"));
		assertEquals("jars/with-2.0.jar", plugin.dependencies.get("jars/with.jar").filename);
		assertTrue(plugin.dependencies.containsKey("jars/too-old.jar"));
		assertEquals("jars/too-old-3.11.jar", plugin.dependencies.get("jars/too-old.jar").filename);

		assertTrue(files.containsKey("jars/without.jar"));
		assertTrue(files.containsKey("jars/with.jar"));
		assertTrue(files.containsKey("jars/too-old.jar"));

		assertNotNull(files.get("jars/with.jar"));
		assertSame(files.get("jars/with.jar"), files.get("jars/with-2.0.jar"));

		assertTrue(files.prefix(obsoleted).delete());
		new Checksummer(files, progress).updateFromLocal();
		assertStatus(Status.NOT_INSTALLED, obsoleted);
		obsoleted.setAction(files, Action.REMOVE);
		//writeFile(files.prefix(jars), "modified");
		upload(files);

		// Update one .jar file to a newer version

		files = readDb();
		assertTrue(files.prefix("jars/too-old-3.11.jar").delete());
		writeFile("jars/too-old-3.12.jar");
		new Checksummer(files, progress).updateFromLocal();
		tooOld = files.get("jars/too-old.jar");
		assertTrue(tooOld.getFilename().equals("jars/too-old-3.11.jar"));
		assertTrue(tooOld.localFilename.equals("jars/too-old-3.12.jar"));
		tooOld.stageForUpload(files, tooOld.updateSite);
		upload(files);

		// check that webRoot's db.xml.gz's previous versions contain the old filename
		final File webRoot = getWebRoot(files);
		final String db = readGzippedStream(new FileInputStream(new File(webRoot, "db.xml.gz")));
		Pattern regex = Pattern.compile(".*<previous-version [^>]*filename=\"jars/too-old-3.11.jar\".*", Pattern.DOTALL);
		assertTrue(regex.matcher(db).matches());

		assertTrue(new File(webRoot, "jars/too-old-3.12.jar-" + tooOld.localTimestamp).exists());

		// The dependencies should be updated automatically

		files = readDb();
		plugin = files.get("plugins/plugin.jar");
		assertTrue(plugin.dependencies.containsKey("jars/too-old.jar"));
		assertEquals("jars/too-old-3.12.jar", plugin.dependencies.get("jars/too-old.jar").filename);
	}

	@Test
	public void testMultipleVersionsSameSite() throws Exception {
		files = initialize();
		final String db = "<pluginRecords>"
				+ " <plugin filename=\"jars/Jama-1.0.2.jar\">"
				+ "  <previous-version timestamp=\"1\" checksum=\"a\" />"
				+ "  <previous-version timestamp=\"2\" checksum=\"b\" />"
				+ " </plugin>"
				+ " <plugin filename=\"jars/Jama.jar\">"
				+ "  <version checksum=\"d\" timestamp=\"4\" filesize=\"10\" />"
				+ "  <previous-version timestamp=\"3\" checksum=\"c\" />"
				+ " </plugin>"
				+ "</pluginRecords>";
		final File webRoot = getWebRoot(files);
		writeGZippedFile(webRoot, "db.xml.gz", db);
		files = readDb();
		files.clear();
		new XMLFileReader(files).read(FilesCollection.DEFAULT_UPDATE_SITE);
		final FileObject jama = files.get("jars/Jama.jar");
		assertNotNull(jama);
		assertCount(3, jama.previous);
		final FileObject.Version previous[] = new FileObject.Version[3];
		for (final FileObject.Version version : jama.previous) {
			previous[(int)(version.timestamp - 1)] = version;
		}
		assertTrue("a".equals(previous[0].checksum));
		assertEquals("jars/Jama-1.0.2.jar", previous[0].filename);
		assertTrue("b".equals(previous[1].checksum));
		assertEquals("jars/Jama-1.0.2.jar", previous[1].filename);
		assertTrue("c".equals(previous[2].checksum));
		assertEquals("jars/Jama.jar", previous[2].filename);
	}

	@Test
	public void testOverriddenObsolete() throws Exception {
		files = initialize();
		final String db = "<pluginRecords>"
				+ " <plugin filename=\"ImageJ-linux64\">"
				+ "  <previous-version timestamp=\"1\" checksum=\"a\" />"
				+ " </plugin>"
				+ "</pluginRecords>";
		final File webRoot = getWebRoot(files);
		writeGZippedFile(webRoot, "db.xml.gz", db);
		File webRoot2 = FileUtils.createTemporaryDirectory("testUpdaterWebRoot2", "");
		final String db2 = "<pluginRecords>"
				+ " <plugin filename=\"ImageJ-linux64\">"
				+ "  <version checksum=\"c\" timestamp=\"3\" filesize=\"10\" />"
				+ "  <previous-version timestamp=\"2\" checksum=\"b\" />"
				+ " </plugin>"
				+ "</pluginRecords>";
		writeGZippedFile(webRoot2, "db.xml.gz", db2);

		files = readDb();
		files.clear();
		files.addUpdateSite("Fiji", webRoot2.toURI().toURL().toString(), null, null, 0);
		new XMLFileReader(files).read(FilesCollection.DEFAULT_UPDATE_SITE);
		new XMLFileReader(files).read("Fiji");

		FileObject file = files.get("ImageJ-linux64");
		assertNotNull(file);
		assertTrue(file.hasPreviousVersion("a"));
		assertTrue(file.hasPreviousVersion("b"));
		assertTrue(file.hasPreviousVersion("c"));
		assertStatus(Status.NEW, files, "ImageJ-linux64");

		files = readDb();
		files.clear();
		files.getUpdateSite(FilesCollection.DEFAULT_UPDATE_SITE).url = webRoot2.toURI().toURL().toString();
		files.getUpdateSite(FilesCollection.DEFAULT_UPDATE_SITE).timestamp = 0;
		files.addUpdateSite("Fiji", webRoot.toURI().toURL().toString(), null, null, 0);
		new XMLFileReader(files).read(FilesCollection.DEFAULT_UPDATE_SITE);
		new XMLFileReader(files).read("Fiji");

		file = files.get("ImageJ-linux64");
		assertNotNull(file);
		assertTrue(file.hasPreviousVersion("a"));
		assertTrue(file.hasPreviousVersion("b"));
		assertTrue(file.hasPreviousVersion("c"));
		assertStatus(Status.NEW, files, "ImageJ-linux64");

		FileUtils.deleteRecursively(webRoot2);
	}

	@Test
	public void testConflictingVersionsToUpload() throws Exception {
		files = initialize("macros/macro.ijm");

		// There should be an upload conflict if .jar file names differ only in version number

		writeFile("jars/file.jar");
		writeFile("jars/file-3.0.jar");
		files = readDb();
		files.get("jars/file.jar").stageForUpload(files, FilesCollection.DEFAULT_UPDATE_SITE);

		final Conflicts conflicts = new Conflicts(files);
		conflicts.conflicts = new ArrayList<Conflict>();
		conflicts.listUploadIssues();
		assertCount(1, conflicts.conflicts);

		// resolve by deleting the file

		assertTrue(files.prefix("jars/file.jar").exists());
		assertTrue(files.prefix("jars/file-3.0.jar").exists());
		conflicts.conflicts.get(0).getResolutions()[0].resolve();
		assertTrue(files.prefix("jars/file.jar").exists() ^
			files.prefix("jars/file-3.0.jar").exists());

		upload(files);
	}

	@Test
	public void testMultipleUpdateSites() throws Exception {
		// initialize secondary update site
		FilesCollection files2 = UpdaterTestUtils.initialize(null, null, progress, "jars/hello.jar");

		// initialize main update site
		files = initialize("macros/macro.ijm");
		writeJar("jars/hello.jar");
		files = readDb();
		assertStatus(Status.LOCAL_ONLY, files.get("jars/hello.jar"));

		// add second update site
		final File webRoot2 = getWebRoot(files2);
		files.addUpdateSite("second", webRoot2.toURI().toURL().toString(), "file:localhost", webRoot2.getAbsolutePath() + "/", 0l);

		// re-read files from update site
		files.reReadUpdateSite("second", progress);
		assertStatus(Status.INSTALLED, files.get("jars/hello.jar"));
		files.write();

		// modify locally and re-read from update site
		assertTrue(files.prefix(".checksums").delete());
		assertTrue(files.prefix("jars/hello.jar").delete());
		writeJar("jars/hello-2.0.jar", "new-file", "empty");
		new Checksummer(files, progress).updateFromLocal();

		files.reReadUpdateSite("second", progress);
		assertStatus(Status.MODIFIED, files.get("jars/hello.jar"));

		FileUtils.deleteRecursively(webRoot2);
	}

	@Test
	public void testUpdateable() throws Exception {
		files = initialize("jars/hello.jar");
		files = readDb();
		assertStatus(Status.INSTALLED, files.get("jars/hello.jar"));
		String origChecksum = files.get("jars/hello.jar").getChecksum();
		assertEquals(origChecksum, Util.getJarDigest(files.prefix("jars/hello.jar")));

		assertTrue(files.prefix(".checksums").delete());
		assertTrue(files.prefix("jars/hello.jar").delete());
		writeJar("jars/hello-2.0.jar", "new-file", "empty");
		new Checksummer(files, progress).updateFromLocal();
		String newChecksum = files.get("jars/hello.jar").localChecksum;
		assertEquals(newChecksum, Util.getJarDigest(files.prefix("jars/hello-2.0.jar")));

		assertNotEqual(origChecksum, newChecksum);

		// upload that version
		files.get("jars/hello.jar").stageForUpload(files, FilesCollection.DEFAULT_UPDATE_SITE);
		assertEquals(newChecksum, files.get("jars/hello.jar").localChecksum);
		upload(files);

		FilesCollection files2 = new FilesCollection(files.prefix("invalid"));
		final File webRoot = getWebRoot(files);
		files2.getUpdateSite(FilesCollection.DEFAULT_UPDATE_SITE).url = webRoot.toURI().toURL().toString();
		XMLFileDownloader xmlLoader = new XMLFileDownloader(files2);
		xmlLoader.start();
		String newChecksum2 = files2.get("jars/hello.jar").current.checksum;
		assertEquals(newChecksum, newChecksum2);

		// re-write the original version
		assertTrue(files.prefix(".checksums").delete());
		writeFile("jars/hello.jar");
		assertTrue(files.prefix("jars/hello-2.0.jar").delete());
		files = readDb();
		String origChecksum2 = files.get("jars/hello.jar").localChecksum;
		assertEquals(origChecksum, origChecksum2);
		assertEquals(origChecksum, Util.getJarDigest(files.prefix("jars/hello.jar")));

		initDb(files);
		files = readDb();
		assertEquals(newChecksum, files.get("jars/hello.jar").current.checksum);

		assertStatus(Status.UPDATEABLE, files.get("jars/hello.jar"));
		files.get("jars/hello.jar").setAction(files, Action.UPDATE);
		Installer installer = new Installer(files, progress);
		installer.start();
		assertEquals(newChecksum, Util.getJarDigest(files.prefix("update/jars/hello-2.0.jar")));
		installer.moveUpdatedIntoPlace();

		assertStatus(Status.INSTALLED, files.get("jars/hello.jar"));
		assertEquals(newChecksum, Util.getJarDigest(files.prefix("jars/hello-2.0.jar")));

		assertTrue(FileUtils.deleteRecursively(files2.prefix("")));
	}

	@Test
	public void testReReadFiles() throws Exception {
		files = initialize("macros/macro.ijm");
		files = readDb();
		files.get("macros/macro.ijm").description = "Narf";
		files.write();
		upload(files);

		files = readDb();
		assertEquals("Narf", files.get("macros/macro.ijm").description);
		new Checksummer(files, progress).updateFromLocal();
		assertEquals("Narf", files.get("macros/macro.ijm").description);
	}

	@Test
	public void testUpdateTheUpdater() throws Exception {
		final String name1 = "jars/ij-updater-core-1.46n.jar";
		final String name2 = "jars/ij-updater-core-2.0.0.jar";

		// initialize main update site
		files = initialize(name1);

		// "change" updater
		assertTrue(files.prefix(name1).delete());
		writeJar(name2, "files.txt", "modified");
		files = readDb();
		assertTrue(files.get(name1) == files.get(name2));
		assertStatus(Status.MODIFIED, files.get(name1));
		final String modifiedChecksum = files.get(name2).localChecksum;
		files.get(name1).stageForUpload(files, FilesCollection.DEFAULT_UPDATE_SITE);
		upload(files);

		// revert back to "old" updater
		writeJar(name1);
		assertTrue(files.prefix(name2).delete());

		// now the updater should be updated first thing
		files = readDb();
		FileObject file2 = files.get(name2);
		assertNotEqual(file2.localFilename, name2);
		assertTrue(file2.isUpdateable());
		assertNotEqual(modifiedChecksum, files.get(name2).localChecksum);
		assertTrue(Installer.isTheUpdaterUpdateable(files));
		Installer.updateTheUpdater(files, progress);
		assertTrue(files.prefix("update/" + name1).exists());
		assertEquals(0l, files.prefix("update/" + name1).length());
		assertTrue(files.prefix("update/" + name2).exists());

		assertTrue(files.prefix(name1).delete());
		assertFalse(files.prefix(name2).exists());
		assertTrue(files.prefix("update/" + name2).renameTo(files.prefix(name2)));
		new Checksummer(files, progress).updateFromLocal();
		assertEquals(modifiedChecksum, files.get(name2).current.checksum);
	}

	@Test
	public void testFillMetadataFromPOM() throws Exception {
		files = initialize();
		writeJar("jars/hello.jar", "META-INF/maven/egads/hello/pom.xml", "<project>"
				+ " <description>Take over the world!</description>"
				+ " <developers>"
				+ "  <developer><name>Jenna Jenkins</name></developer>"
				+ "  <developer><name>Bugs Bunny</name></developer>"
				+ " </developers>"
				+ "</project>");
		new Checksummer(files, progress).updateFromLocal();
		final FileObject object = files.get("jars/hello.jar");
		assertNotNull(object);
		assertEquals(object.description, "Take over the world!");
		assertCount(2, object.authors);
		final String[] authors = new String[2];
		int counter = 0;
		for (final String author : object.authors) {
			authors[counter++] = author;
		}
		Arrays.sort(authors);
		assertEquals(authors[0], "Bugs Bunny");
		assertEquals(authors[1], "Jenna Jenkins");
	}

	@Test
	public void testPomPropertiesHashing() throws Exception {
		files = initialize();
		final String oldContents = "blub = true\n"
			+ "#Tue Jun 12 06:43:48 IST 2012\n"
			+ "narf.egads = pinkie\n";
		final String newContents = "blub = true\n"
			+ "#Tue Jun 17 09:47:43 CST 2012\n"
			+ "narf.egads = pinkie\n";
		final String fileName =
			"META-INF/maven/net.imagej/updater-test/pom.properties";
		final File oldOldJar =
			writeJarWithDatedFile("old.jar", 2012, 6, 12, fileName, oldContents);
		final File oldNewJar =
			writeJarWithDatedFile("new.jar", 2012, 6, 12, fileName, newContents);
		final File newOldJar =
			writeJarWithDatedFile("old2.jar", 2012, 6, 17, fileName, oldContents);
		final File newNewJar =
			writeJarWithDatedFile("new2.jar", 2012, 6, 17, fileName, newContents);

		// before June 15th, they were considered different
		assertNotEqual(Util.getJarDigest(oldOldJar, false, false, false), Util.getJarDigest(oldNewJar, false, false, false));
		// after June 15th, they are considered unchanged
		assertEquals(Util.getJarDigest(newOldJar, true, false, false), Util.getJarDigest(newNewJar, true, false, false));
		// checksums must be different between the old and new way to calculate them
		assertNotEqual(Util.getJarDigest(oldOldJar, false, false, false), Util.getJarDigest(newOldJar));
	}

	@Test
	public void testManifestHashing() throws Exception {
		files = initialize();
		final String oldContents =
			"Manifest-Version: 1.0\n" + "Built-By: Bugs Bunny\n"
				+ "Main-Class: Buxtehude\n";
		final String newContents =
			"Manifest-Version: 1.0\n" + "Built-By: Donald Duck\n"
				+ "Main-Class: Buxtehude\n";
		final String fileName = "META-INF/MANIFEST.MF";
		final File oldOldJar =
			writeJarWithDatedFile("old.jar", 2012, 7, 4, fileName, oldContents);
		final File oldNewJar =
			writeJarWithDatedFile("new.jar", 2012, 7, 4, fileName, newContents);
		final File newOldJar =
			writeJarWithDatedFile("old2.jar", 2012, 7, 8, fileName, oldContents);
		final File newNewJar =
			writeJarWithDatedFile("new2.jar", 2012, 7, 8, fileName, newContents);

		// before June 15th, they were considered different
		assertNotEqual(Util.getJarDigest(oldOldJar, false, false, false), Util.getJarDigest(oldNewJar, false, false, false));
		// after June 15th, they are considered unchanged
		assertEquals(Util.getJarDigest(newOldJar, false, true, true), Util.getJarDigest(newNewJar, false, true, true));
		// checksums must be different between the old and new way to calculate them
		assertNotEqual(Util.getJarDigest(oldOldJar, false, false, false), Util.getJarDigest(newOldJar));
		assertNotEqual(Util.getJarDigest(oldOldJar, false, true, false), Util.getJarDigest(newOldJar, false, true, true));
	}

	private File writeJarWithDatedFile(final String jarFileName, final int year,
		final int month, final int day, final String fileName,
		final String propertiesContents) throws Exception
	{
		files = initialize();
		final File file = files.prefix(jarFileName);
		final File dir = file.getParentFile();
		if (dir != null && !dir.isDirectory()) assertTrue(dir.mkdirs());
		final JarOutputStream out = new JarOutputStream(new FileOutputStream(file));
		final JarEntry entry = new JarEntry(fileName);
		entry.setTime(new GregorianCalendar(year, month, day).getTimeInMillis());
		out.putNextEntry(entry);
		out.write(propertiesContents.getBytes());
		out.closeEntry();
		out.close();
		return file;
	}

	@Test
	public void testHandlingOfObsoleteChecksums() throws Exception {
		files = initialize();
		final String newContents =
			"blub = true\n" + "#Tue Jun 17 09:47:43 CST 2012\n"
				+ "narf.egads = pinkie\n";
		final String fileName =
			"META-INF/maven/net.imagej/updater-test/pom.properties";
		assertTrue(files.prefix("jars").mkdirs());
		File jar =
			writeJarWithDatedFile("jars/new.jar", 2012, 6, 17, fileName, newContents);

		final String checksumOld = Util.getJarDigest(jar, false, false);
		final String checksumNew = Util.getJarDigest(jar, true, true);
		assertNotEqual(checksumOld, checksumNew);

		final String[][] data =
			{
				// previous current expect
				{ "invalid", checksumOld, checksumOld },
				{ checksumOld, checksumNew, checksumNew },
				{ checksumOld, "something else", checksumOld },
				{ checksumNew, "something else", checksumNew } };
		for (final String[] triplet : data) {
			final FileObject file =
				new FileObject(null, "jars/new.jar", jar.length(), triplet[1], Util
					.getTimestamp(jar), Status.NOT_INSTALLED);
			file.addPreviousVersion(triplet[0], 1, null);
			files.add(file);
			new Checksummer(files, progress).updateFromLocal();
			final FileObject file2 = files.get("jars/new.jar");
			assertTrue(file == file2);
			assertEquals(triplet[1], file.getChecksum());
			assertEquals(triplet[2], file.localChecksum != null ? file.localChecksum
				: file.current.checksum);
		}

		final FileObject file =
			new FileObject(FilesCollection.DEFAULT_UPDATE_SITE, "jars/new.jar", jar.length(), checksumOld, Util
				.getTimestamp(jar), Status.INSTALLED);
		files.add(file);

		final File webRoot = getWebRoot(files);
		new File(webRoot, "jars").mkdirs();
		assertTrue(jar.renameTo(new File(webRoot, "jars/new.jar-" +
			file.current.timestamp)));
		new XMLFileWriter(files).write(new GZIPOutputStream(new FileOutputStream(
			new File(webRoot, "db.xml.gz"))), false);

		files = readDb();
		new Installer(files, progress).start();
		jar = files.prefix("update/jars/new.jar");
		assertTrue(jar.exists());
		assertEquals(checksumNew, Util.getJarDigest(jar));
		assertEquals(checksumOld, files.get("jars/new.jar").getChecksum());
	}

	@Test
	public void testUpdateToDifferentVersion() throws Exception {
		files = initialize("jars/egads-1.0.jar");
		files = readDb();

		// upload a newer version
		assertTrue(files.prefix("jars/egads-1.0.jar").delete());
		writeJar("jars/egads-2.1.jar");
		files = readDb();
		files.get("jars/egads.jar").stageForUpload(files, FilesCollection.DEFAULT_UPDATE_SITE);
		upload(files);

		assertTrue(files.prefix("jars/egads-2.1.jar").exists());
		assertFalse(files.prefix("jars/egads-1.0.jar").exists());

		// downgrade locally
		assertTrue(files.prefix("jars/egads-2.1.jar").delete());
		writeJar("jars/egads-1.0.jar");
		files = readDb();

		// update again
		assertTrue("egads.jar's status: " + files.get("jars/egads.jar").getStatus(), files.get("jars/egads.jar").stageForUpdate(files,  false));
		Installer installer = new Installer(files, progress);
		installer.start();
		assertTrue(files.prefixUpdate("jars/egads-2.1.jar").length() > 0);
		assertTrue(files.prefixUpdate("jars/egads-1.0.jar").length() == 0);
		installer.moveUpdatedIntoPlace();

		assertTrue(files.prefix("jars/egads-2.1.jar").exists());
		assertFalse(files.prefix("jars/egads-1.0.jar").exists());

		// remove the file from the update site
		assertTrue(files.prefix("jars/egads-2.1.jar").delete());
		files = readDb();
		files.get("jars/egads.jar").setAction(files, Action.REMOVE);
		upload(files);

		// re-instate an old version with a different name
		writeJar("jars/egads-1.0.jar");
		files = readDb();
		assertStatus(Status.OBSOLETE, files, "jars/egads.jar");

		// uninstall it
		files.get("jars/egads.jar").stageForUninstall(files);
		installer = new Installer(files, progress);
		installer.start();
		assertFalse(files.prefixUpdate("jars/egads-2.1.jar").exists());
		assertTrue(files.prefixUpdate("jars/egads-1.0.jar").exists());
		assertTrue(files.prefixUpdate("jars/egads-1.0.jar").length() == 0);
		installer.moveUpdatedIntoPlace();
		assertFalse(files.prefixUpdate("jars/egads-1.0.jar").exists());
		assertStatus(Status.OBSOLETE_UNINSTALLED, files, "jars/egads.jar");
	}

	@Test
	public void reconcileMultipleVersions() throws Exception {
		files = initialize();
		UpdaterTestUtils.writeJar(files.prefix("jars/egads-0.1.jar"), "hello", "world");
		files = readDb();
		files.get("jars/egads.jar").stageForUpload(files, FilesCollection.DEFAULT_UPDATE_SITE);
		upload(files);

		assertTrue(files.prefix("jars/egads-0.1.jar").delete());
		UpdaterTestUtils.writeJar(files.prefix("jars/egads-0.2.jar"), "hello", "world2");
		new Checksummer(files, progress).updateFromLocal();
		files.get("jars/egads.jar").stageForUpload(files, FilesCollection.DEFAULT_UPDATE_SITE);
		upload(files);

		UpdaterTestUtils.writeJar(files.prefix("jars/egads-0.1.jar"), "hello", "world");
		UpdaterTestUtils.writeJar(files.prefix("jars/egads.jar"), "hello", "world");
		touch(files.prefix("jars/egads-0.2.jar"), 19800101000001l);
		files = readDb();
		List<Conflict> conflicts = files.getConflicts();
		assertEquals(1, conflicts.size());
		Conflict conflict = conflicts.get(0);
		assertEquals(conflict.filename, "jars/egads-0.2.jar");
		conflict.resolutions[1].resolve();
		assertFalse(files.prefix("jars/egads.jar").exists());
		assertFalse(files.prefix("jars/egads-0.1.jar").exists());
		assertTrue(files.prefix("jars/egads-0.2.jar").exists());
	}

	@Test
	public void uninstallRemoved() throws Exception {
		files = initialize("jars/to-be-removed.jar");
		files = readDb();
		files.write();

		final File ijRoot = files.prefix("");
		final File webRoot = getWebRoot(files);
		File ijRoot2 = makeIJRoot(webRoot);

		files = new FilesCollection(ijRoot2);
		files.read();
		files.downloadIndexAndChecksum(progress);
		files.get("jars/to-be-removed.jar").setAction(files, Action.REMOVE);
		upload(files);

		// make sure that the timestamp of the update site is "new"
		files = new FilesCollection(ijRoot);
		files.read();
		files.getUpdateSite(FilesCollection.DEFAULT_UPDATE_SITE).timestamp = 0;
		files.write();

		files = readDb();
		FileObject obsolete = files.get("jars/to-be-removed.jar");
		assertStatus(Status.OBSOLETE, obsolete);
		assertAction(Action.OBSOLETE, obsolete);

		FileUtils.deleteRecursively(ijRoot2);
	}

	@Test
	public void removeDependencies() throws Exception {
		files = initialize("jars/plugin.jar", "jars/dependency.jar");
		writeJar("jars/not-uploaded-0.11.jar");
		files = readDb();
		FileObject plugin = files.get("jars/plugin.jar");
		plugin.addDependency(files, files.get("jars/dependency.jar"));
		plugin.addDependency(files, files.get("jars/not-uploaded.jar"));
		List<Conflict> conflicts = new ArrayList<Conflict>();
		for (Conflict conflict : new Conflicts(files).getConflicts(true))
			conflicts.add(conflict);
		assertCount(1, conflicts);
		Conflict conflict = conflicts.get(0);
		assertEquals(1, conflict.getResolutions().length);
		assertTrue(conflict.getResolutions()[0].getDescription().startsWith("Break"));
		conflict.getResolutions()[0].resolve();
		assertCount(0, new Conflicts(files).getConflicts(true));
	}

	@Test
	public void byteCodeAnalyzer() throws Exception {
		files = initialize();
		writeJar("jars/dependencee.jar", Dependencee.class);
		writeJar("jars/dependency.jar", Dependency.class);
		new Checksummer(files, progress).updateFromLocal();

		FileObject dependencee = files.get("jars/dependencee.jar");
		assertCount(0, dependencee.getDependencies());
		files.updateDependencies(dependencee);
		assertCount(1, dependencee.getDependencies());
		assertEquals("jars/dependency.jar", dependencee.getDependencies().iterator().next().filename);

		writeJar("jars/bogus.jar", Dependency.class, Dependencee.class);
		files = new FilesCollection(files.prefix("")); // force a new dependency analyzer
		files.read();
		new Checksummer(files, progress).updateFromLocal();
		dependencee = files.get("jars/dependencee.jar");
		dependencee.addDependency(files, files.get("jars/dependency.jar"));
		files.updateDependencies(dependencee);
		assertCount(1, dependencee.getDependencies());
		assertEquals("jars/dependency.jar", dependencee.getDependencies().iterator().next().filename);
	}

	@Test
	public void keepObsoleteRecords() throws Exception {
		files = initialize("jars/obsolete.jar");
		assertTrue(files.prefix("jars/obsolete.jar").delete());
		files = readDb();
		files.get("jars/obsolete.jar").setAction(files, Action.REMOVE);
		upload(files);
		writeFile("jars/new.jar");
		files = readDb();
		files.get("jars/new.jar").stageForUpload(files, FilesCollection.DEFAULT_UPDATE_SITE);
		upload(files);
		final File webRoot = getWebRoot(files);
		String db = readGzippedStream(new FileInputStream(new File(webRoot, "db.xml.gz")));
		assertTrue(db.indexOf("<plugin filename=\"jars/obsolete.jar\"") > 0);
	}

	@Test
	public void keepOverriddenObsoleteRecords() throws Exception {
		files = initialize();

		// upload to secondary
		writeFile("jars/overridden.jar");
		files = readDb();
		File webRoot2 = FileUtils.createTemporaryDirectory("testUpdaterWebRoot2", "");
		files.addUpdateSite("Second", webRoot2.toURI().toURL().toString(), "file:localhost", webRoot2.getAbsolutePath() + "/", 0l);
		files.get("jars/overridden.jar").stageForUpload(files, "Second");
		upload(files, "Second");
		files.write();
		String obsoleteChecksum = files.get("jars/overridden.jar").getChecksum();

		// delete from secondary
		assertTrue(files.prefix("jars/overridden.jar").delete());
		files = readDb();
		files.get("jars/overridden.jar").setAction(files, Action.REMOVE);
		upload(files, "Second");
		files.write();

		// upload to primary
		initDb(files);
		writeJar("jars/overridden.jar", "Jola", "Theo");
		files = readDb();
		files.get("jars/overridden.jar").stageForUpload(files, FilesCollection.DEFAULT_UPDATE_SITE);
		upload(files);

		// re-add secondary
		files = readDb();
		files.addUpdateSite("Second", webRoot2.toURI().toURL().toString(), "file:localhost", webRoot2.getAbsolutePath() + "/", 0l);
		files.write();

		// upload sumpin' else to secondary
		writeJar("jars/new.jar");
		files = readDb();
		files.get("jars/new.jar").stageForUpload(files, "Second");
		upload(files, "Second");

		String db = readGzippedStream(new FileInputStream(new File(webRoot2, "db.xml.gz")));
		assertTrue(db.indexOf("<plugin filename=\"jars/overridden.jar\"") > 0);
		assertTrue(db.indexOf(obsoleteChecksum) > 0);

		FileUtils.deleteRecursively(webRoot2);
	}

	@Test
	public void testShownByDefault() throws Exception {
		files = initialize("jars/will-have-dependency.jar");
		files = readDb();
		files.write();

		final File webRoot = getWebRoot(files);
		File ijRoot2 = makeIJRoot(webRoot);

		FilesCollection files2 = new FilesCollection(ijRoot2);
		files2.read();
		files2.downloadIndexAndChecksum(progress);
		UpdaterTestUtils.update(files2, progress);

		writeFile("jars/dependency.jar");
		new Checksummer(files, progress).updateFromLocal();
		final FileObject dependency = files.get("jars/dependency.jar");
		assertNotNull(dependency);
		dependency.updateSite = FilesCollection.DEFAULT_UPDATE_SITE;
		dependency.setAction(files, Action.UPLOAD);
		assertCount(1, files.toUpload());
		upload(files);

		files2 = UpdaterTestUtils.readDb(new FilesCollection(ijRoot2), progress);
		files2.write();

		files.get("jars/will-have-dependency.jar").addDependency(files, dependency);
		upload(files);

		files2 = UpdaterTestUtils.readDb(new FilesCollection(ijRoot2), progress);
		assertAction(Action.INSTALL, files2, "jars/dependency.jar");

		assertTrue(FileUtils.deleteRecursively(ijRoot2));
	}

	/**
	 * Verifies that old update sites still work.
	 *
	 * Old update sites might have entries with checksums using the old way to calculate checksums.
	 * Mark such entries up-to-date if the obsolete checksum matches.
	 */
	@Test
	public void testHandleObsoleteChecksum() throws Exception {
		files = initialize();

		final String contents =
				"blub = true\n" + "#Tue Jun 17 09:47:43 CST 2012\n"
				+ "narf.egads = pinkie\n";
		final String fileName =
			"META-INF/maven/net.imagej/new/pom.properties";
		assertTrue(files.prefix("jars").mkdirs());
		File jar =
			writeJarWithDatedFile("jars/new.jar", 2012, 6, 17, fileName, contents);

		final String checksumOld = Util.getJarDigest(jar, false, false);
		final String checksumNew = Util.getJarDigest(jar, true, true);

		files = new FilesCollection(files.prefix(""));
		files.read();
		final FileObject file =
			new FileObject(FilesCollection.DEFAULT_UPDATE_SITE, "jars/new.jar", jar.length(), checksumOld, Util
				.getTimestamp(jar), Status.INSTALLED);
		file.addPreviousVersion(checksumNew, Util.getTimestamp(jar) - 1l, null);
		files.add(file);

		final File webRoot = getWebRoot(files);
		new XMLFileWriter(files).write(new GZIPOutputStream(new FileOutputStream(
				new File(webRoot, "db.xml.gz"))), false);

		files = readDb();
		assertStatus(Status.INSTALLED, files.get("jars/new.jar"));
	}

	/**
	 * Verify that dependencies from another update site are handled gracefully when they become obsolete.
	 * 
	 * Example: blub.jar depends on something-cool.jar on another update site. The latter is then renamed
	 * to some new name. We do not want to force blub.jar to require the (now obsolete) something-cool.jar
	 * in that case.
	 */
	@Test
	public void obsoletedDependencies() throws Exception {
		files = initialize("jars/something-cool.jar");

		// add another update site
		final File webRoot2 = FileUtils.createTemporaryDirectory("testUpdaterWebRoot2", "");
		files = readDb();
		files.addUpdateSite("Second", webRoot2.toURI().toURL().toString(), "file:localhost", webRoot2.getAbsolutePath(), -1);
		files.write();

		final UpdateSite second = files.getUpdateSite("Second");
		FilesUploader uploader = FilesUploader.initialUpload(second.url, second.sshHost, second.uploadDirectory);
		assertTrue(uploader.login());
		uploader.upload(progress);

		// upload a new .jar file to the second update site which depends on one of the first update site's .jar files
		writeFile("jars/blub.jar");
		files = readDb();
		final FileObject blub = files.get("jars/blub.jar");
		assertNotNull(blub);
		blub.addDependency(files, files.get("jars/something-cool.jar"));
		blub.updateSite = "Second";
		blub.setAction(files, Action.UPLOAD);
		upload(files, "Second");

		// remove the dependency from the first update site
		assertTrue(files.prefix("jars/something-cool.jar").delete());
		files = readDb();
		files.removeUpdateSite("Second");
		files.get("jars/something-cool.jar").setAction(files, Action.REMOVE);
		upload(files, FilesCollection.DEFAULT_UPDATE_SITE);

		final File webRoot = getWebRoot(files);
		final File ijRoot2 = makeIJRoot(webRoot);
		files = new FilesCollection(ijRoot2);
		files.read();
		files.addUpdateSite("Second", webRoot2.toURI().toURL().toString(), "file:localhost", webRoot2.getAbsolutePath(), -1);
		files.downloadIndexAndChecksum(progress);
		files.markForUpdate(false);

		assertStatus(Status.OBSOLETE_UNINSTALLED, files.get("jars/something-cool.jar"));
		FileUtils.deleteRecursively(ijRoot2);
		FileUtils.deleteRecursively(webRoot2);
	}

	@Test
	public void uploadObsolete() throws Exception {
		files = initialize("macros/macro.ijm");

		files = readDb();
		FileObject macro = files.get("macros/macro.ijm");
		assertTrue(files.prefix(macro).delete());

		files = readDb();
		macro = files.get("macros/macro.ijm");
		macro.setAction(files, Action.REMOVE);
		upload(files);

		UpdaterTestUtils.writeFile(files.prefix(macro), "changed");
		files = readDb();
		macro = files.get("macros/macro.ijm");
		assertStatus(Status.OBSOLETE_MODIFIED, macro);
		macro.setAction(files, Action.UPLOAD);
		upload(files);

		files = readDb();
		macro = files.get("macros/macro.ijm");
		assertStatus(Status.INSTALLED, macro);
	}

	//
	// Convenience methods
	//

	protected FilesCollection initialize(final String... fileNames)
			throws Exception {
		return UpdaterTestUtils.initialize(null, null, progress, fileNames);
	}

	protected FilesCollection readDb() throws ParserConfigurationException,
			SAXException {
		return UpdaterTestUtils.readDb(files, progress);
	}

	protected void upload(final FilesCollection files) throws Exception {
		upload(files, FilesCollection.DEFAULT_UPDATE_SITE);
	}

	protected void upload(final FilesCollection files, final String updateSite) throws Exception {
		UpdaterTestUtils.upload(files, updateSite, progress);
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
		return UpdaterTestUtils.writeJar(files.prefix(name), name, name);
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
		return UpdaterTestUtils.writeJar(files.prefix(name), args);
	}

	protected File writeJar(final String path, Class<?>... classes) throws FileNotFoundException, IOException {
		return UpdaterTestUtils.writeJar(files.prefix(path), classes);
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
		return UpdaterTestUtils.writeFile(files.prefix(name), name);
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
		return UpdaterTestUtils.writeFile(files.prefix(name), content);
	}

}
