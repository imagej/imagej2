
package imagej.updater.util;

import java.awt.Frame;
import java.io.IOException;
import java.io.OutputStream;

public abstract class UserInterface {

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

	public abstract String getPref(String key);

	public abstract void setPref(String key, String value);

	public abstract void savePreferences();

	public abstract void openURL(String url) throws IOException;

	public abstract String getString(String title);

	public abstract String getPassword(String title);

	public abstract void addWindow(Frame window);

	public abstract void removeWindow(Frame window);

	// The singleton
	protected static UserInterface ui = new StderrInterface();

	public static void set(final UserInterface ui) {
		UserInterface.ui = ui;
	}

	public final static UserInterface get() {
		return ui;
	}

	// The default implementation
	protected static class StderrInterface extends UserInterface {

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
			exception.printStackTrace();
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
	}
}
