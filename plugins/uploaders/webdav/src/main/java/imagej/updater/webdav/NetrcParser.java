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

package imagej.updater.webdav;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * A helper class to obtain credentials stored in $HOME/.netrc
 * 
 * @author Johannes Schindelin
 */
public class NetrcParser {

	final private URL url;

	public NetrcParser() {
		this(getDefaultNetrc());
	}

	public NetrcParser(final URL netrcURL) {
		url = netrcURL;
	}

	private static URL getDefaultNetrc() {
		final String path = System.getProperty("webdav.netrc");
		if (path != null) {
			final File file = new File(path);
			try {
				return file.isFile() ? file.toURI().toURL() : null;
			} catch (MalformedURLException e) {
				e.printStackTrace();
				return null;
			}
		}

		final String home = System.getProperty("user.home");
		if (home == null) return null;
		final File homeDirectory = new File(home);
		if (!homeDirectory.isDirectory()) return null;
		final File file = new File(homeDirectory, ".netrc");
		try {
			return file.isFile() ? file.toURI().toURL() :  null;
		} catch (MalformedURLException e) {
			e.printStackTrace();
			return null;
		}
	}

	public Credentials getCredentials(final String hostname) throws IOException {
		return getCredentials(hostname, null);
	}

	public Credentials getCredentials(final String hostname, final String username) throws IOException {
		final Credentials credentials = new Credentials(hostname, username, null);
		return getCredentials(credentials) ? credentials : null;
	}

	/**
	 * Obtains the credentials from $HOME/.netrc for the given hostname (and optionally username).
	 * 
	 * @param credentials specifies the hostname (and optionally the username)
	 * @return whether the host was found and the missing parts of the credentials filled
	 * @throws IOException
	 */
	public boolean getCredentials(final Credentials credentials) throws IOException {
		if (url == null) return false;
		String keyword = null;
		boolean isOurHost = false;
		final boolean needsUsername = credentials.getUsername() == null;
		final Tokenizer tokenizer = new Tokenizer(url);
		for (;;) {
			if (credentials.getPassword() != null && credentials.getUsername() != null) {
				tokenizer.close();
				return true;
			}
			final String token = tokenizer.nextToken();
			if (token == null) break;
			if (keyword == null) {
				keyword = token;
			} else {
				if ("machine".equals(keyword)) {
					isOurHost = token.equals(credentials.getHostname());
				} else if ("login".equals(keyword)) {
					if (isOurHost) {
						if (needsUsername) {
							credentials.username = token;
						} else {
							isOurHost = token.equals(credentials.getUsername());
						}
					}
				} else if ("password".equals(keyword)) {
					if (isOurHost) {
						credentials.password = token;
					}
				}
				// skip unknown keyword
				keyword = null;
			}
		}
		return false;
	}

	private static class Tokenizer {
		final private BufferedReader reader;
		private String[] tokens;
		private int current;

		public Tokenizer(final URL url) throws IOException {
			reader = new BufferedReader(new InputStreamReader(url.openStream()));
			nextLine();
		}

		public String nextToken() throws IOException {
			for (;;) {
				if (current < 0) return null;
				while (current >= tokens.length || "#".equals(tokens[current])) {
					if (!nextLine()) return null;
				}
				final String token = tokens[current++];
				if (!"".equals(token)) return token;
			}
		}

		public boolean nextLine() throws IOException {
			final String line = reader.readLine();
			if (line == null) {
				close();
				return false;
			}
			tokens = line.split("[ \t\n]+");
			current = 0;
			return true;
		}

		public void close() throws IOException {
			reader.close();
			tokens = null;
			current = -1;
		}
	}

	public static class Credentials {
		private String hostname, username, password;

		public Credentials(final String hostname, final String username, final String password) {
			this.hostname = hostname;
			this.username = username;
			this.password = password;
		}

		public String getHostname() {
			return hostname;
		}

		public String getUsername() {
			return username;
		}

		public String getPassword() {
			return password;
		}
	}
}
