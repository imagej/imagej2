package imagej.ui.swing.widget;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;

import imagej.module.Module;
import imagej.plugin.Plugin;
import imagej.widget.InputWidget;
import imagej.widget.WidgetModel;
import imagej.widget.WidgetStyle;

@Plugin(type = InputWidget.class)
public class SwingTextButtonWidget extends SwingInputWidget<Void>
{
	private JButton button;
	
	@Override
	public void initialize(final WidgetModel model) {
		super.initialize(model);

		// add widgets, if specified
		final WidgetStyle style = model.getItem().getWidgetStyle();
		if (style.equals(WidgetStyle.TEXT_BUTTON)) {
			String label = model.getItem().getLabel();
			if ((label == null) || label.isEmpty()) label = "A button";
			button = new JButton(label);
			button.addActionListener(new ActionListener() {
				
				@Override
				public void actionPerformed(ActionEvent arg0) {
					Module module = model.getModule();
					model.getItem().callback(module);
				}
			});
			setToolTip(button);
			getComponent().add(button);
		}
	}

	@Override
	public boolean isCompatible(WidgetModel model) {
		return model.isType(Void.class);
	}
	
	@Override
	public Void getValue() {
		return null;
	}

	@Override
	public void refreshWidget() {
		// nothing to do
	}

	@Override
	public boolean isLabeled() {
		return false;
	}

}
