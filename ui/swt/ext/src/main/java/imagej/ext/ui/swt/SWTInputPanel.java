
package imagej.ext.ui.swt;

import imagej.ext.module.ModuleException;
import imagej.ext.module.ui.AbstractInputPanel;
import imagej.ext.module.ui.InputPanel;
import imagej.ext.module.ui.WidgetModel;
import net.miginfocom.swt.MigLayout;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

/**
 * SWT implementation of {@link InputPanel}.
 * 
 * @author Curtis Rueden
 */
public class SWTInputPanel extends AbstractInputPanel {

	private final Composite panel;

	public SWTInputPanel(final Composite parent) {
		panel = new Composite(parent, 0);
		panel.setLayout(new MigLayout("wrap 2"));
	}
	
	// -- SWTInputPanel methods --

	public Composite getPanel() {
		return panel;
	}

	// -- InputPanel methods --

	@Override
	public void addMessage(final String text) {
		final Label label = addLabel(text);
		label.setLayoutData("span");
		messageCount++;
	}

	@Override
	public void addNumber(final WidgetModel model, final Number min,
		final Number max, final Number stepSize)
	{
		addLabel(model.getWidgetLabel());
		final SWTNumberWidget numberWidget =
			new SWTNumberWidget(panel, model, min, max, stepSize);
		numberWidgets.put(model.getItem().getName(), numberWidget);
	}

	@Override
	public void addToggle(final WidgetModel model) {
		addLabel(model.getWidgetLabel());
		final SWTToggleWidget toggleWidget = new SWTToggleWidget(panel, model);
		toggleWidgets.put(model.getItem().getName(), toggleWidget);
	}

	@Override
	public void addTextField(final WidgetModel model, final int columns) {
		addLabel(model.getWidgetLabel());
		final SWTTextFieldWidget textFieldWidget =
			new SWTTextFieldWidget(panel, model, columns);
		textFieldWidgets.put(model.getItem().getName(), textFieldWidget);
	}

	@Override
	public void addChoice(final WidgetModel model, final String[] items) {
		addLabel(model.getWidgetLabel());
		final SWTChoiceWidget choiceWidget =
			new SWTChoiceWidget(panel, model, items);
		choiceWidgets.put(model.getItem().getName(), choiceWidget);
	}

	@Override
	public void addFile(final WidgetModel model) {
		addLabel(model.getWidgetLabel());
		final SWTFileWidget fileWidget = new SWTFileWidget(panel, model);
		fileWidgets.put(model.getItem().getName(), fileWidget);
	}

	@Override
	public void addColor(final WidgetModel model) {
		// TODO create SWTColorWidget and add here
	}

	@Override
	public void addObject(final WidgetModel model) throws ModuleException {
		addLabel(model.getWidgetLabel());
		final Object[] items = getObjects(model);
		final SWTObjectWidget objectWidget =
			new SWTObjectWidget(panel, model, items);
		objectWidgets.put(model.getItem().getName(), objectWidget);
	}

	@Override
	public int getWidgetCount() {
		return panel.getChildren().length;
	}

	// -- Helper methods --

	private Label addLabel(final String text) {
		final Label label = new Label(panel, 0);
		label.setText(text == null ? "" : text);
		return label;
	}

}
