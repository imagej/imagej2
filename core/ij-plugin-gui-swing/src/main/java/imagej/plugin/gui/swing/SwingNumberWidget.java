package imagej.plugin.gui.swing;

import imagej.plugin.gui.NumberWidget;
import imagej.plugin.gui.WidgetStyle;

import javax.swing.JPanel;

/**
 * Swing implementation of number chooser widget.
 *
 * @author Curtis Rueden
 */
public abstract class SwingNumberWidget
	extends JPanel implements NumberWidget
{

	public static SwingNumberWidget create(final Number initialValue,
		final Number min, final Number max, final Number stepSize,
		final WidgetStyle style)
	{
		if (style == WidgetStyle.DEFAULT || style == WidgetStyle.NUMBER_SPINNER) {
			return new SwingNumberSpinnerWidget(initialValue, min, max, stepSize);
		}
		else if (style == WidgetStyle.NUMBER_SCROLL_BAR) {
			return new SwingNumberScrollBarWidget(initialValue, min, max, stepSize);
		}
		else if (style == WidgetStyle.NUMBER_SLIDER) {
			return new SwingNumberSliderWidget(initialValue, min, max, stepSize);
		}
		else {
			throw new IllegalArgumentException("Invalid widget style: " + style);
		}
	}

}
