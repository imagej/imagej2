package imagej.plugin.gui.swing;

import java.awt.Adjustable;
import java.awt.BorderLayout;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;

import javax.swing.JScrollBar;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

/**
 * Swing implementation of number chooser widget, using a scroll bar.
 *
 * @author Curtis Rueden
 */
public class SwingNumberScrollBarWidget extends SwingNumberWidget
	implements AdjustmentListener, DocumentListener
{

	private JScrollBar scrollBar;
	private JTextField textField;

	public SwingNumberScrollBarWidget(final Number initialValue,
		final Number min, final Number max, final Number stepSize)
	{
		scrollBar = new JScrollBar(Adjustable.HORIZONTAL,
			initialValue.intValue(), 1, min.intValue(), max.intValue() + 1);
		scrollBar.setUnitIncrement(stepSize.intValue());
		scrollBar.addAdjustmentListener(this);
		add(scrollBar, BorderLayout.CENTER);

		textField = new JTextField(initialValue.toString(), 6);
		textField.getDocument().addDocumentListener(this);
		add(textField, BorderLayout.EAST);
	}

	@Override
	public Number getValue() {
		return scrollBar.getValue();
	}

	@Override
	public void adjustmentValueChanged(final AdjustmentEvent e) {
		scrollBar.removeAdjustmentListener(this);
		textField.setText("" + scrollBar.getValue());
		scrollBar.addAdjustmentListener(this);
	}

	@Override
	public void changedUpdate(final DocumentEvent e) {
		documentUpdate();
	}

	@Override
	public void insertUpdate(final DocumentEvent e) {
		documentUpdate();
	}

	@Override
	public void removeUpdate(final DocumentEvent e) {
		documentUpdate();
	}

	private void documentUpdate() {
		try {
			scrollBar.setValue(Integer.parseInt(textField.getText()));
		}
		catch (NumberFormatException e) {
			// invalid number in text field; do not update scroll bar
		}
	}

}
