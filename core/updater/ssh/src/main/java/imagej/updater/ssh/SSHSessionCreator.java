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

package imagej.updater.ssh;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.UserInfo;

import imagej.updater.core.FilesUploader;
import imagej.updater.util.UpdaterUserInterface;
import imagej.updater.util.Util;
import imagej.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

/**
 * @author Jarek Sacha
 * @author Johannes Schindelin
 */
final class SSHSessionCreator {

	private SSHSessionCreator() {}

	/**
	 * Creates and connects SSH session.
	 * 
	 * @param username SSH user name.
	 * @param sshHost SSH host to connect to.
	 * @param userInfo authentication data.
	 * @return connected session.
	 * @throws JSchException if authentication or connection fails.
	 */
	protected static Session connect(String username, String sshHost,
		final UserInfo userInfo) throws JSchException
	{

		int port = 22;
		final int colon = sshHost.indexOf(':');
		if (colon > 0) {
			port = Integer.parseInt(sshHost.substring(colon + 1));
			sshHost = sshHost.substring(0, colon);
		}

		final JSch jsch = new JSch();

		// Reuse ~/.ssh/known_hosts file
		final File knownHosts =
			new File(new File(System.getProperty("user.home"), ".ssh"), "known_hosts");
		jsch.setKnownHosts(knownHosts.getAbsolutePath());

		final ConfigInfo configInfo = getIdentity(username, sshHost);
		if (configInfo != null) {
			if (configInfo.username != null) {
				username = configInfo.username;
			}
			if (configInfo.sshHost != null) {
				sshHost = configInfo.sshHost;
			}
			if (configInfo.identity != null) {
				jsch.addIdentity(configInfo.identity);
			}
		}

		final Session session = jsch.getSession(username, sshHost, port);
		session.setUserInfo(userInfo);
		session.connect();

		return session;
	}

	private static ConfigInfo getIdentity(final String username,
		final String sshHost)
	{
		final File config =
			new File(new File(System.getProperty("user.home"), ".ssh"), "config");
		if (!config.exists()) {
			return null;
		}

		try {
			final ConfigInfo result = new ConfigInfo();
			final BufferedReader reader = new BufferedReader(new FileReader(config));
			boolean hostMatches = false;
			for (;;) {
				String line = reader.readLine();
				if (line == null) break;
				line = line.trim();
				final int space = line.indexOf(' ');
				if (space < 0) {
					continue;
				}
				final String key = line.substring(0, space).toLowerCase();
				if (key.equals("host")) {
					hostMatches = line.substring(5).trim().equals(sshHost);
				}
				else if (hostMatches) {
					if (key.equals("user")) {
						if (username == null || username.equals("")) {
							result.username = line.substring(5).trim();
						}
					}
					else if (key.equals("hostname")) {
						result.sshHost = line.substring(9).trim();
					}
					else if (key.equals("identityfile")) {
						result.identity = line.substring(13).trim();
					}
					// TODO what if condition do match any here?
				}
			}
			reader.close();
			return result;
		}
		catch (final Exception e) {
			Log.error(e);
			return null;
		}
	}

	private static class ConfigInfo {

		String username;
		String sshHost;
		String identity;
	}

	protected static UserInfo getUserInfo(final String password) {
		return new UserInfo() {

			protected String prompt;
			protected int count = 0;

			@Override
			public String getPassphrase() {
				return UpdaterUserInterface.get().getPassword(prompt);
			}

			@Override
			public String getPassword() {
				if (count == 1) return password;
				return UpdaterUserInterface.get().getPassword(prompt);
			}

			@Override
			public boolean promptPassphrase(final String message) {
				prompt = message;
				return count++ < 3;
			}

			@Override
			public boolean promptPassword(final String message) {
				prompt = message;
				return count++ < 4;
			}

			@Override
			public boolean promptYesNo(final String message) {
				return UpdaterUserInterface.get().promptYesNo(message, "Password");
			}

			@Override
			public void showMessage(final String message) {
				UpdaterUserInterface.get().info(message, "Password");
			}
		};
	}

	public static Session getSession(final FilesUploader uploader) {
		String username = uploader.getDefaultUsername();
		final String sshHost = uploader.getUploadHost();
		for (;;) {
			// Dialog to enter user name and password
			if (username == null) {
				username =
					UpdaterUserInterface.get()
						.getString("Login for " + uploader.getUploadHost());
				if (username == null || username.equals("")) return null;
			}
			final String password =
				UpdaterUserInterface.get().getPassword(
					"Password for " + username + "@" + uploader.getUploadHost());
			if (password == null) return null; // return back to user interface

			final UserInfo userInfo = getUserInfo(password);
			try {
				final Session session = connect(username, sshHost, userInfo);
				if (session != null) {
					UpdaterUserInterface.get().setPref(Util.PREFS_USER, username);
					return session;
				}
			}
			catch (final JSchException e) {
				Log.error(e);
				return null;
			}
		}
	}

}
