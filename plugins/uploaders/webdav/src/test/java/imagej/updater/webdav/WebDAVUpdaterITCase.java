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

package imagej.updater.webdav;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

import net.imagej.updater.AbstractUploaderTestBase;

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
public class WebDAVUpdaterITCase extends AbstractUploaderTestBase {
	public WebDAVUpdaterITCase() {
		super("webdav");
	}

	@Test
	public void testWebDAVUpload() throws Exception {
		final String username = getProperty("username");
		final String password = getProperty("password");

		test(new WebDAVDeleter(username, password), "webdav:" + username + ":" + password, "");
	}

	private class WebDAVDeleter extends WebDAVUploader implements AbstractUploaderTestBase.Deleter {
		public WebDAVDeleter(final String username, final String password) {
			setCredentials(username, password);
		}

		@Override
		public boolean login() {
			return true; // we did that already in the constructor
		}

		@Override
		public void logout() { }

		@Override
		public void delete(final String path) throws IOException {
			final URL target = new URL(url + path);
			final boolean isDirectory = path.endsWith("/");
			final HttpURLConnection connection = isDirectory ?
					connect("DELETE", target, null) :
					connect("DELETE", target, null, "Depth", "Infinity");
			int code = connection.getResponseCode();
			if (code > 299) {
				throw new IOException("Could not delete " + url + ": " + code + " " + connection.getResponseMessage());
			}
		}
	}
}
