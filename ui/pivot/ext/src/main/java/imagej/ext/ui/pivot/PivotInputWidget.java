
package imagej.ext.ui.pivot;

import imagej.ext.module.ui.InputWidget;
import imagej.ext.module.ui.WidgetModel;

import org.apache.pivot.wtk.BoxPane;

/**
 * Common superclass for Pivot-based input widgets.
 *
 * @author Curtis Rueden
 */
public abstract class PivotInputWidget extends BoxPane implements InputWidget {

	private WidgetModel model;

	public PivotInputWidget(final WidgetModel model) {
		this.model = model;
	}

	// -- InputWidget methods --

	@Override
	public WidgetModel getModel() {
		return model;
	}

	@Override
	public void updateModel() {
		model.setValue(getValue());
	}

}
