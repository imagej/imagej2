
package imagej.ext.ui.pivot;

import imagej.ext.module.ui.NumberWidget;
import imagej.ext.module.ui.WidgetModel;
import imagej.ext.module.ui.WidgetStyle;
import imagej.util.Log;

/**
 * Pivot implementation of number chooser widget.
 *
 * @author Curtis Rueden
 */
public abstract class PivotNumberWidget
	extends PivotInputWidget implements NumberWidget
{

	public PivotNumberWidget(final WidgetModel model) {
		super(model);
	}

	public static PivotNumberWidget create(final WidgetModel model,
		final Number min, final Number max, final Number stepSize,
		final WidgetStyle style)
	{
		if (style == WidgetStyle.NUMBER_SCROLL_BAR) {
			return new PivotNumberScrollBarWidget(model, min, max, stepSize);
		}
		if (style == WidgetStyle.NUMBER_SLIDER) {
			return new PivotNumberSliderWidget(model, min, max);
		}
		if (style != WidgetStyle.DEFAULT && style != WidgetStyle.NUMBER_SPINNER) {
			Log.warn("Ignoring unsupported widget style: " + style);
		}
		return new PivotNumberSpinnerWidget(model, min, max, stepSize);
	}

}
