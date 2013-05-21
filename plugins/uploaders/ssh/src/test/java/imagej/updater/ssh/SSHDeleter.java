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

package imagej.updater.ssh;

import static org.junit.Assert.assertTrue;
import imagej.updater.core.AbstractUploaderTestBase.Deleter;

import java.io.IOException;

/**
 * A helper class to delete files from an SSH update site before running the integration test.
 * 
 * @author Johannes Schindelin
 */
public class SSHDeleter extends SSHFileUploader implements Deleter {
	private final String host, uploadDirectory;

	public SSHDeleter(final String host, final String uploadDirectory) {
		this.host = host;
		this.uploadDirectory = uploadDirectory + (uploadDirectory.endsWith("/") ? "" : "/");
	}

	@Override
	public boolean login() {
		System.err.println("logging into " + host);
		return debugLogin(host);
	}

	@Override
	public void delete(final String file) throws IOException {
		final boolean isDirectory = file.endsWith("/");
		String command = "rm " + (isDirectory ? "-r " : "") + "'" + uploadDirectory + file + "'";
		System.err.println("Executing: " + command);
		setCommand(command);
		System.err.println("logging out");
	}
}