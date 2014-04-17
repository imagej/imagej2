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
 * #L%
 */

package imagej.util;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * This program mirrors a given website.
 * <p>
 * Its primary purpose is to provide the code necessary to keep <a
 * href="http://mirror.imagej.net/">ImageJ Mirror</a> up-to-date.
 * </p>
 * 
 * @author Johannes Schindelin
 */
public class MirrorWebsite {
	public final static int THREAD_COUNT = 20;
	public final static long DELAY_IN_MICROSECONDS = 0;
	private String baseURL;
	private String basePath; // the local directory for file:// baseURL, otherwise null
	private File localDirectory;
	private Map<String, String> linkMap = new HashMap<String, String>();
	private Set<String> missingLinks = new LinkedHashSet<String>();
	private ExecutorService executorService;
	private Map<String, MirrorJob> jobs;
	private Set<String> done;
	private int threadCount;
	private long delay;

	public MirrorWebsite(final String baseURL, final File localDirectory,
			final int threadCount, final long delay) {
		this.baseURL = baseURL + (baseURL.endsWith("/") ? "" : "/");
		this.basePath = baseURL.startsWith("file:") ? baseURL.substring(5) : null;
		this.localDirectory = localDirectory;
		this.threadCount = threadCount;
		this.delay = delay;
	}

	public void run() throws InterruptedException {
		synchronized (this) {
			if (jobs != null)
				throw new RuntimeException("Mirroring already in progress!");

			executorService = Executors.newFixedThreadPool(threadCount);
			done = new TreeSet<String>();
			jobs = new LinkedHashMap<String, MirrorJob>();

			mirror("index.html");
		}
		executorService.awaitTermination(Long.MAX_VALUE, TimeUnit.SECONDS);
	}

	public void mirror(String path) {
		final MirrorJob job;
		synchronized (this) {
			if (jobs.containsKey(path)) return;
			job = new MirrorJob(path);
			jobs.put(path, job);
		}
		try {
			executorService.execute(job);
		} catch (Throwable t) {
			t.printStackTrace();
			done.add(path);
		}
	}

	private static long getRemoteTimestamp(String url) throws IOException {
		URLConnection connection = null;
		try {
			connection = new URL(url).openConnection();
		} catch (FileNotFoundException e) {
			if (url.endsWith("/index.html"))
				connection = new URL(url.substring(0, url.length() - 10)).openConnection();
			else
				throw e;
		}
		if (connection instanceof HttpURLConnection)
			((HttpURLConnection)connection).setRequestMethod("HEAD");
		connection.setUseCaches(false);
		long lastModified = connection.getLastModified();
		connection.getInputStream().close();
		return lastModified;
	}

	// returns 0 if it is up-to-date, otherwise the desired lastModified
	private long upToDate(String path) throws IOException {
		long remote = getRemoteTimestamp(baseURL + path);
		File file = new File(localDirectory, path);
		if (!file.exists())
			return remote;
		long local = file.lastModified();
		return remote < 0 || local == remote ? 0 : remote;
	}

	private String getValue(String html, int startOffset) {
		int offset = startOffset;
		while (offset < html.length() &&
				(html.charAt(offset) == '\n' || html.charAt(offset) == '\r' || html.charAt(offset) == ' '))
			offset++;

		if (offset + 1 >= html.length())
			return "";

		char delim = ' ', delim2 = '>';
		char c = html.charAt(offset);
		if (c == '"' || c == '\'') {
			delim = delim2 = c;
			offset++;
		}

		for (int end = offset; end < html.length(); end++)
			if (html.charAt(end) == delim || html.charAt(end) == delim2)
				return html.substring(offset, end);
		return html.substring(offset);
	}

	private void addLinkRelation(List<String> result, String sourceURL, String url) {
		String normalized = normalizeURL(url);
		if (normalized == null)
			return;
		result.add(normalized);
		synchronized(linkMap) {
			String previous = linkMap.get(normalized);
			if (previous == null)
				linkMap.put(normalized, sourceURL);
			else if ((" " + previous + " ").indexOf(" " + sourceURL + " ") < 0)
				linkMap.put(normalized, previous + " " + sourceURL);
		}
	}

	private List<String> getLinks(String relativePath, String path, String html) {
		List<String> result = new ArrayList<String>();

		int offset = -1;
		for (;;) {
			int newOffset = -1;
			for (String pattern : new String[] { " href=", " src=", " HREF=", " SRC=" }) {
				int tmp = html.indexOf(pattern, offset + 1);
				if (tmp >= 0 && (newOffset < 0 || newOffset > tmp))
					newOffset = tmp + pattern.length();
			}
			if (newOffset < 0)
				break;
			offset = newOffset;

			String value = getValue(html, offset);
			offset += value.length();

			if (value.startsWith("mailto:") || value.startsWith("MAILTO:"))
				continue;

			for (char c : new char[] { '#', '?', ';' }) {
				int hash = value.indexOf(c);
				if (hash >= 0)
					value = value.substring(0, hash);
			}

			if (value.endsWith("/"))
				value += "index.html";
			if (value.startsWith("/")) {
				int colon = baseURL.indexOf("://");
				int slash = baseURL.indexOf('/', colon + 3);
				value = baseURL.substring(0, slash) + value;
			}
			else if (value.indexOf("://") < 0) {
				if (!value.equals(""))
					addLinkRelation(result, path, relativePath + value);
				if (offset < 0)
					break;
				continue;
			}
			if (value.startsWith(baseURL))
				addLinkRelation(result, path, value.substring(baseURL.length()));
			if (offset < 0)
				break;
		}

		return result;
	}

	private static boolean isHTML(String path) {
		String lower = path.toLowerCase();
		return lower.endsWith(".htm") || lower.endsWith(".html");
	}

	private static void copyStream(InputStream in, StringBuffer string, OutputStream out) throws IOException {
		byte[] buffer = new byte[65536];
		for (;;) {
			int count = in.read(buffer);
			if (count < 0)
				break;
			if (string != null)
				string.append(new String(buffer, 0, count));
			if (out != null)
				out.write(buffer, 0, count);
		}
		in.close();
		if (out != null)
			out.close();
	}

	private List<String> ensureUptodate(String path) throws IOException {
		StringBuffer string = new StringBuffer();
		String relativePath = path.substring(0, path.lastIndexOf('/') + 1);
		File file = new File(localDirectory, path);

		// special-case local case: file:/.../ does not list the directory contents
		if (basePath != null && ("/" + path).endsWith("/index.html") && !new File(basePath + path).exists()) {
			final String directory = path.substring(0, path.length() - 10);
			final File[] list = new File(basePath + directory).listFiles();
			if (list == null) return Collections.emptyList();
			final List<String> result = new ArrayList<String>();
			for (final File item : list) {
				if (item.isDirectory()) result.add(directory + item.getName() + "/index.html");
				else result.add(directory + item.getName());
			}
			return result;
		}

		long remoteLastModified;
		try {
			remoteLastModified = upToDate(path);
			if (remoteLastModified == 0) {
				if (!isHTML(path))
					return Collections.emptyList();
				copyStream(new FileInputStream(file), string, null);
				return getLinks(relativePath, path, string.toString());
			}
		} catch (FileNotFoundException e) {
			if (!path.endsWith("/index.html"))
				throw e;
			remoteLastModified = -1;
		}

		InputStream in = null;
		try {
			in = new URL(baseURL + path).openStream();
		}
		catch (MalformedURLException e) {
			throw new MalformedURLException(baseURL + path);
		}
		catch (FileNotFoundException e) {
			if (path.endsWith("/index.html"))
				in = new URL(baseURL + path.substring(0, path.length() - 10)).openStream();
			else
				throw e;
		}
		System.err.println("Downloading " + path);
		File tmp = new File(localDirectory, path + ".download.tmp");
		tmp.getParentFile().mkdirs();
		FileOutputStream out = new FileOutputStream(tmp);
		if (isHTML(path)) {
			copyStream(in, string, null);
			String rewritten = string.toString()
				.replaceAll("http://rsb.info.nih.gov",
					"http://imagej.nih.gov");
			String replacement = "", path2 = path;
			for (;;) {
				path2 = path2.substring(0, path2.lastIndexOf('/') + 1);
				rewritten = rewritten.replaceAll(baseURL + path2, replacement);
				// special-case rewriting from a local mirror
				if (basePath != null) {
					rewritten = rewritten.replaceAll("http://imagej.nih.gov/ij/" + path2, replacement);
				}
				if (path2.equals(""))
					break;
				// strip trailing slash
				path2 = path2.substring(0, path2.length() - 1);
				replacement = "../" + replacement;
			}
			copyStream(new ByteArrayInputStream(rewritten.getBytes()),
				null, out);
		}
		else
			copyStream(in, null, out);

		tmp.renameTo(file);
		if (remoteLastModified >= 0)
			file.setLastModified(remoteLastModified);

		if (!isHTML(path))
			return Collections.emptyList();
		return getLinks(relativePath, path, string.toString());
	}

	private static String normalizeURL(String originalPath) {
		String path = originalPath;
		for (;;) {
			int dot = path.indexOf("/./");
			if (dot >= 0) {
				path = path.substring(0, dot) + path.substring(dot + 2);
				continue;
			}
			int dotdot = path.indexOf("/../");
			if (dotdot < 0)
				break;
			if (dotdot == 0)
				return null;
			int slash = path.lastIndexOf(dotdot - 1);
			if (slash < 0)
				return null;
			path = path.substring(0, slash) + path.substring(dotdot + 3);
		}
		if (path.startsWith("../"))
			throw new RuntimeException("ignore");
		return path;
	}

	private void reportMissingLinks() {
		if (missingLinks.size() == 0) return;
		System.err.println("Found broken links:");
		for (final String path : missingLinks) {
			final String source = linkMap.get(path);
			System.err.println(path + (source == null ? "" : " (linked from " + source + ")"));
		}
	}

	private class MirrorJob implements Runnable {
		private String path;

		public MirrorJob(String path) {
			this.path = path;
		}

		@Override
		public void run() {
			try {
				System.err.println("Looking at " + path + " (" + (1 + done.size()) + "/" + jobs.size() + ")");
				for (String path2 : ensureUptodate(path)) try {
					mirror(path2);
				}
				catch (Throwable e) {
					System.err.println("" + e);
				}
			}
			catch (FileNotFoundException e) {
				String source = linkMap.get(path);
				System.err.println("" + e + (source == null ? "" : " (linked from " + source + ")"));
				missingLinks.add(path);
			}
			catch (Throwable e) {
				System.err.println("Error while trying to mirror " + path);
				e.printStackTrace();
			}
			if (delay > 0) try {
				Thread.sleep(delay);
			} catch (InterruptedException e) {
				// ignore
			}
			synchronized (MirrorWebsite.this) {
				done.add(path);
				if (done.size() == jobs.size()) {
					executorService.shutdown();
					reportMissingLinks();
				}
			}
		}
	}

	private static void usage() {
		System.err.println("Usage: MirrorWebsite [<option>...] <url> <directory>");
		System.err.println("Options:");
		System.err.println("--help");
		System.err.println("\tshow this help");
		System.err.println("--threads <n>");
		System.err.println("\tuse <n> threads (default: " + THREAD_COUNT + ")");
		System.err.println("--delay <microseconds>");
		System.err.println("\twait after each request (default: " + DELAY_IN_MICROSECONDS + ")");
		System.exit(1);
	}

	public static void main(String[] args) {
		int threadCount = THREAD_COUNT;
		long delay = DELAY_IN_MICROSECONDS;

		// option parsing
		int i = 0;
		while (i < args.length) {
			if (!args[i].startsWith("--"))
				break;
			final String option = args[i++];
			if (option.equals("--"))
				break;
			// no-arg options
			if (option.equals("--help"))
				usage();
			// one-arg options
			if (i + 1 >= args.length) {
				System.err.println("Missing argument: " + option);
				usage();
			}
			final String arg = args[i++];
			if (option.equals("--threads"))
				threadCount = Integer.parseInt(arg);
			else if (option.equals("--delay"))
				delay = Long.parseLong(arg);
			else {
				System.err.println("Unknown option: " + option);
				usage();
			}
		}
		if (args.length - i != 2)
			usage();

		// now the fun starts
		final File directory = new File(args[i + 1]);
		if (!directory.isDirectory() && !directory.mkdirs()) {
			System.err.println("Could not make directories " + directory);
			System.exit(1);
		}
		try {
			System.err.println("Mirroring " + args[i] + " to " + directory);
			new MirrorWebsite(args[i], directory, threadCount, delay).run();
		} catch (Throwable t) {
			t.printStackTrace();
			System.exit(1);
		}
	}
}
