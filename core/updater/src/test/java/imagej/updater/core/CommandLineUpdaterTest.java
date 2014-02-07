/*
 * #%L
 * ImageJ software for multidimensional image processing and analysis.
 * %%
 * Copyright (C) 2009 - 2014 Board of Regents of the University of
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
 * #L%
 */

package imagej.updater.core;

import static imagej.updater.core.FilesCollection.DEFAULT_UPDATE_SITE;
import static imagej.updater.core.UpdaterTestUtils.addUpdateSite;
import static imagej.updater.core.UpdaterTestUtils.assertStatus;
import static imagej.updater.core.UpdaterTestUtils.cleanup;
import static imagej.updater.core.UpdaterTestUtils.initialize;
import static imagej.updater.core.UpdaterTestUtils.main;
import static imagej.updater.core.UpdaterTestUtils.writeGZippedFile;
import static imagej.updater.core.UpdaterTestUtils.writeFile;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.scijava.util.FileUtils.deleteRecursively;
import imagej.updater.core.FileObject.Status;
import imagej.updater.util.StderrProgress;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

import org.junit.After;
import org.junit.Test;

/**
 * Tests the command-line updater.
 * 
 * @author Johannes Schindelin
 */
public class CommandLineUpdaterTest {
	protected FilesCollection files;
	protected StderrProgress progress = new StderrProgress();

	@After
	public void after() {
		if (files != null) cleanup(files);
	}

	@Test
	public void testUploadCompleteSite() throws Exception {
		final String to_remove = "macros/to_remove.ijm";
		final String modified = "macros/modified.ijm";
		final String installed = "macros/installed.ijm";
		final String new_file = "macros/new_file.ijm";
		files = initialize(to_remove, modified, installed);

		File ijRoot = files.prefix("");
		writeFile(new File(ijRoot, modified), "Zing! Zing a zong!");
		writeFile(new File(ijRoot, new_file), "Aitch!");
		assertTrue(new File(ijRoot, to_remove).delete());

		files = main(files, "upload-complete-site", FilesCollection.DEFAULT_UPDATE_SITE);

		assertStatus(Status.OBSOLETE_UNINSTALLED, files, to_remove);
		assertStatus(Status.INSTALLED, files, modified);
		assertStatus(Status.INSTALLED, files, installed);
		assertStatus(Status.INSTALLED, files, new_file);
	}

	@Test
	public void testUpload() throws Exception {
		files = initialize();

		final String path = "macros/test.ijm";
		final File file = files.prefix(path);
		writeFile(file, "// test");
		files = main(files, "upload", "--update-site", DEFAULT_UPDATE_SITE, path);

		assertStatus(Status.INSTALLED, files, path);

		assertTrue(file.delete());
		files = main(files, "upload", path);

		assertStatus(Status.OBSOLETE_UNINSTALLED, files, path);
	}

	@Test
	public void testUploadCompleteSiteWithShadow() throws Exception {
		final String path = "macros/test.ijm";
		final String obsolete = "macros/obsolete.ijm";
		files = initialize(path, obsolete);

		assertTrue(files.prefix(obsolete).delete());
		files = main(files, "upload", obsolete);

		final File tmp = addUpdateSite(files, "second");
		writeFile(files.prefix(path), "// shadowing");
		writeFile(files.prefix(obsolete), obsolete);
		files = main(files, "upload-complete-site", "--force-shadow", "second");

		assertStatus(Status.INSTALLED, files, path);
		assertStatus(Status.INSTALLED, files, obsolete);
		files = main(files, "remove-update-site", "second");

		assertStatus(Status.MODIFIED, files, path);
		assertStatus(Status.OBSOLETE, files, obsolete);

		assertTrue(deleteRecursively(tmp));
	}

	@Test
	public void testForcedShadow() throws Exception {
		final String path = "macros/test.ijm";
		files = initialize(path);

		final File tmp = addUpdateSite(files, "second");
		files = main(files, "upload", "--update-site", "second", "--force-shadow", path);

		final File onSecondSite = new File(tmp, path + "-" + files.get(path).current.timestamp);
		assertTrue("File exists: " + onSecondSite, onSecondSite.exists());
	}

	@Test
	public void testUploadCompleteSiteWithPlatforms() throws Exception {
		final String macro = "macros/macro.ijm";
		final String linux32 = "lib/linux32/libtest.so";
		files = initialize(macro, linux32);

		assertPlatforms(files.get(linux32), "linux32");

		File ijRoot = files.prefix("");
		final String win64 = "lib/win64/test.dll";
		assertTrue(new File(ijRoot, linux32).delete());
		writeFile(new File(ijRoot, win64), "Dummy");
		writeFile(new File(ijRoot, macro), "New version");
		files = main(files, "upload-complete-site", "--platforms", "win64", FilesCollection.DEFAULT_UPDATE_SITE);

		assertStatus(Status.NOT_INSTALLED, files, linux32);
		assertStatus(Status.INSTALLED, files, win64);
		assertStatus(Status.INSTALLED, files, macro);

		files = main(files, "upload-complete-site", "--platforms", "all", FilesCollection.DEFAULT_UPDATE_SITE);
		assertStatus(Status.OBSOLETE_UNINSTALLED, files, linux32);
	}

	private void assertPlatforms(final FileObject file, final String... platforms) {
		final Set<String> filePlatforms = new HashSet<String>();
		for (final String platform : file.getPlatforms()) filePlatforms.add(platform);
		assertEquals(platforms.length, filePlatforms.size());
		for (final String platform : platforms) {
			assertTrue(file.getFilename(true) + "'s platforms should contain " + platform
				+ " (" + filePlatforms + ")", filePlatforms.contains(platform));
		}
	}

	@Test
	public void testCircularDependenciesInOtherSite() throws Exception {
		files = initialize();

		final File second = addUpdateSite(files, "second");
		final File third = addUpdateSite(files, "third");

		// fake circular dependencies
		writeGZippedFile(second, "db.xml.gz", "<pluginRecords>"
				+ "<plugin filename=\"jars/a.jar\">"
				+ "<version checksum=\"1\" timestamp=\"2\" filesize=\"3\" />"
				+ "<dependency filename=\"jars/b.jar\" timestamp=\"2\" />"
				+ "</plugin>"
				+ "<plugin filename=\"jars/b.jar\">"
				+ "<version checksum=\"4\" timestamp=\"2\" filesize=\"5\" />"
				+ "<dependency filename=\"jars/a.jar\" timestamp=\"2\" />"
				+ "</plugin>"
				+ "</pluginRecords>");

		writeFile(files, "macros/a.ijm");
		files = main(files, "upload", "--update-site", "third", "macros/a.ijm");
		final File uploaded = new File(third, "macros/a.ijm-" + files.get("macros/a.ijm").current.timestamp);
		assertTrue(uploaded.exists());

		// make sure that circular dependencies are still reported when uploading to the site
		writeFile(files, "macros/b.ijm");
		try {
			files = main(files, "upload", "--update-site", "second", "macros/b.ijm");
			assertTrue("Circular dependency not reported!", false);
		} catch (RuntimeException e) {
			assertEquals("Circular dependency detected: jars/b.jar -> jars/a.jar -> jars/b.jar\n",
					e.getMessage());
		}
	}
}
