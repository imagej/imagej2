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

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

import imagej.updater.core.AbstractUploader;
import imagej.updater.core.FilesUploader;
import imagej.updater.core.Uploadable;
import imagej.updater.core.Uploader;
import imagej.updater.util.Canceled;
import imagej.updater.util.InputStream2OutputStream;
import imagej.updater.util.UpdaterUserInterface;
import imagej.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

/**
 * Uploads files to an update server using SSH. In addition to writing files, it
 * uses 'mv' and permissions to provide safe locking.
 * 
 * @author Johannes Schindelin
 * @author Yap Chin Kiet
 */
@Uploader(protocol = "ssh")
public class SSHFileUploader extends AbstractUploader {

	private Session session;
	private Channel channel;
	private OutputStream out;
	protected OutputStream err;
	private InputStream in;

	public SSHFileUploader() throws JSchException {
		err = UpdaterUserInterface.get().getOutputStream();
	}

	@Override
	public boolean login(final FilesUploader uploader) {
		if (!super.login(uploader)) return false;
		session = SSHSessionCreator.getSession(uploader);
		return session != null;
	}

	// Steps to accomplish entire upload task
	@Override
	public synchronized void upload(final List<Uploadable> sources,
		final List<String> locks) throws IOException
	{

		setCommand("date --utc +%Y%m%d%H%M%S");
		timestamp = readNumber(in);
		setTitle("Uploading");

		final String uploadFilesCommand = "scp -p -t -r " + uploadDir;
		setCommand(uploadFilesCommand);
		if (checkAck(in) != 0) {
			throw new IOException("Failed to set command " + uploadFilesCommand);
		}

		try {
			uploadFiles(sources);
		}
		catch (final Canceled cancel) {
			for (final String lock : locks)
				setCommand("rm " + uploadDir + lock + ".lock");
			out.close();
			channel.disconnect();
			throw cancel;
		}

		// Unlock process
		for (final String lock : locks)
			setCommand("mv " + uploadDir + lock + ".lock " + uploadDir + lock);

		out.close();
		disconnectSession();
	}

	private void uploadFiles(final List<Uploadable> sources) throws IOException {
		calculateTotalSize(sources);
		int count = 0;

		String prefix = "";
		final byte[] buf = new byte[16384];
		for (final Uploadable source : sources) {
			final String target = source.getFilename();
			while (!target.startsWith(prefix))
				prefix = cdUp(prefix);

			// maybe need to enter directory
			final int slash = target.lastIndexOf('/');
			final String directory = target.substring(0, slash + 1);
			cdInto(directory.substring(prefix.length()));
			prefix = directory;

			// notification that file is about to be written
			final String command =
				source.getPermissions() + " " + source.getFilesize() + " " +
					target.substring(slash + 1) + "\n";
			out.write(command.getBytes());
			out.flush();
			checkAckUploadError(target);

			/*
			 * Make sure that the file is there; this is critical
			 * to get the server timestamp from db.xml.gz.lock.
			 */
			addItem(source);

			// send contents of file
			final InputStream input = source.getInputStream();
			int currentCount = 0;
			final int currentTotal = (int) source.getFilesize();
			for (;;) {
				final int len = input.read(buf, 0, buf.length);
				if (len <= 0) break;
				out.write(buf, 0, len);
				currentCount += len;
				setItemCount(currentCount, currentTotal);
				setCount(count + currentCount, total);
			}
			input.close();
			count += currentCount;

			// send '\0'
			buf[0] = 0;
			out.write(buf, 0, 1);
			out.flush();
			checkAckUploadError(target);
			itemDone(source);
		}

		while (!prefix.equals("")) {
			prefix = cdUp(prefix);
		}

		done();
	}

	private String cdUp(final String directory) throws IOException {
		out.write("E\n".getBytes());
		out.flush();
		checkAckUploadError(directory);
		final int slash = directory.lastIndexOf('/', directory.length() - 2);
		return directory.substring(0, slash + 1);
	}

	private void cdInto(String directory) throws IOException {
		while (!directory.equals("")) {
			final int slash = directory.indexOf('/');
			final String name =
				(slash < 0 ? directory : directory.substring(0, slash));
			final String command = "D2775 0 " + name + "\n";
			out.write(command.getBytes());
			out.flush();
			if (checkAck(in) != 0) throw new IOException("Cannot enter directory " +
				name);
			if (slash < 0) return;
			directory = directory.substring(slash + 1);
		}
	}

	private void setCommand(final String command) throws IOException {
		if (out != null) {
			out.close();
			channel.disconnect();
		}
		try {
			UpdaterUserInterface.get().debug("launching command " + command);
			channel = session.openChannel("exec");
			((ChannelExec) channel).setCommand(command);
			channel.setInputStream(null);
			((ChannelExec) channel).setErrStream(err);

			// get I/O streams for remote scp
			out = channel.getOutputStream();
			in = channel.getInputStream();
			channel.connect();
		}
		catch (final JSchException e) {
			Log.error(e);
			throw new IOException(e.getMessage());
		}
	}

	private void checkAckUploadError(final String target) throws IOException {
		if (checkAck(in) != 0) throw new IOException("Failed to upload " + target);
	}

	public void disconnectSession() throws IOException {
		new InputStream2OutputStream(in, UpdaterUserInterface.get().getOutputStream());
		try {
			Thread.sleep(100);
		}
		catch (final InterruptedException e) {
			/* ignore */
		}
		out.close();
		try {
			Thread.sleep(1000);
		}
		catch (final InterruptedException e) {
			/* ignore */
		}
		final int exitStatus = channel.getExitStatus();
		UpdaterUserInterface.get().debug(
			"disconnect session; exit status is " + exitStatus);
		channel.disconnect();
		session.disconnect();
		err.close();
		if (exitStatus != 0) throw new IOException("Command failed with status " +
			exitStatus + " (see Log)!");
	}

	protected long readNumber(final InputStream in) throws IOException {
		long result = 0;
		for (;;) {
			final int b = in.read();
			if (b >= '0' && b <= '9') result = 10 * result + b - '0';
			else if (b == '\n') return result;
		}
	}

	private int checkAck(final InputStream in) throws IOException {
		final int b = in.read();
		// b may be 0 for success,
		// 1 for error,
		// 2 for fatal error,
		// -1
		if (b == 0) return b;
		UpdaterUserInterface.get().handleException(new Exception("checkAck returns " + b));
		if (b == -1) return b;

		if (b == 1 || b == 2) {
			final StringBuffer sb = new StringBuffer();
			int c;
			do {
				c = in.read();
				sb.append((char) c);
			}
			while (c != '\n');
			UpdaterUserInterface.get().log("checkAck returned '" + sb.toString() + "'");
			UpdaterUserInterface.get().error(sb.toString());
		}
		return b;
	}
}
