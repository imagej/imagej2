package imagej.plugin.gui.swing;

import imagej.plugin.gui.ChoiceWidget;

import java.awt.BorderLayout;

import javax.swing.JComboBox;
import javax.swing.JPanel;

/**
 * Swing implementation of multiple choice selector widget.
 *
 * @author Curtis Rueden
 */
public class SwingChoiceWidget extends JPanel implements ChoiceWidget {

	private JComboBox comboBox;

	public SwingChoiceWidget(final String initialValue, final String[] items) {
		comboBox = new JComboBox(items);
		comboBox.setSelectedItem(initialValue);
		add(comboBox, BorderLayout.CENTER);
	}

	@Override
	public String getItem() {
		return comboBox.getSelectedItem().toString();
	}

	@Override
	public int getIndex() {
		return comboBox.getSelectedIndex();
	}

}
