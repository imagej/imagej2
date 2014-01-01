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
 * 
 * The views and conclusions contained in the software and documentation are
 * those of the authors and should not be interpreted as representing official
 * policies, either expressed or implied, of any organization.
 * #L%
 */

package imagej.updater.ssh;

import imagej.updater.core.AbstractUploaderTestBase;

import org.junit.Test;

/**
 * A conditional JUnit test for uploading via SSH/SFTP.
 * 
 * This test is only activated iff the following system properties are set:
 * <dl>
 * <dt>ssh.test.url</dt>
 * <dd>The URL of the update site</dd>
 * <dt>ssh.test.host</dt>
 * <dd>The SSH host of the update site</dd>
 * <dt>ssh.test.upload-directory</dt>
 * <dd>The upload directory on the SSH host corresponding to the URL of the
 * update site</dd>
 * </dl>
 * 
 * The given SSH host <b>must</b> have a section in $HOME/.ssh/config defining
 * user name and private key.
 * 
 * Any files in the given directory will be deleted before running the test!
 * 
 * @author Johannes Schindelin
 */
public class SSHUploaderITCase extends AbstractUploaderTestBase {
	public SSHUploaderITCase() {
		super("ssh");
	}

	@Test
	public void testSSHUpload() throws Exception {
		final String host = getProperty("host");
		final String uploadDirectory = getDirectoryProperty("upload-directory");

		test(new SSHDeleter(host, uploadDirectory), "ssh:" + host, uploadDirectory);
	}

	@Test
	public void testSFTPUpload() throws Exception {
		final String host = getProperty("host");
		final String uploadDirectory = getDirectoryProperty("upload-directory");
		test(new SSHDeleter(host, uploadDirectory), "sftp:" + host, uploadDirectory);
	}
}
