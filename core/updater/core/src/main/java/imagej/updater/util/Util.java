//
// Util.java
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

package imagej.updater.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/*
 * Class functionality:
 * Extend from it if you need to
 * - Calculate timestamps of files
 * - Calculate the checksums of files
 * - Get the absolute path (prefix()) of the ImageJ directory
 * - Copy a file over to a particular location
 * - Get details of the Operating System ImageJ application is on
 */
public class Util {

	public static String MAIN_URL = "http://fiji.sc/update/";
	public static String UPDATE_DIRECTORY = "/var/www/update/";
	public static String SSH_HOST = "fiji.sc";

	public static final String XML_COMPRESSED = "db.xml.gz";

	// Prefix for the preference key names
	public static final String PREFS_USER = "fiji.updater.login";

	public final static String macPrefix = "Contents/MacOS/";

	public final static String imagejRoot, platform;
	public final static boolean isDeveloper;
	public final static String[] platforms, launchers;
	protected final static Set<String> updateablePlatforms;

	static {
		final String imagejDir = System.getProperty("imagej.dir");
		final String property =
			imagejDir != null ? imagejDir : System.getProperty("ij.dir");
		if (property != null) imagejRoot = property + File.separator;
		else {
			String path = new Util().getClass().getResource("Util.class").toString();
			if (path.startsWith("jar:")) path = path.substring(4);
			if (path.startsWith("file:")) path = path.substring(5);
			int offset = path.lastIndexOf("/plugins/");
			if (offset < 0) offset = path.lastIndexOf("\\plugins\\");
			if (offset < 0) offset = path.lastIndexOf("/core/updater/core/");
			if (offset < 0) throw new RuntimeException(
				"Could not determine ImageJ directory!");
			imagejRoot = path.substring(0, offset + 1);
		}
		isDeveloper = new File(imagejRoot + "/imagej.c").exists();
		platform = getPlatform();

		platforms =
			new String[] { "linux32", "linux64", "macosx", "tiger", "win32", "win64" };
		final int macIndex = 2;
		Arrays.sort(platforms);

		launchers = platforms.clone();
		for (int i = 0; i < launchers.length; i++)
			launchers[i] =
				(i == macIndex || i == macIndex + 1 ? macPrefix : "") + "ImageJ-" +
					platforms[i] + (platforms[i].startsWith("win") ? ".exe" : "");
		Arrays.sort(launchers);

		updateablePlatforms = new HashSet<String>();
		updateablePlatforms.add(platform);
		if (new File(imagejRoot, launchers[macIndex]).exists() ||
			new File(imagejRoot, macPrefix + "fiji-macosx").exists()) updateablePlatforms
			.add("macosx");
		final String[] files = new File(imagejRoot).list();
		for (final String name : files == null ? new String[0] : files)
			if (name.startsWith("ImageJ-") || name.startsWith("fiji-")) updateablePlatforms
				.add(platformForLauncher(name));
	}

	public static String platformForLauncher(final String fileName) {
		final int dash = fileName.lastIndexOf('-');
		if (dash < 0) return null;
		String name = fileName.substring(dash + 1);
		if (name.endsWith(".exe")) name = name.substring(0, name.length() - 4);
		if (name.equals("tiger") || name.equals("panther")) name = "macosx";
		else if (name.equals("linux")) name = "linux32";
		return name;
	}

	private Util() {} // make sure this class is not instantiated

	public static String stripSuffix(final String string, final String suffix) {
		if (!string.endsWith(suffix)) return string;
		return string.substring(0, string.length() - suffix.length());
	}

	public static String stripPrefix(final String string, final String prefix) {
		if (!string.startsWith(prefix)) return string;
		return string.substring(prefix.length());
	}

	public static String getPlatform() {
		final boolean is64bit =
			System.getProperty("os.arch", "").indexOf("64") >= 0;
		final String osName = System.getProperty("os.name", "<unknown>");
		if (osName.equals("Linux")) return "linux" + (is64bit ? "64" : "32");
		if (osName.equals("Mac OS X")) return "macosx";
		if (osName.startsWith("Windows")) return "win" + (is64bit ? "64" : "32");
		// System.err.println("Unknown platform: " + osName);
		return osName.toLowerCase();
	}

	// get digest of the file as according to fullPath
	public static String getDigest(final String path, final String fullPath)
		throws NoSuchAlgorithmException, FileNotFoundException, IOException,
		UnsupportedEncodingException
	{
		if (path.endsWith(".jar")) return getJarDigest(fullPath);
		final MessageDigest digest = getDigest();
		digest.update(path.getBytes("ASCII"));
		if (fullPath != null) updateDigest(new FileInputStream(fullPath), digest);
		return toHex(digest.digest());
	}

	public static MessageDigest getDigest() throws NoSuchAlgorithmException {
		return MessageDigest.getInstance("SHA-1");
	}

	public static void updateDigest(final InputStream input,
		final MessageDigest digest) throws IOException
	{
		final byte[] buffer = new byte[65536];
		final DigestInputStream digestStream = new DigestInputStream(input, digest);
		while (digestStream.read(buffer) >= 0); /* do nothing */
		digestStream.close();
	}

	public final static char[] hex = { '0', '1', '2', '3', '4', '5', '6', '7',
		'8', '9', 'a', 'b', 'c', 'd', 'e', 'f' };

	public static String toHex(final byte[] bytes) {
		final char[] buffer = new char[bytes.length * 2];
		for (int i = 0; i < bytes.length; i++) {
			buffer[i * 2] = hex[(bytes[i] & 0xf0) >> 4];
			buffer[i * 2 + 1] = hex[bytes[i] & 0xf];
		}
		return new String(buffer);
	}

	public static String getJarDigest(final String path)
		throws FileNotFoundException, IOException
	{
		MessageDigest digest = null;
		try {
			digest = getDigest();
		}
		catch (final NoSuchAlgorithmException e) {
			throw new RuntimeException(e);
		}

		if (path != null) {
			final JarFile jar = new JarFile(path);
			final List<JarEntry> list = Collections.list(jar.entries());
			Collections.sort(list, new JarEntryComparator());

			for (final JarEntry entry : list) {
				digest.update(entry.getName().getBytes("ASCII"));
				updateDigest(jar.getInputStream(entry), digest);
			}
		}
		return toHex(digest.digest());
	}

	private static class JarEntryComparator implements Comparator<JarEntry> {

		@Override
		public int compare(final JarEntry entry1, final JarEntry entry2) {
			final String name1 = entry1.getName();
			final String name2 = entry2.getName();
			return name1.compareTo(name2);
		}

	}

	// Gets the location of specified file when inside of saveDirectory
	public static String
		prefix(final String saveDirectory, final String filename)
	{
		return prefix(saveDirectory + File.separator + filename);
	}

	public static long getTimestamp(final String filename) {
		final String fullPath = prefix(filename);
		final long modified = new File(fullPath).lastModified();
		return Long.parseLong(timestamp(modified));
	}

	public static String timestamp(final long millis) {
		final Calendar date = Calendar.getInstance();
		date.setTimeInMillis(millis);
		return timestamp(date);
	}

	public static String timestamp(final Calendar date) {
		final DecimalFormat format = new DecimalFormat("00");
		final int month = date.get(Calendar.MONTH) + 1;
		final int day = date.get(Calendar.DAY_OF_MONTH);
		final int hour = date.get(Calendar.HOUR_OF_DAY);
		final int minute = date.get(Calendar.MINUTE);
		final int second = date.get(Calendar.SECOND);
		return "" + date.get(Calendar.YEAR) + format.format(month) +
			format.format(day) + format.format(hour) + format.format(minute) +
			format.format(second);
	}

	public static long timestamp2millis(final long timestamp) {
		return timestamp2millis("" + timestamp);
	}

	public static long timestamp2millis(final String timestamp) {
		final Calendar calendar = Calendar.getInstance();
		calendar.set(Integer.parseInt(timestamp.substring(0, 4)), Integer
			.parseInt(timestamp.substring(4, 6)) - 1, Integer.parseInt(timestamp
			.substring(6, 8)), Integer.parseInt(timestamp.substring(8, 10)), Integer
			.parseInt(timestamp.substring(10, 12)), Integer.parseInt(timestamp
			.substring(12, 14)));
		return calendar.getTimeInMillis();
	}

	public static long getFilesize(final String filename) {
		return new File(prefix(filename)).length();
	}

	public static String getDigest(final String filename) {
		try {
			return getDigest(filename, prefix(filename));
		}
		catch (final Exception e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}

	public static String prefix(String path) {
		if (new File(path).isAbsolute()) return path;
		if (File.separator.equals("\\")) path = path.replace("\\", "/");
		return imagejRoot + path;
	}

	public static String prefixUpdate(final String path) {
		return prefix("update/" + path);
	}

	public static boolean fileExists(final String filename) {
		return new File(prefix(filename)).exists();
	}

	public static boolean isLauncher(final String filename) {
		return Arrays.binarySearch(launchers, stripPrefix(filename, imagejRoot)) >= 0;
	}

	public static String[] getLaunchers() {
		if (platform.equals("macosx")) return new String[] {
			macPrefix + "fiji-macosx", macPrefix + "fiji-tiger" };

		int index = Arrays.binarySearch(launchers, "fiji-" + platform);
		if (index < 0) index = -1 - index;
		return new String[] { launchers[index] };
	}

	public static boolean isUpdateablePlatform(final String platform) {
		return updateablePlatforms.contains(platform);
	}

	public static boolean isMacOSX() {
		return platform.equals("macosx");
	}

	public static <T> String join(final String delimiter, final Iterable<T> list)
	{
		final StringBuilder builder = new StringBuilder();
		for (final T object : list)
			builder.append((builder.length() > 0 ? ", " : "") + object.toString());
		return builder.toString();
	}

	public static void useSystemProxies() {
		System.setProperty("java.net.useSystemProxies", "true");
	}

	public static long getLastModified(final String url) {
		try {
			final URLConnection connection = new URL(url).openConnection();
			if (connection instanceof HttpURLConnection) ((HttpURLConnection) connection)
				.setRequestMethod("HEAD");
			connection.setUseCaches(false);
			final long lastModified = connection.getLastModified();
			connection.getInputStream().close();
			return lastModified;
		}
		catch (final IOException e) {
			if (e.getMessage().startsWith("Server returned HTTP response code: 407")) return -111381;
			// assume no network; so let's pretend everything's ok.
			return -1;
		}
	}

}
