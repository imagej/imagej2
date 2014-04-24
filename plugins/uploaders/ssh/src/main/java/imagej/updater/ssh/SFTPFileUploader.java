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

package imagej.updater.ssh;

import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import net.imagej.updater.AbstractUploader;
import net.imagej.updater.FilesUploader;
import net.imagej.updater.Uploadable;
import net.imagej.updater.Uploader;
import net.imagej.updater.util.UpdateCanceledException;
import net.imagej.updater.util.UpdaterUserInterface;

import org.scijava.log.LogService;
import org.scijava.plugin.Plugin;

/**
 * Uploads files to an update server using only SFTP protocol. In contrast to
 * SSHFileUploader it does not execute any remote commands using SSH. This
 * important when setting up update site on Source Forge its restricted Shell
 * does not allow execution of any remote commands.
 * 
 * @author Jarek Sacha
 */
@Plugin(type = Uploader.class)
public final class SFTPFileUploader extends AbstractUploader {

	private SFTPOperations sftp;
	protected LogService log;

	@Override
	public boolean login(final FilesUploader uploader) {
		log = uploader.getLog();
		if (!super.login(uploader)) return false;
		final Session session = SSHSessionCreator.getSession(uploader);
		if (session == null) return false;
		try {
			sftp = new SFTPOperations(session);
			return true;
		}
		catch (final JSchException e) {
			log.error(e);
			return false;
		}
	}

	@Override
	public void logout() {
		try {
			disconnectSession();
		} catch (IOException e) {
			log.error(e);
		}
	}

	@Override
	public synchronized void upload(final List<Uploadable> sources,
		final List<String> locks) throws IOException
	{

		timestamp = remoteTimeStamp();
		// 'timestamp' has to be set before calling setTitle("Uploading")
		// setTitle("Uploading") has a side effect of adding timestamp to file names
		// of 'sources'
		// If timestamp is not set file names will end with '*-0'.
		// See imagej.updater.core.FilesUploader.VerifyTimestamp.setTitle(String)
		setTitle("Uploading");

		try {
			uploadFiles(sources);
		}
		catch (final UpdateCanceledException cancel) {
			// Delete locks
			for (final String lock : locks) {
				final String path = uploadDir + lock + ".lock";
				try {
					sftp.rm(path);
				}
				catch (final IOException ex) {
					// Do not re-throw, since 'cancel' exception will be thrown.
				}
			}
			throw cancel;
		}

		// Unlock process
		for (final String lock : locks) {
			final String src = uploadDir + lock + ".lock";
			final String dest = uploadDir + lock;
			sftp.rename(src, dest);
		}

		disconnectSession();
	}

	private void uploadFiles(final List<Uploadable> uploadables)
		throws IOException
	{
		calculateTotalSize(uploadables);

		int sizeOfFilesUploadedSoFar = 0;

		for (final Uploadable uploadable : uploadables) {
			final String target = uploadable.getFilename();

			/*
			 * Make sure that the file is there; this is critical
			 * to get the server timestamp from db.xml.gz.lock.
			 */
			addItem(uploadable);

			// send contents of file
			final InputStream input = uploadable.getInputStream();
			final int currentFileSize = (int) uploadable.getFilesize();
			final String dest = this.uploadDir + target;
			try {
				UpdaterUserInterface.get().log(
					"Upload '" + uploadable.getFilename() + "', size " +
						uploadable.getFilesize());

				// Setup progress monitoring for current file
				final int uploadedBytesCount = sizeOfFilesUploadedSoFar;
				final SFTPOperations.ProgressListener listener =
					new SFTPOperations.ProgressListener() {

						@Override
						public void progress(final long currentCount) {
							setItemCount((int) currentCount, currentFileSize);
							setCount(uploadedBytesCount + (int) currentCount, total);
						}
					};

				// Upload file
				sftp.put(input, dest, listener);
			}
			finally {
				input.close();
			}

			// Update progress notifications
			sizeOfFilesUploadedSoFar += currentFileSize;
			setItemCount(currentFileSize, currentFileSize);
			setCount(sizeOfFilesUploadedSoFar, total);

			itemDone(uploadable);
		}

		// Complete progress notification
		done();
	}

	void disconnectSession() throws IOException {
		sftp.disconnect();
	}

	/**
	 * Extract current time at remote server
	 * 
	 * @return time stamp
	 * @throws IOException when execution of remote date command fails.
	 */
	private long remoteTimeStamp() throws IOException {
		// Normally time stamp would be created using shell command: date
		// +%Y%m%d%H%M%S
		// Shell commands cannot be executed on restricted shell accounts like
		// SourceForge
		// Need to simulate date command by creating a temporary file and reading
		// its modification time.

		final InputStream in = new ByteArrayInputStream("".getBytes());
		final String destFile = uploadDir + "timestamp";
		final long timestamp;
		sftp.put(in, destFile);
		timestamp = sftp.timestamp(destFile);
		sftp.rm(destFile);

		return timestamp;
	}

	@Override
	public String getProtocol() {
		return "sftp";
	}

}
