package imagej.plugin.gui.swing;

import java.awt.BorderLayout;
import java.awt.Dimension;

import imagej.plugin.gui.NumberWidget;
import imagej.plugin.gui.WidgetStyle;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

/**
 * Swing implementation of number chooser widget.
 *
 * @author Curtis Rueden
 */
public class SwingNumberWidget extends JPanel implements NumberWidget {

	private JSpinner spinner;

	public SwingNumberWidget(final Number initialValue, final Number min,
		final Number max, final Number stepSize, final WidgetStyle style)
	{
		if (style == WidgetStyle.DEFAULT || style == WidgetStyle.NUMBER_SPINNER) {
			final SpinnerNumberModel spinnerModel = new SpinnerNumberModel(
				initialValue, (Comparable<?>) min, (Comparable<?>) max, stepSize);
			spinner = new JSpinner(spinnerModel);
			limitWidth(spinner, 300);
			add(spinner, BorderLayout.CENTER);
		}
		else if (style == WidgetStyle.NUMBER_SCROLLBAR) {
			// TODO
		}
		else if (style == WidgetStyle.NUMBER_SLIDER) {
			// TODO
		}
		else {
			throw new IllegalArgumentException("Invalid widget style: " + style);
		}
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
