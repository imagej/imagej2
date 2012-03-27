
package imagej.ext.ui.swt;

import imagej.ext.module.ui.TextFieldWidget;
import imagej.ext.module.ui.WidgetModel;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;

/**
 * SWT implementation of text field widget.
 * 
 * @author Curtis Rueden
 */
public class SWTTextFieldWidget extends SWTInputWidget
	implements TextFieldWidget
{

	private final Text text;

	public SWTTextFieldWidget(final Composite parent,
		final WidgetModel model, final int columns)
	{
		super(parent, model);

		text = new Text(this, 0);
		text.setTextLimit(columns);

		refreshWidget();
	}

	// -- TextFieldWidget methods --

	@Override
	public String getValue() {
		return text.getText();
	}

	// -- InputWidget methods --

	@Override
	public void refreshWidget() {
		text.setText(getModel().getValue().toString());
	}

}
