
package imagej.updater.core;

import imagej.updater.core.FileUploader.SourceFile;
import imagej.updater.util.Util;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

// TODO: Replace this comment by something helpful.
/*
 * Implementation of class containing information for FileUploader to use
 */
// TODO: unify "FileUploader.SourceFile" and "Downloader.FileDownload" into
// a single "Transferable", and refactor Downloader and FileUploader into
// a single "Transfer" class extending Progressable.
// TODO: this class should be merged into Uploader.
public class UploadableFile implements SourceFile {

	PluginObject plugin;
	String permissions, sourceFilename, filename;
	long filesize;

	public UploadableFile(final String target) {
		this(Util.prefix(target), target);
	}

	public UploadableFile(final PluginObject plugin) {
		this(Util.prefix(plugin.getFilename()), plugin.getFilename() + "-" +
			plugin.getTimestamp());
		this.plugin = plugin;
	}

	public UploadableFile(final String source, final String target) {
		this(source, target, "C0644");
	}

	public UploadableFile(final String source, final String target,
		final String permissions)
	{
		// TODO: fix naming
		this.sourceFilename = source;
		this.filename = target;
		this.permissions = permissions;
		final File file = new File(source);
		filesize = file.exists() ? file.length() : 0;
	}

	void updateFilesize() {
		filesize = new File(sourceFilename).length();
	}

	// ********** Implemented methods for SourceFile **********
	@Override
	public long getFilesize() {
		return filesize;
	}

	@Override
	public String getFilename() {
		return filename;
	}

	@Override
	public String getPermissions() {
		return permissions;
	}

	@Override
	public InputStream getInputStream() throws IOException {
		try {
			return new FileInputStream(sourceFilename);
		}
		catch (final FileNotFoundException e) {
			return new ByteArrayInputStream(new byte[0]);
		}
	}

	@Override
	public String toString() {
		return filename;
	}
}
