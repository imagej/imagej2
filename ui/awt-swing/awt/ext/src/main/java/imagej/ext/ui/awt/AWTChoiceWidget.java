
package imagej.ext.ui.awt;

import imagej.ext.module.ui.ChoiceWidget;
import imagej.ext.module.ui.WidgetModel;

import java.awt.BorderLayout;
import java.awt.Choice;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

/**
 * AWT implementation of multiple choice selector widget.
 *
 * @author Curtis Rueden
 */
public class AWTChoiceWidget extends AWTInputWidget
	implements ChoiceWidget, ItemListener
{

	private Choice choice;

	public AWTChoiceWidget(final WidgetModel model, final String[] items) {
		super(model);

		choice = new Choice();
		for (final String item : items) choice.add(item);
		choice.addItemListener(this);
		add(choice, BorderLayout.CENTER);

		refreshWidget();
	}

	// -- ChoiceWidget methods --

	@Override
	public String getValue() {
		return choice.getSelectedItem();
	}

	@Override
	public int getIndex() {
		return choice.getSelectedIndex();
	}

	// -- InputWidget methods --

	@Override
	public void refreshWidget() {
		final String value = getValidValue();
		if (value.equals(choice.getSelectedItem())) return; // no change
		choice.select(value);
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
