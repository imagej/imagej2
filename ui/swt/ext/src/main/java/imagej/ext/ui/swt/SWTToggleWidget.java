
package imagej.ext.ui.swt;

import imagej.ext.module.ui.ToggleWidget;
import imagej.ext.module.ui.WidgetModel;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;

/**
 * SWT implementation of boolean toggle widget.
 * 
 * @author Curtis Rueden
 */
public class SWTToggleWidget extends SWTInputWidget implements ToggleWidget {

	private final Button checkbox;

	public SWTToggleWidget(final Composite parent, final WidgetModel model) {
		super(parent, model);

		checkbox = new Button(this, SWT.CHECK);

		refreshWidget();
	}

	// -- ToggleWidget methods --

	@Override
	public Boolean getValue() {
		return checkbox.getSelection();
	}

	// -- InputWidget methods --

	@Override
	public void refreshWidget() {
		checkbox.setSelection((Boolean) getModel().getValue());
	}

}
