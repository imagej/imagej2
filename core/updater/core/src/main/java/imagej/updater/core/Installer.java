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

import imagej.updater.core.FileObject.Status;
import imagej.updater.util.Downloadable;
import imagej.updater.util.Downloader;
import imagej.updater.util.Progress;
import imagej.updater.util.UpdaterUserInterface;
import imagej.updater.util.Util;
import imagej.util.Log;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Installer extends Downloader {

	protected FilesCollection files;

	public Installer(final FilesCollection files, final Progress progress) {
		this.files = files;
		addProgress(progress);
		addProgress(new VerifyFiles());
	}

	class Download implements Downloadable {

		protected FileObject file;
		protected String url;
		protected File destination;

		Download(final FileObject file, final String url, final File destination) {
			this.file = file;
			this.url = url;
			this.destination = destination;
		}

		@Override
		public String toString() {
			return file.getFilename();
		}

		@Override
		public File getDestination() {
			return destination;
		}

		@Override
		public String getURL() {
			return url;
		}

		@Override
		public long getFilesize() {
			return file.filesize;
		}
	}

	public synchronized void start() throws IOException {
		if (new Conflicts(files).hasDownloadConflicts()) throw new RuntimeException(
			"Unresolved conflicts!");

		// mark for removal
		final FilesCollection uninstalled = files.clone(files.toUninstall());
		for (final FileObject file : uninstalled)
			try {
				file.stageForUninstall(files);
			}
			catch (final IOException e) {
				Log.error(e);
				throw new RuntimeException("Could not mark '" + file + "' for removal");
			}

		final List<Downloadable> list = new ArrayList<Downloadable>();
		for (final FileObject file : files.toInstallOrUpdate()) {
			final String name = file.filename;
			File saveTo = files.prefixUpdate(name);
			if (file.executable) {
				saveTo = files.prefix(name);
				String oldName = saveTo.getAbsolutePath() + ".old";
				if (oldName.endsWith(".exe.old")) oldName =
					oldName.substring(0, oldName.length() - 8) + ".old.exe";
				final File old = new File(oldName);
				if (old.exists()) old.delete();
				saveTo.renameTo(old);
				if (name.equals(Util.macPrefix + "ImageJ-tiger")) try {
					Util.patchInfoPList(files.prefix("Contents/Info.plist"), "ImageJ-tiger");
				}
				catch (final IOException e) {
					UpdaterUserInterface.get().error("Could not patch Info.plist");
				}
			}

			final String url = files.getURL(file);
			final Download download = new Download(file, url, saveTo);
			list.add(download);
		}

		start(list);

		for (final FileObject file : uninstalled)
			if (file.isLocalOnly()) files.remove(file);
			else file.setStatus(file.isObsolete() ? Status.OBSOLETE_UNINSTALLED
				: Status.NOT_INSTALLED);
	}

	class VerifyFiles implements Progress {

		@Override
		public void itemDone(final Object item) {
			verify((Download) item);
		}

		@Override
		public void setTitle(final String title) {}

		@Override
		public void setCount(final int count, final int total) {}

		@Override
		public void addItem(final Object item) {}

		@Override
		public void setItemCount(final int count, final int total) {}

		@Override
		public void done() {}
	}

	public void verify(final Download download) {
		final File destination = download.getDestination();
		final long size = download.getFilesize();
		final long actualSize = destination.length();
		if (size != actualSize) throw new RuntimeException(
			"Incorrect file size for " + destination + ": " + actualSize +
				" (expected " + size + ")");

		final FileObject file = download.file;
		final String digest = download.file.getChecksum();
		String actualDigest;
		try {
			actualDigest = Util.getDigest(file.getFilename(), destination);
		}
		catch (final Exception e) {
			Log.error(e);
			throw new RuntimeException("Could not verify checksum " + "for " +
				destination);
		}

		if (!digest.equals(actualDigest)) throw new RuntimeException(
			"Incorrect checksum " + "for " + destination + ":\n" + actualDigest +
				"\n(expected " + digest + ")");

		file.setLocalVersion(digest, file.getTimestamp());
		file.setStatus(FileObject.Status.INSTALLED);

		if (file.executable && !Util.platform.startsWith("win")) try {
			Runtime.getRuntime()
				.exec(
					new String[] { "chmod", "0755",
						download.destination.getAbsolutePath() });
		}
		catch (final Exception e) {
			Log.error(e);
			throw new RuntimeException("Could not mark " + destination +
				" as executable");
		}
	}

	public void moveUpdatedIntoPlace() throws IOException {
		moveUpdatedIntoPlace(files.prefix("update"), files.prefix("."));
	}

	protected void moveUpdatedIntoPlace(final File sourceDirectory,
		final File targetDirectory) throws IOException
	{
		if (!sourceDirectory.isDirectory()) return;
		final File[] list = sourceDirectory.listFiles();
		if (list == null) return;
		if (!targetDirectory.isDirectory() && !targetDirectory.mkdir()) throw new IOException(
			"Could not create directory '" + targetDirectory + "'");
		for (final File file : list) {
			final File targetFile = new File(targetDirectory, file.getName());
			if (file.isDirectory()) {
				moveUpdatedIntoPlace(file, targetFile);
			}
			else if (file.isFile()) {
				if (file.length() == 0) {
					if (targetFile.exists()) deleteOrThrowException(targetFile);
					deleteOrThrowException(file);
				}
				else {
					if (!file.renameTo(targetFile) &&
						!((targetFile.delete() || moveOutOfTheWay(targetFile)) && file
							.renameTo(targetFile)))
					{
						throw new IOException("Could not move '" + file + "' to '" +
							targetFile + "'");
					}
				}
			}
		}
		deleteOrThrowException(sourceDirectory);
	}

	protected static void deleteOrThrowException(final File file)
		throws IOException
	{
		if (!file.delete()) throw new IOException("Could not remove '" + file + "'");
	}

	protected static boolean moveOutOfTheWay(final File file) {
		if (!file.exists()) return true;
		String prefix = file.getName(), suffix = "";
		if (prefix.endsWith(".exe") || prefix.endsWith(".EXE")) {
			suffix = prefix.substring(prefix.length() - 4);
			prefix = prefix.substring(0, prefix.length() - 4);
		}
		File backup = new File(file.getParentFile(), prefix + ".old" + suffix);
		if (backup.exists() && !backup.delete()) {
			final int i = 2;
			for (;;) {
				backup = new File(file.getParentFile(), prefix + ".old" + i + suffix);
				if (!backup.exists()) break;
			}
		}
		return file.renameTo(backup);
	}

}
