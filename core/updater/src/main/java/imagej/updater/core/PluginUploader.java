
package imagej.updater.core;

import imagej.updater.core.FileUploader.SourceFile;
import imagej.updater.core.PluginCollection.UpdateSite;
import imagej.updater.util.Progress;
import imagej.updater.util.UserInterface;
import imagej.updater.util.Util;

import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

/*
 * This class is responsible for writing updates to server, upon given the
 * updated plugin records.
 *
 * Note: Plugins are uploaded differently
 * - Local-only plugins & new file versions will have files AND details uploaded
 * - Uninstalled & up-to-date plugins will ONLY have their details uploaded
 *   (i.e.: XML file)
 */
public class PluginUploader {

	protected PluginCollection plugins;
	protected FileUploader uploader;

	protected String siteName;
	protected UpdateSite site;
	protected List<SourceFile> files;
	protected String compressed;

	// TODO: add a button to check for new db.xml.gz, and merge if necessary
	public PluginUploader(final PluginCollection plugins, final String updateSite)
	{
		this.plugins = plugins;
		siteName = updateSite;
		site = plugins.getUpdateSite(updateSite);
		compressed = Util.XML_COMPRESSED;
		if (site.sshHost == null || site.sshHost.equals("")) uploader =
			new FileUploader(site.uploadDirectory);
	}

	public boolean hasUploader() {
		return uploader != null;
	}

	public String getUploadProtocol() {
		final String host = site.sshHost;
		final int at = host.indexOf('@');
		final int colon = host.indexOf(':');
		if (colon > 0 && colon < at) return host.substring(0, colon);
		return null;
	}

	public String getDefaultUsername() {
		String host = site.sshHost;
		if (host.startsWith("sftp:")) host = host.substring(5);
		final int at = host.indexOf('@');
		if (at > 0) return host.substring(0, at);
		final String name = UserInterface.get().getPref(Util.PREFS_USER);
		if (name == null) return "";
		return name;
	}

	public String getUploadHost() {
		return site.sshHost.substring(site.sshHost.indexOf('@') + 1);
	}

	public String getUploadDirectory() {
		return site.uploadDirectory;
	}

	public void setUploader(final FileUploader uploader) {
		this.uploader = uploader;
	}

	protected class DbXmlFile implements SourceFile {

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
		uploader.addProgress(progress);
		uploader.addProgress(new VerifyTimestamp());

		// TODO: rename "UpdateSource" to "Transferable", reuse!
		files = new ArrayList<SourceFile>();
		final List<String> locks = new ArrayList<String>();
		files.add(new DbXmlFile());
		for (final PluginObject plugin : plugins.toUpload(siteName))
			files.add(new UploadableFile(plugin));

		// must be last lock
		locks.add(Util.XML_COMPRESSED);

		// verify that the files have not changed in the meantime
		for (final SourceFile file : files)
			verifyUnchanged(file, true);

		uploader.upload(files, locks);

		site.setLastModified(getCurrentLastModified());
	}

	protected void verifyUnchanged(final SourceFile file,
		final boolean checkTimestamp)
	{
		if (!(file instanceof UploadableFile)) return;
		final UploadableFile uploadable = (UploadableFile) file;
		if (uploadable.filesize != Util.getFilesize(uploadable.sourceFilename)) throw new RuntimeException(
			"File size of " + uploadable.plugin.filename +
				" changed since being checksummed (was " + uploadable.filesize +
				" but is " + Util.getFilesize(uploadable.sourceFilename) + ")!");
		if (checkTimestamp) {
			final long stored =
				uploadable.plugin.getStatus() == PluginObject.Status.NOT_FIJI
					? uploadable.plugin.current.timestamp
					: uploadable.plugin.newTimestamp;
			if (stored != Util.getTimestamp(uploadable.sourceFilename)) throw new RuntimeException(
				"Timestamp of " + uploadable.plugin.filename +
					" changed since being checksummed (was " + stored + " but is " +
					Util.getTimestamp(uploadable.sourceFilename) + ")!");
		}
	}

	protected void updateUploadTimestamp(final long timestamp) throws Exception {
		for (final SourceFile f : files) {
			if (!(f instanceof UploadableFile)) continue;
			final UploadableFile file = (UploadableFile) f;
			final PluginObject plugin = file.plugin;
			if (plugin == null) continue;
			plugin.filesize = file.filesize = Util.getFilesize(plugin.filename);
			plugin.newTimestamp = timestamp;
			file.filename = plugin.filename + "-" + timestamp;
			if (plugin.getStatus() == PluginObject.Status.NOT_FIJI) {
				plugin.setStatus(PluginObject.Status.INSTALLED);
				plugin.current.timestamp = timestamp;
			}
		}

		final XMLFileWriter writer =
			new XMLFileWriter(PluginCollection.clone(plugins.forUpdateSite(siteName)));
		if (plugins.size() > 0) writer.validate(false);
		((DbXmlFile) files.get(0)).bytes = writer.toCompressedByteArray(false);

		uploader.calculateTotalSize(files);
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
			if (item != files.get(0)) return;
			verifyTimestamp();
		}

		@Override
		public void setTitle(final String string) {
			try {
				updateUploadTimestamp(uploader.timestamp);
			}
			catch (final Exception e) {
				e.printStackTrace();
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
				e.printStackTrace();
				Thread.sleep(500);
				connection = new URL(site.url + Util.XML_COMPRESSED).openConnection();
			}
			connection.setUseCaches(false);
			final long lastModified = connection.getLastModified();
			connection.getInputStream().close();
			UserInterface.get().debug(
				"got last modified " + lastModified + " = timestamp " +
					Util.timestamp(lastModified));
			return lastModified;
		}
		catch (final Exception e) {
			UserInterface.get().debug(e.getMessage());
			if (plugins.size() == 0) return -1; // assume initial upload
			e.printStackTrace();
			return 0;
		}
	}

	protected void verifyTimestamp() {
		final long lastModified = getCurrentLastModified();
		if (!site.isLastModified(lastModified)) throw new RuntimeException(
			"db.xml.gz was " + "changed in the meantime (was " + site.timestamp +
				" but now is " + Util.timestamp(lastModified) + ")");
	}
}
