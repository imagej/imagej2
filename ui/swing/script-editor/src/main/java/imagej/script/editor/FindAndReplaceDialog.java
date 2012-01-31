package imagej.script.editor;

import java.awt.Container;
import java.awt.Color;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.fife.ui.rtextarea.SearchEngine;

import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;

public class FindAndReplaceDialog extends JDialog implements ActionListener {
	TextEditor textEditor;
	JTextField searchField, replaceField;
	JLabel replaceLabel;
	JCheckBox matchCase, wholeWord, markAll, regex, forward;
	JButton findNext, replace, replaceAll, cancel;

	public FindAndReplaceDialog(TextEditor editor) {
		super(editor);
		textEditor = editor;

		Container root = getContentPane();
		root.setLayout(new GridBagLayout());

		JPanel text = new JPanel(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();

		c.gridx = c.gridy = 0;
		c.gridwidth = c.gridheight = 1;
		c.weightx = c.weighty = 1;
		c.ipadx = c.ipady = 1;
		c.fill = c.HORIZONTAL;
		c.anchor = c.LINE_START;
		searchField = createField("Find Next", text, c, null);
		replaceField = createField("Replace with", text, c, this);

		c.gridwidth = 4; c.gridheight = c.gridy;
		c.gridx = c.gridy = 0;
		c.weightx = c.weighty = 1;
		c.fill = c.HORIZONTAL;
		c.anchor = c.LINE_START;
		root.add(text, c);

		c.gridy = c.gridheight;
		c.gridwidth = 1; c.gridheight = 1;
		c.weightx = 0.001;
		matchCase = createCheckBox("Match Case", root, c);
		regex = createCheckBox("Regex", root, c);
		forward = createCheckBox("Search forward", root, c);
		forward.setSelected(true);
		c.gridx = 0; c.gridy++;
		markAll = createCheckBox("Mark All", root, c);
		wholeWord = createCheckBox("Whole Word", root, c);

		c.gridx = 4; c.gridy = 0;
		findNext = createButton("Find Next", root, c);
		replace = createButton("Replace", root, c);
		replaceAll = createButton("Replace All", root, c);
		cancel = createButton("Cancel", root, c);
		setResizable(true);
		pack();

		getRootPane().setDefaultButton(findNext);

		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

		KeyAdapter listener = new KeyAdapter() {
			public void keyPressed(KeyEvent e) {
				if (e.getKeyCode() == e.VK_ESCAPE)
					dispose();
			}
		};
		// TODO: handle via actionmap
		for (Component component : getContentPane().getComponents())
			component.addKeyListener(listener);
		searchField.addKeyListener(listener);
		replaceField.addKeyListener(listener);
	}

	protected RSyntaxTextArea getTextArea() {
		return textEditor.getTextArea();
	}

	public void show(boolean replace) {
		setTitle(replace ? "Replace" : "Find");
		replaceLabel.setEnabled(replace);
		replaceField.setEnabled(replace);
		replaceField.setBackground(replace ?
			searchField.getBackground() :
			getRootPane().getBackground());
		this.replace.setEnabled(replace);
		replaceAll.setEnabled(replace);

		searchField.selectAll();
		replaceField.selectAll();
		getRootPane().setDefaultButton(findNext);
		show();
	}

	private JTextField createField(String name, Container container,
			GridBagConstraints c,
			FindAndReplaceDialog replaceDialog) {
		c.weightx = 0.001;
		JLabel label = new JLabel(name);
		if (replaceDialog != null)
			replaceDialog.replaceLabel = label;
		container.add(label, c);
		c.gridx++;
		c.weightx = 1;
		JTextField field = new JTextField();
		container.add(field, c);
		c.gridx--; c.gridy++;
		return field;
	}

	private JCheckBox createCheckBox(String name, Container panel,
			GridBagConstraints c) {
		JCheckBox checkBox = new JCheckBox(name);
		checkBox.addActionListener(this);
		panel.add(checkBox, c);
		c.gridx++;
		return checkBox;
	}

	private JButton createButton(String name, Container panel,
			GridBagConstraints c) {
		JButton button = new JButton(name);
		button.addActionListener(this);
		panel.add(button, c);
		c.gridy++;
		return button;
	}

	public void actionPerformed(ActionEvent e) {
		Object source = e.getSource();
		if (source == cancel) {
			dispose();
			return;
		}
		String text = searchField.getText();
		if (text.length() == 0)
			return;
		if (source == findNext)
			searchOrReplace(false);
		else if (source == replace)
			searchOrReplace(true);
		else if (source == replaceAll) {
			int replace = SearchEngine.replaceAll(getTextArea(),
					text,
					replaceField.getText(),
					matchCase.isSelected(),
					wholeWord.isSelected(),
					regex.isSelected());
			JOptionPane.showMessageDialog(this, replace
					+ " replacements made!");
		}
	}

	public boolean searchOrReplace(boolean replace) {
		return searchOrReplace(replace, forward.isSelected());
	}

	public boolean searchOrReplace(boolean replace, boolean forward) {
		if (searchOrReplaceFromHere(replace, forward))
			return true;
		RSyntaxTextArea textArea = getTextArea();
		int caret = textArea.getCaretPosition();
		textArea.setCaretPosition(forward ?
				0 : textArea.getDocument().getLength());
		if (searchOrReplaceFromHere(replace, forward))
			return true;
		JOptionPane.showMessageDialog(this, "No match found!");
		textArea.setCaretPosition(caret);
		return false;
	}

	protected boolean searchOrReplaceFromHere(boolean replace) {
		return searchOrReplaceFromHere(forward.isSelected());
	}

	protected boolean searchOrReplaceFromHere(boolean replace, boolean forward) {
		RSyntaxTextArea textArea = getTextArea();
		return replace ?
			SearchEngine.replace(textArea, searchField.getText(),
					replaceField.getText(),
					forward,
					matchCase.isSelected(),
					wholeWord.isSelected(),
					regex.isSelected()) :
			SearchEngine.find(textArea, searchField.getText(),
					forward,
					matchCase.isSelected(),
					wholeWord.isSelected(),
					regex.isSelected());
	}

	public boolean isReplace() {
		return replace.isEnabled();
	}

	/**
	 * Sets the content of the search field.
	 *
	 * @param pattern The new content of the search field.
	 */
	public void setSearchPattern(String pattern) {
		searchField.setText(pattern);
	}
}

