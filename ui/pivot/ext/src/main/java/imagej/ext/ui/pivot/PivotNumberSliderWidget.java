
package imagej.ext.ui.pivot;

import imagej.ext.module.ui.WidgetModel;
import imagej.util.NumberUtils;

import org.apache.pivot.wtk.Label;
import org.apache.pivot.wtk.Slider;
import org.apache.pivot.wtk.SliderValueListener;

/**
 * Pivot implementation of number chooser widget, using a slider.
 * 
 * @author Curtis Rueden
 */
public class PivotNumberSliderWidget extends PivotNumberWidget
	implements SliderValueListener
{

	private final Slider slider;
	private final Label label;

	public PivotNumberSliderWidget(final WidgetModel model,
		final Number min, final Number max)
	{
		super(model);

		slider = new Slider();
		slider.setRange(min.intValue(), max.intValue());
		add(slider);
		slider.getSliderValueListeners().add(this);

		label = new Label();
		add(label);

		refreshWidget();
	}

	// -- NumberWidget methods --

	@Override
	public Number getValue() {
		final String value = "" + slider.getValue();
		return NumberUtils.toNumber(value, getModel().getItem().getType());
	}

	// -- InputWidget methods --

	@Override
	public void refreshWidget() {
		final Number value = (Number) getModel().getValue();
		slider.setValue(value.intValue());
		label.setText(value.toString());
	}

	// -- SliderValueListener methods --

	@Override
	public void valueChanged(final Slider s, final int previousValue) {
		label.setText("" + s.getValue());
	}

}
