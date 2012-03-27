
package imagej.ext.ui.pivot;

import imagej.ext.module.ui.WidgetModel;
import imagej.util.NumberUtils;

import org.apache.pivot.wtk.Spinner;
import org.apache.pivot.wtk.content.NumericSpinnerData;

/**
 * Pivot implementation of number chooser widget, using a spinner.
 * 
 * @author Curtis Rueden
 */
public class PivotNumberSpinnerWidget extends PivotNumberWidget {

	private final Spinner spinner;

	public PivotNumberSpinnerWidget(final WidgetModel model,
		final Number min, final Number max, final Number stepSize)
	{
		super(model);

		spinner = new Spinner();
		spinner.setPreferredWidth(100);
		spinner.setSpinnerData(new NumericSpinnerData(min.intValue(),
			max.intValue(), stepSize.intValue()));
		add(spinner);

		refreshWidget();
	}

	// -- NumberWidget methods --

	@Override
	public Number getValue() {
		final String value = spinner.getSelectedItem().toString();
		return NumberUtils.toNumber(value, getModel().getItem().getType());
	}

	// -- InputWidget methods --

	@Override
	public void refreshWidget() {
		final Number value = (Number) getModel().getValue();
		spinner.setSelectedItem(value.intValue());
	}

}
