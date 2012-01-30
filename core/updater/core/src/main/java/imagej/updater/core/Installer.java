//
// Installer.java
//

/*
ImageJ software for multidimensional image processing and analysis.

Copyright (c) 2010, ImageJDev.org.
All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met:
    * Redistributions of source code must retain the above copyright
      notice, this list of conditions and the following disclaimer.
    * Redistributions in binary form must reproduce the above copyright
      notice, this list of conditions and the following disclaimer in the
      documentation and/or other materials provided with the distribution.
    * Neither the names of the ImageJDev.org developers nor the
      names of its contributors may be used to endorse or promote products
      derived from this software without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
POSSIBILITY OF SUCH DAMAGE.
*/

package imagej.updater.core;

import imagej.updater.util.Downloadable;
import imagej.updater.util.Downloader;
import imagej.updater.util.Progress;
import imagej.updater.util.UserInterface;
import imagej.updater.util.Util;

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
		for (final FileObject file : files.toUninstall())
			try {
				file.stageForUninstall(files);
			}
			catch (final IOException e) {
				e.printStackTrace();
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
					Util.patchInfoPList("ImageJ-tiger");
				}
				catch (final IOException e) {
					UserInterface.get().error("Could not patch Info.plist");
				}
			}

			final String url = files.getURL(file);
			final Download download = new Download(file, url, saveTo);
			list.add(download);
		}

		start(list);
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
			e.printStackTrace();
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
			e.printStackTrace();
			throw new RuntimeException("Could not mark " + destination +
				" as executable");
		}
	}
}
