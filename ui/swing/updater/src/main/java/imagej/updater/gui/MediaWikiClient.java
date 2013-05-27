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

package imagej.updater.gui;

import java.io.IOException;
import java.io.PrintStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.xml.parsers.ParserConfigurationException;

import org.scijava.util.XML;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * A minimal MediaWiki API client for interacting with the Fiji Wiki.
 * 
 * @author Johannes Schindelin
 */
class MediaWikiClient {
	private final String baseURL;
	private final Set<String> postActions = new HashSet<String>(Arrays.asList("login", "changeuploadpassword"));
	private String currentUser;
	private Map<String, String> cookies = new LinkedHashMap<String, String>();

	public MediaWikiClient(final String baseURL) {
		if (baseURL.endsWith("/index.php")) this.baseURL = baseURL.substring(0, baseURL.length() - 9);
		else if (baseURL.endsWith("/")) this.baseURL = baseURL;
		else this.baseURL = baseURL + "/";
	}

	public String getPageSource(final String title) throws IOException {
		final XML xml = query("titles", title, "export", "true", "exportnowrap", "true");
		return xml.cdata("/mediawiki/page/revision/text");
	}

	public boolean userExists(final String name) throws IOException {
		final XML xml = query("list", "users", "ususers", name);
		final NodeList list = xml.xpath("/api/query/users/user");
		int count = list.getLength();
		for (int i = 0; i < count; i++) {
			final NamedNodeMap node = list.item(i).getAttributes();
			if (node != null && node.getNamedItem("missing") == null) return true;
		}
		return false;
	}

	public boolean createUser(final String userName, final String realName, final String email, final String reason) throws IOException {
		final String[] headers = {
				"Requested-User", userName
		};
		final XML xml = request(headers, "createfijiwikiaccount",
				"name", userName, "email", email, "realname", realName, "reason", reason);
		final String error = getAttribute(xml.xpath("/api/error"), "info");
		if (error != null) {
			System.err.println("Error creating user " + userName + ": " + error);
			return false;
		}
		if (userName.equals(getAttribute(xml.xpath("/api/createfijiwikiaccount"), "created"))) {
			return true;
		}
		return false;
	}

	public boolean changeUploadPassword(final String password) throws IOException {
		if (currentUser == null) throw new IOException("Can only change the password for a logged-in user");
		System.err.println("action: changeuploadpassword");
		final XML xml = request(null, "changeuploadpassword", "password", password);
		final String error = getAttribute(xml.xpath("/api/error"), "info");
		if (error != null) {
			System.err.println("Error setting upload password for user '" + currentUser + "': " + error);
			return false;
		}
		final NodeList response = xml.xpath("/api/changeuploadpassword");
		final boolean result = "0".equals(getAttribute(response, "result"));
		if (!result) {
			System.err.println("a1: " + response.item(0));
			System.err.println("Error: " + getAttribute(response, "output"));
		}
		return result;
	}

	public boolean login(final String user, final String password) throws IOException {
		XML xml = request(null, "login", "lgname", user, "lgpassword", password);
		final String loginToken = getAttribute(xml.xpath("/api/login"), "token");
		if (loginToken == null) {
			System.err.println("Did not get a token!");
			return false;
		}
		xml = request(null, "login", "lgname", user, "lgpassword", password, "lgtoken", loginToken);
		final boolean result = "Success".equals(getAttribute(xml.xpath("/api/login"), "result"));
		currentUser = result ? user : null;
		return result;
	}

	public void logout() throws IOException {
		if (currentUser == null) return;
		request(null, "logout");
		cookies.clear();
		currentUser = null;
	}

	public XML request(final String[] headers, final String action, final String... parameters) throws IOException {
		if (parameters.length % 2 != 0) throw new IllegalArgumentException("Requires key/value pairs");
		final boolean requiresPOST = postActions.contains(action);
		try {
			final StringBuilder url = new StringBuilder();
			url.append(baseURL).append("api.php?action=").append(action).append("&format=xml");
			if (!requiresPOST) {
				for (int i = 0; i < parameters.length; i += 2) {
					url.append("&").append(URLEncoder.encode(parameters[i], "UTF-8"));
					url.append("=").append(URLEncoder.encode(parameters[i + 1], "UTF-8"));
				}
			}
			final URLConnection connection = new URL(url.toString()).openConnection();
			for (final Entry<String, String> entry : cookies.entrySet()) {
				connection.addRequestProperty("Cookie", entry.getKey() + "=" + entry.getValue());
			}
			if (headers != null) {
				if (headers.length % 2 != 0) throw new IllegalArgumentException("Requires key/value pairs");
				for (int i = 0; i < headers.length; i += 2) {
					connection.setRequestProperty(headers[i], headers[i + 1]);
				}
			}
			final HttpURLConnection http = (connection instanceof HttpURLConnection) ? (HttpURLConnection) connection : null;
			if (http != null && requiresPOST) {
				http.setRequestMethod("POST");
				final String boundary = "---e69de29bb2d1d6434b8b29ae775ad8c2e48c5391";
				http.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);
				http.setDoOutput(true);
				http.connect();
				final PrintStream ps = new PrintStream(http.getOutputStream());
				for (int i = 0; i < parameters.length; i += 2) {
				 ps.print("--" + boundary + "\r\n"
					 + "Content-Disposition: "
					 + "form-data; name=\""
					 + parameters[i] + "\"\r\n\r\n"
					 + parameters[i + 1] + "\r\n");
				}
				ps.println("--" + boundary + "--");
				ps.close();
			}
			final List<String> newCookies = http.getHeaderFields().get("Set-Cookie");
			if (newCookies != null) {
				for (final String cookie : newCookies) {
					final int equal = cookie.indexOf("=");
					if (equal < 0) continue;
					final String key = cookie.substring(0, equal);
					final String value = cookie.substring(equal + 1);
					if (value.startsWith("deleted; ")) cookies.remove(key);
					else cookies.put(key, value);
				}
			}
			return new XML(connection.getInputStream());
		} catch (ParserConfigurationException e) {
			throw new IOException(e);
		} catch (SAXException e) {
			throw new IOException(e);
		}
	}

	public XML query(final String... parameters) throws IOException {
		return request(null, "query", parameters);
	}

	public static String getAttribute(final NodeList list, final String attributeName) {
		if (list == null || list.getLength() != 1) return null;
		final NamedNodeMap attrs = list.item(0).getAttributes();
		if (attrs == null) return null;
		final Node node = attrs.getNamedItem(attributeName);
		return node == null ? null : node.getNodeValue();
	}

	public static void main(String... args) throws Exception {
		final MediaWikiClient wiki = new MediaWikiClient("http://fiji.sc.localhost/");
		System.err.println("login: " + wiki.login("BugsBunny", "12345"));
		System.err.println("change upload password: " + wiki.changeUploadPassword("12345"));
		wiki.logout();
	}
}