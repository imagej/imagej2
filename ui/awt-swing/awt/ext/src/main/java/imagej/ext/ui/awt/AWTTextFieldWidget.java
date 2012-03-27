
package imagej.ext.ui.awt;

import imagej.ext.module.ui.TextFieldWidget;
import imagej.ext.module.ui.WidgetModel;

import java.awt.BorderLayout;
import java.awt.TextField;
import java.awt.event.TextEvent;
import java.awt.event.TextListener;

/**
 * AWT implementation of text field widget.
 *
 * @author Curtis Rueden
 */
public class AWTTextFieldWidget extends AWTInputWidget
	implements TextFieldWidget, TextListener
{

	private TextField textField;

	public AWTTextFieldWidget(final WidgetModel model, final int columns) {
		super(model);

		textField = new TextField("", columns);
		textField.addTextListener(this);
		add(textField, BorderLayout.CENTER);

		refreshWidget();
	}

	// -- TextFieldWidget methods --

	@Override
	public String getValue() {
		return textField.getText();
	}

	// -- InputWidget methods --

	@Override
	public void refreshWidget() {
		textField.setText(getModel().getValue().toString());
	}

	// -- TextListener methods --

	@Override
	public void textValueChanged(final TextEvent e) {
		updateModel();
	}

}
