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

package imagej.updater.webdav;

import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeTrue;
import imagej.updater.core.FilesCollection;
import imagej.updater.core.UpdaterTestUtils;
import imagej.updater.ui.CommandLine;
import imagej.updater.util.StderrProgress;
import imagej.updater.util.Util;

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

import org.junit.After;
import org.junit.Test;

/**
 * A conditional JUnit test for uploading via WebDAV.
 * 
 * This test is only activated iff the following system properties are set:
 * <dl>
 * <dt>webdav.test.url</dt><dd>The URL of the update site</dd>
 * <dt>webdav.test.username</dt><dd>The name of the WebDAV account with write permission to the URL</dd>
 * <dt>webdav.test.password</dt><dd>The password of the WebDAV account with write permission to the URL</dd>
 * </dl>
 * 
 * Any files in the given directory will be deleted before running the test!
 * 
 * @author Johannes Schindelin
 */
public class WebDAVUpdaterITCase {
	private FilesCollection files;

	@After
	public void cleanup() {
		if (files != null) UpdaterTestUtils.cleanup(files);
	}

	@Test
	public void testWebDAVUpload() throws Exception {
		String url = System.getProperty("webdav.test.url");
		String username = System.getProperty("webdav.test.username");
		String password = System.getProperty("webdav.test.password");
		assumeTrue(url != null && username != null && password != null);

		if (url == null) throw new RuntimeException("Eclipse, it cannot be null here, so shut up!");
		if (!url.endsWith("/")) url += "/";

		FilesCollection files = UpdaterTestUtils.initialize();
		File tmpDir = files.prefix("");
		CommandLine.main(tmpDir, Integer.MAX_VALUE, "add-update-site", "webdav-test", url, "webdav:" + username + ":" + password, "");

		HttpURLConnection connection = (HttpURLConnection) new URL(url + Util.XML_COMPRESSED).openConnection();
		if (404 != connection.getResponseCode()) {
			final Deleter deleter = new Deleter(username, password);
			deleter.delete(new URL(url + Util.XML_COMPRESSED));
			deleter.delete(new URL(url + "plugins/"));
		}

		final File hello = new File(tmpDir, "plugins/Say_Hello.bsh");
		UpdaterTestUtils.writeFile(hello, "print(\"Hello, world!\");");
		CommandLine.main(tmpDir, Integer.MAX_VALUE, "upload", "--update-site", "webdav-test", "plugins/Say_Hello.bsh");

		files.read();
		files.clear();
		files.downloadIndexAndChecksum(new StderrProgress());
		assertTrue("" + files.get("plugins/Say_Hello.bsh").current.timestamp + " >= " + Util.getTimestamp(tmpDir), files.get("plugins/Say_Hello.bsh").current.timestamp >= Util.getTimestamp(tmpDir));

		assertTrue(hello.delete());
		CommandLine.main(tmpDir, Integer.MAX_VALUE, "update", "plugins/Say_Hello.bsh");
		assertTrue(hello.exists());
	}

	private static class Deleter extends WebDAVUploader {
		public Deleter(final String username, final String password) {
			setCredentials(username, password);
		}

		public void delete(final URL url) throws IOException {
			final boolean isDirectory = url.getPath().endsWith("/");
			final HttpURLConnection connection = isDirectory ?
					connect("DELETE", url, null) :
					connect("DELETE", url, null, "Depth", "Infinity");
			int code = connection.getResponseCode();
			if (code > 299) {
				throw new IOException("Could not delete " + url + ": " + code + " " + connection.getResponseMessage());
			}
		}
	}
}
