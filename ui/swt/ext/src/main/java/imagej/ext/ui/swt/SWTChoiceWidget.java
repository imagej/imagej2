
package imagej.ext.ui.swt;

import imagej.ext.module.ui.ChoiceWidget;
import imagej.ext.module.ui.WidgetModel;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;

/**
 * SWT implementation of multiple choice selector widget.
 * 
 * @author Curtis Rueden
 */
public class SWTChoiceWidget extends SWTInputWidget implements ChoiceWidget {

	private final Combo combo;

	public SWTChoiceWidget(final Composite parent, final WidgetModel model,
		final String[] items)
	{
		super(parent, model);

		combo = new Combo(this, SWT.DROP_DOWN);
		combo.setItems(items);

		refreshWidget();
	}

	// -- ChoiceWidget methods --

	@Override
	public String getValue() {
		return combo.getItem(combo.getSelectionIndex());
	}

	@Override
	public int getIndex() {
		return combo.getSelectionIndex();
	}

	// -- InputWidget methods --

	@Override
	public void refreshWidget() {
		final String value = getModel().getValue().toString();
		if (value.equals(getValue())) return; // no change
		for (int i = 0; i < combo.getItemCount(); i++) {
			final String item = combo.getItem(i);
			if (item.equals(value)) {
				combo.select(i);
				break;
			}
		}
	}

}
