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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeNotNull;
import imagej.updater.ui.CommandLine;
import imagej.updater.util.StderrProgress;
import imagej.updater.util.Util;

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import org.junit.After;

/**
 * An abstract base class for testing uploader backends.
 * 
 * @author Johannes Schindelin
 */
public abstract class AbstractUploaderTest {
	protected final String propertyPrefix, updateSiteName;
	protected String url;
	protected FilesCollection files;

	public AbstractUploaderTest(final String propertyPrefix) {
		this.propertyPrefix = propertyPrefix;
		updateSiteName = propertyPrefix + "-test";
	}

	@After
	public void cleanup() {
		if (files != null) UpdaterTestUtils.cleanup(files);
	}

	public void test(final Deleter deleter, final String host, final String uploadDirectory) throws Exception {
		getURL();
		files = UpdaterTestUtils.initialize();

		File ijRoot = files.prefix("");
		CommandLine.main(ijRoot, Integer.MAX_VALUE, "add-update-site",
				updateSiteName, url, host, uploadDirectory);

		if (!isUpdateSiteEmpty()) {
			deleter.delete(Util.XML_COMPRESSED);
			deleter.delete("plugins/");
		}

		final String path = "plugins/Say_Hello.bsh";
		final String contents = "print(\"Hello, world!\");";
		final File file = new File(ijRoot, path);
		UpdaterTestUtils.writeFile(file, contents);
		CommandLine.main(ijRoot, Integer.MAX_VALUE, "upload", "--update-site", updateSiteName, path);

		assertFalse(isUpdateSiteEmpty());

		files.read();
		files.clear();
		files.downloadIndexAndChecksum(new StderrProgress());
		final long timestamp = files.get(path).current.timestamp;
		final long minimalTimestamp = 20130322000000l;
		assertTrue("" + timestamp + " >= " + minimalTimestamp,
				timestamp >= minimalTimestamp);

		assertTrue(file.delete());
		CommandLine.main(ijRoot, Integer.MAX_VALUE, "update", path);
		assertTrue(file.exists());
	}

	public String getURL() {
		return url = getDirectoryProperty("url");
	}

	public String getDirectoryProperty(final String key) {
		final String directory = getProperty(key);
		return directory.endsWith("/") ? directory : directory + "/";
	}

	public String getProperty(final String key) {
		final String result = System.getProperty(propertyPrefix + ".test." + key);
		assumeNotNull(result);
		return result;
	}

	public boolean isUpdateSiteEmpty() throws MalformedURLException, IOException {
		HttpURLConnection connection = (HttpURLConnection) new URL(url + Util.XML_COMPRESSED).openConnection();
		return 404 == connection.getResponseCode();
	}

	public interface Deleter {
		public abstract void delete(final String path) throws IOException;
	}
}
