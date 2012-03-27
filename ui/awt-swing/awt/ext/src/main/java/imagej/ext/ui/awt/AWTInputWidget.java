
package imagej.ext.ui.awt;

import imagej.ext.module.ui.InputWidget;
import imagej.ext.module.ui.WidgetModel;

import java.awt.Panel;

/**
 * Common superclass for AWT-based input widgets.
 *
 * @author Curtis Rueden
 */
public abstract class AWTInputWidget extends Panel implements InputWidget {

	private WidgetModel model;

	public AWTInputWidget(final WidgetModel model) {
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
