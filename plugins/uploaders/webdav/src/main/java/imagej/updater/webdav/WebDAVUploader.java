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

package imagej.updater.webdav;

import imagej.updater.core.AbstractUploader;
import imagej.updater.core.FilesUploader;
import imagej.updater.core.UpdateSite;
import imagej.updater.core.Uploadable;
import imagej.updater.core.Uploader;
import imagej.updater.util.UpdaterUserInterface;
import imagej.updater.util.Util;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.ParserConfigurationException;

import net.iharder.Base64;

import org.scijava.log.LogService;
import org.scijava.log.StderrLogService;
import org.scijava.plugin.Plugin;
import org.scijava.util.XML;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * Uploads files to an update server using WebDAV.
 * 
 * @author Johannes Schindelin
 */
@Plugin(type = Uploader.class)
public class WebDAVUploader extends AbstractUploader {

	private String baseURL,username, password;
	private Set<String> existingDirectories;
	private LogService log;
	private boolean debug = false;

	/**
	 * The {@link HttpURLConnection} does not accept the WebDAV-specific verbs
	 * we want to pass to the server. Hence we need to use ugly reflection...
	 */
	private static Field methodField;

	@Override
	public String getProtocol() {
		return "webdav";
	}

	@Override
	public boolean login(final FilesUploader uploader) {
		if (!super.login(uploader)) return false;

		log = uploader.getLog();
		debug = log.isDebug();

		if (methodField == null) try {
			 methodField = HttpURLConnection.class.getDeclaredField("method");
			 methodField.setAccessible(true);
		} catch (Throwable t) {
			log.error(t);
		}

		String host = uploader.getUploadHost();
		if (!"".equals(host)) {
			username = host;
			int colon = username.indexOf(':');
			if (colon < 0) {
				password = null;
			} else {
				password = username.substring(colon + 1);
				username = username.substring(0, colon);
			}
		}

		UpdateSite site = uploader.getFilesCollection().getUpdateSite(uploader.getSiteName(), true);
		baseURL = site.getURL();

		if (username == null) {
			uploader.getDefaultUsername();
			if (username == null) username = UpdaterUserInterface.get().getString("Login for " + baseURL);
			if (username == null) return false;
		}

		if (password == null) {
			final String prompt = "Password for " + username + "@" + baseURL;
			password = UpdaterUserInterface.get().getPassword(prompt);
			if (password == null) return false;
		}

		if (!baseURL.endsWith("/")) baseURL += "/";

		existingDirectories = new HashSet<String>();

		if (!isAllowed()) {
			UpdaterUserInterface.get().error("User " + username + " lacks upload permissions for " + baseURL);
			return false;
		}
		try {
			if (!directoryExists("")) {
				UpdaterUserInterface.get().error(baseURL + " does not exist yet!");
				return false;
			}
		} catch (UnauthenticatedException e) {
			return false;
		}
		return true;
	}

	private static class UnauthenticatedException extends Exception {
		private static final long serialVersionUID = 8269335582341674291L;
	}

	@Override
	public void logout() {
		username = password = null;
	}

	// Steps to accomplish entire upload task
	@Override
	public synchronized void upload(final List<Uploadable> sources,
		final List<String> locks) throws IOException
	{
		timestamp = -1;
		Map<String, String> tokens = new HashMap<String, String>();
		for (final String lock : locks) {
			final String path = lock + ".lock";
			final String token = lock(path);
			tokens.put(path, token);
		}
		setTitle("Uploading");

		try {
			calculateTotalSize(sources);
			int count = 0;
			final byte[] buf = new byte[16384];
			for (final Uploadable source : sources) {
				final String target = source.getFilename();

				// make sure that the target directory exists
				int slash = target.lastIndexOf('/');
				if (slash > 0 && ! ensureDirectoryExists(target.substring(0, slash + 1))) {
					throw new IOException("Could not make subdirectory for " + target);
				}

				addItem(source);
				final InputStream input = source.getInputStream();
				int currentCount = 0;
				final int currentTotal = (int) source.getFilesize();
				String token = tokens.get(target);
				final HttpURLConnection connection;
				URL url = getURL(target, false);
				if (token == null) {
					connection = connect("PUT", url, null);
				} else {
					connection = connect("PUT", url, null, "If", "<" + url + "> (<" + token + ">)");
				}
				connection.setRequestProperty("Content-Length", "" + source.getFilesize());
				connection.setDoOutput(true);
				final OutputStream out = connection.getOutputStream(); 
				for (;;) {
					final int len = input.read(buf, 0, buf.length);
					if (len <= 0) break;
					out.write(buf, 0, len);
					currentCount += len;
					setItemCount(currentCount, currentTotal);
					setCount(count + currentCount, total);
				}
				input.close();
				count += currentCount;
				out.close();
				itemDone(source);
				int code = connection.getResponseCode();
				if (code != 201 && code != 204) {
					log.error("Code: " + code + " " + connection.getResponseMessage());
					throw new IOException("Could not write " + target);
				}
			}
			done();

			addItem("Moving locks");
			for (final String lock : locks) {
				final String source = lock + ".lock";
				if (move(source, lock, tokens.get(source), true)) {
					/*
					 * According to RFC4918, a MOVE *must not* move the locks.
					 * And it also says a MOVE is equivalent to a COPY followed
					 * by a DELETE, hence the lock is gone upon a successful
					 * MOVE.
					 */
					tokens.remove(source);
				} else {
					log.error("Could not move " + source + " to " + lock);
				}
			}
		} catch (Exception e) {
			log.error(e);
		} finally {
			for (final String key : tokens.keySet()) {
				final String token = tokens.get(key);
				if (!unlock(key, token)) {
					throw new IOException("Could not unlock " + key + " with token " + token);
				}
			}
		}
	}

	private String lock(final String path) throws IOException {
		String xml = "<?xml version=\"1.0\" encoding=\"utf-8\" ?>" 
			+ "<lockinfo xmlns='DAV:'>"
			+ "<lockscope><exclusive/></lockscope>"
			+ "<locktype><write/></locktype>" 
			+ "<owner>"
			+ "<href>" + baseURL + "User:" + username + "</href>"
			+ "</owner>"
			+ "</lockinfo>";
		try {
			final HttpURLConnection connection = connect("LOCK", getURL(path, false), xml, "Timeout", "Second-1800");

			if (connection.getResponseCode() != 200) {
				throw new IOException("Error obtaining lock for " + path + ": "
					+ connection.getResponseCode() + " " + connection.getResponseMessage());
			}

			if (timestamp < 0) {
				timestamp = Long.parseLong(Util.timestamp(connection.getHeaderFieldDate("Date", -1)));
				if (timestamp < 0) {
					throw new IOException("Could not obtain date from the server");
				}
			}

			final XML result = new XML(connection.getInputStream());
			final String token = result.cdata("/prop/lockdiscovery/activelock/locktoken/href");
			if (debug) {
				log.info("Tried to obtain a lock (" + token + "):\n" + result);
			}
			if (token == null) {
				log.error("Expected lock for '" + path + "', got:\n" + result.toString());
				throw new IOException("Could not obtain lock for " + path);
			}
			return token;
		} catch (IOException e) {
			throw e;
		} catch (Exception e) {
			log.error(e);
			throw new RuntimeException(e);
		}
	}

	private boolean unlock(final String path, final String token) {
		try {
			final HttpURLConnection connection = connect("UNLOCK", getURL(path, false), null, "Lock-Token", "<" + token + ">");
			int code = connection.getResponseCode();
			if (code == 204) return true;
			log.error("Error removing lock from " + path + ": " + code + " " + connection.getResponseMessage());
		} catch (Exception e) {
			log.error(e);
		}
		return false;
	}

	private boolean move(final String source, final String target, final String token, boolean force) {
		try {
			final URL url = getURL(source, false);
			final String targetURL = getURL(target, false).toString();
			final HttpURLConnection connection = connect("MOVE", url, null,
					"Destination", targetURL,
					"Overwrite", force ? "T" : "F",
					"If", "<" + url + "> (<" + token + ">)");
			int code = connection.getResponseCode();
			if (code == 201 || code == 204) return true;
			log.error("Error moving " + source + " to " + target + ": " + code + " " + connection.getResponseMessage());
		} catch (Exception e) {
			log.error(e);
		}
		return false;
	}

	private boolean ensureDirectoryExists(final String path) {
		if (existingDirectories.contains(path)) {
			return true;
		}

		try {
			if (directoryExists(path)) {
				existingDirectories.add(path);
				return true;
			}
		} catch (UnauthenticatedException e) {
			return false;
		}

		int slash = path.lastIndexOf('/', path.length() - 2);
		if (slash > 0 && !ensureDirectoryExists(path.substring(0, slash + 1))) {
			return false;
		}

		if (makeDirectory(path)) {
			existingDirectories.add(path);
			return true;
		}

		return false;
	}

	@SuppressWarnings("unused")
	private XML propfind(final String path) {
		String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>"
				+ "<propfind xmlns=\"DAV:\">"
				+ "<allprop />"
				//+ "<prop><propname /></prop>"
				+ "</propfind>";
		try {
			final HttpURLConnection connection = connect("PROPFIND", getURL(path, false), xml);
			return new XML(connection.getInputStream());
		} catch (Exception e) {
			log.error(e);
		}
		return null;
	}

	private boolean directoryExists(final String path) throws UnauthenticatedException {
		String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>"
				+ "<propfind xmlns=\"DAV:\">"
				+ "<prop>"
				+ "<resourcetype/>"
				+ "</prop>"
				+ "</propfind>";
		try {
			final HttpURLConnection connection = connect("PROPFIND", getURL(path, true), xml);

			try {
				final XML result = new XML(connection.getInputStream());
				final NodeList list = result.xpath("/multistatus/response/propstat/prop/resourcetype/collection");
				return list != null && list.getLength() > 0;
			} catch (IOException e) {
				if (connection.getResponseCode() == 401) throw new UnauthenticatedException();
				throw e;
			}
		} catch (FileNotFoundException e) {
			return false;
		} catch (IOException e) {
			log.error(e);
		} catch (ParserConfigurationException e) {
			log.error(e);
		} catch (SAXException e) {
			log.error(e);
		}
		return false;
	}

	private boolean isAllowed() {
		try {
			final HttpURLConnection connection = connect("OPTIONS", new URL(baseURL), null);
			connection.connect();
			String allow = connection.getHeaderField("Allow");
			return allow != null && allow.contains("LOCK");
		} catch (Exception e) {
			log.error(e);
		}
		return false;
	}

	private boolean makeDirectory(final String path) {
		try {
			final HttpURLConnection connection = connect("MKCOL", getURL(path, true), null);
			connection.connect();
			return connection.getResponseCode() == 201;
		} catch (Exception e) {
			log.error(e);
		}
		return false;
	}

	private URL getURL(final String path, boolean isDirectory) throws MalformedURLException, UnsupportedEncodingException {
		final String url = baseURL + URLEncoder.encode(path, "UTF-8").replaceAll("%2F", "/").replaceAll("\\+","%20");
		if (!isDirectory || "".equals(path) && path.endsWith("/")) return new URL(url);
		return new URL(url + "/");
	}

	protected void setCredentials(final String username, final String password) {
		this.username = username;
		this.password = password;
		if (log == null) {
			log = new StderrLogService();
			log.setLevel(LogService.DEBUG);
			debug = true;
		}

		if (methodField == null) try {
			 methodField = HttpURLConnection.class.getDeclaredField("method");
			 methodField.setAccessible(true);
		} catch (Throwable t) {
			log.error(t);
		}
	}

	protected HttpURLConnection connect(final String method, final URL url, final String xml, final String... headers) throws IOException {
		if (headers != null && (headers.length % 2) != 0) {
			throw new IOException("Invalid list of header pairs");
		}
		final HttpURLConnection connection = (HttpURLConnection) url.openConnection();
		setRequestMethod(connection, method);

		final String authentication = username + ":" + password;
		connection.setRequestProperty("Authorization", "Basic " + Base64.encodeBytes(authentication.getBytes("UTF-8")));

		if (headers != null) {
			for (int i = 0; i < headers.length; i += 2) {
				connection.setRequestProperty(headers[i], headers[i + 1]);
			}
		}

		if (xml == null) {
			connection.setRequestProperty("Content-Type", "application/octet-stream");
		} else {
			connection.addRequestProperty("Depth", "0");
			connection.addRequestProperty("Brief", "t");
			connection.addRequestProperty("Content-Type", "text/xml; charset=\"utf-8\"");
			connection.setDoInput(true);
			connection.setDoOutput(true);
			connection.connect();

			byte[] xmlBytes = xml.getBytes("UTF-8");
			OutputStream out = getOutputStream(connection);
			out.write(xmlBytes);
			out.close();
		}

		if (debug) {
			log.debug("Sent request " + connection.getRequestMethod() + " " + connection.getURL());
			if (!"PUT".equals(connection.getRequestMethod())) {
				log.debug("Response: " + connection.getResponseCode() + " " + connection.getResponseMessage());
				Map<?, ?> map = connection.getHeaderFields();
				for (Object key : map.keySet()) {
					log.debug("Header: " + key + " = " + map.get(key));
				}
			}
		}

		return connection;
	}

	private OutputStream getOutputStream(HttpURLConnection connection) throws IOException {
		try {
			String savedMethod = getRequestMethod(connection);
			setRequestMethod(connection, "PUT");
			final OutputStream out;
			try {
				out = connection.getOutputStream();
			} catch (IOException e) {
				setRequestMethod(connection, savedMethod);
				throw e;
			}
			setRequestMethod(connection, savedMethod);
			return out;
		} catch (IllegalArgumentException e) {
			log.error(e);
		}
		return null;
	}

	// -- reflected field made accessible

	private String getRequestMethod(final HttpURLConnection connection) {
		if (methodField == null) return null;
		try {
			return (String)methodField.get(connection);
		} catch (IllegalArgumentException e) {
			log.error(e);
		} catch (IllegalAccessException e) {
			log.error(e);
		}
		return null;
	}

	private void setRequestMethod(final HttpURLConnection connection, final String method) {
		if (methodField == null) return;
		try {
			methodField.set(connection, method);
		} catch (IllegalAccessException e) {
			log.error(e);
		}
	}

}
