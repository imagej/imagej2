
package imagej.ext.ui.pivot;

import imagej.ext.module.ui.WidgetModel;
import imagej.util.NumberUtils;

import org.apache.pivot.wtk.Label;
import org.apache.pivot.wtk.ScrollBar;
import org.apache.pivot.wtk.ScrollBarValueListener;

/**
 * Pivot implementation of number chooser widget, using a scroll bar.
 * 
 * @author Curtis Rueden
 */
public class PivotNumberScrollBarWidget extends PivotNumberWidget
	implements ScrollBarValueListener
{

	private final ScrollBar scrollBar;
	private final Label label;

	public PivotNumberScrollBarWidget(final WidgetModel model,
		final Number min, final Number max, final Number stepSize)
	{
		super(model);

		scrollBar = new ScrollBar();
		scrollBar.setRange(min.intValue(), max.intValue());
		scrollBar.setBlockIncrement(stepSize.intValue());
		add(scrollBar);
		scrollBar.getScrollBarValueListeners().add(this);

		label = new Label();
		add(label);

		refreshWidget();
	}

	// -- NumberWidget methods --

	@Override
	public Number getValue() {
		final String value = "" + scrollBar.getValue();
		return NumberUtils.toNumber(value, getModel().getItem().getType());
	}

	// -- InputWidget methods --

	@Override
	public void refreshWidget() {
		final Number value = (Number) getModel().getValue();
		scrollBar.setValue(value.intValue());
		label.setText(value.toString());
	}

	// -- ScrollBarValueListener methods --

	@Override
	public void valueChanged(final ScrollBar s, final int previousValue) {
		label.setText("" + scrollBar.getValue());
	}

}
