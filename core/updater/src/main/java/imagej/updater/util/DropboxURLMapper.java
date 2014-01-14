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

package imagej.updater.util;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Maps pseudo-DropBox URLs to valid ones.
 * 
 * DropBox used to have all files in public folders available via a URL of
 * the form: http://BASEURL/RELPATH where the BASEURL was a fixed URL for the
 * public folder and RELPATH the relative path into the public folder.
 * 
 * This convention changed, now the URLs are of the form:
 * https://www.dropbox.com/sh/USER-SPECIFIC/ALWAYS-CHANGING/RELPATH
 * 
 * This class helps to map URLs of the form BASEURL/RELPATH into the correct URL.
 * 
 * @author Johannes Schindelin
 */
public class DropboxURLMapper {
	/**
	 * The map.
	 * 
	 * When parsing directories, all contained files will be added, then the URL
	 * with a trailing slash. Therefore, missing URLs can be detected by the
	 * containing directory being mapped but not the file.
	 */
	private Map<URL, URL> map = new HashMap<URL, URL>();

	protected final Util util;

	/**
	 * Constructs a Dropbox URL mapper for use in a specific Util instance.
	 */
	protected DropboxURLMapper(final Util util) {
		this.util = util;
	}

	public URL get(final URL url) throws IOException {
		if (!isDropBoxURL(url)) return url;
		URL result = map.get(url);
		if (result != null) return result;
		final Matcher matcher = urlPattern.matcher(url.toString());
		if (!matcher.matches() || matcher.group(2) == null) {
			map.put(url,  url);
			return url;
		}
		try {
			parseParent(url);
		} catch (final Exception e) {
			throw new IOException(e);
		}
		result = map.get(url);
		if (result != null) return result;
		throw new FileNotFoundException(url.toString());
	}

	private final static Pattern urlPattern =
			Pattern.compile("^(https?://www.dropbox.com/sh/[^/]*/[^/]*)(/.*)?$");

	public static boolean isDropBoxURL(final URL url) {
		final String host = url.getHost();
		return host.endsWith(".dropbox.com");
	}

	private void parseParent(final URL url) throws IOException,
			MalformedURLException {
		final String urlString = url.toString();
		final int slash = urlString.lastIndexOf('/');
		if (slash < 0) throw new IOException("No slash in " + urlString);
		final String parentURLString = urlString.substring(0,  slash);
		final URL parentURLWithSlash = new URL(urlString.substring(0,  slash + 1));
		if (map.containsKey(parentURLWithSlash)) return;
		final URL parentURL = new URL(urlString.substring(0,  slash));
		final Matcher matcher = urlPattern.matcher(parentURLString);
		if (!matcher.matches()) return;
		if (matcher.group(2) == null) {
			map.put(parentURL, parentURL);
		} else {
			parseParent(parentURL);
		}
		final StringBuilder builder = read(map.get(parentURL));
		int offset = builder.indexOf("<ol class=\"browse-files");
		if (offset < 0) {
			throw new IOException("DropBox changed the URL format again!");
		}
		while (offset > 0) {
			final String anchor = "<a href=\"";
			offset = builder.indexOf(anchor, offset);
			if (offset < 0) break;
			offset += anchor.length();
			final int quote = builder.indexOf("\"", offset);
			if (quote < 0) {
				throw new IOException("Invalid HTML: "
						+ builder.substring(offset - anchor.length()));
			}
			final String urlString2 = builder.substring(offset, quote);
			if (!urlPattern.matcher(urlString2).matches()) continue;
			final int slash2 = urlString2.lastIndexOf('/');
			if (slash2 < 0) {
				throw new IOException("Unexpected URL: " + urlString2);
			}
			final URL targetURL = new URL(urlString2);
			map.put(targetURL, targetURL); // so that util.openStream(url) works
			map.put(new URL(parentURLString + urlString2.substring(slash)), targetURL);
			offset = quote;
		}
		map.put(parentURLWithSlash, map.get(parentURL));
	}

	private StringBuilder read(final URL url) throws IOException {
		final Reader reader = new InputStreamReader(util.openStream(url));
		final char[] buffer = new char[65536];
		final StringBuilder builder = new StringBuilder();
		for (;;) {
			int count = reader.read(buffer);
			if (count < 0) break;
			builder.append(buffer, 0, count);
		}
		reader.close();
		return builder;
	}

	public static void main(final String... args) throws Exception {
		final String baseURL = "https://www.dropbox.com/sh/la8g3k4uzhg53kz/UZUKTY-CX4/";
		final DropboxURLMapper map = new Util(null).dropboxURLMapper;
		final URL mapped = map.get(new URL(baseURL + "plugins/Say_Hello.ijm-20130313122802"));
		System.err.println("mapped: " + mapped);
		System.err.println("read: " + map.read(mapped));
	}
}
