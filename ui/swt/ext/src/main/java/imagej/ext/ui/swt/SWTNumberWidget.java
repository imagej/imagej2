
package imagej.ext.ui.swt;

import imagej.ext.module.ui.NumberWidget;
import imagej.ext.module.ui.WidgetModel;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Slider;

/**
 * SWT implementation of number chooser widget.
 * 
 * @author Curtis Rueden
 */
public class SWTNumberWidget extends SWTInputWidget implements NumberWidget {

	private final Slider slider;

	public SWTNumberWidget(final Composite parent, final WidgetModel model,
		final Number min, final Number max, final Number stepSize)
	{
		super(parent, model);

		slider = new Slider(this, SWT.HORIZONTAL);
		slider.setValues(min.intValue(), min.intValue(), max.intValue(),
			stepSize.intValue(), stepSize.intValue(), 10 * stepSize.intValue());

		refreshWidget();
	}

	// -- NumberWidget methods --

	@Override
	public Number getValue() {
		return slider.getSelection();
	}

	// -- InputWidget methods --

	@Override
	public void refreshWidget() {
		final int value = ((Number) getModel().getValue()).intValue();
		if (slider.getSelection() == value) return; // no change
		slider.setSelection(value);
	}

}
