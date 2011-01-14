package imagej.gui;

import imagej.dataset.Dataset;

import java.io.File;

import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.border.EmptyBorder;

public class SwingInputPanel extends JPanel implements InputPanel {

	public SwingInputPanel() {
		setBorder(new EmptyBorder(15, 15, 15, 15));
		// TODO - use a better layout manager
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS)); //TEMP
	}

	@Override
	public void addMessage(String text) {
		final JLabel label = new JLabel(text);
		add(label);
	}

	@Override
	public void addNumber(String label, Number initialValue,
		Number min, Number max, Number stepSize)
	{
		@SuppressWarnings("rawtypes")
		final SpinnerNumberModel spinnerModel = new SpinnerNumberModel(
			initialValue, (Comparable) min, (Comparable) max, stepSize);
		final JSpinner spinner = new JSpinner(spinnerModel);
		addField(label, spinner);
	}

	@Override
	public Number getNextNumber() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void addToggle(String label, boolean initialValue) {
		final JCheckBox checkBox = new JCheckBox(label, initialValue);
		add(checkBox);
	}

	@Override
	public boolean getNextToggle() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void addTextField(String label, String initialValue, int columns) {
		final JTextField textField = new JTextField(initialValue, columns);
		addField(label, textField);
	}

	@Override
	public String getNextTextField() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void addChoice(String label, String[] items, String initialValue) {
		final JComboBox comboBox = new JComboBox(items);
		comboBox.setSelectedItem(initialValue);
		addField(label, comboBox);
	}

	@Override
	public String getNextChoice() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getNextChoiceIndex() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void addFile(String label, File initialValue) {
		// TODO create FileSelector widget and add here
	}

	@Override
	public File getNextFile() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void addDataset(String label, Dataset initialValue) {
		// TODO create DatasetSelector widget and add here
	}

	@Override
	public Dataset getNextDataset() {
		// TODO Auto-generated method stub
		return null;
	}

	private void addField(String label, JComponent component) {
		JPanel p = new JPanel(); //TEMP
		p.setBorder(new EmptyBorder(5, 5, 5, 5)); //TEMP
		p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS)); //TEMP
		p.add(new JLabel(label));
		p.add(component);
		add(p);
	}

}
