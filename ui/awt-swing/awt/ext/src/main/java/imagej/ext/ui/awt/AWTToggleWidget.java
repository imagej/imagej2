
package imagej.ext.ui.awt;

import imagej.ext.module.ui.ToggleWidget;
import imagej.ext.module.ui.WidgetModel;

import java.awt.BorderLayout;
import java.awt.Checkbox;

/**
 * AWT implementation of boolean toggle widget.
 *
 * @author Curtis Rueden
 */
public class AWTToggleWidget extends AWTInputWidget implements ToggleWidget {

	private Checkbox checkbox;

	public AWTToggleWidget(final WidgetModel model) {
		super(model);

		checkbox = new Checkbox("");
		add(checkbox, BorderLayout.CENTER);

		refreshWidget();
	}

	// -- ToggleWidget methods --

	@Override
	public Boolean getValue() {
		return checkbox.getState();
	}

	// -- InputWidget methods --

	@Override
	public void refreshWidget() {
		final boolean value = (Boolean) getModel().getValue();
		checkbox.setState(value);
	}

}
