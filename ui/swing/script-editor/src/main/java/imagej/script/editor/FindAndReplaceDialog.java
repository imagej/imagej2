/*
 * #%L
 * ImageJ software for multidimensional image processing and analysis.
 * %%
 * Copyright (C) 2009 - 2013 Board of Regents of the University of
 * Wisconsin-Madison, Broad Institute of MIT and Harvard, and Max Planck
 * Institute of Molecular Cell Biology and Genetics.
 * %%
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * 
 * The views and conclusions contained in the software and documentation are
 * those of the authors and should not be interpreted as representing official
 * policies, either expressed or implied, of any organization.
 * #L%
 */

package imagej.script.editor;

import java.awt.Component;
import java.awt.Container;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rtextarea.SearchContext;
import org.fife.ui.rtextarea.SearchEngine;

/**
 * TODO
 * 
 * @author Johannes Schindelin
 */
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
			int replace = SearchEngine.replaceAll(getTextArea(), getSearchContext(true));
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

	protected SearchContext getSearchContext(boolean forward) {
		SearchContext context = new SearchContext();
		context.setSearchFor(searchField.getText());
		context.setReplaceWith(replaceField.getText());
		context.setSearchForward(forward);
		context.setMatchCase(matchCase.isSelected());
		context.setWholeWord(wholeWord.isSelected());
		context.setRegularExpression(regex.isSelected());
		return context;
	}

	protected boolean searchOrReplaceFromHere(boolean replace, boolean forward) {
		RSyntaxTextArea textArea = getTextArea();
		SearchContext context = getSearchContext(forward);
		return replace ?
			SearchEngine.replace(textArea, context) :
			SearchEngine.find(textArea, context);
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

