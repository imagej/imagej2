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

package imagej.updater.core;

import imagej.updater.core.FilesCollection.UpdateSite;
import imagej.updater.util.Progress;
import imagej.updater.util.UpdaterUserInterface;
import imagej.updater.util.Util;
import imagej.util.Log;

import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

import net.java.sezpoz.Index;
import net.java.sezpoz.IndexItem;

/*
 * This class is responsible for writing updates to server, upon given the
 * updated file records.
 *
 * Note: Files are uploaded differently
 * - Local-only files & new file versions will have files AND details uploaded
 * - Uninstalled & up-to-date files will ONLY have their details uploaded
 *   (i.e.: XML file)
 */
public class FilesUploader {

	protected FilesCollection files;
	protected AbstractUploader uploader;

	protected String siteName;
	protected UpdateSite site;
	protected List<Uploadable> uploadables;
	protected String compressed;
	protected boolean loggedIn;

	public static AbstractUploader getUploader(String protocol)
		throws InstantiationException
	{
		if (protocol == null || protocol.equals("")) protocol = "ssh";
		for (final IndexItem<Uploader, AbstractUploader> item : Index.load(
			Uploader.class, AbstractUploader.class))
			if (item.annotation().protocol().equals(protocol)) return item.instance();
		throw new InstantiationException("No uploader found for protocol " +
			protocol);
	}

	// TODO: add a button to check for new db.xml.gz, and merge if necessary
	public FilesUploader(final FilesCollection files, final String updateSite)
		throws InstantiationException
	{
		this.files = files;
		siteName = updateSite;
		site = files.getUpdateSite(updateSite);
		compressed = Util.XML_COMPRESSED;
		uploader = getUploader(getUploadProtocol());
	}

	public boolean hasUploader() {
		return uploader != null;
	}

	public String getUploadProtocol() {
		final String host = site.sshHost;
		final int at = host.indexOf('@');
		final int colon = host.indexOf(':');
		if (colon > 0 && (at < 0 || colon < at)) return host.substring(0, colon);
		return null;
	}

	public String getDefaultUsername() {
		String host = site.sshHost;
		if (host.startsWith("sftp:")) host = host.substring(5);
		final int at = host.indexOf('@');
		if (at > 0) return host.substring(0, at);
		final String name = UpdaterUserInterface.get().getPref(Util.PREFS_USER);
		if (name == null) return "";
		return name;
	}

	public String getUploadHost() {
		return site.sshHost.substring(site.sshHost.indexOf('@') + 1);
	}

	public String getUploadDirectory() {
		return site.uploadDirectory;
	}

	protected class DbXmlFile implements Uploadable {

		public byte[] bytes;

		@Override
		public String getFilename() {
			return compressed + ".lock";
		}

		@Override
		public String getPermissions() {
			return "C0444";
		}

		@Override
		public long getFilesize() {
			return bytes.length;
		}

		@Override
		public InputStream getInputStream() {
			return new ByteArrayInputStream(bytes);
		}

		@Override
		public String toString() {
			return compressed;
		}
	}

	public void upload(final Progress progress) throws Exception {
		if (uploader == null) throw new RuntimeException("No uploader set for " +
			site.sshHost);
		if (!loggedIn) throw new RuntimeException("Not logged in!");
		if (new Conflicts(files).hasUploadConflicts()) throw new RuntimeException(
			"Unresolved upload conflicts!");
		uploader.addProgress(progress);
		uploader.addProgress(new VerifyTimestamp());

		uploadables = new ArrayList<Uploadable>();
		final List<String> locks = new ArrayList<String>();
		uploadables.add(new DbXmlFile());
		for (final FileObject file : files.toUpload(siteName))
			uploadables.add(new UploadableFile(files, file));

		// must be last lock
		locks.add(Util.XML_COMPRESSED);

		// verify that the files have not changed in the meantime
		final long[] timestamps = new long[uploadables.size()];
		int counter = 0;
		for (final Uploadable uploadable : uploadables) {
			if (uploadable instanceof UploadableFile) {
				final UploadableFile file = (UploadableFile) uploadable;
				timestamps[counter] = Util.getTimestamp(file.source);
			}
			verifyUnchanged(uploadable, true);
			counter++;
		}

		uploader.upload(uploadables, locks);

		// verify that the files have not changed in the meantime
		counter = 0;
		for (final Uploadable uploadable : uploadables) {
			if (uploadable instanceof UploadableFile) {
				final UploadableFile file = (UploadableFile) uploadable;
				if (timestamps[counter] != Util.getTimestamp(file.source)) throw new RuntimeException(
					"Timestamp of " + file.getFilename() +
						"changed since being checksummed (was " + timestamps[counter] +
						" but is " + Util.getTimestamp(file.source) + "!)");
			}
			counter++;
		}

		site.setLastModified(getCurrentLastModified());
	}

	protected void verifyUnchanged(final Uploadable file,
		final boolean checkTimestamp)
	{
		if (!(file instanceof UploadableFile)) return;
		final UploadableFile uploadable = (UploadableFile) file;
		final long size = uploadable.source.length();
		if (uploadable.filesize != size) throw new RuntimeException(
			"File size of " + uploadable.file.filename +
				" changed since being checksummed (was " + uploadable.filesize +
				" but is " + size + ")!");
		if (checkTimestamp) {
			final long stored =
				uploadable.file.getStatus() == FileObject.Status.LOCAL_ONLY
					? uploadable.file.current.timestamp : uploadable.file.newTimestamp;
			if (stored != Util.getTimestamp(uploadable.source)) throw new RuntimeException(
				"Timestamp of " + uploadable.file.filename +
					" changed since being checksummed (was " + stored + " but is " +
					Util.getTimestamp(uploadable.source) + ")!");
		}
	}

	protected void updateUploadTimestamp(final long timestamp) throws Exception {
		for (final Uploadable f : uploadables) {
			if (!(f instanceof UploadableFile)) continue;
			final UploadableFile uploadable = (UploadableFile) f;
			final FileObject file = uploadable.file;
			if (file == null) continue;
			file.filesize = uploadable.filesize = uploadable.source.length();
			file.newTimestamp = timestamp;
			uploadable.filename = file.filename + "-" + timestamp;
			if (file.getStatus() == FileObject.Status.LOCAL_ONLY) {
				file.setStatus(FileObject.Status.INSTALLED);
				file.current.timestamp = timestamp;
			}
		}

		final XMLFileWriter writer =
			new XMLFileWriter(files.clone(files.forUpdateSite(siteName)));
		if (files.size() > 0) writer.validate(false);
		((DbXmlFile) uploadables.get(0)).bytes =
			writer.toCompressedByteArray(false);

		uploader.calculateTotalSize(uploadables);
	}

	/*
	 * This class serves two purposes:
	 *
	 * - after locking, it ensures that the timestamp of db.xml.gz is the
	 *   same as when it was last downloaded, to prevent race-conditions
	 *
	 * - it takes the timestamp of the lock file and updates the timestamps
	 *   of all files to be uploaded, so that local time skews do not
	 *   harm
	 */
	protected class VerifyTimestamp implements Progress {

		@Override
		public void addItem(final Object item) {
			if (item != uploadables.get(0)) return;
			verifyTimestamp();
		}

		@Override
		public void setTitle(final String string) {
			try {
				updateUploadTimestamp(uploader.timestamp);
			}
			catch (final Exception e) {
				Log.error(e);
				throw new RuntimeException("Could not update "
					+ "the timestamps in db.xml.gz");
			}
		}

		@Override
		public void itemDone(final Object item) {
			if (item instanceof UploadableFile) verifyUnchanged(
				(UploadableFile) item, false);
		}

		@Override
		public void setCount(final int count, final int total) {}

		@Override
		public void setItemCount(final int count, final int total) {}

		@Override
		public void done() {}
	}

	protected long getCurrentLastModified() {
		try {
			URLConnection connection;
			try {
				connection = new URL(site.url + Util.XML_COMPRESSED).openConnection();
			}
			catch (final FileNotFoundException e) {
				Log.error(e);
				Thread.sleep(500);
				connection = new URL(site.url + Util.XML_COMPRESSED).openConnection();
			}
			connection.setUseCaches(false);
			final long lastModified = connection.getLastModified();
			connection.getInputStream().close();
			UpdaterUserInterface.get().debug(
				"got last modified " + lastModified + " = timestamp " +
					Util.timestamp(lastModified));
			return lastModified;
		}
		catch (final Exception e) {
			UpdaterUserInterface.get().debug(e.getMessage());
			if (files.size() == 0) return -1; // assume initial upload
			Log.error(e);
			return 0;
		}
	}

	protected void verifyTimestamp() {
		final long lastModified = getCurrentLastModified();
		if (!site.isLastModified(lastModified)) throw new RuntimeException(
			"db.xml.gz was " + "changed in the meantime (was " + site.timestamp +
				" but now is " + Util.timestamp(lastModified) + ")");
	}

	public boolean login() {
		if (loggedIn) return loggedIn;
		loggedIn = uploader.login(this);
		return loggedIn;
	}

	public static FilesUploader initialUpload(final String url,
		final String sshHost, final String uploadDirectory)
		throws InstantiationException
	{
		final String updateSiteName = "Dummy";
		final FilesCollection files = new FilesCollection(null);
		files.addUpdateSite(updateSiteName, url, sshHost, uploadDirectory, Long
			.parseLong(Util.timestamp(-1)));
		return new FilesUploader(files, updateSiteName);
	}
}
