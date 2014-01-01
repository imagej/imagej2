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
 * 
 * The views and conclusions contained in the software and documentation are
 * those of the authors and should not be interpreted as representing official
 * policies, either expressed or implied, of any organization.
 * #L%
 */

package imagej.updater.core;

import imagej.updater.core.Conflicts.Conflict;
import imagej.updater.core.Conflicts.Resolution;
import imagej.updater.core.Conflicts.Conflict.Severity;
import imagej.updater.core.FileObject.Status;
import imagej.updater.util.AbstractProgressable;
import imagej.updater.util.Progress;
import imagej.updater.util.Util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
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
public class Checksummer extends AbstractProgressable {

	private FilesCollection files;
	private int counter, total;
	private Map<String, FileObject.Version> cachedChecksums;
	private boolean isWindows; // time tax for Redmont
	private Map<String, List<StringAndFile>> queue;

	public Checksummer(final FilesCollection files, final Progress progress) {
		this.files = files;
		if (progress != null) addProgress(progress);
		setTitle("Checksummer");
		isWindows = Util.getPlatform().startsWith("win");
	}

	protected static class StringAndFile {

		private String path;
		private File file;
		public long timestamp;
		public String checksum;

		protected StringAndFile(final String path, final File file) {
			this.path = path;
			this.file = file;
		}

		@Override
		public String toString() {
			return "{" + path + " - " + file + "}";
		}
	}

	public Map<String, FileObject.Version> getCachedChecksums() {
		return cachedChecksums;
	}

	/* follows symlinks */
	protected boolean exists(final File file) {
		try {
			return file.getCanonicalFile().exists();
		}
		catch (final IOException e) {
			files.log.error(e);
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
		String unversioned = FileObject.getFilename(path, true);
		if (!queue.containsKey(unversioned))
			queue.put(unversioned, new ArrayList<StringAndFile>());
		queue.get(unversioned).add(new StringAndFile(path, file));
	}

	/**
	 * Handle a single component, adding conflicts if there are multiple
	 * versions.
	 *
	 * @param unversioned
	 *            the unversioned name of the component
	 */
	protected void handle(final String unversioned) {
		final List<StringAndFile> pairs = queue.get(unversioned);
		for (final StringAndFile pair : pairs) {
			addItem(pair.path);

			if (pair.file.exists()) try {
				pair.timestamp = Util.getTimestamp(pair.file);
				pair.checksum = getDigest(pair.path, pair.file, pair.timestamp);
			}
			catch (final ZipException e) {
				files.log.error("Problem digesting " + pair.file);
			}
			catch (final Exception e) {
				files.log.error(e);
			}

			counter += (int) pair.file.length();
			itemDone(pair.path);
			setCount(counter, total);
		}

		if (pairs.size() == 1) {
			handle(pairs.get(0));
			return;
		}

		// there are multiple versions of the same component;
		StringAndFile pair = null;
		FileObject object = files.get(unversioned);
		if (object == null || object.isObsolete()) {
			// the component is local-only; fall back to using the newest one
			for (StringAndFile p : pairs)
				if (pair == null || (p.file.lastModified() > pair.file.lastModified()
						&& pair.path.equals(FileObject.getFilename(pair.path, true))))
					pair = p;
			final List<File> obsoletes = new ArrayList<File>();
			for (StringAndFile p : pairs)
				if (p != pair)
					obsoletes.add(p.file);
			if (pair != null) addConflict(pair.path, "", false, obsoletes);
		} else {
			// let's find out whether there are obsoletes or locally-modified versions
			final List<StringAndFile> upToDates = new ArrayList<StringAndFile>();
			final List<StringAndFile> obsoletes = new ArrayList<StringAndFile>();
			final List<StringAndFile> locallyModifieds = new ArrayList<StringAndFile>();
			for (final StringAndFile p : pairs) {
				if (object.current.checksum.equals(p.checksum))
					upToDates.add(p);
				else if (object.hasPreviousVersion(p.checksum))
					obsoletes.add(p);
				else
					locallyModifieds.add(p);
			}
			Comparator<StringAndFile> comparator = new Comparator<StringAndFile>() {
				@Override
				public int compare(StringAndFile a, StringAndFile b) {
					long diff = a.file.lastModified() - b.file.lastModified();
					return diff < 0 ? +1 : diff > 0 ? -1 : 0;
				}
			};
			Collections.sort(upToDates, comparator);
			Collections.sort(obsoletes, comparator);
			Collections.sort(locallyModifieds, comparator);
			if (upToDates.size() > 0)
				pair = pickNewest(upToDates);
			else if (obsoletes.size() > 0)
				pair = pickNewest(obsoletes);
			else
				pair = pickNewest(locallyModifieds);
			if (locallyModifieds.size() > 0)
				addConflict(pair.path, "locally-modified", true, convert(locallyModifieds));
			if (obsoletes.size() > 0)
				addConflict(pair.path, "obsolete", false, convert(obsoletes));
			if (upToDates.size() > 0)
				addConflict(pair.path, "up-to-date", false, convert(upToDates));
		}
		handle(pair);
	}

	protected static StringAndFile pickNewest(final List<StringAndFile> list) {
		int index = 0;
		if (list.size() > 1) {
			final String filename = list.get(0).path;
			if (filename.equals(FileObject.getFilename(filename, true)))
				index++;
		}

		final StringAndFile result = list.get(index);
		list.remove(index);
		return result;
	}

	protected static List<File> convert(final List<StringAndFile> pairs) {
		final List<File> result = new ArrayList<File>();
		for (final StringAndFile pair : pairs)
			result.add(pair.file);
		return result;
	}

	protected void addConflict(final String filename, String adjective, boolean isCritical, final List<File> toDelete) {
		if (!adjective.equals("") && !adjective.endsWith(" "))
			adjective += " ";
		String conflictMessage = "Multiple " + adjective + "versions of " + filename + " exist: " + Util.join(", ", toDelete);
		Resolution ignore = new Resolution("Ignore for now") {
			@Override
			public void resolve() {
				removeConflict(filename);
			}
		};
		Resolution delete = new Resolution("Delete!") {
			@Override
			public void resolve() {
				for (final File file : toDelete)
					file.delete();
				removeConflict(filename);
			}
		};
		files.conflicts.add(new Conflict(isCritical ? Severity.CRITICAL_ERROR : Severity.ERROR,
				filename, conflictMessage, ignore, delete));
	}

	protected void removeConflict(final String filename) {
		if (files.conflicts == null)
			return;
		Iterator<Conflict> iterator = files.conflicts.iterator();
		while (iterator.hasNext()) {
			Conflict conflict = iterator.next();
			if (conflict.filename.equals(filename)) {
				iterator.remove();
				return;
			}
		}
	}

	protected void handle(final StringAndFile pair) {
		if (pair.checksum != null) {
			FileObject object = files.get(pair.path);
			if (object == null) {
				object =
					new FileObject(null, pair.path, pair.file.length(), pair.checksum, pair.timestamp,
						Status.LOCAL_ONLY);
				object.localFilename = pair.path;
				object.localChecksum = pair.checksum;
				object.localTimestamp = pair.timestamp;
				if ((!isWindows && Util.canExecute(pair.file)) || pair.path.endsWith(".exe")) object.executable =
					true;
				tryToGuessPlatform(object);
				files.add(object);
			}
			else {
				final FileObject.Version obsoletes =
						cachedChecksums.get(":" + pair.checksum);
				if (!object.hasPreviousVersion(pair.checksum)) {
					if (obsoletes != null) {
						for (final String obsolete : obsoletes.checksum.split(":")) {
							if (object.hasPreviousVersion(obsolete)) {
								pair.checksum = obsolete;
								break;
							}
						}
					}
				} else if (object.current != null && obsoletes != null
						&& (":" + obsoletes.checksum + ":").indexOf(":" + object.current.checksum + ":") >= 0) {
					// if the recorded checksum is an obsolete equivalent of the current one, use the obsolete one
					pair.checksum = object.current.checksum;
				}
				object.setLocalVersion(pair.path, pair.checksum, pair.timestamp);
				if (object.getStatus() == Status.OBSOLETE_UNINSTALLED) object
					.setStatus(Status.OBSOLETE);
			}
			if (pair.path.endsWith((".jar"))) try {
				POMParser.fillMetadataFromJar(object, pair.file);
			} catch (Exception e) {
				files.log.error("Could not read pom.xml from " + pair.path);
			}
		}
		else {
			final FileObject object = files.get(pair.path);
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
						files.remove(pair.path);
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
	}

	protected void handleQueue() {
		total = 0;
		for (final String unversioned : queue.keySet())
			for (final StringAndFile pair : queue.get(unversioned))
				total += (int) pair.file.length();
		counter = 0;
		for (final String unversioned : queue.keySet())
			handle(unversioned);
		done();
		writeCachedChecksums();
	}

	public void updateFromLocal(final List<String> files) {
		queue = new LinkedHashMap<String, List<StringAndFile>>();
		for (final String file : files)
			queue(file);
		handleQueue();
	}

	protected boolean tryToGuessPlatform(final FileObject file) {
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

		for (final String valid : files.util.platforms)
			if (platform.equals(valid)) {
				file.addPlatform(platform);
				return true;
			}
		return false;
	}

	public static final String[][] directories = {
		{ "jars", "retro", "misc" }, { ".jar", ".class" },
		{ "plugins" }, { ".jar", ".class", ".txt", ".ijm", ".py", ".rb", ".clj", ".js", ".bsh" },
		{ "scripts" }, { ".py", ".rb", ".clj", ".js", ".bsh", ".m" },
		{ "macros" }, { ".txt", ".ijm" },
		{ "luts" }, { ".lut" },
		{ "images" }, { ".png" },
		{ "Contents" }, { ".icns", ".plist" },
		{ "lib" }, { "" },
		{ "mm" }, { "" },
		{ "mmautofocus" }, { "" },
		{ "mmplugins" }, { "" }
	};

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

	public boolean isCandidate(String path) {
		path = path.replace('\\', '/'); // Microsoft time toll
		final int slash = path.indexOf('/');
		if (slash < 0) return files.util.isLauncher(path);
		final Set<String> exts = extensions.get(path.substring(0, slash));
		final int dot = path.lastIndexOf('.');
		return exts == null || dot < 0 ? false : exts.contains(path.substring(dot));
	}

	protected void initializeQueue() {
		queue = new LinkedHashMap<String, List<StringAndFile>>();

		for (final String launcher : files.util.launchers)
			queueIfExists(launcher);

		for (int i = 0; i < directories.length; i += 2)
			queueDir(directories[i], directories[i + 1]);

		for (final FileObject file : files)
			if (!queue.containsKey(file.getFilename(true)))
				queue(file.getFilename());
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
				if (filename.startsWith(":") || files.prefix(filename).exists()) {
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
		FileObject.Version version = cachedChecksums.get(path);
		if (version == null || timestamp != version.timestamp) {
			final String checksum = path.equals("plugins/Fiji_Updater.jar") ?
				Util.getJarDigest(file, false, false, false) :
				Util.getDigest(path, file);
			version = new FileObject.Version(checksum, timestamp);
			cachedChecksums.put(path, version);
		}
		if (!cachedChecksums.containsKey(":" + version.checksum)) {
			final List<String> obsoletes = Util.getObsoleteDigests(path, file);
			if (obsoletes != null) {
				final StringBuilder builder = new StringBuilder();
				for (final String obsolete : obsoletes) {
					if (builder.length() > 0) builder.append(':');
					builder.append(obsolete);
				}
				cachedChecksums.put(":" + version.checksum, new FileObject.Version(
					builder.toString(), timestamp));
			}
		}
		return version.checksum;
	}
}
