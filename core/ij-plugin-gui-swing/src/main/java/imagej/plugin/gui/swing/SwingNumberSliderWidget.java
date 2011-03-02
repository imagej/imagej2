package imagej.plugin.gui.swing;

import java.awt.BorderLayout;

import javax.swing.JSlider;

/**
 * Swing implementation of number chooser widget, using a slider.
 *
 * @author Curtis Rueden
 */
public class SwingNumberSliderWidget extends SwingNumberWidget {

	private JSlider slider;

	public SwingNumberSliderWidget(final Number initialValue,
		final Number min, final Number max, final Number stepSize)
	{
		slider = new JSlider(min.intValue(), max.intValue(),
			initialValue.intValue());
		slider.setMajorTickSpacing((max.intValue() - min.intValue()) / 4);
		slider.setMinorTickSpacing(stepSize.intValue());
		slider.setPaintLabels(true);
		slider.setPaintTicks(true);
		add(slider, BorderLayout.CENTER);
	}

	@Override
	public Number getValue() {
		return slider.getValue();
	}

}
