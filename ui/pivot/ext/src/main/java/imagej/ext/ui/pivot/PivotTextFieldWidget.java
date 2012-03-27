
package imagej.ext.ui.pivot;

import imagej.ext.module.ui.TextFieldWidget;
import imagej.ext.module.ui.WidgetModel;

import org.apache.pivot.wtk.TextInput;

/**
 * Pivot implementation of text field widget.
 * 
 * @author Curtis Rueden
 */
public class PivotTextFieldWidget extends PivotInputWidget
	implements TextFieldWidget
{

	private final TextInput textInput;

	public PivotTextFieldWidget(final WidgetModel model) {
		super(model);

		textInput = new TextInput();

		add(textInput);
	}

	// -- TextFieldWidget methods --

	@Override
	public String getValue() {
		return textInput.getText();
	}

	// -- InputWidget methods --

	@Override
	public void refreshWidget() {
		textInput.setText(getModel().getValue().toString());
	}

}
