
package imagej.ext.ui.pivot;

import imagej.ext.module.ui.ToggleWidget;
import imagej.ext.module.ui.WidgetModel;

import org.apache.pivot.wtk.Checkbox;

/**
 * Pivot implementation of boolean toggle widget.
 * 
 * @author Curtis Rueden
 */
public class PivotToggleWidget extends PivotInputWidget implements ToggleWidget
{

	private final Checkbox checkbox;

	public PivotToggleWidget(final WidgetModel model) {
		super(model);

		checkbox = new Checkbox();
		add(checkbox);

		refreshWidget();
	}

	// -- ToggleWidget methods --

	@Override
	public Boolean getValue() {
		return checkbox.isSelected();
	}

	// -- InputWidget methods --

	@Override
	public void refreshWidget() {
		checkbox.setSelected((Boolean) getModel().getValue());
	}

}
