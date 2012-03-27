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

package imagej.updater.ssh;

import imagej.updater.util.UpdaterUserInterface;

import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpATTRS;
import com.jcraft.jsch.SftpException;
import com.jcraft.jsch.SftpProgressMonitor;

/**
 * Wraps low level SFTP operations and isolates from implementation API.
 * 
 * @author Jarek Sacha
 * @author Johannes Schindelin
 */
final class SFTPOperations {

	final private Session session;
	final private ChannelSftp sftp;

	/**
	 * Create new instance of operations and initialize connection to remote site.
	 * 
	 * @param Session JSch session.
	 * @throws JSchException if the connection fails.
	 */
	public SFTPOperations(final Session session) throws JSchException {

		this.session = session;

		sftp = (ChannelSftp) session.openChannel("sftp");
		sftp.connect();
	}

	/**
	 * Disconnect session.
	 * 
	 * @throws IOException last SFTP command failed.
	 */
	public void disconnect() throws IOException {
		final int exitStatus = sftp.getExitStatus();
		sftp.disconnect();
		session.disconnect();
		if (exitStatus != -1) {
			throw new IOException("Command failed (see Log)!");
		}
	}

	/**
	 * Transfer data to remote server.
	 * 
	 * @param in input stream used fr transfer.
	 * @param dest name of the destination file to which the data will be saved
	 *          to.
	 * @throws IOException if transfer fails.
	 */
	public void put(final InputStream in, final String dest) throws IOException {
		put(in, dest, null);
	}

	/**
	 * Transfer data to remote server.
	 * 
	 * @param in input stream used fr transfer.
	 * @param dest name of the destination file to which the data will be saved
	 *          to.
	 * @param listener upload progress listener.
	 * @throws IOException if transfer fails.
	 */
	public void put(final InputStream in, final String dest,
		final ProgressListener listener) throws IOException
	{
		UpdaterUserInterface.get().debug("SFTPOperations.put(...,...," + dest + ")");
		mkParentDirs(dest);

		final ProgressMonitor monitor = new ProgressMonitor(listener);
		try {
			sftp.put(in, dest, monitor);
		}
		catch (final SftpException ex) {
			throw wrapException("Failed to upload file '" + dest + "'.", ex);
		}
	}

	/**
	 * Rename remote file.
	 * 
	 * @param src file to rename.
	 * @param dest new name.
	 * @throws IOException when operation fails.
	 */
	public void rename(final String src, final String dest) throws IOException {

		rm(dest);
		mkParentDirs(dest);

		// Rename to final
		try {
			sftp.rename(src, dest);
		}
		catch (final SftpException ex) {
			throw wrapException("Failed to rename remote file '" + src + "' to '" +
				dest + "'.", ex);
		}

	}

	/**
	 * Remove remote file or directory.
	 * 
	 * @param path path to be deleted..
	 * @throws IOException when operation fails.
	 */
	public void rm(final String path) throws IOException {

		if (fileExists(path)) {
			try {
				sftp.rm(path);
			}
			catch (final SftpException ex) {
				throw wrapException("Failed to remove remote file '" + path + "'.", ex);
			}
		}
	}

	/**
	 * Test if specified path exists on the remote server.
	 * 
	 * @param path path to test
	 * @return {@code true} if the path exists
	 */
	public boolean fileExists(final String path) {
		UpdaterUserInterface.get().debug("SFTPOperations.fileExists2(" + path + ")");

		// Traversing the path may hit directories without read access.
		// Rather than listing content to see if directory exists just test the path
		// directly (using nasty exception).
		try {
			sftp.stat(path);
			return true;
		}
		catch (final SftpException e) {
			return false;
		}
	}

	/**
	 * Creates the directory named by this path, including any necessary but
	 * nonexistent parent directories.
	 * 
	 * @param path path for which to create parent directories
	 * @throws IOException in case of sftp error.
	 */
	public void mkParentDirs(final String path) throws IOException {
		UpdaterUserInterface.get().debug("SFTPOperations.mkParentDirs(" + path + ")");
		mkParentDirs("", path);
	}

	/**
	 * Retrieve a timestamp of a remote file.
	 * 
	 * @param file requested file.
	 * @return timestamp in a format "yyyyMMddHHmmss", for instance,
	 *         20110729132712
	 * @throws IOException in case of sftp error.
	 */
	public long timestamp(final String file) throws IOException {
		final int mTime;
		try {
			final SftpATTRS stats = sftp.stat(file);
			mTime = stats.getMTime();
		}
		catch (final SftpException ex) {
			throw new IOException("Failed to extract remote timestamp from file '" +
				file + "'.", ex);
		}

		final Date date = new Date(((long) mTime) * 1000);
		return Long.parseLong(new SimpleDateFormat("yyyyMMddHHmmss").format(date));
	}

	private void mkParentDirs(final String root, final String path)
		throws IOException
	{
		if (path.contains("/")) {
			final int index = path.indexOf('/');
			final String newRoot = root + path.substring(0, index + 1);
			final String newPath = path.substring(index + 1);
			if (!"/".equals(newRoot) && !fileExists(newRoot)) {
				try {
					sftp.mkdir(newRoot);
				}
				catch (final SftpException ex) {
					throw wrapException("Failed to create directory '" + newRoot + "'",
						ex);
				}
			}
			mkParentDirs(newRoot, newPath);
		}
	}

	private IOException
		wrapException(final String message, final SftpException ex)
	{
		final String m =
			message + " SFTP error id=" + ex.id + ": " + ex.getMessage();
		UpdaterUserInterface.get().log(m);
		return new IOException(m);
	}

	/**
	 * Provides notification about count of uploaded bytes.
	 */
	public interface ProgressListener {

		/**
		 * Called with progress update.
		 * 
		 * @param count number of bytes uploaded so far.
		 */
		void progress(long count);
	}

	private static class ProgressMonitor implements SftpProgressMonitor {

		private long count = 0;
		private final ProgressListener listener;

		public ProgressMonitor(final ProgressListener listener) {
			this.listener = listener;
		}

		@Override
		public void init(final int op, final String src, final String dest,
			final long max)
		{
			count = 0;
		}

		@Override
		public boolean count(final long chunk) {
			count += chunk;
			if (listener != null) {
				listener.progress(count);
			}
			return true;
		}

		@Override
		public void end() {}
	}
}
