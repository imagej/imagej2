
package imagej.ext.ui.awt;

import imagej.ext.module.ModuleException;
import imagej.ext.module.ui.AbstractInputPanel;
import imagej.ext.module.ui.InputPanel;
import imagej.ext.module.ui.WidgetModel;

import java.awt.Component;
import java.awt.Label;
import java.awt.Panel;

import net.miginfocom.swing.MigLayout;

/**
 * AWT implementation of {@link InputPanel}.
 * 
 * @author Curtis Rueden
 */
public class AWTInputPanel extends AbstractInputPanel {

	private final Panel panel;

	public AWTInputPanel() {
		panel = new Panel();
		panel.setLayout(new MigLayout("wrap 2"));
	}

	public Panel getPanel() {
		return panel;
	}

	// -- InputPanel methods --

	@Override
	public void addMessage(final String text) {
		panel.add(new Label(text), "span");
		messageCount++;
	}

	@Override
	public void addNumber(final WidgetModel model, final Number min,
		final Number max, final Number stepSize)
	{
		final AWTNumberWidget numberWidget =
			new AWTNumberWidget(model, min, max, stepSize);
		addField(model.getWidgetLabel(), numberWidget);
		numberWidgets.put(model.getItem().getName(), numberWidget);
	}

	@Override
	public void addToggle(final WidgetModel model) {
		final AWTToggleWidget toggleWidget = new AWTToggleWidget(model);
		addField(model.getWidgetLabel(), toggleWidget);
		toggleWidgets.put(model.getItem().getName(), toggleWidget);
	}

	@Override
	public void addTextField(final WidgetModel model, final int columns) {
		final AWTTextFieldWidget textFieldWidget =
			new AWTTextFieldWidget(model, columns);
		addField(model.getWidgetLabel(), textFieldWidget);
		textFieldWidgets.put(model.getItem().getName(), textFieldWidget);
	}

	@Override
	public void addChoice(final WidgetModel model, final String[] items) {
		final AWTChoiceWidget choiceWidget = new AWTChoiceWidget(model, items);
		addField(model.getWidgetLabel(), choiceWidget);
		choiceWidgets.put(model.getItem().getName(), choiceWidget);
	}

	@Override
	public void addFile(final WidgetModel model) {
		final AWTFileWidget fileWidget = new AWTFileWidget(model);
		addField(model.getWidgetLabel(), fileWidget);
		fileWidgets.put(model.getItem().getName(), fileWidget);
	}

	@Override
	public void addColor(final WidgetModel model) {
		// TODO create AWTColorWidget and add here
	}

	@Override
	public void addObject(final WidgetModel model) throws ModuleException {
		final Object[] items = getObjects(model);
		final AWTObjectWidget objectWidget = new AWTObjectWidget(model, items);
		addField(model.getWidgetLabel(), objectWidget);
		objectWidgets.put(model.getItem().getName(), objectWidget);
	}

	@Override
	public int getWidgetCount() {
		return panel.getComponentCount();
	}

	// -- Helper methods --

	private void addField(final String label, final Component component) {
		panel.add(new Label(label == null ? "" : label));
		panel.add(component);
	}

}
