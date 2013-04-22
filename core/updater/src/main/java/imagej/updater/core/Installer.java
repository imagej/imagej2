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

package imagej.updater.core;

import imagej.command.CommandInfo;
import imagej.command.CommandService;
import imagej.updater.core.Conflicts.Conflict;
import imagej.updater.core.FileObject.Action;
import imagej.updater.core.FileObject.Status;
import imagej.updater.util.Downloadable;
import imagej.updater.util.Downloader;
import imagej.updater.util.Progress;
import imagej.updater.util.UpdaterUserInterface;
import imagej.updater.util.Util;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * A class that updates local files from all available update sites.
 * 
 * @author Johannes Schindelin
 */
public class Installer extends Downloader {

	private FilesCollection files;

	public Installer(final FilesCollection files, final Progress progress) {
		this.files = files;
		if (progress != null)
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
		if (new Conflicts(files).hasDownloadConflicts()) {
			final StringBuilder builder = new StringBuilder();
			builder.append("Unresolved conflicts:\n");
			for (Conflict conflict : new Conflicts(files).getConflicts(false)) {
				builder.append(conflict.getFilename()).append(": ").append(conflict.getConflict());
			}
			throw new RuntimeException(builder.toString());
		}

		// mark for removal
		final FilesCollection uninstalled = files.clone(files.toUninstall());
		for (final FileObject file : uninstalled)
			try {
				file.stageForUninstall(files);
			}
			catch (final IOException e) {
				files.log.error(e);
				throw new RuntimeException("Could not mark '" + file + "' for removal");
			}

		final List<Downloadable> list = new ArrayList<Downloadable>();
		for (final FileObject file : files.toInstallOrUpdate()) {
			final String name = file.filename;
			File saveTo = files.prefixUpdate(name);
			if (file.localFilename != null && !file.localFilename.equals(file.filename)) {
				// if the name changed, remove the file with the old name
				FileObject.touch(files.prefixUpdate(file.localFilename));
			}
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

	protected final static String UPDATER_JAR_NAME = "jars/ij-updater-core.jar";

	public static Set<FileObject> getUpdaterFiles(final FilesCollection files, final CommandService commandService, final boolean onlyUpdateable) {
		final Set<FileObject> result = new HashSet<FileObject>();
		final FileObject updater = files.get(UPDATER_JAR_NAME);
		if (updater == null) return result;
		final Set<FileObject> topLevel = new HashSet<FileObject>();
		topLevel.add(updater);
		if (commandService == null) {
			final String hardcoded = "jars/ij-ui-swing-updater.jar";
			final FileObject file = files.get(hardcoded);
			if (file != null) topLevel.add(file);
		} else {
			for (final CommandInfo info : commandService
				.getCommandsOfType(UpdaterUI.class))
			{
				final FileObject file = getFileObject(files, info.getClassName());
				if (file != null) {
					topLevel.add(file);
				}
			}
		}
		for (final FileObject file : topLevel) {
			for (final FileObject file2 : file.getFileDependencies(files, true)) {
				if (!onlyUpdateable) {
					result.add(file2);
				} else switch (file.getStatus()) {
				case NEW: case NOT_INSTALLED: case UPDATEABLE:
					result.add(file2);
					break;
				default:
				}
			}
		}
		return result;
	}

	/**
	 * Gets the file object for the .jar file containing the given class.
	 * 
	 * Unfortunately, at the time of writing, we could not rely on ij-core being updated properly when ij-updater-core was updated,
	 * so we had to invent this method which logically belongs into imagej.util.FileUtils.
	 * 
	 * @param files the database of available files
	 * @param className the name of the class we seek the .jar file for
	 * @return the file object, or null if the class could not be found in any file of the collection
	 */
	private static FileObject getFileObject(final FilesCollection files, final String className) {
		try {
			final String path = "/" + className.replace('.', '/') + ".class";
			String jar = Installer.class.getClassLoader().loadClass(className).getResource(path).toString();
			if (!jar.endsWith(".jar!" + path)) return null;
			jar = jar.substring(0, jar.length() - path.length() - 1);
			String prefix = "jar:file:" + System.getProperty("ij.dir");
			if (!prefix.endsWith("/")) prefix += "/";
			if (!jar.startsWith(prefix)) return null;
			jar = jar.substring(prefix.length());
			return files.get(jar);
		} catch (ClassNotFoundException e) { /* ignore */ }
		return null;
	}

	public static boolean isTheUpdaterUpdateable(final FilesCollection files) {
		return isTheUpdaterUpdateable(files, null);
	}

	public static boolean isTheUpdaterUpdateable(final FilesCollection files, final CommandService commandService) {
		return getUpdaterFiles(files, commandService, true).size() > 0;
	}

	public static void updateTheUpdater(final FilesCollection files, final Progress progress) throws IOException {
		updateTheUpdater(files, progress, null);
	}

	public static void updateTheUpdater(final FilesCollection files, final Progress progress, final CommandService commandService) throws IOException {
		final Set<FileObject> all = getUpdaterFiles(files, commandService, true);
		int counter = 0;
		for (final FileObject file : all) {
			if (file.setFirstValidAction(files, Action.UPDATE, Action.INSTALL))
				counter++;
		}
		if (counter == 0) return; // nothing to be done
		final FilesCollection.Filter filter = new FilesCollection.Filter() {

			@Override
			public boolean matches(final FileObject file) {
				return all.contains(file);
			}
		};
		final FilesCollection justTheUpdater = files.clone(files.filter(filter));
		final Installer installer = new Installer(justTheUpdater, progress);
		try {
			installer.start();
		}
		finally {
			// TODO: remove "update/" directory
			installer.done();
		}
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
			if (!digest.equals(actualDigest)) {
				List<String> obsoletes = Util.getObsoleteDigests(file.getFilename(), destination);
				if (obsoletes != null) {
					for (final String obsolete : obsoletes) {
						if (digest.equals(obsolete)) actualDigest = obsolete;
					}
				}
			}
		}
		catch (final Exception e) {
			files.log.error(e);
			throw new RuntimeException("Could not verify checksum " + "for " +
				destination);
		}

		if (!digest.equals(actualDigest)) throw new RuntimeException(
			"Incorrect checksum " + "for " + destination + ":\n" + actualDigest +
				"\n(expected " + digest + ")");

		file.setLocalVersion(file.getFilename(), digest, file.getTimestamp());
		file.setStatus(FileObject.Status.INSTALLED);

		if (file.executable && !files.util.platform.startsWith("win")) try {
			Runtime.getRuntime()
				.exec(
					new String[] { "chmod", "0755",
						download.destination.getAbsolutePath() });
		}
		catch (final Exception e) {
			files.log.error(e);
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
