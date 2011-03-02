package imagej.plugin.gui.swing;

import imagej.plugin.gui.TextFieldWidget;

import java.awt.BorderLayout;

import javax.swing.JPanel;
import javax.swing.JTextField;

/**
 * Swing implementation of text field widget.
 *
 * @author Curtis Rueden
 */
public class SwingTextFieldWidget extends JPanel implements TextFieldWidget {

	private JTextField textField;

	public SwingTextFieldWidget(final String initialValue, final int columns) {
		textField = new JTextField(initialValue, columns);
		add(textField, BorderLayout.CENTER);
	}

	@Override
	public String getText() {
		return textField.getText();
	}

}
