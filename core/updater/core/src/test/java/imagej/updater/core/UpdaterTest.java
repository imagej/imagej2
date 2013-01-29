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
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import imagej.log.LogService;
import imagej.updater.core.Conflicts.Conflict;
import imagej.updater.core.Conflicts.Resolution;
import imagej.updater.core.FileObject.Action;
import imagej.updater.core.FileObject.Status;
import imagej.updater.core.FilesCollection.UpdateSite;
import imagej.updater.util.Progress;
import imagej.updater.util.StderrProgress;
import imagej.updater.util.Util;
import imagej.util.FileUtils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import javax.xml.parsers.ParserConfigurationException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.xml.sax.SAXException;

/**
 * Tests various classes of the {@link imagej.updater} package and subpackages.
 * 
 * @author Johannes Schindelin
 */
public class UpdaterTest {

	final Progress progress = new StderrProgress();
	protected File ijRoot, webRoot;

	//
	// Setup
	//

	@Before
	public void setup() throws IOException {
		ijRoot = FileUtils.createTemporaryDirectory("testUpdaterIJRoot", "");
		webRoot = FileUtils.createTemporaryDirectory("testUpdaterWebRoot", "");

		System.err.println("ij: " + ijRoot + ", web: " + webRoot);
	}

	@After
	public void release() {
		FileUtils.deleteRecursively(ijRoot);
		FileUtils.deleteRecursively(webRoot);
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
		final String launcherName =
			isWindows ? "ImageJ-win32.exe" : "ImageJ-linux32";

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
		assertTrue(new File(ijRoot, ".checksums").delete());

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

		// Comma(NOT_INSTALLED), World(MODIFIED), hello(LOCAL_ONLY)
		assertCount(3, files.uploadable());
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
		FilesCollection files = new FilesCollection(ijRoot);
		files.getUpdateSite(FilesCollection.DEFAULT_UPDATE_SITE).url = webRoot.toURI().toURL().toString();
		files.getUpdateSite(FilesCollection.DEFAULT_UPDATE_SITE).timestamp =
			19991224134121l;
		files.write();

		files = readDb(true, true);

		assertCount(1, files);
		assertCount(1, files.shownByDefault());
		assertStatus(Status.NEW, files, filename);
		assertAction(Action.INSTALL, files, filename);

		// Start the update
		update(files, progress);
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
		assertEquals(20030115203432l, object.localTimestamp);
		resolutions[0].resolve();
		assertNotEqual(20030115203432l, object.localTimestamp);

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

		update(files, progress);

		assertFalse(files.prefix(obsoleted).exists());
	}

	@Test
	public void testReChecksumming() throws Exception {
		writeFile("jars/new.jar");
		FilesCollection files = new FilesCollection(ijRoot);
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
		initializeUpdateSite("jars/obsoleted-2.1.jar", "jars/without.jar",
			"jars/with-2.0.jar", "jars/too-old-3.11.jar", "plugins/plugin.jar");

		// Add the dependency relations

		FilesCollection files = readDb(true, true);
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

		files = readDb(true, true);
		assertTrue(files.prefix("jars/too-old-3.11.jar").delete());
		writeFile("jars/too-old-3.12.jar");
		new Checksummer(files, progress).updateFromLocal();
		tooOld = files.get("jars/too-old.jar");
		assertTrue(tooOld.getFilename().equals("jars/too-old-3.11.jar"));
		assertTrue(tooOld.localFilename.equals("jars/too-old-3.12.jar"));
		tooOld.stageForUpload(files, tooOld.updateSite);
		upload(files);

		// check that webRoot's db.xml.gz's previous versions contain the old filename
		final String db = readGzippedStream(new FileInputStream(new File(webRoot, "db.xml.gz")));
		Pattern regex = Pattern.compile(".*<previous-version [^>]*filename=\"jars/too-old-3.11.jar\".*", Pattern.DOTALL);
		assertTrue(regex.matcher(db).matches());

		assertTrue(new File(webRoot, "jars/too-old-3.12.jar-" + tooOld.localTimestamp).exists());

		// The dependencies should be updated automatically

		files = readDb(true, true);
		plugin = files.get("plugins/plugin.jar");
		assertTrue(plugin.dependencies.containsKey("jars/too-old.jar"));
		assertEquals("jars/too-old-3.12.jar", plugin.dependencies.get("jars/too-old.jar").filename);
	}

	@Test
	public void testMultipleVersionsSameSite() throws Exception {
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
		writeGZippedFile(webRoot, "db.xml.gz", db);
		final FilesCollection files = new FilesCollection(ijRoot);
		files.getUpdateSite(FilesCollection.DEFAULT_UPDATE_SITE).url = webRoot.toURI().toURL().toString();
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
		final String db = "<pluginRecords>"
				+ " <plugin filename=\"ImageJ-linux64\">"
				+ "  <previous-version timestamp=\"1\" checksum=\"a\" />"
				+ " </plugin>"
				+ "</pluginRecords>";
		writeGZippedFile(webRoot, "db.xml.gz", db);
		File webRoot2 = FileUtils.createTemporaryDirectory("testUpdaterWebRoot2", "");
		final String db2 = "<pluginRecords>"
				+ " <plugin filename=\"ImageJ-linux64\">"
				+ "  <version checksum=\"c\" timestamp=\"3\" filesize=\"10\" />"
				+ "  <previous-version timestamp=\"2\" checksum=\"b\" />"
				+ " </plugin>"
				+ "</pluginRecords>";
		writeGZippedFile(webRoot2, "db.xml.gz", db2);

		FilesCollection files = new FilesCollection(ijRoot);
		files.getUpdateSite(FilesCollection.DEFAULT_UPDATE_SITE).url = webRoot.toURI().toURL().toString();
		files.addUpdateSite("Fiji", webRoot2.toURI().toURL().toString(), null, null, 0);
		new XMLFileReader(files).read(FilesCollection.DEFAULT_UPDATE_SITE);
		new XMLFileReader(files).read("Fiji");

		FileObject file = files.get("ImageJ-linux64");
		assertNotNull(file);
		assertTrue(file.hasPreviousVersion("a"));
		assertTrue(file.hasPreviousVersion("b"));
		assertTrue(file.hasPreviousVersion("c"));
		assertStatus(Status.NEW, files, "ImageJ-linux64");

		files = new FilesCollection(ijRoot);
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
		initializeUpdateSite("macros/macro.ijm");

		// There should be an upload conflict if .jar file names differ only in version number

		writeFile("jars/file.jar");
		writeFile("jars/file-3.0.jar");
		final FilesCollection files = readDb(true, true);
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
		File webRoot2 = FileUtils.createTemporaryDirectory("testUpdaterWebRoot2", "");
		initializeUpdateSite(ijRoot, webRoot2, progress, "jars/hello.jar");
		assertFalse(new File(webRoot, "db.xml.gz").exists());

		// initialize main update site
		assertTrue(new File(ijRoot, "db.xml.gz").delete());
		initializeUpdateSite("macros/macro.ijm");

		FilesCollection files = readDb(true, true);
		assertStatus(Status.LOCAL_ONLY, files.get("jars/hello.jar"));

		// add second update site
		files.addUpdateSite("second", webRoot2.toURI().toURL().toString(), "file:localhost", webRoot2.getAbsolutePath() + "/", 0l);

		// re-read files from update site
		files.reReadUpdateSite("second", progress);
		assertStatus(Status.INSTALLED, files.get("jars/hello.jar"));
		files.write();

		// modify locally and re-read from update site
		assertTrue(new File(ijRoot, ".checksums").delete());
		assertTrue(new File(ijRoot, "jars/hello.jar").delete());
		writeJar("jars/hello-2.0.jar", "new-file", "empty");
		new Checksummer(files, progress).updateFromLocal();

		files.reReadUpdateSite("second", progress);
		assertStatus(Status.MODIFIED, files.get("jars/hello.jar"));

		FileUtils.deleteRecursively(webRoot2);
	}

	@Test
	public void testUpdateable() throws Exception {
		initializeUpdateSite("jars/hello.jar");
		FilesCollection files = readDb(true, true);
		assertStatus(Status.INSTALLED, files.get("jars/hello.jar"));
		String origChecksum = files.get("jars/hello.jar").getChecksum();
		assertEquals(origChecksum, Util.getJarDigest(new File(ijRoot, "jars/hello.jar")));

		assertTrue(new File(ijRoot, ".checksums").delete());
		assertTrue(new File(ijRoot, "jars/hello.jar").delete());
		writeJar("jars/hello-2.0.jar", "new-file", "empty");
		new Checksummer(files, progress).updateFromLocal();
		String newChecksum = files.get("jars/hello.jar").localChecksum;
		assertEquals(newChecksum, Util.getJarDigest(new File(ijRoot, "jars/hello-2.0.jar")));

		assertNotEqual(origChecksum, newChecksum);

		// upload that version
		files.get("jars/hello.jar").stageForUpload(files, FilesCollection.DEFAULT_UPDATE_SITE);
		assertEquals(newChecksum, files.get("jars/hello.jar").localChecksum);
		upload(files);

		FilesCollection files2 = new FilesCollection(new File(ijRoot, "invalid"));
		files2.getUpdateSite(FilesCollection.DEFAULT_UPDATE_SITE).url = webRoot.toURI().toURL().toString();
		XMLFileDownloader xmlLoader = new XMLFileDownloader(files2);
		xmlLoader.start();
		String newChecksum2 = files2.get("jars/hello.jar").current.checksum;
		assertEquals(newChecksum, newChecksum2);

		// re-write the original version
		assertTrue(new File(ijRoot, ".checksums").delete());
		writeFile("jars/hello.jar");
		assertTrue(new File(ijRoot, "jars/hello-2.0.jar").delete());
		files = readDb(true, true);
		String origChecksum2 = files.get("jars/hello.jar").localChecksum;
		assertEquals(origChecksum, origChecksum2);
		assertEquals(origChecksum, Util.getJarDigest(new File(ijRoot, "jars/hello.jar")));

		assertTrue(new File(ijRoot, "db.xml.gz").delete());
		files = readDb(false, true);
		assertEquals(newChecksum, files.get("jars/hello.jar").current.checksum);

		assertStatus(Status.UPDATEABLE, files.get("jars/hello.jar"));
		files.get("jars/hello.jar").setAction(files, Action.UPDATE);
		Installer installer = new Installer(files, progress);
		installer.start();
		assertEquals(newChecksum, Util.getJarDigest(new File(ijRoot, "update/jars/hello-2.0.jar")));
		installer.moveUpdatedIntoPlace();

		assertStatus(Status.INSTALLED, files.get("jars/hello.jar"));
		assertEquals(newChecksum, Util.getJarDigest(new File(ijRoot, "jars/hello-2.0.jar")));
	}

	@Test
	public void testReReadFiles() throws Exception {
		initializeUpdateSite("macros/macro.ijm");
		FilesCollection files = readDb(true, true);
		files.get("macros/macro.ijm").description = "Narf";
		files.write();
		upload(files);

		files = readDb(true, true);
		assertEquals("Narf", files.get("macros/macro.ijm").description);
		new Checksummer(files, progress).updateFromLocal();
		assertEquals("Narf", files.get("macros/macro.ijm").description);
	}

	@Test
	public void testUpdateTheUpdater() throws Exception {
		final String name1 = "jars/ij-updater-core-1.46n.jar";
		final String name2 = "jars/ij-updater-core-2.0.0.jar";

		// initialize main update site
		initializeUpdateSite(name1);

		// "change" updater
		assertTrue(new File(ijRoot, name1).delete());
		writeJar(name2, "files.txt", "modified");
		FilesCollection files = readDb(true, true);
		assertTrue(files.get(name1) == files.get(name2));
		assertStatus(Status.MODIFIED, files.get(name1));
		final String modifiedChecksum = files.get(name2).localChecksum;
		files.get(name1).stageForUpload(files, FilesCollection.DEFAULT_UPDATE_SITE);
		upload(files);

		// revert back to "old" updater
		writeJar(name1);
		assertTrue(new File(ijRoot, name2).delete());

		// now the updater should be updated first thing
		files = readDb(true, true);
		FileObject file2 = files.get(name2);
		assertNotEqual(file2.localFilename, name2);
		assertTrue(file2.isUpdateable());
		assertNotEqual(modifiedChecksum, files.get(name2).localChecksum);
		assertTrue(Installer.isTheUpdaterUpdateable(files));
		Installer.updateTheUpdater(files, progress);
		assertTrue(new File(ijRoot, "update/" + name1).exists());
		assertEquals(0l, new File(ijRoot, "update/" + name1).length());
		assertTrue(new File(ijRoot, "update/" + name2).exists());

		assertTrue(new File(ijRoot, name1).delete());
		assertFalse(new File(ijRoot, name2).exists());
		assertTrue(new File(ijRoot, "update/" + name2).renameTo(new File(ijRoot, name2)));
		new Checksummer(files, progress).updateFromLocal();
		assertEquals(modifiedChecksum, files.get(name2).current.checksum);
	}

	@Test
	public void testFillMetadataFromPOM() throws Exception {
		writeJar("jars/hello.jar", "META-INF/maven/egads/hello/pom.xml", "<project>"
				+ " <description>Take over the world!</description>"
				+ " <developers>"
				+ "  <developer><name>Jenna Jenkins</name></developer>"
				+ "  <developer><name>Bugs Bunny</name></developer>"
				+ " </developers>"
				+ "</project>");
		final FilesCollection files = new FilesCollection(ijRoot);
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
		final String propertiesContents) throws IOException
	{
		final File file = new File(ijRoot, jarFileName);
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
		final String newContents =
			"blub = true\n" + "#Tue Jun 17 09:47:43 CST 2012\n"
				+ "narf.egads = pinkie\n";
		final String fileName =
			"META-INF/maven/net.imagej/updater-test/pom.properties";
		assertTrue(new File(ijRoot, "jars").mkdirs());
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
			final FilesCollection files = new FilesCollection(ijRoot);
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

		FilesCollection files = new FilesCollection(ijRoot);
		final FileObject file =
			new FileObject(FilesCollection.DEFAULT_UPDATE_SITE, "jars/new.jar", jar.length(), checksumOld, Util
				.getTimestamp(jar), Status.INSTALLED);
		files.add(file);

		new File(webRoot, "jars").mkdirs();
		assertTrue(jar.renameTo(new File(webRoot, "jars/new.jar-" +
			file.current.timestamp)));
		new XMLFileWriter(files).write(new GZIPOutputStream(new FileOutputStream(
			new File(webRoot, "db.xml.gz"))), false);

		files = readDb(false, true);
		new Installer(files, progress).start();
		jar = new File(ijRoot, "update/jars/new.jar");
		assertTrue(jar.exists());
		assertEquals(checksumNew, Util.getJarDigest(jar));
		assertEquals(checksumOld, files.get("jars/new.jar").getChecksum());
	}

	@Test
	public void testUpdateToDifferentVersion() throws Exception {
		initializeUpdateSite("jars/egads-1.0.jar");
		FilesCollection files = readDb(true, true);

		// upload a newer version
		assertTrue(files.prefix("jars/egads-1.0.jar").delete());
		writeJar("jars/egads-2.1.jar");
		files = readDb(true, true);
		files.get("jars/egads.jar").stageForUpload(files, FilesCollection.DEFAULT_UPDATE_SITE);
		upload(files);

		assertTrue(files.prefix("jars/egads-2.1.jar").exists());
		assertFalse(files.prefix("jars/egads-1.0.jar").exists());

		// downgrade locally
		assertTrue(files.prefix("jars/egads-2.1.jar").delete());
		writeJar("jars/egads-1.0.jar");
		files = readDb(true, true);

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
		files = readDb(true, true);
		files.get("jars/egads.jar").setAction(files, Action.REMOVE);
		upload(files);

		// re-instate an old version with a different name
		writeJar("jars/egads-1.0.jar");
		files = readDb(true, true);
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
		initializeUpdateSite();
		writeJar(ijRoot, "jars/egads-0.1.jar", "hello", "world");
		FilesCollection files = readDb(false,  true);
		files.get("jars/egads.jar").stageForUpload(files, FilesCollection.DEFAULT_UPDATE_SITE);
		upload(files);

		assertTrue(new File(ijRoot, "jars/egads-0.1.jar").delete());
		writeJar(ijRoot, "jars/egads-0.2.jar", "hello", "world2");
		new Checksummer(files, progress).updateFromLocal();
		files.get("jars/egads.jar").stageForUpload(files, FilesCollection.DEFAULT_UPDATE_SITE);
		upload(files);

		writeJar(ijRoot, "jars/egads-0.1.jar", "hello", "world");
		writeJar(ijRoot, "jars/egads.jar", "hello", "world");
		touch(new File(ijRoot, "jars/egads-0.2.jar"), 19800101000001l);
		files = readDb(true, true);
		List<Conflict> conflicts = files.getConflicts();
		assertEquals(1, conflicts.size());
		Conflict conflict = conflicts.get(0);
		assertEquals(conflict.filename, "jars/egads-0.2.jar");
		conflict.resolutions[1].resolve();
		assertFalse(new File(ijRoot, "jars/egads.jar").exists());
		assertFalse(new File(ijRoot, "jars/egads-0.1.jar").exists());
		assertTrue(new File(ijRoot, "jars/egads-0.2.jar").exists());
	}

	@Test
	public void uninstallRemoved() throws Exception {
		initializeUpdateSite("jars/to-be-removed.jar");
		FilesCollection files = readDb(true,  true);
		files.write();

		File ijRoot2 = makeIJRoot(webRoot);

		files = new FilesCollection(ijRoot2);
		files.downloadIndexAndChecksum(progress);
		files.get("jars/to-be-removed.jar").setAction(files, Action.REMOVE);
		upload(files);

		// make sure that the timestamp of the update site is "new"
		files = new FilesCollection(ijRoot);
		files.read();
		files.getUpdateSite(FilesCollection.DEFAULT_UPDATE_SITE).timestamp = 0;
		files.write();

		files = readDb(true, true);
		FileObject obsolete = files.get("jars/to-be-removed.jar");
		assertStatus(Status.OBSOLETE, obsolete);
		assertAction(Action.OBSOLETE, obsolete);

		FileUtils.deleteRecursively(ijRoot2);
	}

	@Test
	public void removeDependencies() throws Exception {
		initializeUpdateSite("jars/plugin.jar", "jars/dependency.jar");
		writeJar("jars/not-uploaded-0.11.jar");
		FilesCollection files = readDb(true, true);
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
		writeJar("jars/dependencee.jar", imagej.updater.test.Dependencee.class);
		writeJar("jars/dependency.jar", imagej.updater.test.Dependency.class);
		FilesCollection files = new FilesCollection(ijRoot);
		new Checksummer(files, progress).updateFromLocal();

		FileObject dependencee = files.get("jars/dependencee.jar");
		assertCount(0, dependencee.getDependencies());
		files.updateDependencies(dependencee);
		assertCount(1, dependencee.getDependencies());
		assertEquals("jars/dependency.jar", dependencee.getDependencies().iterator().next().filename);

		writeJar("jars/bogus.jar", imagej.updater.test.Dependency.class, imagej.updater.test.Dependencee.class);
		files = new FilesCollection(ijRoot); // force a new dependency analyzer
		new Checksummer(files, progress).updateFromLocal();
		dependencee = files.get("jars/dependencee.jar");
		dependencee.addDependency(files, files.get("jars/dependency.jar"));
		files.updateDependencies(dependencee);
		assertCount(1, dependencee.getDependencies());
		assertEquals("jars/dependency.jar", dependencee.getDependencies().iterator().next().filename);
	}

	@Test
	public void keepObsoleteRecords() throws Exception {
		initializeUpdateSite("jars/obsolete.jar");
		assertTrue(new File(ijRoot, "jars/obsolete.jar").delete());
		FilesCollection files = readDb(true, true);
		files.get("jars/obsolete.jar").setAction(files, Action.REMOVE);
		upload(files);
		writeFile("jars/new.jar");
		files = readDb(true, true);
		files.get("jars/new.jar").stageForUpload(files, FilesCollection.DEFAULT_UPDATE_SITE);
		upload(files);
		String db = readGzippedStream(new FileInputStream(new File(webRoot, "db.xml.gz")));
		assertTrue(db.indexOf("<plugin filename=\"jars/obsolete.jar\"") > 0);
	}

	@Test
	public void keepOverriddenObsoleteRecords() throws Exception {
		initializeUpdateSite();

		// upload to secondary
		writeFile("jars/overridden.jar");
		FilesCollection files = readDb(false, true);
		File webRoot2 = FileUtils.createTemporaryDirectory("testUpdaterWebRoot2", "");
		files.addUpdateSite("Second", webRoot2.toURI().toURL().toString(), "file:localhost", webRoot2.getAbsolutePath() + "/", 0l);
		files.get("jars/overridden.jar").stageForUpload(files, "Second");
		upload(files, "Second");
		files.write();
		String obsoleteChecksum = files.get("jars/overridden.jar").getChecksum();

		// delete from secondary
		assertTrue(new File(ijRoot, "jars/overridden.jar").delete());
		files = readDb(true, true);
		files.get("jars/overridden.jar").setAction(files, Action.REMOVE);
		upload(files, "Second");
		files.write();

		// upload to primary
		assertTrue(new File(ijRoot, "db.xml.gz").delete());
		writeJar("jars/overridden.jar", "Jola", "Theo");
		files = readDb(false, true);
		files.get("jars/overridden.jar").stageForUpload(files, FilesCollection.DEFAULT_UPDATE_SITE);
		upload(files);

		// re-add secondary
		files = readDb(true, true);
		files.addUpdateSite("Second", webRoot2.toURI().toURL().toString(), "file:localhost", webRoot2.getAbsolutePath() + "/", 0l);
		files.write();

		// upload sumpin' else to secondary
		writeJar("jars/new.jar");
		files = readDb(true, true);
		files.get("jars/new.jar").stageForUpload(files, "Second");
		upload(files, "Second");

		String db = readGzippedStream(new FileInputStream(new File(webRoot2, "db.xml.gz")));
		assertTrue(db.indexOf("<plugin filename=\"jars/overridden.jar\"") > 0);
		assertTrue(db.indexOf(obsoleteChecksum) > 0);

		FileUtils.deleteRecursively(webRoot2);
	}

	@Test
	public void testShownByDefault() throws Exception {
		initializeUpdateSite("jars/will-have-dependency.jar");
		FilesCollection files = readDb(true, true);
		files.write();

		File ijRoot2 = makeIJRoot(webRoot);

		FilesCollection files2 = new FilesCollection(ijRoot2);
		files2.downloadIndexAndChecksum(progress);
		update(files2, progress);

		writeFile("jars/dependency.jar");
		new Checksummer(files, progress).updateFromLocal();
		final FileObject dependency = files.get("jars/dependency.jar");
		assertNotNull(dependency);
		dependency.updateSite = FilesCollection.DEFAULT_UPDATE_SITE;
		dependency.setAction(files, Action.UPLOAD);
		assertCount(1, files.toUpload());
		upload(files);

		files2 = readDb(true, true, ijRoot2, webRoot, progress);
		files2.write();

		files.get("jars/will-have-dependency.jar").addDependency(files, dependency);
		upload(files);

		files2 = readDb(true, true, ijRoot2, webRoot, progress);
		assertAction(Action.INSTALL, files2, "jars/dependency.jar");

		FileUtils.deleteRecursively(ijRoot2);
	}

	/**
	 * Verifies that old update sites still work.
	 *
	 * Old update sites might have entries with checksums using the old way to calculate checksums.
	 * Mark such entries up-to-date if the obsolete checksum matches.
	 */
	@Test
	public void testHandleObsoleteChecksum() throws Exception {
		initializeUpdateSite();

		final String contents =
				"blub = true\n" + "#Tue Jun 17 09:47:43 CST 2012\n"
				+ "narf.egads = pinkie\n";
		final String fileName =
			"META-INF/maven/net.imagej/new/pom.properties";
		assertTrue(new File(ijRoot, "jars").mkdirs());
		File jar =
			writeJarWithDatedFile("jars/new.jar", 2012, 6, 17, fileName, contents);

		final String checksumOld = Util.getJarDigest(jar, false, false);
		final String checksumNew = Util.getJarDigest(jar, true, true);

		FilesCollection files = new FilesCollection(ijRoot);
		final FileObject file =
			new FileObject(FilesCollection.DEFAULT_UPDATE_SITE, "jars/new.jar", jar.length(), checksumOld, Util
				.getTimestamp(jar), Status.INSTALLED);
		file.addPreviousVersion(checksumNew, Util.getTimestamp(jar) - 1l, null);
		files.add(file);

		new XMLFileWriter(files).write(new GZIPOutputStream(new FileOutputStream(
				new File(webRoot, "db.xml.gz"))), false);

		files = readDb(true, true);
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
		initializeUpdateSite("jars/something-cool.jar");

		// add another update site
		final File webRoot2 = FileUtils.createTemporaryDirectory("testUpdaterWebRoot2", "");
		FilesCollection files = readDb(true, true);
		files.addUpdateSite("Second", webRoot2.toURI().toURL().toString(), "file:localhost", webRoot2.getAbsolutePath(), -1);
		files.write();

		final UpdateSite second = files.getUpdateSite("Second");
		FilesUploader uploader = FilesUploader.initialUpload(second.url, second.sshHost, second.uploadDirectory);
		assertTrue(uploader.login());
		uploader.upload(progress);

		// upload a new .jar file to the second update site which depends on one of the first update site's .jar files
		writeFile("jars/blub.jar");
		files = readDb(true, true);
		final FileObject blub = files.get("jars/blub.jar");
		assertNotNull(blub);
		blub.addDependency(files, files.get("jars/something-cool.jar"));
		blub.updateSite = "Second";
		blub.setAction(files, Action.UPLOAD);
		upload(files, "Second");

		// remove the dependency from the first update site
		assertTrue(new File(ijRoot, "jars/something-cool.jar").delete());
		files = readDb(true, true);
		files.removeUpdateSite("Second");
		files.get("jars/something-cool.jar").setAction(files, Action.REMOVE);
		upload(files, FilesCollection.DEFAULT_UPDATE_SITE);

		final File ijRoot2 = FileUtils.createTemporaryDirectory("testUpdaterIJRoot2", "");
		files = new FilesCollection(ijRoot2);
		files.getUpdateSite(FilesCollection.DEFAULT_UPDATE_SITE).url = webRoot.toURI().toURL().toString();
		files.addUpdateSite("Second", webRoot2.toURI().toURL().toString(), "file:localhost", webRoot2.getAbsolutePath(), -1);
		files.downloadIndexAndChecksum(progress);
		files.markForUpdate(false);

		assertStatus(Status.OBSOLETE_UNINSTALLED, files.get("jars/something-cool.jar"));
		FileUtils.deleteRecursively(ijRoot2);
		FileUtils.deleteRecursively(webRoot2);
	}

	@Test
	public void uploadObsolete() throws Exception {
		initializeUpdateSite("macros/macro.ijm");

		FilesCollection files = readDb(true, true);
		FileObject macro = files.get("macros/macro.ijm");
		assertTrue(files.prefix(macro).delete());

		files = readDb(true, true);
		macro = files.get("macros/macro.ijm");
		macro.setAction(files, Action.REMOVE);
		upload(files);

		writeFile(files.prefix(macro), "changed");
		files = readDb(true, true);
		macro = files.get("macros/macro.ijm");
		assertStatus(Status.OBSOLETE_MODIFIED, macro);
		macro.setAction(files, Action.UPLOAD);
		upload(files);

		files = readDb(true, true);
		macro = files.get("macros/macro.ijm");
		assertStatus(Status.INSTALLED, macro);
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

	protected void initializeUpdateSite(final String... fileNames)
			throws Exception {
		initializeUpdateSite(ijRoot, webRoot, progress, fileNames);
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

	protected FilesCollection readDb(final boolean readLocalDb,
		final boolean runChecksummer) throws IOException,
		ParserConfigurationException, SAXException
	{
		return readDb(readLocalDb, runChecksummer, ijRoot, webRoot, progress);
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

	protected void upload(final FilesCollection files) throws Exception {
		upload(files, FilesCollection.DEFAULT_UPDATE_SITE);
	}

	protected void upload(final FilesCollection files, final String updateSite) throws Exception {
		for (final FileObject file : files.toUpload())
			assertEquals(updateSite, file.updateSite);
		final FilesUploader uploader =
			new FilesUploader(files, updateSite);
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
	protected static File writeJar(final File dir, final String name,
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

	protected File writeJar(final String path, Class<?>... classes) throws FileNotFoundException, IOException {
		return writeJar(new File(ijRoot, path), classes);
	}

	protected File writeJar(final File file, Class<?>... classes) throws FileNotFoundException, IOException {
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
		return writeFile(new File(ijRoot, name), name);
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
	protected static File writeFile(final File dir, final String name,
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
	protected static File writeFile(final File file, final String content)
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
	protected String readGzippedStream(final InputStream in) throws IOException {
		return readStream(new GZIPInputStream(in));
	}

	/**
	 * Read a stream and return what we got as a String
	 * 
	 * @param in the input stream
	 * @return the contents, as a String
	 * @throws IOException
	 */
	protected String readStream(final InputStream in) throws IOException {
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
