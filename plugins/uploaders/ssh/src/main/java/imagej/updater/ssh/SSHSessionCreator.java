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

package imagej.updater.ssh;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.ProxyHTTP;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.UserInfo;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

import net.imagej.updater.FilesUploader;
import net.imagej.updater.util.UpdaterUserInterface;
import net.imagej.updater.util.UpdaterUtil;

import org.scijava.log.LogService;

/**
 * Start an SSH connection.
 * 
 * @author Jarek Sacha
 * @author Johannes Schindelin
 */
final class SSHSessionCreator {

	private SSHSessionCreator() {}

	/**
	 * Creates and connects SSH session.
	 * 
	 * @param config connection data.
	 * @param userInfo authentication data.
	 * @return connected session.
	 * @throws JSchException if authentication or connection fails.
	 */
	protected static Session connect(ConfigInfo config, final UserInfo userInfo) throws JSchException
	{

		final JSch jsch = new JSch();

		// Reuse ~/.ssh/known_hosts file
		final File knownHosts =
			new File(new File(System.getProperty("user.home"), ".ssh"), "known_hosts");
		jsch.setKnownHosts(knownHosts.getAbsolutePath());

		final Session session = jsch.getSession(config.username, config.sshHost, config.port);
		if (config.identity != null) {
			jsch.addIdentity(config.identity);
		}
		String proxyHost = System.getProperty("http.proxyHost");
		String proxyPort = System.getProperty("http.proxyPort");
		if (proxyHost != null && proxyPort != null)
			session.setProxy(new ProxyHTTP(proxyHost, Integer.parseInt(proxyPort)));
		session.setUserInfo(userInfo);
		try {
			session.connect();
		} catch (JSchException e) {
			if (proxyHost != null && proxyPort != null && e.getMessage().indexOf("Forbidden") >= 0) {
				System.err.println("Trying to connect to " + config.username + "@" + config.sshHost + " without a proxy.");
				session.setProxy(null);
				session.connect();
			} else {
				// re-throw
				throw e;
			}
		}

		return session;
	}

	private static ConfigInfo getIdentity(final String username,
		final String sshHost, final LogService log)
	{
		final ConfigInfo result = new ConfigInfo();
		result.username = username;
		result.port = 22;
		final int colon = sshHost.indexOf(':');
		if (colon < 0)
			result.sshHost = sshHost;
		else {
			result.port = Integer.parseInt(sshHost.substring(colon + 1));
			result.sshHost = sshHost.substring(0, colon);
		}

		final File config =
			new File(new File(System.getProperty("user.home"), ".ssh"), "config");
		if (!config.exists()) {
			return result;
		}

		try {
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
					hostMatches = line.substring(5).trim().equals(result.sshHost);
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
				}
			}
			reader.close();
			return result;
		}
		catch (final Exception e) {
			log.error(e);
			return null;
		}
	}

	private static class ConfigInfo {
		protected String username;
		protected String sshHost;
		protected String identity;
		protected int port;
	}

	protected static UserInfo getUserInfo(final String initialPrompt, final String password) {
		return new UserInfo() {

			protected String prompt = initialPrompt;
			protected int count = 0;

			@Override
			public String getPassphrase() {
				return UpdaterUserInterface.get().getPassword(prompt);
			}

			@Override
			public String getPassword() {
				if (count == 1 && password != null) return password;
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
		final ConfigInfo configInfo = getIdentity(uploader.getDefaultUsername(), uploader.getUploadHost(), uploader.getLog());

		for (;;) {
			// Dialog to enter user name and password
			if (configInfo.username == null) {
				configInfo.username = UpdaterUserInterface.get().getString("Login for " + uploader.getUploadHost());
				if (configInfo.username == null || configInfo.username.equals("")) {
					return null;
				}
			}
			final String prompt = "Password for " + configInfo.username + "@" + uploader.getUploadHost();
			String password = null;
			if (configInfo.identity == null) {
				password = UpdaterUserInterface.get().getPassword(prompt);
				if (password == null)
					return null;
			}
			final UserInfo userInfo = getUserInfo(prompt, password);
			try {
				final Session session = connect(configInfo, userInfo);
				if (session != null) {
					UpdaterUserInterface.get().setPref(UpdaterUtil.PREFS_USER, configInfo.username);
					return session;
				}
			}
			catch (final JSchException e) {
				uploader.getLog().error(e);
				return null;
			}
		}
	}

	/**
	 * For debugging only.
	 * 
	 * <p>This connects to an SSH server for testing purposes. The given host must
	 * be specified in $HOME/.ssh/config and it must be equipped with a private
	 * key.</p>
	 * 
	 * <p>Note that you need to connect with command-line ssh first, to record the
	 * finger-print of the host. It might be necessary to call
	 * <tt>ssh-keyscan test.imagej.net >> $HOME/.ssh/known_hosts</tt> if your
	 * <tt>$HOME/.ssh/known_hosts contains hashed lines.</p>
	 * 
	 * @param host
	 *            the ssh host, as specified in $HOME/.ssh/config
	 * @param log
	 *            the log service
	 * @return a valid SSH session
	 * @throws JSchException
	 */
	protected static Session debugConnect(final String host, final LogService log) throws JSchException {
		final ConfigInfo info = getIdentity(null, host, log);
		if (info.username == null || info.identity == null) {
			throw new JSchException("Could not determine user name or identity for " + host);
		}
		final JSch jsch = new JSch();
		jsch.addIdentity(info.identity);

		// Reuse ~/.ssh/known_hosts file
		final File knownHosts =
			new File(new File(System.getProperty("user.home"), ".ssh"), "known_hosts");
		jsch.setKnownHosts(knownHosts.getAbsolutePath());

		final Session session = jsch.getSession(info.username, info.sshHost);
		session.connect();
		return session;
	}
}
