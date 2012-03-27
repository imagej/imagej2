
package imagej.ext.ui.swt;

import imagej.ext.module.ui.InputWidget;
import imagej.ext.module.ui.WidgetModel;

import org.eclipse.swt.widgets.Composite;

/**
 * Common superclass for SWT-based input widgets.
 *
 * @author Curtis Rueden
 */
public abstract class SWTInputWidget extends Composite implements InputWidget {

	private WidgetModel model;

	public SWTInputWidget(final Composite parent, final WidgetModel model) {
		super(parent, 0);
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
