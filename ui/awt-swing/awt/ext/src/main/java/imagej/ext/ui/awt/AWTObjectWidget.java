
package imagej.ext.ui.awt;

import imagej.ext.module.ui.ObjectWidget;
import imagej.ext.module.ui.WidgetModel;

import java.awt.BorderLayout;
import java.awt.Choice;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

/**
 * AWT implementation of object selector widget.
 * 
 * @author Curtis Rueden
 */
public class AWTObjectWidget extends AWTInputWidget
	implements ItemListener, ObjectWidget
{

	private final Choice choice;
	private final Object[] items;

	public AWTObjectWidget(final WidgetModel model, final Object[] items) {
		super(model);
		this.items = items;

		choice = new Choice();
		for (final Object item : items) choice.add(item.toString());
		add(choice, BorderLayout.CENTER);
		choice.addItemListener(this);

		refreshWidget();
	}

	// -- InputWidget methods --

	@Override
	public Object getValue() {
		return items[choice.getSelectedIndex()];
	}

	@Override
	public void refreshWidget() {
		choice.select(getValidValue());
	}

	// -- ItemListener methods --

	@Override
	public void itemStateChanged(ItemEvent e) {
		updateModel();
	}

	// -- Helper methods --

	private String getValidValue() {
		final int itemCount = choice.getItemCount();
		if (itemCount == 0) return null; // no valid values exist

		final String value = getModel().getValue().toString();
		for (int i = 0; i < itemCount; i++) {
			final String item = choice.getItem(i);
			if (value == item) return value;
		}

		// value was invalid; reset to first choice on the list
		final String validValue = choice.getItem(0);
		// CTR FIXME should not update model in getter!
		getModel().setValue(validValue);
		return validValue;
	}

}
