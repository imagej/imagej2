
package imagej.updater.gui;

import java.net.Authenticator;
import java.net.PasswordAuthentication;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

import net.miginfocom.swing.MigLayout;

public class SwingAuthenticator extends Authenticator {

	@Override
	protected PasswordAuthentication getPasswordAuthentication() {
		final JPanel panel = new JPanel();
		panel.setLayout(new MigLayout());

		panel.add(new JLabel("User:"));
		final JTextField user = new JTextField();
		panel.add(user, "wrap");

		panel.add(new JLabel("Password:"));
		final JPasswordField password = new JPasswordField();
		panel.add(password);

		if (JOptionPane.showConfirmDialog(null, panel, "Proxy Authentication", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.CANCEL_OPTION)
			return null;
		return new PasswordAuthentication(user.getText(), password.getPassword());
	}

}
