
package imagej.updater.gui;

import imagej.event.EventService;
import imagej.event.StatusEvent;
import imagej.updater.util.UserInterface;
import imagej.util.Log;
import imagej.util.Prefs;

import java.awt.Frame;
import java.io.IOException;
import java.io.OutputStream;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

import net.miginfocom.swing.MigLayout;

public class SwingUserInterface extends UserInterface {

	final protected EventService eventService;

	public SwingUserInterface(final EventService eventService) {
		this.eventService = eventService;
	}

	@Override
	public void error(final String message) {

		JOptionPane.showMessageDialog(null, message, "ImageJ Updater",
			JOptionPane.ERROR_MESSAGE);

	}

	@Override
	public void info(final String message, final String title) {

		JOptionPane.showMessageDialog(null, message, "ImageJ Updater",
			JOptionPane.INFORMATION_MESSAGE);

	}

	@Override
	public void log(final String message) {

		Log.info(message);

	}

	@Override
	public void debug(final String message) {

		Log.debug(message);

	}

	@Override
	public OutputStream getOutputStream() {

		// TODO: create a JFrame with a JTextPane
		return System.err;

	}

	@Override
	public void showStatus(final String message) {

		eventService.publish(new StatusEvent(message, false));

	}

	@Override
	public void handleException(final Throwable exception) {

		Log.error(exception);

	}

	@Override
	public boolean isBatchMode() {

		return false;

	}

	@Override
	public int optionDialog(final String message, final String title,
		final Object[] options, final int def)
	{

		return JOptionPane.showOptionDialog(null, message, title,
			JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null,
			options, options[def]);

	}

	@Override
	public String getPref(final String key) {

		return Prefs.get(this.getClass(), key);

	}

	@Override
	public void setPref(final String key, final String value) {

		Prefs.put(this.getClass(), key, value);

	}

	@Override
	public void savePreferences() {

		/* is done automatically */

	}

	@Override
	public void openURL(final String url) throws IOException {
		// TODO Auto-generated method stub

	}

	@Override
	public String getString(final String title) {

		final JPanel panel = new JPanel();
		panel.setLayout(new MigLayout());

		panel.add(new JLabel("User"));
		final JTextField user = new JTextField();

		if (JOptionPane.showConfirmDialog(null, panel, title,
			JOptionPane.OK_CANCEL_OPTION) == JOptionPane.CANCEL_OPTION) return null;
		return user.getText();

	}

	@Override
	public String getPassword(final String title) {

		final JLabel label = new JLabel("Password:");
		final JPasswordField password = new JPasswordField();

		if (JOptionPane.showConfirmDialog(null, new Object[] { label, password }, title,
			JOptionPane.OK_CANCEL_OPTION) == JOptionPane.CANCEL_OPTION) return null;
		return new String(password.getPassword());

	}

	@Override
	public void addWindow(final Frame window) {

		// TODO How to do this?

	}

	@Override
	public void removeWindow(final Frame window) {

		// TODO How to do this?

	}

}
