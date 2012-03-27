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

import imagej.util.Log;

import java.awt.Frame;
import java.io.IOException;
import java.io.OutputStream;

public abstract class UpdaterUserInterface {

	// The methods
	public abstract void error(String message);

	public abstract void info(String message, String title);

	public abstract void log(String message);

	public abstract void debug(String message);

	public abstract OutputStream getOutputStream();

	public abstract void showStatus(String message);

	public abstract void handleException(Throwable exception);

	public abstract boolean isBatchMode();

	public abstract int optionDialog(String message, String title,
		Object[] options, int def);

	public abstract boolean promptYesNo(String message, String title);

	public abstract String getPref(String key);

	public abstract void setPref(String key, String value);

	public abstract void savePreferences();

	public abstract void openURL(String url) throws IOException;

	public abstract String getString(String title);

	public abstract String getPassword(String title);

	public abstract void addWindow(Frame window);

	public abstract void removeWindow(Frame window);

	// The singleton
	protected static UpdaterUserInterface ui = new StderrInterface();

	public static void set(final UpdaterUserInterface ui) {
		UpdaterUserInterface.ui = ui;
	}

	public final static UpdaterUserInterface get() {
		return ui;
	}

	// The default implementation
	protected static class StderrInterface extends UpdaterUserInterface {

		private final boolean debug = false;

		@Override
		public void error(final String message) {
			System.err.println(message);
		}

		@Override
		public void info(final String message, final String title) {
			System.err.println(title + ": " + message);
		}

		@Override
		public void log(final String message) {
			System.err.println(message);
		}

		@Override
		public void debug(final String message) {
			if (debug) System.err.println(message);
		}

		@Override
		public OutputStream getOutputStream() {
			return System.err;
		}

		@Override
		public void showStatus(final String message) {
			System.err.println(message);
		}

		@Override
		public void handleException(final Throwable exception) {
			Log.error(exception);
		}

		@Override
		public boolean isBatchMode() {
			return true;
		}

		@Override
		public int optionDialog(final String message, final String title,
			final Object[] options, final int def)
		{
			throw new RuntimeException("TODO");
		}

		@Override
		public String getPref(final String key) {
			return null;
		}

		@Override
		public void setPref(final String key, final String value) {
			/* ignore */
		}

		@Override
		public void savePreferences() {
			throw new RuntimeException("TODO");
		}

		@Override
		public void openURL(final String url) {
			System.err.println("Open URL " + url);
		}

		@Override
		public String getString(final String title) {
			System.err.print(title + " ");
			return new String(System.console().readLine());
		}

		@Override
		public String getPassword(final String title) {
			System.err.print(title + " ");
			return new String(System.console().readPassword());
		}

		@Override
		public void addWindow(final Frame window) {
			// do nothing
		}

		@Override
		public void removeWindow(final Frame window) {
			// do nothing
		}

		@Override
		public boolean promptYesNo(final String message, final String title) {
			System.err.println(title + " " + message);
			final String answer = new String(System.console().readLine());
			return answer.toLowerCase().startsWith("y");
		}
	}
}
