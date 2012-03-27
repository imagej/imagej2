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

package imagej.updater.util;

import imagej.util.FileUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

	public static String MAIN_URL = "http://update.imagej.net/";
	public static String UPDATE_DIRECTORY = "/home/imagej/update-site";
	public static String SSH_HOST = "update.imagej.net";

	public static final String XML_COMPRESSED = "db.xml.gz";

	// Prefix for the preference key names
	public static final String PREFS_USER = "imagej.updater.login";

	public final static String macPrefix = "Contents/MacOS/";

	public final static String platform;
	public final static String[] platforms, launchers;
	protected final static Set<String> updateablePlatforms;

	static {
		// TODO: since this is all dependent on the ijRoot, don't make it static.
		final File imagejRoot = FileUtils.getImageJDirectory();
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
		if (new File(imagejRoot, launchers[macIndex]).exists()) updateablePlatforms
			.add("macosx");
		final String[] files = imagejRoot.list();
		for (final String name : files == null ? new String[0] : files)
			if (name.startsWith("ImageJ-")) updateablePlatforms
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
	public static String getDigest(final String path, final File file)
		throws NoSuchAlgorithmException, FileNotFoundException, IOException,
		UnsupportedEncodingException
	{
		if (path.endsWith(".jar")) return getJarDigest(file);
		final MessageDigest digest = getDigest();
		digest.update(path.getBytes("ASCII"));
		if (file != null) updateDigest(new FileInputStream(file), digest);
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

	public static String getJarDigest(final File file)
		throws FileNotFoundException, IOException
	{
		MessageDigest digest = null;
		try {
			digest = getDigest();
		}
		catch (final NoSuchAlgorithmException e) {
			throw new RuntimeException(e);
		}

		if (file != null) {
			final JarFile jar = new JarFile(file);
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

	public static long getTimestamp(final File file) {
		final long modified = file.lastModified();
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

	public static boolean isLauncher(final String filename) {
		return Arrays.binarySearch(launchers, filename) >= 0;
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

	// Get entire byte data
	public static byte[] readStreamAsBytes(final InputStream input)
		throws IOException
	{
		byte[] buffer = new byte[1024];
		int offset = 0, len = 0;
		for (;;) {
			if (offset == buffer.length) buffer = realloc(buffer, 2 * buffer.length);
			len = input.read(buffer, offset, buffer.length - offset);
			if (len < 0) return realloc(buffer, offset);
			offset += len;
		}
	}

	private static byte[] realloc(final byte[] buffer, final int newLength) {
		if (newLength == buffer.length) return buffer;
		final byte[] newBuffer = new byte[newLength];
		System.arraycopy(buffer, 0, newBuffer, 0, Math
			.min(newLength, buffer.length));
		return newBuffer;
	}

	protected static String readFile(final File file) throws IOException {
		final StringBuilder builder = new StringBuilder();
		final BufferedReader reader = new BufferedReader(new FileReader(file));
		for (;;) {
			final String line = reader.readLine();
			if (line == null) break;
			builder.append(line).append('\n');
		}
		reader.close();

		return builder.toString();
	}

	// This method writes to a .bup file and then renames; this might not work on
	// Windows
	protected static void writeFile(final File file, final String contents)
		throws IOException
	{
		final File result =
			new File(file.getAbsoluteFile().getParentFile(), file.getName() + ".new");
		final FileOutputStream out = new FileOutputStream(result);
		out.write(contents.getBytes());
		out.close();
		result.renameTo(file);
	}

	public static boolean patchInfoPList(final File infoPList, final String executable)
		throws IOException
	{
		if (!infoPList.exists()) return false;
		String contents = readFile(infoPList);
		final Pattern pattern =
			Pattern.compile(".*<key>CFBundleExecutable</key>[^<]*<string>([^<]*).*",
				Pattern.DOTALL | Pattern.MULTILINE);
		final Matcher matcher = pattern.matcher(contents);
		if (!matcher.matches()) return false;
		contents =
			contents.substring(0, matcher.start(1)) + executable +
				contents.substring(matcher.end(1));
		writeFile(infoPList, contents);
		return true;
	}
}
