package imagej.plugin.gui.swing;

import imagej.dataset.Dataset;
import imagej.plugin.gui.InputPanel;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

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

	/** Widget table for numbers. */
	private Map<String, JSpinner> spinners =
		new HashMap<String, JSpinner>();

	/** Widget table for toggles. */
	private Map<String, JCheckBox> checkBoxes =
		new HashMap<String, JCheckBox>();

	/** Widget table for text fields. */
	private Map<String, JTextField> textFields =
		new HashMap<String, JTextField>();

	/** Widget table for choices. */
	private Map<String, JComboBox> comboBoxes =
		new HashMap<String, JComboBox>();

	/** Widget table for files. */
	private Map<String, SwingFileSelector> fileSelectors =
		new HashMap<String, SwingFileSelector>();

	public SwingInputPanel() {
		setBorder(new EmptyBorder(15, 15, 15, 15));
		// TODO - use a better layout manager
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS)); //TEMP
	}

	@Override
	public void addMessage(String text) {
		add(new JLabel(text));
	}

	@Override
	public void addNumber(String name, String label, Number initialValue,
		Number min, Number max, Number stepSize)
	{
		@SuppressWarnings("rawtypes")
		final SpinnerNumberModel spinnerModel = new SpinnerNumberModel(
			initialValue, (Comparable) min, (Comparable) max, stepSize);
		final JSpinner spinner = new JSpinner(spinnerModel);
		addField(label, spinner);
		spinners.put(name, spinner);
	}

	@Override
	public void addToggle(String name, String label, boolean initialValue) {
		final JCheckBox checkBox = new JCheckBox(label, initialValue);
		add(checkBox);
		checkBoxes.put(name, checkBox);
	}

	@Override
	public void addTextField(String name, String label, String initialValue,
		int columns)
	{
		final JTextField textField = new JTextField(initialValue, columns);
		addField(label, textField);
		textFields.put(name, textField);
	}

	@Override
	public void addChoice(String name, String label, String initialValue,
		String[] items)
	{
		final JComboBox comboBox = new JComboBox(items);
		comboBox.setSelectedItem(initialValue);
		addField(label, comboBox);
		comboBoxes.put(name, comboBox);
	}

	@Override
	public void addFile(String name, String label, File initialValue) {
		// TODO create FileSelector widget and add here
		final SwingFileSelector fileSelector =
			new SwingFileSelector(initialValue);
		addField(label, fileSelector);
		fileSelectors.put(name, fileSelector);
	}

	@Override
	public void addDataset(String name, String label, Dataset initialValue) {
		// TODO create DatasetSelector widget and add here
	}

	@Override
	public Number getNumber(String name) {
		return (Number) spinners.get(name).getValue();
	}
	
	@Override
	public boolean getToggle(String name) {
		return checkBoxes.get(name).isSelected();
	}
	
	@Override
	public String getTextField(String name) {
		return textFields.get(name).getText();
	}
	
	@Override
	public String getChoice(String name) {
		return comboBoxes.get(name).getSelectedItem().toString();
	}
	
	@Override
	public int getChoiceIndex(String name) {
		return comboBoxes.get(name).getSelectedIndex();
	}

	@Override
	public File getFile(String name) {
		return fileSelectors.get(name).getFile();
	}
	
	@Override
	public Dataset getDataset(String name) {
		// TODO Auto-generated method stub
		return null;
	}

	private void addField(String label, JComponent component) {
		JPanel p = new JPanel(); //TEMP
		p.setBorder(new EmptyBorder(5, 5, 5, 5)); //TEMP
		p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS)); //TEMP
		if (label != null) p.add(new JLabel(label));
		p.add(component);
		add(p);
	}

}
