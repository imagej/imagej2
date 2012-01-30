//
// Checksummer.java
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

import imagej.updater.core.PluginObject.Status;
import imagej.updater.util.Progress;
import imagej.updater.util.Progressable;
import imagej.updater.util.Util;

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
 * A class to checksum all the files shown in the Updater's UI.
 *
 * 1st step: Get information of local plugins (checksums and version)
 * 2nd step: Given XML file, get information of latest updatable files (checksums
 * and version)
 * 3rd step: Build up list of "PluginObject" using both local and updates
 *
 * digests and dates hold checksums and versions of local plugins respectively
 * latestDigests and latestDates hold checksums and versions of latest file versions
 * 
 * @author Johannes Schindelin
 * @author Yap Chin Kiet
 */
public class Checksummer extends Progressable {

	protected PluginCollection plugins;
	protected int counter, total;
	protected Map<String, PluginObject.Version> cachedChecksums;

	public Checksummer(final PluginCollection plugins, final Progress progress) {
		this.plugins = plugins;
		addProgress(progress);
		setTitle("Czechsummer");
	}

	protected static class StringPair {

		String path, realPath;

		StringPair(final String path, final String realPath) {
			this.path = path;
			this.realPath = realPath;
		}
	}

	public Map<String, PluginObject.Version> getCachedChecksums() {
		return cachedChecksums;
	}

	protected List<StringPair> queue;
	protected String imagejRoot;

	/* follows symlinks */
	protected boolean exists(final File file) {
		try {
			return file.getCanonicalFile().exists();
		}
		catch (final IOException e) {
			e.printStackTrace();
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
		File file = new File(prefix(dir));
		if (!exists(file)) return;
		for (final String item : file.list()) {
			final String path = dir + "/" + item;
			file = new File(prefix(path));
			if (item.startsWith(".")) continue;
			if (file.isDirectory()) {
				queueDir(path, extensions);
				continue;
			}
			if (!extensions.contains("")) {
				final int dot = item.lastIndexOf('.');
				if (dot < 0 || !extensions.contains(item.substring(dot))) continue;
			}
			if (exists(file)) queue(path, file.getAbsolutePath());
		}
	}

	protected void queueIfExists(final String path) {
		final String realPath = prefix(path);
		if (exists(new File(realPath))) queue(path, realPath);
	}

	protected void queue(final String path) {
		queue(path, prefix(path));
	}

	protected String prefix(final String path) {
		return imagejRoot == null ? Util.prefix(path) : imagejRoot + path;
	}

	protected void queue(final String path, final String realPath) {
		queue.add(new StringPair(path, realPath));
	}

	protected void handle(final StringPair pair) {
		final String path = pair.path;
		final String realPath = Util.prefix(pair.realPath);
		addItem(path);

		String checksum = null;
		long timestamp = 0;
		if (new File(realPath).exists()) try {
			timestamp = Util.getTimestamp(realPath);
			checksum = getDigest(path, realPath, timestamp);

			PluginObject plugin = plugins.getPlugin(path);
			if (plugin == null) {
				if (checksum == null) throw new RuntimeException("Tried to remove " +
					path + ", which is not known to the Updater");
				if (imagejRoot == null) {
					plugin =
						new PluginObject(null, path, checksum, timestamp, Status.LOCAL_ONLY);
					tryToGuessPlatform(plugin);
				}
				else {
					plugin = new PluginObject(null, path, null, 0, Status.OBSOLETE);
					plugin.addPreviousVersion(checksum, timestamp);
					// for re-upload
					plugin.newChecksum = checksum;
					plugin.newTimestamp = timestamp;
				}
				plugins.add(plugin);
			}
			else if (checksum != null) {
				plugin.setLocalVersion(checksum, timestamp);
				if (plugin.getStatus() == Status.OBSOLETE_UNINSTALLED) plugin
					.setStatus(Status.OBSOLETE);
			}
		}
		catch (final ZipException e) {
			System.err.println("Problem digesting " + realPath);
		}
		catch (final Exception e) {
			e.printStackTrace();
		}

		counter += (int) Util.getFilesize(realPath);
		itemDone(path);
		setCount(counter, total);
	}

	protected void handleQueue() {
		total = 0;
		for (final StringPair pair : queue)
			total += Util.getFilesize(pair.realPath);
		counter = 0;
		for (final StringPair pair : queue)
			handle(pair);
		done();
		writeCachedChecksums();
	}

	public void updateFromLocal(final List<String> files) {
		queue = new ArrayList<StringPair>();
		for (final String file : files)
			queue(file);
		handleQueue();
	}

	public void updateFromPreviousInstallation(final String imagejRoot) {
		if (!Util.isDeveloper) throw new RuntimeException("Must be developer");
		this.imagejRoot = new File(imagejRoot).getAbsolutePath() + "/";
		updateFromLocal();
		for (final PluginObject plugin : plugins)
			if (plugin.isLocallyModified()) plugin.addPreviousVersion(
				plugin.newChecksum, plugin.newTimestamp);
	}

	protected static boolean tryToGuessPlatform(final PluginObject plugin) {
		// Look for platform names as subdirectories of lib/ and mm/
		String platform;
		if (plugin.filename.startsWith("lib/")) platform =
			plugin.filename.substring(4);
		else if (plugin.filename.startsWith("mm/")) platform =
			plugin.filename.substring(3);
		else return false;

		final int slash = platform.indexOf('/');
		if (slash < 0) return false;
		platform = platform.substring(0, slash);

		if (platform.equals("linux32")) platform = "linux";
		for (final String valid : Util.platforms)
			if (platform.equals(valid)) {
				plugin.addPlatform(platform);
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
		queue = new ArrayList<StringPair>();

		for (final String launcher : Util.launchers)
			queueIfExists(launcher);

		for (int i = 0; i < directories.length; i += 2)
			queueDir(directories[i], directories[i + 1]);
	}

	public void updateFromLocal() {
		initializeQueue();
		handleQueue();
	}

	protected void readCachedChecksums() {
		cachedChecksums = new TreeMap<String, PluginObject.Version>();
		final File file = new File(Util.prefix(".checksums"));
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
					cachedChecksums.put(filename, new PluginObject.Version(checksum,
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
		final File file = new File(Util.prefix(".checksums"));
		// file.canWrite() not applicable, as the file need not exist
		try {
			final Writer writer = new FileWriter(file);
			for (final String filename : cachedChecksums.keySet())
				if (new File(Util.prefix(filename)).exists()) {
					final PluginObject.Version version = cachedChecksums.get(filename);
					writer.write(version.checksum + " " + version.timestamp + " " +
						filename + "\n");
				}
			writer.close();
		}
		catch (final IOException e) {
			// ignore
		}
	}

	protected String getDigest(final String path, final String realPath,
		final long timestamp) throws IOException, NoSuchAlgorithmException,
		ZipException
	{
		if (cachedChecksums == null) readCachedChecksums();
		final PluginObject.Version version = cachedChecksums.get(path);
		if (version != null && timestamp == version.timestamp) return version.checksum;
		final String checksum = Util.getDigest(path, realPath);
		cachedChecksums.put(path, new PluginObject.Version(checksum, timestamp));
		return checksum;
	}
}
