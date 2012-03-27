
package imagej.ext.ui.awt;

import imagej.ext.module.ui.NumberWidget;
import imagej.ext.module.ui.WidgetModel;

import java.awt.Adjustable;
import java.awt.BorderLayout;
import java.awt.Scrollbar;
import java.awt.TextField;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.awt.event.TextEvent;
import java.awt.event.TextListener;

/**
 * AWT implementation of number chooser widget.
 *
 * @author Curtis Rueden
 */
public class AWTNumberWidget extends AWTInputWidget
	implements NumberWidget, AdjustmentListener, TextListener
{

	// CTR FIXME - Update the model properly, and handle non-integer values.

	private Scrollbar scrollBar;
	private TextField textField;

	public AWTNumberWidget(final WidgetModel model,
		final Number min, final Number max, final Number stepSize)
	{
		super(model);

		scrollBar = new Scrollbar(Adjustable.HORIZONTAL,
			min.intValue(), 1, min.intValue(), max.intValue() + 1);
		scrollBar.setUnitIncrement(stepSize.intValue());
		scrollBar.addAdjustmentListener(this);
		add(scrollBar, BorderLayout.CENTER);

		textField = new TextField(6);
		textField.addTextListener(this);
		add(textField, BorderLayout.EAST);

		refreshWidget();
	}

	// -- NumberWidget methods --

	@Override
	public Number getValue() {
		return scrollBar.getValue();
	}

	// -- InputWidget methods --

	@Override
	public void refreshWidget() {
 		final String value = ((Number) getModel().getValue()).toString();
 		if (textField.getText().equals(value)) return; // no change
 		textField.setText(value);
	}

	// -- AdjustmentListener methods --

	@Override
	public void adjustmentValueChanged(final AdjustmentEvent e) {
		textField.setText("" + scrollBar.getValue());
	}

	// -- TextListener methods --

	@Override
	public void textValueChanged(TextEvent e) {
		try {
			scrollBar.setValue(Integer.parseInt(textField.getText()));
		}
		catch (NumberFormatException exc) {
			// invalid number in text field; do not update scroll bar
		}
	}

}
