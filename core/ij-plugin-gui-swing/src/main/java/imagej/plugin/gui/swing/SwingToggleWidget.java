package imagej.plugin.gui.swing;

import imagej.plugin.gui.ToggleWidget;

import java.awt.BorderLayout;

import javax.swing.JCheckBox;
import javax.swing.JPanel;

/**
 * Swing implementation of boolean toggle widget.
 *
 * @author Curtis Rueden
 */
public class SwingToggleWidget extends JPanel implements ToggleWidget {

	private JCheckBox checkBox;

	public SwingToggleWidget(final boolean initialValue) {
		checkBox = new JCheckBox("", initialValue);
		add(checkBox, BorderLayout.CENTER);
	}

	@Override
	public boolean isSelected() {
		return checkBox.isSelected();
	}

}
