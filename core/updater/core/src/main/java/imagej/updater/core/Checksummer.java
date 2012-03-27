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
import imagej.updater.util.Progress;
import imagej.updater.util.Progressable;
import imagej.updater.util.Util;
import imagej.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.zip.ZipException;

/**
 * A class to checksum and timestamp all the files shown in the Updater's UI.
 * 
 * @author Johannes Schindelin
 * @author Yap Chin Kiet
 */
public class Checksummer extends Progressable {

	protected FilesCollection files;
	protected int counter, total;
	protected Map<String, FileObject.Version> cachedChecksums;

	public Checksummer(final FilesCollection files, final Progress progress) {
		this.files = files;
		if (progress != null) addProgress(progress);
		setTitle("Czechsummer");
	}

	protected static class StringAndFile {

		protected String path;
		protected File file;

		StringAndFile(final String path, final File file) {
			this.path = path;
			this.file = file;
		}
	}

	public Map<String, FileObject.Version> getCachedChecksums() {
		return cachedChecksums;
	}

	protected List<StringAndFile> queue;

	/* follows symlinks */
	protected boolean exists(final File file) {
		try {
			return file.getCanonicalFile().exists();
		}
		catch (final IOException e) {
			Log.error(e);
		}
		return false;
	}

	public void queueDir(final String[] dirs, final String[] extensions) {
		final Set<String> set = new HashSet<String>();
		for (final String extension : extensions)
			set.add(extension);
		for (final String dir : dirs)
			queueDir(dir, set);
	}

	public void queueDir(final String dir, final Set<String> extensions) {
		File file = files.prefix(dir);
		if (!exists(file)) return;
		for (final String item : file.list()) {
			final String path = dir + "/" + item;
			file = files.prefix(path);
			if (item.startsWith(".")) continue;
			if (file.isDirectory()) {
				queueDir(path, extensions);
				continue;
			}
			if (!extensions.contains("")) {
				final int dot = item.lastIndexOf('.');
				if (dot < 0 || !extensions.contains(item.substring(dot))) continue;
			}
			if (exists(file)) queue(path, file);
		}
	}

	protected void queueIfExists(final String path) {
		final File file = files.prefix(path);
		if (file.exists()) queue(path, file);
	}

	protected void queue(final String path) {
		queue(path, files.prefix(path));
	}

	protected void queue(final String path, final File file) {
		queue.add(new StringAndFile(path, file));
	}

	protected void handle(final StringAndFile pair) {
		final String path = pair.path;
		final File file = pair.file;
		addItem(path);

		String checksum = null;
		long timestamp = 0;
		if (file.exists()) try {
			timestamp = Util.getTimestamp(file);
			checksum = getDigest(path, file, timestamp);

			FileObject object = files.get(path);
			if (object == null) {
				if (checksum == null) throw new RuntimeException("Tried to remove " +
					path + ", which is not known to the Updater");
				object =
					new FileObject(null, path, file.length(), checksum, timestamp,
						Status.LOCAL_ONLY);
				if (file.canExecute() || path.endsWith(".exe")) object.executable =
					true;
				tryToGuessPlatform(object);
				files.add(object);
			}
			else if (checksum != null) {
				object.setLocalVersion(checksum, timestamp);
				if (object.getStatus() == Status.OBSOLETE_UNINSTALLED) object
					.setStatus(Status.OBSOLETE);
			}
		}
		catch (final ZipException e) {
			Log.error("Problem digesting " + file);
		}
		catch (final Exception e) {
			Log.error(e);
		}
		else {
			final FileObject object = files.get(path);
			if (object != null) {
				switch (object.getStatus()) {
					case OBSOLETE:
					case OBSOLETE_MODIFIED:
						object.setStatus(Status.OBSOLETE_UNINSTALLED);
						break;
					case INSTALLED:
					case MODIFIED:
					case UPDATEABLE:
						object.setStatus(Status.NOT_INSTALLED);
						break;
					case LOCAL_ONLY:
						files.remove(path);
						break;
					case NEW:
					case NOT_INSTALLED:
					case OBSOLETE_UNINSTALLED:
						// leave as-is
						break;
					default:
						throw new RuntimeException("Unhandled status!");
				}
			}
		}

		counter += (int) file.length();
		itemDone(path);
		setCount(counter, total);
	}

	protected void handleQueue() {
		total = 0;
		for (final StringAndFile pair : queue)
			total += (int) pair.file.length();
		counter = 0;
		for (final StringAndFile pair : queue)
			handle(pair);
		done();
		writeCachedChecksums();
	}

	public void updateFromLocal(final List<String> files) {
		queue = new ArrayList<StringAndFile>();
		for (final String file : files)
			queue(file);
		handleQueue();
	}

	protected static boolean tryToGuessPlatform(final FileObject file) {
		// Look for platform names as subdirectories of lib/ and mm/
		String platform;
		if (file.executable) {
			platform = Util.platformForLauncher(file.filename);
			if (platform == null) return false;
		}
		else {
			if (file.filename.startsWith("lib/")) platform =
				file.filename.substring(4);
			else if (file.filename.startsWith("mm/")) platform =
				file.filename.substring(3);
			else return false;

			final int slash = platform.indexOf('/');
			if (slash < 0) return false;
			platform = platform.substring(0, slash);
		}

		if (platform.equals("linux")) platform = "linux32";

		for (final String valid : Util.platforms)
			if (platform.equals(valid)) {
				file.addPlatform(platform);
				return true;
			}
		return false;
	}

	public static final String[][] directories = { { "jars", "retro", "misc" },
		{ ".jar", ".class" }, { "plugins" },
		{ ".jar", ".class", ".txt", ".ijm", ".py", ".rb", ".clj", ".js", ".bsh" },
		{ "scripts" }, { ".py", ".rb", ".clj", ".js", ".bsh", ".m" }, { "macros" },
		{ ".txt", ".ijm" }, { "luts" }, { ".lut" }, { "images" }, { ".png" },
		{ "lib" }, { "" }, { "mm" }, { "" }, { "mmautofocus" }, { "" },
		{ "mmplugins" }, { "" } };

	protected static final Map<String, Set<String>> extensions;

	static {
		extensions = new HashMap<String, Set<String>>();
		for (int i = 0; i < directories.length; i += 2) {
			final Set<String> set = new HashSet<String>();
			for (final String extension : directories[i + 1])
				set.add(extension);
			for (final String dir : directories[i + 1])
				extensions.put(dir, set);
		}
	}

	public static boolean isCandidate(String path) {
		path = path.replace('\\', '/'); // Microsoft time toll
		final int slash = path.indexOf('/');
		if (slash < 0) return Util.isLauncher(path);
		final Set<String> exts = extensions.get(path.substring(0, slash));
		final int dot = path.lastIndexOf('.');
		return exts == null || dot < 0 ? false : exts.contains(path.substring(dot));
	}

	protected void initializeQueue() {
		queue = new ArrayList<StringAndFile>();

		for (final String launcher : Util.launchers)
			queueIfExists(launcher);

		for (int i = 0; i < directories.length; i += 2)
			queueDir(directories[i], directories[i + 1]);

		final Set<String> alreadyQueued = new HashSet<String>();
		for (final StringAndFile pair : queue)
			alreadyQueued.add(pair.path);
		for (final FileObject file : files)
			if (!alreadyQueued.contains(file.getFilename())) queue(file.getFilename());
	}

	public void updateFromLocal() {
		initializeQueue();
		handleQueue();
	}

	protected void readCachedChecksums() {
		cachedChecksums = new TreeMap<String, FileObject.Version>();
		final File file = files.prefix(".checksums");
		if (!file.exists()) return;
		try {
			final BufferedReader reader = new BufferedReader(new FileReader(file));
			String line;
			while ((line = reader.readLine()) != null)
				try {
					final int space = line.indexOf(' ');
					if (space < 0) continue;
					final String checksum = line.substring(0, space);
					final int space2 = line.indexOf(' ', space + 1);
					if (space2 < 0) continue;
					final long timestamp =
						Long.parseLong(line.substring(space + 1, space2));
					final String filename = line.substring(space2 + 1);
					cachedChecksums.put(filename, new FileObject.Version(checksum,
						timestamp));
				}
				catch (final NumberFormatException e) {
					/* ignore line */
				}
			reader.close();
		}
		catch (final IOException e) {
			// ignore
		}
	}

	protected void writeCachedChecksums() {
		if (cachedChecksums == null) return;
		final File file = files.prefix(".checksums");
		// file.canWrite() not applicable, as the file need not exist
		try {
			final Writer writer = new FileWriter(file);
			for (final String filename : cachedChecksums.keySet())
				if (files.prefix(filename).exists()) {
					final FileObject.Version version = cachedChecksums.get(filename);
					writer.write(version.checksum + " " + version.timestamp + " " +
						filename + "\n");
				}
			writer.close();
		}
		catch (final IOException e) {
			// ignore
		}
	}

	protected String getDigest(final String path, final File file,
		final long timestamp) throws IOException, NoSuchAlgorithmException,
		ZipException
	{
		if (cachedChecksums == null) readCachedChecksums();
		final FileObject.Version version = cachedChecksums.get(path);
		if (version != null && timestamp == version.timestamp) return version.checksum;
		final String checksum = Util.getDigest(path, file);
		cachedChecksums.put(path, new FileObject.Version(checksum, timestamp));
		return checksum;
	}
}
