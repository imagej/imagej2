package imagej.plugin.gui.swing;

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.JComponent;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

/**
 * Swing implementation of number chooser widget, using a spinner.
 *
 * @author Curtis Rueden
 */
public class SwingNumberSpinnerWidget extends SwingNumberWidget {

	private JSpinner spinner;

	public SwingNumberSpinnerWidget(final Number initialValue,
		final Number min, final Number max, final Number stepSize)
	{
		final SpinnerNumberModel spinnerModel = new SpinnerNumberModel(
			initialValue, (Comparable<?>) min, (Comparable<?>) max, stepSize);
		spinner = new JSpinner(spinnerModel);
		limitWidth(spinner, 300);
		add(spinner, BorderLayout.CENTER);
	}

	@Override
	public Number getValue() {
		return (Number) spinner.getValue();
	}

	/**
	 * Limit component width to a certain maximum.
	 *
	 * This is a HACK to work around an issue with Double-based spinners
	 * that attempt to size themselves very large (presumably to match
	 * Double.MAX_VALUE).
	 */
	private void limitWidth(final JComponent c, final int maxWidth) {
		final Dimension prefSize = c.getPreferredSize();
		if (prefSize.width > maxWidth) prefSize.width = maxWidth;
		c.setMaximumSize(prefSize);
	}

}
