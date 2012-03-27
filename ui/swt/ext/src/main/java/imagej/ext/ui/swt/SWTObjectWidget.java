
package imagej.ext.ui.swt;

import imagej.ext.module.ui.ObjectWidget;
import imagej.ext.module.ui.WidgetModel;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;

/**
 * SWT implementation of multiple choice selector widget.
 * 
 * @author Curtis Rueden
 */
public class SWTObjectWidget extends SWTInputWidget implements ObjectWidget {

	private final Combo combo;
	private final Object[] items;

	public SWTObjectWidget(final Composite parent, final WidgetModel model,
		final Object[] items)
	{
		super(parent, model);
		this.items = items;

		combo = new Combo(this, SWT.DROP_DOWN);
		for (final Object item : items) combo.add(item.toString());

		refreshWidget();
	}

	// -- InputWidget methods --

	@Override
	public Object getValue() {
		return items[combo.getSelectionIndex()];
	}

	@Override
	public void refreshWidget() {
		final Object value = getModel().getValue();
		for (int i = 0; i < items.length; i++) {
			final Object item = items[i];
			if (item == value) {
				combo.select(i);
				break;
			}
		}
	}

}
